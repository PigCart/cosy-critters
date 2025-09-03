package pigcart.cosycritters.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleGroup;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.mixin.access.ParticleEngineAccessor;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Supplier;

public class ConfigManager {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String CONFIG_PATH = "config/" + CosyCritters.MOD_ID + ".json";
    public static ModConfig config;
    public static ModConfig defaultConfig = new ModConfig();

    public static ParticleGroup mothGroup;
    public static ParticleGroup birdGroup;
    public static ParticleGroup spiderGroup;

    public static void loadConfig() {
        File file = new File(CONFIG_PATH);
        try (FileReader reader = new FileReader(file)) {
            config = GSON.fromJson(reader, ModConfig.class);
        } catch (IOException e) {
            CosyCritters.LOGGER.error(e.getMessage());
        }
        if (config == null || config.configVersion < defaultConfig.configVersion) {
            config = new ModConfig();
            saveConfig();
        }
        mothGroup = new ParticleGroup(config.maxMoths);
        birdGroup = new ParticleGroup(config.maxBirds);
        spiderGroup = new ParticleGroup(config.maxSpiders);
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            CosyCritters.LOGGER.error(e.getMessage());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Percentage {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface OverrideName { String newName(); }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Label {String key();}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface BooleanFormat {String t(); String f();}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Group {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface NoGUI {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface OnChange {Class<? extends Runnable> runnable();}

    public static class resetParticles implements Runnable {
        @Override
        public void run() {
            ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).callClearParticles();
            mothGroup = new ParticleGroup(config.maxMoths);
            birdGroup = new ParticleGroup(config.maxBirds);
            spiderGroup = new ParticleGroup(config.maxSpiders);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface EditAsString {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Dropdown {Class<? extends Supplier<List<String>>> supplier();}
}