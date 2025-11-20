package pigcart.cosycritters.mixin.access;

import net.minecraft.client.particle.ParticleEngine;
//? if < 1.21.9 {
import net.minecraft.client.renderer.texture.TextureAtlas;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {

    //? if < 1.21.9 {
    @Accessor
    TextureAtlas getTextureAtlas();
    //?}

    @Invoker
    void callClearParticles();
}
