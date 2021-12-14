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
import dev.technici4n.moderndynamics.network.NetworkCache;
import dev.technici4n.moderndynamics.network.NetworkNode;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;

public class ItemCache extends NetworkCache<ItemHost, ItemCache> {
    private boolean inserting = false;
    private ItemPathCache pathCache = new ItemPathCache();

    protected ItemCache(List<NetworkNode<ItemHost, ItemCache>> networkNodes) {
        super(networkNodes);
    }

    protected void invalidatePathCache() {
        pathCache = new ItemPathCache();
    }

    @Override
    protected void doTick() {
        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().tickMovingItems();
            }
        }

        for (var node : nodes) {
            if (node.getHost().isTicking()) {
                node.getHost().tickAttachments();
            }
        }
    }

    protected long insert(Direction initialDirection, NetworkNode<ItemHost, ItemCache> startingPoint, FailedInsertStrategy strategy,
            ItemVariant variant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        Preconditions.checkArgument(startingPoint.getNetworkCache() == this, "Tried to insert into another network!");

        if (inserting) {
            return 0;
        }

        inserting = true;
        try {
            var paths = pathCache.getPaths(startingPoint, initialDirection);

            long totalInserted = 0;
            for (var path : paths) {
                var simulatedTarget = path.getInsertionTarget(startingPoint.getHost().getPipe().getLevel());

                totalInserted += simulatedTarget.insert(variant, maxAmount - totalInserted, transaction, (v, amount) -> {
                    var travelingItem = path.makeTravelingItem(v, amount);
                    startingPoint.getHost().addTravelingItem(travelingItem);
                });
                if (totalInserted == maxAmount) {
                    return totalInserted;
                }
            }
            return totalInserted;
        } finally {
            inserting = false;
        }
    }
}
