package pigcart.cosycritters.particle;

//? if >=1.21.9 {
/*import net.minecraft.client.renderer.state.QuadParticleRenderState;
*///?} else {
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class ComplexCritterParticle extends CritterParticle {
    protected ComplexCritterParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
    }
    @Override
    //? if >= 1.21.9 {
    /*public void extract(QuadParticleRenderState renderInfo, Camera camera, float tickPercent) {
        renderCustom(renderInfo, camera, tickPercent);
    }
    *///?} else {
    public void render(VertexConsumer renderInfo, Camera camera, float tickPercent) {
        renderCustom(renderInfo, camera, tickPercent);
    }
     //?}

    public abstract <T> void renderCustom(T renderInfo, Camera camera, float tickPercent);

    protected <T> void renderCustomQuad(T renderInfo, Quaternionf quaternion, float x, float y, float z, float tickPercent) {
        //? if >= 1.21.9 {
        /*this.extractRotatedQuad((QuadParticleRenderState) renderInfo, quaternion, x, y, z, tickPercent);
        *///?} else {
        this.renderRotatedQuad((VertexConsumer) renderInfo, quaternion, x, y, z, tickPercent);
        //?}
    }
    //? if <=1.20.1 {
    protected void renderRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternionf, float x, float y, float z, float tickPercent) {
        quaternionf.rotateY(Mth.PI);
        float size = this.getQuadSize(tickPercent);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int lightColor = this.getLightColor(tickPercent);

        Vector3f[] vector3fs = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)};

        for(int k = 0; k < 4; ++k) {
            Vector3f vector3f = vector3fs[k];
            vector3f.rotate(quaternionf);
            vector3f.mul(size);
            vector3f.add(x, y, z);
        }

        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();

    }
    //?}
}
