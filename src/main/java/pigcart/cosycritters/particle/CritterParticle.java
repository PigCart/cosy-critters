package pigcart.cosycritters.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
//? if >= 1.21.9 {
/*import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleLimit;
*///?} else {
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
//?}
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

//? if >= 1.21.9 {
/*public abstract class CritterParticle extends SingleQuadParticle {
*///?} else {
public abstract class CritterParticle extends TextureSheetParticle {
 //?}
    protected CritterParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        //? if >=1.21.9 {
        /*super(level, x, y, z, sprite);
        *///?} else {
        super(level, x, y, z);
        this.sprite = sprite;
        //?}
        this.hasPhysics = false;
    }

    @Override
    //? if >= 1.21.9 {
    /*protected Layer getLayer() {
        return Layer.OPAQUE;
    }
    *///?} else {
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
     //?}

    public abstract static class CritterProvider implements ParticleProvider<SimpleParticleType> {

        SpriteSet spriteSet;

        CritterProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public abstract Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ);
        //? if >=1.21.9 {
        /*public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource randomSource) {
            return createParticle(type, level, x, y, z, velocityX, velocityY, velocityZ);
        }
        *///?}
    }
}
