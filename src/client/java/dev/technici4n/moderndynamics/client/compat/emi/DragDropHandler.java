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

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.technici4n.moderndynamics.client.screen.AttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.gui.menu.ItemConfigSlot;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class DragDropHandler implements EmiDragDropHandler<Screen> {
    private static Rect2i getSlotBounds(Slot slot, AttachedIoScreen<?> screen) {
        return new Rect2i(slot.x + screen.getLeftPos(), slot.y + screen.getTopPos(), 16, 16);
    }

    @Override
    public boolean dropStack(Screen gui, EmiIngredient dragged, int mouseX, int mouseY) {
        var ing = dragged.getEmiStacks().get(0);

        if (gui instanceof ItemAttachedIoScreen ioScreen && ing.getKey() instanceof Item i) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof ItemConfigSlot slot && slot.isEnabled()) {
                    var iv = ItemVariant.of(i, ing.getNbt());
                    ioScreen.getMenu().setFilter(slot.getConfigIdx(), iv, true);
                    return true;
                }
            }
        }

        if (gui instanceof FluidAttachedIoScreen ioScreen && ing.getKey() instanceof Fluid f) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof FluidConfigSlot slot && slot.isEnabled() && getSlotBounds(s, ioScreen).contains(mouseX, mouseY)) {
                    var fv = FluidVariant.of(f, ing.getNbt());
                    ioScreen.getMenu().setFilter(slot.getConfigIdx(), fv, true);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void render(Screen gui, EmiIngredient dragged, PoseStack matrices, int mouseX, int mouseY, float delta) {
        var ing = dragged.getEmiStacks().get(0);
        List<Slot> targets = new ArrayList<>();

        if (gui instanceof ItemAttachedIoScreen ioScreen && ing.getKey() instanceof Item) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof ItemConfigSlot slot && slot.isEnabled()) {
                    targets.add(s);
                }
            }
        }

        if (gui instanceof FluidAttachedIoScreen ioScreen && ing.getKey() instanceof Fluid) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof FluidConfigSlot slot && slot.isEnabled()) {
                    targets.add(s);
                }
            }
        }

        for (Slot s : targets) {
            var b = getSlotBounds(s, (AttachedIoScreen<?>) gui);
            GuiComponent.fill(matrices, b.getX(), b.getY(), b.getX() + b.getWidth(), b.getY() + b.getHeight(), 0x8822BB33);
        }
    }
}
