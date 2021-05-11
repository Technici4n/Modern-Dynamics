package dev.technici4n.moderntransportation.init;

import dev.technici4n.moderntransportation.block.PipeItem;
import dev.technici4n.moderntransportation.debug.DebugToolItem;
import dev.technici4n.moderntransportation.util.MtId;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class MtItems {
    public static final PipeItem PIPE = new PipeItem();
    public static final DebugToolItem DEBUG_TOOL = new DebugToolItem();

    public static void init() {
        register(PIPE, "pipe");
        register(DEBUG_TOOL, "debug_tool");
    }

    private static void register(Item item, String id) {
        item.setRegistryName(MtId.of(id));
        ForgeRegistries.ITEMS.register(item);
    }
}
