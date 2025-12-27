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

import static net.minecraft.client.renderer.item.BlockModelWrapper.computeExtents;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public record PipeModelSubPart(
        List<BakedQuad> quads,
        Supplier<Vector3fc[]> extents,
        ModelRenderProperties renderProperties,
        @Nullable RenderType renderType) implements BlockModelPart {

    public PipeModelSubPart(List<BakedQuad> quads, ModelRenderProperties renderProperties, @Nullable RenderType renderType) {
        this(quads, Suppliers.memoize(() -> computeExtents(quads)), renderProperties, renderType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return direction == null ? quads : List.of();
    }

    @Override
    public TriState ambientOcclusion() {
        return TriState.FALSE;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return renderProperties.particleIcon();
    }

    @Override
    public ChunkSectionLayer getRenderType(BlockState state) {
        return ChunkSectionLayer.CUTOUT;
    }

    public static PipeModelSubPart bake(ModelBaker modelBaker, Identifier id, ModelState pipeBakeSetting) {
        var baseModel = modelBaker.getModel(id);

        var baseModelTextures = baseModel.getTopTextureSlots();
        List<BakedQuad> baseModelQuads = baseModel
                .bakeTopGeometry(baseModelTextures, modelBaker, pipeBakeSetting).getAll();

        var modelRenderProperties = ModelRenderProperties.fromResolvedModel(modelBaker, baseModel, baseModelTextures);
        var renderTypeGroup = baseModel.getTopAdditionalProperties().getOptional(NeoForgeModelProperties.RENDER_TYPE);
        var renderType = renderTypeGroup == null ? null : renderTypeGroup.entityBlock();

        return new PipeModelSubPart(baseModelQuads, modelRenderProperties, renderType);
    }

    public void applyToLayer(ItemStackRenderState.LayerRenderState layer, ItemDisplayContext context) {
        layer.setExtents(extents);
        renderProperties.applyToLayer(layer, context);
        layer.prepareQuadList().addAll(quads);
        if (renderType != null) {
            layer.setRenderType(renderType);
        } else {
            layer.setRenderType(Sheets.translucentBlockItemSheet());
        }
    }
}
