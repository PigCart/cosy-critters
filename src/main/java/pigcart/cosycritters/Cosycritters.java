package pigcart.cosycritters;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import pigcart.cosycritters.particle.BirdParticle;
import pigcart.cosycritters.particle.HatManParticle;
import pigcart.cosycritters.particle.MothParticle;
import pigcart.cosycritters.particle.SpiderParticle;

import java.util.ArrayList;
import java.util.Optional;

public class Cosycritters implements ClientModInitializer {
    //TODO: more robust mixins
    // ants (spiders that walk in a line)
    // flies (attracted to the scene of a death)
    // fireflies (swamps and plains, they glow)
    // fire flies (they glow, but aggressively)
    // fired flies (they are handing out resumes)
    // fryer flies (they are no longer handing out resumes)
    // bird flocking behaviour
    // bird angle-based sprite selection
    // a few more common bird types (pigeons, robins)
    // butterflies (moths without a lamp)
    // silverfish swarm (boids, renders in place of silverfish)
    // bee swarm (boids, renders in place of bee)
    // fish maybe?
    // rats/mice
    // jesus or something (when on low health and a totem is on your hotbar but youre not holding it)
    // herobrine (appears at edge of render distance by a wall, walking behind it when you look at him)
    // game of life
    //FIXME: torch/lantern mixins arent applying in 1.21.0 ???

    public static final String MOD_ID = "cosycritters";
    public static SimpleParticleType BIRD;
    public static SimpleParticleType HAT_MAN;
    public static SimpleParticleType MOTH;
    public static SimpleParticleType SPIDER;

    private static boolean wasSleeping = false;
    public static int birdCount = 0;
    public static int maxBirdCount = 50;
    public static int mothCount = 0;
    public static int maxMothCount = 10;
    public static ArrayList<MothParticle> moths = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        BIRD = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "bird"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(BIRD, BirdParticle.DefaultFactory::new);
        HAT_MAN = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "hat_man"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(HAT_MAN, HatManParticle.DefaultFactory::new);
        MOTH = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "moth"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(MOTH, MothParticle.DefaultFactory::new);
        SPIDER = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "spider"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(SPIDER, SpiderParticle.DefaultFactory::new);

        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
    }

    private void onJoin(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
        birdCount = 0;
        mothCount = 0;
    }

    private void onTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            tickHatManSpawnConditions(minecraft);
        }
    }

    private void tickHatManSpawnConditions(Minecraft minecraft) {
        if (minecraft.level.dimensionType().moonPhase(minecraft.level.dayTime()) == 4) {
            if (minecraft.player.isSleeping()) {
                if (!wasSleeping) {
                    trySpawnHatman(minecraft);
                    wasSleeping = true;
                }
            } else if (wasSleeping) {
                wasSleeping = false;
            }
        }
    }
    private void trySpawnHatman(Minecraft minecraft) {
        final Optional<BlockPos> sleepingPos = minecraft.player.getSleepingPos();
        if (sleepingPos.isPresent()) {
            BlockState state = minecraft.level.getBlockState(sleepingPos.get());
            Property property = BlockStateProperties.HORIZONTAL_FACING;
            if (state.hasProperty(property)) {
                Direction direction = (Direction) state.getValue(property);
                BlockPos blockPos = BlockPos.containing(minecraft.player.position()).relative(direction.getOpposite(), 2);
                Vec3 pos = blockPos.getCenter();
                RandomSource random = minecraft.player.getRandom();
                Vec3 randomPos = new Vec3(pos.x + random.nextInt(2) - 1, pos.y, pos.z + random.nextInt(2) - 1);
                if (minecraft.level.getBlockState(BlockPos.containing(randomPos)).isAir()) {
                    minecraft.particleEngine.createParticle(HAT_MAN, randomPos.x, randomPos.y + 0.5, randomPos.z, 0, 0, 0);
                }
            }
        }
    }
    public static void trySpawnBird(BlockState state, Level level, BlockPos blockPos) {
        if (level.isDay()
                && birdCount < maxBirdCount
                && level.getBlockState(blockPos.above()).isAir()
                && !Minecraft.getInstance().player.position().closerThan(blockPos.getCenter(), 10)
        ) {
            Vec3 pos = blockPos.getCenter();
            pos = state.getCollisionShape(level, blockPos).clip(pos.add(0, 2, 0), pos.add(0, -0.6, 0), blockPos).getLocation();
            Vec3 spawnFrom = pos.add(level.random.nextInt(10) - 5, level.random.nextInt(5), level.random.nextInt(10) - 5);
            if (level.clip(new ClipContext(spawnFrom, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getType().equals(HitResult.Type.MISS)) {
                level.addParticle(BIRD, spawnFrom.x, spawnFrom.y, spawnFrom.z, pos.x, pos.y, pos.z);
            }
        }
    }
    public static void trySpawnMoth(Level level, BlockPos blockPos) {
        if (level.isNight() && mothCount < maxMothCount && level.canSeeSky(blockPos)) {
            System.out.println("hey!");
            level.addParticle(MOTH, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0, 0);
        }
    }
    public static void trySpawnSpider(Level level, BlockPos blockPos) {
        if (Minecraft.getInstance().player.position().closerThan(blockPos.getCenter(), 2)) return;
        Direction direction = Direction.getRandom(level.random);
        blockPos = blockPos.relative(direction);
        BlockState state = level.getBlockState(blockPos);
        if (state.isFaceSturdy(level, blockPos, direction.getOpposite())) {
            final Vec3 spawnPos = blockPos.getCenter().add(new Vec3(direction.step()).multiply(-0.6f, -0.6f, -0.6f));
            level.addParticle(SPIDER, spawnPos.x, spawnPos.y, spawnPos.z, direction.get3DDataValue(), 0, 0);
        }
    }
}
