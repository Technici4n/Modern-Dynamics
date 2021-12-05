/*
 * Modern Transportation
 * Copyright (C) 2021 shartte & Technici4n
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package dev.technici4n.moderntransportation.network.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import dev.technici4n.moderntransportation.network.NetworkCache;
import dev.technici4n.moderntransportation.network.NetworkNode;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

public class ItemCache extends NetworkCache<ItemHost, ItemCache> {
    private boolean inserting = false;

    protected ItemCache(List<NetworkNode<ItemHost, ItemCache>> networkNodes) {
        super(networkNodes);
    }

    @Override
    protected void doTick() {
        // TODO: only tick pipes that have items and are in ticking chunks
        for (var node : nodes) {
            node.getHost().tickMovingItems();
        }
    }

    protected long insert(Direction initialDirection, NetworkNode<ItemHost, ItemCache> startingPoint, FailedInsertStrategy strategy,
            ItemVariant variant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        Preconditions.checkArgument(startingPoint.getNetworkCache() == this, "Tried to insert into another network!");

        if (inserting) {
            return 0;
        }

        inserting = true;
        try {
            // First, gather all target nodes by priority.
            PriorityQueue<PqNode> pq = new PriorityQueue<>(Comparator.comparingLong(PqNode::distance));
            Reference2LongMap<NetworkNode<ItemHost, ItemCache>> distance = new Reference2LongOpenHashMap<>();
            Map<NetworkNode<ItemHost, ItemCache>, Direction> prevDirection = new IdentityHashMap<>();
            Map<NetworkNode<ItemHost, ItemCache>, NetworkNode<ItemHost, ItemCache>> prevNode = new IdentityHashMap<>();
            List<NetworkNode<ItemHost, ItemCache>> possibleTargets = new ArrayList<>();

            pq.add(new PqNode(startingPoint, 0));
            distance.put(startingPoint, 0);

            while (!pq.isEmpty()) {
                var currentPqNode = pq.poll();
                var currentNode = currentPqNode.node;
                long currentDistance = currentPqNode.distance;

                if (currentDistance != distance.getLong(currentNode)) {
                    continue;
                }

                if (currentNode.getHost().hasInventoryConnections()) {
                    possibleTargets.add(currentNode);
                }

                for (var connection : currentNode.getConnections()) {
                    long newDistance = currentDistance + connection.target.getHost().getPathingWeight();
                    if (distance.getOrDefault(connection.target, Long.MAX_VALUE) > newDistance) {
                        distance.put(connection.target, newDistance);
                        pq.add(new PqNode(connection.target, newDistance));
                        prevDirection.put(connection.target, connection.direction);
                        prevNode.put(connection.target, currentNode);
                    }
                }
            }

            // Then, do the actual insertion
            long totalInserted = 0;
            for (var target : possibleTargets) {
                for (var side : target.getHost().getInventoryConnections()) {
                    if (target == startingPoint && side == initialDirection.getOpposite()) {
                        continue; // prevent insertion back into the source
                    }
                    var pipe = target.getHost().getPipe();
                    var ajdPos = pipe.getPos().offset(side);
                    var simulatedTarget = SimulatedInsertionTargets.getTarget((ServerWorld) pipe.getWorld(), ajdPos, side.getOpposite());

                    totalInserted += simulatedTarget.insert(variant, maxAmount - totalInserted, transaction, (v, amount) -> {
                        // Backtrack to find the path.
                        List<Direction> reversedPath = new ArrayList<>();
                        var current = target;
                        var currentDir = side;
                        while (current != null) {
                            reversedPath.add(currentDir);
                            currentDir = prevDirection.get(current);
                            current = prevNode.get(current);
                        }
                        reversedPath.add(initialDirection);
                        Direction[] path = Lists.reverse(reversedPath).toArray(Direction[]::new);

                        // Construct traveling item.
                        var travelingItem = new TravelingItem(
                                v,
                                amount,
                                pipe.getWorld(),
                                startingPoint.getHost().getPipe().getPos(),
                                ajdPos,
                                path,
                                strategy,
                                0);
                        startingPoint.getHost().addTravelingItem(travelingItem);
                    });
                    if (totalInserted == maxAmount) {
                        return totalInserted;
                    }
                }
            }
            return totalInserted;
        } finally {
            inserting = false;
        }
    }

    private record PqNode(NetworkNode<ItemHost, ItemCache> node, long distance) {
    }
}
