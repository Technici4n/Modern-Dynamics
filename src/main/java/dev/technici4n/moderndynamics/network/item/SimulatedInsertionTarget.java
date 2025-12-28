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

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

// TODO: needs to support recursive queries if filters are being used.
public class SimulatedInsertionTarget {
    private final SimulatedInsertionTargets.Coord coord; // used for crash report info
    private final Supplier<@Nullable ResourceHandler<ItemResource>> storageFinder;
    private final Object2IntMap<ItemResource> awaitedStacks = new Object2IntLinkedOpenHashMap<>();
    private final PendingStacks pendingStacksJournal = new PendingStacks();

    public SimulatedInsertionTarget(SimulatedInsertionTargets.Coord coord, Supplier<@Nullable ResourceHandler<ItemResource>> storageFinder) {
        this.coord = coord;
        this.storageFinder = storageFinder;
    }

    public boolean hasStorage() {
        return storageFinder.get() != null;
    }

    public int insert(ItemResource resource, int maxAmount, TransactionContext tx, StartTravelCallback callback) {
        try {
            return innerInsert(resource, maxAmount, tx, callback);
        } catch (Throwable t) {
            var report = CrashReport.forThrowable(t, "Item pipe simulated insertion failed");

            var target = report.addCategory("Simulated insertion details");
            CrashReportCategory.populateBlockDetails(target, coord.level(), coord.pos(), coord.level().getBlockState(coord.pos()));
            target.setDetail("Accessed from side", coord.direction());
            target.setDetail("Storage", () -> Objects.toString(storageFinder.get(), null))
                    .setDetail("Item resource", resource)
                    .setDetail("Max amount", maxAmount);

            throw new ReportedException(report);
        }
    }

    private int innerInsert(ItemResource resource, int maxAmount, TransactionContext tx, StartTravelCallback callback) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);
        var targetStorage = storageFinder.get();
        if (targetStorage == null) {
            return 0;
        }

        // This tests how much we could insert into this target while considering everything that is already
        // en-route to it.
        try (var nested = Transaction.open(tx)) {
            // Insert everything already en-route
            for (var entry : awaitedStacks.object2IntEntrySet()) {
                if (targetStorage.insert(entry.getKey(), entry.getIntValue(), nested) != entry.getIntValue()) {
                    // We have scheduled too many stacks already, let's not make it worse.
                    return 0;
                }
            }

            // Now check how much more we can send
            maxAmount = targetStorage.insert(resource, maxAmount, nested);
        }

        if (maxAmount == 0) {
            return 0;
        }

        // Schedule stack to start traveling when the transaction commits
        pendingStacksJournal.updateSnapshots(tx);
        startAwaiting(resource, maxAmount);
        pendingStacksJournal.pendingStacks.add(new PendingStack(resource, maxAmount, callback));

        return maxAmount;
    }

    public void startAwaiting(ItemResource resource, int amount) {
        awaitedStacks.mergeInt(resource, amount, Integer::sum);
    }

    public void stopAwaiting(ItemResource resource, int amount) {
        var awaited = awaitedStacks.removeInt(resource);
        if (awaited > amount) {
            awaitedStacks.put(resource, awaited - amount);
        }
    }

    public record Snapshot(Object2IntMap<ItemResource> pendingStacks, List<ItemStack> awaitedStacks) {
    }

    /**
     * Stack that was accepted in {@link #insert} in a transaction that hasn't been committed yet.
     */
    private record PendingStack(ItemResource resource, int amount, StartTravelCallback callback) {
    }

    private class PendingStacks extends SnapshotJournal<Integer> {
        private final List<PendingStack> pendingStacks = new ArrayList<>();

        @Override
        protected Integer createSnapshot() {
            return pendingStacks.size();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            while (pendingStacks.size() > snapshot) {
                var stack = pendingStacks.removeLast();
                stopAwaiting(stack.resource, stack.amount);
            }
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            for (var pendingStack : pendingStacks) {
                pendingStack.callback.startTravel(pendingStack.resource, pendingStack.amount);
            }
            pendingStacks.clear();
        }
    }
}
