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

import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.attached.AttachedIo;
import dev.technici4n.moderndynamics.attachment.attached.FluidAttachedIo;
import dev.technici4n.moderndynamics.network.HostAdjacentCaps;
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
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class FluidHost extends NodeHost {
    private static final NetworkManager<FluidHost, FluidCache> MANAGER = NetworkManager.get(FluidCache.class, FluidCache::new);

    private FluidResource resource = FluidResource.EMPTY;
    private int amount = 0;
    private final TransferLimits extractorLimit = new TransferLimits(side -> {
        if (!(getAttachment(side) instanceof FluidAttachedIo io) || io.getType() != IoAttachmentType.EXTRACTOR) {
            return 0;
        }

        return io.getFluidMaxIo();
    }, FluidType.BUCKET_VOLUME);
    // Caps
    @SuppressWarnings("unchecked")
    private final ResourceHandler<FluidResource>[] caps = new ResourceHandler[6];
    private final ResourceHandler<FluidResource> unsidedCap;
    private final HostAdjacentCaps<ResourceHandler<FluidResource>> adjacentCaps = new HostAdjacentCaps<>(this, Capabilities.Fluid.BLOCK);

    public FluidHost(PipeBlockEntity pipe) {
        super(pipe);

        for (int i = 0; i < 6; ++i) {
            var dir = Direction.from3DDataValue(i);
            caps[i] = new FilteringFluidHandler(this::getInternalNetworkStorage) {
                @Override
                protected boolean canInsert(FluidResource resource) {
                    return canMoveOutsideToNetwork(dir, resource);
                }

                @Override
                protected boolean canExtract(FluidResource resource) {
                    return canMoveNetworkToOutside(dir, resource);
                }
            };
        }
        unsidedCap = new FilteringFluidHandler(this::getInternalNetworkStorage) {
            @Override
            protected boolean canInsert(FluidResource resource) {
                return false;
            }

            @Override
            protected boolean canExtract(FluidResource resource) {
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
    public Object getApiInstance(BlockCapability<?, Direction> lookup, @Nullable Direction side) {
        if (lookup == Capabilities.Fluid.BLOCK) {
            if (side == null) {
                return unsidedCap;
            } else if ((pipe.connectionBlacklist & (1 << side.get3DDataValue())) == 0) {
                return caps[side.get3DDataValue()];
            }
        }
        return null;
    }

    public int getAmount() {
        return amount;
    }

    public FluidResource getResource() {
        return resource;
    }

    public void setContents(FluidResource resource, int nodeFluid) {
        if (!resource.equals(this.resource) || nodeFluid != this.amount) {
            this.resource = resource;
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
        return FluidCache.areCompatible(((FluidHost) other).resource, resource);
    }

    @Override
    public void onConnectedTo(NodeHost other) {
        if (other instanceof FluidHost fh && !fh.resource.isEmpty()) {
            resource = fh.resource;
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
            // rejected because of incompatible item: blacklist this side!
            pipe.connectionBlacklist |= 1 << direction.get3DDataValue();
            pipe.setChanged();
        }
    }

    public void gatherCapabilities(@Nullable List<ConnectedFluidStorage> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                var adjacentCap = adjacentCaps.getCapability(dir);

                if (adjacentCap != null) {
                    if (out != null) {
                        var attachment = getAttachment(dir) instanceof FluidAttachedIo io ? io : null;
                        if (attachment == null) {
                            out.add(new ConnectedFluidStorage(adjacentCap, null, null));
                        } else if (attachment.isEnabledViaRedstone(pipe)) {
                            var filteredStorage = new FilteringFluidHandler(adjacentCap) {
                                @Override
                                protected boolean canExtract(FluidResource resource) {
                                    return canMoveOutsideToNetwork(dir, resource);
                                }

                                @Override
                                protected boolean canInsert(FluidResource resource) {
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
    public void write(ValueOutput output) {
        super.write(output);
        output.putInt("amount", amount);
        output.store("resource", FluidResource.OPTIONAL_CODEC, resource);
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        resource = input.read("resource", FluidResource.OPTIONAL_CODEC).orElse(FluidResource.EMPTY);
        // Guard against max changes
        amount = Math.max(0, Math.min(input.getIntOr("amount", 0), Constants.Fluids.CAPACITY));
        // Guard against removed resource
        if (resource.isEmpty()) {
            amount = 0;
        }
    }

    @Override
    public void writeClientData(ValueOutput output) {
        super.writeClientData(output);
        output.putInt("amount", amount);
        output.store("resource", FluidResource.OPTIONAL_CODEC, resource);
    }

    @Override
    public void readClientData(ValueInput input) {
        super.readClientData(input);
        resource = input.read("resource", FluidResource.OPTIONAL_CODEC).orElse(FluidResource.EMPTY);
        amount = input.getIntOr("amount", 0);
    }

    private boolean canMoveNetworkToOutside(Direction side, FluidResource resource) {
        if (getAttachment(side) instanceof FluidAttachedIo io) {
            return io.matchesFilter(resource) && io.isEnabledViaRedstone(pipe) && io.getType() != IoAttachmentType.EXTRACTOR;
        }
        return true;
    }

    private boolean canMoveOutsideToNetwork(Direction side, FluidResource resource) {
        if (getAttachment(side) instanceof FluidAttachedIo io) {
            return io.matchesFilter(resource) && io.isEnabledViaRedstone(pipe) && io.getType() != IoAttachmentType.ATTRACTOR;
        }
        return true;
    }

    private ResourceHandler<FluidResource> getInternalNetworkStorage() {
        NetworkNode<FluidHost, FluidCache> node = findNode();

        if (node != null && node.getHost() == FluidHost.this) {
            return node.getNetworkCache().getOrCreateStorage();
        } else {
            return EmptyResourceHandler.instance();
        }
    }

    /**
     * Wrapper of a storage that's behind an extractor. Only used for extraction. Used to rate limit the extractor.
     */
    private class ExtractorStorage extends DelegatingResourceHandler<FluidResource> {
        private final int directionId;

        ExtractorStorage(ResourceHandler<FluidResource> delegate, int directionId) {
            super(delegate);
            this.directionId = directionId;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
            throw new UnsupportedOperationException("Should not be used to insert, only to extract!");
        }

        @Override
        public int insert(FluidResource resource, int amount, TransactionContext tx) {
            throw new UnsupportedOperationException("Should not be used to insert, only to extract!");
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
            amount = extractorLimit.limit(directionId, amount);
            if (amount <= 0) {
                return 0;
            }
            var extracted = super.extract(index, resource, amount, tx);
            extractorLimit.updateSnapshots(tx);
            extractorLimit.use(directionId, extracted);
            return extracted;
        }

        @Override
        public int extract(FluidResource resource, int amount, TransactionContext tx) {
            amount = extractorLimit.limit(directionId, amount);
            if (amount <= 0) {
                return 0;
            }
            var extracted = super.extract(resource, amount, tx);
            extractorLimit.updateSnapshots(tx);
            extractorLimit.use(directionId, extracted);
            return extracted;
        }
    }
}
