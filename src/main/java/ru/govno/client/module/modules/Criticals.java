package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Command.impl.Clip;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
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
   public BoolSettings BowingAwpSound;
   public BoolSettings VehicleInstakill;
   public ModeSettings HitMode;
   public ModeSettings BowMode;
   private int ticksInningShoot;
   private static boolean doAddPacket;
   private static boolean groundS;
   static TimerHelper timeCancel = new TimerHelper();
   static float yawS;
   static float pitchS;
   private final TimerHelper timeFix = TimerHelper.TimerHelperReseted();
   private final List<Criticals.TimedVec2s> TIMED_VECS2S_LIST = new ArrayList<>();

   public Criticals() {
      super("Criticals", 0, Module.Category.COMBAT);
      this.settings.add(this.EntityHit = new BoolSettings("EntityHit", true, this));
      this.settings
         .add(
            this.HitMode = new ModeSettings(
               "HitMode",
               "VanillaHop",
               this,
               new String[]{"VanillaHop", "Matrix", "Matrix2", "NCP", "MatrixElytra", "MatrixStand", "MatrixSmart", "Grim"},
               () -> this.EntityHit.getBool()
            )
         );
      this.settings.add(this.Bowing = new BoolSettings("Bowing", false, this));
      this.settings.add(this.BowingAwpSound = new BoolSettings("BowingAwpSound", true, this, () -> this.Bowing.getBool()));
      this.settings.add(this.BowMode = new ModeSettings("BowMode", "Vanilla", this, new String[]{"Vanilla", "Matrix6.4.0-"}, () -> this.Bowing.getBool()));
      this.settings.add(this.VehicleInstakill = new BoolSettings("VehicleInstakill", true, this));
      get = this;
   }

   @Override
   public void onUpdate() {
      if (!this.EntityHit.getBool() && !this.Bowing.getBool() && !this.VehicleInstakill.getBool()) {
         Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Сначала включите что-нибудь в настройках.", false);
         this.toggle(false);
      } else {
         if (this.ticksInningShoot > 0) {
            this.ticksInningShoot--;
         }

         if (doAddPacket && yawS != 0.0F && pitchS != 0.0F) {
            Minecraft.player.connection.sendPacket(new Rotation(yawS, pitchS, groundS));
            doAddPacket = false;
         }

         boolean debug = false;
         if (debug && Minecraft.player.isJumping()) {
            Client.msg(Minecraft.player.posY + " | " + Minecraft.player.fallDistance, false);
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
         && (Minecraft.player.onGround || grimUpCriticals())
         && !Minecraft.player.isInWater()
         && !Minecraft.player.isInWeb
         && (!Minecraft.player.isJumping() || grimUpCriticals())) {
         Module mod = get;
         if (mod != null && mod.actived) {
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY;
            double z = Minecraft.player.posZ;
            String var9 = get.HitMode.getMode();
            switch (var9) {
               case "Matrix":
                  mc.getConnection().sendPacket(new Position(x, y + 1.0E-6, z, false));
                  mc.getConnection().sendPacket(new Position(x, y, z, false));
                  break;
               case "Matrix2":
                  if (EntityLivingBase.isMatrixDamaged) {
                     mc.getConnection().sendPacket(new Position(x, y + 1.0E-6, z, false));
                     mc.getConnection().sendPacket(new Position(x, y, z, false));
                  }
                  break;
               case "MatrixSmart":
                  if (EntityLivingBase.isMatrixDamaged) {
                     mc.getConnection().sendPacket(new Position(x, y + 1.0E-6, z, false));
                     mc.getConnection().sendPacket(new Position(x, y, z, false));
                     float forcedown = 0.5F;
                     mc.timer.tempSpeed = (double)forcedown;
                  } else if (Minecraft.player.onGround && MoveMeHelp.getSpeed() == 0.0 && !Minecraft.player.isJumping()) {
                     float forcedown = 1.0F;
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
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                           forcedown -= 0.0825F;
                        }

                        timeCancel.reset();
                        doAddPacket = true;
                        mc.timer.tempSpeed = (double)forcedown;
                     } else if (mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(2.499)).isEmpty()) {
                        for (double offset : new double[]{0.41999998688698, 0.70000004768372, 0.62160004615784, 0.46636804164123, 0.23584067272827, 0.02}) {
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                           forcedown -= 0.0825F;
                        }

                        timeCancel.reset();
                        doAddPacket = true;
                        mc.timer.tempSpeed = (double)forcedown;
                     } else if (mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(1.999)).isEmpty()) {
                        for (double offset : new double[]{0.20000004768372, 0.12160004615784, 0.02}) {
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                           forcedown -= 0.0825F;
                        }

                        timeCancel.reset();
                        doAddPacket = true;
                        mc.timer.tempSpeed = (double)forcedown;
                     } else if (mc.world
                        .getCollisionBoxes(
                           Minecraft.player,
                           Minecraft.player.boundingBox.maxYToMinY(Minecraft.player.boundingBox.maxY - Minecraft.player.boundingBox.minY + 0.01)
                        )
                        .isEmpty()) {
                        for (double offset : new double[]{0.01250004768372, 0.01}) {
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                           forcedown -= 0.0825F;
                        }

                        timeCancel.reset();
                        doAddPacket = true;
                        mc.timer.tempSpeed = (double)forcedown;
                     }
                  }
                  break;
               case "NCP":
                  mc.getConnection().sendPacket(new Position(x, y + 0.05F, z, false));
                  mc.getConnection().sendPacket(new Position(x, y, z, false));
                  mc.getConnection().sendPacket(new Position(x, y + 0.012511F, z, false));
                  mc.getConnection().sendPacket(new Position(x, y, z, false));
                  break;
               case "MatrixElytra":
                  if (InventoryUtil.getElytra() != -1 && InventoryUtil.getItemInInv(Items.air) != -1) {
                     ElytraBoost.eq();
                     mc.getConnection().sendPacket(new Position(x, y + 0.0201, z, true));
                     mc.getConnection().sendPacket(new Position(x, y + 0.0201, z, false));
                     ElytraBoost.badPacket();
                     mc.getConnection().sendPacket(new Position(x, y + 0.02, z, false));
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
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     } else if (mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(2.499)).isEmpty()) {
                        for (double offset : new double[]{0.41999998688698, 0.70000004768372, 0.62160004615784, 0.46636804164123, 0.23584067272827, 0.02}) {
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     } else if (mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.maxYToMinY(1.999)).isEmpty()) {
                        for (double offset : new double[]{0.20000004768372, 0.12160004615784, 0.02}) {
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     } else if (mc.world
                        .getCollisionBoxes(
                           Minecraft.player,
                           Minecraft.player.boundingBox.maxYToMinY(Minecraft.player.boundingBox.maxY - Minecraft.player.boundingBox.minY + 0.01)
                        )
                        .isEmpty()) {
                        for (double offset : new double[]{0.01250004768372, 0.01}) {
                           mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + offset, Minecraft.player.posZ, false));
                        }

                        timeCancel.reset();
                     }
                  }
                  break;
               case "Grim":
                  double padding = 1.0E-14;
                  if (Minecraft.player.fallDistance == 0.0F && axisNotCollided(-padding)) {
                     mc.getConnection().sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY - padding, Minecraft.player.posZ, false));
                     Minecraft.player.fallDistance = (float)((double)Minecraft.player.fallDistance + padding);
                  }
            }
         }
      }
   }

   private static boolean axisNotCollided(double yOffset) {
      return mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offset(0.0, yOffset, 0.0)).isEmpty();
   }

   public static boolean grimUpCriticals() {
      return get != null
         && get.isActived()
         && get.EntityHit.getBool()
         && get.HitMode.getMode().equalsIgnoreCase("Grim")
         && Minecraft.player.fallDistance == 0.0F
         && axisNotCollided(1.0E-14);
   }

   @EventTarget
   public void onPacketSend(EventSendPacket event) {
      if (event.getPacket() instanceof CPacketPlayer packet
         && (packet instanceof PositionRotation || packet instanceof Rotation)
         && (
            this.HitMode.getMode().equalsIgnoreCase("MatrixStand")
               || this.HitMode.getMode().equalsIgnoreCase("MatrixSmart") && !EntityLivingBase.isMatrixDamaged
         )
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
            if (packet instanceof PositionRotation positionRotationPacket) {
               if (timeCancel.hasReached(900.0)) {
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
                  .sendPacket(new Position(positionRotationPacket.x, positionRotationPacket.y, positionRotationPacket.z, positionRotationPacket.onGround));
               event.cancel();
            }

            if (packet instanceof Rotation rotationPacket) {
               if (timeCancel.hasReached(900.0)) {
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

      if (event.getPacket() instanceof SPacketSoundEffect soundEffect
         && soundEffect != null
         && soundEffect.getSound() != null
         && soundEffect.getSound().getSoundName() != null
         && (
            soundEffect.getSound().getSoundName() == SoundEvents.ENTITY_ARROW_SHOOT.getSoundName()
               || soundEffect.getSound().getSoundName() == SoundEvents.ENTITY_ARROW_HIT.getSoundName()
               || soundEffect.getSound().getSoundName() == SoundEvents.ENTITY_ARROW_HIT_PLAYER.getSoundName()
         )
         && this.ticksInningShoot > 0
         && Minecraft.player != null) {
         float dst = (float)Minecraft.player.getDistance(soundEffect.getX(), soundEffect.getY(), soundEffect.getZ());
         if ((double)dst < 92.0 && (double)dst > 1.0) {
            MusicHelper.playSound("awpshoot.wav", 0.4F * soundEffect.getVolume());
            this.addTimedVec2s(soundEffect);
            this.ticksInningShoot = 0;
            event.cancel();
         }
      }
   }

   private float getCurrentLongUseDamage(float packetsCount) {
      return 2.24F + packetsCount * 0.092159994F;
   }

   @EventTarget
   public void onSend(EventSendPacket event) {
      if (event.getPacket() instanceof CPacketPlayerDigging packet && this.Bowing.getBool() && this.timeFix.hasReached(300.0)) {
         if (packet != null && packet.getAction() == Action.RELEASE_USE_ITEM) {
            if (this.correctUseMod()) {
               this.damageMultiply(this.hasTautString(this.getTautPercent()), 66, true);
            }

            this.timeFix.reset();
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
            Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Не могу увеличить урон лука.", false);
         }
      } else {
         float yaw = BowAimbot.get.getTarget() != null ? BowAimbot.getVirt()[0] : Minecraft.player.rotationYaw;
         float pitch = BowAimbot.get.getTarget() != null ? BowAimbot.getVirt()[1] : Minecraft.player.rotationPitch;
         if (this.BowMode.getMode().equalsIgnoreCase("Matrix6.4.0-")) {
            Clip.goClip(0.0, 0.0, false);
            if (ElytraBoost.canElytra()) {
               ElytraBoost.equipElytra();
               ElytraBoost.badPacket();
               Minecraft.player
                  .connection
                  .preSendPacket(new CPacketEntityAction(Minecraft.player, net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING));
               Minecraft.player
                  .connection
                  .preSendPacket(new CPacketEntityAction(Minecraft.player, net.minecraft.network.play.client.CPacketEntityAction.Action.STOP_SPRINTING));
               Minecraft.player
                  .connection
                  .preSendPacket(new CPacketEntityAction(Minecraft.player, net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING));
               ElytraBoost.badPacket();
               ElytraBoost.dequipElytra();
               Minecraft.player
                  .connection
                  .preSendPacket(new CPacketEntityAction(Minecraft.player, net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING));

               for (int packet = 0; packet < 26; packet++) {
                  Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + 0.2, Minecraft.player.posZ, false));
                  Minecraft.player.connection.sendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
               }

               Minecraft.player.connection.sendPacket(new Rotation(yaw, 4.2F, false));
            } else {
               Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: У вас нет элитры в инвентаре.", false);
            }
         } else {
            Minecraft.player
               .connection
               .preSendPacket(new CPacketEntityAction(Minecraft.player, net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING));

            for (int packet = 0; packet < packetsCount / 2; packet++) {
               Minecraft.player.connection.preSendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY + 1.0E-13, Minecraft.player.posZ, false));
               Minecraft.player.connection.preSendPacket(new Position(Minecraft.player.posX, Minecraft.player.posY - 1.0E-13, Minecraft.player.posZ, true));
            }

            Minecraft.player.connection.sendPacket(new Rotation(yaw, MathUtils.clamp(pitch * 3.0F, -90.0F, 90.0F), true));
         }

         if (sendFakeMassage) {
            Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Увеличваю урон лука.", false);
         }

         this.usingProgress.setAnim(0.0F);
         if (this.BowingAwpSound.getBool()) {
            this.ticksInningShoot = 12;
         }
      }
   }

   private void addTimedVec2s(SPacketSoundEffect effectPacket) {
      this.TIMED_VECS2S_LIST
         .add(
            new Criticals.TimedVec2s(
               new Vec3d(effectPacket.getX(), effectPacket.getY(), effectPacket.getZ()), Minecraft.player.getPositionEyes(mc.getRenderPartialTicks()), 4000.0F
            )
         );
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      if (this.isActived()) {
         if (!this.TIMED_VECS2S_LIST.isEmpty()) {
            this.TIMED_VECS2S_LIST.removeIf(Criticals.TimedVec2s::isToRemove);
            RenderUtils.setup3dForBlockPos(() -> this.TIMED_VECS2S_LIST.forEach(timedVec -> {
                  float aPC = timedVec.getAlphaPC();
                  if (aPC * 255.0F >= 1.0F) {
                     Vec3d vec = timedVec.getVec();
                     Vec3d vec2 = timedVec.getVec2();
                     if (vec != null && vec2 != null) {
                        int color = -1;
                        color = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * aPC);
                        GL11.glEnable(2848);
                        GL11.glHint(3154, 4354);
                        GL11.glLineWidth(1.0F + 2.5F * aPC);
                        RenderUtils.glColor(color);
                        GL11.glBegin(1);
                        GL11.glVertex3d(vec.xCoord, vec.yCoord, vec.zCoord);
                        GL11.glVertex3d(vec2.xCoord, vec2.yCoord, vec2.zCoord);
                        GL11.glEnd();
                        GL11.glLineWidth(1.0F);
                        GL11.glHint(3154, 4352);
                        GL11.glDisable(2848);
                     }
                  }
               }), true);
         }
      }
   }

   private class TimedVec2s {
      private final long startTime = System.currentTimeMillis();
      private final float maxTime;
      private final Vec3d vec;
      private final Vec3d vec2;

      public TimedVec2s(Vec3d vec, Vec3d vec2, float maxTime) {
         this.vec = vec;
         this.vec2 = vec2;
         this.maxTime = maxTime;
      }

      public float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public float getAlphaPC() {
         return 1.0F - this.getTimePC() * this.getTimePC();
      }

      public Vec3d getVec() {
         return this.vec;
      }

      public Vec3d getVec2() {
         return this.vec2;
      }

      public boolean isToRemove() {
         return this.getVec() == null || this.getTimePC() == 1.0F;
      }
   }
}
