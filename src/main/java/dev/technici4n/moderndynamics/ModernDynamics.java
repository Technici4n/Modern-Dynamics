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

import dev.technici4n.moderndynamics.attachment.upgrade.AttachmentUpgradesLoader;
import dev.technici4n.moderndynamics.client.ModernDynamicsClient;
import dev.technici4n.moderndynamics.init.MdAttachments;
import dev.technici4n.moderndynamics.init.MdBlockEntities;
import dev.technici4n.moderndynamics.init.MdBlocks;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.init.MdMenus;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.TickHelper;
import dev.technici4n.moderndynamics.network.item.SimulatedInsertionTargets;
import dev.technici4n.moderndynamics.packets.MdPackets;
import dev.technici4n.moderndynamics.util.MdId;
import dev.technici4n.moderndynamics.util.MdItemGroup;
import dev.technici4n.moderndynamics.util.WrenchHelper;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MdId.MOD_ID)
public class ModernDynamics {
    public static final Logger LOGGER = LogManager.getLogger("Modern Dynamics");

    public ModernDynamics(IEventBus modEvents) {
        modEvents.addListener(RegisterEvent.class, this::register);
        modEvents.addListener(RegisterPayloadHandlersEvent.class, this::registerPayloadHandlers);

        modEvents.addListener(MdBlockEntities::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, e -> {
            NetworkManager.onServerStopped();
            SimulatedInsertionTargets.clear();
        });
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> {
            TickHelper.onEndTick();
            NetworkManager.onEndTick();
        });
        NeoForge.EVENT_BUS.addListener(WrenchHelper::handleEvent);
        AttachmentUpgradesLoader.setup();

        if (FMLLoader.getDist().isClient()) {
            new ModernDynamicsClient(modEvents);
        }
        LOGGER.info("Successfully loaded Modern Dynamics!");
    }

    private void register(RegisterEvent registerEvent) {
        var registryKey = registerEvent.getRegistryKey();
        if (registryKey == Registries.BLOCK) {
            MdBlocks.init();
        } else if (registryKey == Registries.ITEM) {
            MdItems.init();
            MdAttachments.init();
        } else if (registryKey == Registries.BLOCK_ENTITY_TYPE) {
            MdBlockEntities.init();
        } else if (registryKey == Registries.MENU) {
            MdMenus.init();
        } else if (registryKey == Registries.CREATIVE_MODE_TAB) {
            MdItemGroup.init();
        }
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent e) {
        var registrar = e.registrar(MdId.MOD_ID);
        MdPackets.register(registrar);
    }
}
