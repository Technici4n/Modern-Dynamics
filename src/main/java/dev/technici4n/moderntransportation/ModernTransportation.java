/*
 * Modern Transportation
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
package dev.technici4n.moderntransportation;

import dev.technici4n.moderntransportation.attachment.MtAttachments;
import dev.technici4n.moderntransportation.init.MtBlockEntities;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.init.MtItems;
import dev.technici4n.moderntransportation.init.MtTags;
import dev.technici4n.moderntransportation.network.NetworkManager;
import dev.technici4n.moderntransportation.network.TickHelper;
import dev.technici4n.moderntransportation.util.MtItemGroup;
import dev.technici4n.moderntransportation.util.WrenchHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModernTransportation implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Modern Transportation");

    @Override
    public void onInitialize() {
        MtItemGroup.init();

        MtBlocks.init();
        MtItems.init();
        MtBlockEntities.init();
        MtAttachments.init();
        MtTags.init();

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> NetworkManager.onServerStopped());
        ServerTickEvents.END_SERVER_TICK.register(server -> TickHelper.onEndTick());
        ServerTickEvents.END_SERVER_TICK.register(server -> NetworkManager.onEndTick());
        WrenchHelper.registerEvents();

        LOGGER.info("Successfully loaded Modern Transportation!");
    }

}
