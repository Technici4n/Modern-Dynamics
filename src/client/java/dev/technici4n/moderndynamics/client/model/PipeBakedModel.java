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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PipeBakedModel implements BakedModel, FabricBakedModel {
    private final TextureAtlasSprite baseSprite;
    private final Mesh[] baseMeshes;
    private final BakedModel[] connectorModels;
    private final BakedModel[] straightLineModels;
    private final AttachmentsBakedModel attachments;

    public PipeBakedModel(TextureAtlasSprite baseSprite, BakedModel[] connectorModels, BakedModel[] straightLineModels,
            AttachmentsBakedModel attachments) {
        this.baseSprite = baseSprite;
        this.connectorModels = connectorModels;
        this.straightLineModels = straightLineModels;
        this.attachments = attachments;

        this.baseMeshes = new Mesh[1 << 6];

        var meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();

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
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return List.of();
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

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    // Pipes in item form only connect to NORTH and SOUTH.
    private static final PipeModelData ITEM_DATA = new PipeModelData((byte) 12, (byte) 12, new AttachedAttachment[6]);

    private void appendBitmasked(Consumer<BakedModel> consumer, int mask, BakedModel[] models) {
        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) > 0) {
                consumer.accept(models[i]);
            }
        }
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier,
            RenderContext context) {
        var attachmentView = (RenderAttachedBlockView) blockView;
        var pipeData = Objects.requireNonNullElse((PipeModelData) attachmentView.getBlockEntityRenderAttachment(pos), PipeModelData.DEFAULT);
        drawPipe(pipeData, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        drawPipe(ITEM_DATA, context);
    }

    private void baseQuad(QuadEmitter qe, Direction side, float left, float bottom, float right, float top, float depth) {
        // Forward face
        qe.square(side, left, bottom, right, top, depth);
        qe.spriteBake(0, baseSprite, MutableQuadView.BAKE_LOCK_UV);
        qe.spriteColor(0, -1, -1, -1, -1);
        qe.emit();
        // Backward face
        switch (side) {
        case UP, DOWN -> qe.square(side.getOpposite(), left, 1 - top, right, 1 - bottom, 1 - depth);
        default -> qe.square(side.getOpposite(), 1 - right, bottom, 1 - left, top, 1 - depth);
        }
        qe.spriteBake(0, baseSprite, MutableQuadView.BAKE_LOCK_UV);
        qe.spriteColor(0, -1, -1, -1, -1);
        qe.emit();
    }

    private void drawPipe(PipeModelData data, RenderContext context) {
        int connectionsPipe = data.pipeConnections();
        int connectionsInventory = data.inventoryConnections();

        if (connectionsInventory == 0 && (connectionsPipe == 3 || connectionsPipe == 12 || connectionsPipe == 48)) {
            // Straight line!
            if (connectionsPipe == 3) {
                context.fallbackConsumer().accept(straightLineModels[0]);
            } else if (connectionsPipe == 12) {
                context.fallbackConsumer().accept(straightLineModels[2]);
            } else {
                context.fallbackConsumer().accept(straightLineModels[4]);
            }
        } else {
            // Render base connections
            {
                int connections = connectionsInventory | connectionsPipe;

                // Also render connections to attachments
                for (int i = 0; i < 6; ++i) {
                    var attachment = data.attachments()[i];
                    if (attachment != null) {
                        connections |= 1 << i;
                    }
                }

                context.meshConsumer().accept(baseMeshes[connections]);
            }

            // Render connectors
            appendBitmasked(context.fallbackConsumer(), connectionsInventory, connectorModels);
        }

        // Render attachments
        for (int i = 0; i < 6; ++i) {
            var attachment = data.attachments()[i];
            if (attachment != null) {
                context.fallbackConsumer().accept(attachments.attachmentModels.get(attachment.modelId())[i]);
            }
        }
    }
}
