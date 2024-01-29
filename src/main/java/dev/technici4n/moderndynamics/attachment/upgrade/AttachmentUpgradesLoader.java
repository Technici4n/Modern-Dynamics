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
package dev.technici4n.moderndynamics.attachment.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.technici4n.moderndynamics.ModernDynamics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class AttachmentUpgradesLoader extends SimplePreparableReloadListener<List<JsonObject>> {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // A bit dirty... could maybe use a better fabric API hook?
    private static final Map<ResourceManager, LoadedUpgrades> LOADED_UPGRADES = new WeakHashMap<>();

    private AttachmentUpgradesLoader() {
    }

    @Override
    protected List<JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<JsonObject> result = new ArrayList<>();

        for (var entry : resourceManager.listResources("attachment_upgrades", s -> s.getPath().endsWith(".json")).entrySet()) {
            var resource = entry.getValue();
            try (var inputStream = resource.open();
                    var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                result.add(JsonParser.parseReader(reader).getAsJsonObject());
            } catch (IOException | JsonParseException | IllegalStateException exception) { // getAsJsonObject can throw ISE
                ModernDynamics.LOGGER.error("Error when loading Modern Dynamics attachment upgrade with path %s".formatted(entry.getKey()),
                        exception);
            }
        }

        return result;
    }

    @Override
    protected void apply(List<JsonObject> array, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Item, UpgradeType> map = new IdentityHashMap<>();
        List<Item> list = new ArrayList<>();

        for (JsonObject obj : array) {
            if (!ICondition.conditionsMatched(JsonOps.INSTANCE, obj)) {
                continue;
            }

            try {
                var item = GsonHelper.getAsItem(obj, "item").value();
                var deserialized = GSON.fromJson(obj, UpgradeType.class);
                // TODO validate

                if (!map.containsKey(item)) {
                    list.add(item);
                }
                map.put(item, deserialized);
            } catch (Exception exception) {
                ModernDynamics.LOGGER.error("Failed to read attachment upgrade entry " + obj, exception);
            }
        }

        LOADED_UPGRADES.put(resourceManager, new LoadedUpgrades(map, list));
    }

    public static void setup() {
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, e -> {
            e.addListener(new AttachmentUpgradesLoader());
        });
        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, e -> {
            var server = e.getServer();
            LoadedUpgrades.trySet(LOADED_UPGRADES.remove(server.getResourceManager()));
        });
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, e -> {
            var server = ServerLifecycleHooks.getCurrentServer();
            var player = e.getPlayer();
            if (player != null) {
                LoadedUpgrades.trySet(LOADED_UPGRADES.remove(server.getResourceManager()));
                LoadedUpgrades.syncToClient(player);
            } else {
                LoadedUpgrades.trySet(LOADED_UPGRADES.remove(server.getResourceManager()));
                // TODO: should maybe invalidate all cached filters?
            }
        });
    }
}
