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
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.DelegatingEnergyStorage;

public class EnergyHost extends NodeHost {
    private static final NetworkManager<EnergyHost, EnergyCache> MANAGER = NetworkManager.get(EnergyCache.class, EnergyCache::new);

    private final EnergyPipeTier tier;
    private long energy;
    // Rate limiting
    // inserted INTO the neighbor inventories
    private final TransferLimits insertLimit = new TransferLimits(this::getTransferLimit, 0);
    // extracted FROM the neighbor inventories
    private final TransferLimits extractLimit = new TransferLimits(this::getTransferLimit, 0);
    // Caps
    private final EnergyStorage[] caps = new EnergyStorage[6];

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
    public Object getApiInstance(BlockApiLookup<?, Direction> lookup, Direction side) {
        if (lookup == EnergyStorage.SIDED && (pipe.connectionBlacklist & (1 << side.get3DDataValue())) == 0) {
            return caps[side.get3DDataValue()];
        } else {
            return null;
        }
    }

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
        return tier.getCapacity();
    }

    public void setEnergy(long energy) {
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

    protected void addEnergyStorages(List<EnergyStorage> out) {
        gatherCapabilities(out);
    }

    public void gatherCapabilities(@Nullable List<EnergyStorage> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                EnergyStorage adjacentCap = EnergyStorage.SIDED.find(pipe.getLevel(), pipe.getBlockPos().relative(dir), dir.getOpposite());

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
    public void writeNbt(CompoundTag tag) {
        super.writeNbt(tag);
        tag.putLong("energy", energy);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        super.readNbt(tag);
        // Guard against max energy config changes
        energy = Math.max(0, Math.min(tag.getLong("energy"), getMaxEnergy()));
    }

    private long getTransferLimit(Direction side) {
        return tier.getMaxConnectionTransfer();
    }

    private class ExternalEnergyStorage extends DelegatingEnergyStorage {
        private final int directionId;

        private ExternalEnergyStorage(EnergyStorage delegate, int directionId) {
            super(delegate, null);
            this.directionId = directionId;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            maxAmount = insertLimit.limit(directionId, maxAmount);
            if (maxAmount <= 0)
                return 0;

            long transferred = backingStorage.get().insert(maxAmount, transaction);
            insertLimit.use(directionId, transferred, transaction);
            return transferred;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            maxAmount = extractLimit.limit(directionId, maxAmount);
            if (maxAmount <= 0)
                return 0;

            long transferred = backingStorage.get().extract(maxAmount, transaction);
            extractLimit.use(directionId, transferred, transaction);
            return transferred;
        }
    }

    private class NetworkEnergyStorage implements EnergyStorage {
        private final int directionId;

        private NetworkEnergyStorage(int directionId) {
            this.directionId = directionId;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                // extractLimit because the network is receiving from an adjacent inventory,
                // as if it was extracting from it
                maxAmount = extractLimit.limit(directionId, maxAmount);
                if (maxAmount <= 0)
                    return 0;

                long transferred = node.getNetworkCache().insert(maxAmount, transaction);
                extractLimit.use(directionId, transferred, transaction);

                return transferred;
            }

            return 0;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                // insertLimit because the network is being extracted from an adjacent inventory,
                // as if it was inserting into it
                maxAmount = insertLimit.limit(directionId, maxAmount);
                if (maxAmount <= 0)
                    return 0;

                long transferred = node.getNetworkCache().extract(maxAmount, transaction);
                insertLimit.use(directionId, transferred, transaction);

                return transferred;
            }

            return 0;
        }

        @Override
        public long getAmount() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                return node.getNetworkCache().getAmount();
            }

            return 0;
        }

        @Override
        public long getCapacity() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                return node.getNetworkCache().getCapacity();
            }

            return 0;
        }
    }
}
