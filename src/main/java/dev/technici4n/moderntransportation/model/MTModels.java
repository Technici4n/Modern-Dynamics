package dev.technici4n.moderntransportation.model;

import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraftforge.client.model.data.ModelProperty;

public class MTModels {
    public static final ModelProperty<Byte> CONNECTIONS = new ModelProperty<>();

    /**
     * The model rotation to rotate a model facing NORTH to the correct facing direction.
     * Rotations are indexed by {@link Direction} id.
     */
    public static final ModelBakeSettings[] PIPE_BAKE_SETTINGS = new ModelBakeSettings[] {
            ModelRotation.X90_Y0,
            ModelRotation.X270_Y0,
            ModelRotation.X0_Y0,
            ModelRotation.X0_Y180,
            preRotated(ModelRotation.X0_Y270, 90),
            ModelRotation.X0_Y90,
    };

    public static ModelBakeSettings preRotated(ModelRotation rotation, float preAngle) {
        AffineTransformation preRotation = new AffineTransformation(null, new Quaternion(new Vector3f(0, 0, 1), preAngle, true), null, null);
        AffineTransformation combined = rotation.getRotation().multiply(preRotation);
        return new ModelBakeSettings() {
            @Override
            public AffineTransformation getRotation() {
                return combined;
            }
        };
    }
}
