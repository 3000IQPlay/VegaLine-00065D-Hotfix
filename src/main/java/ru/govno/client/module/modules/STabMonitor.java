package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Math.TimerHelper;

public class STabMonitor extends Module {
   TimerHelper timer = new TimerHelper();

   public STabMonitor() {
      super("STabMonitor", 0, Module.Category.MISC, false, () -> false);
   }

   String decoloredString(String toDecolor) {
      List<String> CHAR_SAMPLES = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "c", "e", "a", "b", "d", "f", "r", "l", "k", "o", "m", "n");
      String formatChar = "§";

      for (String sample : CHAR_SAMPLES) {
         toDecolor = toDecolor.replace("§" + sample, "");
      }

      return toDecolor;
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         mc.getConnection().sendPacket(new CPacketTabComplete("/", BlockPos.ORIGIN, false));
         mc.getConnection().sendPacket(new CPacketTabComplete("", BlockPos.ORIGIN, false));
         this.timer.reset();
      }

      super.onToggled(actived);
   }

   String getStringAsMassive(String[] massiveString) {
      String string = "";

      for (String str : massiveString) {
         string = string + str;
      }

      return string;
   }

   @EventTarget
   public void onReceived(EventReceivePacket received) {
      if (received.getPacket() instanceof SPacketTabComplete completed) {
         this.toggleSilent(false);
         if (completed.getMatches().length != 0) {
            List<String> outPut = Arrays.asList(this.getStringAsMassive(completed.getMatches()).split("/"))
               .stream()
               .filter(str -> str.length() > 0)
               .map(str -> str.replace(":", "").replace(";", "").replace(" ", ""))
               .toList();
            List<String> outPutFiltered = new ArrayList<>();

            for (String out : outPut) {
               if (!outPutFiltered.stream().anyMatch(str -> str.contains(out))) {
                  outPutFiltered.add(out);
               }
            }

            outPutFiltered.sort(String.CASE_INSENSITIVE_ORDER);
            if (outPutFiltered.isEmpty()) {
               Client.msg("§b§lWorldInfo:§r §7Failed to get result.", false);
               return;
            }

            Client.msg("§b§lWorldInfo:§r §7Results found -> " + outPutFiltered.size() + ":", false);
            int number = 0;

            for (String result : outPutFiltered) {
               Client.msg("§7№" + ++number + "§r -> " + this.decoloredString(result) + (number == outPutFiltered.size() ? "." : ";"), this.actived);
            }

            return;
         }

         Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7]: Failed to get result.", false);
      }
   }

   @Override
   public void onUpdate() {
      if (mc.getConnection() == null
         || Minecraft.player == null
         || mc.getConnection().getPlayerInfo(Minecraft.player.getUniqueID()) == null
         || this.timer.hasReached((double)((long)mc.getConnection().getPlayerInfo(Minecraft.player.getUniqueID()).getResponseTime() + 450L))) {
         this.toggle(false);
      }
   }
}
