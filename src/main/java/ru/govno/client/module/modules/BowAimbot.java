package ru.govno.client.module.modules;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.RandomUtils;
import ru.govno.client.utils.Combat.GCDFix;
import ru.govno.client.utils.Math.MathUtils;

public class BowAimbot extends Module {
   public static BowAimbot get;
   ModeSettings ShotTo;
   FloatSettings Range;
   BoolSettings Walls;
   public static float yaw;
   public static float pitch;
   private boolean doRotate = false;
   public static EntityLivingBase target = null;

   public BowAimbot() {
      super("BowAimbot", 0, Module.Category.COMBAT);
      this.settings.add(this.ShotTo = new ModeSettings("ShotTo", "Chestplate", this, new String[]{"Boots", "Leggings", "Chestplate", "Helmet"}));
      this.settings.add(this.Range = new FloatSettings("Range", 25.0F, 50.0F, 3.0F, this));
      this.settings.add(this.Walls = new BoolSettings("Walls", false, this));
      get = this;
   }

   private float theta(double v, double g, double x, double y) {
      double yv = 2.0 * y * v * v;
      double gx = g * x * x;
      double g2 = g * (gx + yv);
      double insqrt = v * v * v * v - g2;
      double sqrt = Math.sqrt(insqrt);
      double numerator = v * v + sqrt;
      double numerator2 = v * v - sqrt;
      double atan1 = Math.atan2(numerator, g * x);
      double atan2 = Math.atan2(numerator2, g * x);
      return (float)Math.min(atan1, atan2);
   }

   private float getLaunchAngle(EntityLivingBase entity, double v, double g) {
      String mode = this.ShotTo.currentMode;
      float pc = mode.equalsIgnoreCase("Boots")
         ? entity.getEyeHeight() / 8.0F
         : (
            mode.equalsIgnoreCase("Leggings")
               ? entity.getEyeHeight() / 3.0F
               : (
                  mode.equalsIgnoreCase("Chestplate")
                     ? entity.getEyeHeight() / 1.85F
                     : (mode.equalsIgnoreCase("Helmet") ? entity.getEyeHeight() / 1.2F : entity.getEyeHeight())
               )
         );
      double yDiff = entity.posY + (double)pc - (getMe().posY + (double)getMe().getEyeHeight());
      double xDiff = entity.posX - getMe().posX;
      double zDiff = entity.posZ - getMe().posZ;
      double xCoord = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
      return this.theta(v + 2.0, g, xCoord, yDiff);
   }

   private float[] getYawPitch(EntityLivingBase entity) {
      float akb = (float)getMe().getItemInUseMaxCount() / 20.0F;
      akb = (akb * akb + akb * 2.0F) / 3.0F;
      akb = MathHelper.clamp_float(akb, 0.0F, 1.0F);
      double v = (double)(akb * 3.0F);
      double g = 0.05F;
      if (akb > 1.0F) {
         akb = 1.0F;
      }

      float bowTr = (float)((double)((float)(-Math.toDegrees((double)this.getLaunchAngle(entity, v, g)))) - 3.8);
      float tickPlus = 0.5F;
      if (Minecraft.player.connection.getPlayerInfo(Minecraft.player.getUniqueID()) != null) {
         NetworkPlayerInfo net = Minecraft.player.connection.getPlayerInfo(Minecraft.player.getUniqueID());
         tickPlus += net.getResponseTime() < 37 ? 1.0F : (float)net.getResponseTime() / 50.0F;
      }

      double diffX = entity.posX + (entity.lastTickPosX - entity.posX) * -((double)tickPlus / 2.35) - getMe().posX;
      double diffZ = entity.posZ + (entity.lastTickPosX - entity.posX) * -((double)tickPlus / 2.35) - getMe().posZ;
      float tThetaYaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0);
      tThetaYaw = Minecraft.player.rotationYaw + GCDFix.getFixedRotation(MathHelper.wrapAngleTo180_float(tThetaYaw - Minecraft.player.rotationYaw));
      return new float[]{tThetaYaw, bowTr};
   }

   private static EntityPlayer getMe() {
      return Minecraft.player;
   }

   private boolean entityIsCurrentToFilter(EntityLivingBase entity) {
      return entity != null
         && entity.getHealth() != 0.0F
         && !(entity instanceof EntityPlayerSP)
         && !(entity instanceof EntityArmorStand)
         && !(entity instanceof EntityEnderman)
         && (this.Walls.getBool() || getMe().canEntityBeSeen(entity))
         && (!(entity instanceof EntityPlayer player) || !player.isCreative())
         && getMe().getDistanceToEntity(entity) <= this.Range.getFloat()
         && !Client.friendManager.isFriend(entity.getName())
         && !Client.isClientAdmin(target);
   }

   public final EntityLivingBase getCurrentTarget() {
      return !getMe().isBowing() || HitAura.TARGET_ROTS != null && HitAura.get.actived && !HitAura.get.Rotation.currentMode.equalsIgnoreCase("None")
         ? null
         : mc.world
            .getLoadedEntityList()
            .stream()
            .map(Entity::getLivingBaseOf)
            .filter(Objects::nonNull)
            .filter(e -> this.entityIsCurrentToFilter(e))
            .findFirst()
            .orElse(null);
   }

   public final EntityLivingBase getTarget() {
      return target;
   }

   public static float[] getVirt() {
      return new float[]{yaw, AWP.get.actived ? -1.0F : pitch};
   }

   private void virtRotate(EventPlayerMotionUpdate e, EntityLivingBase entity) {
      if (getMe().isBowing() && this.entityIsCurrentToFilter(entity) && MathUtils.getDifferenceOf(Minecraft.player.rotationPitch, 0.0F) < 60.0) {
         this.doRotate = true;
         yaw = yaw + MathUtils.clamp(this.getYawPitch(entity)[0] - yaw, -45.0F, 45.0F);
         pitch = pitch + MathUtils.clamp(this.getYawPitch(entity)[1] - pitch, -15.0F, 15.0F);
         float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
         float gcd = f * f * f * 1.2F + (float)RandomUtils.randomNumber((int)f, (int)(-f));
         yaw = yaw - yaw % gcd % gcd;
         pitch = pitch - pitch % gcd % gcd;
      } else if (MathUtils.getDifferenceOf(yaw, e.getYaw()) >= 1.0 && MathUtils.getDifferenceOf(pitch, e.getPitch()) >= 1.0 && this.doRotate) {
         yaw = yaw + MathUtils.clamp(e.getYaw() - yaw, -45.0F, 45.0F);
         pitch = pitch + MathUtils.clamp(e.getPitch() - pitch, -15.0F, 15.0F);
         float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
         float gcd = f * f * f * 1.2F + (float)RandomUtils.randomNumber((int)f, (int)(-f));
         yaw = yaw - yaw % gcd % gcd;
         pitch = pitch - pitch % gcd % gcd;
      } else {
         this.doRotate = false;
         yaw = e.getYaw();
         pitch = e.getPitch();
      }
   }

   private void rotate(EventPlayerMotionUpdate e) {
      e.setYaw(yaw);
      e.setPitch(pitch);
      getMe().rotationYawHead = yaw;
      getMe().renderYawOffset = yaw;
      getMe().rotationPitchHead = pitch;
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      if (this.actived && mc.world != null && getMe() != null) {
         target = this.getCurrentTarget();
         this.virtRotate(e, target);
         if (this.doRotate) {
            this.rotate(e);
         }
      }
   }

   @Override
   public void onToggled(boolean actived) {
      target = null;
      super.onToggled(actived);
   }
}
