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

import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class MdGameTestHelper extends GameTestHelper {

    public static final String EMPTY_STRUCTURE = "empty";

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
     * Throw exception unless target block pos (relative) has at least some amount of some item.
     */
    public void checkFluid(BlockPos pos, Fluid fluid, int minAmount) {
        var handler = getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absolutePos(pos), Direction.UP);

        if (handler != null) {
            var drained = handler.drain(new FluidStack(fluid, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE);

            if (!drained.isEmpty() && drained.getAmount() >= minAmount) {
                return;
            }
        }

        fail("Fluid not found", pos);
    }

    /**
     * Throw exception unless target block pos (relative) has at least some amount of some item.
     */
    public void checkItem(BlockPos pos, Item item, long minAmount) {
        var handler = getLevel().getCapability(Capabilities.ItemHandler.BLOCK, absolutePos(pos), Direction.UP);

        if (handler != null) {
            var extracted = handler.extractItem(0, Integer.MAX_VALUE, true);

            if (!extracted.isEmpty() && extracted.getCount() >= minAmount) {
                return;
            }
        }

        fail("Item not found", pos);
    }
}
