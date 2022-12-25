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
package dev.technici4n.moderndynamics.client;

import dev.technici4n.moderndynamics.MdProxy;
import dev.technici4n.moderndynamics.util.UnsidedPacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ClientProxy extends MdProxy {
    @Override
    public boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public void registerPacketHandler(ResourceLocation packetId, UnsidedPacketHandler unsidedHandler) {
        super.registerPacketHandler(packetId, unsidedHandler);

        ClientPlayNetworking.registerGlobalReceiver(packetId,
                (mc, handler, buf, responseSender) -> mc.execute(unsidedHandler.handlePacket(mc.player, buf)));
    }

    @Override
    public void sendPacket(ResourceLocation packetId, FriendlyByteBuf buf) {
        ClientPlayNetworking.send(packetId, buf);
    }
}
