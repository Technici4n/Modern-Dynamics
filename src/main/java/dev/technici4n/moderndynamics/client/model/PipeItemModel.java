package dev.technici4n.moderndynamics.client.model;

import com.mojang.serialization.MapCodec;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record PipeItemModel(PipeModelGenerator generator) implements ItemModel {

    @Override
    public void update(ItemStackRenderState output,
                       ItemStack item,
                       ItemModelResolver resolver,
                       ItemDisplayContext displayContext,
                       @Nullable ClientLevel level,
                       @Nullable ItemOwner owner,
                       int seed) {

        generator.collectParts(PipeModelGenerator.ITEM_DATA, part -> {
            var layer = output.newLayer();
            part.applyToLayer(layer, displayContext);
        });

        output.appendModelIdentityElement(this);
    }

    public record Unbaked(PipeModelGenerator.Unbaked generator) implements ItemModel.Unbaked {
        public static final Identifier ID = MdId.of("pipe");
        public static final MapCodec<Unbaked> MAP_CODEC = PipeModelGenerator.Unbaked.MAP_CODEC.xmap(Unbaked::new, Unbaked::generator);

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            return new PipeItemModel(generator.bake(context.blockModelBaker()));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            generator.resolveDependencies(resolver);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
