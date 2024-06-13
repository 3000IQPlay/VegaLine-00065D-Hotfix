package ru.govno.client.utils;

import dev.intave.viamcp.ViaMCP;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.impl.Panic;

public class DiscordRP {
   private boolean running = true;
   private long created = 0L;
   private String firstLine;
   private String secondLine;

   public void start() {
      this.created = System.currentTimeMillis();
      DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(new ReadyCallback() {
         @Override
         public void apply(DiscordUser user) {
            System.out.println("Websome " + user.username + "#" + user.discriminator + ".");
            DiscordRP.this.update("Загрузка...", "Ещё чуть-чуть");
         }
      }).build();
      DiscordRPC.discordInitialize("1054570317726617711", handlers, true);
      (new Thread("Discord RPC Callback") {
         @Override
         public void run() {
            while (DiscordRP.this.running) {
               DiscordRPC.discordRunCallbacks();
            }
         }
      }).start();
   }

   public void shutdown() {
      this.running = false;
      DiscordRPC.discordClearPresence();
      DiscordRPC.discordShutdown();
   }

   public void refresh() {
      if (!Panic.stop) {
         if (this.firstLine != null && this.secondLine != null) {
            DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(this.secondLine + this.mods());
            b.setBigImage("large", "Версия" + Client.version.replace("00", "").trim() + " | Бета " + Client.version.replace("00", "0"));
            b.setSmallImage("large2", ViaMCP.INSTANCE.getViaPanel().getCurrentProtocol().getName());
            b.setDetails(this.firstLine);
            b.setStartTimestamps(this.created);
            DiscordRPC.discordUpdatePresence(b.build());
         }
      }
   }

   public void replace() {
      if (this.firstLine != null && this.secondLine != null) {
         DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(this.secondLine + this.mods());
         b.setBigImage("large", "Версия" + Client.version.replace("00", "").trim() + " | Бета " + Client.version.replace("00", "0"));
         b.setSmallImage("large2", ViaMCP.INSTANCE.getViaPanel().getCurrentProtocol().getName());
         b.setDetails(this.firstLine);
         b.setStartTimestamps(this.created);
         DiscordRPC.discordUpdatePresence(b.build());
         this.firstLine = null;
         this.secondLine = null;
      }
   }

   private String mods() {
      return Client.moduleManager != null
         ? " | Моды: " + Client.moduleManager.getEnabledModulesCount() + "/" + Client.moduleManager.getModuleList().size()
         : "";
   }

   public void update(String firstLine, String secondLine) {
      this.firstLine = firstLine;
      this.secondLine = secondLine;
      if (this.mods() != "") {
         secondLine = secondLine + this.mods();
      }

      DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(secondLine);
      b.setBigImage("large", "Версия" + Client.version.replace("00", "").trim() + " | Бета " + Client.version.replace("00", "0"));
      b.setSmallImage("large2", ViaMCP.INSTANCE.getViaPanel().getCurrentProtocol().getName());
      b.setDetails(firstLine);
      b.setStartTimestamps(this.created);
      DiscordRPC.discordUpdatePresence(b.build());
   }
}
