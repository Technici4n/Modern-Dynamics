package dev.technici4n.moderndynamics.util;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;

public class DropHelper {
	public static void dropStack(BlockEntity blockEntity, ItemStack stack) {
		var pos = blockEntity.getPos();
		ItemScatterer.spawn(blockEntity.getWorld(), pos.getX(), pos.getY(), pos.getZ(), stack);
	}

	public static void dropStack(BlockEntity blockEntity, ItemVariant variant, long amount) {
		while (amount > 0) {
			int dropped = (int) Math.min(amount, variant.getItem().getMaxCount());
			dropStack(blockEntity, variant.toStack(dropped));
			amount -= dropped;
		}
	}
}
