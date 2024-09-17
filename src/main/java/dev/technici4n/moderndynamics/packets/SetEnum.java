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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

record SetEnum<T extends Enum<T>>(Type<SetEnum<T>> type, int syncId, T value) implements CustomPacketPayload {
    public static <T extends Enum<T>> StreamCodec<FriendlyByteBuf, SetEnum<T>> codec(Type<SetEnum<T>> type, Class<T> enumClass) {
        StreamCodec<FriendlyByteBuf, T> enumCodec = StreamCodec.of(
                FriendlyByteBuf::writeEnum,
                buf -> buf.readEnum(enumClass));

        return StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                SetEnum::syncId,
                enumCodec,
                SetEnum::value,
                (syncId, value) -> new SetEnum<>(type, syncId, value));
    }
}
