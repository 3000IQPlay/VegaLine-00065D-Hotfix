package ru.govno.client.utils;

import net.minecraft.client.Minecraft;

public class GCDCalcHelper {
   static Minecraft mc = Minecraft.getMinecraft();

   public static float getFixedRotation(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getGCDValue() {
      return (float)((double)getGCD() * 0.15);
   }

   public static float getGCD() {
      float f1;
      return (f1 = (float)((double)mc.gameSettings.mouseSensitivity * 0.6 + 0.2)) * f1 * f1 * 8.0F;
   }

   public static float getDeltaMouse(float delta) {
      return (float)Math.round(delta / getGCDValue());
   }
}
