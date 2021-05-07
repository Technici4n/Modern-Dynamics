package dev.technici4n.moderntransportation.api.network;

import dev.technici4n.moderntransportation.impl.network.NetworkManager;

import java.util.List;

/**
 * Cache for a given network.
 * It will be rebuilt every time the nodes in the network change, and should not store information that must be persisted.
 */
public interface INetworkCache<H extends INodeHost> {
    /**
     * Called at the end of the server tick.
     * Make sure to only take nodes {@link INetworkNode#isTicking() that are ticking} into account.
     */
    void tick();

    static <H extends INodeHost, C extends INetworkCache<H>> void register(Class<C> cacheClass, Factory<H, C> factory) {
        NetworkManager.registerCacheClass(cacheClass, factory);
    }

    interface Factory<H extends INodeHost, C extends INetworkCache<H>> {
        C build(List<? extends INetworkNode<H, C>> nodes);
    }
}
