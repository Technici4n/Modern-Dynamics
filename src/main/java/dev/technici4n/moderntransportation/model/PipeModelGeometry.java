package dev.technici4n.moderntransportation.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.function.Function;

public class PipeModelGeometry implements IModelGeometry<PipeModelGeometry> {
    private final SpriteIdentifier texture;
    private final Identifier centerModelId;
    private final Identifier sideModelId;

    public PipeModelGeometry(Identifier texture, Identifier centerModelId, Identifier sideModelId) {
        this.texture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, texture);
        this.centerModelId = centerModelId;
        this.sideModelId = sideModelId;
    }

    @Override
    public BakedModel bake(IModelConfiguration iModelConfiguration, ModelLoader modelLoader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings arg2, ModelOverrideList arg3, Identifier arg4) {
        // Particle sprite
        Sprite sprite = textureGetter.apply(texture);
        // Load center model
        BakedModel centerModel = modelLoader.getBakedModel(centerModelId, ModelRotation.X0_Y0, textureGetter);
        // Load side models
        BakedModel[] connectionModels = new BakedModel[6];

        for (int i = 0; i < 6; ++i) {
            connectionModels[i] = modelLoader.getBakedModel(sideModelId, MTModels.PIPE_BAKE_SETTINGS[i], textureGetter);
        }

        return new PipeBakedModel(sprite, connectionModels, centerModel);
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration iModelConfiguration, Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> set) {
        List<SpriteIdentifier> textures = new ArrayList<>();
        textures.add(texture);
        textures.addAll(unbakedModelGetter.apply(centerModelId).getTextureDependencies(unbakedModelGetter, set));
        textures.addAll(unbakedModelGetter.apply(sideModelId).getTextureDependencies(unbakedModelGetter, set));
        return textures;
    }
}
