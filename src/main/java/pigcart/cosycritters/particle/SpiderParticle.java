package pigcart.cosycritters.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;

public class SpiderParticle extends TextureSheetParticle {

    boolean clockwise;
    BlockPos pos;

    private SpiderParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z);
        this.sprite = provider.get(this.random);
        this.quadSize = 0.1F;
        this.lifetime = 500;
        this.clockwise = this.random.nextBoolean();
        this.pos = BlockPos.containing(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (!pos.equals(BlockPos.containing(x, y, z))) {
            this.pos = BlockPos.containing(x, y, z);
            clockwise = random.nextBoolean();
        }
        this.oRoll = this.roll;

        this.roll += (clockwise ? 0.2f : -0.2f);
        this.xd = Mth.cos(roll) * -0.1;
        this.zd = Mth.sin(roll) * 0.1;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Quaternionf quaternionf = new Quaternionf(new AxisAngle4d(Mth.HALF_PI, -1, 0, 0));
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll) + Mth.HALF_PI);
        }

        this.renderRotatedQuad(buffer, camera, quaternionf, partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet provider;

        public DefaultFactory(SpriteSet provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new SpiderParticle(level, x, y, z, this.provider);
        }
    }
}