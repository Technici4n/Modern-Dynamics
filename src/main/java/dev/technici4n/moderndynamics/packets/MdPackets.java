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
import dev.technici4n.moderndynamics.util.MdId;
import java.util.function.BiConsumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class MdPackets {

    public static void sendSetFilter(int syncId, int filterSlot, ItemResource resource) {
        ClientPacketDistributor.sendToServer(new SetItemResource(
                syncId, filterSlot, resource));
    }

    public static void sendSetFilter(int syncId, int filterSlot, FluidResource resource) {
        ClientPacketDistributor.sendToServer(new SetFluidResource(
                syncId, filterSlot, resource));
    }

    public static final CustomPacketPayload.Type<SetEnum<FilterInversionMode>> SET_FILTER_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_filter_mode"));
    public static final IPayloadHandler<SetEnum<FilterInversionMode>> SET_FILTER_MODE_HANDLER = createSetEnumHandler(AttachedIoMenu.class,
            AttachedIoMenu::setFilterMode);

    public static void sendSetFilterMode(int syncId, FilterInversionMode filterMode) {
        sendSetEnum(syncId, SET_FILTER_MODE, filterMode);
    }

    public static final CustomPacketPayload.Type<SetEnum<FilterDamageMode>> SET_FILTER_DAMAGE = new CustomPacketPayload.Type<>(
            MdId.of("set_filter_damage"));
    public static final IPayloadHandler<SetEnum<FilterDamageMode>> SET_FILTER_DAMAGE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterDamage);

    public static void sendSetFilterDamage(int syncId, FilterDamageMode value) {
        sendSetEnum(syncId, SET_FILTER_DAMAGE, value);
    }

    public static final CustomPacketPayload.Type<SetEnum<FilterNbtMode>> SET_FILTER_NBT = new CustomPacketPayload.Type<>(MdId.of("set_filter_nbt"));
    public static final IPayloadHandler<SetEnum<FilterNbtMode>> SET_FILTER_NBT_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterNbt);

    public static void sendSetFilterNbt(int syncId, FilterNbtMode value) {
        sendSetEnum(syncId, SET_FILTER_NBT, value);
    }

    public static final CustomPacketPayload.Type<SetEnum<FilterModMode>> SET_FILTER_MOD = new CustomPacketPayload.Type<>(MdId.of("set_filter_mod"));
    public static final IPayloadHandler<SetEnum<FilterModMode>> SET_FILTER_MOD_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterMod);

    public static void sendSetFilterMod(int syncId, FilterModMode value) {
        sendSetEnum(syncId, SET_FILTER_MOD, value);
    }

    public static final CustomPacketPayload.Type<SetEnum<FilterSimilarMode>> SET_FILTER_SIMILAR = new CustomPacketPayload.Type<>(
            MdId.of("set_filter_similar"));
    public static final IPayloadHandler<SetEnum<FilterSimilarMode>> SET_FILTER_SIMILAR_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setFilterSimilar);

    public static void sendSetFilterSimilar(int syncId, FilterSimilarMode value) {
        sendSetEnum(syncId, SET_FILTER_SIMILAR, value);
    }

    public static final CustomPacketPayload.Type<SetEnum<RoutingMode>> SET_ROUTING_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_routing_mode"));
    public static final IPayloadHandler<SetEnum<RoutingMode>> SET_ROUTING_MODE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setRoutingMode);

    public static void sendSetRoutingMode(int syncId, RoutingMode value) {
        sendSetEnum(syncId, SET_ROUTING_MODE, value);
    }

    public static final CustomPacketPayload.Type<SetEnum<OversendingMode>> SET_OVERSENDING_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_oversending_mode"));
    public static final IPayloadHandler<SetEnum<OversendingMode>> SET_OVERSENDING_MODE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class,
            ItemAttachedIoMenu::setOversendingMode);

    public static void sendSetOversendingMode(int syncId, OversendingMode value) {
        sendSetEnum(syncId, SET_OVERSENDING_MODE, value);
    }

    public static final CustomPacketPayload.Type<SetEnum<RedstoneMode>> SET_REDSTONE_MODE = new CustomPacketPayload.Type<>(
            MdId.of("set_redstone_mode"));
    public static final IPayloadHandler<SetEnum<RedstoneMode>> SET_REDSTONE_MODE_HANDLER = createSetEnumHandler(AttachedIoMenu.class,
            AttachedIoMenu::setRedstoneMode);

    public static void sendSetRedstoneMode(int syncId, RedstoneMode value) {
        sendSetEnum(syncId, SET_REDSTONE_MODE, value);
    }

    public static final CustomPacketPayload.Type<SetInt> SET_MAX_ITEMS_IN_INVENTORY = new CustomPacketPayload.Type<>(
            MdId.of("set_max_items_in_inventory"));
    public static final IPayloadHandler<SetInt> SET_MAX_ITEMS_IN_INVENTORY_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsInInventory);

    public static void sendSetMaxItemsInInventory(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_IN_INVENTORY, value);
    }

    public static final CustomPacketPayload.Type<SetInt> SET_MAX_ITEMS_EXTRACTED = new CustomPacketPayload.Type<>(
            MdId.of("set_max_items_extracted"));
    public static final IPayloadHandler<SetInt> SET_MAX_ITEMS_EXTRACTED_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsExtracted);

    public static final IPayloadHandler<SetItemResource> SET_ITEM_RESOURCE_HANDLER = handler(MdPackets::handleSetItemResource);
    public static final IPayloadHandler<SetFluidResource> SET_FLUID_RESOURCE_HANDLER = handler(MdPackets::handleSetFluidResource);

    public static void sendSetMaxItemsExtracted(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_EXTRACTED, value);
    }

    private static <T extends Enum<T>> void sendSetEnum(int syncId, CustomPacketPayload.Type<SetEnum<T>> packetType, T enumValue) {
        ClientPacketDistributor.sendToServer(new SetEnum<>(packetType, syncId, enumValue));
    }

    private static <T extends Enum<T>, M extends AbstractContainerMenu> IPayloadHandler<SetEnum<T>> createSetEnumHandler(Class<M> menuClass,
            EnumSetter<T, M> setter) {
        return handler((player, payload) -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == payload.syncId()) {
                setter.setEnum(menuClass.cast(handler), payload.value(), false);
            }
        });
    }

    private static void sendSetInt(int syncId, CustomPacketPayload.Type<SetInt> packetType, int value) {
        ClientPacketDistributor.sendToServer(new SetInt(packetType, syncId, value));
    }

    private static IPayloadHandler<SetInt> createSetIntHandler(IntSetter setter) {
        return handler((player, payload) -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == payload.syncId() && handler instanceof ItemAttachedIoMenu attachmentMenu) {
                setter.setInt(attachmentMenu, payload.value(), false);
            }
        });
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(SetAttachmentUpgrades.TYPE, SetAttachmentUpgrades.STREAM_CODEC);

        registrar.playBidirectional(SetItemResource.TYPE, SetItemResource.STREAM_CODEC, SET_ITEM_RESOURCE_HANDLER);
        registrar.playBidirectional(SetFluidResource.TYPE, SetFluidResource.STREAM_CODEC, SET_FLUID_RESOURCE_HANDLER);
        registrar.playBidirectional(SET_FILTER_MODE, SetEnum.codec(SET_FILTER_MODE, FilterInversionMode.class), SET_FILTER_MODE_HANDLER);
        registrar.playBidirectional(SET_FILTER_DAMAGE, SetEnum.codec(SET_FILTER_DAMAGE, FilterDamageMode.class), SET_FILTER_DAMAGE_HANDLER);
        registrar.playBidirectional(SET_FILTER_NBT, SetEnum.codec(SET_FILTER_NBT, FilterNbtMode.class), SET_FILTER_NBT_HANDLER);
        registrar.playBidirectional(SET_FILTER_MOD, SetEnum.codec(SET_FILTER_MOD, FilterModMode.class), SET_FILTER_MOD_HANDLER);
        registrar.playBidirectional(SET_FILTER_SIMILAR, SetEnum.codec(SET_FILTER_SIMILAR, FilterSimilarMode.class), SET_FILTER_SIMILAR_HANDLER);
        registrar.playBidirectional(SET_ROUTING_MODE, SetEnum.codec(SET_ROUTING_MODE, RoutingMode.class), SET_ROUTING_MODE_HANDLER);
        registrar.playBidirectional(SET_OVERSENDING_MODE, SetEnum.codec(SET_OVERSENDING_MODE, OversendingMode.class), SET_OVERSENDING_MODE_HANDLER);
        registrar.playBidirectional(SET_REDSTONE_MODE, SetEnum.codec(SET_REDSTONE_MODE, RedstoneMode.class), SET_REDSTONE_MODE_HANDLER);
        registrar.playBidirectional(SET_MAX_ITEMS_IN_INVENTORY, SetInt.codec(SET_MAX_ITEMS_IN_INVENTORY), SET_MAX_ITEMS_IN_INVENTORY_HANDLER);
        registrar.playBidirectional(SET_MAX_ITEMS_EXTRACTED, SetInt.codec(SET_MAX_ITEMS_EXTRACTED), SET_MAX_ITEMS_EXTRACTED_HANDLER);
    }

    private static void handleSetItemResource(Player player, SetItemResource payload) {
        AbstractContainerMenu handler = player.containerMenu;
        if (handler.containerId == payload.syncId() && handler instanceof ItemAttachedIoMenu attachmentMenu) {
            attachmentMenu.setFilter(payload.configIdx(), payload.resource(), false);
        }
    }

    private static void handleSetFluidResource(Player player, SetFluidResource payload) {
        AbstractContainerMenu handler = player.containerMenu;
        if (handler.containerId == payload.syncId() && handler instanceof FluidAttachedIoMenu attachmentMenu) {
            attachmentMenu.setFilter(payload.configIdx(), payload.resource(), false);
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
