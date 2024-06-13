package ru.govno.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
   private Minecraft mc = Minecraft.getMinecraft();
   private float yaw;
   private float pitch;
   private boolean rotationsSet = false;

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public boolean isRotationsSet() {
      return this.rotationsSet;
   }

   public void reset() {
      this.yaw = Minecraft.player.rotationYaw;
      this.pitch = Minecraft.player.rotationPitch;
      this.rotationsSet = false;
   }

   public void setRotations(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.rotationsSet = true;
   }

   public boolean safeSetRotations(float yaw, float pitch) {
      if (this.rotationsSet) {
         return false;
      } else {
         this.setRotations(yaw, pitch);
         return true;
      }
   }

   public void lookAtPos(BlockPos pos) {
      float[] angle = calculateAngle(
         Minecraft.player.getPositionEyes(this.mc.getRenderPartialTicks()),
         new Vec3d((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F))
      );
      this.setRotations(angle[0], angle[1]);
   }

   public void lookAtVec3d(Vec3d vec3d) {
      float[] angle = calculateAngle(Minecraft.player.getPositionEyes(this.mc.getRenderPartialTicks()), new Vec3d(vec3d.x, vec3d.y, vec3d.z));
      this.setRotations(angle[0], angle[1]);
   }

   public void lookAtXYZ(double x, double y, double z) {
      Vec3d vec3d = new Vec3d(x, y, z);
      this.lookAtVec3d(vec3d);
   }

   public static float[] calculateAngle(Vec3d from, Vec3d to) {
      double difX = to.x - from.x;
      double difY = (to.y - from.y) * -1.0;
      double difZ = to.z - from.z;
      double dist = (double)MathHelper.sqrt(difX * difX + difZ * difZ);
      float yD = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
      float pD = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)));
      if (pD > 90.0F) {
         pD = 90.0F;
      } else if (pD < -90.0F) {
         pD = -90.0F;
      }

      return new float[]{yD, pD};
   }
}
