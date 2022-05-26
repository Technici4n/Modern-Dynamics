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

import dev.technici4n.moderndynamics.attachment.Setting;
import dev.technici4n.moderndynamics.attachment.attached.AttachedIo;
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import dev.technici4n.moderndynamics.gui.MdPackets;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// TODO: need to find a clean way to sync changes done by other players
public class AttachedIoMenu<A extends AttachedIo> extends AbstractContainerMenu {
    public final PipeBlockEntity pipe;
    public final Direction side;
    public final A attachment;
    protected final Player player;

    public AttachedIoMenu(MenuType<?> menuType, int syncId, Inventory playerInventory, PipeBlockEntity pipe, Direction side, A attachment) {
        super(menuType, syncId);
        this.pipe = pipe;
        this.side = side;
        this.attachment = attachment;
        this.player = playerInventory.player;

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

        // Upgrade slots
        for (int k = 0; k < AttachedIo.UPGRADE_SLOTS; ++k) {
            this.addSlot(new UpgradeSlot(attachment, k, UpgradePanel.FIRST_SLOT_LEFT, UpgradePanel.FIRST_SLOT_TOP + k * 18));
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

    @Override
    public boolean stillValid(Player player) {
        if (isClientSide())
            return true;

        var pos = pipe.getBlockPos();
        if (player.getLevel().getBlockEntity(pos) != pipe) {
            return false;
        }
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return false;
        }
        return pipe.getAttachment(side) == attachment;
    }

    public FilterInversionMode getFilterMode() {
        return attachment.getFilterInversion();
    }

    public void setFilterMode(FilterInversionMode value) {
        if (isClientSide()) {
            MdPackets.sendSetFilterMode(containerId, value);
        }
        attachment.setFilterInversion(value);
        pipe.setChanged();
    }

    public RedstoneMode getRedstoneMode() {
        return attachment.getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        if (isClientSide()) {
            MdPackets.sendSetRedstoneMode(containerId, redstoneMode);
        }
        attachment.setRedstoneMode(redstoneMode);
        pipe.setChanged();
    }

    public boolean isSettingSupported(Setting setting) {
        return attachment.getSupportedSettings().contains(setting);
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

    public boolean isEnabledViaRedstone() {
        return attachment.isEnabledViaRedstone(pipe);
    }
}
