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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemPath {
    /**
     * Starting position of the items, i.e. the chest they were pulled from. (Not the pipe!)
     */
    public final BlockPos startingPos;
    public final BlockPos targetPos;
    public final Direction[] path;

    public ItemPath(BlockPos startingPos, BlockPos targetPos, Direction[] path) {
        this.startingPos = startingPos;
        this.targetPos = targetPos;
        this.path = path;
    }

    public SimulatedInsertionTarget getInsertionTarget(World world) {
        return SimulatedInsertionTargets.getTarget(world, targetPos, path[path.length - 1].getOpposite());
    }

    public TravelingItem makeTravelingItem(ItemVariant variant, long amount) {
        return new TravelingItem(
                variant,
                amount,
                this,
                FailedInsertStrategy.SEND_BACK_TO_SOURCE,
                0);
    }
}
