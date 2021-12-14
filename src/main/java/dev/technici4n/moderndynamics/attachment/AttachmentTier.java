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
package dev.technici4n.moderndynamics.attachment;

public enum AttachmentTier {
    BASIC(5, 1, 4, 40, 1),
    IMPROVED(5, 2, 16, 20, 2),
    ADVANCED(5, 3, 64, 10, 3),
    ;

    public final int configWidth;
    public final int configHeight;
    public final int transferCount;
    public final int transferFrequency;
    public final int speedupFactor;

    AttachmentTier(int configWidth, int configHeight, int transferCount, int transferFrequency, int speedupFactor) {
        this.configWidth = configWidth;
        this.configHeight = configHeight;
        this.transferCount = transferCount;
        this.transferFrequency = transferFrequency;
        this.speedupFactor = speedupFactor;
    }

    public boolean allowAdvancedBehavior() {
        return this == ADVANCED;
    }
}
