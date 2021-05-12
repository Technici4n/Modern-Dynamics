package dev.technici4n.moderntransportation;

import dev.technici4n.moderntransportation.model.PipeModelLoader;
import dev.technici4n.moderntransportation.util.MtId;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public final class ModernTransportationClient {
    private ModernTransportationClient() {
        throw new UnsupportedOperationException();
    }

    public static void registerClientEvents() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(ModernTransportationClient::registerModelLoaders);
    }

    private static void registerModelLoaders(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(PipeModelLoader.ID, new PipeModelLoader());
    }
}
