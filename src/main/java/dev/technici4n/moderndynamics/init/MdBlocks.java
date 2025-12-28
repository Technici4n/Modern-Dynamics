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

import dev.technici4n.moderndynamics.extender.MachineExtenderBlock;
import dev.technici4n.moderndynamics.pipe.PipeBlock;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.List;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MdBlocks {

    public static DeferredRegister.Blocks DR = DeferredRegister.createBlocks(MdId.MOD_ID);

    public static final DeferredBlock<PipeBlock> ITEM_PIPE = DR.registerBlock("item_pipe", PipeBlock::new);
    public static final DeferredBlock<PipeBlock> FLUID_PIPE = DR.registerBlock("fluid_pipe", PipeBlock::new);

    public static final DeferredBlock<PipeBlock> LV_CABLE = DR.registerBlock("lv_cable", props -> new PipeBlock(props).setTransparent(false));
    public static final DeferredBlock<PipeBlock> MV_CABLE = DR.registerBlock("mv_cable", props -> new PipeBlock(props).setTransparent(false));
    public static final DeferredBlock<PipeBlock> HV_CABLE = DR.registerBlock("hv_cable", props -> new PipeBlock(props).setTransparent(false));
    public static final DeferredBlock<PipeBlock> EV_CABLE = DR.registerBlock("ev_cable", props -> new PipeBlock(props).setTransparent(false));
    public static final DeferredBlock<PipeBlock> SUPERCONDUCTOR_CABLE = DR.registerBlock("superconductor_cable",
            props -> new PipeBlock(props).setTransparent(false));

    public static final DeferredBlock<MachineExtenderBlock> MACHINE_EXTENDER = DR.registerBlock("machine_extender", MachineExtenderBlock::new);

    public static List<PipeBlock> getAllPipes() {
        return List.of(
                ITEM_PIPE.get(),
                FLUID_PIPE.get(),
                // MI energy cables
                LV_CABLE.get(),
                MV_CABLE.get(),
                HV_CABLE.get(),
                EV_CABLE.get(),
                SUPERCONDUCTOR_CABLE.get());
    }

}
