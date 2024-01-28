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
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyCache extends NetworkCache<EnergyHost, EnergyCache> {
    private SimpleEnergyStorage energyStorage = null;

    public EnergyCache(ServerLevel level, List<NetworkNode<EnergyHost, EnergyCache>> nodes) {
        super(level, nodes);
    }

    public int getEnergyStored() {
        combine();
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        combine();
        return energyStorage.getMaxEnergyStored();
    }

    public int insert(int maxAmount, boolean simulate) {
        combine();
        return energyStorage.receiveEnergy(maxAmount, simulate);
    }

    public int extract(int maxAmount, boolean simulate) {
        combine();
        return energyStorage.extractEnergy(maxAmount, simulate);
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

            int nodeEnergy = Math.min(host.getMaxEnergy(), energyStorage.getEnergyStored() / remainingNodes);
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
        List<IEnergyStorage> storages = new ArrayList<>();

        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().addEnergyStorages(storages);
            }
        }

        // Extract
        var remainingCapacity = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
        energyStorage.addEnergyStored(transferForTargets(IEnergyStorage::extractEnergy, storages, remainingCapacity));
        // Insert
        energyStorage.reduceEnergyStored(transferForTargets(IEnergyStorage::receiveEnergy, storages, energyStorage.getEnergyStored()));
    }

    /**
     * Dispatch a transfer operation among a list of targets. Will not modify the list.
     */
    public static int transferForTargets(TransferOperation operation, List<IEnergyStorage> targets, int maxAmount) {
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
            target.simulationResult = operation.transfer(target.target, intMaxAmount, true);
        }
        // Sort from low to high result
        sortableTargets.sort(Comparator.comparingLong(t -> t.simulationResult));
        // Actually perform the transfer
        int transferredAmount = 0;
        for (int i = 0; i < sortableTargets.size(); ++i) {
            EnergyTarget target = sortableTargets.get(i);
            int remainingTargets = sortableTargets.size() - i;
            int remainingAmount = maxAmount - transferredAmount;
            int targetMaxAmount = Ints.saturatedCast(remainingAmount / remainingTargets);

            transferredAmount += operation.transfer(target.target, targetMaxAmount, false);
        }
        return transferredAmount;
    }

    public interface TransferOperation {
        int transfer(IEnergyStorage storage, int maxTransfer, boolean simulate);
    }

    private static class EnergyTarget {
        final IEnergyStorage target;
        int simulationResult;

        EnergyTarget(IEnergyStorage target) {
            this.target = target;
        }
    }

    @Override
    public void appendDebugInfo(StringBuilder out) {
        super.appendDebugInfo(out);
        if (energyStorage == null) {
            out.append("no energy storage\n");
        } else {
            out.append("energy = ").append(energyStorage.getEnergyStored()).append("\n");
            out.append("max energy = ").append(energyStorage.getMaxEnergyStored()).append("\n");
        }
    }
}
