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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UpgradeSlot extends Slot {
    private final AttachedIo io;
    private final int slot;

    public UpgradeSlot(AttachedIo io, int slot, int x, int y) {
        super(new SimpleContainer(), -1, x, y);

        this.io = io;
        this.slot = slot;
    }

    @Override
    public ItemStack getItem() {
        return io.getUpgrade(slot);
    }

    @Override
    public void set(ItemStack stack) {
        io.setUpgrade(slot, stack);
        setChanged();
    }

    @Override
    public ItemStack remove(int amount) {
        return io.removeUpgrade(slot, amount);
    }

    @Override
    public void setChanged() {
        io.onUpgradesChanged();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return io.mayPlaceUpgrade(slot, stack.getItem());
    }

    @Override
    public int getMaxStackSize() {
        return getMaxStackSize(getItem());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return LoadedUpgrades.getType(stack.getItem()).getSlotLimit();
    }
}
