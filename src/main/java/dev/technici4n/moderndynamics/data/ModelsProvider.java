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
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;

public class ModelsProvider extends FabricModelProvider {
    public ModelsProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockGen) {
        var ext = MdBlocks.MACHINE_EXTENDER;

        var columnTexture = TextureMapping.getBlockTexture(ext, "_column");
        var sideTexture = TextureMapping.getBlockTexture(ext, "_side");
        var sideTopTexture = TextureMapping.getBlockTexture(ext, "_side_top");
        var endTexture = TextureMapping.getBlockTexture(ext, "_end");

        TextureMapping topMapping = new TextureMapping()
                .put(TextureSlot.SIDE, sideTopTexture)
                .put(TextureSlot.TOP, endTexture)
                .put(TextureSlot.BOTTOM, columnTexture);
        Variant topVariant = Variant.variant()
                .with(VariantProperties.MODEL,
                        ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(ext, "_top", topMapping, blockGen.modelOutput));

        TextureMapping normalMapping = new TextureMapping()
                .put(TextureSlot.SIDE, sideTexture)
                .put(TextureSlot.END, columnTexture);
        ResourceLocation normalVariantId = ModelTemplates.CUBE_COLUMN.createWithSuffix(ext, "_normal", normalMapping, blockGen.modelOutput);
        Variant normalVariant = Variant.variant().with(VariantProperties.MODEL, normalVariantId);

        blockGen.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(ext)
                        .with(PropertyDispatch.property(MachineExtenderBlock.TOP)
                                .select(true, topVariant)
                                .select(false, normalVariant)));
        blockGen.delegateItemModel(ext, normalVariantId);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemGen) {
        for (var attachment : MdItems.ALL_ATTACHMENTS) {
            itemGen.generateFlatItem(attachment, ModelTemplates.FLAT_ITEM);
        }
        itemGen.generateFlatItem(MdItems.WRENCH, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemGen.generateFlatItem(MdItems.DEBUG_TOOL, ModelTemplates.FLAT_ITEM);
    }
}
