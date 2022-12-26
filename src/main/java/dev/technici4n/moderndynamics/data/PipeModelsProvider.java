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
import dev.technici4n.moderndynamics.attachment.RenderedAttachment;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.pipe.PipeBlock;
import dev.technici4n.moderndynamics.util.MdId;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

public class PipeModelsProvider implements DataProvider {
    private final FabricDataOutput gen;

    public PipeModelsProvider(FabricDataOutput gen) {
        this.gen = gen;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {

        return CompletableFuture.allOf(registerPipeModels(cache), registerAttachments(cache));
    }

    private CompletableFuture<?> registerPipeModels(CachedOutput cache) {

        CompletableFuture<?> registerItemPipe = registerPipeModel(cache, MdBlocks.ITEM_PIPE, true);
        CompletableFuture<?> registerFluidPipe = registerPipeModel(cache, MdBlocks.FLUID_PIPE, true);
        return CompletableFuture.allOf(registerItemPipe, registerFluidPipe);
        /*
         * registerPipeModel(cache, MdBlocks.BASIC_ITEM_PIPE_OPAQUE, "base/item/basic_opaque", "connector/tin", false);
         * registerPipeModel(cache, MdBlocks.FAST_ITEM_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.FAST_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_ITEM_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_FAST_ITEM_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE, "lead", "connection_lead");
         * 
         * registerPipeModel(cache, MdBlocks.BASIC_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.FAST_FLUID_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.FAST_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_FLUID_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_FAST_FLUID_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE, "lead", "connection_lead");
         * 
         * registerPipeModel(cache, MdBlocks.BASIC_ENERGY_PIPE, "base/energy/lead", "connector/lead");
         * registerPipeModel(cache, MdBlocks.IMPROVED_ENERGY_PIPE, "base/energy/invar", "connector/invar");
         * registerPipeModel(cache, MdBlocks.ADVANCED_ENERGY_PIPE, "base/energy/electrum", "connector/electrum");
         * 
         * registerPipeModel(cache, MdBlocks.EMPTY_REINFORCED_ENERGY_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.EMPTY_SIGNALUM_ENERGY_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.EMPTY_RESONANT_ENERGY_PIPE, "lead", "connection_lead");
         * registerPipeModel(cache, MdBlocks.EMPTY_SUPERCONDUCTING_PIPE, "lead", "connection_lead");
         */
    }

    private CompletableFuture<?> registerPipeModel(CachedOutput cache, PipeBlock pipe, boolean transparent) {
        var baseFolder = gen.getOutputFolder().resolve("assets/%s/models/pipe/%s".formatted(gen.getModId(), pipe.id));

        var connectorModelFuture = registerPipePart(cache, baseFolder, pipe, "connector", transparent);
        var straightModelFuture = registerPipePart(cache, baseFolder, pipe, "straight", transparent);
        return CompletableFuture.allOf(connectorModelFuture, straightModelFuture);
    }

    /**
     * Register a simple textures pipe part model, and return the id of the model.
     */
    private CompletableFuture<?> registerPipePart(CachedOutput cache, Path baseFolder, PipeBlock pipe, String kind, boolean transparentSuffix) {
        var obj = new JsonObject();
        obj.addProperty("parent", MdId.of("base/%s%s".formatted(kind, transparentSuffix ? "_transparent" : "")).toString());
        var textures = new JsonObject();
        obj.add("textures", textures);
        textures.addProperty("0", MdId.of("pipe/%s/%s".formatted(pipe.id, kind)).toString());

        return DataProvider.saveStable(cache, obj, baseFolder.resolve(kind + ".json"));
    }

    private CompletableFuture<?> registerAttachments(CachedOutput cache) {
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        // Register each model.
        for (var attachment : RenderedAttachment.getAllAttachments()) {
            futures.add(registerAttachment(cache, attachment, "attachment/" + attachment.id.toLowerCase(Locale.ROOT)));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * Register a simple attachment part model, and return the id of the model.
     */
    private CompletableFuture<?> registerAttachment(CachedOutput cache, RenderedAttachment attachment, String texture) {
        var obj = new JsonObject();
        obj.addProperty("parent", MdId.of("base/connector_transparent").toString());
        var textures = new JsonObject();
        obj.add("textures", textures);
        textures.addProperty("0", MdId.of(texture).toString());

        return DataProvider.saveStable(cache, obj,
                gen.getOutputFolder().resolve("assets/%s/models/attachment/%s.json".formatted(gen.getModId(), attachment.id)));
    }

    @Override
    public String getName() {
        return "Pipe Models";
    }
}
