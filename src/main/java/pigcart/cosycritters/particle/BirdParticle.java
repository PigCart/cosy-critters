package pigcart.cosycritters.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;
import pigcart.cosycritters.Util;
import pigcart.cosycritters.config.ConfigManager;

import java.util.List;
import java.util.Optional;

import static pigcart.cosycritters.config.ConfigManager.config;

public class BirdParticle extends TextureSheetParticle {

    int spawnAnimationLength = 40;
    int spawnAnimationTime = spawnAnimationLength;
    Vec3 spawnAnimationStart;
    Vec3 spawnAnimationEnd;
    Vector3f facing;
    Behaviour behaviour;
    BlockState perch;

    private enum Behaviour {
        SPAWNING,
        PERCHED,
        CHECKING,
        FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE
    }

    private BirdParticle(ClientLevel level, double x, double y, double z, double landAtX, double landAtY, double landAtZ) {
        super(level, x, y, z);
        this.hasPhysics = false;
        this.quadSize = 0;
        this.lifetime = 6000;
        this.facing = new Vector3f((this.random.nextFloat() - 0.5F), this.random.nextFloat(), (this.random.nextFloat() - 0.5F)).normalize().mul(0.5F);
        this.spawnAnimationStart = new Vec3(x, y, z);
        this.spawnAnimationEnd = new Vec3(landAtX, landAtY, landAtZ);
        setBehaviour(Behaviour.SPAWNING);
        // for some reason removed particles will crash if a sprite wasnt set in the constructor
        this.setSprite(Util.getSprite("crow_" + getRelativeDirection()));
    }

    @Override
    public Optional<ParticleGroup> getParticleGroup() {
        return Optional.of(ConfigManager.birdGroup);
    }

    private void setBehaviour(Behaviour behaviour) {
        this.behaviour = behaviour;
        switch (behaviour) {
            case PERCHED -> this.perch = level.getBlockState(BlockPos.containing(x, y - 0.5, z));
            case FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE -> {
                this.lifetime = 100;
                this.age = 0;
                // for some reason updating the velocity after this sends the bird to its spawn position????
                this.setPos(spawnAnimationEnd.x, spawnAnimationEnd.y, spawnAnimationEnd.z);
            }
        }
    }

    private String getRelativeDirection() {
        Vector2f birdFacing = new Vector2f(facing.x, facing.z).normalize();
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vector2f relativeDirection = new Vector2f((float) (x - camPos.x()), (float) (z - camPos.z())).normalize();
        float a = Math.atan2(relativeDirection.y, relativeDirection.x);
        float b = Math.atan2(birdFacing.y, birdFacing.x);
        float c = a - b;
        if (c > Mth.PI) {
            c -= Mth.TWO_PI;
        } else if (c < -Mth.PI) {
            c += Mth.TWO_PI;;
        }
        if (c < 0) {
            return "right";
        } else {
            return "left";
        }
         //away / toward / left / right
         //potentially also away_left, away_right, toward_left, toward_right
    }
    private void reactToDisturbances() {
        if (!perch.equals(level.getBlockState(BlockPos.containing(x, y-0.5, z)))) {
            setBehaviour(Behaviour.FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE);
        } else if (this.age % config.birdReactionSpeed == 0) {
            Vec3 birdPos = new Vec3(this.x, this.y, this.z);
            final List<Entity> nearbyEntities = level.getEntitiesOfClass(Entity.class, AABB.ofSize(birdPos,
                    config.birdReactionDistance, config.birdReactionDistance, config.birdReactionDistance));
            if (!nearbyEntities.isEmpty()) {
                setBehaviour(Behaviour.FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE);
            }
            else if (!Minecraft.getInstance().cameraEntity.position().closerThan(birdPos, config.birdDespawnDistance)) {
                this.remove();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        switch (behaviour) {
            case SPAWNING -> {
                this.setSprite(Util.getSprite("crow_flying_%s_%d".formatted(getRelativeDirection(), (age / 2) % 2)));
                if (spawnAnimationTime != 0) {
                    spawnAnimationTime--;
                    this.x = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.x, spawnAnimationStart.x);
                    this.y = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.y, spawnAnimationStart.y);
                    this.z = Mth.lerp((double) spawnAnimationTime / spawnAnimationLength, spawnAnimationEnd.z, spawnAnimationStart.z);
                    this.quadSize = Mth.lerp((float) spawnAnimationTime / spawnAnimationLength, 0.5F, 0);
                } else {
                    setBehaviour(Behaviour.PERCHED);
                }
            }
            case PERCHED -> {
                if (random.nextFloat() < 0.01) setBehaviour(Behaviour.CHECKING);
                this.setSprite(Util.getSprite("crow_" + getRelativeDirection()));
                reactToDisturbances();
            }
            case CHECKING -> {
                if (random.nextFloat() < 0.02) setBehaviour(Behaviour.PERCHED);
                this.setSprite(Util.getSprite("crow_checking_" + getRelativeDirection()));
                reactToDisturbances();
            }
            case FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE -> {
                this.setSprite(Util.getSprite("crow_flying_%s_%d".formatted(getRelativeDirection(), (age / 2) % 2)));
                this.quadSize = Mth.lerp((float) age / lifetime, 0.5F, 0);
                this.xd = this.facing.x;
                this.yd = this.facing.y;
                this.zd = this.facing.z;
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