package pigcart.cosycritters;

//? if >=1.21.9 {
/*import net.minecraft.client.gui.components.debug.DebugScreenEntries;
*///?}
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigcart.cosycritters.config.ConfigManager;
import pigcart.cosycritters.particle.BirdParticle;

import java.util.List;
import java.util.Optional;

import static pigcart.cosycritters.Util.*;
import static pigcart.cosycritters.config.ConfigManager.*;

public class CosyCritters {
    //TODO
    // ants (spiders that walk in a line)
    // flies (attracted to the scene of a death)
    // fireflies {ehhh maybe}
    // a few more common bird types (pigeons, robins)
    // butterflies (moths without a lamp)
    // silverfish swarm (boids, renders in place of silverfish)
    // bee swarm (boids, renders in place of bee)
    // fish maybe?
    // rats/mice
    // squirrel? (vertical tree rat)
    // game of life

    public static final String MOD_ID = "cosycritters";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SimpleParticleType BIRD;
    public static SimpleParticleType HAT_MAN;
    public static SimpleParticleType MOTH;
    public static SimpleParticleType SPIDER;

    public static int moths = 0;
    public static int spiders = 0;

    private static boolean wasSleeping = false;

    private static List<String> getDebugStrings() {
        return List.of(
                String.format("Birds: %d/%d", BirdParticle.birds.size(), config.maxBirds),
                String.format("Moths: %d/%d", moths, config.maxMoths),
                String.format("Spiders: %d/%d", spiders, config.maxSpiders)
        );
    }

    public static void onInitializeClient() {
        ConfigManager.load();

        //? if >= 1.21.9 {
        /*DebugScreenEntries.register(
                Util.getId("a_stats"),
                (display, level, levelChunk, levelChunk2) ->
                        display.addToGroup(Util.getId("particle_tracking"), getDebugStrings())
        );
        *///?}
    }

    @SuppressWarnings("unchecked")
    public static <S> LiteralArgumentBuilder<S> getCommands() {
        return (LiteralArgumentBuilder<S>) LiteralArgumentBuilder.literal(MOD_ID)
                .executes(ctx -> {
                    // schedule set screen so chat screen can close first
                    Util.schedule(() ->
                            Minecraft.getInstance().setScreen(screenPlease(null)));
                    return 0;
                })
                .then(LiteralArgumentBuilder.literal("debug")
                        .executes(ctx -> {
                            getDebugStrings().forEach(Util::addChatMsg);
                            return 0;
                        })
                );
    }

    public static void doAnimateTick(BlockPos blockPos, BlockState state) {
        trySpawnMoth(Minecraft.getInstance().level, blockPos);
    }

    public static void onTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            tickHatManSpawnConditions(minecraft);
            trySpawnBird(minecraft.level);
        }
    }

    private static void tickHatManSpawnConditions(Minecraft minecraft) {
        if (Util.isNewMoon(minecraft.level)) {
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
    private static void trySpawnHatman(Minecraft minecraft) {
        if (!config.spawnHatman) return;
        final Optional<BlockPos> sleepingPos = minecraft.player.getSleepingPos();
        if (sleepingPos.isPresent()) {
            BlockState state = minecraft.level.getBlockState(sleepingPos.get());
            Property<Direction> property = BlockStateProperties.HORIZONTAL_FACING;
            if (state.hasProperty(property)) {
                Direction direction = state.getValue(property);
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
    public static void trySpawnBird(Level level) {
        if (    config.spawnBird
                && Util.isDay(level)
                && BirdParticle.birds.size() < config.maxBirds
        ) {
            Vec3 player = Minecraft.getInstance().player.position();
            int x = level.random.nextIntBetweenInclusive((int) (player.x - config.bird.despawnDistance), (int) (player.x + config.bird.despawnDistance));
            int z = level.random.nextIntBetweenInclusive((int) (player.z - config.bird.despawnDistance), (int) (player.z + config.bird.despawnDistance));
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (    Vector3d.distance(x, y, z, player.x, player.y, player.z) > config.bird.reactionDistance
                    && level.getBiome(BlockPos.containing(x, y, z)).is(BiomeTags.IS_FOREST)
            ) {
                level.addParticle(BIRD, x, y, z, 0, 0.5, 0);
            }
        }
    }
    public static void trySpawnMoth(Level level, BlockPos blockPos) {
        if (    config.spawnMoth
                && moths < config.maxMoths
                && level.getBiome(blockPos).is(BiomeTags.IS_OVERWORLD)
                && !Util.isDay(level)
                && level.getBrightness(LightLayer.BLOCK, blockPos) > 13
                && isExposed(level, blockPos.getX(), blockPos.getY(), blockPos.getZ())
        ) {
            level.addParticle(MOTH, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0, 0);
        }
    }
    public static void trySpawnSpider(Level level, BlockPos blockPos) {
        if (    config.spawnSpider
                && spiders < config.maxSpiders
                && !Minecraft.getInstance().player.position().closerThan(blockPos.getCenter(), 2)
        ) {
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
}
