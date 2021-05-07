package dev.technici4n.moderntransportation.util;

import net.minecraft.util.Identifier;

public class MTId {
    public static Identifier of(String path) {
        return new Identifier("moderntransportation", path);
    }
}
