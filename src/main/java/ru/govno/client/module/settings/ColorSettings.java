package ru.govno.client.module.settings;

import java.util.function.Supplier;
import ru.govno.client.module.Module;

public class ColorSettings extends Settings {
   public int color;

   public ColorSettings(String name, int color, Module module) {
      this.name = name;
      this.module = module;
      this.color = color;
      this.category = Settings.Category.Color;
   }

   public ColorSettings(String name, int color, Module module, Supplier<Boolean> visible) {
      this.name = name;
      this.module = module;
      this.color = color;
      this.visible = visible;
      this.category = Settings.Category.Color;
   }

   public int getCol() {
      return this == null ? 0 : this.color;
   }

   public void setCol(int value) {
      this.color = value;
   }
}
