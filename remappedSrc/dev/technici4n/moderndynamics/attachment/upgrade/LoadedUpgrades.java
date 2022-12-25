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
package dev.technici4n.moderndynamics.attachment.upgrade;

import dev.technici4n.moderndynamics.gui.MdPackets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class LoadedUpgrades {
    private static LoadedUpgrades holder = new LoadedUpgrades(Map.of(), List.of());
    private static final UpgradeType DUMMY_UPGRADE = UpgradeType.createDummy();

    public static LoadedUpgrades get() {
        return holder;
    }

    public static UpgradeType getType(Item item) {
        return holder.map.getOrDefault(item, DUMMY_UPGRADE);
    }

    public static void trySet(@Nullable LoadedUpgrades upgrades) {
        if (upgrades != null) {
            holder = upgrades;
        }
    }

    public static void syncToClient(ServerPlayerEntity player) {
        var buf = PacketByteBufs.create();
        holder.toPacket(buf);
        ServerPlayNetworking.send(player, MdPackets.SET_ATTACHMENT_UPGRADES, buf);
    }

    public final Map<Item, UpgradeType> map;
    public final List<Item> list;

    public LoadedUpgrades(Map<Item, UpgradeType> map, List<Item> list) {
        this.map = Collections.unmodifiableMap(map);
        this.list = Collections.unmodifiableList(list);
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeVarInt(list.size());
        for (var upgradeItem : list) {
            buf.writeVarInt(Item.getRawId(upgradeItem));
            map.get(upgradeItem).writePacket(buf);
        }
    }

    public static LoadedUpgrades fromPacket(PacketByteBuf buf) {
        int count = buf.readVarInt();
        Map<Item, UpgradeType> upgrades = new IdentityHashMap<>();
        List<Item> orderedUpgrades = new ArrayList<>();

        for (int i = 0; i < count; ++i) {
            var item = Item.byRawId(buf.readVarInt());
            upgrades.put(item, UpgradeType.readPacket(buf));
            orderedUpgrades.add(item);
        }

        return new LoadedUpgrades(upgrades, orderedUpgrades);
    }
}
