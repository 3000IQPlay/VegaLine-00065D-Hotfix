package ru.govno.client.module.modules;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;

public class EntityBox extends Module {
   public static EntityBox get;
   public BoolSettings BoxScalling;
   public BoolSettings Predict;
   public BoolSettings ExtendedRange;
   public FloatSettings BoxScale;
   public FloatSettings TicksCount;
   public FloatSettings EntitiesReach;
   public FloatSettings BlocksReach;
   private static float boxScale = 1.0F;
   private static float blocksReach;
   private static float entitiesReach;
   private static float ticksOffset;

   public EntityBox() {
      super("EntityBox", 0, Module.Category.COMBAT);
      this.settings.add(this.BoxScalling = new BoolSettings("BoxScalling", true, this));
      this.settings.add(this.BoxScale = new FloatSettings("BoxScale", 1.2F, 5.0F, 0.75F, this, () -> this.BoxScalling.getBool()));
      this.settings.add(this.Predict = new BoolSettings("Predict", true, this));
      this.settings.add(this.TicksCount = new FloatSettings("TicksCount", 1.0F, 4.0F, 0.25F, this, () -> this.Predict.getBool()));
      this.settings.add(this.ExtendedRange = new BoolSettings("ExtendedRange", true, this));
      this.settings.add(this.EntitiesReach = new FloatSettings("EntitiesReach", 0.6F, 3.0F, 0.0F, this, () -> this.ExtendedRange.getBool()));
      this.settings.add(this.BlocksReach = new FloatSettings("BlocksReach", 0.4F, 2.0F, 0.0F, this, () -> this.ExtendedRange.getBool()));
      get = this;
   }

   public static AxisAlignedBB getExtendedHitbox(Vec3d addPos, Entity entityIn, float scale, AxisAlignedBB prevBox) {
      if (mc.isSingleplayer()) {
         return prevBox;
      } else if (entityIn == null) {
         return null;
      } else if (prevBox == null) {
         return null;
      } else if (entityIn instanceof EntityPlayerSP) {
         return entityIn.boundingBox;
      } else {
         float w = (float)(prevBox.maxX - prevBox.minX) / 2.0F;
         float h = (float)(prevBox.maxY - prevBox.minY);
         double x = entityIn.posX + addPos.xCoord;
         double y = entityIn.posY + addPos.yCoord;
         double z = entityIn.posZ + addPos.zCoord;
         Vec3d firstPos = new Vec3d(x - (double)(w * scale), y, z - (double)(w * scale));
         Vec3d secondPos = new Vec3d(x + (double)(w * scale), y + (double)h, z + (double)(w * scale));
         AxisAlignedBB aabb = new AxisAlignedBB(firstPos, secondPos);
         return aabb == null ? entityIn.boundingBox : aabb;
      }
   }

   @Override
   public void onUpdate() {
      boxScale = get.BoxScalling.getBool() ? get.BoxScale.getFloat() : 1.0F;
      blocksReach = get.ExtendedRange.getBool() ? get.BlocksReach.getFloat() : 0.0F;
      entitiesReach = get.ExtendedRange.getBool() ? get.EntitiesReach.getFloat() : 0.0F;
      ticksOffset = get.Predict.getBool() ? get.TicksCount.getFloat() : 0.0F;
   }

   public static boolean hitboxModState() {
      return get.actived;
   }

   public static float hitboxModSizeBox() {
      return boxScale;
   }

   public static float hitboxModReachBlocks() {
      return blocksReach;
   }

   public static float hitboxModReachEntities() {
      return entitiesReach;
   }

   public static float hitboxModPredictSize() {
      return ticksOffset;
   }

   public static Vec3d hitboxModPredictVec(Entity entityIn, float ticks) {
      return new Vec3d(
         -(entityIn.prevPosX - entityIn.posX) * (double)ticks,
         -(entityIn.prevPosY - entityIn.posY) * (double)ticks,
         -(entityIn.prevPosZ - entityIn.posZ) * (double)ticks
      );
   }

   public static boolean entityIsCurrentToExtend(Entity entityIn) {
      return entityIn != null && entityIn instanceof EntityLivingBase && !(entityIn instanceof EntityPlayerSP);
   }
}
