package pigcart.cosycritters.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;
import pigcart.cosycritters.Util;
import pigcart.cosycritters.config.ConfigManager;

import java.text.DecimalFormat;
import java.util.Optional;

import static pigcart.cosycritters.config.ConfigManager.config;

public class BirdParticle extends TextureSheetParticle {

    int spawnAnimationLength = 40;
    int spawnAnimationTime = spawnAnimationLength;
    Vec3 spawnAnimationStart;
    Vec3 spawnAnimationEnd;
    boolean spawnAnimation = true;
    boolean flyUpAwayToTheSun = false;
    Vector3f facing;
    Behaviour behaviour;

    private enum Behaviour {
        FLYING,
        PERCHED,
        CHECKING
    }

    private BirdParticle(ClientLevel level, double x, double y, double z, double landAtX, double landAtY, double landAtZ) {
        super(level, x, y, z);
        this.quadSize = 0;
        this.lifetime = 6000;
        this.facing = new Vector3f((this.random.nextFloat() - 0.5F), this.random.nextFloat(), (this.random.nextFloat() - 0.5F)).normalize().mul(0.5F);
        this.spawnAnimationStart = new Vec3(x, y, z);
        this.spawnAnimationEnd = new Vec3(landAtX, landAtY, landAtZ);
        setBehaviour(Behaviour.FLYING);
        // for some reason removed particles will crash if a sprite wasnt set in the constructor
        this.setSprite(Util.getSprite("crow_" + getRelativeDirection()));
    }

    @Override
    public Optional<ParticleGroup> getParticleGroup() {
        return Optional.of(ConfigManager.birdGroup);
    }

    private void setBehaviour(Behaviour behaviour) {
        this.behaviour = behaviour;
    }

    private String getRelativeDirection() {
        Vector2f birdFacing = new Vector2f(facing.x, facing.z).normalize();
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vector2f relativeDirection = new Vector2f((float) (x - camPos.x()), (float) (z - camPos.z())).normalize();
        float a = Math.atan2(relativeDirection.y, relativeDirection.x) / Mth.PI;
        float b = Math.atan2(birdFacing.y, birdFacing.x) / Mth.PI;
        float c = a - b;
        if (c > 1) {
            c = c - 2;
        } else if (c < -1) {
            c = c + 2;
        }
        if (c < 0) {
            return "right";
        } else {
            return "left";
        }
         //away / toward / left / right
         //potentially also away_left, away_right, toward_left, toward_right
    }

    @Override
    public void tick() {
        super.tick();
        switch (behaviour) {
            case FLYING -> {
                this.setSprite(Util.getSprite("crow_flying_%s_%d".formatted(getRelativeDirection(), (age / 2) % 2)));
            }
            case PERCHED -> {
                if (random.nextFloat() < 0.01) setBehaviour(Behaviour.CHECKING);
                this.setSprite(Util.getSprite("crow_" + getRelativeDirection()));
            }
            case CHECKING -> {
                if (random.nextFloat() < 0.02) setBehaviour(Behaviour.PERCHED);
                this.setSprite(Util.getSprite("crow_checking_" + getRelativeDirection()));
            }
        }
        if (spawnAnimation) {
            if (spawnAnimationTime != 0) {
                spawnAnimationTime--;
                this.x = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.x, spawnAnimationStart.x);
                this.y = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.y, spawnAnimationStart.y);
                this.z = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.z, spawnAnimationStart.z);
                this.quadSize = Mth.lerp((float) spawnAnimationTime / spawnAnimationLength, 0.5F, 0);
            } else {
                spawnAnimation = false;
                setBehaviour(Behaviour.PERCHED);
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
            if (Minecraft.getInstance().cameraEntity.position().distanceTo(birdPos) < config.birdDisturbDistance && !flyUpAwayToTheSun) {
                flyUpAwayToTheSun = true;
                setBehaviour(Behaviour.FLYING);
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

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        public Provider(SpriteSet spriteSet) {
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double landAtX, double landAtY, double landAtZ) {
            return new BirdParticle(level, x, y, z, landAtX, landAtY, landAtZ);
        }
    }
}