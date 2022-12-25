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
package dev.technici4n.moderndynamics.network.shared;

import dev.technici4n.moderndynamics.network.TickHelper;
import java.util.Arrays;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class TransferLimits<T> extends SnapshotParticipant<long[]> {
    // These numbers are somewhat arbitrary, we can always change them later...
    private static final long MAX_TICK_DIFF = 20;
    private static final long MAX_BUFFER_FACTOR = 50;

    private long lastUpdateTick = 0;
    private final long[] available = new long[6];
    private final LimitSupplier<T> limitSupplier;

    public TransferLimits(LimitSupplier<T> limitSupplier) {
        this.limitSupplier = limitSupplier;
    }

    private void checkForNewTick() {
        long currentTick = TickHelper.getTickCounter();

        if (currentTick != lastUpdateTick) {
            long tickDiff = lastUpdateTick != 0 ? currentTick - lastUpdateTick : 1;
            // Add transfer for used ticks.
            tickDiff = Math.min(tickDiff, MAX_TICK_DIFF); // 20 at most

            if (tickDiff > 0) {
                for (var dir : Direction.values()) {
                    int i = dir.get3DDataValue();
                    long cap = limitSupplier.getLimit(dir, null);
                    available[i] = Math.min(available[i] + tickDiff * cap, MAX_BUFFER_FACTOR * cap);
                }
            }

            lastUpdateTick = currentTick;
        }
    }

    /**
     * Limit the passed amount.
     */
    public long limit(int side, long amount, T context) {
        StoragePreconditions.notNegative(amount);
        checkForNewTick();

        // Always check if transfer is still valid.
        if (limitSupplier.getLimit(Direction.from3DDataValue(side), context) == 0) {
            return 0;
        }
        // If ok, check what's available.
        return Math.min(available[side], amount);
    }

    public void use(int side, long amount, TransactionContext tx) {
        updateSnapshots(tx);
        available[side] -= amount;
    }

    @Override
    protected long[] createSnapshot() {
        return Arrays.copyOf(available, available.length);
    }

    @Override
    protected void readSnapshot(long[] snapshot) {
        System.arraycopy(snapshot, 0, available, 0, snapshot.length);
    }

    @FunctionalInterface
    public interface LimitSupplier<T> {
        /**
         * @return Transfer limit for direction and context, or "default" limit if context is null.
         */
        long getLimit(Direction direction, @Nullable T context);
    }
}
