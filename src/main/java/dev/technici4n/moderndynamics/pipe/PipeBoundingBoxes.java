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
package dev.technici4n.moderndynamics.pipe;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PipeBoundingBoxes {
    public static final float CORE_SIZE = 6f / 16;
    public static final float CORE_START = (1 - CORE_SIZE) / 2;
    public static final float CORE_END = CORE_START + CORE_SIZE;

    public static final VoxelShape CORE_SHAPE = Shapes.box(CORE_START, CORE_START, CORE_START, CORE_END, CORE_END, CORE_END);
    public static final VoxelShape[] PIPE_CONNECTIONS = buildSideShapes(CORE_SIZE, CORE_START);
    public static final VoxelShape[] CONNECTOR_SHAPES = buildSideShapes(8.0 / 16, 4.0 / 16);
    public static final VoxelShape[] INVENTORY_CONNECTIONS = combinePiecewise(PIPE_CONNECTIONS, CONNECTOR_SHAPES);

    public static VoxelShape[] buildSideShapes(double connectorSide, double connectorDepth) {
        double connectorSideStart = (1 - connectorSide) / 2;
        double connectorSideEnd = connectorSideStart + connectorSide;
        return new VoxelShape[] {
                Shapes.box(connectorSideStart, 0, connectorSideStart, connectorSideEnd, connectorDepth, connectorSideEnd),
                Shapes.box(connectorSideStart, 1 - connectorDepth, connectorSideStart, connectorSideEnd, 1, connectorSideEnd),
                Shapes.box(connectorSideStart, connectorSideStart, 0, connectorSideEnd, connectorSideEnd, connectorDepth),
                Shapes.box(connectorSideStart, connectorSideStart, 1 - connectorDepth, connectorSideEnd, connectorSideEnd, 1),
                Shapes.box(0, connectorSideStart, connectorSideStart, connectorDepth, connectorSideEnd, connectorSideEnd),
                Shapes.box(1 - connectorDepth, connectorSideStart, connectorSideStart, 1, connectorSideEnd, connectorSideEnd),
        };
    }

    public static VoxelShape[] buildCombinedShapes(VoxelShape[] sideShapes) {
        VoxelShape[] combinedShapes = new VoxelShape[1 << 6];

        for (int mask = 0; mask < (1 << 6); ++mask) {
            VoxelShape currentShape = CORE_SHAPE;

            for (int i = 0; i < 6; ++i) {
                if ((mask & (1 << i)) > 0) {
                    currentShape = Shapes.or(currentShape, sideShapes[i]);
                }
            }

            combinedShapes[mask] = currentShape.optimize();
        }

        return combinedShapes;
    }

    public static VoxelShape[] combinePiecewise(VoxelShape[] part1, VoxelShape[] part2) {
        VoxelShape[] combinedShapes = new VoxelShape[6];

        for (int i = 0; i < 6; ++i) {
            combinedShapes[i] = Shapes.or(part1[i], part2[i]).optimize();
        }

        return combinedShapes;
    }
}
