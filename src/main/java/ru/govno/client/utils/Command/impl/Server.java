package ru.govno.client.utils.Command.impl;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import dev.intave.vialoadingbase.ViaLoadingBase;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import net.minecraft.client.Minecraft;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;

public class Server
extends Command {
    public Server() {
        super("Server", new String[]{"ip", "online", "onl", "ping", "delay", "version", "ver"});
    }

    @Override
    public void onCommand(String[] args) {
        try {
            if (args[0].isEmpty()) {
                Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
                Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give ip: ip", false);
                Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give online: online/onl", false);
                Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give ping: ping/delay", false);
                Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give version: version/ver", false);
                return;
            }
            Minecraft mc = Minecraft.getMinecraft();
            if (args[0].equalsIgnoreCase("ip")) {
                if (mc.isSingleplayer() || mc.world == null) {
                    Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77Ip \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442.", false);
                    return;
                }
                if (mc.getCurrentServerData() != null && !mc.getCurrentServerData().serverIP.isEmpty()) {
                    Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77Ip \u0441\u0435\u0440\u0432\u0435\u0440\u0430: " + mc.getCurrentServerData().serverIP + ".", false);
                    StringSelection selection = new StringSelection(mc.getCurrentServerData().serverIP);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77Ip \u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d \u0432 \u0431\u0443\u0444\u0435\u0440 \u043e\u0431\u043c\u0435\u043d\u0430.", false);
                    return;
                }
            }
            if (args[0].equalsIgnoreCase("online") || args[0].equalsIgnoreCase("onl")) {
                Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77\u0418\u0433\u0440\u043e\u043a\u043e\u0432 \u043e\u043d\u043b\u0430\u0439\u043d: " + (mc.world == null ? 0 : (mc.isSingleplayer() ? 1 : (mc.getCurrentServerData() != null && mc.getConnection() != null && mc.getConnection().getPlayerInfoMap() != null ? mc.getConnection().getPlayerInfoMap().size() : 0))) + ".", false);
                return;
            }
            if (args[0].equalsIgnoreCase("ping") || args[0].equalsIgnoreCase("delay")) {
                Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77\u0412\u0430\u0448 \u043f\u0438\u043d\u0433: " + (mc.world == null ? 0L : (mc.isSingleplayer() ? 0L : (mc.getCurrentServerData() != null ? mc.getCurrentServerData().pingToServer : 0L))) + ".", false);
                return;
            }
            if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver")) {
                if (mc.isSingleplayer() || mc.world == null) {
                    Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77\u0412\u0435\u0440\u043d\u0430\u044f \u0432\u0435\u0440\u0441\u0438\u044f: 1.12.2.", false);
                } else {
                    int version = mc.getCurrentServerData() != null ? mc.getCurrentServerData().version : -1;
                    String versionName = ViaLoadingBase.getProtocols().stream().filter(protocol -> protocol.getVersion() == version).findAny().orElse(ProtocolVersion.v1_12_2).getName();
                    Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77\u0412\u0435\u0440\u043d\u0430\u044f \u0432\u0435\u0440\u0441\u0438\u044f: " + versionName + ".", false);
                }
                return;
            }
            Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
        }
        catch (Exception formatException) {
            Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
            Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give ip: ip", false);
            Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give online: online/onl", false);
            Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give ping: ping/delay", false);
            Client.msg("\u00a7a\u00a7lServer:\u00a7r \u00a77give version: version/ver", false);
        }
    }
}

