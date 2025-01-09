package pigcart.cosycritters.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.cosycritters.Cosycritters;

public class MothParticle extends TextureSheetParticle {

    private MothParticle(ClientLevel level, double x, double y, double z, SpriteSet provider) {
        super(level, x, y, z);
        this.sprite = provider.get(level.random);
        this.quadSize = 0.1f;
        this.lifetime = 6000;
        Cosycritters.birdCount++;
    }

    @Override
    public void remove() {
        Cosycritters.birdCount--;
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();

    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        float x = (float)(Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float y = (float)(Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float z = (float)(Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        Vector3f cameraOffset = new Vector3f(x, y, z);

        float lerpedAge = Mth.lerp(partialTick, age - 1, age);
        float wingsPos = Mth.sin(lerpedAge * 2) * 0.7f;

        Quaternionf leftWing = new Quaternionf(new AxisAngle4d((wingsPos * Mth.HALF_PI) - Mth.HALF_PI, 1, 0, 0));
        Quaternionf rightWing = new Quaternionf(new AxisAngle4d((wingsPos * Mth.HALF_PI) - Mth.HALF_PI, -1, 0, 0));
        Vector3f leftWingOffset = new Vector3f(cameraOffset).add(0, -quadSize * wingsPos, quadSize - (quadSize * Mth.abs(wingsPos)));
        Vector3f rightWingOffset = new Vector3f(cameraOffset).add(0, -quadSize * wingsPos, (quadSize * Mth.abs(wingsPos) - quadSize));
        flipItTurnwaysIfBackfaced(leftWing, leftWingOffset);
        flipItTurnwaysIfBackfaced(rightWing, rightWingOffset);
        this.renderRotatedQuad(buffer, leftWing, leftWingOffset.x, leftWingOffset.y, leftWingOffset.z, partialTick);
        this.renderRotatedQuad(buffer, rightWing, rightWingOffset.x, rightWingOffset.y, rightWingOffset.z, partialTick);
    }

    protected void flipItTurnwaysIfBackfaced(Quaternionf quaternion, Vector3f toCamera) {
        Vector3f normal = new Vector3f(0, 0, 1);
        normal.rotate(quaternion).normalize();
        float dot = normal.dot(toCamera);
        if (dot > 0) {
            quaternion.rotateY(Mth.PI);
        }
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
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double veloctiyX, double veloctiyY, double veloctiyZ) {
            return new MothParticle(level, x, y, z, this.provider);
        }
    }
}