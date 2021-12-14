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

import dev.technici4n.moderndynamics.util.SerializationHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.NbtCompound;

public class TravelingItem {
    public final ItemVariant variant;
    public final long amount;
    public final ItemPath path;
    public final FailedInsertStrategy strategy;
    public double traveledDistance;

    public TravelingItem(ItemVariant variant, long amount, ItemPath path, FailedInsertStrategy strategy, double traveledDistance) {
        this.variant = variant;
        this.amount = amount;
        this.path = path;
        this.strategy = strategy;
        this.traveledDistance = traveledDistance;
    }

    public int getPathLength() {
        return path.path.length;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("v", variant.toNbt());
        nbt.putLong("a", amount);
        nbt.put("start", SerializationHelper.posToNbt(path.startingPos));
        nbt.put("end", SerializationHelper.posToNbt(path.targetPos));
        nbt.putString("path", SerializationHelper.encodePath(path.path));
        nbt.putString("strategy", strategy.getSerializedName());
        nbt.putDouble("d", traveledDistance);
        return nbt;
    }

    public static TravelingItem fromNbt(NbtCompound nbt) {
        return new TravelingItem(
                ItemVariant.fromNbt(nbt.getCompound("v")),
                nbt.getLong("a"),
                new ItemPath(
                        SerializationHelper.posFromNbt(nbt.getCompound("start")),
                        SerializationHelper.posFromNbt(nbt.getCompound("end")),
                        SerializationHelper.decodePath(nbt.getString("path"))),
                FailedInsertStrategy.bySerializedName(nbt.getString("strategy")),
                nbt.getDouble("d"));
    }
}
