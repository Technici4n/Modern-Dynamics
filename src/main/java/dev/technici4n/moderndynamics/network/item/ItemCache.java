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
import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.network.NetworkCache;
import dev.technici4n.moderndynamics.network.NetworkNode;
import java.util.List;
import dev.technici4n.moderndynamics.util.ItemVariant;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class ItemCache extends NetworkCache<ItemHost, ItemCache> {
    private boolean inserting = false;
    protected final ItemPathCache pathCache = new ItemPathCache();

    protected ItemCache(ServerLevel level, List<NetworkNode<ItemHost, ItemCache>> networkNodes) {
        super(level, networkNodes);
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

    /**
     * @param checkedPathsConsumer Accepts how many paths were evaluated if not null. Ignored if null.
     */
    protected int insertList(NetworkNode<ItemHost, ItemCache> startingPoint, Iterable<ItemPath> paths, ItemVariant variant,
            int maxAmount, boolean simulate, double speedMultiplier, @Nullable MaxParticipant checkedPathsConsumer) {
        Preconditions.checkArgument(!variant.isBlank(), "blank variant");
        Preconditions.checkArgument(maxAmount >= 0);
        Preconditions.checkArgument(startingPoint.getNetworkCache() == this, "Tried to insert into another network!");

        if (inserting) {
            return 0;
        }

        inserting = true;
        try {
            int totalInserted = 0;
            int nextPathIndex = 0;
            for (var path : paths) {
                nextPathIndex++;

                // Check possible filter at the endpoint.
                if (!path.getEndFilter(level).test(variant)) {
                    continue;
                }
                // Don't schedule more items if the output is already stuffed.
                if (path.getEndAttachment(level) instanceof ItemAttachedIo io && io.isStuffed()) {
                    continue;
                }

                var simulatedTarget = path.getInsertionTarget(startingPoint.getHost().getPipe().getLevel());

                totalInserted += simulatedTarget.insert(variant, maxAmount - totalInserted, simulate, (v, amount) -> {
                    var travelingItem = path.makeTravelingItem(v, amount, speedMultiplier);
                    startingPoint.getHost().addTravelingItem(travelingItem);
                });
                if (totalInserted == maxAmount) {
                    break;
                }
            }

            if (checkedPathsConsumer != null) {
                checkedPathsConsumer.addEntry(nextPathIndex, simulate);
            }

            return totalInserted;
        } finally {
            inserting = false;
        }
    }
}
