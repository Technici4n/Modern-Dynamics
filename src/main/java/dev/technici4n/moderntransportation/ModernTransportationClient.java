package dev.technici4n.moderntransportation;

import dev.technici4n.moderntransportation.block.PipeBlock;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.model.PipeModelLoader;
import dev.technici4n.moderntransportation.util.MtId;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public final class ModernTransportationClient {
    private ModernTransportationClient() {
        throw new UnsupportedOperationException();
    }

    public static void registerClientEvents() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(ModernTransportationClient::clientSetup);
        modEventBus.addListener(ModernTransportationClient::registerModelLoaders);
    }

    private static void registerModelLoaders(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(PipeModelLoader.ID, new PipeModelLoader());
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        for (PipeBlock pipeBlock : MtBlocks.ALL_PIPES) {
            RenderLayers.setRenderLayer(pipeBlock, RenderLayer.getCutout());
        }
    }
}
