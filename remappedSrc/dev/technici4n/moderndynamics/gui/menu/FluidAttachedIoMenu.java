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
package dev.technici4n.moderndynamics.gui.menu;

import I;
import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.attached.FluidAttachedIo;
import dev.technici4n.moderndynamics.gui.MdPackets;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Direction;

public class FluidAttachedIoMenu extends AttachedIoMenu<FluidAttachedIo> {

    public FluidAttachedIoMenu(int syncId, PlayerInventory playerInventory, PipeBlockEntity pipe, Direction side, FluidAttachedIo attachment) {
        super(MdMenus.FLUID_IO, syncId, playerInventory, pipe, side, attachment);

        // Config slots
        var row = 0;
        var col = 0;
        for (int i = 0; i < Constants.Upgrades.MAX_FILTER; i++) {
            this.addSlot(new FluidConfigSlot(44 + col * 18, 20 + row * 18, attachment, i));
            if (++col >= 5) {
                col = 0;
                row++;
            }
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && getSlot(slotIndex) instanceof FluidConfigSlot configSlot && configSlot.isEnabled()) {
            var selectedVariant = Objects.requireNonNullElse(
                    StorageUtil.findStoredResource(ContainerItemContext.ofPlayerCursor(player, this).find(FluidStorage.ITEM)),
                    FluidVariant.blank());
            attachment.setFilter(configSlot.getConfigIdx(), selectedVariant);
        } else {
            super.onSlotClick(slotIndex, button, actionType, player);
        }
    }

    @Override
    protected boolean trySetFilterOnShiftClick(int clickedSlot) {
        // Find resource that's not configured yet
        var fluidVariant = StorageUtil.findStoredResource(
                ContainerItemContext.withInitial(slots.get(clickedSlot).getStack()).find(FluidStorage.ITEM),
                fv -> {
                    for (var slot : slots) {
                        if (slot instanceof FluidConfigSlot fluidConfig) {
                            if (fluidConfig.getFilter().equals(fv)) {
                                return false;
                            }
                        }
                    }
                    return true;
                });
        if (fluidVariant != null) {
            for (var slot : slots) {
                if (slot instanceof FluidConfigSlot fluidConfig) {
                    if (fluidConfig.getFilter().isBlank()) {
                        setFilter(fluidConfig.getConfigIdx(), fluidVariant, false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setFilter(int configIdx, FluidVariant variant, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetFilter(syncId, configIdx, variant);
        }
        attachment.setFilter(configIdx, variant);
    }
}
