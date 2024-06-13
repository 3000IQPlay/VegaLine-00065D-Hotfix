package ru.govno.client.utils.Render;

import com.ibm.icu.text.NumberFormat;
import java.awt.Color;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import ru.govno.client.utils.Math.MathUtils;

public class ColorUtils {
   public static int getColor(Color color) {
      return getColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
   }

   public static Color getJavaColor(int color) {
      return new Color(getRedFromColor(color), getGreenFromColor(color), getBlueFromColor(color), getAlphaFromColor(color));
   }

   public static Color getColorWithOpacity(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   public static Color injectAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   public static Color TwoColor(Color cl1, Color cl2, double speed) {
      double thing = speed / 4.0 % 1.0;
      float val = MathUtils.clamp((float)Math.sin((Math.PI * 6) * thing) / 2.0F + 0.5F, 0.0F, 1.0F);
      return new Color(
         MathUtils.lerp((float)cl1.getRed() / 255.0F, (float)cl2.getRed() / 255.0F, val),
         MathUtils.lerp((float)cl1.getGreen() / 255.0F, (float)cl2.getGreen() / 255.0F, val),
         MathUtils.lerp((float)cl1.getBlue() / 255.0F, (float)cl2.getBlue() / 255.0F, val),
         1.0F
      );
   }

   public static int rainbow(int delay, long index) {
      double rainbowState = Math.ceil((double)(System.currentTimeMillis() + index + (long)delay)) / 3.0;
      double var5;
      return Color.getHSBColor((float)((var5 = rainbowState % 360.0) / 360.0), 0.4F, 1.0F).getRGB();
   }

   public static int getFixedWhiteColor() {
      return -65537;
   }

   public static int rainbowWithDark(int delay, long index, float dark) {
      double rainbowState = Math.ceil((double)(System.currentTimeMillis() + index + (long)delay)) / 3.0;
      double var6;
      return Color.getHSBColor((float)((var6 = rainbowState % 360.0) / 360.0), 0.4F, dark).getRGB();
   }

   public static int Flicker2(int delay, long index) {
      double rainbowState = Math.ceil((double)(System.currentTimeMillis() + index + (long)delay)) / 5.0;
      double var5;
      return Color.getHSBColor((float)((var5 = rainbowState % 30.0) / 30.0), 1.0F, 1.0F).getRGB();
   }

   public static int rainbowLT(int delay, long index) {
      double rainbowState = Math.ceil((double)(System.currentTimeMillis() + index + (long)delay)) / 3.0;
      double var5;
      return Color.getHSBColor((float)((var5 = rainbowState % 248.0) / 248.0), 0.5F, 0.6F).getRGB();
   }

   public static int rainbowGui(int delay, long index) {
      double rainbowState = Math.ceil((double)(System.currentTimeMillis() + index + (long)delay)) / 3.0;
      double var5;
      return Color.getHSBColor((float)((var5 = rainbowState % 360.0) / 360.0), 0.8F, 1.0F).getRGB();
   }

   public static int rainbowGui2(int delay, long index) {
      double rainbowState = Math.ceil((double)(System.currentTimeMillis() + index + (long)delay)) / 3.0;
      double var5;
      return Color.getHSBColor((float)((var5 = rainbowState % 360.0) / 360.0), 0.7F, 1.0F).getRGB();
   }

   public static int rainbowGui2WithDark(int delay, long index, float dark) {
      double rainbowState = Math.ceil((double)(System.currentTimeMillis() + index + (long)delay)) / 3.0;
      double var6;
      return Color.getHSBColor((float)((var6 = rainbowState % 360.0) / 360.0), 0.7F, dark).getRGB();
   }

   public static Color fade(Color color) {
      return fade(color, 2, 100);
   }

   public static int color(int n, int n2, int n3, int n4) {
      int var4 = 255;
      return new Color(n, n2, n3, var4).getRGB();
   }

   public static int getRandomColor() {
      char[] letters = "012345678".toCharArray();
      String color = "0x";

      for (int i = 0; i < 6; i++) {
         color = color + letters[new Random().nextInt(letters.length)];
      }

      return Integer.decode(color);
   }

   public static int getRedFromColor(int color) {
      return color >> 16 & 0xFF;
   }

   public static int getGreenFromColor(int color) {
      return color >> 8 & 0xFF;
   }

   public static int getBlueFromColor(int color) {
      return color & 0xFF;
   }

   public static int getAlphaFromColor(int color) {
      return color >> 24 & 0xFF;
   }

   public static float getGLRedFromColor(int color) {
      return (float)(color >> 16 & 0xFF) / 255.0F;
   }

   public static float getGLGreenFromColor(int color) {
      return (float)(color >> 8 & 0xFF) / 255.0F;
   }

   public static float getGLBlueFromColor(int color) {
      return (float)(color & 0xFF) / 255.0F;
   }

   public static float getGLAlphaFromColor(int color) {
      return (float)(color >> 24 & 0xFF) / 255.0F;
   }

   public static int getHue(int red, int green, int blue) {
      float min = (float)Math.min(Math.min(red, green), blue);
      float max = (float)Math.max(Math.max(red, green), blue);
      if (min == max) {
         return 0;
      } else {
         float hue = 0.0F;
         if (max == (float)red) {
            hue = (float)(green - blue) / (max - min);
         } else if (max == (float)green) {
            hue = 2.0F + (float)(blue - red) / (max - min);
         } else {
            hue = 4.0F + (float)(red - green) / (max - min);
         }

         hue *= 60.0F;
         if (hue < 0.0F) {
            hue += 360.0F;
         }

         return Math.round(hue);
      }
   }

   public static int getHueFromColor(int color) {
      return getHue(getRedFromColor(color), getGreenFromColor(color), getBlueFromColor(color));
   }

   public static float getBrightnessFromColor(int color) {
      float[] athsb = Color.RGBtoHSB(getRedFromColor(color), getGreenFromColor(color), getBlueFromColor(color), null);
      return athsb[2];
   }

   public static float getSaturateFromColor(int color) {
      float[] athsb = Color.RGBtoHSB(getRedFromColor(color), getGreenFromColor(color), getBlueFromColor(color), null);
      return athsb[1];
   }

   public static int getOverallColorFrom(int color1, int color2) {
      int red1 = getRedFromColor(color1);
      int green1 = getGreenFromColor(color1);
      int blue1 = getBlueFromColor(color1);
      int alpha1 = getAlphaFromColor(color1);
      int red2 = getRedFromColor(color2);
      int green2 = getGreenFromColor(color2);
      int blue2 = getBlueFromColor(color2);
      int alpha2 = getAlphaFromColor(color2);
      int finalRed = (red1 + red2) / 2;
      int finalGreen = (green1 + green2) / 2;
      int finalBlue = (blue1 + blue2) / 2;
      int finalAlpha = (alpha1 + alpha2) / 2;
      return getColor(finalRed, finalGreen, finalBlue, finalAlpha);
   }

   public static int getOverallColorFrom(int color1, int color2, float percentTo2) {
      int finalRed = (int)MathUtils.lerp((float)(color1 >> 16 & 0xFF), (float)(color2 >> 16 & 0xFF), percentTo2);
      int finalGreen = (int)MathUtils.lerp((float)(color1 >> 8 & 0xFF), (float)(color2 >> 8 & 0xFF), percentTo2);
      int finalBlue = (int)MathUtils.lerp((float)(color1 & 0xFF), (float)(color2 & 0xFF), percentTo2);
      int finalAlpha = (int)MathUtils.lerp((float)(color1 >> 24 & 0xFF), (float)(color2 >> 24 & 0xFF), percentTo2);
      return getColor(finalRed, finalGreen, finalBlue, finalAlpha);
   }

   public static int fadeColor(int color1, int color2, float speed) {
      float cr1 = (float)(color1 >> 16 & 0xFF);
      float cg1 = (float)(color1 >> 8 & 0xFF);
      float cb1 = (float)(color1 & 0xFF);
      float ca1 = (float)(color1 >> 24 & 0xFF);
      float cr2 = (float)(color2 >> 16 & 0xFF);
      float cg2 = (float)(color2 >> 8 & 0xFF);
      float cb2 = (float)(color2 & 0xFF);
      float ca2 = (float)(color2 >> 24 & 0xFF);
      return TwoColoreffect(
         (int)cr1,
         (int)cg1,
         (int)cb1,
         (int)ca1,
         (int)cr2,
         (int)cg2,
         (int)cb2,
         (int)ca2,
         (double)Math.abs(System.currentTimeMillis() / 4L) / 100.1275 * (double)speed
      );
   }

   public static int fadeColor(int color1, int color2, float speed, int index) {
      float cr1 = (float)(color1 >> 16 & 0xFF);
      float cg1 = (float)(color1 >> 8 & 0xFF);
      float cb1 = (float)(color1 & 0xFF);
      float ca1 = (float)(color1 >> 24 & 0xFF);
      float cr2 = (float)(color2 >> 16 & 0xFF);
      float cg2 = (float)(color2 >> 8 & 0xFF);
      float cb2 = (float)(color2 & 0xFF);
      float ca2 = (float)(color2 >> 24 & 0xFF);
      return TwoColoreffect(
         (int)cr1,
         (int)cg1,
         (int)cb1,
         (int)ca1,
         (int)cr2,
         (int)cg2,
         (int)cb2,
         (int)ca2,
         (double)Math.abs(System.currentTimeMillis() / 4L + (long)index) / 100.1275 * (double)speed
      );
   }

   public static int fadeColorIndexed(int color1, int color2, float speed, int index) {
      float cr1 = (float)(color1 >> 16 & 0xFF);
      float cg1 = (float)(color1 >> 8 & 0xFF);
      float cb1 = (float)(color1 & 0xFF);
      float ca1 = (float)(color1 >> 24 & 0xFF);
      float cr2 = (float)(color2 >> 16 & 0xFF);
      float cg2 = (float)(color2 >> 8 & 0xFF);
      float cb2 = (float)(color2 & 0xFF);
      float ca2 = (float)(color2 >> 24 & 0xFF);
      return TwoColoreffect(
         (int)cr1,
         (int)cg1,
         (int)cb1,
         (int)ca1,
         (int)cr2,
         (int)cg2,
         (int)cb2,
         (int)ca2,
         (double)Math.abs(System.currentTimeMillis() / 4L + (long)index) / 100.1275 * (double)speed
      );
   }

   public static int reAlpha(int color, float alpha) {
      Color c = new Color(color);
      float r = 0.003921569F * (float)c.getRed();
      float g = 0.003921569F * (float)c.getGreen();
      float b = 0.003921569F * (float)c.getBlue();
      return new Color(r, g, b, alpha).getRGB();
   }

   public static int swapAlpha(int color, float alpha) {
      int f = color >> 16 & 0xFF;
      int f1 = color >> 8 & 0xFF;
      int f2 = color & 0xFF;
      return getColor(f, f1, f2, (int)alpha);
   }

   public static int swapDark(int color, float dark) {
      int f = color >> 16 & 0xFF;
      int f1 = color >> 8 & 0xFF;
      int f2 = color & 0xFF;
      return getColor((int)((float)f * dark), (int)((float)f1 * dark), (int)((float)f2 * dark));
   }

   public static int toDark(int color, float dark) {
      return getColor(
         (int)((float)getRedFromColor(color) * dark),
         (int)((float)getGreenFromColor(color) * dark),
         (int)((float)getBlueFromColor(color) * dark),
         getAlphaFromColor(color)
      );
   }

   public static Color getGradientOffset(Color color1, Color color2, double offset) {
      if (offset > 1.0) {
         double left = offset % 1.0;
         int off = (int)offset;
         offset = off % 2 == 0 ? left : 1.0 - left;
      }

      double inverse_percent = 1.0 - offset;
      int redPart = (int)((double)color1.getRed() * inverse_percent + (double)color2.getRed() * offset);
      int greenPart = (int)((double)color1.getGreen() * inverse_percent + (double)color2.getGreen() * offset);
      int bluePart = (int)((double)color1.getBlue() * inverse_percent + (double)color2.getBlue() * offset);
      return new Color(redPart, greenPart, bluePart);
   }

   public static int getColor1(int brightness) {
      return getColor(brightness, brightness, brightness, 255);
   }

   public static int getColor(int red, int green, int blue) {
      return getColor(red, green, blue, 255);
   }

   public static int getColor(int red, int green, int blue, int alpha) {
      int color = 0;
      color |= alpha << 24;
      color |= red << 16;
      color |= green << 8;
      int var8;
      return var8 = color | blue;
   }

   public static int getColor(int red, int green, int blue, float alpha) {
      int color = 0;
      color |= (int)alpha << 24;
      color |= red << 16;
      color |= green << 8;
      int var8;
      return var8 = color | blue;
   }

   public static int getColor(int brightness) {
      return getColor(brightness, brightness, brightness, 255);
   }

   public static int getColor(int brightness, int alpha) {
      return getColor(brightness, brightness, brightness, alpha);
   }

   public static Color fade(Color color, int index, int count) {
      float[] hsb = new float[3];
      Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
      float brightness = Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0F + (float)index / (float)count * 2.0F) % 2.0F - 1.0F);
      brightness = 0.5F + 0.5F * brightness;
      hsb[2] = brightness % 2.0F;
      return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
   }

   public static Color getHealthColor(EntityLivingBase entityLivingBase) {
      float health = entityLivingBase.getHealth();
      float[] fractions = new float[]{0.0F, 0.15F, 0.55F, 0.7F, 0.9F};
      Color[] colors = new Color[]{new Color(133, 0, 0), Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN};
      float progress = health / entityLivingBase.getMaxHealth();
      return health >= 0.0F ? blendColors(fractions, colors, progress).brighter() : colors[0];
   }

   public static Color getProgressColor(float val) {
      float[] fractions = new float[]{0.0F, 0.15F, 0.55F, 0.7F, 0.9F};
      Color[] colors = new Color[]{new Color(133, 0, 0), Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN};
      return val >= 0.0F ? blendColors(fractions, colors, val).brighter() : colors[0];
   }

   public static Color blendColors(float[] fractions, Color[] colors, float progress) {
      if (fractions == null) {
         throw new IllegalArgumentException("Fractions can't be null");
      } else if (colors == null) {
         throw new IllegalArgumentException("Colours can't be null");
      } else if (fractions.length != colors.length) {
         throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
      } else {
         int[] indicies = getFractionIndicies(fractions, progress);
         float[] range = new float[]{fractions[indicies[0]], fractions[indicies[1]]};
         Color[] colorRange = new Color[]{colors[indicies[0]], colors[indicies[1]]};
         float max = range[1] - range[0];
         float value = progress - range[0];
         float weight = value / max;
         return blend(colorRange[0], colorRange[1], (double)(1.0F - weight));
      }
   }

   public static int[] getFractionIndicies(float[] fractions, float progress) {
      int[] range = new int[2];
      int startPoint = 0;

      while (startPoint < fractions.length && fractions[startPoint] <= progress) {
         startPoint++;
      }

      if (startPoint >= fractions.length) {
         startPoint = fractions.length - 1;
      }

      range[0] = startPoint - 1;
      range[1] = startPoint;
      return range;
   }

   public static Color blend(Color color1, Color color2, double ratio) {
      float r = (float)ratio;
      float ir = 1.0F - r;
      float[] rgb1 = new float[3];
      float[] rgb2 = new float[3];
      color1.getColorComponents(rgb1);
      color2.getColorComponents(rgb2);
      float red = rgb1[0] * r + rgb2[0] * ir;
      float green = rgb1[1] * r + rgb2[1] * ir;
      float blue = rgb1[2] * r + rgb2[2] * ir;
      if (red < 0.0F) {
         red = 0.0F;
      } else if (red > 255.0F) {
         red = 255.0F;
      }

      if (green < 0.0F) {
         green = 0.0F;
      } else if (green > 255.0F) {
         green = 255.0F;
      }

      if (blue < 0.0F) {
         blue = 0.0F;
      } else if (blue > 255.0F) {
         blue = 255.0F;
      }

      Color color = null;

      try {
         color = new Color(red, green, blue);
      } catch (IllegalArgumentException var14) {
         NumberFormat var13 = NumberFormat.getNumberInstance();
      }

      return color;
   }

   public static int astolfo(int delay, float offset) {
      float speed = 3000.0F;
      float hue = Math.abs((float)(System.currentTimeMillis() % (long)delay) + -offset / 21.0F * 2.0F);

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.5) {
         hue = 0.5F - (hue - 0.5F);
      }

      float var5;
      return Color.HSBtoRGB(var5 = hue + 0.5F, 0.5F, 1.0F);
   }

   public static int Yellowastolfo(int delay, float offset) {
      float speed = 2900.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + ((float)(-delay) - offset) * 9.0F;

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.6) {
         hue = 0.6F - (hue - 0.6F);
      }

      float var5;
      return Color.HSBtoRGB(var5 = hue + 0.6F, 0.5F, 1.0F);
   }

   public static int YellowastolfoLT(int delay, float offset) {
      float speed = 1450.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + ((float)(-delay) - offset) * 9.0F;

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.6) {
         hue = 0.6F - (hue - 0.6F);
      }

      float var5;
      return Color.HSBtoRGB(var5 = hue + 0.6F, 0.5F, 1.0F);
   }

   public static Color Yellowastolfo1(int delay, float offset) {
      float speed = 2900.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + ((float)delay - offset) * 9.0F;

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.6) {
         hue = 0.6F - (hue - 0.6F);
      }

      float var5;
      return new Color(var5 = hue + 0.6F, 0.5F, 1.0F);
   }

   public static Color Yellowastolfo2(int delay, float offset) {
      float speed = 2900.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + ((float)delay - offset) * 9.0F;

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.6) {
         hue = 0.6F - (hue - 0.6F);
      }

      float var5;
      return new Color(var5 = hue + 0.6F, 0.5F, 1.0F);
   }

   public static Color TwoColoreffect(Color cl1, Color cl2, double speed) {
      double thing = speed / 4.0 % 1.0;
      float val = MathUtils.clamp((float)Math.sin((Math.PI * 6) * thing) / 2.0F + 0.5F, 0.0F, 1.0F);
      return new Color(
         MathUtils.lerp((float)cl1.getRed() / 255.0F, (float)cl2.getRed() / 255.0F, val),
         MathUtils.lerp((float)cl1.getGreen() / 255.0F, (float)cl2.getGreen() / 255.0F, val),
         MathUtils.lerp((float)cl1.getBlue() / 255.0F, (float)cl2.getBlue() / 255.0F, val)
      );
   }

   public static int TwoColoreffect(int cl1, int cl2, double speed) {
      double thing = speed / 4.0 % 1.0;
      float val = MathUtils.clamp((float)Math.sin((Math.PI * 6) * thing) / 2.0F + 0.5F, 0.0F, 1.0F);
      return getColor(
         (int)MathUtils.lerp((float)cl1, (float)cl2, val), (int)MathUtils.lerp((float)cl1, (float)cl2, val), (int)MathUtils.lerp((float)cl1, (float)cl2, val)
      );
   }

   public static int TwoColoreffect(int cl1r, int cl1g, int cl1b, int cl2r, int cl2g, int cl2b, double speed) {
      double thing = speed / 4.0 % 1.0;
      float val = MathUtils.clamp((float)Math.sin((Math.PI * 6) * thing) / 2.0F + 0.5F, 0.0F, 1.0F);
      return getColor(
         (int)MathUtils.lerp((float)cl1r, (float)cl2r, val),
         (int)MathUtils.lerp((float)cl1g, (float)cl2g, val),
         (int)MathUtils.lerp((float)cl1b, (float)cl2b, val)
      );
   }

   public static int TwoColoreffect(int cl1r, int cl1g, int cl1b, int cl2r, int cl2g, int cl2b, int alpha, double speed) {
      double thing = speed / 4.0 % 1.0;
      float val = MathUtils.clamp((float)Math.sin((Math.PI * 6) * thing) / 2.0F + 0.5F, 0.0F, 1.0F);
      return getColor(
         (int)MathUtils.lerp((float)cl1r, (float)cl2r, val),
         (int)MathUtils.lerp((float)cl1g, (float)cl2g, val),
         (int)MathUtils.lerp((float)cl1b, (float)cl2b, val),
         alpha
      );
   }

   public static int TwoColoreffect(int cl1r, int cl1g, int cl1b, int cl1a, int cl2r, int cl2g, int cl2b, int cl2a, double speed) {
      double thing = speed / 4.0 % 1.0;
      float val = MathUtils.clamp((float)Math.sin((Math.PI * 6) * thing) / 2.0F + 0.5F, 0.0F, 1.0F);
      return getColor(
         (int)MathUtils.lerp((float)cl1r, (float)cl2r, val),
         (int)MathUtils.lerp((float)cl1g, (float)cl2g, val),
         (int)MathUtils.lerp((float)cl1b, (float)cl2b, val),
         (int)MathUtils.lerp((float)cl1a, (float)cl2a, val)
      );
   }

   public static int astolfoColors(int yOffset, int yTotal) {
      float speed = 2900.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9);

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.5) {
         hue = 0.5F - (hue - 0.5F);
      }

      float var5;
      return Color.HSBtoRGB(var5 = hue + 0.5F, 0.5F, 1.0F);
   }

   public static int astolfoNew(int delay, float offset) {
      float speed = 2900.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + ((float)delay - offset) * 9.0F;

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.5) {
         hue = 0.5F - (hue - 0.5F);
      }

      float var5;
      return Color.HSBtoRGB(var5 = hue + 0.5F, 0.5F, 1.0F);
   }

   public static int astolfoColorsCool(int yOffset, int yTotal) {
      float speed = 1450.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9);

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.5) {
         hue = 0.5F - (hue - 0.5F);
      }

      float var5;
      return Color.HSBtoRGB(var5 = hue + 0.5F, 0.6F, 1.0F);
   }

   public static int astolfoColorsCoolWithDark(int yOffset, int yTotal, float dark) {
      float speed = 1450.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9);

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.5) {
         hue = 0.5F - (hue - 0.5F);
      }

      float var6;
      return Color.HSBtoRGB(var6 = hue + 0.5F, 0.67F, dark);
   }

   public static int getTeamColor(Entity entityIn) {
      int i = -1;
      return entityIn.getDisplayName().getUnformattedText().equalsIgnoreCase("пїЅf[пїЅcRпїЅf]пїЅc" + entityIn.getName())
         ? getColor(new Color(255, 60, 60))
         : (
            entityIn.getDisplayName().getUnformattedText().equalsIgnoreCase("пїЅf[пїЅ9BпїЅf]пїЅ9" + entityIn.getName())
               ? getColor(new Color(60, 60, 255))
               : (
                  entityIn.getDisplayName().getUnformattedText().equalsIgnoreCase("пїЅf[пїЅeYпїЅf]пїЅe" + entityIn.getName())
                     ? getColor(new Color(255, 255, 60))
                     : (
                        entityIn.getDisplayName().getUnformattedText().equalsIgnoreCase("пїЅf[пїЅaGпїЅf]пїЅa" + entityIn.getName())
                           ? getColor(new Color(60, 255, 60))
                           : getColor(new Color(255, 255, 255))
                     )
               )
         );
   }

   public static Color astolfoColors1(int yOffset, int yTotal) {
      float speed = 2900.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9);

      while (hue > speed) {
         hue -= speed;
      }

      if ((double)(hue = hue / speed) > 0.5) {
         hue = 0.5F - (hue - 0.5F);
      }

      float var5;
      return new Color(var5 = hue + 0.5F, 0.5F, 1.0F);
   }

   public static Color rainbowCol(int delay, float saturation, float brightness) {
      double rainbow = Math.ceil((double)((System.currentTimeMillis() + (long)delay) / 12L));
      double var5;
      return Color.getHSBColor((float)((var5 = rainbow % 360.0) / 360.0), saturation, brightness);
   }

   public static Color Flicker(int delay, float saturation, float brightness) {
      double rainbow = Math.ceil((double)((System.currentTimeMillis() + (long)delay) / 10L));
      double var5;
      return Color.getHSBColor((float)((var5 = rainbow % 10.0) / 10.0), saturation, brightness);
   }

   public static Color Rgbdel(int delay, float saturation, float brightness, float d) {
      double rainbow = Math.ceil((double)(System.currentTimeMillis() + (long)d) / 128.0);
      double var6;
      return Color.getHSBColor((float)((var6 = rainbow % ((double)d * 3.6)) / (double)d * 3.6), saturation, brightness);
   }

   public static Color rainbowColA(int delay, float saturation, float brightness) {
      double rainbow = Math.ceil((double)((System.currentTimeMillis() + (long)delay) / 64L));
      double var5;
      return Color.getHSBColor((float)((var5 = rainbow % 360.0) / 360.0), saturation, brightness);
   }

   public static int rainbowNew(int delay, float saturation, float brightness) {
      double rainbow = Math.ceil((double)((System.currentTimeMillis() + (long)delay) / 16L));
      double var5;
      return Color.getHSBColor((float)((var5 = rainbow % 360.0) / 360.0), saturation, brightness).getRGB();
   }

   public static int reverseColor(int color, boolean reverseAlpha) {
      float hue = (float)getHue(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF) / 360.0F;
      float bright = getBrightnessFromColor(color);
      float saturation = getSaturateFromColor(color);
      int color1 = Color.HSBtoRGB((hue + 0.5F) % 1.0F, saturation, bright);
      return swapAlpha(color1, reverseAlpha ? (float)(255 - color1 >> 24 & 0xFF) : (float)(color1 >> 24 & 0xFF));
   }

   public static int rebrightColor(int color, float brightMul) {
      float hue = (float)getHue(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF) / 360.0F;
      float bright = getBrightnessFromColor(color) * brightMul;
      float saturation = getSaturateFromColor(color);
      bright = bright > 1.0F ? 1.0F : (bright < 0.0F ? 0.0F : bright);
      return swapAlpha(Color.HSBtoRGB(hue, saturation, bright), (float)getAlphaFromColor(color));
   }
}
