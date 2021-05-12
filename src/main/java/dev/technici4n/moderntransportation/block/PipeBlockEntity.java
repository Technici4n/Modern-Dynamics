package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.MtBlockEntity;
import dev.technici4n.moderntransportation.network.energy.EnergyHost;
import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.model.MTModels;
import dev.technici4n.moderntransportation.util.SerializationHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.jetbrains.annotations.NotNull;

public class PipeBlockEntity extends MtBlockEntity {
    public PipeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    public final EnergyHost energy = new EnergyHost(this, 1000);
    private boolean hostRegistered = false;

    private IModelData modelData = EmptyModelData.INSTANCE;

    @Override
    public void toClientTag(CompoundTag tag) {
        tag.putByte("connections", SerializationHelper.directionsToMask(energy.pipeConnections));
        tag.putByte("inventoryConnections", energy.inventoryConnections.getConnectionMask());
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        byte connections = tag.getByte("connections");
        byte inventoryConnections = tag.getByte("inventoryConnections");

        modelData = new ModelDataMap.Builder()
                .withInitial(MTModels.CONNECTIONS_PIPE, connections)
                .withInitial(MTModels.CONNECTIONS_INVENTORY, inventoryConnections)
                .build();
        requestModelDataUpdate();
        remesh();
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);

        energy.separateNetwork();

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

    public void neighborUpdate() {
        energy.scheduleUpdate();
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
