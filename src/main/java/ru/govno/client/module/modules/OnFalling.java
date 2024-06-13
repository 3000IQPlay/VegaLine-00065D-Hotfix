package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class OnFalling extends Module {
   BoolSettings SneakBack;
   BoolSettings FallBoost;
   BoolSettings NoDamage;
   ModeSettings BackMode;
   ModeSettings NoDmgMode;
   boolean fall = false;
   private double egX;
   private double egY;
   private double egZ;

   public OnFalling() {
      super("OnFalling", 0, Module.Category.PLAYER);
      this.settings.add(this.SneakBack = new BoolSettings("SneakBack", true, this));
      this.settings
         .add(this.BackMode = new ModeSettings("BackMode", "Matrix", this, new String[]{"Matrix", "OldGround", "Vulcan"}, () -> this.SneakBack.getBool()));
      this.settings.add(this.FallBoost = new BoolSettings("FallBoost", true, this));
      this.settings.add(this.NoDamage = new BoolSettings("NoDamage", true, this));
      this.settings
         .add(
            this.NoDmgMode = new ModeSettings(
               "NoDmgMode", "MatrixOld", this, new String[]{"MatrixOld", "MatrixNew", "NCP", "PreGround"}, () -> this.NoDamage.getBool()
            )
         );
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      if (e.ground) {
         this.egX = Minecraft.player.posX;
         this.egY = Minecraft.player.posY;
         this.egZ = Minecraft.player.posZ;
      }
   }

   public static double getDistanceTofall() {
      for (int i = 0; i < 500; i++) {
         if (Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - (double)i, Minecraft.player.posZ)) {
            return (double)i;
         }
      }

      return 0.0;
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (this.NoDamage.getBool() && this.fall && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixNew")) {
         CPacketPlayer packet = (CPacketPlayer)event.getPacket();
         this.fall = false;
         packet.onGround = true;
         Minecraft.player.motionY = -0.0199F;
      }

      if (mc.timer.speed == 0.650000243527852 && Minecraft.player.ticksExisted % 2 != 0) {
         mc.timer.speed = 1.0;
      }
   }

   @Override
   public void onUpdate() {
      if (!FreeCam.get.actived && !Fly.get.actived && !ElytraBoost.get.actived) {
         if (mc.gameSettings.keyBindSneak.isKeyDown() && this.SneakBack.getBool() && Minecraft.player.fallDistance >= 3.3F) {
            if (this.BackMode.getMode().equalsIgnoreCase("OldGround")) {
               Minecraft.player.fallDistance = 0.0F;
               if (this.egX != 0.0 && this.egY != 0.0 && this.egZ != 0.0) {
                  Minecraft.player.setPosition(this.egX, this.egY, this.egZ);
                  this.egX = 0.0;
                  this.egY = 0.0;
                  this.egZ = 0.0;
                  Minecraft.player.motionX = 0.0;
                  Minecraft.player.motionZ = 0.0;
               } else {
                  Minecraft.player.setPosition(Minecraft.player.posX, Minecraft.player.posY + (double)Minecraft.player.height, Minecraft.player.posZ);
                  Minecraft.player.motionY = MoveMeHelp.getBaseJumpHeight();
                  Minecraft.player.motionY += 0.164157;
               }
            } else {
               boolean oldGravity = Minecraft.player.hasNoGravity();
               Minecraft.player.fallDistance = (float)((double)Minecraft.player.fallDistance - 0.2);
               Minecraft.player.onGround = true;
               Minecraft.player.motionY = -0.01F;
               Entity.motiony = Minecraft.player.motionY;
               Timer.forceTimer(0.2F);
               Minecraft.player.setNoGravity(oldGravity);
            }
         }

         if (mc.gameSettings.keyBindSneak.isKeyDown()
            && this.SneakBack.getBool()
            && Minecraft.player.fallDistance > 4.0F
            && this.BackMode.getMode().equalsIgnoreCase("Vulcan")) {
            Minecraft.player.onGround = true;
            Entity.motiony = -Entity.Getmotiony;
            Minecraft.player.fallDistance = 0.0F;
         }

         if (Minecraft.player.posY > 0.0) {
            if (this.FallBoost.getBool() && getDistanceTofall() > 5.0) {
               if ((int)Minecraft.player.fallDistance >= 4 && Minecraft.player.fallDistance < 10.0F) {
                  Minecraft.player.connection.sendPacket(new CPacketPlayer(true));
                  Minecraft.player.fallDistance += 10.0F;
               }

               if (Minecraft.player.fallDistance > 5.0F && Minecraft.player.motionY < 0.0 && Minecraft.player.hurtTime != 0) {
                  Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
                  Minecraft.player.motionY = -10.0;
               }
            }

            if (this.NoDamage.getBool()) {
               if (Minecraft.player.fallDistance > (float)(Minecraft.player.getHealth() > 6.0F ? 3 : 2)
                  && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixOld")) {
                  Minecraft.player.fallDistance = (float)(Math.random() * 1.0E-12);
                  Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, true));
                  Minecraft.player.jumpMovementFactor = 0.0F;
               }

               if (Minecraft.player.fallDistance > 5.0F && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixNew")) {
                  Minecraft.player.fallDistance = 0.0F;
                  mc.timer.speed = 0.650000243527852;
                  this.fall = true;
               }

               if (mc.timer.speed == 0.650000243527852 && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixNew") && Minecraft.player.ticksExisted % 4 == 0) {
                  mc.timer.speed = 1.0;
               }

               if (Minecraft.player.fallDistance >= 3.0F && this.NoDmgMode.getMode().equalsIgnoreCase("NCP")) {
                  Minecraft.player.onGround = false;
                  Minecraft.player.motionY = 0.02F;

                  for (int i = 0; i < 30; i++) {
                     Minecraft.player
                        .connection
                        .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 110000.0, Minecraft.player.posZ, false));
                     Minecraft.player
                        .connection
                        .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 2.0, Minecraft.player.posZ, false));
                  }

                  Minecraft.player.connection.sendPacket(new CPacketPlayer(true));
                  Minecraft.player.fallDistance = 0.0F;
               }

               if (Minecraft.player.fallDistance >= 3.0F
                  && Minecraft.player.motionY < -0.4
                  && Minecraft.player.motionY > -1.0
                  && this.NoDmgMode.getMode().equalsIgnoreCase("PreGround")
                  && mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox).isEmpty()
                  && !mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offsetMinDown(0.999)).isEmpty()) {
                  Minecraft.player.fallDistance = 0.0F;
                  Minecraft.player
                     .forceUpdatePlayerServerPosition(
                        Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Minecraft.player.rotationYaw, Minecraft.player.rotationPitch, true
                     );
               }
            } else {
               if (mc.timer.speed == 0.650000243527852) {
                  mc.timer.speed = 1.0;
               }

               this.fall = false;
            }
         }
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived) {
         this.fall = false;
      }

      super.onToggled(actived);
   }
}
