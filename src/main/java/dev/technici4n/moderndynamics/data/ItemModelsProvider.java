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
		itemGen.register(MdItems.SERVO, Models.GENERATED);
		itemGen.register(MdItems.FILTER, Models.GENERATED);
		itemGen.register(MdItems.WRENCH, Models.HANDHELD);
		itemGen.register(MdItems.DEBUG_TOOL, Models.GENERATED);
	}
}
