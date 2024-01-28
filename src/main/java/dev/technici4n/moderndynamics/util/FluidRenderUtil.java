package dev.technici4n.moderndynamics.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public final class FluidRenderUtil {
    private FluidRenderUtil() {
    }

    public static TextureAtlasSprite getStillSprite(FluidVariant variant) {
        if (variant.isBlank()) {
            return null;
        }

        var renderProps = IClientFluidTypeExtensions.of(variant.getFluid());
        var stack = variant.toStack(1);
        var texture = renderProps.getStillTexture(stack);
        if (texture == null) {
            return null;
        }

        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        return atlas.apply(texture);
    }

    public static int getTint(FluidVariant variant) {
        var renderProps = IClientFluidTypeExtensions.of(variant.getFluid());
        var stack = variant.toStack(1);
        return renderProps.getTintColor(stack);
    }
}
