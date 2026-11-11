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
package dev.technici4n.moderndynamics.network.energy;

import com.google.common.primitives.Ints;
import dev.technici4n.moderndynamics.network.NetworkCache;
import dev.technici4n.moderndynamics.network.NetworkNode;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergyCache extends NetworkCache<EnergyHost, EnergyCache> {
    private SimpleEnergyStorage energyStorage = null;

    public EnergyCache(ServerLevel level, List<NetworkNode<EnergyHost, EnergyCache>> nodes) {
        super(level, nodes);
    }

    public int getStorageAmount() {
        combine();
        return energyStorage.getAmountAsInt();
    }

    public int getStorageCapacity() {
        combine();
        return energyStorage.getCapacityAsInt();
    }

    public int insert(int amount, TransactionContext tx) {
        TransferPreconditions.checkNonNegative(amount);
        combine();
        return energyStorage.insert(amount, tx);
    }

    public int extract(int amount, TransactionContext tx) {
        TransferPreconditions.checkNonNegative(amount);
        combine();
        return energyStorage.extract(amount, tx);
    }

    @Override
    protected void doCombine() {
        // Gather energy from nodes
        int energy = 0;
        int maxEnergy = 0;

        for (NetworkNode<EnergyHost, EnergyCache> node : nodes) {
            EnergyHost host = node.getHost();

            energy += host.getEnergy();
            maxEnergy += host.getMaxEnergy();
        }

        energyStorage = new SimpleEnergyStorage(maxEnergy, Integer.MAX_VALUE, Integer.MAX_VALUE);
        energyStorage.setEnergy(energy);
    }

    @Override
    protected void doSeparate() {
        // Distribute new energy among nodes
        // Start with nodes with the lowest capacity
        nodes.sort(Comparator.comparingLong(node -> node.getHost().getMaxEnergy()));
        int remainingNodes = nodes.size();

        for (NetworkNode<EnergyHost, EnergyCache> node : nodes) {
            EnergyHost host = node.getHost();

            int nodeEnergy = Math.min(host.getMaxEnergy(), energyStorage.getAmountAsInt() / remainingNodes);
            host.setEnergy(nodeEnergy);
            energyStorage.reduceEnergyStored(nodeEnergy);
            remainingNodes--;
        }

        energyStorage = null;
    }

    @Override
    public void doTick() {
        // Make sure the network is combined
        combine();

        // Gather inventory connections
        List<EnergyHandler> storages = new ArrayList<>();

        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().addEnergyStorages(storages);
            }
        }

        // Extract
        var remainingCapacity = energyStorage.getCapacityAsInt() - energyStorage.getAmountAsInt();
        energyStorage.addEnergyStored(transferForTargets(EnergyHandler::extract, storages, remainingCapacity));
        // Insert
        energyStorage.reduceEnergyStored(transferForTargets(EnergyHandler::insert, storages, energyStorage.getAmountAsInt()));
    }

    /**
     * Dispatch a transfer operation among a list of targets. Will not modify the list.
     */
    public static int transferForTargets(TransferOperation operation, List<EnergyHandler> targets, int maxAmount) {
        // Build target list
        List<EnergyTarget> sortableTargets = new ArrayList<>(targets.size());
        for (var target : targets) {
            sortableTargets.add(new EnergyTarget(target));
        }
        // Shuffle for better transfer on average
        Collections.shuffle(sortableTargets);
        // Simulate the transfer for every target in an overarching transaction
        try (var planningTx = Transaction.openRoot()) {
            for (var target : sortableTargets) {
                target.simulationResult = operation.transfer(target.target, maxAmount, planningTx);
            }
        }
        // Sort from low to high result
        sortableTargets.sort(Comparator.comparingLong(t -> t.simulationResult));
        // Actually perform the transfer
        int transferredAmount = 0;
        try (var tx = Transaction.openRoot()) {
            for (int i = 0; i < sortableTargets.size(); ++i) {
                EnergyTarget target = sortableTargets.get(i);
                int remainingTargets = sortableTargets.size() - i;
                int remainingAmount = maxAmount - transferredAmount;
                int targetMaxAmount = Ints.saturatedCast(remainingAmount / remainingTargets);

                transferredAmount += operation.transfer(target.target, targetMaxAmount, tx);
            }
            tx.commit();
        }
        return transferredAmount;
    }

    public interface TransferOperation {
        int transfer(EnergyHandler storage, int maxTransfer, TransactionContext tx);
    }

    private static class EnergyTarget {
        final EnergyHandler target;
        int simulationResult;

        EnergyTarget(EnergyHandler target) {
            this.target = target;
        }
    }

    @Override
    public void appendDebugInfo(StringBuilder out) {
        super.appendDebugInfo(out);
        if (energyStorage == null) {
            out.append("no energy storage\n");
        } else {
            out.append("energy = ").append(energyStorage.getAmountAsInt()).append("\n");
            out.append("max energy = ").append(energyStorage.getCapacityAsInt()).append("\n");
        }
    }
}
