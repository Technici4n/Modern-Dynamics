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
package dev.technici4n.moderndynamics.network.mienergy;

public enum MICableTier {
    LV("lv", 32),
    MV("mv", 32 * 4),
    HV("hv", 32 * 4 * 8),
    EV("ev", 32 * 4 * 8 * 8),
    SUPERCONDUCTOR("superconductor", 128000000);

    // Very important that this matches exactly what's used in MIEnergyStorage#canConnect.
    private final String tierName;
    private final long baseEu;

    MICableTier(String tierName, long baseEu) {
        this.tierName = tierName;
        this.baseEu = baseEu;
    }

    public String getName() {
        return this.tierName;
    }

    /**
     * Max network-wide transfer and max storage in each cable.
     */
    public long getMax() {
        return baseEu * 8;
    }
}
