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
package dev.technici4n.moderndynamics.client.attachment;

import dev.technici4n.moderndynamics.attachment.upgrade.UpgradeType;
import dev.technici4n.moderndynamics.attachment.upgrade.UpgradeTypes;
import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.Item;

public class SetAttachmentUpgradesPacket {
    public static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler, buf, responseSender) -> {
        int count = buf.readVarInt();
        Map<Item, UpgradeType> upgrades = new IdentityHashMap<>();

        for (int i = 0; i < count; ++i) {
            upgrades.put(Item.byId(buf.readVarInt()), UpgradeType.readPacket(buf));
        }

        if (!handler.getConnection().isMemoryConnection()) {
            client.execute(() -> {
                UpgradeTypes.uploadMap(upgrades);
            });
        }
    };
}
