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
package dev.technici4n.moderndynamics.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.technici4n.moderndynamics.gui.menu.FluidAttachedIoMenu;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

public class FluidAttachedIoScreen extends AttachedIoScreen<FluidAttachedIoMenu> {
    public FluidAttachedIoScreen(FluidAttachedIoMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        this.imageHeight = 204;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (getMenu().getCarried().isEmpty() && this.hoveredSlot instanceof FluidConfigSlot fluidSlot) {
            var variant = fluidSlot.getFilter();
            if (!variant.isBlank()) {
                renderTooltip(poseStack, FluidVariantRendering.getTooltip(variant), Optional.empty(), mouseX, mouseY);
            }
        } else {
            renderTooltip(poseStack, mouseX, mouseY);
        }
    }

    public static void drawFluidInGui(PoseStack ms, FluidVariant fluid, int i, int j) {
        drawFluidInGui(ms, fluid, i, j, 16, 1);
        RenderSystem.enableDepthTest();
    }

    public static void drawFluidInGui(PoseStack ms, FluidVariant fluid, float i, float j, int scale, float fractionUp) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        int color = FluidVariantRendering.getColor(fluid);

        if (sprite == null)
            return;

        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        float x0 = i;
        float y0 = j;
        float x1 = x0 + scale;
        float y1 = y0 + scale * fractionUp;
        float z = 0.5f;
        float u0 = sprite.getU0();
        float v1 = sprite.getV1();
        float v0 = v1 + (sprite.getV0() - v1) * fractionUp;
        float u1 = sprite.getU1();

        Matrix4f model = ms.last().pose();
        bufferBuilder.vertex(model, x0, y1, z).color(r, g, b, 1).uv(u0, v1).endVertex();
        bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, 1).uv(u1, v1).endVertex();
        bufferBuilder.vertex(model, x1, y0, z).color(r, g, b, 1).uv(u1, v0).endVertex();
        bufferBuilder.vertex(model, x0, y0, z).color(r, g, b, 1).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.enableDepthTest();
    }
}
