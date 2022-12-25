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
import dev.technici4n.moderndynamics.ModernDynamics;
import dev.technici4n.moderndynamics.util.MdId;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.item.Item;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class AttachmentUpgradesLoader extends SinglePreparationResourceReloader<List<JsonObject>> implements IdentifiableResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // A bit dirty... could maybe use a better fabric API hook?
    private static final Map<ResourceManager, LoadedUpgrades> LOADED_UPGRADES = new WeakHashMap<>();

    private AttachmentUpgradesLoader() {
    }

    @Override
    public Identifier getFabricId() {
        return MdId.of("attachment_upgrades_loader");
    }

    @Override
    protected List<JsonObject> prepare(ResourceManager resourceManager, Profiler profiler) {
        List<JsonObject> result = new ArrayList<>();

        for (var entry : resourceManager.findResources("attachment_upgrades", s -> s.getPath().endsWith(".json")).entrySet()) {
            var resource = entry.getValue();
            try (var inputStream = resource.getInputStream();
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
    protected void apply(List<JsonObject> array, ResourceManager resourceManager, Profiler profiler) {
        Map<Item, UpgradeType> map = new IdentityHashMap<>();
        List<Item> list = new ArrayList<>();

        for (JsonObject obj : array) {
            if (!ResourceConditions.objectMatchesConditions(obj)) {
                continue;
            }

            try {
                var item = JsonHelper.getItem(obj, "item");
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
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AttachmentUpgradesLoader());
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LoadedUpgrades.trySet(LOADED_UPGRADES.remove(server.getResourceManager()));
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                LoadedUpgrades.trySet(LOADED_UPGRADES.remove(resourceManager));
                // TODO: should maybe invalidate all cached filters?
            }
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            LoadedUpgrades.trySet(LOADED_UPGRADES.remove(player.server.getResourceManager()));
            LoadedUpgrades.syncToClient(player);
        });
    }
}
