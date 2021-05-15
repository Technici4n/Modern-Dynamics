package dev.technici4n.moderntransportation.network.item;

public enum ItemPipeTier {
    BASIC(1000, 1);

    public final long virtualDistance;
    public final double itemSpeed;

    ItemPipeTier(long virtualDistance, double itemSpeed) {
        this.virtualDistance = virtualDistance;
        this.itemSpeed = itemSpeed;
    }
}
