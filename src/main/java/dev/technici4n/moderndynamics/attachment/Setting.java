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
package dev.technici4n.moderndynamics.attachment;

import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum Setting {
    /**
     * @see ItemAttachedIo#setFilterInversion
     */
    FILTER_INVERSION,
    /**
     * @see ItemAttachedIo#setFilterDamage
     */
    FILTER_DAMAGE,
    /**
     * @see ItemAttachedIo#setFilterNbt
     */
    FILTER_NBT,
    /**
     * @see ItemAttachedIo#setFilterSimilar
     */
    FILTER_SIMILAR,
    /**
     * @see ItemAttachedIo#setFilterMod
     */
    FILTER_MOD,
    /**
     * @see ItemAttachedIo#setRoutingMode
     */
    ROUTING_MODE,
    /**
     * @see ItemAttachedIo#setOversendingMode
     */
    OVERSENDING_MODE,
    /**
     * @see ItemAttachedIo#setMaxItemsInInventory
     */
    MAX_ITEMS_IN_INVENTORY,
    /**
     * @see ItemAttachedIo#setMaxItemsExtracted
     */
    MAX_ITEMS_EXTRACTED;

    private final Component tooltipName;

    public Component getTooltipName() {
        return tooltipName;
    }

    public boolean isFilter() {
        return name().startsWith("FILTER_");
    }

    Setting() {
        this.tooltipName = new TranslatableComponent("gui.moderndynamics.tooltip."
                + name().toLowerCase(Locale.ROOT));
    }
}
