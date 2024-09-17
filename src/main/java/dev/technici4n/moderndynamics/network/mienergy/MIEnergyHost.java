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
package dev.technici4n.moderndynamics.network.mienergy;

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.compat.mi.MIProxy;
import dev.technici4n.moderndynamics.network.HostAdjacentCaps;
import dev.technici4n.moderndynamics.network.NetworkManager;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class MIEnergyHost extends NodeHost {
    private static final NetworkManager<MIEnergyHost, MIEnergyCache> MANAGER = NetworkManager.get(MIEnergyCache.class, MIEnergyCache::new);

    public final MICableTier tier;
    private long energy = 0;
    private final HostAdjacentCaps<? extends IEnergyStorage> adjacentCaps = new HostAdjacentCaps<>(this, MIProxy.INSTANCE.getLookup());

    public MIEnergyHost(PipeBlockEntity pipe, MICableTier tier) {
        super(pipe);

        this.tier = tier;
    }

    @Override
    protected void doUpdate() {
        updateConnections();
    }

    @Override
    public boolean acceptsAttachment(AttachmentItem attachment, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canConnectTo(Direction connectionDirection, NodeHost adjacentHost) {
        return super.canConnectTo(connectionDirection, adjacentHost) && ((MIEnergyHost) adjacentHost).tier == tier;
    }

    public void gatherCapabilities(@Nullable List<IEnergyStorage> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.from3DDataValue(i);
                IEnergyStorage adjacentCap = adjacentCaps.getCapability(dir);

                if (adjacentCap != null && MIProxy.INSTANCE.canConnect(adjacentCap, tier)) {
                    if (out != null) {
                        out.add(adjacentCap);
                    }
                } else {
                    // Remove the direction from the bitmask
                    inventoryConnections ^= 1 << i;
                }
            }
        }

        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    public void updateConnections() {
        // Store old connections
        int oldConnections = inventoryConnections;

        // Compute new connections (excluding existing adjacent pipe connections, and the blacklist)
        inventoryConnections = (1 << 6) - 1 - (pipeConnections | pipe.connectionBlacklist);
        gatherCapabilities(null);

        // Update render
        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeNbt(tag, registries);
        tag.putLong("mi_energy", energy);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.readNbt(tag, registries);
        // Guard against max energy changes
        energy = Math.max(0, Math.min(tag.getLong("mi_energy"), getMaxEnergy()));
    }

    @Override
    public NetworkManager<MIEnergyHost, MIEnergyCache> getManager() {
        return MANAGER;
    }

    @Override
    @Nullable
    public Object getApiInstance(BlockCapability<?, Direction> lookup, @Nullable Direction side) {
        return null;
    }

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
        return tier.getMax();
    }

    public void setEnergy(long energy) {
        if (energy < 0 || energy > getMaxEnergy()) {
            throw new IllegalArgumentException("Invalid energy value " + energy);
        }

        this.energy = energy;
        pipe.setChanged();
    }
}
