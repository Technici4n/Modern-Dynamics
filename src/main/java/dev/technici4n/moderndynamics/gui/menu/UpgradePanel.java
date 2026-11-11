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
package dev.technici4n.moderndynamics.gui.menu;

import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * Constants for the upgrade panel
 */
public class UpgradePanel {
    private static final int START_LEFT = -24;
    private static final int START_TOP = 4;
    private static final int HEIGHT = 82;
    private static final int WIDTH = 24;

    public static ScreenRectangle getRect(int x, int y, boolean withUpgradesButton) {
        return new ScreenRectangle(
                x + START_LEFT,
                y + START_TOP,
                WIDTH,
                HEIGHT + (withUpgradesButton ? 16 : 0));
    }

    public static final int FIRST_SLOT_LEFT = START_LEFT + 6;
    public static final int FIRST_SLOT_TOP = START_TOP + 6;
}
