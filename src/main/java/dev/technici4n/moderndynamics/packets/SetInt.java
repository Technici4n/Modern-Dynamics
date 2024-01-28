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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

record SetInt(ResourceLocation id, int syncId, int value) implements CustomPacketPayload {
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeInt(value);
    }

    public static FriendlyByteBuf.Reader<SetInt> makeReader(ResourceLocation id) {
        return buf -> {
            var syncId = buf.readInt();
            var value = buf.readInt();
            return new SetInt(id, syncId, value);
        };
    }
}
