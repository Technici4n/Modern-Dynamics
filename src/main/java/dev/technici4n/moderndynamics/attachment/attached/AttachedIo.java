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

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.Setting;
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import dev.technici4n.moderndynamics.attachment.upgrade.UpgradeType;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.WrenchHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class AttachedIo extends AttachedAttachment {
    public static final int UPGRADE_SLOTS = 4;

    protected final Runnable setChangedCallback;
    private FilterInversionMode filterInversion;
    private RedstoneMode redstoneMode;
    protected final UpgradeContainer upgradeContainer = new UpgradeContainer();

    public AttachedIo(AttachmentItem item, CompoundTag configData, Runnable setChangedCallback) {
        super(item, configData);

        this.setChangedCallback = setChangedCallback;
        this.filterInversion = readEnum(FilterInversionMode.values(), configData, "filterInversion", FilterInversionMode.BLACKLIST);
        this.redstoneMode = readEnum(RedstoneMode.values(), configData, "redstoneMode", RedstoneMode.IGNORED);
        this.upgradeContainer.readNbt(configData);
    }

    @Override
    public CompoundTag writeConfigTag(CompoundTag configData) {
        super.writeConfigTag(configData);

        writeEnum(this.filterInversion, configData, "filterInversion");
        writeEnum(this.redstoneMode, configData, "redstoneMode");
        this.upgradeContainer.writeNbt(configData);

        return configData;
    }

    public FilterInversionMode getFilterInversion() {
        return filterInversion;
    }

    public void setFilterInversion(FilterInversionMode filterInversion) {
        if (filterInversion != this.filterInversion) {
            this.filterInversion = filterInversion;
            resetCachedFilter();
        }
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode mode) {
        this.redstoneMode = mode;
    }

    public ItemStack getUpgrade(int slot) {
        return upgradeContainer.upgrades.get(slot);
    }

    public void setUpgrade(int slot, ItemStack upgrade) {
        upgradeContainer.upgrades.set(slot, upgrade);
        onUpgradesChanged();
    }

    public ItemStack removeUpgrade(int slot, int count) {
        return ContainerHelper.removeItem(upgradeContainer.upgrades, slot, count);
    }

    public boolean mayPlaceUpgrade(int slot, Item upgrade) {
        if (!upgradeContainer.mayPlaceUpgrade(slot, upgrade)) {
            // Duplicate upgrade
            return false;
        }

        UpgradeType type = LoadedUpgrades.getType(upgrade);
        if (type.getAddFilterSlots() > 0) {
            return true;
        }

        if (this instanceof ItemAttachedIo) {
            if (type.isEnableAdvancedBehavior() || type.getAddItemSpeed() > 0) {
                return true;
            }

            if (getType() != IoAttachmentType.FILTER) {
                if (type.getAddItemCount() > 0 || type.getAddItemTransferFrequency() > 0) {
                    return true;
                }
            }
        }

        if (this instanceof FluidAttachedIo) {
            if (type.getAddFluidTransfer() > 0 || type.getMultiplyFluidTransfer() > 0) {
                return true;
            }
        }

        return false;
    }

    public void onUpgradesChanged() {
        setChangedCallback.run();
        resetCachedFilter();
    }

    public int getFilterSize() {
        return upgradeContainer.getFilterSize();
    }

    protected abstract void resetCachedFilter();

    @Override
    public IoAttachmentItem getItem() {
        return (IoAttachmentItem) super.getItem();
    }

    public IoAttachmentType getType() {
        return getItem().getType();
    }

    public Set<Setting> getSupportedSettings() {
        return getItem().getSupportedSettings();
    }

    public boolean isAdvancedBehaviorAllowed() {
        return upgradeContainer.isAdvancedBehaviorAllowed();
    }

    protected static <T extends Enum<T>> T readEnum(T[] enumValues, CompoundTag tag, String key, T defaultValue) {
        var idx = tag.getByte(key);
        if (!tag.contains(key) || idx < 0 || idx >= enumValues.length) {
            return defaultValue;
        } else {
            return enumValues[idx];
        }
    }

    protected static <T extends Enum<T>> void writeEnum(T enumValue, CompoundTag tag, String key) {
        tag.putByte(key, (byte) enumValue.ordinal());
    }

    // TODO: be smart and cache this
    public boolean isEnabledViaRedstone(PipeBlockEntity pipe) {
        if (getRedstoneMode() == RedstoneMode.IGNORED) {
            return true;
        }

        var signal = pipe.getLevel().hasNeighborSignal(pipe.getBlockPos());
        if (signal) {
            return getRedstoneMode() == RedstoneMode.REQUIRES_HIGH;
        } else {
            return getRedstoneMode() == RedstoneMode.REQUIRES_LOW;
        }
    }

    @Override
    public List<ItemStack> getDrops() {
        var drops = new ArrayList<>(super.getDrops());
        for (var upgrade : upgradeContainer.upgrades) {
            if (!upgrade.isEmpty()) {
                drops.add(upgrade);
            }
        }
        return drops;
    }

    @Override
    public void onPlaced(Player player) {
        if (WrenchHelper.isWrench(player.getOffhandItem())) {
            // Set to whitelist mode if the player has a wrench in his offhand.
            filterInversion = FilterInversionMode.WHITELIST;
        }
    }
}
