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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Attachment {
    private static final Map<String, Attachment> REGISTERED_ATTACHMENTS = new HashMap<>();

    public static List<String> getAttachmentIds() {
        return new ArrayList<>(REGISTERED_ATTACHMENTS.keySet());
    }

    public static List<Attachment> getAllAttachments() {
        return new ArrayList<>(REGISTERED_ATTACHMENTS.values());
    }

    public final String id;

    public Attachment(String id) {
        this.id = id;

        if (REGISTERED_ATTACHMENTS.put(id, this) != null) {
            throw new IllegalStateException("Duplicate attachment registration with id " + id);
        }
    }
}
