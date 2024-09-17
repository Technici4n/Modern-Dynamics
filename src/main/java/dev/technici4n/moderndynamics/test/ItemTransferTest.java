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
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public class ItemTransferTest {
    @MdGameTest
    public void testHopperInsertingDamagedItem(MdGameTestHelper helper) {
        var targetChest = new BlockPos(0, 1, 0);
        helper.setBlock(targetChest, Blocks.CHEST.defaultBlockState());
        var chest = (ChestBlockEntity) helper.getBlockEntity(targetChest);

        var pipe = targetChest.east();
        helper.pipe(pipe, MdBlocks.ITEM_PIPE);

        var hopper = pipe.above();
        helper.setBlock(hopper, Blocks.HOPPER.defaultBlockState());

        var damagedItem = Items.DIAMOND_PICKAXE.getDefaultInstance();
        damagedItem.setDamageValue(500);
        ((HopperBlockEntity) helper.getBlockEntity(hopper)).setItem(0, damagedItem.copy());

        helper.startSequence()
                .thenWaitUntil(() -> {
                    if (chest.getItem(0).isEmpty()) {
                        helper.fail("Expected item in chest", targetChest);
                    }
                    if (!ItemStack.matches(damagedItem, chest.getItem(0))) {
                        helper.fail("Wrong item in chest", targetChest);
                    }
                })
                .thenSucceed();
    }

    @MdGameTest
    public void testExtractorLimitIsForEntireInventory(MdGameTestHelper helper) {
        var sourceChest = new BlockPos(0, 1, 0);
        helper.setBlock(sourceChest, Blocks.CHEST.defaultBlockState());
        var chest = (ChestBlockEntity) helper.getBlockEntity(sourceChest);

        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.ITEM_PIPE)
                .attachment(Direction.WEST, MdItems.EXTRACTOR)
                .configureItemIo(Direction.WEST, io -> {
                    io.setMaxItemsExtracted(2);
                });

        var targetChest = new BlockPos(2, 1, 0);
        helper.setBlock(targetChest, Blocks.CHEST.defaultBlockState());

        for (int i = 0; i < 9; ++i) {
            chest.setItem(i, new ItemStack(Items.DIAMOND, 7));
        }

        helper.startSequence()
                .thenIdle(1)
                .thenExecute(() -> {
                    if (!ItemStack.matches(new ItemStack(Items.DIAMOND, 5), chest.getItem(0))) {
                        helper.fail("Expected 6 diamonds in slot 0", targetChest);
                    }
                    for (int i = 1; i < 9; ++i) {
                        if (!ItemStack.matches(new ItemStack(Items.DIAMOND, 7), chest.getItem(i))) {
                            helper.fail("Expected 16 diamonds in slot " + i, targetChest);
                        }
                    }
                })
                .thenSucceed();
    }
}
