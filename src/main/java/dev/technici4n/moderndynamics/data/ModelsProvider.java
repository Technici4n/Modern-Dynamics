/*
 * Modern Dynamics
 * Copyright (C) 2021 shartte & Technici4n
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package dev.technici4n.moderndynamics.data;

import dev.technici4n.moderndynamics.extender.MachineExtenderBlock;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModelsProvider extends BlockStateProvider {
    public ModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MdId.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var ext = MdBlocks.MACHINE_EXTENDER;

        var columnTexture = TextureMapping.getBlockTexture(ext, "_column");
        var sideTexture = TextureMapping.getBlockTexture(ext, "_side");
        var sideTopTexture = TextureMapping.getBlockTexture(ext, "_side_top");
        var endTexture = TextureMapping.getBlockTexture(ext, "_end");

        var topModel = models().cubeBottomTop("machine_extender_top", sideTopTexture, columnTexture, endTexture);
        var normalModel = models().cubeColumn("machine_extender_normal", sideTexture, columnTexture);

        getVariantBuilder(ext)
                .partialState().with(MachineExtenderBlock.TOP, true).addModels(new ConfiguredModel(topModel))
                .partialState().with(MachineExtenderBlock.TOP, false).addModels(new ConfiguredModel(normalModel));
        simpleBlockItem(ext, normalModel);

        for (var attachment : MdItems.ALL_ATTACHMENTS) {
            itemModels().basicItem(attachment);
        }
        wrench();
        itemModels().basicItem(MdItems.DEBUG_TOOL);
    }

    private void wrench() {
        ResourceLocation id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(MdItems.WRENCH));
        itemModels().getBuilder(id.toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
    }

}
