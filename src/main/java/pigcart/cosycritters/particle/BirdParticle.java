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

    int spawnAnimationLength = 40;
    int spawnAnimationTime = spawnAnimationLength;
    Vec3 spawnAnimationStart;
    Vec3 spawnAnimationEnd;
    boolean spawnAnimation = true;
    boolean flyUpAwayToTheSun = false;
    Vec3 facing;

    private BirdParticle(ClientLevel level, double x, double y, double z, double landAtX, double landAtY, double landAtZ, SpriteSet provider) {
        super(level, x, y, z);
        this.sprite = Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(Cosycritters.MOD_ID, random.nextBoolean() ? "bird_flight_left" : "bird_flight_right"));
        this.quadSize = 0;
        this.lifetime = 6000;
        this.facing = new Vec3((this.random.nextFloat() - 0.5), this.random.nextFloat(), (this.random.nextFloat() - 0.5)).normalize().multiply(0.5, 0.5, 0.5);
        this.spawnAnimationStart = new Vec3(x, y, z);
        this.spawnAnimationEnd = new Vec3(landAtX, landAtY, landAtZ);
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
        if (spawnAnimation) {
            if (spawnAnimationTime != 0) {
                spawnAnimationTime--;
                this.x = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.x, spawnAnimationStart.x);
                this.y = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.y, spawnAnimationStart.y);
                this.z = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.z, spawnAnimationStart.z);
                this.quadSize = Mth.lerp((float) spawnAnimationTime / spawnAnimationLength, 0.5F, 0);
            } else {
                spawnAnimation = false;
                this.sprite = Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(Cosycritters.MOD_ID, random.nextBoolean() ? "bird_left" : "bird_right"));
            }
        }
        else if (flyUpAwayToTheSun) {
            this.quadSize = Mth.lerp((float) age / lifetime, 0.5F, 0);
            this.xd = this.facing.x;
            this.yd = this.facing.y;
            this.zd = this.facing.z;
        }
        else if (this.age % 20 == 0) {
            Vec3 birdPos = new Vec3(this.x, this.y, this.z);
            if (Minecraft.getInstance().cameraEntity.position().distanceTo(birdPos) < 10 && !flyUpAwayToTheSun) {
                flyUpAwayToTheSun = true;
                this.sprite = Minecraft.getInstance().particleEngine.textureAtlas.getSprite(ResourceLocation.fromNamespaceAndPath(Cosycritters.MOD_ID, random.nextBoolean() ? "bird_flight_left" : "bird_flight_right"));
                this.lifetime = 100;
                this.age = 0;
                // for some reason updating the velocity after this sends the bird to its spawn position????
                this.setPos(spawnAnimationEnd.x, spawnAnimationEnd.y, spawnAnimationEnd.z);
            }
            else if (!Minecraft.getInstance().cameraEntity.position().closerThan(new Vec3(x, y, z), 64)) {
                this.remove();
            }
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
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double landAtX, double landAtY, double landAtZ) {
            return new BirdParticle(level, x, y, z, landAtX, landAtY, landAtZ, this.provider);
        }
    }
}