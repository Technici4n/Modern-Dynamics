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
package dev.technici4n.moderndynamics.test;

import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.test.framework.MdGameTest;
import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;

public class FluidTransferTest extends MdGameTest {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void cauldronToCauldronExtractor(MdGameTestHelper helper) {
        var toFillPos = new BlockPos(0, 1, 0);
        var toEmptyPos = new BlockPos(3, 1, 0);

        helper.setBlock(toFillPos, Blocks.CAULDRON);
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.FLUID_PIPE);
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.EAST, MdItems.EXTRACTOR);
        helper.setBlock(toEmptyPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

        int timeToFill = (int) Math.ceil((double) FluidConstants.BUCKET / Constants.Fluids.BASE_IO);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidConstants.BUCKET);
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void cauldronToCauldronAttractor(MdGameTestHelper helper) {
        var toFillPos = new BlockPos(0, 1, 0);
        var toEmptyPos = new BlockPos(3, 1, 0);

        helper.setBlock(toFillPos, Blocks.CAULDRON);
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.WEST, MdItems.ATTRACTOR);
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.FLUID_PIPE);
        helper.setBlock(toEmptyPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

        int timeToFill = (int) Math.ceil((double) FluidConstants.BUCKET / Constants.Fluids.BASE_IO);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidConstants.BUCKET);
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void cauldronToCauldronAttractorExtractor(MdGameTestHelper helper) {
        var toFillPos = new BlockPos(0, 1, 0);
        var toEmptyPos = new BlockPos(3, 1, 0);

        helper.setBlock(toFillPos, Blocks.CAULDRON);
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.WEST, MdItems.ATTRACTOR);
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.EAST, MdItems.EXTRACTOR);
        helper.setBlock(toEmptyPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

        // Both attractor and extractor are active. Cauldron is discrete, only first two levels can be done in parallel.
        int timeToFill = (int) Math.ceil((double) FluidConstants.BUCKET / Constants.Fluids.BASE_IO * 2 / 3);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidConstants.BUCKET);
        });
    }

    // TODO Edge case: Attractor should only grant pulling power for fluids matching filter. Imagine water attractor and lava attractor. Their pulling
    // power can never be combined.

    // TODO Edge case: Pushing into the network should not be allowed to change the fluid if part of the network is unloaded.
}
