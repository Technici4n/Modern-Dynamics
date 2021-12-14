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
package dev.technici4n.moderndynamics.network.item;

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.NetworkNode;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.TickHelper;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.DropHelper;
import dev.technici4n.moderndynamics.util.SerializationHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class ItemHost extends NodeHost {
    private static final NetworkManager<ItemHost, ItemCache> MANAGER = NetworkManager.get(ItemCache.class, ItemCache::new);
    private final List<TravelingItem> travelingItems = new ArrayList<>();
    private final List<ClientTravelingItem> clientTravelingItems = new ArrayList<>();
    private final long[] lastOperationTick = new long[6];

    public ItemHost(PipeBlockEntity pipe) {
        super(pipe);
    }

    @Override
    public boolean acceptsAttachment(AttachmentItem attachment, ItemStack stack) {
        return true;
    }

    @Override
    public NetworkManager<ItemHost, ItemCache> getManager() {
        return MANAGER;
    }

    @Override
    @Nullable
    public Object getApiInstance(BlockApiLookup<?, Direction> lookup, Direction side) {
        if (lookup == ItemStorage.SIDED && allowItemConnection(side)) {
            return buildNetworkInjectStorage(side);
        }
        return null;
    }

    private boolean allowItemConnection(Direction side) {
        // don't expose the API if there is a servo on this side
        return !(getAttachment(side).getItem() instanceof IoAttachmentItem ticking) || !ticking.isServo();
    }

    private Storage<ItemVariant> buildNetworkInjectStorage(Direction side) {
        return new InsertionOnlyStorage<>() {
            @Override
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                NetworkNode<ItemHost, ItemCache> node = findNode();
                if (node != null) {
                    // The node can be null if the pipe was just placed, and not initialized yet.
                    return node.getNetworkCache().insert(side.getOpposite(), node, FailedInsertStrategy.SEND_BACK_TO_SOURCE, resource, maxAmount,
                            transaction);
                } else {
                    return 0;
                }
            }

            @Override
            public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
                return Collections.emptyIterator();
            }
        };
    }

    protected EnumSet<Direction> getInventoryConnections() {
        return SerializationHelper.directionsFromMask((byte) inventoryConnections);
    }

    @Nullable
    protected Storage<ItemVariant> getAdjacentStorage(Direction side) {
        if ((inventoryConnections & (1 << side.getId())) > 0 && (pipeConnections & (1 << side.getId())) == 0 && allowItemConnection(side)) {
            return ItemStorage.SIDED.find(pipe.getWorld(), pipe.getPos().offset(side), side.getOpposite());
        }
        return null;
    }

    public void tickAttachments() {
        long currentTick = TickHelper.getTickCounter();
        for (var side : Direction.values()) {
            ItemStack attachment = getAttachment(side);
            if (attachment.getItem() instanceof IoAttachmentItem tickingItem) {
                if (currentTick - lastOperationTick[side.getId()] < tickingItem.tier.transferFrequency)
                    continue;
                lastOperationTick[side.getId()] = currentTick;

                if (tickingItem.isServo()) {
                    var adjStorage = ItemStorage.SIDED.find(pipe.getWorld(), pipe.getPos().offset(side), side.getOpposite());
                    if (adjStorage == null)
                        continue;

                    StorageUtil.move(
                            adjStorage,
                            buildNetworkInjectStorage(side),
                            iv -> tickingItem.matchesFilter(attachment, iv),
                            tickingItem.tier.transferCount,
                            null);
                }
            }
        }
    }

    public void tickMovingItems() {
        if (travelingItems.size() == 0) {
            return;
        }

        // List of items that moved out of this pipe.
        List<TravelingItem> movedOut = new ArrayList<>();

        double speed = 0.05; // WARNING: must always be < 1.
        for (var iterator = travelingItems.iterator(); iterator.hasNext();) {
            var travelingItem = iterator.next();
            int currentIndex = (int) Math.floor(travelingItem.traveledDistance);
            travelingItem.traveledDistance += speed;
            int newIndex = (int) Math.floor(travelingItem.traveledDistance);

            if (newIndex != currentIndex) {
                // Item is moving out of this pipe!
                movedOut.add(travelingItem);
                iterator.remove();
            }
        }

        for (var travelingItem : movedOut) {
            int newIndex = (int) Math.floor(travelingItem.traveledDistance);

            if (newIndex == travelingItem.getPathLength() - 1) {
                // End of path: inserting into a target storage.
                var storage = getAdjacentStorage(travelingItem.path.path[newIndex]);
                if (storage == null) {
                    storage = Storage.empty();
                }
                long inserted;
                try (var transaction = Transaction.openOuter()) {
                    inserted = storage.insert(travelingItem.variant, travelingItem.amount, transaction);
                    transaction.commit();
                }
                finishTravel(travelingItem, inserted);
            } else {
                // Otherwise: must be inserting into another pipe. Check that the connection exists.
                var adjPipeDirection = travelingItem.path.path[newIndex];

                @Nullable
                ItemHost adjacentItemHost = null;
                NetworkNode<ItemHost, ItemCache> ownNode = findNode();
                for (var connection : ownNode.getConnections()) {
                    if (connection.direction == adjPipeDirection) {
                        adjacentItemHost = connection.target.getHost();
                    }
                }

                if (adjacentItemHost != null) {
                    // All good: move to adjacent pipe
                    adjacentItemHost.travelingItems.add(travelingItem);
                    adjacentItemHost.pipe.markDirty();
                } else {
                    // Cancel the travel and handle the overflow
                    finishTravel(travelingItem, 0);
                }
            }
        }

        pipe.markDirty();
        pipe.sync(false);
    }

    private void finishTravel(TravelingItem item, long inserted) {
        // In any case, remove the item from the simulated insertion target
        item.path.getInsertionTarget(pipe.getWorld()).stopAwaiting(item.variant, item.amount);
        long leftover = item.amount - inserted;

        if (leftover > 0) {
            if (item.strategy == FailedInsertStrategy.SEND_BACK_TO_SOURCE) {
                Direction[] revertedPath = new Direction[item.getPathLength()];
                for (int i = 0; i < item.getPathLength(); ++i) {
                    revertedPath[revertedPath.length - i - 1] = item.path.path[i].getOpposite();
                }
                addTravelingItem(new TravelingItem(
                        item.variant,
                        leftover,
                        new ItemPath(
                                item.path.targetPos,
                                item.path.startingPos,
                                revertedPath),
                        FailedInsertStrategy.DROP,
                        item.getPathLength() - 1 - Math.floor(item.traveledDistance)));
            } else {
                DropHelper.dropStack(pipe, item.variant, item.amount - inserted);
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (travelingItems.size() > 0) {
            NbtList list = new NbtList();
            for (var travelingItem : travelingItems) {
                list.add(travelingItem.toNbt());
            }
            tag.put("travelingItems", list);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        NbtList list = tag.getList("travelingItems", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); ++i) {
            travelingItems.add(TravelingItem.fromNbt(list.getCompound(i)));
        }
    }

    @Override
    public void addSelf() {
        super.addSelf();
        for (var travelingItem : travelingItems) {
            travelingItem.path.getInsertionTarget(pipe.getWorld()).startAwaiting(travelingItem.variant, travelingItem.amount);
        }
    }

    @Override
    public void removeSelf() {
        super.removeSelf();
        for (var travelingItem : travelingItems) {
            travelingItem.path.getInsertionTarget(pipe.getWorld()).stopAwaiting(travelingItem.variant, travelingItem.amount);
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        for (var travelingItem : travelingItems) {
            travelingItem.path.getInsertionTarget(pipe.getWorld()).stopAwaiting(travelingItem.variant, travelingItem.amount);
            DropHelper.dropStack(pipe, travelingItem.variant, travelingItem.amount);
        }
        travelingItems.clear();
    }

    public void addTravelingItem(TravelingItem travelingItem) {
        this.travelingItems.add(travelingItem);
        pipe.markDirty();
    }

    @Override
    protected void doUpdate() {
        updateConnections();
    }

    public void gatherCapabilities() {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.byId(i);
                Storage<ItemVariant> adjacentCap = ItemStorage.SIDED.find(pipe.getWorld(), pipe.getPos().offset(dir), dir.getOpposite());

                if (adjacentCap == null) {
                    // Remove the direction from the bitmask
                    inventoryConnections ^= 1 << i;
                }
            }
        }

        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    public void updateConnections() {
        // Store old connections
        int oldConnections = inventoryConnections;

        // Compute new connections (excluding existing adjacent pipe connections, and the blacklist)
        inventoryConnections = (1 << 6) - 1 - (pipeConnections | pipe.connectionBlacklist);
        gatherCapabilities();

        // Update render
        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    @Override
    public void writeClientNbt(NbtCompound tag) {
        if (travelingItems.size() > 0) {
            NbtList list = new NbtList();
            for (var travelingItem : travelingItems) {
                NbtCompound compound = new NbtCompound();
                compound.put("v", travelingItem.variant.toNbt());
                compound.putLong("a", travelingItem.amount);
                int currentBlock = (int) Math.floor(travelingItem.traveledDistance);
                compound.putDouble("d", travelingItem.traveledDistance - currentBlock);
                compound.putByte("in", (byte) travelingItem.path.path[currentBlock].getId());
                compound.putByte("out", (byte) travelingItem.path.path[currentBlock + 1].getId());
                list.add(compound);
            }
            tag.put("travelingItems", list);
        }
    }

    @Override
    public void readClientNbt(NbtCompound tag) {
        clientTravelingItems.clear();
        NbtList list = tag.getList("travelingItems", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); ++i) {
            NbtCompound compound = list.getCompound(i);
            clientTravelingItems.add(new ClientTravelingItem(
                    ItemVariant.fromNbt(compound.getCompound("v")),
                    compound.getLong("a"),
                    compound.getDouble("d"),
                    Direction.byId(compound.getByte("in")),
                    Direction.byId(compound.getByte("out"))));
        }
    }

    public List<ClientTravelingItem> getClientTravelingItems() {
        return clientTravelingItems;
    }
}
