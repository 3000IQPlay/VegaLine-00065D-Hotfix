package ru.govno.client.utils.Command.impl;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;
import ru.govno.client.utils.MacroMngr.Macros;
import ru.govno.client.utils.MacroMngr.MacrosManager;

public class Macro
extends Command {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public Macro() {
        super("Macro", new String[]{"macros", "macro", "bind say", "mc"});
    }

    @Override
    public void onCommand(String[] args) {
        try {
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("new")) {
                if (MacrosManager.macroses.size() >= 30) {
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u041b\u0438\u043c\u0438\u0442 \u0432 100 \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432 \u0437\u0430\u043f\u043e\u043b\u043d\u0435\u043d,", false);
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u043d\u0443\u0436\u043d\u043e \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u043e\u0434\u0438\u043d \u0438\u0437 \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432 \u0434\u043b\u044f \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u043d\u043e\u0432\u043e\u0433\u043e!", false);
                    return;
                }
                StringBuilder command = new StringBuilder();
                for (int i = 4; i < args.length; ++i) {
                    command.append(args[i]).append(" ");
                }
                for (Macros macros : MacrosManager.macroses) {
                    if (!macros.getName().equalsIgnoreCase(args[2])) continue;
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u00a77[\u00a7l" + args[2] + "\u00a7r\u00a77] \u00a7c\u0443\u0436\u0435 \u0437\u0430\u043d\u044f\u0442\u043e!", false);
                    return;
                }
                MacrosManager.macroses.add(new Macros(args[2], Keyboard.getKeyIndex((String)args[3].toUpperCase()), "" + command));
                Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7a\u041d\u043e\u0432\u044b\u0439 \u043c\u0430\u043a\u0440\u043e\u0441 \u00a77[\u00a7l" + args[2] + "\u00a7r\u00a77] \u00a7a\u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d.", false);
                return;
            }
            if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del")) {
                for (Macros macros : MacrosManager.macroses) {
                    if (!macros.getName().equalsIgnoreCase(args[2])) continue;
                    MacrosManager.macroses.remove(macros);
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u041c\u0430\u043a\u0440\u043e\u0441 \u00a77[\u00a7l" + args[2] + "\u00a7r\u00a77] \u00a7c\u0443\u0434\u0430\u043b\u0451\u043d.", false);
                    return;
                }
                Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u041c\u0430\u043a\u0440\u043e\u0441\u0430 \u00a77[\u00a7l" + args[2] + "\u00a7r\u00a77] \u00a7c\u043d\u0435\u0442 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435.", false);
                return;
            }
            if (args[1].equalsIgnoreCase("clear") && args[2].equalsIgnoreCase("clear all") || args[1].equalsIgnoreCase("ci")) {
                if (MacrosManager.macroses.size() != 0) {
                    MacrosManager.macroses.clear();
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432 \u0431\u044b\u043b \u043e\u0447\u0438\u0449\u0435\u043d!", false);
                    return;
                }
                Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432 \u043f\u0443\u0441\u0442!", false);
                return;
            }
            if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("see")) {
                if (MacrosManager.macroses.size() == 0) {
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a7c\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432 \u043f\u0443\u0441\u0442!", false);
                }
                for (Macros macros : MacrosManager.macroses) {
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a76\u0418\u043c\u044f: \u00a7a\u00a7n" + macros.getName() + "\u00a7r\u00a77 \u041a\u043d\u043e\u043f\u043a\u0430 [\u00a7l" + Keyboard.getKeyName((int)macros.getKey()) + "\u00a7r\u00a77] \u00a7f" + macros.getMassage(), false);
                }
                return;
            }
            if (args[1].equalsIgnoreCase("use") || args[1].equalsIgnoreCase("test")) {
                for (Macros macros : MacrosManager.macroses) {
                    if (macros.getName().equalsIgnoreCase(args[2])) {
                        macros.use();
                        Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u041c\u0430\u043a\u0440\u043e\u0441 [" + args[2] + "] \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442.", false);
                        continue;
                    }
                    Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u041c\u0430\u043a\u0440\u043e\u0441\u0430 [" + args[2] + "] \u043d\u0435\u0442 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435.", false);
                }
            }
        }
        catch (Exception formatException) {
            Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
            Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u044b\u00a7r: \u00a78[\u00a77macros/macro/mc\u00a78].", false);
            Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a77\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c\u00a7r: \u00a78[\u00a77add/new name key msg\u00a78].", false);
            Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a77\u0423\u0434\u0430\u043b\u0438\u0442\u044c\u00a7r: \u00a78[\u00a77remove/delete/del [name].", false);
            Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a77\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c\u00a7r: \u00a78[\u00a77clear all/ci\u00a78].", false);
            Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a77\u0421\u043f\u0438\u0441\u043e\u043a\u00a7r: \u00a78[\u00a77list/see\u00a78].", false);
            Client.msg("\u00a76\u00a7lMacros:\u00a7r\u00a77 \u00a77\u0422\u0435\u0441\u0442\u00a7r: \u00a78[\u00a77use/test\u00a78].", false);
            formatException.printStackTrace();
        }
    }
}

