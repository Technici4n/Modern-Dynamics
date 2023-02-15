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
package dev.technici4n.moderndynamics.compat.mi;

import dev.technici4n.moderndynamics.network.mienergy.MICableTier;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import team.reborn.energy.api.EnergyStorage;

public class MIAbsentProxy implements MIProxy {
    private static final BlockApiLookup<? extends EnergyStorage, Direction> MISSING_LOOKUP = BlockApiLookup.get(MdId.of("mi_energy_missing"),
            EnergyStorage.class, Direction.class);

    @Override
    public BlockApiLookup<? extends EnergyStorage, Direction> getLookup() {
        return MISSING_LOOKUP;
    }

    @Override
    public boolean canConnect(EnergyStorage storage, MICableTier tier) {
        return false;
    }
}
