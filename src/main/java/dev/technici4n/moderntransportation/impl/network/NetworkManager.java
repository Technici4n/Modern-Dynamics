package dev.technici4n.moderntransportation.impl.network;

import dev.technici4n.moderntransportation.api.network.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.ServerLifecycleEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NetworkManager<H extends INodeHost, C extends INetworkCache<H>> implements INetworkManager<H, C> {
    private static final Map<Class<?>, NetworkManager<?, ?>> MANAGERS = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public static synchronized <H extends INodeHost, C extends INetworkCache<H>> INetworkManager<H, C> get(Class<C> cacheClass) {
        INetworkManager<H, C> manager = (INetworkManager<H, C>) MANAGERS.get(cacheClass);

        if (manager == null) {
            throw new IllegalArgumentException("NetworkManager does not exist for cache class " + cacheClass.getCanonicalName());
        }

        return manager;
    }

    public static synchronized <H extends INodeHost, C extends INetworkCache<H>> void registerCacheClass(Class<C> cacheClass, INetworkCache.Factory<H, C> factory) {
        Objects.requireNonNull(cacheClass, "Cache class may not be null.");
        Objects.requireNonNull(factory, "Factory may not be null.");

        if (MANAGERS.put(cacheClass, new NetworkManager<>(cacheClass, factory)) != null) {
            throw new IllegalArgumentException("Duplicate registration of cache class " + cacheClass.getCanonicalName());
        }
    }

    public static synchronized void onServerStopped() {
        for (NetworkManager<?, ?> manager : MANAGERS.values()) {
            manager.nodes.clear();
            manager.pendingUpdates.clear();
            manager.networks.clear();
        }
    }

    public static synchronized void onEndTick() {
        for (NetworkManager<?, ?> manager : MANAGERS.values()) {
            manager.updateNetworks();

            for (Network<?, ?> network : manager.networks) {
                network.cache.tick();
            }
        }
    }

    // TODO: remove this?
    private final Class<C> cacheClass;
    private final INetworkCache.Factory<H, C> cacheFactory;
    private final IdentityHashMap<ServerWorld, Long2ObjectOpenHashMap<NetworkNode<H, C>>> nodes = new IdentityHashMap<>();
    private final Set<NetworkNode<H, C>> pendingUpdates = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Network<H, C>> networks = Collections.newSetFromMap(new IdentityHashMap<>());

    NetworkManager(Class<C> cacheClass, INetworkCache.Factory<H, C> cacheFactory) {
        this.cacheClass = cacheClass;
        this.cacheFactory = cacheFactory;
    }

    @Override
    public void addNode(ServerWorld world, BlockPos pos, H host) {
        Long2ObjectOpenHashMap<NetworkNode<H, C>> worldNodes = nodes.computeIfAbsent(world, w -> new Long2ObjectOpenHashMap<>());

        NetworkNode<H, C> newNode = new NetworkNode<>(host);
        EnumSet<Direction> allowedConnections = host.getAllowedNodeConnections();

        if (worldNodes.put(pos.asLong(), newNode) != null) {
            throw new IllegalArgumentException("Node at position " + pos + " in world " + world + " already exists.");
        }

        pendingUpdates.add(newNode);

        for (Direction direction : allowedConnections) {
            BlockPos adjacentPos = pos.offset(direction);
            @Nullable
            NetworkNode<H, C> adjacentNode = worldNodes.get(adjacentPos.asLong());

            if (adjacentNode != null && adjacentNode.getHost().getAllowedNodeConnections().contains(direction.getOpposite())) {
                newNode.addConnection(direction, adjacentNode);
                adjacentNode.addConnection(direction.getOpposite(), newNode);
            }
        }
    }

    @Override
    public void removeNode(ServerWorld world, BlockPos pos, H host) {
        Long2ObjectOpenHashMap<NetworkNode<H, C>> worldNodes = nodes.computeIfAbsent(world, w -> new Long2ObjectOpenHashMap<>());

        NetworkNode<H, C> node = worldNodes.remove(pos.asLong());

        if (node == null) {
            throw new IllegalArgumentException("Node at position " + pos + " in world " + world + " can't be removed: it doesn't exist.");
        }

        for (INetworkNode.Connection<H, C> connection : node.getConnections()) {
            NetworkNode<H, C> target = (NetworkNode<H, C>) connection.target;
            target.removeConnection(connection.direction.getOpposite(), node);
            pendingUpdates.add(target);
        }
    }

    @Override
    public @Nullable INetworkNode<H, C> findNode(ServerWorld world, BlockPos pos) {
        updateNetworks();

        return nodes.computeIfAbsent(world, w -> new Long2ObjectOpenHashMap<>()).get(pos.asLong());
    }

    private void updateNetworks() {
        if (pendingUpdates.size() == 0) return;

        List<NetworkNode<H, C>> pendingUpdatesCopy = new ArrayList<>(pendingUpdates);
        pendingUpdates.clear();

        // pendingUpdates is now used as a visited flag while we rebuild the networks
        for (NetworkNode<H, C> node : pendingUpdatesCopy) {
            if (!pendingUpdates.contains(node)) {
                List<NetworkNode<H, C>> nodes = new ArrayList<>();
                Network<H, C> network = new Network<>(nodes);
                assignNetworkDfs(node, network);
                network.cache = cacheFactory.build(network.nodes);
                networks.add(network);
            }
        }

        // clear the visited flag
        pendingUpdates.clear();
    }

    private void assignNetworkDfs(NetworkNode<H, C> u, Network<H, C> network) {
        if (pendingUpdates.add(u)) {
            // Remove previous network
            if (u.network != null) networks.remove(u.network);

            // Link node to new network
            u.network = network;
            network.nodes.add(u);

            // Visit neighbors
            for (INetworkNode.Connection<H, C> connection : u.getConnections()) {
                assignNetworkDfs((NetworkNode<H, C>) connection.target, network);
            }
        }
    }
}
