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

import static net.minecraft.client.data.models.BlockModelGenerators.createBooleanModelDispatch;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

import dev.technici4n.moderndynamics.client.model.PipeBlockstateModel;
import dev.technici4n.moderndynamics.client.model.PipeItemModel;
import dev.technici4n.moderndynamics.client.model.PipeModelGenerator;
import dev.technici4n.moderndynamics.extender.MachineExtenderBlock;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModelsProvider extends ModelSubProvider {
    public ModelsProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        super(blockModels, itemModels);
    }

    @Override
    protected void register() {
        machineExtender();

        for (var pipeBlock : MdBlocks.getAllPipes()) {
            var id = BuiltInRegistries.BLOCK.getKey(pipeBlock).getPath();

            var generator = new PipeModelGenerator.Unbaked(id, pipeBlock.isTransparent());
            itemModels.itemModelOutput.accept(pipeBlock.asItem(), new PipeItemModel.Unbaked(generator));

            blockStateOutput.accept(createSimpleBlock(pipeBlock, customBlockStateModel(new PipeBlockstateModel.Unbaked(generator))));
        }

        // Generate the model-file for attachments
        for (var item : MdItems.getAllAttachments()) {
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        itemModels.generateFlatItem(MdItems.WRENCH.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(MdItems.DEBUG_TOOL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    private void machineExtender() {
        var ext = MdBlocks.MACHINE_EXTENDER.get();
        var columnTexture = TextureMapping.getBlockTexture(ext, "_column");
        var sideTexture = TextureMapping.getBlockTexture(ext, "_side");
        var sideTopTexture = TextureMapping.getBlockTexture(ext, "_side_top");
        var endTexture = TextureMapping.getBlockTexture(ext, "_end");

        var topModelTextures = new TextureMapping()
                .put(TextureSlot.SIDE, sideTopTexture)
                .put(TextureSlot.TOP, endTexture)
                .put(TextureSlot.BOTTOM, columnTexture);
        var topModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(ext, "_top", topModelTextures, modelOutput);

        var normalModelTextures = new TextureMapping()
                .put(TextureSlot.SIDE, sideTexture)
                .put(TextureSlot.END, columnTexture);
        var normalModel = ModelTemplates.CUBE_COLUMN.create(ext, normalModelTextures, modelOutput);

        blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ext)
                        .with(createBooleanModelDispatch(MachineExtenderBlock.TOP, plainVariant(topModel), plainVariant(normalModel))));
    }

}
