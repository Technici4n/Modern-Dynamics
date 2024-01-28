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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import dev.technici4n.moderndynamics.util.ItemVariant;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

// TODO: needs to support recursive queries if filters are being used.
public class SimulatedInsertionTarget {
    private final SimulatedInsertionTargets.Coord coord; // used for crash report info
    private final Supplier<@Nullable IItemHandler> storageFinder;
    private final Object2IntMap<ItemVariant> awaitedStacks = new Object2IntLinkedOpenHashMap<>();
    private final Participant participant = new Participant();

    public SimulatedInsertionTarget(SimulatedInsertionTargets.Coord coord, Supplier<@Nullable IItemHandler> storageFinder) {
        this.coord = coord;
        this.storageFinder = storageFinder;
    }

    public boolean hasStorage() {
        return storageFinder.get() != null;
    }

    public int insert(ItemVariant variant, int maxAmount, boolean simulate, StartTravelCallback callback) {
        try {
            return innerInsert(variant, maxAmount, simulate, callback);
        } catch (Throwable t) {
            var report = CrashReport.forThrowable(t, "Item pipe simulated insertion failed");

            var target = report.addCategory("Simulated insertion details");
            CrashReportCategory.populateBlockDetails(target, coord.world(), coord.pos(), coord.world().getBlockState(coord.pos()));
            target.setDetail("Accessed from side", coord.direction());
            target.setDetail("Storage", () -> Objects.toString(storageFinder.get(), null))
                    .setDetail("Item variant", variant)
                    .setDetail("Max amount", maxAmount)
                    .setDetail("Simulate", simulate);

            throw new ReportedException(report);
        }
    }

    private int innerInsert(ItemVariant variant, int maxAmount, boolean simulate, StartTravelCallback callback) {
        Preconditions.checkArgument(!variant.isBlank(), "blank variant");
        Preconditions.checkArgument(maxAmount >= 0, "non-negative amount");
        var targetStorage = storageFinder.get();
        if (targetStorage == null) {
            return 0;
        }

        // TODO: Obviously we need to claim slots for the targets here...

        // Simulate insertion of all the awaited stacks
// TODO        for (var entry : awaitedStacks.object2IntEntrySet()) {
// TODO            if (targetStorage.insertItem(entry.getKey(), entry.getIntValue(), true) != entry.getIntValue()) {
// TODO                // We have scheduled too many stacks already, let's not make it worse.
// TODO                return 0;
// TODO            }
// TODO        }
// TODO
// TODO        // All good? Check how much can actually be inserted then.
// TODO        maxAmount = targetStorage.insertItem(variant, maxAmount, true);
// TODO
// TODO        if (maxAmount == 0) {
// TODO            return 0;
// TODO        }
// TODO
// TODO        // Schedule stack to be sent on commit
// TODO        startAwaiting(variant, maxAmount);
// TODO        participant.pendingStacks.add(new PendingStack(variant, maxAmount, callback));

        return maxAmount;
    }

    public void startAwaiting(ItemVariant variant, int amount) {
        awaitedStacks.mergeInt(variant, amount, Integer::sum);
    }

    public void stopAwaiting(ItemVariant variant, int amount) {
        int awaited = awaitedStacks.removeInt(variant);
        if (awaited > amount) {
            awaitedStacks.put(variant, awaited - amount);
        }
    }

    /**
     * Stack that was accepted in {@link #insert} in a transaction that hasn't been committed yet.
     */
    private record PendingStack(ItemVariant variant, int amount, StartTravelCallback callback) {
    }

    private class Participant {
        private final List<PendingStack> pendingStacks = new ArrayList<>();

        protected Integer createSnapshot() {
            return pendingStacks.size();
        }

        protected void readSnapshot(Integer snapshot) {
            while (pendingStacks.size() > snapshot) {
                var stack = pendingStacks.remove(pendingStacks.size() - 1);
                stopAwaiting(stack.variant, stack.amount);
            }
        }

        protected void onFinalCommit() {
            for (var pendingStack : pendingStacks) {
                pendingStack.callback.startTravel(pendingStack.variant, pendingStack.amount);
            }
            pendingStacks.clear();
        }
    }
}
