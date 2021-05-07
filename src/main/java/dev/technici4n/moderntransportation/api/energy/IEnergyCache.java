package dev.technici4n.moderntransportation.api.energy;

import dev.technici4n.moderntransportation.api.network.INetworkCache;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IEnergyCache extends INetworkCache<IEnergyHost> {
}
