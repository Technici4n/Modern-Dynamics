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

import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import dev.technici4n.moderndynamics.attachment.settings.RoutingMode;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.test.framework.MdGameTest;
import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class ItemDistributionTest extends MdGameTest {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void testRoundRobinInvalidTarget(MdGameTestHelper helper) {
        var sourceChest = new BlockPos(0, 1, 0);
        helper.setBlock(sourceChest, Blocks.CHEST);

        var targetChest1 = new BlockPos(2, 1, 0);
        helper.setBlock(targetChest1, Blocks.CHEST);
        var targetChest2 = new BlockPos(2, 1, 1);
        helper.setBlock(targetChest2, Blocks.CHEST);
        var targetChest3 = new BlockPos(2, 1, 2);
        helper.setBlock(targetChest3, Blocks.CHEST);

        var pipePos = new BlockPos(1, 1, 0);
        helper.pipe(pipePos, MdBlocks.ITEM_PIPE)
                .attachment(Direction.WEST, MdItems.EXTRACTOR)
                .configureItemIo(Direction.WEST, io -> {
                    io.setUpgrade(0, Items.COMPARATOR.getDefaultInstance());
                    io.setUpgrade(1, new ItemStack(Items.REPEATER, 3));
                    io.setUpgrade(2, Items.STICKY_PISTON.getDefaultInstance());
                    io.setMaxItemsExtracted(1);
                    io.setRoutingMode(RoutingMode.ROUND_ROBIN);
                });
        helper.pipe(new BlockPos(1, 1, 1), MdBlocks.ITEM_PIPE)
                .attachment(Direction.EAST, MdItems.FILTER)
                .configureItemIo(Direction.EAST, io -> {
                    io.setRedstoneMode(RedstoneMode.REQUIRES_HIGH);
                });
        helper.pipe(new BlockPos(1, 1, 2), MdBlocks.ITEM_PIPE);

        ((ChestBlockEntity) helper.getBlockEntity(sourceChest)).setItem(0, new ItemStack(Items.DIAMOND, 6));

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertContainerEmpty(sourceChest);
                    helper.checkItem(targetChest1, Items.DIAMOND, 3);
                    helper.checkItem(targetChest2, Items.DIAMOND, 0);
                    helper.checkItem(targetChest3, Items.DIAMOND, 3);
                })
                .thenSucceed();
    }
}
