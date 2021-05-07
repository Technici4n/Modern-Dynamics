package dev.technici4n.moderntransportation.impl.energy;

import dev.technici4n.moderntransportation.api.energy.IEnergyCache;
import dev.technici4n.moderntransportation.api.energy.IEnergyHost;
import dev.technici4n.moderntransportation.api.network.INetworkManager;
import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.EnumSet;
import java.util.List;

public class EnergyHost implements IEnergyHost {
    private static final INetworkManager<IEnergyHost, IEnergyCache> MANAGER = INetworkManager.get(IEnergyCache.class);

    private final PipeBlockEntity be;
    private final int maxEnergy;
    private int energy;
    public EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);

    public EnergyHost(PipeBlockEntity be, int maxEnergy) {
        this.be = be;
        this.maxEnergy = maxEnergy;
    }

    public void addSelf() {
        MANAGER.addNode((ServerWorld) be.getWorld(), be.getPos(), this);
    }

    public void removeSelf() {
        MANAGER.removeNode((ServerWorld) be.getWorld(), be.getPos(), this);
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public int getMaxEnergy() {
        return maxEnergy;
    }

    @Override
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
        be.markDirty();
    }

    @Override
    public void addEnergyStorages(List<IEnergyStorage> out) {
        for (Direction direction : EnumSet.complementOf(connections)) {
            @SuppressWarnings("ConstantConditions")
            BlockEntity adjBe = be.getWorld().getBlockEntity(be.getPos().offset(direction));

            if (adjBe != null) {
                @SuppressWarnings("ConstantConditions") // the null causes a warning due to @ParametersAreNonnullByDefault
                IEnergyStorage energyStorage = adjBe.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).orElse(null);

                if (energyStorage != null) {
                    out.add(energyStorage);
                }
            }
        }
    }

    @Override
    public EnumSet<Direction> getAllowedNodeConnections() {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public void setConnections(EnumSet<Direction> connections) {
        this.connections = connections;
        be.sync();
    }
}
