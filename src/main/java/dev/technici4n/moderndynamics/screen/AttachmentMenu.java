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

import dev.technici4n.moderndynamics.attachment.attached.AttachedIO;
import dev.technici4n.moderndynamics.attachment.settings.FilterMode;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AttachmentMenu extends AbstractContainerMenu {

    public final PipeBlockEntity pipe;
    public final Direction side;
    public final AttachedIO attachment;
    private final Player player;

    protected AttachmentMenu(int syncId, Inventory playerInventory, PipeBlockEntity pipe, Direction side, AttachedIO attachment) {
        super(AttachmentMenuType.TYPE, syncId);
        this.player = playerInventory.player;

        this.pipe = pipe;
        this.side = side;
        this.attachment = attachment;

        // Config slots
        var row = 0;
        var col = 0;
        for (int i = 0; i < attachment.getFilterSize(); i++) {
            this.addSlot(new ConfigSlot(44 + col * 18, 20 + row * 18, attachment, i));
            if (++col >= 5) {
                col = 0;
                row++;
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
            attachment.setFilter(configSlot.configIdx, ItemVariant.of(getCarried()));
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

    public FilterMode getFilterMode() {
        return attachment.getFilterMode();
    }

    public void setFilterMode(FilterMode filterMode) {
        if (isClientSide()) {
            MdPackets.sendSetFilterMode(containerId, filterMode);
        } else {
            attachment.setFilterMode(filterMode);
        }
    }

    public boolean isClientSide() {
        return player.getCommandSenderWorld().isClientSide();
    }

    /**
     * Convenience method to get the player owning this menu.
     */
    public Player getPlayer() {
        return player;
    }

}
