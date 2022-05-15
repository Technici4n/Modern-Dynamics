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
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class RecipesProvider extends FabricRecipeProvider {
    public RecipesProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> exporter) {
        ShapedRecipeBuilder.shaped(MdItems.ITEM_PIPE, 8)
                .pattern("igi")
                .define('i', Items.IRON_INGOT)
                .define('g', Items.GLASS)
                .unlockedBy("has_ingot", has(Items.IRON_INGOT))
                .save(exporter);
        ShapedRecipeBuilder.shaped(MdItems.FLUID_PIPE, 8)
                .pattern("igi")
                .define('i', Items.COPPER_INGOT)
                .define('g', Items.GLASS)
                .unlockedBy("has_ingot", has(Items.COPPER_INGOT))
                .save(exporter);

        ShapedRecipeBuilder.shaped(MdItems.INHIBITOR, 4)
                .pattern("mnm")
                .define('m', Items.IRON_INGOT)
                .define('n', Items.IRON_NUGGET)
                .unlockedBy("has_item_pipe", has(MdItems.ITEM_PIPE))
                .save(exporter);

        ShapedRecipeBuilder.shaped(MdItems.WRENCH)
                .pattern(" i ")
                .pattern("lii")
                .pattern("il ")
                .define('i', Items.IRON_INGOT)
                .define('l', Items.LAPIS_LAZULI)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(exporter);

        servo(MdItems.IRON_SERVO, Items.IRON_INGOT).save(exporter);
        servo(MdItems.GOLD_SERVO, Items.GOLD_INGOT).save(exporter);
        servo(MdItems.DIAMOND_SERVO, Items.DIAMOND).save(exporter);

        retriever(MdItems.IRON_RETRIEVER, Items.IRON_INGOT).save(exporter);
        retriever(MdItems.GOLD_RETRIEVER, Items.GOLD_INGOT).save(exporter);
        retriever(MdItems.DIAMOND_RETRIEVER, Items.DIAMOND).save(exporter);

        filter(MdItems.IRON_FILTER, Items.IRON_INGOT).save(exporter);
        filter(MdItems.GOLD_FILTER, Items.GOLD_INGOT).save(exporter);
        filter(MdItems.DIAMOND_FILTER, Items.DIAMOND).save(exporter);
    }

    protected ShapedRecipeBuilder servo(Item servo, ItemLike material) {
        return ShapedRecipeBuilder.shaped(servo, 4)
                .pattern(" h ")
                .pattern("mrm")
                .pattern(" p ")
                .define('h', Items.HOPPER)
                .define('m', material)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_hopper", has(Items.HOPPER));
    }

    protected ShapedRecipeBuilder retriever(Item retriever, ItemLike material) {
        return ShapedRecipeBuilder.shaped(retriever, 4)
                .pattern(" e ")
                .pattern("mrm")
                .pattern(" p ")
                .define('e', Items.ENDER_PEARL)
                .define('m', material)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE));
    }

    protected ShapedRecipeBuilder filter(Item filter, ItemLike material) {
        return ShapedRecipeBuilder.shaped(filter, 4)
                .pattern(" p ")
                .pattern("mrm")
                .pattern(" p ")
                .define('m', material)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_redstone", has(Items.REDSTONE));
    }
}
