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

import dev.technici4n.moderndynamics.MdBlock;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class LootTablesProvider extends BlockLootSubProvider {
    public static LootTableProvider create(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(
                pOutput,
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(LootTablesProvider::new, LootContextParamSets.BLOCK)),
                registries);
    }

    protected LootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlagSet.of(), registries);
    }

    @Override
    protected void generate() {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        for (var block : BuiltInRegistries.BLOCK) {
            if (block instanceof MdBlock) {
                output.accept(block.getLootTable(), createSingleItemTable(block));
            }
        }
    }
}
