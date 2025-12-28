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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class MachineExtenderBlock extends MdBlock {
    private static final ScopedValue<?> SIGNAL_FORWARDING = ScopedValue.newInstance();

    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public MachineExtenderBlock(Properties props) {
        super(props.mapColor(MapColor.METAL).destroyTime(0.2f));

        registerDefaultState(defaultBlockState().setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour.getAxis().isVertical()) {
            return state.setValue(TOP, !level.getBlockState(pos.above()).is(this));
        }

        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            @org.jspecify.annotations.Nullable Orientation orientation, boolean movedByPiston) {
        // TODO 26.1: This is likely wrong
        if (orientation == null || orientation.getFront() == Direction.UP) {
            // Forward update if it's coming from below
            if (level.getBlockEntity(pos) instanceof MachineExtenderBlockEntity sideExtender) {
                sideExtender.inNeighborUpdate = true;

                try {
                    sideExtender.getLevel().updateNeighborsAtExceptFromFacing(pos, this, Direction.DOWN, orientation); // TODO 26.1: This is likely
                                                                                                                       // wrong
                } finally {
                    sideExtender.inNeighborUpdate = false;
                }
            }
        }
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (pos.below().equals(neighbor) && level instanceof Level actualLevel) {
            actualLevel.updateNeighborsAtExceptFromFacing(pos, this, Direction.DOWN, null);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (direction == Direction.DOWN || SIGNAL_FORWARDING.isBound()) {
            return 0;
        }

        // Seek to the first non-extender below
        var below = pos.below();
        while (below.getY() > 0 && level.getBlockState(below).is(this)) {
            below = below.below();
        }

        // Ensure we don't get recursively called
        var source = below;
        return ScopedValue.where(SIGNAL_FORWARDING, null).call(() -> {
            return level.getBlockState(source).getAnalogOutputSignal(level, source, Direction.UP);
        });
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        // This mirrors what ChestBlock does
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
}
