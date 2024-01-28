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
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Allows us to load our custom jsons.
 */
public final class MdModelLoader {
    public static void init(IEventBus modEvents) {
        modEvents.addListener(ModelEvent.RegisterGeometryLoaders.class, evt -> {
            Map<String, UnbakedModel> pipeItemModels = new HashMap<>();
// TODO NEOFORGE
// TODO NEOFORGE            for (var pipe : MdBlocks.ALL_PIPES) {
// TODO NEOFORGE                pipeItemModels.put("item/" + pipe.id, new DelegatingUnbakedModel(BlockModelShaper.stateToModelLocation(pipe.defaultBlockState())));
// TODO NEOFORGE
// TODO NEOFORGE                pluginCtx.registerBlockStateResolver(pipe, ctx -> {
// TODO NEOFORGE                    var model = new PipeUnbakedModel(pipe.id, pipe.isTransparent());
// TODO NEOFORGE                    for (var state : pipe.getStateDefinition().getPossibleStates()) {
// TODO NEOFORGE                        ctx.setModel(state, model);
// TODO NEOFORGE                    }
// TODO NEOFORGE                });
// TODO NEOFORGE            }
// TODO NEOFORGE
// TODO NEOFORGE            pluginCtx.resolveModel().register(ctx -> {
// TODO NEOFORGE                if (!MdId.MOD_ID.equals(ctx.id().getNamespace())) {
// TODO NEOFORGE                    return null;
// TODO NEOFORGE                }
// TODO NEOFORGE
// TODO NEOFORGE                if (AttachmentsUnbakedModel.ID.getPath().equals(ctx.id().getPath())) {
// TODO NEOFORGE                    var modelMap = new HashMap<String, ResourceLocation>();
// TODO NEOFORGE                    for (var id : RenderedAttachment.getAttachmentIds()) {
// TODO NEOFORGE                        modelMap.put(id, MdId.of("attachment/" + id));
// TODO NEOFORGE                    }
// TODO NEOFORGE                    return new AttachmentsUnbakedModel(modelMap);
// TODO NEOFORGE                }
// TODO NEOFORGE
// TODO NEOFORGE                if (pipeItemModels.containsKey(ctx.id().getPath())) {
// TODO NEOFORGE                    return pipeItemModels.get(ctx.id().getPath());
// TODO NEOFORGE                }
// TODO NEOFORGE
// TODO NEOFORGE                return null;
// TODO NEOFORGE            });

        });
    }
}
