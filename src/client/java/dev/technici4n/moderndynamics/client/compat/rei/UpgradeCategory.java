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
package dev.technici4n.moderndynamics.client.compat.rei;

import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class UpgradeCategory implements DisplayCategory<UpgradeDisplay> {
    public static final CategoryIdentifier<UpgradeDisplay> ID = CategoryIdentifier.of(MdId.of("upgrades"));

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(MdItems.EXTRACTOR);
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("gui.moderndynamics.rei.upgrade_category");
    }

    @Override
    public CategoryIdentifier<UpgradeDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(UpgradeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();

        var type = display.upgradeInfo;
        var texture = MdId.of("textures/gui/icons.png");
        int baseY = bounds.y + 5 + 36;
        int countXOffset = 17;
        int countY = bounds.y + 18 + 36;
        int effectWidth = 23;
        int effectSpacing = 5;

        widgets.add(Widgets.createRecipeBase(bounds));

        var upgradeSlotPoint = new Point(bounds.x + 7, bounds.y + 7);
        widgets.add(Widgets.createSlot(upgradeSlotPoint).entry(EntryStacks.of(display.item)));

        widgets.add(Widgets
                .createLabel(new Point(bounds.x + 29, bounds.y + 11),
                        new TranslatableComponent("gui.moderndynamics.tooltip.upgrades_max", type.getSlotLimit()))
                .leftAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
        widgets.add(Widgets
                .createLabel(new Point(bounds.getCenterX(), bounds.y + 5 + 22),
                        new TranslatableComponent("gui.moderndynamics.tooltip.upgrades_effects").setStyle(Style.EMPTY.withUnderlined(true)))
                .noShadow().color(0xFF404040, 0xFFBBBBBB));

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
            int baseX = bounds.x + (bounds.getWidth() - totalWidth) / 2;

            for (UpgradeEffect e : effects) {
                var tooltipRect = new Rectangle(baseX, baseY, 20, 20);
                var greenStyle = Style.EMPTY.applyFormat(ChatFormatting.GREEN);
                var tooltip = new TranslatableComponent("gui.moderndynamics.tooltip.upgrade_" + e.upgradeName(),
                        new TextComponent(e.greenText).setStyle(greenStyle));

                widgets.add(Widgets.createTexturedWidget(texture, baseX, baseY, e.textureU(), 0, 16, 16));
                // hack to treat -1 as ""
                var renderedString = e.count() > 0 ? "" + e.count() : "";
                widgets.add(Widgets.createLabel(new Point(baseX + countXOffset, countY), new TextComponent(renderedString)).noShadow()
                        .color(0xFF404040, 0xFFBBBBBB));
                widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    if (tooltipRect.contains(mouseX, mouseY)) {
                        Tooltip.create(tooltip).queue();
                    }
                }));

                baseX += effectWidth + effectSpacing;
            }
        }

        return widgets;
    }

    private record UpgradeEffect(int textureU, int count, String upgradeName, String greenText) {
    }

    @Override
    public int getDisplayHeight() {
        return 67;
    }
}
