package pigcart.cosycritters;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigcart.cosycritters.config.ConfigManager;
import pigcart.cosycritters.config.ConfigScreens;

import java.util.Optional;

import static pigcart.cosycritters.Util.*;
import static pigcart.cosycritters.config.ConfigManager.*;

public class CosyCritters {
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
    // game of life

    public static final String MOD_ID = "cosycritters";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SimpleParticleType BIRD;
    public static SimpleParticleType HAT_MAN;
    public static SimpleParticleType MOTH;
    public static SimpleParticleType SPIDER;

    private static boolean wasSleeping = false;

    public static void onInitializeClient() {
        ConfigManager.loadConfig();
    }

    @SuppressWarnings("unchecked")
    public static <S> LiteralArgumentBuilder<S> getCommands() {
        return (LiteralArgumentBuilder<S>) LiteralArgumentBuilder.literal(MOD_ID)
                .executes(ctx -> {
                    // schedule set screen so chat screen can close first
                    Util.schedule(() ->
                            Minecraft.getInstance().setScreen(ConfigScreens.generateMainConfigScreen(null)));
                    return 0;
                })
                .then(LiteralArgumentBuilder.literal("status")
                        .executes(ctx -> {
                            addChatMsg(String.format("Birds: %d/%d", getCount(birdGroup), birdGroup.getLimit()));
                            addChatMsg(String.format("Moths: %d/%d", getCount(mothGroup), mothGroup.getLimit()));
                            addChatMsg(String.format("Spiders: %d/%d", getCount(spiderGroup), spiderGroup.getLimit()));
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
        }
    }

    private static void tickHatManSpawnConditions(Minecraft minecraft) {
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
    public static void trySpawnBird(BlockState state, Level level, BlockPos blockPos) {
        if (    config.spawnBird
                && Util.isDay(level)
                && hasSpace(birdGroup)
                && level.getBlockState(blockPos.above()).isAir()
                && !Minecraft.getInstance().player.position().closerThan(blockPos.getCenter(), config.birdReactionDistance)
        ) {
            Vec3 pos = blockPos.getCenter();
            final var hitResult = state.getCollisionShape(level, blockPos).clip(pos.add(0, 2, 0), pos.add(0, -0.6, 0), blockPos);
            if (hitResult == null) return;
            pos = hitResult.getLocation();
            Vec3 spawnFrom = pos.add(level.random.nextInt(10) - 5, level.random.nextInt(5), level.random.nextInt(10) - 5);
            if (isExposed(level, (int) spawnFrom.x, (int) spawnFrom.y, (int) spawnFrom.z)
                    && level.clip(Util.getClipContext(spawnFrom, pos)).getType().equals(HitResult.Type.MISS)) {
                level.addParticle(BIRD, spawnFrom.x, spawnFrom.y, spawnFrom.z, pos.x, pos.y, pos.z);
            }
        }
    }
    public static void trySpawnMoth(Level level, BlockPos blockPos) {
        if (    config.spawnMoth
                && hasSpace(mothGroup)
                && !Util.isDay(level)
                && level.getBrightness(LightLayer.BLOCK, blockPos) > 13
                && isExposed(level, blockPos.getX(), blockPos.getY(), blockPos.getZ())
        ) {
            level.addParticle(MOTH, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0, 0);
        }
    }
    public static void trySpawnSpider(Level level, BlockPos blockPos) {
        if (    config.spawnSpider
                && hasSpace(spiderGroup)
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
