package dev.technici4n.moderntransportation.network.energy;

import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import dev.technici4n.moderntransportation.init.MtPipes;
import dev.technici4n.moderntransportation.network.NetworkManager;
import dev.technici4n.moderntransportation.network.NetworkNode;
import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.TickHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnergyHost extends NodeHost {
    private static final NetworkManager<EnergyHost, EnergyCache> MANAGER = NetworkManager.get(EnergyCache.class);

    private final EnergyPipeTier tier;
    private int energy;
    public int inventoryConnections = 0;
    // Rate limiting
    private long lastRateUpdate = 0;
    private final int[] insertedEnergy = new int[6]; // inserted INTO the neighbor inventories
    private final int[] extractedEnergy = new int[6]; // extracted FROM the neighor inventories
    // Caps
    @SuppressWarnings({"unchecked"})
    private final LazyOptional<IEnergyStorage>[] caps = new LazyOptional[6];

    public EnergyHost(PipeBlockEntity pipe, EnergyPipeTier tier) {
        super(pipe);
        this.tier = tier;

        for (int i = 0; i < 6; ++i) {
            int iCopy = i;
            caps[i] = LazyOptional.of(() -> new NetworkEnergyStorage(iCopy));
        }
    }

    @Override
    public NetworkManager<?, ?> getManager() {
        return MANAGER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        // we don't check for inventoryConnections here or it would cause "deadlocks" when two pipes of different tiers
        // try to connect to each other.
        if (capability == CapabilityEnergy.ENERGY && side != null) {
            return caps[side.getId()].cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public void invalidateCapabilities() {
        for (int i = 0; i < 6; ++i) {
            caps[i].invalidate();
        }
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return tier.getCapacity();
    }

    public void setEnergy(int energy) {
        if (energy < 0 || energy > getMaxEnergy()) {
            throw new IllegalArgumentException("Invalid energy value " + energy);
        }

        this.energy = energy;
        pipe.markDirty();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void doUpdate() {
        updateConnections();

        if (hasInventoryConnections()) {
            NetworkNode<EnergyHost, EnergyCache> node = findNode();
            node.getNetworkCache().addInventoryConnectionHost(this);
        }
    }

    @Override
    public boolean canConnectTo(Direction connectionDirection, NodeHost adjacentHost) {
        return ((EnergyHost) adjacentHost).tier == this.tier;
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
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
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

        // Compute new connections (exluding existing adjacent pipe connections)
        inventoryConnections = (1 << 6) - 1 - pipeConnections;
        gatherCapabilities(null);

        // Update render
        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putInt("energy", energy);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        // Guard against max energy config changes
        energy = Math.max(0, Math.min(tag.getInt("energy"), getMaxEnergy()));
    }

    private void updateRateLimits() {
        long currentTick = TickHelper.getTickCounter();

        if (currentTick > lastRateUpdate) {
            lastRateUpdate = currentTick;

            for (int i = 0; i < 6; ++i) {
                extractedEnergy[i] = insertedEnergy[i] = 0;
            }
        }
    }

    private class ExternalEnergyStorage implements IEnergyStorage {
        private final IEnergyStorage delegate;
        private final int directionId;

        private ExternalEnergyStorage(IEnergyStorage delegate, Direction direction) {
            this.delegate = delegate;
            this.directionId = direction.getId();
        }

        @Override
        public int receiveEnergy(int maxAmount, boolean simulate) {
            updateRateLimits();
            maxAmount = Math.min(maxAmount, tier.getMaxConnectionTransfer() - insertedEnergy[directionId]);
            if (maxAmount <= 0) return 0;

            int transferred = delegate.receiveEnergy(maxAmount, simulate);

            if (!simulate) {
                insertedEnergy[directionId] += transferred;
            }

            return transferred;
        }

        @Override
        public int extractEnergy(int maxAmount, boolean simulate) {
            updateRateLimits();
            maxAmount = Math.min(maxAmount, tier.getMaxConnectionTransfer() - extractedEnergy[directionId]);
            if (maxAmount <= 0) return 0;

            int transferred = delegate.extractEnergy(maxAmount, simulate);

            if (!simulate) {
                extractedEnergy[directionId] += transferred;
            }

            return transferred;
        }

        @Override
        public int getEnergyStored() {
            return delegate.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return delegate.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return delegate.canExtract();
        }

        @Override
        public boolean canReceive() {
            return delegate.canReceive();
        }
    }

    private class NetworkEnergyStorage implements IEnergyStorage {
        private final int directionId;

        private NetworkEnergyStorage(int directionId) {
            this.directionId = directionId;
        }

        @Override
        public int receiveEnergy(int maxAmount, boolean simulate) {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                updateRateLimits();
                // extractedEnergy because the network is receiving from an adjacent inventory,
                // as if it was extracting from it
                maxAmount = Math.min(maxAmount, tier.getMaxConnectionTransfer() - extractedEnergy[directionId]);
                if (maxAmount <= 0) return 0;

                int transferred = node.getNetworkCache().insertEnergy(maxAmount, simulate);

                if (!simulate) {
                    extractedEnergy[directionId] += transferred;
                }

                return transferred;
            }

            return 0;
        }

        @Override
        public int extractEnergy(int maxAmount, boolean simulate) {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                updateRateLimits();
                // insertedEnergy because the network is being extracted from an adjacent inventory,
                // as if it was inserting into it
                maxAmount = Math.min(maxAmount, tier.getMaxConnectionTransfer() - insertedEnergy[directionId]);
                if (maxAmount <= 0) return 0;

                int transferred = node.getNetworkCache().extractEnergy(maxAmount, simulate);

                if (!simulate) {
                    insertedEnergy[directionId] += transferred;
                }

                return transferred;
            }

            return 0;
        }

        @Override
        public int getEnergyStored() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                return node.getNetworkCache().getEnergyStored();
            }

            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                return node.getNetworkCache().getMaxEnergyStored();
            }

            return 0;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
