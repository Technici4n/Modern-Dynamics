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

import dev.technici4n.moderndynamics.attachment.attached.AttachedIo;
import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class UpgradeSlot extends Slot {
    private final AttachedIo io;
    private final int slot;

    public UpgradeSlot(AttachedIo io, int slot, int x, int y) {
        super(new SimpleInventory(), -1, x, y);

        this.io = io;
        this.slot = slot;
    }

    @Override
    public ItemStack getStack() {
        return io.getUpgrade(slot);
    }

    @Override
    public void setStack(ItemStack stack) {
        io.setUpgrade(slot, stack);
        markDirty();
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {
        setStack(stack);
    }

    @Override
    public ItemStack takeStack(int amount) {
        return io.removeUpgrade(slot, amount);
    }

    @Override
    public void markDirty() {
        io.onUpgradesChanged();
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return io.mayPlaceUpgrade(slot, stack.getItem());
    }

    @Override
    public int getMaxItemCount() {
        return getMaxItemCount(getStack());
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return LoadedUpgrades.getType(stack.getItem()).getSlotLimit();
    }
}
