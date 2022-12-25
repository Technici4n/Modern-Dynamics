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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.NetworkNode;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.TickHelper;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItem;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItemSmoothing;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.DropHelper;
import dev.technici4n.moderndynamics.util.SerializationHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemHost extends NodeHost {
    public static final NetworkManager<ItemHost, ItemCache> MANAGER = NetworkManager.get(ItemCache.class, ItemCache::new);
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
    public boolean canConnectTo(Direction connectionDirection, NodeHost adjacentHost) {
        var attachment = getAttachment(connectionDirection);
        if (attachment instanceof ItemAttachedIo) {
            return false;
        }
        return super.canConnectTo(connectionDirection, adjacentHost);
    }

    @Override
    @Nullable
    public Object getApiInstance(BlockApiLookup<?, Direction> lookup, Direction side) {
        if (lookup == ItemStorage.SIDED && allowItemConnection(side)) {
            return buildExternalNetworkInjectStorage(side);
        }
        return null;
    }

    private boolean allowItemConnection(Direction side) {
        // don't expose the API if there is a servo on this side
        var attachment = getAttachment(side);
        return attachment == null || attachment.allowsItemConnection();
    }

    /**
     * Storage used for external injections (e.g. via hoppers), does not respect routing mode.
     */
    private Storage<ItemVariant> buildExternalNetworkInjectStorage(Direction side) {
        double speedupFactor = getAttachment(side) instanceof ItemAttachedIo io ? io.getItemSpeedupFactor() : 1;
        return (InsertStorage) (resource, maxAmount, transaction) -> {
            NetworkNode<ItemHost, ItemCache> node = findNode();
            if (node != null) {
                var cache = node.getNetworkCache();
                var paths = cache.pathCache.getPaths(node, side.getOpposite());
                // The node can be null if the pipe was just placed, and not initialized yet.
                return cache.insertList(node, paths, resource, maxAmount, transaction, speedupFactor);
            } else {
                return 0;
            }
        };
    }

    /**
     * Storage used by extractors. It respects routing mode.
     */
    private Storage<ItemVariant> buildExtractorNetworkInjectStorage(Direction side, ItemAttachedIo extractor) {
        double speedupFactor = extractor.getItemSpeedupFactor();
        NetworkNode<ItemHost, ItemCache> node = findNode();
        var cache = node.getNetworkCache();
        var paths = rearrangePaths(cache.pathCache.getPaths(node, side.getOpposite()), extractor);
        return (InsertStorage) (resource, maxAmount, transaction) -> {
            // The node can be null if the pipe was just placed, and not initialized yet.
            return cache.insertList(node, paths, resource, maxAmount, transaction, speedupFactor);
        };
    }

    protected EnumSet<Direction> getInventoryConnections() {
        return SerializationHelper.directionsFromMask((byte) inventoryConnections);
    }

    @Nullable
    protected Storage<ItemVariant> getAdjacentStorage(Direction side, boolean checkAttachments) {
        if ((inventoryConnections & (1 << side.get3DDataValue())) > 0 && (pipeConnections & (1 << side.get3DDataValue())) == 0
                && (!checkAttachments || allowItemConnection(side))) {
            return ItemStorage.SIDED.find(pipe.getLevel(), pipe.getBlockPos().relative(side), side.getOpposite());
        }
        return null;
    }

    private Iterable<ItemPath> rearrangePaths(List<ItemPath> path, ItemAttachedIo io) {
        if (path.size() <= 1) {
            return path;
        }

        return switch (io.getRoutingMode()) {
        case CLOSEST -> path;
        case FURTHEST -> Lists.reverse(path);
        case RANDOM -> {
            var pathCopy = new ArrayList<>(path);
            Collections.shuffle(pathCopy);
            yield pathCopy;
        }
        case ROUND_ROBIN -> {
            int startIndex = io.getRoundRobinIndex(path.size());
            yield Iterables.concat(
                    path.subList(startIndex, path.size()),
                    path.subList(0, startIndex));
        }
        };
    }

    public void tickAttachments() {
        long currentTick = TickHelper.getTickCounter();
        for (var side : Direction.values()) {
            var attachment = getAttachment(side);
            if (attachment instanceof ItemAttachedIo itemAttachedIo && itemAttachedIo.isEnabledViaRedstone(pipe)) {
                if (currentTick - lastOperationTick[side.get3DDataValue()] < itemAttachedIo.getItemOperationTickDelay())
                    continue;
                lastOperationTick[side.get3DDataValue()] = currentTick;
                if (itemAttachedIo.getType() == IoAttachmentType.EXTRACTOR) {
                    if (itemAttachedIo.isStuffed()) {
                        // Move from stuffed items to network
                        if (itemAttachedIo.moveStuffedToStorage(buildExtractorNetworkInjectStorage(side, itemAttachedIo),
                                itemAttachedIo.getMaxItemsExtracted()) > 0) {
                            itemAttachedIo.incrementRoundRobin();
                            pipe.setChanged();
                            if (!itemAttachedIo.isStuffed()) {
                                pipe.sync();
                            }
                        }
                    } else {
                        var adjStorage = getAdjacentStorage(side, false);
                        if (adjStorage == null)
                            continue;
                        if (StorageUtil.move(
                                adjStorage,
                                buildExtractorNetworkInjectStorage(side, itemAttachedIo),
                                itemAttachedIo::matchesItemFilter,
                                itemAttachedIo.getMaxItemsExtracted(),
                                null) > 0) {
                            itemAttachedIo.incrementRoundRobin();
                        }
                    }
                } else if (itemAttachedIo.getType() == IoAttachmentType.ATTRACTOR) {
                    if (itemAttachedIo.isStuffed()) {
                        // Move from stuffed items to target
                        var adjStorage = getAdjacentStorage(side, false);
                        if (adjStorage == null)
                            continue;
                        if (itemAttachedIo.moveStuffedToStorage(adjStorage, itemAttachedIo.getMaxItemsExtracted()) > 0) {
                            pipe.setChanged();
                            if (!itemAttachedIo.isStuffed()) {
                                pipe.sync();
                            }
                        }
                    } else {
                        var insertTarget = SimulatedInsertionTargets.getTarget(pipe.getLevel(), pipe.getBlockPos().relative(side),
                                side.getOpposite());
                        if (!insertTarget.hasStorage())
                            continue;

                        NetworkNode<ItemHost, ItemCache> thisNode = findNode();
                        var cache = thisNode.getNetworkCache();
                        var pathCache = cache.pathCache;
                        var paths = rearrangePaths(pathCache.getPaths(thisNode, side.getOpposite()), itemAttachedIo);

                        long maxTransfer = itemAttachedIo.getMaxItemsExtracted();
                        long toTransfer = maxTransfer;

                        for (var path : paths) {
                            // Don't allow attractors to pull from other attractors
                            if (path.getEndAttachment(cache.level) instanceof ItemAttachedIo io && io.getType() == IoAttachmentType.ATTRACTOR) {
                                continue;
                            }

                            var extractTarget = ItemStorage.SIDED.find(pipe.getLevel(), path.targetPos, path.getTargetBlockSide());
                            if (extractTarget != null) {
                                // Make sure to check the filter at the endpoint.
                                var endpointFilter = path.getEndFilter(cache.level);

                                var insertStorage = (InsertStorage) (variant, maxAmount, tx) -> {
                                    return insertTarget.insert(variant, maxAmount, tx, (v, a) -> {
                                        var reversedPath = path.reversed();
                                        var travelingItem = reversedPath.makeTravelingItem(v, a, itemAttachedIo.getItemSpeedupFactor());
                                        reversedPath.getStartingPoint(cache.level).getHost().addTravelingItem(travelingItem);
                                    });
                                };
                                toTransfer -= StorageUtil.move(
                                        extractTarget,
                                        insertStorage,
                                        v -> itemAttachedIo.matchesItemFilter(v) && endpointFilter.test(v),
                                        toTransfer,
                                        null);
                                if (toTransfer == 0)
                                    break;
                            }
                        }

                        if (toTransfer < maxTransfer) {
                            itemAttachedIo.incrementRoundRobin();
                        }
                    }
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

        for (var iterator = travelingItems.iterator(); iterator.hasNext();) {
            var travelingItem = iterator.next();
            // Calculate in which path segment the item is now, and in which segment it is after moving it
            int currentIndex = (int) travelingItem.traveledDistance;
            travelingItem.traveledDistance += travelingItem.getSpeed();
            int newIndex = (int) travelingItem.traveledDistance;

            if (newIndex != currentIndex) {
                // Item is moving out of this pipe!
                movedOut.add(travelingItem);
                iterator.remove();
            }
        }

        for (var travelingItem : movedOut) {
            int newIndex = (int) travelingItem.traveledDistance;

            if (newIndex >= travelingItem.getPathLength() - 1) {
                // End of path: inserting into a target storage.
                // Prefer ignoring an attachment over dropping the item on the ground.
                boolean checkAttachments = travelingItem.strategy != FailedInsertStrategy.DROP;
                var side = travelingItem.path.path[newIndex];
                var storage = getAdjacentStorage(side, checkAttachments);
                if (storage == null) {
                    storage = Storage.empty();
                }
                long inserted = 0;
                // Check filter.
                if (!checkAttachments || !(getAttachment(side) instanceof ItemAttachedIo io) ||
                        io.matchesItemFilter(travelingItem.variant) && io.isEnabledViaRedstone(pipe)) {
                    try (var transaction = Transaction.openOuter()) {
                        inserted = storage.insert(travelingItem.variant, travelingItem.amount, transaction);
                        transaction.commit();
                    }
                }
                finishTravel(travelingItem, inserted);
            } else {
                // Otherwise: must be inserting into another pipe. Check that the connection exists.
                var adjPipeDirection = travelingItem.path.path[newIndex];

                @Nullable
                ItemHost adjacentItemHost = null;
                NetworkNode<ItemHost, ItemCache> ownNode = findNode();
                for (var connection : ownNode.getConnections()) {
                    if (connection.direction() == adjPipeDirection) {
                        adjacentItemHost = connection.target().getHost();
                    }
                }

                if (adjacentItemHost != null) {
                    // All good: move to adjacent pipe
                    adjacentItemHost.travelingItems.add(travelingItem);
                    adjacentItemHost.pipe.setChanged();
                    adjacentItemHost.pipe.sync(false);
                } else {
                    // Cancel the travel and handle the overflow
                    finishTravel(travelingItem, 0);
                }
            }
        }

        pipe.setChanged();
        pipe.sync(false);
    }

    private void finishTravel(TravelingItem item, long inserted) {
        // In any case, remove the item from the simulated insertion target
        item.path.getInsertionTarget(pipe.getLevel()).stopAwaiting(item.variant, item.amount);
        long leftover = item.amount - inserted;

        // Try to stuff first!
        var attachment = getAttachment(item.path.path[item.getPathLength() - 1]);
        if (leftover > 0 && attachment instanceof ItemAttachedIo io && io.getType() != IoAttachmentType.FILTER) {
            boolean wasStuffed = io.isStuffed();
            io.getStuffedItems().merge(item.variant, item.amount, Long::sum);
            pipe.setChanged();
            if (wasStuffed != io.isStuffed()) {
                pipe.sync();
            }
        } else if (leftover > 0) {
            if (item.strategy == FailedInsertStrategy.SEND_BACK_TO_SOURCE) {
                addTravelingItem(new TravelingItem(
                        item.variant,
                        leftover,
                        item.path.reversed(),
                        FailedInsertStrategy.DROP,
                        item.speedMultiplier,
                        item.getPathLength() - 1 - Math.floor(item.traveledDistance)));
            } else {
                DropHelper.dropStack(pipe, item.variant, item.amount - inserted);
            }
        }
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        super.writeNbt(tag);
        if (travelingItems.size() > 0) {
            ListTag list = new ListTag();
            for (var travelingItem : travelingItems) {
                list.add(travelingItem.toNbt());
            }
            tag.put("travelingItems", list);
        }
    }

    @Override
    public void readNbt(CompoundTag tag) {
        super.readNbt(tag);
        ListTag list = tag.getList("travelingItems", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            var item = TravelingItem.fromNbt(list.getCompound(i));

            if (!item.variant.isBlank()) { // Guard against blank variants in case a mod is removed
                travelingItems.add(item);
            }
        }
    }

    @Override
    public void addSelf() {
        super.addSelf();
        for (var travelingItem : travelingItems) {
            travelingItem.path.getInsertionTarget(pipe.getLevel()).startAwaiting(travelingItem.variant, travelingItem.amount);
        }
    }

    @Override
    public void removeSelf() {
        super.removeSelf();
        for (var travelingItem : travelingItems) {
            travelingItem.path.getInsertionTarget(pipe.getLevel()).stopAwaiting(travelingItem.variant, travelingItem.amount);
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        for (var travelingItem : travelingItems) {
            travelingItem.path.getInsertionTarget(pipe.getLevel()).stopAwaiting(travelingItem.variant, travelingItem.amount);
            DropHelper.dropStack(pipe, travelingItem.variant, travelingItem.amount);
        }
        travelingItems.clear();
    }

    public void addTravelingItem(TravelingItem travelingItem) {
        this.travelingItems.add(travelingItem);
        pipe.setChanged();
    }

    @Override
    protected void doUpdate() {
        updateConnections();
    }

    public void gatherCapabilities() {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                Storage<ItemVariant> adjacentCap = ItemStorage.SIDED.find(pipe.getLevel(), pipe.getBlockPos().relative(dir), dir.getOpposite());

                if (adjacentCap == null) {
                    // Remove the direction from the bitmask
                    inventoryConnections ^= 1 << i;
                }
            }
        }

        if (oldConnections != inventoryConnections) {
            pipe.sync();
            NetworkNode<ItemHost, ItemCache> node = findNode();
            node.getNetworkCache().pathCache.invalidate();
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
    public void writeClientNbt(CompoundTag tag) {
        super.writeClientNbt(tag);

        if (travelingItems.size() > 0) {
            ListTag list = new ListTag();
            for (var travelingItem : travelingItems) {
                CompoundTag compound = new CompoundTag();
                compound.putInt("id", travelingItem.id);
                compound.put("v", travelingItem.variant.toNbt());
                compound.putLong("a", travelingItem.amount);
                compound.putDouble("td", travelingItem.getPathLength() - 1);
                compound.putDouble("d", travelingItem.traveledDistance);
                int currentBlock = (int) Math.floor(travelingItem.traveledDistance);
                compound.putByte("in", (byte) travelingItem.path.path[currentBlock].get3DDataValue());
                compound.putByte("out", (byte) travelingItem.path.path[currentBlock + 1].get3DDataValue());
                compound.putDouble("s", travelingItem.getSpeed());
                list.add(compound);
            }
            tag.put("travelingItems", list);
        }
    }

    @Override
    public void readClientNbt(CompoundTag tag) {
        super.readClientNbt(tag);

        clientTravelingItems.clear();
        ListTag list = tag.getList("travelingItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag compound = list.getCompound(i);
            var newItem = new ClientTravelingItem(
                    compound.getInt("id"),
                    ItemVariant.fromNbt(compound.getCompound("v")),
                    compound.getLong("a"),
                    compound.getDouble("td"),
                    compound.getDouble("d"),
                    Direction.from3DDataValue(compound.getByte("in")),
                    Direction.from3DDataValue(compound.getByte("out")),
                    compound.getDouble("s"));
            clientTravelingItems.add(newItem);
            ClientTravelingItemSmoothing.onReceiveItem(newItem);
        }
    }

    @Override
    public void clientTick() {
        for (var it = clientTravelingItems.iterator(); it.hasNext();) {
            ClientTravelingItem travelingItem = it.next();
            travelingItem.traveledDistance += travelingItem.speed();

            if (Mth.frac(travelingItem.traveledDistance) < travelingItem.speed()) {
                // Goes out of this pipe!
                it.remove();
                if (travelingItem.traveledDistance < travelingItem.totalPathDistance) {
                    // Add to next pipe
                    if (getLevel().getBlockEntity(getPos().relative(travelingItem.out)) instanceof PipeBlockEntity otherPipe) {
                        for (var host : otherPipe.getHosts()) {
                            if (host instanceof ItemHost otherItemHost) {
                                otherItemHost.clientTravelingItems.add(travelingItem);
                                travelingItem.in = travelingItem.out; // ensure item appears from the correct side
                            }
                        }
                    }
                }
            }
        }
    }

    public List<ClientTravelingItem> getClientTravelingItems() {
        return clientTravelingItems;
    }
}
