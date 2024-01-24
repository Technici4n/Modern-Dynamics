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

import dev.technici4n.moderndynamics.util.MdId;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

public class PipeUnbakedModel implements UnbakedModel {
    private final Material baseTexture;
    private final ResourceLocation connector;
    private final ResourceLocation straightLine;
    private final boolean transparent;

    public PipeUnbakedModel(String pipeType, boolean transparent) {
        this.baseTexture = new Material(InventoryMenu.BLOCK_ATLAS, MdId.of("pipe/" + pipeType + "/base"));
        this.connector = MdId.of("pipe/" + pipeType + "/connector");
        this.straightLine = MdId.of("pipe/" + pipeType + "/straight");
        this.transparent = transparent;
    }

    public static BakedModel[] loadRotatedModels(ResourceLocation modelId, ModelBaker baker) {
        // Load side models
        BakedModel[] models = new BakedModel[6];

        for (int i = 0; i < 6; ++i) {
            models[i] = baker.bake(modelId, MdModels.PIPE_BAKE_SETTINGS[i]);
        }

        return models;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of(connector, straightLine, AttachmentsUnbakedModel.ID);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        resolver.apply(connector).resolveParents(resolver);
        resolver.apply(straightLine).resolveParents(resolver);
        resolver.apply(AttachmentsUnbakedModel.ID).resolveParents(resolver);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform,
            ResourceLocation location) {
        return new PipeBakedModel(
                spriteGetter.apply(baseTexture),
                loadRotatedModels(connector, baker),
                loadRotatedModels(straightLine, baker),
                (AttachmentsBakedModel) WrapperBakedModel.unwrap(baker.bake(AttachmentsUnbakedModel.ID, BlockModelRotation.X0_Y0)),
                transparent);
    }
}
