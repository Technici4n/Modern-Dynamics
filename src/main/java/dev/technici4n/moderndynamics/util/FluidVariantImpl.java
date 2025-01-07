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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidVariantImpl implements FluidVariant {
    private static final Map<Fluid, FluidVariant> noTagCache = new ConcurrentHashMap<>();

    private static Fluid normalizeFluid(Fluid fluid) {
        if (!fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
            // Note: the empty fluid is not still, that's why we check for it specifically.

            if (fluid instanceof FlowingFluid flowable) {
                // Normalize FlowableFluids to their still variants.
                return flowable.getSource();
            } else {
                // If not a FlowableFluid, we don't know how to convert -> crash.
                ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                throw new IllegalArgumentException("Cannot convert flowing fluid %s (%s) into a still fluid.".formatted(id, fluid));
            }
        }
        return fluid;
    }

    public static FluidVariant of(Fluid fluid) {
        Objects.requireNonNull(fluid, "Fluid may not be null.");

        return noTagCache.computeIfAbsent(normalizeFluid(fluid), f -> new FluidVariantImpl(new FluidStack(f, 1)));
    }

    public static FluidVariant of(FluidStack stack) {
        Objects.requireNonNull(stack);

        if (stack.isComponentsPatchEmpty() || stack.isEmpty()) {
            return of(stack.getFluid());
        } else {
            return new FluidVariantImpl(stack);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1/fluid");

    private final FluidStack stack;
    private final int hashCode;

    public FluidVariantImpl(FluidStack stack) {
        this.stack = stack.copyWithAmount(1); // defensive copy
        this.hashCode = FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public Fluid getObject() {
        return this.stack.getFluid();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return this.stack.getComponentsPatch();
    }

    @Override
    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(this.stack, stack);
    }

    @Override
    public FluidStack toStack(int count) {
        return this.stack.copyWithAmount(count);
    }

    @Override
    public boolean isBlank() {
        return this.stack.isEmpty();
    }

    @Override
    public String toString() {
        return "FluidVariant{stack=" + stack + '}';
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FluidVariantImpl fluidVariant = (FluidVariantImpl) o;
        // fail fast with hash code
        return hashCode == fluidVariant.hashCode && matches(fluidVariant.stack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
