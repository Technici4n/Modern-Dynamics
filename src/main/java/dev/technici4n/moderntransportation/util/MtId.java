package dev.technici4n.moderntransportation.util;

import net.minecraft.util.Identifier;

public class MtId {
    public static Identifier of(String path) {
        return new Identifier("moderntransportation", path);
    }
}
