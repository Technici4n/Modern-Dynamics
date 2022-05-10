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
package dev.technici4n.moderndynamics.network.fluid;

import com.google.common.collect.Iterators;
import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.TickHelper;
import dev.technici4n.moderndynamics.network.shared.TransferLimits;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FluidHost extends NodeHost {
    private static final NetworkManager<FluidHost, FluidCache> MANAGER = NetworkManager.get(FluidCache.class, FluidCache::new);

    private FluidVariant variant = FluidVariant.blank();
    private long amount = 0;
    // Rate limiting
    private long lastRateUpdate = 0;
    private final TransferLimits insertLimit = new TransferLimits(); // inserted INTO the neighbor inventories
    private final TransferLimits extractLimit = new TransferLimits(); // extracted FROM the neighbor inventories
    // Caps
    // private final Storage<FluidVariant>[] caps = new Storage[6];

    public FluidHost(PipeBlockEntity pipe) {
        super(pipe);
    }

    @Override
    public NetworkManager<FluidHost, FluidCache> getManager() {
        return MANAGER;
    }

    @Override
    @Nullable
    public Object getApiInstance(BlockApiLookup<?, Direction> lookup, Direction side) {
        return null;
    }

    public long getAmount() {
        return amount;
    }

    public FluidVariant getVariant() {
        return variant;
    }

    public void setContents(FluidVariant variant, long nodeFluid) {
        this.variant = variant;
        this.amount = nodeFluid;

        pipe.setChanged();
    }

    @Override
    protected void doUpdate() {
        updateConnections();
    }

    @Override
    public boolean acceptsAttachment(AttachmentItem attachment, ItemStack stack) {
        return attachment instanceof IoAttachmentItem;
    }

    @Override
    public boolean canConnectTo(Direction connectionDirection, NodeHost adjacentHost) {
        var attachment = getAttachment(connectionDirection);
        if (attachment instanceof ItemAttachedIo) {
            return false;
        }
        // TODO: should check the entire networks somehow, not just the two hosts!
        return super.canConnectTo(connectionDirection, adjacentHost) && FluidCache.areCompatible(((FluidHost) adjacentHost).variant, variant);
    }

    void addFluidStorages(List<Storage<FluidVariant>> out) {
        gatherCapabilities(out);
    }

    public void gatherCapabilities(@Nullable List<Storage<FluidVariant>> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                Storage<FluidVariant> adjacentCap = FluidStorage.SIDED.find(pipe.getLevel(), pipe.getBlockPos().relative(dir), dir.getOpposite());

                if (adjacentCap != null) {
                    if (out != null) {
                        out.add(new ExternalFluidStorage(adjacentCap, i));
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
        tag.putLong("amount", amount);
        tag.put("variant", variant.toNbt());
    }

    @Override
    public void readNbt(CompoundTag tag) {
        super.readNbt(tag);
        variant = FluidVariant.fromNbt(tag.getCompound("variant"));
        // Guard against max changes
        amount = Math.max(0, Math.min(tag.getLong("amount"), Constants.Fluids.CAPACITY));
        // Guard against removed variant
        if (variant.isBlank()) {
            amount = 0;
        }
    }

    @Override
    public void writeClientNbt(CompoundTag tag) {
        super.writeClientNbt(tag);
        tag.putLong("amount", amount);
        tag.put("variant", variant.toNbt());
    }

    @Override
    public void readClientNbt(CompoundTag tag) {
        super.readClientNbt(tag);
        variant = FluidVariant.fromNbt(tag.getCompound("variant"));
        amount = tag.getLong("amount");
    }

    private void updateRateLimits() {
        long currentTick = TickHelper.getTickCounter();

        if (currentTick > lastRateUpdate) {
            lastRateUpdate = currentTick;
            extractLimit.reset();
            insertLimit.reset();
        }
    }

    private long getNetworkToOutsideLimit(Direction side) {
        if (getAttachment(side) instanceof ItemAttachedIo io) {
            if (io.getType() == IoAttachmentType.SERVO)
                return 0;
            if (io.getType() == IoAttachmentType.RETRIEVER) {
                return Constants.Fluids.BASE_IO << io.getTier().speedupFactor;
            }
        }
        return Constants.Fluids.BASE_IO;
    }

    private long getOutsideToNetworkLimit(Direction side) {
        if (getAttachment(side) instanceof ItemAttachedIo io) {
            if (io.getType() == IoAttachmentType.SERVO) {
                return Constants.Fluids.BASE_IO << io.getTier().speedupFactor;
            }
        }
        return 0;
    }

    private class ExternalFluidStorage implements Storage<FluidVariant> {
        private final int directionId;
        private final Storage<FluidVariant> delegate;

        ExternalFluidStorage(Storage<FluidVariant> delegate, int directionId) {
            this.delegate = delegate;
            this.directionId = directionId;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            updateRateLimits();
            maxAmount = Math.min(maxAmount, getNetworkToOutsideLimit(Direction.from3DDataValue(directionId)) - insertLimit.used[directionId]);
            if (maxAmount <= 0)
                return 0;

            long transferred = delegate.insert(resource, maxAmount, transaction);
            insertLimit.use(directionId, transferred, transaction);
            return transferred;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            updateRateLimits();
            maxAmount = Math.min(maxAmount, getOutsideToNetworkLimit(Direction.from3DDataValue(directionId)) - extractLimit.used[directionId]);
            if (maxAmount <= 0)
                return 0;

            long transferred = delegate.extract(resource, maxAmount, transaction);
            extractLimit.use(directionId, transferred, transaction);
            return transferred;
        }

        @Override
        public Iterator<? extends StorageView<FluidVariant>> iterator(TransactionContext transaction) {
            return Iterators.transform(delegate.iterator(transaction), View::new);
        }

        private class View implements StorageView<FluidVariant> {
            private final StorageView<FluidVariant> view;

            private View(StorageView<FluidVariant> view) {
                this.view = view;
            }

            @Override
            public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                updateRateLimits();
                maxAmount = Math.min(maxAmount, getOutsideToNetworkLimit(Direction.from3DDataValue(directionId)) - extractLimit.used[directionId]);
                if (maxAmount <= 0)
                    return 0;

                long transferred = view.extract(resource, maxAmount, transaction);
                extractLimit.use(directionId, transferred, transaction);
                return transferred;
            }

            @Override
            public boolean isResourceBlank() {
                return view.isResourceBlank();
            }

            @Override
            public FluidVariant getResource() {
                return view.getResource();
            }

            @Override
            public long getAmount() {
                return view.getAmount();
            }

            @Override
            public long getCapacity() {
                return view.getCapacity();
            }

            @Override
            public StorageView<FluidVariant> getUnderlyingView() {
                return view.getUnderlyingView();
            }
        }
    }
}
