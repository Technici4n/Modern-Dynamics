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
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AttachmentsUnbakedModel implements UnbakedModel {
    public static final ResourceLocation ID = MdId.of("attachments");

    private final Map<String, ResourceLocation> attachmentModels;

    public AttachmentsUnbakedModel(Map<String, ResourceLocation> attachmentModels) {
        this.attachmentModels = attachmentModels;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return attachmentModels.values();
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        var allTextureDependencies = new ArrayList<Material>();
        for (var dependentId : getDependencies()) {
            allTextureDependencies
                    .addAll(unbakedModelGetter.apply(dependentId).getMaterials(unbakedModelGetter, unresolvedTextureReferences));
        }
        return allTextureDependencies;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer,
            ResourceLocation modelId) {
        var attachmentBakedModels = new HashMap<String, BakedModel[]>();

        for (var entry : attachmentModels.entrySet()) {
            attachmentBakedModels.put(entry.getKey(), PipeUnbakedModel.loadRotatedModels(entry.getValue(), loader));
        }

        return new AttachmentsBakedModel(attachmentBakedModels);
    }
}
