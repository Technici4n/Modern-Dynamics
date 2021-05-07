package dev.technici4n.moderntransportation.impl.energy;

import com.google.common.primitives.Ints;
import dev.technici4n.moderntransportation.api.energy.IEnergyCache;
import dev.technici4n.moderntransportation.api.energy.IEnergyHost;
import dev.technici4n.moderntransportation.api.network.INetworkNode;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EnergyCache implements IEnergyCache {
    private final List<? extends INetworkNode<IEnergyHost, IEnergyCache>> nodes;

    public EnergyCache(List<? extends INetworkNode<IEnergyHost, IEnergyCache>> nodes) {
        this.nodes = nodes;
    }

    @Override
    public void tick() {
        List<IEnergyStorage> storages = new ArrayList<>();

        // Gather targets and energy
        long energy = 0;
        long maxEnergy = 0;
        int tickingNodes = 0;

        for (INetworkNode<IEnergyHost, IEnergyCache> node : nodes) {
            if (node.isTicking()) {
                IEnergyHost host = node.getHost();

                host.addEnergyStorages(storages);
                energy += host.getEnergy();
                maxEnergy += host.getMaxEnergy();
                tickingNodes++;
            }
        }

        // Extract
        energy += transferForTargets(IEnergyStorage::extractEnergy, storages, maxEnergy - energy);
        // Insert
        energy -= transferForTargets(IEnergyStorage::receiveEnergy, storages, energy);

        // Distribute new energy among nodes
        // Start with nodes with the lowest capacity
        nodes.sort(Comparator.comparingInt(node -> node.getHost().getMaxEnergy()));

        for (INetworkNode<IEnergyHost, IEnergyCache> node : nodes) {
            if (node.isTicking()) {
                IEnergyHost host = node.getHost();

                int nodeEnergy = Math.max(host.getMaxEnergy(), Ints.saturatedCast(energy / tickingNodes));
                host.setEnergy(nodeEnergy);
                energy -= nodeEnergy;
                tickingNodes--;
            }
        }
    }

    /**
     * Dispatch a transfer operation among a list of targets. Will not modify the list.
     */
    private static long transferForTargets(TransferOperation operation, List<IEnergyStorage> targets, long maxAmount) {
        int intMaxAmount = Ints.saturatedCast(maxAmount);
        // Build target list
        List<EnergyTarget> sortableTargets = new ArrayList<>(targets.size());
        for (IEnergyStorage target : targets) {
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
        long transferredAmount = 0;
        for (int i = 0; i < sortableTargets.size(); ++i) {
            EnergyTarget target = sortableTargets.get(i);
            int remainingTargets = sortableTargets.size() - i;
            long remainingAmount = maxAmount - transferredAmount;
            int targetMaxAmount = Ints.saturatedCast(remainingAmount / remainingTargets);

            transferredAmount += operation.transfer(target.target, targetMaxAmount, false);
        }
        return transferredAmount;
    }

    private interface TransferOperation {
        int transfer(IEnergyStorage storage, int maxTransfer, boolean simulate);
    }

    private static class EnergyTarget {
        final IEnergyStorage target;
        int simulationResult;

        EnergyTarget(IEnergyStorage target) {
            this.target = target;
        }
    }
}
