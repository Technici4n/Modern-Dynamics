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

import dev.technici4n.moderndynamics.attachment.RenderedAttachment;
import dev.technici4n.moderndynamics.client.ber.PipeBlockEntityRenderer;
import dev.technici4n.moderndynamics.client.model.PipeItemModel;
import dev.technici4n.moderndynamics.client.model.PipeBlockstateModel;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItemSmoothing;
import dev.technici4n.moderndynamics.packets.MdPackets;
import dev.technici4n.moderndynamics.packets.SetAttachmentUpgrades;
import dev.technici4n.moderndynamics.packets.SetFluidVariant;
import dev.technici4n.moderndynamics.packets.SetItemVariant;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

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
            e.register(SetItemVariant.TYPE, MdPackets.SET_ITEM_VARIANT_HANDLER);
            e.register(SetFluidVariant.TYPE, MdPackets.SET_FLUID_VARIANT_HANDLER);
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
        // TODO 26.1var level = Minecraft.getInstance().level;
        // TODO 26.1var poseStack = evt.getPoseStack();
        // TODO 26.1var buffers = evt.getMultiBufferSource();
        // TODO 26.1var camera = evt.getCamera();
        // TODO 26.1if (level == null) {
        // TODO 26.1    return;
        // TODO 26.1}
// TODO 26.1
        // TODO 26.1var blockHitResult = evt.getTarget();
        // TODO 26.1if (blockHitResult.getType() != HitResult.Type.BLOCK) {
        // TODO 26.1    return;
        // TODO 26.1}
// TODO 26.1
        // TODO 26.1var pos = blockHitResult.getBlockPos();
        // TODO 26.1var blockState = level.getBlockState(pos);
        // TODO 26.1if (blockState.getBlock() instanceof PipeBlock) {
// TODO 26.1
        // TODO 26.1    var be = level.getBlockEntity(pos);
        // TODO 26.1    if (be instanceof PipeBlockEntity pipe) {
        // TODO 26.1        var hitPosInBlock = Minecraft.getInstance().hitResult.getLocation();
        // TODO 26.1        hitPosInBlock = hitPosInBlock.subtract(pos.getX(), pos.getY(), pos.getZ());
// TODO 26.1
        // TODO 26.1        var hitSide = pipe.hitTestAttachments(hitPosInBlock);
        // TODO 26.1        if (hitSide != null) {
        // TODO 26.1            LevelRenderer.renderShape(
        // TODO 26.1                    poseStack,
        // TODO 26.1                    buffers.getBuffer(RenderType.lines()),
        // TODO 26.1                    PipeBoundingBoxes.CONNECTOR_SHAPES[hitSide.ordinal()],
        // TODO 26.1                    (double) pos.getX() - camera.getPosition().x,
        // TODO 26.1                    (double) pos.getY() - camera.getPosition().y,
        // TODO 26.1                    (double) pos.getZ() - camera.getPosition().z,
        // TODO 26.1                    0.0F,
        // TODO 26.1                    0.0F,
        // TODO 26.1                    0.0F,
        // TODO 26.1                    0.4F);
        // TODO 26.1            evt.setCanceled(true);
        // TODO 26.1        }
        // TODO 26.1    }
    }
}
