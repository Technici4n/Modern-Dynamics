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
package dev.technici4n.moderntransportation.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.technici4n.moderntransportation.attachment.Attachment;
import dev.technici4n.moderntransportation.attachment.MtAttachments;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.pipe.PipeBlock;
import dev.technici4n.moderntransportation.util.MtId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataProvider;

public class PipeModelsProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final FabricDataGenerator gen;

    public PipeModelsProvider(FabricDataGenerator gen) {
        this.gen = gen;
    }

    @Override
    public void run(DataCache cache) throws IOException {
        registerPipeModels(cache);
        registerAttachments(cache);
    }

    private void registerPipeModels(DataCache cache) throws IOException {
        registerPipeModel(cache, MtBlocks.BASIC_ITEM_PIPE, "base/item/basic", "connector/tin");
        registerPipeModel(cache, MtBlocks.BASIC_ITEM_PIPE_OPAQUE, "base/item/basic_opaque", "connector/tin");
        registerPipeModel(cache, MtBlocks.FAST_ITEM_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.FAST_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_ITEM_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE, "lead", "connection_lead");

        registerPipeModel(cache, MtBlocks.BASIC_FLUID_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.BASIC_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.FAST_FLUID_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.FAST_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_FLUID_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE, "lead", "connection_lead");

        registerPipeModel(cache, MtBlocks.BASIC_ENERGY_PIPE, "base/energy/lead", "connector/lead");
        registerPipeModel(cache, MtBlocks.HARDENED_ENERGY_PIPE, "base/energy/invar", "connector/invar");
        registerPipeModel(cache, MtBlocks.REINFORCED_ENERGY_PIPE, "base/energy/electrum", "connector/electrum");
        registerPipeModel(cache, MtBlocks.SIGNALUM_ENERGY_PIPE, "base/energy/signalum", "connector/signalum");
        registerPipeModel(cache, MtBlocks.RESONANT_ENERGY_PIPE, "base/energy/enderium", "connector/enderium");
        registerPipeModel(cache, MtBlocks.SUPERCONDUCTING_PIPE, "lead", "connection_lead");

        registerPipeModel(cache, MtBlocks.EMPTY_REINFORCED_ENERGY_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.EMPTY_SIGNALUM_ENERGY_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.EMPTY_RESONANT_ENERGY_PIPE, "lead", "connection_lead");
        registerPipeModel(cache, MtBlocks.EMPTY_SUPERCONDUCTING_PIPE, "lead", "connection_lead");
    }

    private void registerPipeModel(DataCache cache, PipeBlock pipe, String texture, String connectionTexture) throws IOException {
        var baseFolder = gen.getOutput().resolve("assets/%s/models/%s".formatted(gen.getModId(), pipe.id));

        var noneModel = registerPipePart(cache, baseFolder, pipe, "none", texture);
        var inventoryModel = registerPipePart(cache, baseFolder, pipe, "inventory", connectionTexture);
        var pipeModel = registerPipePart(cache, baseFolder, pipe, "pipe", texture);

        var modelJson = new JsonObject();
        modelJson.addProperty("connection_none", noneModel);
        modelJson.addProperty("connection_inventory", inventoryModel);
        modelJson.addProperty("connection_pipe", pipeModel);
        DataProvider.writeToPath(GSON, cache, modelJson, baseFolder.resolve("main.json"));
    }

    /**
     * Register a simple textures pipe part model, and return the id of the model.
     */
    private String registerPipePart(DataCache cache, Path baseFolder, PipeBlock pipe, String kind, String texture) throws IOException {
        var obj = new JsonObject();
        obj.addProperty("parent", MtId.of("base/pipe_%s".formatted(kind)).toString());
        var textures = new JsonObject();
        obj.add("textures", textures);
        textures.addProperty("0", MtId.of(texture).toString());

        DataProvider.writeToPath(GSON, cache, obj, baseFolder.resolve(kind + ".json"));

        var id = "%s/%s".formatted(pipe.id, kind);
        return MtId.of(id).toString();
    }

    private void registerAttachments(DataCache cache) throws IOException {
        registerAttachment(cache, MtAttachments.FILTER, "attachment/filter_0");
        registerAttachment(cache, MtAttachments.SERVO, "attachment/servo_base_0_0");

        // Now register the base model json.
        var obj = new JsonObject();
        for (var attachment : Attachment.getAllAttachments()) {
            Path modelPath = gen.getOutput().resolve("assets/%s/models/attachments/%s.json".formatted(gen.getModId(), attachment.id));
            if (!Files.exists(modelPath)) {
                throw new RuntimeException("Missing attachment json file: " + modelPath);
            }
            obj.addProperty(attachment.id, MtId.of("attachments/%s".formatted(attachment.id)).toString());
        }
        DataProvider.writeToPath(GSON, cache, obj, gen.getOutput().resolve("assets/%s/models/attachments.json".formatted(gen.getModId())));
    }

    /**
     * Register a simple attachment part model, and return the id of the model.
     */
    private void registerAttachment(DataCache cache, Attachment attachment, String texture) throws IOException {
        var obj = new JsonObject();
        obj.addProperty("parent", MtId.of("base/pipe_inventory").toString());
        var textures = new JsonObject();
        obj.add("textures", textures);
        textures.addProperty("0", MtId.of(texture).toString());

        DataProvider.writeToPath(GSON, cache, obj,
                gen.getOutput().resolve("assets/%s/models/attachments/%s.json".formatted(gen.getModId(), attachment.id)));
    }

    @Override
    public String getName() {
        return "Pipe Models";
    }
}
