/*
 * Modern Dynamics
 * Copyright (C) 2021 shartte & Technici4n
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package dev.technici4n.moderndynamics.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.network.fluid.FluidHost;
import dev.technici4n.moderndynamics.network.item.ItemHost;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItemSmoothing;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.FluidRenderUtil;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jspecify.annotations.Nullable;

public class PipeBlockEntityRenderer implements BlockEntityRenderer<PipeBlockEntity, PipeRenderState> {
    private final Random random = new Random();
    private final ItemModelResolver itemModelResolver;

    public PipeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public PipeRenderState createRenderState() {
        return new PipeRenderState();
    }

    @Override
    public void extractRenderState(PipeBlockEntity blockEntity,
            PipeRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.clientSideConnections = blockEntity.getClientSideConnections();

        var level = blockEntity.getLevel();

        boolean hadItemHost = false;
        boolean hadFluidHost = false;

        for (var host : blockEntity.getHosts()) {
            if (host instanceof ItemHost itemHost) {
                hadItemHost = true;
                var clientTravelingItems = itemHost.getClientTravelingItems();

                state.travelingItems = Arrays.copyOf(state.travelingItems, clientTravelingItems.size());

                for (int i = 0; i < clientTravelingItems.size(); i++) {
                    var item = clientTravelingItems.get(i);
                    var itemState = state.travelingItems[i];
                    if (itemState == null) {
                        state.travelingItems[i] = itemState = new PipeRenderState.TravelingItemState();
                    }

                    // Compute the position within the pipe
                    Vec3 from, to;
                    double ratio;
                    var distance = Mth.frac(item.traveledDistance()) + ClientTravelingItemSmoothing.getDistanceDelta(item, partialTicks)
                            + item.speed() * partialTicks;
                    if (distance <= 0.5) {
                        from = findFaceMiddle(item.in().getOpposite());
                        to = CENTER;
                        ratio = distance * 2;
                    } else {
                        from = CENTER;
                        to = findFaceMiddle(item.out());
                        ratio = (distance - 0.5) * 2;
                    }
                    itemState.tx = (float) (to.x() * ratio + from.x() * (1 - ratio));
                    itemState.ty = (float) (to.y() * ratio + from.y() * (1 - ratio));
                    itemState.tz = (float) (to.z() * ratio + from.z() * (1 - ratio));

                    itemState.randomSeed = item.variant().hashCode() + item.id;
                    random.setSeed(itemState.randomSeed);

                    // Cool rotation
                    itemState.rotAngle = (float) ((ClientTravelingItemSmoothing.getClientTick() + partialTicks) * item.speed()
                            + random.nextFloat() * 2 * Math.PI);

                    // Render multiple items depending on stack size
                    itemState.renderCount = getRenderAmount(item.amount());

                    itemModelResolver.updateForTopItem(itemState.itemStack, item.variant().toStack(), ItemDisplayContext.GROUND, level, null, 0);
                }
            } else if (host instanceof FluidHost fluidHost) {
                var fluid = fluidHost.getVariant();
                var fill = (float) fluidHost.getAmount() / Constants.Fluids.CAPACITY;

                var renderProps = IClientFluidTypeExtensions.of(fluid.getFluid());
                var sprite = FluidRenderUtil.getStillSprite(fluid);
                if (sprite != null && fill > 1e-5) {
                    hadFluidHost = true;

                    if (state.fluid == null) {
                        state.fluid = new PipeRenderState.FluidState();
                    }
                    state.fluid.fill = fill;
                    state.fluid.sprite = sprite;
                    state.fluid.tintColor = renderProps.getTintColor(fluid.getFluid().defaultFluidState(), level, blockEntity.getBlockPos());
                }
            }
        }

        // Reset state if type changed (unlikely, but still)
        if (!hadItemHost) {
            state.travelingItems = new PipeRenderState.TravelingItemState[0];
        }
        if (!hadFluidHost) {
            state.fluid = null;
        }
    }

    @Override
    public void submit(PipeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (PipeRenderState.TravelingItemState travelingItem : state.travelingItems) {
            poseStack.pushPose();

            poseStack.translate(travelingItem.tx, travelingItem.ty, travelingItem.tz);
            poseStack.scale(0.6f, 0.6f, 0.6f);
            poseStack.translate(0, -0.15f, 0);

            // Cool rotation
            poseStack.mulPose(Axis.YP.rotation(travelingItem.rotAngle));

            // Render multiple items depending on stack size
            poseStack.translate(0, 0, -(travelingItem.renderCount - 1) * 0.1 / 2);

            var random = new SingleThreadedRandomSource(travelingItem.randomSeed);
            for (int r = 0; r < travelingItem.renderCount; ++r) {
                poseStack.pushPose();
                poseStack.translate(
                        (random.nextFloat() * 2.0f - 1.0f) * 0.02f,
                        (random.nextFloat() * 2.0f - 1.0f) * 0.02f,
                        r * 0.1);
                travelingItem.itemStack.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }

            poseStack.popPose();
        }

        if (state.fluid != null) {
            submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, buffer) -> {
                FluidPipeRendering.drawFluidInPipe(
                        state.clientSideConnections,
                        state.fluid.tintColor,
                        state.fluid.sprite,
                        state.fluid.fill,
                        pose,
                        buffer);
            });
        }
    }

    private static final Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);

    private static Vec3 findFaceMiddle(Direction face) {
        return switch (face) {
        case DOWN -> new Vec3(0.5, 0, 0.5);
        case UP -> new Vec3(0.5, 1, 0.5);
        case NORTH -> new Vec3(0.5, 0.5, 0);
        case SOUTH -> new Vec3(0.5, 0.5, 1);
        case WEST -> new Vec3(0, 0.5, 0.5);
        case EAST -> new Vec3(1, 0.5, 0.5);
        };
    }

    private static int getRenderAmount(long amount) {
        int i = 1;
        if (amount > 48) {
            i = 5;
        } else if (amount > 32) {
            i = 4;
        } else if (amount > 16) {
            i = 3;
        } else if (amount > 1) {
            i = 2;
        }
        return i;
    }
}
