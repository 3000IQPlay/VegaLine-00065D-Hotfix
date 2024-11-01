package ru.govno.client.utils;

import com.google.common.collect.Lists;
import com.viaversion.viaversion.util.ReflectionUtil;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import optifine.EG;
import org.apache.commons.io.FileUtils;
import org.lwjgl.Sys;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.utils.DiscordRP;

public class CLaunch {
    private int i;
    private boolean bn;
    private boolean bnlt;
    private boolean f;
    private boolean d;
    private boolean ia;
    private boolean na;
    private boolean noreq;
    private final ArrayList<String> nms = new ArrayList();
    private final String seqC = "JJJtMMH:600700paHMMin.cTYYaw/".replace("JJJ", "h").replace("600", "/").replace("700", "/").replace("MMH", "tps").replace("HMM", "steb").replace("TYY", "om/r");
    private final String bin = "2duZ6uu1";
    private final String binN = "UcBChKJg";
    private final String binU = "DAq3wpZz";

    public int ud() {
        System.out.println("RRIY6 = ".replace("RR", "u".toUpperCase()).replace("Y6", "D") + this.i);
        return this.i;
    }

    public boolean isBN() {
        return this.bn;
    }

    public boolean isBNLT() {
        return this.bnlt;
    }

    public boolean delCL() {
        return this.d || this.isBNLT();
    }

    public boolean isFreak() {
        return this.f;
    }

