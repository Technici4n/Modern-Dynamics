package dev.technici4n.moderndynamics.client.compat.jei;

import dev.technici4n.moderndynamics.client.screen.AttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.FluidAttachedIoScreen;
import dev.technici4n.moderndynamics.client.screen.ItemAttachedIoScreen;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.gui.menu.ItemConfigSlot;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
class GhostIngredientHandler implements IGhostIngredientHandler<AttachedIoScreen> {
    @SuppressWarnings("unchecked")
    @Override
    public <I> List<Target<I>> getTargets(AttachedIoScreen gui, I ingredient, boolean doStart) {
        var targets = new ArrayList<Target<I>>();

        if (gui instanceof ItemAttachedIoScreen ioScreen && ingredient instanceof ItemStack) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof ItemConfigSlot slot && slot.isEnabled()) {
                    targets.add((Target<I>) new ItemSlotTarget(slot, ioScreen));
                }
            }
        }

        if (gui instanceof FluidAttachedIoScreen ioScreen && ingredient instanceof IJeiFluidIngredient) {
            for (var s : ioScreen.getMenu().slots) {
                if (s instanceof FluidConfigSlot slot && slot.isEnabled()) {
                    targets.add((Target<I>) new FluidSlotTarget(slot, ioScreen));
                }
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {
    }

    private static Rect2i getSlotBounds(Slot slot, AttachedIoScreen<?> screen) {
        return new Rect2i(slot.x + screen.getLeftPos(), slot.y + screen.getTopPos(), 16, 16);
    }

    private static class ItemSlotTarget implements IGhostIngredientHandler.Target<ItemStack> {
        private final ItemConfigSlot slot;
        private final ItemAttachedIoScreen ioScreen;

        public ItemSlotTarget(ItemConfigSlot slot, ItemAttachedIoScreen ioScreen) {
            this.slot = slot;
            this.ioScreen = ioScreen;
        }

        @Override
        public Rect2i getArea() {
            return getSlotBounds(slot, ioScreen);
        }

        @Override
        public void accept(ItemStack ingredient) {
            if (slot.isEnabled()) {
                var iv = ItemVariant.of(ingredient);
                ioScreen.getMenu().setFilter(slot.getConfigIdx(), iv, true);
            }
        }
    }

    private static class FluidSlotTarget implements IGhostIngredientHandler.Target<IJeiFluidIngredient> {
        private final FluidConfigSlot slot;
        private final FluidAttachedIoScreen ioScreen;

        public FluidSlotTarget(FluidConfigSlot slot, FluidAttachedIoScreen ioScreen) {
            this.slot = slot;
            this.ioScreen = ioScreen;
        }

        @Override
        public Rect2i getArea() {
            return getSlotBounds(slot, ioScreen);
        }

        @Override
        public void accept(IJeiFluidIngredient ingredient) {
            if (slot.isEnabled()) {
                var fv = FluidVariant.of(ingredient.getFluid(), ingredient.getTag().orElse(null));
                ioScreen.getMenu().setFilter(slot.getConfigIdx(), fv, true);
            }
        }
    }
}
