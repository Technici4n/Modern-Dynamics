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

import java.util.List;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class SpriteSourceProvider extends FabricCodecDataProvider<List<SpriteSource>> {
    public SpriteSourceProvider(FabricDataOutput packOutput) {
        super(packOutput, PackOutput.Target.RESOURCE_PACK, "atlases", SpriteSources.FILE_CODEC);
    }

    @Override
    protected void configure(BiConsumer<ResourceLocation, List<SpriteSource>> provider) {
        provider.accept(new ResourceLocation("minecraft", "blocks"),
                List.of(new DirectoryLister("pipe", "pipe/"), new DirectoryLister("attachment", "attachment/")));
    }

    @Override
    public String getName() {
        return "Sprite Sources";
    }
}
