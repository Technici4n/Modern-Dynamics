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

import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import dev.technici4n.moderndynamics.client.screen.AttachedIoScreen;
import dev.technici4n.moderndynamics.init.MdItems;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.common.util.EntryStacks;

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
}
