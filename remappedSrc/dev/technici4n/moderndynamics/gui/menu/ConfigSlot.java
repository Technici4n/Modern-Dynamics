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
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ConfigSlot<T extends AttachedIo> extends Slot {
    private final T attachment;
    private final int configIdx;

    public ConfigSlot(int x, int y, T attachment, int configIdx) {
        super(new SimpleInventory(1), 0, x, y);
        this.attachment = attachment;
        this.configIdx = configIdx;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    // This just disables vanilla's hover rectangle.
    @Override
    public boolean isEnabled() {
        return isEnabled();
    }

    public T getAttachment() {
        return attachment;
    }

    public int getConfigIdx() {
        return configIdx;
    }

    public boolean isEnabled() {
        return configIdx < attachment.getFilterSize();
    }
}
