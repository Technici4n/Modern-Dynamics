/*
 * Modern Transportation
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block implements BlockEntityProvider {

    public final String id;
    private PipeItem item;
    private BlockEntityType<PipeBlockEntity> blockEntityType;

    public PipeBlock(String id) {
        super(Settings.of(Material.METAL).nonOpaque().solidBlock((state, world, pos) -> false));
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
    public int getOpacity(BlockState p_200011_1_, BlockView p_200011_2_, BlockPos p_200011_3_) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block param4, BlockPos param5, boolean param6) {
        BlockEntity be = world.getBlockEntity(pos);

        if (be instanceof PipeBlockEntity) {
            ((PipeBlockEntity) be).scheduleHostUpdates();
        }
    }

    @Override
    public boolean hasDynamicBounds() {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        BlockEntity be = world.getBlockEntity(pos);

        if (be instanceof PipeBlockEntity) {
            return ((PipeBlockEntity) be).getCachedShape();
        } else {
            return PipeBoundingBoxes.CORE_SHAPE;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        BlockEntity be = world.getBlockEntity(pos);

        if (be instanceof PipeBlockEntity) {
            return ((PipeBlockEntity) be).onUse(player, hand, hitResult);
        } else {
            return ActionResult.PASS;
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.instantiate(pos, state);
    }
}
