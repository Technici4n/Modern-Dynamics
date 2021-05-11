package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.util.MtId;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block {
    public PipeBlock(String id) {
        super(Settings.of(Material.METAL).nonOpaque());
        this.setRegistryName(MtId.of(id));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockView world) {
        return new PipeBlockEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getOpacity(BlockState p_200011_1_, BlockView p_200011_2_, BlockPos p_200011_3_) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block param4, BlockPos param5, boolean param6) {
        BlockEntity be = world.getBlockEntity(pos);

        if (be instanceof PipeBlockEntity) {
            ((PipeBlockEntity) be).neighborUpdate();
        }
    }
}
