package ru.govno.client.utils.Command.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

// TODO: Translate

public class Chat extends Command {
   public static List<String> blackListMassages = new CopyOnWriteArrayList<>();

   public Chat() {
      super("Chat", new String[]{"ignore"});
   }

   Minecraft mc() {
      return Minecraft.getMinecraft();
   }

   public static boolean stringIsContainsBadMassage(String text) {
      if (blackListMassages.size() != 0) {
         boolean suspicious = false;

         for (String blms : blackListMassages) {
            if (text.contains(blms)) {
               suspicious = !text.contains("Chat:");
            }
         }

         if (suspicious) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("new")) {
            if (args[2] == null) {
               Client.msg("§e§lChat:§r §7введите контайн сообщения", false);
            } else {
               boolean ujeEst = false;
               if (blackListMassages.size() > 0) {
                  for (String blm : blackListMassages) {
                     if (blm.equalsIgnoreCase(args[2])) {
                        ujeEst = true;
                     }
                  }
               }

               if (ujeEst) {
                  Client.msg("§e§lChat:§r §7контайн уже есть в списке", false);
               } else {
                  blackListMassages.add(args[2]);
                  Client.msg("§e§lChat:§r §7добавлен новый контайн с сообщение:", false);
                  Client.msg("§e§lChat:§r §7" + args[2], false);
               }
            }
         }

         if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("del")) {
            if (args[2] == null) {
               Client.msg("§e§lChat:§r §7введите контайн списка", false);
            } else if (blackListMassages.size() == 0) {
               Client.msg("§e§lChat:§r §7список игноров пуст", false);
            } else {
               String name = "unnamed blm";

               for (String blmx : blackListMassages) {
                  if (blmx.equalsIgnoreCase(args[2])) {
                     name = blmx;
                  }
               }

               if (name.equalsIgnoreCase("unnamed blm")) {
                  Client.msg("§e§lChat:§r §7этого кoнтайна нет в списке", false);
               } else {
                  blackListMassages.remove(name);
                  Client.msg("§e§lChat:§r §7контайн был удалён ->", false);
                  Client.msg("§e§lChat:§r §7" + args[2], false);
               }
            }
         }

         if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("see")) {
            if (blackListMassages.size() == 0) {
               Client.msg("§e§lChat:§r §7список игноров пуст", false);
            } else {
               Client.msg("§e§lChat:§r §7список игноров:", false);
               int number = 0;

               for (String blmxx : blackListMassages) {
                  Client.msg("§e§lChat:§r §7№" + ++number + ": " + blmxx, false);
               }
            }
         }

         if (args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("ci")) {
            if (blackListMassages.size() == 0) {
               Client.msg("§e§lChat:§r §7список игноров пуст", false);
            } else {
               blackListMassages.clear();
               Client.msg("§e§lChat:§r §7список игноров oчищен", false);
            }
         }
      } catch (Exception var5) {
         Client.msg("§e§lChat:§r §7Комманда написана неверно.", false);
         Client.msg("§e§lChat:§r §7use: ignore", false);
         Client.msg("§e§lChat:§r §7add: add/new [§lSTRING§r§7]", false);
         Client.msg("§e§lChat:§r §7remove: remove/del [§lSTRING§r§7]", false);
         Client.msg("§e§lChat:§r §7list: list/see", false);
         Client.msg("§e§lChat:§r §7clear: clear/ci", false);
      }
   }
}
