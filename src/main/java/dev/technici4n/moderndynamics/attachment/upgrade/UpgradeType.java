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
package dev.technici4n.moderndynamics.attachment.upgrade;

import net.minecraft.network.FriendlyByteBuf;

public class UpgradeType {
    private int slotLimit = 1;
    private boolean enableAdvancedBehavior = false;
    private int addFilterSlots = 0;
    private int addItemCount = 0;
    private int addItemSpeed = 0;
    private int addItemTransferFrequency = 0;
    private int addFluidTransfer = 0;
    private int multiplyFluidTransfer = 0;

    public static UpgradeType createDummy() {
        var type = new UpgradeType();
        type.slotLimit = 0;
        return type;
    }

    public void writePacket(FriendlyByteBuf buf) {
        buf.writeVarInt(getSlotLimit());
        buf.writeBoolean(isenableAdvancedBehavior());
        buf.writeVarInt(getAddFilterSlots());
        buf.writeVarInt(getAddItemCount());
        buf.writeVarInt(getAddItemSpeed());
        buf.writeVarInt(getAddItemTransferFrequency());
        buf.writeVarInt(getAddFluidTransfer());
        buf.writeVarInt(getMultiplyFluidTransfer());
    }

    public static UpgradeType readPacket(FriendlyByteBuf buf) {
        var type = new UpgradeType();
        type.slotLimit = buf.readVarInt();
        type.enableAdvancedBehavior = buf.readBoolean();
        type.addFilterSlots = buf.readVarInt();
        type.addItemCount = buf.readVarInt();
        type.addItemSpeed = buf.readVarInt();
        type.addItemTransferFrequency = buf.readVarInt();
        type.addFluidTransfer = buf.readVarInt();
        type.multiplyFluidTransfer = buf.readVarInt();
        return type;
    }

    public int getSlotLimit() {
        return slotLimit;
    }

    public boolean isenableAdvancedBehavior() {
        return enableAdvancedBehavior;
    }

    public int getAddFilterSlots() {
        return addFilterSlots;
    }

    public int getAddItemCount() {
        return addItemCount;
    }

    public int getAddItemSpeed() {
        return addItemSpeed;
    }

    public int getAddItemTransferFrequency() {
        return addItemTransferFrequency;
    }

    public int getAddFluidTransfer() {
        return addFluidTransfer;
    }

    public int getMultiplyFluidTransfer() {
        return multiplyFluidTransfer;
    }
}
