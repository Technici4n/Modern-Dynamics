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
package dev.technici4n.moderndynamics.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

public class MdModels {
    /**
     * The model rotation to rotate a model facing NORTH to the correct facing direction.
     * Rotations are indexed by {@link Direction} id.
     */
    public static final ModelState[] PIPE_BAKE_SETTINGS = new ModelState[] {
            preRotated(BlockModelRotation.X90_Y0, 270),
            BlockModelRotation.X270_Y0,
            BlockModelRotation.X0_Y0,
            preRotated(BlockModelRotation.X0_Y180, 90),
            preRotated(BlockModelRotation.X0_Y270, 90),
            BlockModelRotation.X0_Y90,
    };

    public static ModelState preRotated(BlockModelRotation rotation, float preAngle) {

        Transformation preRotation = new Transformation(null, new Quaternionf(0, 0, 1, 0), null, null);
        Transformation combined = rotation.getRotation().compose(preRotation);
        return new ModelState() {
            @Override
            public Transformation getRotation() {
                return combined;
            }
        };
    }
}
