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
package dev.technici4n.moderntransportation.network;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.Direction;

public class NetworkNode<H extends NodeHost, C extends NetworkCache<H, C>> {
    private final H host;
    Network<H, C> network;
    private final List<Connection<H, C>> connections = new ArrayList<>();

    public NetworkNode(H host) {
        this.host = host;
    }

    public List<Connection<H, C>> getConnections() {
        return connections;
    }

    public H getHost() {
        return host;
    }

    public C getNetworkCache() {
        return network.cache;
    }

    void addConnection(Direction direction, NetworkNode<H, C> target) {
        for (Connection<H, C> connection : connections) {
            if (connection.direction == direction) {
                throw new IllegalStateException("Connection already exists.");
            }
        }

        connections.add(new Connection<>(direction, target));
    }

    void removeConnection(Direction direction, NetworkNode<H, C> target) {
        for (Iterator<Connection<H, C>> it = connections.iterator(); it.hasNext();) {
            Connection<H, C> connection = it.next();

            if (connection.direction == direction) {
                if (connection.target != target) {
                    throw new IllegalStateException("Target mismatch!");
                }

                it.remove();
                return;
            }
        }

        throw new IllegalStateException("Connection does not exist.");
    }

    void updateHostConnections() {
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);

        for (Connection<H, C> connection : this.connections) {
            connections.add(connection.direction);
        }

        host.setConnections(connections);
    }

    public static class Connection<H extends NodeHost, C extends NetworkCache<H, C>> {
        public final Direction direction;
        public final NetworkNode<H, C> target;

        public Connection(Direction direction, NetworkNode<H, C> target) {
            this.direction = direction;
            this.target = target;
        }
    }
}
