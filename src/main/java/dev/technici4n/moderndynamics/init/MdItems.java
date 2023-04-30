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
import dev.technici4n.moderndynamics.attachment.InhibitorAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentItem;
import dev.technici4n.moderndynamics.attachment.IoAttachmentType;
import dev.technici4n.moderndynamics.debug.DebugToolItem;
import dev.technici4n.moderndynamics.pipe.PipeItem;
import dev.technici4n.moderndynamics.util.MdId;
import dev.technici4n.moderndynamics.util.MdItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class MdItems {
    public static final PipeItem ITEM_PIPE = new PipeItem(MdBlocks.ITEM_PIPE);
    public static final PipeItem FLUID_PIPE = new PipeItem(MdBlocks.FLUID_PIPE);

    public static final PipeItem LV_CABLE = new PipeItem(MdBlocks.LV_CABLE);
    public static final PipeItem MV_CABLE = new PipeItem(MdBlocks.MV_CABLE);
    public static final PipeItem HV_CABLE = new PipeItem(MdBlocks.HV_CABLE);
    public static final PipeItem EV_CABLE = new PipeItem(MdBlocks.EV_CABLE);
    public static final PipeItem SUPERCONDUCTOR_CABLE = new PipeItem(MdBlocks.SUPERCONDUCTOR_CABLE);

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

    public static final AttachmentItem ATTRACTOR = new IoAttachmentItem(MdAttachments.ATTRACTOR, IoAttachmentType.ATTRACTOR);
    public static final AttachmentItem EXTRACTOR = new IoAttachmentItem(MdAttachments.EXTRACTOR, IoAttachmentType.EXTRACTOR);
    public static final AttachmentItem FILTER = new IoAttachmentItem(MdAttachments.FILTER, IoAttachmentType.FILTER);
    public static final AttachmentItem INHIBITOR = new InhibitorAttachmentItem(MdAttachments.INHIBITOR);

    public static final BlockItem MACHINE_EXTENDER = new BlockItem(MdBlocks.MACHINE_EXTENDER, new Item.Properties().tab(MdItemGroup.getInstance()));

    public static final Item WRENCH = new Item(new Item.Properties().stacksTo(1).tab(MdItemGroup.getInstance()));
    public static final DebugToolItem DEBUG_TOOL = new DebugToolItem();

    public static final PipeItem[] ALL_PIPES = new PipeItem[] {
            ITEM_PIPE,
            FLUID_PIPE,
            LV_CABLE,
            MV_CABLE,
            HV_CABLE,
            EV_CABLE,
            SUPERCONDUCTOR_CABLE,
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

    public static final AttachmentItem[] ALL_ATTACHMENTS = new AttachmentItem[] {
            ATTRACTOR,
            EXTRACTOR,
            FILTER,
            INHIBITOR,
    };

    public static void init() {
        for (var pipe : ALL_PIPES) {
            Registry.register(Registry.ITEM, MdId.of(pipe.getBlock().id), pipe);
        }

        for (var attachmentItem : ALL_ATTACHMENTS) {
            Registry.register(Registry.ITEM, MdId.of(attachmentItem.attachment.id), attachmentItem);
        }

        Registry.register(Registry.ITEM, MdId.of(MdBlocks.MACHINE_EXTENDER.id), MACHINE_EXTENDER);

        Registry.register(Registry.ITEM, MdId.of("wrench"), WRENCH);
        Registry.register(Registry.ITEM, MdId.of("debug_tool"), DEBUG_TOOL);
    }
}
