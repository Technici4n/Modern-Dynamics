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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public abstract class MdBlockEntity extends BlockEntity {
    private boolean shouldClientRemesh = true;

    public MdBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state) {
        super(bet, pos, state);
    }

    // Thank you Fabric API
    public void sync(boolean shouldRemesh) {
        Preconditions.checkNotNull(world); // Maintain distinct failure case from below
        if (!(world instanceof ServerWorld serverWorld))
            throw new IllegalStateException("Cannot call sync() on the logical client! Did you check world.isClient first?");

        shouldClientRemesh = shouldRemesh | shouldClientRemesh;
        serverWorld.getChunkManager().markForUpdate(getPos());
    }

    public void sync() {
        sync(true);
    }

    public abstract void toTag(NbtCompound tag);

    public abstract void fromTag(NbtCompound tag);

    public abstract void toClientTag(NbtCompound tag);

    public abstract void fromClientTag(NbtCompound tag);

    @Override
    public final BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public final NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = super.toInitialChunkDataNbt();
        toClientTag(nbt);
        nbt.putBoolean("#c", shouldClientRemesh); // mark client tag
        shouldClientRemesh = false;
        return nbt;
    }

    @Override
    protected final void writeNbt(NbtCompound nbt) {
        toTag(nbt);
    }

    @Override
    public final void readNbt(NbtCompound nbt) {
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
        Preconditions.checkNotNull(world);
        if (!world.isClient())
            throw new IllegalStateException("Cannot call remesh() on the server!");

        world.updateListeners(pos, null, null, 0);
    }

    protected final boolean isClientSide() {
        if (world == null) {
            throw new IllegalStateException("Cannot determine if the BE is client-side if it has no level yet");
        }
        return world.isClient;
    }
}
