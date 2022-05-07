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
package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.attached.AttachedIO;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class AttachmentMenuType implements ExtendedScreenHandlerFactory {
    public static final MenuType<AttachmentMenu> TYPE = new ExtendedScreenHandlerType<>(AttachmentMenuType::fromPacket);

    public static void init() {
        Registry.register(Registry.MENU, MdId.of("attachment"), TYPE);
    }

    private final PipeBlockEntity pipe;
    private final Direction side;
    private final AttachedIO attachment;

    public AttachmentMenuType(PipeBlockEntity pipe, Direction side, AttachedIO attachment) {
        this.pipe = pipe;
        this.side = side;
        this.attachment = attachment;
    }

    private static AttachmentMenu fromPacket(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        var world = playerInventory.player.level;

        var bet = Registry.BLOCK_ENTITY_TYPE.get(packetByteBuf.readResourceLocation());
        var side = packetByteBuf.readEnum(Direction.class);
        var pos = packetByteBuf.readBlockPos();
        var item = Registry.ITEM.byId(packetByteBuf.readVarInt());
        if (!(item instanceof IoAttachmentItem attachmentItem)) {
            throw new IllegalStateException("Server sent a non-attachment item as menu host: " + item);
        }
        var tag = packetByteBuf.readNbt();
        var attachment = attachmentItem.createAttached(tag);

        return world.getBlockEntity(pos, bet).map(blockEntity -> {
            if (blockEntity instanceof PipeBlockEntity pipe) {
                return new AttachmentMenu(syncId, playerInventory, pipe, side, attachment);
            }
            return null;
        }).orElse(null);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeResourceLocation(Registry.BLOCK_ENTITY_TYPE.getKey(pipe.getType()));
        buf.writeEnum(side);
        buf.writeBlockPos(pipe.getBlockPos());
        buf.writeVarInt(Registry.ITEM.getId(attachment.getItem()));
        buf.writeNbt(attachment.writeConfigTag(new CompoundTag()));
    }

    @Override
    public Component getDisplayName() {
        return attachment.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        // This is called server-side
        return new AttachmentMenu(syncId, inv, pipe, side, attachment) {
            @Override
            public boolean stillValid(Player player) {
                var pos = pipe.getBlockPos();
                if (player.getLevel().getBlockEntity(pos) != pipe) {
                    return false;
                }
                if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
                    return false;
                }
                return pipe.getAttachment(side) == attachment;
            }
        };
    }
}
