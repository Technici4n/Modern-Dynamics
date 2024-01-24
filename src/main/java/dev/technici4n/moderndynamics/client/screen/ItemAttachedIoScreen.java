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

import dev.technici4n.moderndynamics.attachment.Setting;
import dev.technici4n.moderndynamics.attachment.settings.FilterNbtMode;
import dev.technici4n.moderndynamics.attachment.settings.OversendingMode;
import dev.technici4n.moderndynamics.attachment.settings.RoutingMode;
import dev.technici4n.moderndynamics.gui.menu.ItemAttachedIoMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class ItemAttachedIoScreen extends AttachedIoScreen<ItemAttachedIoMenu> {
    @Nullable
    private Rect2i maxItemsInInventoryTooltipRect;
    @Nullable
    private Rect2i maxItemsExtractedTooltipRect;
    private PlusMinusButton decMaxItemsInInventory;
    private PlusMinusButton incMaxItemsInInventory;
    private PlusMinusButton decMaxItemsExtracted;
    private PlusMinusButton incMaxItemsExtracted;
    private CycleSettingButton<FilterNbtMode> filterNbtModeButton;
    private CycleSettingButton<RoutingMode> routingModeButton;
    private CycleSettingButton<OversendingMode> oversendingModeButton;

    public ItemAttachedIoScreen(ItemAttachedIoMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 204;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    protected void init() {
        super.init();

        // Add the segment for setting the inventory target size
        if (menu.isSettingSupported(Setting.MAX_ITEMS_IN_INVENTORY)) {
            decMaxItemsInInventory = new PlusMinusButton(leftPos + 137, topPos + 28, true, () -> adjustMaxItemsInInventory(-1));
            addRenderableWidget(decMaxItemsInInventory);
            incMaxItemsInInventory = new PlusMinusButton(leftPos + 153, topPos + 28, false, () -> adjustMaxItemsInInventory(1));
            addRenderableWidget(incMaxItemsInInventory);
        }

        // Add the segment for setting the max extraction size
        if (menu.isSettingSupported(Setting.MAX_ITEMS_EXTRACTED)) {
            decMaxItemsExtracted = new PlusMinusButton(leftPos + 137, topPos + 57, true, () -> adjustMaxItemsExtracted(-1));
            addRenderableWidget(decMaxItemsExtracted);
            incMaxItemsExtracted = new PlusMinusButton(leftPos + 153, topPos + 57, false, () -> adjustMaxItemsExtracted(1));
            addRenderableWidget(incMaxItemsExtracted);
        }
    }

    @Override
    protected void addToggleButtons(List<CycleSettingButton<?>> toggleButtons) {
        super.addToggleButtons(toggleButtons);

        if (menu.isSettingSupported(Setting.FILTER_NBT)) {
            toggleButtons.add(filterNbtModeButton = new CycleSettingButton<>(
                    CycleSettingButton.FILTER_NBT, menu.getFilterNbt(), menu::setFilterNbt).requiresAdvancedBehavior());
        }
        if (menu.isSettingSupported(Setting.ROUTING_MODE)) {
            toggleButtons.add(routingModeButton = new CycleSettingButton<>(
                    CycleSettingButton.ROUTING_MODE, menu.getRoutingMode(), menu::setRoutingMode).requiresAdvancedBehavior());
        }
        if (menu.isSettingSupported(Setting.OVERSENDING_MODE)) {
            toggleButtons.add(oversendingModeButton = new CycleSettingButton<>(
                    CycleSettingButton.OVERSENDING_MODE, menu.getOversendingMode(), menu::setOversendingMode).requiresAdvancedBehavior());
        }

        /*
         * if (menu.isSettingSupported(Setting.FILTER_DAMAGE)) {
         * toggleButtons.add(new CycleSettingButton<>(CycleSettingButton.FILTER_DAMAGE, menu.getFilterDamage(), menu::setFilterDamage));
         * }
         * if (menu.isSettingSupported(Setting.FILTER_NBT)) {
         * toggleButtons.add(new CycleSettingButton<>(CycleSettingButton.FILTER_NBT, menu.getFilterNbt(), menu::setFilterNbt));
         * }
         * if (menu.isSettingSupported(Setting.FILTER_SIMILAR)) {
         * toggleButtons.add(new CycleSettingButton<>(CycleSettingButton.FILTER_SIMILAR, menu.getFilterSimilar(), menu::setFilterSimilar));
         * }
         * if (menu.isSettingSupported(Setting.FILTER_MOD)) {
         * toggleButtons.add(new CycleSettingButton<>(CycleSettingButton.FILTER_MOD, menu.getFilterMod(), menu::setFilterMod));
         * }
         * if (menu.isSettingSupported(Setting.ROUTING_MODE)) {
         * toggleButtons.add(new CycleSettingButton<>(CycleSettingButton.ROUTING_MODE, menu.getRoutingMode(), menu::setRoutingMode));
         * }
         * if (menu.isSettingSupported(Setting.OVERSENDING_MODE)) {
         * toggleButtons.add(new CycleSettingButton<>(CycleSettingButton.OVERSENDING_MODE, menu.getOversendingMode(), menu::setOversendingMode));
         * }
         */
    }

    private void adjustMaxItemsInInventory(int i) {
        if (hasShiftDown()) {
            i *= 16;
        }
        menu.setMaxItemsInInventory(menu.getMaxItemsInInventory() + i, true);
    }

    private void adjustMaxItemsExtracted(int i) {
        if (hasShiftDown()) {
            i *= 16;
        }
        menu.setMaxItemsExtracted(menu.getMaxItemsExtracted() + i, true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (decMaxItemsInInventory != null && incMaxItemsInInventory != null) {
            decMaxItemsInInventory.active = menu.getMaxItemsInInventory() > 0;
            incMaxItemsInInventory.active = menu.getMaxItemsInInventory() < Integer.MAX_VALUE;
        }
        if (decMaxItemsExtracted != null && incMaxItemsExtracted != null) {
            decMaxItemsExtracted.active = menu.getMaxItemsExtracted() > 1;
            incMaxItemsExtracted.active = menu.getMaxItemsExtracted() < menu.getMaxItemsExtractedMaximum();
        }
        if (filterNbtModeButton != null) {
            filterNbtModeButton.active = menu.isAdvancedBehaviorAllowed();
            filterNbtModeButton.setValue(menu.getFilterNbt());
        }
        if (routingModeButton != null) {
            routingModeButton.active = menu.isAdvancedBehaviorAllowed();
            routingModeButton.setValue(menu.getRoutingMode());
        }
        if (oversendingModeButton != null) {
            oversendingModeButton.active = menu.isAdvancedBehaviorAllowed();
            oversendingModeButton.setValue(menu.getOversendingMode());
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render tooltips (except buttons, those are handled in the buttons themselves)
        if (maxItemsInInventoryTooltipRect != null && maxItemsInInventoryTooltipRect.contains(Math.round(mouseX), Math.round(mouseY))) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.moderndynamics.setting.max_items_in_inventory.tooltip"), mouseX, mouseY);
        } else if (maxItemsExtractedTooltipRect != null && maxItemsExtractedTooltipRect.contains(Math.round(mouseX), Math.round(mouseY))) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.moderndynamics.setting.max_items_extracted.tooltip"), mouseX, mouseY);
        } else {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, delta, mouseX, mouseY);

        // Render current settings for max total items
        if (menu.isSettingSupported(Setting.MAX_ITEMS_IN_INVENTORY)) {
            renderMaxItemsInInventoryValue(guiGraphics);
        }

        // Render current settings for max extraction amount
        if (menu.isSettingSupported(Setting.MAX_ITEMS_EXTRACTED)) {
            renderMaxItemsExtractedValue(guiGraphics);
        }
    }

    private void renderMaxItemsInInventoryValue(GuiGraphics guiGraphics) {
        var text = getMaxItemsInInventoryText();
        var width = font.width(text);
        var rect = new Rect2i(
                leftPos + 152 - width / 2,
                topPos + 18,
                width,
                font.lineHeight);
        guiGraphics.drawString(font, text, rect.getX(), rect.getY(), 0x404040, false);
        maxItemsInInventoryTooltipRect = rect;
    }

    private void renderMaxItemsExtractedValue(GuiGraphics guiGraphics) {
        var text = String.valueOf(menu.getMaxItemsExtracted());
        var width = font.width(text);
        var rect = new Rect2i(
                leftPos + 152 - width / 2,
                topPos + 46,
                width,
                font.lineHeight);
        guiGraphics.drawString(font, text, rect.getX(), rect.getY(), 0x404040, false);
        maxItemsExtractedTooltipRect = rect;
    }

    private Component getMaxItemsInInventoryText() {
        if (menu.getMaxItemsInInventory() <= 0) {
            return Component.translatable("gui.moderndynamics.setting.max_items_in_inventory.infinite");
        } else {
            return Component.literal(String.valueOf(menu.getMaxItemsInInventory()));
        }
    }

}
