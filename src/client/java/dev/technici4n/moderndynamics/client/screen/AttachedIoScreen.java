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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.technici4n.moderndynamics.attachment.Setting;
import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import dev.technici4n.moderndynamics.gui.menu.AttachedIoMenu;
import dev.technici4n.moderndynamics.gui.menu.ConfigSlot;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.gui.menu.UpgradePanel;
import dev.technici4n.moderndynamics.gui.menu.UpgradeSlot;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AttachedIoScreen<T extends AttachedIoMenu<?>> extends AbstractContainerScreen<T> {
    /**
     * Horizontal gap between setting buttons.
     */
    protected static final int BUTTON_GAP = 6;

    /**
     * Tab Border
     */
    private static final int TAB_BORDER = 4;

    private static final float TAB_OPEN_PER_TICK = 0.20f;

    public static final ResourceLocation TEXTURE = MdId.of("textures/gui/attachment.png");
    public static final ResourceLocation TAB_RIGHT_TEXTURE = MdId.of("textures/gui/tab_right.png");

    private boolean redstoneTabOpen;
    private float redstoneTabCurrentOpen;
    private Rect2i redstoneTabRect = new Rect2i(0, 0, 0, 0);

    private final RedstoneModeButton redstoneModeIgnored;
    private final RedstoneModeButton redstoneModeLow;
    private final RedstoneModeButton redstoneModeHigh;
    private final List<RedstoneModeButton> redstoneButtons;

    public AttachedIoScreen(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);

        this.redstoneModeIgnored = new RedstoneModeButton(RedstoneMode.IGNORED, menu::getRedstoneMode, menu::setRedstoneMode);
        this.redstoneModeLow = new RedstoneModeButton(RedstoneMode.REQUIRES_LOW, menu::getRedstoneMode, menu::setRedstoneMode);
        this.redstoneModeHigh = new RedstoneModeButton(RedstoneMode.REQUIRES_HIGH, menu::getRedstoneMode, menu::setRedstoneMode);
        this.redstoneButtons = List.of(this.redstoneModeIgnored, this.redstoneModeLow, this.redstoneModeHigh);
    }

    @Override
    protected void init() {
        super.init();

        // Center the title
        titleLabelX = (imageWidth - font.width(title)) / 2;

        var toggleButtons = new ArrayList<CycleSettingButton<?>>();
        addToggleButtons(toggleButtons);

        // Lay out toggle buttons
        var overallWidth = toggleButtons.stream().mapToInt(AbstractWidget::getWidth).sum()
                + (toggleButtons.size() - 1) * BUTTON_GAP;
        var x = leftPos + (imageWidth - overallWidth) / 2;

        for (var toggleButton : toggleButtons) {
            toggleButton.setX(x);
            toggleButton.setY(topPos + 82);
            x += toggleButton.getWidth() + BUTTON_GAP;

            addRenderableWidget(toggleButton);
        }

        // Reposition redstone tab buttons
        updateRedstoneTabRect(0);
        redstoneModeIgnored.setX(redstoneTabRect.getX() + 28);
        redstoneModeIgnored.setY(redstoneTabRect.getY() + 22);
        redstoneModeLow.setX(redstoneModeIgnored.getX() + redstoneModeIgnored.getWidth() + 4);
        redstoneModeLow.setY(redstoneModeIgnored.getY());
        redstoneModeHigh.setX(redstoneModeLow.getX() + redstoneModeLow.getWidth() + 4);
        redstoneModeHigh.setY(redstoneModeLow.getY());
        addRenderableWidget(redstoneModeIgnored);
        addRenderableWidget(redstoneModeLow);
        addRenderableWidget(redstoneModeHigh);

        // After the buttons, add a handler for opening and closing the tab
        addRenderableWidget(new RedstoneTabOpenCloseHandler());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot instanceof UpgradeSlot) {
            renderTooltip(poseStack, List.of(
                    Component.translatable("gui.moderndynamics.tooltip.slot.upgrade"),
                    Component.translatable("gui.moderndynamics.tooltip.slot.upgrade_desc1").withStyle(ChatFormatting.GOLD),
                    Component.translatable("gui.moderndynamics.tooltip.slot.upgrade_desc2").withStyle(ChatFormatting.GOLD)),
                    Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(poseStack, mouseX, mouseY);
        }
    }

    @Override
    public void renderSlot(PoseStack poseStack, Slot slot) {
        // Skip disabled slots
        if (slot instanceof ConfigSlot<?>configSlot && !configSlot.isEnabled()) {
            return;
        }
        if (slot instanceof FluidConfigSlot fluidConfigSlot) {
            var variant = fluidConfigSlot.getFilter();
            if (!variant.isBlank()) {
                FluidAttachedIoScreen.drawFluidInGui(poseStack, fluidConfigSlot.getFilter(), slot.x, slot.y);
            }
        } else {
            super.renderSlot(poseStack, slot);
        }
    }

    protected void addToggleButtons(List<CycleSettingButton<?>> toggleButtons) {
        if (menu.isSettingSupported(Setting.FILTER_INVERSION)) {
            toggleButtons.add(new CycleSettingButton<>(CycleSettingButton.FILTER_INVERSION, menu.getFilterMode(), menu::setFilterMode));
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        renderRedstoneTabBg(poseStack, partialTick);

        // Background
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Upgrade panel background
        blit(
                poseStack,
                leftPos + UpgradePanel.START_LEFT, topPos + UpgradePanel.START_TOP,
                0, 0,
                UpgradePanel.WIDTH, UpgradePanel.HEIGHT - 5);
        // Render last 5 rows with a different offset to have a proper corner
        blit(
                poseStack,
                leftPos + UpgradePanel.START_LEFT, topPos + UpgradePanel.START_TOP + UpgradePanel.HEIGHT - 5,
                0, 199,
                UpgradePanel.WIDTH, 5);

        // Draw each slot's background
        for (Slot slot : getMenu().slots) {
            if (slot instanceof ConfigSlot || slot instanceof UpgradeSlot) {
                if (slot instanceof ConfigSlot<?>cfg && !cfg.isEnabled()) {
                    // Disabled slot
                    blit(poseStack, leftPos + slot.x - 1, topPos + slot.y - 1, 216, 162, 18, 18);
                } else {
                    blit(poseStack, leftPos + slot.x - 1, topPos + slot.y - 1, 7, 122, 18, 18);
                }
            }
        }
    }

    private void renderRedstoneTabBg(PoseStack matrices, float partialTicks) {

        updateRedstoneTabRect(partialTicks);

        RenderSystem.setShaderTexture(0, TAB_RIGHT_TEXTURE);

        RenderSystem.setShaderColor(0.81f, 0.14f, 0.04f, 1.0f);

        // The background image is treated as a border image,
        // for context see https://developer.mozilla.org/en-US/docs/Web/CSS/border-image
        // The border is assumed to be 4 PX

        var tabX = redstoneTabRect.getX();
        var tabY = redstoneTabRect.getY();
        var tabWidth = redstoneTabRect.getWidth();
        var tabHeight = redstoneTabRect.getHeight();

        var tabRight = tabX + tabWidth;
        var tabBottom = tabY + tabHeight;

        // Draw all four corners clock-wise starting from top-left
        blit(matrices, tabX, tabY, 0, 0, TAB_BORDER, TAB_BORDER);
        blit(matrices, tabRight - TAB_BORDER, tabY, 256 - TAB_BORDER, 0, TAB_BORDER, TAB_BORDER);
        blit(matrices, tabRight - TAB_BORDER, tabBottom - TAB_BORDER, 256 - TAB_BORDER, 256 - TAB_BORDER, TAB_BORDER, TAB_BORDER);
        blit(matrices, tabX, tabBottom - TAB_BORDER, 0, 256 - TAB_BORDER, TAB_BORDER, TAB_BORDER);

        // Draw the borders between the corners in the same order
        blit(matrices, tabX + TAB_BORDER, tabY, tabWidth - 2 * TAB_BORDER, TAB_BORDER, TAB_BORDER, 0, 256 - 2 * TAB_BORDER, TAB_BORDER, 256, 256);
        blit(matrices, tabRight - TAB_BORDER, tabY + TAB_BORDER, TAB_BORDER, tabHeight - 2 * TAB_BORDER, 256 - TAB_BORDER, TAB_BORDER, TAB_BORDER,
                256 - 2 * TAB_BORDER, 256, 256);
        blit(matrices, tabX + TAB_BORDER, tabBottom - TAB_BORDER, tabWidth - 2 * TAB_BORDER, TAB_BORDER, TAB_BORDER, 256 - TAB_BORDER,
                256 - 2 * TAB_BORDER, TAB_BORDER, 256, 256);
        blit(matrices, tabX, tabY + TAB_BORDER, TAB_BORDER, tabHeight - 2 * TAB_BORDER, 0, TAB_BORDER, TAB_BORDER, 256 - 2 * TAB_BORDER, 256, 256);

        // Center
        blit(matrices, tabX + TAB_BORDER, tabY + TAB_BORDER, TAB_BORDER, TAB_BORDER, tabWidth - 2 * TAB_BORDER, tabHeight - 2 * TAB_BORDER);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        var scissorRect = getRedstoneTabScissorRect();
        RenderSystem.enableScissor(
                scissorRect.getX(), scissorRect.getY(), scissorRect.getWidth(), scissorRect.getHeight());
        // Tell the buttons the scissor rect since they need to be cut off too
        redstoneModeIgnored.setScissorRect(scissorRect);
        redstoneModeLow.setScissorRect(scissorRect);
        redstoneModeHigh.setScissorRect(scissorRect);

        // Draw a small rectangle background for the buttons
        fill(
                matrices,
                redstoneModeIgnored.getX() - 4,
                redstoneModeIgnored.getY() - 4,
                redstoneModeHigh.getX() + redstoneModeHigh.getWidth() + 4,
                redstoneModeHigh.getY() + redstoneModeHigh.getHeight() + 4,
                0xff611005);

        // Redstone Icon
        minecraft.getItemRenderer().renderGuiItem(
                new ItemStack(Items.REDSTONE),
                tabX + 2,
                tabY + 3);

        var header = Component.translatable("gui.moderndynamics.setting.redstone_control.header");
        font.drawShadow(matrices, header, tabX + TAB_BORDER + 16, tabY + TAB_BORDER + 4, 0xe1c92f);

        /* Draw a sub-header that indicates whether the attachment is currently operating based on the status or not */
        var subHeaderStatus = Component.translatable("gui.moderndynamics.setting.redstone_control.status_header");
        font.drawShadow(matrices, subHeaderStatus, tabX + TAB_BORDER + 4, tabY + TAB_BORDER + 42, 0xaaafb8);
        Component enabledStatusText;
        if (menu.isEnabledViaRedstone()) {
            enabledStatusText = Component.translatable("gui.moderndynamics.setting.redstone_control.enabled");
        } else {
            enabledStatusText = Component.translatable("gui.moderndynamics.setting.redstone_control.disabled");
        }
        font.draw(matrices, enabledStatusText, tabX + TAB_BORDER + 12, tabY + TAB_BORDER + 54, 0);

        /* Draw a sub-header that simply spells out the currently chosen redstone mode again */
        var subHeaderSetting = Component.translatable("gui.moderndynamics.setting.redstone_control.signal_required_header");
        font.drawShadow(matrices, subHeaderSetting, tabX + TAB_BORDER + 4, tabY + TAB_BORDER + 66, 0xaaafb8);
        font.draw(matrices, menu.getRedstoneMode().getTranslation(), tabX + TAB_BORDER + 12, tabY + TAB_BORDER + 78, 0);

        RenderSystem.disableScissor();
    }

    public void appendExclusionZones(Consumer<Rect2i> consumer) {
        // Upgrades
        consumer.accept(new Rect2i(leftPos + UpgradePanel.START_LEFT, topPos + UpgradePanel.START_TOP, UpgradePanel.WIDTH, UpgradePanel.HEIGHT));
        // Redstone tab
        consumer.accept(redstoneTabRect);
    }

    private void updateRedstoneTabRect(float partialTicks) {
        var tabX = leftPos + imageWidth;
        var tabY = topPos + 4;
        var open = getCurrentRedstoneTabOpen(partialTicks);
        var tabWidth = Math.round(Mth.lerp(open, 16 + 6, 112));
        var tabHeight = Math.round(Mth.lerp(open, 16 + 6, 97));
        redstoneTabRect = new Rect2i(tabX, tabY, tabWidth, tabHeight);

        // Making these invisible will hide their tooltips too
        for (var button : redstoneButtons) {
            button.visible = open > 0;
        }
        if (open <= 0) {
            if (getFocused() instanceof RedstoneModeButton button) {
                button.changeFocus(false);
                setFocused(null);
            }
        }
    }

    private Rect2i getRedstoneTabScissorRect() {
        var tabRect = redstoneTabRect;

        // Scissor rect is in physical window coordinates not the rescaled UI space
        double scale = this.minecraft.getWindow().getGuiScale();
        return new Rect2i(
                (int) ((tabRect.getX() + TAB_BORDER) * scale),
                // glScissor has y=0 at the bottom of the screen
                (int) ((height - (tabRect.getY() + tabRect.getHeight() - TAB_BORDER)) * scale),
                (int) ((tabRect.getWidth() - 2 * TAB_BORDER) * scale),
                (int) ((tabRect.getHeight() - 2 * TAB_BORDER) * scale));
    }

    private float getCurrentRedstoneTabOpen(float partialTicks) {
        var result = this.redstoneTabCurrentOpen;
        // partialTicks is the time (measured in partial ticks) that has
        // elapsed since the last 20fps client simulation tick
        if (redstoneTabOpen) {
            result += partialTicks * TAB_OPEN_PER_TICK;
        } else {
            result -= partialTicks * TAB_OPEN_PER_TICK;
        }
        return Mth.clamp(result, 0f, 1f);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        // This function is called at 20fps in the client-tick.
        // If we want to open the tab in 250ms, that means we have 5 ticks, so 1/5 each tick.
        if (redstoneTabOpen) {
            this.redstoneTabCurrentOpen += TAB_OPEN_PER_TICK;
            if (this.redstoneTabCurrentOpen > 1) {
                this.redstoneTabCurrentOpen = 1;
            }
        } else {
            this.redstoneTabCurrentOpen -= TAB_OPEN_PER_TICK;
            if (this.redstoneTabCurrentOpen < 0) {
                this.redstoneTabCurrentOpen = 0;
            }
        }
        updateRedstoneTabRect(0);
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        if (!super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton)) {
            return false;
        }

        return !UpgradePanel.isInside(mouseX - leftPos, mouseY - topPos) && !isInRedstoneTabRect(mouseX, mouseY);
    }

    private boolean isInRedstoneTabRect(double mouseX, double mouseY) {
        return mouseX >= redstoneTabRect.getX()
                && mouseY >= redstoneTabRect.getY()
                && mouseX < redstoneTabRect.getX() + redstoneTabRect.getWidth()
                && mouseY < redstoneTabRect.getY() + redstoneTabRect.getHeight();
    }

    private class RedstoneTabOpenCloseHandler implements GuiEventListener, NarratableEntry, Renderable {
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isInRedstoneTabRect(mouseX, mouseY) && button == 0) {
                redstoneTabOpen = !redstoneTabOpen;
                return true;
            }
            return false;
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (redstoneTabCurrentOpen <= 0 && isInRedstoneTabRect(mouseX, mouseY)) {
                var lines = List.<Component>of(
                        Component.translatable("gui.moderndynamics.setting.redstone_control.header")
                                .withStyle(ChatFormatting.WHITE),
                        menu.getRedstoneMode().getTranslation()
                                .copy()
                                .withStyle(ChatFormatting.YELLOW));
                renderComponentTooltip(poseStack, lines, mouseX, mouseY);
            }
        }
    }

    // Accessors for use by the REI plugin
    public Slot getHoveredSlot() {
        return hoveredSlot;
    }

    public int getLeftPos() {
        return leftPos;
    }

    public int getTopPos() {
        return topPos;
    }
}
