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

import dev.technici4n.moderndynamics.MdBlock;
import dev.technici4n.moderndynamics.init.MdTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * Helper to detect if items are wrenches, and to make wrench shift-clicking dismantle MT pipes.
 */
public class WrenchHelper {
    public static boolean isWrench(ItemStack stack) {
        return stack.is(MdTags.WRENCHES);
    }

    /**
     * Dismantle target pipe on shift right-click with a wrench.
     */
    public static void handleEvent(UseItemOnBlockEvent e) {
        if (e.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) {
            return;
        }

        var context = e.getUseOnContext();
        var player = context.getPlayer();
        if (player == null) {
            return;
        }
        var world = context.getLevel();
        var pos = context.getClickedPos();

        if (player.isSpectator() || !player.isShiftKeyDown() || !world.mayInteract(player, pos)
                || !isWrench(player.getItemInHand(context.getHand()))) {
            return;
        }

        var state = world.getBlockState(pos);
        if (state.getBlock() instanceof MdBlock) {
            var entity = world.getBlockEntity(pos);
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (!player.isCreative()) {
                Block.dropResources(state, world, pos, entity);
            }
            // Play a cool sound
            var group = state.getSoundType();
            world.playSound(player, pos, group.getBreakSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                    group.getPitch() * 0.8F);
            e.cancelWithResult(ItemInteractionResult.sidedSuccess(world.isClientSide));
        }
    }
}
