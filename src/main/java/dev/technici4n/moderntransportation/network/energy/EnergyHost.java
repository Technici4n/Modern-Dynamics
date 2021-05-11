package dev.technici4n.moderntransportation.network.energy;

import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import dev.technici4n.moderntransportation.network.CapabilityConnections;
import dev.technici4n.moderntransportation.network.NetworkManager;
import dev.technici4n.moderntransportation.network.NodeHost;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyHost extends NodeHost {
    private static final NetworkManager<EnergyHost, EnergyCache> MANAGER = NetworkManager.get(EnergyCache.class);

    private final int maxEnergy;
    private int energy;
    public final CapabilityConnections<IEnergyStorage> inventoryConnections = new CapabilityConnections<>(CapabilityEnergy.ENERGY, this);

    public EnergyHost(PipeBlockEntity pipe, int maxEnergy) {
        super(pipe);
        this.maxEnergy = maxEnergy;
    }

    @Override
    public NetworkManager<?, ?> getManager() {
        return MANAGER;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setEnergy(int energy) {
        setEnergy(energy, true);
    }

    public void setEnergy(int energy, boolean hardFail) {
        if (energy < 0 || energy > maxEnergy) {
            if (hardFail) {
                throw new IllegalArgumentException("Invalid energy value " + energy);
            } else {
                energy = Math.max(0, Math.min(energy, maxEnergy));
            }
        }

        this.energy = energy;
        pipe.markDirty();
    }

    @Override
    protected void doUpdate() {
        inventoryConnections.updateConnections();

        if (hasInventoryConnections()) {
            ((EnergyCache) findNode().getNetworkCache()).addInventoryConnectionHost(this);
        }
    }

    protected void addEnergyStorages(List<IEnergyStorage> out) {
        inventoryConnections.gatherCapabilities(out);
    }

    public boolean hasInventoryConnections() {
        return inventoryConnections.getConnectionMask() != 0;
    }
}
