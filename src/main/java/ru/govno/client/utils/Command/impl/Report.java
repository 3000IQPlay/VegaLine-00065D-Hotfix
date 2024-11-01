package ru.govno.client.utils.Command.impl;

import java.awt.Color;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import ru.govno.client.Client;
import ru.govno.client.utils.Command.Command;
import ru.govno.client.utils.Command.Whook;
import ru.govno.client.utils.DiscordRP;
import ru.govno.client.utils.TimerHelper;

public class Report
extends Command {
    private final TimerHelper timer = new TimerHelper();

    public Report() {
        super("Report", new String[]{"report", "problem"});
    }

    @Override
    public void onCommand(String[] args) {
        try {
            if (args.length > 3) {
                if (!this.timer.hasReached(207000.0f) && !Client.summit(Minecraft.player)) {
                    long timeWait = 207000L - this.timer.getTime();
                    Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77\u0434\u043b\u044f \u043e\u0442\u043f\u0440\u0430\u0432\u043a\u0438 \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c \u0436\u0434\u0430\u0442\u044c:" + Report.getReadableTime(timeWait), false);
                    return;
                }
                String discord = args[1];
                Object property = "";
                for (int i = 2; i < args.length; ++i) {
                    property = (String)property + args[i] + " ";
                }
                if (((String)property).length() > 14 && discord.length() > 3) {
                    if (this.sendReport((String)property, discord)) {
                        Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77\u0432\u0430\u0448 \u0440\u0435\u043f\u043e\u0440\u0442 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d.", false);
                    } else {
                        Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77\u043d\u0435\u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043f\u0440\u0430\u0432\u0438\u0442\u044c \u0440\u0435\u043f\u043e\u0440\u0442", false);
                    }
                } else {
                    Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
                }
            } else {
                Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
                Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77report (your discord) (problems)", false);
            }
        }
        catch (Exception formatException) {
            Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77\u041a\u043e\u043c\u043c\u0430\u043d\u0434\u0430 \u043d\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e.", false);
            Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77report (your discord) (problems)", false);
            Client.msg("\u00a7c\u00a7lReport:\u00a7r \u00a77\u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0440\u0435\u043f\u043e\u0440\u0442\u044b \u0447\u0430\u0449\u0435 3\u0445 \u043c\u0438\u043d\u0443\u0442 \u043d\u0435\u043b\u044c\u0437\u044f", false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean sendReport(String problem, String discordAccount) {
        Whook whook = new Whook("");
        whook.setUsername("\ud83c\udd85\ud83c\udd74\ud83c\udd76\ud83c\udd70\ud83c\udd7b\ud83c\udd78\ud83c\udd7d\ud83c\udd74 " + System.getProperty("user.name") + " | " + discordAccount);
        Whook.EmbedObject embedObject = new Whook.EmbedObject();
        embedObject.addField("VL version", Client.typeUpdate + " update | " + Client.version, true);
        embedObject.addField("User", System.getProperty("user.name") + " | " + Report.getHwid() + " | ", true);
        embedObject.addField("Discord", discordAccount + " | user discord: " + DiscordRP.tag, true);
        embedObject.addField("Problem description", problem, false);
        embedObject.setColor(Color.getHSBColor((float)(Arrays.hashCode(Report.getHwid().getBytes()) % 3600) / 3600.0f, 1.0f, 1.0f));
        whook.addEmbed(embedObject);
        try {
            whook.execute();
            boolean i5 = true;
            return i5;
        }
        catch (Exception e) {
            e.fillInStackTrace();
        }
        finally {
            this.timer.reset();
        }
        return false;
    }

    public static String getHwid() {
        try {
            byte[] byteData;
            String e = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(e.getBytes());
            StringBuffer hexString = new StringBuffer();
            for (byte g : byteData = md.digest()) {
                String hex = Integer.toHexString(0xFF & g);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (Exception e) {
            e.fillInStackTrace();
            return "error whid";
        }
    }

    public static String getReadableTime(long numberOfMilliseconds) {
        long numberOfDays = TimeUnit.MILLISECONDS.toDays(numberOfMilliseconds);
        long numberOfHours = TimeUnit.MILLISECONDS.toHours(numberOfMilliseconds -= TimeUnit.DAYS.toMillis(numberOfDays));
        long numberOfMinutes = TimeUnit.MILLISECONDS.toMinutes(numberOfMilliseconds -= TimeUnit.HOURS.toMillis(numberOfHours));
        long numberOfSeconds = TimeUnit.MILLISECONDS.toSeconds(numberOfMilliseconds -= TimeUnit.MINUTES.toMillis(numberOfMinutes));
        numberOfMilliseconds -= TimeUnit.SECONDS.toMillis(numberOfSeconds);
        StringBuilder stringBuilder = new StringBuilder();
        if (numberOfMinutes > 0L) {
            if (numberOfMinutes == 1L) {
                stringBuilder.append(String.format("%dm ", numberOfMinutes));
            } else {
                stringBuilder.append(String.format("%dm ", numberOfMinutes));
            }
        }
        if (numberOfSeconds > 0L) {
            if (numberOfSeconds == 1L) {
                stringBuilder.append(String.format("%ds ", numberOfSeconds));
            } else {
                stringBuilder.append(String.format("%ds ", numberOfSeconds));
            }
        }
        return stringBuilder.toString();
    }
}

