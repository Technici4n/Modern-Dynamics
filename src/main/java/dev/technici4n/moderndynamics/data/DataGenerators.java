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

import dev.technici4n.moderndynamics.util.MdId;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = MdId.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void onGatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.lootTable(LootTablesProvider.entry())
                .recipe(RecipesProvider::new);
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        // Models
        event.createProvider(MdModelProvider.create(MdId.MOD_ID, ModelsProvider::new)::create);

        event.createProvider(PipeModelsProvider::new);
        event.createProvider(SpriteSourceProvider::new);

        event.createProvider(AttachmentUpgradesProvider::new);
        event.createProvider(ItemTagsProvider::new);
        event.createProvider(MiRecipeGenerator::new);

        event.createProvider(EmptyTestStructureGenerator::new);
    }
}
