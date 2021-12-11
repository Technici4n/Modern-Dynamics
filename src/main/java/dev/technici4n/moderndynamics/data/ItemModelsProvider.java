package dev.technici4n.moderndynamics.data;

import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockStateDefinitionProvider;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.model.BlockStateModelGenerator;
import net.minecraft.data.client.model.Models;

public class ItemModelsProvider extends FabricBlockStateDefinitionProvider {
	public ItemModelsProvider(FabricDataGenerator dataGenerator) {
		super(dataGenerator);
	}

	@Override
	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
	}

	@Override
	public void generateItemModels(ItemModelGenerator itemGen) {
		for (var attachment : MdItems.ALL_ATTACHMENTS) {
			itemGen.register(attachment, Models.GENERATED);
		}
		itemGen.register(MdItems.WRENCH, Models.HANDHELD);
		itemGen.register(MdItems.DEBUG_TOOL, Models.GENERATED);
	}
}
