package dev.technici4n.moderntransportation.network.item;

import dev.technici4n.moderntransportation.network.NetworkCache;
import dev.technici4n.moderntransportation.network.NetworkNode;

import java.util.List;

public class ItemCache extends NetworkCache<ItemHost, ItemCache> {
    public ItemCache(List<NetworkNode<ItemHost, ItemCache>> networkNodes) {
        super(networkNodes);
    }

    @Override
    protected void doTick() {

    }
}
