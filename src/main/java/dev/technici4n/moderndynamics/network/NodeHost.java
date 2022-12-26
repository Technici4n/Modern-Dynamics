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
package dev.technici4n.moderndynamics.network;

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.SerializationHelper;
import java.util.EnumSet;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * A node host is what gives its behavior to a {@link NetworkNode}.
 */
public abstract class NodeHost {
    protected final PipeBlockEntity pipe;
    private final AttachedAttachment[] attachments = new AttachedAttachment[6];
    /**
     * Current connections to adjacent pipes.
     */
    public byte pipeConnections = 0;
    public int inventoryConnections = 0;
    /**
     * True if the host needs an update.
     * The update is done by the network when the host is in a ticking chunk.
     */
    private boolean needsUpdate = true;

    protected NodeHost(PipeBlockEntity pipe) {
        this.pipe = pipe;
    }

    public Level getLevel() {
        return pipe.getLevel();
    }

    public BlockPos getPos() {
        return pipe.getBlockPos();
    }

    @Nullable
    public final AttachedAttachment removeAttachment(Direction side) {
        var attachment = this.attachments[side.get3DDataValue()];
        if (attachment != null) {
            this.attachments[side.get3DDataValue()] = null;
            update();
            return attachment;
        }
        return null;
    }

    public final void setAttachment(Direction side, AttachmentItem item, CompoundTag data) {
        var current = attachments[side.get3DDataValue()];
        if (current != null && current.getItem() == item) {
            if (current.update(data)) {
                scheduleUpdate();
            }
        } else {
            attachments[side.get3DDataValue()] = item.createAttached(this, data);
            scheduleUpdate();
        }
    }

    @Nullable
    public final AttachedAttachment getAttachment(Direction side) {
        return attachments[side.get3DDataValue()];
    }

    public abstract boolean acceptsAttachment(AttachmentItem attachment, ItemStack stack);

    @SuppressWarnings("ConstantConditions")
    public final boolean isTicking() {
        return ((ServerLevel) pipe.getLevel()).isPositionEntityTicking(pipe.getBlockPos());
    }

    /**
     * Return true if this node can connect to the target (adjacent) node as part of a network.
     */
    public boolean canConnectTo(Direction connectionDirection, NodeHost adjacentHost) {
        return (pipe.connectionBlacklist & (1 << connectionDirection.get3DDataValue())) == 0;
    }

    /**
     * Set the list of current connections.
     */
    public final void setConnections(EnumSet<Direction> connections) {
        pipeConnections = SerializationHelper.directionsToMask(connections);
        pipe.sync();
    }

    public void onConnectedTo(NodeHost other) {
    }

    public void onConnectionRejectedTo(Direction direction, NodeHost other) {
    }

    @SuppressWarnings("rawtypes")
    public abstract NetworkManager getManager();

    @SuppressWarnings("unchecked")
    public void addSelf() {
        getManager().addNode((ServerLevel) pipe.getLevel(), pipe.getBlockPos(), this);
    }

    @SuppressWarnings("unchecked")
    public void removeSelf() {
        getManager().removeNode((ServerLevel) pipe.getLevel(), pipe.getBlockPos(), this);
    }

    @SuppressWarnings("unchecked")
    public final void refreshSelf() {
        getManager().refreshNode((ServerLevel) pipe.getLevel(), pipe.getBlockPos(), this);
    }

    @Nullable
    public abstract Object getApiInstance(BlockApiLookup<?, Direction> lookup, Direction side);

    @SuppressWarnings("unchecked")
    @Nullable
    protected final <H extends NodeHost, C extends NetworkCache<H, C>> NetworkNode<H, C> findNode() {
        // TODO: not the best unchecked cast...
        return getManager().findNode((ServerLevel) pipe.getLevel(), pipe.getBlockPos());
    }

    public final void separateNetwork() {
        @Nullable
        NetworkNode<?, ?> node = findNode();

        if (node != null && node.getHost() == this) {
            node.getNetworkCache().separate();
        }
    }

    protected final void update() {
        if (needsUpdate) {
            needsUpdate = false;
            doUpdate();
        }
    }

    protected void doUpdate() {
    }

    /**
     * Schedule an update.
     */
    @SuppressWarnings({ "rawtypes" })
    public final void scheduleUpdate() {
        if (!needsUpdate) {
            needsUpdate = true;
            @Nullable
            NetworkNode node = findNode();

            if (node != null) {
                node.getNetworkCache().scheduleHostUpdate(this);
            }
        }
    }

    public final boolean needsUpdate() {
        return needsUpdate;
    }

    public final PipeBlockEntity getPipe() {
        return pipe;
    }

    private boolean hasAttachments() {
        for (var attachment : attachments) {
            if (attachment != null) {
                return true;
            }
        }
        return false;
    }

    public void writeNbt(CompoundTag tag) {
        // Only write a sub-tag if any attachments exist
        if (hasAttachments()) {
            var attachmentTags = new ListTag();
            for (var attachment : attachments) {
                var attachmentTag = new CompoundTag();
                if (attachment != null) {
                    var id = BuiltInRegistries.ITEM.getKey(attachment.getItem());
                    attachmentTag.putString("#i", id.toString());
                    attachment.writeConfigTag(attachmentTag);
                }
                attachmentTags.add(attachmentTag);
            }
            tag.put("attachments", attachmentTags);
        }
    }

    public void readNbt(CompoundTag tag) {
        if (tag.contains("attachments", Tag.TAG_LIST)) {
            var attachmentTags = tag.getList("attachments", Tag.TAG_COMPOUND);
            for (int i = 0; i < attachments.length; i++) {
                this.attachments[i] = null;

                if (i < attachmentTags.size()) {
                    var attachmentTag = attachmentTags.getCompound(i);
                    var item = BuiltInRegistries.ITEM.get(new ResourceLocation(attachmentTag.getString("#i")));
                    if ((item instanceof AttachmentItem attachmentItem)) {
                        this.attachments[i] = attachmentItem.createAttached(this, attachmentTag);
                    }
                }
            }
        }
    }

    @MustBeInvokedByOverriders
    public void writeClientNbt(CompoundTag tag) {
    }

    @MustBeInvokedByOverriders
    public void readClientNbt(CompoundTag tag) {
    }

    public void clientTick() {
    }

    public void onRemoved() {
    }

    public void addDrops(List<ItemStack> drops) {
        for (Direction side : Direction.values()) {
            var attachment = getAttachment(side);
            if (attachment != null) {
                drops.addAll(attachment.getDrops());
            }
        }

    }
}
