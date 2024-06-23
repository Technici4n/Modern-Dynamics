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
package dev.technici4n.moderndynamics.util;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemVariantImpl implements ItemVariant {
    private static final Map<Item, ItemVariant> noTagCache = new ConcurrentHashMap<>();

    public static ItemVariant of(Item item) {
        Objects.requireNonNull(item, "Item may not be null.");

        return noTagCache.computeIfAbsent(item, i -> new ItemVariantImpl(new ItemStack(i)));
    }

    public static ItemVariant of(ItemStack stack) {
        Objects.requireNonNull(stack);

        // Only tag-less or empty item variants are cached for now.
        if (stack.isComponentsPatchEmpty() || stack.isEmpty()) {
            return of(stack.getItem());
        } else {
            return new ItemVariantImpl(stack);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1/item");

    private final ItemStack stack;
    private final int hashCode;

    private ItemVariantImpl(ItemStack stack) {
        this.stack = stack.copyWithCount(1); // defensive copy
        hashCode = ItemStack.hashItemAndComponents(stack);
    }

    @Override
    public Item getObject() {
        return stack.getItem();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return this.stack.getComponentsPatch();
    }

    @Override
    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(this.stack, stack);
    }

    @Override
    public ItemStack toStack(int count) {
        return this.stack.copyWithCount(count);
    }

    @Override
    public int getMaxStackSize() {
        return this.stack.getMaxStackSize();
    }

    @Override
    public boolean isBlank() {
        return this.stack.isEmpty();
    }

    @Override
    public String toString() {
        return "ItemVariant{stack=" + stack + '}';
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ItemVariantImpl itemVariant = (ItemVariantImpl) o;
        // fail fast with hash code
        return hashCode == itemVariant.hashCode && matches(itemVariant.stack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
