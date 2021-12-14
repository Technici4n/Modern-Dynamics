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
package dev.technici4n.moderndynamics.pipe;

import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.energy.EnergyHost;
import dev.technici4n.moderndynamics.network.energy.EnergyPipeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyPipeBlockEntity extends PipeBlockEntity {
    private final NodeHost[] hosts;

    public EnergyPipeBlockEntity(BlockEntityType<?> type, EnergyPipeTier tier, BlockPos pos, BlockState state) {
        super(type, pos, state);

        EnergyHost energy = new EnergyHost(this, tier);
        this.hosts = new NodeHost[] { energy };
    }

    @Override
    public NodeHost[] getHosts() {
        return hosts;
    }
}
