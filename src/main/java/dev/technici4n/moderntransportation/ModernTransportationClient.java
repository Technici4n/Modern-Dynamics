package dev.technici4n.moderntransportation;

import dev.technici4n.moderntransportation.model.PipeModelLoader;
import dev.technici4n.moderntransportation.util.MtId;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public final class ModernTransportationClient {
    private ModernTransportationClient() {
        throw new UnsupportedOperationException();
    }

    public static void initClient() {
        setupModels();
    }

    private static void setupModels() {
        ModelLoaderRegistry.registerLoader(MtId.of("pipe"), new PipeModelLoader());
    }
}
