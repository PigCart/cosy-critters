package pigcart.cosycritters.mixin;

import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.particle.BirdParticle;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "clearParticles", at = @At("HEAD"))
    public void clearParticles(CallbackInfo ci) {
        BirdParticle.birds.clear();
        CosyCritters.spiders = 0;
        CosyCritters.moths = 0;
    }
}
