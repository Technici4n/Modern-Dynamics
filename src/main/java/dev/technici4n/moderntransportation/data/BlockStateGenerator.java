package dev.technici4n.moderntransportation.data;

import dev.technici4n.moderntransportation.block.PipeBlock;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.util.MtId;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateGenerator extends BlockStateProvider {

	public BlockStateGenerator(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
		super(gen, modid, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		registerPipeModel(MtBlocks.BASIC_ITEM_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.BASIC_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.FAST_ITEM_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.FAST_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_ITEM_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE, "lead", "connection_lead");

		registerPipeModel(MtBlocks.BASIC_FLUID_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.BASIC_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.FAST_FLUID_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.FAST_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_FLUID_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE, "lead", "connection_lead");

		registerPipeModel(MtBlocks.BASIC_ENERGY_PIPE, "base/energy/lead", "connector/lead");
		registerPipeModel(MtBlocks.HARDENED_ENERGY_PIPE, "base/energy/invar", "connector/invar");
		registerPipeModel(MtBlocks.REINFORCED_ENERGY_PIPE, "base/energy/electrum", "connector/electrum");
		registerPipeModel(MtBlocks.SIGNALUM_ENERGY_PIPE, "base/energy/signalum", "connector/signalum");
		registerPipeModel(MtBlocks.RESONANT_ENERGY_PIPE, "base/energy/enderium", "connector/enderium");
		registerPipeModel(MtBlocks.SUPERCONDUCTING_PIPE, "lead", "connection_lead");

		registerPipeModel(MtBlocks.EMPTY_REINFORCED_ENERGY_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.EMPTY_SIGNALUM_ENERGY_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.EMPTY_RESONANT_ENERGY_PIPE, "lead", "connection_lead");
		registerPipeModel(MtBlocks.EMPTY_SUPERCONDUCTING_PIPE, "lead", "connection_lead");
	}

	private void registerPipeModel(PipeBlock pipe, String texture, String connectionTexture) {
		String basePath = pipe.getRegistryName().getPath() + "/";

		simpleBlock(
				pipe,
				itemModels()
						.getBuilder(pipe.getRegistryName().getPath())
						.customLoader(PipeModelBuilder::begin)
						.connectionNone(
								models().getBuilder(basePath + "none")
										.parent(models().getExistingFile(modLoc("base/pipe_none")))
										.texture("0", MtId.of(texture))
						)
						.connectionInventory(
								models().getBuilder(basePath + "inventory")
										.parent(models().getExistingFile(modLoc("base/pipe_inventory")))
										.texture("0", MtId.of(connectionTexture))
						)
						.connectionPipe(
								models().getBuilder(basePath + "pipe")
										.parent(models().getExistingFile(modLoc("base/pipe_pipe")))
										.texture("0", MtId.of(texture))
						)
						.end()
		);
	}


}
