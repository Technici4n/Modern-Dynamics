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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.technici4n.moderndynamics.util.MdId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public final class JsonFinishedRecipe implements FinishedRecipe {
    private final RecipeSerializer<?> serializer;
    private final String id;
    private final String json;

    public JsonFinishedRecipe(RecipeSerializer<?> serializer, String id, String json) {
        this.serializer = serializer;
        this.id = id;
        this.json = json;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonObject serializeRecipe() {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        jsonObject.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.getType()).toString());
        return jsonObject;
    }

    @Override
    public ResourceLocation getId() {
        return MdId.of(id);
    }

    @Override
    public RecipeSerializer<?> getType() {
        return serializer;
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}
