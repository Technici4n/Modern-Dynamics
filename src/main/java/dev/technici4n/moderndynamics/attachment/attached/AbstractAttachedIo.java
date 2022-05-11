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
import dev.technici4n.moderndynamics.attachment.AttachmentTier;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.Setting;
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;

public abstract class AbstractAttachedIo extends AttachedAttachment {
    private FilterInversionMode filterInversion = FilterInversionMode.WHITELIST;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;

    public AbstractAttachedIo(AttachmentItem item, CompoundTag configData) {
        super(item, configData);

        this.filterInversion = readEnum(FilterInversionMode.values(), configData, "filterInversion");
        this.redstoneMode = readEnum(RedstoneMode.values(), configData, "redstoneMode");
    }

    @Override
    public CompoundTag writeConfigTag(CompoundTag configData) {
        super.writeConfigTag(configData);

        writeEnum(this.filterInversion, configData, "filterInversion");
        writeEnum(this.redstoneMode, configData, "redstoneMode");

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

    protected abstract void resetCachedFilter();

    @Override
    public IoAttachmentItem getItem() {
        return (IoAttachmentItem) super.getItem();
    }

    public IoAttachmentType getType() {
        return getItem().getType();
    }

    public AttachmentTier getTier() {
        return getItem().getTier();
    }

    public Set<Setting> getSupportedSettings() {
        return getItem().getSupportedSettings();
    }

    protected static <T extends Enum<T>> T readEnum(T[] enumValues, CompoundTag tag, String key) {
        var idx = tag.getByte(key);
        if (idx > 0 && idx < enumValues.length) {
            return enumValues[idx];
        } else {
            // TODO LOG
            return enumValues[0];
        }
    }

    protected static <T extends Enum<T>> void writeEnum(T enumValue, CompoundTag tag, String key) {
        if (enumValue.ordinal() == 0) {
            tag.remove(key);
        } else {
            tag.putByte(key, (byte) enumValue.ordinal());
        }
    }
}
