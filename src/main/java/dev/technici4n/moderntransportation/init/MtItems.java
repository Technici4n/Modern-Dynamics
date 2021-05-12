package dev.technici4n.moderntransportation.init;

import dev.technici4n.moderntransportation.block.PipeItem;
import dev.technici4n.moderntransportation.debug.DebugToolItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class MtItems {

	public static final PipeItem BASIC_ITEM_PIPE = new PipeItem(MtBlocks.BASIC_ITEM_PIPE);
	public static final PipeItem BASIC_ITEM_PIPE_OPAQUE = new PipeItem(MtBlocks.BASIC_ITEM_PIPE_OPAQUE);
	public static final PipeItem FAST_ITEM_PIPE = new PipeItem(MtBlocks.FAST_ITEM_PIPE);
	public static final PipeItem FAST_ITEM_PIPE_OPAQUE = new PipeItem(MtBlocks.FAST_ITEM_PIPE_OPAQUE);
	public static final PipeItem CONDUCTIVE_ITEM_PIPE = new PipeItem(MtBlocks.CONDUCTIVE_ITEM_PIPE);
	public static final PipeItem CONDUCTIVE_ITEM_PIPE_OPAQUE = new PipeItem(MtBlocks.CONDUCTIVE_ITEM_PIPE_OPAQUE);
	public static final PipeItem CONDUCTIVE_FAST_ITEM_PIPE = new PipeItem(MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE);
	public static final PipeItem CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE = new PipeItem(MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE);
	public static final PipeItem BASIC_FLUID_PIPE = new PipeItem(MtBlocks.BASIC_FLUID_PIPE);
	public static final PipeItem BASIC_FLUID_PIPE_OPAQUE = new PipeItem(MtBlocks.BASIC_FLUID_PIPE_OPAQUE);
	public static final PipeItem FAST_FLUID_PIPE = new PipeItem(MtBlocks.FAST_FLUID_PIPE);
	public static final PipeItem FAST_FLUID_PIPE_OPAQUE = new PipeItem(MtBlocks.FAST_FLUID_PIPE_OPAQUE);
	public static final PipeItem CONDUCTIVE_FLUID_PIPE = new PipeItem(MtBlocks.CONDUCTIVE_FLUID_PIPE);
	public static final PipeItem CONDUCTIVE_FLUID_PIPE_OPAQUE = new PipeItem(MtBlocks.CONDUCTIVE_FLUID_PIPE_OPAQUE);
	public static final PipeItem CONDUCTIVE_FAST_FLUID_PIPE = new PipeItem(MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE);
	public static final PipeItem CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE = new PipeItem(MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE);
	public static final PipeItem BASIC_ENERGY_PIPE = new PipeItem(MtBlocks.BASIC_ENERGY_PIPE);
	public static final PipeItem HARDENED_ENERGY_PIPE = new PipeItem(MtBlocks.HARDENED_ENERGY_PIPE);
	public static final PipeItem REINFORCED_ENERGY_PIPE = new PipeItem(MtBlocks.REINFORCED_ENERGY_PIPE);
	public static final PipeItem SIGNALUM_ENERGY_PIPE = new PipeItem(MtBlocks.SIGNALUM_ENERGY_PIPE);
	public static final PipeItem RESONANT_ENERGY_PIPE = new PipeItem(MtBlocks.RESONANT_ENERGY_PIPE);
	public static final PipeItem SUPERCONDUCTING_PIPE = new PipeItem(MtBlocks.SUPERCONDUCTING_PIPE);
	public static final PipeItem EMPTY_REINFORCED_ENERGY_PIPE = new PipeItem(MtBlocks.EMPTY_REINFORCED_ENERGY_PIPE);
	public static final PipeItem EMPTY_SIGNALUM_ENERGY_PIPE = new PipeItem(MtBlocks.EMPTY_SIGNALUM_ENERGY_PIPE);
	public static final PipeItem EMPTY_RESONANT_ENERGY_PIPE = new PipeItem(MtBlocks.EMPTY_RESONANT_ENERGY_PIPE);
	public static final PipeItem EMPTY_SUPERCONDUCTING_PIPE = new PipeItem(MtBlocks.EMPTY_SUPERCONDUCTING_PIPE);

	public static final DebugToolItem DEBUG_TOOL = new DebugToolItem();

	public static void init(IForgeRegistry<Item> registry) {
		ForgeRegistries.ITEMS.register(DEBUG_TOOL.setRegistryName("debug_tool"));

		registry.registerAll(
				BASIC_ITEM_PIPE,
				BASIC_ITEM_PIPE_OPAQUE,
				FAST_ITEM_PIPE,
				FAST_ITEM_PIPE_OPAQUE,
				CONDUCTIVE_ITEM_PIPE,
				CONDUCTIVE_ITEM_PIPE_OPAQUE,
				CONDUCTIVE_FAST_ITEM_PIPE,
				CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE,
				BASIC_FLUID_PIPE,
				BASIC_FLUID_PIPE_OPAQUE,
				FAST_FLUID_PIPE,
				FAST_FLUID_PIPE_OPAQUE,
				CONDUCTIVE_FLUID_PIPE,
				CONDUCTIVE_FLUID_PIPE_OPAQUE,
				CONDUCTIVE_FAST_FLUID_PIPE,
				CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE,
				BASIC_ENERGY_PIPE,
				HARDENED_ENERGY_PIPE,
				REINFORCED_ENERGY_PIPE,
				SIGNALUM_ENERGY_PIPE,
				RESONANT_ENERGY_PIPE,
				SUPERCONDUCTING_PIPE,
				EMPTY_REINFORCED_ENERGY_PIPE,
				EMPTY_SIGNALUM_ENERGY_PIPE,
				EMPTY_RESONANT_ENERGY_PIPE,
				EMPTY_SUPERCONDUCTING_PIPE
		);
	}

}
