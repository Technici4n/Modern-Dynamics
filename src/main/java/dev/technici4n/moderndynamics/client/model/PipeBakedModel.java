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

import static dev.technici4n.moderndynamics.pipe.PipeBoundingBoxes.CORE_END;
import static dev.technici4n.moderndynamics.pipe.PipeBoundingBoxes.CORE_START;

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.client.GeometryHelper;
import dev.technici4n.moderndynamics.model.PipeModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.technici4n.moderndynamics.thirdparty.fabric.Mesh;
import dev.technici4n.moderndynamics.thirdparty.fabric.MeshBuilderImpl;
import dev.technici4n.moderndynamics.thirdparty.fabric.ModelHelper;
import dev.technici4n.moderndynamics.thirdparty.fabric.MutableQuadView;
import dev.technici4n.moderndynamics.thirdparty.fabric.QuadEmitter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PipeBakedModel implements BakedModel {
    private static final ChunkRenderTypeSet CUTOUT_RENDER_TYPES = ChunkRenderTypeSet.of(RenderType.cutout());
    private final TextureAtlasSprite baseSprite;
    private final Mesh[] baseMeshes;
    private final BakedModel[] connectorModels;
    private final BakedModel[] straightLineModels;
    private final AttachmentsBakedModel attachments;
    private final boolean transparent;

    public PipeBakedModel(TextureAtlasSprite baseSprite, BakedModel[] connectorModels, BakedModel[] straightLineModels,
            AttachmentsBakedModel attachments, boolean transparent) {
        this.baseSprite = baseSprite;
        this.connectorModels = connectorModels;
        this.straightLineModels = straightLineModels;
        this.attachments = attachments;

        this.transparent = transparent;
        this.baseMeshes = new Mesh[1 << 6];

        var meshBuilder = new MeshBuilderImpl();

        for (int connections = 0; connections < baseMeshes.length; ++connections) {
            var qe = meshBuilder.getEmitter();

            for (int i = 0; i < 6; ++i) {
                var dir = Direction.from3DDataValue(i);

                // Center quads (if there is no connection on that side)
                if ((connections & (1 << i)) == 0) {
                    baseQuad(qe, dir, CORE_START, CORE_START, CORE_END, CORE_END, CORE_START);
                }

                // Connection quads
                if ((connections & (1 << GeometryHelper.FACE_RIGHT[i].getOpposite().get3DDataValue())) > 0) {
                    baseQuad(qe, dir, 0, CORE_START, CORE_START, CORE_END, CORE_START);
                }
                if ((connections & (1 << GeometryHelper.FACE_RIGHT[i].get3DDataValue())) > 0) {
                    baseQuad(qe, dir, CORE_END, CORE_START, 1, CORE_END, CORE_START);
                }
                if ((connections & (1 << GeometryHelper.FACE_UP[i].getOpposite().get3DDataValue())) > 0) {
                    baseQuad(qe, dir, CORE_START, 0, CORE_END, CORE_START, CORE_START);
                }
                if ((connections & (1 << GeometryHelper.FACE_UP[i].get3DDataValue())) > 0) {
                    baseQuad(qe, dir, CORE_START, CORE_END, CORE_END, 1, CORE_START);
                }
            }

            baseMeshes[connections] = meshBuilder.build();
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseSprite;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    // Pipes in item form only connect to NORTH and SOUTH.
    private static final PipeModelData ITEM_DATA = new PipeModelData((byte) 12, (byte) 12, new AttachedAttachment[6]);
    private static final ModelData ITEM_MODEL_DATA = ModelData.builder().with(PipeModelData.PIPE_DATA, ITEM_DATA).build();

    private void appendBitmasked(Consumer<BakedModel> consumer, int mask, BakedModel[] models) {
        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) > 0) {
                consumer.accept(models[i]);
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
        return getQuads(pState, pDirection, pRandom, ITEM_MODEL_DATA, null);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        var pipeData = data.get(PipeModelData.PIPE_DATA);
        if (pipeData == null) {
            pipeData = PipeModelData.DEFAULT;
        }

        // TODO ITEM_DATA ...?
        int connectionsPipe = pipeData.pipeConnections();
        int connectionsInventory = pipeData.inventoryConnections();
        int connections = connectionsInventory | connectionsPipe;

        // Also render connections to attachments
        for (int i = 0; i < 6; ++i) {
            var attachment = pipeData.attachments()[i];
            if (attachment != null) {
                connections |= 1 << i;
            }
        }

        var result = new ArrayList<BakedQuad>();

        // Render base connections
        if (connections == 3 || connections == 12 || connections == 48) {
            // Straight line!
            BakedModel straightModel;
            if (connections == 3) {
                straightModel = straightLineModels[0];
            } else if (connections == 12) {
                straightModel = straightLineModels[2];
            } else {
                straightModel = straightLineModels[4];
            }
            result.addAll(straightModel.getQuads(state, side, rand, data, renderType));
        } else {
            result.addAll(baseMeshes[connections].toBakedBlockQuads());
        }

        // Render connectors
        Consumer<BakedModel> fallbackConsumer = bakedModel -> {
            result.addAll(bakedModel.getQuads(state, side, rand, data, renderType));
        };
        appendBitmasked(fallbackConsumer, connectionsInventory, connectorModels);

        // Render attachments
        for (int i = 0; i < 6; ++i) {
            var attachment = pipeData.attachments()[i];
            if (attachment != null) {
                fallbackConsumer.accept(attachments.attachmentModels.get(attachment.modelId())[i]);
            }
        }

        return result;
    }

    private void baseQuad(QuadEmitter qe, Direction side, float left, float bottom, float right, float top, float depth) {
        // Forward face
        qe.square(side, left, bottom, right, top, depth);
        qe.spriteBake(baseSprite, MutableQuadView.BAKE_LOCK_UV);
        qe.color(-1, -1, -1, -1);
        qe.emit();
        // Backward face
        if (transparent) {
            switch (side) {
            case UP, DOWN -> qe.square(side.getOpposite(), left, 1 - top, right, 1 - bottom, 1 - depth);
            default -> qe.square(side.getOpposite(), 1 - right, bottom, 1 - left, top, 1 - depth);
            }
            qe.spriteBake(baseSprite, MutableQuadView.BAKE_LOCK_UV);
            qe.color(-1, -1, -1, -1);
            qe.emit();
        }
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return CUTOUT_RENDER_TYPES;
    }
}
