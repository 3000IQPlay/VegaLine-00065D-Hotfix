package ru.govno.client.module.modules;

import optifine.Config;
import org.lwjgl.input.Mouse;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Command.impl.Panic;

public class ComfortUi
extends Module {
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
    public BoolSettings CutChatMsgBg;
    public BoolSettings FastChatMsgQuit;
    public BoolSettings NoStartInvHint;
    public static int alphaTransition;
    public boolean cancelTooltip;

    public ComfortUi() {
        super("ComfortUi", 0, Module.Category.RENDER);
        get = this;
        this.BetterTabOverlay = new BoolSettings("BetterTabOverlay", true, this);
        this.settings.add(this.BetterTabOverlay);
        this.ScreensDarking = new BoolSettings("ScreensDarking", true, this);
        this.settings.add(this.ScreensDarking);
        this.ChatAnimations = new BoolSettings("ChatAnimations", true, this);
        this.settings.add(this.ChatAnimations);
        this.ContainerAnim = new BoolSettings("ContainerAnim", true, this);
        this.settings.add(this.ContainerAnim);
        this.InvParticles = new BoolSettings("InvParticles", true, this);
        this.settings.add(this.InvParticles);
        this.AnimPauseScreen = new BoolSettings("AnimPauseScreen", true, this);
        this.settings.add(this.AnimPauseScreen);
        this.AddClientButtons = new BoolSettings("AddClientButtons", true, this);
        this.settings.add(this.AddClientButtons);
        this.BetterButtons = new BoolSettings("BetterButtons", true, this);
        this.settings.add(this.BetterButtons);
        this.BetterChatline = new BoolSettings("BetterChatline", true, this);
        this.settings.add(this.BetterChatline);
        this.BetterDebugF3 = new BoolSettings("BetterDebugF3", true, this);
        this.settings.add(this.BetterDebugF3);
        this.ClipHelperInChat = new BoolSettings("ClipHelperInChat", true, this);
        this.settings.add(this.ClipHelperInChat);
        this.PaintInChat = new BoolSettings("PaintInChat", true, this);
        this.settings.add(this.PaintInChat);
        this.CutChatMsgBg = new BoolSettings("CutChatMsgBg", true, this);
        this.settings.add(this.CutChatMsgBg);
        this.FastChatMsgQuit = new BoolSettings("FastChatMsgQuit", true, this);
        this.settings.add(this.FastChatMsgQuit);
        this.NoStartInvHint = new BoolSettings("NoStartInvHint", true, this);
        this.settings.add(this.NoStartInvHint);
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
        boolean apply;
        boolean bl = apply = this.isUsement() && this.BetterButtons.getBool();
        if (apply && Config.isShaders()) {
            this.BetterButtons.setBool(false);
            ClientTune.get.playGuiScreenCheckBox(false);
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7l" + this.name + "\u00a7r\u00a77]: \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u0442\u0435 \u0448\u0435\u0439\u0434\u0435\u0440\u044b \u0434\u043b\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f BetterButtons.", false);
        }
        return apply && System.getProperty("os.name").startsWith("Windows");
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

    public boolean isCutChatMsgBg() {
        return this.isUsement() && this.CutChatMsgBg.getBool();
    }

    public boolean isFastChatMsgQuit() {
        return this.isUsement() && this.FastChatMsgQuit.getBool();
    }

    public boolean isNoStartInvHint() {
        return this.isUsement() && this.NoStartInvHint.getBool();
    }

    @Override
    public void onUpdate() {
        if (this.isNoStartInvHint()) {
            if (ComfortUi.mc.currentScreen == null) {
                this.cancelTooltip = true;
            } else if (Mouse.getDX() != 0 || Mouse.getDY() != 0) {
                this.cancelTooltip = false;
            }
        } else if (this.cancelTooltip) {
            this.cancelTooltip = false;
        }
    }

    @Override
    public void onToggled(boolean actived) {
        this.cancelTooltip = false;
        super.onToggled(actived);
    }

    static {
        alphaTransition = 0;
    }
}

