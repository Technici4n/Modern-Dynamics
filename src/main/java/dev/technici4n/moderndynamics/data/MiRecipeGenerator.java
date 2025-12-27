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
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class MiRecipeGenerator implements DataProvider {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final PackOutput packOutput;
    private final PackOutput.PathProvider recipePathProvider;

    public MiRecipeGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.packOutput = packOutput;
        this.recipePathProvider = this.packOutput.createRegistryElementsPathProvider(Registries.RECIPE);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        generateMiCableRecipes(cache);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "";
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
        writeRawRecipe(output, MdId.of("cable/%s_from_mi".formatted(cableName)), ImmutableMap.of(
                "type", getRecipeTypeId(RecipeSerializer.SHAPELESS_RECIPE),
                "ingredients", List.of(
                        ImmutableMap.of(
                                "item", miCableItemId)),
                "result", ImmutableMap.of(
                        "id", mdCableItemId,
                        "count", 4)),
                condition);

        writeRawRecipe(output, MdId.of("cable/%s_to_mi".formatted(cableName)), ImmutableMap.of(
                "type", getRecipeTypeId(RecipeSerializer.SHAPED_RECIPE),
                "pattern", List.of("cc", "cc"),
                "key", ImmutableMap.of(
                        "c", ImmutableMap.of("item", mdCableItemId)),
                "result", ImmutableMap.of(
                        "id", miCableItemId)),
                condition);
    }

    private void writeRawRecipe(CachedOutput output, Identifier id, Map<String, Object> recipe, ICondition... conditions) {
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
