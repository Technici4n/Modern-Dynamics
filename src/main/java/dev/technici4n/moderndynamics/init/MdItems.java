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
package dev.technici4n.moderndynamics.init;

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.AttachmentTier;
import dev.technici4n.moderndynamics.attachment.InhibitorAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.attachment.MdAttachments;
import dev.technici4n.moderndynamics.debug.DebugToolItem;
import dev.technici4n.moderndynamics.pipe.PipeItem;
import dev.technici4n.moderndynamics.util.MdId;
import dev.technici4n.moderndynamics.util.MdItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

public class MdItems {
    public static final PipeItem ITEM_PIPE = new PipeItem(MdBlocks.ITEM_PIPE);
    public static final PipeItem FLUID_PIPE = new PipeItem(MdBlocks.FLUID_PIPE);
    /*
     * public static final PipeItem BASIC_ITEM_PIPE_OPAQUE = new PipeItem(MdBlocks.BASIC_ITEM_PIPE_OPAQUE);
     * public static final PipeItem FAST_ITEM_PIPE = new PipeItem(MdBlocks.FAST_ITEM_PIPE);
     * public static final PipeItem FAST_ITEM_PIPE_OPAQUE = new PipeItem(MdBlocks.FAST_ITEM_PIPE_OPAQUE);
     * public static final PipeItem CONDUCTIVE_ITEM_PIPE = new PipeItem(MdBlocks.CONDUCTIVE_ITEM_PIPE);
     * public static final PipeItem CONDUCTIVE_ITEM_PIPE_OPAQUE = new PipeItem(MdBlocks.CONDUCTIVE_ITEM_PIPE_OPAQUE);
     * public static final PipeItem CONDUCTIVE_FAST_ITEM_PIPE = new PipeItem(MdBlocks.CONDUCTIVE_FAST_ITEM_PIPE);
     * public static final PipeItem CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE = new PipeItem(MdBlocks.CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE);
     * public static final PipeItem BASIC_FLUID_PIPE_OPAQUE = new PipeItem(MdBlocks.BASIC_FLUID_PIPE_OPAQUE);
     * public static final PipeItem FAST_FLUID_PIPE = new PipeItem(MdBlocks.FAST_FLUID_PIPE);
     * public static final PipeItem FAST_FLUID_PIPE_OPAQUE = new PipeItem(MdBlocks.FAST_FLUID_PIPE_OPAQUE);
     * public static final PipeItem CONDUCTIVE_FLUID_PIPE = new PipeItem(MdBlocks.CONDUCTIVE_FLUID_PIPE);
     * public static final PipeItem CONDUCTIVE_FLUID_PIPE_OPAQUE = new PipeItem(MdBlocks.CONDUCTIVE_FLUID_PIPE_OPAQUE);
     * public static final PipeItem CONDUCTIVE_FAST_FLUID_PIPE = new PipeItem(MdBlocks.CONDUCTIVE_FAST_FLUID_PIPE);
     * public static final PipeItem CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE = new PipeItem(MdBlocks.CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE);
     * public static final PipeItem BASIC_ENERGY_PIPE = new PipeItem(MdBlocks.BASIC_ENERGY_PIPE);
     * public static final PipeItem IMPROVED_ENERGY_PIPE = new PipeItem(MdBlocks.IMPROVED_ENERGY_PIPE);
     * public static final PipeItem ADVANCED_ENERGY_PIPE = new PipeItem(MdBlocks.ADVANCED_ENERGY_PIPE);
     * public static final PipeItem EMPTY_REINFORCED_ENERGY_PIPE = new PipeItem(MdBlocks.EMPTY_REINFORCED_ENERGY_PIPE);
     * public static final PipeItem EMPTY_SIGNALUM_ENERGY_PIPE = new PipeItem(MdBlocks.EMPTY_SIGNALUM_ENERGY_PIPE);
     * public static final PipeItem EMPTY_RESONANT_ENERGY_PIPE = new PipeItem(MdBlocks.EMPTY_RESONANT_ENERGY_PIPE);
     * public static final PipeItem EMPTY_SUPERCONDUCTING_PIPE = new PipeItem(MdBlocks.EMPTY_SUPERCONDUCTING_PIPE);
     */

