package ru.govno.client.clickgui;

import java.util.Arrays;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;
import ru.govno.client.utils.Render.Vec2fColored;

public class Slider extends Set {
   boolean dragging = false;
   AnimationUtils animL = new AnimationUtils(1.0F, 1.0F, 0.1F);
   AnimationUtils animR = new AnimationUtils(1.0F, 1.0F, 0.1F);
   AnimationUtils notify = new AnimationUtils(0.0F, 0.0F, 0.2F);
   TimerHelper soundTicker = new TimerHelper();
   FloatSettings setting;

   @Override
   public void onGuiClosed() {
      this.dragging = false;
   }

   private double getMouseSpeed() {
      float dX = (float)Mouse.getDX();
      float dY = (float)Mouse.getDX();
      return Math.sqrt((double)(dX * dX + dY * dY));
   }

   private long getTimerateSoundMove(double mouseSpeed) {
      if (mouseSpeed == 0.0) {
         return Long.MAX_VALUE;
      } else {
         long time = 1000L;
         return (long)((double)time / MathUtils.clamp(mouseSpeed * 15.0, 1.0, 200.0));
      }
   }

   private void updateSliderSounds() {
      if (this.soundTicker.hasReached((float)this.getTimerateSoundMove(this.getMouseSpeed()))) {
         ClientTune.get.playGuiSliderMoveSong();
         this.soundTicker.reset();
      }
   }

   public Slider(FloatSettings setting) {
      super(setting);
      this.setting = setting;
   }

   void drawNotify(String val, float x, float y, float alphaPC) {
      CFontRenderer font = Fonts.mntsb_10;
      float w = (float)font.getStringWidth(val);
      float texX = x - w / 2.0F;
      float smooth = 3.0F;
      w = w < smooth * 2.0F ? smooth * 2.0F : w;
      int bgCol = ColorUtils.swapAlpha(Integer.MIN_VALUE, 160.0F * alphaPC);
      GL11.glPushMatrix();
      RenderUtils.customScaledObject2D(x, y + 3.0F, 0.0F, 0.0F, alphaPC);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x - 3.0F - w / 2.0F, y - 8.0F, x + w / 2.0F + 3.0F, y, smooth, 0.5F, bgCol, bgCol, bgCol, bgCol, false, true, true
      );
      if ((double)(255.0F * alphaPC) >= 33.0) {
         font.drawString(val, (double)texX, (double)(y - 4.0F), ColorUtils.swapAlpha(-1, 255.0F * alphaPC));
      }

