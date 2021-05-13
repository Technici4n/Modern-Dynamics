package dev.technici4n.moderntransportation.block;

import com.google.common.base.Preconditions;
import dev.technici4n.moderntransportation.MtBlockEntity;
import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.TickHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base BE class for all pipes.
 * Subclasses must have a static list of {@link NodeHost}s that will be used for all the registration and saving logic.
 */
public abstract class PipeBlockEntity extends MtBlockEntity {
    public PipeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    private boolean hostsRegistered = false;
    public int connectionBlacklist = 0;

    public abstract NodeHost[] getHosts();

    public abstract VoxelShape getCachedShape();

    public abstract ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hitResult);

    @Override
    public void toClientTag(CompoundTag tag) {
        tag.putByte("connectionBlacklist", (byte) connectionBlacklist);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        connectionBlacklist = tag.getByte("connectionBlacklist");
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);

        nbt.putByte("connectionBlacklist", (byte) connectionBlacklist);

        for (NodeHost host : getHosts()) {
            host.separateNetwork();
            host.writeNbt(nbt);
        }

        return nbt;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag nbt) {
        super.fromTag(state, nbt);

        connectionBlacklist = nbt.getByte("connectionBlacklist");

        for (NodeHost host : getHosts()) {
            host.separateNetwork();
            host.readNbt(nbt);
        }
    }

    public void scheduleHostUpdates() {
        for (NodeHost host : getHosts()) {
            host.scheduleUpdate();
        }
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();

        if (!world.isClient()) {
            if (!hostsRegistered) {
                TickHelper.runLater(() -> {
                    if (!hostsRegistered) {
                        hostsRegistered = true;

                        for (NodeHost host : getHosts()) {
                            host.addSelf();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if (!world.isClient()) {
            if (hostsRegistered) {
                hostsRegistered = false;

                for (NodeHost host : getHosts()) {
                    host.removeSelf();
                }
            }
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();

        if (!world.isClient()) {
            if (hostsRegistered) {
                hostsRegistered = false;

                for (NodeHost host : getHosts()) {
                    host.removeSelf();
                }
            }
        }
    }

    public void refreshHosts() {
        if (hostsRegistered) {
            for (NodeHost host : getHosts()) {
                host.refreshSelf();
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        for (NodeHost host : getHosts()) {
            LazyOptional<T> returnedCap = host.getCapability(cap, side);

            if (returnedCap.isPresent()) {
                return returnedCap;
            }
        }

        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        for (NodeHost host : getHosts()) {
            host.invalidateCapabilities();
        }

        super.invalidateCaps();
    }

    /**
     * Update connection blacklist for a side, and schedule a node update, on the server side.
     */
    protected void updateConnection(Direction side, boolean addConnection) {
        if (world.isClient()) {
            throw new IllegalStateException("updateConnections() should not be called client-side.");
        }

        // Update mask
        if (addConnection) {
            connectionBlacklist &= ~(1 << side.getId());
        } else {
            connectionBlacklist |= 1 << side.getId();
        }

        // Schedule inventory and network updates.
        refreshHosts();
        // The call to getNode() causes a network rebuild, but that shouldn't be an issue. (?)
        scheduleHostUpdates();

        // Update neighbor's mask as well
        BlockEntity be = world.getBlockEntity(pos.offset(side));

        if (be instanceof PipeBlockEntity) {
            PipeBlockEntity neighborPipe = (PipeBlockEntity) be;
            if (addConnection) {
                neighborPipe.connectionBlacklist &= ~(1 << side.getOpposite().getId());
            } else {
                neighborPipe.connectionBlacklist |= 1 << side.getOpposite().getId();
            }
            neighborPipe.markDirty();
        }

        world.updateNeighbors(pos, getCachedState().getBlock());
        markDirty();
        // no need to sync(), that's already handled by the refresh or update if necessary
    }
}
