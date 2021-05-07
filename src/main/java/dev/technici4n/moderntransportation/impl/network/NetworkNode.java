package dev.technici4n.moderntransportation.impl.network;

import dev.technici4n.moderntransportation.api.network.INetworkCache;
import dev.technici4n.moderntransportation.api.network.INetworkNode;
import dev.technici4n.moderntransportation.api.network.INodeHost;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class NetworkNode<H extends INodeHost, C extends INetworkCache<H>> implements INetworkNode<H, C> {
    private final H host;
    Network<H, C> network;
    private final List<Connection<H, C>> connections = new ArrayList<>();

    public NetworkNode(H host) {
        this.host = host;
    }

    @Override
    public List<Connection<H, C>> getConnections() {
        return connections;
    }

    @Override
    public H getHost() {
        return host;
    }

    @Override
    public C getNetworkCache() {
        return network.cache;
    }

    @Override
    public boolean isTicking() {
        return true; // FIXME: do the check
    }

    void addConnection(Direction direction, NetworkNode<H, C> target) {
        for (Connection<H, C> connection : connections) {
            if (connection.direction == direction) {
                throw new IllegalStateException("Connection already exists.");
            }
        }

        connections.add(new Connection<H, C>(direction, target));
        updateHostConnections();
    }

    void removeConnection(Direction direction, NetworkNode<H, C> target) {
        for (Iterator<Connection<H, C>> it = connections.iterator(); it.hasNext();) {
            Connection<H, C> connection = it.next();

            if (connection.direction == direction) {
                if (connection.target != target) {
                    throw new IllegalStateException("Target mismatch!");
                }

                it.remove();
                updateHostConnections();
                return;
            }
        }

        throw new IllegalStateException("Connection does not exist.");
    }

    void updateHostConnections() {
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);

        for (Connection<H, C> connection : this.connections) {
            connections.add(connection.direction);
        }

        host.setConnections(connections);
    }
}
