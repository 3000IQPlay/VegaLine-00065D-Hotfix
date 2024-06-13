package ru.govno.client.utils.Movement;

import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;

public class MovementHelper {
   public static boolean isMoving() {
      Minecraft.getMinecraft();
      if (MovementInput.moveForward == 0.0F) {
         Minecraft.getMinecraft();
         if (MovementInput.moveStrafe == 0.0F) {
            return false;
         }
      }

      return true;
   }

   public static float getBaseMoveSpeed() {
      Minecraft mc = Minecraft.getMinecraft();
      float baseSpeed = 0.2873F;
      if (Minecraft.player != null && Minecraft.player.isPotionActive(MobEffects.SPEED)) {
         int amplifier = Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
         baseSpeed = (float)((double)baseSpeed * (1.0 + 0.2 * (double)(amplifier + 1)));
      }

      return baseSpeed;
   }

   public static void strafe() {
      if (!Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
         strafe(getSpeed());
      }
   }

   public static float getSpeeds() {
      Minecraft.getMinecraft();
      double var10000 = Minecraft.player.motionX;
      Minecraft.getMinecraft();
      var10000 *= Minecraft.player.motionX;
      Minecraft.getMinecraft();
      double var10001 = Minecraft.player.motionZ;
      Minecraft.getMinecraft();
      return (float)Math.sqrt(var10000 + var10001 * Minecraft.player.motionZ);
   }

   public static void strafe(float speed) {
      if (isMoving()) {
         double yaw = (double)getDirection();
         Minecraft.getMinecraft();
         Minecraft.player.motionX = -Math.sin(yaw) * (double)speed;
         Minecraft.getMinecraft();
         Minecraft.player.motionZ = Math.cos(yaw) * (double)speed;
      }
   }

   public static float getDirection() {
      Minecraft.getMinecraft();
      float rotationYaw = Minecraft.player.rotationYaw;
      float factor = 1.0F;
      Minecraft.getMinecraft();
      if (MovementInput.moveForward > 0.0F) {
         factor = 1.0F;
      }

      Minecraft.getMinecraft();
      if (MovementInput.moveForward < 0.0F) {
         factor = -1.0F;
      }

      if (factor == 0.0F) {
         Minecraft.getMinecraft();
         if (MovementInput.moveStrafe > 0.0F) {
            rotationYaw -= 90.0F;
         }

         Minecraft.getMinecraft();
         if (MovementInput.moveStrafe < 0.0F) {
            rotationYaw += 90.0F;
         }
      } else if (!Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
         Minecraft.getMinecraft();
         float var10001;
         if (MovementInput.moveStrafe > 0.0F) {
            var10001 = -90.0F;
         } else {
            Minecraft.getMinecraft();
            var10001 = MovementInput.moveStrafe < 0.0F ? 90.0F : 0.0F;
         }

         rotationYaw += var10001;
      } else {
         Minecraft.getMinecraft();
         if (MovementInput.moveStrafe > 0.0F) {
            rotationYaw -= Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown() ? 45.0F * factor : -(45.0F * factor);
         }

         Minecraft.getMinecraft();
         if (MovementInput.moveStrafe < 0.0F) {
            rotationYaw += Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown() ? 45.0F * factor : -(45.0F * factor);
         }
      }

      if (factor < 0.0F) {
         rotationYaw -= 180.0F;
      }

      return (float)Math.toRadians((double)rotationYaw);
   }

   public static float getDirection2() {
      Minecraft mc = Minecraft.getMinecraft();
      float var1 = Minecraft.player.rotationYaw;
      if (Minecraft.player.moveForward < 0.0F) {
         var1 += 180.0F;
      }

      float forward = 1.0F;
      if (Minecraft.player.moveForward < 0.0F) {
         forward = -50.5F;
      } else if (Minecraft.player.moveForward > 0.0F) {
         forward = 50.5F;
      }

      if (Minecraft.player.moveStrafing > 0.0F) {
         var1 -= 22.0F * forward;
      }

      if (Minecraft.player.moveStrafing < 0.0F) {
         var1 += 22.0F * forward;
      }

      float var3;
      return var3 = var1 * (float) (Math.PI / 180.0);
   }

   public static int getSpeedEffect() {
      Minecraft mc = Minecraft.getMinecraft();
      return Minecraft.player.isPotionActive(MobEffects.SPEED) ? Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1 : 0;
   }

   public static float getMoveDirection() {
      Minecraft mc = Minecraft.getMinecraft();
      double motionX = Minecraft.player.motionX;
      double motionZ = Minecraft.player.motionZ;
      float direction = (float)(Math.atan2(motionX, motionZ) / Math.PI * 180.0);
      return -direction;
   }

   public static boolean isBlockAboveHead() {
      Minecraft mc = Minecraft.getMinecraft();
      AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
         Minecraft.player.posX - 0.3,
         Minecraft.player.posY + (double)Minecraft.player.getEyeHeight(),
         Minecraft.player.posZ + 0.3,
         Minecraft.player.posX + 0.3,
         Minecraft.player.posY + (!Minecraft.player.onGround ? 1.5 : 2.5),
         Minecraft.player.posZ - 0.3
      );
      return mc.world.getCollisionBoxes(Minecraft.player, axisAlignedBB).isEmpty();
   }

   public static float getSpeed() {
      Minecraft mc = Minecraft.getMinecraft();
      return (float)Math.sqrt(Minecraft.player.motionX * Minecraft.player.motionX + Minecraft.player.motionZ * Minecraft.player.motionZ);
   }

   public static void setSpeed(float speed, float arrf, int direction, double d) {
      Minecraft mc = Minecraft.getMinecraft();
      float yaw = Minecraft.player.rotationYaw;
      float forward = MovementInput.moveForward;
      float strafe = MovementInput.moveStrafe;
      if (forward != 0.0F) {
         if (strafe > 0.0F) {
            yaw += (float)(forward > 0.0F ? -45 : 45);
         } else if (strafe < 0.0F) {
            yaw += (float)(forward > 0.0F ? 45 : -45);
         }

         strafe = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      Minecraft.player.motionX = (double)(forward * speed) * Math.cos(Math.toRadians((double)(yaw + 90.0F)))
         + (double)(strafe * speed) * Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      Minecraft.player.motionZ = (double)(forward * speed) * Math.sin(Math.toRadians((double)(yaw + 90.0F)))
         - (double)(strafe * speed) * Math.cos(Math.toRadians((double)(yaw + 90.0F)));
   }

   public static double getDirectionAll() {
      Minecraft mc = Minecraft.getMinecraft();
      float rotationYaw = Minecraft.player.rotationYaw;
      float forward = 1.0F;
      if (Minecraft.player.moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      if (Minecraft.player.moveForward < 0.0F) {
         forward = -0.5F;
      } else if (Minecraft.player.moveForward > 0.0F) {
         forward = 0.5F;
      }

      if (Minecraft.player.moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (Minecraft.player.moveStrafing < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }

   public static void strafePlayer(float speed) {
      Minecraft mc = Minecraft.getMinecraft();
      double yaw = getDirectionAll();
      float getSpeed = speed == 0.0F ? getSpeed() : speed;
      Minecraft.player.motionX = -Math.sin(yaw) * (double)getSpeed;
      Minecraft.player.motionZ = Math.cos(yaw) * (double)getSpeed;
   }
}
