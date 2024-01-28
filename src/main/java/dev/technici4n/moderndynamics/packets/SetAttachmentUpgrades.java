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
