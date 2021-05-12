package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.model.MTModels;
import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.energy.EnergyHost;
import dev.technici4n.moderntransportation.network.energy.EnergyPipeTier;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.jetbrains.annotations.NotNull;

public class EnergyPipeBlockEntity extends PipeBlockEntity {
    private final EnergyHost energy;
    private final NodeHost[] hosts;
    private VoxelShape cachedShape = PipeBoundingBoxes.CORE_SHAPE;
    private IModelData modelData = EmptyModelData.INSTANCE;

    public EnergyPipeBlockEntity(BlockEntityType<?> type, EnergyPipeTier tier) {
        super(type);

        this.energy = new EnergyHost(this, tier);
        this.hosts = new NodeHost[] { energy };
    }

    @Override
    public NodeHost[] getHosts() {
        return hosts;
    }

    @Override
    public void sync() {
        super.sync();
        updateCachedShape(energy.pipeConnections, energy.inventoryConnections);
    }

    @Override
    public void toClientTag(CompoundTag tag) {
        tag.putByte("connections", energy.pipeConnections);
        tag.putByte("inventoryConnections", (byte) energy.inventoryConnections);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        byte connections = tag.getByte("connections");
        byte inventoryConnections = tag.getByte("inventoryConnections");

        updateCachedShape(connections, inventoryConnections);
        modelData = new ModelDataMap.Builder()
                .withInitial(MTModels.CONNECTIONS_PIPE, connections)
                .withInitial(MTModels.CONNECTIONS_INVENTORY, inventoryConnections)
                .build();
        requestModelDataUpdate();
        remesh();
    }

    @NotNull
    @Override
    public IModelData getModelData() {
        return modelData;
    }

    public void updateCachedShape(int pipeConnections, int inventoryConnections) {
        int allConnections = pipeConnections | inventoryConnections;

        VoxelShape shape = PipeBoundingBoxes.CORE_SHAPE;

        for (int i = 0; i < 6; ++i) {
            if ((allConnections & (1 << i)) > 0) {
                shape = VoxelShapes.union(shape, PipeBoundingBoxes.PIPE_CONNECTIONS[i]);
            }

            if ((inventoryConnections & (1 << i)) > 0) {
                shape = VoxelShapes.union(shape, PipeBoundingBoxes.INVENTORY_CONNECTIONS[i]);
            }
        }

        cachedShape = shape.simplify();
    }

    @Override
    public VoxelShape getCachedShape() {
        return cachedShape;
    }
}
