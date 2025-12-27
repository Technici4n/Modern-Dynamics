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

import java.util.function.Consumer;

/**
 * Implementation of {@link Mesh}. The way we encode meshes makes it very simple.
 */
public class MeshImpl implements Mesh {
    /** Used to satisfy external calls to {@link #forEach(Consumer)}. */
    ThreadLocal<QuadViewImpl> POOL = ThreadLocal.withInitial(QuadViewImpl::new);

    final int[] data;

    MeshImpl(int[] data) {
        this.data = data;
    }

    public int[] data() {
        return data;
    }

    @Override
    public void forEach(Consumer<QuadView> consumer) {
        forEach(consumer, POOL.get());
    }

    /**
     * The renderer will call this with it's own cursor to avoid the performance hit of a thread-local lookup. Also
     * means renderer can hold final references to quad buffers.
     */
    void forEach(Consumer<QuadView> consumer, QuadViewImpl cursor) {
        final int limit = data.length;
        int index = 0;

        while (index < limit) {
            cursor.load(data, index);
            consumer.accept(cursor);
            index += EncodingFormat.TOTAL_STRIDE;
        }
    }
}
