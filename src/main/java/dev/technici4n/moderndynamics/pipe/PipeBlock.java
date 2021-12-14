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

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block implements EntityBlock {

    public final String id;
    private PipeItem item;
    private BlockEntityType<PipeBlockEntity> blockEntityType;

    public PipeBlock(String id) {
        super(Properties.of(Material.METAL).noOcclusion().isRedstoneConductor((state, world, pos) -> false));
        this.id = id;
    }

    public PipeItem getItem() {
        Preconditions.checkState(this.item != null, "Item has not been set on %s", this);
        return this.item;
    }

    public void setItem(PipeItem item) {
        Preconditions.checkState(this.item == null, "Item has already been set on %s", this);
        this.item = item;
    }

    @Nullable
    public BlockEntityType<PipeBlockEntity> getBlockEntityTypeNullable() {
        return this.blockEntityType;
    }

    public BlockEntityType<PipeBlockEntity> getBlockEntityType() {
        Preconditions.checkState(this.blockEntityType != null, "Block entity type has not been set on %s", this);
        return this.blockEntityType;
    }

    public void setBlockEntityProvider(BlockEntityType<PipeBlockEntity> blockEntityType) {
        Preconditions.checkState(this.blockEntityType == null, "blockEntityType has already been set on %s", this);
        this.blockEntityType = blockEntityType;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getLightBlock(BlockState state, BlockGetter blockView, BlockPos pos) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block param4, BlockPos param5, boolean param6) {
        if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            pipe.scheduleHostUpdates();
        }
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            return pipe.getCachedShape();
        } else {
            return PipeBoundingBoxes.CORE_SHAPE;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            return pipe.onUse(player, hand, hitResult);
        } else {
            return InteractionResult.PASS;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
                pipe.onRemoved();
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.create(pos, state);
    }
}
