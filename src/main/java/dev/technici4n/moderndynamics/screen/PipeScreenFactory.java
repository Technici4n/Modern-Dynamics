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
package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class PipeScreenFactory implements ExtendedScreenHandlerFactory {
    private final AttachedAttachment attachment;

    public PipeScreenFactory(AttachedAttachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeResourceLocation(Registry.BLOCK_ENTITY_TYPE.getKey(attachment.getPipe().getType()));
        buf.writeEnum(attachment.getSide());
        buf.writeBlockPos(attachment.getPipe().getBlockPos());
    }

    @Override
    public Component getDisplayName() {
        return attachment.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        // This is called server-side
        return new AttachmentMenu(syncId, inv, attachment) {
            @Override
            public boolean stillValid(Player player) {
                var pos = attachment.getPipe().getBlockPos();
                if (player.getLevel().getBlockEntity(pos) != attachment.getPipe()) {
                    return false;
                }
                if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
                    return false;
                }
                return attachment.isAttached();
            }
        };
    }
}
