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
package dev.technici4n.moderndynamics.extender;

import dev.technici4n.moderndynamics.MdBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class MachineExtenderBlock extends MdBlock {
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public MachineExtenderBlock() {
        super("machine_extender", Properties.of().mapColor(MapColor.METAL).destroyTime(0.2f));

        registerDefaultState(defaultBlockState().setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos,
            BlockPos neighborPos) {
        if (direction.getAxis().isVertical()) {
            return state.setValue(TOP, !level.getBlockState(currentPos.above()).is(this));
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = super.getStateForPlacement(context);
        if (state != null) {
            var pos = context.getClickedPos();
            var level = context.getLevel();
            state = state.setValue(TOP, !level.getBlockState(pos.above()).is(this));
        }
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (fromPos.equals(pos.below())) {
            // Forward update if it's coming from below
            if (level.getBlockEntity(pos) instanceof MachineExtenderBlockEntity sideExtender) {
                sideExtender.inNeighborUpdate = true;

                try {
                    level.updateNeighborsAtExceptFromFacing(pos, this, Direction.DOWN);
                } finally {
                    sideExtender.inNeighborUpdate = false;
                }
            }
        }

        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }
}
