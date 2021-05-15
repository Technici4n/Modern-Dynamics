package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.MtBlockEntity;
import dev.technici4n.moderntransportation.model.MTModels;
import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.TickHelper;
import dev.technici4n.moderntransportation.util.ShapeHelper;
import dev.technici4n.moderntransportation.util.WrenchHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
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
    private VoxelShape cachedShape = PipeBoundingBoxes.CORE_SHAPE;
    private IModelData modelData = EmptyModelData.INSTANCE;
    private int clientSideConnections = 0;

    public abstract NodeHost[] getHosts();

    @Override
    public void sync() {
        super.sync();
        updateCachedShape(getPipeConnections(), getInventoryConnections());
    }

    @Override
    public void toClientTag(CompoundTag tag) {
        tag.putByte("connectionBlacklist", (byte) connectionBlacklist);
        tag.putByte("connections", (byte) getPipeConnections());
        tag.putByte("inventoryConnections", (byte) getInventoryConnections());
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        connectionBlacklist = tag.getByte("connectionBlacklist");
        byte connections = tag.getByte("connections");
        byte inventoryConnections = tag.getByte("inventoryConnections");

        updateCachedShape(connections, inventoryConnections);
        modelData = new ModelDataMap.Builder()
                .withInitial(MTModels.CONNECTIONS_PIPE, connections)
                .withInitial(MTModels.CONNECTIONS_INVENTORY, inventoryConnections)
                .build();
        clientSideConnections = connections | inventoryConnections;
        requestModelDataUpdate();
        remesh();
    }

    @NotNull
    @Override
    public IModelData getModelData() {
        return modelData;
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
                    if (!hostsRegistered && !isRemoved()) {
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

    protected int getPipeConnections() {
        int pipeConnections = 0;

        for (NodeHost host : getHosts()) {
            pipeConnections |= host.pipeConnections;
        }

        return pipeConnections;
    }

    protected int getInventoryConnections() {
        int inventoryConnections = 0;

        for (NodeHost host : getHosts()) {
            inventoryConnections |= host.inventoryConnections;
        }

        return inventoryConnections;
    }

    public VoxelShape getCachedShape() {
        return cachedShape;
    }

    public void updateCachedShape(int pipeConnections, int inventoryConnections) {
        int allConnections = pipeConnections | inventoryConnections;

        VoxelShape shape = PipeBoundingBoxes.CORE_SHAPE;

        for (int i = 0; i < 6; ++i) {
            if ((allConnections & (1 << i)) > 0) {
                shape = VoxelShapes.union(shape, PipeBoundingBoxes.PIPE_CONNECTIONS[i]);
            }

            if ((inventoryConnections & (1 << i)) > 0) {
                shape = VoxelShapes.union(shape, PipeBoundingBoxes.CONNECTOR_SHAPES[i]);
            }
        }

        cachedShape = shape.simplify();
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

        // Schedule inventory and network updates.
        refreshHosts();
        // The call to getNode() causes a network rebuild, but that shouldn't be an issue. (?)
        scheduleHostUpdates();

        world.updateNeighbors(pos, getCachedState().getBlock());
        markDirty();
        // no need to sync(), that's already handled by the refresh or update if necessary
    }

    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (WrenchHelper.isWrench(player.getStackInHand(hand))) {
            Vec3d posInBlock = hitResult.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());

            // If the core was hit, add back the pipe on the target side
            if (ShapeHelper.shapeContains(PipeBoundingBoxes.CORE_SHAPE, posInBlock)) {
                if ((connectionBlacklist & (1 << hitResult.getSide().getId())) > 0) {
                    if (!world.isClient()) {
                        updateConnection(hitResult.getSide(), true);
                    }

                    return ActionResult.success(world.isClient());
                }
            }

            for (int i = 0; i < 6; ++i) {
                // If a pipe or inventory connection was hit, add it to the blacklist
                // INVENTORY_CONNECTIONS contains both the pipe and the connector, so it will work in both cases
                if (ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                    if (world.isClient()) {
                        if ((clientSideConnections & (1 << i)) > 0) {
                            return ActionResult.SUCCESS;
                        }
                    } else {
                        if ((getPipeConnections() & (1 << i)) > 0 || (getInventoryConnections() & (1 << i)) > 0) {
                            updateConnection(Direction.byId(i), false);
                            return ActionResult.CONSUME;
                        }
                    }
                }
            }
        }

        return ActionResult.PASS;
    }
}
