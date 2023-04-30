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
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MachineExtenderBlockEntity extends MdBlockEntity {
    private static int registeredApis = 0;

    public static <A> void forwardApi(BlockEntityType<MachineExtenderBlockEntity> bet, BlockApiLookup<A, Direction> lookup) {
        int apiId = registeredApis++;

        lookup.registerForBlockEntity((sideExtender, direction) -> {
            if (sideExtender.inApiQuery[apiId]) {
                return null;
            }

            sideExtender.inApiQuery[apiId] = true;
            try {
                var cache = (BlockApiCache<A, Direction>) sideExtender.apiCaches[apiId];

                if (cache == null) {
                    var queryPos = sideExtender.getBlockPos().below();

                    if (sideExtender.getLevel() instanceof ServerLevel serverLevel) {
                        sideExtender.apiCaches[apiId] = cache = BlockApiCache.create(lookup, serverLevel, queryPos);
                    } else {
                        // Client path, fall back to normal lookup
                        return lookup.find(sideExtender.getLevel(), queryPos, direction);
                    }
                }

                return cache.find(direction);
            } finally {
                sideExtender.inApiQuery[apiId] = false;
            }
        }, bet);
    }

    private final boolean[] inApiQuery = new boolean[registeredApis];
    private final BlockApiCache[] apiCaches = new BlockApiCache[registeredApis];
    boolean inNeighborUpdate = false;

    public MachineExtenderBlockEntity(BlockEntityType<?> bet, BlockPos pos, BlockState state) {
        super(bet, pos, state);
    }

    @Override
    public void toTag(CompoundTag tag) {
    }

    @Override
    public void fromTag(CompoundTag tag) {
    }

    @Override
    public void toClientTag(CompoundTag tag) {
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
    }
}
