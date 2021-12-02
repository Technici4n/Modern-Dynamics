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
package dev.technici4n.moderntransportation.network.energy;

import java.util.Arrays;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

class EnergyLimits extends SnapshotParticipant<long[]> {
    final long[] used = new long[6];

    void reset() {
        for (int i = 0; i < 6; ++i) {
            used[i] = 0;
        }
    }

    void use(int i, long amount, TransactionContext tx) {
        updateSnapshots(tx);
        used[i] += amount;
    }

    @Override
    protected long[] createSnapshot() {
        return Arrays.copyOf(used, used.length);
    }

    @Override
    protected void readSnapshot(long[] snapshot) {
        System.arraycopy(snapshot, 0, used, 0, snapshot.length);
    }
}
