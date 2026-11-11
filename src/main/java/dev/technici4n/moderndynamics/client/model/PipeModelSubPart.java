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

import static net.minecraft.client.renderer.item.CuboidItemModelWrapper.computeExtents;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public record PipeModelSubPart(
        List<BakedQuad> quads,
        Supplier<Vector3fc[]> extents,
        ModelRenderProperties renderProperties,
        @BakedQuad.MaterialFlags int materialFlags) implements BlockStateModelPart {

    public PipeModelSubPart(List<BakedQuad> quads, ModelRenderProperties renderProperties) {
        this(quads, Suppliers.memoize(() -> computeExtents(quads)), renderProperties, computeMaterialFlags(quads));
    }

    private static int computeMaterialFlags(List<BakedQuad> quads) {
        int flags = 0;
        for (var quad : quads) {
            flags |= quad.materialInfo().flags();
        }
        return flags;
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
    public Material.Baked particleMaterial() {
        return renderProperties.particleMaterial();
    }

    public static PipeModelSubPart bake(ModelBaker modelBaker, Identifier id, ModelState pipeBakeSetting) {
        var baseModel = modelBaker.getModel(id);

        var baseModelTextures = baseModel.getTopTextureSlots();
        List<BakedQuad> baseModelQuads = baseModel
                .bakeTopGeometry(baseModelTextures, modelBaker, pipeBakeSetting).getAll();

        var modelRenderProperties = ModelRenderProperties.fromResolvedModel(modelBaker, baseModel, baseModelTextures);

        return new PipeModelSubPart(baseModelQuads, modelRenderProperties);
    }

    public void applyToLayer(ItemStackRenderState.LayerRenderState layer, ItemDisplayContext context) {
        layer.setExtents(extents);
        renderProperties.applyToLayer(layer, context);
        layer.prepareQuadList().addAll(quads);
    }
}
