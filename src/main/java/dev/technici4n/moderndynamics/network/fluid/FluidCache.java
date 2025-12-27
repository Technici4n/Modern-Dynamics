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
package dev.technici4n.moderndynamics.network.fluid;

import com.google.common.primitives.Ints;
import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.attached.FluidAttachedIo;
import dev.technici4n.moderndynamics.network.NetworkCache;
import dev.technici4n.moderndynamics.network.NetworkNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class FluidCache extends NetworkCache<FluidHost, FluidCache> {
    private FluidCacheStorage fluidStorage = null;
    private int attractorBuffer = 0;
    private boolean allowNetworkIo = true;

    protected FluidCache(ServerLevel level, List<NetworkNode<FluidHost, FluidCache>> networkNodes) {
        super(level, networkNodes);
    }

    public FluidCacheStorage getOrCreateStorage() {
        combine();
        return fluidStorage;
    }

    @Override
    protected void doCombine() {
        FluidResource fv = FluidResource.EMPTY;
        int amount = 0;

        for (var node : nodes) {
            var host = node.getHost();

            if (!host.getVariant().isEmpty()) {
                // TODO: what if the host has another variant??? Need to handle that case!
                if (fv.isEmpty()) {
                    fv = host.getVariant();
                }
                amount += host.getAmount();
            }
        }

        fluidStorage = new FluidCacheStorage();
        fluidStorage.resource = fv;
        fluidStorage.storedAmount = amount;
    }

    @Override
    protected void doSeparate() {
        // Distribute new energy among nodes
        // Start with nodes with the lowest capacity
        nodes.sort(Comparator.comparingLong(node -> Constants.Fluids.CAPACITY));
        int remainingNodes = nodes.size();

        for (NetworkNode<FluidHost, FluidCache> node : nodes) {
            FluidHost host = node.getHost();

            var nodeAmount = Math.min(Constants.Fluids.CAPACITY, fluidStorage.storedAmount / remainingNodes);
            host.setContents(fluidStorage.resource, nodeAmount);
            fluidStorage.storedAmount -= nodeAmount;
            remainingNodes--;
        }

        fluidStorage = null;
    }

    @Override
    public void doTick() {
        // Make sure the network is combined
        combine();

        // Gather inventory connections
        List<ConnectedFluidStorage> targets = new ArrayList<>();
        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().gatherCapabilities(targets);
            }
        }

        List<FluidAttachedIo> attractors = new ArrayList<>();
        for (var conn : targets) {
            if (conn.attachment() != null && conn.attachment().getType() == IoAttachmentType.ATTRACTOR) {
                attractors.add(conn.attachment());
            }
        }

        boolean changedVariant = false;
        allowNetworkIo = false;

        try {
            // Find item to extract
            if (fluidStorage.isResourceEmpty()) {
                var newVariant = findVariantForNetwork(targets, attractors);
                if (!newVariant.isEmpty() && canChangeVariant()) {
                    fluidStorage.resource = newVariant;
                    changedVariant = true;
                }
            }

            if (!fluidStorage.isResourceEmpty()) {
                // Take from connected storages
                extractFluid(targets);
                attractFluid(targets, attractors);
                // Push to connected storages
                distributeFluid(targets);

                if (fluidStorage.storedAmount == 0 && canChangeVariant()) {
                    fluidStorage.resource = FluidResource.EMPTY;
                    changedVariant = true;
                }
            }
        } finally {
            allowNetworkIo = true;
        }

        // Always separate after a change of variant to ensure that the nodes properly update their stored item.
        if (changedVariant) {
            separate();
        }

        // For the MVP, we separate again and then sync each item value.
        // TODO: smarter item syncing logic
        separate();
    }

    /**
     * We only allow changing the item in the network if all hosts are ticking.
     * This guarantees that we have made all the connections that we wanted to before,
     * since changing the item of the network will change how pipes can connect to each other.
     */
    private boolean canChangeVariant() {
        for (var node : nodes) {
            if (!node.getHost().isTicking()) {
                return false;
            }
        }
        return true;
    }

    private FluidResource findVariantForNetwork(List<ConnectedFluidStorage> targets, List<FluidAttachedIo> attractors) {
        // Look for item matching an extractor
        for (var t : targets) {
            if (t.attachment() != null && t.attachment().getType() == IoAttachmentType.EXTRACTOR) {
                var toExtract = ResourceHandlerUtil.findExtractableResource(t.storage(), fv -> t.attachment().matchesFilter(fv), null);
                if (toExtract != null) {
                    return toExtract;
                }
            }
        }

        // Look for item matching an attractor
        if (!attractors.isEmpty()) {
            Predicate<FluidResource> attractorFilter = fv -> {
                for (var a : attractors) {
                    if (a.matchesFilter(fv)) {
                        return true;
                    }
                }
                return false;
            };

            for (var t : targets) {
                var toExtract = ResourceHandlerUtil.findExtractableResource(t.storage(), attractorFilter, null);
                if (toExtract != null) {
                    return toExtract;
                }
            }
        }

        return FluidResource.EMPTY;
    }

    /**
     * Extract from connected storages that have an extractor.
     */
    private void extractFluid(List<ConnectedFluidStorage> targets) {
        fluidStorage.storedAmount += transferForTargets(ResourceHandler::extract, targets, fluidStorage.resource,
                fluidStorage.getCapacity() - fluidStorage.storedAmount, ConnectedFluidStorage::extractorFilteredStorage);
    }

    /**
     * Attract, i.e. extract from connected storages if there's attractors on the network.
     */
    private void attractFluid(List<ConnectedFluidStorage> targets, List<FluidAttachedIo> attractors) {
        int attractorPower = 0;
        for (var attractor : attractors) {
            attractorPower += attractor.matchesFilter(fluidStorage.resource) ? attractor.getFluidMaxIo() : 0;
        }
        int maxAttract = attractorBuffer + attractorPower;
        int attracted = transferForTargets(ResourceHandler::extract, targets, fluidStorage.resource,
                Math.min(fluidStorage.getCapacity() - fluidStorage.storedAmount, maxAttract),
                ConnectedFluidStorage::storage);
        attractorBuffer = Math.min(maxAttract - attracted, FluidType.BUCKET_VOLUME);
        fluidStorage.storedAmount += attracted;
    }

    /**
     * Distribute stored item among connected storages.
     */
    private void distributeFluid(List<ConnectedFluidStorage> targets) {
        // Insert into storages with attractors first
        fluidStorage.storedAmount -= transferForTargets(ResourceHandler::insert, targets, fluidStorage.resource,
                fluidStorage.storedAmount, ConnectedFluidStorage.filterAttractors(true));
        // Insert into others
        fluidStorage.storedAmount -= transferForTargets(ResourceHandler::insert, targets, fluidStorage.resource,
                fluidStorage.storedAmount, ConnectedFluidStorage.filterAttractors(false));
    }

    /**
     * Dispatch a transfer operation among a list of targets. Will not modify the list.
     *
     * @param storageGetter Can return null to skip the target
     */
    private static int transferForTargets(TransferOperation operation, List<ConnectedFluidStorage> targets, FluidResource variant, int maxAmount,
            Function<ConnectedFluidStorage, ResourceHandler<FluidResource>> storageGetter) {
        if (maxAmount == 0) {
            return 0;
        }

        int intMaxAmount = Ints.saturatedCast(maxAmount);
        // Build target list
        List<FluidTarget> sortableTargets = new ArrayList<>(targets.size());
        for (var target : targets) {
            var storage = storageGetter.apply(target);

            if (storage != null) {
                sortableTargets.add(new FluidTarget(storage));
            }
        }
        // Shuffle for better transfer on average
        Collections.shuffle(sortableTargets);
        // Simulate the transfer for every target
        try (var tx = Transaction.openRoot()) {
            for (FluidTarget target : sortableTargets) {
                target.simulationResult = operation.transfer(target.target, variant, intMaxAmount, tx);
            }
        }
        // Sort from low to high result
        sortableTargets.sort(Comparator.comparingLong(t -> t.simulationResult));
        // Actually perform the transfer
        int transferredAmount = 0;
        try (var tx = Transaction.openRoot()) {
            for (int i = 0; i < sortableTargets.size(); ++i) {
                FluidTarget target = sortableTargets.get(i);
                int remainingTargets = sortableTargets.size() - i;
                long remainingAmount = maxAmount - transferredAmount;
                int targetMaxAmount = Ints.saturatedCast(remainingAmount / remainingTargets);

                transferredAmount += operation.transfer(target.target, variant, targetMaxAmount, tx);
            }
            tx.commit();
        }
        return transferredAmount;
    }

    interface TransferOperation {
        int transfer(ResourceHandler<FluidResource> storage, FluidResource resource, int maxAmount, TransactionContext tx);
    }

    private static class FluidTarget {
        final ResourceHandler<FluidResource> target;
        long simulationResult;

        FluidTarget(ResourceHandler<FluidResource> target) {
            this.target = target;
        }
    }

    @Override
    public void appendDebugInfo(StringBuilder out) {
        super.appendDebugInfo(out);
        if (fluidStorage == null) {
            out.append("no item storage\n");
        } else {
            out.append("item variant = ").append(fluidStorage.resource).append("\n");
            out.append("amount = ").append(fluidStorage.storedAmount).append("\n");
            out.append("capacity = ").append(fluidStorage.getCapacity()).append("\n");
        }
    }

    static boolean areCompatible(FluidResource v1, FluidResource v2) {
        return v1.isEmpty() || v2.isEmpty() || v1.equals(v2);
    }

    public class FluidCacheStorage extends SnapshotJournal<FluidCacheStorage.Snapshot> implements ResourceHandler<FluidResource> {
        private FluidResource resource = FluidResource.EMPTY;
        private int storedAmount;

        @Override
        public int size() {
            return 1;
        }

        @Override
        public @NotNull FluidResource getResource(int index) {
            Objects.checkIndex(index, 1);
            return resource;
        }

        @Override
        public long getAmountAsLong(int index) {
            Objects.checkIndex(index, 1);
            return storedAmount;
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            Objects.checkIndex(index, 1);
            return getCapacity();
        }

        @Override
        public boolean isValid(int index, @NotNull FluidResource resource) {
            Objects.checkIndex(index, 1);
            return this.resource.equals(resource) || (this.resource.isEmpty() && canChangeVariant());
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
            Objects.checkIndex(index, 1);
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            if (!allowNetworkIo || !isValid(0, resource)) {
                return 0;
            }

            var insertedAmount = Math.min(amount, getCapacity() - storedAmount);
            if (insertedAmount > 0) {
                updateSnapshots(tx);
                if (this.resource.isEmpty()) {
                    this.resource = resource;
                    storedAmount = insertedAmount;
                } else {
                    storedAmount += insertedAmount;
                }
            }
            return insertedAmount;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
            Objects.checkIndex(index, 1);
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (!allowNetworkIo || !this.resource.equals(resource)) {
                return 0;
            }

            var extractedAmount = Math.min(amount, storedAmount);
            if (extractedAmount > 0) {
                updateSnapshots(tx);
                storedAmount -= extractedAmount;
                if (storedAmount == 0 && canChangeVariant()) {
                    this.resource = FluidResource.EMPTY;
                }
            }

            return extractedAmount;
        }

        protected record Snapshot(FluidResource resource, int storedAmount) {
        }

        @Override
        protected void onRootCommit(Snapshot originalState) {
            if (!originalState.resource.equals(resource)) {
                // Make sure we updated the resource stored in each node!
                separate();
            }
        }

        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(resource, storedAmount);
        }

        @Override
        protected void revertToSnapshot(Snapshot snapshot) {
            resource = snapshot.resource;
            storedAmount = snapshot.storedAmount;
        }

        public boolean isResourceEmpty() {
            return resource.isEmpty();
        }

        public FluidResource getResource() {
            return resource;
        }

        public int getStoredAmount() {
            return storedAmount;
        }

        public int getCapacity() {
            return nodes.size() * Constants.Fluids.CAPACITY;
        }
    }
}
