package pigcart.cosycritters.mixin;

import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.cosycritters.CosyCritters;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(at = @At("HEAD"), method = "clearParticles")
    private void clearParticles(CallbackInfo ci) {
        CosyCritters.spiderCount = 0;
        CosyCritters.birdCount = 0;
        CosyCritters.mothCount = 0;
    }
}
