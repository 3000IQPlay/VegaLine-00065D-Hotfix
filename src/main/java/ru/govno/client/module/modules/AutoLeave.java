package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketUseEntity;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class AutoLeave
extends Module {
    ModeSettings LeaveType;
    BoolSettings SethomePreLeave;
    BoolSettings OnlyStandAfk20s;
    BoolSettings PostDisable;
    FloatSettings LeaveOnDistance;
    FloatSettings LeaveOnHealth;
    TimerHelper timeAfk = TimerHelper.TimerHelperReseted();
    String ab = "";

    public AutoLeave() {
        super("AutoLeave", 0, Module.Category.PLAYER);
        this.LeaveType = new ModeSettings("LeaveType", "/spawn", this, new String[]{"/spawn", "/lobby", "/logout", "disconnect", "SelfKick"});
        this.settings.add(this.LeaveType);
        this.SethomePreLeave = new BoolSettings("SethomePreLeave", true, this);
        this.settings.add(this.SethomePreLeave);
        this.LeaveOnDistance = new FloatSettings("LeaveOnDistance", 40.0f, 120.0f, 10.0f, this);
        this.settings.add(this.LeaveOnDistance);
        this.LeaveOnHealth = new FloatSettings("LeaveOnHealth", 5.0f, 20.0f, 0.0f, this);
        this.settings.add(this.LeaveOnHealth);
        this.OnlyStandAfk20s = new BoolSettings("OnlyStandAfk20s", false, this);
        this.settings.add(this.OnlyStandAfk20s);
        this.PostDisable = new BoolSettings("PostDisable", true, this);
        this.settings.add(this.PostDisable);
    }

    private final EntityPlayer getMe() {
        return FreeCam.fakePlayer != null && FreeCam.get.actived ? FreeCam.fakePlayer : Minecraft.player;
    }

    private final boolean playersIsInRange(float range) {
        for (Entity e : AutoLeave.mc.world.getLoadedEntityList()) {
            EntityPlayer player = null;
            if (e != null && e instanceof EntityOtherPlayerMP) {
                player = (EntityPlayer)e;
            }
            if (player == null || Client.friendManager.isFriend(player.getName()) || player == FreeCam.fakePlayer || player.getEntityId() == 462462999 || Client.summit(player) || this.getMe() == null || !(this.getMe().getDistanceToEntity(player) < range)) continue;
            this.msg(player);
            return true;
        }
        return false;
    }

    private boolean leaveByHp() {
        return Minecraft.player.getHealth() <= this.LeaveOnHealth.getFloat() && (Minecraft.player.hurtTime != 0 || this.LeaveOnHealth.getFloat() >= 20.0f);
    }

    private final void msg(EntityPlayer e) {
        if (e == null) {
            Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lAutoLeave\u00a7r\u00a77]: \u0423 \u0432\u0430\u0441 \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c " + (int)Minecraft.player.getHealth() + "\u0425\u041f.", false);
            return;
        }
        Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lAutoLeave\u00a7r\u00a77]: \u0420\u044f\u0434\u043e\u043c \u0441 \u0432\u0430\u043c\u0438 \u043d\u0435\u0436\u0435\u043b\u0430\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u0438\u0433\u0440\u043e\u043a", false);
        Client.msg("\u00a77\u0415\u0433\u043e \u0438\u043c\u044f: " + e.getDisplayName().getFormattedText(), false);
        this.ab = e.getDisplayName().getFormattedText();
    }

    private final void doLeave(boolean isPlayer) {
        String type2 = this.LeaveType.currentMode;
        if (type2.equalsIgnoreCase("/spawn")) {
            Minecraft.player.connection.preSendPacket(new CPacketChatMessage("/spawn"));
        } else if (type2.equalsIgnoreCase("/lobby")) {
            Minecraft.player.connection.preSendPacket(new CPacketChatMessage("/hub"));
        } else if (type2.equalsIgnoreCase("/logout")) {
            Minecraft.player.connection.preSendPacket(new CPacketChatMessage("/logout"));
        } else if (type2.equalsIgnoreCase("disconnect")) {
            if (isPlayer) {
                AutoLeave.mc.world.sendQuittingDisconnectingPacket("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lAutoLeave\u00a7r\u00a77]: \u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d \u043d\u0435\u0436\u0435\u043b\u0430\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u0438\u0433\u0440\u043e\u043a \u0441 \u043d\u0438\u043a\u043e\u043c: " + this.ab);
            } else {
                AutoLeave.mc.world.sendQuittingDisconnectingPacket("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lAutoLeave\u00a7r\u00a77]: \u0423 \u0432\u0430\u0441 \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c " + (int)Minecraft.player.getHealth() + "\u0425\u041f.");
            }
        } else {
            for (int i = 0; i < 100; ++i) {
                mc.getConnection().sendPacket(new CPacketUseEntity(Minecraft.player));
            }
        }
        if (this.PostDisable.getBool()) {
            this.toggle(false);
        }
    }

    private final void doSethome() {
        Minecraft.player.sendChatMessage("/sethome home");
    }

    @Override
    public void onUpdate() {
        if (this.OnlyStandAfk20s.getBool() && (MoveMeHelp.getSpeed() > 0.0 || Minecraft.player.posY - Minecraft.player.lastTickPosY != 0.0 || Minecraft.player.rotationYaw - Minecraft.player.lastReportedYaw != 0.0f || Minecraft.player.rotationPitch - Minecraft.player.prevRotationPitch != 0.0f || Minecraft.player.isSwingInProgress)) {
            this.timeAfk.reset();
        }
        if (!this.OnlyStandAfk20s.getBool() || this.timeAfk.hasReached(20000.0)) {
            if (this.playersIsInRange(this.LeaveOnDistance.getFloat())) {
                if (this.SethomePreLeave.getBool()) {
                    this.doSethome();
                }
                this.doLeave(true);
                return;
            }
            if (this.leaveByHp()) {
                if (this.SethomePreLeave.getBool()) {
                    this.doSethome();
                }
                this.doLeave(false);
            }
        }
    }
}

