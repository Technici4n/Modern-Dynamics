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

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import org.jetbrains.annotations.Nullable;

// TODO: needs to support recursive queries if filters are being used.
public class SimulatedInsertionTarget {
    private final SimulatedInsertionTargets.Coord coord; // used for crash report info
    private final Supplier<@Nullable Storage<ItemVariant>> storageFinder;
    private final Object2LongMap<ItemVariant> awaitedStacks = new Object2LongLinkedOpenHashMap<>();
    private final Participant participant = new Participant();

    public SimulatedInsertionTarget(SimulatedInsertionTargets.Coord coord, Supplier<@Nullable Storage<ItemVariant>> storageFinder) {
        this.coord = coord;
        this.storageFinder = storageFinder;
    }

    public boolean hasStorage() {
        return storageFinder.get() != null;
    }

    public long insert(ItemVariant variant, long maxAmount, TransactionContext transaction, StartTravelCallback callback) {
        try {
            return innerInsert(variant, maxAmount, transaction, callback);
        } catch (Throwable t) {
            var report = CrashReport.forThrowable(t, "Item pipe simulated insertion failed");

            var target = report.addCategory("Simulated insertion details");
            CrashReportCategory.populateBlockDetails(target, coord.world(), coord.pos(), coord.world().getBlockState(coord.pos()));
            target.setDetail("Accessed from side", coord.direction());
            target.setDetail("Storage", () -> Objects.toString(storageFinder.get(), null))
                    .setDetail("Item variant", variant)
                    .setDetail("Max amount", maxAmount)
                    .setDetail("Transaction", transaction);

            throw new ReportedException(report);
        }
    }

    private long innerInsert(ItemVariant variant, long maxAmount, TransactionContext transaction, StartTravelCallback callback) {
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        var targetStorage = storageFinder.get();
        if (targetStorage == null) {
            return 0;
        }

        try (var nested = transaction.openNested()) {
            // Simulate insertion of all the awaited stacks
            for (var entry : awaitedStacks.object2LongEntrySet()) {
                if (targetStorage.insert(entry.getKey(), entry.getLongValue(), nested) != entry.getLongValue()) {
                    // We have scheduled too many stacks already, let's not make it worse.
                    return 0;
                }
            }

            // All good? Check how much can actually be inserted then.
            maxAmount = targetStorage.insert(variant, maxAmount, nested);
        }

        if (maxAmount == 0) {
            return 0;
        }

        // Schedule stack to be sent on commit
        participant.updateSnapshots(transaction);
        startAwaiting(variant, maxAmount);
        participant.pendingStacks.add(new PendingStack(variant, maxAmount, callback));

        return maxAmount;
    }

    public void startAwaiting(ItemVariant variant, long amount) {
        awaitedStacks.mergeLong(variant, amount, Long::sum);
    }

    public void stopAwaiting(ItemVariant variant, long amount) {
        long awaited = awaitedStacks.removeLong(variant);
        if (awaited > amount) {
            awaitedStacks.put(variant, awaited - amount);
        }
    }

    /**
     * Stack that was accepted in {@link #insert} in a transaction that hasn't been committed yet.
     */
    private record PendingStack(ItemVariant variant, long amount, StartTravelCallback callback) {
    }

    private class Participant extends SnapshotParticipant<Integer> {
        private final List<PendingStack> pendingStacks = new ArrayList<>();

        @Override
        protected Integer createSnapshot() {
            return pendingStacks.size();
        }

        @Override
        protected void readSnapshot(Integer snapshot) {
            while (pendingStacks.size() > snapshot) {
                var stack = pendingStacks.remove(pendingStacks.size() - 1);
                stopAwaiting(stack.variant, stack.amount);
            }
        }

        @Override
        protected void onFinalCommit() {
            for (var pendingStack : pendingStacks) {
                pendingStack.callback.startTravel(pendingStack.variant, pendingStack.amount);
            }
            pendingStacks.clear();
        }
    }
}
