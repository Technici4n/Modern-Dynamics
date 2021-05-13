package dev.technici4n.moderntransportation.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class ShapeHelper {
    /**
     * Return true if the passed shape contains the position, or is on its border.
     */
    public static boolean shapeContains(VoxelShape shape, Vec3d posInBlock) {
        for (Box box : shape.getBoundingBoxes()) {
            // Move slightly toward the center of the box
            Vec3d centerDirection = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);

            if (box.contains(posInBlock.add(centerDirection))) {
                return true;
            }
        }

        return false;
    }
}
