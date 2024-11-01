package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketEntityAction;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.Bypass;
import ru.govno.client.module.modules.Velocity;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Command.impl.Panic;

public class InvWalk
extends Module {
    public static InvWalk get;
    public BoolSettings AbilitySneak = new BoolSettings("AbilitySneak", false, this);
    public BoolSettings MouseMove;
    public BoolSettings FlagFix;

    public InvWalk() {
        super("InvWalk", 0, Module.Category.MOVEMENT);
        this.settings.add(this.AbilitySneak);
        this.MouseMove = new BoolSettings("MouseMove", false, this);
        this.settings.add(this.MouseMove);
        this.FlagFix = new BoolSettings("FlagFix", true, this);
        this.settings.add(this.FlagFix);
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
        return Keyboard.isKeyDown((int)keyNum);
    }

    private static void updateKeyStates(GameSettings gameSettings, boolean canSneak) {
        gameSettings.keyBindJump.pressed = InvWalk.keyIsDown(gameSettings.keyBindJump.getKeyCode());
        gameSettings.keyBindForward.pressed = InvWalk.keyIsDown(gameSettings.keyBindForward.getKeyCode());
        gameSettings.keyBindBack.pressed = InvWalk.keyIsDown(gameSettings.keyBindBack.getKeyCode());
        gameSettings.keyBindLeft.pressed = InvWalk.keyIsDown(gameSettings.keyBindLeft.getKeyCode());
        gameSettings.keyBindRight.pressed = InvWalk.keyIsDown(gameSettings.keyBindRight.getKeyCode());
        if (canSneak) {
            gameSettings.keyBindSneak.pressed = InvWalk.keyIsDown(gameSettings.keyBindSneak.getKeyCode());
        } else {
            if (Velocity.get.isActived() && Velocity.get.OnKnockBack.getBool() && !Velocity.pass && Velocity.get.KnockType.currentMode.equalsIgnoreCase("Sneaking") && Velocity.get.sneakTicks > 0) {
                return;
            }
            if (!(Minecraft.player == null || !Minecraft.player.isSneaking() || Minecraft.player.hasNewVersionMoves && Minecraft.player.newPhisicsFixes.updateLayOrShift((EntityLivingBase)Minecraft.player)[1])) {
                gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown((int)gameSettings.keyBindSneak.getKeyCode()) && Minecraft.getMinecraft().currentScreen == null;
            }
        }
    }

    private static boolean canUpdateKeys(Gui gui) {
        return !Panic.stop && gui != null && get != null && InvWalk.get.actived && !(gui instanceof GuiChat) && !(gui instanceof GuiEditSign) && !Bypass.isCancelInvWalk();
    }

    public static void inInitScreen(Gui gui) {
        if (InvWalk.canUpdateKeys(gui) && gui instanceof GuiContainer && !InvWalk.get.FlagFix.getBool()) {
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }
    }

    public static boolean keysHasUpdated(Gui gui, boolean silent) {
        if (InvWalk.canUpdateKeys(gui)) {
            GameSettings gameSettings = InvWalk.mc.gameSettings;
            if (!silent) {
                InvWalk.updateKeyStates(gameSettings, InvWalk.get.AbilitySneak.getBool());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onToggled(boolean actived) {
        if (InvWalk.mc.currentScreen != null && !actived) {
            KeyBinding.unPressAllKeys();
        }
        super.onToggled(actived);
    }

    @Override
    public void onUpdate() {
        GuiScreen gui = InvWalk.mc.currentScreen;
        if (InvWalk.keysHasUpdated(gui, true) && gui instanceof GuiContainer && this.MouseMove.getBool()) {
            mc.setIngameFocus();
            KeyBinding.updateKeyBindState();
            Mouse.setGrabbed((boolean)false);
        }
    }
}

