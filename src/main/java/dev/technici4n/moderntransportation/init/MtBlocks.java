package dev.technici4n.moderntransportation.init;

import dev.technici4n.moderntransportation.block.PipeBlock;
import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import dev.technici4n.moderntransportation.util.MtId;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class MtBlocks {
    public static final PipeBlock PIPE = new PipeBlock("pipe");

    public static void init() {
        ForgeRegistries.BLOCKS.register(PIPE);
    }

    public static class Bet {
        public static final BlockEntityType<PipeBlockEntity> PIPE = BlockEntityType.Builder.create(PipeBlockEntity::new, MtBlocks.PIPE).build(null);

        public static void init() {
            PIPE.setRegistryName(MtId.of("pipe"));
            ForgeRegistries.TILE_ENTITIES.register(PIPE);
        }
    }
}
