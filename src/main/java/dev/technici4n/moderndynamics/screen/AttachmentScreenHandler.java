package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.ConfigurableAttachmentItem;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AttachmentScreenHandler extends ScreenHandler {
	public static final ScreenHandlerType<AttachmentScreenHandler> TYPE = ScreenHandlerRegistry.registerExtended(
			MdId.of("attachment"),
			AttachmentScreenHandler::new
	);

	public final ConfigBackend configBackend;

	private AttachmentScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		this(syncId, playerInventory, ConfigBackend.makeClient(buf));
	}

	protected AttachmentScreenHandler(int syncId, PlayerInventory playerInventory, ConfigBackend backend) {
		super(TYPE, syncId);
		this.configBackend = backend;

		// Config slots
		var attachment = configBackend.getAttachment();
		for (int i = 0; i < attachment.configHeight; ++i) {
			for (int j = 0; j < attachment.configWidth; ++j) {
				this.addSlot(new ConfigSlot(8 + j * 18, 30 + i * 18, backend, i, j));
			}
		}

		// Player inventory slots
		int i;
		for(i = 0; i < 3; ++i) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
			}
		}
		for(i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 181));
		}
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		if (slotIndex >= 0 && getSlot(slotIndex) instanceof ConfigSlot configSlot) {
			configBackend.setItemVariant(configSlot.configX, configSlot.configY, ItemVariant.of(getCursorStack()));
		} else {
			super.onSlotClick(slotIndex, button, actionType, player);
		}
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void sendContentUpdates() {
		super.sendContentUpdates();
	}
}
