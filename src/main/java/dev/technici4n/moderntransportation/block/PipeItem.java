package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.init.MtBlocks;
import dev.technici4n.moderntransportation.util.MTId;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class PipeItem extends BlockItem {
    public PipeItem() {
        super(MtBlocks.PIPE, new Item.Settings());
        setRegistryName(MTId.of("pipe"));
    }
}
