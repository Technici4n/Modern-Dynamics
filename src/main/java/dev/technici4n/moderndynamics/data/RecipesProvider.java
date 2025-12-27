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

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.internal.NeoForgeRecipeProvider;

public class RecipesProvider extends RecipeProvider {


    public RecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.TRANSPORTATION, MdItems.ITEM_PIPE, 8)
                .pattern("igi")
                .define('i', Items.IRON_INGOT)
                .define('g', Items.GLASS)
                .unlockedBy("has_ingot", has(Items.IRON_INGOT))
                .save(output);
        shaped(RecipeCategory.TRANSPORTATION, MdItems.FLUID_PIPE, 8)
                .pattern("igi")
                .define('i', Items.COPPER_INGOT)
                .define('g', Items.GLASS)
                .unlockedBy("has_ingot", has(Items.COPPER_INGOT))
                .save(output);

        shaped(RecipeCategory.TRANSPORTATION, MdItems.INHIBITOR, 4)
                .pattern("mnm")
                .define('m', Items.IRON_INGOT)
                .define('n', Items.IRON_NUGGET)
                .unlockedBy("has_item_pipe", has(MdItems.ITEM_PIPE))
                .save(output);

        shaped(RecipeCategory.TOOLS, MdItems.WRENCH)
                .pattern(" i ")
                .pattern("lii")
                .pattern("il ")
                .define('i', Items.IRON_INGOT)
                .define('l', Items.LAPIS_LAZULI)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.TRANSPORTATION, MdItems.ATTRACTOR, 1)
                .pattern(" e ")
                .pattern("mrm")
                .pattern(" p ")
                .define('e', Items.ENDER_PEARL)
                .define('m', Items.IRON_INGOT)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(output);

        shaped(RecipeCategory.TRANSPORTATION, MdItems.EXTRACTOR, 1)
                .pattern(" h ")
                .pattern("mrm")
                .pattern(" p ")
                .define('h', Items.HOPPER)
                .define('m', Items.IRON_INGOT)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_hopper", has(Items.HOPPER))
                .save(output);

        shaped(RecipeCategory.TRANSPORTATION, MdItems.FILTER, 1)
                .pattern(" l ")
                .pattern("mrm")
                .pattern(" p ")
                .define('l', Items.LAPIS_LAZULI)
                .define('m', Items.IRON_INGOT)
                .define('r', Items.REDSTONE)
                .define('p', Items.PAPER)
                .unlockedBy("has_lapis", has(Items.LAPIS_LAZULI))
                .save(output);

        shaped(RecipeCategory.TRANSPORTATION, MdItems.MACHINE_EXTENDER, 4)
                .pattern("bbb")
                .pattern("ibf")
                .pattern("bbb")
                .define('b', Items.IRON_BARS)
                .define('i', MdItems.ITEM_PIPE)
                .define('f', MdItems.FLUID_PIPE)
                .unlockedBy("has_item_pipe", has(MdItems.ITEM_PIPE))
                .unlockedBy("has_fluid_pipe", has(MdItems.FLUID_PIPE))
                .save(output);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider lookupProvider, RecipeOutput output) {
            return new RecipesProvider(lookupProvider, output);
        }

        @Override
        public String getName() {
            return "NeoForge recipes";
        }
    }
}
