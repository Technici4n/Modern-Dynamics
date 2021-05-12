package dev.technici4n.moderntransportation.block;

import com.google.common.base.Preconditions;
import dev.technici4n.moderntransportation.util.MtId;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block {

	private PipeItem item;
	private BlockEntityType<?> blockEntityType;

	public PipeBlock(String id) {
		super(Settings.of(Material.METAL).nonOpaque());
		this.setRegistryName(MtId.of(id));
	}

	public PipeItem getItem() {
		Preconditions.checkState(this.item != null, "Item has not been set on %s", this);
		return this.item;
	}

	public void setItem(PipeItem item) {
		Preconditions.checkState(this.item == null, "Item has already been set on %s", this);
		this.item = item;
	}

	public BlockEntityType<?> getBlockEntityType() {
		Preconditions.checkState(this.blockEntityType != null, "Block entity type has not been set on %s", this);
		return this.blockEntityType;
	}

	public void setBlockEntityProvider(BlockEntityType<?> blockEntityType) {
		Preconditions.checkState(this.blockEntityType == null, "blockEntityType has already been set on %s", this);
		this.blockEntityType = blockEntityType;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return getBlockEntityType().instantiate();
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
