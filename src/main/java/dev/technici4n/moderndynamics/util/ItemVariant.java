package dev.technici4n.moderndynamics.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ItemVariant(Item item, @Nullable CompoundTag nbt) {
    public ItemVariant(ItemLike item) {
        this(item.asItem(), null);
    }

    public static ItemVariant blank() {
        return new ItemVariant(Items.AIR, null);
    }

    public static ItemVariant fromNbt(CompoundTag filterTag) {
        throw new UnsupportedOperationException();
    }

    public static ItemVariant fromPacket(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public static ItemVariant of(ItemStack stack) {
        return new ItemVariant(stack.getItem(), stack.getTag() != null ? stack.getTag().copy() : null);
    }

    public static ItemVariant of(ItemLike item, @Nullable CompoundTag nbt) {
        return new ItemVariant(item.asItem(), nbt);
    }


    public CompoundTag toNbt() {
        throw new UnsupportedOperationException();
    }

    public boolean isBlank() {
        return item.equals(Items.AIR);
    }

    public Item getItem() {
        return item();
    }

    public ItemStack toStack(int amount) {
        return new ItemStack(item, amount, nbt != null ? nbt.copy() : null);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public boolean matches(ItemStack stack) {
        return this.item == stack.getItem() && Objects.equals(this.nbt, stack.getTag());
    }
}
