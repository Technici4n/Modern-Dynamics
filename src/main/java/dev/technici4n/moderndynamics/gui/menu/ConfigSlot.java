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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ConfigSlot<T extends AttachedIo> extends Slot {
    private final T attachment;
    private final int configIdx;

    public ConfigSlot(int x, int y, T attachment, int configIdx) {
        super(new SimpleContainer(1), 0, x, y);
        this.attachment = attachment;
        this.configIdx = configIdx;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isActive() {
        return configIdx < attachment.getFilterSize();
    }

    public T getAttachment() {
        return attachment;
    }

    public int getConfigIdx() {
        return configIdx;
    }
}
