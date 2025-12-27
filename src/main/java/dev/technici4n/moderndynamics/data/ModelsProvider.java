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

import dev.technici4n.moderndynamics.client.model.PipeItemModel;
import dev.technici4n.moderndynamics.client.model.PipeBlockstateModel;
import dev.technici4n.moderndynamics.client.model.PipeModelGenerator;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModelsProvider extends ModelSubProvider {
    public ModelsProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        super(blockModels, itemModels);
    }

    @Override
    protected void register() {
        var ext = MdBlocks.MACHINE_EXTENDER.get();

        var columnTexture = TextureMapping.getBlockTexture(ext, "_column");
        var sideTexture = TextureMapping.getBlockTexture(ext, "_side");
        var sideTopTexture = TextureMapping.getBlockTexture(ext, "_side_top");
        var endTexture = TextureMapping.getBlockTexture(ext, "_end");

        simpleBlockAndItem(MdBlocks.MACHINE_EXTENDER.get());
//
//        var topModel = blockModels.cubeBottomTop("machine_extender_top", sideTopTexture, columnTexture, endTexture);
//        var normalModel = models().cubeColumn("machine_extender_normal", sideTexture, columnTexture);
//
//        getVariantBuilder(ext)
//                .partialState().with(MachineExtenderBlock.TOP, true).addModels(new ConfiguredModel(topModel))
//                .partialState().with(MachineExtenderBlock.TOP, false).addModels(new ConfiguredModel(normalModel));
//        simpleBlockItem(ext, normalModel);
//
//        for (var attachment : MdItems.ALL_ATTACHMENTS) {
//            itemModels().basicItem(attachment);
//        }
//        wrench();
//        itemModels().basicItem(MdItems.DEBUG_TOOL);
//
        // Generate model files referencing the custom loader for Pipe
        var pipeBaseModel = Identifier.withDefaultNamespace("item/generated");
        for (var pipeBlock : MdBlocks.getAllPipes()) {
            var id = BuiltInRegistries.BLOCK.getKey(pipeBlock).getPath();

            var generator = new PipeModelGenerator.Unbaked(id, pipeBlock.isTransparent());
            itemModels.itemModelOutput.accept(pipeBlock.asItem(), new PipeItemModel.Unbaked(generator));

            blockStateOutput.accept(createSimpleBlock(pipeBlock, customBlockStateModel(new PipeBlockstateModel.Unbaked(generator))));

            //var path = "block/" + pipeBlock.id;
            //var model = models().getBuilder(path)
            //        .customLoader(PipeModelLoaderBuilder::new)
            //        .pipeType(pipeBlock.id)
            //        .transparent(pipeBlock.isTransparent())
            //        .end();
            //// Use the block model as the item parent
            //itemModels().withExistingParent("item/" + pipeBlock.id, modLoc(path));
            //getVariantBuilder(pipeBlock).partialState().setModels(
            //        ConfiguredModel.builder().modelFile(model).build());
        }

        // Generate the model-file for attachments
        for (var item : MdItems.getAllAttachments()) {
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        itemModels.generateFlatItem(MdItems.WRENCH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(MdItems.DEBUG_TOOL.get(), ModelTemplates.FLAT_ITEM);
    }

    private void wrench() {
//        Identifier id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(MdItems.WRENCH));
//        itemModels().getBuilder(id.toString())
//                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
//                .texture("layer0", Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()));
    }

}
