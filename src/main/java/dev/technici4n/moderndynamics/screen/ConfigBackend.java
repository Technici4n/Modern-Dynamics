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
package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.attachment.ConfigurableAttachmentItem;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.PacketByteBuf;

public interface ConfigBackend {
    ConfigurableAttachmentItem getAttachment();

    ItemVariant getItemVariant(int x, int y);

    void setItemVariant(int x, int y, ItemVariant variant);

    static ConfigBackend makeClient(PacketByteBuf buf) {
        var attachment = buf.readItemStack();
        return new ConfigBackend() {
            @Override
            public ConfigurableAttachmentItem getAttachment() {
                return (ConfigurableAttachmentItem) attachment.getItem();
            }

            @Override
            public ItemVariant getItemVariant(int x, int y) {
                return getAttachment().getItemVariant(attachment, x, y);
            }

            @Override
            public void setItemVariant(int x, int y, ItemVariant variant) {
                getAttachment().setItemVariant(attachment, x, y, variant);
            }
        };
    }
}
