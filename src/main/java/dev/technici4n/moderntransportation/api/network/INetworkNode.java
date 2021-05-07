package dev.technici4n.moderntransportation.api.network;

import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Single node in a network.
 */
@ApiStatus.NonExtendable
public interface INetworkNode<H extends INodeHost, C extends INetworkCache<H>> {
    /**
     * Return the connections of this node. Never modify the returned list!
     */
    List<Connection<H, C>> getConnections();

    /**
     * Return the host of this node.
     */
    H getHost();

    /**
     * Return the current network cache of this node.
     */
    C getNetworkCache();

    /**
     * Return {@code true} if this node is in a ticking chunk, false otherwise.
     */
    boolean isTicking();

    final class Connection<H extends INodeHost, C extends INetworkCache<H>> {
        public final Direction direction;
        public final INetworkNode<H, C> target;

        public Connection(Direction direction, INetworkNode<H, C> target) {
            this.direction = direction;
            this.target = target;
        }
    }
}
