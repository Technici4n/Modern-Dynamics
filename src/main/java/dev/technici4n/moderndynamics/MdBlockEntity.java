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
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MdBlockEntity extends BlockEntity {
    private static final Logger LOG = LoggerFactory.getLogger(MdBlockEntity.class);

    private boolean shouldClientRemesh = true;

    public MdBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state) {
        super(bet, pos, state);
    }

    // Thank you Fabric API
    public void sync(boolean shouldRemesh) {
        Preconditions.checkNotNull(level); // Maintain distinct failure case from below
        if (!(level instanceof ServerLevel serverLevel))
            throw new IllegalStateException("Cannot call sync() on the logical client! Did you check level.isClient first?");

        shouldClientRemesh = shouldRemesh | shouldClientRemesh;
        serverLevel.getChunkSource().blockChanged(getBlockPos());
    }

    public void sync() {
        sync(true);
    }

    public abstract void toTag(ValueOutput output);

    public abstract void fromTag(ValueInput input);

    public abstract void toClientTag(ValueOutput output);

    public abstract void fromClientTag(ValueInput input);

    @Override
    public final ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (var reporter = new ProblemReporter.ScopedCollector(problemPath(), LOG)) {
            var output = TagValueOutput.createWithContext(reporter, registries);
            this.saveCustomOnly(output);
            toClientTag(output);
            output.putInt("#c", shouldClientRemesh ? 1 : 0); // mark client tag
            shouldClientRemesh = false;
            return super.getUpdateTag(registries).merge(output.buildResult());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        var client = input.getInt("#c");
        if (client.isPresent()) {
            fromClientTag(input);
            if (client.get() > 0) {
                remesh();
            }
        } else {
            fromTag(input);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        toTag(output);
    }

    public final void remesh() {
        Preconditions.checkNotNull(level);
        if (!level.isClientSide())
            throw new IllegalStateException("Cannot call remesh() on the server!");

        level.sendBlockUpdated(getBlockPos(), null, null, 0);
    }

    protected final boolean isClientSide() {
        if (level == null) {
            throw new IllegalStateException("Cannot determine if the BE is client-side if it has no level yet");
        }
        return level.isClientSide();
    }
}
