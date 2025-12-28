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

import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class FilteringFluidHandler extends DelegatingResourceHandler<FluidResource> {
    public FilteringFluidHandler(ResourceHandler<FluidResource> delegate) {
        super(delegate);
    }

    public FilteringFluidHandler(Supplier<ResourceHandler<FluidResource>> delegate) {
        super(delegate);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext tx) {
        if (!canInsert(resource)) {
            return 0;
        }
        return super.insert(resource, amount, tx);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (!canInsert(resource)) {
            return 0;
        }
        return super.insert(index, resource, amount, tx);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext tx) {
        if (!canExtract(resource)) {
            return 0;
        }
        return super.extract(resource, amount, tx);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (!canExtract(resource)) {
            return 0;
        }
        return super.extract(index, resource, amount, tx);
    }

    protected abstract boolean canInsert(FluidResource resource);

    protected abstract boolean canExtract(FluidResource resource);
}
