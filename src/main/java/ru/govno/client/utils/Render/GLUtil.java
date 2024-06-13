package ru.govno.client.utils.Render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GLUtil {
   public static void render(int mode, Runnable render) {
      GL11.glBegin(mode);
      render.run();
      GL11.glEnd();
   }

   public static void setup2DRendering(Runnable f) {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3553);
      f.run();
      GL11.glEnable(3553);
      GlStateManager.disableBlend();
   }

   public static void rotate(float x, float y, float rotate, Runnable f) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 0.0F);
      GlStateManager.rotate(rotate, 0.0F, 0.0F, -1.0F);
      GlStateManager.translate(-x, -y, 0.0F);
      f.run();
      GlStateManager.popMatrix();
   }
}
