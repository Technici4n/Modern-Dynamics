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

import dev.technici4n.moderndynamics.client.screen.AttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.gui.menu.ItemConfigSlot;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("rawtypes")
class GhostIngredientHandler implements IGhostIngredientHandler<AttachedIoScreen> {
    @SuppressWarnings("unchecked")
    @Override
    public <I> List<Target<I>> getTargetsTyped(AttachedIoScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        var targets = new ArrayList<Target<I>>();

        if (gui instanceof ItemAttachedIoScreen ioScreen && ingredient.getIngredient() instanceof ItemStack) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof ItemConfigSlot slot && slot.isActive()) {
                    targets.add((Target<I>) new ItemSlotTarget(slot, ioScreen));
                }
            }
        }

        if (gui instanceof FluidAttachedIoScreen ioScreen && ingredient instanceof IJeiFluidIngredient) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof FluidConfigSlot slot && slot.isActive()) {
                    targets.add((Target<I>) new FluidSlotTarget(slot, ioScreen));
                }
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {
    }

    private static Rect2i getSlotBounds(Slot slot, AttachedIoScreen<?> screen) {
        return new Rect2i(slot.x + screen.getLeftPos(), slot.y + screen.getTopPos(), 16, 16);
    }

    private static class ItemSlotTarget implements IGhostIngredientHandler.Target<ItemStack> {
        private final ItemConfigSlot slot;
        private final ItemAttachedIoScreen ioScreen;

        public ItemSlotTarget(ItemConfigSlot slot, ItemAttachedIoScreen ioScreen) {
            this.slot = slot;
            this.ioScreen = ioScreen;
        }

        @Override
        public Rect2i getArea() {
            return getSlotBounds(slot, ioScreen);
        }

        @Override
        public void accept(ItemStack ingredient) {
            if (slot.isActive()) {
                var iv = ItemVariant.of(ingredient);
                ioScreen.getMenu().setFilter(slot.getConfigIdx(), iv, true);
            }
        }
    }

    private static class FluidSlotTarget implements IGhostIngredientHandler.Target<IJeiFluidIngredient> {
        private final FluidConfigSlot slot;
        private final FluidAttachedIoScreen ioScreen;

        public FluidSlotTarget(FluidConfigSlot slot, FluidAttachedIoScreen ioScreen) {
            this.slot = slot;
            this.ioScreen = ioScreen;
        }

        @Override
        public Rect2i getArea() {
            return getSlotBounds(slot, ioScreen);
        }

        @Override
        public void accept(IJeiFluidIngredient ingredient) {
            if (slot.isActive()) {
                var fv = FluidVariant.of(ingredient.getFluid(), ingredient.getTag().orElse(null));
                ioScreen.getMenu().setFilter(slot.getConfigIdx(), fv, true);
            }
        }
    }
}
