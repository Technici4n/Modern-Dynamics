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
import dev.technici4n.moderndynamics.model.AttachmentModelData;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.ExtendedMenuProvider;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * Base interface for an active attachment on a pipe.
 */
public class AttachedAttachment {
    /**
     * The item that is attached.
     */
    private final AttachmentItem item;

    public AttachedAttachment(AttachmentItem item, CompoundTag configData) {
        this.item = item;
    }

    public AttachmentItem getItem() {
        return item;
    }

    public List<ItemStack> getDrops() {
        return List.of(new ItemStack(item));
    }

    /**
     * Try to clear contents of the attachment,
     * for example when the player right-clicks with a wrench.
     *
     * @return {@code true} if there was something to clear
     */
    public boolean tryClearContents(PipeBlockEntity pipe) {
        return false;
    }

    public Component getDisplayName() {
        return item.getDescription();
    }

    public AttachmentModelData getModelData() {
        return AttachmentModelData.from(getItem().attachment, getItem());
    }

    public boolean allowsItemConnection() {
        // Servos prevent connections using the transport item
        return !(getItem() instanceof IoAttachmentItem ticking) || ticking.getType() != IoAttachmentType.EXTRACTOR;
    }

    /**
     * Update an existing attached attachment with new data from the server.
     *
     * @return True if an update is needed.
     */
    public boolean update(CompoundTag data) {
        return true;
    }

    @MustBeInvokedByOverriders
    public CompoundTag writeConfigTag(CompoundTag configData, HolderLookup.Provider registries) {
        return configData;
    }

    public boolean hasMenu() {
        return false;
    }

    @Nullable
    public ExtendedMenuProvider createMenu(PipeBlockEntity pipe, Direction side) {
        return null;
    }

    public void onPlaced(Player player) {
    }
}
