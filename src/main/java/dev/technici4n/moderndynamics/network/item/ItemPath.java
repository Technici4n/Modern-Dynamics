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

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.network.NetworkNode;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemPath {
    /**
     * Starting position of the items, i.e. the chest they were pulled from. (Not the pipe!)
     */
    public final BlockPos startingPos;
    public final BlockPos targetPos;
    public final Direction[] path;

    public ItemPath(BlockPos startingPos, BlockPos targetPos, Direction[] path) {
        this.startingPos = startingPos;
        this.targetPos = targetPos;
        this.path = path;
    }

    public NetworkNode<ItemHost, ItemCache> getStartingPoint(ServerLevel level) {
        return ItemHost.MANAGER.findNode(level, startingPos.relative(path[0]));
    }

    public SimulatedInsertionTarget getInsertionTarget(Level world) {
        return SimulatedInsertionTargets.getTarget(world, targetPos, path[path.length - 1].getOpposite());
    }

    public TravelingItem makeTravelingItem(ItemVariant variant, long amount) {
        return new TravelingItem(
                variant,
                amount,
                this,
                FailedInsertStrategy.SEND_BACK_TO_SOURCE,
                0);
    }

    @Nullable
    AttachedAttachment getEndAttachment(ServerLevel level) {
        var lastNode = ItemHost.MANAGER.findNode(level, targetPos.relative(path[path.length - 1].getOpposite()));
        var host = lastNode.getHost();
        return host.getAttachment(path[path.length - 1]);
    }

    /**
     * Return the predicate for the attachment at the very end of the pipe.
     */
    Predicate<ItemVariant> getEndFilter(ServerLevel level) {
        return getEndAttachment(level) instanceof ItemAttachedIo io ? io::matchesItemFilter : v -> true;
    }

    public ItemPath reverse() {
        Direction[] reversedPath = new Direction[path.length];
        for (int i = 0; i < path.length; ++i) {
            reversedPath[path.length - i - 1] = path[i].getOpposite();
        }
        return new ItemPath(targetPos, startingPos, reversedPath);
    }
}
