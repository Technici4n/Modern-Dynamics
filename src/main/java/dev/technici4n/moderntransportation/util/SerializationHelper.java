package dev.technici4n.moderntransportation.util;

import net.minecraft.util.math.Direction;

import java.util.EnumSet;

public class SerializationHelper {
    public static byte directionsToMask(EnumSet<Direction> directions) {
        byte result = 0;

        for (Direction direction : directions) {
            result |= 1 << direction.getId();
        }

        return result;
    }

    public static EnumSet<Direction> directionsFromMask(byte mask) {
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);

        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) > 0) {
                result.add(Direction.byId(i));
            }
        }

        return result;
    }
}
