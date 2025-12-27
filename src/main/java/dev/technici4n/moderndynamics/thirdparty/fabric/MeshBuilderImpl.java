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

/**
 * Our implementation of {@link MeshBuilder}, used for static mesh creation and baking. Not much to it - mainly it just
 * needs to grow the int[] array as quads are appended and maintain/provide a properly-configured
 * {@link MutableQuadView} instance. All the encoding and other work is handled in the quad base classes. The one
 * interesting bit is in {@link Maker#emit()}.
 */
public class MeshBuilderImpl implements MeshBuilder {
    int[] data = new int[256];
    private final Maker maker = new Maker();
    int index = 0;
    int limit = data.length;

    protected void ensureCapacity(int stride) {
        if (stride > limit - index) {
            limit *= 2;
            final int[] bigger = new int[limit];
            System.arraycopy(data, 0, bigger, 0, index);
            data = bigger;
            maker.data = bigger;
        }
    }

    @Override
    public Mesh build() {
        final int[] packed = new int[index];
        System.arraycopy(data, 0, packed, 0, index);
        index = 0;
        maker.begin(data, index);
        return new MeshImpl(packed);
    }

    @Override
    public QuadEmitter getEmitter() {
        ensureCapacity(EncodingFormat.TOTAL_STRIDE);
        maker.begin(data, index);
        return maker;
    }

    /**
     * Our base classes are used differently so we define final encoding steps in subtypes. This will be a static mesh
     * used at render time so we want to capture all geometry now and apply non-location-dependent lighting.
     */
    private class Maker extends MutableQuadViewImpl implements QuadEmitter {
        @Override
        public Maker emit() {
            computeGeometry();
            index += EncodingFormat.TOTAL_STRIDE;
            ensureCapacity(EncodingFormat.TOTAL_STRIDE);
            baseIndex = index;
            clear();
            return this;
        }
    }
}
