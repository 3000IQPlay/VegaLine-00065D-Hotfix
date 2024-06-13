package ru.govno.client.clickgui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec2f;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class Colors extends Set {
   AnimationUtils anim = new AnimationUtils(0.0F, 0.0F, 0.15F);
   float offsetX = -1.2312312E8F;
   float offsetY = -1.2312312E8F;
   float offsetX2 = -1.2312312E8F;
   float offsetX3 = -1.2312312E8F;
   AnimationUtils arrow = new AnimationUtils(0.0F, 0.0F, 0.15F);
   boolean dragginggr = false;
   boolean dragginghsb = false;
   boolean draggingalpha = false;
   boolean open = false;
   TimerHelper soundTicker = new TimerHelper();
   ColorSettings setting;

   @Override
   public void onGuiClosed() {
      this.dragginggr = false;
      this.dragginghsb = false;
      this.draggingalpha = false;
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
         return (long)((double)time / MathUtils.clamp(mouseSpeed * 10.0, 1.0, 100.0));
      }
   }

   private void updateSliderSounds() {
      if (this.soundTicker.hasReached((float)this.getTimerateSoundMove(this.getMouseSpeed()))) {
         ClientTune.get.playGuiSliderMoveSong();
         this.soundTicker.reset();
      }
   }

   public Colors(ColorSettings setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void drawScreen(float x, float y, int step, int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(x, y, step, mouseX, mouseY, partialTicks);
      float scaledAlphaPercent = ClickGuiScreen.globalAlpha.anim / 255.0F;
      scaledAlphaPercent *= scaledAlphaPercent;
      if (ClickGuiScreen.colose) {
         scaledAlphaPercent *= scaledAlphaPercent;
      }

      GlStateManager.enableAlpha();
      this.anim.to = this.open ? 1.0F : 0.0F;
      float toRot = this.open ? -90.0F : 0.0F;
      this.arrow.to = toRot;
      this.arrow.speed = MathUtils.getDifferenceOf(this.arrow.getAnim(), toRot) > 1.0 ? 0.1F : 0.2F;
      float posX = x + 4.0F;
      float posY = y + 1.5F;
      float w = this.getWidth() - 8.0F;
      float getHeight = this.getHeight();
      float h = getHeight - 1.0F;
      int cc = ColorUtils.getColor(0, 0, 0, 110.0F * scaledAlphaPercent);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         posX, posY + 0.5F, posX + w, posY + getHeight - 2.0F, 2.0F, 0.5F, cc, cc, cc, cc, false, true, true
      );
      RenderUtils.drawAlphedSideways(
         (double)posX,
         (double)posY,
         (double)(posX + w),
         (double)(posY + 1.0F),
         ColorUtils.swapAlpha(ClickGuiScreen.getColor((int)((float)(step + 120) + y / getHeight), this.setting.module.category), 255.0F * scaledAlphaPercent),
         ColorUtils.swapAlpha(ClickGuiScreen.getColor((int)((float)step + y / this.getHeight()), this.setting.module.category), 255.0F * scaledAlphaPercent)
      );
      if (255.0F * scaledAlphaPercent > 26.0F) {
         Fonts.comfortaaBold_12
            .drawStringWithShadow(
               this.setting.getName(),
               (double)(posX + 22.0F),
               (double)(posY + 7.0F),
               ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * scaledAlphaPercent)
            );
      }

      this.drawArrow(
         posX + w - 8.0F,
         posY + 5.0F,
         ColorUtils.swapAlpha(
            ColorUtils.getFixedWhiteColor(), MathUtils.clamp(26.0F * scaledAlphaPercent + 175.0F * this.anim.anim * scaledAlphaPercent, 0.0F, 255.0F)
         )
      );
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         posX + 5.0F, posY + 6.0F, posX + 16.0F, posY + 11.0F, 2.0F, 0.5F, -1, -1, -1, -1, false, true, false
      );
      StencilUtil.readStencilBuffer(1);
      RenderUtils.drawAlphedRect(
         (double)(x + 8.5F),
         (double)(y + 6.0F),
         (double)(x + 20.0F),
         (double)(y + 14.0F),
         ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 60.0F * scaledAlphaPercent)
      );

      for (int i2 = 0; i2 < 12; i2 += 2) {
         for (int i = 0; i < 8; i += 2) {
            GL11.glTranslated((double)(x + 8.5F), (double)(y + 6.0F), 0.0);
            RenderUtils.drawAlphedRect(
               (double)i2, (double)i, (double)(i2 + 1), (double)(i + 1), ColorUtils.getColor(0, 0, 0, (int)(90.0F * scaledAlphaPercent))
            );
            GL11.glTranslated((double)(-(x + 8.5F)), (double)(-(y + 6.0F)), 0.0);
         }
      }

      StencilUtil.uninitStencilBuffer();
      int finalColor = ColorUtils.swapAlpha(this.setting.color, (float)ColorUtils.getAlphaFromColor(this.setting.color) * scaledAlphaPercent);
      int finalColor2 = ColorUtils.swapAlpha(this.setting.color, (float)ColorUtils.getAlphaFromColor(this.setting.color) * scaledAlphaPercent / 2.5F);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         posX + 5.0F, posY + 6.0F, posX + 16.0F, posY + 11.0F, 2.0F, 0.5F, finalColor, finalColor, finalColor, finalColor, false, true, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         posX + 5.0F, posY + 6.0F, posX + 16.0F, posY + 11.0F, 2.0F, 6.0F, finalColor2, finalColor2, finalColor2, finalColor2, true, false, true
      );
      this.anim.speed = MathUtils.getDifferenceOf(this.anim.anim, h) < 1.0 ? 0.5F : 0.1F;
      scaledAlphaPercent *= this.anim.anim;
      if (this.anim.anim > 0.1F) {
         float grX = posX + 3.0F;
         float grY = posY + 19.0F;
         float grH = 34.0F;
         float grW = 34.0F;
         if (this.dragginggr) {
            this.dragginghsb = false;
            this.draggingalpha = false;
            this.offsetX = MathUtils.clamp((float)mouseX - grX, 0.0F, grW);
            this.offsetY = MathUtils.clamp((float)mouseY - grY, 0.0F, grH);
         } else if (ColorUtils.getSaturateFromColor(this.setting.color) != 0.0F) {
            this.offsetX = ColorUtils.getSaturateFromColor(this.setting.color) * grW;
            this.offsetY = grH - ColorUtils.getBrightnessFromColor(this.setting.color) * grH;
         }

         float draggXgr = grX + this.offsetX;
         float draggYgr = grY + this.offsetY;
         float percXgr = this.offsetX / grW;
         float percYgr = this.offsetY / grH;
         StencilUtil.initStencilToWrite();
         RenderUtils.drawAlphedRect((double)posX, (double)(posY + 16.0F), (double)(posX + w), (double)(posY + h), -1);
         StencilUtil.readStencilBuffer(1);
         int colHSB = -1;
         float hsbX = posX + 42.0F;
         float hsbY = posY + 45.0F;
         float hsbW = 64.0F;
         float hsbH = 6.0F;
         float alphaX = posX + 42.0F;
         float alphaY = posY + 42.0F - 14.0F;
         float alphaW = 64.0F;
         float alphaH = 6.0F;
         RenderUtils.drawLightContureRect(
            (double)hsbX, (double)hsbY, (double)(hsbX + hsbW + 0.25F), (double)(hsbY + hsbH), ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent)
         );
         float draggXhsb = hsbX + this.offsetX2;
         float percXhsb = this.offsetX2 / hsbW;
         float percXalpha = this.offsetX3 / alphaW;
         if (255.0F * scaledAlphaPercent > 26.0F) {
            Fonts.comfortaaBold_12
               .drawStringWithShadow(
                  "Hsb - " + String.format("%.2f", this.offsetX2 / hsbW * 360.0F),
                  (double)(hsbX + 1.0F),
                  (double)(hsbY - 7.0F),
                  ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent)
               );
         }

         for (int i = 0; i < 359; i++) {
            float hsb = (float)i / 360.0F;
            colHSB = Color.getHSBColor(hsb, 1.0F, 1.0F).getRGB();
            float pc = (float)i / 360.0F * hsbW;
            RenderUtils.drawAlphedRect(
               (double)(hsbX + pc), (double)hsbY, (double)(hsbX + pc + 0.5F), (double)(hsbY + hsbH), ColorUtils.swapAlpha(colHSB, 255.0F * scaledAlphaPercent)
            );
         }

         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            grX,
            grY,
            grX + grW,
            grY + grH,
            1.5F,
            0.5F,
            ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent),
            ColorUtils.swapAlpha(Color.getHSBColor(this.offsetX2 / hsbW, 1.0F, 1.0F).getRGB(), 255.0F * scaledAlphaPercent),
            ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * scaledAlphaPercent),
            ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * scaledAlphaPercent),
            false,
            true,
            true
         );
         RenderUtils.resetBlender();
         RenderUtils.drawAlphedRect(
            (double)(draggXgr - 1.5F),
            (double)(draggYgr - 1.5F),
            (double)(draggXgr + 1.5F),
            (double)(draggYgr + 1.5F),
            ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * scaledAlphaPercent)
         );
         RenderUtils.drawAlphedRect(
            (double)(draggXgr - 1.0F),
            (double)(draggYgr - 1.0F),
            (double)(draggXgr + 1.0F),
            (double)(draggYgr + 1.0F),
            ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent)
         );
         float hsbXCursor = hsbX + this.offsetX2;
         float hsbYCursor = hsbY + hsbH / 2.0F;
         RenderUtils.drawAlphedRect(
            (double)(hsbXCursor - 1.5F),
            (double)(hsbYCursor - 2.5F),
            (double)(hsbXCursor + 1.5F),
            (double)(hsbYCursor + 2.5F),
            ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * scaledAlphaPercent)
         );
         RenderUtils.drawAlphedRect(
            (double)(hsbXCursor - 1.0F),
            (double)(hsbYCursor - 2.0F),
            (double)(hsbXCursor + 1.0F),
            (double)(hsbYCursor + 2.0F),
            ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent)
         );
         RenderUtils.drawLightContureRect(
            (double)alphaX, (double)alphaY, (double)(alphaX + alphaW), (double)(alphaY + alphaH), ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent)
         );
         if (255.0F * scaledAlphaPercent > 26.0F) {
            Fonts.comfortaaBold_12
               .drawStringWithShadow(
                  "Alpha - " + (int)(percXalpha * 255.0F),
                  (double)(alphaX + 1.0F),
                  (double)(alphaY - 7.0F),
                  ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent)
               );
         }

         alphaW *= 2.0F;
         alphaH *= 2.0F;

         for (int i2 = 0; (float)i2 < alphaH; i2 += 4) {
            for (int i = 0; (float)i < alphaW; i += 4) {
               int colTest = (i2 != 0 && i2 != 8 || (float)i / alphaW * 16.0F == (float)((int)((float)i / alphaW * 16.0F)))
                     && ((float)i / alphaW * 16.0F != (float)((int)((float)i / alphaW * 16.0F)) || i2 == 0 || i2 == 8)
                  ? ColorUtils.getColor(200, 200, 200, 125)
                  : ColorUtils.getColor(125, 125, 125, 125);
               int colAlpha = ColorUtils.getOverallColorFrom(colTest, ColorUtils.swapAlpha(this.setting.color, 255.0F), (float)i / alphaW);
               RenderUtils.drawAlphedRect(
                  (double)(alphaX + (float)(i / 2)),
                  (double)(alphaY + (float)(i2 / 2)),
                  (double)(alphaX + (float)(i / 2) + 2.0F),
                  (double)(alphaY + (float)(i2 / 2) + 2.0F),
                  ColorUtils.swapAlpha(colAlpha, 255.0F * scaledAlphaPercent * scaledAlphaPercent * scaledAlphaPercent)
               );
            }
         }

         alphaW /= 2.0F;
         alphaH /= 2.0F;
         float alphaXCursor = alphaX + this.offsetX3;
         float alphaYCursor = alphaY + alphaH / 2.0F;
         RenderUtils.drawAlphedRect(
            (double)(alphaXCursor - 1.5F),
            (double)(alphaYCursor - 2.5F),
            (double)(alphaXCursor + 1.5F),
            (double)(alphaYCursor + 2.5F),
            ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * scaledAlphaPercent)
         );
         RenderUtils.drawAlphedRect(
            (double)(alphaXCursor - 1.0F),
            (double)(alphaYCursor - 2.0F),
            (double)(alphaXCursor + 1.0F),
            (double)(alphaYCursor + 2.0F),
            ColorUtils.swapAlpha(-1, 255.0F * scaledAlphaPercent)
         );
         StencilUtil.uninitStencilBuffer();
         if (this.dragginghsb) {
            this.dragginggr = false;
            this.draggingalpha = false;
            this.offsetX2 = MathUtils.clamp((float)mouseX - hsbX, 0.0F, hsbW);
         } else if (ColorUtils.getHueFromColor(this.setting.color) != 0 && !this.dragginggr) {
            this.offsetX2 = (float)ColorUtils.getHueFromColor(this.setting.color) / 360.0F * hsbW;
         }

         if (this.draggingalpha) {
            this.dragginggr = false;
            this.dragginghsb = false;
            this.offsetX3 = MathUtils.clamp((float)mouseX - alphaX, 0.0F, alphaW);
         } else {
            this.offsetX3 = (float)ColorUtils.getAlphaFromColor(this.setting.color) / 255.0F * alphaW;
         }

         int col = Color.getHSBColor(MathUtils.clamp(percXhsb, 0.0F, 0.999F), percXgr, 1.0F - percYgr).getRGB();
         col = ColorUtils.swapAlpha(col, 255.0F * percXalpha);
         if (col != 0 && Mouse.isButtonDown(0)) {
            this.setting.color = col;
         }
      }

      if (this.draggingalpha || this.dragginggr || this.dragginghsb) {
         this.updateSliderSounds();
      }
   }

   public void drawArrow(float xpos, float ypos, int color) {
      GL11.glPushMatrix();
      float ex = 4.5F;
      float xp = xpos - ex / 2.0F * (-this.arrow.anim / 90.0F);
      float yp = ypos + ex / 4.0F * (-this.arrow.anim / 90.0F);
      RenderUtils.customRotatedObject2D(xp, yp, ex / 2.0F, ex, (double)(this.arrow.anim - 90.0F));
      ArrayList<Vec2f> vec2fs = new ArrayList<>();
      vec2fs.add(new Vec2f(xp, yp));
      vec2fs.add(new Vec2f(xp - ex, yp + ex));
      vec2fs.add(new Vec2f(xp + ex, yp + ex));
      RenderUtils.drawSome(vec2fs, color);
      GL11.glPopMatrix();
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton == 0) {
         this.dragginggr = false;
         this.dragginghsb = false;
         this.draggingalpha = false;
      }
   }

   @Override
   public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(x, y, mouseX, mouseY, mouseButton);
      float posX = (float)(x + 4);
      float posY = (float)y + 1.5F;
      float w = this.getWidth() - 8.0F;
      float h = this.getHeight() - 1.0F;
      float grX = posX + 3.0F;
      float grY = posY + 19.0F;
      float grH = 34.0F;
      float grW = 34.0F;
      float hsbX = posX + 42.0F;
      float hsbY = posY + 45.0F;
      float hsbW = 64.0F;
      float hsbH = 6.0F;
      float alphaX = posX + 42.0F;
      float alphaY = posY + 42.0F - 14.0F;
      float alphaW = 64.0F;
      float alphaH = 6.0F;
      if (mouseButton == 0 && this.ishover(grX, grY, grX + grW, grY + grH, mouseX, mouseY)) {
         this.dragginggr = true;
      }

      if (mouseButton == 0 && this.ishover(hsbX, hsbY, hsbX + hsbW, hsbY + hsbH, mouseX, mouseY)) {
         this.dragginghsb = true;
      }

      if (mouseButton == 0 && this.ishover(alphaX, alphaY, alphaX + alphaW, alphaY + alphaH, mouseX, mouseY)) {
         this.draggingalpha = true;
      }

      if (mouseButton == 1 && this.ishover((float)(x + 5), (float)(y + 5), (float)x + this.getWidth() - 4.0F, (float)(y + 20), mouseX, mouseY)) {
         this.open = !this.open;
         if (this.open) {
            Client.clickGuiScreen
               .panels
               .stream()
               .filter(panel -> panel.open)
               .filter(panel -> panel.category == this.setting.module.category)
               .forEach(
                  panel -> panel.mods
                        .stream()
                        .filter(mod -> mod.open)
                        .filter(mod -> this.setting.module == mod.module)
                        .forEach(
                           module -> module.sets
                                 .stream()
                                 .map(Set::getHasColors)
                                 .filter(Objects::nonNull)
                                 .filter(colors -> colors != this)
                                 .filter(colors -> colors.open)
                                 .forEach(set -> {
                                    set.open = false;
                                    ClientTune.get.playGuiCheckOpenOrCloseSong(false);
                                 })
                        )
               );
         }

         ClientTune.get.playGuiCheckOpenOrCloseSong(this.open);
      }
   }

   @Override
   public float getWidth() {
      return 118.0F;
   }

   @Override
   public float getHeight() {
      return 17.0F + 40.0F * this.anim.getAnim() + 1.0F;
   }
}
