package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.network.NodeHost;
import net.minecraft.block.entity.BlockEntityType;

public class NyiPipeBlockEntity extends PipeBlockEntity {
    public NyiPipeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public NodeHost[] getHosts() {
        return new NodeHost[0];
    }
}
