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

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class MaxParticipant extends SnapshotParticipant<Integer> {
    private int max = 0;

    public void addEntry(int amount, TransactionContext transaction) {
        if (amount > max) {
            updateSnapshots(transaction);
            max = amount;
        }
    }

    public int getMax() {
        return max;
    }

    @Override
    protected Integer createSnapshot() {
        return max;
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        max = snapshot;
    }
}
