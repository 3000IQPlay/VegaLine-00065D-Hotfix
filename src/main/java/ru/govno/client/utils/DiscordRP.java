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
    public static String tag = "nullName";
    public static String avatar = null;
    private String firstLine;
    private String secondLine;

    public void start() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        this.created = System.currentTimeMillis();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(new ReadyCallback(){

            @Override
            public void apply(DiscordUser user) {
                avatar = "hYYYps://cdn.dMMMcordapp.com/avatars/".replace("YYY", "tt").replace("MMM", "is") + user.userId + "/" + user.avatar + ".png";
                if (tag.equalsIgnoreCase("nullName")) {
                    tag = user.username;
                }
                DiscordRP.this.update("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430...", "\u0415\u0449\u0451 \u0447\u0443\u0442\u044c-\u0447\u0443\u0442\u044c");
            }
        }).build();
        DiscordRPC.discordInitialize("1054570317726617711", handlers, true);
        new Thread("DTTTord RPC Callback".replace("TTT", "isc")){

            @Override
            public void run() {
                while (DiscordRP.this.running) {
                    DiscordRPC.discordRunCallbacks();
                }
            }
        }.start();
    }

    public void shutdown() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        this.running = false;
        DiscordRPC.discordClearPresence();
        DiscordRPC.discordShutdown();
    }

    public void refresh() {
        if (Panic.stop) {
            return;
        }
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        if (this.firstLine != null && this.secondLine != null && ViaMCP.INSTANCE != null) {
            DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(this.secondLine + this.mods());
            String name = Client.name.replace("00", "").trim();
            b.setBigImage("large", Client.nameCut + " " + Client.releaseType + " v065 " + name.replace(Client.nameCut, ""));
            b.setSmallImage("large2", ViaMCP.INSTANCE.getViaPanel().getCurrentProtocol().getName());
            b.setDetails(this.firstLine);
            b.setStartTimestamps(this.created);
            DiscordRPC.discordUpdatePresence(b.build());
        }
    }

    public void replace() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        if (this.firstLine != null && this.secondLine != null) {
            DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(this.secondLine + this.mods());
            String name = Client.name.replace("00", "").trim();
            b.setBigImage("large", Client.nameCut + " " + Client.releaseType + " v065 " + name.replace(Client.nameCut, ""));
            b.setSmallImage("large2", "Minecraft " + ViaMCP.INSTANCE.getViaPanel().getCurrentProtocol().getName() + " UID: " + Client.uid);
            b.setDetails(this.firstLine);
            b.setStartTimestamps(this.created);
            DiscordRPC.discordUpdatePresence(b.build());
            this.firstLine = null;
            this.secondLine = null;
        }
    }

    private String mods() {
        return Client.moduleManager != null ? " | \u041c\u043e\u0434\u044b: " + Client.moduleManager.getEnabledModulesCount() + "/" + Client.moduleManager.getModuleList().size() : "";
    }

    public void update(String firstLine, String secondLine) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        if (this.mods() != "") {
            secondLine = (String)secondLine + this.mods();
        }
        DiscordRichPresence.Builder b = new DiscordRichPresence.Builder((String)secondLine);
        String name = Client.name.replace("00", "").trim();
        b.setBigImage("large", Client.nameCut + " " + Client.releaseType + " v065 " + name.replace(Client.nameCut, ""));
        b.setSmallImage("large2", "Minecraft " + ViaMCP.INSTANCE.getViaPanel().getCurrentProtocol().getName() + " UID: " + Client.uid);
        b.setDetails(firstLine);
        b.setStartTimestamps(this.created);
        DiscordRPC.discordUpdatePresence(b.build());
    }
}

