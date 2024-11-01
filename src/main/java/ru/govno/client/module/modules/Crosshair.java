package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.math.MathHelper;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Crosshair extends Module {
   public static Crosshair get;
   public ModeSettings Mode;
   public ColorSettings PickColor;
   final AnimationUtils anim = new AnimationUtils(360.0F, 360.0F, 0.08F);
   AnimationUtils mousePreX = new AnimationUtils(0.0F, 0.0F, 0.025F);
   AnimationUtils mousePreY = new AnimationUtils(0.0F, 0.0F, 0.025F);
   public float[] crossPosMotions = new float[]{0.0F, 0.0F};

   public Crosshair() {
      super("Crosshair", 0, Module.Category.RENDER);
      this.settings.add(this.Mode = new ModeSettings("Mode", "Sniper", this, new String[]{"Sniper", "Quad", "Circle", "Fortnite", "NewBalance"}));
      this.settings
         .add(
            this.PickColor = new ColorSettings("PickColor", ColorUtils.getColor(30, 80, 255), this, () -> !this.Mode.currentMode.equalsIgnoreCase("Fortnite"))
         );
      get = this;
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.Mode.currentMode);
   }

   @Override
   public void onRender2D(ScaledResolution sr) {
      int crosshairColor = this.PickColor.color;
      float scaledWidth = (float)GuiScreen.width;
      float scaledHeight = (float)GuiScreen.height;
      int nullAlpha = ColorUtils.swapAlpha(crosshairColor, 0.0F);
      this.mousePreX.to = 0.0F;
      this.mousePreY.to = 0.0F;
      if (mc.gameSettings.thirdPersonView == 0 && !mc.gameSettings.showDebugInfo) {
         if (this.Mode.currentMode.equalsIgnoreCase("Circle")) {
            RenderUtils.fixShadows();
            this.anim.to = MathHelper.clamp(Minecraft.player.getCooledAttackStrength(0.0F) * 380.0F, 0.0F, 361.0F);
            this.anim.speed = 0.1F + (float)(0.1F * (MathUtils.getDifferenceOf(360.0F, this.anim.getAnim()) / 360.0));
            RenderUtils.drawCroneShadow(
               (double)(scaledWidth / 2.0F),
               (double)(scaledHeight / 2.0F),
               0,
               360,
               4.0F,
               1.25F,
               ColorUtils.getColor(0, 0, 0, 50),
               ColorUtils.getColor(0, 0, 0, 50),
               false
            );
            RenderUtils.drawCroneShadow(
               (double)(scaledWidth / 2.0F), (double)(scaledHeight / 2.0F), 360 - (int)this.anim.getAnim() + 90, 450, 3.0F, 1.5F, 0, crosshairColor, false
            );
            RenderUtils.drawCroneShadow(
               (double)(scaledWidth / 2.0F), (double)(scaledHeight / 2.0F), 360 - (int)this.anim.getAnim() + 90, 450, 4.5F, 1.5F, crosshairColor, 0, false
            );
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Sniper")) {
            RenderUtils.drawRect(
               (double)(scaledWidth / 2.0F - 0.25F),
               (double)(scaledHeight / 2.0F - 0.25F),
               (double)(scaledWidth / 2.0F + 0.25F),
               (double)(scaledHeight / 2.0F + 0.25F),
               crosshairColor
            );
            RenderUtils.fixShadows();
            RenderUtils.drawVGradientRect(
               scaledWidth / 2.0F - 0.25F, scaledHeight / 2.0F - 7.0F, scaledWidth / 2.0F + 0.25F, scaledHeight / 2.0F - 1.0F, nullAlpha, crosshairColor
            );
            RenderUtils.drawVGradientRect(
               scaledWidth / 2.0F - 0.25F, scaledHeight / 2.0F + 1.25F, scaledWidth / 2.0F + 0.25F, scaledHeight / 2.0F + 7.0F, crosshairColor, nullAlpha
            );
            RenderUtils.drawGradientSideways(
               (double)(scaledWidth / 2.0F - 7.0F),
               (double)(scaledHeight / 2.0F - 0.25F),
               (double)(scaledWidth / 2.0F - 1.25F),
               (double)(scaledHeight / 2.0F + 0.25F),
               nullAlpha,
               crosshairColor
            );
            RenderUtils.drawGradientSideways(
               (double)(scaledWidth / 2.0F + 1.25F),
               (double)(scaledHeight / 2.0F - 0.25F),
               (double)(scaledWidth / 2.0F + 7.0F),
               (double)(scaledHeight / 2.0F + 0.25F),
               crosshairColor,
               nullAlpha
            );
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Quad")) {
            float climb = 4.0F;
            float radius = climb * Minecraft.player.getCooledAttackStrength(mc.getRenderPartialTicks());
            RenderUtils.drawRect(
               (double)(scaledWidth / 2.0F - radius - 0.5F),
               (double)(scaledHeight / 2.0F - radius),
               (double)(scaledWidth / 2.0F - radius),
               (double)(scaledHeight / 2.0F + radius),
               crosshairColor
            );
            RenderUtils.drawRect(
               (double)(scaledWidth / 2.0F + radius),
               (double)(scaledHeight / 2.0F - radius),
               (double)(scaledWidth / 2.0F + radius + 0.5F),
               (double)(scaledHeight / 2.0F + radius),
               crosshairColor
            );
            RenderUtils.drawRect(
               (double)(scaledWidth / 2.0F - radius),
               (double)(scaledHeight / 2.0F - radius - 0.5F),
               (double)(scaledWidth / 2.0F + radius),
               (double)(scaledHeight / 2.0F - radius),
               crosshairColor
            );
            RenderUtils.drawRect(
               (double)(scaledWidth / 2.0F - radius),
               (double)(scaledHeight / 2.0F + radius),
               (double)(scaledWidth / 2.0F + radius),
               (double)(scaledHeight / 2.0F + radius + 0.5F),
               crosshairColor
            );
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Fortnite")) {
            RenderUtils.drawSmoothCircle((double)(sr.getScaledWidth() / 2), (double)(sr.getScaledHeight() / 2), 1.75F, ColorUtils.getColor(10, 10, 10));
            RenderUtils.drawSmoothCircle((double)(sr.getScaledWidth() / 2), (double)(sr.getScaledHeight() / 2), 1.2F, -1);
         }

         if (this.Mode.currentMode.equalsIgnoreCase("NewBalance")) {
            this.updateMotions();
            this.draw((float)(sr.getScaledWidth() / 2) - this.getMouseMotion()[0], (float)(sr.getScaledHeight() / 2) - this.getMouseMotion()[1], crosshairColor);
         }
      }
   }

   void updateMotions() {
      int vantuz = Minecraft.player.ticksExisted < 10 ? 0 : 1;
      if (Minecraft.player.isRiding()) {
         this.mousePreX.to = 0.0F;
         this.mousePreY.to = 0.0F;
      } else {
         this.mousePreX.to = (Minecraft.player.lastReportedPreYaw - Minecraft.player.rotationYaw) * 3.5F * (float)vantuz;
         this.mousePreY.to = (EntityPlayerSP.lastReportedPrePitch - Minecraft.player.rotationPitch) * 5.0F * (float)vantuz;
      }

      this.crossPosMotions[0] = -this.mousePreX.getAnim();
      this.crossPosMotions[1] = -this.mousePreY.getAnim();
   }

   public float[] getMouseMotion() {
      return new float[]{this.mousePreX.anim, this.mousePreY.anim};
   }

   public void draw(float x, float y, int color) {
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      RenderUtils.drawPolygonPartsGlowBackSAlpha((double)x, (double)y, 4.0F, 1, color, 0, ColorUtils.getGLAlphaFromColor(color), true);
   }
}
