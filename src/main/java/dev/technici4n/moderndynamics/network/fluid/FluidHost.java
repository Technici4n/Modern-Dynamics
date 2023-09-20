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
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FluidHost extends NodeHost {
    private static final NetworkManager<FluidHost, FluidCache> MANAGER = NetworkManager.get(FluidCache.class, FluidCache::new);

    private FluidVariant variant = FluidVariant.blank();
    private long amount = 0;
    private final TransferLimits extractorLimit = new TransferLimits(side -> {
        if (!(getAttachment(side) instanceof FluidAttachedIo io) || io.getType() != IoAttachmentType.EXTRACTOR) {
            return 0;
        }

        return io.getFluidMaxIo();
    }, FluidConstants.BUCKET);
    // Caps
    private final Storage<FluidVariant>[] caps = new Storage[6];
    private final Storage<FluidVariant> unsidedCap;

    public FluidHost(PipeBlockEntity pipe) {
        super(pipe);

        for (int i = 0; i < 6; ++i) {
            var dir = Direction.from3DDataValue(i);
            caps[i] = new FilteringStorage<>(this::getInternalNetworkStorage) {
                @Override
                protected boolean canInsert(FluidVariant resource) {
                    return canMoveOutsideToNetwork(dir, resource);
                }

                @Override
                protected boolean canExtract(FluidVariant resource) {
                    return canMoveNetworkToOutside(dir, resource);
                }
            };
        }
        unsidedCap = new FilteringStorage<>(this::getInternalNetworkStorage) {
            @Override
            protected boolean canInsert(FluidVariant resource) {
                return false;
            }

            @Override
            protected boolean canExtract(FluidVariant resource) {
                return false;
            }

            @Override
            public boolean supportsInsertion() {
                return false;
            }

            @Override
            public boolean supportsExtraction() {
                return false;
            }
        };
    }

    @Override
    public NetworkManager<FluidHost, FluidCache> getManager() {
        return MANAGER;
    }

    @Override
    @Nullable
    public Object getApiInstance(BlockApiLookup<?, Direction> lookup, @Nullable Direction side) {
        if (lookup == FluidStorage.SIDED) {
            if (side == null) {
                return unsidedCap;
            } else if ((pipe.connectionBlacklist & (1 << side.get3DDataValue())) == 0) {
                return caps[side.get3DDataValue()];
            }
        }
        return null;
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

            pipe.setChanged();
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
            pipe.setChanged();
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
            pipe.connectionBlacklist |= 1 << direction.get3DDataValue();
            pipe.setChanged();
        }
    }

    public void gatherCapabilities(@Nullable List<ConnectedFluidStorage> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                Storage<FluidVariant> adjacentCap = FluidStorage.SIDED.find(pipe.getLevel(), pipe.getBlockPos().relative(dir), dir.getOpposite());

                if (adjacentCap != null) {
                    if (out != null) {
                        var attachment = getAttachment(dir) instanceof FluidAttachedIo io ? io : null;
                        if (attachment == null) {
                            out.add(new ConnectedFluidStorage(adjacentCap, null, null));
                        } else if (attachment.isEnabledViaRedstone(pipe)) {
                            FilteringStorage<FluidVariant> filteredStorage = new FilteringStorage<>(adjacentCap) {
                                @Override
                                protected boolean canExtract(FluidVariant resource) {
                                    return canMoveOutsideToNetwork(dir, resource);
                                }

                                @Override
                                protected boolean canInsert(FluidVariant resource) {
                                    return canMoveNetworkToOutside(dir, resource);
                                }
                            };
                            var extractorRateLimit = attachment.getType() == IoAttachmentType.EXTRACTOR ? new ExtractorStorage(filteredStorage, i)
                                    : null;
                            out.add(new ConnectedFluidStorage(filteredStorage, attachment, extractorRateLimit));
                        }
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

    private boolean canMoveNetworkToOutside(Direction side, FluidVariant variant) {
        if (getAttachment(side) instanceof FluidAttachedIo io) {
            return io.matchesFilter(variant) && io.isEnabledViaRedstone(pipe) && io.getType() != IoAttachmentType.EXTRACTOR;
        }
        return true;
    }

    private boolean canMoveOutsideToNetwork(Direction side, FluidVariant variant) {
        if (getAttachment(side) instanceof FluidAttachedIo io) {
            return io.matchesFilter(variant) && io.isEnabledViaRedstone(pipe) && io.getType() != IoAttachmentType.ATTRACTOR;
        }
        return true;
    }

    private SingleSlotStorage<FluidVariant> getInternalNetworkStorage() {
        NetworkNode<FluidHost, FluidCache> node = findNode();

        if (node != null && node.getHost() == FluidHost.this) {
            return node.getNetworkCache().getOrCreateStorage();
        } else {
            return TransferUtil.EMPTY_SLOT;
        }
    }

    /**
     * Wrapper of a storage that's behind an extractor. Only used for extraction. Used to rate limit the extractor.
     */
    private class ExtractorStorage implements Storage<FluidVariant> {
        private final int directionId;
        private final Storage<FluidVariant> delegate;

        ExtractorStorage(Storage<FluidVariant> delegate, int directionId) {
            this.delegate = delegate;
            this.directionId = directionId;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            throw new UnsupportedOperationException("Should not be used to insert, only to extract!");
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            maxAmount = extractorLimit.limit(directionId, maxAmount);
            if (maxAmount <= 0)
                return 0;

            long transferred = delegate.extract(resource, maxAmount, transaction);
            extractorLimit.use(directionId, transferred, transaction);
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
                maxAmount = extractorLimit.limit(directionId, maxAmount);
                if (maxAmount <= 0)
                    return 0;

                long transferred = view.extract(resource, maxAmount, transaction);
                extractorLimit.use(directionId, transferred, transaction);
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
