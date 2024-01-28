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
package dev.technici4n.moderndynamics;

import net.neoforged.neoforge.fluids.FluidType;

// All the things that should be moved to a config "one day"
public class Constants {
    public static class Fluids {
        public static final int BASE_IO = FluidType.BUCKET_VOLUME / 50;
        public static final int CAPACITY = FluidType.BUCKET_VOLUME;
    }

    public static class Items {
        public static final double SPEED_IN_PIPES = 0.02;
    }

    public static class Upgrades {
        public static final int MAX_FILTER = 15;
    }
}
