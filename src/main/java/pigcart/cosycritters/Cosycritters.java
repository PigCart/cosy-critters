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

import java.util.Optional;

public class Cosycritters implements ClientModInitializer {

    public static final String MOD_ID = "cosycritters";
    public static SimpleParticleType BIRD;
    public static SimpleParticleType HAT_MAN;

    private static boolean wasSleeping = false;
    public static int birdCount = 0;
    public static int maxBirdCount = 50;

    @Override
    public void onInitializeClient() {
        BIRD = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "bird"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(BIRD, BirdParticle.DefaultFactory::new);
        HAT_MAN = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "hat_man"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(HAT_MAN, HatManParticle.DefaultFactory::new);

        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
    }

    private void onJoin(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
        birdCount = 0;
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
                && level.dayTime() % 1000 < 500
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
}
