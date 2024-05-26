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
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable association of a still fluid and an optional NBT tag.
 *
 * <p>
 * Do not extend this class. Use {@link #of(Fluid)} and {@link #of(Fluid, NbtCompound)} to create instances.
 *
 * <p>
 * {@link aztech.modern_industrialization.thirdparty.fabrictransfer.api.client.fluid.FluidVariantRendering} can be used for client-side rendering of
 * fluid variants.
 *
 * <p>
 * <b>Fluid variants must always be compared with {@code equals}, never by reference!</b>
 * {@code hashCode} is guaranteed to be correct and constant time independently of the size of the NBT.
 */
@ApiStatus.NonExtendable
public interface FluidVariant extends TransferVariant<Fluid> {
    /**
     * Retrieve a blank FluidVariant.
     */
    static FluidVariant blank() {
        return of(Fluids.EMPTY);
    }

    /**
     * Retrieve an ItemVariant with the item and tag of a stack.
     */
    static FluidVariant of(FluidStack stack) {
        return of(stack.getFluid(), stack.getTag());
    }

    /**
     * Retrieve a FluidVariant with a fluid, and a {@code null} tag.
     *
     * <p>
     * The flowing and still variations of {@linkplain net.minecraft.fluid.FlowableFluid flowable fluids}
     * are normalized to always refer to the still variant. For example,
     * {@code FluidVariant.of(Fluids.FLOWING_WATER).getFluid() == Fluids.WATER}.
     */
    static FluidVariant of(Fluid fluid) {
        return of(fluid, null);
    }

    /**
     * Retrieve a FluidVariant with a fluid, and an optional tag.
     *
     * <p>
     * The flowing and still variations of {@linkplain net.minecraft.fluid.FlowableFluid flowable fluids}
     * are normalized to always refer to the still fluid. For example,
     * {@code FluidVariant.of(Fluids.FLOWING_WATER, nbt).getFluid() == Fluids.WATER}.
     */
    static FluidVariant of(Fluid fluid, @Nullable CompoundTag nbt) {
        return FluidVariantImpl.of(fluid, nbt);
    }

    /**
     * Return the fluid of this variant.
     */
    default Fluid getFluid() {
        return getObject();
    }

    /**
     * Create a new fluid stack from this variant.
     */
    default FluidStack toStack(int count) {
        if (isBlank())
            return FluidStack.EMPTY;
        FluidStack stack = new FluidStack(getFluid(), count);
        stack.setTag(copyNbt());
        return stack;
    }

    /**
     * Deserialize a variant from an NBT compound tag, assuming it was serialized using {@link #toNbt}.
     *
     * <p>
     * If an error occurs during deserialization, it will be logged with the DEBUG level, and a blank variant will be returned.
     */
    static FluidVariant fromNbt(CompoundTag nbt) {
        return FluidVariantImpl.fromNbt(nbt);
    }

    /**
     * Read a variant from a packet byte buffer, assuming it was serialized using {@link #toPacket}.
     */
    static FluidVariant fromPacket(FriendlyByteBuf buf) {
        return FluidVariantImpl.fromPacket(buf);
    }

    // Not in MI variants:
    default List<Component> getTooltip() {
        var tooltip = new ArrayList<Component>();
        tooltip.add(toStack(1).getDisplayName());

        var modId = BuiltInRegistries.FLUID.getKey(getFluid()).getNamespace();

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

    default boolean matches(FluidStack stack) {
        return getFluid() == stack.getFluid() && Objects.equals(getNbt(), stack.getTag());
    }
}
