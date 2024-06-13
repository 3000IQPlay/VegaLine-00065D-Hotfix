package ru.govno.client.utils.Movement;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.event.events.EventMovementInput;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.utils.Math.MathUtils;

public class MoveMeHelp {
   private static double direction(float rotationYaw, double moveForward, double moveStrafing) {
      if (moveForward < 0.0) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0) {
         forward = -0.5F;
      } else if (moveForward > 0.0) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }

   public static void fixDirMove(EventMovementInput event, float yaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(Minecraft.player.rotationYaw, (double)forward, (double)strafe)));
      if (forward != 0.0F || strafe != 0.0F) {
         float closestForward = 0.0F;
         float closestStrafe = 0.0F;
         float closestDifference = Float.MAX_VALUE;

         for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward++) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe++) {
               if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                  double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, (double)predictedForward, (double)predictedStrafe)));
                  double difference = Math.abs(angle - predictedAngle);
                  if (difference < (double)closestDifference) {
                     closestDifference = (float)difference;
                     closestForward = predictedForward;
                     closestStrafe = predictedStrafe;
                  }
               }
            }
         }

         event.setForward(closestForward);
         event.setStrafe(closestStrafe);
      }
   }

   public static double getMotionYaw() {
      double motionYaw = Math.toDegrees(Math.atan2(Entity.Getmotionz, Entity.Getmotionx) - 90.0);
      return motionYaw < 0.0 ? motionYaw + 360.0 : motionYaw;
   }

   public static double getDirDiffOfMotions(double motionX, double motionZ, double moveYaw) {
      return Math.abs(MathUtils.wrapDegrees(Math.toDegrees(Math.atan2(motionZ, motionX))) - MathUtils.wrapDegrees(moveYaw % 360.0 + 90.0));
   }

   public static double getDirDiffOfMotions(double motionX, double motionZ) {
      return getDirDiffOfMotions(motionX, motionZ, (double)Minecraft.player.rotationYaw);
   }

   public static double getDirDiffOfMotionsNoAbs(double motionX, double motionZ, double moveYaw) {
      return MathUtils.wrapDegrees(Math.toDegrees(Math.atan2(motionZ, motionX))) - MathUtils.wrapDegrees(moveYaw % 360.0 + 90.0);
   }

   public static double getDirDiffOfMotionsNoAbs(double motionX, double motionZ) {
      return getDirDiffOfMotionsNoAbs(motionX, motionZ, (double)Minecraft.player.rotationYaw);
   }

   public static boolean trapdoorAdobedEntity(EntityLivingBase livingIn) {
      if (!isBlockAboveHead(livingIn)) {
         return false;
      } else {
         double xzExpand = (double)livingIn.width / 2.0 - 0.01;
         int offsetsCount = 18;
         int trapDoorLevels = 0;
         boolean anyCounter1 = false;
         boolean anyCounter2 = false;
         AxisAlignedBB entityBox = livingIn.getEntityBoundingBox();
         if (Minecraft.getMinecraft().world.getCollisionBoxes(livingIn, entityBox).isEmpty()
            && !Minecraft.getMinecraft().world.getCollisionBoxes(livingIn, entityBox.setMaxY(entityBox.maxY + 0.2)).isEmpty()) {
            double[] xOffsets = new double[]{0.0, xzExpand, -xzExpand, xzExpand, -xzExpand, 0.0, 0.0, xzExpand, xzExpand};
            double[] zOffsets = new double[]{0.0, xzExpand, -xzExpand, 0.0, 0.0, xzExpand, -xzExpand, -xzExpand, -xzExpand};
            BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
            Vec3d entityPos = livingIn.getPositionVector();
            List<IBlockState> checkStatesList = new ArrayList<>();
            IBlockState blockState = null;

            for (int offsetsNum = 0; offsetsNum < offsetsCount; offsetsNum++) {
               double xOffset = xOffsets[offsetsNum / 2];
               double zOffset = zOffsets[offsetsNum / 2];
               Vec3d forSetMulVec = entityPos.addVector(xOffset, 1.19 - (double)(offsetsNum % 2), zOffset);
               blockState = Minecraft.getMinecraft().world.getBlockState(mutPos.setPos(forSetMulVec.xCoord, forSetMulVec.yCoord, forSetMulVec.zCoord));
               checkStatesList.add(blockState);
            }

            if (checkStatesList.isEmpty()) {
               return false;
            } else {
               for (int blockStateCounter = 0; blockStateCounter < checkStatesList.size(); blockStateCounter++) {
                  if (blockState != null && checkStatesList.get(blockStateCounter).getBlock() instanceof BlockTrapDoor) {
                     if (blockStateCounter % 2 == 0) {
                        anyCounter1 = true;
                     } else {
                        anyCounter2 = true;
                     }
                  }
               }

               return (anyCounter1 || anyCounter2) && anyCounter1 != anyCounter2;
            }
         } else {
            return false;
         }
      }
   }

   public static final boolean moveKeyPressed(int keyNumber) {
      boolean w = Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown();
      boolean a = Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown();
      boolean s = Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown();
      boolean d = Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown();
      return keyNumber == 0 ? w : (keyNumber == 1 ? a : (keyNumber == 2 ? s : keyNumber == 3 && d));
   }

   public static final boolean w() {
      return moveKeyPressed(0);
   }

   public static final boolean a() {
      return moveKeyPressed(1);
   }

   public static final boolean s() {
      return moveKeyPressed(2);
   }

   public static final boolean d() {
      return moveKeyPressed(3);
   }

   public static final float moveYaw(float entityYaw) {
      return entityYaw
         + (float)(
            !a() || !d() || w() && s() || !w() && !s()
               ? (
                  !w() || !s() || a() && d() || !a() && !d()
                     ? (
                        (!a() || !d() || w() && s()) && (!w() || !s() || a() && d())
                           ? (
                              !a() && !d() && !s()
                                 ? 0
                                 : (w() && !s() ? 45 : (s() && !w() ? (!a() && !d() ? 180 : 135) : ((w() || s()) && (!w() || !s()) ? 0 : 90))) * (a() ? -1 : 1)
                           )
                           : 0
                     )
                     : (a() ? -90 : (d() ? 90 : 0))
               )
               : (w() ? 0 : (s() ? 180 : 0))
         );
   }

   public static Minecraft mc() {
      return Minecraft.getMinecraft();
   }

   public static boolean moving() {
      return w() || a() || s() || d();
   }

   public static double getSpeedByBPS(double bps) {
      return bps / 15.3571428571;
   }

   public static void setMotionSpeed(boolean cutting, boolean onlyMove, double speed) {
      if (!moving()) {
         speed = 0.0;
      }

      float yawPre = -(Minecraft.player.lastReportedPreYaw - Minecraft.player.rotationYaw) * 3.0F;
      if (MathUtils.getDifferenceOf(yawPre, 0.0F) > 30.0) {
         yawPre = yawPre > 0.0F ? 30.0F : -30.0F;
      }

      float yaw = Minecraft.player.rotationYaw;
      if (HitAura.get.currentBooleanValue("RotateMoveSide") && HitAura.TARGET != null) {
         yaw = HitAura.get.rotations[0];
         yawPre = 0.0F;
      }

      float moveYaw = moveYaw(yaw + yawPre);
      double sin = -Math.sin(Math.toRadians((double)moveYaw)) * speed;
      double cos = Math.cos(Math.toRadians((double)moveYaw)) * speed;
      if (!onlyMove || moving()) {
         if (cutting) {
            Entity.motionx = sin / 1.06F;
         }

         Minecraft.player.motionX = sin;
         if (cutting) {
            Entity.motionz = cos / 1.06F;
         }

         Minecraft.player.motionZ = cos;
      }
   }

   public static void setMotionSpeed(boolean cutting, boolean onlyMove, double speed, float moveYaw) {
      if (!moving()) {
         speed = 0.0;
      }

      double sin = -Math.sin(Math.toRadians((double)moveYaw)) * speed;
      double cos = Math.cos(Math.toRadians((double)moveYaw)) * speed;
      if (!onlyMove || moving()) {
         if (cutting) {
            Entity.motionx = sin / 1.06F;
         }

         Minecraft.player.motionX = sin;
         if (cutting) {
            Entity.motionz = cos / 1.06F;
         }

         Minecraft.player.motionZ = cos;
      }
   }

   public static void multiplySpeed(double speed) {
      setSpeed(getSpeed() * speed);
   }

   private static void Motion(double d, float f, double d2, double d3, boolean onMove, boolean smartKeep) {
      Minecraft mc = Minecraft.getMinecraft();
      double d4 = d3;
      double d5 = d2;
      float keep = 0.0F;
      float f2 = f;
      if (d3 != 0.0 || d2 != 0.0) {
         if (d3 != 0.0) {
            if (d2 > 0.0) {
               f2 = f + (d3 > 0.0 ? -keep : keep);
            } else if (d2 < 0.0) {
               f2 = f + (d3 > 0.0 ? keep : -keep);
            }

            d5 = 0.0;
            if (d3 > 0.0) {
               d4 = 1.0;
            } else if (d3 < 0.0) {
               d4 = -1.0;
            }
         }

         double d6 = Math.cos(Math.toRadians((double)(f2 + 93.5F)));
         double d7 = Math.sin(Math.toRadians((double)(f2 + 93.5F)));
         if (onMove) {
            Entity.motionx = (d4 * d * d6 + d5 * d * d7) / 1.06;
            Entity.motionz = (d4 * d * d7 - d5 * d * d6) / 1.06;
         } else {
            Minecraft.player.motionX = d4 * d * d6 + d5 * d * d7;
            Minecraft.player.motionZ = d4 * d * d7 - d5 * d * d6;
         }
      }
   }

   private static float[] getRotationFromPosition(double x, double z, double y) {
      double xDiff = x - Minecraft.player.posX;
      double zDiff = z - Minecraft.player.posZ;
      double yDiff = y - Minecraft.player.posY - 1.8;
      double dist = (double)MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff);
      float yaw = (float)(Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 92.0F;
      float pitch = (float)(-(Math.atan2(yDiff, dist) * 180.0 / Math.PI));
      return new float[]{yaw, pitch};
   }

   public static double[] getSpeed(double speed) {
      Minecraft mc = Minecraft.getMinecraft();
      float yaw = Minecraft.player.rotationYaw;
      double forward = (double)MovementInput.moveForward;
      double strafe = (double)MovementInput.moveStrafe;
      if (forward != 0.0) {
         if (strafe > 0.0) {
            yaw += (float)(forward > 0.0 ? -45 : 45);
         } else if (strafe < 0.0) {
            yaw += (float)(forward > 0.0 ? 45 : -45);
         }

         strafe = 0.0;
         if (forward > 0.0) {
            forward = 1.0;
         } else if (forward < 0.0) {
            forward = -1.0;
         }
      }

      return new double[]{
         forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))),
         forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))),
         (double)yaw
      };
   }

   public static double getSpeed() {
      return Math.sqrt(Minecraft.player.motionX * Minecraft.player.motionX + Minecraft.player.motionZ * Minecraft.player.motionZ);
   }

   public static double getCuttingSpeed() {
      return Math.sqrt(Entity.Getmotionx * Entity.Getmotionx + Entity.Getmotionz * Entity.Getmotionz);
   }

   public static float getKeysDirection() {
      Minecraft mc = Minecraft.getMinecraft();
      double yaw = (double)Minecraft.player.rotationYaw;
      if (mc.gameSettings.keyBindForward.isKeyDown()
         && Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
         yaw -= 45.0;
      }

      if (mc.gameSettings.keyBindForward.isKeyDown()
         && Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
         yaw += 45.0;
      }

      if (!mc.gameSettings.keyBindForward.isKeyDown()
         && !mc.gameSettings.keyBindBack.isKeyDown()
         && Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
         yaw -= 90.0;
      }

      if (!mc.gameSettings.keyBindForward.isKeyDown()
         && !mc.gameSettings.keyBindBack.isKeyDown()
         && Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
         yaw += 90.0;
      }

      if (!mc.gameSettings.keyBindForward.isKeyDown()
         && mc.gameSettings.keyBindBack.isKeyDown()
         && Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
         yaw -= 135.0;
      }

      if (!mc.gameSettings.keyBindForward.isKeyDown()
         && mc.gameSettings.keyBindBack.isKeyDown()
         && Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
         yaw += 135.0;
      }

      if (!mc.gameSettings.keyBindForward.isKeyDown()
         && mc.gameSettings.keyBindBack.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
         yaw += 180.0;
      }

      return 0.0F;
   }

   public static boolean isBlockAboveHead() {
      Minecraft mc = Minecraft.getMinecraft();
      AxisAlignedBB bb1 = new AxisAlignedBB(
         Minecraft.player.posX - 0.3,
         Minecraft.player.posY,
         Minecraft.player.posZ - 0.3,
         Minecraft.player.posX + 0.3,
         Minecraft.player.posY + (double)Minecraft.player.height,
         Minecraft.player.posZ - 0.3
      );
      AxisAlignedBB bb2 = new AxisAlignedBB(
         Minecraft.player.posX - 0.3,
         Minecraft.player.posY + (double)Minecraft.player.getEyeHeight(),
         Minecraft.player.posZ - 0.3,
         Minecraft.player.posX + 0.3,
         Minecraft.player.posY + 2.4,
         Minecraft.player.posZ - 0.3
      );
      return mc.world != null
         && Minecraft.player != null
         && mc.world.getCollisionBoxes(Minecraft.player, bb1).isEmpty()
         && !mc.world.getCollisionBoxes(Minecraft.player, bb2).isEmpty();
   }

   public static boolean isBlockAboveHeadSolo() {
      Minecraft mc = Minecraft.getMinecraft();
      AxisAlignedBB bb = new AxisAlignedBB(
         Minecraft.player.posX,
         Minecraft.player.posY + (double)Minecraft.player.getEyeHeight(),
         Minecraft.player.posZ,
         Minecraft.player.posX,
         Minecraft.player.posY + (double)Minecraft.player.height + 0.4,
         Minecraft.player.posZ
      );
      return !mc.world.getCollisionBoxes(Minecraft.player, bb).isEmpty();
   }

   public static boolean isBlockAboveHead(Entity entity) {
      Minecraft mc = Minecraft.getMinecraft();
      AxisAlignedBB bb = new AxisAlignedBB(
         entity.posX - 0.3, entity.posY + (double)entity.getEyeHeight(), entity.posZ + 0.3, entity.posX + 0.3, entity.posY + 2.5, entity.posZ - 0.3
      );
      return !mc.world.getCollisionBoxes(entity, bb).isEmpty();
   }

   public static boolean moveKeysPressed() {
      Minecraft mc = Minecraft.getMinecraft();
      return mc.gameSettings.keyBindForward.isKeyDown()
         || mc.gameSettings.keyBindBack.isKeyDown()
         || Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()
         || Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown();
   }

   public static void setSpeed(double speed) {
      double forward = (double)MovementInput.moveForward;
      double strafe = (double)MovementInput.moveStrafe;
      float yaw = Minecraft.player.rotationYaw;
      if (HitAura.get.currentBooleanValue("RotateMoveSide") && HitAura.TARGET != null) {
         yaw = HitAura.get.rotations[0];
      }

      if (isMoving()) {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
               yaw += (float)(forward > 0.0 ? 45 : -45);
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else if (forward < 0.0) {
               forward = -1.0;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 89.5F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 89.5F)));
         Minecraft.player.motionX = forward * speed * cos + strafe * speed * sin;
         Minecraft.player.motionZ = forward * speed * sin - strafe * speed * cos;
      } else {
         Minecraft.player.motionX = 0.0;
         Minecraft.player.motionZ = 0.0;
      }
   }

   public static void setSmoothSpeed(double speed, double strengh, boolean doResetOnNotMove) {
      double forward = (double)MovementInput.moveForward;
      double strafe = (double)MovementInput.moveStrafe;
      float yaw = Minecraft.player.rotationYaw;
      if (HitAura.get.currentBooleanValue("RotateMoveSide") && HitAura.TARGET != null) {
         yaw = HitAura.get.rotations[0];
      }

      if (isMoving()) {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
               yaw += (float)(forward > 0.0 ? 45 : -45);
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else if (forward < 0.0) {
               forward = -1.0;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 89.5F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 89.5F)));
         Minecraft.player.motionX = MathUtils.lerp(Minecraft.player.motionX, forward * speed * cos + strafe * speed * sin, strengh);
         Minecraft.player.motionZ = MathUtils.lerp(Minecraft.player.motionZ, forward * speed * sin - strafe * speed * cos, strengh);
      } else if (doResetOnNotMove) {
         Minecraft.player.motionX = 0.0;
         Minecraft.player.motionZ = 0.0;
      }
   }

   public static void setCuttingSpeed(double speed) {
      Minecraft mc = Minecraft.getMinecraft();
      boolean tickTime = Minecraft.player.ticksExisted % 2 == 0;
      double forward = (double)MovementInput.moveForward;
      double strafe = (double)MovementInput.moveStrafe;
      float yaw = Minecraft.player.rotationYaw - (Minecraft.player.lastReportedPreYaw - Minecraft.player.rotationYaw) * 2.0F;
      if (HitAura.get.currentBooleanValue("RotateMoveSide") && HitAura.TARGET != null) {
         yaw = HitAura.get.rotations[0];
      }

      if (!mc.gameSettings.keyBindForward.isKeyDown()
         && !mc.gameSettings.keyBindBack.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
         Entity.motionx = tickTime ? 1.0E-10 : -1.0E-10;
         Entity.motionz = tickTime ? 1.0E-10 : -1.0E-10;
      } else {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
               yaw += (float)(forward > 0.0 ? 45 : -45);
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else if (forward < 0.0) {
               forward = -1.0;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 89.5F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 89.5F)));
         Entity.motionx = forward * speed * cos + strafe * speed * sin;
         Entity.motionz = forward * speed * sin - strafe * speed * cos;
      }
   }

   public static void setSmoothCuttingSpeed(double speed, double strengh, boolean doResetOnNotMove) {
      Minecraft mc = Minecraft.getMinecraft();
      boolean tickTime = Minecraft.player.ticksExisted % 2 == 0;
      double forward = (double)MovementInput.moveForward;
      double strafe = (double)MovementInput.moveStrafe;
      float yaw = Minecraft.player.rotationYaw - (Minecraft.player.lastReportedPreYaw - Minecraft.player.rotationYaw) * 2.0F;
      if (HitAura.get.currentBooleanValue("RotateMoveSide") && HitAura.TARGET != null) {
         yaw = HitAura.get.rotations[0];
      }

      if (!mc.gameSettings.keyBindForward.isKeyDown()
         && !mc.gameSettings.keyBindBack.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()
         && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown()) {
         if (doResetOnNotMove) {
            Entity.motionx = tickTime ? 1.0E-10 : -1.0E-10;
            Entity.motionz = tickTime ? 1.0E-10 : -1.0E-10;
         }
      } else {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
               yaw += (float)(forward > 0.0 ? 45 : -45);
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else if (forward < 0.0) {
               forward = -1.0;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 89.5F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 89.5F)));
         Entity.motionx = MathUtils.lerp(Entity.Getmotionx, forward * speed * cos + strafe * speed * sin, strengh);
         Entity.motionz = MathUtils.lerp(Entity.Getmotionz, forward * speed * sin - strafe * speed * cos, strengh);
      }
   }

   public static void setSpeed(double speed, float noMoveSlowSpeed) {
      double forward = (double)MovementInput.moveForward;
      double strafe = (double)MovementInput.moveStrafe;
      float yaw = Minecraft.player.rotationYaw;
      if (HitAura.get.currentBooleanValue("RotateMoveSide") && HitAura.TARGET != null) {
         yaw = HitAura.get.rotations[0];
      }

      if (isMoving()) {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
               yaw += (float)(forward > 0.0 ? 45 : -45);
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else if (forward < 0.0) {
               forward = -1.0;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 89.5F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 89.5F)));
         Minecraft.player.motionX = forward * speed * cos + strafe * speed * sin;
         Minecraft.player.motionZ = forward * speed * sin - strafe * speed * cos;
      } else {
         Minecraft.player.motionX *= (double)noMoveSlowSpeed;
         Minecraft.player.motionZ *= (double)noMoveSlowSpeed;
      }
   }

   public static void setSpeed(double speed, int strafeMove) {
      double forward = (double)MovementInput.moveForward;
      double strafe = (double)MovementInput.moveStrafe;
      float yaw = Minecraft.player.rotationYaw;
      if (HitAura.get.currentBooleanValue("RotateMoveSide") && HitAura.TARGET != null) {
         yaw = HitAura.get.rotations[0];
      }

      if (isMoving()) {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += (float)(forward > 0.0 ? -strafeMove : strafeMove);
            } else if (strafe < 0.0) {
               yaw += (float)(forward > 0.0 ? strafeMove : -strafeMove);
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else if (forward < 0.0) {
               forward = -1.0;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
         Minecraft.player.motionX = forward * speed * cos + strafe * speed * sin;
         Minecraft.player.motionZ = forward * speed * sin - strafe * speed * cos;
      } else {
         Minecraft.player.motionX = 0.0;
         Minecraft.player.motionZ = 0.0;
      }
   }

   public static boolean isMoving() {
      Minecraft mc = Minecraft.getMinecraft();
      return MovementInput.moveForward != 0.0F || MovementInput.moveStrafe != 0.0F;
   }

   public static float getMaxFallDist() {
      PotionEffect potioneffect = Minecraft.player.getActivePotionEffect(Potion.REGISTRY.getObject(new ResourceLocation("jump")));
      int f = potioneffect != null ? potioneffect.getAmplifier() + 1 : 0;
      return (float)(Minecraft.player.getMaxFallHeight() + f);
   }

   public static double getJumpBoostModifier(double baseJumpHeight) {
      if (Minecraft.player.isPotionActive(Potion.REGISTRY.getObject(new ResourceLocation("jump")))) {
         int amplifier = Minecraft.player.getActivePotionEffect(Potion.REGISTRY.getObject(new ResourceLocation("jump"))).getAmplifier();
         baseJumpHeight += (double)((float)(amplifier + 1) * 0.1F);
      }

      return baseJumpHeight;
   }

   public static double getBaseJumpHeight() {
      return getJumpBoostModifier(0.41999998688698);
   }
}
