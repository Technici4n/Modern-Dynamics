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
import dev.technici4n.moderndynamics.attachment.ConfigurableAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.model.AttachmentModelData;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

/**
 * Base interface for an active attachment on a pipe.
 */
public class AttachedAttachment {
    /**
     * The pipe this is attached to.
     */
    private final PipeBlockEntity pipe;

    /**
     * Which side of {@link #pipe} is this attached to.
     */
    private final Direction side;

    /**
     * The item that is attached.
     */
    private final AttachmentItem item;

    public AttachedAttachment(PipeBlockEntity pipe, Direction side, AttachmentItem item, NbtCompound initialData) {
        this.pipe = pipe;
        this.side = side;
        this.item = item;
    }

    public AttachmentItem getItem() {
        return item;
    }

    public PipeBlockEntity getPipe() {
        return pipe;
    }

    public Direction getSide() {
        return side;
    }

    public void writeNbt(NbtCompound tag) {
    }

    public ItemStack toStack() {
        var stack = new ItemStack(item);
        var tag = stack.getOrCreateNbt();
        writeNbt(tag);
        if (tag.isEmpty()) {
            stack.setNbt(null);
        }
        return stack;
    }

    public List<ItemStack> getDrops() {
        return Collections.singletonList(toStack());
    }

    public boolean hasScreen() {
        return getItem() instanceof ConfigurableAttachmentItem;
    }

    public Text getDisplayName() {
        return new LiteralText("");
    }

    public int getConfigHeight() {
        return 0;
    }

    public int getConfigWidth() {
        return 0;
    }

    public ItemVariant getFilter(int x, int y) {
        return null;
    }

    public void setFilter(int configX, int configY, ItemVariant variant) {

    }

    public AttachmentModelData getModelData() {
        return new AttachmentModelData(getItem().attachment.id);
    }

    public boolean isAttached() {
        return pipe.getAttachment(side) == this;
    }

    public boolean allowsItemConnection() {
        // Servos prevent connections using the transport item
        return !(getItem() instanceof IoAttachmentItem ticking) || !ticking.isServo();
    }

    /**
     * Update an existing attached attachment with new data from the server.
     *
     * @return True if an update is needed.
     */
    public boolean update(NbtCompound data) {
        return true;
    }
}
