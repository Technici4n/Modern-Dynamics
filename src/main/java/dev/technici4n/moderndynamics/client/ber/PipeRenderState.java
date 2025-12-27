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
package dev.technici4n.moderndynamics.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class PipeRenderState extends BlockEntityRenderState {
    public TravelingItemState[] travelingItems = new TravelingItemState[0];
    public int clientSideConnections;
    public FluidState fluid = null;

    public static class FluidState {
        public int tintColor;
        public float fill;
        public TextureAtlasSprite sprite;
    }

    public static class TravelingItemState {
        public ItemStackRenderState itemStack = new ItemStackRenderState();
        public float rotAngle;
        public int renderCount;
        public float tx, ty, tz;
        public int randomSeed;
    }
}
