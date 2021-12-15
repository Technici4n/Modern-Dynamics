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

import dev.technici4n.moderndynamics.attachment.AttachmentTier;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.settings.FilterMode;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.screen.AttachmentMenuType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.Nullable;

public class AttachedIO extends AttachedAttachment {

    private final NonNullList<ItemVariant> filters;

    private FilterMode filterMode = FilterMode.WHITELIST;

    public AttachedIO(IoAttachmentItem item, CompoundTag configData) {
        super(item, configData);

        this.filters = NonNullList.withSize(item.getTier().filterSize, ItemVariant.blank());
        var filterTags = configData.getList("filters", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < this.filters.size(); i++) {
            var filterTag = filterTags.getCompound(i);
            if (!filterTag.isEmpty()) {
                this.filters.set(i, ItemVariant.fromNbt(filterTag));
            }
        }

        this.filterMode = configData.getByte("filterMode") == 1 ? FilterMode.BLACKLIST : FilterMode.WHITELIST;
    }

    @Override
    public IoAttachmentItem getItem() {
        return (IoAttachmentItem) super.getItem();
    }

    @Override
    public CompoundTag writeConfigTag(CompoundTag tag) {
        super.writeConfigTag(tag);

        var filterTags = new ListTag();
        for (ItemVariant filter : this.filters) {
            if (filter.isBlank()) {
                filterTags.add(new CompoundTag());
            } else {
                filterTags.add(filter.toNbt());
            }
        }
        tag.put("filters", filterTags);

        if (filterMode == FilterMode.BLACKLIST) {
            tag.putByte("filterMode", (byte) 1);
        }

        return tag;
    }

    public IoAttachmentType getType() {
        return getItem().getType();
    }

    public AttachmentTier getTier() {
        return getItem().getTier();
    }

    public boolean matchesItemFilter(ItemVariant variant) {
        boolean filterSet = false;
        for (ItemVariant filter : filters) {
            if (!filter.isBlank()) {
                filterSet = true;
                if (filter.equals(variant)) {
                    return true;
                }
            }
        }

        return !filterSet;
    }

    public int getFilterSize() {
        return getTier().filterSize;
    }

    public ItemVariant getFilter(int idx) {
        return filters.get(idx);
    }

    public void setFilter(int idx, ItemVariant variant) {
        this.filters.set(idx, variant);
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public @Nullable MenuProvider createMenu(PipeBlockEntity pipe, Direction side) {
        return new AttachmentMenuType(pipe, side, this);
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }
}
