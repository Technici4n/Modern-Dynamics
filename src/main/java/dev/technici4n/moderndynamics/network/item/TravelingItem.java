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

import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.network.item.sync.ClientTravelingItem;
import dev.technici4n.moderndynamics.util.ItemVariant;
import dev.technici4n.moderndynamics.util.SerializationHelper;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class TravelingItem {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();

    public final int id = NEXT_ID.getAndIncrement();
    public final ItemVariant variant;
    public final int amount;
    public final ItemPath path;
    public final FailedInsertStrategy strategy;
    public final double speedMultiplier;
    public double traveledDistance;
    public long lastTick;

    public TravelingItem(ItemVariant variant, int amount, ItemPath path, FailedInsertStrategy strategy, double speedMultiplier,
            double traveledDistance) {
        if (speedMultiplier < 0.5) {
            // Upgrade path from before the speed multiplier was added.
            speedMultiplier = 0.5;
        }
        this.variant = variant;
        this.amount = amount;
        this.path = path;
        this.strategy = strategy;
        this.speedMultiplier = speedMultiplier;
        this.traveledDistance = traveledDistance;
    }

    public int getPathLength() {
        return path.path.length;
    }

    /**
     * Remember: this must never reach 1 !
     */
    public double getSpeed() {
        return speedMultiplier * Constants.Items.SPEED_IN_PIPES;
    }

    public void write(ValueOutput output) {
        output.store("v", ItemVariant.CODEC, variant);
        output.putInt("a", amount);
        output.store("start", BlockPos.CODEC, path.startingPos);
        output.store("end", BlockPos.CODEC, path.targetPos);
        output.putString("path", SerializationHelper.encodePath(path.path));
        output.putDouble("speedMultiplier", speedMultiplier);
        output.putString("strategy", strategy.getSerializedName());
        output.putDouble("d", traveledDistance);
    }

    public static TravelingItem read(ValueInput input) {
        return new TravelingItem(
                input.read("v", ItemVariant.CODEC).orElse(ItemVariant.blank()),
                input.getIntOr("a", 0),
                new ItemPath(
                        input.read("start", BlockPos.CODEC).orElse(BlockPos.ZERO),
                        input.read("end", BlockPos.CODEC).orElse(BlockPos.ZERO),
                        SerializationHelper.decodePath(input.getStringOr("path", ""))),
                FailedInsertStrategy.bySerializedName(input.getStringOr("strategy", "")),
                input.getDoubleOr("speedMultiplier", 0),
                input.getDoubleOr("d", 0));
    }

    void writeClient(RegistryFriendlyByteBuf buf) {
        buf.writeInt(id);
        ItemVariant.STREAM_CODEC.encode(buf, variant);
        buf.writeInt(amount);
        buf.writeDouble(getPathLength() - 1);
        buf.writeDouble(traveledDistance);
        int currentBlock = (int) Math.floor(traveledDistance);
        buf.writeEnum(path.path[currentBlock]);
        buf.writeEnum(path.path[currentBlock + 1]);
        buf.writeDouble(getSpeed());
    }

    static ClientTravelingItem readClient(RegistryFriendlyByteBuf buf) {
        return new ClientTravelingItem(
                buf.readInt(),
                ItemVariant.STREAM_CODEC.decode(buf),
                buf.readInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readEnum(Direction.class),
                buf.readEnum(Direction.class),
                buf.readDouble());
    }
}
