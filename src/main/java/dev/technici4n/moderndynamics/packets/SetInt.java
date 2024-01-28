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
