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
package dev.technici4n.moderndynamics;

import dev.technici4n.moderndynamics.attachment.MdAttachments;
import dev.technici4n.moderndynamics.gui.MdPackets;
import dev.technici4n.moderndynamics.init.MdBlockEntities;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.TickHelper;
import dev.technici4n.moderndynamics.util.MdItemGroup;
import dev.technici4n.moderndynamics.util.UnsidedPacketHandler;
import dev.technici4n.moderndynamics.util.WrenchHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModernDynamics implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Modern Dynamics");

    @Override
    public void onInitialize() {
        MdItemGroup.init();

        MdBlocks.init();
        MdItems.init();
        MdBlockEntities.init();
        MdAttachments.init();
        MdMenus.init();

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> NetworkManager.onServerStopped());
        ServerTickEvents.END_SERVER_TICK.register(server -> TickHelper.onEndTick());
        ServerTickEvents.END_SERVER_TICK.register(server -> NetworkManager.onEndTick());
        WrenchHelper.registerEvents();

        registerPacketHandler(MdPackets.SET_ITEM_VARIANT, MdPackets.SET_ITEM_VARIANT_HANDLER);
        registerPacketHandler(MdPackets.SET_FILTER_MODE, MdPackets.SET_FILTER_MODE_HANDLER);
        registerPacketHandler(MdPackets.SET_FILTER_DAMAGE, MdPackets.SET_FILTER_DAMAGE_HANDLER);
        registerPacketHandler(MdPackets.SET_FILTER_NBT, MdPackets.SET_FILTER_NBT_HANDLER);
        registerPacketHandler(MdPackets.SET_FILTER_MOD, MdPackets.SET_FILTER_MOD_HANDLER);
        registerPacketHandler(MdPackets.SET_FILTER_SIMILAR, MdPackets.SET_FILTER_SIMILAR_HANDLER);
        registerPacketHandler(MdPackets.SET_ROUTING_MODE, MdPackets.SET_ROUTING_MODE_HANDLER);
        registerPacketHandler(MdPackets.SET_OVERSENDING_MODE, MdPackets.SET_OVERSENDING_MODE_HANDLER);
        registerPacketHandler(MdPackets.SET_REDSTONE_MODE, MdPackets.SET_REDSTONE_MODE_HANDLER);
        registerPacketHandler(MdPackets.SET_MAX_ITEMS_IN_INVENTORY, MdPackets.SET_MAX_ITEMS_IN_INVENTORY_HANDLER);
        registerPacketHandler(MdPackets.SET_MAX_ITEMS_EXTRACTED, MdPackets.SET_MAX_ITEMS_EXTRACTED_HANDLER);

        LOGGER.info("Successfully loaded Modern Dynamics!");
    }

    private static void registerPacketHandler(ResourceLocation channelId, UnsidedPacketHandler unsidedPacketHandler) {
        ServerPlayNetworking.registerGlobalReceiver(channelId,
                (ms, player, handler, buf, responseSender) -> unsidedPacketHandler.handlePacket(player, buf));
    }
}
