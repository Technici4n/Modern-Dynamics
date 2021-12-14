/*
 * Modern Dynamics
 * Copyright (C) 2021 shartte & Technici4n
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
