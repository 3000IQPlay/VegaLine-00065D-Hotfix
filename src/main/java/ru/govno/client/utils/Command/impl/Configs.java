package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import org.lwjgl.Sys;
import ru.govno.client.Client;
import ru.govno.client.cfg.ConfigManager;
import ru.govno.client.cfg.GuiConfig;
import ru.govno.client.utils.Command.Command;

public class Configs extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public Configs() {
      super("Configs", new String[]{"config", "cfg"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[1].equalsIgnoreCase("dir") || args[1].equalsIgnoreCase("folder")) {
            Sys.openURL(ConfigManager.configDirectory.getAbsolutePath());
            Client.msg("§a§lConfigs:§r §7Папка с конфигами открыта.", false);
         }

         if (args[1].equalsIgnoreCase("load")) {
            if (Client.configManager.loadConfig(args[2])) {
               GuiConfig.loadedConfig = args[2];
               Client.msg("§a§lConfigs:§r §7Конфиг [§l" + args[2] + "§r§7] §7загружен.", false);
               if (!args[2].equalsIgnoreCase("Default") && Client.configManager.saveConfig("Default")) {
                  mc.entityRenderer.runCfgSaveAnim();
               }
            } else {
               Client.msg("§a§lConfigs:§r §7Конфиг [§l" + args[2] + "§r§7] §7небыл загружен.", false);
            }
         }

         if (args[1].equalsIgnoreCase("save")) {
            if (Client.configManager.saveConfig(args[2])) {
               Client.msg("§a§lConfigs:§r §7Конфиг [§l" + args[2] + "§r§7] §7сохранён.", false);
               ConfigManager.getLoadedConfigs().clear();
               Client.configManager.load();
            } else {
               Client.msg("§a§lConfigs:§r §7Конфиг [§l" + args[2] + "§r§7] §7небыл сохранён.", false);
            }
         }

         if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove")) {
            if (Client.configManager.deleteConfig(args[2])) {
               Client.msg("§a§lConfigs:§r §7Конфиг [§l" + args[2] + "§r§7] §7удалён.", false);
            } else {
               Client.msg("§a§lConfigs:§r §7Конфиг [§l" + args[2] + "§r§7] §7небыл удалён.", false);
            }
         }

         if (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("new")) {
            Client.configManager.saveConfig(args[2]);
            Client.msg("§a§lConfigs:§r §7Конфиг [§l" + args[2] + "§r§7] §7создан и сохранён.", false);
            ConfigManager.getLoadedConfigs().clear();
            Client.configManager.load();
            GuiConfig.loadedConfig = args[2];
         }

         if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("see")) {
            for (int i = 0; i < ConfigManager.getLoadedConfigs().size(); i++) {
               Client.msg("§a§lConfigs:§r §7Конфиг №" + (i + 1) + " [§l" + ConfigManager.getLoadedConfigs().get(i).getName() + "§r§7].", false);
            }
         }

         if (args[1].equalsIgnoreCase("reset") || args[1].equalsIgnoreCase("unload")) {
            if (Client.configManager.loadConfig("nulled")) {
               GuiConfig.loadedConfig = "nulled";
               Client.msg("§a§lConfigs:§r §7Конфиг обнулён.", false);
            } else {
               Client.msg("§a§lConfigs:§r §7Конфиг небыл обнулён.", false);
            }
         }
      } catch (Exception var3) {
         Client.msg("§a§lConfigs:§r §7Комманда написана неверно.", false);
         Client.msg("§a§lConfigs:§r §7сохранить: save [§lNAME§r§7]", false);
         Client.msg("§a§lConfigs:§r §7загрузить: load [§lNAME§r§7]", false);
         Client.msg("§a§lConfigs:§r §7добавить: add/new [§lNAME§r§7]", false);
         Client.msg("§a§lConfigs:§r §7удалить: remove/del [§lNAME§r§7]", false);
         Client.msg("§a§lConfigs:§r §7показать все: list/see", false);
         Client.msg("§a§lConfigs:§r §7показать папку: dir/folder", false);
         Client.msg("§a§lConfigs:§r §7обнулить: reset/unload", false);
         var3.printStackTrace();
      }
   }
}
