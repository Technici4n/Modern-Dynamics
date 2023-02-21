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

public class TransferLimits extends SnapshotParticipant<long[]> {
    // These numbers are somewhat arbitrary, we can always change them later...
    // TODO: I don't like this, we should cap at bucket capacity...
    private static final long MAX_TICK_DIFF = 20;
    private static final long MAX_BUFFER_FACTOR = 50;

    private long lastUpdateTick = 0;
    private final long[] available = new long[6];
    private final LimitSupplier limitSupplier;
    private final long maxBuffer;

    public TransferLimits(LimitSupplier limitSupplier, long maxBuffer) {
        this.limitSupplier = limitSupplier;
        this.maxBuffer = maxBuffer;
    }

    private void checkForNewTick() {
        long currentTick = TickHelper.getTickCounter();

        if (currentTick != lastUpdateTick) {
            if (maxBuffer <= 0 || lastUpdateTick == 0) {
                // No buffering (or first update ever): just reset available
                for (var dir : Direction.values()) {
                    int i = dir.get3DDataValue();
                    available[i] = limitSupplier.getLimit(dir);
                }
            } else {
                // Buffering
                long tickDiff = currentTick - lastUpdateTick;

                for (var dir : Direction.values()) {
                    int i = dir.get3DDataValue();
                    long tickLimit = limitSupplier.getLimit(dir);

                    if (tickLimit > 0) {
                        // Buffer up to buffer limit
                        long potentialBuffer = Math.min(available[i] + tickLimit * tickDiff, maxBuffer);
                        // Pick max between (limited) buffer and raw tick limit
                        available[i] = Math.max(potentialBuffer, tickLimit);
                    } else {
                        available[i] = 0;
                    }
                }

            }

            lastUpdateTick = currentTick;
        }
    }

    /**
     * Limit the passed amount.
     */
    public long limit(int side, long amount) {
        StoragePreconditions.notNegative(amount);
        checkForNewTick();

        // Always check if transfer is still valid.
        if (limitSupplier.getLimit(Direction.from3DDataValue(side)) == 0) {
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
    public interface LimitSupplier {
        /**
         * @return Transfer limit for direction and context, or "default" limit if context is null.
         */
        long getLimit(Direction direction);
    }
}
