package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;
import ru.govno.client.utils.MacroMngr.Macros;
import ru.govno.client.utils.MacroMngr.MacrosManager;

// TODO: Translate

public class Macro extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public Macro() {
      super("Macro", new String[]{"macros", "macro", "bind say", "mc"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("new")) {
            if (MacrosManager.macroses.size() >= 30) {
               Client.msg("§6§lMacros:§r§7 §cЛимит в 100 макросов заполнен,", false);
               Client.msg("§6§lMacros:§r§7 §cнужно удалить один из макросов для добавления нового!", false);
               return;
            }

            StringBuilder command = new StringBuilder();

            for (int i = 4; i < args.length; i++) {
               command.append(args[i]).append(" ");
            }

            for (Macros macros : MacrosManager.macroses) {
               if (macros.getName().equalsIgnoreCase(args[2])) {
                  Client.msg("§6§lMacros:§r§7 §cНазвание §7[§l" + args[2] + "§r§7] §cуже занято!", false);
                  return;
               }
            }

            MacrosManager.macroses.add(new Macros(args[2], Keyboard.getKeyIndex(args[3].toUpperCase()), command + ""));
            Client.msg("§6§lMacros:§r§7 §aНовый макрос §7[§l" + args[2] + "§r§7] §aдобавлен.", false);
            return;
         }

         if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del")) {
            for (Macros macrosx : MacrosManager.macroses) {
               if (macrosx.getName().equalsIgnoreCase(args[2])) {
                  MacrosManager.macroses.remove(macrosx);
                  Client.msg("§6§lMacros:§r§7 §cМакрос §7[§l" + args[2] + "§r§7] §cудалён.", false);
                  return;
               }
            }

            Client.msg("§6§lMacros:§r§7 §cМакроса §7[§l" + args[2] + "§r§7] §cнет в списке.", false);
            return;
         }

         if (args[1].equalsIgnoreCase("clear") && args[2].equalsIgnoreCase("clear all") || args[1].equalsIgnoreCase("ci")) {
            if (MacrosManager.macroses.size() != 0) {
               MacrosManager.macroses.clear();
               Client.msg("§6§lMacros:§r§7 §cСписок макросов был очищен!", false);
               return;
            }

            Client.msg("§6§lMacros:§r§7 §cСписок макросов пуст!", false);
            return;
         }

         if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("see")) {
            if (MacrosManager.macroses.size() == 0) {
               Client.msg("§6§lMacros:§r§7 §cСписок макросов пуст!", false);
            }

            for (Macros macrosxx : MacrosManager.macroses) {
               Client.msg(
                  "§6§lMacros:§r§7 §6Имя: §a§n"
                     + macrosxx.getName()
                     + "§r§7 Кнопка [§l"
                     + Keyboard.getKeyName(macrosxx.getKey())
                     + "§r§7] §f"
                     + macrosxx.getMassage(),
                  false
               );
            }

            return;
         }

         if (args[1].equalsIgnoreCase("use") || args[1].equalsIgnoreCase("test")) {
            for (Macros macrosxx : MacrosManager.macroses) {
               if (macrosxx.getName().equalsIgnoreCase(args[2])) {
                  macrosxx.use();
                  Client.msg("§6§lMacros:§r§7 Макрос [" + args[2] + "] работает.", false);
               } else {
                  Client.msg("§6§lMacros:§r§7 Макроса [" + args[2] + "] нет в списке.", false);
               }
            }
         }
      } catch (Exception var5) {
         Client.msg("§6§lMacros:§r§7 Комманда написана неверно.", false);
         Client.msg("§6§lMacros:§r§7 §7Комманды§r: §8[§7macros/macro/mc§8].", false);
         Client.msg("§6§lMacros:§r§7 §7Добавить§r: §8[§7add/new name key msg§8].", false);
         Client.msg("§6§lMacros:§r§7 §7Удалить§r: §8[§7remove/delete/del [name].", false);
         Client.msg("§6§lMacros:§r§7 §7Очистить§r: §8[§7clear all/ci§8].", false);
         Client.msg("§6§lMacros:§r§7 §7Список§r: §8[§7list/see§8].", false);
         Client.msg("§6§lMacros:§r§7 §7Тест§r: §8[§7use/test§8].", false);
         var5.printStackTrace();
      }
   }
}
