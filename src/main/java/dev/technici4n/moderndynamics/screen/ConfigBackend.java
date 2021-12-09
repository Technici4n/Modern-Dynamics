package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.attachment.ConfigurableAttachmentItem;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.PacketByteBuf;

public interface ConfigBackend {
	ConfigurableAttachmentItem getAttachment();
	ItemVariant getItemVariant(int x, int y);
	void setItemVariant(int x, int y, ItemVariant variant);

	static ConfigBackend makeClient(PacketByteBuf buf) {
		var attachment = buf.readItemStack();
		return new ConfigBackend() {
			@Override
			public ConfigurableAttachmentItem getAttachment() {
				return (ConfigurableAttachmentItem) attachment.getItem();
			}

			@Override
			public ItemVariant getItemVariant(int x, int y) {
				return getAttachment().getItemVariant(attachment, x, y);
			}

			@Override
			public void setItemVariant(int x, int y, ItemVariant variant) {
				getAttachment().setItemVariant(attachment, x, y, variant);
			}
		};
	}
}
