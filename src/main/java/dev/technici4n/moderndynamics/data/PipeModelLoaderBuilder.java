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

import com.google.gson.JsonObject;
import dev.technici4n.moderndynamics.client.model.PipeModelLoader;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PipeModelLoaderBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private String pipeType;
    private boolean transparent;

    public PipeModelLoaderBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(PipeModelLoader.ID, parent, existingFileHelper, false);
    }

    public PipeModelLoaderBuilder<T> pipeType(String pipeType) {
        this.pipeType = pipeType;
        return this;
    }

    public PipeModelLoaderBuilder<T> transparent(boolean transparent) {
        this.transparent = transparent;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        json.addProperty("pipeType", pipeType);
        json.addProperty("transparent", transparent);
        return json;
    }
}
