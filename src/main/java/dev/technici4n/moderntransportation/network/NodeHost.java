package dev.technici4n.moderntransportation.network;

import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import dev.technici4n.moderntransportation.network.energy.EnergyCache;
import dev.technici4n.moderntransportation.network.energy.EnergyHost;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * A node host is what gives its behavior to a {@link NetworkNode}.
 */
public abstract class NodeHost {
    protected final PipeBlockEntity pipe;
    /**
     * Current connections to adjacent pipes.
     */
    public EnumSet<Direction> pipeConnections = EnumSet.noneOf(Direction.class);
    /**
     * True if the host needs an update.
     * The update is done by the network when the host is in a ticking chunk.
     */
    private boolean needsUpdate = true;

    protected NodeHost(PipeBlockEntity pipe) {
        this.pipe = pipe;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean isTicking() {
        return ((ServerWorld) pipe.getWorld()).getChunkManager().shouldTickBlock(pipe.getPos());
    }

    /**
     * Return the list of allowed directions for connections to adjacent nodes.
     */
    public final EnumSet<Direction> getAllowedNodeConnections() {
        return EnumSet.allOf(Direction.class);
    }

    /**
     * Set the list of current connections.
     */
    public final void setConnections(EnumSet<Direction> connections) {
        pipeConnections = connections;
        pipe.sync();
    }

    @SuppressWarnings("rawtypes")
    protected abstract NetworkManager getManager();

    @SuppressWarnings("unchecked")
    public void addSelf() {
        getManager().addNode((ServerWorld) pipe.getWorld(), pipe.getPos(), this);
    }

    @SuppressWarnings("unchecked")
    public void removeSelf() {
        getManager().removeNode((ServerWorld) pipe.getWorld(), pipe.getPos(), this);
    }

    @Nullable
    protected final NetworkNode<?, ?> findNode() {
        return getManager().findNode((ServerWorld) pipe.getWorld(), pipe.getPos());
    }

    public final void separateNetwork() {
        @Nullable
        NetworkNode<?, ?> node = findNode();

        if (node != null && node.getHost() == this) {
            node.getNetworkCache().separate();
        }
    }

    protected final void update() {
        if (needsUpdate) {
            needsUpdate = false;
            doUpdate();
        }
    }

    protected void doUpdate() {
    }

    /**
     * Schedule an update.
     */
    @SuppressWarnings({"rawtypes"})
    public final void scheduleUpdate() {
        if (!needsUpdate) {
            needsUpdate = true;
            @Nullable
            NetworkNode node = findNode();

            if (node != null) {
                node.getNetworkCache().scheduleHostUpdate(this);
            }
        }
    }

    public final boolean needsUpdate() {
        return needsUpdate;
    }
}
