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

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import dev.technici4n.moderndynamics.client.screen.AttachedIoScreen;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.init.MdItems;
import java.util.List;

public class MdEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(UpgradeRecipe.CATEGORY);

        for (var workstation : List.of(MdItems.ATTRACTOR, MdItems.EXTRACTOR, MdItems.FILTER)) {
            registry.addWorkstation(UpgradeRecipe.CATEGORY, EmiStack.of(workstation));
        }

        for (var u : LoadedUpgrades.get().list) {
            registry.addRecipe(new UpgradeRecipe(u, LoadedUpgrades.getType(u)));
        }

        registry.addGenericExclusionArea((screen, bounds) -> {
            if (screen instanceof AttachedIoScreen<?>ioScreen) {
                ioScreen.appendExclusionZones(r -> bounds.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
            }
        });

        registry.addGenericStackProvider((screen, mouseX, mouseY) -> {
            if (screen instanceof AttachedIoScreen<?>ioScreen) {
                // Ensures that users can press R, U, etc... on fluid config slots.
                if (ioScreen.getHoveredSlot() instanceof FluidConfigSlot fluidConfig) {
                    var variant = fluidConfig.getFilter();
                    if (!variant.isBlank()) {
                        return new EmiStackInteraction(EmiStack.of(variant), null, false);
                    }
                }
            }
            return EmiStackInteraction.EMPTY;
        });

        registry.addGenericDragDropHandler(new DragDropHandler());
    }
}
