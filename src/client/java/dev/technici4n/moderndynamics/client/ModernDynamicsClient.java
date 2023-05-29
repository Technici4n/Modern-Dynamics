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

import dev.technici4n.moderndynamics.client.attachment.SetAttachmentUpgradesPacket;
import dev.technici4n.moderndynamics.client.ber.PipeBlockEntityRenderer;
import dev.technici4n.moderndynamics.client.model.MdModelLoader;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.gui.MdPackets;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItemSmoothing;
import dev.technici4n.moderndynamics.pipe.PipeBlock;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.pipe.PipeBoundingBoxes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public final class ModernDynamicsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MdModelLoader.init();

        for (PipeBlock pipeBlock : MdBlocks.ALL_PIPES) {
            BlockRenderLayerMap.INSTANCE.putBlock(pipeBlock, RenderType.cutout());
            var blockEntityType = pipeBlock.getBlockEntityTypeNullable();
            if (blockEntityType != null) { // some pipes don't have a block entity type (empty high tier energy pipes)
                BlockEntityRendererRegistry.register(blockEntityType, PipeBlockEntityRenderer::new);
            }
        }

        MenuScreens.register(MdMenus.ITEM_IO, ItemAttachedIoScreen::new);
        MenuScreens.register(MdMenus.FLUID_IO, FluidAttachedIoScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(MdPackets.SET_ATTACHMENT_UPGRADES, SetAttachmentUpgradesPacket.HANDLER);

        WorldRenderEvents.BLOCK_OUTLINE.register(ModernDynamicsClient::renderPipeAttachmentOutline);
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            if (!mc.isPaused()) {
                ClientTravelingItemSmoothing.onUnpausedTick();
            }
        });
        ClientPickBlockGatherCallback.EVENT.register((player, hitResult) -> {
            if (hitResult instanceof BlockHitResult bir) {
                if (player.getLevel().getBlockEntity(bir.getBlockPos()) instanceof PipeBlockEntity pipe) {
                    return pipe.overridePickBlock(hitResult);
                }
            }
            return ItemStack.EMPTY;
        });
    }

    /**
     * Highlights only the pipe attachment when it's under the mouse cursor to indicate it has special interactions.
     */
    private static boolean renderPipeAttachmentOutline(WorldRenderContext worldRenderContext,
            WorldRenderContext.BlockOutlineContext blockOutlineContext) {

        if (blockOutlineContext.blockState().getBlock() instanceof PipeBlock) {

            var be = worldRenderContext.world().getBlockEntity(blockOutlineContext.blockPos());
            if (be instanceof PipeBlockEntity pipe) {
                var pos = blockOutlineContext.blockPos();

                var hitPosInBlock = Minecraft.getInstance().hitResult.getLocation();
                hitPosInBlock = hitPosInBlock.subtract(pos.getX(), pos.getY(), pos.getZ());

                var hitSide = pipe.hitTestAttachments(hitPosInBlock);
                if (hitSide != null) {
                    LevelRenderer.renderShape(
                            worldRenderContext.matrixStack(),
                            worldRenderContext.consumers().getBuffer(RenderType.lines()),
                            PipeBoundingBoxes.CONNECTOR_SHAPES[hitSide.ordinal()],
                            (double) pos.getX() - blockOutlineContext.cameraX(),
                            (double) pos.getY() - blockOutlineContext.cameraY(),
                            (double) pos.getZ() - blockOutlineContext.cameraZ(),
                            0.0F,
                            0.0F,
                            0.0F,
                            0.4F);
                    return false;
                }
            }

        }

        return true;
    }
}
