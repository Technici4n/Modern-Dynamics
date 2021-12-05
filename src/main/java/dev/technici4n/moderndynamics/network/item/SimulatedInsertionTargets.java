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
package dev.technici4n.moderndynamics.network.item;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SimulatedInsertionTargets {
    private static final Map<Coord, SimulatedInsertionTarget> TARGETS = new HashMap<>();

    private record Coord(ServerWorld world, BlockPos pos, Direction direction) {
    }

    public static SimulatedInsertionTarget getTarget(ServerWorld world, BlockPos pos, Direction side) {
        return TARGETS.computeIfAbsent(new Coord(world, pos, side), coord -> {
            var cache = BlockApiCache.create(ItemStorage.SIDED, world, pos);
            return new SimulatedInsertionTarget(() -> cache.find(side));
        });
    }

    static {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TARGETS.clear());
    }
}
