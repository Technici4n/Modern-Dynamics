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
package dev.technici4n.moderndynamics.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class PlusMinusButton extends Button {
    private final boolean minus;

    public PlusMinusButton(int x, int y, boolean minus, Runnable click) {
        super(x, y, 14, 14, Component.empty(), btn -> click.run(), DEFAULT_NARRATION);
        this.minus = minus;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var x = 216 + (minus ? 0 : width);
        var y = 120;
        if (!isActive()) {
            y += 2 * height;
        } else if (isHovered) {
            y += height;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ItemAttachedIoScreen.TEXTURE, this.getX(), this.getY(), x, y, width, height, 256, 256,
                ARGB.white(this.alpha));
    }
}
