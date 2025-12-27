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

import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

public class RedstoneModeButton extends Button {
    private final RedstoneMode mode;
    private final Supplier<RedstoneMode> getter;
    private final BiConsumer<RedstoneMode, Boolean> setter;
    private Rect2i scissorRect;

    public RedstoneModeButton(RedstoneMode mode, Supplier<RedstoneMode> getter, BiConsumer<RedstoneMode, Boolean> setter) {
        super(0, 0, 16, 16, mode.getTranslation(), button -> {
        }, DEFAULT_NARRATION);
        this.mode = mode;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.setter.accept(this.mode, true);
    }

    public void setScissorRect(Rect2i scissorRect) {
        this.scissorRect = scissorRect;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (scissorRect == null) {
            return;
        }

        guiGraphics.enableScissor(
                scissorRect.getX(),
                scissorRect.getY(),
                scissorRect.getWidth(),
                scissorRect.getHeight());

        var x = 176;
        if (!isActive()) {
            x += 16;
        } else if (getter.get() == mode) {
            x += 32;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ItemAttachedIoScreen.TEXTURE, this.getX(), this.getY(), x, 180, width, height, 256, 256,
                ARGB.white(this.alpha));

        // Draw an icon appropriate for the mode of this button
        int iconX = 240;
        int iconY = switch (mode) {
        case IGNORED -> 208;
        case REQUIRES_LOW -> 240;
        case REQUIRES_HIGH -> 224;
        };
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ItemAttachedIoScreen.TEXTURE, this.getX(), this.getY(), iconX, iconY, 16, 16, 256, 256,
                ARGB.white(this.alpha));
        guiGraphics.disableScissor();

        if (this.isHovered) {
            guiGraphics.setTooltipForNextFrame(Minecraft.getInstance().font, getMessage(), mouseX, mouseY);
        }
    }
}
