package dev.technici4n.moderndynamics.network.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

class SimpleEnergyStorage extends EnergyStorage {
    public SimpleEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void addEnergyStored(int energy) {
        this.energy += energy;
    }

    public void reduceEnergyStored(int energy) {
        this.energy -= energy;
    }
}
