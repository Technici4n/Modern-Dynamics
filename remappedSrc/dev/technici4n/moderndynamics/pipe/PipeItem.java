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
import dev.technici4n.moderndynamics.util.WrenchHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PipeItem extends BlockItem {
    public PipeItem(PipeBlock block) {
        super(block, new Item.Settings().tab(MdItemGroup.getInstance()));
        block.setItem(this);
    }

    @Override
    public PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        boolean couldPlace = super.place(context, state);
        if (!couldPlace) {
            return false;
        }

        var level = context.getWorld();
        var pos = context.getBlockPos();
        var maybePlayer = context.getPlayer();

        if (!level.isClient()) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof PipeBlockEntity pipe) {
                for (Direction direction : Direction.values()) {
                    // If adjacent pipes have us blacklisted, we blacklist them here.
                    BlockEntity adjBe = level.getBlockEntity(pos.offset(direction));
                    if (adjBe instanceof PipeBlockEntity adjPipe) {
                        if ((adjPipe.connectionBlacklist & (1 << direction.getOpposite().getId())) > 0) {
                            pipe.connectionBlacklist |= 1 << direction.getId();
                        }
                    }

                    // If wrench in offhand: blacklist all sides regardless.
                    if (maybePlayer != null && WrenchHelper.isWrench(maybePlayer.getOffHandStack())) {
                        pipe.connectionBlacklist |= 1 << direction.getId();
                        // Also blacklist the adjacent pipe.
                        if (adjBe instanceof PipeBlockEntity adjPipe) {
                            adjPipe.connectionBlacklist |= 1 << direction.getOpposite().getId();
                            level.markDirty(adjPipe.getPos()); // Skip comparator update, just in case.
                        }

                        // If sneaking: allow connection to the clicked face, so remove from both blacklists.
                        if (maybePlayer.isSneaking() && direction.getOpposite() == context.getSide()) {
                            pipe.connectionBlacklist ^= 1 << direction.getId();
                            // Also remove from adjacent pipe.
                            if (adjBe instanceof PipeBlockEntity adjPipe) {
                                adjPipe.connectionBlacklist ^= 1 << direction.getOpposite().getId();
                                level.markDirty(adjPipe.getPos()); // Skip comparator update, just in case.
                            }
                        }
                    }
                }
            }
            // else warn?
        }

        return true;
    }
}
