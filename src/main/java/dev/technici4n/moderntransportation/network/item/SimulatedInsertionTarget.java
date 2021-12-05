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

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.Nullable;

public class SimulatedInsertionTarget {
    private final Supplier<@Nullable Storage<ItemVariant>> storageFinder;
    private final Object2LongMap<ItemVariant> awaitedStacks = new Object2LongLinkedOpenHashMap<>();
    private final Participant participant = new Participant();

    public SimulatedInsertionTarget(Supplier<@Nullable Storage<ItemVariant>> storageFinder) {
        this.storageFinder = storageFinder;
    }

    public long insert(ItemVariant variant, long maxAmount, TransactionContext transaction, StartTravelCallback callback) {
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
        participant.pendingStacks.add(new PendingStack(variant, maxAmount, callback));

        return maxAmount;
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
            // Not sure if this works, but Chocohead says it should.
            pendingStacks.subList(snapshot, pendingStacks.size()).clear();
        }

        @Override
        protected void onFinalCommit() {
            for (var pendingStack : pendingStacks) {
                awaitedStacks.mergeLong(pendingStack.variant, pendingStack.amount, Long::sum);
                pendingStack.callback.startTravel(pendingStack.variant, pendingStack.amount);
            }
            pendingStacks.clear();
        }
    }
}
