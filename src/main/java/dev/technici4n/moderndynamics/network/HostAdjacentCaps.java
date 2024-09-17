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
package dev.technici4n.moderndynamics.network;

import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to track capability caches for all 6 adjacent sides of a host,
 * and automatically schedule a host update on capability invalidation.
 */
public final class HostAdjacentCaps<C> {
    private final NodeHost host;
    private final BlockCapability<C, @Nullable Direction> cap;
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<C, @Nullable Direction>[] capCaches = new BlockCapabilityCache[6];

    public HostAdjacentCaps(NodeHost host, BlockCapability<C, @Nullable Direction> cap) {
        this.host = host;
        this.cap = cap;
    }

    @Nullable
    public C getCapability(Direction dir) {
        int side = dir.get3DDataValue();
        if (capCaches[side] == null) {
            capCaches[side] = BlockCapabilityCache.create(
                    cap,
                    (ServerLevel) Objects.requireNonNull(host.pipe.getLevel()),
                    host.pipe.getBlockPos().relative(dir),
                    dir.getOpposite(),
                    () -> !host.pipe.isRemoved(),
                    host::scheduleUpdate);
        }
        return capCaches[side].getCapability();
    }
}