      RenderUtils.drawVec2Colored(
         Arrays.asList(new Vec2fColored(x - 3.0F, y, bgCol), new Vec2fColored(x + 3.0F, y, bgCol), new Vec2fColored(x, y + 3.0F, bgCol))
      );
      GL11.glPopMatrix();
   }

   @Override
   public void drawScreen(float x, float y, int step, int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(x, y, step, mouseX, mouseY, partialTicks);
      float scaledAlphaPercent = ClickGuiScreen.globalAlpha.anim / 255.0F;
      scaledAlphaPercent *= scaledAlphaPercent;
      if (ClickGuiScreen.colose) {
         scaledAlphaPercent *= scaledAlphaPercent;
      }

      float delRound = 20.0F;
      if (this.setting.fMin == 0.0F && this.setting.fMax == 1.0F) {
         delRound = 100.0F;
      }

      if (this.setting.fMax - this.setting.fMin > 5.0F) {
         delRound = 10.0F;
      }

      if (this.setting.fMax >= 100.0F
         || (this.setting.fMax == 255.0F || this.setting.fMax % 50.0F == 0.0F)
            && (this.setting.fMin == 0.0F || this.setting.fMin == 50.0F || this.setting.fMin == 26.0F)) {
         delRound = 1.0F;
      }

      double finVal = MathUtils.roundPROBLYA(delRound == 1.0F ? (float)this.setting.getInt() : this.setting.getFloat(), (double)(1.0F / delRound));
      String val = finVal + "";
      if (finVal == (double)((int)finVal)) {
         val = val.replace(".0", "");
      }

      if (this.dragging) {
         if (Keyboard.isKeyDown(42)) {
            float dx = (float)Mouse.getDX();
            if (dx != 0.0F && this.setting.getFloat() != (dx > 0.0F ? this.setting.fMax : this.setting.fMin)) {
               this.setting.setFloat(dx > 0.0F ? this.setting.fMax : this.setting.fMin);
               ClientTune.get.playGuiSliderMoveSong();
            }
         } else {
            this.setting
               .setFloat(
                  (float)MathUtils.clamp(
                     MathUtils.roundPROBLYA(
                        (float)(
                           (double)((float)mouseX - (x + 6.0F)) * (double)(this.setting.fMax - this.setting.fMin) / (double)(this.getWidth() - 12.0F)
                              + (double)this.setting.fMin
                        ),
                        (double)(1.0F / delRound)
                     ),
                     (double)this.setting.fMin,
                     (double)this.setting.fMax
                  )
               );
            this.updateSliderSounds();
         }
      }

      this.notify.to = this.dragging && this.setting.getFloat() != this.setting.fMax && this.setting.getFloat() != this.setting.fMin ? 1.0F : 0.0F;
      this.notify.speed = 0.1F;
      float xExtSlider = 6.0F;
      double renderPerc = (double)(this.getWidth() - xExtSlider * 2.0F) / (double)(this.setting.fMax - this.setting.fMin);
      float anim = (float)(
         renderPerc * MathUtils.roundPROBLYA(MathUtils.clamp(this.setting.getAnimation(), this.setting.fMin, this.setting.fMax), 0.01)
            - renderPerc * (double)this.setting.fMin
      );
      float x1 = x + xExtSlider;
      float x2 = x + this.getWidth() - xExtSlider;
      float xSlider = x + xExtSlider + anim;
      float y1 = y + 11.5F;
      float y2 = y + 14.0F;
      boolean hover = this.ishover(x1, y1, x2, y2, mouseX, mouseY);
      if (this.dragging || hover) {
         float yOffsets = this.dragging ? 1.0F : (hover ? 0.5F : 0.0F);
         if (yOffsets != 0.0F) {
            y1 -= yOffsets;
            y2 += yOffsets;
         }
      }

      int texCol = ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent);
      int colSlider1 = ClickGuiScreen.getColor(step + (int)y1, this.setting.module.category);
      int colSlider2 = ClickGuiScreen.getColor(step + (int)y1 + 180, this.setting.module.category);
      colSlider1 = ColorUtils.swapAlpha(colSlider1, (float)ColorUtils.getAlphaFromColor(colSlider1) * scaledAlphaPercent);
      colSlider2 = ColorUtils.swapAlpha(colSlider2, (float)ColorUtils.getAlphaFromColor(colSlider2) * scaledAlphaPercent);
      colSlider2 = ColorUtils.getOverallColorFrom(colSlider1, colSlider2, (xSlider - x1) / (x2 - x1));
      int bgCol = ColorUtils.getColor(0, 0, 0, 180.0F * scaledAlphaPercent);
      int bgCol2 = ColorUtils.getColor(0, 0, 0, 80.0F * scaledAlphaPercent);
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRect((double)(x1 - 0.5F), (double)(y1 - 0.5F), (double)(x2 + 0.5F), (double)(y2 + 0.5F), -1);
      StencilUtil.readStencilBuffer(1);
      RenderUtils.drawGradientSideways((double)x1, (double)y1, (double)xSlider, (double)y2, colSlider1, colSlider2);
      RenderUtils.drawAlphedRect((double)xSlider, (double)y1, (double)x2, (double)y2, bgCol2);
      RenderUtils.drawLightContureRectSmooth((double)x1, (double)y1, (double)x2, (double)y2, bgCol);
      StencilUtil.readStencilBuffer(0);
      if ((double)this.notify.getAnim() > 0.08) {
         GL11.glPushMatrix();
         RenderUtils.customScaledObject2D(xSlider, y1 + (y2 - y1) / 2.0F, 0.0F, 0.0F, this.notify.getAnim());
         GL11.glScaled(0.5, 0.5, 1.0);
         GL11.glTranslated((double)(xSlider * 2.0F + 4.0F), (double)((y1 - 9.0F) * 2.0F), 0.0);
         int texCol2 = ColorUtils.swapAlpha(
            texCol,
            (float)ColorUtils.getAlphaFromColor(texCol) * (float)(1.0 - MathUtils.getDifferenceOf(xSlider, x1 + (x2 - x1) / 2.0F) / (double)((x2 - x1) / 2.0F))
         );
         if (ColorUtils.getAlphaFromColor(texCol2) >= 33) {
            Fonts.minecraftia_14.drawString("+", 16.0, 0.0, texCol2);
            Fonts.minecraftia_14.drawString("-", -28.0, 0.0, texCol2);
         }

         GL11.glPopMatrix();
      }

      StencilUtil.uninitStencilBuffer();
      if (xSlider > x1 && xSlider < x2) {
         boolean e1 = xSlider > x1 + (x2 - x1) / 2.0F;
         double pointSize = MathUtils.clamp(
            MathUtils.getDifferenceOf(e1 ? x2 + 2.0F : x1 - 2.0F, xSlider) / 2.0 - 1.0,
            0.0,
            (double)this.notify.anim + (this.dragging ? 3.5 : (hover ? 3.25 : 3.0))
         );
         if (pointSize > 0.0) {
            RenderUtils.drawSmoothCircle((double)xSlider, (double)(y1 + (y2 - y1) / 2.0F), (float)pointSize, bgCol2);
         }

         if (pointSize > 0.5) {
            RenderUtils.drawSmoothCircle((double)xSlider, (double)(y1 + (y2 - y1) / 2.0F), (float)pointSize - 0.5F, colSlider2);
         }

         if (pointSize > 1.5) {
            RenderUtils.drawSmoothCircle((double)xSlider, (double)(y1 + (y2 - y1) / 2.0F), (float)pointSize - 1.5F, bgCol2);
         }

         if (!this.dragging && (double)this.notify.anim > 0.03) {
            pointSize += (double)(2.0F + this.notify.anim * 5.0F);
            int effectColor = ColorUtils.swapAlpha(
               colSlider2, (float)ColorUtils.getAlphaFromColor(colSlider2) * ((double)this.notify.anim > 0.5 ? 1.0F - this.notify.anim : this.notify.anim)
            );
            if (ColorUtils.getAlphaFromColor(effectColor) >= 1) {
               RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(
                  (float)((double)xSlider - pointSize * 2.0),
                  (float)((double)(y1 + (y2 - y1) / 2.0F) - pointSize / 2.0),
                  (float)((double)xSlider + pointSize * 2.0),
                  (float)((double)(y1 + (y2 - y1) / 2.0F) + pointSize / 2.0),
                  (float)pointSize,
                  effectColor,
                  effectColor,
                  effectColor,
                  effectColor,
                  true
               );
            }
         }
      }

      if (255.0F * scaledAlphaPercent >= 33.0F) {
         float scale = 1.0F - this.notify.getAnim();
         if (scale > 0.98F) {
            scale = 1.0F;
         }

         if (scale != 1.0F) {
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(x + (this.getWidth() - anim / ((this.getWidth() - xExtSlider) / this.getWidth())), y1, 0.0F, 0.0F, scale);
         }

         if (255.0F * scaledAlphaPercent * scale >= 33.0F) {
            String centerString = this.ishover(
                     x + this.getWidth() / 2.0F - (float)Fonts.comfortaaBold_15.getStringWidth(this.setting.getName()) / 2.0F + 2.0F,
                     y + 4.0F,
                     x + this.getWidth() / 2.0F + (float)Fonts.comfortaaBold_15.getStringWidth(this.setting.getName()) / 2.0F - 2.0F,
                     y + 7.0F,
                     mouseX,
                     mouseY
                  )
                  && !this.dragging
               ? val
               : this.setting.getName();
            Fonts.comfortaaBold_15
               .drawString(
                  centerString,
                  (double)(x + this.getWidth() / 2.0F - (float)Fonts.comfortaaBold_15.getStringWidth(centerString) / 2.0F),
                  (double)(y + 3.0F),
                  ColorUtils.swapAlpha(texCol, (float)ColorUtils.getAlphaFromColor(texCol) * scale)
               );
         }

         if (scale != 1.0F) {
            GL11.glPopMatrix();
         }
      }

      double diffL = MathUtils.getDifferenceOf(x1, xSlider);
      this.animL.to = !ClickGuiScreen.colose && this.dragging && diffL < 15.0 ? 1.0F - this.notify.getAnim() : 1.0F;
      double diffR = MathUtils.getDifferenceOf(x2, xSlider);
      this.animR.to = !ClickGuiScreen.colose && this.dragging && diffR < 15.0 ? 1.0F - this.notify.getAnim() : 1.0F;
      if ((double)this.animL.getAnim() < 0.98) {
         GL11.glPushMatrix();
         RenderUtils.customScaledObject2DCoords(
            x1 + 0.5F, y + 0.5F, x1 + (float)Fonts.comfortaaBold_12.getStringWidth(this.setting.fMin + "") + 5.5F, y1 - 2.0F, this.animL.anim
         );
      }

      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x1 + 0.5F,
         y + 0.5F,
         x1 + (float)Fonts.comfortaaBold_12.getStringWidth(this.setting.fMin + "") + 5.5F,
         y1 - 2.0F,
         2.0F,
         0.5F,
         bgCol2,
         bgCol2,
         bgCol2,
         bgCol2,
         false,
         true,
         true
      );
      if (255.0F * scaledAlphaPercent * this.animL.anim >= 33.0F) {
         Fonts.comfortaaBold_12
            .drawString(
               this.setting.fMin + "",
               (double)(x1 + 3.0F),
               (double)(y1 - 7.5F),
               ColorUtils.swapAlpha(texCol, (float)ColorUtils.getAlphaFromColor(texCol) * this.animL.anim)
            );
      }

      if ((double)this.animL.anim < 0.98) {
         GL11.glPopMatrix();
      }

      if ((double)this.animR.getAnim() < 0.98) {
         GL11.glPushMatrix();
         RenderUtils.customScaledObject2DCoords(
            x2 - (float)Fonts.comfortaaBold_12.getStringWidth(this.setting.fMax + "") - 5.5F, y + 0.5F, x2 - 0.5F, y1 - 2.0F, this.animR.anim
         );
      }

      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x2 - (float)Fonts.comfortaaBold_12.getStringWidth(this.setting.fMax + "") - 5.5F,
         y + 0.5F,
         x2 - 0.5F,
         y1 - 2.0F,
         2.0F,
         0.5F,
         bgCol2,
         bgCol2,
         bgCol2,
         bgCol2,
         false,
         true,
         true
      );
      if (255.0F * scaledAlphaPercent * this.animR.anim >= 33.0F) {
         Fonts.comfortaaBold_12
            .drawString(
               this.setting.fMax + "", (double)(x2 - (float)Fonts.comfortaaBold_12.getStringWidth(this.setting.fMax + "") - 3.0F), (double)(y1 - 7.5F), texCol
            );
      }

      if ((double)this.animR.anim < 0.98) {
         GL11.glPopMatrix();
      }

      if ((double)this.notify.getAnim() > 0.08) {
         this.drawNotify(val, xSlider, y1 - 5.0F, this.notify.anim * scaledAlphaPercent);
      }

      if (ClickGuiScreen.colose) {
         this.dragging = false;
      }
   }

   @Override
   public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(x, y, mouseX, mouseY, mouseButton);
      if (this.ishover((float)x, (float)(y + 10), (float)x + this.getWidth(), (float)(y + 16), mouseX, mouseY) && mouseButton == 0) {
         this.dragging = true;
         ClickGuiScreen.resetHolds();
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      super.mouseReleased(mouseX, mouseY, mouseButton);
      if (mouseButton == 0) {
         this.dragging = false;
      }
   }

   @Override
   public float getWidth() {
      return 118.0F;
   }

   @Override
   public float getHeight() {
      return 17.0F;
   }
}
