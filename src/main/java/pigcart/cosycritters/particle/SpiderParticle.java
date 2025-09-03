package pigcart.cosycritters.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import pigcart.cosycritters.Util;
import pigcart.cosycritters.config.ConfigManager;

import java.util.Optional;

public class SpiderParticle extends CustomRenderParticle {

    boolean clockwise;
    BlockPos blockPos;
    Direction direction;
    float speed;
    Vec3 oldPosition;

    private SpiderParticle(ClientLevel level, double x, double y, double z, int direction3DDataValue) {
        super(level, x, y, z);
        this.quadSize = (random.nextFloat() * 0.1f) + 0.05f;
        this.speed = quadSize / 2;
        this.roll = Mth.TWO_PI * random.nextFloat();
        this.lifetime = 500 + random.nextInt(50);
        this.clockwise = random.nextBoolean();
        this.blockPos = BlockPos.containing(x, y, z);
        this.direction = Direction.from3DDataValue(direction3DDataValue);
        this.oldPosition = new Vec3(x, y, z);
        this.hasPhysics = false;
        // game crashes if a sprite isnt set in constructor
        this.setSprite(Util.getSprite("spider_crawling_" + age % 2));
    }

    @Override
    public Optional<ParticleGroup> getParticleGroup() {
        return Optional.of(ConfigManager.spiderGroup);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSprite(Util.getSprite("spider_crawling_" + age % 2));
        if (!Minecraft.getInstance().cameraEntity.position().closerThan(new Vec3(x, y, z), 32)) {
            this.remove();
        }
        Vec3 from = new Vec3(x, y, z);
        if (age % 20 == 0) {
            if (oldPosition.closerThan(from, 0.05)) {
                this.remove();
            } else {
                oldPosition = from;
            }
        }

        oRoll = roll;
        roll += (clockwise ? speed : -speed);
        
        if (!blockPos.equals(BlockPos.containing(x, y, z))) {
            if (!level.getFluidState(BlockPos.containing(from)).isEmpty()) this.remove();
            blockPos = BlockPos.containing(x, y, z);
            clockwise = random.nextBoolean();
        }
        // feel forwards for a block face to crawl onto
        Vec3 to = from.add(new Vec3(xd, yd, zd).normalize().multiply(quadSize / 2, quadSize / 2, quadSize / 2));
        BlockHitResult hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, Util.getCollisionContext()));
        if (hitResult.getType().equals(HitResult.Type.BLOCK)) {
            // reorient
            Direction oldDirection = direction;
            direction = hitResult.getDirection().getOpposite();

            if (oldDirection.getAxis() == Direction.Axis.Y) {
                // from ceiling or floor to a wall
                if (direction.equals(Direction.SOUTH)) {
                    roll = Mth.HALF_PI * 3;
                } else if (direction.equals(Direction.EAST)) {
                    roll = Mth.HALF_PI * 2;
                } else if (direction.equals(Direction.NORTH)) {
                    roll = Mth.HALF_PI * 4;
                } else if (direction.equals(Direction.WEST)) {
                    roll = Mth.HALF_PI;
                }
                if (oldDirection.equals(Direction.UP)) roll += Mth.PI;
            } else if (oldDirection.getAxis() == Direction.Axis.X) {
                // from as east or west wall
                if (direction.equals(Direction.SOUTH)) {
                    roll = Mth.HALF_PI * 2;
                } else if (direction.equals(Direction.UP)) {
                    roll = Mth.HALF_PI * 3;
                } else if (direction.equals(Direction.NORTH)) {
                    roll = Mth.HALF_PI;
                } else if (direction.equals(Direction.DOWN)) {
                    roll = Mth.HALF_PI * 4;
                }
                if (oldDirection.equals(Direction.WEST)) roll += Mth.PI;
            } else if (oldDirection.getAxis() == Direction.Axis.Z) {
                // from a north or south wall
                // i dont understand why the numbers are like this TwT
                if (direction.equals(Direction.EAST)) {
                    roll = Mth.HALF_PI;
                } else if (direction.equals(Direction.UP)) {
                    roll = Mth.HALF_PI * 2;
                } else if (direction.equals(Direction.WEST)) {
                    roll = Mth.HALF_PI * 2;
                } else if (direction.equals(Direction.DOWN)) {
                    roll = Mth.HALF_PI;
                }
                if (oldDirection.equals(Direction.SOUTH)) roll += Mth.PI;
            }
        } else {
            // feel down, are we floating?
            to = from.add(new Vec3(direction.step()).multiply(0.2, 0.2, 0.2));
            hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, Util.getCollisionContext()));
            if (hitResult.getType().equals(HitResult.Type.MISS)) {
                // feel down + backwards for a ledge that we've just crawled off
                to = from.add(new Vec3(direction.step()).multiply(0.5, 0.5, 0.5))
                        .add(new Vec3(-xd, -yd, -zd).normalize().multiply(0.5, 0.5, 0.5));
                hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, Util.getCollisionContext()));
                if (hitResult.getType().equals(HitResult.Type.BLOCK)) {
                    if (direction != hitResult.getDirection().getOpposite()) {
                        // reorient
                        Direction oldDirection = direction;
                        direction = hitResult.getDirection().getOpposite();
                        if (oldDirection.getAxis() == Direction.Axis.Y) {
                            // from ceiling or floor to a wall
                            if (direction.equals(Direction.SOUTH)) {
                                roll = Mth.HALF_PI * 3;
                            } else if (direction.equals(Direction.EAST)) {
                                roll = Mth.HALF_PI * 2;
                            } else if (direction.equals(Direction.NORTH)) {
                                roll = Mth.HALF_PI * 4;
                            } else if (direction.equals(Direction.WEST)) {
                                roll = Mth.HALF_PI;
                            }
                            if (oldDirection.equals(Direction.DOWN)) roll += Mth.PI;
                        } else if (oldDirection.getAxis() == Direction.Axis.X) {
                            // from as east or west wall
                            if (direction.equals(Direction.SOUTH)) {
                                roll = Mth.HALF_PI * 2;
                            } else if (direction.equals(Direction.UP)) {
                                roll = Mth.HALF_PI * 3;
                            } else if (direction.equals(Direction.NORTH)) {
                                roll = Mth.HALF_PI;
                            } else if (direction.equals(Direction.DOWN)) {
                                roll = Mth.HALF_PI * 4;
                            }
                            if (oldDirection.equals(Direction.EAST)) roll += Mth.PI;
                        } else if (oldDirection.getAxis() == Direction.Axis.Z) {
                            // from a north or south wall
                            // i dont understand why the numbers are like this TwT
                            if (direction.equals(Direction.EAST)) {
                                roll = Mth.HALF_PI;
                            } else if (direction.equals(Direction.UP)) {
                                roll = Mth.HALF_PI * 2;
                            } else if (direction.equals(Direction.WEST)) {
                                roll = Mth.HALF_PI * 2;
                            } else if (direction.equals(Direction.DOWN)) {
                                roll = Mth.HALF_PI;
                            }
                            if (oldDirection.equals(Direction.NORTH)) roll += Mth.PI;
                        }
                    }
                } else {
                    // bail out if we can't find the ledge. floating spider? what floating spider?
                    this.remove();
                }
            }
        }
        switch (this.direction) {
            case DOWN -> {
                this.xd = Mth.cos(roll) * -speed;
                this.zd = Mth.sin(roll) * speed;
                this.yd = 0;
            }
            case UP -> {
                this.xd = Mth.sin(roll) * speed;
                this.zd = Mth.cos(roll) * -speed;
                this.yd = 0;
            }
            case NORTH -> {
                this.xd = Mth.sin(roll) * -speed;
                this.yd = Mth.cos(roll) * speed;
                this.zd = 0;
            }
            case SOUTH -> {
                this.xd = Mth.cos(roll) * speed;
                this.yd = Mth.sin(roll) * -speed;
                this.zd = 0;
            }
            case WEST -> {
                this.yd = Mth.sin(roll) * speed;
                this.zd = Mth.cos(roll) * -speed;
                this.xd = 0;
            }
            case EAST -> {
                this.yd = Mth.cos(roll) * -speed;
                this.zd = Mth.sin(roll) * speed;
                this.xd = 0;
            }
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickPercent) {
        float x = (float)(Mth.lerp(tickPercent, this.xo, this.x) - camera.getPosition().x());
        float y = (float)(Mth.lerp(tickPercent, this.yo, this.y) - camera.getPosition().y());
        float z = (float)(Mth.lerp(tickPercent, this.zo, this.z) - camera.getPosition().z());

        Quaternionf quaternionf = switch (direction) {
            case DOWN  -> new Quaternionf(new AxisAngle4f(Mth.HALF_PI,-1, 0, 0));
            case UP    -> new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 1, 0, 0)).rotateZ(Mth.HALF_PI);
            case WEST  -> new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 1, 0)).rotateZ(Mth.PI);
            case EAST  -> new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0,-1, 0)).rotateZ(Mth.HALF_PI);
            case NORTH -> new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 0,-1));
            case SOUTH -> new Quaternionf(new AxisAngle4f(Mth.PI,      0, 1, 0));
            // up/down & east/west are just the inverse of each other why is south like this
            // i feel like theres something obvious that im completely missing
        };
        quaternionf.rotateZ(Mth.lerp(tickPercent, this.oRoll, this.roll));
        this.renderRotatedQuad(vertexConsumer, quaternionf, x, y, z, tickPercent);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        public Provider(SpriteSet sprites) {
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double direction3DDataValue, double ySpeedUnused, double zSpeedUnused) {
            return new SpiderParticle(level, x, y, z, (int)direction3DDataValue);
        }
    }
}