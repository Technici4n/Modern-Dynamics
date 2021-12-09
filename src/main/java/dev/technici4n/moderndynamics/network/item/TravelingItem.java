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

import com.mojang.serialization.Codec;
import dev.technici4n.moderndynamics.util.SerializationHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TravelingItem {
    public final ItemVariant variant;
    public final long amount;
    /**
     * Starting position of the items, i.e. the chest they were pulled from. (Not the pipe!)
     */
    public final BlockPos startingPos;
    public final BlockPos targetPos;
    public final Direction[] path;
    public final FailedInsertStrategy strategy;
    public double traveledDistance;

    public TravelingItem(ItemVariant variant, long amount, BlockPos startingPos, BlockPos targetPos, Direction[] path,
            FailedInsertStrategy strategy, double traveledDistance) {
        this.variant = variant;
        this.amount = amount;
        this.startingPos = startingPos;
        this.targetPos = targetPos;
        this.path = path;
        this.strategy = strategy;
        this.traveledDistance = traveledDistance;
    }

    public SimulatedInsertionTarget getInsertionTarget(World world) {
        return SimulatedInsertionTargets.getTarget(world, targetPos, path[path.length - 1].getOpposite());
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("v", variant.toNbt());
        nbt.putLong("a", amount);
        nbt.put("start", SerializationHelper.posToNbt(startingPos));
        nbt.put("end", SerializationHelper.posToNbt(targetPos));
        nbt.putString("path", SerializationHelper.encodePath(path));
        nbt.putString("strategy", strategy.getSerializedName());
        nbt.putDouble("d", traveledDistance);
        return nbt;
    }

    public static TravelingItem fromNbt(NbtCompound nbt) {
        return new TravelingItem(
                ItemVariant.fromNbt(nbt.getCompound("v")),
                nbt.getLong("a"),
                SerializationHelper.posFromNbt(nbt.getCompound("start")),
                SerializationHelper.posFromNbt(nbt.getCompound("end")),
                SerializationHelper.decodePath(nbt.getString("path")),
                FailedInsertStrategy.bySerializedName(nbt.getString("strategy")),
                nbt.getDouble("d")
        );
    }
}
