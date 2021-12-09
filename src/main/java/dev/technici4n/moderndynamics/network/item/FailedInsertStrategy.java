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
package dev.technici4n.moderndynamics.network.item;

public enum FailedInsertStrategy {
    DROP("drop"),
    SEND_BACK_TO_SOURCE("source"),
    BUFFER_IN_TARGET("target"),
    ;

    private final String serializedName;

    FailedInsertStrategy(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static FailedInsertStrategy bySerializedName(String serializedName) {
        return switch (serializedName) {
            case "drop" -> DROP;
            case "source" -> SEND_BACK_TO_SOURCE;
            case "target" -> BUFFER_IN_TARGET;
            default -> throw new RuntimeException("Unknown failed insert strategy: " + serializedName);
        };
    }
}
