package dev.technici4n.moderntransportation.network;

import java.util.List;

public class Network<H extends NodeHost, C extends NetworkCache<H, C>> {
    final List<NetworkNode<H, C>> nodes;
    C cache;

    public Network(List<NetworkNode<H, C>> nodes) {
        this.nodes = nodes;
    }
}
