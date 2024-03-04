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
package dev.technici4n.moderndynamics.packets;

import dev.technici4n.moderndynamics.util.ItemVariant;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetItemVariant(int syncId, int configIdx, ItemVariant variant) implements CustomPacketPayload {

    public static final ResourceLocation ID = MdId.of("set_item_variant");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeInt(configIdx);
        variant.toPacket(buf);
    }

    public static SetItemVariant read(FriendlyByteBuf buf) {
        int syncId = buf.readInt();
        int configIdx = buf.readInt();
        ItemVariant variant = ItemVariant.fromPacket(buf);
        return new SetItemVariant(syncId, configIdx, variant);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