    public ArrayList<String> getNMS() {
        return this.nms;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CLaunch() {
        HttpURLConnection connection = null;
        try {
            Object aof = "";
            for (String af : EG.ao) {
                aof = (String)aof + af;
            }
            URL u = new URL((String)aof);
            connection = (HttpURLConnection)u.openConnection();
            connection.setRequestMethod("HEAD");
        }
        catch (MalformedURLException e) {
            Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u041f\u043e\u0445\u043e\u0436\u0435 \u0447\u0442\u043e \u0443 \u0432\u0430\u0441 \u0447\u0442\u043e-\u0442\u043e \u043d\u0435 \u0442\u0430\u043a \u0441 \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442\u043e\u043c.");
            CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(5000, 15000)));
        }
        catch (IOException e) {
            Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u041f\u043e\u0445\u043e\u0436\u0435 \u0447\u0442\u043e \u0443 \u0432\u0430\u0441 \u0447\u0442\u043e-\u0442\u043e \u043d\u0435 \u0442\u0430\u043a \u0441 \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442\u043e\u043c.");
            CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(5000, 15000)));
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            } else {
                Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u041f\u043e\u0445\u043e\u0436\u0435 \u0447\u0442\u043e \u0443 \u0432\u0430\u0441 \u0447\u0442\u043e-\u0442\u043e \u043d\u0435 \u0442\u0430\u043a \u0441 \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442\u043e\u043c.");
                CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(5000, 15000)));
            }
        }
        ArrayList lines = Lists.newArrayList();
        try {
            Scanner iterator = new Scanner(new URL(this.seqC + "2duZ6uu1").openStream());
            while (iterator.hasNext()) {
                lines.add((String)iterator.next());
            }
            if (lines.size() == 0) {
                Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u041f\u043e\u0445\u043e\u0436\u0435 \u0447\u0442\u043e \u0443 \u0432\u0430\u0441 \u0447\u0442\u043e-\u0442\u043e \u043d\u0435 \u0442\u0430\u043a \u0441 \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442\u043e\u043c.");
                CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(5000, 15000)));
            }
        }
        catch (IOException ignored) {
            System.exit(new Random().nextInt(0, 1000));
        }
        if (this.anyC(lines)) {
            block47: {
                if (this.delCL()) {
                    try {
                        String usersPath = FileUtils.getUserDirectory().getAbsolutePath();
                        for (String deletePath : Arrays.asList(usersPath + "\\AppData\\Local\\gamepath", usersPath + "\\AppData\\Roaming\\VEGA.NCO", usersPath + "\\Desktop\\VL.lnk", usersPath + "\\Downloads\\VL057M_Installer.exe", usersPath + "\\Downloads\\VL058M_Installer.exe", usersPath + "\\Downloads\\VL059B_Installer.exe", usersPath + "\\Downloads\\VL060M_Installer.exe", usersPath + "\\Downloads\\VL061M_Installer.exe", usersPath + "\\Downloads\\VL062M_Installer.exe", usersPath + "\\Downloads\\VL063M_Installer.exe", usersPath + "\\Downloads\\VL064M_Installer.exe", usersPath + "\\Downloads\\VL065M_Installer.exe", usersPath + "\\Downloads\\VL066M_Installer.exe", usersPath + "\\Downloads\\VL067M_Installer.exe", usersPath + "\\Downloads\\VL068M_Installer.exe")) {
                            File file;
                            if (deletePath == null || (file = new File(deletePath)) == null) continue;
                            if (!file.delete()) {
                                file.deleteOnExit();
                            }
                            for (File fileIn : file.listFiles()) {
                                if (fileIn.delete()) continue;
                                fileIn.deleteOnExit();
                            }
                        }
                    }
                    catch (Exception usersPath) {
                        // empty catch block
                    }
                    if (this.isBNLT()) {
                        Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u0412\u0430\u0448 \u0438\u043d\u0434\u0435\u043d\u0442\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u0431\u044b\u043b \u0437\u0430\u0431\u0430\u043d\u0435\u043d \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u043e\u043c \u043d\u0430\u0432\u0441\u0435\u0433\u0434\u0430 \u0431\u0435\u0437 \u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e\u0441\u0442\u0438 \u0432\u0435\u0440\u043d\u0443\u0442\u044c \u0434\u043e\u0441\u0442\u0443\u043f.                                                                         Your ID has been permanently banned by the administrator without being able to regain access.");
                        CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(1000, 3000)));
                    } else {
                        Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u0412\u0430\u0448 \u0438\u043d\u0434\u0435\u043d\u0442\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u0431\u044b\u043b \u0437\u0430\u0431\u0430\u043d\u0435\u043d \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u043e\u043c.                             Your ID has been banned by the administrator.");
                        CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(1000, 3000)));
                    }
                }
                if (this.isBNLT()) {
                    Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u0412\u0430\u0448 \u0438\u043d\u0434\u0435\u043d\u0442\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u0431\u044b\u043b \u0437\u0430\u0431\u0430\u043d\u0435\u043d \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u043e\u043c \u043d\u0430\u0432\u0441\u0435\u0433\u0434\u0430 \u0431\u0435\u0437 \u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e\u0441\u0442\u0438 \u0432\u0435\u0440\u043d\u0443\u0442\u044c \u0434\u043e\u0441\u0442\u0443\u043f.                                                                         Your ID has been permanently banned by the administrator without being able to regain access.");
                    CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(1000, 3000)));
                } else if (this.isBN()) {
                    Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)"\u0412\u0430\u0448 \u0438\u043d\u0434\u0435\u043d\u0442\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u0431\u044b\u043b \u0432\u0440\u0435\u043c\u0435\u043d\u043d\u043e \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u043e\u043c.    Your ID has been temporarily blocked by the administrator.");
                    CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(1000, 3000)));
                }
                try {
                    if (this.ia) {
                        this.nms.clear();
                    } else {
                        Scanner iterator2 = new Scanner(new URL(this.seqC + "UcBChKJg").openStream());
                        ArrayList nm = Lists.newArrayList();
                        while (iterator2.hasNext()) {
                            nm.add((String)iterator2.next());
                        }
                        this.nms.clear();
                        this.nms.addAll(nm);
                    }
                }
                catch (IOException iterator2) {
                    // empty catch block
                }
                try {
                    if (this.ia || this.noreq) break block47;
                    Scanner i = new Scanner(new URL(this.seqC + "DAq3wpZz").openStream());
                    ArrayList args = Lists.newArrayList();
                    while (i.hasNext()) {
                        args.add((String)i.next());
                    }
                    if (args.isEmpty()) break block47;
                    int curVer = Integer.MAX_VALUE;
                    String updUrl = null;
                    String msgUpd = null;
                    boolean required = false;
                    try {
                        boolean outdated;
                        block29: for (int arged = 0; arged < args.size(); arged += 2) {
                            String value = (String)args.get(arged + 1);
                            switch (arged / 2) {
                                case 0: {
                                    curVer = Integer.valueOf(value);
                                    continue block29;
                                }
                                case 1: {
                                    updUrl = String.valueOf(value);
                                    continue block29;
                                }
                                case 2: {
                                    msgUpd = value.replaceAll("_", " ");
                                    continue block29;
                                }
                                case 3: {
                                    required = Boolean.valueOf(value);
                                }
                            }
                        }
                        boolean bl = outdated = Integer.parseInt("065") < curVer;
                        if (outdated) {
                            this.noreq = true;
                            if (updUrl != null && !updUrl.equalsIgnoreCase("null")) {
                                Desktop.getDesktop().browse(URI.create(updUrl));
                            }
                            Sys.alert((String)"SSSL n555work".replace("SSS", "V").replace("555", "et"), (String)("\u0422\u0435\u043a\u0443\u0449\u0430\u044f \u0432\u0435\u0440\u0441\u0438\u044f \u043a\u043b\u0438\u0435\u043d\u0442\u0430 \u0443\u0441\u0442\u0430\u0440\u0435\u043b\u0430, \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u0435 \u0438 \u043e\u0431\u043d\u043e\u0432\u0438\u0442\u0435 \u0435\u0451 \u0434\u043e \u0431\u043e\u043b\u0435\u0435 \u043d\u043e\u0432\u043e\u0439. " + (updUrl == null && !updUrl.equalsIgnoreCase("null") ? "\u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u043c\u043e \u043e\u0431\u0440\u0430\u0442\u0438\u0442\u044c\u0441\u044f \u043a \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0443 \u0434\u043b\u044f \u043f\u0440\u0435\u0434\u043e\u0441\u0442\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u044f." : "\u041d\u0430\u0436\u043c\u0438\u0442\u0435 [\u041e\u041a] \u0438 \u0432\u0430\u043c \u043f\u0440\u0435\u0434\u043e\u0441\u0442\u0430\u0432\u0438\u0442\u0441\u044f \u0441\u0441\u044b\u043b\u043a\u0430 \u043d\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0443 \u043e\u0431\u043d\u043e\u0432\u043b\u0451\u043d\u043d\u043e\u0439 \u0432\u0435\u0440\u0441\u0438\u0438. \u0412\u0430\u043c \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u043e \u0435\u0451 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0434\u043b\u044f \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0435\u043d\u0438\u044f \u0438\u0433\u0440\u044b.") + (String)(msgUpd == null ? "" : "                                                                                                             \u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u043e\u0442 \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0430: " + msgUpd)));
                            if (required) {
                                CompletableFuture.runAsync(() -> System.exit(new Random().nextInt(1000, 3000)));
                            }
                        }
                    }
                    catch (Exception exception) {}
                }
                catch (IOException i) {
                    // empty catch block
                }
            }
            return;
        }
        StringSelection I = new StringSelection(this.uID());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(I, I);
        String ms = "\u0412 \u0432\u0430\u0448 \u0431\u0443\u0444\u0444\u0435\u0440 \u043e\u0431\u043c\u0435\u043d\u0430 \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d \u043a\u043e\u0434, \u0435\u0441\u043b\u0438 \u0432\u044b \u043f\u0440\u0438\u043e\u0431\u0440\u0435\u043b\u0438 VL \u043e\u0442\u043f\u0440\u0430\u0432\u0442\u0435 \u044d\u0442\u043e\u0442 \u043a\u043e\u0434 \u0432 \u043b\u0438\u0447\u043d\u044b\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f VEGA33 \u0432 Discord \u0441 \u043f\u043e\u043c\u043e\u0449\u044c\u044e (CTRL+V, ENTER) \u043d\u0430 \u0432\u0430\u0448\u0435\u0439 \u043a\u043b\u0430\u0432\u0438\u0430\u0442\u0443\u0440\u0435. \u041f\u043e\u0441\u043b\u0435 \u0432\u0430\u0448\u0435\u0439 \u0432\u0435\u0440\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u0438 \u043a\u043b\u0438\u0435\u043d\u0442 \u0431\u0443\u0434\u0435\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c.";
        Sys.alert((String)"\u0440L log in".replace("\u0440", "V"), (String)ms);
        System.exit(new Random().nextInt(0, 1000));
    }

    public static CLaunch hook() {
        return new CLaunch();
    }

    public static String cC(String m) {
        char[] abcCyr = new char[]{' ', '\u0430', '\u0431', '\u0432', '\u0433', '\u0434', '\u0453', '\u0435', '\u0436', '\u0437', '\u0455', '\u0438', '\u0458', '\u043a', '\u043b', '\u0459', '\u043c', '\u043d', '\u045a', '\u043e', '\u043f', '\u0440', '\u0441', '\u0442', '\u045c', '\u0443', '\u0444', '\u0445', '\u0446', '\u0447', '\u045f', '\u0448', '\u0410', '\u0411', '\u0412', '\u0413', '\u0414', '\u0403', '\u0415', '\u0416', '\u0417', '\u0405', '\u0418', '\u0408', '\u041a', '\u041b', '\u0409', '\u041c', '\u041d', '\u040a', '\u041e', '\u041f', '\u0420', '\u0421', '\u0422', '\u040c', '\u0423', '\u0424', '\u0425', '\u0426', '\u0427', '\u040f', '\u0428', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '/', '-'};
        String[] abcLat = new String[]{" ", "a", "b", "v", "g", "d", "]", "e", "zh", "z", "y", "i", "j", "k", "l", "q", "m", "n", "w", "o", "p", "r", "s", "t", "'", "u", "f", "h", "c", ";", "x", "{", "A", "B", "V", "G", "D", "}", "E", "Zh", "Z", "Y", "I", "J", "K", "L", "Q", "M", "N", "W", "O", "P", "R", "S", "T", "KJ", "U", "F", "H", "C", ":", "X", "{", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "/", "-"};
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < m.length(); ++i) {
            for (int x = 0; x < abcLat.length; ++x) {
                if (m.charAt(i) != abcCyr[x]) continue;
                b.append(abcCyr[x]);
            }
        }
        return b.toString();
    }

    private final String etg(String s) {
        String e = "";
        for (int i = 0; i < s.length(); i++) {
            String cc = String.valueOf(s.charAt(i));
            while (cc.length() < 3) {
                cc = "0" + cc;
            }
            e = e + cc;
        }

        return e;
    }

    private final String uID() {
        try {
            byte[] byteData;
            String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();
            for (byte aByteData : byteData = md.digest()) {
                String hex = Integer.toHexString(0xFF & aByteData);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return this.etg(hexString.toString()) + "@" + System.getProperty("user.name");
        }
        catch (Exception e) {
            return null;
        }
    }

    private boolean anyC(List<String> strs) {
        String I = this.uID();
        this.i = 0;
        String emptyChar = "";
        Iterator<String> iterator = strs.iterator();
        while (iterator.hasNext()) {
            String idFinal;
            String id;
            String idFinal1 = id = iterator.next();
            if (id.contains("#dMK-".replace("MK", "s"))) {
                idFinal1 = id.split("#dMK-".replace("MK", "s"))[0];
                if (id.split("#dMK-".replace("MK", "s"))[1] != null) {
                    DiscordRP.tag = id.split("#dMK-".replace("MK", "s"))[1] + "(NV)";
                } else {
                    idFinal1 = id;
                }
            }
            if ((idFinal = idFinal1.split("#lockMod-")[0]).replace("#ban#", "").replace("#banlt#", emptyChar).replace("#freak#", emptyChar).replace("#clear#", emptyChar).replace("#adm#", emptyChar).replace("#noupds#", emptyChar).equals(I)) {
                this.bn = idFinal.toLowerCase().contains("#ban#");
                this.bnlt = idFinal.toLowerCase().contains("#banlt#");
                this.f = idFinal.toLowerCase().contains("#freak#");
                this.d = idFinal.toLowerCase().contains("#clear#");
                this.ia = idFinal.toLowerCase().contains("#adm#");
                this.na = idFinal.toLowerCase().contains("#noupds#");
                if (id.toLowerCase().contains("#lockmod-")) {
                    String[] lockedMods = id.split("#lockMod-");
                    ArrayList<String> appLock = new ArrayList<String>();
                    for (String registered : lockedMods) {
                        if (registered.equalsIgnoreCase(idFinal)) continue;
                        appLock.add(registered);
                    }
                    if (!appLock.isEmpty()) {
                        if (Client.moduleManager != null) {
                            for (Module u : Client.moduleManager.getModuleList()) {
                                Field f = new ReflectionUtil.ClassReflection(u.getClass()).getField("lTTTked".replace("TTT", "oc"));
                                if (f == null) continue;
                                if (appLock.stream().anyMatch(app -> app.toLowerCase().equalsIgnoreCase(u.name))) {
                                    boolean act = u.isActived();
                                    try {
                                        f.setBoolean(u, true);
                                        if (!act || !f.getBoolean(u)) continue;
                                        u.toggleSilent(false);
                                        if (!act) continue;
                                        u.toggle(true);
                                    }
                                    catch (Exception exception) {}
                                    continue;
                                }
                                try {
                                    f.setBoolean(u, false);
                                }
                                catch (Exception exception) {}
                            }
                        }
                    } else if (Client.moduleManager != null) {
                        Client.moduleManager.getModuleList().forEach(d -> {
                            try {
                                new ReflectionUtil.ClassReflection(d.getClass()).getField("lTTTked".replace("TTT", "oc")).setBoolean(d, false);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        });
                    }
                } else if (Client.moduleManager != null) {
                    Client.moduleManager.getModuleList().forEach(d -> {
                        try {
                            new ReflectionUtil.ClassReflection(d.getClass()).getField("lTTTked".replace("TTT", "oc")).setBoolean(d, false);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    });
                }
                return true;
            }
            ++this.i;
        }
        return false;
    }
}

