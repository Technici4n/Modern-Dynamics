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
package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.util.MdId;
import dev.technici4n.moderndynamics.util.UnsidedPacketHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class MdPackets {
    public static final Identifier SET_ITEM_VARIANT = MdId.of("set_item_variant");
    public static final UnsidedPacketHandler SET_ITEM_VARIANT_HANDLER = (player, buf) -> {
        int syncId = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        ItemVariant variant = ItemVariant.fromPacket(buf);
        return () -> {
            ScreenHandler handler = player.currentScreenHandler;
            if (handler.syncId == syncId) {
                ((AttachmentScreenHandler) handler).attachment.setFilter(x, y, variant);
            }
        };
    };
}
