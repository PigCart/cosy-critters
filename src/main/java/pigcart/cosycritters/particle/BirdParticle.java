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
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.Util;
import pigcart.cosycritters.config.ConfigData;
import pigcart.cosycritters.config.ConfigManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BirdParticle extends CritterParticle {

    public static final ConfigData.BirdOptions CONFIG = ConfigManager.config.bird;
    Vector3f target;
    Behaviour behaviour;
    BlockState perch;
    public static final Collection<BirdParticle> birds = new ArrayList<>();
    int behaviourTime;

    private enum Behaviour {
        FLYING,
        LANDING,
        PERCHED,
        CHECKING
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
        }
    }

    private String getRelativeDirection(float facingX, float facingZ) {
        Vector2f facing = new Vector2f(facingX, facingZ).normalize();
        Vec3 camPos = Util.getCameraPos();
        Vector2f relativeDirection = new Vector2f((float) (x - camPos.x()), (float) (z - camPos.z())).normalize();
        float a = (float) Math.atan2(relativeDirection.y, relativeDirection.x);
        float b = (float) Math.atan2(facing.y, facing.x);
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
        } else if (this.age % CONFIG.reactionSpeed == 0) {
            Vec3 birdPos = new Vec3(this.x, this.y, this.z);
            if (entitiesNearby(birdPos)) {
                setBehaviour(Behaviour.FLYING);
            }
        }
    }
    private boolean entitiesNearby(Vec3 pos) {
        final List<Entity> nearbyEntities = level.getEntitiesOfClass(Entity.class, AABB.ofSize(pos,
                CONFIG.reactionDistance, CONFIG.reactionDistance, CONFIG.reactionDistance));
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
        if (Util.getCameraPos().distanceToSqr(x, y, z) > Mth.square(CONFIG.despawnDistance)) {
            this.remove();
        }
        behaviourTime++;
        switch (behaviour) {
            case FLYING -> {
                double avgVelX = 0; double avgVelY = 0; double avgVelZ = 0; // alignment - match the velocity of other boids
                double avgPosX = 0; double avgPosY = 0; double avgPosZ = 0; // cohesion - steer towards center mass of other boids
                double closeXd = 0; double closeYd = 0; double closeZd = 0; // separation - avoid running into other boids

                int neighbors = 0;
                for (BirdParticle other : birds) {
                    if (other == this) continue;
                    final double distance = Vector3d.distance(x, y, z, other.x, other.y, other.z);
                    if (distance < CONFIG.flockRange) {
                        avgVelX += other.xd; avgVelY += other.yd; avgVelZ += other.zd;
                        avgPosX += other.x;  avgPosY += other.y;  avgPosZ += other.z;
                        neighbors += 1;
                        if (distance < CONFIG.separationDistance) {
                            closeXd += this.x - other.x;
                            closeYd += this.y - other.y;
                            closeZd += this.z - other.z;
                        }
                    }
                }
                if (neighbors > 0) {
                    avgVelX = avgVelX / neighbors; avgVelY = avgVelY / neighbors; avgVelZ = avgVelZ / neighbors;
                    avgPosX = avgPosX / neighbors; avgPosY = avgPosY / neighbors; avgPosZ = avgPosZ / neighbors;
                    this.xd += ((avgVelX - this.xd) * CONFIG.alignment)
                            +  ((avgPosX - this.x)  * CONFIG.cohesion)
                            +  (closeXd             * CONFIG.separation)
                            + random.triangle(0, CONFIG.flightRandomness);
                    this.yd += ((avgVelY - this.yd) * CONFIG.alignment)
                            +  ((avgPosY - this.y)  * CONFIG.cohesion)
                            +  (closeYd             * CONFIG.separation)
                            + random.triangle(0, CONFIG.flightRandomness);
                    this.zd += ((avgVelZ - this.zd) * CONFIG.alignment)
                            +  ((avgPosZ - this.z)  * CONFIG.cohesion)
                            +  (closeZd             * CONFIG.separation)
                            + random.triangle(0, CONFIG.flightRandomness);
                }

                // constrain height
                if (y - level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z
                ) > CONFIG.flightHeightLimit) this.yd -= CONFIG.flightHeightLimitFactor;

                // avoid colliding with blocks
                int avoidDistance = CONFIG.blockAvoidanceDistance;
                Vec3 lineToCast = new Vec3(xd, yd, zd).normalize().multiply(avoidDistance, avoidDistance, avoidDistance);
                Vec3 start = new Vec3(x, y, z);
                Vec3 end = new Vec3(x, y, z).add(lineToCast);
                final BlockHitResult hitResult = level.clip(Util.getClipContext(start, end));
                if (hitResult.getType() != HitResult.Type.MISS) {

                    float factor = CONFIG.blockAvoidanceFactor;
                    if (hitResult.getLocation().closerThan(start, 1)) factor = 0.5F;
                    // meh, good enough for now
                    xd -= Math.signum(xd) * factor;
                    yd -= Math.signum(yd) * factor;
                    zd -= Math.signum(zd) * factor;
                }

                // constrain speed
                double speed = Vector3d.length(xd, yd, zd);
                if (speed > CONFIG.maxSpeed) {
                    this.xd = (this.xd / speed) * CONFIG.maxSpeed;
                    this.yd = (this.yd / speed) * CONFIG.maxSpeed;
                    this.zd = (this.zd / speed) * CONFIG.maxSpeed;
                } else if (speed < CONFIG.minSpeed) {
                    this.xd = (this.xd / speed) * CONFIG.minSpeed;
                    this.yd = (this.yd / speed) * CONFIG.minSpeed;
                    this.zd = (this.zd / speed) * CONFIG.minSpeed;
                }

                /*if (behaviourTime > config.bird.birdFlightTime) {
                    setBehaviour(Behaviour.LANDING);
                }*/

                this.setSprite(Util.getSprite("crow_flying_%s_%d".formatted(getRelativeDirection((float)xd, (float)zd), (age / 2) % 2)));
            }
            case LANDING -> {
                this.setSprite(Util.getSprite("crow_flying_%s_1".formatted(getRelativeDirection((float)xd, (float)zd))));
                Vec3 targetOffset = new Vec3(x - target.x, y - target.y, z - target.z);
                final float responsiveness = (float) Math.min(CONFIG.landingResponsiveness, targetOffset.length());

                //TODO rotate velocity direction directly towards target when close to it
                if (this.x > target.x) this.xd -= responsiveness;
                if (this.x < target.x) this.xd += responsiveness;
                if (this.y > target.y) this.yd -= responsiveness;
                if (this.y < target.y) this.yd += responsiveness;
                if (this.z > target.z) this.zd -= responsiveness;
                if (this.z < target.z) this.zd += responsiveness;
                final BlockPos pos = BlockPos.containing(x, y - 0.7, z);
                final BlockState state = level.getBlockState(pos);
                if (!state.getCollisionShape(level, pos).isEmpty()) {
                    setBehaviour(Behaviour.PERCHED);
                }
                //Gizmos.point(new Vec3(target), 0xFFFF0000, 10F);
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
        }
    }

    public static class Provider extends CritterProvider {
        public Provider(SpriteSet spriteSet) {super(spriteSet);}
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new BirdParticle(level, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}