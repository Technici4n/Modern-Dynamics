package dev.technici4n.moderntransportation.impl.network;

import dev.technici4n.moderntransportation.api.network.INetworkCache;
import dev.technici4n.moderntransportation.api.network.INodeHost;

import java.util.List;

public class Network<H extends INodeHost, C extends INetworkCache<H>> {
    final List<NetworkNode<H, C>> nodes;
    C cache;

    public Network(List<NetworkNode<H, C>> nodes) {
        this.nodes = nodes;
    }
}
