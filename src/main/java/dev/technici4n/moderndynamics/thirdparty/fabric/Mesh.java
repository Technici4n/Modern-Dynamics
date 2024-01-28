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
package dev.technici4n.moderndynamics.thirdparty.fabric;
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * A bundle of one or more {@link QuadView} instances encoded by the renderer.
 *
 * <p>
 * Similar in purpose to the {@code List<BakedQuad>} instances returned by BakedModel, but affords the renderer the
 * ability to optimize the format for performance and memory allocation.
 *
 * <p>
 * Only the renderer should implement or extend this interface.
 */
public interface Mesh {
    /**
     * Use to access all of the quads encoded in this mesh. The quad instances sent to the consumer will likely be
     * threadlocal/reused and should never be retained by the consumer.
     */
    void forEach(Consumer<QuadView> consumer);

    default Collection<BakedQuad> toBakedBlockQuads() {
        SpriteFinder finder = SpriteFinder
                .get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
        var result = new ArrayList<BakedQuad>();
        forEach(qv -> result.add(qv.toBakedQuad(finder.find(qv))));
        return result;
    }
}
