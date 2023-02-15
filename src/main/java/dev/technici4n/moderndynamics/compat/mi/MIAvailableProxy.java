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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import team.reborn.energy.api.EnergyStorage;

public class MIAvailableProxy implements MIProxy {
    private static final BlockApiLookup<? extends EnergyStorage, Direction> LOOKUP;
    private static final MethodHandle CAN_CONNECT;

    static {
        try {
            // noinspection unchecked,rawtypes
            LOOKUP = (BlockApiLookup) Class.forName("aztech.modern_industrialization.api.energy.EnergyApi").getField("SIDED").get(null);

            var miEnergyStorage = Class.forName("aztech.modern_industrialization.api.energy.MIEnergyStorage");
            var rawMethod = MethodHandles.lookup().findVirtual(miEnergyStorage, "canConnect", MethodType.methodType(boolean.class, String.class));
            // Convert first argument to EnergyStorage because that's what we'll be passing in the invokeExact call.
            CAN_CONNECT = rawMethod.asType(rawMethod.type().changeParameterType(0, EnergyStorage.class));
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Failed to initialize Modern Dynamics MI proxy", exception);
        }
    }

    @Override
    public BlockApiLookup<? extends EnergyStorage, Direction> getLookup() {
        return LOOKUP;
    }

    @Override
    public boolean canConnect(EnergyStorage storage, MICableTier tier) {
        try {
            return (boolean) CAN_CONNECT.invokeExact(storage, tier.getName());
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to invoke MIEnergyStorage#canConnect", throwable);
        }
    }
}
