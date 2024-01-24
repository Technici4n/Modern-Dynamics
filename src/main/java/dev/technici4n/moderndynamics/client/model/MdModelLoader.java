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
package dev.technici4n.moderndynamics.client.model;

import dev.technici4n.moderndynamics.attachment.RenderedAttachment;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.model.loading.v1.DelegatingUnbakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Allows us to load our custom jsons.
 */
public final class MdModelLoader {
    public static void init() {
        ModelLoadingPlugin.register(pluginCtx -> {
            Map<String, UnbakedModel> pipeItemModels = new HashMap<>();

            for (var pipe : MdBlocks.ALL_PIPES) {
                pipeItemModels.put("item/" + pipe.id, new DelegatingUnbakedModel(BlockModelShaper.stateToModelLocation(pipe.defaultBlockState())));

                pluginCtx.registerBlockStateResolver(pipe, ctx -> {
                    var model = new PipeUnbakedModel(pipe.id, pipe.isTransparent());
                    for (var state : pipe.getStateDefinition().getPossibleStates()) {
                        ctx.setModel(state, model);
                    }
                });
            }

            pluginCtx.resolveModel().register(ctx -> {
                if (!MdId.MOD_ID.equals(ctx.id().getNamespace())) {
                    return null;
                }

                if (AttachmentsUnbakedModel.ID.getPath().equals(ctx.id().getPath())) {
                    var modelMap = new HashMap<String, ResourceLocation>();
                    for (var id : RenderedAttachment.getAttachmentIds()) {
                        modelMap.put(id, MdId.of("attachment/" + id));
                    }
                    return new AttachmentsUnbakedModel(modelMap);
                }

                if (pipeItemModels.containsKey(ctx.id().getPath())) {
                    return pipeItemModels.get(ctx.id().getPath());
                }

                return null;
            });

        });
    }
}
