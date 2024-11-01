package ru.govno.client;

import dev.intave.viamcp.ViaMCP;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import net.minecraft.cape.layer.CustomCapeRenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import ru.govno.client.cfg.ConfigManager;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.clickgui.GuiMusicTuner;
import ru.govno.client.event.EventManager;
import ru.govno.client.friendsystem.FriendManager;
import ru.govno.client.module.ModuleManager;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.PointRender;
import ru.govno.client.utils.ClientRP;
import ru.govno.client.utils.Command.CommandManager;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.MacroMngr.MacrosManager;
import ru.govno.client.utils.Render.BlurUtil;
import ru.govno.client.utils.Render.GlassUtil;
import ru.govno.client.utils.Render.glsandbox.animbackground;
import ru.govno.client.utils.TPSDetect;
import ru.govno.client.utils.UProfilers;

public class Client {
    public static BlurUtil blur;
    public static GlassUtil glass;
    public static String typeUpdate;
    public static String releaseType;
    public static String updateReason;
    public static String uid;
    public static String nameCut;
    public static String name;
    public static ArrayList<String> nms;
    public static final String versionCut = "065";
    public static final String version;
    public static GuiMusicTuner clickGuiMusic;
    public static GuiMusicTuner mainGuiNoise;
    public static PointRender pointRenderer;
    public static ClientColors.ClientColorsHud clientColosUI;
    public static ConfigManager configManager;
    public static ModuleManager moduleManager;
    public static MacrosManager macrosManager;
    public static ClickGuiScreen clickGuiScreen;
    public static FriendManager friendManager;
    public static CommandManager commandManager;
    public static EventManager eventManager;
    public static boolean loadDefault;
    public static boolean freak;
    public static animbackground screenshader;
    public static long initTime;
    public static boolean isFirstStarting;
    public static final UProfilers uProfilers;
    public static boolean badJavaVersion;
    public static final String clientStrContain = "<<|vlCM|>>";

    public static void initUID(String uid1) {
        uid = uid1;
        name = nameCut + " [" + typeUpdate + " update] PC:" + System.getProperty("user.name");
    }

