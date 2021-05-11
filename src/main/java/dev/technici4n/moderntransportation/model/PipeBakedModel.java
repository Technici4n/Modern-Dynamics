package dev.technici4n.moderntransportation.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PipeBakedModel implements BakedModel {
    private final ModelTransformation transformation;
    private final BakedModel[] connectionNone;
    private final BakedModel[] connectionPipe;
    private final BakedModel[] connectionInventory;

    public PipeBakedModel(ModelTransformation transformation, BakedModel[] connectionNone, BakedModel[] connectionPipe, BakedModel[] connectionInventory) {
        this.transformation = transformation;
        this.connectionNone = connectionNone;
        this.connectionPipe = connectionPipe;
        this.connectionInventory = connectionInventory;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        if (state == null) {
            // item render
            return getQuads(null, side, rand, EmptyModelData.INSTANCE);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
        return connectionNone[0].getSprite();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ModelTransformation getTransformation() {
        return transformation;
    }

    private static final byte ITEM_CONNECTIONS = 12;

    private static <T> T getOr(IModelData data, ModelProperty<T> prop, T defaultValue) {
        T result = data.getData(prop);
        return result == null ? defaultValue : result;
    }

    private void appendBitmasked(List<BakedQuad> quads, int mask, BakedModel[] models, @Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) > 0) {
                quads.addAll(models[i].getQuads(state, side, rand, extraData));
            }
        }
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        // TODO: is the returned list safe to cache? would increase performance a lot...
        List<BakedQuad> quads = new ArrayList<>();

        int connectionsPipe = getOr(extraData, MTModels.CONNECTIONS_PIPE, (byte) 0);
        int connectionsInventory = getOr(extraData, MTModels.CONNECTIONS_INVENTORY, (byte) 0);
        connectionsPipe |= connectionsInventory;

        if (state == null) {
            // Item form
            // only connect to NORTH and SOUTH
            connectionsPipe = 12;
            connectionsInventory = 12;
        }

        int connectionsNone = ~connectionsPipe;

        appendBitmasked(quads, connectionsNone, connectionNone, state, side, rand, extraData);
        appendBitmasked(quads, connectionsPipe, connectionPipe, state, side, rand, extraData);
        appendBitmasked(quads, connectionsInventory, connectionInventory, state, side, rand, extraData);

        return quads;
    }
}
