package dev.technici4n.moderndynamics.test;

import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class MachineExtenderTest {
    @MdGameTest
    public void testForwardWeakSignal(MdGameTestHelper helper) {
        var l0 = BlockPos.ZERO.above();
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
