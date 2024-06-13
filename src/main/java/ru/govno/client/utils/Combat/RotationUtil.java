package ru.govno.client.utils.Combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javax.vecmath.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.Client;
import ru.govno.client.utils.RandomUtils;
import ru.govno.client.utils.Math.MathUtils;

public class RotationUtil {
   public static RotationUtil instance = new RotationUtil();
   private static final Minecraft mc = Minecraft.getMinecraft();
   static float prevAdditionYaw;
   public static Vec3d vec;
   public static float Yaw = 0.0F;
   public static float Pitch = 0.0F;

   public static float calcYawOffset(float yaw) {
      double xDiff = Minecraft.player.posX - Minecraft.player.prevPosX;
      double zDiff = Minecraft.player.posZ - Minecraft.player.prevPosZ;
      float distSquared = (float)(xDiff * xDiff + zDiff * zDiff);
      float renderYawOffset = Minecraft.player.renderYawOffset;
      float offset = renderYawOffset;
      if (distSquared > 0.0025000002F) {
         offset = (float)MathHelper.atan2(zDiff, xDiff) * 180.0F / (float) Math.PI - 90.0F;
      }

      if (Minecraft.player != null && Minecraft.player.swingProgress > 0.0F) {
         offset = yaw;
      }

      float yawOffsetDiff = MathHelper.wrapDegrees(yaw - (renderYawOffset + MathHelper.wrapDegrees(offset - renderYawOffset) * 0.3F));
      yawOffsetDiff = MathUtils.clamp(yawOffsetDiff, -75.0F, 75.0F);
      renderYawOffset = yaw - yawOffsetDiff;
      if (yawOffsetDiff * yawOffsetDiff > 2500.0F) {
         renderYawOffset += yawOffsetDiff * 0.2F;
      }

      return renderYawOffset;
   }

   public static boolean canSeeEntityAtFov(Entity entityLiving, float scope) {
      double diffX = entityLiving.posX - Minecraft.player.posX;
      double diffZ = entityLiving.posZ - Minecraft.player.posZ;
      float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
      double difference = angleDifference(yaw, Minecraft.player.rotationYaw);
      return difference <= (double)scope;
   }

   public static double angleDifference(float oldYaw, float newYaw) {
      float yaw = Math.abs(oldYaw - newYaw) % 360.0F;
      if (yaw > 180.0F) {
         yaw = 360.0F - yaw;
      }

      return (double)yaw;
   }

