package ru.govno.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class EntityHelper {
   static Minecraft mc = Minecraft.getMinecraft();

   public static double getDistance(double x, double y, double z, double x1, double y1, double z1) {
      double posX = x - x1;
      double posY = y - y1;
      double posZ = z - z1;
      return Math.sqrt(posX * posX + posY * posY + posZ * posZ);
   }

   public static double getDistance(double x1, double z1, double x2, double z2) {
      double deltaX = x1 - x2;
      double deltaZ = z1 - z2;
      return Math.hypot(deltaX, deltaZ);
   }

   public static Entity rayCast(Entity entityIn, double range) {
      Vec3d vec = entityIn.getPositionVector().add(new Vec3d(0.0, (double)entityIn.getEyeHeight(), 0.0));
      Vec3d vecPositionVector = Minecraft.player.getPositionVector().add(new Vec3d(0.0, (double)Minecraft.player.getEyeHeight(), 0.0));
      AxisAlignedBB axis = Minecraft.player
         .getEntityBoundingBox()
         .addCoord(vec.xCoord - vecPositionVector.xCoord, vec.yCoord - vecPositionVector.yCoord, vec.zCoord - vecPositionVector.zCoord)
         .expand(1.0, 1.0, 1.0);
      Entity entityRayCast = null;

      for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(Minecraft.player, axis)) {
         if (entity.canBeCollidedWith() && entity instanceof EntityLivingBase) {
            float size = entity.getCollisionBorderSize();
            AxisAlignedBB axis1 = entity.getEntityBoundingBox().expand((double)size, (double)size, (double)size);
            RayTraceResult rayTrace = axis1.calculateIntercept(vecPositionVector, vec);
            if (axis1.isVecInside(vecPositionVector)) {
               if (range >= 0.0) {
                  entityRayCast = entity;
                  range = 0.0;
               }
            } else if (rayTrace != null) {
               double dist = vecPositionVector.distanceTo(rayTrace.hitVec);
               if (range == 0.0 || dist < range) {
                  entityRayCast = entity;
                  range = dist;
               }
            }
         }
      }

      return entityRayCast;
   }

   public static boolean isTeamWithYou(EntityLivingBase entity) {
      if (Minecraft.player != null && entity != null && Minecraft.player.getDisplayName() != null && entity.getDisplayName() != null) {
         if (Minecraft.player.getTeam() != null && entity.getTeam() != null && Minecraft.player.getTeam().isSameTeam(entity.getTeam())) {
            return true;
         } else {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = Minecraft.player.getDisplayName().getFormattedText().replace("§r", "");
            return targetName.startsWith("§" + clientName.charAt(1));
         }
      } else {
         return false;
      }
   }

   public static boolean checkArmor(Entity entity) {
      return ((EntityLivingBase)entity).getTotalArmorValue() < 6;
   }

   public static int getPing(EntityPlayer entityPlayer) {
      return Minecraft.player.connection.getPlayerInfo(entityPlayer.getUniqueID()).getResponseTime();
   }

   public static double getDistanceOfEntityToBlock(Entity entity, BlockPos pos) {
      return getDistance(entity.posX, entity.posY, entity.posZ, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
   }
}
