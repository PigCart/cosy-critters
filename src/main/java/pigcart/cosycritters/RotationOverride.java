package pigcart.cosycritters;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.SingleQuadParticle;
import org.joml.Quaternionf;

public interface RotationOverride {
    default void setParticleRotation(SingleQuadParticle.FacingCameraMode facingCameraMode, Quaternionf quaternionf, Camera camera, float tickPercent) {
        facingCameraMode.setRotation(quaternionf, camera, tickPercent);
    }
}
