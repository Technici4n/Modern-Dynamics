package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.network.NodeHost;
import dev.technici4n.moderntransportation.network.energy.EnergyHost;
import dev.technici4n.moderntransportation.network.energy.EnergyPipeTier;
import net.minecraft.block.entity.BlockEntityType;

public class EnergyPipeBlockEntity extends PipeBlockEntity {
    private final NodeHost[] hosts;

    public EnergyPipeBlockEntity(BlockEntityType<?> type, EnergyPipeTier tier) {
        super(type);

        EnergyHost energy = new EnergyHost(this, tier);
        this.hosts = new NodeHost[] { energy };
    }

    @Override
    public NodeHost[] getHosts() {
        return hosts;
    }
}
