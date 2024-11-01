package ru.govno.client.module.modules;

import optifine.Config;
import ru.govno.client.Client;
import ru.govno.client.cfg.GuiConfig;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.clickgui.Panel;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Render.AnimationUtils;

public class ClickGui
extends Module {
    public static ClickGui instance;
    public static AnimationUtils categoryColorFactor;
    public BoolSettings Images;
    public BoolSettings Gradient;
    public BoolSettings BlurBackground;
    public BoolSettings Descriptions;
    public BoolSettings CustomCursor;
    public BoolSettings Darkness;
    public BoolSettings Particles;
    public BoolSettings Epilepsy;
    public BoolSettings CategoryColor;
    public BoolSettings MusicInGui;
    public BoolSettings ScanLinesOverlay;
    public BoolSettings ScreenBounds;
    public BoolSettings SaveMusic;
    public FloatSettings GradientAlpha;
    public FloatSettings BlurStrengh;
    public FloatSettings DarkOpacity;
    public FloatSettings MusicVolume;
    public ModeSettings Image;
    public ModeSettings Song;
    boolean doForceMusicChange = true;
    int savedScale = -1;

    public ClickGui() {
        super("ClickGui", 29, Module.Category.RENDER);
        BoolSettings set;
        instance = this;
        int x = 80;
        for (int i = 0; i < 5; ++i) {
            this.settings.add(new FloatSettings("P" + i + "X", x, 10000.0f, -10000.0f, this, () -> false));
            this.settings.add(new FloatSettings("P" + i + "Y", 20.0f, 10000.0f, -10000.0f, this, () -> false));
            x += 135;
        }
        this.Images = new BoolSettings("Images", false, this);
        this.settings.add(this.Images);
        this.Image = new ModeSettings("Image", "Sage", this, new String[]{"Nolik", "Succubbus", "SuccubbusHot", "AstolfoHot", "Furry", "Lake", "Kiskis", "IceGirl", "LoliGirl", "LoliGirl2", "PandaPo", "Sage", "SonicGenerations", "SonicMovie", "PlayStationSFW", "PlayStationNSFW"}, () -> this.Images.getBool());
        this.settings.add(this.Image);
        this.Gradient = new BoolSettings("Gradient", false, this);
        this.settings.add(this.Gradient);
        this.GradientAlpha = new FloatSettings("GradientAlpha", 70.0f, 255.0f, 0.0f, this, () -> this.Gradient.getBool());
        this.settings.add(this.GradientAlpha);
        this.BlurBackground = new BoolSettings("BlurBackground", true, this, () -> System.getProperty("os.name").startsWith("Windows"));
        this.settings.add(this.BlurBackground);
        this.BlurStrengh = new FloatSettings("BlurStrengh", 1.6f, 2.0f, 0.25f, this, () -> this.BlurBackground.getBool() && System.getProperty("os.name").startsWith("Windows"));
        this.settings.add(this.BlurStrengh);
        this.Descriptions = new BoolSettings("Descriptions", true, this);
        this.settings.add(this.Descriptions);
        this.CustomCursor = new BoolSettings("CustomCursor", false, this);
        this.settings.add(this.CustomCursor);
        this.Darkness = new BoolSettings("Darkness", false, this);
        this.settings.add(this.Darkness);
        this.DarkOpacity = new FloatSettings("DarkOpacity", 170.0f, 255.0f, 0.0f, this, () -> this.Darkness.getBool());
        this.settings.add(this.DarkOpacity);
        this.Particles = new BoolSettings("Particles", false, this);
        this.settings.add(this.Particles);
        this.Epilepsy = new BoolSettings("Epilepsy", false, this);
        this.settings.add(this.Epilepsy);
        this.CategoryColor = set = new BoolSettings("CategoryColor", false, this);
        this.settings.add(set);
        ClickGui.categoryColorFactor.to = set.getBool() ? 1.0f : 0.0f;
        categoryColorFactor.setAnim(set.getBool() ? 1.0f : 0.0f);
        this.MusicInGui = new BoolSettings("MusicInGui", true, this);
        this.settings.add(this.MusicInGui);
        this.Song = new ModeSettings("Song", "Ost-RA-3", this, new String[]{"Ost-RA-1", "Ost-RA-2", "Ost-RA-3", "Ost-RA-4"}, () -> this.MusicInGui.getBool());
        this.settings.add(this.Song);
        this.MusicVolume = new FloatSettings("MusicVolume", 70.0f, 200.0f, 5.0f, this, () -> this.MusicInGui.getBool());
        this.settings.add(this.MusicVolume);
        this.ScanLinesOverlay = new BoolSettings("ScanLinesOverlay", false, this, () -> System.getProperty("os.name").startsWith("Windows"));
        this.settings.add(this.ScanLinesOverlay);
        this.ScreenBounds = new BoolSettings("ScreenBounds", false, this);
        this.settings.add(this.ScreenBounds);
        this.SaveMusic = new BoolSettings("SaveMusic", false, this, () -> false);
        this.settings.add(this.SaveMusic);
    }

    public static float[] getPositionPanel(Panel curPanel) {
        float X = 0.0f;
        float Y = 0.0f;
        int i = 0;
        for (Panel panel : Client.clickGuiScreen.panels) {
            if (panel == curPanel) {
                X = instance.currentFloatValue("P" + i + "X");
                Y = instance.currentFloatValue("P" + i + "Y");
            }
            ++i;
        }
        return new float[]{X, Y};
    }

    public static void setPositionPanel(Panel curPanel, float x, float y) {
        int i = 0;
        for (Panel panel : Client.clickGuiScreen.panels) {
            if (panel == curPanel) {
                ((FloatSettings)ClickGui.instance.settings.get(i)).setFloat(x);
                ((FloatSettings)ClickGui.instance.settings.get(i + 1)).setFloat(y);
            }
            i += 2;
        }
    }

    @Override
    public void onToggled(boolean actived) {
        if (this.savedScale == -1 && ClickGui.mc.gameSettings.guiScale != -1) {
            this.savedScale = ClickGui.mc.gameSettings.guiScale;
        }
        boolean playMusic = (actived || this.SaveMusic.getBool()) && this.MusicInGui.getBool();
        Client.clickGuiMusic.setPlaying(playMusic);
        if (actived) {
            this.savedScale = ClickGui.mc.gameSettings.guiScale;
            ClickGui.mc.gameSettings.guiScale = 2;
            if (Client.clickGuiScreen != null) {
                mc.displayGuiScreen(Client.clickGuiScreen);
            }
            int i = 0;
            for (Panel panel : Client.clickGuiScreen.panels) {
                panel.X = instance.currentFloatValue("P" + i + "X");
                panel.Y = instance.currentFloatValue("P" + i + "Y");
                panel.posX.to = panel.X;
                panel.posY.to = panel.Y;
                panel.posX.setAnim(panel.posX.to);
                panel.posY.setAnim(panel.posY.to);
                ++i;
            }
        } else {
            if (ClickGui.mc.currentScreen == Client.clickGuiScreen) {
                ClickGuiScreen.colose = true;
                ClickGuiScreen.scale.to = 0.0f;
                ClickGuiScreen.globalAlpha.to = 0.0f;
                ClientTune.get.playGuiScreenOpenOrCloseSong(false);
            }
            ClickGui.mc.gameSettings.guiScale = this.savedScale;
        }
        super.onToggled(actived);
    }

    @Override
    public void alwaysUpdate() {
        boolean playMusic = this.MusicInGui.getBool();
        if (playMusic) {
            String track = this.Song.currentMode.replace("Ost-RA-", "foneticmusic");
            if (this.doForceMusicChange) {
                Client.clickGuiMusic.setTrackNameForce(track);
                this.doForceMusicChange = false;
            } else {
                Client.clickGuiMusic.setTrackName(track);
            }
            Client.clickGuiMusic.setTrackName(this.Song.currentMode.replace("Ost-RA-", "foneticmusic"));
            Client.clickGuiMusic.setMaxVolume(this.MusicVolume.getFloat() / 200.0f);
        }
        Client.clickGuiMusic.setPlaying(playMusic && (ClickGui.mc.currentScreen == Client.clickGuiScreen && !ClickGuiScreen.colose || ClickGui.mc.currentScreen instanceof GuiConfig || this.SaveMusic.getBool()));
    }

    @Override
    public void onUpdate() {
        if (ClickGui.mc.currentScreen != Client.clickGuiScreen && !(ClickGui.mc.currentScreen instanceof GuiConfig)) {
            this.toggleSilent(false);
        } else {
            boolean categoryFactored;
            if (ClickGui.categoryColorFactor.to == 1.0f != (categoryFactored = this.CategoryColor.getBool())) {
                float f = ClickGui.categoryColorFactor.to = categoryFactored ? 1.0f : 0.0f;
            }
            if (Config.isShaders() && this.BlurBackground.getBool()) {
                this.BlurBackground.setBool(false);
                ClientTune.get.playGuiScreenCheckBox(false);
                Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7l" + this.name + "\u00a7r\u00a77]: \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u0435 \u0448\u0435\u0439\u0434\u0435\u0440\u044b \u0434\u043b\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f BlurBackground.", false);
            }
        }
    }

    static {
        categoryColorFactor = new AnimationUtils(0.0f, 0.0f, 0.015f);
    }
}

