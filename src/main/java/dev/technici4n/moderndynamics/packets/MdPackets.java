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
package dev.technici4n.moderndynamics.packets;

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
import dev.technici4n.moderndynamics.util.FluidVariant;
import dev.technici4n.moderndynamics.util.ItemVariant;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.function.BiConsumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MdPackets {

    public static void sendSetFilter(int syncId, int filterSlot, ItemVariant variant) {
        PacketDistributor.sendToServer(new SetItemVariant(
                syncId, filterSlot, variant));
    }

    public static void sendSetFilter(int syncId, int filterSlot, FluidVariant variant) {
        PacketDistributor.sendToServer(new SetFluidVariant(
                syncId, filterSlot, variant));
    }

    private static final CustomPacketPayload.Type<SetEnum<FilterInversionMode>> SET_FILTER_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_filter_mode"));
    private static final SetEnumHandler<FilterInversionMode> SET_FILTER_MODE_HANDLER = createSetEnumHandler(AttachedIoMenu.class,
            AttachedIoMenu::setFilterMode);

    public static void sendSetFilterMode(int syncId, FilterInversionMode filterMode) {
        sendSetEnum(syncId, SET_FILTER_MODE, filterMode);
    }

    private static final CustomPacketPayload.Type<SetEnum<FilterDamageMode>> SET_FILTER_DAMAGE = new CustomPacketPayload.Type<>(
            MdId.of("set_filter_damage"));
    private static final SetEnumHandler<FilterDamageMode> SET_FILTER_DAMAGE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterDamage);

    public static void sendSetFilterDamage(int syncId, FilterDamageMode value) {
        sendSetEnum(syncId, SET_FILTER_DAMAGE, value);
    }

    private static final CustomPacketPayload.Type<SetEnum<FilterNbtMode>> SET_FILTER_NBT = new CustomPacketPayload.Type<>(MdId.of("set_filter_nbt"));
    private static final SetEnumHandler<FilterNbtMode> SET_FILTER_NBT_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterNbt);

    public static void sendSetFilterNbt(int syncId, FilterNbtMode value) {
        sendSetEnum(syncId, SET_FILTER_NBT, value);
    }

    private static final CustomPacketPayload.Type<SetEnum<FilterModMode>> SET_FILTER_MOD = new CustomPacketPayload.Type<>(MdId.of("set_filter_mod"));
    private static final SetEnumHandler<FilterModMode> SET_FILTER_MOD_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterMod);

    public static void sendSetFilterMod(int syncId, FilterModMode value) {
        sendSetEnum(syncId, SET_FILTER_MOD, value);
    }

    private static final CustomPacketPayload.Type<SetEnum<FilterSimilarMode>> SET_FILTER_SIMILAR = new CustomPacketPayload.Type<>(
            MdId.of("set_filter_similar"));
    private static final SetEnumHandler<FilterSimilarMode> SET_FILTER_SIMILAR_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterSimilar);

    public static void sendSetFilterSimilar(int syncId, FilterSimilarMode value) {
        sendSetEnum(syncId, SET_FILTER_SIMILAR, value);
    }

    private static final CustomPacketPayload.Type<SetEnum<RoutingMode>> SET_ROUTING_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_routing_mode"));
    private static final SetEnumHandler<RoutingMode> SET_ROUTING_MODE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setRoutingMode);

    public static void sendSetRoutingMode(int syncId, RoutingMode value) {
        sendSetEnum(syncId, SET_ROUTING_MODE, value);
    }

    private static final CustomPacketPayload.Type<SetEnum<OversendingMode>> SET_OVERSENDING_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_oversending_mode"));
    private static final SetEnumHandler<OversendingMode> SET_OVERSENDING_MODE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setOversendingMode);

    public static void sendSetOversendingMode(int syncId, OversendingMode value) {
        sendSetEnum(syncId, SET_OVERSENDING_MODE, value);
    }

    private static final CustomPacketPayload.Type<SetEnum<RedstoneMode>> SET_REDSTONE_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_redstone_mode"));
    private static final SetEnumHandler<RedstoneMode> SET_REDSTONE_MODE_HANDLER = createSetEnumHandler(AttachedIoMenu.class,
            AttachedIoMenu::setRedstoneMode);

    public static void sendSetRedstoneMode(int syncId, RedstoneMode value) {
        sendSetEnum(syncId, SET_REDSTONE_MODE, value);
    }

    private static final CustomPacketPayload.Type<SetInt> SET_MAX_ITEMS_IN_INVENTORY = new CustomPacketPayload.Type<>(
            MdId.of("set_max_items_in_inventory"));
    private static final SetIntHandler SET_MAX_ITEMS_IN_INVENTORY_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsInInventory);

    public static void sendSetMaxItemsInInventory(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_IN_INVENTORY, value);
    }

    private static final CustomPacketPayload.Type<SetInt> SET_MAX_ITEMS_EXTRACTED = new CustomPacketPayload.Type<>(
            MdId.of("set_max_items_extracted"));
    private static final SetIntHandler SET_MAX_ITEMS_EXTRACTED_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsExtracted);

    public static void sendSetMaxItemsExtracted(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_EXTRACTED, value);
    }

    private static <T extends Enum<T>> void sendSetEnum(int syncId, CustomPacketPayload.Type<SetEnum<T>> packetType, T enumValue) {
        PacketDistributor.sendToServer(new SetEnum<>(packetType, syncId, enumValue));
    }

    private static <T extends Enum<T>, M extends AbstractContainerMenu> SetEnumHandler<T> createSetEnumHandler(Class<M> menuClass,
            EnumSetter<T, M> setter) {
        return (player, syncId, value) -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId) {
                setter.setEnum(menuClass.cast(handler), value, false);
            }
        };
    }

    private static void sendSetInt(int syncId, CustomPacketPayload.Type<SetInt> packetType, int value) {
        PacketDistributor.sendToServer(new SetInt(packetType, syncId, value));
    }

    private static SetIntHandler createSetIntHandler(IntSetter setter) {
        return (player, syncId, value) -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId && handler instanceof ItemAttachedIoMenu attachmentMenu) {
                setter.setInt(attachmentMenu, value, false);
            }
        };
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(SetAttachmentUpgrades.TYPE, SetAttachmentUpgrades.STREAM_CODEC, SetAttachmentUpgrades.HANDLER);

        registrar.playBidirectional(SetItemVariant.TYPE, SetItemVariant.STREAM_CODEC, handler(MdPackets::handleSetItemVariant));
        registrar.playBidirectional(SetFluidVariant.TYPE, SetFluidVariant.STREAM_CODEC, handler(MdPackets::handleSetFluidVariant));

        registerSetEnum(registrar, SET_FILTER_MODE, FilterInversionMode.class, SET_FILTER_MODE_HANDLER);
        registerSetEnum(registrar, SET_FILTER_DAMAGE, FilterDamageMode.class, SET_FILTER_DAMAGE_HANDLER);
        registerSetEnum(registrar, SET_FILTER_NBT, FilterNbtMode.class, SET_FILTER_NBT_HANDLER);
        registerSetEnum(registrar, SET_FILTER_MOD, FilterModMode.class, SET_FILTER_MOD_HANDLER);
        registerSetEnum(registrar, SET_FILTER_SIMILAR, FilterSimilarMode.class, SET_FILTER_SIMILAR_HANDLER);
        registerSetEnum(registrar, SET_ROUTING_MODE, RoutingMode.class, SET_ROUTING_MODE_HANDLER);
        registerSetEnum(registrar, SET_OVERSENDING_MODE, OversendingMode.class, SET_OVERSENDING_MODE_HANDLER);
        registerSetEnum(registrar, SET_REDSTONE_MODE, RedstoneMode.class, SET_REDSTONE_MODE_HANDLER);
        registerSetInt(registrar, MdPackets.SET_MAX_ITEMS_IN_INVENTORY, MdPackets.SET_MAX_ITEMS_IN_INVENTORY_HANDLER);
        registerSetInt(registrar, MdPackets.SET_MAX_ITEMS_EXTRACTED, MdPackets.SET_MAX_ITEMS_EXTRACTED_HANDLER);
    }

    private static <T extends Enum<T>> void registerSetEnum(PayloadRegistrar registrar, CustomPacketPayload.Type<SetEnum<T>> type, Class<T> enumClass,
            SetEnumHandler<T> handler) {
        registrar.playBidirectional(type, SetEnum.codec(type, enumClass), handler((player, payload) -> {
            handler.handleSetEnum(player, payload.syncId(), payload.value());
        }));

    }

    private static void registerSetInt(PayloadRegistrar registrar, CustomPacketPayload.Type<SetInt> type, SetIntHandler handler) {
        registrar.playBidirectional(type, SetInt.codec(type), handler((player, payload) -> {
            handler.handleSetInt(player, payload.syncId(), payload.value());
        }));

    }

    private static void handleSetItemVariant(Player player, SetItemVariant payload) {
        AbstractContainerMenu handler = player.containerMenu;
        if (handler.containerId == payload.syncId() && handler instanceof ItemAttachedIoMenu attachmentMenu) {
            attachmentMenu.setFilter(payload.configIdx(), payload.variant(), false);
        }
    }

    private static void handleSetFluidVariant(Player player, SetFluidVariant payload) {
        AbstractContainerMenu handler = player.containerMenu;
        if (handler.containerId == payload.syncId() && handler instanceof FluidAttachedIoMenu attachmentMenu) {
            attachmentMenu.setFilter(payload.configIdx(), payload.variant(), false);
        }
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> handler(BiConsumer<Player, T> handler) {
        return (payload, context) -> handler.accept(context.player(), payload);
    }

    private interface EnumSetter<T extends Enum<T>, M extends AbstractContainerMenu> {
        void setEnum(M menu, T value, boolean sendPacket);
    }

    private interface IntSetter {
        void setInt(ItemAttachedIoMenu menu, int value, boolean sendPacket);
    }

    @FunctionalInterface
    interface SetEnumHandler<T> {
        void handleSetEnum(Player player, int syncId, T enumValue);
    }

    @FunctionalInterface
    interface SetIntHandler {
        void handleSetInt(Player player, int syncId, int value);
    }
}