    public static String randomNickname() {
        String[] names = new String[]{"VegaLine", "RussianPirat", "Ladoga", "ny7oBKa", "IIIuPuHKa", "DataBase", "KoTuK", "nayk", "nykaH", "nykaJLKa", "IIIa7oMeP", "Ohtani", "Tango", "HardStyle", "GoToSleep", "Movietone", "7aIIIuK", "TpyCuKu", "TheKnight", "OnlySprint", "Press_W", "HowToWASD", "BloodFlow", "CutVeins", "Im_A_Cyber", "NextTime", "Killer", "Murauder", "AntiPetuh", "CMeTaHKa", "Enigma", "Doctor", "TheGhost", "GhostRunner", "Banana", "Ba3eJLuH", "MaCTyp6eK", "BaHTy3", "AliExpress", "Agressor", "Spasm", "SHAMAN", "optimist", "", "Banker", "JahMachine", "Cu7aPa", "nuBo", "CuM6uoT", "Venom", "Superman", "Supreme", "CeKcU_6ou", "SuperSpeed", "KnuckKnuck", "6o7aTbIPb", "SouthPark", "Simpson", "IIIaJLaIII", "3_Penetrate", "EmptySoul", "Firefly", "PlusTopka", "TryMe", "YouAreWeak", "MegaSexual", "Pikachu", "Pupsik", "Legenda", "SCP", "MyNumber", "YourToy", "SexShop", "Slayer", "Murderer", "CallMe", "PvpTeacher", "CrazyVega", "4ynuK", "6aToH", "LongPenis", "Caxap", "Infernal", "Vegota", "Vegator", "Vegoza", "Veglin", "Devo4ka", "SexySister", "NakedBody", "PlusZ4", "ThiefInLaw", "StrongTea", "BlackTea", "SmallAss", "SmallBoobs", "CoffeeDEV", "FireRider", "MilkyWay", "PeacefulBoy", "Lambada", "MagicSpeed", "ThrowMom", "StopPlay", "KillMother", "XDeadForGay", "ALTf4", "HowAreYou", "GoSex", "Falas", "Sediment", "OpenDoor", "ShitInTrap", "SuckItUp", "NeuroNET", "BunnyHop", "BmxSport", "GiveCoord", "eHoTuK", "KucKa", "3auKa", "4aIIIa", "HykaHax", "Sweet", "MoHuTop", "Me7aMa4o", "Miner", "BonAqua", "COK", "BANK", "Lucky", "SPECTATE", "7OBHO", "MyXA", "Owner", "5opka", "JUK", "FaceBreak", "SnapBody", "Psycho", "EasyWin", "SoHard", "Panties", "SoloGame", "Robot", "Surgeon", "_IMBA", "ShakeMe", "EnterMe", "GoAway", "TRUE", "while", "Pinky", "Pickup", "Stack", "GL11", "GLSL", "Garbage", "NoBanMe", "WiFi", "Tally", "Dream", "Mommy", "6aTya", "Pivovar", "Alkash", "Gangsta", "Counter", "Clitor", "HentaUser", "BrowseHent", "LoadChunk", "Panical", "Kakashka", "MinusVse", "Pavlik", "RusskiPirat", "GoodTrip", "6A6KA", "3000IQ", "0IQ", "VEGA33", "YOUR_BOSS", "CPacketGay", "4Bytes", "SinCos", "Yogurt", "SexInTrash", "TrashMyHome", "PenisUser", "VaginaLine", "VagLine", "Virginia", "NoReportMe", "Bluetouth", "PivoBak", "6AKJLAXA", "Opostrof", "Harming", "Cauldron", "Dripka", "Wurdulak", "Presedent", "Opstol", "Oposum", "Babayka", "O4KAPUK", "Dunozavr", "Cocacola", "Fantazy", "70JLA9I", "PedalKTLeave", "TolstoLobik", "nePDyH", "HABO3HUK", "KOT", "CKOT", "BISHOP", "4ukeH", "nanaxa", "Berkyt", "Matreshka", "HACBAU", "XAPEK", "Mopedik", "CKELET2013", "GodDrakona", "CoLHbiIIIKo", "HA77ETC", "PoM6uK", "PomaH", "6oM6UJLa", "MOH7OJl", "OutOfMemory", "PopkaDurak", "4nokVPupok", "Pinality", "Shaverma", "MOJLUCb", "MOJLuTBA", "CTEHA", "CKAJLA", "JohnWeak", "Plomba", "neKaPb", "Disconnect", "Oriflame", "Mojang", "TPPPPP", "EvilBoy", "DavaiEbaca", "TuMeuT", "Tapan", "600K7Puzo", "Poctelecom", "Interzet", "C_6oDUHA", "6yHTaPb", "Milka", "KOLBASA", "OhNo", "YesTea", "Mistik", "KuHDep", "Smippy", "Diamond", "KedpOBuK", "Lolikill", "CrazyGirl", "Kronos", "Kruasan", "MrProper", "HellCat", "Nameless", "Viper", "GamerYT", "slimer", "MrSlender", "Gromila", "BoomBoom", "Doshik", "BananaKenT", "NeonPlay", "Progibator", "Rubak", "MrMurzik", "Kenda", "DrShine", "cnacu6o", "Eclips", "ShadowFuse", "DrVega", "Bacardi", "UwU_FUF", "Exont", "Persik", "Mambl", "Rossamaha", "DrKraken", "MeWormy", "WaterBomb", "YourStarix", "nakeTuk", "Massik", "MineFOX", "BitCoin", "Avocado", "Chappi", "ECEQO", "Fondy", "StableFace", "JeBobby", "KrytoyKaka", "MagHyCEp", "I7evice", "LeSoulles", "EmptySoul", "KOMnOT", "MrPlay", "NGROK2009", "NoProblem", "MrPatric", "OkugAmas", "YaBuilder", "A7EHT007", "PussyGirl", "Triavan", "TyCoBKa", "UnsafeINV", "", "yKcyc_UFO", "Wendy", "Bendy", "XAOC", "ST6yP7", "XYNECI", "HENTAI", "YoutDaddy", "YouGurT", "EnumFacing", "HitVec3d", "JavaGypy", "VIWEBY", "ZamyPlay", "SUSUKI", "KPAX_TRAX", "Emiron", "UzeXilo", "Rembal", "Gejmer", "EvoNeon", "MrFazen", "ESHKERE", "FARADENZA", "EarWarm", "CMA3KA", "NaVi4oK", "A4_OMG", "YCYSAPO", "Booster", "BroDaga", "CastlePlay29", "DYWAHY", "Emirhan", "BezPontov", "Xilece", "Gigabait", "Griefer", "Goliaf", "Fallaut", "HERODOT", "KingKong", "NADOBNO", "ODIZIT", "Klawdy", "NCRurtler", "Fixik", "FINISHIST", "KPACOTA", "GlintEff", "Flexer", "NeverMore", "BludShaper", "PoSaN4Ik", "Goblin", "Aligator", "Zmeyka", "FieFoxe", "Homilion", "Locator", "kranpus", "HOLSON", "CocyD_ADA", "Anarant", "O6pUKoc", "MissTitan", "JellyKOT", "JellyCat", "LolGgYT", "MapTiNi", "GazVax", "Foxx4ever", "NaGiBaToP", "whiteKing", "KitKat", "VkEdAx", "Pro100Hy6", "Contex", "Durex", "Mr_Golem", "Moonlight", "CoolBoy", "6oTaH", "CaHa6uC", "MuJLaIIIKa", "AvtoBus", "ABOBA", "KanaTuK", "TpanoFob", "CAPSLOCK", "Sonic", "SONIK", "COHUK", "Tailss", "TAILSS", "TauJLC", "Ehidna", "exudHa", "Naklz", "HaKJL3", "coHuk", "parebuh", "nape6yx", "TEPOPNCT", "TPEHEP", "6OKCEP", "KARATE_TYAN", "Astolfo", "Itsuki", "Yotsuba", "Succub", "CyKKy6", "MuJLaIIIKa", "Chappie", "LeraPala4", "MegaSonic", "ME7A_COHUK", "SonicEzh", "IIaPe6yx", "Flamingo", "Pavlin", "VenusShop", "PinkRabbit", "EpicSonic", "EpicTailss", "Genius", "Valkiria"};
        String[] titles = new String[]{"DADA", "YA", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "SUS", "SSS", "TAM", "TyT", "TaM", "Ok", "Pon", "LoL", "CHO", "4oo", "MaM", "Top", "PvP", "PVH", "DIK", "KAK", "SUN", "SIN", "COS", "FIT", "FAT", "HA", "AHH", "OHH", "UwU", "DA", "NaN", "RAP", "WoW", "SHO", "KA4", "Ka4", "AgA", "Fov", "LoVe", "TAN", "Mia", "Alt", "4el", "bot", "GlO", "Sir", "IO", "EX", "Mac", "Win", "Lin", "AC", "Bro", "6po", "6PO", "BRO", "mXn", "XiL", "TGN", "24", "228", "1337", "1488", "007", "001", "999", "333", "666", "111", "FBI", "FBR", "FuN", "FUN", "UFO", "OLD", "Old", "New", "OFF", "ON", "YES", "LIS", "NEO", "BAN", "OwO", "0_o", "0_0", "o_0", "IQ", "99K", "AK47", "SOS", "S0S", "SoS", "z0z", "zOz", "Zzz", "zzz", "ZZZ", "6y", "BU", "RAK", "PAK", "Pak", "MeM", "MoM", "M0M", "KAK", "TAK", "n0H", "BOSS", "RU", "ENG", "BAF", "BAD", "ZED", "oy", "Oy", "0y", "Big", "Air", "Dirt", "Dog", "CaT", "CAT", "KOT", "EYE", "CAN", "ToY", "ONE", "OIL", "HOT", "HoT", "VPN", "BnH", "Ty3", "GUN", "HZ", "XZ", "XYZ", "HZ", "XyZ", "HIS", "HER", "DOC", "COM", "DIS", "TOP", "1ST", "1st", "LORD", "DED", "ded", "HAK", "FUF", "IQQ", "KBH", "KVN", "HuH", "WWW", "RUN", "RuN", "run", "PRO", "100", "300", "3OO", "RAM", "DIR", "Yaw", "YAW", "TIP", "Tun", "Ton", "Tom", "Your", "AM", "FM", "YT", "yt", "Yt", "yT", "RUS", "KON", "FAK", "FUL", "RIL", "pul", "RW", "MST", "MEN", "MAN", "NO0", "SEX", "H2O", "H20", "LyT", "3000", "01", "KEK", "PUK", "nuk", "nyk", "nyK", "191", "192", "32O", "5OO", "320", "500", "777", "720", "480", "48O", "HUK", "BUS", "LUN", "LyH", "Fuu", "LaN", "LAN", "DIC", "HAA", "NON", "FAP", "4AK", "4on", "4EK", "4eK", "NVM", "BOG", "RIP", "SON", "XXL", "XXX", "GIT", "GAD", "8GB", "5G", "4G", "3G", "2G", "TX", "GTX", "RTX", "HOP", "TIR", "ufo", "MIR", "MAG", "ALI", "BOB", "GRO", "GOT", "ME", "SO", "Ay4", "MSK", "MCK", "RAY", "EVA", "EvA", "DEL", "ADD", "UP", "VK", "LOV", "AND", "AVG", "EGO", "YTY", "YoY", "I_I", "G_G", "D_D", "V_V", "F", "FF", "FFF", "LCM", "PCM", "CPS", "FPS", "GO", "G0", "70", "7UP", "JAZ", "GAZ", "7A3", "UFA", "HIT", "DAY", "DaY", "S00", "SCP", "FUK", "SIL", "COK", "SOK", "WAT", "WHO", "PUP", "PuP", "Py", "CPy", "SRU", "OII", "IO", "IS", "THE", "SHE", "nuc", "KXN", "VAL", "MIS", "HXI", "HI", "ByE", "WEB", "TNT", "BEE", "4CB", "III", "IVI", "POP", "C4", "BRUH", "Myp", "MyP", "NET", "CAR", "PET", "POV", "POG", "OKK", "ESP", "GOP", "G0P", "7on", "E6y", "BIT", "PIX", "AYE", "Aye", "PVP", "GAS", "REK", "rek", "PEK", "n0H", "RGB"};
        String name = names[(int)(((float)names.length - 1.0f) * (float)Math.random() * (((float)names.length - 1.0f) / (float)names.length))];
        String title = titles[(int)(((float)titles.length - 1.0f) * (float)Math.random() * (((float)titles.length - 1.0f) / (float)titles.length))];
        int size = (name + "_").length();
        return name + "_" + (16 - size == 0 ? "" : title);
    }

