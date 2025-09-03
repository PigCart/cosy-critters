package pigcart.cosycritters;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

//? if >=1.21.9 {
/*import net.minecraft.data.AtlasIds;
*///?}

import pigcart.cosycritters.mixin.access.ParticleEngineAccessor;

public class Util {

    @SuppressWarnings("removal")
    public static ResourceLocation getId(String path) {
        //? if <=1.20.1 {
        return new ResourceLocation(CosyCritters.MOD_ID, path);
        //?} else {
        /*return ResourceLocation.fromNamespaceAndPath(CosyCritters.MOD_ID, path);
        *///?}
    }

    public static ClipContext getClipContext(Vec3 clipStart, Vec3 clipEnd) {
        return new ClipContext(clipStart, clipEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, getCollisionContext());
    }

    //? if <=1.20.1 {
    public static Entity getCollisionContext() {
        return Minecraft.getInstance().cameraEntity;
    }
    //?} else {
    /*public static CollisionContext getCollisionContext() {
        return CollisionContext.empty();
    }
    *///?}

    public static TextureAtlasSprite getSprite(String path) {
        //? if >= 1.21.9 {
        /*return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES).getSprite(getId(path));
        *///?} else {
        return ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).getTextureAtlas().getSprite(getId(path));
        //?}
    }

    public static void schedule(Runnable task) {
        //? if >=1.21.4 {
        /*Minecraft.getInstance().schedule(task);
        *///?} else {
        Minecraft.getInstance().tell(task);
        //?}
    }

    public static boolean isDay(Level level) {
        // level.isDay always returns true in 1.21.0
        return level.dayTime() % 24000 < 13000;
    }

    public static void addChatMsg(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
    }

    public static boolean isExposed(Level level, int x, int y, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) <= y;
    }

    public static boolean hasSpace(ParticleGroup group) {
        return ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).callHasSpaceInParticleLimit(group);
    }

    public static int getCount(ParticleGroup group) {
        return ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).getTrackedParticleCounts().getInt(group);
    }

    public static void debug(String string) {
        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(string), true);
    }
}
