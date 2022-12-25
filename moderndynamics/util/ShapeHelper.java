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
package dev.technici4n.moderndynamics.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapeHelper {
    /**
     * Return true if the passed shape contains the position, or is on its border.
     */
    public static boolean shapeContains(VoxelShape shape, Vec3 posInBlock) {
        for (AABB box : shape.toAabbs()) {
            // Move slightly toward the center of the box
            Vec3 centerDirection = box.getCenter().subtract(posInBlock).normalize().scale(1e-4);

            if (box.contains(posInBlock.add(centerDirection))) {
                return true;
            }
        }

        return false;
    }
}
