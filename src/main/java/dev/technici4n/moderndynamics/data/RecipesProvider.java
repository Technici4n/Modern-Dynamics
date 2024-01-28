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

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class RecipesProvider extends RecipeProvider {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private final PackOutput packOutput;

    public RecipesProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
        this.packOutput = packOutput;
    }

    @Override
    protected void buildRecipes(RecipeOutput exporter) {
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
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        generateMiCableRecipes(output);

        return super.run(output);
    }

    private void generateMiCableRecipes(CachedOutput output) {
        generateMiCableRecipes("lv", "tin_cable", output);
        generateMiCableRecipes("mv", "electrum_cable", output);
        generateMiCableRecipes("hv", "aluminum_cable", output);
        generateMiCableRecipes("ev", "annealed_copper_cable", output);
        generateMiCableRecipes("superconductor", "superconductor_cable", output);
    }

    private void generateMiCableRecipes(String cableName, String miCable, CachedOutput output) {
        String mdCableItemId = MdId.of(cableName + "_cable").toString();
        String miCableItemId = "modern_industrialization:" + miCable;
        var condition = new ModLoadedCondition("modern_industrialization");
        writeRawRecipe(output, MdId.of("cable/%s_from_mi".formatted(cableName)), Map.of(
                "type", getRecipeTypeId(RecipeSerializer.SHAPELESS_RECIPE),
                "ingredients", List.of(
                        Map.of(
                                "item", miCableItemId)),
                "result", Map.of(
                        "item", mdCableItemId,
                        "count", 4)),
                condition);

        writeRawRecipe(output, MdId.of("cable/%s_to_mi".formatted(cableName)), Map.of(
                "type", getRecipeTypeId(RecipeSerializer.SHAPED_RECIPE),
                "pattern", List.of("cc", "cc"),
                "key", Map.of(
                        "c", Map.of("item", mdCableItemId)),
                "result", Map.of(
                        "item", miCableItemId)),
                condition);
    }

    private void writeRawRecipe(CachedOutput output, ResourceLocation id, Map<String, Object> recipe, ICondition... conditions) {
        var path = recipePathProvider.json(id);
        var outputFile = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(path);

        var jsonObject = (JsonObject) GSON.toJsonTree(recipe);
        ICondition.writeConditions(JsonOps.INSTANCE, jsonObject, Arrays.asList(conditions));

        var content = GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);

        try {
            output.writeIfNeeded(outputFile, content, HashCode.fromBytes(content));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String getRecipeTypeId(RecipeSerializer<?> serializer) {
        var serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
        Objects.requireNonNull(serializerId, "Serializer " + serializer + " is unregistered");
        return serializerId.toString();
    }
}
