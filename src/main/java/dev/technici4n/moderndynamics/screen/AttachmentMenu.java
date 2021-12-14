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

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AttachmentMenu extends AbstractContainerMenu {
    public static final MenuType<AttachmentMenu> TYPE = ScreenHandlerRegistry.registerExtended(
            MdId.of("attachment"),
            AttachmentMenu::fromPacket);

    private static AttachmentMenu fromPacket(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        var world = playerInventory.player.level;

        var bet = Registry.BLOCK_ENTITY_TYPE.get(packetByteBuf.readResourceLocation());
        var side = packetByteBuf.readEnum(Direction.class);
        return world.getBlockEntity(packetByteBuf.readBlockPos(), bet).map(blockEntity -> {
            if (blockEntity instanceof PipeBlockEntity pipe) {
                var attachment = pipe.getAttachment(side);
                if (attachment != null) {
                    return new AttachmentMenu(syncId, playerInventory, attachment);
                }
            }
            return null;
        }).orElse(null);
    }

    public final AttachedAttachment attachment;

    protected AttachmentMenu(int syncId, Inventory playerInventory, AttachedAttachment attachment) {
        super(TYPE, syncId);
        this.attachment = attachment;

        // Config slots
        for (int i = 0; i < attachment.getConfigHeight(); ++i) {
            for (int j = 0; j < attachment.getConfigWidth(); ++j) {
                this.addSlot(new ConfigSlot(8 + j * 18, 30 + i * 18, attachment, i, j));
            }
        }

        // Player inventory slots
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 181));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
        if (slotIndex >= 0 && getSlot(slotIndex) instanceof ConfigSlot configSlot) {
            attachment.setFilter(configSlot.configX, configSlot.configY, ItemVariant.of(getCarried()));
        } else {
            super.clicked(slotIndex, button, actionType, player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }
}
