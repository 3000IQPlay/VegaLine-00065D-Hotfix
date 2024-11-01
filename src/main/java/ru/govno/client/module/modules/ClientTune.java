package ru.govno.client.module.modules;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import ru.govno.client.clickgui.CheckBox;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.MiddleClick;
import ru.govno.client.module.modules.TargetHUD;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.MacroMngr.Macros;
import ru.govno.client.utils.MusicHelper;

public class ClientTune
        extends Module {
   public static ClientTune get;
   public BoolSettings Modules = new BoolSettings("Modules", true, this);
   public BoolSettings Module;
   public BoolSettings ClickGui;
   public BoolSettings MiddleClick;
   public BoolSettings Containers;
   public BoolSettings Macroses;
   public BoolSettings RTSoundSurround;
   public BoolSettings RTPerfomanceMode;
   public BoolSettings RTXDebugView;
   public BoolSettings HurtSounds;
   public ModeSettings Module1;
   public ModeSettings HurtSound;
   public FloatSettings ModuleVolume;

   public ClientTune() {
      super("ClientTune", 0, Category.MISC, true);
      this.settings.add(this.Modules);
      this.Module1 = new ModeSettings("Module", "Ori", this, new String[]{"VL", "Dev", "Discord", "Sigma", "Akrien", "Hanabi", "Tone", "Alarm", "Heavy", "Speech", "SpeechEcho", "Frontiers", "Sweep", "Nurik", "Basic", "Baloon", "BaloonFast", "Ori", "Ori2", "Airpods"}, () -> this.Modules.getBool());
      this.settings.add(this.Module1);
      this.ModuleVolume = new FloatSettings("ModuleVolume", 120.0f, 200.0f, 10.0f, this, () -> this.Modules.getBool());
      this.settings.add(this.ModuleVolume);
      this.ClickGui = new BoolSettings("ClickGui", true, this);
      this.settings.add(this.ClickGui);
      this.MiddleClick = new BoolSettings("MiddleClick", true, this);
      this.settings.add(this.MiddleClick);
      this.Containers = new BoolSettings("Containers", false, this);
      this.settings.add(this.Containers);
      this.Macroses = new BoolSettings("Macroses", true, this);
      this.settings.add(this.Macroses);
      this.RTSoundSurround = new BoolSettings("RTSoundSurround", false, this);
      this.settings.add(this.RTSoundSurround);
      this.RTPerfomanceMode = new BoolSettings("RTRTPerfomanceMode", false, this, () -> this.RTSoundSurround.getBool());
      this.settings.add(this.RTPerfomanceMode);
      this.RTXDebugView = new BoolSettings("RTXDebugView", false, this, () -> this.RTSoundSurround.getBool());
      this.settings.add(this.RTXDebugView);
      this.HurtSounds = new BoolSettings("HurtSounds", false, this);
      this.settings.add(this.HurtSounds);
      this.HurtSound = new ModeSettings("HurtSound", "Blaze", this, new String[]{"Cow", "Chicken", "Blaze", "Wolf", "Pig", "Skeleton", "Zombie"}, () -> this.HurtSounds.getBool());
      this.settings.add(this.HurtSound);
      get = this;
   }

   @EventTarget
   public void onReceivePackets(EventReceivePacket event) {
      SPacketSoundEffect soundEffect;
      Packet packet;
      if (this.actived && (packet = event.getPacket()) instanceof SPacketSoundEffect && (soundEffect = (SPacketSoundEffect)packet).getSound() != null && this.HurtSounds.getBool() && soundEffect.getCategory() == SoundCategory.PLAYERS && soundEffect.getSound().getSoundName().getResourceDomain().contains("entity.player.hurt")) {
         soundEffect.sound = this.reEventSound(soundEffect.getSound());
      }
   }

   public SoundEvent reEventSound(SoundEvent prevEventSound) {
      if (this.HurtSounds.getBool() && this.isActived()) {
         switch (this.HurtSound.getMode()) {
            case "Cow": {
               prevEventSound = SoundEvents.ENTITY_COW_HURT;
               break;
            }
            case "Chicken": {
               prevEventSound = SoundEvents.ENTITY_CHICKEN_HURT;
               break;
            }
            case "Blaze": {
               prevEventSound = SoundEvents.ENTITY_BLAZE_HURT;
               break;
            }
            case "Wolf": {
               prevEventSound = SoundEvents.ENTITY_WOLF_HURT;
               break;
            }
            case "Pig": {
               prevEventSound = SoundEvents.ENTITY_PIG_HURT;
               break;
            }
            case "Skeleton": {
               prevEventSound = SoundEvents.ENTITY_SKELETON_HURT;
               break;
            }
            case "Zombie": {
               prevEventSound = SoundEvents.ENTITY_ZOMBIE_HURT;
            }
         }
      }
      return prevEventSound;
   }

   @Override
   public boolean isBetaModule() {
      return true;
   }

   public boolean getRTSoundSurround() {
      return get.isActived() && !Panic.stop && this.RTSoundSurround.getBool();
   }

   public boolean getRTPerfomanceMode() {
      return get.isActived() && !Panic.stop && this.RTPerfomanceMode.getBool();
   }

   public boolean getIsRTXDebugView() {
      return get.isActived() && !Panic.stop && this.RTXDebugView.getBool();
   }

   public void playSong(String song) {
      MusicHelper.playSound(song);
   }

   public void playSong(String song, float volume) {
      MusicHelper.playSound(song, volume);
   }

   private boolean canPlaySong(Class at) {
      boolean play;
      boolean bl = play = at == Module.class && this.Modules.getBool();
      if ((at == ClickGuiScreen.class || at == CheckBox.class || at == ClientColors.class) && this.ClickGui.getBool() || at == TargetHUD.class || at == MiddleClick.class && this.MiddleClick.getBool() || at == GuiContainer.class && this.Containers.getBool() || at == Macros.class && this.Macroses.getBool()) {
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
         this.playSong(this.macrosUseSong(), this.ModuleVolume.getFloat() / 600.0f);
      }
   }

   public void playModule(boolean enable) {
      if (this.canPlaySong(Module.class)) {
         this.playSong(this.moduleSong(enable), this.ModuleVolume.getFloat() / 200.0f);
      }
   }

   public void playGuiScreenOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenSong(open), open ? 0.3f : 0.35f);
      }
   }

   public void playGuiScreenFoneticSong() {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenFoneticOpenSong());
      }
   }

   public void playGuiScreenScrollSong() {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenScrollSong());
   }

   public void playGuiScreenCheckBox(boolean enable) {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenCheckBoxSong(enable));
      }
   }

   public void playGuiScreenChangeModeSong() {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenModeChangeSong());
   }

   public void playGuiCheckOpenOrCloseSong(boolean open) {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenCheckOpenOrCloseSong(open));
   }

   public void playGuiSliderMoveSong() {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.getSliderMoveSong());
   }

   public void playGuiModuleOpenOrCloseSong(boolean open) {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenModuleOpenOrCloseSong(open), 0.2f);
   }

   public void playGuiPenelOpenOrCloseSong(boolean open) {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenPanelOpenOrCloseSong(open));
   }

   public void playGuiModuleBindSong(boolean nonNullBind) {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenModuleBindSong(nonNullBind));
   }

   public void playGuiModuleBindingToggleSong(boolean enable) {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenModuleBindToggleSong(enable));
   }

   public void playGuiModuleBindingHoldStatusSong(boolean reset) {
      if (!this.canPlaySong(ClickGuiScreen.class)) {
         return;
      }
      this.playSong(this.guiScreenModuleBindHoldStatusSong(reset), reset ? 0.1f : 0.25f);
   }

   public void playGuiClientcolorsChangeModeSong() {
      if (!this.canPlaySong(ClientColors.class)) {
         return;
      }
      this.playSong(this.guiClientcolorModeChangeSong());
   }

   public void playGuiClientcolorsChangePresetSong() {
      if (!this.canPlaySong(ClientColors.class)) {
         return;
      }
      this.playSong(this.guiClientcolorPresetChangeSong());
   }

   public void playGuiColorsScreenOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(ClientColors.class)) {
         this.playSong(this.guicolorsScreenSong(open));
      }
   }

   public void playGuiScreenModuleHoveringSong() {
      if (this.canPlaySong(ClickGuiScreen.class)) {
         this.playSong(this.guiScreenModuleHovering(), 0.05f);
      }
   }

   public void playGuiScreenMusicSaveToggleSong(boolean enable) {
      if (this.canPlaySong(ClientColors.class)) {
         this.playSong(this.guiScreenMusicSaveToggleSong(enable), 0.2f);
      }
   }

   public void playTargetSelect() {
      if (this.canPlaySong(TargetHUD.class)) {
         this.playSong(this.targetSelectSong());
      }
   }

   public void playMiddleMouseSong() {
      if (this.canPlaySong(MiddleClick.class)) {
         this.playSong(this.pressMiddleButtonSong(), 0.05f);
      }
   }

   public void playFriendUpdateSong(boolean addFriend) {
      if (this.canPlaySong(MiddleClick.class)) {
         this.playSong(this.friendStatusUpdateSong(addFriend), 0.2f);
      }
   }

   public void playGuiContannerOpenOrCloseSong(boolean open) {
      if (this.canPlaySong(GuiContainer.class)) {
         this.playSong(this.guiContannerOpenOrCloseSong(open), 0.2f);
      }
   }
}
