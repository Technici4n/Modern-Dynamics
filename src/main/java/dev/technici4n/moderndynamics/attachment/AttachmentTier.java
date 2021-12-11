package dev.technici4n.moderndynamics.attachment;

public enum AttachmentTier {
	BASIC(5, 1, 4, 40, 1),
	IMPROVED(5, 2, 16, 20, 2),
	ADVANCED(5, 3, 64, 10, 3),
	;

	public final int configWidth;
	public final int configHeight;
	public final int transferCount;
	public final int transferFrequency;
	public final int speedupFactor;

	AttachmentTier(int configWidth, int configHeight, int transferCount, int transferFrequency, int speedupFactor) {
		this.configWidth = configWidth;
		this.configHeight = configHeight;
		this.transferCount = transferCount;
		this.transferFrequency = transferFrequency;
		this.speedupFactor = speedupFactor;
	}

	public boolean allowAdvancedBehavior() {
		return this == ADVANCED;
	}
}
