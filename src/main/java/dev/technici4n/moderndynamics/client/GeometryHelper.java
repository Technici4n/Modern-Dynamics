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
package dev.technici4n.moderndynamics.client;

import static net.minecraft.core.Direction.*;

import net.minecraft.core.Direction;

public final class GeometryHelper {
    private GeometryHelper() {
    }

    /**
     * Vectors to the right of the face, i.e. the X axis in the XY plane of the
     * face.
     */
    public static Direction[] FACE_RIGHT = new Direction[] { EAST, EAST, WEST, EAST, SOUTH, NORTH, };
    /**
     * Vectors to the up of the face, i.e. the Y axis in the XY plane of the face.
     */
    public static Direction[] FACE_UP = new Direction[] { SOUTH, NORTH, UP, UP, UP, UP, };
}
