package dev.technici4n.moderndynamics.network.item;

import dev.technici4n.moderndynamics.util.ItemVariant;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

final class InsertionOnlyItemHandler implements IItemHandler {
    private final InsertionHandler handler;

    public InsertionOnlyItemHandler(InsertionHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if  (stack.isEmpty()) {
            return stack;
        }

        var maxAmount = stack.getCount();
        var variant = ItemVariant.of(stack);
        var amountInserted = handler.handle(variant, maxAmount, simulate);
        if (amountInserted <= 0) {
            return stack;
        }
        var notInserted = maxAmount - amountInserted;
        if (notInserted > 0) {
            return variant.toStack(notInserted);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    @FunctionalInterface
    public interface InsertionHandler {
        int handle(ItemVariant resource, int maxAmount, boolean simulate);
    }
}
