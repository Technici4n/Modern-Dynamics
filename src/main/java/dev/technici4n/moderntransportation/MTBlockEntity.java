package dev.technici4n.moderntransportation;

import com.google.common.base.Preconditions;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;

public abstract class MTBlockEntity extends BlockEntity {
    public MTBlockEntity(BlockEntityType<?> bet) {
        super(bet);
    }

    // Thank you Fabric API
    public final void sync() {
        Preconditions.checkNotNull(world); //Maintain distinct failure case from below
        if (!(world instanceof ServerWorld)) throw new IllegalStateException("Cannot call sync() on the logical client! Did you check world.isClient first?");

        ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
    }

    public abstract void toClientTag(CompoundTag tag);

    public abstract void fromClientTag(CompoundTag tag);

    @Override
    public final BlockEntityUpdateS2CPacket toUpdatePacket() {
        CompoundTag tag = new CompoundTag();
        toClientTag(tag);
        return new BlockEntityUpdateS2CPacket(pos, 42, tag);
    }

    @Override
    public final CompoundTag toInitialChunkDataTag() {
        CompoundTag tag = super.toInitialChunkDataTag();
        toClientTag(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        fromClientTag(tag);
    }

    @Override
    public final void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket pkt) {
        fromClientTag(pkt.getCompoundTag());
    }

    public final void remesh() {
        Preconditions.checkNotNull(world);
        if (!(world instanceof ClientWorld)) throw new IllegalStateException("Cannot call remesh() on the server!");

        world.updateListeners(pos, null, null, 0);
    }
}
