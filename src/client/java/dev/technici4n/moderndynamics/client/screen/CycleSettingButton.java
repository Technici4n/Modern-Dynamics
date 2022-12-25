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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class CycleSettingButton<T> extends Button {
    private static final int DISABLED_SETTING = 0; // first setting is used when the button is disabled

    private final List<CycleSetting<T>> settings;
    private int currentSetting;
    private final BiConsumer<T, Boolean> onChange;
    private boolean advancedBehavior = false;

    public static final List<CycleSetting<FilterInversionMode>> FILTER_INVERSION = ImmutableList.of(
            new CycleSetting<>(FilterInversionMode.WHITELIST, Component.translatable("gui.moderndynamics.setting.filter_mode.whitelist"), 176, 0),
            new CycleSetting<>(FilterInversionMode.BLACKLIST, Component.translatable("gui.moderndynamics.setting.filter_mode.blacklist"), 196, 0));

    public static final List<CycleSetting<FilterNbtMode>> FILTER_NBT = ImmutableList.of(
            new CycleSetting<>(FilterNbtMode.RESPECT_NBT, Component.translatable("gui.moderndynamics.setting.filter_nbt.respect_nbt"), 216, 0),
            new CycleSetting<>(FilterNbtMode.IGNORE_NBT, Component.translatable("gui.moderndynamics.setting.filter_nbt.ignore_nbt"), 236, 0));

    public static final List<CycleSetting<FilterDamageMode>> FILTER_DAMAGE = ImmutableList.of(
            new CycleSetting<>(FilterDamageMode.RESPECT_DAMAGE, Component.translatable("gui.moderndynamics.setting.filter_damage.respect_damage"),
                    176, 60),
            new CycleSetting<>(FilterDamageMode.IGNORE_DAMAGE, Component.translatable("gui.moderndynamics.setting.filter_damage.ignore_damage"),
                    196, 60));

    public static final List<CycleSetting<FilterModMode>> FILTER_MOD = ImmutableList.of(
            new CycleSetting<>(FilterModMode.IGNORE_MOD, Component.translatable("gui.moderndynamics.setting.filter_mod.ignore_mod"), 196, 120),
            new CycleSetting<>(FilterModMode.INCLUDE_ALL_OF_MOD,
                    Component.translatable("gui.moderndynamics.setting.filter_mod.include_all_of_mod"), 176, 120));

    public static final List<CycleSetting<FilterSimilarMode>> FILTER_SIMILAR = ImmutableList.of(
            new CycleSetting<>(FilterSimilarMode.IGNORE_SIMILAR,
                    Component.translatable("gui.moderndynamics.setting.filter_similar.ignore_similar"), 236, 60),
            new CycleSetting<>(FilterSimilarMode.INCLUDE_SIMILAR,
                    Component.translatable("gui.moderndynamics.setting.filter_similar.include_similar"), 216, 60));

    public static final List<CycleSetting<RoutingMode>> ROUTING_MODE = ImmutableList.of(
            new CycleSetting<>(RoutingMode.CLOSEST, Component.translatable("gui.moderndynamics.setting.routing_mode.closest"), 216, 196),
            new CycleSetting<>(RoutingMode.FURTHEST, Component.translatable("gui.moderndynamics.setting.routing_mode.furthest"), 20, 204),
            new CycleSetting<>(RoutingMode.RANDOM, Component.translatable("gui.moderndynamics.setting.routing_mode.random"), 40, 204),
            new CycleSetting<>(RoutingMode.ROUND_ROBIN, Component.translatable("gui.moderndynamics.setting.routing_mode.round_robin"), 60, 204));

    public static final List<CycleSetting<OversendingMode>> OVERSENDING_MODE = ImmutableList.of(
            new CycleSetting<>(OversendingMode.PREVENT_OVERSENDING,
                    Component.translatable("gui.moderndynamics.setting.oversending_mode.prevent_oversending"), 196, 196),
            new CycleSetting<>(OversendingMode.ALLOW_OVERSENDING,
                    Component.translatable("gui.moderndynamics.setting.oversending_mode.allow_oversending"), 176, 196));

    public static final Component REQUIRES_ADVANCED_BEHAVIOR = Component.translatable(
            "gui.moderndynamics.tooltip.requires_advanced_behavior").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED));

    public CycleSettingButton(List<CycleSetting<T>> settings, T initialSetting, BiConsumer<T, Boolean> onChange) {
        super(0, 0, 20, 20, Component.empty(), button -> {
        }, DEFAULT_NARRATION);
        this.settings = settings;
        setValue(initialSetting);
        this.onChange = onChange;
    }

    /**
     * Enable the tooltip saying that the setting requires advanced behavior.
     */
    public CycleSettingButton<T> requiresAdvancedBehavior() {
        advancedBehavior = true;
        return this;
    }

    public void setValue(T newValue) {
        for (int i = 0; i < settings.size(); i++) {
            if (settings.get(i).value() == newValue) {
                this.currentSetting = i;
                break;
            }
        }
    }

    @Override
    public void onPress() {
        if (!isActive()) {
            return;
        }
        currentSetting = (currentSetting + 1) % settings.size();
        onChange.accept(getCurrentSetting().value(), true);
    }

    private CycleSetting<T> getCurrentSetting() {
        return settings.get(isActive() ? currentSetting : DISABLED_SETTING);
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
        this.blit(poseStack, this.getX(), this.getY(), setting.spriteX(), y, width, height);

        if (this.isHovered) {
            var tooltip = new ArrayList<Component>();
            tooltip.add(setting.tooltip());
            if (advancedBehavior && !isActive()) {
                tooltip.add(REQUIRES_ADVANCED_BEHAVIOR);
            }
            Minecraft.getInstance().screen.renderTooltip(poseStack, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }
}
