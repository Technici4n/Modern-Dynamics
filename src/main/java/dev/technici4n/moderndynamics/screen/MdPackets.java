package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.util.MdId;
import dev.technici4n.moderndynamics.util.UnsidedPacketHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class MdPackets {
	public static final Identifier SET_ITEM_VARIANT = MdId.of("set_item_variant");
	public static final UnsidedPacketHandler SET_ITEM_VARIANT_HANDLER = (player, buf) -> {
		int syncId = buf.readInt();
		int x = buf.readInt();
		int y = buf.readInt();
		ItemVariant variant = ItemVariant.fromPacket(buf);
		return () -> {
			ScreenHandler handler = player.currentScreenHandler;
			if (handler.syncId == syncId) {
				((AttachmentScreenHandler) handler).configBackend.setItemVariant(x, y, variant);
			}
		};
	};
}
