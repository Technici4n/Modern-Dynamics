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
import dev.technici4n.moderndynamics.network.NetworkCache;
import dev.technici4n.moderndynamics.network.NetworkNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.level.ServerLevel;

public class FluidCache extends NetworkCache<FluidHost, FluidCache> {
    private SingleVariantStorage<FluidVariant> fluidStorage = null;

    protected FluidCache(ServerLevel level, List<NetworkNode<FluidHost, FluidCache>> networkNodes) {
        super(level, networkNodes);
    }

    @Override
    protected void doCombine() {
        FluidVariant fv = FluidVariant.blank();
        long amount = 0;

        for (var node : nodes) {
            var host = node.getHost();

            if (!host.getVariant().isBlank()) {
                // TODO: what if the host has another variant??? Need to handle that case!
                if (fv.isBlank()) {
                    fv = host.getVariant();
                }
                amount += host.getAmount();
            }
        }

        fluidStorage = new SingleVariantStorage<>() {
            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }

            @Override
            protected long getCapacity(FluidVariant variant) {
                return nodes.size() * Constants.Fluids.CAPACITY;
            }
        };
        fluidStorage.variant = fv;
        fluidStorage.amount = amount;
    }

    @Override
    protected void doSeparate() {
        if (Transaction.isOpen()) {
            throw new IllegalStateException("Can't separate a network when a transaction is open!");
        }

        // Distribute new energy among nodes
        // Start with nodes with the lowest capacity
        nodes.sort(Comparator.comparingLong(node -> Constants.Fluids.CAPACITY));
        int remainingNodes = nodes.size();

        for (NetworkNode<FluidHost, FluidCache> node : nodes) {
            FluidHost host = node.getHost();

            long nodeAmount = Math.min(Constants.Fluids.CAPACITY, fluidStorage.amount / remainingNodes);
            host.setContents(fluidStorage.variant, nodeAmount);
            fluidStorage.amount -= nodeAmount;
            remainingNodes--;
        }

        fluidStorage = null;
    }

    @Override
    public void doTick() {
        // Make sure the network is combined
        combine();

        // Gather inventory connections
        List<Storage<FluidVariant>> storages = new ArrayList<>();

        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().addFluidStorages(storages);
            }
        }

        try (var tx = Transaction.openOuter()) {
            // Find fluid to extract
            if (fluidStorage.isResourceBlank()) {
                for (var storage : storages) {
                    var toExtract = StorageUtil.findExtractableResource(storage, tx);

                    if (toExtract != null) {
                        fluidStorage.variant = toExtract;
                    }
                }
            }
            if (!fluidStorage.isResourceBlank()) {
                // Extract
                fluidStorage.amount += transferForTargets(Storage::extract, storages, fluidStorage.variant,
                        fluidStorage.getCapacity() - fluidStorage.amount, tx);
                // Insert
                fluidStorage.amount -= transferForTargets(Storage::insert, storages, fluidStorage.variant, fluidStorage.amount, tx);

                if (fluidStorage.amount == 0) {
                    fluidStorage.variant = FluidVariant.blank();
                }
            }

            tx.commit();
        }

        // For the MVP, we separate again and then sync each fluid value.
        // TODO: smarter fluid syncing logic
        separate();
        for (var node : nodes) {
            node.getHost().getPipe().sync();
        }
    }

    /**
     * Dispatch a transfer operation among a list of targets. Will not modify the list.
     */
    private static long transferForTargets(TransferOperation operation, List<Storage<FluidVariant>> targets, FluidVariant variant, long maxAmount,
            Transaction tx) {
        int intMaxAmount = Ints.saturatedCast(maxAmount);
        // Build target list
        List<EnergyTarget> sortableTargets = new ArrayList<>(targets.size());
        for (var target : targets) {
            sortableTargets.add(new EnergyTarget(target));
        }
        // Shuffle for better transfer on average
        Collections.shuffle(sortableTargets);
        // Simulate the transfer for every target
        for (EnergyTarget target : sortableTargets) {
            try (var simulation = tx.openNested()) {
                target.simulationResult = operation.transfer(target.target, variant, intMaxAmount, simulation);
            }
        }
        // Sort from low to high result
        sortableTargets.sort(Comparator.comparingLong(t -> t.simulationResult));
        // Actually perform the transfer
        long transferredAmount = 0;
        for (int i = 0; i < sortableTargets.size(); ++i) {
            EnergyTarget target = sortableTargets.get(i);
            int remainingTargets = sortableTargets.size() - i;
            long remainingAmount = maxAmount - transferredAmount;
            int targetMaxAmount = Ints.saturatedCast(remainingAmount / remainingTargets);

            transferredAmount += operation.transfer(target.target, variant, targetMaxAmount, tx);
        }
        return transferredAmount;
    }

    interface TransferOperation {
        long transfer(Storage<FluidVariant> storage, FluidVariant resource, long maxTransfer, TransactionContext transaction);
    }

    private static class EnergyTarget {
        final Storage<FluidVariant> target;
        long simulationResult;

        EnergyTarget(Storage<FluidVariant> target) {
            this.target = target;
        }
    }

    @Override
    public void appendDebugInfo(StringBuilder out) {
        super.appendDebugInfo(out);
        if (fluidStorage == null) {
            out.append("no fluid storage\n");
        } else {
            out.append("fluid variant = ").append(fluidStorage.variant).append("\n");
            out.append("amount = ").append(fluidStorage.amount).append("\n");
            out.append("capacity = ").append(fluidStorage.getCapacity()).append("\n");
        }
    }

    static boolean areCompatible(FluidVariant v1, FluidVariant v2) {
        return v1.isBlank() || v2.isBlank() || v1.equals(v2);
    }
}
