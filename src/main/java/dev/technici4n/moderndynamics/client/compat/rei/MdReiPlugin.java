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

import dev.architectury.event.CompoundEventResult;
import dev.architectury.fluid.FluidStack;
import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import dev.technici4n.moderndynamics.client.screen.AttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.gui.menu.ItemConfigSlot;
import dev.technici4n.moderndynamics.init.MdItems;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import dev.technici4n.moderndynamics.util.FluidVariant;
import dev.technici4n.moderndynamics.util.ItemVariant;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MdReiPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UpgradeCategory());

        for (var workstation : List.of(MdItems.ATTRACTOR, MdItems.EXTRACTOR, MdItems.FILTER)) {
            registry.addWorkstations(UpgradeCategory.ID, EntryStacks.of(workstation));
        }

        registry.removePlusButton(UpgradeCategory.ID);
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AttachedIoScreen.class, s -> {
            var screen = (AttachedIoScreen<?>) s;
            List<Rectangle> rectangles = new ArrayList<>();
            screen.appendExclusionZones(r -> rectangles.add(new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
            return rectangles;
        });
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (var upgradeItem : LoadedUpgrades.get().list) {
            registry.add(new UpgradeDisplay(upgradeItem, LoadedUpgrades.getType(upgradeItem)));
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        // Ensures that users can press R, U, etc... on item config slots.
        registry.registerFocusedStack((screen, mouse) -> {
            if (screen instanceof AttachedIoScreen<?>ioScreen) {
                if (ioScreen.getHoveredSlot() instanceof FluidConfigSlot fluidConfig) {
                    var variant = fluidConfig.getFilter();
                    if (!variant.isBlank()) {
                        // TODO: use FluidStackHooks when REI uses a more up to date version of Architectury
                        return CompoundEventResult.interruptTrue(EntryStacks.of(FluidStack.create(variant.getFluid(), 1, variant.getNbt())));
                    }
                }
            }
            return CompoundEventResult.pass();
        });
        // Allows slots to be locked by dragging from REI
        registry.registerDraggableStackVisitor(new DraggableStackVisitor<>() {
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                return screen instanceof AttachedIoScreen<?>;
            }

            @Override
            public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
                FluidVariant fv = stack.getStack().getValue() instanceof FluidStack fs ? FluidVariant.of(fs.getFluid(), fs.getTag()) : null;
                ItemVariant iv = stack.getStack().getValue() instanceof ItemStack is ? ItemVariant.of(is) : null;

                if (context.getScreen() instanceof ItemAttachedIoScreen ioScreen) {
                    if (ioScreen.getHoveredSlot() instanceof ItemConfigSlot slot) {
                        if (slot.isActive() && iv != null) {
                            ioScreen.getMenu().setFilter(slot.getConfigIdx(), iv, true);
                            return DraggedAcceptorResult.ACCEPTED;
                        }
                    }
                }

                if (context.getScreen() instanceof FluidAttachedIoScreen ioScreen) {
                    if (ioScreen.getHoveredSlot() instanceof FluidConfigSlot slot) {
                        if (slot.isActive() && fv != null) {
                            ioScreen.getMenu().setFilter(slot.getConfigIdx(), fv, true);
                            return DraggedAcceptorResult.ACCEPTED;
                        }
                    }
                }

                return DraggedAcceptorResult.PASS;
            }

            @Override
            public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<Screen> context, DraggableStack stack) {
                FluidVariant fv = stack.getStack().getValue() instanceof FluidStack fs ? FluidVariant.of(fs.getFluid(), fs.getTag()) : null;
                ItemVariant iv = stack.getStack().getValue() instanceof ItemStack is ? ItemVariant.of(is) : null;

                if (context.getScreen() instanceof ItemAttachedIoScreen ioScreen && iv != null) {
                    return ioScreen.getMenu().slots.stream()
                            .filter(s -> s instanceof ItemConfigSlot slot && slot.isActive())
                            .map(s -> getSlotBounds(s, ioScreen));
                }

                if (context.getScreen() instanceof FluidAttachedIoScreen ioScreen && fv != null) {
                    return ioScreen.getMenu().slots.stream()
                            .filter(s -> s instanceof FluidConfigSlot slot && slot.isActive())
                            .map(s -> getSlotBounds(s, ioScreen));
                }

                return Stream.empty();
            }
        });
    }

    private static DraggableStackVisitor.BoundsProvider getSlotBounds(Slot slot, AttachedIoScreen<?> screen) {
        return DraggableStackVisitor.BoundsProvider.ofRectangle(new Rectangle(slot.x + screen.getLeftPos(), slot.y + screen.getTopPos(), 16, 16));
    }
}
