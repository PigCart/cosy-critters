package pigcart.cosycritters.config;

import pigcart.cosycritters.config.ConfigManager.resetParticles;

import static pigcart.cosycritters.config.Annotations.*;

public class ConfigData {
    @NoGUI public byte configVersion = 1;

    @OnChange(resetParticles.class) public int maxBirds = 50;
    @OnChange(resetParticles.class) public int maxMoths = 8;
    @OnChange(resetParticles.class) public int maxSpiders = 16;

    public boolean spawnHatman = true;
    @OnChange(resetParticles.class) public boolean spawnBird = true;
    @OnChange(resetParticles.class) public boolean spawnMoth = true;
    @OnChange(resetParticles.class) public boolean spawnSpider = true;

    public BirdOptions bird = new BirdOptions();
    public static class BirdOptions {
        public int reactionDistance = 10;
        public int reactionSpeed = 20;
        public int despawnDistance = 128;
        public int blockAvoidanceDistance = 8;
        public float blockAvoidanceFactor = 0.1F;
        public int separationDistance = 2;
        public float separation = 0.01F;
        public int flockRange = 20;
        public float alignment = 0.05F;
        public float cohesion = 0.001F;
        public float maxSpeed = 1;
        public float minSpeed = 0.3F;
        public int maxBehaviourTime = 400;
        public float landingResponsiveness = 0.01F;
        public int flightHeightLimit = 20;
        public float flightHeightLimitFactor = 0.005F;
        public float flightRandomness = 0.03F;
    }
}

