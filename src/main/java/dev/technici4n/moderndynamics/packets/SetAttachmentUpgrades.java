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

import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;

public record SetAttachmentUpgrades(LoadedUpgrades holder) implements CustomPacketPayload {
    public static final ResourceLocation ID = MdId.of("set_attachment_upgrades");

    @Override
    public void write(FriendlyByteBuf buffer) {
        holder.toPacket(buffer);
    }

    public static SetAttachmentUpgrades read(FriendlyByteBuf buffer) {
        return new SetAttachmentUpgrades(LoadedUpgrades.fromPacket(buffer));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static final IPlayPayloadHandler<SetAttachmentUpgrades> HANDLER = (payload, context) -> {
        context.player().ifPresent(player -> {
            if (!(player instanceof LocalPlayer)) {
                return;
            }

            context.workHandler().execute(() -> {
                LoadedUpgrades.trySet(payload.holder);
            });
        });
    };

}
