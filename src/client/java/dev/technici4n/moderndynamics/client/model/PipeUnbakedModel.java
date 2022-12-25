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

import java.util.*;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class PipeUnbakedModel implements UnbakedModel {
    private final ResourceLocation connectionNone;
    private final ResourceLocation connectionPipe;
    private final ResourceLocation connectionInventory;

    public PipeUnbakedModel(ResourceLocation connectionNone, ResourceLocation connectionPipe, ResourceLocation connectionInventory) {
        this.connectionNone = connectionNone;
        this.connectionPipe = connectionPipe;
        this.connectionInventory = connectionInventory;
    }

    public static BakedModel[] loadRotatedModels(ResourceLocation modelId, ModelBaker modelLoader) {
        // Load side models
        BakedModel[] models = new BakedModel[6];

        for (int i = 0; i < 6; ++i) {
            models[i] = modelLoader.bake(modelId, MdModels.PIPE_BAKE_SETTINGS[i]);
        }

        return models;
    }

    @Override
    public BakedModel bake(ModelBaker modelLoader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer,
            ResourceLocation modelId) {
        return new PipeBakedModel(
                // Load transform from the vanilla block model
                ((BlockModel) modelLoader.getModel(new ResourceLocation("block/cube"))).getTransforms(),
                (AttachmentsBakedModel) modelLoader.bake(AttachmentsUnbakedModel.ID, BlockModelRotation.X0_Y0),
                loadRotatedModels(connectionNone, modelLoader),
                loadRotatedModels(connectionPipe, modelLoader),
                loadRotatedModels(connectionInventory, modelLoader));
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> unbakedModelGetter) {
        unbakedModelGetter.apply(AttachmentsUnbakedModel.ID).resolveParents(unbakedModelGetter);
        unbakedModelGetter.apply(connectionNone).resolveParents(unbakedModelGetter);
        unbakedModelGetter.apply(connectionPipe).resolveParents(unbakedModelGetter);
        unbakedModelGetter.apply(connectionInventory).resolveParents(unbakedModelGetter);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of(AttachmentsUnbakedModel.ID, connectionNone, connectionPipe, connectionInventory);
    }
}
