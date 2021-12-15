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

import dev.technici4n.moderndynamics.attachment.settings.FilterMode;
import dev.technici4n.moderndynamics.util.MdId;
import dev.technici4n.moderndynamics.util.UnsidedPacketHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MdPackets {
    public static final ResourceLocation SET_ITEM_VARIANT = MdId.of("set_item_variant");
    public static final UnsidedPacketHandler SET_ITEM_VARIANT_HANDLER = (player, buf) -> {
        int syncId = buf.readInt();
        int configIdx = buf.readInt();
        ItemVariant variant = ItemVariant.fromPacket(buf);
        return () -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId && handler instanceof AttachmentMenu attachmentMenu) {
                attachmentMenu.attachment.setFilter(configIdx, variant);
            }
        };
    };

    public static final ResourceLocation SET_FILTER_MODE = MdId.of("set_filter_mode");
    public static final UnsidedPacketHandler SET_FILTER_MODE_HANDLER = (player, buf) -> {
        int syncId = buf.readInt();
        var mode = buf.readEnum(FilterMode.class);
        return () -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId && handler instanceof AttachmentMenu attachmentMenu) {
                attachmentMenu.setFilterMode(mode);
            }
        };
    };

    public static void sendSetFilterMode(int syncId, FilterMode filterMode) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer(64));
        buffer.writeInt(syncId);
        buffer.writeEnum(filterMode);
        ClientPlayNetworking.send(SET_FILTER_MODE, buffer);
    }
}
