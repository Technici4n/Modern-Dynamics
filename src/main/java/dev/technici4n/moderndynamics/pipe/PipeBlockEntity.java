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
package dev.technici4n.moderndynamics.pipe;

import dev.technici4n.moderndynamics.MdBlockEntity;
import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.model.PipeModelData;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.TickHelper;
import dev.technici4n.moderndynamics.screen.PipeScreenFactory;
import dev.technici4n.moderndynamics.util.DropHelper;
import dev.technici4n.moderndynamics.util.ShapeHelper;
import dev.technici4n.moderndynamics.util.WrenchHelper;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base BE class for all pipes.
 * Subclasses must have a static list of {@link NodeHost}s that will be used for all the registration and saving logic.
 */
public abstract class PipeBlockEntity extends MdBlockEntity implements RenderAttachmentBlockEntity {
    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private boolean hostsRegistered = false;
    public int connectionBlacklist = 0;
    private VoxelShape cachedShape = PipeBoundingBoxes.CORE_SHAPE;
    /* client side stuff */
    private PipeModelData clientModelData = null;
    private int clientSideConnections = 0;

    public abstract NodeHost[] getHosts();

    public final AttachedAttachment getAttachment(Direction side) {
        for (var host : getHosts()) {
            var attachment = host.getAttachment(side);
            if (attachment != null) {
                return attachment;
            }
        }
        return null;
    }

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
        for (var host : getHosts()) {
            host.writeClientNbt(tag);
        }
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        connectionBlacklist = tag.getByte("connectionBlacklist");
        byte connections = tag.getByte("connections");
        byte inventoryConnections = tag.getByte("inventoryConnections");
        var attachmentStacks = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, attachmentStacks);

        for (var host : getHosts()) {
            host.readClientNbt(tag);
        }

        updateCachedShape(connections, inventoryConnections);

        var attachments = new AttachedAttachment[6];
        for (var direction : Direction.values()) {
            attachments[direction.get3DDataValue()] = getAttachment(direction);
        }

        clientModelData = new PipeModelData(connections, inventoryConnections, attachments);
        clientSideConnections = connections | inventoryConnections;
    }

    @Override
    @Nullable
    public Object getRenderAttachmentData() {
        return clientModelData;
    }

    @Override
    public void toTag(CompoundTag nbt) {
        nbt.putByte("connectionBlacklist", (byte) connectionBlacklist);

        for (NodeHost host : getHosts()) {
            host.separateNetwork();
            host.writeNbt(nbt);
        }
    }

    @Override
    public void fromTag(CompoundTag nbt) {
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
    public void clearRemoved() {
        super.clearRemoved();

        if (!level.isClientSide()) {
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
    public void setRemoved() {
        super.setRemoved();

        if (!level.isClientSide()) {
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

    @Nullable
    public Object getApiInstance(BlockApiLookup<?, Direction> direction, Direction side) {
        for (var host : getHosts()) {
            var api = host.getApiInstance(direction, side);
            if (api != null) {
                return api;
            }
        }
        return null;
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
            var attachment = getAttachment(Direction.from3DDataValue(i));
            if ((allConnections & (1 << i)) > 0 || attachment != null) {
                shape = Shapes.or(shape, PipeBoundingBoxes.PIPE_CONNECTIONS[i]);
            }

            if ((inventoryConnections & (1 << i)) > 0 || attachment != null) {
                shape = Shapes.or(shape, PipeBoundingBoxes.CONNECTOR_SHAPES[i]);
            }
        }

        cachedShape = shape.optimize();
    }

    /**
     * Update connection blacklist for a side, and schedule a node update, on the server side.
     */
    protected void updateConnection(Direction side, boolean addConnection) {
        if (level.isClientSide()) {
            throw new IllegalStateException("updateConnections() should not be called client-side.");
        }

        // Update mask
        if (addConnection) {
            connectionBlacklist &= ~(1 << side.get3DDataValue());
        } else {
            connectionBlacklist |= 1 << side.get3DDataValue();
        }

        // Update neighbor's mask as well
        BlockEntity be = level.getBlockEntity(worldPosition.relative(side));

        if (be instanceof PipeBlockEntity neighborPipe) {
            if (addConnection) {
                neighborPipe.connectionBlacklist &= ~(1 << side.getOpposite().get3DDataValue());
            } else {
                neighborPipe.connectionBlacklist |= 1 << side.getOpposite().get3DDataValue();
            }
            neighborPipe.setChanged();
        }

        // Schedule inventory and network updates.
        refreshHosts();
        // The call to getNode() causes a network rebuild, but that shouldn't be an issue. (?)
        scheduleHostUpdates();

        level.blockUpdated(worldPosition, getBlockState().getBlock());
        setChanged();
        // no need to sync(), that's already handled by the refresh or update if necessary
    }

    public InteractionResult onUse(Player player, InteractionHand hand, BlockHitResult hitResult) {
        var stack = player.getItemInHand(hand);
        Vec3 posInBlock = hitResult.getLocation().subtract(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

        if (WrenchHelper.isWrench(stack)) {
            // If the core was hit, add back the pipe on the target side
            if (ShapeHelper.shapeContains(PipeBoundingBoxes.CORE_SHAPE, posInBlock)) {
                if ((connectionBlacklist & (1 << hitResult.getDirection().get3DDataValue())) > 0) {
                    if (!level.isClientSide()) {
                        updateConnection(hitResult.getDirection(), true);
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }

            for (int i = 0; i < 6; ++i) {
                if (ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                    var side = Direction.from3DDataValue(i);
                    if (getAttachment(side) != null) {
                        // Remove attachment
                        if (level.isClientSide()) {
                            return InteractionResult.SUCCESS;
                        } else {
                            for (var host : getHosts()) {
                                var attachment = host.removeAttachment(side);
                                if (attachment != null) {
                                    DropHelper.dropStacks(this, attachment.getDrops());
                                    level.blockUpdated(worldPosition, getBlockState().getBlock());
                                    setChanged();
                                    sync();
                                    return InteractionResult.CONSUME;
                                }
                            }
                        }
                    } else {
                        // If a pipe or inventory connection was hit, add it to the blacklist
                        // INVENTORY_CONNECTIONS contains both the pipe and the connector, so it will work in both cases
                        if (level.isClientSide()) {
                            if ((clientSideConnections & (1 << i)) > 0) {
                                return InteractionResult.SUCCESS;
                            }
                        } else {
                            if ((getPipeConnections() & (1 << i)) > 0 || (getInventoryConnections() & (1 << i)) > 0) {
                                updateConnection(Direction.from3DDataValue(i), false);
                                return InteractionResult.CONSUME;
                            }
                        }
                    }
                }
            }
        }

        if (stack.getItem() instanceof AttachmentItem attachmentItem) {
            Direction hitSide = null;
            if (ShapeHelper.shapeContains(PipeBoundingBoxes.CORE_SHAPE, posInBlock)) {
                hitSide = hitResult.getDirection();
            }
            for (int i = 0; i < 6; ++i) {
                if (ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                    hitSide = Direction.from3DDataValue(i);
                }
            }

            if (hitSide != null) {
                if (getAttachment(hitSide) == null) {
                    for (var host : getHosts()) {
                        if (host.acceptsAttachment(attachmentItem, stack)) {
                            if (!level.isClientSide) {
                                var initialData = stack.getTag();
                                if (initialData == null) {
                                    initialData = new CompoundTag();
                                }
                                host.setAttachment(hitSide, attachmentItem, initialData);
                                level.blockUpdated(worldPosition, getBlockState().getBlock());
                                setChanged();
                                sync();
                            }
                            if (!player.isCreative()) {
                                stack.shrink(1);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide);
                        }
                    }
                }
            }
        }

        // Handle click on attachment
        var attachmentHit = hitTestAttachments(posInBlock);
        if (attachmentHit != null) {
            if (attachmentHit.hasScreen()) {
                // Open attachment GUI
                player.openMenu(new PipeScreenFactory(attachmentHit));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    public AttachedAttachment hitTestAttachments(Vec3 posInBlock) {
        // Handle click on attachment
        for (int i = 0; i < 6; ++i) {
            if (ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                var side = Direction.from3DDataValue(i);
                return getAttachment(side);
            }
        }

        return null;
    }

    public void onRemoved() {
        for (var host : getHosts()) {
            host.onRemoved();
        }
    }
}
