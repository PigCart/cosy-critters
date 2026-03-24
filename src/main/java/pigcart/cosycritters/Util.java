package pigcart.cosycritters;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.resources.ResourceLocation;

import pigcart.cosycritters.mixin.access.ParticleEngineAccessor;

import java.net.URI;

// make forge shut up when im trying to keep an eye out for actual errors
@SuppressWarnings("removal")
public class Util {


    public static /*? >=1.21.11 {*//*ResourceLocation*//*?} else {*/ResourceLocation/*?}*/ getId(String path) {
        //? if <=1.20.1 {
        return new ResourceLocation(CosyCritters.MOD_ID, path);
         //?} else {
        /*return /^? >=1.21.11 {^//^ResourceLocation^//^?} else {^/ResourceLocation/^?}^/.fromNamespaceAndPath(CosyCritters.MOD_ID, path);
        *///?}
    }

    public static ResourceLocation parseId(String string) {
        try {
            //? if <=1.20.1 {
            return ResourceLocation.tryParse(string);
             //?} else {
            /*return ResourceLocation.parse(string);
            *///?}
        } catch (ResourceLocationException e) {
            return null;
        }
    }

    public static <T extends ParticleOptions> void spawnParticle(T particleType, String providerId, ClientLevel level, double x, double y, double z) {
        //? forge {
        /*Minecraft.getInstance().particleEngine.add(makeParticle(particleType, providerId, level, x, y, z));
        *///?} else {
        level.addParticle(particleType, x, y, z, 0, 0, 0);
        //?}
    }

    //? forge {
    /*/// reimplementation of ParticleEngine.makeParticle to bypass a multiplayer bug in forge's registry in 1.20.1
    public static <T extends ParticleOptions> Particle makeParticle(T particleType, String providerId, ClientLevel level, double x, double y, double z) {
        final ParticleEngineAccessor particleEngine = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
        final ParticleProvider<T> provider = (ParticleProvider) particleEngine.getProviders().get(getId(providerId));
        return provider.createParticle(particleType, level, x, y, z, 0, 0, 0);
    }
    *///?}

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
        /*return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.PARTICLES).getSprite(getId(path));
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
        //? >=26.1 {
        /*return level.isBrightOutside();
        *///?} >=1.21.11 {
        /*return level.getDayTime() % 24000 < 13000;
        *///?} else {
        // level.isDay always returns true in 1.21.0
        return level.dayTime() % 24000 < 13000;
        //?}
    }

    public static boolean isExposed(Level level, int x, int y, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) <= y;
    }

    public static Vec3 getCameraPos() {
        return getCameraPos(Minecraft.getInstance().gameRenderer.getMainCamera());
    }

    public static Vec3 getCameraPos(Camera camera) {
        //? if >= 1.21.9 {
        /*return camera.position();
        *///?} else {
        return camera.getPosition();
         //?}
    }

    public static boolean isNewMoon(ClientLevel level) {
        //? >=26.1 {
        /*return Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.skyRenderState.moonPhase.equals(net.minecraft.world.level.MoonPhase.NEW_MOON);
        *///?} >=1.21.11 {
        /*return Minecraft.getInstance().gameRenderer.getLevelRenderState().skyRenderState.moonPhase.equals(net.minecraft.world.level.MoonPhase.NEW_MOON);
        *///?} else {
        return level.dimensionType().moonPhase(level.dayTime()) == 4;
        //?}
    }

    public static void openUri(URI uri) {
        //? >=1.21.11 {
        /*net.minecraft.util.Util.getPlatform().openUri(uri);
        *///?} else {
        net.minecraft.Util.getPlatform().openUri(uri);
        //?}
    }

    public static void addChatMsg(String message) {
        //? >=26.1 {
        /*Minecraft.getInstance().gui.getChat().addClientSystemMessage(Component.literal(message));
         *///?} else {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
        //?}
    }
}
