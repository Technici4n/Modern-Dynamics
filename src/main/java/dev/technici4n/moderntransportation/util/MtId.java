package dev.technici4n.moderntransportation.util;

import net.minecraft.util.Identifier;

public class MtId {

    public static final String MOD_ID = "moderntransportation";

    public static Identifier of(String path) {
        return new Identifier(MOD_ID, path);
    }
}
