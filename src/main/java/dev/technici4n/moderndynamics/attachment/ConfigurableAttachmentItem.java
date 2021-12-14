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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;

public class ConfigurableAttachmentItem extends AttachmentItem {
    public final int configWidth, configHeight;

    public ConfigurableAttachmentItem(RenderedAttachment attachment, int configWidth, int configHeight) {
        super(attachment);
        this.configWidth = configWidth;
        this.configHeight = configHeight;
    }

    public ItemVariant getItemVariant(ItemStack stack, int x, int y) {
        var nbt = stack.getOrCreateNbt();
        var list = nbt.getList("items", NbtElement.COMPOUND_TYPE);
        return ItemVariant.fromNbt(list.getCompound(x + y * configWidth));
    }

    public void setItemVariant(ItemStack stack, int x, int y, ItemVariant variant) {
        var nbt = stack.getOrCreateNbt();
        var list = nbt.getList("items", NbtElement.COMPOUND_TYPE);
        nbt.put("items", list);
        while (list.size() < configWidth * configHeight) {
            list.add(ItemVariant.blank().toNbt());
        }
        list.set(x + y * configWidth, variant.toNbt());
    }

    public boolean matchesFilter(ItemStack stack, ItemVariant variant) {
        boolean isEmpty = true;
        for (int i = 0; i < configHeight; ++i) {
            for (int j = 0; j < configWidth; ++j) {
                ItemVariant v = getItemVariant(stack, i, j);
                if (!v.isBlank()) {
                    if (v.equals(variant)) {
                        return true;
                    }
                    isEmpty = false;
                }
            }
        }
        return isEmpty;
    }
}
