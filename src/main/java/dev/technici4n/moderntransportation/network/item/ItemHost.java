/*
 * Modern Transportation
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
package dev.technici4n.moderntransportation.network.item;

import dev.technici4n.moderntransportation.attachment.AttachmentItem;
import dev.technici4n.moderntransportation.network.NetworkManager;
import dev.technici4n.moderntransportation.network.NetworkNode;
import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.pipe.PipeBlockEntity;
import dev.technici4n.moderntransportation.util.SerializationHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class ItemHost extends NodeHost {
    private static final NetworkManager<ItemHost, ItemCache> MANAGER = NetworkManager.get(ItemCache.class, ItemCache::new);
    private final List<TravelingItem> travelingItems = new ArrayList<>();
    private final List<ClientTravelingItem> clientTravelingItems = new ArrayList<>();

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
        if (lookup == ItemStorage.SIDED) {
            return new InsertionOnlyStorage<ItemVariant>() {
                @Override
                public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    NetworkNode<ItemHost, ItemCache> node = findNode();
                    return node.getNetworkCache().insert(
                            side.getOpposite(), node, FailedInsertStrategy.SEND_BACK_TO_SOURCE, resource, maxAmount, transaction);
                }

                @Override
                public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
                    return Collections.emptyIterator();
                }
            };
        }
        return null;
    }

    protected long getPathingWeight() {
        return 1000;
    }

    protected EnumSet<Direction> getInventoryConnections() {
        return SerializationHelper.directionsFromMask((byte) inventoryConnections);
    }

    @Nullable
    protected Storage<ItemVariant> getAdjacentStorage(Direction side) {
        if ((inventoryConnections & (1 << side.getId())) > 0 && (pipeConnections & (1 << side.getId())) == 0) {
            return ItemStorage.SIDED.find(pipe.getWorld(), pipe.getPos().offset(side), side.getOpposite());
        }
        return null;
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

            if (newIndex == travelingItem.path.length - 1) {
                // End of path: inserting into a target storage.
                var storage = getAdjacentStorage(travelingItem.path[newIndex]);
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
                var adjPipeDirection = travelingItem.path[newIndex];

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
        var simulatedInsertionTarget = SimulatedInsertionTargets.getTarget((ServerWorld) pipe.getWorld(), item.targetPos,
                item.path[item.path.length - 1].getOpposite());
        simulatedInsertionTarget.stopAwaiting(item.variant, item.amount);
        // TODO
        // if strategy is to send back to source, send back to source
        // otherwise just drop the remainder
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
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
                compound.putByte("in", (byte) travelingItem.path[currentBlock].getId());
                compound.putByte("out", (byte) travelingItem.path[currentBlock + 1].getId());
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
