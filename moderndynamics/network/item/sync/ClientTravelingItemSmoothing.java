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
package dev.technici4n.moderndynamics.network.item.sync;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Exponential window interpolation of the traveled distance predicted on the client and received from the server,
 * to reduce visible jitter.
 * Also keeps track of a client tick counter.
 */
public class ClientTravelingItemSmoothing {

    public static void onUnpausedTick() {
        // Cleanup items that are not used anymore
        for (var it = INFOS.values().iterator(); it.hasNext();) {
            SmoothingInfo info = it.next();
            info.deadTimer--;
            if (info.deadTimer <= 0) {
                it.remove();
            }
        }

        clientTick++;
    }

    public static void onReceiveItem(ClientTravelingItem item) {
        SmoothingInfo info = INFOS.get(item.id);
        if (info == null) {
            info = new SmoothingInfo();
            info.lastTraveledDistance = item.traveledDistance;
            info.wrongOffset = 0;
            INFOS.put(item.id, info);
        } else {
            info.wrongOffset += info.lastTraveledDistance - item.traveledDistance;
            info.lastTraveledDistance = item.traveledDistance;
        }
        info.resetTimer();
    }

    public static double getAndUpdateDistanceDelta(ClientTravelingItem item) {
        var info = INFOS.get(item.id);
        if (info == null) {
            return 0;
        }
        info.resetTimer();
        info.lastTraveledDistance = item.traveledDistance;
        double offset = info.wrongOffset;
        info.wrongOffset *= 0.95; // might want to adjust based on FPS or similar
        return offset;
    }

    public static long getClientTick() {
        return clientTick;
    }

    private static final Int2ObjectMap<SmoothingInfo> INFOS = new Int2ObjectOpenHashMap<>();
    private static long clientTick = 0;

    private static class SmoothingInfo {
        double lastTraveledDistance;
        double wrongOffset;
        int deadTimer;

        void resetTimer() {
            deadTimer = 10;
        }
    }
}
