package dev.technici4n.moderntransportation.api.item;

import dev.technici4n.moderntransportation.api.network.INetworkCache;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IItemCache extends INetworkCache<IItemHost> {
}
