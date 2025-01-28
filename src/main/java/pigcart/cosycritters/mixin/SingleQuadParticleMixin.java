package pigcart.cosycritters.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pigcart.cosycritters.RotationOverride;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin extends Particle implements RotationOverride {

    protected SingleQuadParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/SingleQuadParticle$FacingCameraMode;setRotation(Lorg/joml/Quaternionf;Lnet/minecraft/client/Camera;F)V")
    )
    private void setRotation(SingleQuadParticle.FacingCameraMode facingCameraMode, Quaternionf quaternionf, Camera camera, float tickPercent) {
        setParticleRotation(facingCameraMode, quaternionf, camera, tickPercent);
    }

}