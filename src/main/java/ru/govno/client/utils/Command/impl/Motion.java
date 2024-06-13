package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

// TODO: Translate

public class Motion extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public Motion() {
      super("Motion", new String[]{"vmotion", "hmotion", "vm", "hm"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[0].equalsIgnoreCase("vmotion") || args[0].equalsIgnoreCase("vm")) {
            Client.msg(
               "§e§lMotion:§r §7Вы сдвинуты на "
                  + (Double.valueOf(args[1]) > 0.0 ? Double.valueOf(args[1]) : -Double.valueOf(args[1]))
                  + " моушн "
                  + (Double.valueOf(args[1]) > 0.0 ? "вверх." : "вниз."),
               false
            );
            Minecraft.player.motionY = Double.valueOf(args[1]);
            mc.getConnection().sendPacket(new CPacketPlayer(true));
         }

         if (args[0].equalsIgnoreCase("hmotion") || args[0].equalsIgnoreCase("hm")) {
            Client.msg(
               "§e§lMotion:§r §7Вы сдвинуты на "
                  + (Double.valueOf(args[1]) > 0.0 ? Double.valueOf(args[1]) : -Double.valueOf(args[1]))
                  + " моушн "
                  + (Double.valueOf(args[1]) > 0.0 ? "вперёд." : "назад."),
               false
            );
            float f = Minecraft.player.rotationYaw * (float) (Math.PI / 180.0);
            double speed = Double.valueOf(args[1]);
            double x = -((double)MathHelper.sin(f) * speed);
            double z = (double)MathHelper.cos(f) * speed;
            Minecraft.player.motionX = x;
            Minecraft.player.motionZ = z;
         }
      } catch (Exception var9) {
         Client.msg("§e§lMotion:§r §7Комманда написана неверно.", false);
         Client.msg("§e§lMotion:§r §7vmotion: vmotion/vm [§ly+§r§7]", false);
         Client.msg("§e§lMotion:§r §7hmotion: hmotion/hm [§lh+§r§7]", false);
         var9.printStackTrace();
      }
   }
}
