package dev.technici4n.moderndynamics.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A packet handler that works both client-side and server-side.
 */
public interface UnsidedPacketHandler {
    Runnable handlePacket(PlayerEntity player, PacketByteBuf buf);

    default void handleC2S(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf,
                           PacketSender responseSender) {
        server.execute(handlePacket(player, buf));
    }

    @Environment(EnvType.CLIENT)
    default void handleS2C(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        client.execute(handlePacket(client.player, buf));
    }
}