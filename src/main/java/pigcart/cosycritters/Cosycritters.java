package pigcart.cosycritters;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import pigcart.cosycritters.particle.BirdParticle;
import pigcart.cosycritters.particle.HatManParticle;

import java.util.Optional;

public class Cosycritters implements ClientModInitializer {

    public static final String MOD_ID = "cosycritters";
    public static SimpleParticleType BIRD;
    public static SimpleParticleType HAT_MAN;

    private static boolean wasSleeping = false;
    private static int hatManSpawnDelay = 0;
    public static int birdCount = 0;
    public static int maxBirdCount = 20;

    @Override
    public void onInitializeClient() {
        BIRD = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "bird"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(BIRD, BirdParticle.DefaultFactory::new);
        HAT_MAN = Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "hat_man"), FabricParticleTypes.simple(true));
        ParticleFactoryRegistry.getInstance().register(HAT_MAN, HatManParticle.DefaultFactory::new);

        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
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
                    spawnHatMan(minecraft);
                    wasSleeping = true;
                }
            } else if (wasSleeping) {
                wasSleeping = false;
            }
        }
    }
    private void spawnHatMan(Minecraft minecraft) {
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
    public static void spawnBird(BlockState state, Level level, BlockPos blockPos) {
        if (birdCount < maxBirdCount
                && level.getBlockState(blockPos.above()).isAir()
                && !Minecraft.getInstance().player.position().closerThan(blockPos.getCenter(), 10)
        ) {
            Vec3 pos = blockPos.getCenter();
            pos = state.getCollisionShape(level, blockPos).clip(pos.add(0, 2, 0), pos.add(0, -0.6, 0), blockPos).getLocation();
            level.addParticle(BIRD, pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }
}
