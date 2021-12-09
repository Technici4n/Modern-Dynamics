package dev.technici4n.moderndynamics.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AttachmentScreen extends HandledScreen<AttachmentScreenHandler> {
	private static final Identifier TEXTURE = MdId.of("textures/gui/attachment.png");

	public AttachmentScreen(AttachmentScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundHeight = 204;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, TEXTURE);
		// Background
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		// Draw each slot's background
		for (Slot slot : getScreenHandler().slots) {
			if (slot instanceof ConfigSlot) {
				drawTexture(matrices, x + slot.x - 1, y + slot.y - 1, 7, 122, 18, 18);
			}
		}
	}
}