    public static final AttachmentItem IRON_SERVO = new IoAttachmentItem(MdAttachments.IRON_SERVO, AttachmentTier.IRON, IoAttachmentType.SERVO);
    public static final AttachmentItem GOLD_SERVO = new IoAttachmentItem(MdAttachments.GOLD_SERVO, AttachmentTier.GOLD,
            IoAttachmentType.SERVO);
    public static final AttachmentItem DIAMOND_SERVO = new IoAttachmentItem(MdAttachments.DIAMOND_SERVO, AttachmentTier.DIAMOND,
            IoAttachmentType.SERVO);
    public static final AttachmentItem IRON_RETRIEVER = new IoAttachmentItem(MdAttachments.IRON_RETRIEVER, AttachmentTier.IRON,
            IoAttachmentType.RETRIEVER);
    // TODO: add funny "golden retriever" dog joke, advancement, ...
    public static final AttachmentItem GOLD_RETRIEVER = new IoAttachmentItem(MdAttachments.GOLD_RETRIEVER, AttachmentTier.GOLD,
            IoAttachmentType.RETRIEVER);
    public static final AttachmentItem DIAMOND_RETRIEVER = new IoAttachmentItem(MdAttachments.DIAMOND_RETRIEVER, AttachmentTier.DIAMOND,
            IoAttachmentType.RETRIEVER);
    public static final AttachmentItem IRON_FILTER = new IoAttachmentItem(MdAttachments.IRON_FILTER, AttachmentTier.IRON, IoAttachmentType.FILTER);
    public static final AttachmentItem GOLD_FILTER = new IoAttachmentItem(MdAttachments.GOLD_FILTER, AttachmentTier.GOLD,
            IoAttachmentType.FILTER);
    public static final AttachmentItem DIAMOND_FILTER = new IoAttachmentItem(MdAttachments.DIAMOND_FILTER, AttachmentTier.DIAMOND,
            IoAttachmentType.FILTER);
    public static final AttachmentItem INHIBITOR = new InhibitorAttachmentItem(MdAttachments.INHIBITOR);

    public static final Item WRENCH = new Item(new Item.Properties().tab(MdItemGroup.getInstance()));
    public static final DebugToolItem DEBUG_TOOL = new DebugToolItem();

    public static final PipeItem[] ALL_PIPES = new PipeItem[] {
            ITEM_PIPE,
            FLUID_PIPE,
            /*
             * BASIC_ITEM_PIPE_OPAQUE,
             * FAST_ITEM_PIPE,
             * FAST_ITEM_PIPE_OPAQUE,
             * CONDUCTIVE_ITEM_PIPE,
             * CONDUCTIVE_ITEM_PIPE_OPAQUE,
             * CONDUCTIVE_FAST_ITEM_PIPE,
             * CONDUCTIVE_FAST_ITEM_PIPE_OPAQUE,
             * BASIC_FLUID_PIPE_OPAQUE,
             * FAST_FLUID_PIPE,
             * FAST_FLUID_PIPE_OPAQUE,
             * CONDUCTIVE_FLUID_PIPE,
             * CONDUCTIVE_FLUID_PIPE_OPAQUE,
             * CONDUCTIVE_FAST_FLUID_PIPE,
             * CONDUCTIVE_FAST_FLUID_PIPE_OPAQUE,
             * BASIC_ENERGY_PIPE,
             * IMPROVED_ENERGY_PIPE,
             * ADVANCED_ENERGY_PIPE,
             * EMPTY_REINFORCED_ENERGY_PIPE,
             * EMPTY_SIGNALUM_ENERGY_PIPE,
             * EMPTY_RESONANT_ENERGY_PIPE,
             * EMPTY_SUPERCONDUCTING_PIPE,
             */
    };

    public static final AttachmentItem[] ALL_ATTACHMENTS = new AttachmentItem[] { IRON_SERVO, GOLD_SERVO, DIAMOND_SERVO, IRON_RETRIEVER,
            GOLD_RETRIEVER, DIAMOND_RETRIEVER, IRON_FILTER, GOLD_FILTER, DIAMOND_FILTER,
            INHIBITOR,
    };

    public static void init() {
        for (var pipe : ALL_PIPES) {
            Registry.register(Registry.ITEM, MdId.of(pipe.getBlock().id), pipe);
        }

        for (var attachmentItem : ALL_ATTACHMENTS) {
            Registry.register(Registry.ITEM, MdId.of(attachmentItem.attachment.id), attachmentItem);
        }

        Registry.register(Registry.ITEM, MdId.of("wrench"), WRENCH);
        Registry.register(Registry.ITEM, MdId.of("debug_tool"), DEBUG_TOOL);
    }
}
