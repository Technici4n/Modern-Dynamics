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
package dev.technici4n.moderndynamics;

import dev.technici4n.moderndynamics.util.UnsidedPacketHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class MdProxy {
    public static final MdProxy INSTANCE = switch (FabricLoader.getInstance().getEnvironmentType()) {
    case SERVER -> new MdProxy();
    case CLIENT -> {
        try {
            yield (MdProxy) Class.forName("dev.technici4n.moderndynamics.client.ClientProxy").getConstructor().newInstance();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to instantiate Modern Dynamics client proxy.", exception);
        }
    }
    };

    public boolean isShiftDown() {
        return false;
    }

    /**
     * Register a packet that can be received by both sides, server and client.
     */
    public void registerPacketHandler(ResourceLocation packetId, UnsidedPacketHandler unsidedHandler) {
        ServerPlayNetworking.registerGlobalReceiver(packetId,
                (ms, player, handler, buf, responseSender) -> ms.execute(unsidedHandler.handlePacket(player, buf)));
    }

    /**
     * Send a packet to the server.
     */
    public void sendPacket(ResourceLocation packetId, FriendlyByteBuf buf) {
    }
}
