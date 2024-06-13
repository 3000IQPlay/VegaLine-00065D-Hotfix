package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketVehicleMove;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

public class RidingDRSync extends Command {
   private Entity ridingEntity;

   public RidingDRSync() {
      super("RidingDRSync", new String[]{"entity", "ent", "riding", "ride"});
   }

   private final Minecraft mc() {
      return Minecraft.getMinecraft();
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[1].equalsIgnoreCase("getHui")) {
         }

         if (args[1].equalsIgnoreCase("desync") || args[1].equalsIgnoreCase("des")) {
            this.mc();
            if (Minecraft.player.getRidingEntity() != null) {
               this.mc();
               this.ridingEntity = Minecraft.player.getRidingEntity();
               this.mc();
               Minecraft.player.dismountRidingEntity();
               this.mc().world.removeEntity(this.ridingEntity);
               Client.msg("§9§lRiding:§r §7desync is successful", false);
            } else {
               Client.msg("§9§lRiding:§r §7riding entity is null", false);
            }

            return;
         }

         if (args[1].equalsIgnoreCase("resync") || args[1].equalsIgnoreCase("res")) {
            if (this.ridingEntity != null) {
               this.mc().world.addEntityToWorld(this.ridingEntity.getEntityId(), this.ridingEntity);
               this.mc();
               Minecraft.player.startRiding(this.ridingEntity);
               this.mc();
               Minecraft.player.ridingEntity = this.ridingEntity;
               Client.msg("§9§lRiding:§r §7resync is successful", false);
               this.ridingEntity = null;
            } else {
               Client.msg("§9§lRiding:§r §7old riding entity is null", false);
            }

            return;
         }

         if (args[1].equalsIgnoreCase("dismount") || args[1].equalsIgnoreCase("dis")) {
            this.mc();
            if (Minecraft.player.getRidingEntity() != null) {
               this.mc();
               if (Minecraft.player.isSneaking()) {
                  this.mc();
                  if (!Minecraft.player.isAirBorne) {
                     NetHandlerPlayClient var10000 = this.mc().getConnection();
                     this.mc();
                     var10000.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
                  }
               }

               NetHandlerPlayClient var4 = this.mc().getConnection();
               this.mc();
               var4.sendPacket(new CPacketVehicleMove(Minecraft.player.getRidingEntity()));
               var4 = this.mc().getConnection();
               this.mc();
               var4.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SNEAKING));
               this.mc();
               Minecraft.player.dismountRidingEntity();
               Client.msg("§9§lRiding:§r §7dismount entity is successful", false);
            } else {
               Client.msg("§9§lRiding:§r §7riding entity is null", false);
            }

            return;
         }
      } catch (Exception var3) {
         Client.msg("§9§lRiding:§r §7Комманда написана неверно.", false);
         Client.msg("§9§lRiding:§r §7use: entity/ent/riding/ride", false);
         Client.msg("§9§lRiding:§r §7desync: desync/des", false);
         Client.msg("§9§lRiding:§r §7resync: resync/res", false);
         Client.msg("§9§lRiding:§r §7dismount: dismount/dis", false);
      }
   }
}
