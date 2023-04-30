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
package dev.technici4n.moderndynamics;

import com.google.common.base.Preconditions;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MdBlock extends Block implements EntityBlock {

    public final String id;
    private BlockEntityType<PipeBlockEntity> blockEntityType;

    public MdBlock(String id, Properties properties) {
        super(properties);
        this.id = id;
    }

    @Nullable
    public final BlockEntityType<PipeBlockEntity> getBlockEntityTypeNullable() {
        return this.blockEntityType;
    }

    public final BlockEntityType<PipeBlockEntity> getBlockEntityType() {
        Preconditions.checkState(this.blockEntityType != null, "Block entity type has not been set on %s", this);
        return this.blockEntityType;
    }

    public final void setBlockEntityProvider(BlockEntityType<PipeBlockEntity> blockEntityType) {
        Preconditions.checkState(this.blockEntityType == null, "blockEntityType has already been set on %s", this);
        this.blockEntityType = blockEntityType;
    }

    @Nullable
    @Override
    public final BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.create(pos, state);
    }
}
