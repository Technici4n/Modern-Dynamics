package dev.technici4n.moderntransportation.api.network;

import net.minecraft.util.math.Direction;

import java.util.EnumSet;

public interface INodeHost {
    /**
     * Return the list of allowed directions for connections to adjacent nodes.
     */
    EnumSet<Direction> getAllowedNodeConnections();

    /**
     * Set the list of current connections.
     */
    void setConnections(EnumSet<Direction> connections);
}
