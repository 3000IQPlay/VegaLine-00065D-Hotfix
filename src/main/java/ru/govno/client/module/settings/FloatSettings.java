package ru.govno.client.module.settings;

import java.util.function.Supplier;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Render.AnimationUtils;

public class FloatSettings extends Settings {
   public float fMin;
   public float fMax;
   public AnimationUtils floatAnim;

   public FloatSettings(String name, float fValue, float fMax, float fMin, Module module) {
      this.name = name;
      this.floatAnim = new AnimationUtils(fValue, fValue, 0.1F);
      this.fMax = fMax;
      this.fMin = fMin;
      this.module = module;
      this.category = Settings.Category.Float;
   }

   public FloatSettings(String name, float fValue, float fMax, float fMin, Module module, Supplier<Boolean> visible) {
      this.name = name;
      this.floatAnim = new AnimationUtils(fValue, fValue, 0.1F);
      this.fMax = fMax;
      this.fMin = fMin;
      this.module = module;
      this.category = Settings.Category.Float;
      this.visible = visible;
   }

   public float getFloat() {
      return this.floatAnim.to;
   }

   public int getInt() {
      return (int)this.floatAnim.to;
   }

   public float getAnimation() {
      return this.floatAnim.getAnim();
   }

   public void setFloat(float value) {
      this.floatAnim.to = value;
   }
}
