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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;

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
}
