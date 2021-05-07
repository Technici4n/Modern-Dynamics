package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.util.MTId;
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
        this.setRegistryName(MTId.of(id));
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

    @Override
    public void onStateReplaced(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_, boolean p_196243_5_) {
        super.onStateReplaced(p_196243_1_, p_196243_2_, p_196243_3_, p_196243_4_, p_196243_5_);
    }
}
