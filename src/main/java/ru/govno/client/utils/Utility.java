package ru.govno.client.utils;

import java.util.ArrayList;
import java.util.List;
import ru.govno.client.utils.Render.GaussianBlur;

public interface Utility {
   List<Runnable> NORMAL_BLUR_RUNNABLES = new ArrayList<>();
   List<Runnable> NORMAL_SHADOW_BLACK = new ArrayList<>();

   static void render2DRunnables() {
      if (!NORMAL_BLUR_RUNNABLES.isEmpty()) {
         GaussianBlur.renderBlur(15.0F, NORMAL_BLUR_RUNNABLES);
      }

      if (!NORMAL_BLUR_RUNNABLES.isEmpty() || !NORMAL_SHADOW_BLACK.isEmpty()) {
         clearRunnables();
      }
   }

   static void clearRunnables() {
      NORMAL_BLUR_RUNNABLES.clear();
      NORMAL_SHADOW_BLACK.clear();
   }
}
