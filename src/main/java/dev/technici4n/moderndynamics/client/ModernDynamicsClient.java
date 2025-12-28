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
package dev.technici4n.moderndynamics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.technici4n.moderndynamics.attachment.RenderedAttachment;
import dev.technici4n.moderndynamics.client.ber.PipeBlockEntityRenderer;
import dev.technici4n.moderndynamics.client.model.PipeBlockstateModel;
import dev.technici4n.moderndynamics.client.model.PipeItemModel;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItemSmoothing;
import dev.technici4n.moderndynamics.packets.MdPackets;
import dev.technici4n.moderndynamics.packets.SetAttachmentUpgrades;
import dev.technici4n.moderndynamics.packets.SetFluidResource;
import dev.technici4n.moderndynamics.packets.SetItemResource;
import dev.technici4n.moderndynamics.pipe.PipeBlock;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.pipe.PipeBoundingBoxes;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = MdId.MOD_ID, dist = Dist.CLIENT)
public final class ModernDynamicsClient {
    public ModernDynamicsClient(IEventBus modEvents) {
        modEvents.addListener(EntityRenderersEvent.RegisterRenderers.class, this::registerRenderers);

        modEvents.addListener(RegisterMenuScreensEvent.class, e -> {
            e.register(MdMenus.ITEM_IO, ItemAttachedIoScreen::new);
            e.register(MdMenus.FLUID_IO, FluidAttachedIoScreen::new);
        });

        modEvents.addListener(RegisterRenderPipelinesEvent.class, e -> {
            e.registerPipeline(FluidAttachedIoScreen.GUI_TEXTURED_NOBLEND);
        });

        modEvents.addListener(RegisterBlockStateModels.class, e -> {
            e.registerModel(PipeBlockstateModel.Unbaked.ID, PipeBlockstateModel.Unbaked.MAP_CODEC);
        });

        modEvents.addListener(RegisterItemModelsEvent.class, e -> {
            e.register(PipeItemModel.Unbaked.ID, PipeItemModel.Unbaked.MAP_CODEC);
        });

        modEvents.addListener(RegisterClientPayloadHandlersEvent.class, e -> {
            e.register(SetAttachmentUpgrades.TYPE, SetAttachmentUpgrades.HANDLER);
            e.register(SetItemResource.TYPE, MdPackets.SET_ITEM_RESOURCE_HANDLER);
            e.register(SetFluidResource.TYPE, MdPackets.SET_FLUID_RESOURCE_HANDLER);
            e.register(MdPackets.SET_FILTER_MODE, MdPackets.SET_FILTER_MODE_HANDLER);
            e.register(MdPackets.SET_FILTER_DAMAGE, MdPackets.SET_FILTER_DAMAGE_HANDLER);
            e.register(MdPackets.SET_FILTER_NBT, MdPackets.SET_FILTER_NBT_HANDLER);
            e.register(MdPackets.SET_FILTER_MOD, MdPackets.SET_FILTER_MOD_HANDLER);
            e.register(MdPackets.SET_FILTER_SIMILAR, MdPackets.SET_FILTER_SIMILAR_HANDLER);
            e.register(MdPackets.SET_ROUTING_MODE, MdPackets.SET_ROUTING_MODE_HANDLER);
            e.register(MdPackets.SET_OVERSENDING_MODE, MdPackets.SET_OVERSENDING_MODE_HANDLER);
            e.register(MdPackets.SET_REDSTONE_MODE, MdPackets.SET_REDSTONE_MODE_HANDLER);
            e.register(MdPackets.SET_MAX_ITEMS_IN_INVENTORY, MdPackets.SET_MAX_ITEMS_IN_INVENTORY_HANDLER);
            e.register(MdPackets.SET_MAX_ITEMS_EXTRACTED, MdPackets.SET_MAX_ITEMS_EXTRACTED_HANDLER);
        });

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, e -> {
            if (!Minecraft.getInstance().isPaused()) {
                ClientTravelingItemSmoothing.onUnpausedTick();
            }
        });
        NeoForge.EVENT_BUS.addListener(ExtractBlockOutlineRenderStateEvent.class, ModernDynamicsClient::renderPipeAttachmentOutline);
    }

    public static Map<String, Identifier> getAttachmentModels() {
        var modelMap = new HashMap<String, Identifier>();
        for (var id : RenderedAttachment.getAttachmentIds()) {
            modelMap.put(id, MdId.of("attachment/" + id));
        }
        return modelMap;
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {

        for (var pipeBlock : MdBlocks.getAllPipes()) {
            var blockEntityType = pipeBlock.getBlockEntityTypeNullable();
            if (blockEntityType != null) { // some pipes don't have a block entity type (empty high tier energy pipes)
                evt.registerBlockEntityRenderer(blockEntityType, PipeBlockEntityRenderer::new);
            }
        }

    }

    /**
     * Highlights only the pipe attachment when it's under the mouse cursor to indicate it has special interactions.
     */
    private static void renderPipeAttachmentOutline(ExtractBlockOutlineRenderStateEvent evt) {
        var level = Minecraft.getInstance().level;
        var camera = evt.getCamera();
        if (level == null) {
            return;
        }

        var blockHitResult = evt.getHitResult();
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        if (evt.getBlockState().getBlock() instanceof PipeBlock) {
            var pos = evt.getBlockPos();
            var be = level.getBlockEntity(pos);
            if (be instanceof PipeBlockEntity pipe) {
                var hitPosInBlock = Minecraft.getInstance().hitResult.getLocation();
                hitPosInBlock = hitPosInBlock.subtract(pos.getX(), pos.getY(), pos.getZ());

                var hitSide = pipe.hitTestAttachments(hitPosInBlock);
                if (hitSide != null) {
                    evt.getCustomRenderers().add(new OutlineRenderer(
                            hitSide,
                            (pos.getX() - camera.position().x),
                            (pos.getY() - camera.position().y),
                            (pos.getZ() - camera.position().z)));
                }
            }
        }
    }

    record OutlineRenderer(Direction hitSide, double x, double y, double z) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState renderState,
                MultiBufferSource.BufferSource buffer,
                PoseStack poseStack,
                boolean translucentPass,
                LevelRenderState levelRenderState) {
            ShapeRenderer.renderShape(
                    poseStack,
                    buffer.getBuffer(RenderTypes.lines()),
                    PipeBoundingBoxes.CONNECTOR_SHAPES[hitSide.ordinal()],
                    x,
                    y,
                    z,
                    ARGB.black(0.4F),
                    7);
            return true;
        }
    }
}
