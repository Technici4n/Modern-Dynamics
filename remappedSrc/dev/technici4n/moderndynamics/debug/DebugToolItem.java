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
package dev.technici4n.moderndynamics.debug;

import dev.technici4n.moderndynamics.network.NetworkCache;
import dev.technici4n.moderndynamics.network.NetworkNode;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class DebugToolItem extends Item {
    public DebugToolItem() {
        super(new Settings());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        BlockEntity be = ctx.getWorld().getBlockEntity(ctx.getBlockPos());

        if (be instanceof PipeBlockEntity && ctx.getPlayer() != null) {
            if (ctx.getWorld().isClient()) {
                return ActionResult.SUCCESS;
            } else {
                PipeBlockEntity pipe = (PipeBlockEntity) be;
                StringBuilder message = new StringBuilder();
                boolean foundNode = false;

                for (NodeHost host : pipe.getHosts()) {
                    @SuppressWarnings("unchecked")
                    NetworkNode<?, ? extends NetworkCache<?, ?>> node = host.getManager().findNode((ServerWorld) pipe.getWorld(), pipe.getPos());
                    if (node != null) {
                        node.getNetworkCache().appendDebugInfo(message);
                        foundNode = true;
                    }
                }

                if (!foundNode) {
                    message.append("No node found.\n");
                }

                ctx.getPlayer().sendMessage(Text.literal(message.toString()), false);

                return ActionResult.CONSUME;
            }
        }

        return ActionResult.PASS;
    }
}
