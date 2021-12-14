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
package dev.technici4n.moderndynamics.pipe;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.technici4n.moderndynamics.network.item.ItemHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class PipeBlockEntityRenderer implements BlockEntityRenderer<PipeBlockEntity> {
    private final BlockEntityRendererProvider.Context ctx;

    public PipeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(PipeBlockEntity pipe, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        for (var host : pipe.getHosts()) {
            if (host instanceof ItemHost itemHost) {
                for (var item : itemHost.getClientTravelingItems()) {
                    matrices.pushPose();

                    Vec3 from, to;
                    double ratio;

                    if (item.traveledDistance() <= 0.5) {
                        from = findFaceMiddle(item.in().getOpposite());
                        to = CENTER;
                        ratio = item.traveledDistance() * 2;
                    } else {
                        from = CENTER;
                        to = findFaceMiddle(item.out());
                        ratio = (item.traveledDistance() - 0.5) * 2;
                    }

                    matrices.translate(
                            to.x() * ratio + from.x() * (1 - ratio),
                            to.y() * ratio + from.y() * (1 - ratio),
                            to.z() * ratio + from.z() * (1 - ratio));
                    matrices.scale(0.6f, 0.6f, 0.6f);
                    matrices.translate(0, -0.2f, 0);

                    Minecraft.getInstance().getItemRenderer().renderStatic(item.variant().toStack(), ItemTransforms.TransformType.GROUND, light,
                            overlay, matrices, vertexConsumers, 0);

                    matrices.popPose();
                }
            }
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
}
