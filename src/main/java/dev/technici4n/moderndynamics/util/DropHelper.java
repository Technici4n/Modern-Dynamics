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
package dev.technici4n.moderndynamics.util;

import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DropHelper {
    public static void dropStacks(BlockEntity blockEntity, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            dropStack(blockEntity, stack);
        }
    }

    public static void dropStack(BlockEntity blockEntity, ItemStack stack) {
        var pos = blockEntity.getBlockPos();
        Containers.dropItemStack(blockEntity.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    public static void dropStack(BlockEntity blockEntity, ItemVariant variant, long amount) {
        while (amount > 0) {
            int dropped = (int) Math.min(amount, variant.getItem().getMaxStackSize());
            dropStack(blockEntity, variant.toStack(dropped));
            amount -= dropped;
        }
    }
}
