package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.util.MtItemGroup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PipeItem extends BlockItem {
	public PipeItem(PipeBlock block) {
		super(block, new Item.Settings().group(MtItemGroup.getInstance()));
		setRegistryName(block.getRegistryName());
		block.setItem(this);
	}

	@Override
	protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity placer, ItemStack stack, BlockState state) {
		if (!world.isClient()) {
			// If adjacent pipes have us blacklisted, we blacklist them here.
			BlockEntity be = world.getBlockEntity(pos);

			if (be instanceof PipeBlockEntity) {
				PipeBlockEntity pipe = (PipeBlockEntity) be;

				for (Direction direction : Direction.values()) {
					BlockEntity adjBe = world.getBlockEntity(pos.offset(direction));

					if (adjBe instanceof PipeBlockEntity) {
						if ((((PipeBlockEntity) adjBe).connectionBlacklist & (1 << direction.getOpposite().getId())) > 0) {
							pipe.connectionBlacklist |= 1 << direction.getId();
						}
					}
				}
			}
			// else warn?
		}

		// No clue what the return is for, vanilla doesn't seem to use it.
		return true;
	}
}
