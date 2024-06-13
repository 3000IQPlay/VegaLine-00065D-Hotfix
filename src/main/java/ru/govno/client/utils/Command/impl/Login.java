package ru.govno.client.utils.Command.impl;

import java.security.SecureRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.Session;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

public class Login extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final String alphabet = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
   private static final SecureRandom secureRandom = new SecureRandom();
   static boolean run = false;
   static String nn = null;
   static String nn2 = null;

   public Login() {
      super("Login", new String[]{"login", "l", "connect", "con"});
   }

   public static String randomString(int strLength) {
      StringBuilder stringBuilder = new StringBuilder(strLength);

      for (int i = 0; i < strLength; i++) {
         stringBuilder.append(
            "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
               .charAt(secureRandom.nextInt("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890".length()))
         );
      }

      return stringBuilder.toString();
   }

   public static void doRct() {
      if (run && Minecraft.player != null && mc.world != null && Minecraft.player.connection != null && mc.getSession() != null) {
         if (nn != null) {
            mc.session = new Session(nn, "", "", "legacy");
         }

         for (int i = 0; i < 60; i++) {
            System.out.println(nn);
         }

         if (!mc.isSingleplayer() || nn2 != null) {
            if (nn2 != null) {
               GuiDisconnected.lastServer = nn2;
            }

            boolean flag = mc.isIntegratedServerRunning();
            boolean flag1 = mc.isConnectedToRealms();
            mc.world.sendQuittingDisconnectingPacket();
            if (flag) {
               mc.displayGuiScreen(new GuiMainMenu());
            } else if (flag1) {
               RealmsBridge realmsbridge = new RealmsBridge();
               realmsbridge.switchToRealms(new GuiMainMenu());
            } else {
               mc.displayGuiScreen(new GuiMainMenu());
            }

            if (nn2 != null) {
               GuiDisconnected.does = true;
            }
         }

         run = false;
      }
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (!args[0].equalsIgnoreCase("login") && !args[0].equalsIgnoreCase("l")) {
            if (args[0].equalsIgnoreCase("connect") || args[0].equalsIgnoreCase("con")) {
               if (!args[1].isEmpty()) {
                  nn2 = args[1];
                  nn = null;
                  String nickname = mc.session.getUsername();
                  if (args.length == 3 && !args[2].isEmpty()) {
                     if (!mc.isSingleplayer()) {
                        if (args[2].equalsIgnoreCase("random")) {
                           nickname = Client.randomNickname();
                        } else {
                           nickname = args[2];
                        }

                        nn = nickname;
                        run = true;
                        return;
                     }

                     String warn = args[2].length() > 16
                        ? "ваш ник слишком длинный."
                        : (
                           args[2].length() < 3
                              ? "ваш ник слишком короткий."
                              : (mc.isSingleplayer() ? "вы не можете менять ник в одиночной игре." : "Комманда написана неверно.")
                        );
                     Client.msg("§7§lLogin:§r §7" + warn, false);
                     return;
                  }

                  run = true;
                  Client.msg("§7§lLogin:§r §7захожу на ip " + args[1], false);
                  return;
               }

               Client.msg("§7§lLogin:§r §7Комманда написана неверно.", false);
               Client.msg("§7§lLogin:§r §7login: login/l [§lNAME / random§r§7]", false);
               Client.msg("§7§lLogin:§r §7connect: connect/con [§lIP / IP + NAME§r§7]", false);
            }
         } else {
            if (!mc.isSingleplayer() && args[1] != null && !args[1].isEmpty()) {
               String nickname = mc.session.getUsername();
               if (args[1].equalsIgnoreCase("random") || args[1].equalsIgnoreCase("r")) {
                  nickname = Client.randomNickname();
               } else if (args[1].length() <= 16 && args[1].length() >= 3) {
                  nickname = args[1];
               }

               nn = nickname;
               nn2 = mc.getCurrentServerData().serverIP;
               run = true;
               Client.msg("§7§lLogin:§r §7вы заходите под ником §b" + nickname, false);
               return;
            }

            String warn = args[1] != null && !args[1].isEmpty()
               ? (
                  args[1].length() > 16
                     ? "ваш ник слишком длинный."
                     : (
                        args[1].length() < 3
                           ? "ваш ник слишком короткий."
                           : (mc.isSingleplayer() ? "вы не можете менять ник в одиночной игре." : "Комманда написана неверно.")
                     )
               )
               : "укажите ник.";
            Client.msg("§7§lLogin:§r §7" + warn, false);
         }
      } catch (Exception var4) {
         Client.msg("§7§lLogin:§r §7Комманда написана неверно.", false);
         Client.msg("§7§lLogin:§r §7login: login/l [§lNAME / random§r§7]", false);
         Client.msg("§7§lLogin:§r §7connect: connect/con [§lIP / IP + NAME§r§7]", false);
         var4.printStackTrace();
      }
   }
}
