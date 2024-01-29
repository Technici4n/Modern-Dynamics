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

import dev.technici4n.moderndynamics.client.ber.PipeBlockEntityRenderer;
import dev.technici4n.moderndynamics.client.model.MdModelLoader;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItemSmoothing;
import dev.technici4n.moderndynamics.pipe.PipeBlock;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.pipe.PipeBoundingBoxes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

public final class ModernDynamicsClient {
    public ModernDynamicsClient(IEventBus modEvents) {
        MdModelLoader.init(modEvents);

        modEvents.addListener(EntityRenderersEvent.RegisterRenderers.class, this::registerRenderers);

        modEvents.addListener(RegisterMenuScreensEvent.class, e -> {
            e.register(MdMenus.ITEM_IO, ItemAttachedIoScreen::new);
            e.register(MdMenus.FLUID_IO, FluidAttachedIoScreen::new);
        });

        NeoForge.EVENT_BUS.addListener(TickEvent.ClientTickEvent.class, e -> {
            if (e.phase == TickEvent.Phase.START) {
                if (!Minecraft.getInstance().isPaused()) {
                    ClientTravelingItemSmoothing.onUnpausedTick();
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(RenderHighlightEvent.Block.class, ModernDynamicsClient::renderPipeAttachmentOutline);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {

        for (var pipeBlock : MdBlocks.ALL_PIPES) {
            var blockEntityType = pipeBlock.getBlockEntityTypeNullable();
            if (blockEntityType != null) { // some pipes don't have a block entity type (empty high tier energy pipes)
                evt.registerBlockEntityRenderer(blockEntityType, PipeBlockEntityRenderer::new);
            }
        }

    }

    /**
     * Highlights only the pipe attachment when it's under the mouse cursor to indicate it has special interactions.
     */
    private static void renderPipeAttachmentOutline(RenderHighlightEvent.Block evt) {
        var level = Minecraft.getInstance().level;
        var poseStack = evt.getPoseStack();
        var buffers = evt.getMultiBufferSource();
        var camera = evt.getCamera();
        if (level == null) {
            return;
        }

        var blockHitResult = evt.getTarget();
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        var pos = blockHitResult.getBlockPos();
        var blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof PipeBlock) {

            var be = level.getBlockEntity(pos);
            if (be instanceof PipeBlockEntity pipe) {
                var hitPosInBlock = Minecraft.getInstance().hitResult.getLocation();
                hitPosInBlock = hitPosInBlock.subtract(pos.getX(), pos.getY(), pos.getZ());

                var hitSide = pipe.hitTestAttachments(hitPosInBlock);
                if (hitSide != null) {
                    LevelRenderer.renderShape(
                            poseStack,
                            buffers.getBuffer(RenderType.lines()),
                            PipeBoundingBoxes.CONNECTOR_SHAPES[hitSide.ordinal()],
                            (double) pos.getX() - camera.getPosition().x,
                            (double) pos.getY() - camera.getPosition().y,
                            (double) pos.getZ() - camera.getPosition().z,
                            0.0F,
                            0.0F,
                            0.0F,
                            0.4F);
                    evt.setCanceled(true);
                }
            }
        }
    }
}
