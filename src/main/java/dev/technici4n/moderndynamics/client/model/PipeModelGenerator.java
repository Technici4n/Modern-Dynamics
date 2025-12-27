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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.client.GeometryHelper;
import dev.technici4n.moderndynamics.client.ModernDynamicsClient;
import dev.technici4n.moderndynamics.model.PipeModelData;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.technici4n.moderndynamics.pipe.PipeBoundingBoxes.CORE_END;
import static dev.technici4n.moderndynamics.pipe.PipeBoundingBoxes.CORE_START;

public class PipeModelGenerator {
    private final TextureAtlasSprite baseSprite;
    private final PipeModelSubPart[] baseMeshes;
    private final PipeModelSubPart[] connectorModels;
    private final PipeModelSubPart[] straightLineModels;
    private final Map<String, PipeModelSubPart[]> attachments;
    private final boolean transparent;

    public PipeModelGenerator(TextureAtlasSprite baseSprite,
                              PipeModelSubPart[] connectorModels,
                              PipeModelSubPart[] straightLineModels,
                              Map<String, PipeModelSubPart[]> attachments,
                              boolean transparent) {
        this.baseSprite = baseSprite;
        this.connectorModels = connectorModels;
        this.straightLineModels = straightLineModels;
        this.attachments = attachments;

        this.transparent = transparent;
        this.baseMeshes = new PipeModelSubPart[1 << 6];

        for (int connections = 0; connections < baseMeshes.length; ++connections) {
            var qe = new ArrayList<BakedQuad>();

            for (int i = 0; i < 6; ++i) {
                var dir = Direction.from3DDataValue(i);

                // Center quads (if there is no connection on that side)
                if ((connections & (1 << i)) == 0) {
                    baseQuad(qe::add, dir, CORE_START, CORE_START, CORE_END, CORE_END, CORE_START);
                }

                // Connection quads
                if ((connections & (1 << GeometryHelper.FACE_RIGHT[i].getOpposite().get3DDataValue())) > 0) {
                    baseQuad(qe::add, dir, 0, CORE_START, CORE_START, CORE_END, CORE_START);
                }
                if ((connections & (1 << GeometryHelper.FACE_RIGHT[i].get3DDataValue())) > 0) {
                    baseQuad(qe::add, dir, CORE_END, CORE_START, 1, CORE_END, CORE_START);
                }
                if ((connections & (1 << GeometryHelper.FACE_UP[i].getOpposite().get3DDataValue())) > 0) {
                    baseQuad(qe::add, dir, CORE_START, 0, CORE_END, CORE_START, CORE_START);
                }
                if ((connections & (1 << GeometryHelper.FACE_UP[i].get3DDataValue())) > 0) {
                    baseQuad(qe::add, dir, CORE_START, CORE_END, CORE_END, 1, CORE_START);
                }
            }

            baseMeshes[connections] = new PipeModelSubPart(
                    qe,
                    new ModelRenderProperties(true, baseSprite, ItemTransforms.NO_TRANSFORMS),
                    Sheets.cutoutBlockSheet()
            );
        }
    }

    // Pipes in item form only connect to NORTH and SOUTH.
    public static final PipeModelData ITEM_DATA = new PipeModelData((byte) 12, (byte) 12, new AttachedAttachment[6]);

