package pigcart.cosycritters.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import pigcart.cosycritters.Cosycritters;

public class BirdParticle extends TextureSheetParticle {

    boolean flying = false;
    Vec3 facing;

    private BirdParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z);
        this.sprite = provider.get(this.random);
        this.quadSize = 0.5F;
        this.lifetime = 6000;
        this.facing = new Vec3(this.random.nextFloat() - 0.5, this.random.nextFloat(), this.random.nextFloat() - 0.5);
        Cosycritters.birdCount++;
    }

    @Override
    public void remove() {
        Cosycritters.birdCount--;
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age % 20 == 0) {
            Vec3 birdPos = new Vec3(this.x, this.y, this.z);
            if (Minecraft.getInstance().cameraEntity.position().distanceTo(birdPos) < 10) {
                flying = true;
                this.sprite = Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(Cosycritters.MOD_ID, "bird_flight"));
                this.lifetime = 100;
                this.age = 0;
            }
        }
        if (flying) {
            this.quadSize = Mth.lerp((float) age / lifetime, 0.5F, 0);
            this.xd = this.facing.x;
            this.yd = this.facing.y;
            this.zd = this.facing.z;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new BirdParticle(level, x, y, z, this.provider);
        }
    }
}