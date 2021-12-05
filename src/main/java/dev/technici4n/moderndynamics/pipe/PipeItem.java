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
package dev.technici4n.moderndynamics.pipe;

import dev.technici4n.moderndynamics.util.MdItemGroup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PipeItem extends BlockItem {
    public PipeItem(PipeBlock block) {
        super(block, new Item.Settings().group(MdItemGroup.getInstance()));
        block.setItem(this);
    }

    @Override
    public PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity placer, ItemStack stack, BlockState state) {
        if (!world.isClient()) {
            // If adjacent pipes have us blacklisted, we blacklist them here.
            BlockEntity be = world.getBlockEntity(pos);

            if (be instanceof PipeBlockEntity pipe) {
                for (Direction direction : Direction.values()) {
                    BlockEntity adjBe = world.getBlockEntity(pos.offset(direction));

                    if (adjBe instanceof PipeBlockEntity adjPipe) {
                        if ((adjPipe.connectionBlacklist & (1 << direction.getOpposite().getId())) > 0) {
                            pipe.connectionBlacklist |= 1 << direction.getId();
                        }
                    }
                }
            }
            // else warn?
        }

        // No clue what the return is for, vanilla doesn't seem to use it.
        return true;
    }
}
