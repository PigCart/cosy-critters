package pigcart.cosycritters.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class SpiderParticle extends TextureSheetParticle {

    boolean clockwise;
    BlockPos blockPos;
    Direction direction;
    float speed;
    Vec3 oldPosition;

    private SpiderParticle(ClientLevel level, double x, double y, double z, int direction3DDataValue, SpriteSet provider) {
        super(level, x, y, z);
        this.sprite = provider.get(random);
        this.quadSize = (random.nextFloat() * 0.1f) + 0.05f;
        this.speed = quadSize / 2;
        this.roll = Mth.TWO_PI * random.nextFloat();
        this.lifetime = 500 + random.nextInt(50);
        this.clockwise = random.nextBoolean();
        this.blockPos = BlockPos.containing(x, y, z);
        this.direction = Direction.from3DDataValue(direction3DDataValue);
        this.oldPosition = new Vec3(x, y, z);
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
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
            blockPos = BlockPos.containing(x, y, z);
            clockwise = random.nextBoolean();
        }
        // feel forwards for a block face to crawl onto
        Vec3 to = from.add(new Vec3(xd, yd, zd).normalize().multiply(quadSize / 2, quadSize / 2, quadSize / 2));
        BlockHitResult hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
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
            hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hitResult.getType().equals(HitResult.Type.MISS)) {
                // feel down + backwards for a ledge that we've just crawled off
                to = from.add(new Vec3(direction.step()).multiply(0.5, 0.5, 0.5))
                        .add(new Vec3(-xd, -yd, -zd).normalize().multiply(0.5, 0.5, 0.5));
                hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
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
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Quaternionf quaternionf = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, -1, 0, 0));
        //direction.getRotation();
        // well i can't figure out whats going on with the coordinates so fuck it switch time
        switch (direction) {
            case DOWN -> {
                quaternionf = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, -1, 0, 0));
            }
            case UP -> {
                quaternionf = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 1, 0, 0));
                quaternionf.rotateZ(Mth.HALF_PI);
            }
            case NORTH -> {
                quaternionf = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 0, -1));
            }
            case SOUTH -> {
                quaternionf = new Quaternionf(new AxisAngle4f(Mth.PI, 0, 1, 0));
                // up/down & east/west are just the inverse of each other why is south like this
                // i feel like theres something obvious that im completely missing
            }
            case WEST -> {
                quaternionf = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 1, 0));
                quaternionf.rotateZ(Mth.PI);
            }
            case EAST -> {
                quaternionf = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, -1, 0));
                quaternionf.rotateZ(Mth.HALF_PI);
            }
        }
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll) + Mth.HALF_PI);
        }
        this.renderRotatedQuad(buffer, camera, quaternionf, partialTick);
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
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double direction3DDataValue, double velocityYUnused, double velocityZUnused) {
            return new SpiderParticle(level, x, y, z, (int)direction3DDataValue, this.provider);
        }
    }
}