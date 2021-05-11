package dev.technici4n.moderntransportation.network;

import java.util.*;

/**
 * Cache for a given network, storing the actual logic.
 */
public abstract class NetworkCache<H extends NodeHost, C extends NetworkCache<H, C>> {
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

    protected NetworkCache(List<NetworkNode<H, C>> nodes) {
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
        doTick();
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
        C build(List<NetworkNode<H, C>> nodes);
    }
}
