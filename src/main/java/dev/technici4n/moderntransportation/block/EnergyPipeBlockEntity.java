package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.model.MTModels;
import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.energy.EnergyHost;
import dev.technici4n.moderntransportation.network.energy.EnergyPipeTier;
import dev.technici4n.moderntransportation.util.ShapeHelper;
import dev.technici4n.moderntransportation.util.WrenchHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
    private int clientSideConnections = 0;

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
        super.toClientTag(tag);
        tag.putByte("connections", energy.pipeConnections);
        tag.putByte("inventoryConnections", (byte) energy.inventoryConnections);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        super.fromClientTag(tag);
        byte connections = tag.getByte("connections");
        byte inventoryConnections = tag.getByte("inventoryConnections");

        updateCachedShape(connections, inventoryConnections);
        modelData = new ModelDataMap.Builder()
                .withInitial(MTModels.CONNECTIONS_PIPE, connections)
                .withInitial(MTModels.CONNECTIONS_INVENTORY, inventoryConnections)
                .build();
        clientSideConnections = connections | inventoryConnections;
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
                shape = VoxelShapes.union(shape, PipeBoundingBoxes.CONNECTOR_SHAPES[i]);
            }
        }

        cachedShape = shape.simplify();
    }

    @Override
    public VoxelShape getCachedShape() {
        return cachedShape;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (WrenchHelper.isWrench(player.getStackInHand(hand))) {
            Vec3d posInBlock = hitResult.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());

            // If the core was hit, add back the pipe on the target side
            if (ShapeHelper.shapeContains(PipeBoundingBoxes.CORE_SHAPE, posInBlock)) {
                if ((connectionBlacklist & (1 << hitResult.getSide().getId())) > 0) {
                    if (!world.isClient()) {
                        updateConnection(hitResult.getSide(), true);
                    }

                    return ActionResult.success(world.isClient());
                }
            }

            for (int i = 0; i < 6; ++i) {
                // If a pipe or inventory connection was hit, add it to the blacklist
                // INVENTORY_CONNECTIONS contains both the pipe and the connector, so it will work in both cases
                if (ShapeHelper.shapeContains(PipeBoundingBoxes.INVENTORY_CONNECTIONS[i], posInBlock)) {
                    if (world.isClient()) {
                        if ((clientSideConnections & (1 << i)) > 0) {
                            return ActionResult.SUCCESS;
                        }
                    } else {
                        if ((energy.pipeConnections & (1 << i)) > 0 || (energy.inventoryConnections & (1 << i)) > 0) {
                            updateConnection(Direction.byId(i), false);
                            return ActionResult.CONSUME;
                        }
                    }
                }
            }
        }

        return ActionResult.PASS;
    }
}
