package ru.govno.client.utils.Command.impl;

import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

public class GetCommands extends Command {
   public GetCommands() {
      super("GetCommands", new String[]{"gc", "getcom", "getcommands"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         Client.moduleManager.getModule("STabMonitor").toggleSilent(true);
      } catch (Exception var3) {
         Client.msg("§5§lGetCommands:§r §7The command was written incorrectly.", false);
         Client.msg("§5§lGetCommands:§r §7Use: getcommands/getcom/gc", false);
         var3.printStackTrace();
      }
   }
}
