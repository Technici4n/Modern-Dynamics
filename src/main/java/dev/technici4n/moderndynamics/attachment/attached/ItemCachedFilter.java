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
package dev.technici4n.moderndynamics.attachment.attached;

import dev.technici4n.moderndynamics.attachment.settings.FilterDamageMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterModMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterNbtMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class ItemCachedFilter {
    private final Set<ItemResource> listedResources;
    private final Set<Item> listedItems;
    private final FilterInversionMode filterInversion;
    private final FilterDamageMode filterDamage;
    private final FilterNbtMode filterNbt;
    private final FilterModMode filterMod;

    /**
     * Lists mod IDs in case mod-id based filtering is enabled.
     * This supersedes/replaces filtering by explicit items or fluids.
     * It is always null otherwise.
     */
    @Nullable
    private Set<String> listedMods;

    public ItemCachedFilter(List<ItemResource> filterConfig,
            FilterInversionMode filterInversion,
            FilterDamageMode filterDamage,
            FilterNbtMode filterNbt,
            FilterModMode filterMod) {
        this.filterInversion = filterInversion;
        this.filterDamage = filterDamage;
        this.filterNbt = filterNbt;
        this.filterMod = filterMod;

        // Dedupe and drop blanks
        this.listedResources = new HashSet<>(filterConfig.size());
        this.listedItems = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var resource : filterConfig) {
            if (!resource.isEmpty()) {
                this.listedResources.add(resource);
                this.listedItems.add(resource.getItem());
            }
        }
    }

    private boolean isItemListed(ItemResource resource) {
        // Return value if the resource is included
        boolean itemIsListed = false;

        // When inclusion of all listed mods is enabled, matching by individual item/NBT/damage is pointless
        if (filterMod == FilterModMode.INCLUDE_ALL_OF_MOD) {
            if (getListedMods().contains(getModId(resource))) {
                itemIsListed = true;
            }
        } else {
            if (filterNbt == FilterNbtMode.RESPECT_NBT) {
                itemIsListed = listedResources.contains(resource);
            } else {
                itemIsListed = listedItems.contains(resource.getItem());
            }

            // Possibly handle matching damage too
        }

        // The "ore dictionary" search could treat an otherwise unlisted item as listed based on its tags
        if (!itemIsListed) {

        }

        return itemIsListed;
    }

    public boolean matchesItem(ItemResource resource) {
        return isItemListed(resource) == (filterInversion == FilterInversionMode.WHITELIST);
    }

    private Set<String> getListedMods() {
        if (listedMods == null) {
            listedMods = new HashSet<>();
            for (var resource : listedResources) {
                listedMods.add(getModId(resource));
            }
        }

        return listedMods;
    }

    public boolean matchesFluid(FluidResource resource) {
        return false;
    }

    private static String getModId(ItemResource resource) {
        // This returns "minecraft" if the item is unregistered
        return BuiltInRegistries.ITEM.getKey(resource.getItem()).getNamespace();
    }

    private static String getModId(FluidResource resource) {
        // This returns "minecraft" if the item is unregistered
        return BuiltInRegistries.FLUID.getKey(resource.getFluid()).getNamespace();
    }

    @FunctionalInterface
    interface NbtMatcher {
        boolean matches(@Nullable CompoundTag a, @Nullable CompoundTag b);
    }
}
