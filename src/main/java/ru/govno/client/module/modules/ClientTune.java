package ru.govno.client.module.modules;

import net.minecraft.client.gui.inventory.GuiContainer;
import ru.govno.client.clickgui.CheckBox;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.MacroMngr.Macros;

public class ClientTune extends Module {
   public static ClientTune get;
   public BoolSettings Modules;
   public BoolSettings Module;
   public BoolSettings ClickGui;
   public BoolSettings MiddleClick;
   public BoolSettings Containers;
   public BoolSettings Macroses;
   public ModeSettings Module1;
   public FloatSettings ModuleVolume;

   public ClientTune() {
      super("ClientTune", 0, Category.MISC, true);
      this.settings.add(this.Modules = new BoolSettings("Modules", true, this));
      this.settings
         .add(
            this.Module1 = new ModeSettings(
               "Module",
               "VL",
               this,
               new String[]{"VL", "Dev", "Discord", "Sigma", "Akrien", "Hanabi", "Tone", "Alarm", "Heavy", "Speech", "Frontiers", "Sweep", "Nurik"},
               () -> this.Modules.getBool()
            )
         );
      this.settings.add(this.ModuleVolume = new FloatSettings("ModuleVolume", 90.0F, 200.0F, 1.0F, this, () -> this.Modules.getBool()));
      this.settings.add(this.ClickGui = new BoolSettings("ClickGui", true, this));
      this.settings.add(this.MiddleClick = new BoolSettings("MiddleClick", false, this));
      this.settings.add(this.Containers = new BoolSettings("Containers", true, this));
      this.settings.add(this.Macroses = new BoolSettings("Macroses", true, this));
      get = this;
   }

   public void playSong(String song) {
      MusicHelper.playSound(song);
   }

   public void playSong(String song, float volume) {
      MusicHelper.playSound(song, volume);
   }

   private boolean canPlaySong(Class at) {
      boolean play = at == Module.class && this.Modules.getBool();
      if ((at == ClickGuiScreen.class || at == CheckBox.class || at == ClientColors.class) && this.ClickGui.getBool()
         || at == TargetHUD.class
         || at == MiddleClick.class && this.MiddleClick.getBool()
         || at == GuiContainer.class && this.Containers.getBool()
         || at == Macros.class && this.Macroses.getBool()) {
         play = true;
      }

      return this.actived && play;
   }

   private String moduleSong(boolean enable) {
      return (enable ? "enable" : "disable") + this.Module1.getMode().toLowerCase() + ".wav";
   }

   private String guiScreenSong(boolean open) {
      return open ? "guienabledev2.wav" : "guidisabledev2.wav";
   }

   private String guiScreenFoneticOpenSong() {
      return "guifoneticonopen.wav";
   }

   private String guicolorsScreenSong(boolean open) {
      return open ? "guicolorsopen.wav" : "guicolorsclose.wav";
   }

   private String guiScreenMusicSaveToggleSong(boolean enable) {
      return enable ? "guisavemusonenable.wav" : "guisavemusondisable.wav";
   }

   private String macrosUseSong() {
      return "usemacros.wav";
   }

   private String targetSelectSong() {
      return "targetselect.wav";
   }

   private String guiScreenScrollSong() {
      return "guiscrolldev.wav";
   }

   private String guiScreenModeChangeSong() {
      return "guichangemode.wav";
   }

   private String guiScreenCheckOpenOrCloseSong(boolean open) {
      return "guicheck" + (open ? "open" : "close") + ".wav";
   }

   private String guiScreenCheckBoxSong(boolean enable) {
      return "gui" + (enable ? "enable" : "disable") + "checkbox.wav";
   }

   private String getSliderMoveSong() {
      return "guislidermovedev.wav";
   }

   private String guiScreenModuleOpenOrCloseSong(boolean open) {
      return "guimodulepanel2" + (open ? "open" : "close") + ".wav";
   }

   private String guiScreenModuleBindSong(boolean nonNullBind) {
      return "guibindset" + (nonNullBind ? "released" : "nulled") + ".wav";
   }

   private String guiScreenModuleBindToggleSong(boolean enable) {
      return "guibinding" + (enable ? "enable" : "disable") + ".wav";
   }

