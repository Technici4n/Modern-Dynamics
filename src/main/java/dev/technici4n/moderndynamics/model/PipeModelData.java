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
package dev.technici4n.moderndynamics.model;

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

public record PipeModelData(byte pipeConnections, byte inventoryConnections,
        AttachmentModelData @Nullable [] attachments) {

    public static ModelProperty<PipeModelData> PIPE_DATA = new ModelProperty<>();

    public static final PipeModelData DEFAULT = new PipeModelData((byte) 0, (byte) 0, new AttachmentModelData[6]);

    public PipeModelData(byte pipeConnections, byte inventoryConnections, AttachedAttachment[] attachments) {
        this(pipeConnections, inventoryConnections, getAttachmentModelData(attachments));
    }

    private static AttachmentModelData[] getAttachmentModelData(AttachedAttachment[] attachments) {
        var result = new AttachmentModelData[attachments.length];
        for (int i = 0; i < result.length; i++) {
            if (attachments[i] != null) {
                result[i] = attachments[i].getModelData();
            }
        }
        return result;
    }

}
