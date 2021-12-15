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
import com.mojang.blaze3d.vertex.PoseStack;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AttachmentScreen extends AbstractContainerScreen<AttachmentMenu> {
    /**
     * Horizontal gap between setting buttons.
     */
    private static final int BUTTON_GAP = 6;

    public static final ResourceLocation TEXTURE = MdId.of("textures/gui/attachment.png");

    public AttachmentScreen(AttachmentMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 204;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    protected void init() {
        super.init();

        var toggleButtons = new ArrayList<CycleSettingButton<?>>();
        toggleButtons.add(new CycleSettingButton<>(0, 0, CycleSettingButton.FILTER_MODE, menu.getFilterMode(), menu::setFilterMode));

        // Lay out toggle buttons
        var overallWidth = toggleButtons.stream().mapToInt(AbstractWidget::getWidth).sum()
                + (toggleButtons.size() - 1) * BUTTON_GAP;
        var x = leftPos + (imageWidth - overallWidth) / 2;

        for (var toggleButton : toggleButtons) {
            toggleButton.x = x;
            toggleButton.y = topPos + 82;
            x += toggleButton.getWidth() + BUTTON_GAP;

            addRenderableWidget(toggleButton);
        }
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        // Background
        blit(matrices, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // Draw each slot's background
        for (Slot slot : getMenu().slots) {
            if (slot instanceof ConfigSlot) {
                blit(matrices, leftPos + slot.x - 1, topPos + slot.y - 1, 7, 122, 18, 18);
            }
        }
    }
}
