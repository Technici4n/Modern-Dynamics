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
package dev.technici4n.moderndynamics.pipe;

import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class PipeBoundingBoxes {
    private static final double CORE_SIZE = 6.0 / 16;
    private static final double CORE_START = (1 - CORE_SIZE) / 2;
    private static final double CORE_END = CORE_START + CORE_SIZE;

    public static final VoxelShape CORE_SHAPE = VoxelShapes.cuboid(CORE_START, CORE_START, CORE_START, CORE_END, CORE_END, CORE_END);
    public static final VoxelShape[] PIPE_CONNECTIONS = buildSideShapes(CORE_SIZE, CORE_START);
    public static final VoxelShape[] CONNECTOR_SHAPES = buildSideShapes(8.0 / 16, 4.0 / 16);
    public static final VoxelShape[] INVENTORY_CONNECTIONS = combinePiecewise(PIPE_CONNECTIONS, CONNECTOR_SHAPES);

    public static VoxelShape[] buildSideShapes(double connectorSide, double connectorDepth) {
        double connectorSideStart = (1 - connectorSide) / 2;
        double connectorSideEnd = connectorSideStart + connectorSide;
        return new VoxelShape[] {
                VoxelShapes.cuboid(connectorSideStart, 0, connectorSideStart, connectorSideEnd, connectorDepth, connectorSideEnd),
                VoxelShapes.cuboid(connectorSideStart, 1 - connectorDepth, connectorSideStart, connectorSideEnd, 1, connectorSideEnd),
                VoxelShapes.cuboid(connectorSideStart, connectorSideStart, 0, connectorSideEnd, connectorSideEnd, connectorDepth),
                VoxelShapes.cuboid(connectorSideStart, connectorSideStart, 1 - connectorDepth, connectorSideEnd, connectorSideEnd, 1),
                VoxelShapes.cuboid(0, connectorSideStart, connectorSideStart, connectorDepth, connectorSideEnd, connectorSideEnd),
                VoxelShapes.cuboid(1 - connectorDepth, connectorSideStart, connectorSideStart, 1, connectorSideEnd, connectorSideEnd),
        };
    }

    public static VoxelShape[] buildCombinedShapes(VoxelShape[] sideShapes) {
        VoxelShape[] combinedShapes = new VoxelShape[1 << 6];

        for (int mask = 0; mask < (1 << 6); ++mask) {
            VoxelShape currentShape = CORE_SHAPE;

            for (int i = 0; i < 6; ++i) {
                if ((mask & (1 << i)) > 0) {
                    currentShape = VoxelShapes.union(currentShape, sideShapes[i]);
                }
            }

            combinedShapes[mask] = currentShape.simplify();
        }

        return combinedShapes;
    }

    public static VoxelShape[] combinePiecewise(VoxelShape[] part1, VoxelShape[] part2) {
        VoxelShape[] combinedShapes = new VoxelShape[6];

        for (int i = 0; i < 6; ++i) {
            combinedShapes[i] = VoxelShapes.union(part1[i], part2[i]).simplify();
        }

        return combinedShapes;
    }
}
