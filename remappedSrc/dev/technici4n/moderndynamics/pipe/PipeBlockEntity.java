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

import ;
import Z;
import com.google.common.base.Preconditions;
import dev.technici4n.moderndynamics.MdBlockEntity;
import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.model.AttachmentModelData;
import dev.technici4n.moderndynamics.model.PipeModelData;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.TickHelper;
import dev.technici4n.moderndynamics.util.DropHelper;
import dev.technici4n.moderndynamics.util.ShapeHelper;
import dev.technici4n.moderndynamics.util.WrenchHelper;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base BE class for all pipes.
 * Subclasses must have a static list of {@link NodeHost}s that will be used for all the registration and saving logic.
 */
public abstract class PipeBlockEntity extends MdBlockEntity implements RenderAttachmentBlockEntity {
    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private NodeHost[] hosts;
    private boolean hostsRegistered = false;
    public int connectionBlacklist = 0;
    private VoxelShape cachedShape = PipeBoundingBoxes.CORE_SHAPE;
    /* client side stuff */
    private PipeModelData clientModelData = null;

    private int clientSideConnections = 0;

    protected abstract NodeHost[] createHosts();

    public final NodeHost[] getHosts() {
        if (hosts == null) {
            hosts = createHosts();
        }
        return hosts;
    }

    private boolean hasAttachment(Direction side) {
        if (isClientSide()) {
            return clientModelData != null && clientModelData.attachments()[side.getId()] != null;
        } else {
            return getAttachment(side) != null;
        }
    }

