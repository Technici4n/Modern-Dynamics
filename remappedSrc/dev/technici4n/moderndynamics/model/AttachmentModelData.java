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

import dev.technici4n.moderndynamics.attachment.RenderedAttachment;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class AttachmentModelData {
    private final String modelId;
    /**
     * Also available to make "block picking" work on the client.
     */
    private final Item item;

    private AttachmentModelData(String modelId, Item item) {
        this.modelId = modelId;
        this.item = item;
    }

    public String getModelId() {
        return modelId;
    }

    public Item getItem() {
        return item;
    }

    public NbtCompound write(NbtCompound tag) {
        tag.putString("model", modelId);
        tag.putString("item", Registry.ITEM.getKey(item).toString());
        return tag;
    }

    @Nullable
    public static AttachmentModelData from(NbtCompound tag) {
        var modelId = tag.getString("model");
        var item = Registry.ITEM.get(new Identifier(tag.getString("item")));
        if (!modelId.isEmpty()) {
            return new AttachmentModelData(modelId, item);
        }
        return null;
    }

    public static AttachmentModelData from(RenderedAttachment rendered, Item item) {
        return new AttachmentModelData(rendered.id, item);
    }
}
