package ru.govno.client.module.modules;

import java.util.ArrayList;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Notifications extends Module {
   public static Module get;
   ModeSettings Mode;

   public Notifications() {
      super("Notifications", 0, Module.Category.RENDER);
      get = this;
      this.settings.add(this.Mode = new ModeSettings("Mode", "Colored", this, new String[]{"Colored", "Dark"}));
   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      drawNotifyS();
   }

   public static void drawNotifyS() {
      if (Notifications.Notify.notifications.size() != 0) {
         int yDist = 1;
         String mode = get.currentMode("Mode");

         for (Notifications.Notification notification : Notifications.Notify.notifications) {
            Notifications.Notify.draw(notification, yDist, mode);
            yDist++;
         }

         Notifications.Notify.notifications.removeIf(huy -> System.currentTimeMillis() - huy.getTime() >= huy.max_time);
      }
   }

   static class Notification {
      private final AnimationUtils animY = new AnimationUtils(1.0F, 1.1F, 0.075F);
      private final AnimationUtils animX = new AnimationUtils(0.0F, 1.0F, 0.075F);
      private final String message;
      private final long time;
      private final long max_time;
      Notifications.type type;

      public Notification(String message, long max_time, Notifications.type type) {
         this.max_time = max_time;
         this.message = message;
         this.time = System.currentTimeMillis();
         this.type = type;
      }

      public long getTime() {
         return this.time;
      }

      public int getColorize() {
         return this.type.color;
      }

      public long getMax_Time() {
         return this.max_time;
      }

      public String getMessage() {
         return this.message;
      }
   }

   public class Notify {
      public static ArrayList<Notifications.Notification> notifications = new ArrayList<>();

      public static void spawnNotify(String message, Notifications.type usedtype) {
         long maxTime = (long)(1500 * (usedtype == Notifications.type.STAFF ? 3 : 1));
         notifications.add(new Notifications.Notification(message, maxTime, usedtype));
      }

      static void drawIcon(Notifications.type type, float alpha, float x, float y, float size, String mode) {
         if (mode.equalsIgnoreCase("Dark")) {
            int c1 = ColorUtils.swapAlpha(type.color, (float)ColorUtils.getAlphaFromColor(type.color));
            int c2 = ColorUtils.swapAlpha(type.color, (float)ColorUtils.getAlphaFromColor(type.color) / 4.0F);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               x - size / 2.0F, y - size / 2.0F, x + size / 2.0F, y + size / 2.0F, 0.0F, 3.0F * alpha, c2, c2, c2, c2, true, false, true
            );
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               x - size / 2.0F, y - size / 2.0F, x + size / 2.0F, y + size / 2.0F, 0.0F, 0.0F, c1, c1, c1, c1, true, true, false
            );
         } else {
            ResourceLocation icon = new ResourceLocation("vegaline/modules/notifications/icons/" + type.icon.toLowerCase() + ".png");
            if (icon != null) {
               RenderUtils.drawImageWithAlpha(icon, x, y, size, size, ColorUtils.getFixedWhiteColor(), (int)(255.0F * alpha));
            }
         }
      }

      static void draw(Notifications.Notification notify, int index, String mode) {
         notify.animY.speed = 0.15F;
         notify.animX.speed = 0.075F;
         boolean isDark = mode.equalsIgnoreCase("Dark");
         ScaledResolution sr = new ScaledResolution(Module.mc);
         CFontRenderer font = isDark ? Fonts.comfortaaBold_16 : Fonts.noise_16;
         Notifications.type type = notify.type;
         String text = notify.getMessage();
         String massage = notify.type.icon + (notify.type.equals(Notifications.type.STAFF) ? "" : "d");
         int colorize = notify.getColorize();
         int colorize2 = ColorUtils.getColor(
            (int)MathUtils.clamp((float)ColorUtils.getRedFromColor(notify.getColorize()) * 1.75F, 0.0F, 255.0F),
            (int)MathUtils.clamp((float)ColorUtils.getGreenFromColor(notify.getColorize()) * 1.75F, 0.0F, 255.0F),
            (int)MathUtils.clamp((float)ColorUtils.getBlueFromColor(notify.getColorize()) * 1.75F, 0.0F, 255.0F)
         );
         float max_time = (float)notify.getMax_Time();
         float time = (float)(System.currentTimeMillis() - notify.getTime());
         String surf = isDark ? " " : " | ";
         String surf2 = isDark ? "§r§f §r" : "§r§f | §r";
         if (time < 50.0F) {
            notify.animY.setAnim((float)index);
         }

         if (time + 100.0F > max_time) {
            notify.animX.speed = 0.125F;
            notify.animX.to = 0.0F;
            if (notify.animY.getAnim() == 1.0F) {
               notify.animY.setAnim((float)index - 1.5F);
            }
         } else {
            notify.animX.to = 1.0F;
            if (index - 1 >= 0) {
               notify.animY.to = (float)(index - 1);
            }
         }

         float width = (isDark ? 18.5F : 24.0F) + (float)font.getStringWidth(text + surf + massage);
         float w = width * notify.animX.getAnim();
         float hStep = (float)(isDark ? 17 : 20) * notify.animY.getAnim();
         float expX = 3.5F;
         float expY = 4.5F;
         float x = (float)sr.getScaledWidth() - w - 3.5F;
         float y = (float)(sr.getScaledHeight() - 16) - 4.5F - hStep;
         float x2 = (float)sr.getScaledWidth() - 3.5F + width - width * notify.animX.getAnim();
         float y2 = (float)sr.getScaledHeight() - 4.5F - hStep;
         float extenderOut = 0.0F;
         if ((double)(time / max_time) > 0.8) {
            extenderOut = (time / max_time - 0.8F) * (float)(isDark ? 10 : 80);
         }

         float alphaPercent = notify.animX.getAnim();
         int c1 = ColorUtils.swapAlpha(isDark ? Integer.MIN_VALUE : colorize, (float)(isDark ? 205 : 80) * alphaPercent);
         int c3 = ColorUtils.swapAlpha(isDark ? Integer.MIN_VALUE : colorize2, (float)(isDark ? 90 : 6) * alphaPercent);
         GL11.glTranslated((double)(x - extenderOut), (double)y, 0.0);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            0.5F, 0.5F, x2 - x - 0.5F, y2 - y - 0.5F, isDark ? 2.0F : 4.0F, isDark ? 1.0F : 2.5F, c1, c3, c3, c1, false, true, true
         );
         RenderUtils.resetBlender();
         float extX = isDark ? 7.0F : 0.5F;
         float extY = isDark ? 8.0F : 0.5F;
         float size = isDark ? 3.0F : 16.0F;
         drawIcon(type, alphaPercent, extX, extY, size, mode);
         font.drawStringWithShadow(
            "§f" + text + surf2 + massage, isDark ? 13.0 : 19.0, isDark ? 5.5 : 5.0, ColorUtils.swapAlpha(colorize2, 255.0F * alphaPercent)
         );
         GL11.glTranslated((double)(-x + extenderOut), (double)(-y), 0.0);
      }
   }

   public static enum type {
      ENABLE(ColorUtils.getColor(32, 143, 50), "Enable"),
      DISABLE(ColorUtils.getColor(175, 35, 37), "Disable"),
      STAFF(ColorUtils.getColor(92, 142, 255), "Staff"),
      FADD(ColorUtils.getColor(190, 250, 140), "Friend add"),
      FDEL(ColorUtils.getColor(250, 140, 140), "Friend remove");

      private final int color;
      private final String icon;

      private type(int color, String icon) {
         this.color = color;
         this.icon = icon;
      }
   }
}
