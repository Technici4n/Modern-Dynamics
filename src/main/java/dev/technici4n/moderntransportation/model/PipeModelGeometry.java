package dev.technici4n.moderntransportation.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.function.Function;

public class PipeModelGeometry implements IModelGeometry<PipeModelGeometry> {
    private final Identifier connectionNone;
    private final Identifier connectionPipe;
    private final Identifier connectionInventory;

    public PipeModelGeometry(Identifier connectionNone, Identifier connectionPipe, Identifier connectionInventory) {
        this.connectionNone = connectionNone;
        this.connectionPipe = connectionPipe;
        this.connectionInventory = connectionInventory;
    }

    private static BakedModel[] loadRotatedModels(Identifier modelId, ModelLoader modelLoader, Function<SpriteIdentifier, Sprite> textureGetter) {
        // Load side models
        BakedModel[] models = new BakedModel[6];

        for (int i = 0; i < 6; ++i) {
            models[i] = modelLoader.getBakedModel(modelId, MTModels.PIPE_BAKE_SETTINGS[i], textureGetter);
        }

        return models;
    }

    @Override
    public BakedModel bake(IModelConfiguration iModelConfiguration, ModelLoader modelLoader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings arg2, ModelOverrideList arg3, Identifier arg4) {
        // Load transform from the vanilla block model
        ModelTransformation transformation = ((JsonUnbakedModel) modelLoader.getOrLoadModel(new Identifier("block/cube"))).getTransformations();

        return new PipeBakedModel(
                transformation,
                loadRotatedModels(connectionNone, modelLoader, textureGetter),
                loadRotatedModels(connectionPipe, modelLoader, textureGetter),
                loadRotatedModels(connectionInventory, modelLoader, textureGetter)
        );
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration iModelConfiguration, Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> set) {
        List<SpriteIdentifier> textures = new ArrayList<>();
        textures.addAll(unbakedModelGetter.apply(connectionNone).getTextureDependencies(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionPipe).getTextureDependencies(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(connectionInventory).getTextureDependencies(unbakedModelGetter, set));
        return textures;
    }
}
