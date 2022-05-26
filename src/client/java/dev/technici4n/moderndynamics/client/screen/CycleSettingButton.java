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

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.technici4n.moderndynamics.attachment.settings.FilterDamageMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterModMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterNbtMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterSimilarMode;
import dev.technici4n.moderndynamics.attachment.settings.OversendingMode;
import dev.technici4n.moderndynamics.attachment.settings.RoutingMode;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class CycleSettingButton<T> extends Button {
    private final List<CycleSetting<T>> settings;
    private int currentSetting;
    private final BiConsumer<T, Boolean> onChange;

    public static final List<CycleSetting<FilterInversionMode>> FILTER_INVERSION = ImmutableList.of(
            new CycleSetting<>(FilterInversionMode.WHITELIST, new TranslatableComponent("gui.moderndynamics.setting.filter_mode.whitelist"), 176, 0),
            new CycleSetting<>(FilterInversionMode.BLACKLIST, new TranslatableComponent("gui.moderndynamics.setting.filter_mode.blacklist"), 196, 0));

    public static final List<CycleSetting<FilterNbtMode>> FILTER_NBT = ImmutableList.of(
            new CycleSetting<>(FilterNbtMode.RESPECT_NBT, new TranslatableComponent("gui.moderndynamics.setting.filter_nbt.respect_nbt"), 216, 0),
            new CycleSetting<>(FilterNbtMode.IGNORE_NBT, new TranslatableComponent("gui.moderndynamics.setting.filter_nbt.ignore_nbt"), 236, 0));

    public static final List<CycleSetting<FilterDamageMode>> FILTER_DAMAGE = ImmutableList.of(
            new CycleSetting<>(FilterDamageMode.RESPECT_DAMAGE, new TranslatableComponent("gui.moderndynamics.setting.filter_damage.respect_damage"),
                    176, 60),
            new CycleSetting<>(FilterDamageMode.IGNORE_DAMAGE, new TranslatableComponent("gui.moderndynamics.setting.filter_damage.ignore_damage"),
                    196, 60));

    public static final List<CycleSetting<FilterModMode>> FILTER_MOD = ImmutableList.of(
            new CycleSetting<>(FilterModMode.IGNORE_MOD, new TranslatableComponent("gui.moderndynamics.setting.filter_mod.ignore_mod"), 196, 120),
            new CycleSetting<>(FilterModMode.INCLUDE_ALL_OF_MOD,
                    new TranslatableComponent("gui.moderndynamics.setting.filter_mod.include_all_of_mod"), 176, 120));

    public static final List<CycleSetting<FilterSimilarMode>> FILTER_SIMILAR = ImmutableList.of(
            new CycleSetting<>(FilterSimilarMode.IGNORE_SIMILAR,
                    new TranslatableComponent("gui.moderndynamics.setting.filter_similar.ignore_similar"), 236, 60),
            new CycleSetting<>(FilterSimilarMode.INCLUDE_SIMILAR,
                    new TranslatableComponent("gui.moderndynamics.setting.filter_similar.include_similar"), 216, 60));

    public static final List<CycleSetting<RoutingMode>> ROUTING_MODE = ImmutableList.of(
            new CycleSetting<>(RoutingMode.CLOSEST, new TranslatableComponent("gui.moderndynamics.setting.routing_mode.closest"), 0, 204),
            new CycleSetting<>(RoutingMode.FURTHEST, new TranslatableComponent("gui.moderndynamics.setting.routing_mode.furthest"), 20, 204),
            new CycleSetting<>(RoutingMode.RANDOM, new TranslatableComponent("gui.moderndynamics.setting.routing_mode.random"), 40, 204),
            new CycleSetting<>(RoutingMode.ROUND_ROBIN, new TranslatableComponent("gui.moderndynamics.setting.routing_mode.round_robin"), 60, 204));

    public static final List<CycleSetting<OversendingMode>> OVERSENDING_MODE = ImmutableList.of(
            new CycleSetting<>(OversendingMode.PREVENT_OVERSENDING,
                    new TranslatableComponent("gui.moderndynamics.setting.oversending_mode.prevent_oversending"), 100, 204),
            new CycleSetting<>(OversendingMode.ALLOW_OVERSENDING,
                    new TranslatableComponent("gui.moderndynamics.setting.oversending_mode.allow_oversending"), 80, 204));

    public CycleSettingButton(List<CycleSetting<T>> settings, T initialSetting, BiConsumer<T, Boolean> onChange) {
        super(0, 0, 20, 20, TextComponent.EMPTY, button -> {
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
        onChange.accept(getCurrentSetting().value(), true);
    }

    private CycleSetting<T> getCurrentSetting() {
        return settings.get(currentSetting);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ItemAttachedIoScreen.TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        var setting = getCurrentSetting();
        var y = setting.spriteY();
        if (!isActive()) {
            y += 40;
        } else if (isHovered) {
            y += 20;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, setting.spriteX(), y, width, height);

        if (this.isHovered) {
            Minecraft.getInstance().screen.renderTooltip(poseStack, setting.tooltip(), mouseX, mouseY);
        }
    }
}
