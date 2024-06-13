package ru.govno.client.module.modules;

import optifine.Config;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Command.impl.Panic;

public class ComfortUi extends Module {
   public static ComfortUi get;
   public BoolSettings BetterTabOverlay;
   public BoolSettings ScreensDarking;
   public BoolSettings ChatAnimations;
   public BoolSettings ContainerAnim;
   public BoolSettings InvParticles;
   public BoolSettings AnimPauseScreen;
   public BoolSettings AddClientButtons;
   public BoolSettings BetterButtons;
   public BoolSettings BetterChatline;
   public BoolSettings BetterDebugF3;
   public BoolSettings ClipHelperInChat;
   public BoolSettings PaintInChat;
   public static int alphaTransition = 0;

   public ComfortUi() {
      super("ComfortUi", 0, Module.Category.RENDER);
      get = this;
      this.settings.add(this.BetterTabOverlay = new BoolSettings("BetterTabOverlay", true, this));
      this.settings.add(this.ScreensDarking = new BoolSettings("ScreensDarking", true, this));
      this.settings.add(this.ChatAnimations = new BoolSettings("ChatAnimations", true, this));
      this.settings.add(this.ContainerAnim = new BoolSettings("ContainerAnim", true, this));
      this.settings.add(this.InvParticles = new BoolSettings("InvParticles", true, this));
      this.settings.add(this.AnimPauseScreen = new BoolSettings("AnimPauseScreen", true, this));
      this.settings.add(this.AddClientButtons = new BoolSettings("AddClientButtons", true, this));
      this.settings.add(this.BetterButtons = new BoolSettings("BetterButtons", true, this));
      this.settings.add(this.BetterChatline = new BoolSettings("BetterChatline", true, this));
      this.settings.add(this.BetterDebugF3 = new BoolSettings("BetterDebugF3", true, this));
      this.settings.add(this.ClipHelperInChat = new BoolSettings("ClipHelperInChat", true, this));
      this.settings.add(this.PaintInChat = new BoolSettings("PaintInChat", true, this));
   }

   public boolean isUsement() {
      return get != null && this.actived && !Panic.stop;
   }

   public boolean isBetterTabOverlay() {
      return this.isUsement() && this.BetterTabOverlay.getBool();
   }

   public boolean isScreensDarking() {
      return this.isUsement() && this.ScreensDarking.getBool();
   }

   public boolean isChatAnimations() {
      return this.isUsement() && this.ChatAnimations.getBool();
   }

   public boolean isContainerAnim() {
      return this.isUsement() && this.ContainerAnim.getBool();
   }

   public boolean isAnimPauseScreen() {
      return this.isUsement() && this.AnimPauseScreen.getBool();
   }

   public boolean isAddClientButtons() {
      return this.isUsement() && this.AddClientButtons.getBool();
   }

   public boolean isBetterButtons() {
      boolean apply = this.isUsement() && this.BetterButtons.getBool();
      if (apply && Config.isShaders()) {
         this.BetterButtons.setBool(false);
         ClientTune.get.playGuiScreenCheckBox(false);
         Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: выключите шейдеры для использования BetterButtons.", false);
      }

      return apply;
   }

   public boolean isBetterChatline() {
      return this.isUsement() && this.BetterChatline.getBool();
   }

   public boolean isBetterDebugF3() {
      return this.isUsement() && this.BetterDebugF3.getBool();
   }

   public boolean isClipHelperInChat() {
      return this.isUsement() && this.ClipHelperInChat.getBool();
   }

   public boolean isPaintInChat() {
      return this.isUsement() && this.PaintInChat.getBool();
   }

   public boolean isInvParticles() {
      return this.isUsement() && this.InvParticles.getBool();
   }
}
