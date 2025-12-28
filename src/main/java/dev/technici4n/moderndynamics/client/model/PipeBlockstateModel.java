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

import com.mojang.serialization.MapCodec;
import dev.technici4n.moderndynamics.model.PipeModelData;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record PipeBlockstateModel(PipeModelGenerator generator) implements DynamicBlockStateModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        var pipeData = level.getModelData(pos).get(PipeModelData.PIPE_DATA);
        if (pipeData == null) {
            pipeData = PipeModelData.DEFAULT;
        }

        generator.collectParts(pipeData, parts::add);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return generator.particleIcon();
    }

    public record Unbaked(PipeModelGenerator.Unbaked generator) implements CustomUnbakedBlockStateModel {
        public static final Identifier ID = MdId.of("pipe");
        public static final MapCodec<Unbaked> MAP_CODEC = PipeModelGenerator.Unbaked.MAP_CODEC.xmap(Unbaked::new, Unbaked::generator);

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new PipeBlockstateModel(generator.bake(baker));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            generator.resolveDependencies(resolver);
        }

        @Override
        public MapCodec<PipeBlockstateModel.Unbaked> codec() {
            return MAP_CODEC;
        }
    }
}
