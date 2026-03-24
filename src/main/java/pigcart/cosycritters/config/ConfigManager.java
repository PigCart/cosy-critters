package pigcart.cosycritters.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pigcart.cosycritters.CosyCritters;

import java.io.*;

public class ConfigManager {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String CONFIG_PATH = "config/" + CosyCritters.MOD_ID + ".json";
    public static ConfigData config;

    public static ConfigData getDefaultConfig() {
        return new ConfigData();
    }

    public static void load() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, ConfigData.class);
            } catch (Exception e) {
                CosyCritters.LOGGER.error("Error loading config", e);
                config = getDefaultConfig();
                save();
            }
        } else {
            CosyCritters.LOGGER.info("Creating config file at " + CONFIG_PATH);
            config = getDefaultConfig();
            save();
        }
        if (config == null || config.configVersion < getDefaultConfig().configVersion) {
            CosyCritters.LOGGER.info("Overwriting old config file");
            config = new ConfigData();
            save();
        }
        config.bird.biomes.populateInternalLists();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            CosyCritters.LOGGER.error(e.getMessage());
        }
        config.bird.biomes.populateInternalLists();
    }
}