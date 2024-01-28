package dev.technici4n.moderndynamics.packets;

import dev.technici4n.moderndynamics.util.FluidVariant;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetFluidVariant(int syncId, int configIdx, FluidVariant variant) implements CustomPacketPayload {
    public static final ResourceLocation ID = MdId.of("set_fluid_variant");

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    public static SetFluidVariant read(FriendlyByteBuf buf) {
        int syncId = buf.readInt();
        int configIdx = buf.readInt();
        FluidVariant variant = FluidVariant.fromPacket(buf);
        return new SetFluidVariant(syncId, configIdx, variant);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
