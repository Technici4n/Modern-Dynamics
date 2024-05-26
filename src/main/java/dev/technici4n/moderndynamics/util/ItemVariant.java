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

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable count-less ItemStack, i.e. an immutable association of an item and an optional NBT compound tag.
 *
 * <p>
 * Do not implement, use the static {@code of(...)} functions instead.
 */
@ApiStatus.NonExtendable
public interface ItemVariant extends TransferVariant<Item> {
    /**
     * Retrieve a blank ItemVariant.
     */
    static ItemVariant blank() {
        return of(Items.AIR);
    }

    /**
     * Retrieve an ItemVariant with the item and tag of a stack.
     */
    static ItemVariant of(ItemStack stack) {
        return of(stack.getItem(), stack.getTag(), stack.serializeAttachments());
    }

    /**
     * Retrieve an ItemVariant with an item and without a tag.
     */
    static ItemVariant of(ItemLike item) {
        return of(item, null, null);
    }

    /**
     * Retrieve an ItemVariant with an item and an optional tag.
     */
    static ItemVariant of(ItemLike item, @Nullable CompoundTag tag, @Nullable CompoundTag attachmentsTags) {
        return ItemVariantImpl.of(item.asItem(), tag, attachmentsTags);
    }

    /**
     * Return true if the item and tag of this variant match those of the passed stack, and false otherwise.
     */
    default boolean matches(ItemStack stack) {
        return isOf(stack.getItem()) && nbtMatches(stack.getTag()) && Objects.equals(getAttachments(), stack.serializeAttachments());
    }

    /**
     * Return the item of this variant.
     */
    default Item getItem() {
        return getObject();
    }

    @Nullable
    CompoundTag getAttachments();

    /**
     * Create a new item stack with count 1 from this variant.
     */
    default ItemStack toStack() {
        return toStack(1);
    }

    /**
     * Create a new item stack from this variant.
     *
     * @param count The count of the returned stack. It may lead to counts higher than maximum stack size.
     */
    default ItemStack toStack(int count) {
        if (isBlank())
            return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(getItem(), count, getAttachments());
        stack.setTag(copyNbt());
        return stack;
    }

    /**
     * Deserialize a variant from an NBT compound tag, assuming it was serialized using
     * {@link #toNbt}. If an error occurs during deserialization, it will be logged
     * with the DEBUG level, and a blank variant will be returned.
     */
    static ItemVariant fromNbt(CompoundTag nbt) {
        return ItemVariantImpl.fromNbt(nbt);
    }

    /**
     * Write a variant from a packet byte buffer, assuming it was serialized using
     * {@link #toPacket}.
     */
    static ItemVariant fromPacket(FriendlyByteBuf buf) {
        return ItemVariantImpl.fromPacket(buf);
    }
}
