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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public class PipeUnbakedModel implements IUnbakedGeometry<PipeUnbakedModel> {
    private final Material baseTexture;
    private final ResourceLocation connector;
    private final ResourceLocation straightLine;
    private final String pipeType;
    private final boolean transparent;
    private final Map<String, ResourceLocation> attachmentModels;

    public PipeUnbakedModel(String pipeType, boolean transparent, Map<String, ResourceLocation> attachmentModels) {
        this.attachmentModels = attachmentModels;
        this.baseTexture = new Material(InventoryMenu.BLOCK_ATLAS, MdId.of("pipe/" + pipeType + "/base"));
        this.connector = MdId.of("pipe/" + pipeType + "/connector");
        this.straightLine = MdId.of("pipe/" + pipeType + "/straight");
        this.pipeType = pipeType;
        this.transparent = transparent;
    }

    public String getPipeType() {
        return pipeType;
    }

    public boolean isTransparent() {
        return transparent;
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
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext context) {
        resolver.apply(connector).resolveParents(resolver);
        resolver.apply(straightLine).resolveParents(resolver);

        for (var subModel : attachmentModels.values()) {
            resolver.apply(subModel).resolveParents(resolver);
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState, ItemOverrides overrides) {

        var bakedAttachments = attachmentModels.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> loadRotatedModels(e.getValue(), baker)));

        return new PipeBakedModel(
                spriteGetter.apply(baseTexture),
                loadRotatedModels(connector, baker),
                loadRotatedModels(straightLine, baker),
                bakedAttachments,
                transparent);
    }
}
