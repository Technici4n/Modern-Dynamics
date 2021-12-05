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
package dev.technici4n.moderntransportation.pipe;

import dev.technici4n.moderntransportation.network.item.ItemHost;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PipeBlockEntityRenderer implements BlockEntityRenderer<PipeBlockEntity> {
    private final BlockEntityRendererFactory.Context ctx;

    public PipeBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(PipeBlockEntity pipe, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        for (var host : pipe.getHosts()) {
            if (host instanceof ItemHost itemHost) {
                for (var item : itemHost.getClientTravelingItems()) {
                    matrices.push();

                    Vec3d from, to;
                    double ratio;

                    if (item.traveledDistance() <= 0.5) {
                        from = findFaceMiddle(item.in().getOpposite());
                        to = CENTER;
                        ratio = item.traveledDistance() * 2;
                    } else {
                        from = CENTER;
                        to = findFaceMiddle(item.out());
                        ratio = (item.traveledDistance() - 0.5) * 2;
                    }

                    matrices.translate(
                            to.getX() * ratio + from.getX() * (1 - ratio),
                            to.getY() * ratio + from.getY() * (1 - ratio),
                            to.getZ() * ratio + from.getZ() * (1 - ratio));
                    matrices.scale(0.6f, 0.6f, 0.6f);
                    matrices.translate(0, -0.2f, 0);

                    MinecraftClient.getInstance().getItemRenderer().renderItem(item.variant().toStack(), ModelTransformation.Mode.GROUND, light,
                            overlay, matrices, vertexConsumers, 0);

                    matrices.pop();
                }
            }
        }
    }

    private static final Vec3d CENTER = new Vec3d(0.5, 0.5, 0.5);

    private static Vec3d findFaceMiddle(Direction face) {
        return switch (face) {
        case DOWN -> new Vec3d(0.5, 0, 0.5);
        case UP -> new Vec3d(0.5, 1, 0.5);
        case NORTH -> new Vec3d(0.5, 0.5, 0);
        case SOUTH -> new Vec3d(0.5, 0.5, 1);
        case WEST -> new Vec3d(0, 0.5, 0.5);
        case EAST -> new Vec3d(1, 0.5, 0.5);
        };
    }
}
