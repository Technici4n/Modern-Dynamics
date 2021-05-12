package dev.technici4n.moderntransportation.util;

import com.google.common.base.Preconditions;
import dev.technici4n.moderntransportation.init.MtItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MtItemGroup extends ItemGroup {
	private static MtItemGroup INSTANCE;

	public MtItemGroup() {
		super(MtId.MOD_ID);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ItemStack createIcon() {
		return new ItemStack(MtItems.BASIC_ITEM_PIPE);
	}

	public static MtItemGroup getInstance() {
		Preconditions.checkState(INSTANCE != null, "item group has not been initialized yet");
		return INSTANCE;
	}

	public static void init() {
		Preconditions.checkState(INSTANCE == null, "item group has already been initialized");
		INSTANCE = new MtItemGroup();
	}

}
