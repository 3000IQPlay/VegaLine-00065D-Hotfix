package ru.govno.client.utils.Command.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.lwjgl.input.Keyboard;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Command.Command;

// TODO: Translate

public class Bind extends Command {
   public Bind() {
      super("Bind", new String[]{"bind", "b", "bnd", "unbind", "unb"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         for (Module f : Client.moduleManager.getModuleList()) {
            if (args[1].equalsIgnoreCase("get") && f.getName().equalsIgnoreCase(args[2])) {
               Client.msg("§b§lBinds:§r §7[§7§l" + f.getName() + "§r§7] key: " + Keyboard.getKeyName(f.getBind()) + ".", false);
            } else if (!args[1].equalsIgnoreCase("see") && !args[1].equalsIgnoreCase("list")) {
               if (!args[0].equalsIgnoreCase("bind") && !args[0].equalsIgnoreCase("bnd") && !args[0].equalsIgnoreCase("b")) {
                  if (args[0].equalsIgnoreCase("unbind") || args[0].equalsIgnoreCase("unb")) {
                     if (args[1].equalsIgnoreCase("all")) {
                        List<Module> toUnbind = Client.moduleManager
                           .getModuleList()
                           .stream()
                           .filter(m -> m.getBind() != 0 && !m.getName().equalsIgnoreCase("ClickGui"))
                           .collect(Collectors.toList());
                        if (toUnbind.isEmpty()) {
                           Client.msg("§b§lBinds:§r §7Все модули уже были разбинжены.", false);
                        } else {
                           Client.msg("§b§lBinds:§r §7Все модули (" + toUnbind.size() + "шт) были разбинжены.", false);
                           toUnbind.forEach(m -> m.setBind(0));
                        }

                        return;
                     }

                     if (f.getName().equalsIgnoreCase(args[1])) {
                        if (f.getBind() == 0) {
                           Client.msg("§b§lBinds:§r §7Модуль [§7§l" + f.getName() + "§r§7] уже разбинжен.", false);
                        } else {
                           Client.msg("§b§lBinds:§r §7Модуль [§7§l" + f.getName() + "§r§7] был разбинжен.", false);
                           f.setBind(0);
                        }

                        return;
                     }
                  }
               } else if (f.getName().equalsIgnoreCase(args[1])) {
                  if (!args[2].equalsIgnoreCase("null") && !args[2].equalsIgnoreCase("none") && !args[2].equalsIgnoreCase("0")) {
                     if (f.getBind() == Keyboard.getKeyIndex(args[2].toUpperCase())) {
                        Client.msg("§b§lBinds:§r §7Модуль [§l" + f.getName() + "§r§7] уже забинжен на [§l" + args[2].toLowerCase() + "§r§7].", false);
                     } else {
                        Client.msg("§b§lBinds:§r §7Модуль [§l" + f.getName() + "§r§7] был забинжен на [§l" + args[2].toLowerCase() + "§r§7].", false);
                        f.setBind(Keyboard.getKeyIndex(args[2].toUpperCase()));
                     }
                  } else if (f.getBind() == 0) {
                     Client.msg("§b§lBinds:§r §7Модуль [§7§l" + f.getName() + "§r§7] уже разбинжен.", false);
                  } else {
                     Client.msg("§b§lBinds:§r §7Модуль [§7§l" + f.getName() + "§r§7] был разбинжен.", false);
                     f.setBind(0);
                  }

                  return;
               }
            } else if ((args[0].equalsIgnoreCase("bind") || args[0].equalsIgnoreCase("bnd") || args[0].equalsIgnoreCase("b")) && f.getBind() != 0) {
               Client.msg("§b§lBinds:§r §7[§7§l" + f.getName() + "§r§7] key: " + Keyboard.getKeyName(f.getBind()) + ".", false);
            }
         }
      } catch (Exception var5) {
         Client.msg("§b§lBinds:§r §7Комманда написана неверно.", false);
         Client.msg("§b§lBinds:§r §7bind: bind/bnd/b [§lname§r§7] [§lkey§r§7]", false);
         Client.msg("§b§lBinds:§r §7unbind: unbind/unbnd [§lname§r§7 | §lall§r§7]", false);
         Client.msg("§b§lBinds:§r §7show binds: list/see", false);
         Client.msg("§b§lBinds:§r §7get bind: get [§lname§r§7]", false);
      }
   }
}
