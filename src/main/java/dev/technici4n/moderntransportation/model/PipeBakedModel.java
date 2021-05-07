package dev.technici4n.moderntransportation.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PipeBakedModel implements BakedModel {
    private final Sprite sprite;
    private final BakedModel[] connectionModels;
    private final BakedModel centerModel;

    public PipeBakedModel(Sprite sprite, BakedModel[] connectionModels, BakedModel centerModel) {
        this.sprite = sprite;
        this.connectionModels = connectionModels;
        this.centerModel = centerModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    private static final byte DEFAULT_CONNECTIONS = 12;

    private byte getConnections(@Nullable BlockState state, @Nullable IModelData data) {
        if (state == null || data == null) {
            return DEFAULT_CONNECTIONS;
        } else {
            @Nullable
            Byte nullableConnections = data.getData(MTModels.CONNECTIONS);

            return nullableConnections == null ? 0 : nullableConnections;
        }
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        // TODO: is the returned list safe to cache? would increase performance a lot...
        byte connections = getConnections(state, extraData);

        List<BakedQuad> quads = new ArrayList<>(centerModel.getQuads(state, side, rand, extraData));

        for (int i = 0; i < 6; ++i) {
            if ((connections & (1 << i)) > 0) {
                quads.addAll(connectionModels[i].getQuads(state, side, rand, extraData));
            }
        }


        return quads;
    }
}
