package dev.technici4n.moderndynamics.screen;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ConfigSlot extends Slot {
	public final int configX, configY;
	private final ConfigBackend backend;

	public ConfigSlot(int x, int y, ConfigBackend backend, int configX, int configY) {
		super(new SimpleInventory(1), 0, x, y);
		this.configX = configX;
		this.configY = configY;
		this.backend = backend;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack getStack() {
		return backend.getItemVariant(configX, configY).toStack();
	}
}
