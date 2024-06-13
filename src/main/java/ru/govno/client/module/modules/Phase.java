package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathHelper;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class Phase extends Module {
   TimerHelper timer = new TimerHelper();
   boolean flagg;
   ModeSettings Mode;

   public Phase() {
      super("Phase", 0, Module.Category.PLAYER);
      this.settings.add(this.Mode = new ModeSettings("Mode", "MatrixSkip", this, new String[]{"MatrixNotOpaque", "MatrixSkip", "PacketNitro"}));
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (this.Mode.getMode().equalsIgnoreCase("MatrixNotOpaque")
         && Minecraft.player != null
         && mc.world != null
         && !Minecraft.player.isDead
         && this.actived
         && event.getPacket() instanceof CPacketConfirmTeleport) {
         this.flagg = true;
      }

      if (this.Mode.getMode().equalsIgnoreCase("MatrixSkip")) {
         SPacketPlayerPosLook pos = (SPacketPlayerPosLook)event.getPacket();
         float x = (float)(pos.x - Minecraft.player.posX);
         float y = (float)(pos.y - Minecraft.player.posY);
         float z = (float)(pos.z - Minecraft.player.posZ);
         float distance = MathHelper.sqrt((double)(x * x + y * y + z * z));
         if (distance <= 8.0F) {
            event.setCancelled(true);
            Minecraft.player.connection.sendPacket(new CPacketPlayer.PositionRotation(pos.x, pos.y, pos.z, pos.getYaw(), pos.getPitch(), true));
         }
      }
   }

   @EventTarget
   public void onPUpdate(EventPlayerMotionUpdate event) {
      if (this.Mode.getMode().equalsIgnoreCase("PacketNitro") && this.actived) {
         boolean opaue = !mc.world
            .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ))
            .getMaterial()
            .isReplaceable();
         if (!Minecraft.player.isCollidedHorizontally) {
            this.timer.reset();
         }

         if (this.timer.hasReached(150.0) && !opaue) {
            double dst = 0.65;
            double xe = Minecraft.player.posX - Math.sin((double)MoveMeHelp.moveYaw(Minecraft.player.rotationYaw) * 0.017453292) * dst;
            double ye = Minecraft.player.posY;
            double ze = Minecraft.player.posZ + Math.cos((double)MoveMeHelp.moveYaw(Minecraft.player.rotationYaw) * 0.017453292) * dst;
            Minecraft.player.forceUpdatePlayerServerPosition(xe, ye, ze, Minecraft.player.rotationYaw, Minecraft.player.rotationPitch, true);
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(xe, ye, ze, true));
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(xe, ye + 0.1, ze, true));
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(xe, ye, ze, true));
            Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(xe, ye, ze, true));
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(0.0);
            this.timer.reset();
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.Mode.getMode().equalsIgnoreCase("MatrixNotOpaque")) {
         for (int i = 0; i < 2; i++) {
            if (Minecraft.player.isCollidedHorizontally) {
               if (mc.gameSettings.keyBindForward.pressed) {
                  Minecraft.player
                     .connection
                     .sendPacket(
                        new CPacketPlayer.Position(
                           Minecraft.player.posX - Math.sin(Math.toRadians((double)Minecraft.player.rotationYaw)) * 0.25,
                           Minecraft.player.posY,
                           Minecraft.player.posZ + Math.cos(Math.toRadians((double)Minecraft.player.rotationYaw)) * 0.25,
                           true
                        )
                     );
                  Minecraft.player
                     .connection
                     .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 15000.0, Minecraft.player.posZ, true));
               }

               Minecraft.player
                  .connection
                  .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 9000.0, Minecraft.player.posZ, true));
               Minecraft.player
                  .connection
                  .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY - 20.0, Minecraft.player.posZ, false));
            }
         }
      }

      if (this.Mode.getMode().equalsIgnoreCase("MatrixSkip")) {
         if ((double)Minecraft.player.fallDistance > 0.08
            && Minecraft.player.posY != (double)((int)Minecraft.player.posY)
            && Minecraft.player.posY != (double)((float)((int)Minecraft.player.posY) + 0.5F)) {
            this.toggle(false);
         }

         if (Minecraft.player.onGround) {
            this.flagg = true;
            Minecraft.player
               .setPosition(
                  Minecraft.player.posX + -Math.sin((double)((float)Math.toRadians((double)Minecraft.player.rotationYaw))) * 1.999F,
                  Minecraft.player.posY - 1.0E-5,
                  Minecraft.player.posZ + Math.cos((double)((float)Math.toRadians((double)Minecraft.player.rotationYaw))) * 1.999F
               );
            Minecraft.player.motionY = -0.0476;
            Entity.motiony = 0.01F;
         }
      }
   }
}
