package pigcart.cosycritters.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static pigcart.cosycritters.config.ConfigManager.config;

public class BirdParticle extends CritterParticle {

    Vector3f target;
    Behaviour behaviour;
    BlockState perch;
    // boids variables
    public static final Collection<BirdParticle> birds = new ArrayList<>();
    int behaviourTime;

    private enum Behaviour {
        FLYING,
        LANDING,
        PERCHED,
        CHECKING,
        FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE
    }

    private BirdParticle(ClientLevel level, double x, double y, double z, double landAtX, double landAtY, double landAtZ) {
        super(level, x, y, z, Util.getSprite("crow_left"));
        if (entitiesNearby(new Vec3(landAtX, landAtY, landAtZ))) this.remove();
        this.quadSize = 0.5F;
        this.setSize(0.5F, 0.5F);
        this.lifetime = 6000;
        this.target = new Vector3f((this.random.nextFloat() - 0.5F), this.random.nextFloat(), (this.random.nextFloat() - 0.5F)).normalize().mul(0.5F);
        setBehaviour(Behaviour.FLYING);
        birds.add(this);
        CosyCritters.birds++;
        this.xd = target.x;
        this.yd = target.y;
        this.zd = target.z;
    }

    private void setBehaviour(Behaviour behaviour) {
        this.behaviour = behaviour;
        behaviourTime = 0;
        switch (behaviour) {
            case LANDING -> {
                BlockPos.MutableBlockPos highest = new BlockPos.MutableBlockPos(this.x, Integer.MIN_VALUE, this.z);
                for (int i = 0; i < 3; i++) {
                    int x = random.nextIntBetweenInclusive((int)this.x - 16, (int)this.x + 16);
                    int z = random.nextIntBetweenInclusive((int)this.z - 16, (int)this.z + 16);
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                    if (y > highest.getY()) highest.set(x, y, z);
                }
                this.target = highest.getCenter().toVector3f();
            }
            case PERCHED -> {
                this.setParticleSpeed(0, 0, 0);
                this.perch = level.getBlockState(BlockPos.containing(x, y - 0.5, z));
            }
            case FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE -> {
                this.lifetime = 100;
                this.age = 0;
            }
        }
    }

    private String getRelativeDirection(float facingX, float facingZ) {
        Vector2f facing = new Vector2f(facingX, facingZ).normalize();
        Vec3 camPos = Util.getCameraPos();
        Vector2f relativeDirection = new Vector2f((float) (x - camPos.x()), (float) (z - camPos.z())).normalize();
        float a = Math.atan2(relativeDirection.y, relativeDirection.x);
        float b = Math.atan2(facing.y, facing.x);
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
        if (perch != null && !perch.equals(level.getBlockState(BlockPos.containing(x, y-0.5, z)))) {
            setBehaviour(Behaviour.FLYING);
        } else if (this.age % config.birdReactionSpeed == 0) {
            Vec3 birdPos = new Vec3(this.x, this.y, this.z);
            if (entitiesNearby(birdPos)) {
                setBehaviour(Behaviour.FLYING);
            }
        }
    }
    private boolean entitiesNearby(Vec3 pos) {
        final List<Entity> nearbyEntities = level.getEntitiesOfClass(Entity.class, AABB.ofSize(pos,
                config.birdReactionDistance, config.birdReactionDistance, config.birdReactionDistance));
        return !nearbyEntities.isEmpty();
    }

