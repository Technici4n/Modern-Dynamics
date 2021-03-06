/*
 * Modern Dynamics
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
package dev.technici4n.moderndynamics.network.item;

import com.google.common.collect.Lists;
import dev.technici4n.moderndynamics.attachment.attached.AttachedInhibitor;
import dev.technici4n.moderndynamics.network.NetworkNode;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.minecraft.core.Direction;

public class ItemPathCache {
    private final Map<SidedNode, List<ItemPath>> cache = new HashMap<>();

    public List<ItemPath> getPaths(NetworkNode<ItemHost, ItemCache> startingPoint, Direction startingSide) {
        return cache.computeIfAbsent(new SidedNode(startingPoint, startingSide), ItemPathCache::computePaths);
    }

    public void invalidate() {
        cache.clear();
    }

    private static List<ItemPath> computePaths(SidedNode startingPoint) {
        // First, gather all targets, sorted by priority.
        PriorityQueue<PqNode> pq = new PriorityQueue<>(Comparator.comparingLong(PqNode::distance));
        Reference2LongMap<NetworkNode<ItemHost, ItemCache>> distance = new Reference2LongOpenHashMap<>();
        Map<NetworkNode<ItemHost, ItemCache>, Direction> prevDirection = new IdentityHashMap<>();
        Map<NetworkNode<ItemHost, ItemCache>, NetworkNode<ItemHost, ItemCache>> prevNode = new IdentityHashMap<>();

        // We use a second priority queue for the targets.
        // This might seem redundant, but it's an easy way for us to take into account inhibitors at the very end of a path,
        // which might still change the distance for the entire path, even if the distance to the end node doesn't change.
        PriorityQueue<PqSidedNode> targets = new PriorityQueue<>(Comparator.comparingLong(PqSidedNode::distance));

        pq.add(new PqNode(startingPoint.node, 0));
        distance.put(startingPoint.node, 0);

        while (!pq.isEmpty()) {
            var currentPqNode = pq.poll();
            var currentNode = currentPqNode.node;
            long currentDistance = currentPqNode.distance;

            if (currentDistance != distance.getLong(currentNode)) {
                continue;
            }

            for (var side : currentNode.getHost().getInventoryConnections()) {
                var attachment = currentNode.getHost().getAttachment(side);
                if (attachment == null || attachment.allowsItemConnection()) { // Check that the attachment allows the connection in the first place.
                    long edgeWeight = 1;
                    if (currentNode.getHost().getAttachment(side) instanceof AttachedInhibitor) {
                        edgeWeight += 1000;
                    }
                    targets.add(new PqSidedNode(new SidedNode(currentNode, side), currentDistance + edgeWeight));
                }
            }

            for (var connection : currentNode.getConnections()) {
                long edgeWeight = 1;
                if (currentNode.getHost().getAttachment(connection.direction()) instanceof AttachedInhibitor) {
                    edgeWeight += 1000;
                }
                if (connection.target().getHost().getAttachment(connection.direction().getOpposite()) instanceof AttachedInhibitor) {
                    edgeWeight += 1000;
                }
                long newDistance = currentDistance + edgeWeight;
                if (distance.getOrDefault(connection.target(), Long.MAX_VALUE) > newDistance) {
                    distance.put(connection.target(), newDistance);
                    pq.add(new PqNode(connection.target(), newDistance));
                    prevDirection.put(connection.target(), connection.direction());
                    prevNode.put(connection.target(), currentNode);
                }
            }
        }

        // Build the paths
        List<ItemPath> computedPaths = new ArrayList<>(targets.size());
        for (var pqTarget : targets) {
            var target = pqTarget.sidedNode;
            var side = target.side;

            if (target.node == startingPoint.node && side == startingPoint.side.getOpposite()) {
                continue; // prevent insertion back into the source
            }
            var adjPos = target.node.getHost().getPipe().getBlockPos().relative(side);

            // Backtrack to find the path.
            List<Direction> reversedPath = new ArrayList<>();
            var current = target.node;
            var currentDir = side;
            while (current != null) {
                reversedPath.add(currentDir);
                currentDir = prevDirection.get(current);
                current = prevNode.get(current);
            }
            reversedPath.add(startingPoint.side);
            Direction[] path = Lists.reverse(reversedPath).toArray(Direction[]::new);
            var startPos = startingPoint.node.getHost().getPipe().getBlockPos().relative(startingPoint.side.getOpposite());
            computedPaths.add(new ItemPath(startPos, adjPos, path));
        }

        return Collections.unmodifiableList(computedPaths);
    }

    private record SidedNode(NetworkNode<ItemHost, ItemCache> node, Direction side) {
    }

    private record PqNode(NetworkNode<ItemHost, ItemCache> node, long distance) {
    }

    private record PqSidedNode(SidedNode sidedNode, long distance) {
    }
}
