package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.item.ItemHost;
import dev.technici4n.moderntransportation.network.item.ItemPipeTier;
import net.minecraft.block.entity.BlockEntityType;

public class ItemPipeBlockEntity extends PipeBlockEntity {
    private final NodeHost[] hosts;

    public ItemPipeBlockEntity(BlockEntityType<?> type, ItemPipeTier tier) {
        super(type);

        ItemHost item = new ItemHost(this, tier);
        this.hosts = new NodeHost[] { item };
    }

    @Override
    public NodeHost[] getHosts() {
        return hosts;
    }
}
