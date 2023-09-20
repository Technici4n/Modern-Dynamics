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

import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipesProvider extends FabricRecipeProvider {
    public RecipesProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, MdItems.ITEM_PIPE, 8)
                .pattern("igi")
                .define('i', Items.IRON_INGOT)
                .define('g', Items.GLASS)
                .unlockedBy("has_ingot", has(Items.IRON_INGOT))
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, MdItems.FLUID_PIPE, 8)
                .pattern("igi")
                .define('i', Items.COPPER_INGOT)
                .define('g', Items.GLASS)
                .unlockedBy("has_ingot", has(Items.COPPER_INGOT))
                .save(exporter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, MdItems.INHIBITOR, 4)
                .pattern("mnm")
                .define('m', Items.IRON_INGOT)
                .define('n', Items.IRON_NUGGET)
                .unlockedBy("has_item_pipe", has(MdItems.ITEM_PIPE))
                .save(exporter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MdItems.WRENCH)
                .pattern(" i ")
                .pattern("lii")
                .pattern("il ")
                .define('i', Items.IRON_INGOT)
                .define('l', Items.LAPIS_LAZULI)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(exporter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, MdItems.ATTRACTOR, 1)
                .pattern(" e ")
                .pattern("mrm")
                .pattern(" p ")
                .define('e', Items.ENDER_PEARL)
                .define('m', Items.IRON_INGOT)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(exporter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, MdItems.EXTRACTOR, 1)
                .pattern(" h ")
                .pattern("mrm")
                .pattern(" p ")
                .define('h', Items.HOPPER)
                .define('m', Items.IRON_INGOT)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_hopper", has(Items.HOPPER))
                .save(exporter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, MdItems.FILTER, 1)
                .pattern(" l ")
                .pattern("mrm")
                .pattern(" p ")
                .define('l', Items.LAPIS_LAZULI)
                .define('m', Items.IRON_INGOT)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_lapis", has(Items.LAPIS_LAZULI))
                .save(exporter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, MdItems.MACHINE_EXTENDER, 4)
                .pattern("bbb")
                .pattern("ibf")
                .pattern("bbb")
                .define('b', Items.IRON_BARS)
                .define('i', MdItems.ITEM_PIPE)
                .define('f', MdItems.FLUID_PIPE)
                .unlockedBy("has_item_pipe", has(MdItems.ITEM_PIPE))
                .unlockedBy("has_fluid_pipe", has(MdItems.FLUID_PIPE))
                .save(exporter);

        var miExporter = withConditions(exporter, DefaultResourceConditions.allModsLoaded("modern_industrialization"));
        generateMiCableRecipes("lv", "tin_cable", miExporter);
        generateMiCableRecipes("mv", "electrum_cable", miExporter);
        generateMiCableRecipes("hv", "aluminum_cable", miExporter);
        generateMiCableRecipes("ev", "annealed_copper_cable", miExporter);
        generateMiCableRecipes("superconductor", "superconductor_cable", miExporter);
    }

    private void generateMiCableRecipes(String cableName, String miCable, Consumer<FinishedRecipe> exporter) {
        String mdCableItemId = MdId.of(cableName + "_cable").toString();
        String miCableItemId = "modern_industrialization:" + miCable;

        exporter.accept(new JsonFinishedRecipe(RecipeSerializer.SHAPELESS_RECIPE, "cable/%s_from_mi".formatted(cableName), """
                {
                    "ingredients": [
                        {
                            "item": "%s"
                        }
                    ],
                    "result": {
                        "item": "%s",
                        "count": 4
                    }
                }
                """.formatted(miCableItemId, mdCableItemId)));

        exporter.accept(new JsonFinishedRecipe(RecipeSerializer.SHAPED_RECIPE, "cable/%s_to_mi".formatted(cableName), """
                {
                    "pattern": [ "cc", "cc" ],
                    "key": {
                        "c": {
                            "item": "%s"
                        }
                    },
                    "result": {
                        "item": "%s"
                    }
                }
                """.formatted(mdCableItemId, miCableItemId)));
    }
}
