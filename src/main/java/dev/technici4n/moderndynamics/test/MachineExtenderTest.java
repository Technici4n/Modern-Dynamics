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

import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class MachineExtenderTest {
    @MdGameTest
    public void machineExtenderNotForwardingFromBelow(MdGameTestHelper helper) {
        var origin = new BlockPos(4, 0, 4);
        helper.setBlock(origin, Blocks.HOPPER);
        helper.setBlock(origin.above(), MdBlocks.MACHINE_EXTENDER.get());

        helper.startSequence()
                .thenExecute(() -> {
                    var hopperCap = helper.requireCapability(Capabilities.Item.BLOCK, origin, Direction.UP);

                    var extenderPos = origin.above();
                    for (var side : Direction.values()) {
                        var cap = helper.getCapability(Capabilities.Item.BLOCK, extenderPos, side);
                        if (side != Direction.DOWN) {
                            if (cap != hopperCap) {
                                throw helper.assertionException(extenderPos, Component.literal("Should expose Hopper on side " + side));
                            }
                        } else {
                            if (cap != null) {
                                throw helper.assertionException(extenderPos, Component.literal("Should NOT expose Hopper on side " + side));
                            }
                        }
                    }
                })
                .thenSucceed();
    }

    @MdGameTest
    public void machineExtenderForwardWeakSignal(MdGameTestHelper helper) {
        var l0 = new BlockPos(4, 1, 4);
        var l1 = l0.above();
        var l2 = l1.above();

        helper.setBlock(l0, Blocks.HOPPER);
        helper.setBlock(l0.north().below(), Blocks.STONE, Direction.SOUTH);
        helper.setBlock(l0.north(), Blocks.COMPARATOR, Direction.SOUTH);
        helper.setBlock(l0.north().north(), Blocks.REDSTONE_LAMP);

        helper.setBlock(l1, MdBlocks.MACHINE_EXTENDER.get());
        helper.setBlock(l1.east().below(), Blocks.STONE);
        helper.setBlock(l1.east(), Blocks.COMPARATOR, Direction.WEST);
        helper.setBlock(l1.east().east(), Blocks.REDSTONE_LAMP);

        helper.setBlock(l2, MdBlocks.MACHINE_EXTENDER.get());
        helper.setBlock(l2.west().below(), Blocks.STONE);
        helper.setBlock(l2.west(), Blocks.COMPARATOR, Direction.EAST);
        helper.setBlock(l2.west().west(), Blocks.REDSTONE_LAMP);

        var lamps = List.of(l0.north().north(), l1.east().east(), l2.west().west());
        var lampOff = Blocks.REDSTONE_LAMP.defaultBlockState();
        var lampOn = lampOff.setValue(RedstoneLampBlock.LIT, true);

        helper.startSequence()
                .thenExecute(() -> {
                    for (var lamp : lamps) {
                        helper.assertBlockState(lamp, lampOff);
                    }
                })
                .thenExecuteAfter(1, () -> {
                    // Fill the hopper
                    try (var tx = Transaction.openRoot()) {
                        var items = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(l0), Direction.UP);
                        for (int i = 0; i < items.size(); i++) {
                            items.insert(ItemResource.of(Items.STICK), 64, tx);
                        }
                        tx.commit();
                    }
                })
                .thenExecuteAfter(5, () -> {
                    for (var lamp : lamps) {
                        helper.assertBlockState(lamp, lampOn);
                    }
                })
                .thenSucceed();
    }
}
