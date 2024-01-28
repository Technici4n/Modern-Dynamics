package dev.technici4n.moderndynamics.mixins;

import com.mojang.datafixers.DataFixer;
import dev.technici4n.moderndynamics.ModernDynamics;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// TODO: this should really be in neoforge
@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {
    private static final FileToIdConverter md_SNBT_LISTER = new FileToIdConverter("structures", ".snbt");

    @Shadow
    @Mutable
    private List<StructureTemplateManager.Source> sources;

    @Shadow
    private ResourceManager resourceManager;

    @Shadow
    protected abstract Stream<ResourceLocation> listFolderContents(Path pFolder, String pNamespace, String pPath);

    @Inject(at = @At("RETURN"), method = "<init>")
    private void addTemplateSource(
            ResourceManager pResourceManager,
            LevelStorageSource.LevelStorageAccess pLevelStorageAccess,
            DataFixer pFixerUpper,
            HolderGetter<Block> pBlockLookup,
            CallbackInfo ci) {
        sources = new ArrayList<>(sources);
        sources.add(new StructureTemplateManager.Source(
                loc -> {
                    ResourceLocation resourcelocation = md_SNBT_LISTER.idToFile(loc);
                    try {
                        Optional optional;
                        try (InputStream inputstream = this.resourceManager.open(resourcelocation)) {
                            String s = IOUtils.toString(inputstream, Charset.defaultCharset());
                            optional = Optional.of(((StructureTemplateManager) (Object) this).readStructure(NbtUtils.snbtToStructure(s)));
                        }

                        return optional;
                    } catch (FileNotFoundException filenotfoundexception) {
                        return Optional.empty();
                    } catch (Throwable throwable1) {
                        ModernDynamics.LOGGER.error("Couldn't load SNBT structure {}", loc, throwable1);
                        return Optional.empty();
                    }
                },
                () -> {
                    return md_SNBT_LISTER.listMatchingResources(this.resourceManager).keySet().stream().map(md_SNBT_LISTER::fileToId);
                }));
    }
}
