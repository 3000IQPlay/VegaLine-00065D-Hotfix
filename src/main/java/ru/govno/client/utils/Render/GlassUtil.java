package ru.govno.client.utils.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GlassUtil {
   protected static Minecraft mc = Minecraft.getMinecraft();
   public ResourceLocation resourceLocation = new ResourceLocation("vegaline/system/shaders/blur.json");
   public ShaderGroup shaderGroup;
   public Framebuffer framebuffer;
   public int lastFactor;
   public int lastWidth;
   public int lastHeight;

   public void init() {
      try {
         this.shaderGroup = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.resourceLocation);
         this.shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
         this.framebuffer = this.shaderGroup.mainFramebuffer;
      } catch (Exception var2) {
      }
   }

   public void glass(float xBlur, float yBlur, float widthBlur, float heightBlur, float strength, float dirX, float dirY) {
      ScaledResolution scaledResolution = new ScaledResolution(mc);
      int scaleFactor = ScaledResolution.getScaleFactor();
      int width = scaledResolution.getScaledWidth();
      int height = scaledResolution.getScaledHeight();
      if (this.sizeHasChanged(scaleFactor, width, height) || this.framebuffer == null || this.shaderGroup == null) {
         this.init();
      }

      this.lastFactor = scaleFactor;
      this.lastWidth = width;
      this.lastHeight = height;
      GL11.glPushMatrix();
      GL11.glEnable(3089);
      scissorRect(xBlur, yBlur, widthBlur, (double)heightBlur);
      this.framebuffer.bindFramebuffer(true);
      this.shaderGroup.loadShaderGroup(mc.getRenderPartialTicks());
      this.shaderGroup.getShaders().get(0).getShaderManager().getShaderUniform("Radius").set(strength);
      this.shaderGroup.getShaders().get(1).getShaderManager().getShaderUniform("Radius").set(strength);
      this.shaderGroup.getShaders().get(0).getShaderManager().getShaderUniform("BlurDir").set(dirX, dirY);
      this.shaderGroup.getShaders().get(1).getShaderManager().getShaderUniform("BlurDir").set(dirX, dirY);
      mc.getFramebuffer().bindFramebuffer(true);
      GL11.glDisable(3089);
      GL11.glPopMatrix();
   }

   public boolean sizeHasChanged(int scaleFactor, int width, int height) {
      return this.lastFactor != scaleFactor || this.lastWidth != width || this.lastHeight != height;
   }

   public static void scissorRect(float x, float y, float width, double height) {
      ScaledResolution sr = new ScaledResolution(mc);
      int factor = ScaledResolution.getScaleFactor();
      GL11.glScissor(
         (int)(x * (float)factor),
         (int)(((double)((float)sr.getScaledHeight()) - height) * (double)((float)factor)),
         (int)((width - x) * (float)factor),
         (int)((height - (double)y) * (double)((float)factor))
      );
   }
}
