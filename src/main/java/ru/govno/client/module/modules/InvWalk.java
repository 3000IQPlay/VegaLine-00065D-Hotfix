package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketEntityAction;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Command.impl.Panic;

public class InvWalk extends Module {
   public static InvWalk get;
   public BoolSettings AbilitySneak;
   public BoolSettings MouseMove;
   public BoolSettings FlagFix;

   public InvWalk() {
      super("InvWalk", 0, Module.Category.MOVEMENT);
      this.settings.add(this.AbilitySneak = new BoolSettings("AbilitySneak", false, this));
      this.settings.add(this.MouseMove = new BoolSettings("MouseMove", false, this));
      this.settings.add(this.FlagFix = new BoolSettings("FlagFix", true, this));
      get = this;
   }

   private static List<Integer> keyPuts(GameSettings gs, boolean canSneak) {
      List<KeyBinding> list = Arrays.asList(gs.keyBindJump, gs.keyBindForward, gs.keyBindBack, gs.keyBindLeft, gs.keyBindRight);
      if (canSneak) {
         list.add(gs.keyBindSneak);
      }

      return list.stream().map(key -> key.getKeyCode()).toList();
   }

   private static boolean keyIsDown(int keyNum) {
      return Keyboard.isKeyDown(keyNum);
   }

   private static void updateKeyStates(GameSettings gameSettings, boolean canSneak) {
      gameSettings.keyBindJump.pressed = keyIsDown(gameSettings.keyBindJump.getKeyCode());
      gameSettings.keyBindForward.pressed = keyIsDown(gameSettings.keyBindForward.getKeyCode());
      gameSettings.keyBindBack.pressed = keyIsDown(gameSettings.keyBindBack.getKeyCode());
      gameSettings.keyBindLeft.pressed = keyIsDown(gameSettings.keyBindLeft.getKeyCode());
      gameSettings.keyBindRight.pressed = keyIsDown(gameSettings.keyBindRight.getKeyCode());
      if (canSneak) {
         gameSettings.keyBindSneak.pressed = keyIsDown(gameSettings.keyBindSneak.getKeyCode());
      } else {
         if (Velocity.get.isActived()
            && Velocity.get.OnKnockBack.getBool()
            && !Velocity.pass
            && Velocity.get.KnockType.currentMode.equalsIgnoreCase("Sneaking")
            && Velocity.get.sneakTicks > 0) {
            return;
         }

         if (Minecraft.player != null
            && Minecraft.player.isSneaking()
            && (!Minecraft.player.hasNewVersionMoves || !Minecraft.player.newPhisicsFixes.updateLayOrShift(Minecraft.player)[1])) {
            gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(gameSettings.keyBindSneak.getKeyCode()) && Minecraft.getMinecraft().currentScreen == null;
         }
      }
   }

   private static boolean canUpdateKeys(Gui gui) {
      return !Panic.stop
         && gui != null
         && get != null
         && get.actived
         && !(gui instanceof GuiChat)
         && !(gui instanceof GuiEditSign)
         && !Bypass.isCancelInvWalk();
   }

   public static void inInitScreen(Gui gui) {
      if (canUpdateKeys(gui) && gui instanceof GuiContainer && !get.FlagFix.getBool()) {
         Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING));
      }
   }

   public static boolean keysHasUpdated(Gui gui, boolean silent) {
      if (canUpdateKeys(gui)) {
         GameSettings gameSettings = mc.gameSettings;
         if (!silent) {
            updateKeyStates(gameSettings, get.AbilitySneak.getBool());
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (mc.currentScreen != null && !actived) {
         KeyBinding.unPressAllKeys();
      }

      super.onToggled(actived);
   }

   @Override
   public void onUpdate() {
      Gui gui = mc.currentScreen;
      if (keysHasUpdated(gui, true) && gui instanceof GuiContainer && this.MouseMove.getBool()) {
         mc.setIngameFocus();
         KeyBinding.updateKeyBindState();
         Mouse.setGrabbed(false);
      }
   }
}
