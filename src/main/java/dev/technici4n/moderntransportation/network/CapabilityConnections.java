package dev.technici4n.moderntransportation.network;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CapabilityConnections<T> {
    private final Capability<T> capability;
    private final NodeHost host;
    private int connections = 0;

    public CapabilityConnections(Capability<T> capability, NodeHost host) {
        this.capability = capability;
        this.host = host;
    }

    public void gatherCapabilities(@Nullable List<T> out) {
        int oldConnections = connections;

        for (int i = 0; i < 6; ++i) {
            if ((connections & (1 << i)) > 0) {
                Direction dir = Direction.byId(i);
                @SuppressWarnings("ConstantConditions")
                BlockEntity adjacentBe = host.pipe.getWorld().getBlockEntity(host.pipe.getPos().offset(dir));
                T adjacentCap = null;

                if (adjacentBe != null) {
                    adjacentCap = adjacentBe.getCapability(capability, dir.getOpposite()).orElse(null);
                }

                if (adjacentCap != null) {
                    if (out != null) {
                        out.add(adjacentCap);
                    }
                } else {
                    // Remove the direction from the bitmask
                    connections ^= 1 << i;
                }
            }
        }

        if (oldConnections != connections) {
            host.pipe.sync();
        }
    }

    public byte getConnectionMask() {
        return (byte) connections;
    }

    public void updateConnections() {
        // Store old connections
        int oldConnections = connections;

        // Compute new connections
        connections = (1 << 6) - 1;
        gatherCapabilities(null);

        // Update render
        if (oldConnections != connections) {
            host.pipe.sync();
        }
    }
}
