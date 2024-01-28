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
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Allows us to load our custom jsons.
 */
public final class MdModelLoader {
    public static void init(IEventBus modEvents) {
        modEvents.addListener(FMLClientSetupEvent.class, evt -> {
            for (var pipe : MdBlocks.ALL_PIPES) {
                BuiltInModelHooks.addBuiltInModel(MdId.of("item/" + pipe.id), new DelegatingUnbakedModel(BlockModelShaper.stateToModelLocation(pipe.defaultBlockState())));

                var model = new PipeUnbakedModel(pipe.id, pipe.isTransparent());
                for (var state : pipe.getStateDefinition().getPossibleStates()) {
                    var stateId = BlockModelShaper.stateToModelLocation(state);
                    BuiltInModelHooks.addBuiltInModel(stateId, model);
                }
            }

            var modelMap = new HashMap<String, ResourceLocation>();
            for (var id : RenderedAttachment.getAttachmentIds()) {
                modelMap.put(id, MdId.of("attachment/" + id));
            }
            BuiltInModelHooks.addBuiltInModel(
                    AttachmentsUnbakedModel.ID,
                    new AttachmentsUnbakedModel(modelMap)
            );
        });
    }
}

final class DelegatingUnbakedModel implements UnbakedModel {
    private final ResourceLocation delegate;
    private final List<ResourceLocation> dependencies;

    public DelegatingUnbakedModel(ResourceLocation delegate) {
        this.delegate = delegate;
        this.dependencies = List.of(delegate);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return dependencies;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker pBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location) {
        return pBaker.bake(delegate, state, spriteGetter);
    }
}
