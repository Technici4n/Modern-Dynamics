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

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.NetworkNode;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.shared.TransferLimits;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class EnergyHost extends NodeHost {
    private static final NetworkManager<EnergyHost, EnergyCache> MANAGER = NetworkManager.get(EnergyCache.class, EnergyCache::new);

    private final EnergyPipeTier tier;
    private int energy;
    // Rate limiting
    // inserted INTO the neighbor inventories
    private final TransferLimits insertLimit = new TransferLimits(this::getTransferLimit, 0);
    // extracted FROM the neighbor inventories
    private final TransferLimits extractLimit = new TransferLimits(this::getTransferLimit, 0);
    // Caps
    private final IEnergyStorage[] caps = new IEnergyStorage[6];
    private final IEnergyStorage unsidedCap = new ReadOnlyNetworkStorage();

    public EnergyHost(PipeBlockEntity pipe, EnergyPipeTier tier) {
        super(pipe);
        this.tier = tier;

        for (int i = 0; i < 6; ++i) {
            caps[i] = new NetworkEnergyStorage(i);
        }
    }

    @Override
    public NetworkManager<EnergyHost, EnergyCache> getManager() {
        return MANAGER;
    }

    @Override
    public Object getApiInstance(BlockCapability<?, Direction> lookup, @Nullable Direction side) {
        if (lookup == Capabilities.EnergyStorage.BLOCK) {
            if (side == null) {
                return unsidedCap;
            } else if ((pipe.connectionBlacklist & (1 << side.get3DDataValue())) == 0) {
                return caps[side.get3DDataValue()];
            }
        }
        return null;
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
        pipe.setChanged();
    }

    @Override
    protected void doUpdate() {
        updateConnections();
    }

    @Override
    public boolean acceptsAttachment(AttachmentItem attachment, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canConnectTo(Direction connectionDirection, NodeHost adjacentHost) {
        return super.canConnectTo(connectionDirection, adjacentHost) && ((EnergyHost) adjacentHost).tier == this.tier;
    }

    protected void addEnergyStorages(List<IEnergyStorage> out) {
        gatherCapabilities(out);
    }

    public void gatherCapabilities(@Nullable List<IEnergyStorage> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                IEnergyStorage adjacentCap = pipe.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, pipe.getBlockPos().relative(dir),
                        dir.getOpposite());

                if (adjacentCap != null) {
                    if (out != null) {
                        out.add(new ExternalEnergyStorage(adjacentCap, i));
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

        // Compute new connections (excluding existing adjacent pipe connections, and the blacklist)
        inventoryConnections = (1 << 6) - 1 - (pipeConnections | pipe.connectionBlacklist);
        gatherCapabilities(null);

        // Update render
        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeNbt(tag, registries);
        tag.putInt("energy", energy);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.readNbt(tag, registries);
        // Guard against max energy config changes
        energy = Math.max(0, Math.min(tag.getInt("energy"), getMaxEnergy()));
    }

    private int getTransferLimit(Direction side) {
        return tier.getMaxConnectionTransfer();
    }

    private class ExternalEnergyStorage implements IEnergyStorage {
        private final int directionId;
        private final IEnergyStorage delegate;

        private ExternalEnergyStorage(IEnergyStorage delegate, int directionId) {
            this.delegate = delegate;
            this.directionId = directionId;
        }

        @Override
        public int receiveEnergy(int maxAmount, boolean simulate) {
            maxAmount = insertLimit.limit(directionId, maxAmount);
            if (maxAmount <= 0)
                return 0;

            int transferred = delegate.receiveEnergy(maxAmount, simulate);
            if (!simulate) {
                insertLimit.use(directionId, transferred);
            }
            return transferred;
        }

        @Override
        public int extractEnergy(int maxAmount, boolean simulate) {
            maxAmount = extractLimit.limit(directionId, maxAmount);
            if (maxAmount <= 0)
                return 0;

            int transferred = delegate.extractEnergy(maxAmount, simulate);
            if (!simulate) {
                extractLimit.use(directionId, transferred);
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

    private abstract class AbstractNetworkStorage implements IEnergyStorage {
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
    }

    private class NetworkEnergyStorage extends AbstractNetworkStorage {
        private final int directionId;

        private NetworkEnergyStorage(int directionId) {
            this.directionId = directionId;
        }

        @Override
        public boolean canExtract() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();
            return node != null && node.getHost() == EnergyHost.this;
        }

        @Override
        public boolean canReceive() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();
            return node != null && node.getHost() == EnergyHost.this;
        }

        @Override
        public int receiveEnergy(int maxAmount, boolean simulate) {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                // extractLimit because the network is receiving from an adjacent inventory,
                // as if it was extracting from it
                maxAmount = extractLimit.limit(directionId, maxAmount);
                if (maxAmount <= 0)
                    return 0;

                int transferred = node.getNetworkCache().insert(maxAmount, simulate);
                if (!simulate) {
                    extractLimit.use(directionId, transferred);
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
                // insertLimit because the network is being extracted from an adjacent inventory,
                // as if it was inserting into it
                maxAmount = insertLimit.limit(directionId, maxAmount);
                if (maxAmount <= 0)
                    return 0;

                int transferred = node.getNetworkCache().extract(maxAmount, simulate);
                if (!simulate) {
                    insertLimit.use(directionId, transferred);
                }

                return transferred;
            }

            return 0;
        }
    }

    private class ReadOnlyNetworkStorage extends AbstractNetworkStorage {
        @Override
        public int receiveEnergy(int maxAmount, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }
    }
}