   public static float[] getFacePosRemote(Vec3d src, Vec3d dest) {
      double diffX = dest.xCoord - src.xCoord;
      double diffY = dest.yCoord - src.yCoord;
      double diffZ = dest.zCoord - src.zCoord;
      double dist = (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
      return new float[]{fixRotation(Minecraft.player.rotationYaw, yaw), fixRotation(Minecraft.player.rotationPitch, pitch)};
   }

   public static float[] getFacePosEntityRemote(EntityLivingBase facing, Entity en) {
      if (en == null) {
         return new float[]{facing.rotationYaw, facing.rotationPitch};
      } else {
         new Vec3d(facing.posX, facing.posY, facing.posZ);
         new Vec3d(en.posX, en.posY, en.posZ);
         return getFacePosRemote(new Vec3d(facing.posX, facing.posY, facing.posZ), new Vec3d(en.posX, en.posY, en.posZ));
      }
   }

   public static EntityLivingBase getClosestEntityToEntity(float range, Entity ent) {
      EntityLivingBase closestEntity = null;
      float mindistance = range;

      for (Object o : Minecraft.getMinecraft().world.loadedEntityList) {
         EntityLivingBase en;
         if (isNotItem(o) && !ent.isEntityEqual((EntityLivingBase)o) && ent.getDistanceToEntity(en = (EntityLivingBase)o) < mindistance) {
            mindistance = ent.getDistanceToEntity(en);
            closestEntity = en;
         }
      }

      return closestEntity;
   }

   public static boolean isNotItem(Object o) {
      return o instanceof EntityLivingBase;
   }

   public static float getAngleDifference(float dir, float yaw) {
      float f = Math.abs(yaw - dir) % 360.0F;
      return f > 180.0F ? 360.0F - f : f;
   }

   public static float[] getRotations(double x, double y, double z) {
      double diffX = x + 0.5 - Minecraft.player.posX;
      double diffY = (y + 0.5) / 2.0 - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      double diffZ = z + 0.5 - Minecraft.player.posZ;
      double dist = (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
      return new float[]{yaw, pitch};
   }

   public static Float[] getLookAngles(Vec3d vec) {
      Float[] angles = new Float[2];
      Minecraft mc = Minecraft.getMinecraft();
      angles[0] = (float)(Math.atan2(Minecraft.player.posZ - vec.zCoord, Minecraft.player.posX - vec.xCoord) / Math.PI * 180.0) + 90.0F;
      float heightdiff = (float)(Minecraft.player.posY + (double)Minecraft.player.getEyeHeight() - vec.yCoord);
      float distance = (float)Math.sqrt(
         (Minecraft.player.posZ - vec.zCoord) * (Minecraft.player.posZ - vec.zCoord)
            + (Minecraft.player.posX - vec.xCoord) * (Minecraft.player.posX - vec.xCoord)
      );
      angles[1] = (float)(Math.atan2((double)heightdiff, (double)distance) / Math.PI * 180.0);
      return angles;
   }

   public static Vec3d getBestPos(final Entity entity) {
      double diffX = entity.posX - Minecraft.player.posX;
      double diffZ = entity.posZ - Minecraft.player.posZ;
      float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0) + getFixedRotation(Yaw);
      double diffY = entity.posY - Minecraft.player.posY;
      double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float pitch = (float)Math.toDegrees(-Math.atan2(diffY, dist)) + getFixedRotation(Pitch);
      ArrayList<Vec3d> vec3ds = new ArrayList<>();
      float X = (float)((entity.posX - entity.prevPosX) * (double)mc.getRenderPartialTicks());
      float Y = (float)((entity.posY - entity.prevPosY) * (double)mc.getRenderPartialTicks());
      float Z = (float)((entity.posZ - entity.prevPosZ) * (double)mc.getRenderPartialTicks());
      AxisAlignedBB aabb = entity.getEntityBoundingBox().offset((double)X, (double)Y, (double)Z);
      double accuracy = 0.2F;
      double minx = aabb.minX + accuracy;
      double maxx = aabb.maxX - accuracy;
      double miny = aabb.minY + accuracy;
      double maxy = aabb.maxY - accuracy;
      double minz = aabb.minZ + accuracy;
      double maxz = aabb.maxZ - accuracy;

      for (double x = minx; x <= maxx; x += accuracy) {
         for (double y = miny; y <= maxy; y += accuracy) {
            for (double z = minz; z <= maxz; z += accuracy) {
               if (Minecraft.player.canEntityBeSeenCoords(entity.posX + x, entity.posY + y, entity.posZ + z)
                  && MathUtils.getPointedEntity(new Vector2f(yaw, pitch), 8.0, 1.0F, true) == null) {
                  vec3ds.add(new Vec3d(x, y, z));
               }
            }
         }
      }

      vec3ds.sort(
         new Comparator<Vec3d>() {
            public int compare(Vec3d o1, Vec3d o2) {
               float d = RotationUtil.getDistance(o1, new Vec3d(0.0, (double)entity.getEyeHeight(), 0.0))
                  - RotationUtil.getDistance(o2, new Vec3d(0.0, (double)entity.getEyeHeight(), 0.0));
               System.out.println();
               return (int)(d * 100000.0F);
            }
         }
      );
      return vec3ds.get(0);
   }

   public static float getDistance(Vec3d vec3d1, Vec3d vec3d2) {
      float f = (float)(vec3d1.xCoord - vec3d2.xCoord);
      float f1 = (float)(vec3d1.yCoord - vec3d2.yCoord);
      float f2 = (float)(vec3d1.zCoord - vec3d2.zCoord);
      return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   public static float[] rotate(Entity base, boolean attackContext) {
      Vec3d pos = getBestPos(base);
      float pitchToHead = 0.0F;
      EntityPlayerSP client = Minecraft.player;
      double x = base.posX + pos.xCoord - client.posX;
      double y = base.posY + pos.yCoord - client.getPositionEyes(1.0F).yCoord;
      double z = base.posZ + pos.zCoord - client.posZ;
      double dst = Math.sqrt(Math.pow(x, 2.0) + Math.pow(z, 2.0));
      pitchToHead = (float)(-Math.toDegrees(Math.atan2(y, dst)));
      float sensitivity = 1.0001F;
      double xx = base.posX + pos.xCoord - client.posX;
      double yx = base.posY + pos.yCoord - client.getPositionEyes(1.0F).yCoord;
      double zx = base.posZ + pos.zCoord - client.posZ;
      double dstx = Math.sqrt(Math.pow(xx, 2.0) + Math.pow(zx, 2.0));
      float yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(zx, xx)) - 90.0);
      float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(yx, dstx)));
      float yawDelta = MathHelper.wrapDegrees(yawToTarget - Minecraft.player.lastReportedYaw) / sensitivity;
      float pitchDelta = (pitchToTarget - EntityPlayerSP.lastReportedPitch) / sensitivity;
      if (yawDelta > 180.0F) {
         yawDelta -= 180.0F;
      }

      int yawDeltaAbs = (int)Math.abs(yawDelta);
      if (yawDeltaAbs < 360) {
         float pitchDeltaAbs = Math.abs(pitchDelta);
         float additionYaw = (float)Math.min(Math.max(yawDeltaAbs, 1), 80);
         float additionPitch = Math.max(attackContext ? pitchDeltaAbs : 1.0F, 2.0F);
         if (Math.abs(additionYaw - prevAdditionYaw) <= 3.0F) {
            additionYaw = prevAdditionYaw + 3.1F;
         }

         float newYaw = Minecraft.player.lastReportedYaw + (yawDelta > 0.0F ? additionYaw : -additionYaw) * sensitivity;
         float newPitch = MathHelper.clamp(EntityPlayerSP.lastReportedPitch + (pitchDelta > 0.0F ? additionPitch : -additionPitch) * sensitivity, -90.0F, 90.0F);
         prevAdditionYaw = additionYaw;
         return new float[]{newYaw, newPitch};
      } else {
         return null;
      }
   }

   public static Vec3d getResolvePos(Entity entity, double accuracy) {
      double renderOffsetX = (entity.posX - entity.prevPosX) * (double)mc.getRenderPartialTicks();
      double renderOffsetY = (entity.posY - entity.prevPosY) * (double)mc.getRenderPartialTicks();
      double renderOffsetZ = (entity.posZ - entity.prevPosZ) * (double)mc.getRenderPartialTicks();
      AxisAlignedBB aabb = entity.getEntityBoundingBox().offset(renderOffsetX, renderOffsetY, renderOffsetZ);
      List<Vec3d> points = generateMultipoints(aabb, accuracy);
      if (points.isEmpty()) {
         return null;
      } else {
         points.sort(Comparator.comparingDouble(multipoint -> multipoint.distanceTo(Minecraft.player.getPositionEyes(mc.getRenderPartialTicks()))));
         return points.get(0);
      }
   }

   public static Vec3d getResolvePosOfFake(EntityLivingBase fake, Entity entity, double accuracy) {
      double renderOffsetX = (entity.posX - entity.prevPosX) * (double)mc.getRenderPartialTicks();
      double renderOffsetY = (entity.posY - entity.prevPosY) * (double)mc.getRenderPartialTicks();
      double renderOffsetZ = (entity.posZ - entity.prevPosZ) * (double)mc.getRenderPartialTicks();
      AxisAlignedBB aabb = entity.getEntityBoundingBox().offset(renderOffsetX, renderOffsetY, renderOffsetZ);
      List<Vec3d> points = generateMultipoints(aabb, accuracy);
      if (points.isEmpty()) {
         return null;
      } else {
         points.sort(
            Comparator.comparingDouble(
               multipoint -> multipoint.distanceTo(
                     fake != null ? fake.getPositionEyes(mc.getRenderPartialTicks()) : Minecraft.player.getPositionEyes(mc.getRenderPartialTicks())
                  )
            )
         );
         return points.get(0);
      }
   }

   private static List<Vec3d> generateMultipoints(AxisAlignedBB aabb, double accuracy) {
      List<Vec3d> multipoints = new ArrayList<>();
      accuracy = 1.0 / accuracy;

      for (double x = aabb.minX; x < aabb.maxX; x += accuracy * (aabb.maxX - aabb.minX)) {
         for (double y = aabb.minY; y < aabb.maxY; y += accuracy * (aabb.maxY - aabb.minY)) {
            for (double z = aabb.minZ; z < aabb.maxZ; z += accuracy * (aabb.maxZ - aabb.minZ)) {
               multipoints.add(new Vec3d(x, y, z));
            }
         }
      }

      return multipoints;
   }

   public static float[] getMatrixRots(Entity entityIn) {
      double d = entityIn.posX - Minecraft.player.posX;
      double d1 = entityIn.posZ - Minecraft.player.posZ;
      if (entityIn instanceof EntityLivingBase entitylivingbase) {
         ;
      }

      EntityLivingBase entitylivingbase = (EntityLivingBase)entityIn;
      float y = (float)(entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (double)(entitylivingbase.getEyeHeight() / 3.0F));
      double lastY = (double)y + 0.5 - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      if (Minecraft.player.posY + 1.5 < entityIn.posY) {
         if (Minecraft.player.getDistanceToEntity(entityIn) > 3.0F) {
            lastY = (double)y - 0.7F - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
         } else {
            lastY = (double)y - 0.2F - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
         }
      }

      if (Minecraft.player.posY > entityIn.posY + 2.0) {
         lastY = (double)y + 1.2F - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      if (Minecraft.player.posY > entityIn.posY + 2.5) {
         if (Minecraft.player.getDistanceSqToEntity(entityIn) <= 3.0) {
            lastY = (double)y + 1.0 - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
         } else {
            lastY = (double)y + 1.2F - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
         }
      }

      if (Minecraft.player.posY > entityIn.posY + 3.5) {
         lastY = (double)y + 2.2F - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      if (Minecraft.player.posY > entityIn.posY + 4.5) {
         lastY = (double)y + 2.2F - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double d2 = (double)MathHelper.sqrt(d * d + d1 * d1);
      float yaw = (float)(Math.atan2(d1, d) * 180.0 / Math.PI - 92.0) + RandomUtils.nextFloat(1.0F, 6.0F);
      float pitch = (float)(-(Math.atan2(lastY, d2) * 210.0 / Math.PI)) + RandomUtils.nextFloat(1.0F, 6.0F);
      if (Minecraft.player.getDistanceToEntity(entityIn) <= 3.0F) {
         yaw = (float)(Math.atan2(d1, d) * 180.0 / Math.PI - 92.0) + RandomUtils.nextFloat(1.0F, 5.0F);
         pitch = (float)(-(Math.atan2(lastY, d2) * 200.0 / Math.PI)) + RandomUtils.nextFloat(1.0F, 7.0F);
      }

      if (Minecraft.player.getDistanceToEntity(entityIn) >= 2.0F) {
         yaw = (float)(Math.atan2(d1, d) * 180.0 / Math.PI - 92.0) + RandomUtils.nextFloat(1.0F, 6.0F);
         pitch = (float)(-(Math.atan2(lastY, d2) * 200.0 / Math.PI)) + RandomUtils.nextFloat(1.0F, 6.0F);
      }

      if (Minecraft.player.getDistanceToEntity(entityIn) >= 4.0F) {
         yaw = (float)(Math.atan2(d1, d) * 180.0 / Math.PI - 92.0) + RandomUtils.nextFloat(1.0F, 5.0F);
         pitch = (float)(-(Math.atan2(lastY, d2) * 200.0 / Math.PI)) + RandomUtils.nextFloat(1.0F, 5.0F);
      }

      if (Minecraft.player.getDistanceToEntity(entityIn) <= 0.5F) {
         yaw = (float)(Math.atan2(d1, d) * 180.0 / Math.PI - 92.0) + RandomUtils.nextFloat(1.0F, 6.0F);
         pitch = (float)(-(Math.atan2(lastY, d2) * 180.0 / Math.PI)) + RandomUtils.nextFloat(1.0F, 6.0F);
      }

      yaw = Minecraft.player.rotationYaw + getSensitivity(MathHelper.wrapDegrees(yaw - Minecraft.player.rotationYaw));
      pitch = Minecraft.player.rotationPitch + getSensitivity(MathHelper.wrapDegrees(pitch - Minecraft.player.rotationPitch));
      pitch = MathHelper.clamp(pitch, -89.999F, 87.5F);
      Yaw = Yaw + getSensitivity(MathHelper.clamp(MathHelper.wrapDegrees(yaw - Yaw), -80.0F, 80.0F));
      Pitch = Pitch + getSensitivity(MathHelper.clamp(pitch - Pitch, -5.0F, 5.0F));
      return new float[]{Yaw, Pitch};
   }

   public static float[] getRots(Entity entityIn, boolean random, boolean turnAwayBoundingBox) {
      Vec3d to = entityIn.getBestVec3dOnEntityBox();
      if (to == null) {
         to = entityIn.getPositionVector().addVector(0.0, (double)entityIn.getEyeHeight(), 0.0);
      }

      float[] rotate = new float[]{Yaw, Pitch};

      try {
         rotate = getNeededFacing(to, random && !turnAwayBoundingBox, Minecraft.player, turnAwayBoundingBox);
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return rotate;
   }

   public static float[] getRots2(Entity entityIn, boolean random, boolean turnAwayBoundingBox) {
      return getNeededFacing2((EntityLivingBase)entityIn, random && !turnAwayBoundingBox, turnAwayBoundingBox);
   }

   public static float[] getRots3(Entity entityIn, boolean random, boolean turnAwayBoundingBox) {
      Vec3d to = entityIn.getBestVec3dOnEntityBox();
      if (to == null) {
         to = entityIn.getPositionVector().addVector(0.0, (double)entityIn.getEyeHeight(), 0.0);
      }

      float[] rotate = new float[]{Yaw, Pitch};

      try {
         rotate = getNeededFacingNCP(to, random && !turnAwayBoundingBox, Minecraft.player, turnAwayBoundingBox);
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return rotate;
   }

   public static boolean canEntityBeSeen(Vec3d pos, Vec3d pos2) {
      return mc.world.rayTraceBlocks(pos, pos2, false, true, false) == null;
   }

   public static Vec3d getPos(EntityLivingBase target, float range) {
      Vec3d vec3d = null;

      for (Vec3d vec : getPosityionEye(target, range)) {
         if (canEntityBeSeen(vec, Minecraft.player.getPositionVector())) {
            vec3d = vec;
         }
      }

      return vec3d;
   }

   public static ArrayList<Vec3d> getPosityionEye(EntityLivingBase target, float range) {
      List<Vec3d> posses = new ArrayList<>();
      float X = (float)((target.posX - target.prevPosX) * (double)mc.getRenderPartialTicks());
      float Y = (float)((target.posY - target.prevPosY) * (double)mc.getRenderPartialTicks());
      float Z = (float)((target.posZ - target.prevPosZ) * (double)mc.getRenderPartialTicks());
      AxisAlignedBB aabb = target.getEntityBoundingBox().offset((double)X, (double)Y, (double)Z);
      double accuracy = 0.05F;
      double minx = aabb.minX + accuracy;
      double maxx = aabb.maxX + accuracy;
      double miny = aabb.minY + accuracy;
      double maxy = aabb.maxY - accuracy;
      double minz = aabb.minZ - accuracy;
      double maxz = aabb.maxZ - accuracy;

      for (double x = minx; x <= maxx; x += accuracy) {
         for (double y = miny; y <= maxy; y += accuracy) {
            for (double z = minz; z <= maxz; z += accuracy) {
               Vec3d vec = new Vec3d(x, y, z);
               if (getDistance(vec, Minecraft.player.getPositionVector()) <= range) {
                  range = getDistance(vec, Minecraft.player.getPositionVector());
                  posses.add(vec);
               }
            }
         }
      }

      return (ArrayList<Vec3d>)posses;
   }

   public static float[] getMatrixRots2(Entity e, float range) {
      double raz = MathUtils.clamp(
         Minecraft.player.posY + (double)Minecraft.player.getEyeHeight() - e.posY,
         (double)e.height * 0.3,
         (double)Math.min(Minecraft.player.getEyeHeight(), e.height * 0.8F)
      );
      double diffY = e.posY + raz - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      vec = getPos((EntityLivingBase)e, range);
      double diffX;
      double diffZ;
      if (vec != null && Minecraft.player.getSmoothDistanceToEntityXZ(e) > 2.0F) {
         diffX = vec.xCoord - Minecraft.player.getPositionVector().xCoord;
         diffZ = vec.zCoord - Minecraft.player.getPositionVector().zCoord;
         if ((double)Minecraft.player.getDistanceToEntity(e) <= 0.5 || (int)Minecraft.player.posX == (int)e.posX && (int)Minecraft.player.posZ == (int)e.posZ) {
            diffX = e.posX - Minecraft.player.posX;
            diffZ = e.posZ - Minecraft.player.posZ;
         }
      } else {
         diffX = e.posX - Minecraft.player.posX;
         diffZ = e.posZ - Minecraft.player.posZ;
      }

      if (e instanceof EntityLivingBase entitylivingbase) {
         ;
      }

      EntityLivingBase entitylivingbase = (EntityLivingBase)e;
      float y = (float)(raz + Minecraft.player.posY);
      double distance = (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
      float randomXZ = (float)MathUtils.randomNumber(1.0, -1.0);
      float randomY = (float)MathUtils.randomNumber(0.5, -0.5);
      if (MathUtils.getPointedEntity(new Vector2f(Yaw, Pitch), (double)range, 1.0F, true) == null) {
         randomXZ = -randomXZ;
         randomY = -randomY;
      }

      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 92.0) + randomXZ;
      float pitch = (float)(-(Math.atan2(diffY, distance) * 210.0 / Math.PI)) + randomY;
      if (Minecraft.player.getDistanceToEntity(e) <= 3.0F) {
         yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 92.0) + randomXZ;
         pitch = (float)(-(Math.atan2(diffY, distance) * 170.0 / Math.PI)) + randomY;
      }

      if (Minecraft.player.getDistanceToEntity(e) >= 2.0F) {
         yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 92.0) + randomXZ;
         pitch = (float)(-(Math.atan2(diffY, distance) * 200.0 / Math.PI)) + randomY;
      }

      if (Minecraft.player.getDistanceToEntity(e) >= 4.0F) {
         yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 92.0) + randomXZ;
         pitch = (float)(-(Math.atan2(diffY, distance) * 200.0 / Math.PI)) + randomY;
      }

      if (Minecraft.player.getDistanceToEntity(e) <= 0.5F) {
         yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 92.0) + randomXZ;
         pitch = (float)(-(Math.atan2(diffY, distance) * 180.0 / Math.PI)) + randomY;
      }

      if (Minecraft.player.getDistanceToEntity(e) <= 1.0F) {
         yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 92.0) + randomXZ;
         pitch = (float)(-(Math.atan2(diffY, distance) * 180.0 / Math.PI)) + randomY;
      }

      yaw = Minecraft.player.rotationYaw + getSensitivity(MathHelper.wrapDegrees(yaw - Minecraft.player.rotationYaw));
      pitch = Minecraft.player.rotationPitch + getSensitivity(MathHelper.wrapDegrees(pitch - Minecraft.player.rotationPitch));
      pitch = MathHelper.clamp(pitch, -89.999F, 87.5F);
      Yaw = Yaw + getSensitivity(MathHelper.clamp(MathHelper.wrapDegrees(yaw - Yaw), -(30.0F + randomXZ), 30.0F + randomXZ));
      Pitch = Pitch + getSensitivity(MathHelper.clamp(pitch - Pitch, -(10.0F + randomY), 10.0F + randomY));
      return new float[]{Yaw, Pitch};
   }

   public static float getSensitivity(float rot) {
      return round(rot) * lese();
   }

   public static float round(float delta) {
      return (float)Math.round(delta / lese());
   }

   public static float lese() {
      return (float)((double)getLastSensivity() * 0.15);
   }

   public static float getLastSensivity() {
      float sensivity = (float)((double)mc.gameSettings.mouseSensitivity * 0.6 + 0.2);
      return sensivity * sensivity * sensivity * 8.0F;
   }

   public static float RotateHui(float from, float to, float minstep, float maxstep) {
      float f = MathUtils.wrapDegrees(to - from) * MathUtils.clamp(0.6F, 0.0F, 1.0F);
      if (f < 0.0F) {
         f = MathUtils.clamp(f, -maxstep, -minstep);
      } else {
         f = MathUtils.clamp(f, minstep, maxstep);
      }

      return Math.abs(f) > Math.abs(MathUtils.wrapDegrees(to - from)) ? to : from + f;
   }

   public static float[] getRotationsHui(Entity entityIn) {
      double diffX = entityIn.posX - Minecraft.player.posX;
      double diffZ = entityIn.posZ - Minecraft.player.posZ;
      double diffY;
      if (entityIn instanceof EntityLivingBase) {
         diffY = entityIn.posY + (double)entityIn.getEyeHeight() - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight()) - 0.2F;
      } else {
         diffY = (entityIn.getEntityBoundingBox().minY + entityIn.getEntityBoundingBox().maxY) / 2.0
            - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      if (!Minecraft.player.canEntityBeSeen(entityIn)) {
         diffY = entityIn.posY + (double)entityIn.height - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double diffXZ = (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0 + (double)GCDFix.getFixedRotation((float)RandomUtils.getRandomDouble(-1.75, 1.75)));
      float pitch = (float)(Math.toDegrees(-Math.atan2(diffY, diffXZ)) + (double)GCDFix.getFixedRotation((float)RandomUtils.getRandomDouble(-1.8F, 1.75)));
      yaw = Minecraft.player.rotationYaw + GCDFix.getFixedRotation(MathHelper.wrapDegrees(yaw - Minecraft.player.rotationYaw));
      pitch = Minecraft.player.rotationPitch + GCDFix.getFixedRotation(MathHelper.wrapDegrees(pitch - Minecraft.player.rotationPitch));
      pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
      return new float[]{yaw, pitch};
   }

   public static float[] getSunriseRots(Entity e) {
      float[] rots = getRotationsHui(e);
      Yaw = GCDFix.getFixedRotation(RotateHui(Yaw, rots[0], 40.0F, 50.0F));
      Pitch = GCDFix.getFixedRotation(RotateHui(Pitch, rots[1], 0.35F, 2.1F));
      return new float[]{Yaw, Pitch};
   }

   public static float[] getRotationsOfFakeEnt(EntityLivingBase me, Entity e) {
      if (e != null && me != null) {
         double diffY = getResolvePosOfFake(me, e, 9.0).yCoord - me.posY - (double)me.getEyeHeight();
         double diffX = getResolvePosOfFake(me, e, 6.0).xCoord - me.posX;
         double diffZ = getResolvePosOfFake(me, e, 6.0).zCoord - me.posZ;
         double dist = (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
         float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
         float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
         yaw = me.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - me.rotationYaw);
         pitch = me.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - me.rotationPitch);
         pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);
         return new float[]{yaw, pitch};
      } else {
         return new float[]{0.0F, 0.0F};
      }
   }

   public static float[] getMatrixRotations4(Entity target) {
      double XDiff = target.posX - target.lastTickPosX;
      double YDiff = target.posY - target.lastTickPosY;
      double ZDiff = target.posZ - target.lastTickPosZ;
      float predict = 2.0F;
      double x = target.posX + XDiff * (double)predict;
      double y = target.posY + YDiff * (double)predict;
      double z = target.posZ + ZDiff * (double)predict;
      double diffX = x - Minecraft.player.posX;
      double diffZ = z - Minecraft.player.posZ;
      double diffY = y + (double)Minecraft.player.getEyeHeight() - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight()) - 0.4;
      double dist = (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)((double)((float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0)) + MathUtils.getRandomInRange(-1.6F, 1.6F));
      float pitch = (float)((double)((float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI))) + MathUtils.getRandomInRange(-1.6F, 1.6F));
      yaw = Minecraft.player.rotationYaw + GCDFix.getFixedRotation(MathHelper.wrapDegrees(yaw - Minecraft.player.rotationYaw));
      yaw -= (Minecraft.player.lastReportedPreYaw - Minecraft.player.rotationYaw) * 2.0F;
      pitch = Minecraft.player.rotationPitch + GCDFix.getFixedRotation(MathHelper.wrapDegrees(pitch - Minecraft.player.rotationPitch));
      pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
      return new float[]{yaw, pitch};
   }

   public static float[] getMatrixRotations4BlockPos(BlockPos target) {
      double x = (double)((float)target.getX() + 0.5F);
      double y = (double)((float)target.getY() - 0.5F);
      double z = (double)((float)target.getZ() + 0.5F);
      double diffX = x - Minecraft.player.posX;
      double diffZ = z - Minecraft.player.posZ;
      double diffY = y + (double)Minecraft.player.getEyeHeight() - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight()) - 0.4;
      double dist = (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
      float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
      yaw = Minecraft.player.rotationYaw + getSensitivity(MathHelper.wrapDegrees(yaw - Minecraft.player.rotationYaw));
      pitch = Minecraft.player.rotationPitch + getSensitivity(MathHelper.wrapDegrees(pitch - Minecraft.player.rotationPitch));
      pitch = MathHelper.clamp(pitch, -89.999F, 87.5F);
      Yaw = Yaw + getSensitivity(MathHelper.clamp(MathHelper.wrapDegrees(yaw - Yaw), -20.0F, 20.0F));
      Pitch = Pitch + getSensitivity(MathHelper.clamp(pitch - Pitch, -5.0F, 5.0F));
      return new float[]{Yaw, Pitch};
   }

   public static float[] getLookRotations(Entity e, boolean oldPositionUse) {
      double diffX = (oldPositionUse ? e.prevPosX : e.posX) - (oldPositionUse ? Minecraft.player.prevPosX : Minecraft.player.posX);
      double diffZ = (oldPositionUse ? e.prevPosZ : e.posZ) - (oldPositionUse ? Minecraft.player.prevPosZ : Minecraft.player.posZ);
      double diffY;
      if (e instanceof EntityLivingBase entitylivingbase) {
         float randomed = RandomUtils.nextFloat(
            (float)(entitylivingbase.posY + (double)(entitylivingbase.getEyeHeight() / 1.05F)),
            (float)(entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (double)(entitylivingbase.getEyeHeight() / 3.0F))
         );
         diffY = (double)randomed - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      } else {
         diffY = (double)RandomUtils.nextFloat((float)e.getEntityBoundingBox().minY, (float)e.getEntityBoundingBox().maxY)
            - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double dist = (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
      float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
      yaw = Minecraft.player.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - Minecraft.player.rotationYaw);
      pitch = Minecraft.player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - Minecraft.player.rotationPitch);
      pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);
      return new float[]{yaw, pitch};
   }

   public static float[] getLookRots(Entity self, Entity ent) {
      double diffX = ent.posX - self.posX;
      double diffZ = ent.posZ - self.posZ;
      double diffY;
      if (ent instanceof EntityLivingBase entitylivingbase) {
         float randomed = RandomUtils.nextFloat(
            (float)(entitylivingbase.posY + (double)(entitylivingbase.getEyeHeight() / 1.05F)),
            (float)(entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (double)(entitylivingbase.getEyeHeight() / 3.0F))
         );
         diffY = (double)randomed - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      } else {
         diffY = (double)RandomUtils.nextFloat((float)ent.getEntityBoundingBox().minY, (float)ent.getEntityBoundingBox().maxY)
            - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double dist = (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
      float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
      yaw = Minecraft.player.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - Minecraft.player.rotationYaw);
      pitch = Minecraft.player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - Minecraft.player.rotationPitch);
      pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);
      return new float[]{yaw, pitch};
   }

   public static float[] getRatationsG1(Entity entity) {
      double diffX = entity.posX - Minecraft.player.posX;
      double diffZ = entity.posZ - Minecraft.player.posZ;
      double diffY;
      if (entity instanceof EntityLivingBase) {
         diffY = entity.posY + (double)entity.getEyeHeight() - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight()) - 2.8;
      } else {
         diffY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0
            - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double dist = (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0) + CountHelper.nextFloat(-1.5F, 2.0F);
      float yawBody = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
      float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / 5.0 + (double)CountHelper.nextFloat(-1.0F, 1.0F)));
      float pitch2 = (float)(-(Math.atan2(diffY, dist) * 180.0 / 5.0));
      if (Math.abs(yaw - Minecraft.player.rotationYaw) > 160.0F) {
         Minecraft.player.setSprinting(false);
      }

      yaw = Minecraft.player.prevRotationYaw + GCDFix.getFixedRotation(MathHelper.wrapDegrees(yaw - Minecraft.player.rotationYaw));
      yawBody = Minecraft.player.prevRotationYaw + MathHelper.wrapDegrees(yawBody - Minecraft.player.rotationYaw);
      pitch = Minecraft.player.prevRotationPitch + GCDFix.getFixedRotation(MathHelper.wrapDegrees(pitch - Minecraft.player.rotationPitch));
      pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
      return new float[]{yaw, pitch, yawBody, pitch2};
   }

   public static float[] getBowRotations(Entity e, boolean oldPositionUse) {
      double diffX = (oldPositionUse ? e.prevPosX : e.posX) - (oldPositionUse ? Minecraft.player.prevPosX : Minecraft.player.posX);
      double diffZ = (oldPositionUse ? e.prevPosZ : e.posZ) - (oldPositionUse ? Minecraft.player.prevPosZ : Minecraft.player.posZ);
      double diffY;
      if (e instanceof EntityLivingBase entitylivingbase) {
         float randomed = RandomUtils.nextFloat(
            (float)(entitylivingbase.posY + (double)(entitylivingbase.getEyeHeight() / 1.5F)),
            (float)(entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (double)(entitylivingbase.getEyeHeight() / 3.0F))
         );
         diffY = (double)randomed - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      } else {
         diffY = (double)RandomUtils.nextFloat((float)e.getEntityBoundingBox().minY, (float)e.getEntityBoundingBox().maxY)
            - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double dist = (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
      float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
      yaw = Minecraft.player.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - Minecraft.player.rotationYaw);
      pitch = Minecraft.player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - Minecraft.player.rotationPitch);
      pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);
      return new float[]{yaw, pitch};
   }

   public static float getYawToEntity(Entity entity) {
      double pX = Minecraft.player.posX;
      double pZ = Minecraft.player.posZ;
      double eX = entity.posX;
      double eZ = entity.posZ;
      double dX = pX - eX;
      double dZ = pZ - eZ;
      double yaw = Math.toDegrees(Math.atan2(dZ, dX)) + 90.0;
      return (float)yaw;
   }

   public static float getYawToEntity(Entity mainEntity, Entity targetEntity) {
      double pX = mainEntity.posX;
      double pZ = mainEntity.posZ;
      double eX = targetEntity.posX;
      double eZ = targetEntity.posZ;
      double dX = pX - eX;
      double dZ = pZ - eZ;
      double yaw = Math.toDegrees(Math.atan2(dZ, dX)) + 90.0;
      return (float)yaw;
   }

   public static float getNormalizedYaw(float yaw) {
      float yawStageFirst = yaw % 360.0F;
      if (yawStageFirst > 180.0F) {
         float var3;
         return var3 = yawStageFirst - 360.0F;
      } else {
         float var2;
         return yawStageFirst < -180.0F ? (var2 = yawStageFirst + 360.0F) : yawStageFirst;
      }
   }

   public static boolean isAimAtMe(Entity entity) {
      float entityYaw = getNormalizedYaw(entity.rotationYaw);
      float entityPitch = entity.rotationPitch;
      double pMinX = Minecraft.player.getEntityBoundingBox().minX;
      double pMaxX = Minecraft.player.getEntityBoundingBox().maxX;
      double pMaxY = Minecraft.player.posY + (double)Minecraft.player.height;
      double pMinY = Minecraft.player.getEntityBoundingBox().minY;
      double pMaxZ = Minecraft.player.getEntityBoundingBox().maxZ;
      double pMinZ = Minecraft.player.getEntityBoundingBox().minZ;
      double eX = entity.posX;
      double eY = entity.posY + (double)(entity.height / 2.0F);
      double eZ = entity.posZ;
      double dMaxX = pMaxX - eX;
      double dMaxY = pMaxY - eY;
      double dMaxZ = pMaxZ - eZ;
      double dMinX = pMinX - eX;
      double dMinY = pMinY - eY;
      double dMinZ = pMinZ - eZ;
      double dMinH = Math.sqrt(Math.pow(dMinX, 2.0) + Math.pow(dMinZ, 2.0));
      double dMaxH = Math.sqrt(Math.pow(dMaxX, 2.0) + Math.pow(dMaxZ, 2.0));
      double maxPitch = 90.0 - Math.toDegrees(Math.atan2(dMaxH, dMaxY));
      double minPitch = 90.0 - Math.toDegrees(Math.atan2(dMinH, dMinY));
      boolean yawAt = Math.abs(getNormalizedYaw(getYawToEntity(entity, Minecraft.player)) - entityYaw)
         <= 16.0F - Minecraft.player.getDistanceToEntity(entity) / 2.0F;
      boolean pitchAt = maxPitch >= (double)entityPitch && (double)entityPitch >= minPitch
         || minPitch >= (double)entityPitch && (double)entityPitch >= maxPitch;
      return yawAt && pitchAt;
   }

   public static float getSensitivityMultiplier() {
      float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
      return f * f * f * 8.0F * 0.15F;
   }

   public static float getYawToPos(BlockPos blockPos) {
      int pX = Minecraft.player.getPosition().getX();
      int pZ = Minecraft.player.getPosition().getZ();
      double dX = (double)(pX - blockPos.getX());
      double dZ = (double)(pZ - blockPos.getZ());
      double yaw = Math.toDegrees(Math.atan2(dZ, dX)) + 90.0;
      return (float)yaw;
   }

   public static float[] faceTarget(Entity target, float p_706252, float p_706253, boolean miss) {
      double posX = target.posX - Minecraft.player.posX;
      double posZ = target.posZ - Minecraft.player.posZ;
      double posY;
      if (target instanceof EntityLivingBase var6) {
         posY = var6.posY + (double)var6.getEyeHeight() - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      } else {
         posY = (target.getEntityBoundingBox().minY + target.getEntityBoundingBox().maxY) / 2.0
            - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      float range = Minecraft.player.getDistanceToEntity(target);
      float calculate = MathHelper.sqrt(posX * posX + posZ * posZ);
      float var9 = (float)(Math.atan2(posZ, posX) * 180.0 / Math.PI)
         - 90.0F
         + (float)RandomUtils.randomNumber((int)(4.0F / range + calculate), (int)(-5.0F / range + calculate));
      float var10 = (float)(
         -(Math.atan2(posY, (double)calculate) * 180.0 / Math.PI)
            + (double)RandomUtils.randomNumber((int)(5.0F / range + calculate), (int)(-4.0F / range + calculate))
      );
      float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
      float pitch = updateRotation(Minecraft.player.rotationPitch, var10, p_706253);
      float yaw = updateRotation(Minecraft.player.rotationYaw, var9, p_706252);
      float gcd = f * f * f * 1.2F + (float)RandomUtils.randomNumber((int)f, (int)(-f));
      yaw -= yaw % gcd;
      pitch -= pitch % gcd;
      return new float[]{yaw, pitch};
   }

   public static float updateRotation(float current, float intended, float speed) {
      float f = MathHelper.wrapDegrees(intended - current);
      if (f > speed) {
         f = speed;
      }

      if (f < -speed) {
         f = -speed;
      }

      return current + f;
   }

   public static float fixRotation(float p_70663_1_, float p_70663_2_) {
      float var4 = MathHelper.wrapDegrees(p_70663_2_ - p_70663_1_);
      if (var4 > 360.0F) {
         var4 = 360.0F;
      }

      if (var4 < -360.0F) {
         var4 = -360.0F;
      }

      return p_70663_1_ + var4;
   }

   public static float[] getRotationFromPosition(double x, double z, double y) {
      double xDiff = x - Minecraft.player.posX;
      double zDiff = z - Minecraft.player.posZ;
      double yDiff = y - Minecraft.player.posY - 1.2;
      double dist = (double)MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff);
      float yaw = (float)(Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-(Math.atan2(yDiff, dist) * 180.0 / Math.PI));
      return new float[]{yaw, pitch};
   }

   public static Vec3d getEyesPos() {
      return new Vec3d(Minecraft.player.posX, Minecraft.player.posY + (double)Minecraft.player.getEyeHeight(), Minecraft.player.posZ);
   }

   static float[] getTurnAwayedRotate(float[] prevRotate, double randomYawPos) {
      float yawPlus = 0.0F;
      float pitchPlus = 0.0F;
      boolean saded = false;
      randomYawPos = randomYawPos > 0.0 ? 1.0 : -1.0;

      for (int ext = 0; ext <= 30; ext++) {
         if (MathUtils.getPointedEntity(new Vector2f(prevRotate[0] + yawPlus, prevRotate[1]), 6.0, 1.0F, false) != null) {
            yawPlus = (float)((double)yawPlus + 6.0 * randomYawPos);
         }
      }

      boolean saded2 = false;

      for (int extx = 0; extx <= 30; extx++) {
         if (MathUtils.getPointedEntity(new Vector2f(prevRotate[0], prevRotate[1] + pitchPlus), 6.0, 1.0F, false) != null) {
            pitchPlus = (float)((double)pitchPlus + 3.0 * randomYawPos);
         }
      }

      float newYaw = prevRotate[0];
      float newPitch = prevRotate[1];
      if (Math.abs(yawPlus) > Math.abs(pitchPlus)) {
         newPitch += pitchPlus;
      } else {
         newYaw += yawPlus;
      }

      return new float[]{newYaw, newPitch};
   }

   static float randF(float min, float max) {
      float random = new Random().nextFloat();
      return MathUtils.clamp(min + (max - min) * random, min, max);
   }

   public static float[] getVecNeeded(Vec3d vec, Vec3d rotateAt) {
      double[] diffs = new double[]{vec.xCoord - rotateAt.xCoord, vec.yCoord - rotateAt.yCoord, vec.zCoord - rotateAt.zCoord};
      diffs = new double[]{diffs[0], diffs[1], diffs[2], Math.sqrt(diffs[0] * diffs[0] + diffs[2] * diffs[2])};
      float R_T_D = (float) (180.0 / Math.PI);
      return new float[]{
         (float)Math.atan2(diffs[2], diffs[0]) * (float) (180.0 / Math.PI) - 90.0F, (float)Math.atan2(diffs[1], diffs[3]) * (float) (-180.0 / Math.PI)
      };
   }

   public static float[] getNeededFacing(Vec3d vec, boolean randomizer, Entity rotateAt, boolean turnAwayBoundingBox) {
      Vec3d eyesPos = new Vec3d(rotateAt.posX, rotateAt.posY + (double)rotateAt.getEyeHeight(), rotateAt.posZ);
      double[] diffs = new double[]{vec.xCoord - eyesPos.xCoord, vec.yCoord - eyesPos.yCoord, vec.zCoord - eyesPos.zCoord};
      diffs = new double[]{diffs[0], diffs[1], diffs[2], Math.sqrt(diffs[0] * diffs[0] + diffs[2] * diffs[2])};
      float R_T_D = (float) (180.0 / Math.PI);
      float yaw = (float)Math.atan2(diffs[2], diffs[0]) * (float) (180.0 / Math.PI) - 90.0F;
      float pitch = (float)Math.atan2(diffs[1], diffs[3]) * (float) (-180.0 / Math.PI);
      float randYaw = 0.0F;
      if (randomizer) {
         float randStrengh = 1.07F * MathUtils.clamp(1.0F - (float)rotateAt.getDistanceAtVec3dToVec3d(eyesPos, vec) / 2.25F, 0.0F, 1.0F);
         yaw = Yaw + getFixedRotation(yaw - Yaw) + (randomizer ? (randYaw = randF(-randStrengh, randStrengh)) : 0.0F);
         pitch = Pitch + getFixedRotation(pitch - Pitch) + (randomizer ? randF(-randStrengh, randStrengh) : 0.0F);
      }

      if (turnAwayBoundingBox) {
         float[] turnedRotate = getTurnAwayedRotate(new float[]{yaw, pitch}, randomizer ? (double)randYaw : 0.0);
         yaw = turnedRotate[0];
         pitch = turnedRotate[1];
      }

      yaw = GCDFix.getFixedRotation(rotateAt.rotationYaw + MathHelper.wrapDegrees(yaw - rotateAt.rotationYaw));
      pitch = GCDFix.getFixedRotation(rotateAt.rotationPitch + MathHelper.wrapDegrees(pitch - rotateAt.rotationPitch));
      if (randomizer) {
         float randomSpeed = 0.85F + 0.15F * randF(0.0F, 1.0F);
         float speedYaw = 73.15F + 14.65F * randomSpeed;
         float speedPitch = 14.45F + 6.475F * randomSpeed;
         Yaw = GCDFix.getFixedRotation(Yaw + MathHelper.clamp(MathHelper.wrapDegrees(yaw - Yaw), -speedYaw, speedYaw));
         Pitch = GCDFix.getFixedRotation(
            Pitch + (float)MathHelper.clamp(MathHelper.clamp((double)pitch, -89.5, 89.5) - (double)Pitch, (double)(-speedPitch), (double)speedPitch)
         );
      }

      return new float[]{randomizer ? Yaw : yaw, randomizer ? Pitch : pitch};
   }

   public static float[] getNeededFacingNCP(Vec3d vec, boolean randomizer, Entity rotateAt, boolean turnAwayBoundingBox) {
      Vec3d eyesPos = new Vec3d(rotateAt.posX, rotateAt.posY + (double)rotateAt.getEyeHeight(), rotateAt.posZ);
      double[] diffs = new double[]{vec.xCoord - eyesPos.xCoord, vec.yCoord - eyesPos.yCoord, vec.zCoord - eyesPos.zCoord};
      diffs = new double[]{diffs[0], diffs[1], diffs[2], Math.sqrt(diffs[0] * diffs[0] + diffs[2] * diffs[2])};
      float R_T_D = (float) (180.0 / Math.PI);
      float yaw = (float)Math.atan2(diffs[2], diffs[0]) * (float) (180.0 / Math.PI) - 90.0F;
      float pitch = (float)Math.atan2(diffs[1], diffs[3]) * (float) (-180.0 / Math.PI);
      float randYaw = 0.0F;
      if (randomizer) {
         float randStrengh = 1.07F * MathUtils.clamp(1.0F - (float)rotateAt.getDistanceAtVec3dToVec3d(eyesPos, vec) / 2.25F, 0.0F, 1.0F);
         yaw = Yaw + getFixedRotation(yaw - Yaw) + (randomizer ? (randYaw = randF(-randStrengh, randStrengh)) : 0.0F);
         pitch = Pitch + getFixedRotation(pitch - Pitch) + (randomizer ? randF(-randStrengh, randStrengh) : 0.0F);
      }

      if (turnAwayBoundingBox) {
         float[] turnedRotate = getTurnAwayedRotate(new float[]{yaw, pitch}, randomizer ? (double)randYaw : 0.0);
         yaw = turnedRotate[0];
         pitch = turnedRotate[1];
      }

      yaw = GCDFix.getFixedRotation(rotateAt.rotationYaw + MathHelper.wrapDegrees(yaw - rotateAt.rotationYaw));
      pitch = GCDFix.getFixedRotation(rotateAt.rotationPitch + MathHelper.wrapDegrees(pitch - rotateAt.rotationPitch));
      if (randomizer) {
         Yaw = MathUtils.lerp(MathUtils.lerp(Yaw, yaw, 0.3F), yaw, 0.45F);
         Pitch = MathUtils.lerp(Pitch, pitch, 0.18F);
         Yaw = GCDFix.getFixedRotation(rotateAt.rotationYaw + MathHelper.wrapDegrees(Yaw - rotateAt.rotationYaw));
         Pitch = GCDFix.getFixedRotation(rotateAt.rotationPitch + MathHelper.wrapDegrees(Pitch - rotateAt.rotationPitch));
      }

      return new float[]{randomizer ? Yaw : yaw, randomizer ? Pitch : pitch};
   }

   private static double sqrt(double var1, double var3) {
      return Math.sqrt(Math.pow(var1, 2.0) + Math.pow(var3, 2.0));
   }

   public static float M(float def, float min, float max) {
      return Math.min(Math.max(def, min), max);
   }

   public static float[] getNeededFacing2(EntityLivingBase target, boolean randomizer, boolean turnAwayBoundingBox) {
      Vec3d eyesPos = getEyesPos();
      Vec3d to = target.getBestVec3dOnEntityBox();
      Vec3d rot = to.addVector(-eyesPos.xCoord, -eyesPos.yCoord, -eyesPos.zCoord);
      double xzD = (double)MathHelper.sqrt(rot.xCoord * rot.xCoord + rot.zCoord * rot.zCoord);
      float yaw = (float)(Math.atan2(rot.zCoord, rot.xCoord) * 180.0 / Math.PI - 90.0);
      float pitch = (float)Math.toDegrees(-Math.atan2(rot.yCoord, xzD));
      if (turnAwayBoundingBox) {
         float[] turnedRotate = getTurnAwayedRotate(new float[]{yaw, pitch}, -1.0);
         yaw = turnedRotate[0];
         pitch = turnedRotate[1];
      }

      float yRand = 0.5F + Math.abs(Pitch - pitch) / 2.5F;
      yaw = Yaw + getFixedRotation(MathUtils.wrapAngleTo180_float(yaw - Yaw - (0.5F + (float)Math.random()) * yRand));
      pitch = MathHelper.clamp(Pitch + getFixedRotation(MathHelper.wrapDegrees(pitch - Pitch)), -90.0F, 90.0F);
      if (randomizer) {
         Pitch = Pitch + MathUtils.clamp(MathUtils.wrapDegrees(pitch - Pitch), -5.0F, 5.0F);
         float yawSpeed = 40.0F - 20.0F * (float)Math.random();
         Yaw = Yaw + MathUtils.clamp(MathUtils.wrapDegrees(yaw - Yaw), -yawSpeed, yawSpeed);
         return new float[]{Yaw, Pitch};
      } else {
         return new float[]{yaw, pitch};
      }
   }

   public static double Interpolate(double current, double old, double scale) {
      return old + (current - old) * scale;
   }

   public static float getDeltaMouse(float delta) {
      return (float)Math.round(delta / getGCDValue());
   }

   public static float randomNumber(float max, float min) {
      return (float)((double)min + Math.random() * (double)(max - min));
   }

   public static float getFixedRotation(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getAngle(Entity entity) {
      double x = entity.lastTickPosX
         + (entity.posX - entity.lastTickPosX) * (double)Minecraft.getMinecraft().getRenderPartialTicks()
         - RenderManager.renderPosX;
      double z = entity.lastTickPosZ
         + (entity.posZ - entity.lastTickPosZ) * (double)Minecraft.getMinecraft().getRenderPartialTicks()
         - RenderManager.renderPosZ;
      float yaw = (float)(-Math.toDegrees(Math.atan2(x, z)));
      return (float)((double)yaw - Interpolate((double)Minecraft.player.rotationYaw, (double)Minecraft.player.prevRotationYaw, 1.0));
   }

   public static float getGCDValue() {
      return (float)((double)getGCD() * 0.15);
   }

   public static float getGCD() {
      float f1;
      return (f1 = (float)((double)Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.6 + 0.2)) * f1 * f1 * 8.0F;
   }

   public static float Rotate(float from, float to, float minstep, float maxstep) {
      float f = MathUtils.wrapDegrees(to - from) * 0.6F;
      if (f < 0.0F) {
         f = MathUtils.clamp(f, -maxstep, -minstep);
      } else {
         f = MathUtils.clamp(f, minstep, maxstep);
      }

      return Math.abs(f) > Math.abs(MathUtils.wrapDegrees(to - from)) ? to : MathUtils.wrapDegrees(from + f);
   }

   public static float[] rotats(EntityLivingBase entity, float Yaw, float Pitch) {
      double diffX = entity.posX - Minecraft.player.posX;
      double diffZ = entity.posZ - Minecraft.player.posZ;
      double raz = MathUtils.clamp(
         Minecraft.player.posY + (double)Minecraft.player.getEyeHeight() - entity.posY,
         (double)entity.height * 0.3,
         (double)Math.min(Minecraft.player.getEyeHeight(), entity.height * 0.8F)
      );
      double diffY = entity.posY + raz - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      if (!entity.canEntityBeSeen(Minecraft.player)) {
         diffY = entity.posY + (double)entity.height - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0) + getFixedRotation(Yaw);
      float pitch = (float)Math.toDegrees(-Math.atan2(diffY, dist)) + getFixedRotation(Pitch);
      yaw = Minecraft.player.rotationYaw + getFixedRotation(MathUtils.wrapDegrees(yaw - Minecraft.player.rotationYaw));
      pitch = Minecraft.player.rotationPitch + getFixedRotation(pitch - Minecraft.player.rotationPitch);
      pitch = MathUtils.clamp(pitch, -80.0F, 85.0F);
      return new float[]{yaw, pitch};
   }

   public static float[] rotationBebra(EntityLivingBase entity) {
      double diffX = entity.posX - Minecraft.player.posX;
      double diffZ = entity.posZ - Minecraft.player.posZ;
      double diffY = entity.posY + (double)(entity.height / 2.0F) - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      if (!entity.canEntityBeSeen(Minecraft.player)) {
         diffY = entity.posY + (double)entity.height - (Minecraft.player.posY + (double)Minecraft.player.getEyeHeight());
      }

      double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
      float pitch = (float)Math.toDegrees(-Math.atan2(diffY, dist));
      Yaw = Minecraft.player.rotationYaw + getFixedRotation(MathUtils.wrapDegrees(Yaw - Minecraft.player.rotationYaw));
      Pitch = Minecraft.player.rotationPitch + getFixedRotation(Pitch - Minecraft.player.rotationPitch);
      Yaw = Yaw + MathUtils.clamp(MathUtils.wrapDegrees(yaw - Yaw), -80.0F, 80.0F);
      Pitch = Pitch + MathUtils.clamp(pitch - Pitch, -20.0F, 20.0F);
      Yaw = Yaw + getFixedRotation((float)RandomUtils.getRandomDouble(-1.5, 1.5));
      Pitch = Pitch + getFixedRotation((float)RandomUtils.getRandomDouble(-1.5, 1.5));
      Yaw = MathUtils.wrapDegrees(Yaw);
      Pitch = MathUtils.clamp(Pitch, -90.0F, 90.0F);
      Client.msg(Yaw + " " + Pitch, false);
      return new float[]{Yaw, Pitch};
   }
}
