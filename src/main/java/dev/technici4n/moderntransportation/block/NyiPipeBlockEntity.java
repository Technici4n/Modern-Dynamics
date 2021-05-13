package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.network.NodeHost;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShape;

public class NyiPipeBlockEntity extends PipeBlockEntity {
    public NyiPipeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public NodeHost[] getHosts() {
        return new NodeHost[0];
    }

    @Override
    public VoxelShape getCachedShape() {
        return PipeBoundingBoxes.CORE_SHAPE;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        return ActionResult.PASS;
    }

    @Override
    public void toClientTag(CompoundTag tag) {
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
    }
}