    @Override
    public void remove() {
        birds.remove(this);
        CosyCritters.birds--;
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();
        if (Util.getCameraPos().distanceToSqr(x, y, z) > Mth.square(config.birdDespawnDistance)) {
            this.remove();
        }
        behaviourTime++;
        switch (behaviour) {
            case FLYING -> { // naive boids implementation
                // separation - avoid running into other boids
                int avoidRange = config.birdAvoidanceDistance;
                double close_xd = 0;
                double close_yd = 0;
                double close_zd = 0;
                for (BirdParticle other : birds) {
                    if (other != this && Vector3d.distance(x, y, z, other.x, other.y, other.z) < avoidRange) {
                        close_xd += this.x - other.x;
                        close_yd += this.y - other.y;
                        close_zd += this.z - other.z;
                    }
                }
                this.xd += close_xd * config.birdAvoidanceFactor;
                this.yd += close_yd * config.birdAvoidanceFactor;
                this.zd += close_zd * config.birdAvoidanceFactor;

                // alignment - match the velocity of other boids
                int range = config.birdFlockRange;
                double xvel_avg = 0; double yvel_avg = 0; double zvel_avg = 0;
                int neighbors = 0;
                for (BirdParticle other : birds) {
                    if (other != this && Vector3d.distance(x, y, z, other.x, other.y, other.z) < range) {
                        xvel_avg += other.xd;
                        yvel_avg += other.yd;
                        zvel_avg += other.zd;
                        neighbors += 1;
                    }
                }
                if (neighbors > 0) {
                    xvel_avg = xvel_avg / neighbors;
                    yvel_avg = yvel_avg / neighbors;
                    zvel_avg = zvel_avg / neighbors;
                }
                this.xd += (xvel_avg - this.xd) * config.birdVelocityMatchFactor;
                this.yd += (yvel_avg - this.yd) * config.birdVelocityMatchFactor;
                this.zd += (zvel_avg - this.zd) * config.birdVelocityMatchFactor;

                // cohesion - steer toward the center of mass of other boids
                double xpos_avg = 0;
                double ypos_avg = 0;
                double zpos_avg = 0;
                neighbors = 0;
                for (BirdParticle other : birds) {
                    if (other != this && Vector3d.distance(x, y, z, other.x, other.y, other.z) < range) {
                        xpos_avg += other.x;
                        ypos_avg += other.y;
                        zpos_avg += other.z;
                        neighbors += 1;
                    }
                }
                if (neighbors > 0) {
                    xpos_avg = xpos_avg / neighbors;
                    ypos_avg = ypos_avg / neighbors;
                    zpos_avg = zpos_avg / neighbors;
                }
                this.xd += (xpos_avg - this.x) * config.birdCenteringFactor;
                this.yd += (ypos_avg - this.y) * config.birdCenteringFactor;
                this.zd += (zpos_avg - this.z) * config.birdCenteringFactor;

                // constrain height
                if (y - level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z) > config.birdFlightHeightLimit) this.yd -= 0.1;

                // avoid colliding with blocks
                int avoidDistance = config.birdAvoidanceDistance;
                Vec3 lineToCast = new Vec3(xd, yd, zd).normalize().multiply(avoidDistance, avoidDistance, avoidDistance);
                Vec3 start = new Vec3(x, y, z);
                Vec3 end = start.add(lineToCast);
                final BlockHitResult hitResult = level.clip(Util.getClipContext(start, end));
                if (!hitResult.getType().equals(HitResult.Type.MISS)) {
                    xd -= Math.signum(xd) * config.birdAvoidanceFactor;
                    yd -= Math.signum(yd) * config.birdAvoidanceFactor;
                    zd -= Math.signum(zd) * config.birdAvoidanceFactor;
                }

                // constrain speed
                double speed = Vector3d.length(xd, yd, zd);
                if (speed > config.birdMaxSpeed) {
                    this.xd = (this.xd / speed) * config.birdMaxSpeed;
                    this.yd = (this.yd / speed) * config.birdMaxSpeed;
                    this.zd = (this.zd / speed) * config.birdMaxSpeed;
                } else if (speed < config.birdMinSpeed) {
                    this.xd = (this.xd / speed) * config.birdMinSpeed;
                    this.yd = (this.yd / speed) * config.birdMinSpeed;
                    this.zd = (this.zd / speed) * config.birdMinSpeed;
                }

                if (behaviourTime > config.birdFlightTime) {
                    setBehaviour(Behaviour.LANDING);
                }

                this.setSprite(Util.getSprite("crow_flying_%s_%d".formatted(getRelativeDirection((float)xd, (float)zd), (age / 2) % 2)));
            }
            case LANDING -> {
                this.setSprite(Util.getSprite("crow_flying_%s_1".formatted(getRelativeDirection((float)xd, (float)zd))));
                final float distance = target.distance((float) x, (float) y, (float) z);
                final float responsiveness = config.birdLandingResponsiveness;

                Vec3 targetOffset = new Vec3(x - target.x, y - target.y, z - target.z);
                //Gizmos.arrow(new Vec3(x, y, z), new Vec3(target), ARGB.color(1,0,0));

                //TODO rotate velocity direction directly towards target when close to it
                if (this.x > target.x) this.xd -= responsiveness;
                if (this.x < target.x) this.xd += responsiveness;
                if (this.y > target.y) this.yd -= responsiveness;
                if (this.y < target.y) this.yd += responsiveness;
                if (this.z > target.z) this.zd -= responsiveness;
                if (this.z < target.z) this.zd += responsiveness;
                if (distance < 0.1) setBehaviour(Behaviour.PERCHED);
            }
            case PERCHED -> {
                if (random.nextFloat() < 0.01) setBehaviour(Behaviour.CHECKING);
                this.setSprite(Util.getSprite("crow_" + getRelativeDirection(target.x, target.z)));
                reactToDisturbances();
            }
            case CHECKING -> {
                if (random.nextFloat() < 0.02) setBehaviour(Behaviour.PERCHED);
                this.setSprite(Util.getSprite("crow_checking_" + getRelativeDirection(target.x, target.z)));
                reactToDisturbances();
            }
            case FLY_UP_AWAY_TO_THE_SUN_LIKE_A_FEATHERY_PIECE_OF_GARGBAGE -> {
                this.setSprite(Util.getSprite("crow_flying_%s_%d".formatted(getRelativeDirection(target.x, target.z), (age / 2) % 2)));
                this.quadSize = Mth.lerp((float) age / lifetime, 0.5F, 0);
                this.xd = this.target.x;
                this.yd = this.target.y;
                this.zd = this.target.z;
            }
        }
    }

    public static class Provider extends CritterProvider {
        public Provider(SpriteSet spriteSet) {super(spriteSet);}
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new BirdParticle(level, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}