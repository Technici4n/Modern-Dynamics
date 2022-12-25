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
package dev.technici4n.moderndynamics.gui.menu;

import com.google.common.util.concurrent.Runnables;
import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class AttachmentMenuType<A extends AttachedAttachment, T extends AbstractContainerMenu> extends ExtendedScreenHandlerType<T> {
    private final MenuFactory<A, T> menuFactory;

    private <I extends AttachmentItem> AttachmentMenuType(AttachmentFactory<A, I> attachmentFactory, MenuFactory<A, T> menuFactory) {
        super((int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) -> {
            var world = playerInventory.player.level;

            var bet = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(packetByteBuf.readResourceLocation());
            var side = packetByteBuf.readEnum(Direction.class);
            var pos = packetByteBuf.readBlockPos();
            var item = BuiltInRegistries.ITEM.byId(packetByteBuf.readVarInt());
            if (!(item instanceof AttachmentItem attachmentItem)) {
                throw new IllegalStateException("Server sent a non-attachment item as menu host: " + item);
            }
            var tag = packetByteBuf.readNbt();
            // The cast is a bit ugly, but it just means that we trust the server to send the correct item.
            var attachment = ((AttachmentFactory<A, AttachmentItem>) attachmentFactory).createAttachment(attachmentItem, tag, Runnables.doNothing());

            return world.getBlockEntity(pos, bet).map(blockEntity -> {
                if (blockEntity instanceof PipeBlockEntity pipe) {
                    return menuFactory.createMenu(syncId, playerInventory, pipe, side, attachment);
                }
                return null;
            }).orElse(null);
        });

        this.menuFactory = menuFactory;
    }

    public static <A extends AttachedAttachment, T extends AbstractContainerMenu, I extends AttachmentItem> AttachmentMenuType<A, T> create(
            String id, AttachmentFactory<A, I> attachmentFactory, MenuFactory<A, T> menuFactory) {
        var type = new AttachmentMenuType<>(attachmentFactory, menuFactory);
        Registry.register(BuiltInRegistries.MENU, MdId.of(id), type);
        return type;
    }

    public interface AttachmentFactory<A extends AttachedAttachment, I extends AttachmentItem> {
        A createAttachment(I item, CompoundTag configData, Runnable setChangedCallback);
    }

    public interface MenuFactory<A extends AttachedAttachment, T extends AbstractContainerMenu> {
        T createMenu(int syncId, Inventory playerInventory, PipeBlockEntity pipe, Direction side, A attachment);
    }

    public ExtendedScreenHandlerFactory createMenu(PipeBlockEntity pipe, Direction side, A attachment) {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                buf.writeResourceLocation(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(pipe.getType()));
                buf.writeEnum(side);
                buf.writeBlockPos(pipe.getBlockPos());
                buf.writeVarInt(BuiltInRegistries.ITEM.getId(attachment.getItem()));
                buf.writeNbt(attachment.writeConfigTag(new CompoundTag()));
            }

            @Override
            public Component getDisplayName() {
                return attachment.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                return menuFactory.createMenu(syncId, inventory, pipe, side, attachment);
            }
        };
    }
}
