package dev.technici4n.moderntransportation.block;

import dev.technici4n.moderntransportation.util.MtItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class PipeItem extends BlockItem {
	public PipeItem(PipeBlock block) {
		super(block, new Item.Settings().group(MtItemGroup.getInstance()));
		setRegistryName(block.getRegistryName());
		block.setItem(this);
	}
}
