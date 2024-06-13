package ru.govno.client.module.modules;

import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class CaveFinder extends Module {
   public static CaveFinder get;
   public static boolean findEnabled;
   AnimationUtils darkerAlphaPC = new AnimationUtils(0.0F, 0.0F, 0.075F);

   public CaveFinder() {
      super("CaveFinder", 0, Module.Category.RENDER);
      get = this;
   }

   @Override
   public void onToggled(boolean actived) {
      this.darkerAlphaPC.setAnim(this.darkerAlphaPC.anim * 0.95F);
      this.darkerAlphaPC.to = 1.0F;
      super.onToggled(actived);
   }

   public void post2DDark(ScaledResolution sr) {
      float alphaPC;
      if ((alphaPC = this.darkerAlphaPC.getAnim()) != 1.0F && (double)alphaPC > 0.95) {
         this.darkerAlphaPC.setAnim(1.0F);
         this.darkerAlphaPC.to = 0.0F;
         findEnabled = this.isActived();
         mc.renderGlobal.loadRenderers();
      }

      if (this.darkerAlphaPC.to == 0.0F && alphaPC != 0.0F && (double)alphaPC < 0.05) {
         this.darkerAlphaPC.setAnim(0.0F);
      }

      if (alphaPC != 0.0F) {
         GL11.glDisable(2929);
         RenderUtils.drawAlphedRect(0.0, 0.0, (double)sr.getScaledWidth(), (double)sr.getScaledHeight(), ColorUtils.getColor(10, 10, 10, 255.0F * alphaPC));
         GL11.glEnable(2929);
      }
   }
}
