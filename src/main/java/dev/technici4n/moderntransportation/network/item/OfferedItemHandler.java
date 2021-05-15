package dev.technici4n.moderntransportation.network.item;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.items.IItemHandler;

public class OfferedItemHandler {
    public final IItemHandler handler;
    public final BlockPos handlerPos;
    public final Direction side;

    public OfferedItemHandler(IItemHandler handler, BlockPos handlerPos, Direction side) {
        this.handler = handler;
        this.handlerPos = handlerPos;
        this.side = side;
    }
}
