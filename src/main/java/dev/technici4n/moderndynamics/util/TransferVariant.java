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
import org.jetbrains.annotations.Nullable;

/**
 * An immutable association of an immutable object instance (for example {@code Item} or {@code Fluid}) and an optional NBT tag.
 *
 * <p>
 * This is exposed for convenience for code that needs to be generic across multiple transfer variants,
 * but note that a {@link Storage} is not necessarily bound to {@code TransferVariant}. Its generic parameter can be any immutable object.
 *
 * <p>
 * <b>Transfer variants must always be compared with {@code equals}, never by reference!</b>
 * {@code hashCode} is guaranteed to be correct and constant time independently of the size of the NBT.
 *
 * @param <O> The type of the immutable object instance, for example {@code Item} or {@code Fluid}.
 */
public interface TransferVariant<O> {
    /**
     * Return true if this variant is blank, and false otherwise.
     */
    boolean isBlank();

    /**
     * Return the immutable object instance of this variant.
     */
    O getObject();

    /**
     * Return the underlying tag.
     *
     * <p>
     * <b>NEVER MUTATE THIS NBT TAG</b>, if you need to mutate it you can use {@link #copyNbt()} to retrieve a copy instead.
     */
    @Nullable
    CompoundTag getNbt();

    /**
     * Return true if the tag of this variant matches the passed tag, and false otherwise.
     *
     * <p>
     * Note: True is returned if both tags are {@code null}.
     */
    default boolean nbtMatches(@Nullable CompoundTag other) {
        return Objects.equals(getNbt(), other);
    }

    /**
     * Return {@code true} if the object of this variant matches the passed fluid.
     */
    default boolean isOf(O object) {
        return getObject() == object;
    }

    /**
     * Return a copy of the tag of this variant, or {@code null} if this variant doesn't have a tag.
     *
     * <p>
     * Note: Use {@link #nbtMatches} if you only need to check for custom tag equality, or {@link #getNbt()} if you don't need to mutate the tag.
     */
    @Nullable
    default CompoundTag copyNbt() {
        CompoundTag nbt = getNbt();
        return nbt == null ? null : nbt.copy();
    }

    /**
     * Save this variant into an NBT compound tag. Subinterfaces should have a matching static {@code fromNbt}.
     *
     * <p>
     * Note: This is safe to use for persisting data as objects are saved using their full Identifier.
     */
    CompoundTag toNbt();

    /**
     * Write this variant into a packet byte buffer. Subinterfaces should have a matching static {@code fromPacket}.
     *
     * <p>
     * Implementation note: Objects are saved using their raw registry integer id.
     */
    void toPacket(FriendlyByteBuf buf);
}
