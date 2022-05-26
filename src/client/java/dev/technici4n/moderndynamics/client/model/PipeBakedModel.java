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

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.model.PipeModelData;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PipeBakedModel implements BakedModel, FabricBakedModel {
    private final ItemTransforms transformation;
    private final AttachmentsBakedModel attachments;
    private final BakedModel[] connectionNone;
    private final BakedModel[] connectionPipe;
    private final BakedModel[] connectionInventory;

    public PipeBakedModel(ItemTransforms transformation, AttachmentsBakedModel attachments, BakedModel[] connectionNone,
            BakedModel[] connectionPipe, BakedModel[] connectionInventory) {
        this.transformation = transformation;
        this.attachments = attachments;
        this.connectionNone = connectionNone;
        this.connectionPipe = connectionPipe;
        this.connectionInventory = connectionInventory;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
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
        return connectionNone[0].getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transformation;
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
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        var attachmentView = (RenderAttachedBlockView) blockView;
        var pipeData = Objects.requireNonNullElse((PipeModelData) attachmentView.getBlockEntityRenderAttachment(pos), PipeModelData.DEFAULT);
        drawPipe(pipeData, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        drawPipe(new PipeModelData((byte) 12, (byte) 0, new AttachedAttachment[6]), context);
    }

    private void drawPipe(PipeModelData data, RenderContext context) {
        int connectionsPipe = data.pipeConnections();
        int connectionsInventory = data.inventoryConnections();
        connectionsPipe |= connectionsInventory;

        int connectionsNone = ~connectionsPipe;

        appendBitmasked(context.fallbackConsumer(), connectionsNone, connectionNone);
        appendBitmasked(context.fallbackConsumer(), connectionsPipe, connectionPipe);
        appendBitmasked(context.fallbackConsumer(), connectionsInventory, connectionInventory);
        for (int i = 0; i < 6; ++i) {
            var attachment = data.attachments()[i];
            if (attachment != null) {
                context.fallbackConsumer().accept(connectionPipe[i]);
                context.fallbackConsumer().accept(attachments.attachmentModels.get(attachment.getModelId())[i]);
            }
        }
    }
}
