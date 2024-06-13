package ru.govno.client.module.modules;

import optifine.Config;
import ru.govno.client.Client;
import ru.govno.client.cfg.GuiConfig;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.clickgui.Panel;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Render.AnimationUtils;

public class ClickGui extends Module {
   public static ClickGui instance;
   public static AnimationUtils categoryColorFactor = new AnimationUtils(0.0F, 0.0F, 0.015F);
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
      instance = this;
      int x = 80;

      for (int i = 0; i < 5; i++) {
         this.settings.add(new FloatSettings("P" + i + "X", (float)x, 10000.0F, -10000.0F, this, () -> false));
         this.settings.add(new FloatSettings("P" + i + "Y", 20.0F, 10000.0F, -10000.0F, this, () -> false));
         x += 135;
      }

      this.settings.add(this.Images = new BoolSettings("Images", false, this));
      this.settings
         .add(
            this.Image = new ModeSettings(
               "Image",
               "Sage",
               this,
               new String[]{
                  "Nolik",
                  "AstolfoHot",
                  "Lake",
                  "Kiskis",
                  "PandaPo",
                  "Sage",
                  "SonicGenerations",
                  "SonicMovie",
                  "PlayStationSFW",
                  "PlayStationNSFW"
               },
               () -> this.Images.getBool()
            )
         );
      this.settings.add(this.Gradient = new BoolSettings("Gradient", true, this));
      this.settings.add(this.GradientAlpha = new FloatSettings("GradientAlpha", 100.0F, 255.0F, 0.0F, this, () -> this.Gradient.getBool()));
      this.settings.add(this.BlurBackground = new BoolSettings("BlurBackground", true, this));
      this.settings.add(this.BlurStrengh = new FloatSettings("BlurStrengh", 1.0F, 2.0F, 0.25F, this, () -> this.BlurBackground.getBool()));
      this.settings.add(this.Descriptions = new BoolSettings("Descriptions", true, this));
      this.settings.add(this.CustomCursor = new BoolSettings("CustomCursor", false, this));
      this.settings.add(this.Darkness = new BoolSettings("Darkness", false, this));
      this.settings.add(this.DarkOpacity = new FloatSettings("DarkOpacity", 170.0F, 255.0F, 0.0F, this, () -> this.Darkness.getBool()));
      this.settings.add(this.Particles = new BoolSettings("Particles", false, this));
      this.settings.add(this.Epilepsy = new BoolSettings("Epilepsy", false, this));
      BoolSettings set;
      this.settings.add(this.CategoryColor = set = new BoolSettings("CategoryColor", false, this));
      categoryColorFactor.to = set.getBool() ? 1.0F : 0.0F;
      categoryColorFactor.setAnim(set.getBool() ? 1.0F : 0.0F);
      this.settings.add(this.MusicInGui = new BoolSettings("MusicInGui", true, this));
      this.settings
         .add(
            this.Song = new ModeSettings(
               "Song",
               "Ost-SF-6",
               this,
               new String[]{
                  "Ost-SF-1",
                  "Ost-SF-2",
                  "Ost-SF-3",
                  "Ost-SF-4",
                  "Ost-SF-5",
                  "Ost-SF-6",
                  "Ost-SF-7",
                  "Ost-SF-8",
                  "Ost-SF-9",
                  "Ost-SF-10",
                  "Ost-SF-11",
                  "Ost-SF-12",
                  "Ost-SF-13",
                  "Ost-SF-14",
                  "Ost-SF-15",
                  "Ost-SF-16",
                  "Ost-SF-17"
               },
               () -> this.MusicInGui.getBool()
            )
         );
      this.settings.add(this.MusicVolume = new FloatSettings("MusicVolume", 50.0F, 200.0F, 1.0F, this, () -> this.MusicInGui.getBool()));
      this.settings.add(this.ScanLinesOverlay = new BoolSettings("ScanLinesOverlay", true, this));
      this.settings.add(this.ScreenBounds = new BoolSettings("ScreenBounds", true, this));
      this.settings.add(this.SaveMusic = new BoolSettings("SaveMusic", true, this, () -> false));
   }

   public static float[] getPositionPanel(Panel curPanel) {
      float X = 0.0F;
      float Y = 0.0F;
      int i = 0;

      for (Panel panel : Client.clickGuiScreen.panels) {
         if (panel == curPanel) {
            X = instance.currentFloatValue("P" + i + "X");
            Y = instance.currentFloatValue("P" + i + "Y");
         }

         i++;
      }

      return new float[]{X, Y};
   }

   public static void setPositionPanel(Panel curPanel, float x, float y) {
      int i = 0;

      for (Panel panel : Client.clickGuiScreen.panels) {
         if (panel == curPanel) {
            ((FloatSettings)instance.settings.get(Integer.valueOf(i))).setFloat(x);
            ((FloatSettings)instance.settings.get(Integer.valueOf(i + 1))).setFloat(y);
         }

         i += 2;
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (this.savedScale == -1 && mc.gameSettings.guiScale != -1) {
         this.savedScale = mc.gameSettings.guiScale;
      }

      boolean playMusic = (actived || this.SaveMusic.getBool()) && this.MusicInGui.getBool();
      Client.clickGuiMusic.setPlaying(playMusic);
      if (actived) {
         this.savedScale = mc.gameSettings.guiScale;
         mc.gameSettings.guiScale = 2;
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
            i++;
         }
      } else {
         if (mc.currentScreen == Client.clickGuiScreen) {
            ClickGuiScreen.colose = true;
            ClickGuiScreen.scale.to = 0.0F;
            ClickGuiScreen.globalAlpha.to = 0.0F;
            ClientTune.get.playGuiScreenOpenOrCloseSong(false);
         }

         mc.gameSettings.guiScale = this.savedScale;
      }

      super.onToggled(actived);
   }

   @Override
   public void alwaysUpdate() {
      boolean playMusic = this.MusicInGui.getBool();
      if (playMusic) {
         String track = this.Song.currentMode.replace("Ost-SF-", "clickguimusic");
         if (this.doForceMusicChange) {
            Client.clickGuiMusic.setTrackNameForce(track);
            this.doForceMusicChange = false;
         } else {
            Client.clickGuiMusic.setTrackName(track);
         }

         Client.clickGuiMusic.setTrackName(this.Song.currentMode.replace("Ost-SF-", "clickguimusic"));
         Client.clickGuiMusic.setMaxVolume(this.MusicVolume.getFloat() / 200.0F);
      }

      Client.clickGuiMusic
         .setPlaying(
            playMusic
               && (mc.currentScreen == Client.clickGuiScreen && !ClickGuiScreen.colose || mc.currentScreen instanceof GuiConfig || this.SaveMusic.getBool())
         );
   }

   @Override
   public void onUpdate() {
      if (mc.currentScreen != Client.clickGuiScreen && !(mc.currentScreen instanceof GuiConfig)) {
         this.toggleSilent(false);
      } else {
         boolean categoryFactored = this.CategoryColor.getBool();
         if (categoryColorFactor.to == 1.0F != categoryFactored) {
            categoryColorFactor.to = categoryFactored ? 1.0F : 0.0F;
         }

         if (Config.isShaders() && this.BlurBackground.getBool()) {
            this.BlurBackground.setBool(false);
            ClientTune.get.playGuiScreenCheckBox(false);
            Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Turn off shaders to use BlurBackground.", false);
         }
      }
   }
}
