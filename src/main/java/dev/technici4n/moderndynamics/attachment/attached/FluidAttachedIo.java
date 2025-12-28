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

import com.mojang.serialization.Codec;
import dev.technici4n.moderndynamics.Constants;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.gui.menu.AttachmentMenuType;
import dev.technici4n.moderndynamics.gui.menu.FluidAttachedIoMenu;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.ExtendedMenuProvider;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

// TODO: also allow nbt filtering
public class FluidAttachedIo extends AttachedIo {
    private static final Codec<List<FluidResource>> FILTER_LIST_CODEC = FluidResource.OPTIONAL_CODEC.listOf(0, Constants.Upgrades.MAX_FILTER);

    private final NonNullList<FluidResource> filters;
    @Nullable
    private FluidCachedFilter cachedFilter = null;

    public FluidAttachedIo(IoAttachmentItem item, ValueInput configData, Runnable setChangedCallback) {
        super(item, configData, setChangedCallback);

        this.filters = NonNullList.withSize(Constants.Upgrades.MAX_FILTER, FluidResource.EMPTY);
        var filterTags = configData.read("filters", FILTER_LIST_CODEC);
        filterTags.ifPresent(filters::addAll);
    }

    @Override
    public void writeConfigTag(ValueOutput output) {
        super.writeConfigTag(output);
        output.store("filters", FILTER_LIST_CODEC, filters);
    }

    @Override
    protected void resetCachedFilter() {
        cachedFilter = null;
    }

    public FluidResource getFilter(int idx) {
        return filters.get(idx);
    }

    public void setFilter(int idx, FluidResource variant) {
        if (!variant.equals(this.filters.get(idx))) {
            this.filters.set(idx, variant);
            setChangedCallback.run();
            resetCachedFilter();
        }
    }

    public boolean matchesFilter(FluidResource variant) {
        if (cachedFilter == null) {
            cachedFilter = new FluidCachedFilter(filters.subList(0, getFilterSize()), getFilterInversion());
        }
        return cachedFilter.matches(variant);
    }

    public int getFluidMaxIo() {
        return upgradeContainer.getFluidMaxIo();
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
                AttachmentMenuType.writeScreenOpeningData(pipe, side, FluidAttachedIo.this, buf);
            }

            @Override
            public Component getDisplayName() {
                return FluidAttachedIo.this.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory pPlayerInventory, Player pPlayer) {
                return new FluidAttachedIoMenu(syncId, pPlayerInventory, pipe, side, FluidAttachedIo.this);
            }
        };
    }
}
