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
package dev.technici4n.moderndynamics.test.framework;

import dev.technici4n.moderndynamics.pipe.PipeBlock;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.level.material.Fluid;

public class MdGameTestHelper extends GameTestHelper {
    public MdGameTestHelper(GameTestInfo gameTestInfo) {
        super(gameTestInfo);
    }

    /**
     * Place a pipe.
     */
    public PipeBuilder pipe(BlockPos pos, PipeBlock block) {
        setBlock(pos, block);

        var pipe = (PipeBlockEntity) getBlockEntity(pos);
        Objects.requireNonNull(pipe, "Pipe block entity is null");

        return new PipeBuilder(this, pipe);
    }

    /**
     * Throw exception unless target block pos (relative) has at least some amount of some fluid.
     */
    public void checkFluid(BlockPos pos, Fluid fluid, long minAmount) {
        var storage = FluidStorage.SIDED.find(getLevel(), absolutePos(pos), Direction.UP);

        if (storage != null) {
            var storedAmount = storage.simulateExtract(FluidVariant.of(fluid), Long.MAX_VALUE, null);

            if (storedAmount >= minAmount) {
                return;
            }
        }

        fail("Fluid not found");
    }
}
