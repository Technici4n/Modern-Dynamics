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

import com.mojang.datafixers.util.Pair;
import java.util.*;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class PipeUnbakedModel implements UnbakedModel {
    private final ResourceLocation connectionNone;
    private final ResourceLocation connectionPipe;
    private final ResourceLocation connectionInventory;
    private final ResourceLocation straightLine;

    public PipeUnbakedModel(ResourceLocation connectionNone, ResourceLocation connectionPipe, ResourceLocation connectionInventory,
            ResourceLocation straightLine) {
        this.connectionNone = connectionNone;
        this.connectionPipe = connectionPipe;
        this.connectionInventory = connectionInventory;
        this.straightLine = straightLine;
    }

    public static BakedModel[] loadRotatedModels(ResourceLocation modelId, ModelBakery modelLoader) {
        // Load side models
        BakedModel[] models = new BakedModel[6];

        for (int i = 0; i < 6; ++i) {
            models[i] = modelLoader.bake(modelId, MdModels.PIPE_BAKE_SETTINGS[i]);
        }

        return models;
    }

    @Override
    public BakedModel bake(ModelBakery modelLoader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer,
            ResourceLocation modelId) {
        return new PipeBakedModel(
                // Load transform from the vanilla block model
                ((BlockModel) modelLoader.getModel(new ResourceLocation("block/cube"))).getTransforms(),
                (AttachmentsBakedModel) modelLoader.bake(AttachmentsUnbakedModel.ID, BlockModelRotation.X0_Y0),
                loadRotatedModels(connectionNone, modelLoader),
                loadRotatedModels(connectionPipe, modelLoader),
                loadRotatedModels(connectionInventory, modelLoader),
                loadRotatedModels(straightLine, modelLoader));
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> set) {
        List<Material> textures = new ArrayList<>();
        textures.addAll(unbakedModelGetter.apply(AttachmentsUnbakedModel.ID).getMaterials(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionNone).getMaterials(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionPipe).getMaterials(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionInventory).getMaterials(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(straightLine).getMaterials(unbakedModelGetter, set));
        return textures;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of(AttachmentsUnbakedModel.ID, connectionNone, connectionPipe, connectionInventory, straightLine);
    }
}
