package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketUseEntity;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;

public class AutoLeave extends Module {
   ModeSettings LeaveType;
   BoolSettings SethomePreLeave;
   FloatSettings LeaveOnDistance;
   FloatSettings LeaveOnHealth;
   String ab = "";

   public AutoLeave() {
      super("AutoLeave", 0, Module.Category.PLAYER);
      this.settings.add(this.LeaveType = new ModeSettings("LeaveType", "/spawn", this, new String[]{"/spawn", "/lobby", "/logout", "disconnect", "SelfKick"}));
      this.settings.add(this.SethomePreLeave = new BoolSettings("SethomePreLeave", true, this));
      this.settings.add(this.LeaveOnDistance = new FloatSettings("LeaveOnDistance", 40.0F, 120.0F, 10.0F, this));
      this.settings.add(this.LeaveOnHealth = new FloatSettings("LeaveOnHealth", 5.0F, 20.0F, 0.0F, this));
   }

   private final EntityPlayer getMe() {
      return (EntityPlayer)(FreeCam.fakePlayer != null && FreeCam.get.actived ? FreeCam.fakePlayer : Minecraft.player);
   }

   private final boolean playersIsInRange(float range) {
      for (Entity e : mc.world.getLoadedEntityList()) {
         EntityPlayer player = null;
         if (e != null && e instanceof EntityOtherPlayerMP) {
            player = (EntityPlayer)e;
         }

         if (player != null
            && !Client.friendManager.isFriend(player.getName())
            && player != FreeCam.fakePlayer
            && player.getEntityId() != 462462999
            && !Client.isClientAdmin(player)
            && this.getMe() != null
            && this.getMe().getDistanceToEntity(player) < range) {
            this.msg(player);
            return true;
         }
      }

      return false;
   }

   private boolean leaveByHp() {
      return Minecraft.player.getHealth() < this.LeaveOnHealth.getFloat();
   }

   private final void msg(EntityPlayer e) {
      if (e == null) {
         Client.msg("§f§lModules:§r §7[§lAutoLeave§r§7]: У вас осталось " + (int)Minecraft.player.getHealth() + "ХП.", false);
      } else {
         Client.msg("§f§lModules:§r §7[§lAutoLeave§r§7]: Рядом с вами нежелательный игрок", false);
         Client.msg("§7Его имя: " + e.getDisplayName().getFormattedText(), false);
         this.ab = e.getDisplayName().getFormattedText();
      }
   }

   private final void doLeave(boolean isPlayer) {
      String type = this.LeaveType.currentMode;
      if (type.equalsIgnoreCase("/spawn")) {
         Minecraft.player.connection.preSendPacket(new CPacketChatMessage("/spawn"));
      } else if (type.equalsIgnoreCase("/lobby")) {
         Minecraft.player.connection.preSendPacket(new CPacketChatMessage("/hub"));
      } else if (type.equalsIgnoreCase("/logout")) {
         Minecraft.player.connection.preSendPacket(new CPacketChatMessage("/logout"));
      } else if (type.equalsIgnoreCase("disconnect")) {
         if (isPlayer) {
            mc.world.sendQuittingDisconnectingPacket("§f§lModules:§r §7[§lAutoLeave§r§7]: Обнаружен нежелательный игрок с ником: " + this.ab);
         } else {
            mc.world.sendQuittingDisconnectingPacket("§f§lModules:§r §7[§lAutoLeave§r§7]: У вас осталось " + (int)Minecraft.player.getHealth() + "ХП.");
         }
      }

      this.toggle(false);
   }

   private final void doSethome() {
      Minecraft.player.sendChatMessage("/sethome home");
   }

   @Override
   public void onUpdate() {
      if (!this.LeaveType.currentMode.equalsIgnoreCase("SelfKick")) {
         if (this.playersIsInRange(this.LeaveOnDistance.getFloat())) {
            if (this.SethomePreLeave.getBool()) {
               this.doSethome();
            }

            this.doLeave(true);
         } else {
            if (this.leaveByHp()) {
               if (this.SethomePreLeave.getBool()) {
                  this.doSethome();
               }

               this.doLeave(false);
            }
         }
      } else {
         for (int i = 0; i < 100; i++) {
            mc.getConnection().sendPacket(new CPacketUseEntity(Minecraft.player));
         }

         this.toggle(false);
      }
   }
}
