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
package dev.technici4n.moderndynamics.model;

import com.mojang.datafixers.util.Pair;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class AttachmentsUnbakedModel implements UnbakedModel {
    public static final Identifier ID = MdId.of("attachments");

    private final Map<String, Identifier> attachmentModels;

    public AttachmentsUnbakedModel(Map<String, Identifier> attachmentModels) {
        this.attachmentModels = attachmentModels;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return attachmentModels.values();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        var allTextureDependencies = new ArrayList<SpriteIdentifier>();
        for (var dependentId : getModelDependencies()) {
            allTextureDependencies
                    .addAll(unbakedModelGetter.apply(dependentId).getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
        }
        return allTextureDependencies;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
            Identifier modelId) {
        var attachmentBakedModels = new HashMap<String, BakedModel[]>();

        for (var entry : attachmentModels.entrySet()) {
            attachmentBakedModels.put(entry.getKey(), PipeUnbakedModel.loadRotatedModels(entry.getValue(), loader));
        }

        return new AttachmentsBakedModel(attachmentBakedModels);
    }
}
