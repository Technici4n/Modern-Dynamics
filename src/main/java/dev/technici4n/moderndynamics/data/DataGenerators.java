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
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod.EventBusSubscriber(modid = MdId.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var existingFileHelper = event.getExistingFileHelper();
        var registries = event.getLookupProvider();
        var pack = event.getGenerator().getVanillaPack(true);

        pack.addProvider(packOutput -> new ModelsProvider(packOutput, existingFileHelper));
        pack.addProvider(PipeModelsProvider::new);
        pack.addProvider(packOutput -> new SpriteSourceProvider(packOutput, registries, existingFileHelper));

        pack.addProvider(AttachmentUpgradesProvider::new);
        pack.addProvider(packOutput -> new ItemTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(LootTablesProvider::create);
        pack.addProvider(packOutput -> new RecipesProvider(packOutput, registries));

        pack.addProvider(EmptyTestStructureGenerator::new);
    }
}
