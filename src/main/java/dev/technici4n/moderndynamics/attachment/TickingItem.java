package dev.technici4n.moderndynamics.attachment;

public class TickingItem extends ConfigurableAttachmentItem {
	public final AttachmentTier tier;
	private final boolean servo;

	public TickingItem(RenderedAttachment attachment, AttachmentTier tier, boolean servo) {
		super(attachment, tier.configWidth, tier.configHeight);
		this.tier = tier;
		this.servo = servo;
	}

	public boolean isServo() {
		return servo;
	}

	public boolean isRetriever() {
		return !servo;
	}
}
