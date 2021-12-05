/*
 * Modern Transportation
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

import java.util.EnumSet;
import net.minecraft.util.math.Direction;

public class SerializationHelper {
    public static byte directionsToMask(EnumSet<Direction> directions) {
        byte result = 0;

        for (Direction direction : directions) {
            result |= 1 << direction.getId();
        }

        return result;
    }

    public static EnumSet<Direction> directionsFromMask(byte mask) {
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);

        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) > 0) {
                result.add(Direction.byId(i));
            }
        }

        return result;
    }
}
