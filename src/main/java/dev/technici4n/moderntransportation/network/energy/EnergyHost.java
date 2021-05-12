package dev.technici4n.moderntransportation.network.energy;

import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import dev.technici4n.moderntransportation.network.NetworkManager;
import dev.technici4n.moderntransportation.network.NodeHost;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnergyHost extends NodeHost {
    private static final NetworkManager<EnergyHost, EnergyCache> MANAGER = NetworkManager.get(EnergyCache.class);

    private final int maxEnergy;
    private int energy;
    public int inventoryConnections = 0;

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
        updateConnections();

        if (hasInventoryConnections()) {
            ((EnergyCache) findNode().getNetworkCache()).addInventoryConnectionHost(this);
        }
    }

    protected void addEnergyStorages(List<IEnergyStorage> out) {
        gatherCapabilities(out);
    }

    public boolean hasInventoryConnections() {
        return inventoryConnections != 0;
    }

    public void gatherCapabilities(@Nullable List<IEnergyStorage> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0) {
                Direction dir = Direction.byId(i);
                @SuppressWarnings("ConstantConditions")
                BlockEntity adjacentBe = pipe.getWorld().getBlockEntity(pipe.getPos().offset(dir));
                IEnergyStorage adjacentCap = null;

                if (adjacentBe != null) {
                    adjacentCap = adjacentBe.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite()).orElse(null);
                }

                if (adjacentCap != null) {
                    if (out != null) {
                        out.add(adjacentCap);
                    }
                } else {
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

        // Compute new connections
        inventoryConnections = (1 << 6) - 1;
        gatherCapabilities(null);

        // Update render
        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }
}
