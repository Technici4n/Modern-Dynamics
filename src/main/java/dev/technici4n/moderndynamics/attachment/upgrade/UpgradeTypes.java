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
import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class UpgradeTypes {
    private static final Map<Item, UpgradeType> UPGRADES = new IdentityHashMap<>();
    private static final UpgradeType DUMMY_UPGRADE = UpgradeType.createDummy();

    public static UpgradeType getType(Item item) {
        return UPGRADES.getOrDefault(item, DUMMY_UPGRADE);
    }

    public static int getSlotLimit(Item item) {
        return getType(item).getSlotLimit();
    }

    public static void uploadMap(Map<Item, UpgradeType> newMap) {
        UPGRADES.clear();
        UPGRADES.putAll(newMap);
    }

    public static void syncToClients(MinecraftServer server) {
        for (var player : PlayerLookup.all(server)) {
            syncToClient(player);
        }
    }

    public static void syncToClient(ServerPlayer player) {
        var buf = PacketByteBufs.create();
        buf.writeVarInt(UPGRADES.size());
        for (var entry : UPGRADES.entrySet()) {
            buf.writeVarInt(Item.getId(entry.getKey()));
            entry.getValue().writePacket(buf);
        }

        ServerPlayNetworking.send(player, MdPackets.SET_ATTACHMENT_UPGRADES, buf);
    }
}
