package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Command.impl.Clip;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Criticals extends Module {
   public static Criticals get;
   private final AnimationUtils usingProgress = new AnimationUtils(0.0F, 0.0F, 0.1F);
   private float indicatorScale = 0.0F;
   private float radiusPlus = 0.0F;
   public BoolSettings EntityHit;
   public BoolSettings Bowing;
   public BoolSettings VehicleInstakill;
   public ModeSettings HitMode;
   public ModeSettings BowMode;
   private static boolean doAddPacket;
   private static boolean groundS;
   static TimerHelper timeCancel = new TimerHelper();
   static float yawS;
   static float pitchS;

   public Criticals() {
      super("Criticals", 0, Module.Category.COMBAT);
      this.settings.add(this.EntityHit = new BoolSettings("EntityHit", true, this));
      this.settings
         .add(
            this.HitMode = new ModeSettings(
               "HitMode",
               "VanillaHop",
               this,
               new String[]{"VanillaHop", "Matrix", "Matrix2", "NCP", "MatrixElytra", "MatrixStand"},
               () -> this.EntityHit.getBool()
            )
         );
      this.settings.add(this.Bowing = new BoolSettings("Bowing", false, this));
      this.settings.add(this.BowMode = new ModeSettings("BowMode", "Vanilla", this, new String[]{"Vanilla", "Matrix6.4.0-"}, () -> this.Bowing.getBool()));
      this.settings.add(this.VehicleInstakill = new BoolSettings("VehicleInstakill", true, this));
      get = this;
   }

   @Override
   public void onUpdate() {
      if (!this.EntityHit.getBool() && !this.Bowing.getBool()) {
         Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: First enable something in the settings.", false);
         this.toggle(false);
      } else {
         if (doAddPacket && yawS != 9.0F && pitchS != 0.0F) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Rotation(yawS, pitchS, groundS));
            doAddPacket = false;
         }
      }
   }

   @Override
   public String getDisplayName() {
      return !this.EntityHit.getBool() && !this.Bowing.getBool()
         ? this.getName()
         : (
            this.EntityHit.getBool() && this.Bowing.getBool()
               ? this.getDisplayByMode(this.HitMode.getMode() + " | " + this.BowMode.getMode())
               : (this.EntityHit.getBool() ? this.getDisplayByMode(this.HitMode.getMode()) : this.getDisplayByMode(this.BowMode.getMode()))
         );
   }

   public static void vehicleInstakill(Entity entity) {
      if ((entity instanceof EntityBoat || entity instanceof EntityMinecart) && !Client.friendManager.isFriend(entity.getName())) {
         for (int i = 0; i < 17; i++) {
            mc.playerController.attackEntity(Minecraft.player, entity);
         }
      }
   }

   public static void crits(Entity entity) {
      if (get.EntityHit.getBool()
         && entity != null
         && entity instanceof EntityLivingBase base
         && (double)Minecraft.player.getDistanceToEntity(base) <= 6.0
         && Minecraft.player != null
         && Minecraft.player.onGround
         && !Minecraft.player.isInWater()
         && !Minecraft.player.isInWeb
         && !Minecraft.player.isJumping()) {
         Module mod = get;
         if (mod != null && mod.actived) {
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY;
            double z = Minecraft.player.posZ;
            String var9 = get.HitMode.getMode();
            switch (var9) {
               case "Matrix":
                  mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 1.0E-6, z, false));
                  mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, false));
                  break;
               case "Matrix2":
                  if (EntityLivingBase.isMatrixDamaged) {
                     mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 1.0E-6, z, false));
                     mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, false));
                  }
                  break;
               case "NCP":
                  mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 0.05F, z, false));
                  mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, false));
                  mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 0.012511F, z, false));
                  mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, false));
                  break;
               case "MatrixElytra":
                  if (InventoryUtil.getElytra() != -1 && InventoryUtil.getItemInInv(Items.air) != -1) {
                     ElytraBoost.eq();
                     mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 0.0201, z, true));
                     mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 0.0201, z, false));
                     ElytraBoost.badPacket();
                     mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 0.02, z, false));
                     ElytraBoost.badPacket();
                     Minecraft.player.setFlag(7, false);
                     ElytraBoost.deq();
                  }
                  break;
               case "MatrixStand":
                  if (Minecraft.player.onGround && MoveMeHelp.getSpeed() == 0.0 && !Minecraft.player.isJumping()) {
                     if (mc.world
                        .getCollisionBoxes(
                           Minecraft.player,
                           Minecraft.player.boundingBox.maxYToMinY(Minecraft.player.boundingBox.maxY - Minecraft.player.boundingBox.minY + 1.26)
                        )
                        .isEmpty()) {
                        for (double offset : new double[]{
                           0.42F,
                           0.7531999805212024,
                           1.0013359791121417,
                           1.1661092609382138,
                           1.252203340253729,
                           1.1767592750642422,
                           1.0244240882136921,
                           0.7967356006687112,
                           0.49520087700592796,
                           0.02
                        }) {
                           mc.getConnection()
                              .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     } else if (mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(2.499)).isEmpty()) {
                        for (double offset : new double[]{0.41999998688698, 0.70000004768372, 0.62160004615784, 0.46636804164123, 0.23584067272827, 0.02}) {
                           mc.getConnection()
                              .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     } else if (mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(1.999)).isEmpty()) {
                        for (double offset : new double[]{0.20000004768372, 0.12160004615784, 0.02}) {
                           mc.getConnection()
                              .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     } else if (mc.world
                        .getCollisionBoxes(
                           Minecraft.player,
                           Minecraft.player.boundingBox.maxYToMinY(Minecraft.player.boundingBox.maxY - Minecraft.player.boundingBox.minY + 0.01)
                        )
                        .isEmpty()) {
                        for (double offset : new double[]{0.01250004768372, 0.01}) {
                           mc.getConnection()
                              .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     }
                  }
            }
         }
      }
   }

   @EventTarget
   public void onPacketSend(EventSendPacket event) {
      if (event.getPacket() instanceof CPacketPlayer packet
         && (packet instanceof CPacketPlayer.PositionRotation || packet instanceof CPacketPlayer.Rotation)
         && this.HitMode.getMode().equalsIgnoreCase("MatrixStand")
         && Minecraft.player != null
         && Minecraft.player.onGround
         && MoveMeHelp.getSpeed() == 0.0
         && !Minecraft.player.isJumping()
         && MoveMeHelp.getSpeed() == 0.0) {
         boolean replace = mc.world
               .getCollisionBoxes(
                  Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(Minecraft.player.boundingBox.maxY - Minecraft.player.boundingBox.minY + 1.26)
               )
               .isEmpty()
            || mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(2.499)).isEmpty()
            || mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(1.999)).isEmpty()
            || mc.world
               .getCollisionBoxes(
                  Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(Minecraft.player.boundingBox.maxY - Minecraft.player.boundingBox.minY + 0.01)
               )
               .isEmpty();
         if (replace) {
            if (packet instanceof CPacketPlayer.PositionRotation positionRotationPacket) {
               if (timeCancel.hasReached(900.0F)) {
                  return;
               }

               if (Minecraft.player.ticksExisted < 100) {
                  return;
               }

               if (positionRotationPacket.pitch > 87.0F) {
                  return;
               }

               if (MathUtils.getDifferenceOf(positionRotationPacket.yaw, Minecraft.player.lastReportedYaw) > 10.0 && HitAura.TARGET == null) {
                  return;
               }

               mc.getConnection()
                  .sendPacket(
                     new CPacketPlayer.Position(positionRotationPacket.x, positionRotationPacket.y, positionRotationPacket.z, positionRotationPacket.onGround)
                  );
               event.cancel();
            }

            if (packet instanceof CPacketPlayer.Rotation rotationPacket) {
               if (timeCancel.hasReached(900.0F)) {
                  return;
               }

               if (Minecraft.player.ticksExisted < 100) {
                  return;
               }

               if (rotationPacket.pitch > 87.0F) {
                  return;
               }

               if (MathUtils.getDifferenceOf(rotationPacket.yaw, Minecraft.player.lastReportedYaw) > 10.0 && HitAura.TARGET == null) {
                  return;
               }

               yawS = rotationPacket.yaw;
               pitchS = rotationPacket.pitch;
               groundS = rotationPacket.onGround;
               event.cancel();
            }
         }
      }
   }

   private float getCurrentLongUseDamage(float packetsCount) {
      return 2.24F + packetsCount * 0.092159994F;
   }

   @EventTarget
   public void onSend(EventSendPacket event) {
      if (event.getPacket() instanceof CPacketPlayerDigging packet && this.Bowing.getBool()) {
         if (packet != null && packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && this.correctUseMod()) {
            this.damageMultiply(this.hasTautString(this.getTautPercent()), 60, true);
         }

         return;
      }
   }

   private boolean correctUseMod() {
      return Minecraft.player.isBowing() && this.actived && this.Bowing.getBool();
   }

   private void drawIndicator(float scaling, ScaledResolution sr) {
      this.usingProgress.to = this.getTautPercent();
      float progress = MathUtils.clamp(this.usingProgress.getAnim(), 0.05F, 1.0F);
      float plusRad = this.radiusPlus;
      float width = 100.0F - 50.0F * plusRad;
      float height = 4.0F;
      float extendY = 30.0F;
      float x = (float)(sr.getScaledWidth() / 2) - width / 2.0F;
      float x2 = (float)(sr.getScaledWidth() / 2) + width / 2.0F;
      float x3 = (float)(sr.getScaledWidth() / 2) - width / 2.0F + width * progress;
      float y = (float)(sr.getScaledHeight() / 2) + 30.0F;
      float y2 = y + 4.0F;
      float alphed = scaling * scaling;
      int colorShadow = ColorUtils.getColor(5, (int)(plusRad * 255.0F), 14, (int)((90.0F + plusRad * 45.0F) * alphed));
      int colorLeft = ColorUtils.getOverallColorFrom(
         ColorUtils.getColor(255, 110, 70, (int)(140.0F * alphed)), ColorUtils.swapAlpha(colorShadow, alphed * 80.0F), plusRad
      );
      int colorRight = ColorUtils.getOverallColorFrom(
         ColorUtils.getColor(140, 255, 255, (int)(120.0F * alphed)), ColorUtils.swapAlpha(colorShadow, alphed * 95.0F), plusRad
      );
      GlStateManager.pushMatrix();
      RenderUtils.customScaledObject2D(x, y, width, 4.0F, scaling);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x3, y2, 2.0F, 0.0F, colorLeft, colorRight, colorRight, colorLeft, false, true, false
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x2, y2, 2.0F, 2.0F + plusRad * 3.25F, colorShadow, colorShadow, colorShadow, colorShadow, false, false, true
      );
      GlStateManager.popMatrix();
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      if (this.correctUseMod() && this.indicatorScale != 1.0F || !this.correctUseMod() && this.indicatorScale != 0.0F) {
         this.indicatorScale = MathUtils.harp(this.indicatorScale, this.correctUseMod() ? 1.0F : 0.0F, (float)Minecraft.frameTime * 0.005F);
      }

      if (this.indicatorScale != 0.0F) {
         this.radiusPlus = MathUtils.harp(
            this.radiusPlus,
            this.hasTautString(this.getTautPercent()) && (double)this.usingProgress.getAnim() > 0.995 ? 1.0F : 0.0F,
            (float)Minecraft.frameTime * 0.01F
         );
         this.drawIndicator(this.indicatorScale, sr);
      }
   }

   private float getTautPercent() {
      return (float)Minecraft.player.getItemInUseMaxCount()
               / this.getCurrentLongUseDamage(this.BowMode.getMode().equalsIgnoreCase("Matrix6.4.0-") ? 26.0F : 60.0F)
            >= 1.0F
         ? 1.0F
         : (float)Minecraft.player.getItemInUseMaxCount()
            / this.getCurrentLongUseDamage(this.BowMode.getMode().equalsIgnoreCase("Matrix6.4.0-") ? 26.0F : 60.0F);
   }

   private boolean hasTautString(float used) {
      return used == 1.0F;
   }

   private void damageMultiply(boolean successfully, int packetsCount, boolean sendFakeMassage) {
      if (!successfully) {
         if (sendFakeMassage) {
            Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Can't increase bow damage.", false);
         }
      } else {
         float yaw = BowAimbot.get.getTarget() != null ? BowAimbot.getVirt()[0] : Minecraft.player.rotationYaw;
         float pitch = BowAimbot.get.getTarget() != null ? BowAimbot.getVirt()[1] : Minecraft.player.rotationPitch;
         Clip.goClip(0.0, 0.0, false);
         if (this.BowMode.getMode().equalsIgnoreCase("Matrix6.4.0-")) {
            if (ElytraBoost.canElytra()) {
               ElytraBoost.equipElytra();
               ElytraBoost.badPacket();
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING));
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));
               ElytraBoost.badPacket();
               ElytraBoost.dequipElytra();
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));

               for (int packet = 0; packet < 26; packet++) {
                  Minecraft.player
                     .connection
                     .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 0.2, Minecraft.player.posZ, false));
                  Minecraft.player
                     .connection
                     .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
               }

               Minecraft.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, 4.2F, false));
            } else {
               Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: You do not have elytra in your inventory.", false);
            }
         } else {
            Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));

            for (int packet = 0; packet < packetsCount / 2; packet++) {
               Minecraft.player
                  .connection
                  .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 0.002F, Minecraft.player.posZ, false));
               Minecraft.player
                  .connection
                  .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY - 0.002F, Minecraft.player.posZ, true));
            }

            Minecraft.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, (float)MathUtils.clamp((double)pitch * 1.0001, -89.9, 89.9), false));
         }

         if (sendFakeMassage) {
            Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Increased bow damage.", false);
         }

         this.usingProgress.setAnim(0.0F);
      }
   }
}
