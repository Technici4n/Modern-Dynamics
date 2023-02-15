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
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.level.ServerLevel;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class EnergyCache extends NetworkCache<EnergyHost, EnergyCache> {
    private SimpleEnergyStorage energyStorage = null;

    public EnergyCache(ServerLevel level, List<NetworkNode<EnergyHost, EnergyCache>> nodes) {
        super(level, nodes);
    }

    public long getAmount() {
        combine();
        return energyStorage.getAmount();
    }

    public long getCapacity() {
        combine();
        return energyStorage.getCapacity();
    }

    public long insert(long maxAmount, TransactionContext transaction) {
        combine();
        return energyStorage.insert(maxAmount, transaction);
    }

    public long extract(long maxAmount, TransactionContext transaction) {
        combine();
        return energyStorage.extract(maxAmount, transaction);
    }

    @Override
    protected void doCombine() {
        // Gather energy from nodes
        long energy = 0;
        long maxEnergy = 0;

        for (NetworkNode<EnergyHost, EnergyCache> node : nodes) {
            EnergyHost host = node.getHost();

            energy += host.getEnergy();
            maxEnergy += host.getMaxEnergy();
        }

        energyStorage = new SimpleEnergyStorage(maxEnergy, Long.MAX_VALUE, Long.MAX_VALUE);
        energyStorage.amount = energy;
    }

    @Override
    protected void doSeparate() {
        if (Transaction.isOpen()) {
            throw new IllegalStateException("Can't separate a network when a transaction is open!");
        }

        // Distribute new energy among nodes
        // Start with nodes with the lowest capacity
        nodes.sort(Comparator.comparingLong(node -> node.getHost().getMaxEnergy()));
        int remainingNodes = nodes.size();

        for (NetworkNode<EnergyHost, EnergyCache> node : nodes) {
            EnergyHost host = node.getHost();

            long nodeEnergy = Math.min(host.getMaxEnergy(), energyStorage.amount / remainingNodes);
            host.setEnergy(nodeEnergy);
            energyStorage.amount -= nodeEnergy;
            remainingNodes--;
        }

        energyStorage = null;
    }

    @Override
    public void doTick() {
        // Make sure the network is combined
        combine();

        // Gather inventory connections
        List<EnergyStorage> storages = new ArrayList<>();

        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().addEnergyStorages(storages);
            }
        }

        try (var tx = Transaction.openOuter()) {
            // Extract
            energyStorage.amount += transferForTargets(EnergyStorage::extract, storages, energyStorage.capacity - energyStorage.amount, tx);
            // Insert
            energyStorage.amount -= transferForTargets(EnergyStorage::insert, storages, energyStorage.amount, tx);

            tx.commit();
        }
    }

    /**
     * Dispatch a transfer operation among a list of targets. Will not modify the list.
     */
    public static long transferForTargets(TransferOperation operation, List<EnergyStorage> targets, long maxAmount, Transaction tx) {
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
                target.simulationResult = operation.transfer(target.target, intMaxAmount, simulation);
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

            transferredAmount += operation.transfer(target.target, targetMaxAmount, tx);
        }
        return transferredAmount;
    }

    public interface TransferOperation {
        long transfer(EnergyStorage storage, long maxTransfer, TransactionContext transaction);
    }

    private static class EnergyTarget {
        final EnergyStorage target;
        long simulationResult;

        EnergyTarget(EnergyStorage target) {
            this.target = target;
        }
    }

    @Override
    public void appendDebugInfo(StringBuilder out) {
        super.appendDebugInfo(out);
        if (energyStorage == null) {
            out.append("no energy storage\n");
        } else {
            out.append("energy = ").append(energyStorage.amount).append("\n");
            out.append("max energy = ").append(energyStorage.capacity).append("\n");
        }
    }
}
