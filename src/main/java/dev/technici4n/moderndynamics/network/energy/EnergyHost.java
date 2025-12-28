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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.DelegatingEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

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
    private final EnergyHandler[] caps = new EnergyHandler[6];
    private final EnergyHandler unsidedCap = new ReadOnlyNetworkStorage();

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
        if (lookup == Capabilities.Energy.BLOCK) {
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

    protected void addEnergyStorages(List<EnergyHandler> out) {
        gatherCapabilities(out);
    }

    public void gatherCapabilities(@Nullable List<EnergyHandler> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                EnergyHandler adjacentCap = pipe.getLevel().getCapability(Capabilities.Energy.BLOCK,
                        pipe.getBlockPos().relative(dir), dir.getOpposite());

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
    public void write(ValueOutput output) {
        super.write(output);
        output.putInt("energy", energy);
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        // Guard against max energy config changes
        energy = Math.max(0, Math.min(input.getIntOr("energy", 0), getMaxEnergy()));
    }

    private int getTransferLimit(Direction side) {
        return tier.getMaxConnectionTransfer();
    }

    private class ExternalEnergyStorage extends DelegatingEnergyHandler {
        private final int directionId;

        private ExternalEnergyStorage(EnergyHandler delegate, int directionId) {
            super(delegate);
            this.directionId = directionId;
        }

        @Override
        public int insert(int amount, TransactionContext tx) {
            TransferPreconditions.checkNonNegative(amount);
            amount = insertLimit.limit(directionId, amount);
            if (amount <= 0)
                return 0;

            int transferred = super.insert(amount, tx);
            if (transferred > 0) {
                insertLimit.updateSnapshots(tx);
                insertLimit.use(directionId, transferred);
            }
            return transferred;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            TransferPreconditions.checkNonNegative(amount);
            amount = extractLimit.limit(directionId, amount);
            if (amount <= 0)
                return 0;

            int transferred = super.extract(amount, tx);
            if (transferred > 0) {
                extractLimit.updateSnapshots(tx);
                extractLimit.use(directionId, transferred);
            }
            return transferred;
        }
    }

    private abstract class AbstractNetworkStorage implements EnergyHandler {
        @Override
        public long getAmountAsLong() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                return node.getNetworkCache().getStorageAmount();
            }

            return 0;
        }

        @Override
        public long getCapacityAsLong() {
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                return node.getNetworkCache().getStorageCapacity();
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
        public int insert(int amount, TransactionContext tx) {
            TransferPreconditions.checkNonNegative(amount);
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                // extractLimit because the network is receiving from an adjacent inventory,
                // as if it was extracting from it
                amount = extractLimit.limit(directionId, amount);
                if (amount <= 0)
                    return 0;

                int transferred = node.getNetworkCache().insert(amount, tx);
                if (transferred > 0) {
                    extractLimit.updateSnapshots(tx);
                    extractLimit.use(directionId, transferred);
                }

                return transferred;
            }

            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            TransferPreconditions.checkNonNegative(amount);
            @Nullable
            NetworkNode<EnergyHost, EnergyCache> node = findNode();

            if (node != null && node.getHost() == EnergyHost.this) {
                // insertLimit because the network is being extracted from an adjacent inventory,
                // as if it was inserting into it
                amount = insertLimit.limit(directionId, amount);
                if (amount <= 0)
                    return 0;

                int transferred = node.getNetworkCache().extract(amount, tx);
                if (transferred > 0) {
                    insertLimit.updateSnapshots(tx);
                    insertLimit.use(directionId, transferred);
                }

                return transferred;
            }

            return 0;
        }
    }

    private class ReadOnlyNetworkStorage extends AbstractNetworkStorage {
        @Override
        public int insert(int amount, TransactionContext tx) {
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            return 0;
        }
    }
}
