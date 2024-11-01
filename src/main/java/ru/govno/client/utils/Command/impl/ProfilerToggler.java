package ru.govno.client.utils.Command.impl;

import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;
import ru.govno.client.utils.UProfiler;

public class ProfilerToggler
extends Command {
    public ProfilerToggler() {
        super("ProfilerToggler", new String[]{"profiler", "prof"});
    }

    public static boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onCommand(String[] args) {
        try {
            if (args[1].equalsIgnoreCase("toggle") || args[1].equalsIgnoreCase("t")) {
                if (Client.uProfilers.isEnabled()) {
                    Client.uProfilers.stop();
                } else {
                    Client.uProfilers.start();
                }
                if (Client.uProfilers.isEnabled()) {
                    for (UProfiler \u0433Profiler : Client.uProfilers.getProfilers()) {
                        Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a7a\u041f\u0440\u043e\u0444\u0430\u0439\u043b\u0435\u0440 \u043e\u0442\u043a\u0440\u044b\u0442: " + \u0433Profiler.getNameProfiler() + "\u00a78 uProfiler", false);
                    }
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a7a\u041f\u0440\u043e\u0444\u0430\u0439\u043b\u0435\u0440\u044b \u043f\u0440\u0438\u043c\u0435\u043d\u0435\u043d\u044b: " + Client.uProfilers.getProfilers().size() + "\u0448\u0442 - " + Client.uProfilers.getDelay() + " ms updating", false);
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a7a\u0421\u0447\u0451\u0442\u0447\u0438\u043a \u043a\u0430\u0434\u0440\u043e\u0432 \u043f\u0440\u0438\u043c\u0435\u043d\u0451\u043d", false);
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a7a\u041c\u043e\u043d\u0438\u0442\u043e\u0440\u0438\u043d\u0433 \u0441\u0438\u0441\u0442\u0435\u043c\u044b \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d", false);
                } else {
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a7c\u041f\u0440\u043e\u0444\u0430\u0439\u043b\u0435\u0440\u044b: " + Client.uProfilers.getProfilers().size() + "\u0448\u0442 \u0432\u044b\u0433\u0440\u0443\u0436\u0435\u043d\u044b.", false);
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a7c\u0421\u0447\u0451\u0442\u0447\u0438\u043a \u043a\u0430\u0434\u0440\u043e\u0432 \u0441\u043a\u0440\u044b\u0442.", false);
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a7c\u041c\u043e\u043d\u0438\u0442\u043e\u0440\u0438\u043d\u0433 \u0441\u0438\u0441\u0442\u0435\u043c\u044b \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d.", false);
                }
            } else if (args[1].equalsIgnoreCase("delay") && args.length == 3) {
                if (ProfilerToggler.isNumeric(args[2])) {
                    long delay = Long.parseLong(args[2]);
                    if (delay >= 0L && delay < 60000L) {
                        Client.uProfilers.setDelays(delay);
                        Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u0440\u043e\u0432\u0430\u0439\u043b\u0435\u0440\u0430 \u0442\u0435\u043f\u0435\u0440\u044c " + delay, false);
                    } else {
                        Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
                        Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77min delay 0, max delay 60000", false);
                    }
                } else {
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
                    Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c - delay (delayms)", false);
                }
            }
        }
        catch (Exception formatException) {
            Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
            Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77\u041f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0438\u0442\u044c - toggle.", false);
            Client.msg("\u00a77\u00a7lProfiler:\u00a7r \u00a77\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c - delay (delayms)", false);
            formatException.fillInStackTrace();
        }
    }
}