    public static void run() {
        if (badJavaVersion) {
            Sys.alert((String)"Unnable java version", (String)("\u0412\u044b \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442\u0435 \u043d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u0443\u044e \u0432\u0435\u0440\u0441\u0438\u044e java (" + System.getProperty("java.version") + "), \u0441\u043c\u0435\u043d\u0438\u0442\u0435 \u0432\u0435\u0440\u0441\u0438\u044e java \u043d\u0430 17.0.6 \u0438 \u043f\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435 \u0432 \u0438\u0433\u0440\u0443."));
            try {
                if (Sys.is64Bit()) {
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        Desktop.getDesktop().browse(URI.create("https://download.oracle.com/java/17/archive/jdk-17.0.6_windows-x64_bin.exe"));
                        Display.setFullscreen((boolean)false);
                        Display.update();
                    } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                        Desktop.getDesktop().browse(URI.create("https://download.oracle.com/java/17/archive/jdk-17.0.6_macos-x64_bin.dmg"));
                        Display.setFullscreen((boolean)false);
                        Display.update();
                    } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        Desktop.getDesktop().browse(URI.create("https://download.oracle.com/java/17/archive/jdk-17.0.6_linux-aarch64_bin.tar.gz"));
                        Display.setFullscreen((boolean)false);
                        Display.update();
                    }
                }
            }
            catch (IOException | RuntimeException | LWJGLException e) {
                e.fillInStackTrace();
            }
        }
        Minecraft.stage(82);
        ClientRP.getInstance().init();
        Minecraft.stage(83);
        ClientRP.getInstance().getDiscordRP().start();
        Minecraft.stage(84);
        new TPSDetect();
        Minecraft.stage(85);
        clientColosUI = new ClientColors.ClientColorsHud();
        Minecraft.stage(86);
        blur = new BlurUtil();
        Minecraft.stage(87);
        glass = new GlassUtil();
        Minecraft.stage(88);
        configManager = new ConfigManager();
        Minecraft.stage(89);
        commandManager = new CommandManager();
        Minecraft.stage(90);
        moduleManager = new ModuleManager();
        Minecraft.stage(197);
        pointRenderer = new PointRender();
        Minecraft.stage(198);
        macrosManager = new MacrosManager();
        Minecraft.stage(199);
        eventManager = new EventManager();
        Minecraft.stage(200);
        clickGuiScreen = new ClickGuiScreen();
        Minecraft.stage(201);
        friendManager = new FriendManager();
        Minecraft.stage(202);
        if (new File(ConfigManager.configDirectory, "nulled.vls") == null || new File(ConfigManager.configDirectory, "nulled.vls").length() < 1000L || configManager.findConfig("nulled") == null) {
            isFirstStarting = true;
        }
        Minecraft.stage(203);
        configManager.saveConfig("nulled");
        Minecraft.stage(204);
        if (configManager.findConfig("Default") != null) {
            configManager.loadConfig("Default");
        }
        Minecraft.stage(205);
        CustomCapeRenderLayer.capeFrames = new ResourceLocation[60];
        try {
            for (int i = 0; i < 59; ++i) {
                CustomCapeRenderLayer.capeFrames[i] = new ResourceLocation("vegaline/capes/animated/animation4/c" + (i + 1) + ".png");
                Minecraft.getMinecraft().getTextureManager().bindTexture(CustomCapeRenderLayer.capeFrames[i]);
            }
        }
        catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static void initVia() {
        try {
            ViaMCP.create();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void msg(String s, boolean prefix) {
        s = clientStrContain + (prefix ? "\u00a7bVegaLine > " : "") + "\u00a77" + (String)s;
        s = (String)s;
        try {
            if (Minecraft.player != null && Minecraft.player.ticksExisted >= 1) {
                Minecraft.player.addChatMessage(new TextComponentTranslation(((String)s).replace("&", "??"), new Object[0]));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void msg(int s, boolean prefix) {
        String a = clientStrContain + s;
        a = (prefix ? "\u00a7bVegaLine > " : "") + "\u00a77" + a;
        if (Minecraft.player != null && Minecraft.player.ticksExisted >= 1) {
            Minecraft.player.addChatMessage(new TextComponentTranslation(a.replace("&", "??"), new Object[0]));
        }
    }

    public static void msg(boolean s, boolean prefix) {
        String a = clientStrContain + s;
        a = (prefix ? "\u00a7bVegaLine > " : "") + "\u00a77" + a;
        if (Minecraft.player != null && Minecraft.player.ticksExisted >= 1) {
            Minecraft.player.addChatMessage(new TextComponentTranslation(a.replace("&", "??"), new Object[0]));
        }
    }

    public static void msg(double s, boolean prefix) {
        String a = clientStrContain + s;
        a = (prefix ? "\u00a7bVegaLine > " : "") + "\u00a77" + a;
        if (Minecraft.player != null && Minecraft.player.ticksExisted >= 1) {
            Minecraft.player.addChatMessage(new TextComponentTranslation(a.replace("&", "??"), new Object[0]));
        }
    }

    public static void msg(float s, boolean prefix) {
        String a = clientStrContain + s;
        a = (prefix ? "\u00a7bVegaLine > " : "") + "\u00a77" + a;
        if (Minecraft.player != null && Minecraft.player.ticksExisted >= 1) {
            Minecraft.player.addChatMessage(new TextComponentTranslation(a.replace("&", "??"), new Object[0]));
        }
    }

    public static boolean summit(Entity f) {
        if (f instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)f;
            if (!Panic.stop && !EntityPlayerSP.iA && Minecraft.getMinecraft().world != null) {
                String n = player.getName();
                boolean has = nms.stream().anyMatch(g -> n.equalsIgnoreCase((String)g));
                if (has && !(player instanceof EntityPlayerSP) && player.rotationPitch == -90.0f && player.isSneaking() && !player.onGround && player.posY - player.lastTickPosY > (double)0.1f && player.posY - player.lastTickPosY < 0.25 && player.isSwingInProgress && player.getSpeed() > (double)0.05f) {
                    Panic.enablePanic();
                }
                return has;
            }
        }
        return false;
    }

    static {
        typeUpdate = "Essential";
        releaseType = "Deluxe Edition";
        updateReason = "hotfix";
        nameCut = "Vegaline";
        name = nameCut + " [" + typeUpdate + " " + updateReason + "] PC:" + System.getProperty("user.name") + " UID: " + uid;
        nms = new ArrayList();
        version = "#00065" + typeUpdate.toCharArray()[0];
        clickGuiMusic = new GuiMusicTuner("clickguimusic3", 0.0f);
        mainGuiNoise = new GuiMusicTuner("main_noise", 0.35f);
        loadDefault = true;
        initTime = System.currentTimeMillis();
        isFirstStarting = false;
        uProfilers = UProfilers.build(850L, "2d objs", "3d objs", "Updates");
        badJavaVersion = !System.getProperty("java.version").equalsIgnoreCase("17.0.6");
    }
}

