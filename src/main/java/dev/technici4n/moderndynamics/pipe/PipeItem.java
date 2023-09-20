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

import dev.technici4n.moderndynamics.util.WrenchHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PipeItem extends BlockItem {
    public PipeItem(PipeBlock block) {
        super(block, new Item.Properties());
        block.setItem(this);
    }

    @Override
    public PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean couldPlace = super.placeBlock(context, state);
        if (!couldPlace) {
            return false;
        }

        var level = context.getLevel();
        var pos = context.getClickedPos();
        var maybePlayer = context.getPlayer();

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof PipeBlockEntity pipe) {
                for (Direction direction : Direction.values()) {
                    // If adjacent pipes have us blacklisted, we blacklist them here.
                    BlockEntity adjBe = level.getBlockEntity(pos.relative(direction));
                    if (adjBe instanceof PipeBlockEntity adjPipe) {
                        if ((adjPipe.connectionBlacklist & (1 << direction.getOpposite().get3DDataValue())) > 0) {
                            pipe.connectionBlacklist |= 1 << direction.get3DDataValue();
                        }
                    }

                    // If wrench in offhand: blacklist all sides regardless.
                    if (maybePlayer != null && WrenchHelper.isWrench(maybePlayer.getOffhandItem())) {
                        pipe.connectionBlacklist |= 1 << direction.get3DDataValue();
                        // Also blacklist the adjacent pipe.
                        if (adjBe instanceof PipeBlockEntity adjPipe) {
                            adjPipe.connectionBlacklist |= 1 << direction.getOpposite().get3DDataValue();
                            level.blockEntityChanged(adjPipe.getBlockPos()); // Skip comparator update, just in case.
                        }

                        // If sneaking: allow connection to the clicked face, so remove from both blacklists.
                        if (maybePlayer.isShiftKeyDown() && direction.getOpposite() == context.getClickedFace()) {
                            pipe.connectionBlacklist ^= 1 << direction.get3DDataValue();
                            // Also remove from adjacent pipe.
                            if (adjBe instanceof PipeBlockEntity adjPipe) {
                                adjPipe.connectionBlacklist ^= 1 << direction.getOpposite().get3DDataValue();
                                level.blockEntityChanged(adjPipe.getBlockPos()); // Skip comparator update, just in case.
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
