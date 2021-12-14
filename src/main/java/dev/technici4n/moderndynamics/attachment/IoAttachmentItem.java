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

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.attachment.attached.AttachedIO;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class IoAttachmentItem extends ConfigurableAttachmentItem {
    public final AttachmentTier tier;
    private final boolean servo;

    public IoAttachmentItem(RenderedAttachment attachment, AttachmentTier tier, boolean servo) {
        super(attachment, tier.configWidth, tier.configHeight);
        this.tier = tier;
        this.servo = servo;
    }

    public boolean isServo() {
        return servo;
    }

    public boolean isRetriever() {
        return !servo;
    }

    @Override
    public AttachedAttachment createAttached(PipeBlockEntity pipe, Direction side, CompoundTag initialData) {
        return new AttachedIO(pipe, side, this, initialData);
    }
}
