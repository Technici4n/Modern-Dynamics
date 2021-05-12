package dev.technici4n.moderntransportation.init;

import com.google.common.base.Preconditions;
import dev.technici4n.moderntransportation.MtBlockEntity;
import dev.technici4n.moderntransportation.block.PipeBlock;
import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Function;
import java.util.function.Supplier;

public final class MtBlockEntities {

	public static final BlockEntityType<PipeBlockEntity> BASIC_ITEM_PIPE = create(PipeBlockEntity::new, MtBlocks.BASIC_ITEM_PIPE);
	public static final BlockEntityType<PipeBlockEntity> BASIC_ITEM_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.BASIC_ITEM_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> FAST_ITEM_PIPE = create(PipeBlockEntity::new, MtBlocks.FAST_ITEM_PIPE);
	public static final BlockEntityType<PipeBlockEntity> FAST_ITEM_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.FAST_ITEM_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_ITEM_PIPE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_ITEM_PIPE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_ITEM_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_ITEM_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_ITEM_PIPE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> BASIC_FLUID_PIPE = create(PipeBlockEntity::new, MtBlocks.BASIC_FLUID_PIPE);
	public static final BlockEntityType<PipeBlockEntity> BASIC_FLUID_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.BASIC_FLUID_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> FAST_FLUID_PIPE = create(PipeBlockEntity::new, MtBlocks.FAST_FLUID_PIPE);
	public static final BlockEntityType<PipeBlockEntity> FAST_FLUID_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.FAST_FLUID_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FLUID_PIPE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_FLUID_PIPE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FLUID_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_FLUID_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_FLUID_PIPE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE);
	public static final BlockEntityType<PipeBlockEntity> CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE = create(PipeBlockEntity::new, MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE);
	public static final BlockEntityType<PipeBlockEntity> BASIC_ENERGY_PIPE = create(PipeBlockEntity::new, MtBlocks.BASIC_ENERGY_PIPE);
	public static final BlockEntityType<PipeBlockEntity> HARDENED_ENERGY_PIPE = create(PipeBlockEntity::new, MtBlocks.HARDENED_ENERGY_PIPE);
	public static final BlockEntityType<PipeBlockEntity> REINFORCED_ENERGY_PIPE = create(PipeBlockEntity::new, MtBlocks.REINFORCED_ENERGY_PIPE);
	public static final BlockEntityType<PipeBlockEntity> SIGNALUM_ENERGY_PIPE = create(PipeBlockEntity::new, MtBlocks.SIGNALUM_ENERGY_PIPE);
	public static final BlockEntityType<PipeBlockEntity> RESONANT_ENERGY_PIPE = create(PipeBlockEntity::new, MtBlocks.RESONANT_ENERGY_PIPE);
	public static final BlockEntityType<PipeBlockEntity> SUPERCONDUCTING_PIPE = create(PipeBlockEntity::new, MtBlocks.SUPERCONDUCTING_PIPE);

	/**
	 * Registers a {@link BlockEntityType} for a single block type and inherits the blocks registry id for
	 * the type.
	 */
	private static <T extends PipeBlockEntity> BlockEntityType<T> create(Function<BlockEntityType<T>, T> factory, PipeBlock block) {
		TypeFactory<T> typeFactory = new TypeFactory<>(factory);
		BlockEntityType<T> type = BlockEntityType.Builder.create(typeFactory, block).build(null);
		typeFactory.type = type;
		type.setRegistryName(block.getRegistryName());
		block.setBlockEntityProvider(type);
		return type;
	}

	private MtBlockEntities() {
	}

	public static void init(IForgeRegistry<BlockEntityType<?>> registry) {

	}

	/**
	 * Helper class to solve that the constructor for the block entity needs to reference the block entity type,
	 * but to create the block entity type, we need the constructor (recursion, blergh).
	 */
	static class TypeFactory<T extends MtBlockEntity> implements Supplier<T> {

		final Function<BlockEntityType<T>, T> factory;

		BlockEntityType<T> type;

		public TypeFactory(Function<BlockEntityType<T>, T> factory) {
			this.factory = factory;
		}

		@Override
		public T get() {
			Preconditions.checkState(type != null, "type has not been registered properly");
			return factory.apply(type);
		}
	}

}
