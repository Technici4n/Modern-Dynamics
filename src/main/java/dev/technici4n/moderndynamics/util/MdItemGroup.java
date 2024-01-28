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

import dev.technici4n.moderndynamics.init.MdItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class MdItemGroup {
    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, MdId.of(MdId.MOD_ID));

    public static void init() {
        var tab = CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.moderndynamics.moderndynamics"))
                .icon(() -> new ItemStack(MdItems.ITEM_PIPE))
                .displayItems((params, output) -> {
                    for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                        if (MdId.MOD_ID.equals(entry.getKey().location().getNamespace()) && entry.getValue() != MdItems.DEBUG_TOOL) {
                            output.accept(entry.getValue().getDefaultInstance());
                        }
                    }
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, tab);
    }
}
