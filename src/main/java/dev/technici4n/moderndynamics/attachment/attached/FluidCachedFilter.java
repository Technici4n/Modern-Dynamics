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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidCachedFilter {
    private final Set<FluidResource> listedResources;
    private final FilterInversionMode filterInversion;

    public FluidCachedFilter(List<FluidResource> resources,
            FilterInversionMode filterInversion) {
        this.listedResources = new HashSet<>();
        this.filterInversion = filterInversion;

        for (var resource : resources) {
            if (!resource.isEmpty()) {
                listedResources.add(resource);
            }
        }
    }

    public boolean matches(FluidResource resource) {
        return (filterInversion == FilterInversionMode.WHITELIST) == listedResources.contains(resource);
    }
}
