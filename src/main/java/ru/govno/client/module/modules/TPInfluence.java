package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class TPInfluence extends Module {
   public static TPInfluence get;
   public BoolSettings UseOnHitAura;
   public BoolSettings UseOnCrystalField;
   public BoolSettings ShowTpPos;
   public ModeSettings HitAuraRule;
   public ModeSettings TeleportAction;
   public FloatSettings SelfTicksAlive;
   public FloatSettings MaxRange;
   private Vec3d lastHandledVec = new Vec3d(0.0, 0.0, 0.0);
   private final List<TPInfluence.TimedVec> TIMED_VECS_LIST = new ArrayList<>();

   public TPInfluence() {
      super("TPInfluence", 0, Module.Category.COMBAT);
      get = this;
      this.settings.add(this.UseOnHitAura = new BoolSettings("UseOnHitAura", false, this));
      this.settings
         .add(
            this.HitAuraRule = new ModeSettings(
               "HitAuraRule",
               "Always",
               this,
               new String[]{"Always", "Fly", "Fly&SelfTicks", "ElytraFlying", "ElyBoost", "ElyBoost&Stand"},
               () -> this.UseOnHitAura.getBool()
            )
         );
      this.settings
         .add(
            this.SelfTicksAlive = new FloatSettings(
               "SelfTicksAlive",
               400.0F,
               1000.0F,
               100.0F,
               this,
               () -> this.UseOnHitAura.getBool() && this.HitAuraRule.getMode().equalsIgnoreCase("Fly&SelfTicks")
            )
         );
      this.settings.add(this.UseOnCrystalField = new BoolSettings("UseOnCrystalField", false, this));
      this.settings
         .add(
            this.TeleportAction = new ModeSettings(
               "TeleportAction", "StepVH", this, new String[]{"StepVH", "StepV", "StepH", "StepHG", "VanillaVH", "VanillaH"}, () -> this.UseOnHitAura.getBool()
            )
         );
      this.settings
         .add(
            this.MaxRange = new FloatSettings(
               "MaxRange", 60.0F, 200.0F, 10.0F, this, () -> this.UseOnHitAura.getBool() && this.TeleportAction.getMode().contains("Step")
            )
         );
      this.settings.add(this.ShowTpPos = new BoolSettings("ShowTpPos", true, this, () -> this.UseOnHitAura.getBool()));
   }

   private double sqrtAt(double val1) {
      return Math.sqrt(val1 * val1);
   }

   private double sqrtAt(double val1, double val2) {
      return Math.sqrt(val1 * val1 + val2 * val2);
   }

   private double sqrtAt(double val1, double val2, double val3) {
      return Math.sqrt(val1 * val1 + val2 * val2 + val3 * val3);
   }

   private double positive(double val) {
      return val < 0.0 ? -val : val;
   }

   public boolean defaultRule() {
      return (!FreeCam.get.isActived() || FreeCam.fakePlayer == null) && Minecraft.player != null && this.isActived();
   }

   public boolean entityRule(EntityLivingBase targetIn) {
      if (targetIn != null && targetIn.isEntityAlive()) {
         boolean selfCollided = Minecraft.player.boundingBox == null || mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox).isEmpty();
         boolean targetCollided = targetIn.boundingBox == null || mc.world.getCollisionBoxes(targetIn, targetIn.boundingBox).isEmpty();
         return selfCollided || !targetCollided;
      } else {
         return false;
      }
   }

   private void send(double x, double y, double z, boolean ground) {
      mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, ground));
   }

   private void send(double x, double y, double z) {
      this.send(x, y, z, false);
   }

   private void send(boolean ground) {
      mc.getConnection().sendPacket(new CPacketPlayer(ground));
   }

   private void send() {
      mc.getConnection().sendPacket(new CPacketPlayer());
   }

   private Vec3d axisEntityPoint(Entity entityOf) {
      AxisAlignedBB bb;
      return (bb = entityOf.getEntityBoundingBox()) != null
         ? new Vec3d(bb.minX + (bb.maxX - bb.minX) / 2.0, bb.minY, bb.minZ + (bb.maxZ - bb.minZ) / 2.0)
         : entityOf.getPositionVector();
   }

   public void teleportActionOfActionType(boolean pre, Vec3d to, String actionType) {
      Vec3d self = Minecraft.player.getPositionVector();
      double dx = this.positive(self.xCoord - to.xCoord);
      double dy = this.positive(self.yCoord - to.yCoord);
      double dz = this.positive(self.zCoord - to.zCoord);
      int grInt = Minecraft.player.onGround ? 1 : 0;
      float distanceDensity = 1.0f;
      if (pre) {
         switch (actionType) {
            case "StepVH": {
               double diffs = this.sqrtAt(dx, dy, dz);
               for (int packetCount = (int)(diffs / (9.64 * (double)distanceDensity)) + 1; packetCount > 0; --packetCount) {
                  this.send(false);
               }
               if (grInt == 1) {
                  this.send(to.xCoord, to.yCoord + 0.08, to.zCoord);
               }
               this.send(to.xCoord, to.yCoord + 0.01, to.zCoord);
               this.lastHandledVec = new Vec3d(to.xCoord, to.yCoord + 0.01, to.zCoord);
               break;
            }
            case "StepV": {
               double diffs = this.positive(dy);
               int packetCount = 1 + (int)(diffs / (9.73 * (double)distanceDensity));
               if (grInt == 1) {
                  this.send(to.xCoord, self.yCoord + 0.08, to.zCoord);
               }
               while (packetCount > 0) {
                  this.send(false);
                  --packetCount;
               }
               this.send(self.xCoord, to.yCoord + 0.01, self.zCoord);
               this.lastHandledVec = new Vec3d(self.xCoord, to.yCoord + 0.01, self.zCoord);
               break;
            }
            case "StepH": {
               double diffs = this.sqrtAt(dx, dz);
               for (int packetCount = (int)(diffs / (8.953 * (double)distanceDensity)) + grInt; packetCount > 0; --packetCount) {
                  this.send(false);
               }
               this.send(to.xCoord, self.yCoord, to.zCoord);
               this.lastHandledVec = new Vec3d(to.xCoord, self.yCoord, to.zCoord);
               break;
            }
            case "StepHG": {
               int packetCount;
               double diffs = this.sqrtAt(dx, dz);
               for (packetCount = (int)(diffs / (8.317 * (double)distanceDensity)) + grInt; packetCount > 0; --packetCount) {
                  this.send(false);
               }
               this.send(to.xCoord, self.yCoord - (double)grInt * 1.0E-4 * (double)packetCount, to.zCoord);
               this.lastHandledVec = new Vec3d(to.xCoord, self.yCoord - (double)grInt * 1.0E-4 * (double)packetCount, to.zCoord);
               if (grInt != 0) break;
               Minecraft.player.setPosY(self.yCoord);
               break;
            }
            case "VanillaVH": {
               this.send(false);
               this.send(to.xCoord, to.yCoord, to.zCoord);
               this.lastHandledVec = new Vec3d(to.xCoord, to.yCoord, to.zCoord);
            }
            case "VanillaH": {
               this.send(false);
               this.send(to.xCoord, self.yCoord, to.zCoord);
               this.lastHandledVec = new Vec3d(to.xCoord, to.yCoord, to.zCoord);
            }
         }
         return;
      }
      switch (actionType) {
         case "StepVH": {
            this.send(self.xCoord, self.yCoord + 0.15, self.zCoord);
            this.send(self.xCoord, self.yCoord, self.zCoord);
            break;
         }
         case "StepV": {
            this.send(self.xCoord, self.yCoord, self.zCoord);
            this.send(self.xCoord, self.yCoord + (grInt == 1 ? 0.1 : -1.0E-13), self.zCoord);
            break;
         }
         case "StepH": {
            this.send(self.xCoord, self.yCoord, self.zCoord);
            break;
         }
         case "StepHG": {
            this.send(self.xCoord, self.yCoord - this.positive(grInt - 1) * 1.0E-4 * 2.0, self.zCoord);
            break;
         }
         case "VanillaVH": {
            this.send(self.xCoord, self.yCoord + 0.0016, self.zCoord);
            break;
         }
         case "VanillaH": {
            this.send(self.xCoord, self.yCoord, self.zCoord);
         }
      }
   }

   public boolean vectorRule(Vec3d to, double defaultDistanceMax, double distanceMin) {
      String action = this.TeleportAction.getMode();
      Vec3d self = Minecraft.player.getPositionVector();
      double range = action.contains("Step") ? (double)this.MaxRange.getFloat() : (action.contains("Vanilla") ? 9.23 : defaultDistanceMax);
      double dx = self.xCoord - to.xCoord;
      double dy = self.yCoord - to.yCoord;
      double dz = self.zCoord - to.zCoord;
      boolean isInRange = false;
      if (this.sqrtAt(dx, dy, dz) < distanceMin) {
         return false;
      } else {
         String var17 = this.TeleportAction.getMode();
         switch (var17) {
            case "StepVH":
            case "VanillaVH":
               isInRange = this.sqrtAt(dx, dy, dz) < range;
               break;
            case "StepV":
               isInRange = this.sqrtAt(dx, dz) < defaultDistanceMax / 1.33333 && this.positive(dy) < range;
               break;
            case "StepH":
            case "StepHG":
               isInRange = this.positive(dy) < defaultDistanceMax && this.sqrtAt(dx, dz) + this.positive(dy) < range;
               break;
            case "VanillaH":
               isInRange = this.positive(dy) < defaultDistanceMax - 1.0 && this.sqrtAt(dx, dz) < range;
         }

         return isInRange;
      }
   }

   public Vec3d targetWhitePos(EntityLivingBase target, double distanceMin) {
      distanceMin -= (double)target.height;
      if (distanceMin < 0.0) {
         distanceMin = 0.0;
      }

      Vec3d vec = target.getPositionVector();
      double selfX = Minecraft.player.posX;
      double targetX = vec.xCoord;
      double selfY = Minecraft.player.posY;
      double targetY = vec.yCoord;
      double yDst = this.positive(selfY - targetY);
      double targetW = (double)target.width / 2.0;
      double targetH = (double)target.height;
      double selfZ = Minecraft.player.posZ;
      double targetZ = vec.zCoord;
      if (yDst > distanceMin) {
         double appendY = MathUtils.clamp(yDst - distanceMin, 0.0, distanceMin - this.sqrtAt(selfX - targetX, selfZ - targetZ));

         double tempAppend;
         for (tempAppend = 0.0; tempAppend < appendY; tempAppend += 0.1) {
            AxisAlignedBB aabb = new AxisAlignedBB(
               targetX - targetW / 2.0,
               targetY + tempAppend,
               targetZ - targetW / 2.0,
               targetX + targetW / 2.0,
               targetY + targetH + tempAppend,
               targetZ + targetW / 2.0
            );
            if (aabb == null) {
               tempAppend -= 0.1;
            } else if (tempAppend > 0.0 && !mc.world.getCollisionBoxes(target, aabb).isEmpty()) {
               tempAppend -= 0.1;
               break;
            }
         }

         appendY = tempAppend;
         if (tempAppend < 0.0) {
            appendY = 0.0;
         }

         vec = vec.addVector(0.0, appendY, 0.0);
      }

      return vec;
   }

   public boolean forHitAuraRule(EntityLivingBase target) {
      if (target != null && target.isEntityAlive()) {
         boolean sata = this.defaultRule() && this.entityRule(target) && this.UseOnHitAura.getBool();
         if (sata) {
            String rule = this.HitAuraRule.getMode();
            switch (rule) {
               case "Always":
                  sata = true;
                  break;
               case "Fly":
                  sata = Fly.get.isActived();
                  break;
               case "Fly&SelfTicks":
                  sata = Fly.get.isActived() && (float)Minecraft.player.ticksExisted < this.SelfTicksAlive.getFloat();
                  break;
               case "ElytraFlying":
                  sata = Minecraft.player.isElytraFlying();
               case "ElyBoost":
                  sata = ElytraBoost.get.isActived() && ElytraBoost.canElytra();
                  break;
               case "ElyBoost&Stand":
                  sata = ElytraBoost.get.isActived()
                     && ElytraBoost.canElytra()
                     && MoveMeHelp.getSpeed() < 0.05
                     && !MoveMeHelp.moveKeysPressed()
                     && this.positive(Minecraft.player.motionY) < 0.24;
            }
         }

         double auraRangeMin = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS), 3.0, 5.2 - (double)target.height);
         double auraRangeMax = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS) - 0.1, 0.0, 5.2);
         return this.isActived() && sata && this.vectorRule(this.targetWhitePos(target, auraRangeMin), auraRangeMax, auraRangeMin);
      } else {
         return false;
      }
   }

   public void hitAuraTPPre(EntityLivingBase target) {
      double auraRangeMin = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS), 3.0, 5.2 - (double)target.height);
      this.teleportActionOfActionType(true, this.targetWhitePos(target, auraRangeMin), this.TeleportAction.getMode());
      if (this.ShowTpPos.getBool()) {
         this.addTimedVec();
      }
   }

   public void hitAuraTPPost(EntityLivingBase target) {
      double auraRangeMin = MathUtils.clamp((double)HitAura.get.getAuraRange(HitAura.TARGET_ROTS), 3.0, 5.2 - (double)target.height);
      this.teleportActionOfActionType(false, this.targetWhitePos(target, auraRangeMin), this.TeleportAction.getMode());
   }

   public boolean forCrystalFieldRule() {
      return this.defaultRule() && this.UseOnCrystalField.getBool();
   }

   @Override
   public void onToggled(boolean enable) {
      if (!this.TIMED_VECS_LIST.isEmpty()) {
         this.TIMED_VECS_LIST.clear();
      }

      super.onToggled(enable);
   }

   private void addTimedVec() {
      if (this.lastHandledVec != null) {
         this.TIMED_VECS_LIST.add(new TPInfluence.TimedVec(this.lastHandledVec, HitAura.get.msCooldown() * 1.5F));
      }
   }

   private int getTpPointColor() {
      return -1;
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      if (this.isActived()) {
         if (!this.TIMED_VECS_LIST.isEmpty()) {
            this.TIMED_VECS_LIST.removeIf(TPInfluence.TimedVec::isToRemove);
            RenderUtils.setup3dForBlockPos(
               () -> this.TIMED_VECS_LIST
                     .forEach(
                        timedVec -> {
                           float aPC = timedVec.getAlphaPC();
                           if (aPC * 255.0F >= 1.0F) {
                              float range = 0.1F * (0.25F + 0.75F * (float)MathUtils.easeInOutQuad((double)(1.0F - timedVec.getTimePC())));
                              Vec3d vec = timedVec.getVec();
                              AxisAlignedBB aabb = new AxisAlignedBB(
                                 vec.addVector((double)(-range), (double)(-range), (double)(-range)),
                                 vec.addVector((double)range, (double)range, (double)range)
                              );
                              if (aabb != null) {
                                 int color = this.getTpPointColor();
                                 color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * aPC);
                                 RenderUtils.drawCanisterBox(
                                    aabb, true, false, true, color, 0, ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 6.0F)
                                 );
                              }
                           }
                        }
                     ),
               true
            );
         }
      }
   }

   private class TimedVec {
      private final long startTime = System.currentTimeMillis();
      private final float maxTime;
      private final Vec3d vec;

      public TimedVec(Vec3d vec, float maxTime) {
         this.vec = vec;
         this.maxTime = maxTime;
      }

      public float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public float getAlphaPC() {
         float pc = 1.0F - this.getTimePC();
         return (float)MathUtils.easeInCircle(pc > 0.5F ? (double)(1.0F - pc) : (double)pc);
      }

      public Vec3d getVec() {
         return this.vec;
      }

      public boolean isToRemove() {
         return this.getVec() == null || this.getTimePC() == 1.0F;
      }
   }
}
