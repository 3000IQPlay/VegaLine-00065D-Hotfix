package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

// TODO: Translate

public class Teleport extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public Teleport() {
      super("Teleport", new String[]{"teleport", "tport", "tp"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         String ip = "";
         if (!mc.isSingleplayer()) {
            ip = mc.getCurrentServerData().serverIP;
         }

         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tport") || args[0].equalsIgnoreCase("tp")) {
               boolean tp = false;

               for (EntityPlayer entity : mc.world.playerEntities) {
                  if (entity.getName().equalsIgnoreCase(args[1])) {
                     double xe = entity.posX;
                     double ye = entity.posY;
                     double ze = entity.posZ;
                     if (ip.equalsIgnoreCase("mc.reallyworld.ru")) {
                        tp = true;
                     } else if (ip.equalsIgnoreCase("mc.mstnw.net")) {
                        Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
                        Minecraft.player.setPositionAndUpdate(xe, ye, ze);
                     } else {
                        Minecraft.player.setPositionAndUpdate(xe, ye, ze);
                     }

                     Client.msg("§9§lTeleport:§r §7Вы тепнулись к [§l" + args[1] + "§r§7].", false);
                  }
               }

               if (!tp) {
                  Client.msg("§9§lTeleport:§r §7Игрока с ником [§l" + args[1] + "§r§7] нет в мире.", false);
               }
            }
         } else if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tport") || args[0].equalsIgnoreCase("tp")) {
            boolean xyz = args.length == 4;
            float xs = (float)Integer.valueOf(args[1]).intValue() + 0.5F;
            float ys = (float)(xyz ? Integer.valueOf(args[2]) : (int)Minecraft.player.posY);
            float zs = (float)(xyz ? Integer.valueOf(args[3]) : Integer.valueOf(args[2])).intValue() + 0.5F;
            if (!ip.equalsIgnoreCase("mc.reallyworld.ru")) {
               Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SNEAKING));
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Position((double)xs, (double)(ys - 1.0F), (double)zs, false));
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Position((double)xs, (double)ys, (double)zs, false));
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Position((double)xs, 1.0, (double)zs, false));
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Position((double)xs, (double)ys, (double)zs, false));
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Position((double)xs, (double)ys + 0.42, (double)zs, true));
               Minecraft.player.connection.sendPacket(new CPacketPlayer.Position((double)xs, (double)ys, (double)zs, false));
               Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            Client.msg("§9§lTeleport:§r §7Вы тепнулись на [§lx: " + (int)xs + ",y: " + (int)ys + ",z: " + (int)zs + "§r§7]", false);
            if (!ip.equalsIgnoreCase("mc.reallyworld.ru")) {
               mc.renderGlobal.loadRenderers();
            }
         }
      } catch (Exception var12) {
         Client.msg("Комманда написана неверно.", false);
         Client.msg("§9§lTeleport:§r §7Комманда написана неверно.", false);
         Client.msg("§9§lTeleport:§r §7teleport: teleport/tport/tp [§lName§r§7]", false);
         Client.msg("§9§lTeleport:§r §7teleport: teleport/tport/tp [§lx,y,z/x,z§r§7]", false);
         var12.printStackTrace();
      }
   }
}
