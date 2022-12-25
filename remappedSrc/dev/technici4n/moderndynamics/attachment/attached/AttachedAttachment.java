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
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
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

    public AttachedAttachment(AttachmentItem item, NbtCompound configData) {
        this.item = item;
    }

    public AttachmentItem getItem() {
        return item;
    }

    public ItemStack toStack() {
        var stack = new ItemStack(item);
        var tag = stack.getOrCreateNbt();
        writeConfigTag(tag);
        if (tag.isEmpty()) {
            stack.setNbt(null);
        }
        return stack;
    }

    public List<ItemStack> getDrops() {
        return List.of(new ItemStack(item));
    }

    public Text getDisplayName() {
        return item.getName();
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
    public boolean update(NbtCompound data) {
        return true;
    }

    @MustBeInvokedByOverriders
    public NbtCompound writeConfigTag(NbtCompound configData) {
        return configData;
    }

    public boolean hasMenu() {
        return false;
    }

    @Nullable
    public NamedScreenHandlerFactory createMenu(PipeBlockEntity pipe, Direction side) {
        return null;
    }

    public void onPlaced(PlayerEntity player) {
    }
}
