package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.MTBlockEntity;
import dev.technici4n.moderntransportation.impl.energy.EnergyHost;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.model.MTModels;
import dev.technici4n.moderntransportation.util.SerializationHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class PipeBlockEntity extends MTBlockEntity {
    public PipeBlockEntity() {
        super(MtBlocks.Bet.PIPE);
    }

    private final EnergyHost energy = new EnergyHost(this, 1000);
    private boolean hostRegistered = false;

    private IModelData modelData = EmptyModelData.INSTANCE;

    @Override
    public void toClientTag(CompoundTag tag) {
        tag.putByte("connections", SerializationHelper.directionsToMask(energy.connections));
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        byte connections = tag.getByte("connections");

        modelData = new ModelDataMap.Builder().withInitial(MTModels.CONNECTIONS, connections).build();
        requestModelDataUpdate();
        remesh();
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);

        nbt.putInt("energy", energy.getEnergy());

        return nbt;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag nbt) {
        super.fromTag(state, nbt);

        energy.setEnergy(nbt.getInt("energy"), false);
    }

    protected void addHosts() {
        energy.addSelf();
    }

    protected void removeHosts() {
        energy.removeSelf();
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();

        if (!world.isClient()) {
            if (!hostRegistered) {
                hostRegistered = true;
                addHosts();
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if (!world.isClient()) {
            if (hostRegistered) {
                hostRegistered = false;
                removeHosts();
            }
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();

        if (!world.isClient()) {
            if (hostRegistered) {
                hostRegistered = false;
                removeHosts();
            }
        }
    }

    @NotNull
    @Override
    public IModelData getModelData() {
        return modelData;
    }
}
