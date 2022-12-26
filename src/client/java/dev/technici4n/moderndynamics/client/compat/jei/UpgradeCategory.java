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
package dev.technici4n.moderndynamics.client.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class UpgradeCategory implements IRecipeCategory<UpgradeDisplay> {

    public static final RecipeType<UpgradeDisplay> TYPE = RecipeType.create(MdId.MOD_ID, "upgrades", UpgradeDisplay.class);
    public static final int EFFECT_WIDTH = 23;
    public static final int EFFECT_SPACING = 5;
    public static final int EFFECT_BASE_Y = 5 + 32;
    public static final ResourceLocation ICON_TEXTURE = MdId.of("textures/gui/icons.png");

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slotDrawable;
    private final IDrawable[] icons;

    public UpgradeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(142, 59);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MdItems.EXTRACTOR));
        this.slotDrawable = guiHelper.getSlotDrawable();

        this.icons = new IDrawable[] {
                guiHelper.createDrawable(ICON_TEXTURE, 0, 0, 16, 16),
                guiHelper.createDrawable(ICON_TEXTURE, 16, 0, 16, 16),
                guiHelper.createDrawable(ICON_TEXTURE, 32, 0, 16, 16),
                guiHelper.createDrawable(ICON_TEXTURE, 48, 0, 16, 16),
                guiHelper.createDrawable(ICON_TEXTURE, 64, 0, 16, 16),
                guiHelper.createDrawable(ICON_TEXTURE, 80, 0, 16, 16),
                guiHelper.createDrawable(ICON_TEXTURE, 96, 0, 16, 16),
        };
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.moderndynamics.rei.upgrade_category");
    }

    @Override
    public RecipeType<UpgradeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UpgradeDisplay recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 3)
                .addItemStack(new ItemStack(recipe.item()));
    }

    private EffectsInfo computeEffects(UpgradeDisplay recipe) {
        var type = recipe.upgradeInfo();

        var effects = new ArrayList<UpgradeEffect>();
        effects.add(new UpgradeEffect(0, type.isEnableAdvancedBehavior() ? -1 : 0, "enableAdvancedBehavior",
                I18n.get("gui.moderndynamics.tooltip.advanced_behavior_available")));
        effects.add(new UpgradeEffect(1, type.getAddFilterSlots(), "addFilterSlots", "+" + type.getAddFilterSlots()));
        effects.add(new UpgradeEffect(2, type.getAddItemCount(), "addItemCount", "+" + type.getAddItemCount()));
        effects.add(new UpgradeEffect(3, type.getAddItemSpeed(), "addItemSpeed", "+" + type.getAddItemSpeed() * 100 + "%"));
        effects.add(new UpgradeEffect(4, type.getAddItemTransferFrequency(), "addItemTransferFrequency",
                "+" + type.getAddItemTransferFrequency() * 100 + "%"));
        effects.add(new UpgradeEffect(5, type.getAddFluidTransfer(), "addFluidTransfer", "+" + type.getAddFluidTransfer() * 100 + "%"));
        effects.add(
                new UpgradeEffect(6, type.getMultiplyFluidTransfer(), "multiplyFluidTransfer", "+" + type.getMultiplyFluidTransfer() * 100 + "%"));

        effects.removeIf(e -> e.count() == 0);

        int totalWidth = effects.size() * EFFECT_WIDTH + (effects.size() - 1) * EFFECT_SPACING;
        int effectsBaseX = (background.getWidth() - totalWidth) / 2;

        return new EffectsInfo(effects, effectsBaseX);
    }

    @Override
    public void draw(UpgradeDisplay recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        slotDrawable.draw(stack, 2, 2);

        var type = recipe.upgradeInfo();
        int countXOffset = 17;
        int countY = 18 + 32;

        var minecraft = Minecraft.getInstance();
        var fontRenderer = minecraft.font;

        var maxUpgrades = Component.translatable("gui.moderndynamics.tooltip.upgrades_max", type.getSlotLimit());
        fontRenderer.draw(stack, maxUpgrades, 25, 7, 0xFF404040);

        var effectsText = Component.translatable("gui.moderndynamics.tooltip.upgrades_effects")
                .withStyle(ChatFormatting.UNDERLINE);
        var effectsTextX = (background.getWidth() - fontRenderer.width(effectsText)) / 2;
        fontRenderer.draw(stack, effectsText, effectsTextX, 5 + 18, 0xFF404040);

        var effects = computeEffects(recipe);

        int baseX = effects.effectsBaseX();

        for (var e : effects.effects()) {
            icons[e.iconIndex].draw(stack, baseX, EFFECT_BASE_Y);
            if (e.count >= 0) {
                fontRenderer.draw(stack, String.valueOf(e.count), baseX + countXOffset, countY, 0xFF404040);
            }

            baseX += EFFECT_WIDTH + EFFECT_SPACING;
        }
    }

    @Override
    public List<Component> getTooltipStrings(UpgradeDisplay recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var effects = computeEffects(recipe);

        var x = effects.effectsBaseX();
        for (var e : effects.effects()) {
            var tooltipRect = new Rect2i(x, EFFECT_BASE_Y, 20, 20);

            if (tooltipRect.contains((int) mouseX, (int) mouseY)) {
                var greenStyle = Style.EMPTY.applyFormat(ChatFormatting.GREEN);
                var tooltip = Component.translatable("gui.moderndynamics.tooltip.upgrade_" + e.upgradeName(),
                        Component.literal(e.greenText).setStyle(greenStyle));
                return List.of(tooltip);
            }

            x += EFFECT_WIDTH + EFFECT_SPACING;
        }

        return List.of();
    }

    private record UpgradeEffect(int iconIndex, int count, String upgradeName, String greenText) {
    }

    private record EffectsInfo(List<UpgradeEffect> effects, int effectsBaseX) {
    }
}