   private String guiScreenModuleBindHoldStatusSong(boolean reset) {
      return "guibindhold" + (reset ? "reset" : "start") + ".wav";
   }

   private String guiScreenPanelOpenOrCloseSong(boolean open) {
      return "guipanel" + (open ? "open" : "close") + ".wav";
   }

   private String guiScreenModuleHovering() {
      return "guimodulehover.wav";
   }

   private String guiClientcolorModeChangeSong() {
      return "guiclientcolorchangemode.wav";
   }

   private String guiClientcolorPresetChangeSong() {
      return "guiclientcolorchangepreset.wav";
   }

   private String pressMiddleButtonSong() {
      return "middle_mouse_click.wav";
   }

   private String friendStatusUpdateSong(boolean addFriend) {
      return "friend" + (addFriend ? "add" : "remove") + ".wav";
   }

   private String guiContannerOpenOrCloseSong(boolean open) {
      return "guicontainer" + (open ? "open" : "close") + ".wav";
   }

   public void playUseMacros() {
      if (this.canPlaySong(Macros.class)) {
         this.playSong(this.macrosUseSong(), this.ModuleVolume.getFloat() / 600.0F);
      }
   }

   public void playModule(boolean enable) {
      if (this.canPlaySong(Module.class)) {
         this.playSong(this.moduleSong(enable), this.ModuleVolume.getFloat() / 200.0F);
      }
   }

   public void playGuiScreenOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenSong(open));
      }
   }

   public void playGuiScreenFoneticSong() {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenFoneticOpenSong());
      }
   }

   public void playGuiScreenScrollSong() {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenScrollSong());
      }
   }

   public void playGuiScreenCheckBox(boolean enable) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenCheckBoxSong(enable));
      }
   }

   public void playGuiScreenChangeModeSong() {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenModeChangeSong());
      }
   }

   public void playGuiCheckOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenCheckOpenOrCloseSong(open));
      }
   }

   public void playGuiSliderMoveSong() {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.getSliderMoveSong());
      }
   }

   public void playGuiModuleOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenModuleOpenOrCloseSong(open), 0.1F);
      }
   }

   public void playGuiPenelOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenPanelOpenOrCloseSong(open));
      }
   }

   public void playGuiModuleBindSong(boolean nonNullBind) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenModuleBindSong(nonNullBind));
      }
   }

   public void playGuiModuleBindingToggleSong(boolean enable) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenModuleBindToggleSong(enable));
      }
   }

   public void playGuiModuleBindingHoldStatusSong(boolean reset) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenModuleBindHoldStatusSong(reset), reset ? 0.1F : 0.25F);
      }
   }

   public void playGuiClientcolorsChangeModeSong() {
      if (this.canPlaySong(ClientColors.class)) {
         this.playSong(this.guiClientcolorModeChangeSong());
      }
   }

   public void playGuiClientcolorsChangePresetSong() {
      if (this.canPlaySong(ClientColors.class)) {
         this.playSong(this.guiClientcolorPresetChangeSong());
      }
   }

   public void playGuiColorsScreenOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(ClientColors.class)) {
         this.playSong(this.guicolorsScreenSong(open));
      }
   }

   public void playGuiScreenModuleHoveringSong() {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenModuleHovering(), 0.075F);
      }
   }

   public void playGuiScreenMusicSaveToggleSong(boolean enable) {
      if (this.canPlaySong(ClientColors.class)) {
         this.playSong(this.guiScreenMusicSaveToggleSong(enable), 0.2F);
      }
   }

   public void playTargetSelect() {
      if (this.canPlaySong(TargetHUD.class)) {
         this.playSong(this.targetSelectSong());
      }
   }

   public void playMiddleMouseSong() {
      if (this.canPlaySong(MiddleClick.class)) {
         this.playSong(this.pressMiddleButtonSong(), 0.05F);
      }
   }

   public void playFriendUpdateSong(boolean addFriend) {
      if (this.canPlaySong(MiddleClick.class)) {
         this.playSong(this.friendStatusUpdateSong(addFriend), 0.2F);
      }
   }

   public void playGuiContannerOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(GuiContainer.class)) {
         this.playSong(this.guiContannerOpenOrCloseSong(open), 0.2F);
      }
   }
}
