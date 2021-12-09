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
package dev.technici4n.moderndynamics;

import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.model.MdModelLoader;
import dev.technici4n.moderndynamics.pipe.PipeBlock;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntityRenderer;
import dev.technici4n.moderndynamics.screen.AttachmentScreen;
import dev.technici4n.moderndynamics.screen.AttachmentScreenHandler;
import dev.technici4n.moderndynamics.screen.MdPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.render.RenderLayer;

public final class ModernDynamicsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MdModelLoader.init();

        for (PipeBlock pipeBlock : MdBlocks.ALL_PIPES) {
            BlockRenderLayerMap.INSTANCE.putBlock(pipeBlock, RenderLayer.getCutout());
            var blockEntityType = pipeBlock.getBlockEntityTypeNullable();
            if (blockEntityType != null) { // some pipes don't have a block entity type (empty high tier energy pipes)
                BlockEntityRendererRegistry.register(blockEntityType, PipeBlockEntityRenderer::new);
            }
        }

        ScreenRegistry.register(AttachmentScreenHandler.TYPE, AttachmentScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(MdPackets.SET_ITEM_VARIANT, MdPackets.SET_ITEM_VARIANT_HANDLER::handleS2C);
    }
}
