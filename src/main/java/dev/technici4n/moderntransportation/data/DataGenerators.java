package dev.technici4n.moderntransportation.data;

import dev.technici4n.moderntransportation.util.MtId;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = MtId.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

	@SubscribeEvent
	public static void onGatherData(GatherDataEvent dataEvent) {
		DataGenerator generator = dataEvent.getGenerator();
		generator.install(new BlockStateGenerator(generator, MtId.MOD_ID, dataEvent.getExistingFileHelper()));
	}

}
