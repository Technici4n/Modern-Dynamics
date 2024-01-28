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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import dev.technici4n.moderndynamics.util.ItemVariant;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

// TODO: needs to support recursive queries if filters are being used.
public class SimulatedInsertionTarget {
    private final SimulatedInsertionTargets.Coord coord; // used for crash report info
    private final Supplier<@Nullable IItemHandler> storageFinder;
    /**
     * List of stacks that are already traveling, but for which the target slot is not known.
     */
    private final Object2IntMap<ItemVariant> pendingStacks = new Object2IntLinkedOpenHashMap<>();
    /**
     * List of stacks that are already traveling and that the target should accept,
     * for each slot.
     */
    private final List<ItemStack> awaitedStacks = new ArrayList<>();

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

        // Try to plan for pending stacks to begin with...
        var pendingIterator = pendingStacks.object2IntEntrySet().iterator();
        while (pendingIterator.hasNext()) {
            var entry = pendingIterator.next();

            int planned = planForStack(targetStorage, entry.getKey(), entry.getIntValue(), false);
            if (planned == entry.getIntValue()) {
                pendingIterator.remove();
            } else {
                entry.setValue(entry.getIntValue() - planned);
            }
        }

        // Plan for this additional stack
        int inserted = planForStack(targetStorage, variant, maxAmount, simulate);

        if (!simulate && inserted > 0) {
            callback.startTravel(variant, inserted);
        }

        return inserted;
    }

    /**
     * Try to plan for some stack to be inserted, return how much is anticipated to be insertable.
     */
    private int planForStack(IItemHandler targetStorage, ItemVariant variant, int maxAmount, boolean simulate) {
        // Extend pending list if necessary
        int targetSlots = targetStorage.getSlots();
        while (awaitedStacks.size() < targetSlots) {
            awaitedStacks.add(ItemStack.EMPTY);
        }

        // Used to limit stack allocations
        ItemStack leftover = null;

        for (int i = 0; i < targetSlots; ++i) {
            var pending = awaitedStacks.get(i);

            if (pending.isEmpty()) {
                // No pending stack, try to insert as much as we can.
                if (leftover == null) {
                    leftover = variant.toStack(maxAmount);
                }

                int toInsert = leftover.getCount();
                leftover = targetStorage.insertItem(i, leftover, true);
                int inserted = toInsert - leftover.getCount();

                if (inserted > 0 && !simulate) {
                    awaitedStacks.set(i, variant.toStack(inserted));
                }
            } else if (variant.matches(pending)) {
                // Pending stack, try to insert more than what is scheduled.
                if (leftover == null) {
                    leftover = variant.toStack(maxAmount);
                }

                int insertCount = pending.getCount() + leftover.getCount();
                int inserted = insertCount - targetStorage.insertItem(i, variant.toStack(insertCount), true).getCount();

                int delta = inserted - pending.getCount();
                if (delta > 0) {
                    leftover.shrink(delta);
                    if (!simulate) {
                        pending.grow(delta);
                    }
                }
            }

            if (leftover != null && leftover.isEmpty()) {
                break;
            }
        }

        return leftover == null ? 0 : maxAmount - leftover.getCount();
    }

    public void startAwaiting(ItemVariant variant, int amount) {
        pendingStacks.mergeInt(variant, amount, Integer::sum);
    }

    public void stopAwaiting(ItemVariant variant, int amount) {
        // Remove from pending stacks first
        int pending = pendingStacks.getInt(variant);
        if (pending > 0) {
            if (pending >= amount) {
                pendingStacks.put(variant, pending - amount);
                amount = 0;
            } else {
                pendingStacks.removeInt(variant);
                amount -= pending;
            }
        }

        // Then remove from awaited stacks (starting from the end because why not)
        if (amount > 0) {
            for (int slot = awaitedStacks.size(); slot-->0;) {
                var awaited = awaitedStacks.get(slot);

                if (variant.matches(awaited)) {
                    if (awaited.getCount() > amount) {
                        awaited.shrink(amount);
                        amount = 0;
                    } else {
                        awaitedStacks.set(slot, ItemStack.EMPTY);
                        amount -= awaited.getCount();
                    }

                    if (amount == 0) {
                        break;
                    }
                }
            }
        }
    }
}
