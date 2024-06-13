package ru.govno.client.clickgui;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec2f;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.module.settings.Settings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class Modes extends Set {
   boolean open = false;
   AnimationUtils anim = new AnimationUtils(0.0F, 0.0F, 0.15F);
   AnimationUtils changeAnim = new AnimationUtils(0.0F, 0.0F, 0.075F);
   AnimationUtils arrow = new AnimationUtils(0.0F, 0.0F, 0.15F);
   ModeSettings setting;
   boolean playClose;

   public Modes(ModeSettings setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void drawScreen(float x, float y, int step, int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(x, y, step, mouseX, mouseY, partialTicks);
      float scaledAlphaPercent = ClickGuiScreen.globalAlpha.anim / 255.0F;
      scaledAlphaPercent *= scaledAlphaPercent;
      this.anim.to = this.open ? 1.0F : 0.0F;
      if (MathUtils.getDifferenceOf(this.anim.getAnim(), this.anim.to) < 0.03F) {
         this.anim.setAnim(this.anim.to);
      }

      this.anim.speed = 0.15F;
      float toRot = this.open ? -90.0F : 0.0F;
      this.arrow.to = toRot;
      this.arrow.speed = MathUtils.getDifferenceOf(this.arrow.getAnim(), toRot) > 1.0 ? 0.1F : 0.2F;
      if (this.changeAnim.getAnim() >= 4.0F) {
         this.changeAnim.to = 0.0F;
      }

      if (this.playClose) {
         ClientTune.get.playGuiCheckOpenOrCloseSong(false);
         this.playClose = false;
      }

      float getHeight = this.getHeight();
      int cc = ColorUtils.getColor(0, 0, 0, (int)(110.0F * scaledAlphaPercent));
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + 4.0F, y + 2.0F, x + this.getWidth() - 4.0F, y + getHeight - 1.0F, 2.0F, 0.5F, cc, cc, cc, cc, false, true, true
      );
      RenderUtils.drawAlphedSideways(
         (double)(x + 4.0F),
         (double)(y + 1.5F),
         (double)(x + this.getWidth() - 4.0F),
         (double)(y + 2.5F),
         ColorUtils.swapAlpha(ClickGuiScreen.getColor((int)((float)(step + 120) + y / getHeight), this.setting.module.category), 255.0F * scaledAlphaPercent),
         ColorUtils.swapAlpha(ClickGuiScreen.getColor((int)((float)step + y / getHeight), this.setting.module.category), 255.0F * scaledAlphaPercent)
      );
      RenderUtils.fixShadows();
      GlStateManager.resetColor();
      if (scaledAlphaPercent * 255.0F >= 33.0F) {
         Fonts.comfortaaBold_12
            .drawString(
               this.setting.getName(),
               (double)(x + 8.0F),
               (double)(y + 8.5F),
               ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * scaledAlphaPercent)
            );
         if ((175.0F - 175.0F * MathUtils.clamp(this.changeAnim.anim / 3.0F, 0.0F, 1.0F)) * scaledAlphaPercent >= 33.0F) {
            Fonts.comfortaaBold_12
               .drawString(
                  this.setting.currentMode,
                  (double)(x + 11.0F + (float)Fonts.comfortaaBold_12.getStringWidth(this.setting.getName()) + this.changeAnim.anim),
                  (double)(y + 8.5F),
                  ColorUtils.swapAlpha(
                     -1, MathUtils.clamp((175.0F - 175.0F * MathUtils.clamp(this.changeAnim.anim / 3.0F, 0.0F, 1.0F)) * scaledAlphaPercent, 26.0F, 175.0F)
                  )
               );
         }
      }

      GlStateManager.enableAlpha();
      this.drawArrow(
         x + this.getWidth() - 12.0F,
         y + 6.0F,
         ColorUtils.swapAlpha(
            ColorUtils.getFixedWhiteColor(),
            MathUtils.clamp(
               26.0F * (this.ishover(x + 4.0F, y + 2.0F, x + this.getWidth() - 4.0F, y + getHeight + 2.0F, mouseX, mouseY) ? 1.75F : 1.0F) * scaledAlphaPercent
                  + 175.0F * this.anim.anim * scaledAlphaPercent,
               0.0F,
               255.0F
            )
         )
      );
      float height = 8.0F;
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRect(
         (double)(x + 5.0F), (double)(y + 17.0F), (double)(x + this.getWidth() - 5.0F), (double)(y + getHeight - 3.0F), ColorUtils.getColor(255, 255, 255, 60)
      );
      StencilUtil.readStencilBuffer(1);
      if (getHeight > 18.5F) {
         for (String mode : this.setting.modes) {
            CFontRenderer modedFont = mode.equalsIgnoreCase(this.setting.currentMode) ? Fonts.comfortaaBold_17 : Fonts.comfortaaBold_13;
            float width = (float)(modedFont.getStringWidth(mode) / 2);
            height += 13.0F;
            if (height < getHeight) {
               int rectCol = ColorUtils.swapAlpha(ColorUtils.getColor(100), 20.0F * scaledAlphaPercent * scaledAlphaPercent);
               int rectCol2 = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 16.0F * scaledAlphaPercent * scaledAlphaPercent);
               RenderUtils.drawAlphedRect(
                  (double)(x + 6.0F), (double)(y + height - 4.5F), (double)(x + this.getWidth() - 6.0F), (double)(y + height + 6.5F), rectCol
               );
               RenderUtils.drawAlphedRect(
                  (double)(x + 6.5F), (double)(y + height + 6.5F), (double)(x + this.getWidth() - 6.5F), (double)(y + height + 7.0F), rectCol2
               );
               RenderUtils.drawAlphedRect((double)(x + 6.0F), (double)(y + height - 5.0F), (double)(x + 6.5F), (double)(y + height - 4.5F), rectCol2);
               RenderUtils.drawAlphedRect(
                  (double)(x + this.getWidth() - 6.5F),
                  (double)(y + height - 5.0F),
                  (double)(x + this.getWidth() - 6.0F),
                  (double)(y + height - 4.5F),
                  rectCol2
               );
               if (scaledAlphaPercent * 255.0F >= 33.0F) {
                  if (mode.equalsIgnoreCase(this.setting.currentMode)) {
                     float xn = x + this.getWidth() / 2.0F - width + width * 2.0F / (float)mode.length() / 4.0F;
                     float yn = y + height - (float)(mode.equalsIgnoreCase(this.setting.currentMode) ? 1 : 0);
                     xn -= (float)modedFont.getStringWidth(mode);
                     float index = 0.0F;

                     for (char c : mode.toCharArray()) {
                        int col1 = ColorUtils.getOverallColorFrom(
                           ClickGuiScreen.getColor((int)index * 5, this.setting.module.category),
                           ClickGuiScreen.getColor((int)index * 5 + 180, this.setting.module.category),
                           index / (float)modedFont.getStringWidth(mode)
                        );
                        col1 = ColorUtils.swapAlpha(col1, (float)ColorUtils.getAlphaFromColor(col1) * scaledAlphaPercent * this.anim.anim);
                        if (ColorUtils.getAlphaFromColor(col1) >= 32) {
                           modedFont.drawString(
                              String.valueOf(c),
                              (double)((float)modedFont.getStringWidth(mode) + index + xn) + 0.5,
                              (double)yn + 0.5,
                              ColorUtils.swapDark(col1, ColorUtils.getBrightnessFromColor(col1) / 3.0F)
                           );
                           modedFont.drawString(String.valueOf(c), (double)((float)modedFont.getStringWidth(mode) + index + xn), (double)yn, col1);
                        }

                        index += (float)modedFont.getStringWidth(String.valueOf(c));
                     }
                  } else {
                     modedFont.drawStringWithShadow(
                        mode,
                        (double)(x + this.getWidth() / 2.0F - width),
                        (double)(y + height - (float)(mode.equalsIgnoreCase(this.setting.currentMode) ? 1 : 0)),
                        ColorUtils.swapAlpha(
                           ColorUtils.getColor(175), MathUtils.clamp(175.0F * (this.anim.anim / 2.0F + 0.5F) * scaledAlphaPercent, 26.0F, 255.0F)
                        )
                     );
                  }
               }
            }
         }
      }

      StencilUtil.uninitStencilBuffer();
   }

   @Override
   public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(x, y, mouseX, mouseY, mouseButton);
      if (this.ishover((float)(x + 4), (float)(y + 2), (float)x + this.getWidth() - 4.0F, (float)y + this.getHeight() + 2.0F, mouseX, mouseY)) {
         if (mouseButton == 0) {
            int curMode = -1;
            float height = 8.0F;
            if (this.setting.category == Settings.Category.String_Massive && this.open) {
               for (String mode : this.setting.modes) {
                  height += 13.0F;
                  if (this.ishover((float)(x + 6), (float)y + height - 2.0F, (float)x + this.getWidth() - 6.0F, (float)y + height + 10.0F, mouseX, mouseY)) {
                     curMode = (int)(height / 13.0F - 1.0F);
                  }
               }

               try {
                  if (!this.setting.currentMode.equalsIgnoreCase(this.setting.modes[curMode]) && curMode != -1) {
                     this.setting.currentMode = this.setting.modes[curMode];
                     ClientTune.get.playGuiScreenChangeModeSong();
                     this.changeAnim.to = 4.5F;
                     if (this.setting.modes.length < 6) {
                        this.playClose = true;
                        this.open = false;
                     }
                  }
               } catch (Exception var12) {
                  var12.printStackTrace();
               }
            }
         }

         if (mouseButton == 1
            && this.ishover((float)(x + 4), (float)(y + 4), (float)x + this.getWidth() - 5.0F, (float)(y + (this.open ? 18 : 20)), mouseX, mouseY)) {
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
                                    .map(Set::getHasModes)
                                    .filter(Objects::nonNull)
                                    .filter(modes -> modes != this)
                                    .filter(modes -> modes.open)
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
   public float getWidth() {
      return 118.0F;
   }

   @Override
   public float getHeight() {
      return 18.0F + 13.0F * this.anim.getAnim() * (float)this.setting.modes.length;
   }
}