    private void appendBitmasked(Consumer<PipeModelSubPart> consumer, int mask, PipeModelSubPart[] models) {
        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) > 0) {
                consumer.accept(models[i]);
            }
        }
    }

    public void collectParts(PipeModelData pipeData, Consumer<PipeModelSubPart> consumer) {
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

        // Render base connections
        if (connections == 3 || connections == 12 || connections == 48) {
            // Straight line!
            PipeModelSubPart straightModel;
            if (connections == 3) {
                straightModel = straightLineModels[0];
            } else if (connections == 12) {
                straightModel = straightLineModels[2];
            } else {
                straightModel = straightLineModels[4];
            }
            consumer.accept(straightModel);
        } else {
            consumer.accept(baseMeshes[connections]);
        }

        // Render connectors
        appendBitmasked(consumer, connectionsInventory, connectorModels);

        // Render attachments
        for (int i = 0; i < 6; ++i) {
            var attachment = pipeData.attachments()[i];
            if (attachment != null) {
                consumer.accept(attachments.get(attachment.modelId())[i]);
            }
        }
    }

    private void baseQuad(Consumer<BakedQuad> quadOut, Direction side, float left, float bottom, float right, float top, float depth) {
        // Forward face
        QuadBakingVertexConsumer qe = new QuadBakingVertexConsumer();
        square(qe, side, left, bottom, right, top, depth);
        qe.setSprite(baseSprite);
        qe.setColor(-1);
        quadOut.accept(qe.bakeQuad());
        // Backward face
        if (transparent) {
            switch (side) {
                case UP, DOWN -> square(qe, side.getOpposite(), left, 1 - top, right, 1 - bottom, 1 - depth);
                default -> square(qe, side.getOpposite(), 1 - right, bottom, 1 - left, top, 1 - depth);
            }
            quadOut.accept(qe.bakeQuad());
        }
    }

    /**
     * Tolerance for determining if the depth parameter to {@link #square(QuadBakingVertexConsumer, Direction, float, float, float, float, float)}
     * is effectively zero - meaning the face is a cull face.
     */
    float CULL_FACE_EPSILON = 0.00001f;

    /**
     * Helper method to assign vertex coordinates for a square aligned with the given face. Ensures that vertex order is
     * consistent with vanilla convention. (Incorrect order can lead to bad AO lighting unless enhanced lighting logic
     * is available/enabled.)
     *
     * <p>
     * Square will be parallel to the given face and coplanar with the face (and culled if the face is occluded) if the
     * depth parameter is approximately zero. See {@link #CULL_FACE_EPSILON}.
     *
     * <p>
     * All coordinates should be normalized (0-1).
     */
    void square(QuadBakingVertexConsumer qe, Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        var ul = baseSprite.getU(left);
        var ur = baseSprite.getU(right);
        var vt = baseSprite.getV(top);
        var vb = baseSprite.getV(bottom);

        qe.setDirection(nominalFace);
        switch (nominalFace) {
            case UP:
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;

            case DOWN:
                qe.addVertex(left, depth, top).setUv(ul, vt);
                qe.addVertex(left, depth, bottom).setUv(ul, vb);
                qe.addVertex(right, depth, bottom).setUv(ur, vb);
                qe.addVertex(right, depth, top).setUv(ur, vt);
                break;

            case EAST:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case WEST:
                qe.addVertex(depth, top, left).setUv(ul, vt);
                qe.addVertex(depth, bottom, left).setUv(ul, vb);
                qe.addVertex(depth, bottom, right).setUv(ur, vb);
                qe.addVertex(depth, top, right).setUv(ur, vt);
                break;

            case SOUTH:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case NORTH:
                qe.addVertex(1 - left, top, depth).setUv(ul, vt);
                qe.addVertex(1 - left, bottom, depth).setUv(ul, vb);
                qe.addVertex(1 - right, bottom, depth).setUv(ur, vb);
                qe.addVertex(1 - right, top, depth).setUv(ur, vt);
                break;
        }
    }

    public TextureAtlasSprite particleIcon() {
        return baseSprite;
    }

    public record Unbaked(String pipeType, boolean transparent) {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.fieldOf("pipeType").forGetter(Unbaked::pipeType),
                Codec.BOOL.fieldOf("transparent").forGetter(Unbaked::transparent)
        ).apply(builder, Unbaked::new));

        private Material getBaseTexture() {
            return new Material(TextureAtlas.LOCATION_BLOCKS, MdId.of("pipe/" + pipeType + "/base"));
        }

        private Identifier getConnectorModel() {
            return MdId.of("pipe/" + pipeType + "/connector");
        }

        private Identifier getStraightModel() {
            return MdId.of("pipe/" + pipeType + "/straight");
        }

        public PipeModelGenerator bake(ModelBaker modelBaker) {
            var bakedAttachments = ModernDynamicsClient.getAttachmentModels().entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> loadRotatedModels(e.getValue(), modelBaker)));
            var baseSprite = modelBaker.sprites().get(getBaseTexture(), () -> "pipe item");

            return new PipeModelGenerator(
                    baseSprite,
                    loadRotatedModels(getConnectorModel(), modelBaker),
                    loadRotatedModels(getStraightModel(), modelBaker),
                    bakedAttachments,
                    transparent
            );
        }

        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(getConnectorModel());
            resolver.markDependency(getStraightModel());

            for (var subModel : ModernDynamicsClient.getAttachmentModels().values()) {
                resolver.markDependency(subModel);
            }
        }

        public static PipeModelSubPart[] loadRotatedModels(Identifier modelId, ModelBaker baker) {
            // Load side models
            PipeModelSubPart[] models = new PipeModelSubPart[6];

            for (int i = 0; i < 6; ++i) {
                models[i] = PipeModelSubPart.bake(baker, modelId, MdModels.PIPE_BAKE_SETTINGS[i]);
            }

            return models;
        }
    }
}
