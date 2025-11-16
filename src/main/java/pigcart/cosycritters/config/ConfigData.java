package pigcart.cosycritters.config;

import pigcart.cosycritters.config.ConfigManager.resetParticles;

import static pigcart.cosycritters.config.Annotations.*;

public class ConfigData {
    @NoGUI public byte configVersion = 1;

    @OnChange(resetParticles.class) public int maxBirds = 10;
    @OnChange(resetParticles.class) public int maxMoths = 8;
    @OnChange(resetParticles.class) public int maxSpiders = 16;

    public boolean spawnHatman = true;
    @OnChange(resetParticles.class) public boolean spawnBird = true;
    @OnChange(resetParticles.class) public boolean spawnMoth = true;
    @OnChange(resetParticles.class) public boolean spawnSpider = true;

    @Label(key = "cosycritters.birdOptions")
    public int birdReactionDistance = 10;
    public int birdReactionSpeed = 20;
    public int birdDespawnDistance = 64;
    public int birdAvoidanceDistance = 2;
    public float birdAvoidanceFactor = 0.05F;
    public int birdFlockRange = 20;
    public float birdVelocityMatchFactor = 0.05F;
    public float birdCenteringFactor = 0.0005F;
    public float birdMaxSpeed = 1;
    public float birdMinSpeed = 0.5F;
    public int birdFlightTime = 100;
    public float birdLandingResponsiveness = 0.01F;
    public int birdFlightHeightLimit = 30;
}

