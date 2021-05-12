package dev.technici4n.moderntransportation.network.energy;

public enum EnergyPipeTier {
    BASIC(1000, 1000),
    HARDENED(4000, 4000),
    REINFORCED(9000, 9000),
    SIGNALUM(16000, 16000),
    RESONANT(25000, 25000);

    // TODO: config
    private final int capacity;
    private final int maxConnectionTransfer;

    EnergyPipeTier(int capacity, int maxConnectionTransfer) {
        this.capacity = capacity;
        this.maxConnectionTransfer = maxConnectionTransfer;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getMaxConnectionTransfer() {
        return maxConnectionTransfer;
    }
}
