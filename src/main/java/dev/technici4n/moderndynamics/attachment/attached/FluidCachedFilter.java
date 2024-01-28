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
package dev.technici4n.moderndynamics.attachment.attached;

import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.util.FluidVariant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FluidCachedFilter {
    private final Set<FluidVariant> listedVariants;
    private final FilterInversionMode filterInversion;

    public FluidCachedFilter(List<FluidVariant> variants,
            FilterInversionMode filterInversion) {
        this.listedVariants = new HashSet<>();
        this.filterInversion = filterInversion;

        for (var variant : variants) {
            if (!variant.isBlank()) {
                listedVariants.add(variant);
            }
        }
    }

    public boolean matches(FluidVariant variant) {
        return (filterInversion == FilterInversionMode.WHITELIST) == listedVariants.contains(variant);
    }
}
