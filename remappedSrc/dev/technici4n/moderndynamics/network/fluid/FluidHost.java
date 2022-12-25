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
import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.attachment.attached.AttachedIo;
import dev.technici4n.moderndynamics.attachment.attached.FluidAttachedIo;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.NetworkNode;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.shared.TransferLimits;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.TransferUtil;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class FluidHost extends NodeHost {
    private static final NetworkManager<FluidHost, FluidCache> MANAGER = NetworkManager.get(FluidCache.class, FluidCache::new);

    private FluidVariant variant = FluidVariant.blank();
    private long amount = 0;
    // Rate limiting
    // inserted INTO the neighbor inventories
    private final TransferLimits<FluidVariant> insertLimit = new TransferLimits<>(this::getNetworkToOutsideLimit);
    // extracted FROM the neighbor inventories
    private final TransferLimits<FluidVariant> extractLimit = new TransferLimits<>(this::getOutsideToNetworkLimit);
    // Caps
    private final Storage<FluidVariant>[] caps = new Storage[6];

    public FluidHost(PipeBlockEntity pipe) {
        super(pipe);

        for (int i = 0; i < 6; ++i) {
            caps[i] = new NetworkFluidStorage(i);
        }
    }

    @Override
    public NetworkManager<FluidHost, FluidCache> getManager() {
        return MANAGER;
    }

    @Override
    @Nullable
    public Object getApiInstance(BlockApiLookup<?, Direction> lookup, Direction side) {
        if (lookup == FluidStorage.SIDED && (pipe.connectionBlacklist & (1 << side.getId())) == 0) {
            return caps[side.getId()];
        } else {
            return null;
        }
    }

    public long getAmount() {
        return amount;
    }

    public FluidVariant getVariant() {
        return variant;
    }

    public void setContents(FluidVariant variant, long nodeFluid) {
        if (!variant.equals(this.variant) || nodeFluid != this.amount) {
            this.variant = variant;
            this.amount = nodeFluid;

            pipe.markDirty();
            pipe.sync(false);
        }
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
        if (attachment instanceof AttachedIo) {
            return false;
        }
        return super.canConnectTo(connectionDirection, adjacentHost) && hasCompatibleFluid(adjacentHost);
    }

    private boolean hasCompatibleFluid(NodeHost other) {
        return FluidCache.areCompatible(((FluidHost) other).variant, variant);
    }

    @Override
    public void onConnectedTo(NodeHost other) {
        if (other instanceof FluidHost fh && !fh.variant.isBlank()) {
            variant = fh.variant;
            pipe.markDirty();
        }
    }

    @Override
    public void onConnectionRejectedTo(Direction direction, NodeHost other) {
        if (getAttachment(direction) instanceof AttachedIo
                || other.getAttachment(direction.getOpposite()) instanceof AttachedIo) {
            // rejected because of attachment: nothing to do
            return;
        }
        if (!hasCompatibleFluid(other)) {
            // rejected because of incompatible fluid: blacklist this side!
            pipe.connectionBlacklist |= 1 << direction.getId();
            pipe.markDirty();
        }
    }

    void addFluidStorages(List<Storage<FluidVariant>> out) {
        gatherCapabilities(out, externalStorage -> {
            // Make sure that network ticking logic doesn't extract from external storages without a servo
            if (getAttachment(Direction.byId(externalStorage.directionId)) instanceof FluidAttachedIo io) {
                if (io.getType() == IoAttachmentType.EXTRACTOR) {
                    return externalStorage; // servo: nothing to change
                }
            }
            // in all other cases: make the storage insert-only.
            return FilteringStorage.insertOnlyOf(externalStorage);
        });
    }

    public void gatherCapabilities(@Nullable List<Storage<FluidVariant>> out,
            @Nullable Function<ExternalFluidStorage, Storage<FluidVariant>> transformer) {
        if (transformer == null)
            transformer = s -> s;
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.byId(i);
                Storage<FluidVariant> adjacentCap = FluidStorage.SIDED.find(pipe.getWorld(), pipe.getPos().offset(dir), dir.getOpposite());

                if (adjacentCap != null) {
                    if (out != null) {
                        out.add(transformer.apply(new ExternalFluidStorage(adjacentCap, i)));
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
        gatherCapabilities(null, null);

        // Update render
        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putLong("amount", amount);
        tag.put("variant", variant.toNbt());
    }

    @Override
    public void readNbt(NbtCompound tag) {
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
    public void writeClientNbt(NbtCompound tag) {
        super.writeClientNbt(tag);
        tag.putLong("amount", amount);
        tag.put("variant", variant.toNbt());
    }

    @Override
    public void readClientNbt(NbtCompound tag) {
        super.readClientNbt(tag);
        variant = FluidVariant.fromNbt(tag.getCompound("variant"));
        amount = tag.getLong("amount");
    }

    private long getNetworkToOutsideLimit(Direction side, @Nullable FluidVariant variant) {
        if (getAttachment(side) instanceof FluidAttachedIo io) {
            if ((variant != null && !io.matchesFilter(variant)) || !io.isEnabledViaRedstone(pipe)) {
                return 0;
            }
            if (io.getType() == IoAttachmentType.EXTRACTOR)
                return 0;
            if (io.getType() == IoAttachmentType.ATTRACTOR) {
                return io.getFluidMaxIo();
            }
        }
        return Constants.Fluids.BASE_IO;
    }

    private long getOutsideToNetworkLimit(Direction side, @Nullable FluidVariant variant) {
        if (getAttachment(side) instanceof FluidAttachedIo io) {
            if ((variant != null && !io.matchesFilter(variant)) || !io.isEnabledViaRedstone(pipe)) {
                return 0;
            }
            if (io.getType() == IoAttachmentType.ATTRACTOR)
                return 0;
            else if (io.getType() == IoAttachmentType.EXTRACTOR) {
                return io.getFluidMaxIo();
            }
        }
        return Constants.Fluids.BASE_IO;
    }

    private SingleSlotStorage<FluidVariant> getInternalNetworkStorage() {
        NetworkNode<FluidHost, FluidCache> node = findNode();

        if (node != null && node.getHost() == FluidHost.this) {
            return node.getNetworkCache().getOrCreateStorage();
        } else {
            return TransferUtil.EMPTY_SLOT;
        }
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
            maxAmount = insertLimit.limit(directionId, maxAmount, resource);
            if (maxAmount <= 0)
                return 0;

            long transferred = delegate.insert(resource, maxAmount, transaction);
            insertLimit.use(directionId, transferred, transaction);
            return transferred;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            maxAmount = extractLimit.limit(directionId, maxAmount, resource);
            if (maxAmount <= 0)
                return 0;

            long transferred = delegate.extract(resource, maxAmount, transaction);
            extractLimit.use(directionId, transferred, transaction);
            return transferred;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            return Iterators.transform(delegate.iterator(), View::new);
        }

        private class View implements StorageView<FluidVariant> {
            private final StorageView<FluidVariant> view;

            private View(StorageView<FluidVariant> view) {
                this.view = view;
            }

            @Override
            public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                maxAmount = extractLimit.limit(directionId, maxAmount, resource);
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

    private class NetworkFluidStorage implements SingleSlotStorage<FluidVariant> {
        private final int directionId;

        private NetworkFluidStorage(int directionId) {
            this.directionId = directionId;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            // extractLimit because the network is receiving from an adjacent inventory,
            // as if it was extracting from it
            maxAmount = extractLimit.limit(directionId, maxAmount, resource);
            if (maxAmount <= 0)
                return 0;

            long transferred = getInternalNetworkStorage().insert(resource, maxAmount, transaction);
            extractLimit.use(directionId, transferred, transaction);

            return transferred;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            // insertLimit because the network is being extracted from an adjacent inventory,
            // as if it was inserting into it
            maxAmount = insertLimit.limit(directionId, maxAmount, resource);
            if (maxAmount <= 0)
                return 0;

            long transferred = getInternalNetworkStorage().extract(resource, maxAmount, transaction);
            insertLimit.use(directionId, transferred, transaction);

            return transferred;
        }

        @Override
        public boolean isResourceBlank() {
            return getInternalNetworkStorage().isResourceBlank();
        }

        @Override
        public FluidVariant getResource() {
            return getInternalNetworkStorage().getResource();
        }

        @Override
        public long getAmount() {
            return getInternalNetworkStorage().getAmount();
        }

        @Override
        public long getCapacity() {
            return getInternalNetworkStorage().getCapacity();
        }

        @Override
        public StorageView<FluidVariant> getUnderlyingView() {
            return getInternalNetworkStorage().getUnderlyingView();
        }
    }
}
