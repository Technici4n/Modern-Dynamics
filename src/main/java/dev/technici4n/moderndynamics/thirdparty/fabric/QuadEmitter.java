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

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Specialized {@link MutableQuadView} obtained via {@link MeshBuilder#getEmitter()} to append quads during mesh
 * building.
 *
 * <p>
 * Instances of {@link QuadEmitter} will practically always be threadlocal and/or reused - do not retain references.
 *
 * <p>
 * Only the renderer should implement or extend this interface.
 */
public interface QuadEmitter extends MutableQuadView {
    @Override
    QuadEmitter pos(int vertexIndex, float x, float y, float z);

    @Override
    default QuadEmitter pos(int vertexIndex, Vector3f pos) {
        MutableQuadView.super.pos(vertexIndex, pos);
        return this;
    }

    @Override
    QuadEmitter color(int vertexIndex, int color);

    @Override
    default QuadEmitter color(int c0, int c1, int c2, int c3) {
        MutableQuadView.super.color(c0, c1, c2, c3);
        return this;
    }

    @Override
    QuadEmitter uv(int vertexIndex, float u, float v);

    @Override
    default QuadEmitter uv(int vertexIndex, Vector2f uv) {
        MutableQuadView.super.uv(vertexIndex, uv);
        return this;
    }

    @Override
    QuadEmitter spriteBake(TextureAtlasSprite sprite, int bakeFlags);

    default QuadEmitter uvUnitSquare() {
        uv(0, 0, 0);
        uv(1, 0, 1);
        uv(2, 1, 1);
        uv(3, 1, 0);
        return this;
    }

    @Override
    QuadEmitter lightmap(int vertexIndex, int lightmap);

    @Override
    default QuadEmitter lightmap(int b0, int b1, int b2, int b3) {
        MutableQuadView.super.lightmap(b0, b1, b2, b3);
        return this;
    }

    @Override
    QuadEmitter normal(int vertexIndex, float x, float y, float z);

    @Override
    default QuadEmitter normal(int vertexIndex, Vector3f normal) {
        MutableQuadView.super.normal(vertexIndex, normal);
        return this;
    }

    @Override
    QuadEmitter cullFace(Direction face);

    @Override
    QuadEmitter nominalFace(Direction face);

    @Override
    QuadEmitter colorIndex(int colorIndex);

    @Override
    QuadEmitter tag(int tag);

    @Override
    QuadEmitter fromVanilla(int[] quadData, int startIndex);

    /**
     * Tolerance for determining if the depth parameter to {@link #square(Direction, float, float, float, float, float)}
     * is effectively zero - meaning the face is a cull face.
     */
    float CULL_FACE_EPSILON = 0.00001f;

    /**
     * Helper method to assign vertex coordinates for a square aligned with the given face. Ensures that vertex order is
     * consistent with vanilla convention. (Incorrect order can lead to bad AO lighting unless enhanced lighting logic
     * is available/enabled.)
     *
     * <p>
     * Square will be parallel to the given face and coplanar with the face (and culled if the face is occluded) if the
     * depth parameter is approximately zero. See {@link #CULL_FACE_EPSILON}.
     *
     * <p>
     * All coordinates should be normalized (0-1).
     */
    default QuadEmitter square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        if (Math.abs(depth) < CULL_FACE_EPSILON) {
            cullFace(nominalFace);
            depth = 0; // avoid any inconsistency for face quads
        } else {
            cullFace(null);
        }

        nominalFace(nominalFace);
        switch (nominalFace) {
        case UP:
            depth = 1 - depth;
            top = 1 - top;
            bottom = 1 - bottom;

        case DOWN:
            pos(0, left, depth, top);
            pos(1, left, depth, bottom);
            pos(2, right, depth, bottom);
            pos(3, right, depth, top);
            break;

        case EAST:
            depth = 1 - depth;
            left = 1 - left;
            right = 1 - right;

        case WEST:
            pos(0, depth, top, left);
            pos(1, depth, bottom, left);
            pos(2, depth, bottom, right);
            pos(3, depth, top, right);
            break;

        case SOUTH:
            depth = 1 - depth;
            left = 1 - left;
            right = 1 - right;

        case NORTH:
            pos(0, 1 - left, top, depth);
            pos(1, 1 - left, bottom, depth);
            pos(2, 1 - right, bottom, depth);
            pos(3, 1 - right, top, depth);
            break;
        }

        return this;
    }

    /**
     * In static mesh building, causes quad to be appended to the mesh being built. In a dynamic render context, create
     * a new quad to be output to rendering. In both cases, current instance is reset to default values.
     */
    QuadEmitter emit();
}
