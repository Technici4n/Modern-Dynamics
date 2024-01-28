package dev.technici4n.moderndynamics.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

record SetEnum<T extends Enum<T>>(ResourceLocation id, int syncId, T value) implements CustomPacketPayload {
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeEnum(value);
    }

    public static <T extends Enum<T>> FriendlyByteBuf.Reader<SetEnum<T>> makeReader(ResourceLocation id, Class<T> enumClass) {
        return buf -> {
            var syncId = buf.readInt();
            var value = buf.readEnum(enumClass);
            return new SetEnum<>(id, syncId, value);
        };
    }
}
