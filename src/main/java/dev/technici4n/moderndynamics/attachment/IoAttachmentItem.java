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
package dev.technici4n.moderndynamics.attachment;

import dev.technici4n.moderndynamics.attachment.attached.AttachedIO;
import net.minecraft.nbt.CompoundTag;

public class IoAttachmentItem extends AttachmentItem {
    private final AttachmentTier tier;
    private final IoAttachmentType type;

    public IoAttachmentItem(RenderedAttachment attachment, AttachmentTier tier, IoAttachmentType type) {
        super(attachment);
        this.tier = tier;
        this.type = type;
    }

    public AttachmentTier getTier() {
        return tier;
    }

    public IoAttachmentType getType() {
        return type;
    }

    @Override
    public AttachedIO createAttached(CompoundTag configTag) {
        return new AttachedIO(this, configTag);
    }
}
