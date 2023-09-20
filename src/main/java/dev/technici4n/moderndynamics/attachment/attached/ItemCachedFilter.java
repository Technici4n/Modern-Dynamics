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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public final class ItemCachedFilter {
    private final Set<ItemVariant> listedVariants;
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

    public ItemCachedFilter(List<ItemVariant> filterConfig,
            FilterInversionMode filterInversion,
            FilterDamageMode filterDamage,
            FilterNbtMode filterNbt,
            FilterModMode filterMod) {
        this.filterInversion = filterInversion;
        this.filterDamage = filterDamage;
        this.filterNbt = filterNbt;
        this.filterMod = filterMod;

        // Dedupe and drop blanks
        this.listedVariants = new HashSet<>(filterConfig.size());
        this.listedItems = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var variant : filterConfig) {
            if (!variant.isBlank()) {
                this.listedVariants.add(variant);
                this.listedItems.add(variant.getItem());
            }
        }
    }

    private boolean isItemListed(ItemVariant variant) {
        // Return value if the variant is included
        boolean itemIsListed = false;

        // When inclusion of all listed mods is enabled, matching by individual item/NBT/damage is pointless
        if (filterMod == FilterModMode.INCLUDE_ALL_OF_MOD) {
            if (getListedMods().contains(getModId(variant))) {
                itemIsListed = true;
            }
        } else {
            if (filterNbt == FilterNbtMode.RESPECT_NBT) {
                itemIsListed = listedVariants.contains(variant);
            } else {
                itemIsListed = listedItems.contains(variant.getItem());
            }

            // Possibly handle matching damage too
        }

        // The "ore dictionary" search could treat an otherwise unlisted item as listed based on its tags
        if (!itemIsListed) {

        }

        return itemIsListed;
    }

    public boolean matchesItem(ItemVariant variant) {
        return isItemListed(variant) == (filterInversion == FilterInversionMode.WHITELIST);
    }

    private Set<String> getListedMods() {
        if (listedMods == null) {
            listedMods = new HashSet<>();
            for (var variant : listedVariants) {
                listedMods.add(getModId(variant));
            }
        }

        return listedMods;
    }

    public boolean matchesFluid(FluidVariant variant) {
        return false;
    }

    private static String getModId(ItemVariant variant) {
        // This returns "minecraft" if the item is unregistered
        return BuiltInRegistries.ITEM.getKey(variant.getItem()).getNamespace();
    }

    private static String getModId(FluidVariant variant) {
        // This returns "minecraft" if the fluid is unregistered
        return BuiltInRegistries.FLUID.getKey(variant.getFluid()).getNamespace();
    }

    private static boolean matchDamageIgnoreRest(@Nullable CompoundTag a, @Nullable CompoundTag b) {
        return getDamage(a) == getDamage(b);
    }

    private static boolean matchIgnoringDamage(@Nullable CompoundTag a, @Nullable CompoundTag b) {
        if (a == b) {
            return true;
        }

        throw new NotImplementedException();

    }

    private static int getDamage(CompoundTag tag) {
        return tag == null ? 0 : tag.getInt(ItemStack.TAG_DAMAGE);
    }

    @FunctionalInterface
    interface NbtMatcher {
        boolean matches(@Nullable CompoundTag a, @Nullable CompoundTag b);
    }
}
