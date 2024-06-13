package ru.govno.client.utils.Command.impl;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import dev.intave.vialoadingbase.ViaLoadingBase;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import net.minecraft.client.Minecraft;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

// TODO: Translate

public class Server extends Command {
   public Server() {
      super("Server", new String[]{"ip", "online", "onl", "ping", "delay", "version", "ver"});
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[0].isEmpty()) {
            Client.msg("§a§lServer:§r §7Комманда написана неверно.", false);
            Client.msg("§a§lServer:§r §7give ip: ip", false);
            Client.msg("§a§lServer:§r §7give online: online/onl", false);
            Client.msg("§a§lServer:§r §7give ping: ping/delay", false);
            Client.msg("§a§lServer:§r §7give version: version/ver", false);
            return;
         }

         Minecraft mc = Minecraft.getMinecraft();
         if (args[0].equalsIgnoreCase("ip")) {
            if (mc.isSingleplayer() || mc.world == null) {
               Client.msg("§a§lServer:§r §7Ip отсутствует.", false);
               return;
            }

            if (mc.getCurrentServerData() != null && !mc.getCurrentServerData().serverIP.isEmpty()) {
               Client.msg("§a§lServer:§r §7Ip сервера: " + mc.getCurrentServerData().serverIP + ".", false);
               StringSelection selection = new StringSelection(mc.getCurrentServerData().serverIP);
               Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
               clipboard.setContents(selection, selection);
               Client.msg("§a§lServer:§r §7Ip копирован в буфер обмена.", false);
               return;
            }
         }

         if (args[0].equalsIgnoreCase("online") || args[0].equalsIgnoreCase("onl")) {
            Client.msg(
               "§a§lServer:§r §7Игроков онлайн: "
                  + (
                     mc.world == null
                        ? 0
                        : (
                           mc.isSingleplayer()
                              ? 1
                              : (
                                 mc.getCurrentServerData() != null && mc.getConnection() != null && mc.getConnection().getPlayerInfoMap() != null
                                    ? mc.getConnection().getPlayerInfoMap().size()
                                    : 0
                              )
                        )
                  )
                  + ".",
               false
            );
            return;
         }

         if (args[0].equalsIgnoreCase("ping") || args[0].equalsIgnoreCase("delay")) {
            Client.msg(
               "§a§lServer:§r §7Ваш пинг: "
                  + (mc.world == null ? 0L : (mc.isSingleplayer() ? 0L : (mc.getCurrentServerData() != null ? mc.getCurrentServerData().pingToServer : 0L)))
                  + ".",
               false
            );
            return;
         }

         if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver")) {
            if (!mc.isSingleplayer() && mc.world != null) {
               int version = mc.getCurrentServerData() != null ? mc.getCurrentServerData().version : -1;
               String versionName = ViaLoadingBase.getProtocols()
                  .stream()
                  .filter(protocol -> protocol.getVersion() == version)
                  .findAny()
                  .orElse(ProtocolVersion.v1_12_2)
                  .getName();
               Client.msg("§a§lServer:§r §7Верная версия: " + versionName + ".", false);
            } else {
               Client.msg("§a§lServer:§r §7Верная версия: 1.12.2.", false);
            }

            return;
         }

         Client.msg("§a§lServer:§r §7Комманда написана неверно.", false);
      } catch (Exception var5) {
         Client.msg("§a§lServer:§r §7Комманда написана неверно.", false);
         Client.msg("§a§lServer:§r §7give ip: ip", false);
         Client.msg("§a§lServer:§r §7give online: online/onl", false);
         Client.msg("§a§lServer:§r §7give ping: ping/delay", false);
         Client.msg("§a§lServer:§r §7give version: version/ver", false);
      }
   }
}
