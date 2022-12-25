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
package dev.technici4n.moderndynamics.client.model;

import dev.technici4n.moderndynamics.attachment.RenderedAttachment;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

/**
 * Allows us to load our custom jsons.
 */
public final class MdModelLoader {
    public static void init() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(VariantProvider::new);
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new ResourceProvider());

        for (var pipe : MdBlocks.ALL_PIPES) {
            ALL_PIPES.add(pipe.id);
        }
    }

    private static final Set<String> ALL_PIPES = new HashSet<>();

    private static class VariantProvider implements ModelVariantProvider {
        private final ResourceManager resourceManager;

        private VariantProvider(ResourceManager resourceManager) {
            this.resourceManager = resourceManager;
        }

        @Override
        @Nullable
        public UnbakedModel loadModelVariant(ModelResourceLocation modelId, ModelProviderContext context) throws ModelProviderException {
            if (!modelId.getNamespace().equals(MdId.MOD_ID)) {
                return null;
            }

            var path = modelId.getPath();

            if (ALL_PIPES.contains(path)) {
                return new PipeUnbakedModel(path);
            }

            return null;
        }
    }

    private static class ResourceProvider implements ModelResourceProvider {
        @Override
        @Nullable
        public UnbakedModel loadModelResource(ResourceLocation resourceId, ModelProviderContext context) {
            if (!resourceId.getNamespace().equals(MdId.MOD_ID)) {
                return null;
            }

            var path = resourceId.getPath();
            if (path.equals(AttachmentsUnbakedModel.ID.getPath())) {
                var modelMap = new HashMap<String, ResourceLocation>();
                for (var id : RenderedAttachment.getAttachmentIds()) {
                    modelMap.put(id, MdId.of("attachment/" + id));
                }
                return new AttachmentsUnbakedModel(modelMap);
            }

            return null;
        }
    }
}
