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
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pigcart.cosycritters.CosyCritters;

public class MothParticle extends TextureSheetParticle {

    private final Vec3 targetLamp;

    private MothParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.sprite = spriteSet.get(random);
        this.quadSize = 0.1f;
        this.lifetime = 500;
        this.targetLamp = BlockPos.containing(x, y, z).getCenter();
        this.xd = 0.5f;
        CosyCritters.mothCount++;
    }

    @Override
    public void remove() {
        if (!this.removed) {
            CosyCritters.mothCount = Math.max(0, CosyCritters.mothCount - 1);
        }
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.x == this.xo && this.y == this.yo && this.z == this.zo) this.remove();
        // stay within a limit from the lamp
        float centeringFactor = 0.00005f;
        if (targetLamp.distanceTo(new Vec3(x, y, z)) > 1) {
            this.xd =+ (targetLamp.x - this.x) * centeringFactor;
            this.yd =+ (targetLamp.y - this.y) * centeringFactor;
            this.zd =+ (targetLamp.z - this.z) * centeringFactor;
        } else if (targetLamp.distanceTo(new Vec3(x, y, z)) < 0.5) {
            this.xd =- (targetLamp.x - this.x) * centeringFactor;
            this.yd =- (targetLamp.y - this.y) * centeringFactor;
            this.zd =- (targetLamp.z - this.z) * centeringFactor;
        }
        // constrain speed
        float speed = Mth.sqrt((float) (this.xd*this.xd + this.yd*this.yd + this.zd*this.zd));
        float maxSpeed = 0.1f;
        float minSpeed = 0.05f;
        if (speed > maxSpeed) {
            this.xd = (this.xd/speed)*maxSpeed;
            this.yd = (this.yd/speed)*maxSpeed;
            this.zd = (this.zd/speed)*maxSpeed;
        } else if (speed < minSpeed) {
            this.xd = (this.xd/speed)*minSpeed;
            this.yd = (this.yd/speed)*minSpeed;
            this.zd = (this.zd/speed)*minSpeed;
        }
        // add randomness
        this.xd = this.xd * ((random.nextFloat() + 0.5));
        this.yd = this.yd * ((random.nextFloat() + 0.5));
        this.zd = this.zd * ((random.nextFloat() + 0.5));
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        float x = (float)(Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float y = (float)(Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float z = (float)(Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        Vector3f cameraOffset = new Vector3f(x, y, z);

        float lerpedAge = Mth.lerp(partialTick, age - 1, age);
        float wingsPos = Mth.sin(lerpedAge * 1.5f) * 0.7f;

        Quaternionf leftWing = new Quaternionf(new AxisAngle4d((wingsPos * Mth.HALF_PI) - Mth.HALF_PI, 1, 0, 0));
        Quaternionf rightWing = new Quaternionf(new AxisAngle4d((wingsPos * Mth.HALF_PI) - Mth.HALF_PI, -1, 0, 0));
        Vector3f leftWingOffset = new Vector3f(0, -quadSize * wingsPos, quadSize - (quadSize * Mth.abs(wingsPos)));
        Vector3f rightWingOffset = new Vector3f(0, -quadSize * wingsPos, (quadSize * Mth.abs(wingsPos) - quadSize));

        Vector3f delta = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd);
        final float angle = (float) Math.acos(delta.normalize().y);
        Vector3f axis = new Vector3f(-delta.z(), 0, delta.x()).normalize();
        Quaternionf quaternion = new Quaternionf(new AxisAngle4f(-angle, axis));
        leftWing.mul(quaternion);
        rightWing.mul(quaternion);

        leftWingOffset.rotate(quaternion);
        rightWingOffset.rotate(quaternion);
        // AAAAAUUUUUUUUUUUUGH
        // ian hubert school of thought: "moths are pure chaos don't overthink it"

        leftWingOffset.add(cameraOffset);
        rightWingOffset.add(cameraOffset);
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
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel level, double x, double y, double z, double veloctiyX, double veloctiyY, double veloctiyZ) {
            return new MothParticle(level, x, y, z, this.spriteSet);
        }
    }
}