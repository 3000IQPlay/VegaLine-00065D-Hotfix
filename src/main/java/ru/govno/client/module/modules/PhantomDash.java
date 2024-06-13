package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class PhantomDash extends Module {
   private int pushTicks;
   private int prevPushTicks;
   private int dashTicks;
   private int slowingTicks;
   public static double tempSpeed = 1.0;

   private int getMaxDashTicks() {
      return 20;
   }

   private int getPushLimitTicks() {
      return 3;
   }

   public PhantomDash() {
      super("PhantomDash", 0, Module.Category.MOVEMENT);
   }

   @Override
   public void onUpdate() {
      this.prevPushTicks = this.pushTicks;
      if (Minecraft.player.isSneaking()) {
         if (++this.pushTicks == 1) {
            this.slowingTrigger();
         }
      } else {
         if (this.pushTicks != 0 && this.pushTicks < this.getMaxDashTicks()) {
            this.dashingTrigger();
         }

         this.pushTicks = 0;
      }

      this.updateDashFactor();
   }

   private double speedFactor() {
      return 4.5;
   }

   private void slowingTrigger() {
      this.slowingTicks = this.getPushLimitTicks();
   }

   private void dashingTrigger() {
      if (this.pushTicks < this.getMaxDashTicks()) {
         this.dashTicks = (int)((float)this.getMaxDashTicks() * (1.0F - (float)this.slowingTicks / (float)this.getPushLimitTicks()));
      }
   }

   private void updateDashFactor() {
      if (this.slowingTicks > 0) {
         this.slowingTicks--;
      }

      if (this.pushTicks >= this.getMaxDashTicks()) {
         this.dashTicks = 0;
      }

      if (this.dashTicks > 0) {
         this.dashTicks--;
      }

      boolean slowing = this.slowingTicks > 0 && this.pushTicks > this.prevPushTicks;
      boolean dash = this.dashTicks > 0;
      tempSpeed = 1.0;
      if (slowing) {
         tempSpeed = 1.0 / this.speedFactor();
      } else {
         if (dash) {
            tempSpeed = this.speedFactor();
         }
      }
   }

   @Override
   public void onRender2D(ScaledResolution sr) {
      float w = 70.0F;
      float h = 10.0F;
      float x = (float)sr.getScaledWidth() / 2.0F - w / 2.0F;
      float y = (float)sr.getScaledHeight() / 4.0F - h / 2.0F;
      int color = -1;
      float pTicks = mc.getRenderPartialTicks();
      float smoothPush = MathUtils.lerp((float)this.prevPushTicks, (float)this.pushTicks, pTicks);
      float smoothDashTime = this.dashTicks == 0 ? 0.0F : (float)this.dashTicks + 1.0F - pTicks;
      RenderUtils.drawAlphedRect(
         (double)x,
         (double)y,
         (double)(x + w * MathUtils.clamp(smoothPush / ((float)this.getPushLimitTicks() + 1.0F), 0.0F, 1.0F)),
         (double)(y + h / 2.0F),
         color
      );
      RenderUtils.drawAlphedRect((double)x, (double)(y + h / 2.0F), (double)(x + w * (smoothDashTime / (float)this.getMaxDashTicks())), (double)(y + h), color);
      int bgColor = ColorUtils.getColor(0, 0, 0);
      RenderUtils.drawLightContureRect((double)x, (double)y, (double)(x + w), (double)(y + h), bgColor);
   }
}
