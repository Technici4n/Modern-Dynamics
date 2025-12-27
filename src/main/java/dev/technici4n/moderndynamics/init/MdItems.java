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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class MdItems {
    public static DeferredRegister.Items DR = DeferredRegister.createItems(MdId.MOD_ID);

    public static final DeferredItem<PipeItem> ITEM_PIPE = DR.registerItem("item_pipe", props -> new PipeItem(MdBlocks.ITEM_PIPE.get(), props));
    public static final DeferredItem<PipeItem> FLUID_PIPE = DR.registerItem("fluid_pipe", props -> new PipeItem(MdBlocks.FLUID_PIPE.get(), props));
    public static final DeferredItem<PipeItem> LV_CABLE = DR.registerItem("lv_cable", props -> new PipeItem(MdBlocks.LV_CABLE.get(), props));
    public static final DeferredItem<PipeItem> MV_CABLE = DR.registerItem("mv_cable", props -> new PipeItem(MdBlocks.MV_CABLE.get(), props));
    public static final DeferredItem<PipeItem> HV_CABLE = DR.registerItem("hv_cable", props -> new PipeItem(MdBlocks.HV_CABLE.get(), props));
    public static final DeferredItem<PipeItem> EV_CABLE = DR.registerItem("ev_cable", props -> new PipeItem(MdBlocks.EV_CABLE.get(), props));
    public static final DeferredItem<PipeItem> SUPERCONDUCTOR_CABLE = DR.registerItem("superconductor_cable", props -> new PipeItem(MdBlocks.SUPERCONDUCTOR_CABLE.get(), props));

    public static final DeferredItem<AttachmentItem> ATTRACTOR = DR.registerItem("attractor", props -> new IoAttachmentItem(props, MdAttachments.ATTRACTOR, IoAttachmentType.ATTRACTOR));
    public static final DeferredItem<AttachmentItem> EXTRACTOR = DR.registerItem("extractor", props -> new IoAttachmentItem(props, MdAttachments.EXTRACTOR, IoAttachmentType.EXTRACTOR));
    public static final DeferredItem<AttachmentItem> FILTER = DR.registerItem("filter", props -> new IoAttachmentItem(props, MdAttachments.FILTER, IoAttachmentType.FILTER));
    public static final DeferredItem<AttachmentItem> INHIBITOR = DR.registerItem("inhibitor", props -> new InhibitorAttachmentItem(props, MdAttachments.INHIBITOR));

    public static final DeferredItem<BlockItem> MACHINE_EXTENDER = DR.registerSimpleBlockItem(MdBlocks.MACHINE_EXTENDER);

    public static final DeferredItem<Item> WRENCH = DR.registerItem("wrench", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<DebugToolItem> DEBUG_TOOL = DR.registerItem("debug_tool", DebugToolItem::new);

    public static List<PipeItem> getAllPipes() {
        return List.of(
            ITEM_PIPE.get(),
            FLUID_PIPE.get(),
            LV_CABLE.get(),
            MV_CABLE.get(),
            HV_CABLE.get(),
            EV_CABLE.get(),
            SUPERCONDUCTOR_CABLE.get()
        );
    }

    public static List<AttachmentItem> getAllAttachments() {
        return List.of(
            ATTRACTOR.get(),
            EXTRACTOR.get(),
            FILTER.get(),
            INHIBITOR.get()
        );
    }
}
