/*
 * Modern Transportation
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
package dev.technici4n.moderntransportation;

import dev.technici4n.moderntransportation.block.PipeBlock;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.model.MtModelLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public final class ModernTransportationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MtModelLoader.init();

        for (PipeBlock pipeBlock : MtBlocks.ALL_PIPES) {
            BlockRenderLayerMap.INSTANCE.putBlock(pipeBlock, RenderLayer.getCutout());
        }
    }
}
