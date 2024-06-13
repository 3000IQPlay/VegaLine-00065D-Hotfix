package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class PearlFlight extends Module {
   private final TimerHelper timerHelper = new TimerHelper();
   FloatSettings Speed;
   FloatSettings BoostTime;
   FloatSettings MotionY;
   FloatSettings TimerBoost;
   BoolSettings AutoPearl;
   BoolSettings AutoDisable;
   private boolean pearl = false;
   public static boolean go = false;

   public PearlFlight() {
      super("PearlFlight", 0, Module.Category.MOVEMENT);
      this.settings.add(this.Speed = new FloatSettings("Speed", 1.0F, 1.0F, 0.0F, this));
      this.settings.add(this.BoostTime = new FloatSettings("BoostTime", 800.0F, 1000.0F, 100.0F, this));
      this.settings.add(this.MotionY = new FloatSettings("MotionY", 0.55F, 1.0F, 0.0F, this));
      this.settings.add(this.AutoPearl = new BoolSettings("AutoPearl", true, this));
      this.settings.add(this.TimerBoost = new FloatSettings("TimerBoost", 0.6F, 2.0F, 0.0F, this));
      this.settings.add(this.AutoDisable = new BoolSettings("AutoDisable", true, this));
   }

   @Override
   public void onUpdate() {
      if (!go) {
         this.timerHelper.reset();
      }

      if (Minecraft.player.hurtTime != 0) {
         go = true;
      }

      if (go && this.timerHelper.hasReached((double)((int)this.BoostTime.getFloat()))) {
         go = false;
         this.timerHelper.reset();
         Minecraft.player.speedInAir = 0.02F;
         mc.timer.speed = 1.0;
         if (this.AutoDisable.getBool()) {
            this.toggle(false);
         }
      }

      if (go) {
         mc.timer.speed = (double)(1.0F + this.TimerBoost.getFloat());
         Minecraft.player.speedInAir = 0.8F * this.Speed.getFloat();
         Minecraft.player.motionY = (double)this.MotionY.getFloat();
         if (Minecraft.player.ticksExisted % 3 == 0) {
            MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
         }
      }

      if (this.AutoPearl.getBool() && this.pearl) {
         int oldSlot = Minecraft.player.inventory.currentItem;
         float oldPitch = Minecraft.player.rotationPitch;
         boolean can = false;

         for (int i = 0; i < 9; i++) {
            if (Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemEnderPearl) {
               Minecraft.player.inventory.currentItem = i;
               can = true;
               mc.playerController.updateController();
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Rotation(Minecraft.player.rotationYaw, 90.0F, Minecraft.player.onGround));
               mc.playerController.updateController();
               Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
               mc.playerController.updateController();
               Minecraft.player.inventory.currentItem = oldSlot;
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Rotation(Minecraft.player.rotationYaw, oldPitch, Minecraft.player.onGround));
            }
         }

         if (!can) {
            Client.msg("§f§lModules:§r §7[§lPearlFlight§r§7]: У вас нет эндержемчуга.", false);
            this.toggle(false);
         }

         this.pearl = false;
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         this.pearl = true;
         mc.timer.speed = 1.0;
         this.timerHelper.reset();
      } else {
         Minecraft.player.speedInAir = 0.02F;
         go = false;
      }

      super.onToggled(actived);
   }
}
