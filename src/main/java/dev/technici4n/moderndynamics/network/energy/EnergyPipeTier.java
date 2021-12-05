/*
 * Modern Transportation
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
package dev.technici4n.moderndynamics.network.energy;

public enum EnergyPipeTier {
    BASIC(1000, 1000),
    HARDENED(4000, 4000),
    REINFORCED(9000, 9000),
    SIGNALUM(16000, 16000),
    RESONANT(25000, 25000);

    // TODO: config
    private final int capacity;
    private final int maxConnectionTransfer;

    EnergyPipeTier(int capacity, int maxConnectionTransfer) {
        this.capacity = capacity;
        this.maxConnectionTransfer = maxConnectionTransfer;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getMaxConnectionTransfer() {
        return maxConnectionTransfer;
    }
}
