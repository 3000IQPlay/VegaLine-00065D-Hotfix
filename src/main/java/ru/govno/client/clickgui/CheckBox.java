package ru.govno.client.clickgui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class CheckBox extends Set {
   AnimationUtils anim2 = new AnimationUtils(0.0F, 0.0F, 0.06F);
   AnimationUtils toggleAnim = new AnimationUtils(0.0F, 0.0F, 0.12F);
   public boolean binding;
   int keyBindToSet;
   float bindingWidth;
   AnimationUtils bindingAnim = new AnimationUtils(0.0F, 0.0F, 0.1F);
   AnimationUtils bindHoldAnim = new AnimationUtils(0.0F, 0.0F, 0.1F);
   AnimationUtils bindWaveAnim = new AnimationUtils(0.0F, 0.0F, 0.05F);
   public TimerHelper holdBindTimer = new TimerHelper();
   BoolSettings setting;

   public CheckBox(BoolSettings setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void onGuiClosed() {
      this.binding = false;
      this.bindingAnim.to = 0.0F;
      this.keyBindToSet = -1;
   }

   float maxBindTime() {
      return 600.0F;
   }

   void updateBinding() {
      if (ClickGuiScreen.colose && this.binding) {
         this.binding = false;
         this.bindingAnim.to = 0.0F;
      }

      if (!this.binding || this.keyBindToSet == -1 || !Keyboard.isKeyDown(this.keyBindToSet)) {
         this.holdBindTimer.reset();
      }

      if (this.binding) {
         if (this.keyBindToSet != -1 && this.holdBindTimer.hasReached(this.maxBindTime()) && this.bindHoldAnim.getAnim() > 0.9722222F) {
            int prevBind = this.setting.getBind();
            this.setting.setBind(this.keyBindToSet == 211 ? 0 : this.keyBindToSet);
            if (prevBind != this.setting.getBind()) {
               ClientTune.get.playGuiModuleBindSong(this.setting.getBind() != 0);
               this.bindWaveAnim.setAnim(1.0F);
            }

            this.binding = false;
         }

         this.bindHoldAnim.to = MathUtils.clamp((float)this.holdBindTimer.getTime() / this.maxBindTime(), 0.0F, 1.0F);
      } else {
         this.bindHoldAnim.setAnim(0.0F);
      }

      this.bindHoldAnim.speed = 0.15F;
      this.bindWaveAnim.speed = 0.04F;
      this.bindingAnim.getAnim();
      if (MathUtils.getDifferenceOf(this.bindingAnim.anim, this.bindingAnim.to) < 0.003) {
         this.bindingAnim.setAnim(this.bindingAnim.to);
      }

      this.bindingAnim.to = !this.binding && !(this.bindWaveAnim.getAnim() > 0.004F) ? 0.0F : 1.0F;
      this.bindHoldAnim.getAnim();
      this.bindHoldAnim.setAnim((double)this.bindHoldAnim.anim < 0.005 ? 0.0F : ((double)this.bindHoldAnim.anim > 0.999 ? 1.0F : this.bindHoldAnim.anim));
   }

   @Override
   public void drawScreen(float x, float y, int step, int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(x, y, step, mouseX, mouseY, partialTicks);
      float scaledAlphaPercent = ClickGuiScreen.globalAlpha.anim / 255.0F;
      scaledAlphaPercent *= scaledAlphaPercent;
      float anim = this.setting.getAnimation();
      this.anim2.to = anim;
      this.anim2.speed = 0.0175F / (float)MathUtils.clamp(MathUtils.getDifferenceOf(this.anim2.getAnim(), anim), 0.1F, 2.0);
      if (this.toggleAnim.getAnim() > 1.0F) {
         this.toggleAnim.to = 0.0F;
         this.toggleAnim.setAnim(1.0F);
      }

      this.updateBinding();
      String setKeyBindName = Keyboard.getKeyName(this.setting.getBind()).replace("NONE", "");
      CFontRenderer boundFont = Fonts.neverlose500_13;
      float bindCircleSize = 10.0F * this.bindingAnim.anim;
      this.bindingWidth = (this.setting.getBind() != 0 ? (float)boundFont.getStringWidth(setKeyBindName) + 2.0F : 0.0F) * this.bindingAnim.anim
         + bindCircleSize;
      if (this.bindingWidth >= 0.5F) {
         float bindX = x + 4.0F;
         float bindX2 = bindX + this.bindingWidth;
         float round = MathUtils.clamp((bindX2 - bindX) / 4.0F, 0.0F, 2.0F);
         int outCol1 = ClickGuiScreen.getColor((int)((y - 15.0F) / 3.6F), this.setting.module.category);
         outCol1 = ColorUtils.swapAlpha(outCol1, 100.0F * scaledAlphaPercent * this.bindingAnim.anim);
         int outCol2 = ClickGuiScreen.getColor((int)((y - 5.0F) / 3.6F), this.setting.module.category);
         outCol2 = ColorUtils.swapAlpha(outCol2, 100.0F * scaledAlphaPercent * this.bindingAnim.anim);
         if (ColorUtils.getAlphaFromColor(outCol1) >= 10 && ColorUtils.getAlphaFromColor(outCol2) >= 10) {
            RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(
               bindX, y + 2.0F, bindX2, y + this.getHeight() - 2.0F, round, outCol1, outCol2, outCol2, outCol1, true
            );
         }

         int bindBGColor = ColorUtils.getColor(0, 0, 0, 45.0F * scaledAlphaPercent * this.bindingAnim.anim);
         if (ColorUtils.getAlphaFromColor(bindBGColor) >= 10) {
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               bindX, y + 2.0F, bindX2, y + this.getHeight() - 2.0F, round, 0.5F, bindBGColor, bindBGColor, bindBGColor, bindBGColor, false, true, true
            );
         }

         String bindString = MathUtils.getStringPercent(setKeyBindName, this.bindingAnim.anim * (1.0F + bindCircleSize / (bindX2 - bindX)));
         int bindTextColor = ColorUtils.swapAlpha(-1, 255.0F * this.bindingAnim.anim * scaledAlphaPercent);
         float bindProgress = MathUtils.clamp(this.bindHoldAnim.anim + this.bindWaveAnim.anim, 0.0F, 1.0F);
         if ((double)bindProgress < 0.03) {
            bindProgress = 0.0F;
         }

         if (ColorUtils.getAlphaFromColor(bindTextColor) >= 33) {
            float dragRot = bindProgress * 100.0F % 50.0F / 50.0F;
            float dragStrengh = bindString.length() > 1 ? 10.0F : 45.0F;
            dragRot = -(dragStrengh / 2.0F * dragRot) + (float)MathUtils.easeInOutQuadWave((double)dragRot) * dragStrengh;
            dragRot *= bindProgress;
            if (MathUtils.getDifferenceOf(dragRot, 0.0F) > 1.0) {
               GL11.glPushMatrix();
               RenderUtils.customRotatedObject2D(bindX + 1.5F, y, (float)boundFont.getStringWidth(bindString), this.getHeight(), (double)dragRot);
            }

            boundFont.drawStringWithShadow(bindString, (double)(bindX + 1.5F), (double)(y + 5.5F), bindTextColor);
            if ((double)this.bindWaveAnim.anim > 0.004) {
               GL11.glPushMatrix();
               float smoothAsWave = (float)MathUtils.easeInOutQuad((double)this.bindWaveAnim.anim);
               float smoothWaveAsWave = (float)MathUtils.easeInOutQuadWave((double)this.bindWaveAnim.anim);
               RenderUtils.customScaledObject2D(bindX + 1.5F, y, (float)boundFont.getStringWidth(bindString), this.getHeight(), 1.0F + smoothAsWave * 4.0F);
               int textEffectColor = ColorUtils.getOverallColorFrom(0, bindTextColor, MathUtils.clamp(smoothWaveAsWave * 2.0F, 0.0F, 1.0F));
               if (ColorUtils.getAlphaFromColor(textEffectColor) >= 33) {
                  boundFont.drawStringWithShadow(bindString, (double)(bindX + 1.5F), (double)(y + 5.5F), textEffectColor);
               }

               GL11.glPopMatrix();
            }

            if (MathUtils.getDifferenceOf(dragRot, 0.0F) > 1.0) {
               GL11.glPopMatrix();
            }
         }

         if (this.bindingAnim.to != 0.0F) {
            float timedWave = (float)((System.currentTimeMillis() - (long)((int)(y * 5.0F))) % 750L) / 750.0F;
            timedWave = (float)MathUtils.easeInCircle((double)((timedWave > 0.5F ? 1.0F - timedWave : timedWave) * 2.0F));
            timedWave = MathUtils.lerp(timedWave, 1.0F, MathUtils.clamp(this.bindHoldAnim.anim * 4.0F, 0.0F, 1.0F));
            if (timedWave > 1.0F) {
               timedWave = 1.0F;
            }

            int pointColor = ClickGuiScreen.getColor((int)((y - 10.0F) / 3.6F), this.setting.module.category);
            pointColor = ColorUtils.getOverallColorFrom(pointColor, ColorUtils.getColor(255, 255, 255), 0.25F + timedWave / 1.5F);
            pointColor = ColorUtils.swapAlpha(pointColor, 195.0F * scaledAlphaPercent * this.bindingAnim.anim);
            float circleX = bindX2 - bindCircleSize / 2.0F;
            float circleY = y + this.getHeight() / 2.0F;
            float circleR = bindCircleSize / (1.75F - bindProgress / 1.5F) * (0.75F + 0.25F * timedWave) / 2.0F;
            if (bindProgress == 0.0F || (double)this.bindWaveAnim.anim > 0.004) {
               RenderUtils.drawSmoothCircle(
                  (double)circleX,
                  (double)circleY,
                  MathUtils.clamp(circleR, 0.5F, 9.0F),
                  (double)this.bindWaveAnim.anim > 0.004
                     ? ColorUtils.swapAlpha(pointColor, (float)ColorUtils.getAlphaFromColor(pointColor) * this.bindWaveAnim.anim)
                     : pointColor
               );
            }

            RenderUtils.drawSmoothCircle(
               (double)circleX, (double)circleY, MathUtils.clamp(circleR / 1.25F, 0.5F, 9.0F), ColorUtils.toDark(pointColor, bindProgress / 2.0F)
            );
            RenderUtils.drawSmoothCircle(
               (double)circleX, (double)circleY, MathUtils.clamp(circleR / 2.5F, 0.5F, 9.0F), ColorUtils.toDark(pointColor, 0.5F + bindProgress / 2.0F)
            );
            if (bindProgress != 0.0F) {
               int progressColor = ColorUtils.getOverallColorFrom(
                  ClickGuiScreen.getColor((int)((y - 10.0F) / 3.6F), this.setting.module.category), -1, bindProgress
               );
               progressColor = ColorUtils.swapAlpha(
                  progressColor,
                  (float)ColorUtils.getAlphaFromColor(progressColor)
                     * scaledAlphaPercent
                     * this.bindingAnim.anim
                     * MathUtils.clamp(bindProgress * 2.0F, 0.0F, 1.0F)
               );
               RenderUtils.drawClientCircleWithOverallToColor(
                  circleX,
                  (double)circleY,
                  circleR,
                  (double)this.bindWaveAnim.anim > 0.1 ? 360.0F : bindProgress * 361.0F,
                  1.0F + 0.5F * this.bindingAnim.anim * bindProgress,
                  scaledAlphaPercent * this.bindingAnim.anim,
                  progressColor,
                  1.0F
               );
            }
         }
      }

      this.toggleAnim.getAnim();
      float xOffset = this.getCheckBoxXOffset();
      float xPos = x
         + (
            this.ishover(x + xOffset, y + this.getHeight() / 2.0F - 6.0F, x + 21.0F + xOffset, y + this.getHeight() / 2.0F + 6.0F, mouseX, mouseY)
                  && (double)this.bindingAnim.anim < 0.05
                  && !this.binding
               ? 6.5F
               : 6.0F
         )
         + this.bindingWidth;
      float yPos = y + 1.5F;
      float h = this.getHeight() - 3.0F;
      float w = 18.0F;
      float extX = 5.0F;
      float extY = 2.0F;
      float pX1 = xPos + 5.0F + 8.0F * anim;
      float pX2 = xPos + 5.0F + 8.0F * this.anim2.anim;
      float progX1 = pX1 < pX2 ? pX1 : pX2;
      float progX2 = pX1 > pX2 ? pX1 : pX2;
      int color = ClickGuiScreen.getColor((int)(y / 3.6F), this.setting.module.category);
      int offC = ColorUtils.getOverallColorFrom(ColorUtils.getColor(0, 0, 0, 255), color, this.anim2.anim / 3.0F + 0.33333334F);
      int onC = ColorUtils.swapAlpha(color, 255.0F);
      int colBG = ColorUtils.getOverallColorFrom(offC, onC, anim);
      int colBGShadow = ColorUtils.getOverallColorFrom(
         ColorUtils.getOverallColorFrom(offC, ColorUtils.getColor(0, 0, 0, 255), 0.5F), onC, 1.0F - this.anim2.anim
      );
      colBG = ColorUtils.swapAlpha(colBG, (float)ColorUtils.getAlphaFromColor(colBG) * scaledAlphaPercent);
      colBGShadow = ColorUtils.swapAlpha(colBGShadow, (float)ColorUtils.getAlphaFromColor(colBGShadow) * scaledAlphaPercent);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         xPos, yPos, xPos + 18.0F, yPos + h, 4.0F, 0.5F, colBG, colBG, colBG, colBG, false, true, true
      );
      float r = (h - 4.0F) / 2.0F;
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         progX1 - r, yPos + 2.0F, progX2 + r, yPos + h - 2.0F, r, 0.5F + this.anim2.anim, colBGShadow, colBGShadow, colBGShadow, colBGShadow, false, true, true
      );
      CFontRenderer font = Fonts.comfortaaBold_14;
      if (255.0F * scaledAlphaPercent >= 33.0F) {
         float textX = xPos + 18.0F + 3.0F + this.toggleAnim.anim * 2.0F;
         float textY = yPos + 3.5F;
         int textColor = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * scaledAlphaPercent);

         for (char theChar : this.setting.getName().toCharArray()) {
            String charA = String.valueOf(theChar);
            float charW = (float)font.getStringWidth(charA) * 1.025F;
            if (textX + charW < x + this.getWidth() - 3.0F) {
               font.drawString(charA, (double)textX, (double)textY, textColor);
            }

            textX += charW;
         }
      }
   }

   @Override
   public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(x, y, mouseX, mouseY, mouseButton);
      float xOffset = this.getCheckBoxXOffset();
      boolean hover = this.ishover(
         (float)x + xOffset, (float)y + this.getHeight() / 2.0F - 6.0F, (float)x + 21.0F + xOffset, (float)y + this.getHeight() / 2.0F + 6.0F, mouseX, mouseY
      );
      if (hover && mouseButton == 0) {
         ClientTune.get.playGuiScreenCheckBox(this.setting.getBool());
         this.setting.toggleBool();
         this.toggleAnim.to = 1.1F;
         if (this.binding) {
            this.binding = false;
            ClientTune.get.playGuiModuleBindingToggleSong(false);
            this.bindingAnim.to = 0.0F;
            this.keyBindToSet = -1;
         }
      } else if (this.ishover(
            (float)x + (this.binding ? 2.0F : xOffset),
            (float)y + this.getHeight() / 2.0F - 6.0F,
            (float)x + 21.0F + xOffset,
            (float)y + this.getHeight() / 2.0F + 6.0F,
            mouseX,
            mouseY
         )
         && mouseButton == 2) {
         this.binding = !this.binding;
         this.keyBindToSet = -1;
         this.bindingAnim.to = this.binding ? 1.0F : 0.0F;
         ClientTune.get.playGuiModuleBindingToggleSong(this.binding);
      }
   }

   @Override
   public void keyPressed(int key) {
      if (this.binding && !this.holdBindTimer.hasReached(50.0F) && key != 42 && key != 56 && key != 58 && key != 1) {
         this.keyBindToSet = key;
      }
   }

   private float getCheckBoxXOffset() {
      return 3.5F + this.bindingWidth;
   }

   @Override
   public float getWidth() {
      return 118.0F;
   }

   @Override
   public float getHeight() {
      return 13.0F;
   }
}
