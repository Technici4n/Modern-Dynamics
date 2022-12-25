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
package dev.technici4n.moderndynamics.util;

import dev.technici4n.moderndynamics.init.MdTags;
import dev.technici4n.moderndynamics.pipe.PipeBlock;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * Helper to detect if items are wrenches, and to make wrench shift-clicking dismantle MT pipes.
 */
public class WrenchHelper {
    public static boolean isWrench(ItemStack stack) {
        return stack.isIn(MdTags.WRENCHES);
    }

    /**
     * Dismantle target pipe on shift right-click with a wrench.
     */
    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator() || !player.isSneaking() || !world.canPlayerModifyAt(player, hitResult.getBlockPos())
                    || !isWrench(player.getStackInHand(hand))) {
                return ActionResult.PASS;
            }

            var pos = hitResult.getBlockPos();
            var state = world.getBlockState(pos);
            if (state.getBlock() instanceof PipeBlock) {
                var entity = world.getBlockEntity(pos);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                if (!player.isCreative()) {
                    Block.dropStacks(state, world, pos, entity);
                }
                // Play a cool sound
                var group = state.getSoundGroup();
                world.playSound(player, pos, group.getBreakSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                        group.getPitch() * 0.8F);
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        });
    }
}
