package ru.govno.client.utils.Render;

import ru.govno.client.utils.Math.MathUtils;

public class AnimationUtils {
   long mc;
   public float anim;
   public float to;
   public float speed;

   public AnimationUtils(float anim, float to, float speed) {
      this.anim = anim;
      this.to = to;
      this.speed = speed;
      this.mc = System.currentTimeMillis();
   }

   public float getAnim() {
      int count = (int)((float)(System.currentTimeMillis() - this.mc) / 10.0F);
      if (count > 0) {
         this.mc = System.currentTimeMillis();
      }

      for (int i = 0; i < count; i++) {
         this.anim = MathUtils.lerp(this.anim, this.to, this.speed * 1.75F > 1.0F ? 1.0F : this.speed * 1.75F);
      }

      return this.anim;
   }

   public float getAngleAnim() {
      int count = (int)((float)(System.currentTimeMillis() - this.mc) / 10.0F);
      if (count > 0) {
         this.mc = System.currentTimeMillis();
      }

      for (int i = 0; i < count; i++) {
         this.anim = (float)this.lerpAngle(this.anim, this.to, this.speed * 1.75F > 1.0F ? 1.0F : this.speed * 1.75F);
      }

      return MathUtils.wrapAngleTo180_float(this.anim);
   }

   public void setAnim(float anim) {
      this.anim = anim;
      this.mc = System.currentTimeMillis();
   }

   double lerpAngle(float start, float end, float amount) {
      float minAngle = (end - start + 180.0F) % 360.0F - 180.0F;
      return (double)(minAngle * amount + start);
   }
}
