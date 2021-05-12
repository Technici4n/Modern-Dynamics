package dev.technici4n.moderntransportation.network.energy;

import com.google.common.primitives.Ints;
import dev.technici4n.moderntransportation.network.NetworkCache;
import dev.technici4n.moderntransportation.network.NetworkNode;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;

public class EnergyCache extends NetworkCache<EnergyHost, EnergyCache> {
    private long energy = 0;
    private long maxEnergy = 0;

    private final Set<EnergyHost> inventoryConnectionHosts = new HashSet<>();

    public EnergyCache(List<NetworkNode<EnergyHost, EnergyCache>> nodes) {
        super(nodes);

        for (NetworkNode<EnergyHost, EnergyCache> node : nodes) {
            if (node.getHost().hasInventoryConnections()) {
                inventoryConnectionHosts.add(node.getHost());
            }
        }
    }

    public void addInventoryConnectionHost(EnergyHost host) {
        if (!host.hasInventoryConnections()) {
            throw new IllegalArgumentException("Host has no inventory connections!");
        }

        inventoryConnectionHosts.add(host);
    }

    public int getEnergyStored() {
        combine();
        return Ints.saturatedCast(energy);
    }

    public int getMaxEnergyStored() {
        combine();
        return Ints.saturatedCast(maxEnergy);
    }

    public int insertEnergy(int maxAmount, boolean simulate) {
        combine();

        int inserted = (int) Math.min(maxAmount, maxEnergy - energy);

        if (!simulate) {
            energy += inserted;
        }

        return inserted;
    }

    public int extractEnergy(int maxAmount, boolean simulate) {
        combine();

        int extracted = (int) Math.min(maxAmount, energy);

        if (!simulate) {
            energy -= extracted;
        }

        return extracted;
    }

    @Override
    protected void doCombine() {
        // Gather energy from nodes
        for (NetworkNode<EnergyHost, EnergyCache> node : nodes) {
            EnergyHost host = node.getHost();

            energy += host.getEnergy();
            maxEnergy += host.getMaxEnergy();
        }
    }

    @Override
    protected void doSeparate() {
        // Distribute new energy among nodes
        // Start with nodes with the lowest capacity
        nodes.sort(Comparator.comparingInt(node -> node.getHost().getMaxEnergy()));
        int remainingNodes = nodes.size();

        for (NetworkNode<EnergyHost, EnergyCache> node : nodes) {
            EnergyHost host = node.getHost();

            int nodeEnergy = Math.min(host.getMaxEnergy(), Ints.saturatedCast(energy / remainingNodes));
            host.setEnergy(nodeEnergy);
            energy -= nodeEnergy;
            remainingNodes--;
        }

        maxEnergy = 0;
    }

    @Override
    public void doTick() {
        // Make sure the network is combined
        combine();

        // Gather inventory connections
        List<IEnergyStorage> storages = new ArrayList<>();

        for (Iterator<EnergyHost> it = inventoryConnectionHosts.iterator(); it.hasNext();) {
            EnergyHost host = it.next();

            if (host.isTicking()) {
                host.addEnergyStorages(storages);

                if (!host.hasInventoryConnections()) {
                    it.remove();
                }
            }
        }

        // Extract
        energy += transferForTargets(IEnergyStorage::extractEnergy, storages, maxEnergy - energy);
        // Insert
        energy -= transferForTargets(IEnergyStorage::receiveEnergy, storages, energy);
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

    interface TransferOperation {
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
        out.append("energy = ").append(energy).append("\n");
        out.append("max energy = ").append(maxEnergy).append("\n");
        out.append("number of inventory connection hosts = ").append(inventoryConnectionHosts.size()).append("\n");
    }
}
