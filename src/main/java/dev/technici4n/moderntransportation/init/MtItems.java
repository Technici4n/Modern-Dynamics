package dev.technici4n.moderntransportation.init;

import dev.technici4n.moderntransportation.block.PipeItem;
import net.minecraftforge.registries.ForgeRegistries;

public class MtItems {
    public static final PipeItem PIPE = new PipeItem();

    public static void init() {
        ForgeRegistries.ITEMS.register(PIPE);
    }
}
