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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class PipeModelLoader implements IGeometryLoader<PipeUnbakedModel> {
    public static final ResourceLocation ID = MdId.of("pipe");
    private final Map<String, ResourceLocation> attachmentModels;

    public PipeModelLoader(Map<String, ResourceLocation> attachmentModels) {
        this.attachmentModels = attachmentModels;
    }

    @Override
    public PipeUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        var pipeType = GsonHelper.getAsString(jsonObject, "pipeType");
        var transparent = GsonHelper.getAsBoolean(jsonObject, "transparent");
        return new PipeUnbakedModel(pipeType, transparent, attachmentModels);
    }
}
