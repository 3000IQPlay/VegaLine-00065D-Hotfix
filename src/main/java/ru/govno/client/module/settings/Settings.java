package ru.govno.client.module.settings;

import java.util.function.Supplier;
import ru.govno.client.module.Module;

public class Settings {
   public String name;
   public Settings.Category category;
   public Module module;
   Supplier<Boolean> visible = () -> true;

   public boolean isVisible() {
      return this.visible.get();
   }

   public void setVisible(Supplier<Boolean> visible) {
      this.visible = visible;
   }

   public Settings.Category getCategory() {
      return this.category;
   }

   public String getName() {
      return this.name;
   }

   public Module getModule() {
      return this.module;
   }

   public static enum Category {
      Boolean,
      Float,
      String_Massive,
      Color;
   }
}
