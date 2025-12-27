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
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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

        var pipe = getBlockEntity(pos, PipeBlockEntity.class);
        Objects.requireNonNull(pipe, "Pipe block entity is null");

        return new PipeBuilder(this, pipe);
    }

    public PipeBuilder pipe(BlockPos pos, Supplier<PipeBlock> block) {
        return pipe(pos, block.get());
    }

    /**
     * Throw exception unless target block pos (relative) has at least some amount of some item.
     */
    public void checkFluid(BlockPos pos, Fluid fluid, int minAmount) {
        var handler = getLevel().getCapability(Capabilities.Fluid.BLOCK, absolutePos(pos), Direction.UP);

        if (handler != null) {
            try (var tx = Transaction.openRoot()) {
                var drained = handler.extract(FluidResource.of(fluid), Integer.MAX_VALUE, tx);

                if (drained >= minAmount) {
                    return;
                }
            }
        }

        fail(Component.literal("Fluid not found"), pos);
    }

    /**
     * Throw exception unless target block pos (relative) has at least some amount of some item.
     */
    public void checkItem(BlockPos pos, Item item, int minAmount) {
        var handler = getLevel().getCapability(Capabilities.Item.BLOCK, absolutePos(pos), Direction.UP);

        if (handler != null) {
            int amountFound = 0;
            for (int i = 0; i < handler.size(); i++) {
                var stack = handler.getResource(i);
                if (stack.is(item)) {
                    amountFound += handler.getAmountAsInt(i);
                }
            }
            if (amountFound >= minAmount) {
                return;
            }
        }

        fail("Item not found", pos);
    }

    public void fail(String text, BlockPos pos) {
        fail(Component.literal(text), pos);
    }
}
