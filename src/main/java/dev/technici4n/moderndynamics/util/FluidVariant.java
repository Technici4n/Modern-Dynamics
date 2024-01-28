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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FluidVariant {
    private static final Logger LOG = LoggerFactory.getLogger(FluidVariant.class);

    private final Fluid fluid;
    private final @Nullable CompoundTag nbt;
    private final int hashCode;

    private FluidVariant(Fluid fluid, @Nullable CompoundTag nbt) {
        this.fluid = fluid;
        this.nbt = nbt != null ? nbt.copy() : null; // defensive copy
        this.hashCode = Objects.hash(fluid, nbt);
    }

    public FluidVariant(Fluid fluid) {
        this(fluid, null);
    }

    public static FluidVariant blank() {
        return new FluidVariant(Fluids.EMPTY, null);
    }

    public static FluidVariant of(Fluid fluid, @Nullable CompoundTag nbt) {
        Objects.requireNonNull(fluid, "Fluid may not be null.");

        if (!fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
            // Note: the empty fluid is not still, that's why we check for it specifically.

            if (fluid instanceof FlowingFluid flowable) {
                // Normalize FlowableFluids to their still variants.
                fluid = flowable.getSource();
            } else {
                // If not a FlowableFluid, we don't know how to convert -> crash.
                ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                throw new IllegalArgumentException("Cannot convert flowing fluid %s (%s) into a still fluid.".formatted(id, fluid));
            }
        }

        if (nbt == null || fluid == Fluids.EMPTY) {
            // Use the cached variant inside the fluid
            return new FluidVariant(fluid, null); // TODO noTagCache.computeIfAbsent(fluid, f -> new FluidVariant(fluid, null));
        } else {
            // TODO explore caching fluid variants for non null tags.
            return new FluidVariant(fluid, nbt);
        }
    }

    public static FluidVariant of(Fluid fluid) {
        return of(fluid, null);
    }

    public static FluidVariant of(FluidStack resource) {
        return of(resource.getFluid(), resource.getTag());
    }

    public CompoundTag toNbt() {
        CompoundTag result = new CompoundTag();
        result.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());

        if (nbt != null) {
            result.put("tag", nbt.copy());
        }

        return result;
    }

    public static FluidVariant fromNbt(CompoundTag compound) {
        try {
            Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(compound.getString("fluid")));
            CompoundTag nbt = compound.contains("tag") ? compound.getCompound("tag") : null;
            return of(fluid, nbt);
        } catch (RuntimeException runtimeException) {
            LOG.debug("Tried to load an invalid FluidVariant from NBT: {}", compound, runtimeException);
            return FluidVariant.blank();
        }
    }

    public void toPacket(FriendlyByteBuf buf) {
        if (isBlank()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeVarInt(BuiltInRegistries.FLUID.getId(fluid));
            buf.writeNbt(nbt);
        }
    }

    public static FluidVariant fromPacket(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return FluidVariant.blank();
        } else {
            Fluid fluid = BuiltInRegistries.FLUID.byId(buf.readVarInt());
            CompoundTag nbt = buf.readNbt();
            return of(fluid, nbt);
        }
    }

    public boolean isBlank() {
        return fluid.isSame(Fluids.EMPTY);
    }

    public boolean matches(FluidStack stack) {
        return fluid == stack.getFluid() && Objects.equals(nbt, stack.getTag());
    }

    public Fluid getFluid() {
        return fluid();
    }

    public FluidStack toStack(int amount) {
        return new FluidStack(fluid, amount, nbt != null ? nbt.copy() : null);
    }

    public List<Component> getTooltip() {
        var tooltip = new ArrayList<Component>();
        tooltip.add(toStack(1).getDisplayName());

        var modId = BuiltInRegistries.FLUID.getKey(fluid).getNamespace();

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

    public @Nullable CompoundTag getNbt() {
        return nbt != null ? nbt.copy() : null;
    }

    public Fluid fluid() {
        return fluid;
    }

    public @Nullable CompoundTag nbt() {
        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (FluidVariant) obj;
        return hashCode == that.hashCode
                && Objects.equals(this.fluid, that.fluid)
                && Objects.equals(this.nbt, that.nbt);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "FluidVariant[" +
                "fluid=" + fluid + ", " +
                "nbt=" + nbt + ']';
    }

}
