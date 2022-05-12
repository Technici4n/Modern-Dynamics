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

import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.Nullable;

// TODO: also allow nbt filtering
public class FluidAttachedIo extends AbstractAttachedIo {
    private final NonNullList<FluidVariant> filters;
    @Nullable
    private FluidCachedFilter cachedFilter = null;

    public FluidAttachedIo(IoAttachmentItem item, CompoundTag configData) {
        super(item, configData);

        this.filters = NonNullList.withSize(item.getTier().filterSize, FluidVariant.blank());
        var filterTags = configData.getList("filters", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < this.filters.size(); i++) {
            var filterTag = filterTags.getCompound(i);
            if (!filterTag.isEmpty()) {
                this.filters.set(i, FluidVariant.fromNbt(filterTag));
            }
        }
    }

    @Override
    public CompoundTag writeConfigTag(CompoundTag configData) {
        super.writeConfigTag(configData);

        var filterTags = new ListTag();
        for (FluidVariant filter : this.filters) {
            if (filter.isBlank()) {
                filterTags.add(new CompoundTag());
            } else {
                filterTags.add(filter.toNbt());
            }
        }
        configData.put("filters", filterTags);

        return configData;
    }

    @Override
    protected void resetCachedFilter() {
        cachedFilter = null;
    }

    public FluidVariant getFilter(int idx) {
        return filters.get(idx);
    }

    public void setFilter(int idx, FluidVariant variant) {
        if (!variant.equals(this.filters.get(idx))) {
            this.filters.set(idx, variant);
            resetCachedFilter();
        }
    }

    public boolean matchesFilter(FluidVariant variant) {
        if (cachedFilter == null) {
            cachedFilter = new FluidCachedFilter(filters, getFilterInversion());
        }
        return cachedFilter.matches(variant);
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public @Nullable MenuProvider createMenu(PipeBlockEntity pipe, Direction side) {
        return MdMenus.FLUID_IO.createMenu(pipe, side, this);
    }
}
