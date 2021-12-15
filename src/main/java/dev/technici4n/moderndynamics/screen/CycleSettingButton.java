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

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.technici4n.moderndynamics.attachment.settings.FilterMode;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class CycleSettingButton<T> extends Button {
    private final List<CycleSetting<T>> settings;
    private int currentSetting;
    private final Consumer<T> onChange;

    public static final List<CycleSetting<FilterMode>> FILTER_MODE = ImmutableList.of(
            new CycleSetting<>(FilterMode.WHITELIST, new TranslatableComponent("gui.moderndynamics.setting.filter_mode.whitelist"), 176, 0),
            new CycleSetting<>(FilterMode.BLACKLIST, new TranslatableComponent("gui.moderndynamics.setting.filter_mode.blacklist"), 196, 0));

    public CycleSettingButton(int x, int y, List<CycleSetting<T>> settings, T initialSetting, Consumer<T> onChange) {
        super(x, y, 20, 20, TextComponent.EMPTY, button -> {
        });
        this.settings = settings;
        for (int i = 0; i < settings.size(); i++) {
            if (settings.get(i).value() == initialSetting) {
                this.currentSetting = i;
                break;
            }
        }
        this.onChange = onChange;
    }

    @Override
    public void onPress() {
        currentSetting = (currentSetting + 1) % settings.size();
        onChange.accept(getCurrentSetting().value());
    }

    private CycleSetting<T> getCurrentSetting() {
        return settings.get(currentSetting);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AttachmentScreen.TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        var setting = getCurrentSetting();
        var y = setting.spriteY();
        if (!isActive()) {
            y += 40;
        } else if (isHoveredOrFocused()) {
            y += 20;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, setting.spriteX(), y, width, height);

        if (this.isHoveredOrFocused()) {
            Minecraft.getInstance().screen.renderTooltip(poseStack, setting.tooltip(), mouseX, mouseY);
        }
    }
}

record CycleSetting<T> (T value, Component tooltip, int spriteX, int spriteY) {
}
