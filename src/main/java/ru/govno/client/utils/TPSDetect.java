package ru.govno.client.utils;

import java.util.Arrays;
import ru.govno.client.event.EventManager;
import ru.govno.client.utils.Math.MathUtils;

public class TPSDetect {
   public static float[] tickRates = new float[20];
   public static int nextIndex = 0;
   public static long timeLastTimeUpdate;

   public TPSDetect() {
      nextIndex = 0;
      timeLastTimeUpdate = -1L;
      Arrays.fill(tickRates, 20.0F);
      EventManager.register(this);
   }

   public static float getTPSServer() {
      float numTicks = 0.0F;
      float sumTickRates = 0.0F;

      for (float tickRate : tickRates) {
         if (tickRate > 0.0F) {
            sumTickRates += tickRate;
            numTicks++;
         }
      }

      return Float.isNaN(MathUtils.clamp(sumTickRates / numTicks, 0.5F, 20.0F)) ? 0.0F : MathUtils.clamp(sumTickRates / numTicks, 0.5F, 20.0F);
   }

   public static float getConpensationTPS(boolean doConpensation) {
      return doConpensation ? 1.0F / (getTPSServer() / 20.0F) : 1.0F;
   }

   public static String getTpsString() {
      return String.format("%.2f", getTPSServer());
   }

   public static void onTimeUpdate() {
      if (timeLastTimeUpdate != -1L) {
         float timeElapsed = (float)(System.currentTimeMillis() - timeLastTimeUpdate) / 1000.0F;
         tickRates[nextIndex % tickRates.length] = MathUtils.clamp(20.0F / timeElapsed, 0.5F, 20.0F);
         nextIndex++;
      }

      timeLastTimeUpdate = System.currentTimeMillis();
   }
}
