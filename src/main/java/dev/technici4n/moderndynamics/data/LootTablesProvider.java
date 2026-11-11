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
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class LootTablesProvider extends BlockLootSubProvider {
    public static LootTableProvider.SubProviderEntry entry() {
        return new LootTableProvider.SubProviderEntry(LootTablesProvider::new, LootContextParamSets.BLOCK);
    }

    protected LootTablesProvider(LootTableSubProvider.Context output) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, output);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof MdBlock).toList();
    }

    @Override
    protected void generate() {
        for (var block : getKnownBlocks()) {
            dropSelf(block);
        }
    }
}
