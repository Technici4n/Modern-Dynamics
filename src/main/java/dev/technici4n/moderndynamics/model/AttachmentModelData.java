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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Item is also available to make "block picking" work on the client.
 */
public record AttachmentModelData(String modelId, Item item) {
    public CompoundTag write(CompoundTag tag) {
        tag.putString("model", modelId);
        tag.putString("item", BuiltInRegistries.ITEM.getKey(item).toString());
        return tag;
    }

    @Nullable
    public static AttachmentModelData from(CompoundTag tag) {
        var modelId = tag.getString("model");
        var item = BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("item")));
        if (!modelId.isEmpty()) {
            return new AttachmentModelData(modelId, item);
        }
        return null;
    }

    public static AttachmentModelData from(RenderedAttachment rendered, Item item) {
        return new AttachmentModelData(rendered.id, item);
    }
}
