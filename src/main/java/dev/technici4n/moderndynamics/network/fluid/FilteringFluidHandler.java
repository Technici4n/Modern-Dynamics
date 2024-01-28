package dev.technici4n.moderndynamics.network.fluid;

import dev.technici4n.moderndynamics.util.FluidVariant;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

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
