package pigcart.cosycritters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static CosyCrittersConfig config;

    public static void loadConfig() {
        File file = new File(CosyCrittersConfig.CONFIG_PATH);
        if (!file.exists()) {
            config = new CosyCrittersConfig();
            saveConfig();
        } else {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, CosyCrittersConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
                config = new CosyCrittersConfig(); // Fallback
            }
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CosyCrittersConfig.CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CosyCrittersConfig getConfig() {
        return config;
    }
}