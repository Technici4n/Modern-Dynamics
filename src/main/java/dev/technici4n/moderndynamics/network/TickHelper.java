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
package dev.technici4n.moderndynamics.network;

import java.util.ArrayList;
import java.util.List;

public class TickHelper {
    private static long tickCounter = 0;
    private static List<Runnable> delayedActions = new ArrayList<>();
    private static List<Runnable> delayedActions2 = new ArrayList<>();

    public static long getTickCounter() {
        return tickCounter;
    }

    public static synchronized void onEndTick() {
        tickCounter++;

        List<Runnable> actionsToProcess = delayedActions;
        delayedActions = delayedActions2;
        delayedActions2 = actionsToProcess;

        for (Runnable runnable : actionsToProcess) {
            runnable.run();
        }

        actionsToProcess.clear();
    }

    // TODO: thread safety checks
    public static synchronized void runLater(Runnable runnable) {
        delayedActions.add(runnable);
    }
}
