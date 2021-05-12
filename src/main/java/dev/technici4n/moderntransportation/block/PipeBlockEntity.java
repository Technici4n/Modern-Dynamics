package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.MtBlockEntity;
import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.TickHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
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

    public abstract NodeHost[] getHosts();

    public abstract VoxelShape getCachedShape();

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);

        for (NodeHost host : getHosts()) {
            host.separateNetwork();
            host.writeNbt(nbt);
        }

        return nbt;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag nbt) {
        super.fromTag(state, nbt);

        for (NodeHost host : getHosts()) {
            host.separateNetwork();
            host.readNbt(nbt);
        }
    }

    public void neighborUpdate() {
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
}
