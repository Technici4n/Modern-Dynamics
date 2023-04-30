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

import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.attachment.settings.FilterDamageMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterModMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterNbtMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterSimilarMode;
import dev.technici4n.moderndynamics.attachment.settings.OversendingMode;
import dev.technici4n.moderndynamics.attachment.settings.RoutingMode;
import dev.technici4n.moderndynamics.gui.MdPackets;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;

public class ItemAttachedIoMenu extends AttachedIoMenu<ItemAttachedIo> {

    public ItemAttachedIoMenu(int syncId, Inventory playerInventory, PipeBlockEntity pipe, Direction side, ItemAttachedIo attachment) {
        super(MdMenus.ITEM_IO, syncId, playerInventory, pipe, side, attachment);

        // Config slots
        var row = 0;
        var col = 0;
        for (int i = 0; i < Constants.Upgrades.MAX_FILTER; i++) {
            this.addSlot(new ItemConfigSlot(44 + col * 18, 20 + row * 18, attachment, i));
            if (++col >= 5) {
                col = 0;
                row++;
            }
        }

        syncShort(this::getMaxItemsExtracted, this::setMaxItemsExtracted);
        syncShort(this::getMaxItemsInInventory, this::setMaxItemsInInventory);

        syncEnum(RoutingMode.class, this::getRoutingMode, this::setRoutingMode);
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
        if (slotIndex >= 0 && getSlot(slotIndex) instanceof ItemConfigSlot configSlot && configSlot.isActive()) {
            attachment.setFilter(configSlot.getConfigIdx(), ItemVariant.of(getCarried()));
        } else {
            super.clicked(slotIndex, button, actionType, player);
        }
    }

    @Override
    protected boolean trySetFilterOnShiftClick(int clickedSlot) {
        var itemVariant = ItemVariant.of(slots.get(clickedSlot).getItem());
        // Check if variant is already configured.
        for (var slot : slots) {
            if (slot instanceof ItemConfigSlot) {
                if (itemVariant.matches(slot.getItem())) {
                    return false;
                }
            }
        }
        for (var slot : slots) {
            if (slot instanceof ItemConfigSlot itemConfigSlot) {
                if (slot.getItem().isEmpty()) {
                    setFilter(itemConfigSlot.getConfigIdx(), itemVariant, false);
                    return true;
                }
            }
        }
        return false;
    }

    public FilterDamageMode getFilterDamage() {
        return attachment.getFilterDamage();
    }

    public void setFilterDamage(FilterDamageMode value, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetFilterDamage(containerId, value);
        }
        attachment.setFilterDamage(value);
        pipe.setChanged();
    }

    public FilterNbtMode getFilterNbt() {
        return attachment.getFilterNbt();
    }

    public void setFilterNbt(FilterNbtMode value, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetFilterNbt(containerId, value);
        }
        attachment.setFilterNbt(value);
        pipe.setChanged();
    }

    public FilterModMode getFilterMod() {
        return attachment.getFilterMod();
    }

    public void setFilterMod(FilterModMode value, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetFilterMod(containerId, value);
        }
        attachment.setFilterMod(value);
        pipe.setChanged();
    }

    public FilterSimilarMode getFilterSimilar() {
        return attachment.getFilterSimilar();
    }

    public void setFilterSimilar(FilterSimilarMode value, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetFilterSimilar(containerId, value);
        }
        attachment.setFilterSimilar(value);
        pipe.setChanged();
    }

    public RoutingMode getRoutingMode() {
        return attachment.getRoutingMode();
    }

    public void setRoutingMode(RoutingMode routingMode, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetRoutingMode(containerId, routingMode);
        }
        attachment.setRoutingMode(routingMode);
        pipe.setChanged();
    }

    public OversendingMode getOversendingMode() {
        return attachment.getOversendingMode();
    }

    public void setOversendingMode(OversendingMode oversendingMode, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetOversendingMode(containerId, oversendingMode);
        }
        attachment.setOversendingMode(oversendingMode);
        pipe.setChanged();
    }

    public int getMaxItemsInInventory() {
        return attachment.getMaxItemsInInventory();
    }

    public void setMaxItemsInInventory(int value, boolean sendPacket) {
        attachment.setMaxItemsInInventory(value);
        pipe.setChanged();
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetMaxItemsInInventory(containerId, getMaxItemsInInventory());
        }
    }

    public int getMaxItemsExtracted() {
        return attachment.getMaxItemsExtracted();
    }

    public void setMaxItemsExtracted(int value, boolean sendPacket) {
        attachment.setMaxItemsExtracted(value);
        pipe.setChanged();
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetMaxItemsExtracted(containerId, getMaxItemsExtracted());
        }
    }

    public int getMaxItemsExtractedMaximum() {
        return attachment.getMaxItemsExtractedMaximum();
    }

    public void setFilter(int configIdx, ItemVariant variant, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetFilter(containerId, configIdx, variant);
        }
        attachment.setFilter(configIdx, variant);
    }
}
