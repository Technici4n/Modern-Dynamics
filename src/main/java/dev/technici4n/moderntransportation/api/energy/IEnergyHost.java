package dev.technici4n.moderntransportation.api.energy;

import dev.technici4n.moderntransportation.api.network.INodeHost;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public interface IEnergyHost extends INodeHost {
    int getEnergy();

    int getMaxEnergy();

    void setEnergy(int energy);

    void addEnergyStorages(List<IEnergyStorage> out);
}
