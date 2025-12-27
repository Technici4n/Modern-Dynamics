package dev.technici4n.moderndynamics.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.List;

public final class MdModelProvider extends ModelProvider {
    public static Factory<DataProvider> create(String modId, ModelSubProviderFactory... subProviders) {
        var subProviderList = List.of(subProviders);
        return output -> new MdModelProvider(output, modId, subProviderList);
    }

    // This matches the super-class constructor of ModelSubProvider
    @FunctionalInterface
    public interface ModelSubProviderFactory {
        ModelSubProvider create(BlockModelGenerators blockModels, ItemModelGenerators itemModels);
    }

    private final List<ModelSubProviderFactory> subProviders;

    public MdModelProvider(PackOutput packOutput, String modid, List<ModelSubProviderFactory> subProviders) {
        super(packOutput, modid);
        this.subProviders = subProviders;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (var subProvider : subProviders) {
            subProvider.create(blockModels, itemModels).register();
        }
    }

    @Override
    public String getName() {
        return super.getName() + " " + getClass().getName();
    }
}
