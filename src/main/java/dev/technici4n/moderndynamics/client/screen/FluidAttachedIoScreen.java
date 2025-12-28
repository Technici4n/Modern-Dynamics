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

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.technici4n.moderndynamics.gui.menu.FluidAttachedIoMenu;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.util.FluidRenderUtil;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidAttachedIoScreen extends AttachedIoScreen<FluidAttachedIoMenu> {
    public static final RenderPipeline GUI_TEXTURED_NOBLEND = RenderPipelines.GUI_TEXTURED.toBuilder()
            .withoutBlend()
            .withLocation(MdId.of("gui_textured_noblend"))
            .build();

    public FluidAttachedIoScreen(FluidAttachedIoMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component, 176, 204);
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (getMenu().getCarried().isEmpty() && this.hoveredSlot instanceof FluidConfigSlot fluidSlot) {
            var resource = fluidSlot.getFilter();
            if (!resource.isEmpty()) {
                guiGraphics.setTooltipForNextFrame(font, FluidRenderUtil.getTooltip(resource), Optional.empty(), mouseX, mouseY);
            }
        } else {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    public static void drawFluidInGui(GuiGraphics guiGraphics, FluidResource fluid, int x, int y) {
        drawFluidInGui(guiGraphics, fluid, x, y, 16, 1);
    }

    public static void drawFluidInGui(GuiGraphics guiGraphics, FluidResource fluid, int x, int y, int scale, float fractionUp) {
        TextureAtlasSprite sprite = FluidRenderUtil.getStillSprite(fluid);
        int color = FluidRenderUtil.getTint(fluid);

        if (sprite == null)
            return;

        var x0 = x;
        var y0 = y;
        var x1 = x0 + scale;
        var y1 = Math.round(y0 + scale * fractionUp);
        float u0 = sprite.getU0();
        float v1 = sprite.getV1();
        float v0 = v1 + (sprite.getV0() - v1) * fractionUp;
        float u1 = sprite.getU1();

        guiGraphics.innerBlit(
                GUI_TEXTURED_NOBLEND,
                sprite.atlasLocation(),
                x0, x1,
                y0, y1,
                u0, u1,
                v0, v1,
                color);
    }
}
