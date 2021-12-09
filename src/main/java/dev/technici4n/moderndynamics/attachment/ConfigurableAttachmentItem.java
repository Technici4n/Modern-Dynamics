package dev.technici4n.moderndynamics.attachment;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class ConfigurableAttachmentItem extends AttachmentItem {
	public final int configWidth, configHeight;

	public ConfigurableAttachmentItem(Attachment attachment, int configWidth, int configHeight) {
		super(attachment);
		this.configWidth = configWidth;
		this.configHeight = configHeight;
	}

	public ItemVariant getItemVariant(ItemStack stack, int x, int y) {
		var nbt = stack.getOrCreateNbt();
		var list = nbt.getList("items", NbtElement.COMPOUND_TYPE);
		return ItemVariant.fromNbt(list.getCompound(x + y * configWidth));
	}

	public void setItemVariant(ItemStack stack, int x, int y, ItemVariant variant) {
		var nbt = stack.getOrCreateNbt();
		var list = nbt.getList("items", NbtElement.COMPOUND_TYPE);
		nbt.put("items", list);
		while (list.size() < configWidth * configHeight) {
			list.add(ItemVariant.blank().toNbt());
		}
		list.set(x + y * configWidth, variant.toNbt());
	}

	public boolean matchesFilter(ItemStack stack, ItemVariant variant) {
		boolean isEmpty = true;
		for (int i = 0; i < configHeight; ++i) {
			for (int j = 0; j < configWidth; ++j) {
				ItemVariant v = getItemVariant(stack, i, j);
				if (!v.isBlank()) {
					if (v.equals(variant)) {
						return true;
					}
					isEmpty = false;
				}
			}
		}
		return isEmpty;
	}
}