    public final AttachedAttachment getAttachment(Direction side) {
        Preconditions.checkState(!isClientSide(), "Attachments don't exist on the client.");

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
    public void toClientTag(NbtCompound tag) {
        tag.putByte("connectionBlacklist", (byte) connectionBlacklist);
        tag.putByte("connections", (byte) getPipeConnections());
        tag.putByte("inventoryConnections", (byte) getInventoryConnections());
        for (var host : getHosts()) {
            host.writeClientNbt(tag);
        }
        var attachments = new NbtList();
        for (var direction : Direction.values()) {
            var attachment = getAttachment(direction);
            if (attachment != null) {
                attachments.add(attachment.getModelData().write(new NbtCompound()));
            } else {
                attachments.add(new NbtCompound());
            }
        }
        tag.put("attachments", attachments);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        connectionBlacklist = tag.getByte("connectionBlacklist");
        byte connections = tag.getByte("connections");
        byte inventoryConnections = tag.getByte("inventoryConnections");
        var attachmentStacks = DefaultedList.ofSize(6, ItemStack.EMPTY);
        Inventories.readNbt(tag, attachmentStacks);

        for (var host : getHosts()) {
            host.readClientNbt(tag);
        }
        var attachmentTags = tag.getList("attachments", NbtElement.COMPOUND_TYPE);
        var attachments = new AttachmentModelData[6];
        for (var direction : Direction.values()) {
            var attachmentTag = attachmentTags.getCompound(direction.getId());
            attachments[direction.getId()] = AttachmentModelData.from(attachmentTag);
        }

        clientModelData = new PipeModelData(connections, inventoryConnections, attachments);
        clientSideConnections = connections | inventoryConnections;

        updateCachedShape(connections, inventoryConnections);

    }

    @Override
    @Nullable
    public Object getRenderAttachmentData() {
        return clientModelData;
    }

    @Override
    public void toTag(NbtCompound nbt) {
        nbt.putByte("connectionBlacklist", (byte) connectionBlacklist);

        if (!world.isClient()) { // WTHIT calls this on the client side
            for (NodeHost host : getHosts()) {
                if (hostsRegistered) {
                    host.separateNetwork();
                }

                host.writeNbt(nbt);
            }
        }
    }

    @Override
    public void fromTag(NbtCompound nbt) {
        connectionBlacklist = nbt.getByte("connectionBlacklist");

        for (NodeHost host : getHosts()) {
            if (hostsRegistered) {
                host.separateNetwork();
            }

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
            var hasAttachment = hasAttachment(Direction.byId(i));
            if ((allConnections & (1 << i)) > 0 || hasAttachment) {
                shape = VoxelShapes.union(shape, PipeBoundingBoxes.PIPE_CONNECTIONS[i]);
            }

            if ((inventoryConnections & (1 << i)) > 0 || hasAttachment) {
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

        if (be instanceof PipeBlockEntity neighborPipe) {
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
        var stack = player.getStackInHand(hand);
        Vec3d posInBlock = hitResult.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());

        if (WrenchHelper.isWrench(stack)) {
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
                if (ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                    var side = Direction.byId(i);
                    if (hasAttachment(side)) {
                        // Remove attachment
                        if (world.isClient()) {
                            return ActionResult.SUCCESS;
                        } else {
                            for (var host : getHosts()) {
                                var attachment = host.removeAttachment(side);
                                if (attachment != null) {
                                    if (!player.isCreative()) {
                                        DropHelper.dropStacks(this, attachment.getDrops());
                                    }
                                    world.updateNeighbors(pos, getCachedState().getBlock());
                                    refreshHosts();
                                    scheduleHostUpdates();
                                    markDirty();
                                    sync();
                                    return ActionResult.CONSUME;
                                }
                            }
                        }
                    } else {
                        // If a pipe or inventory connection was hit, add it to the blacklist
                        // INVENTORY_CONNECTIONS contains both the pipe and the connector, so it will work in both cases
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
        }

        if (stack.getItem() instanceof AttachmentItem attachmentItem) {
            Direction hitSide = null;
            if (ShapeHelper.shapeContains(PipeBoundingBoxes.CORE_SHAPE, posInBlock)) {
                hitSide = hitResult.getSide();
            }
            for (int i = 0; i < 6; ++i) {
                if (ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                    hitSide = Direction.byId(i);
                }
            }

            if (hitSide != null) {
                if (!hasAttachment(hitSide)) {
                    for (var host : getHosts()) {
                        if (host.acceptsAttachment(attachmentItem, stack)) {
                            if (!world.isClient) {
                                var initialData = stack.getNbt();
                                if (initialData == null) {
                                    initialData = new NbtCompound();
                                }
                                host.setAttachment(hitSide, attachmentItem, initialData);
                                host.getAttachment(hitSide).onPlaced(player);
                                world.updateNeighbors(pos, getCachedState().getBlock());
                                refreshHosts();
                                scheduleHostUpdates();
                                markDirty();
                                sync();
                            }
                            if (!player.isCreative()) {
                                stack.decrement(1);
                            }
                            return ActionResult.success(world.isClient);
                        }
                    }
                }
            }
        }

        // Handle click on attachment
        var hitSide = hitTestAttachments(posInBlock);
        if (hitSide != null) {
            if (!isClientSide()) {
                var attachment = getAttachment(hitSide);
                if (attachment != null && attachment.hasMenu()) {
                    // Open attachment GUI
                    player.openHandledScreen(attachment.createMenu(this, hitSide));
                }
            }
            return ActionResult.success(isClientSide());
        }

        return ActionResult.PASS;
    }

    @Nullable
    public Direction hitTestAttachments(Vec3d posInBlock) {
        // Handle click on attachment
        for (int i = 0; i < 6; ++i) {
            var direction = Direction.byId(i);
            if (hasAttachment(direction)
                    && ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                return direction;
            }
        }

        return null;
    }

    public ItemStack overridePickBlock(HitResult hitResult) {
        Vec3d posInBlock = hitResult.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        Direction side = hitTestAttachments(posInBlock);
        return side != null ? new ItemStack(clientModelData.attachments()[side.getId()].getItem()) : ItemStack.EMPTY;
    }

    public void onRemoved() {
        for (var host : getHosts()) {
            host.onRemoved();
        }
    }

    public int getClientSideConnections() {
        Preconditions.checkState(isClientSide());
        return clientSideConnections;
    }

    public void clientTick() {
        for (var host : getHosts()) {
            host.clientTick();
        }
    }
}
