/*
 * Modern Transportation
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
package dev.technici4n.moderntransportation.model;

import com.google.gson.JsonParser;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.util.MtId;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Allows us to load our custom jsons.
 */
public final class MtModelLoader {
    public static void init() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(VariantProvider::new);

        for (var pipe : MtBlocks.ALL_PIPES) {
            ALL_PIPES.add(pipe.id);
        }
    }

    private static Set<String> ALL_PIPES = new HashSet<>();

    private static class VariantProvider implements ModelVariantProvider {
        private final ResourceManager resourceManager;

        private VariantProvider(ResourceManager resourceManager) {
            this.resourceManager = resourceManager;
        }

        @Override
        @Nullable
        public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
            if (!modelId.getNamespace().equals(MtId.MOD_ID)) {
                return null;
            }

            var path = modelId.getPath();
            if (ALL_PIPES.contains(path)) {
                // This is a pipe, try to load its json model.
                try (var resource = this.resourceManager.getResource(MtId.of("models/" + path + "/main.json"))) {
                    var obj = JsonParser.parseReader(new InputStreamReader(resource.getInputStream())).getAsJsonObject();
                    return new PipeUnbakedModel(
                            new Identifier(JsonHelper.getString(obj, "connection_none")),
                            new Identifier(JsonHelper.getString(obj, "connection_pipe")),
                            new Identifier(JsonHelper.getString(obj, "connection_inventory")));
                } catch (IOException exception) {
                    throw new ModelProviderException("Failed to find pipe model json for pipe " + modelId);
                } catch (RuntimeException runtimeException) {
                    throw new ModelProviderException("Failed to load pipe model json for pipe " + modelId, runtimeException);
                }
            }

            return null;
        }
    }
}
