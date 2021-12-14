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
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class AttachedIO extends AttachedAttachment {
    public AttachedIO(PipeBlockEntity pipe, Direction side, IoAttachmentItem item, CompoundTag initialData) {
        super(pipe, side, item, initialData);
    }

    @Override
    public IoAttachmentItem getItem() {
        return (IoAttachmentItem) super.getItem();
    }

    public boolean isServo() {
        return getItem().isServo();
    }

    public AttachmentTier getTier() {
        return getItem().tier;
    }

    public boolean matchesItemFilter(ItemVariant variant) {
        return false;
    }
}
