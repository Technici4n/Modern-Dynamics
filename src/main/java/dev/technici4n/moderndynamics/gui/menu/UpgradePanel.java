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

/**
 * Constants for the upgrade panel
 */
public class UpgradePanel {
    public static final int START_LEFT = -24;
    public static final int START_TOP = 4;
    public static final int HEIGHT = 82;
    public static final int WIDTH = 24;
    public static final int END_LEFT = START_LEFT + WIDTH;
    public static final int END_TOP = START_TOP + HEIGHT;

    public static final int FIRST_SLOT_LEFT = START_LEFT + 6;
    public static final int FIRST_SLOT_TOP = START_TOP + 6;

    public static boolean isInside(double x, double y) {
        return START_LEFT <= x && x <= END_LEFT && START_TOP <= y && y <= END_TOP;
    }
}
