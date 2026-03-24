package pigcart.cosycritters.mixin.access;

import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {

    //? < 1.21.9 {
    @Accessor
    net.minecraft.client.renderer.texture.TextureAtlas getTextureAtlas();
    //?}

    //? forge {
    /*@Accessor
    java.util.Map<net.minecraft.resources.ResourceLocation, net.minecraft.client.particle.ParticleProvider<?>> getProviders();
    *///?}

    @Invoker
    void callClearParticles();
}
