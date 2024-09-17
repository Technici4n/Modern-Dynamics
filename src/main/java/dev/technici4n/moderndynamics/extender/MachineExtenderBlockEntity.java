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

import dev.technici4n.moderndynamics.MdBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public class MachineExtenderBlockEntity extends MdBlockEntity {
    private static int registeredApis = 0;

    private static int getDirectionIndex(@Nullable Direction direction) {
        return direction == null ? 0 : (1 + direction.ordinal());
    }

    private static int getCacheIndex(int apiId, @Nullable Direction direction) {
        return apiId * 7 + getDirectionIndex(direction);
    }

    @SuppressWarnings("unchecked")
    public static <A> void forwardApi(RegisterCapabilitiesEvent evt, BlockEntityType<MachineExtenderBlockEntity> bet,
            BlockCapability<A, Direction> lookup) {
        int apiId = registeredApis++;

        evt.registerBlockEntity(lookup, bet, (sideExtender, direction) -> {
            var cacheIndex = getCacheIndex(apiId, direction);
            if (sideExtender.inApiQuery[cacheIndex]) {
                return null;
            }

            sideExtender.inApiQuery[cacheIndex] = true;
            try {
                var cache = (BlockCapabilityCache<A, Direction>) sideExtender.apiCaches[cacheIndex];

                if (cache == null) {
                    var queryPos = sideExtender.getBlockPos().below();

                    if (sideExtender.getLevel() instanceof ServerLevel serverLevel) {
                        sideExtender.apiCaches[cacheIndex] = cache = BlockCapabilityCache.create(lookup, serverLevel, queryPos, direction);
                    } else {
                        // Client path, fall back to normal lookup
                        return sideExtender.getLevel().getCapability(lookup, queryPos, direction);
                    }
                }

                return cache.getCapability();
            } finally {
                sideExtender.inApiQuery[cacheIndex] = false;
            }
        });
    }

    private final boolean[] inApiQuery = new boolean[registeredApis * (1 + Direction.values().length)];
    private final BlockCapabilityCache[] apiCaches = new BlockCapabilityCache[registeredApis * (1 + Direction.values().length)];
    boolean inNeighborUpdate = false;
    // We need to proxy invalidations of the bottom block to this block.
    private final ICapabilityInvalidationListener invalidationListener = () -> {
        if (isRemoved()) {
            return false;
        }
        this.invalidateCapabilities();
        return true;
    };

    public MachineExtenderBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state) {
        super(bet, pos, state);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.registerCapabilityListener(getBlockPos().below(), invalidationListener);
        }
    }

    @Override
    public void toTag(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Override
    public void fromTag(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Override
    public void toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
    }
}
