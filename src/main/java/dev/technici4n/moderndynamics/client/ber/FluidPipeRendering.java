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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.thirdparty.fabric.MeshBuilderImpl;
import dev.technici4n.moderndynamics.thirdparty.fabric.MutableQuadView;
import dev.technici4n.moderndynamics.thirdparty.fabric.QuadEmitter;
import dev.technici4n.moderndynamics.util.FluidRenderUtil;
import dev.technici4n.moderndynamics.util.FluidVariant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class FluidPipeRendering {
    private static final float PIPE_W = 6.0F / 16.0F;
    private static final float P0 = 0f;
    private static final float P1 = P0 + (1 - PIPE_W) / 2;
    private static final float P2 = 0.5f;
    private static final float P3 = P1 + PIPE_W;
    private static final float P4 = 1f;
    public static final int FULL_LIGHT = 0x00F0_00F0;

    private static final int DOWN = 1 << 0;
    private static final int UP = 1 << 1;
    private static final int NORTH = 1 << 2;
    private static final int SOUTH = 1 << 3;
    private static final int WEST = 1 << 4;
    private static final int EAST = 1 << 5;

    public static void drawFluidInPipe(PipeBlockEntity pipe, PoseStack ms, MultiBufferSource vcp, FluidVariant fluid, float fill) {
        int conn = pipe.getClientSideConnections();
        var level = pipe.getLevel();
        var pos = pipe.getBlockPos();

        VertexConsumer vc = vcp.getBuffer(RenderType.translucent());

        var renderProps = IClientFluidTypeExtensions.of(fluid.getFluid());
        var sprite = FluidRenderUtil.getStillSprite(fluid);
        if (sprite == null || fill < 1e-5) {
            return;
        }

        int color = renderProps.getTintColor(fluid.fluid().defaultFluidState(), level, pos);
        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;

        var meshBuilder = new MeshBuilderImpl();
        QuadBuilder builder = (direction, x, y, z, X, Y, Z) -> {
            var emitter = meshBuilder.getEmitter();
            quad(emitter, direction, x, y, z, X, Y, Z);
            emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.color(-1, -1, -1, -1);
            vc.putBulkData(ms.last(), emitter.toBakedQuad(sprite), r, g, b, FULL_LIGHT, OverlayTexture.NO_OVERLAY);
        };

        /*
         * var emitter = renderer.meshBuilder().getEmitter();
         * emitter.square(Direction.UP, 0, 0, 1, 1, 0);
         * emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
         * emitter.spriteColor(0, -1, -1, -1, -1);
         * vc.putBulkData(ms.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, FULL_LIGHT, OverlayTexture.NO_OVERLAY);
         */

        float F = fill * PIPE_W;
        float E = 1e-3f;
        // builder.cube(0, 0, 0, 1, 1, 1, 0);
        if (hasConnection(conn, NORTH)) {
            builder.cube(P1 + E, P1 + E, P0, P3 - E, P1 + F - E, P1 + E, NORTH | SOUTH);
        }
        if (hasConnection(conn, SOUTH)) {
            builder.cube(P1 + E, P1 + E, P3 - E, P3 - E, P1 + F - E, P4, NORTH | SOUTH);
        }
        if (hasConnection(conn, EAST)) {
            builder.cube(P3 - E, P1 + E, P1 + E, P4, P1 + F - E, P3 - E, WEST | EAST);
        }
        if (hasConnection(conn, WEST)) {
            builder.cube(P0, P1 + E, P1 + E, P1 + E, P1 + F - E, P3 - E, WEST | EAST);
        }
        if (conn == (DOWN | UP)) {
            // vertical only
            builder.cube(P2 - F / 2 + E, P0, P2 - F / 2 + E, P2 + F / 2 - E, P4, P2 + F / 2 - E, DOWN | UP);
        } else {
            // normal center
            builder.cube(P1 + E, P1 + E, P1 + E, P3 - E, P1 + F - E, P3 - E, conn & (NORTH | SOUTH | WEST | EAST));
            if (hasConnection(conn, UP)) {
                builder.cube(P2 - F / 2 + E, P1 + F - E, P2 - F / 2 + E, P2 + F / 2 - E, P4, P2 + F / 2 - E, DOWN | UP);
            }
            if (hasConnection(conn, DOWN)) {
                builder.cube(P2 - F / 2 + E, P0, P2 - F / 2 + E, P2 + F / 2 - E, P1 + E, P2 + F / 2 - E, DOWN | UP);
            }
        }
    }

    private static boolean hasConnection(int mask, int dir) {
        return (mask & dir) > 0;
    }

    private interface QuadBuilder {
        void build(Direction direction, float x, float y, float z, float X, float Y, float Z);

        default void cube(float x, float y, float z, float X, float Y, float Z, int excludedMask) {
            int mask = 0b111111 ^ excludedMask;
            if ((mask & DOWN) > 0)
                build(Direction.DOWN, x, y, z, X, y, Z);
            if ((mask & UP) > 0)
                build(Direction.UP, x, Y, z, X, Y, Z);
            if ((mask & NORTH) > 0)
                build(Direction.NORTH, x, y, z, X, Y, z);
            if ((mask & SOUTH) > 0)
                build(Direction.SOUTH, x, y, Z, X, Y, Z);
            if ((mask & EAST) > 0)
                build(Direction.EAST, X, y, z, X, Y, Z);
            if ((mask & WEST) > 0)
                build(Direction.WEST, x, y, z, x, Y, Z);
        }
    }

    private static void quad(QuadEmitter emitter, Direction direction, float x, float y, float z, float X, float Y, float Z) {
        if (direction == Direction.UP)
            quad(emitter, Direction.UP, x, 1 - Z, X, 1 - z, 1 - Y);
        else if (direction == Direction.DOWN)
            quad(emitter, Direction.DOWN, x, z, X, Z, y);
        else if (direction == Direction.NORTH)
            quad(emitter, Direction.NORTH, 1 - X, y, 1 - x, Y, z);
        else if (direction == Direction.EAST)
            quad(emitter, Direction.EAST, 1 - Z, y, 1 - z, Y, 1 - X);
        else if (direction == Direction.SOUTH)
            quad(emitter, Direction.SOUTH, x, y, X, Y, 1 - Z);
        else
            quad(emitter, Direction.WEST, z, y, Z, Y, x);
    }

    /**
     * Add a quad, BUT DON'T EMIT.
     */
    protected static void quad(QuadEmitter emitter, Direction direction, float left, float bottom, float right, float top, float depth) {
        emitter.square(direction, left, bottom, right, top, depth);
    }
}
