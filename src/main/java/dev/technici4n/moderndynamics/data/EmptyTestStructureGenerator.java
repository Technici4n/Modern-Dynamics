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

import com.google.common.hash.HashCode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.technici4n.moderndynamics.util.MdId;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;

public class EmptyTestStructureGenerator implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public EmptyTestStructureGenerator(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        String structureIn;
        try (var in = getClass().getResourceAsStream("/data/moderndynamics/structure/empty.snbt")) {
            structureIn = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        CompoundTag structureTag;
        try {
            structureTag = NbtUtils.snbtToStructure(structureIn);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        var out = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(structureTag, new DataOutputStream(out));
            output.writeIfNeeded(
                    pathProvider.file(MdId.of("empty"), "nbt"),
                    out.toByteArray(),
                    HashCode.fromBytes(out.toByteArray()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Empty Test Structure";
    }
}
