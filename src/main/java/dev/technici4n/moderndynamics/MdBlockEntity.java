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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MdBlockEntity extends BlockEntity {
    private boolean shouldClientRemesh = true;

    public MdBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state) {
        super(bet, pos, state);
    }

    // Thank you Fabric API
    public void sync(boolean shouldRemesh) {
        Preconditions.checkNotNull(level); // Maintain distinct failure case from below
        if (!(level instanceof ServerLevel serverWorld))
            throw new IllegalStateException("Cannot call sync() on the logical client! Did you check world.isClient first?");

        shouldClientRemesh = shouldRemesh | shouldClientRemesh;
        serverWorld.getChunkSource().blockChanged(getBlockPos());
    }

    public void sync() {
        sync(true);
    }

    public abstract void toTag(CompoundTag tag);

    public abstract void fromTag(CompoundTag tag);

    public abstract void toClientTag(CompoundTag tag);

    public abstract void fromClientTag(CompoundTag tag);

    @Override
    public final ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public final CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        toClientTag(nbt);
        nbt.putBoolean("#c", shouldClientRemesh); // mark client tag
        shouldClientRemesh = false;
        return nbt;
    }

    @Override
    protected final void saveAdditional(CompoundTag nbt) {
        toTag(nbt);
    }

    @Override
    public final void load(CompoundTag nbt) {
        if (nbt.contains("#c")) {
            fromClientTag(nbt);
            if (nbt.getBoolean("#c")) {
                remesh();
            }
        } else {
            fromTag(nbt);
        }
    }

    public final void remesh() {
        Preconditions.checkNotNull(level);
        if (!(level instanceof ClientLevel))
            throw new IllegalStateException("Cannot call remesh() on the server!");

        level.sendBlockUpdated(worldPosition, null, null, 0);
    }
}
