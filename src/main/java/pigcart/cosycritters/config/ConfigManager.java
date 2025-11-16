package pigcart.cosycritters.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.mixin.access.ParticleEngineAccessor;

//? if >= 1.21.9 {
/*import net.minecraft.core.particles.ParticleLimit;
*///?} else {
import net.minecraft.core.particles.ParticleGroup;
 //?}

import java.io.*;

public class ConfigManager {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String CONFIG_PATH = "config/" + CosyCritters.MOD_ID + ".json";
    public static ConfigData config;
    public static ConfigData defaultConfig = new ConfigData();

    public static void load() {
        File file = new File(CONFIG_PATH);
        try (FileReader reader = new FileReader(file)) {
            config = GSON.fromJson(reader, ConfigData.class);
        } catch (IOException e) {
            CosyCritters.LOGGER.error(e.getMessage());
        }
        if (config == null || config.configVersion < defaultConfig.configVersion) {
            config = new ConfigData();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            CosyCritters.LOGGER.error(e.getMessage());
        }
    }

    public static Screen screenPlease(Screen lastScreen) {
        return new ConfigScreen(lastScreen, config, Component.translatable("cosycritters.title"));
    }

    public static class resetParticles implements Runnable {
        @Override
        public void run() {
            ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).callClearParticles();
        }
    }
}