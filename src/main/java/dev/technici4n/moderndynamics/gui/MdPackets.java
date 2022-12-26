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
package dev.technici4n.moderndynamics.gui;

import dev.technici4n.moderndynamics.MdProxy;
import dev.technici4n.moderndynamics.attachment.settings.FilterDamageMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterModMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterNbtMode;
import dev.technici4n.moderndynamics.attachment.settings.FilterSimilarMode;
import dev.technici4n.moderndynamics.attachment.settings.OversendingMode;
import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import dev.technici4n.moderndynamics.attachment.settings.RoutingMode;
import dev.technici4n.moderndynamics.gui.menu.AttachedIoMenu;
import dev.technici4n.moderndynamics.gui.menu.FluidAttachedIoMenu;
import dev.technici4n.moderndynamics.gui.menu.ItemAttachedIoMenu;
import dev.technici4n.moderndynamics.util.MdId;
import dev.technici4n.moderndynamics.util.UnsidedPacketHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MdPackets {
    public static final ResourceLocation SET_ATTACHMENT_UPGRADES = MdId.of("set_attachment_upgrades");

    public static final ResourceLocation SET_ITEM_VARIANT = MdId.of("set_item_variant");
    public static final UnsidedPacketHandler SET_ITEM_VARIANT_HANDLER = (player, buf) -> {
        int syncId = buf.readInt();
        int configIdx = buf.readInt();
        ItemVariant variant = ItemVariant.fromPacket(buf);
        return () -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId && handler instanceof ItemAttachedIoMenu attachmentMenu) {
                attachmentMenu.setFilter(configIdx, variant, false);
            }
        };
    };

    public static void sendSetFilter(int syncId, int filterSlot, ItemVariant variant) {
        var buffer = PacketByteBufs.create();
        buffer.writeInt(syncId);
        buffer.writeInt(filterSlot);
        variant.toPacket(buffer);
        MdProxy.INSTANCE.sendPacket(SET_ITEM_VARIANT, buffer);
    }

    public static final ResourceLocation SET_FLUID_VARIANT = MdId.of("set_fluid_variant");
    public static final UnsidedPacketHandler SET_FLUID_VARIANT_HANDLER = (player, buf) -> {
        int syncId = buf.readInt();
        int configIdx = buf.readInt();
        FluidVariant variant = FluidVariant.fromPacket(buf);
        return () -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId && handler instanceof FluidAttachedIoMenu attachmentMenu) {
                attachmentMenu.setFilter(configIdx, variant, false);
            }
        };
    };

    public static void sendSetFilter(int syncId, int filterSlot, FluidVariant variant) {
        var buffer = PacketByteBufs.create();
        buffer.writeInt(syncId);
        buffer.writeInt(filterSlot);
        variant.toPacket(buffer);
        MdProxy.INSTANCE.sendPacket(SET_FLUID_VARIANT, buffer);
    }

    public static final ResourceLocation SET_FILTER_MODE = MdId.of("set_filter_mode");
    public static final UnsidedPacketHandler SET_FILTER_MODE_HANDLER = createSetEnumHandler(FilterInversionMode.class, AttachedIoMenu.class,
            AttachedIoMenu::setFilterMode);

    public static void sendSetFilterMode(int syncId, FilterInversionMode filterMode) {
        sendSetEnum(syncId, SET_FILTER_MODE, filterMode);
    }

    public static final ResourceLocation SET_FILTER_DAMAGE = MdId.of("set_filter_damage");
    public static final UnsidedPacketHandler SET_FILTER_DAMAGE_HANDLER = createSetEnumHandler(FilterDamageMode.class,
            ItemAttachedIoMenu.class, ItemAttachedIoMenu::setFilterDamage);

    public static void sendSetFilterDamage(int syncId, FilterDamageMode value) {
        sendSetEnum(syncId, SET_FILTER_DAMAGE, value);
    }

    public static final ResourceLocation SET_FILTER_NBT = MdId.of("set_filter_nbt");
    public static final UnsidedPacketHandler SET_FILTER_NBT_HANDLER = createSetEnumHandler(FilterNbtMode.class, ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterNbt);

    public static void sendSetFilterNbt(int syncId, FilterNbtMode value) {
        sendSetEnum(syncId, SET_FILTER_NBT, value);
    }

    public static final ResourceLocation SET_FILTER_MOD = MdId.of("set_filter_mod");
    public static final UnsidedPacketHandler SET_FILTER_MOD_HANDLER = createSetEnumHandler(FilterModMode.class, ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterMod);

    public static void sendSetFilterMod(int syncId, FilterModMode value) {
        sendSetEnum(syncId, SET_FILTER_MOD, value);
    }

    public static final ResourceLocation SET_FILTER_SIMILAR = MdId.of("set_filter_similar");
    public static final UnsidedPacketHandler SET_FILTER_SIMILAR_HANDLER = createSetEnumHandler(FilterSimilarMode.class,
            ItemAttachedIoMenu.class, ItemAttachedIoMenu::setFilterSimilar);

    public static void sendSetFilterSimilar(int syncId, FilterSimilarMode value) {
        sendSetEnum(syncId, SET_FILTER_SIMILAR, value);
    }

    public static final ResourceLocation SET_ROUTING_MODE = MdId.of("set_routing_mode");
    public static final UnsidedPacketHandler SET_ROUTING_MODE_HANDLER = createSetEnumHandler(RoutingMode.class, ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setRoutingMode);

    public static void sendSetRoutingMode(int syncId, RoutingMode value) {
        sendSetEnum(syncId, SET_ROUTING_MODE, value);
    }

    public static final ResourceLocation SET_OVERSENDING_MODE = MdId.of("set_oversending_mode");
    public static final UnsidedPacketHandler SET_OVERSENDING_MODE_HANDLER = createSetEnumHandler(OversendingMode.class,
            ItemAttachedIoMenu.class, ItemAttachedIoMenu::setOversendingMode);

    public static void sendSetOversendingMode(int syncId, OversendingMode value) {
        sendSetEnum(syncId, SET_OVERSENDING_MODE, value);
    }

    public static final ResourceLocation SET_REDSTONE_MODE = MdId.of("set_redstone_mode");
    public static final UnsidedPacketHandler SET_REDSTONE_MODE_HANDLER = createSetEnumHandler(RedstoneMode.class, AttachedIoMenu.class,
            AttachedIoMenu::setRedstoneMode);

    public static void sendSetRedstoneMode(int syncId, RedstoneMode value) {
        sendSetEnum(syncId, SET_REDSTONE_MODE, value);
    }

    public static final ResourceLocation SET_MAX_ITEMS_IN_INVENTORY = MdId.of("set_max_items_in_inventory");
    public static final UnsidedPacketHandler SET_MAX_ITEMS_IN_INVENTORY_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsInInventory);

    public static void sendSetMaxItemsInInventory(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_IN_INVENTORY, value);
    }

    public static final ResourceLocation SET_MAX_ITEMS_EXTRACTED = MdId.of("set_max_items_extracted");
    public static final UnsidedPacketHandler SET_MAX_ITEMS_EXTRACTED_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsExtracted);

    public static void sendSetMaxItemsExtracted(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_EXTRACTED, value);
    }

    private static <T extends Enum<T>> void sendSetEnum(int syncId, ResourceLocation packetId, T enumValue) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer(64));
        buffer.writeInt(syncId);
        buffer.writeEnum(enumValue);
        MdProxy.INSTANCE.sendPacket(packetId, buffer);
    }

    private static <T extends Enum<T>, M extends AbstractContainerMenu> UnsidedPacketHandler createSetEnumHandler(Class<T> enumClass,
            Class<M> menuClass, EnumSetter<T, M> setter) {
        return (player, buf) -> {
            int syncId = buf.readInt();
            var enumValue = buf.readEnum(enumClass);
            return () -> {
                AbstractContainerMenu handler = player.containerMenu;
                if (handler.containerId == syncId) {
                    setter.setEnum(menuClass.cast(handler), enumValue, false);
                }
            };
        };
    }

    private static void sendSetInt(int syncId, ResourceLocation packetId, int value) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer(64));
        buffer.writeInt(syncId);
        buffer.writeInt(value);
        MdProxy.INSTANCE.sendPacket(packetId, buffer);
    }

    private static UnsidedPacketHandler createSetIntHandler(IntSetter setter) {
        return (player, buf) -> {
            int syncId = buf.readInt();
            var value = buf.readInt();
            return () -> {
                AbstractContainerMenu handler = player.containerMenu;
                if (handler.containerId == syncId && handler instanceof ItemAttachedIoMenu attachmentMenu) {
                    setter.setInt(attachmentMenu, value, false);
                }
            };
        };
    }

    private interface EnumSetter<T extends Enum<T>, M extends AbstractContainerMenu> {
        void setEnum(M menu, T value, boolean sendPacket);
    }

    private interface IntSetter {
        void setInt(ItemAttachedIoMenu menu, int value, boolean sendPacket);
    }
}
