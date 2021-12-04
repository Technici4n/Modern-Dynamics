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
package dev.technici4n.moderntransportation.model;

import com.mojang.datafixers.util.Pair;
import java.util.*;
import java.util.function.Function;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public class PipeUnbakedModel implements UnbakedModel {
    private final Identifier connectionNone;
    private final Identifier connectionPipe;
    private final Identifier connectionInventory;

    public PipeUnbakedModel(Identifier connectionNone, Identifier connectionPipe, Identifier connectionInventory) {
        this.connectionNone = connectionNone;
        this.connectionPipe = connectionPipe;
        this.connectionInventory = connectionInventory;
    }

    public static BakedModel[] loadRotatedModels(Identifier modelId, ModelLoader modelLoader) {
        // Load side models
        BakedModel[] models = new BakedModel[6];

        for (int i = 0; i < 6; ++i) {
            models[i] = modelLoader.bake(modelId, MtModels.PIPE_BAKE_SETTINGS[i]);
        }

        return models;
    }

    @Override
    public BakedModel bake(ModelLoader modelLoader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
            Identifier modelId) {
        return new PipeBakedModel(
                // Load transform from the vanilla block model
                ((JsonUnbakedModel) modelLoader.getOrLoadModel(new Identifier("block/cube"))).getTransformations(),
                (AttachmentsBakedModel) modelLoader.bake(AttachmentsUnbakedModel.ID, ModelRotation.X0_Y0),
                loadRotatedModels(connectionNone, modelLoader),
                loadRotatedModels(connectionPipe, modelLoader),
                loadRotatedModels(connectionInventory, modelLoader));
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> set) {
        List<SpriteIdentifier> textures = new ArrayList<>();
        textures.addAll(unbakedModelGetter.apply(AttachmentsUnbakedModel.ID).getTextureDependencies(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionNone).getTextureDependencies(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionPipe).getTextureDependencies(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionInventory).getTextureDependencies(unbakedModelGetter, set));
        return textures;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(AttachmentsUnbakedModel.ID, connectionNone, connectionPipe, connectionInventory);
    }
}
