package dev.technici4n.moderndynamics.network.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemPath {
	/**
	 * Starting position of the items, i.e. the chest they were pulled from. (Not the pipe!)
	 */
	public final BlockPos startingPos;
	public final BlockPos targetPos;
	public final Direction[] path;

	public ItemPath(BlockPos startingPos, BlockPos targetPos, Direction[] path) {
		this.startingPos = startingPos;
		this.targetPos = targetPos;
		this.path = path;
	}

	public SimulatedInsertionTarget getInsertionTarget(World world) {
		return SimulatedInsertionTargets.getTarget(world, targetPos, path[path.length - 1].getOpposite());
	}

	public TravelingItem makeTravelingItem(ItemVariant variant, long amount) {
		return new TravelingItem(
				variant,
				amount,
				this,
				FailedInsertStrategy.SEND_BACK_TO_SOURCE,
				0
		);
	}
}
