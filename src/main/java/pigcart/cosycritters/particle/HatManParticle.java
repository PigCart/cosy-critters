package pigcart.cosycritters.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class HatManParticle extends CritterParticle {

    private HatManParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z, spriteSet.get(level.random));
        this.sprite = spriteSet.get(this.random);
        this.quadSize = 1F;
        this.lifetime = 6000;
    }

    @Override
    public void tick() {
        super.tick();
        if (!Minecraft.getInstance().player.isSleeping()) this.remove();
    }

    //? if >= 1.21.9 {
    /*@Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }
    *///?} else {
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
     //?}

    public static class Provider extends CritterProvider {
        public Provider(SpriteSet spriteSet) {super(spriteSet);}
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new HatManParticle(level, x, y, z, this.spriteSet);
        }
    }
}