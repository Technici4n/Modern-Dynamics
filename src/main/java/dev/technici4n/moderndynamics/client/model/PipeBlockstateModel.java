package dev.technici4n.moderndynamics.client.model;

import com.mojang.serialization.MapCodec;
import dev.technici4n.moderndynamics.model.PipeModelData;
import dev.technici4n.moderndynamics.util.MdId;
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

import java.util.List;

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
