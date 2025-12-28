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

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.settings.FilterDamageMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterModMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterNbtMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterSimilarMode;
import dev.technici4n.moderndynamics.attachment.settings.OversendingMode;
import dev.technici4n.moderndynamics.attachment.settings.RoutingMode;
import dev.technici4n.moderndynamics.gui.menu.AttachmentMenuType;
import dev.technici4n.moderndynamics.gui.menu.ItemAttachedIoMenu;
import dev.technici4n.moderndynamics.model.AttachmentModelData;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.DropHelper;
import dev.technici4n.moderndynamics.util.ExtendedMenuProvider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class ItemAttachedIo extends AttachedIo {

    private static final Codec<List<ItemResource>> FILTER_LIST_CODEC = ItemResource.OPTIONAL_CODEC.listOf(0, Constants.Upgrades.MAX_FILTER);

    private final Map<ItemResource, Integer> stuffedItems = new LinkedHashMap<>();
    private int roundRobinIndex;

    private final NonNullList<ItemResource> filters;

    private FilterDamageMode filterDamage;
    private FilterNbtMode filterNbt;
    private FilterModMode filterMod;
    private FilterSimilarMode filterSimilar;
    private RoutingMode routingMode;
    private OversendingMode oversendingMode;
    /**
     * Maximum number of items ín the target inventory. Across all slots.
     */
    private int maxItemsInInventory;
    /**
     * Maximum amount of items extracted per operation.
     */
    private int maxItemsExtracted;
    /**
     * Whether we are currently at the max amount of {@link #maxItemsExtracted}. If we are and an upgrade is applied,
     * then {@link #maxItemsExtracted} should be updated to the new maximum.
     */
    boolean maxItemsExtractedAtMax;
    // Is lazily initialized when it is needed and reset to null if any of the config changes
    @Nullable
    private ItemCachedFilter cachedFilter;

    record StuffedEntry(ItemResource item, int amount) {
        public static final Codec<StuffedEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ItemResource.OPTIONAL_CODEC.fieldOf("r").forGetter(StuffedEntry::item),
                Codec.INT.fieldOf("a").forGetter(StuffedEntry::amount)).apply(builder, StuffedEntry::new));
    }

    public ItemAttachedIo(IoAttachmentItem item, ValueInput configData, Runnable setChangedCallback) {
        super(item, configData, setChangedCallback);

        this.filters = NonNullList.withSize(Constants.Upgrades.MAX_FILTER, ItemResource.EMPTY);
        configData.read("filters", FILTER_LIST_CODEC).ifPresent(filterList -> {
            for (int i = 0; i < filterList.size(); i++) {
                if (i < filters.size()) {
                    filters.set(i, filterList.get(i));
                }
            }
        });

        this.filterDamage = readEnum(FilterDamageMode.values(), configData, "filterDamage", FilterDamageMode.RESPECT_DAMAGE);
        this.filterNbt = readEnum(FilterNbtMode.values(), configData, "filterNbt", FilterNbtMode.RESPECT_NBT);
        this.filterMod = readEnum(FilterModMode.values(), configData, "filterMod", FilterModMode.IGNORE_MOD);
        this.filterSimilar = readEnum(FilterSimilarMode.values(), configData, "filterSimilar", FilterSimilarMode.IGNORE_SIMILAR);
        this.routingMode = readEnum(RoutingMode.values(), configData, "routingMode", RoutingMode.CLOSEST);
        this.oversendingMode = readEnum(OversendingMode.values(), configData, "oversendingMode", OversendingMode.PREVENT_OVERSENDING);
        setMaxItemsExtracted(configData.getIntOr("maxItemsExtracted", getMaxItemsExtractedMaximum()));
        this.maxItemsInInventory = configData.getIntOr("maxItemsInInventory", 0);
        this.maxItemsInInventory = Mth.clamp(this.maxItemsInInventory, 0, getMaxItemsExtractedMaximum());

        this.stuffedItems.clear();
        configData.list("stuffed", StuffedEntry.CODEC).ifPresent(stuffedItems -> {
            stuffedItems.forEach(stuffedEntry -> {
                if (!stuffedEntry.item.isEmpty() && stuffedEntry.amount > 0) {
                    this.stuffedItems.put(stuffedEntry.item, stuffedEntry.amount);
                }
            });
        });
        this.roundRobinIndex = Math.max(0, configData.getIntOr("roundRobinIndex", 0));
    }

    @Override
    public void writeConfigTag(ValueOutput output) {
        super.writeConfigTag(output);

        output.store("filters", FILTER_LIST_CODEC, this.filters);
        writeEnum(this.filterDamage, output, "filterDamage");
        writeEnum(this.filterNbt, output, "filterNbt");
        writeEnum(this.filterMod, output, "filterMod");
        writeEnum(this.filterSimilar, output, "filterSimilar");
        writeEnum(this.routingMode, output, "routingMode");
        writeEnum(this.oversendingMode, output, "oversendingMode");
        output.putInt("maxItemsExtracted", this.maxItemsExtracted);
        output.putInt("maxItemsInInventory", this.maxItemsInInventory);
        if (!stuffedItems.isEmpty()) {
            var stuffedOut = output.list("stuffed", StuffedEntry.CODEC);
            for (var entry : stuffedItems.entrySet()) {
                stuffedOut.add(new StuffedEntry(entry.getKey(), entry.getValue()));
            }
        }
        if (roundRobinIndex != 0) {
            output.putInt("roundRobinIndex", roundRobinIndex);
        }
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        // If the max rises and we are at the max, make sure to bump the current value of maxItemsExtracted
        if (maxItemsExtractedAtMax) {
            setMaxItemsExtracted(getMaxItemsExtractedMaximum());
        }
        // Make sure the value stays under the max if the max changes
        setMaxItemsExtracted(getMaxItemsExtracted());
    }

    public boolean matchesItemFilter(ItemResource resource) {
        return getCachedFilter().matchesItem(resource);
    }

    public ItemResource getFilter(int idx) {
        return filters.get(idx);
    }

    public void setFilter(int idx, ItemResource resource) {
        if (!resource.equals(this.filters.get(idx))) {
            this.filters.set(idx, resource);
            setChangedCallback.run();
            resetCachedFilter();
        }
    }

    public double getItemSpeedupFactor() {
        return upgradeContainer.getItemSpeedupFactor();
    }

    public int getItemOperationTickDelay() {
        return upgradeContainer.getItemOperationTickDelay();
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public @Nullable ExtendedMenuProvider createMenu(PipeBlockEntity pipe, Direction side) {
        return new ExtendedMenuProvider() {
            @Override
            public void writeScreenOpeningData(RegistryFriendlyByteBuf buf) {
                AttachmentMenuType.writeScreenOpeningData(pipe, side, ItemAttachedIo.this, buf);
            }

            @Override
            public Component getDisplayName() {
                return ItemAttachedIo.this.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory pPlayerInventory, Player pPlayer) {
                return new ItemAttachedIoMenu(syncId, pPlayerInventory, pipe, side, ItemAttachedIo.this);
            }
        };
    }

    @Override
    public AttachmentModelData getModelData() {
        if (isStuffed()) {
            return AttachmentModelData.from(getItem().attachment.getStuffed(), getItem());
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

    @Override
    public boolean tryClearContents(PipeBlockEntity pipe) {
        if (isStuffed()) {
            List<ItemStack> drops = new ArrayList<>();
            for (var entry : stuffedItems.entrySet()) {
                DropHelper.splitIntoStacks(entry.getKey(), entry.getValue(), drops::add);
            }
            stuffedItems.clear();
            DropHelper.dropStacks(pipe, drops);
            pipe.setChanged();
            pipe.sync();
            return true;
        }
        return super.tryClearContents(pipe);
    }

    public boolean isStuffed() {
        return !stuffedItems.isEmpty();
    }

    /**
     * Returns the raw map of stuffed items, be careful.
     */
    public Map<ItemResource, Integer> getStuffedItems() {
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
        return isAdvancedBehaviorAllowed() ? filterNbt : FilterNbtMode.RESPECT_NBT;
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
        return isAdvancedBehaviorAllowed() ? routingMode : RoutingMode.CLOSEST;
    }

    public void setRoutingMode(RoutingMode mode) {
        this.routingMode = mode;
    }

    public OversendingMode getOversendingMode() {
        return isAdvancedBehaviorAllowed() ? oversendingMode : OversendingMode.PREVENT_OVERSENDING;
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

        this.maxItemsExtractedAtMax = maxItemsExtracted == getMaxItemsExtractedMaximum();
    }

    public int getMaxItemsExtractedMaximum() {
        return upgradeContainer.getItemsPerOperation();
    }

    private ItemCachedFilter getCachedFilter() {
        if (this.cachedFilter == null) {
            this.cachedFilter = new ItemCachedFilter(
                    this.filters.subList(0, getFilterSize()),
                    getFilterInversion(),
                    this.filterDamage,
                    getFilterNbt(),
                    this.filterMod);
        }
        return this.cachedFilter;
    }

    @Override
    protected void resetCachedFilter() {
        this.cachedFilter = null;
    }

    public int getRoundRobinIndex(int maxValue) {
        Preconditions.checkArgument(maxValue > 0, "maxValue > 0");
        roundRobinIndex = roundRobinIndex % maxValue;
        return roundRobinIndex;
    }

    /**
     * Ideally the increment size should correspond to the number of paths that were iterated through this time around.
     * This will ensure uniform distribution even if some paths are blocked.
     */
    public void incrementRoundRobin(int incrementSize) {
        if (getRoutingMode() == RoutingMode.ROUND_ROBIN) {
            roundRobinIndex += incrementSize;
            setChangedCallback.run();
        }
    }

    public int moveStuffedToStorage(ResourceHandler<ItemResource> targetStorage, int maxAmount) {
        int totalMoved = 0;

        for (var it = stuffedItems.entrySet().iterator(); it.hasNext() && totalMoved < maxAmount;) {
            var entry = it.next();
            int stuffedAmount = entry.getValue();
            int result;
            try (var tx = Transaction.openRoot()) {
                result = ResourceHandlerUtil.insertStacking(targetStorage, entry.getKey(), Math.min(stuffedAmount, maxAmount - totalMoved), tx);
                tx.commit();
            }
            int inserted = result;

            if (inserted > 0) {
                totalMoved += inserted;

                if (inserted < stuffedAmount) {
                    entry.setValue(stuffedAmount - inserted);
                } else {
                    it.remove();
                }
            }
        }

        return totalMoved;
    }
}
