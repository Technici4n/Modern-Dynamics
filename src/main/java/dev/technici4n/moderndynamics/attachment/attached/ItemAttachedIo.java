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
import dev.technici4n.moderndynamics.attachment.Setting;
import dev.technici4n.moderndynamics.attachment.settings.FilterDamageMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterModMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterNbtMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterSimilarMode;
import dev.technici4n.moderndynamics.attachment.settings.OversendingMode;
import dev.technici4n.moderndynamics.attachment.settings.RoutingMode;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.model.AttachmentModelData;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.DropHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemAttachedIo extends AbstractAttachedIo {

    private final Map<ItemVariant, Long> stuffedItems = new LinkedHashMap<>(); // TODO: read/write nbt

    private final NonNullList<ItemVariant> filters;

    private FilterDamageMode filterDamage = FilterDamageMode.RESPECT_DAMAGE;
    private FilterNbtMode filterNbt = FilterNbtMode.RESPECT_NBT;
    private FilterModMode filterMod = FilterModMode.IGNORE_MOD;
    private FilterSimilarMode filterSimilar = FilterSimilarMode.IGNORE_SIMILAR;
    private RoutingMode routingMode = RoutingMode.CLOSEST;
    private OversendingMode oversendingMode = OversendingMode.PREVENT_OVERSENDING;
    /**
     * Maximum number of items ín the target inventory. Across all slots.
     */
    private int maxItemsInInventory = 0;
    /**
     * Maximum amount of items extracted per operation.
     */
    private int maxItemsExtracted = Container.LARGE_MAX_STACK_SIZE;
    // Is lazily initialized when it is needed and reset to null if any of the config changes
    @Nullable
    private ItemCachedFilter cachedFilter;

    public ItemAttachedIo(IoAttachmentItem item, CompoundTag configData) {
        super(item, configData);

        this.filters = NonNullList.withSize(item.getTier().filterSize, ItemVariant.blank());
        var filterTags = configData.getList("filters", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < this.filters.size(); i++) {
            var filterTag = filterTags.getCompound(i);
            if (!filterTag.isEmpty()) {
                this.filters.set(i, ItemVariant.fromNbt(filterTag));
            }
        }

        this.filterDamage = readEnum(FilterDamageMode.values(), configData, "filterDamage");
        this.filterNbt = readEnum(FilterNbtMode.values(), configData, "filterNbt");
        this.filterMod = readEnum(FilterModMode.values(), configData, "filterMod");
        this.filterSimilar = readEnum(FilterSimilarMode.values(), configData, "filterSimilar");
        this.routingMode = readEnum(RoutingMode.values(), configData, "routingMode");
        this.oversendingMode = readEnum(OversendingMode.values(), configData, "oversendingMode");
        if (configData.contains("maxItemsExtracted", Tag.TAG_INT)) {
            this.maxItemsExtracted = Mth.clamp(configData.getInt("maxItemsExtracted"),
                    1, getMaxItemsExtractedMaximum());
        } else {
            this.maxItemsExtracted = getMaxItemsExtractedMaximum();
        }
        this.maxItemsInInventory = configData.getInt("maxItemsInInventory");
        this.maxItemsInInventory = Mth.clamp(this.maxItemsInInventory, 0, getMaxItemsExtractedMaximum());
    }

    @Override
    public CompoundTag writeConfigTag(CompoundTag configData) {
        super.writeConfigTag(configData);

        var filterTags = new ListTag();
        for (ItemVariant filter : this.filters) {
            if (filter.isBlank()) {
                filterTags.add(new CompoundTag());
            } else {
                filterTags.add(filter.toNbt());
            }
        }
        configData.put("filters", filterTags);

        writeEnum(this.filterDamage, configData, "filterDamage");
        writeEnum(this.filterNbt, configData, "filterNbt");
        writeEnum(this.filterMod, configData, "filterMod");
        writeEnum(this.filterSimilar, configData, "filterSimilar");
        writeEnum(this.routingMode, configData, "routingMode");
        writeEnum(this.oversendingMode, configData, "oversendingMode");
        if (this.maxItemsExtracted < getMaxItemsExtractedMaximum()) {
            configData.putInt("maxItemsExtracted", this.maxItemsExtracted);
        } else {
            configData.remove("maxItemsExtracted");
        }
        if (this.maxItemsInInventory > 0) {
            configData.putInt("maxItemsInInventory", this.maxItemsInInventory);
        } else {
            configData.remove("maxItemsInInventory");
        }

        return configData;
    }

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

    public boolean matchesItemFilter(ItemVariant variant) {
        return getCachedFilter().matchesItem(variant);
    }

    public boolean matchesFluidFilter(FluidVariant variant) {
        return getCachedFilter().matchesFluid(variant);
    }

    public int getFilterSize() {
        return getTier().filterSize;
    }

    public ItemVariant getFilter(int idx) {
        return filters.get(idx);
    }

    public void setFilter(int idx, ItemVariant variant) {
        if (!variant.equals(this.filters.get(idx))) {
            this.filters.set(idx, variant);
            resetCachedFilter();
        }
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public @Nullable MenuProvider createMenu(PipeBlockEntity pipe, Direction side) {
        return MdMenus.ITEM_IO.createMenu(pipe, side, this);
    }

    @Override
    public AttachmentModelData getModelData() {
        if (isStuffed()) {
            return AttachmentModelData.from(getItem().attachment.getStuffed());
        }
        return super.getModelData();
    }

    @Override
    public List<ItemStack> getDrops() {
        var drops = new ArrayList<>(super.getDrops());
        for (var entry : stuffedItems.entrySet()) {
            DropHelper.splitIntoStacks(entry.getKey(), entry.getValue(), drops::add);
        }
        return drops;
    }

    public boolean isStuffed() {
        return stuffedItems.size() > 0;
    }

    /**
     * Returns the raw map of stuffed items, be careful.
     */
    public Map<ItemVariant, Long> getStuffedItems() {
        return stuffedItems;
    }

    public FilterDamageMode getFilterDamage() {
        return filterDamage;
    }

    public void setFilterDamage(FilterDamageMode filterDamage) {
        if (filterDamage != this.filterDamage) {
            this.filterDamage = filterDamage;
            resetCachedFilter();
        }
    }

    public FilterNbtMode getFilterNbt() {
        return filterNbt;
    }

    public void setFilterNbt(FilterNbtMode filterNbt) {
        if (filterNbt != this.filterNbt) {
            this.filterNbt = filterNbt;
            resetCachedFilter();
        }
    }

    public FilterModMode getFilterMod() {
        return filterMod;
    }

    public void setFilterMod(FilterModMode filterMod) {
        if (filterMod != this.filterMod) {
            this.filterMod = filterMod;
            resetCachedFilter();
        }
    }

    public FilterSimilarMode getFilterSimilar() {
        return filterSimilar;
    }

    public void setFilterSimilar(FilterSimilarMode value) {
        if (value != this.filterSimilar) {
            this.filterSimilar = value;
            resetCachedFilter();
        }
    }

    public RoutingMode getRoutingMode() {
        return routingMode;
    }

    public void setRoutingMode(RoutingMode mode) {
        this.routingMode = mode;
    }

    public OversendingMode getOversendingMode() {
        return oversendingMode;
    }

    public void setOversendingMode(OversendingMode mode) {
        this.oversendingMode = mode;
    }

    public int getMaxItemsInInventory() {
        return maxItemsInInventory;
    }

    public void setMaxItemsInInventory(int value) {
        this.maxItemsInInventory = Mth.clamp(value, 0, Integer.MAX_VALUE);
    }

    public int getMaxItemsExtracted() {
        return maxItemsExtracted;
    }

    public void setMaxItemsExtracted(int value) {
        this.maxItemsExtracted = Mth.clamp(value, 1, getMaxItemsExtractedMaximum());
    }

    public int getMaxItemsExtractedMaximum() {
        return switch (getTier()) {
        case BASIC -> 8;
        case IMPROVED -> 24;
        case ADVANCED -> 64;
        };
    }

    private ItemCachedFilter getCachedFilter() {
        if (this.cachedFilter == null) {
            this.cachedFilter = new ItemCachedFilter(
                    this.filters,
                    getFilterInversion(),
                    this.filterDamage,
                    this.filterNbt,
                    this.filterMod);
        }
        return this.cachedFilter;
    }

    @Override
    protected void resetCachedFilter() {
        this.cachedFilter = null;
    }

    public Set<Setting> getSupportedSettings() {
        return getItem().getSupportedSettings();
    }
}
