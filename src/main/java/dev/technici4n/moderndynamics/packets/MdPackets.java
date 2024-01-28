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
import dev.technici4n.moderndynamics.util.FluidVariant;
import dev.technici4n.moderndynamics.util.ItemVariant;
import dev.technici4n.moderndynamics.util.MdId;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import java.util.function.BiConsumer;

public class MdPackets {

    public static void sendSetFilter(int syncId, int filterSlot, ItemVariant variant) {
        PacketDistributor.SERVER.noArg().send(new SetItemVariant(
                syncId, filterSlot, variant
        ));
    }

    public static void sendSetFilter(int syncId, int filterSlot, FluidVariant variant) {
        PacketDistributor.SERVER.noArg().send(new SetFluidVariant(
                syncId, filterSlot, variant
        ));
    }

    private static final ResourceLocation SET_FILTER_MODE = MdId.of("set_filter_mode");
    private static final SetEnumHandler<FilterInversionMode> SET_FILTER_MODE_HANDLER = createSetEnumHandler(AttachedIoMenu.class, AttachedIoMenu::setFilterMode);

    public static void sendSetFilterMode(int syncId, FilterInversionMode filterMode) {
        sendSetEnum(syncId, SET_FILTER_MODE, filterMode);
    }

    private static final ResourceLocation SET_FILTER_DAMAGE = MdId.of("set_filter_damage");
    private static final SetEnumHandler<FilterDamageMode> SET_FILTER_DAMAGE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class, ItemAttachedIoMenu::setFilterDamage);

    public static void sendSetFilterDamage(int syncId, FilterDamageMode value) {
        sendSetEnum(syncId, SET_FILTER_DAMAGE, value);
    }

    private static final ResourceLocation SET_FILTER_NBT = MdId.of("set_filter_nbt");
    private static final SetEnumHandler<FilterNbtMode> SET_FILTER_NBT_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class, ItemAttachedIoMenu::setFilterNbt);

    public static void sendSetFilterNbt(int syncId, FilterNbtMode value) {
        sendSetEnum(syncId, SET_FILTER_NBT, value);
    }

    private static final ResourceLocation SET_FILTER_MOD = MdId.of("set_filter_mod");
    private static final SetEnumHandler<FilterModMode> SET_FILTER_MOD_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class, ItemAttachedIoMenu::setFilterMod);

    public static void sendSetFilterMod(int syncId, FilterModMode value) {
        sendSetEnum(syncId, SET_FILTER_MOD, value);
    }

    private static final ResourceLocation SET_FILTER_SIMILAR = MdId.of("set_filter_similar");
    private static final SetEnumHandler<FilterSimilarMode> SET_FILTER_SIMILAR_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class, ItemAttachedIoMenu::setFilterSimilar);

    public static void sendSetFilterSimilar(int syncId, FilterSimilarMode value) {
        sendSetEnum(syncId, SET_FILTER_SIMILAR, value);
    }

    private static final ResourceLocation SET_ROUTING_MODE = MdId.of("set_routing_mode");
    private static final SetEnumHandler<RoutingMode> SET_ROUTING_MODE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class, ItemAttachedIoMenu::setRoutingMode);

    public static void sendSetRoutingMode(int syncId, RoutingMode value) {
        sendSetEnum(syncId, SET_ROUTING_MODE, value);
    }

    private static final ResourceLocation SET_OVERSENDING_MODE = MdId.of("set_oversending_mode");
    private static final SetEnumHandler<OversendingMode> SET_OVERSENDING_MODE_HANDLER = createSetEnumHandler(ItemAttachedIoMenu.class, ItemAttachedIoMenu::setOversendingMode);

    public static void sendSetOversendingMode(int syncId, OversendingMode value) {
        sendSetEnum(syncId, SET_OVERSENDING_MODE, value);
    }

    private static final ResourceLocation SET_REDSTONE_MODE = MdId.of("set_redstone_mode");
    private static final SetEnumHandler<RedstoneMode> SET_REDSTONE_MODE_HANDLER = createSetEnumHandler(AttachedIoMenu.class, AttachedIoMenu::setRedstoneMode);

    public static void sendSetRedstoneMode(int syncId, RedstoneMode value) {
        sendSetEnum(syncId, SET_REDSTONE_MODE, value);
    }

    private static final ResourceLocation SET_MAX_ITEMS_IN_INVENTORY = MdId.of("set_max_items_in_inventory");
    private static final SetIntHandler SET_MAX_ITEMS_IN_INVENTORY_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsInInventory);

    public static void sendSetMaxItemsInInventory(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_IN_INVENTORY, value);
    }

    private static final ResourceLocation SET_MAX_ITEMS_EXTRACTED = MdId.of("set_max_items_extracted");
    private static final SetIntHandler SET_MAX_ITEMS_EXTRACTED_HANDLER = createSetIntHandler(ItemAttachedIoMenu::setMaxItemsExtracted);

    public static void sendSetMaxItemsExtracted(int syncId, int value) {
        sendSetInt(syncId, SET_MAX_ITEMS_EXTRACTED, value);
    }

    private static <T extends Enum<T>> void sendSetEnum(int syncId, ResourceLocation packetId, T enumValue) {
        PacketDistributor.SERVER.noArg().send(new SetEnum<>(packetId, syncId, enumValue));
    }

    private static <T extends Enum<T>, M extends AbstractContainerMenu> SetEnumHandler<T> createSetEnumHandler(Class<M> menuClass, EnumSetter<T, M> setter) {
        return (player, syncId, value) -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId) {
                setter.setEnum(menuClass.cast(handler), value, false);
            }
        };
    }

    private static void sendSetInt(int syncId, ResourceLocation packetId, int value) {
        PacketDistributor.SERVER.noArg().send(new SetInt(packetId, syncId, value));
    }

    private static SetIntHandler createSetIntHandler(IntSetter setter) {
        return (player, syncId, value) -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId && handler instanceof ItemAttachedIoMenu attachmentMenu) {
                setter.setInt(attachmentMenu, value, false);
            }
        };
    }

    public static void register(IPayloadRegistrar registrar) {
        registrar.play(SetAttachmentUpgrades.ID, SetAttachmentUpgrades::read, builder -> {
            builder.client(SetAttachmentUpgrades.HANDLER);
        });

        registrar.play(SetItemVariant.ID, SetItemVariant::read, handler(MdPackets::handleSetItemVariant));
        registrar.play(SetFluidVariant.ID, SetFluidVariant::read, handler(MdPackets::handleSetFluidVariant));

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

    private static <T extends Enum<T>> void registerSetEnum(IPayloadRegistrar registrar, ResourceLocation id, Class<T> enumClass, SetEnumHandler<T> handler) {
        registrar.play(id, SetEnum.makeReader(id, enumClass), handler((player, payload) -> {
            handler.handleSetEnum(player, payload.syncId(), payload.value());
        }));

    }

    private static void registerSetInt(IPayloadRegistrar registrar, ResourceLocation id, SetIntHandler handler) {
        registrar.play(id, SetInt.makeReader(id), handler((player, payload) -> {
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

    private static <T extends CustomPacketPayload> IPlayPayloadHandler<T> handler(BiConsumer<Player, T> handler) {
        return (payload, context) -> context.player().ifPresent(player -> context.workHandler().execute(() -> {
            handler.accept(player, payload);
        }));
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
