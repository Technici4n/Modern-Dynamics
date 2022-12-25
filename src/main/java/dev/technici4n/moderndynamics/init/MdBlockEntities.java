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
package dev.technici4n.moderndynamics.init;

import com.google.common.base.Preconditions;
import dev.technici4n.moderndynamics.MdBlockEntity;
import dev.technici4n.moderndynamics.network.energy.EnergyPipeTier;
import dev.technici4n.moderndynamics.pipe.*;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

public final class MdBlockEntities {

    public static final BlockEntityType<PipeBlockEntity> ITEM_PIPE = createItemPipe(MdBlocks.ITEM_PIPE);
    public static final BlockEntityType<PipeBlockEntity> FLUID_PIPE = register(FluidPipeBlockEntity::new, MdBlocks.FLUID_PIPE);
    /*
     * public static final BlockEntityType<PipeBlockEntity> BASIC_ITEM_PIPE_OPAQUE = createItemPipe(MdBlocks.BASIC_ITEM_PIPE_OPAQUE);
     * public static final BlockEntityType<PipeBlockEntity> FAST_ITEM_PIPE = register(NyiPipeBlockEntity::new, MdBlocks.FAST_ITEM_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> FAST_ITEM_PIPE_OPAQUE = register(NyiPipeBlockEntity::new, MdBlocks.FAST_ITEM_PIPE_OPAQUE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_ITEM_PIPE = register(NyiPipeBlockEntity::new, MdBlocks.CONDUCTIVE_ITEM_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_ITEM_PIPE_OPAQUE = register(NyiPipeBlockEntity::new,
     * MdBlocks.CONDUCTIVE_ITEM_PIPE_OPAQUE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_ITEM_PIPE = register(NyiPipeBlockEntity::new,
     * MdBlocks.CONDUCTIVE_FAST_ITEM_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE = register(NyiPipeBlockEntity::new,
     * MdBlocks.CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE);
     * public static final BlockEntityType<PipeBlockEntity> BASIC_FLUID_PIPE_OPAQUE = register(NyiPipeBlockEntity::new,
     * MdBlocks.BASIC_FLUID_PIPE_OPAQUE);
     * public static final BlockEntityType<PipeBlockEntity> FAST_FLUID_PIPE = register(NyiPipeBlockEntity::new, MdBlocks.FAST_FLUID_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> FAST_FLUID_PIPE_OPAQUE = register(NyiPipeBlockEntity::new,
     * MdBlocks.FAST_FLUID_PIPE_OPAQUE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FLUID_PIPE = register(NyiPipeBlockEntity::new, MdBlocks.CONDUCTIVE_FLUID_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FLUID_PIPE_OPAQUE = register(NyiPipeBlockEntity::new,
     * MdBlocks.CONDUCTIVE_FLUID_PIPE_OPAQUE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_FLUID_PIPE = register(NyiPipeBlockEntity::new,
     * MdBlocks.CONDUCTIVE_FAST_FLUID_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE = register(NyiPipeBlockEntity::new,
     * MdBlocks.CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE);
     * 
     * public static final BlockEntityType<PipeBlockEntity> BASIC_ENERGY_PIPE = createEnergyPipe(EnergyPipeTier.BASIC, MdBlocks.BASIC_ENERGY_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> IMPROVED_ENERGY_PIPE = createEnergyPipe(EnergyPipeTier.IMPROVED,
     * MdBlocks.IMPROVED_ENERGY_PIPE);
     * public static final BlockEntityType<PipeBlockEntity> ADVANCED_ENERGY_PIPE = createEnergyPipe(EnergyPipeTier.ADVANCED,
     * MdBlocks.ADVANCED_ENERGY_PIPE);
     */

    public static void init() {
        // init static
    }

    /**
     * Registers a {@link BlockEntityType} for a single block type and inherits the blocks registry id for the type.
     */
    private static <T extends PipeBlockEntity> BlockEntityType<T> register(BlockEntityConstructor<T> factory, PipeBlock block) {
        TypeFactory<T> typeFactory = new TypeFactory<>(factory);
        BlockEntityType<T> type = FabricBlockEntityTypeBuilder.create(typeFactory, block).build(null);
        typeFactory.type = type;
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, MdId.of(block.id), type);
        // noinspection unchecked
        block.setBlockEntityProvider((BlockEntityType<PipeBlockEntity>) type);

        // Register item, fluid and energy API.
        registerLookup(ItemStorage.SIDED, type);
        registerLookup(FluidStorage.SIDED, type);
        registerLookup(EnergyStorage.SIDED, type);

        return type;
    }

    private static <A> void registerLookup(BlockApiLookup<A, Direction> lookup, BlockEntityType<? extends PipeBlockEntity> type) {
        var apiClass = lookup.apiClass();
        lookup.registerForBlockEntity((pipe, dir) -> apiClass.cast(pipe.getApiInstance(lookup, dir)), type);
    }

    private static BlockEntityType<PipeBlockEntity> createItemPipe(PipeBlock block) {
        return register(ItemPipeBlockEntity::new, block);
    }

    private static BlockEntityType<PipeBlockEntity> createEnergyPipe(EnergyPipeTier tier, PipeBlock block) {
        return register((type, pos, state) -> new EnergyPipeBlockEntity(type, tier, pos, state), block);
    }

    /**
     * Helper class to solve that the constructor for the block entity needs to reference the block entity type,
     * but to create the block entity type, we need the constructor (recursion, blergh).
     */
    static class TypeFactory<T extends MdBlockEntity> implements FabricBlockEntityTypeBuilder.Factory<T> {

        final BlockEntityConstructor<T> constructor;

        BlockEntityType<T> type;

        public TypeFactory(BlockEntityConstructor<T> constructor) {
            this.constructor = constructor;
        }

        @Override
        public T create(BlockPos blockPos, BlockState blockState) {
            Preconditions.checkState(type != null, "type has not been registered properly");
            return constructor.instantiate(type, blockPos, blockState);
        }
    }

    private interface BlockEntityConstructor<T extends MdBlockEntity> {
        T instantiate(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }

}
