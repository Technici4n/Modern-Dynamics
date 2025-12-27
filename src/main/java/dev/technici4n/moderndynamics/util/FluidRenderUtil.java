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
package dev.technici4n.moderndynamics.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public final class FluidRenderUtil {
    private FluidRenderUtil() {
    }

    public static TextureAtlasSprite getStillSprite(FluidResource variant) {
        if (variant.isEmpty()) {
            return null;
        }

        var renderProps = IClientFluidTypeExtensions.of(variant.getFluid());
        var stack = variant.toStack(1);
        var texture = renderProps.getStillTexture(stack);
        if (texture == null) {
            return null;
        }

        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
    }

    public static int getTint(FluidResource variant) {
        var renderProps = IClientFluidTypeExtensions.of(variant.getFluid());
        var stack = variant.toStack(1);
        return renderProps.getTintColor(stack);
    }

    public static List<Component> getTooltip(FluidResource resource) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(resource.toStack(1).getHoverName());

        var modId = BuiltInRegistries.FLUID.getKey(resource.getFluid()).getNamespace();

        // Heuristic: If the last line doesn't include the modname, add it ourselves
        var modName = formatModName(modId);
        if (tooltip.isEmpty() || !tooltip.get(tooltip.size() - 1).getString().equals(modName)) {
            tooltip.add(Component.literal(modName));
        }

        return tooltip;
    }

    private static String formatModName(String modId) {
        return "" + ChatFormatting.BLUE + ChatFormatting.ITALIC + getModName(modId);
    }

    private static String getModName(String modId) {
        return ModList.get().getModContainerById(modId).map(mc -> mc.getModInfo().getDisplayName())
                .orElse(modId);
    }
}
