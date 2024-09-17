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
package dev.technici4n.moderndynamics.network.mienergy;

import com.google.common.base.Preconditions;
import dev.technici4n.moderndynamics.network.NetworkCache;
import dev.technici4n.moderndynamics.network.NetworkNode;
import dev.technici4n.moderndynamics.network.energy.EnergyCache;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class MIEnergyCache extends NetworkCache<MIEnergyHost, MIEnergyCache> {
    private long energy = 0;
    private long maxEnergy = 0;

    protected MIEnergyCache(ServerLevel level, List<NetworkNode<MIEnergyHost, MIEnergyCache>> networkNodes) {
        super(level, networkNodes);
    }

    @Override
    protected void doCombine() {
        energy = maxEnergy = 0;

        for (var node : nodes) {
            energy = saturatedSum(energy, node.getHost().getEnergy());
            maxEnergy = saturatedSum(maxEnergy, node.getHost().getMaxEnergy());
        }
    }

    @Override
    protected void doSeparate() {
        int remainingNodes = nodes.size();

        for (var node : nodes) {
            var host = node.getHost();

            long nodeEnergy = Math.min(host.getMaxEnergy(), energy / remainingNodes);
            host.setEnergy(nodeEnergy);
            energy -= nodeEnergy;
            remainingNodes--;
        }
    }

    @Override
    protected void doTick() {
        // Make sure the network is combined
        combine();

        // Gather inventory connections
        List<IEnergyStorage> storages = new ArrayList<>();

        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().gatherCapabilities(storages);
            }
        }

        var tier = nodes.get(0).getHost().tier;

        // tier.getMax() is an int and energy is unsigned, so casting to (int) is safe
        // Extract
        energy += EnergyCache.transferForTargets(IEnergyStorage::extractEnergy, storages, (int) Math.min(maxEnergy - energy, tier.getMax()));
        // Insert
        energy -= EnergyCache.transferForTargets(IEnergyStorage::receiveEnergy, storages, (int) Math.min(energy, tier.getMax()));
    }

    // Energy is unsigned. Hence we handle only one case of satured addition (same sign)
    private static long saturatedSum(long a, long b) {
        Preconditions.checkArgument(a >= 0, "a >= 0");
        Preconditions.checkArgument(b >= 0, "b >= 0");
        var sum = a + b;
        if (sum < a || sum < b) {
            return Long.MAX_VALUE;
        }
        return sum;
    }
}
