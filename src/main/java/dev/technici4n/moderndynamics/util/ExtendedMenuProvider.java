package dev.technici4n.moderndynamics.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;

public interface ExtendedMenuProvider extends MenuProvider {
    void writeScreenOpeningData(FriendlyByteBuf buf);
}
