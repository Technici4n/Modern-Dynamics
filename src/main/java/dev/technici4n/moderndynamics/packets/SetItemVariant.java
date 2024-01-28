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
