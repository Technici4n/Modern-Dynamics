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

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import dev.technici4n.moderndynamics.network.TickHelper;
import net.minecraft.core.Direction;

public class TransferLimits {
    // These numbers are somewhat arbitrary, we can always change them later...
    // TODO: I don't like this, we should cap at bucket capacity...
    private static final int MAX_TICK_DIFF = 20;
    private static final int MAX_BUFFER_FACTOR = 50;

    private long lastUpdateTick = 0;
    private final int[] available = new int[6];
    private final LimitSupplier limitSupplier;
    private final int maxBuffer;

    public TransferLimits(LimitSupplier limitSupplier, int maxBuffer) {
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
                    int tickLimit = limitSupplier.getLimit(dir);

                    if (tickLimit > 0) {
                        // Buffer up to buffer limit
                        int potentialBuffer = Math.min(Ints.saturatedCast(available[i] + tickLimit * tickDiff), maxBuffer);
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
    public int limit(int side, int amount) {
        Preconditions.checkArgument(amount >= 0);
        checkForNewTick();

        // Always check if transfer is still valid.
        if (limitSupplier.getLimit(Direction.from3DDataValue(side)) == 0) {
            return 0;
        }
        // If ok, check what's available.
        return Math.min(available[side], amount);
    }

    public void use(int side, int amount) {
        available[side] -= amount;
    }

    @FunctionalInterface
    public interface LimitSupplier {
        /**
         * @return Transfer limit for direction and context, or "default" limit if context is null.
         */
        int getLimit(Direction direction);
    }
}
