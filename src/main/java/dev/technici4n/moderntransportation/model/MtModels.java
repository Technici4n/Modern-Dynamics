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
package dev.technici4n.moderntransportation.model;

import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class MtModels {
    /**
     * The model rotation to rotate a model facing NORTH to the correct facing direction.
     * Rotations are indexed by {@link Direction} id.
     */
    public static final ModelBakeSettings[] PIPE_BAKE_SETTINGS = new ModelBakeSettings[] {
            preRotated(ModelRotation.X90_Y0, 270),
            ModelRotation.X270_Y0,
            ModelRotation.X0_Y0,
            preRotated(ModelRotation.X0_Y180, 90),
            preRotated(ModelRotation.X0_Y270, 90),
            ModelRotation.X0_Y90,
    };

    public static ModelBakeSettings preRotated(ModelRotation rotation, float preAngle) {
        AffineTransformation preRotation = new AffineTransformation(null, new Quaternion(new Vec3f(0, 0, 1), preAngle, true), null, null);
        AffineTransformation combined = rotation.getRotation().multiply(preRotation);
        return new ModelBakeSettings() {
            @Override
            public AffineTransformation getRotation() {
                return combined;
            }
        };
    }
}
