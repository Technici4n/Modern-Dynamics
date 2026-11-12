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

import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import java.util.concurrent.atomic.AtomicReference;
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
        var chest = helper.getBlockEntity(targetChest, ChestBlockEntity.class);

        var pipe = targetChest.east();
        helper.pipe(pipe, MdBlocks.ITEM_PIPE);

        var hopper = pipe.above();
        helper.setBlock(hopper, Blocks.HOPPER.defaultBlockState());

        var damagedItem = Items.DIAMOND_PICKAXE.getDefaultInstance();
        damagedItem.setDamageValue(500);
        (helper.getBlockEntity(hopper, HopperBlockEntity.class)).setItem(0, damagedItem.copy());

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
        var chest = helper.getBlockEntity(sourceChest, ChestBlockEntity.class);

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

    /**
     * When a traveling stack can only be partially inserted at the end of its path,
     * only the leftover may be stuffed into the attachment there.
     * <p>
     * An upgraded extractor sends a single stack of 16 to a target that is then filled up.
     * The stack is sent back to the source, which by then only has room for 4 of them.
     */
    @MdGameTest(timeoutTicks = 1000)
    public void testPartialDeliveryStuffsOnlyLeftover(MdGameTestHelper helper) {
        var sourcePos = new BlockPos(0, 1, 0);
        helper.setBlock(sourcePos, Blocks.CHEST.defaultBlockState());
        var source = helper.getBlockEntity(sourcePos, ChestBlockEntity.class);
        source.setItem(0, new ItemStack(Items.DIAMOND, 16));

        var extractor = new AtomicReference<ItemAttachedIo>();
        helper.pipe(new BlockPos(1, 1, 0), MdBlocks.ITEM_PIPE)
                .attachment(Direction.WEST, MdItems.EXTRACTOR)
                .configureItemIo(Direction.WEST, io -> {
                    // 4 + 6 * 5 = 34 items per operation: the whole stack leaves in one go
                    io.setUpgrade(0, new ItemStack(Items.HOPPER, 6));
                    io.setMaxItemsExtracted(16);
                    extractor.set(io);
                });
        helper.pipe(new BlockPos(2, 1, 0), MdBlocks.ITEM_PIPE);

        var targetPos = new BlockPos(3, 1, 0);
        helper.setBlock(targetPos, Blocks.CHEST.defaultBlockState());
        var target = helper.getBlockEntity(targetPos, ChestBlockEntity.class);

        helper.startSequence()
                // Wait until the whole stack was extracted and is traveling
                .thenWaitUntil(() -> {
                    if (!source.isEmpty()) {
                        helper.fail("Expected source chest to be emptied", sourcePos);
                    }
                })
                .thenExecute(() -> {
                    // The target rejects the stack, so it travels back to the source...
                    for (int i = 0; i < target.getContainerSize(); ++i) {
                        target.setItem(i, new ItemStack(Items.DIRT, 64));
                    }
                    // ... which only has room for 4 diamonds by then.
                    source.setItem(0, new ItemStack(Items.DIAMOND, 60));
                    for (int i = 1; i < source.getContainerSize(); ++i) {
                        source.setItem(i, new ItemStack(Items.DIRT, 64));
                    }
                })
                // Wait until the stack came back (inserted or stuffed)
                .thenWaitUntil(() -> {
                    if (insertedDiamonds(source) + stuffedCount(extractor.get()) < 16) {
                        helper.fail("Expected the stack to come back", sourcePos);
                    }
                })
                .thenExecute(() -> {
                    if (insertedDiamonds(source) != 4) {
                        helper.fail("Expected 4 diamonds to be inserted, got " + insertedDiamonds(source), sourcePos);
                    }
                    if (stuffedCount(extractor.get()) != 12) {
                        helper.fail("Expected 12 stuffed diamonds, got " + stuffedCount(extractor.get()), sourcePos);
                    }
                })
                .thenSucceed();
    }

    private static int insertedDiamonds(ChestBlockEntity chest) {
        return chest.getItem(0).getCount() - 60;
    }

    private static int stuffedCount(ItemAttachedIo io) {
        return io.getStuffedItems().values().stream().mapToInt(Integer::intValue).sum();
    }
}
