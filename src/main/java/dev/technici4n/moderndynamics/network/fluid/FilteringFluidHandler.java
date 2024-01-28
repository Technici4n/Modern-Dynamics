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
package dev.technici4n.moderndynamics.network.fluid;

import dev.technici4n.moderndynamics.util.FluidVariant;
import java.util.function.Supplier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FilteringFluidHandler implements IFluidHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FilteringFluidHandler.class);
    private final Supplier<IFluidHandler> delegate;

    public FilteringFluidHandler(IFluidHandler delegate) {
        this.delegate = () -> delegate;
    }

    public FilteringFluidHandler(Supplier<IFluidHandler> delegate) {
        this.delegate = delegate;
    }

    private IFluidHandler getDelegate() {
        return delegate.get();
    }

    @Override
    public int getTanks() {
        return getDelegate().getTanks();
    }

    @Override
    @NotNull
    public FluidStack getFluidInTank(int tank) {
        return getDelegate().getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return getDelegate().getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return getDelegate().isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!canInsert(FluidVariant.of(resource))) {
            return 0;
        }

        return getDelegate().fill(resource, action);
    }

    @Override
    @NotNull
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!canExtract(FluidVariant.of(resource))) {
            return FluidStack.EMPTY;
        }

        return getDelegate().drain(resource, action);
    }

    @Override
    @NotNull
    public FluidStack drain(int maxDrain, FluidAction action) {
        var delegate = getDelegate();

        // Pre-flight check for EXECUTE
        FluidVariant simulatedDrain = FluidVariant.blank();
        if (action.execute()) {
            simulatedDrain = FluidVariant.of(delegate.drain(maxDrain, FluidAction.SIMULATE));
            if (!canExtract(simulatedDrain)) {
                return FluidStack.EMPTY;
            }
        }

        var drained = getDelegate().drain(maxDrain, action);
        var drainedVariant = FluidVariant.of(drained);
        if (!simulatedDrain.equals(drainedVariant) || !canExtract(drainedVariant)) {
            if (action.execute()) {
                // try to re-insert, otherwise it will be voided
                LOG.warn("{} returned fluid {} after returning {} during simulation...", delegate, drained, simulatedDrain);
                delegate.fill(drained, FluidAction.EXECUTE);
            }
            return FluidStack.EMPTY;
        }
        return drained;
    }

    protected abstract boolean canInsert(FluidVariant resource);

    protected abstract boolean canExtract(FluidVariant resource);
}
