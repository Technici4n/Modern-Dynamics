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
package dev.technici4n.moderndynamics.network;

import java.util.*;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerLevel;

/**
 * Cache for a given network, storing the actual logic.
 */
public abstract class NetworkCache<H extends NodeHost, C extends NetworkCache<H, C>> {
    public final ServerLevel level;
    protected final List<NetworkNode<H, C>> nodes;
    /**
     * A network can be in two states: combined and separated (not combined).
     * When it's in combined mode, it contains information that is not necessarily in sync with its nodes,
     * and operations will interact with the network as a whole.
     * For example, a combined mode energy network will not sync its energy content with its nodes until
     * it leaves that mode.
     */
    private boolean combined = false;
    private final Set<NodeHost> hostsToUpdate = Collections.newSetFromMap(new IdentityHashMap<>());

    protected NetworkCache(ServerLevel level, List<NetworkNode<H, C>> nodes) {
        this.level = level;
        this.nodes = nodes;

        for (NetworkNode<H, C> node : nodes) {
            if (node.getHost().needsUpdate()) {
                hostsToUpdate.add(node.getHost());
            }
        }
    }

    /**
     * Called at the end of the server tick.
     * Make sure to only take hosts {@link NodeHost#isTicking() that are ticking} into account.
     */
    protected abstract void doTick();

    protected final void tick() {
        // Update ticking nodes that need to be updated.
        for (Iterator<NodeHost> it = hostsToUpdate.iterator(); it.hasNext();) {
            NodeHost host = it.next();

            if (host.isTicking() && host.needsUpdate()) {
                host.update();
                it.remove();
            }
        }

        // Actually tick.
        try {
            doTick();
        } catch (Throwable t) {
            var report = CrashReport.forThrowable(t, "Ticking pipe network");

            report.addCategory("Network details")
                    .setDetail("Level", level.dimension().location())
                    .setDetail("Number of nodes", nodes.size());

            if (nodes.size() > 0) {
                var nodeDetails = report.addCategory("Details of first node in the network");
                nodes.get(0).getHost().pipe.fillCrashReportCategory(nodeDetails);
            }

            throw new ReportedException(report);
        }
    }

    public final void scheduleHostUpdate(NodeHost host) {
        hostsToUpdate.add(host);
    }

    protected void doCombine() {
    }

    protected void doSeparate() {
    }

    public final void combine() {
        if (!combined) {
            combined = true;
            doCombine();
        }
    }

    public final void separate() {
        if (combined) {
            combined = false;
            doSeparate();
        }
    }

    public void appendDebugInfo(StringBuilder out) {
        out.append("==== Cache: ").append(getClass().getSimpleName()).append(" ====\n");
        out.append("Combined = ").append(combined).append('\n');
        out.append("Number of nodes = ").append(nodes.size()).append('\n');
    }

    public interface Factory<H extends NodeHost, C extends NetworkCache<H, C>> {
        C build(ServerLevel level, List<NetworkNode<H, C>> nodes);
    }
}
