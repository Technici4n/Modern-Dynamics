package dev.technici4n.moderntransportation.debug;

import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import dev.technici4n.moderntransportation.network.NetworkCache;
import dev.technici4n.moderntransportation.network.NetworkNode;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;

public class DebugToolItem extends Item {
    public DebugToolItem() {
        super(new Settings());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        BlockEntity be = ctx.getWorld().getBlockEntity(ctx.getBlockPos());

        if (be instanceof PipeBlockEntity && ctx.getPlayer() != null) {
            if (ctx.getWorld().isClient()) {
                return ActionResult.SUCCESS;
            } else {
                PipeBlockEntity pipe = (PipeBlockEntity) be;
                StringBuilder message = new StringBuilder();

                NetworkNode<?, ? extends NetworkCache<?, ?>> node = pipe.energy.getManager().findNode((ServerWorld) pipe.getWorld(), pipe.getPos());
                if (node != null) {
                    node.getNetworkCache().appendDebugInfo(message);
                } else {
                    message.append("No node found.\n");
                }

                ctx.getPlayer().sendMessage(new LiteralText(message.toString()),false);

                return ActionResult.CONSUME;
            }
        }

        return ActionResult.PASS;
    }
}
