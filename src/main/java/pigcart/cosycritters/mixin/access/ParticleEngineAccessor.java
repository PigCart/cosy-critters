package pigcart.cosycritters.mixin.access;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.particle.ParticleEngine;
//? if >= 1.21.9 {
/*import net.minecraft.core.particles.ParticleLimit;
*///?} else {
import net.minecraft.core.particles.ParticleGroup;
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

    @Accessor
    Object2IntOpenHashMap<
            //? if >= 1.21.9 {
            /*ParticleLimit
            *///?} else {
            ParticleGroup
            //?}
            > getTrackedParticleCounts();

    @Invoker
    boolean callHasSpaceInParticleLimit(
            //? if >= 1.21.9 {
            /*ParticleLimit
            *///?} else {
            ParticleGroup
            //?}
            group);

    @Invoker
    void callClearParticles();
}
