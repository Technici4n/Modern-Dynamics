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
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import dev.technici4n.moderndynamics.util.FluidVariant;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MdId.MOD_ID)
@PrefixGameTestTemplate(false)
public class FluidTransferTest {
    @MdGameTest
    public void cauldronToCauldronExtractor(MdGameTestHelper helper) {
        var toFillPos = new BlockPos(0, 1, 0);
        var toEmptyPos = new BlockPos(3, 1, 0);

        helper.setBlock(toFillPos, Blocks.CAULDRON);
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.FLUID_PIPE);
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.EAST, MdItems.EXTRACTOR);
        helper.setBlock(toEmptyPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

        int timeToFill = (int) Math.ceil((double) FluidType.BUCKET_VOLUME / Constants.Fluids.BASE_IO);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidType.BUCKET_VOLUME);
        });
    }

    @MdGameTest
    public void cauldronToCauldronAttractor(MdGameTestHelper helper) {
        var toFillPos = new BlockPos(0, 1, 0);
        var toEmptyPos = new BlockPos(3, 1, 0);

        helper.setBlock(toFillPos, Blocks.CAULDRON);
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.WEST, MdItems.ATTRACTOR);
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.FLUID_PIPE);
        helper.setBlock(toEmptyPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

        int timeToFill = (int) Math.ceil((double) FluidType.BUCKET_VOLUME / Constants.Fluids.BASE_IO);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidType.BUCKET_VOLUME);
        });
    }

    @MdGameTest
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
        int timeToFill = (int) Math.ceil((double) FluidType.BUCKET_VOLUME / Constants.Fluids.BASE_IO * 2 / 3);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidType.BUCKET_VOLUME);
        });
    }

    @MdGameTest
    public void doubleAttractor(MdGameTestHelper helper) {
        var toFillPos = new BlockPos(0, 1, 0);
        var toEmptyPos = new BlockPos(3, 1, 0);

        helper.setBlock(toFillPos, Blocks.CAULDRON);
        helper.setBlock(1, 1, 1, Blocks.LAVA_CAULDRON);
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.WEST, MdItems.ATTRACTOR)
                .attachment(Direction.SOUTH, MdItems.ATTRACTOR);
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.FLUID_PIPE);
        helper.setBlock(toEmptyPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

        // Two attractors are pulling (with only 1 viable target). Only half the time should be needed.
        int timeToFill = (int) Math.ceil((double) FluidType.BUCKET_VOLUME / Constants.Fluids.BASE_IO / 2);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidType.BUCKET_VOLUME);
        });
    }

    /**
     * Test that attractors only grant pulling power for fluids matching filter.
     * Here we test that an attractor filtered for lava will not attract water.
     */
    @MdGameTest
    public void doubleAttractorOneFiltered(MdGameTestHelper helper) {
        var toFillPos = new BlockPos(0, 1, 0);
        var toEmptyPos = new BlockPos(3, 1, 0);

        helper.setBlock(toFillPos, Blocks.CAULDRON);
        helper.setBlock(1, 1, 1, Blocks.LAVA_CAULDRON);
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.FLUID_PIPE)
                .attachment(Direction.WEST, MdItems.ATTRACTOR)
                .attachment(Direction.SOUTH, MdItems.ATTRACTOR)
                .configureFluidIo(Direction.SOUTH, io -> {
                    io.setFilterInversion(FilterInversionMode.WHITELIST);
                    io.setFilter(0, FluidVariant.of(Fluids.LAVA));
                });
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.FLUID_PIPE);
        helper.setBlock(toEmptyPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

        // Two attractors are pulling, but only 1 has a filter matching water. So only 1 is effectively pulling.
        int timeToFill = (int) Math.ceil((double) FluidType.BUCKET_VOLUME / Constants.Fluids.BASE_IO);
        helper.succeedOnTickWhen(timeToFill, () -> {
            helper.checkFluid(toFillPos, Fluids.WATER, FluidType.BUCKET_VOLUME);
        });
    }
}
