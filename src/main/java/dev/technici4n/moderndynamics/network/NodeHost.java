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
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.DropHelper;
import dev.technici4n.moderndynamics.util.SerializationHelper;
import java.util.EnumSet;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * A node host is what gives its behavior to a {@link NetworkNode}.
 */
public abstract class NodeHost {
    protected final PipeBlockEntity pipe;
    private final DefaultedList<ItemStack> attachments = DefaultedList.ofSize(6, ItemStack.EMPTY);
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

    public final void setAttachment(Direction side, ItemStack stack) {
        attachments.set(side.getId(), stack);
        scheduleUpdate();
    }

    public final ItemStack getAttachment(Direction side) {
        return attachments.get(side.getId());
    }

    public abstract boolean acceptsAttachment(AttachmentItem attachment, ItemStack stack);

    @SuppressWarnings("ConstantConditions")
    public boolean isTicking() {
        return ((ServerWorld) pipe.getWorld()).shouldTickEntity(pipe.getPos());
    }

    /**
     * Return true if this node can connect to the target (adjacent) node as part of a network.
     */
    public boolean canConnectTo(Direction connectionDirection, NodeHost adjacentHost) {
        return (pipe.connectionBlacklist & (1 << connectionDirection.getId())) == 0;
    }

    /**
     * Set the list of current connections.
     */
    public final void setConnections(EnumSet<Direction> connections) {
        pipeConnections = SerializationHelper.directionsToMask(connections);
        pipe.sync();
    }

    @SuppressWarnings("rawtypes")
    public abstract NetworkManager getManager();

    @SuppressWarnings("unchecked")
    public void addSelf() {
        getManager().addNode((ServerWorld) pipe.getWorld(), pipe.getPos(), this);
    }

    @SuppressWarnings("unchecked")
    public void removeSelf() {
        getManager().removeNode((ServerWorld) pipe.getWorld(), pipe.getPos(), this);
    }

    @SuppressWarnings("unchecked")
    public final void refreshSelf() {
        getManager().refreshNode((ServerWorld) pipe.getWorld(), pipe.getPos(), this);
    }

    @Nullable
    public abstract Object getApiInstance(BlockApiLookup<?, Direction> lookup, Direction side);

    @SuppressWarnings("unchecked")
    @Nullable
    protected final <H extends NodeHost, C extends NetworkCache<H, C>> NetworkNode<H, C> findNode() {
        // TODO: not the best unchecked cast...
        return getManager().findNode((ServerWorld) pipe.getWorld(), pipe.getPos());
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

    public void writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag, attachments);
    }

    public void readNbt(NbtCompound tag) {
        Inventories.readNbt(tag, attachments);
    }

    public void writeClientNbt(NbtCompound tag) {
    }

    public void readClientNbt(NbtCompound tag) {
    }

    public void onRemoved() {
        for (var attachment : attachments) {
            DropHelper.dropStack(pipe, attachment);
        }
        attachments.clear();
    }
}
