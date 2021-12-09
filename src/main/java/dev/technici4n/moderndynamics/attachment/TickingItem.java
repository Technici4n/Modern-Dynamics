package dev.technici4n.moderndynamics.attachment;

public class TickingItem extends ConfigurableAttachmentItem {
	public final int batchSize, tickFrequency;
	private final boolean servo;

	public TickingItem(Attachment attachment, int configWidth, int configHeight, int extractCount, int extractFrequency, boolean servo) {
		super(attachment, configWidth, configHeight);
		this.batchSize = extractCount;
		this.tickFrequency = extractFrequency;
		this.servo = servo;
	}

	public boolean isServo() {
		return servo;
	}

	public boolean isRetriever() {
		return !servo;
	}
}
