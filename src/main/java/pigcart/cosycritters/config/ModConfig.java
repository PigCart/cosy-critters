package pigcart.cosycritters.config;

import pigcart.cosycritters.config.ConfigManager.NoGUI;
import pigcart.cosycritters.config.ConfigManager.OnChange;
import pigcart.cosycritters.config.ConfigManager.resetParticles;

public class ModConfig {
    @NoGUI public byte configVersion = 0;

    @OnChange(runnable = resetParticles.class) public int maxBirds = 10;
    @OnChange(runnable = resetParticles.class) public int maxMoths = 10;
    @OnChange(runnable = resetParticles.class) public int maxSpiders = 10;

    public boolean spawnHatman = true;
    @OnChange(runnable = resetParticles.class) public boolean spawnBird = true;
    @OnChange(runnable = resetParticles.class) public boolean spawnMoth = true;
    @OnChange(runnable = resetParticles.class) public boolean spawnSpider = true;

    public int birdDisturbDistance = 10;
}

