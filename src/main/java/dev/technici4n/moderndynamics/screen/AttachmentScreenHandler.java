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
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.MdId;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class AttachmentScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<AttachmentScreenHandler> TYPE = ScreenHandlerRegistry.registerExtended(
            MdId.of("attachment"),
            AttachmentScreenHandler::fromPacket);

    private static AttachmentScreenHandler fromPacket(int syncId, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        var world = playerInventory.player.world;

        var bet = Registry.BLOCK_ENTITY_TYPE.get(packetByteBuf.readIdentifier());
        var side = packetByteBuf.readEnumConstant(Direction.class);
        return world.getBlockEntity(packetByteBuf.readBlockPos(), bet).map(blockEntity -> {
            if (blockEntity instanceof PipeBlockEntity pipe) {
                var attachment = pipe.getAttachment(side);
                if (attachment != null) {
                    return new AttachmentScreenHandler(syncId, playerInventory, attachment);
                }
            }
            return null;
        }).orElse(null);
    }

    public final AttachedAttachment attachment;

    protected AttachmentScreenHandler(int syncId, PlayerInventory playerInventory, AttachedAttachment attachment) {
        super(TYPE, syncId);
        this.attachment = attachment;

        // Config slots
        for (int i = 0; i < attachment.getConfigHeight(); ++i) {
            for (int j = 0; j < attachment.getConfigWidth(); ++j) {
                this.addSlot(new ConfigSlot(8 + j * 18, 30 + i * 18, attachment, i, j));
            }
        }

        // Player inventory slots
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 181));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && getSlot(slotIndex) instanceof ConfigSlot configSlot) {
            attachment.setFilter(configSlot.configX, configSlot.configY, ItemVariant.of(getCursorStack()));
        } else {
            super.onSlotClick(slotIndex, button, actionType, player);
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
    }
}
