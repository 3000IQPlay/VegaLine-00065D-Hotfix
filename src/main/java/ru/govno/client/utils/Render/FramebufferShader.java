package ru.govno.client.utils.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class FramebufferShader extends Shader {
   private static Framebuffer framebuffer;
   public static Minecraft mc = Minecraft.getMinecraft();
   protected float red;
   protected float green;
   protected float blue;
   protected float alpha;
   protected float radius = 2.0F;
   protected float quality = 1.0F;
   private boolean entityShadows;

   public FramebufferShader(String fragmentShader) {
      super(fragmentShader);
   }

   public void renderShader(float partialTicks) {
      GlStateManager.enableAlpha();
      GlStateManager.pushMatrix();
      GlStateManager.pushAttrib();
      framebuffer = this.setupFrameBuffer(framebuffer);
      framebuffer.framebufferClear();
      framebuffer.bindFramebuffer(true);
      this.entityShadows = mc.gameSettings.entityShadows;
      mc.gameSettings.entityShadows = false;
      mc.entityRenderer.setupCameraTransform(partialTicks, 0);
   }

   public void stopRenderShader(int color, float radius, float quality) {
      mc.gameSettings.entityShadows = this.entityShadows;
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      float f3 = (float)(color >> 24 & 0xFF) / 255.0F;
      float f = (float)(color >> 16 & 0xFF) / 255.0F;
      float f1 = (float)(color >> 8 & 0xFF) / 255.0F;
      float f2 = (float)(color & 0xFF) / 255.0F;
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
      );
      mc.getFramebuffer().bindFramebuffer(true);
      this.red = f;
      this.green = f1;
      this.blue = f2;
      this.quality = 14.0F;
      mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      this.startShader();
      mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(framebuffer);
      this.stopShader();
      GL11.glEnable(3008);
      GL11.glDisable(3042);
      mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
      if (frameBuffer != null) {
         frameBuffer.deleteFramebuffer();
      }

      return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
   }

   public void drawFramebuffer(Framebuffer framebuffer) {
      ScaledResolution scaledResolution = new ScaledResolution(mc);
      GL11.glBindTexture(3553, framebuffer.framebufferTexture);
      GL11.glBegin(7);
      GL11.glTexCoord2d(0.0, 1.0);
      GL11.glVertex2d(0.0, 0.0);
      GL11.glTexCoord2d(0.0, 0.0);
      GL11.glVertex2d(0.0, (double)scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 0.0);
      GL11.glVertex2d((double)scaledResolution.getScaledWidth(), (double)scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 1.0);
      GL11.glVertex2d((double)scaledResolution.getScaledWidth(), 0.0);
      GL11.glEnd();
      GL20.glUseProgram(0);
   }
}
