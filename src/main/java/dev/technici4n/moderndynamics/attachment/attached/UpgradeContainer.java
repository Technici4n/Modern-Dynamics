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
package dev.technici4n.moderndynamics.attachment.attached;

import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import dev.technici4n.moderndynamics.attachment.upgrade.UpgradeType;
import java.util.function.ToIntFunction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

class UpgradeContainer {
    final NonNullList<ItemStack> upgrades = NonNullList.withSize(AttachedIo.UPGRADE_SLOTS, ItemStack.EMPTY);

    public void readNbt(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, upgrades);
    }

    public void writeNbt(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, upgrades);
    }

    public boolean mayPlaceUpgrade(int slot, Item upgrade) {
        for (int i = 0; i < AttachedIo.UPGRADE_SLOTS; ++i) {
            if (i != slot && upgrades.get(i).is(upgrade)) {
                // We already contain the same upgrade!
                return false;
            }
        }

        return LoadedUpgrades.getType(upgrade).getSlotLimit() > 0;
    }

    private int reduce(ToIntFunction<UpgradeType> valueExtractor) {
        int tot = 0;
        for (var stack : upgrades) {
            tot += stack.getCount() * valueExtractor.applyAsInt(LoadedUpgrades.getType(stack.getItem()));
        }
        return tot;
    }

    public int getFilterSize() {
        return Mth.clamp(3 + reduce(UpgradeType::getAddFilterSlots), 0, Constants.Upgrades.MAX_FILTER);
    }

    public int getItemsPerOperation() {
        return Mth.clamp(4 + reduce(UpgradeType::getAddItemCount), 1, Integer.MAX_VALUE);
    }

    public double getItemSpeedupFactor() {
        return Mth.clamp(1 + reduce(UpgradeType::getAddItemSpeed), 0.25, 20);
    }

    public int getItemOperationTickDelay() {
        return Mth.clamp(40 / (1 + reduce(UpgradeType::getAddItemTransferFrequency)), 1, 200);
    }

    public long getFluidMaxIo() {
        int totalAdd = 1 + reduce(UpgradeType::getAddFluidTransfer);
        int totalMultiply = 1 + reduce(UpgradeType::getMultiplyFluidTransfer);
        return Mth.clamp(totalAdd * totalMultiply, 1, 1_000_000) * Constants.Fluids.BASE_IO;
    }

    public boolean isAdvancedBehaviorAllowed() {
        for (var stack : upgrades) {
            if (LoadedUpgrades.getType(stack.getItem()).isEnableAdvancedBehavior()) {
                return true;
            }
        }
        return false;
    }
}
