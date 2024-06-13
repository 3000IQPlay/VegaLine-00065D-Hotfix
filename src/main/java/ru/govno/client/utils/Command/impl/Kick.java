package ru.govno.client.utils.Command.impl;

import dev.intave.viamcp.fixes.AttackOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

public class Kick extends Command {
   public Kick() {
      super("Kick", new String[]{"kick", "k"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[1].isEmpty()) {
            Client.msg("§8§lKick:§r §7Комманда написана неверно.", false);
            Client.msg("§8§lKick:§r §7допиши способ кика (hit/bp/spam).", false);
            return;
         }

         if (args[1].equalsIgnoreCase("hit")) {
            if (Minecraft.getMinecraft().isSingleplayer()) {
               Client.msg("§8§lKick:§r §7в локальном мире это использовать невозможно.", false);
               return;
            }

            for (int i = 0; i < 2; i++) {
               AttackOrder.sendFixedAttack(Minecraft.player, Minecraft.player, EnumHand.OFF_HAND);
            }

            Client.msg("§8§lKick:§r §7произвожу попытку кинуться.", false);
            return;
         }

         if (args[1].equalsIgnoreCase("bp")) {
            for (int i = 0; i < 300; i++) {
               Minecraft.getMinecraft()
                  .getConnection()
                  .sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockPos.ORIGIN, EnumFacing.UP));
               Minecraft.getMinecraft()
                  .getConnection()
                  .sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos.ORIGIN, EnumFacing.DOWN));
            }

            Client.msg("§8§lKick:§r §7произвожу попытку кинуться.", false);
            return;
         }

         if (args[1].equalsIgnoreCase("spam")) {
            for (int i = 0; i < 1000; i++) {
               Minecraft.getMinecraft()
                  .getConnection()
                  .sendPacket(
                     new CPacketChatMessage(
                        "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                     )
                  );
            }

            Client.msg("§8§lKick:§r §7произвожу попытку кинуться.", false);
            return;
         }

         Client.msg("§8§lKick:§r §7Комманда написана неверно.", false);
         Client.msg("§8§lKick:§r §7видимо вы допустили ошибку в названии метода.", false);
      } catch (Exception var3) {
         Client.msg("§8§lKick:§r §7Комманда написана неверно.", false);
         Client.msg("§8§lKick:§r §7kick:kick/k [§lmethod§r§7]", false);
         Client.msg("§8§lKick:§r §7methods: hit/bp/spam", false);
      }
   }
}
