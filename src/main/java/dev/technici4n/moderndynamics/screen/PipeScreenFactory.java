package dev.technici4n.moderndynamics.screen;

import dev.technici4n.moderndynamics.attachment.ConfigurableAttachmentItem;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class PipeScreenFactory implements ExtendedScreenHandlerFactory {
	private final PipeBlockEntity pipe;
	private final Direction side;
	private final ItemStack attachment;
	private final ConfigBackend backend;

	public PipeScreenFactory(PipeBlockEntity pipe, Direction side, ItemStack attachment) {
		this.pipe = pipe;
		this.side = side;
		this.attachment = attachment;
		this.backend = new ConfigBackend() {
			@Override
			public ConfigurableAttachmentItem getAttachment() {
				return (ConfigurableAttachmentItem) attachment.getItem();
			}

			@Override
			public ItemVariant getItemVariant(int x, int y) {
				return getAttachment().getItemVariant(attachment, x, y);
			}

			@Override
			public void setItemVariant(int x, int y, ItemVariant variant) {
				getAttachment().setItemVariant(attachment, x, y, variant);
				pipe.markDirty();
			}
		};
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeItemStack(attachment);
	}

	@Override
	public Text getDisplayName() {
		return attachment.getName();
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new AttachmentScreenHandler(syncId, inv, backend) {
			@Override
			public boolean canUse(PlayerEntity player) {
				var pos = pipe.getPos();
				if (pipe.getWorld().getBlockEntity(pos) != pipe) return false;
				if (player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return false;
				return pipe.getAttachment(side) == attachment;
			}
		};
	}
}
