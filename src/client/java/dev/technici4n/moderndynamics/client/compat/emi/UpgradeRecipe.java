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
package dev.technici4n.moderndynamics.client.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.technici4n.moderndynamics.attachment.upgrade.UpgradeType;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

record UpgradeRecipe(Item item, UpgradeType upgradeInfo) implements EmiRecipe {

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(MdId.of("upgrades"), EmiStack.of(MdItems.EXTRACTOR)) {
        @Override
        public Component getName() {
            return Component.translatable("gui.moderndynamics.rei.upgrade_category");
        }
    };

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    @Nullable
    public ResourceLocation getId() {
        var itemId = BuiltInRegistries.ITEM.getKey(item);
        return MdId.of("upgrades/" + itemId.getNamespace() + "/" + itemId.getPath());
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.of(item));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 134;
    }

    @Override
    public int getDisplayHeight() {
        return 59;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var type = upgradeInfo;

        var texture = MdId.of("textures/gui/icons.png");
        int baseY = 5 + 32;
        int countXOffset = 17;
        int countY = 18 + 32;
        int effectWidth = 23;
        int effectSpacing = 5;

        widgets.addSlot(EmiStack.of(item), 2, 2);

        widgets.addText(Component.translatable("gui.moderndynamics.tooltip.upgrades_max", type.getSlotLimit()).getVisualOrderText(), 25, 7,
                0xFF404040, false);
        widgets.addText(
                Component.translatable("gui.moderndynamics.tooltip.upgrades_effects").setStyle(Style.EMPTY.withUnderlined(true)).getVisualOrderText(),
                getDisplayWidth() / 2, 5 + 18, 0xFF404040, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);

        var effects = new ArrayList<UpgradeEffect>();
        effects.add(new UpgradeEffect(0, type.isEnableAdvancedBehavior() ? -1 : 0, "enableAdvancedBehavior",
                I18n.get("gui.moderndynamics.tooltip.advanced_behavior_available")));
        effects.add(new UpgradeEffect(16, type.getAddFilterSlots(), "addFilterSlots", "+" + type.getAddFilterSlots()));
        effects.add(new UpgradeEffect(32, type.getAddItemCount(), "addItemCount", "+" + type.getAddItemCount()));
        effects.add(new UpgradeEffect(48, type.getAddItemSpeed(), "addItemSpeed", "+" + type.getAddItemSpeed() * 100 + "%"));
        effects.add(new UpgradeEffect(64, type.getAddItemTransferFrequency(), "addItemTransferFrequency",
                "+" + type.getAddItemTransferFrequency() * 100 + "%"));
        effects.add(new UpgradeEffect(80, type.getAddFluidTransfer(), "addFluidTransfer", "+" + type.getAddFluidTransfer() * 100 + "%"));
        effects.add(
                new UpgradeEffect(96, type.getMultiplyFluidTransfer(), "multiplyFluidTransfer", "+" + type.getMultiplyFluidTransfer() * 100 + "%"));

        effects.removeIf(e -> e.count() == 0);

        if (effects.size() > 0) {
            int totalWidth = effects.size() * effectWidth + (effects.size() - 1) * effectSpacing;
            int baseX = (getDisplayWidth() - totalWidth) / 2;

            for (UpgradeEffect e : effects) {
                var greenStyle = Style.EMPTY.applyFormat(ChatFormatting.GREEN);
                var tooltip = Component.translatable("gui.moderndynamics.tooltip.upgrade_" + e.upgradeName(),
                        Component.literal(e.greenText).setStyle(greenStyle));

                widgets.addTexture(texture, baseX, baseY, 16, 16, e.textureU(), 0);
                // hack to treat -1 as ""
                var renderedString = e.count() > 0 ? "" + e.count() : "";
                widgets.addText(Component.literal(renderedString).getVisualOrderText(), baseX + countXOffset, countY, 0xFF404040, false)
                        .horizontalAlign(TextWidget.Alignment.CENTER);
                widgets.addDrawable(baseX, baseY, 20, 20, (matrices, mouseX, mouseY, delta) -> {
                }).tooltip((mouseX, mouseY) -> {
                    return List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()));
                });

                baseX += effectWidth + effectSpacing;
            }
        }
    }

    private record UpgradeEffect(int textureU, int count, String upgradeName, String greenText) {
    }
}
