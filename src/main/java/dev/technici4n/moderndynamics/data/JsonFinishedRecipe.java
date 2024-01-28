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

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class JsonFinishedRecipe implements Recipe<Container> {
    private final RecipeSerializer<?> serializer;
    private final String json;

    public JsonFinishedRecipe(RecipeSerializer<?> serializer, String json) {
        this.serializer = serializer;
        this.json = json;
    }

    @Override
    public boolean matches(Container container, Level level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        // JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        // jsonObject.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.getType()).toString());
        // return jsonObject;
//        return serializer;
throw new UnsupportedOperationException();

    }

    @Override
    public RecipeType<?> getType() {
        throw new UnsupportedOperationException();
    }
}
