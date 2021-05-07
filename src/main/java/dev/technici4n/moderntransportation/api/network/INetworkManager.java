package dev.technici4n.moderntransportation.api.network;

import dev.technici4n.moderntransportation.impl.network.NetworkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * The manager of all networks for a given cache class.
 */
@ApiStatus.NonExtendable
public interface INetworkManager<H extends INodeHost, C extends INetworkCache<H>> {
    void addNode(ServerWorld world, BlockPos pos, H host);
    void removeNode(ServerWorld world, BlockPos pos, H host);
    default void updateNode(ServerWorld world, BlockPos pos, H host) {
        removeNode(world, pos, host);
        addNode(world, pos, host);
    }
    @Nullable
    INetworkNode<H, C> findNode(ServerWorld world, BlockPos pos);

    static <H extends INodeHost, C extends INetworkCache<H>> INetworkManager<H, C> get(Class<C> cacheClass) {
        return NetworkManager.get(cacheClass);
    }
}
