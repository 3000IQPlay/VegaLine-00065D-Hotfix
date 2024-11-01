package ru.govno.client.utils.URender.other;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class NoiseAnimation {
    private static final ResourceLocation noise = new ResourceLocation("vegaline/ui/noises/noise.png");
    private final AnimationUtils noiseAnimation = new AnimationUtils(1.0f, 1.0f, 0.0f);
    private float noiseProgress;

    public void update(float animationSpeed, boolean toShow) {
        float f = this.noiseAnimation.to = toShow ? 0.0f : 1.0f;
        if (this.noiseAnimation.to == 0.0f && this.noiseAnimation.anim > 0.0f || this.noiseAnimation.to == 1.0f && this.noiseAnimation.anim < 1.0f) {
            this.noiseAnimation.getAnim();
        }
        if (this.noiseAnimation.to == 1.0f && this.noiseAnimation.anim > 0.9980392f) {
            this.noiseAnimation.setAnim(1.0f);
        } else if (this.noiseAnimation.to == 0.0f && this.noiseAnimation.anim < 0.003921569f) {
            this.noiseAnimation.setAnim(0.0f);
        }
        this.noiseAnimation.speed = animationSpeed;
        this.noiseProgress = this.noiseAnimation.anim;
    }

    private void quad(BufferBuilder buffer, float x, float y, float x2, float y2) {
        buffer.pos(x, y).tex(0.0, 0.0).endVertex();
        buffer.pos(x2, y).tex(1.0, 0.0).endVertex();
        buffer.pos(x2, y2).tex(1.0, 1.0).endVertex();
        buffer.pos(x, y2).tex(0.0, 1.0).endVertex();
    }

    public void insertRender2D(Runnable drawable, ScaledResolution sr, int reduction) {
        if (this.noiseProgress == 1.0f) {
            return;
        }
        if (this.noiseProgress == 0.0f) {
            drawable.run();
            return;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(noise);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float w = sr.getScaledWidth() * reduction;
        float h = sr.getScaledHeight() * reduction;
        int densityIterations = 3;
        w /= (float)densityIterations;
        h /= (float)densityIterations;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        for (int x = 0; x < densityIterations; ++x) {
            for (int y = 0; y < densityIterations; ++y) {
                this.quad(buffer, w * (float)x, h * (float)y, w * (float)x + w, h * (float)y + h);
            }
        }
        StencilUtil.initStencilToWrite();
        RenderUtils.glRenderStart();
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
        GL11.glBlendFunc((int)770, (int)32772);
        GL11.glAlphaFunc((int)516, (float)((1.0f - this.noiseProgress) * (1.0f - this.noiseProgress)));
        GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        tessellator.draw();
        GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        GL11.glAlphaFunc((int)516, (float)0.1f);
        GL11.glBlendFunc((int)770, (int)771);
        RenderUtils.glRenderStop();
        GL11.glEnable((int)3008);
        StencilUtil.readStencilBuffer(0);
        drawable.run();
        StencilUtil.uninitStencilBuffer();
    }

    public boolean hasFinished() {
        return this.noiseProgress == 0.0f || this.noiseProgress == 1.0f;
    }

    public float getNoiseProgress() {
        return this.noiseProgress;
    }
}

