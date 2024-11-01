package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ElytraBoost;
import ru.govno.client.module.modules.Fly;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.Speed;
import ru.govno.client.module.modules.Timer;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class OnFalling
extends Module {
    BoolSettings SneakBack = new BoolSettings("SneakBack", true, this);
    BoolSettings FallBoost;
    BoolSettings NoDamage;
    ModeSettings BackMode;
    ModeSettings NoDmgMode;
    boolean fall = false;
    private double egX;
    private double egY;
    private double egZ;

    public OnFalling() {
        super("OnFalling", 0, Module.Category.PLAYER);
        this.settings.add(this.SneakBack);
        this.BackMode = new ModeSettings("BackMode", "Matrix", this, new String[]{"Matrix", "OldGround", "Vulcan"}, () -> this.SneakBack.getBool());
        this.settings.add(this.BackMode);
        this.FallBoost = new BoolSettings("FallBoost", true, this);
        this.settings.add(this.FallBoost);
        this.NoDamage = new BoolSettings("NoDamage", true, this);
        this.settings.add(this.NoDamage);
        this.NoDmgMode = new ModeSettings("NoDmgMode", "MatrixOld", this, new String[]{"MatrixOld", "MatrixNew", "NCP", "PreGround"}, () -> this.NoDamage.getBool());
        this.settings.add(this.NoDmgMode);
    }

    @EventTarget
    public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
        if (e.ground) {
            this.egX = Minecraft.player.posX;
            this.egY = Minecraft.player.posY;
            this.egZ = Minecraft.player.posZ;
        }
    }

    public static double getDistanceTofall() {
        for (int i = 0; i < 500; ++i) {
            if (!Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - (double)i, Minecraft.player.posZ)) continue;
            return i;
        }
        return 0.0;
    }

    @EventTarget
    public void onPacket(EventSendPacket event) {
        if (this.NoDamage.getBool() && this.fall && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixNew")) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            this.fall = false;
            packet.onGround = true;
            Minecraft.player.motionY = -0.0199f;
        }
        if (OnFalling.mc.timer.speed == 0.650000243527852 && Minecraft.player.ticksExisted % 2 != 0) {
            OnFalling.mc.timer.speed = 1.0;
        }
    }

    @Override
    public void onUpdate() {
        if (FreeCam.get.actived || Fly.get.actived || ElytraBoost.get.actived) {
            return;
        }
        if (OnFalling.mc.gameSettings.keyBindSneak.isKeyDown() && this.SneakBack.getBool() && Minecraft.player.fallDistance >= 3.3f) {
            if (this.BackMode.getMode().equalsIgnoreCase("OldGround")) {
                Minecraft.player.fallDistance = 0.0f;
                if (this.egX != 0.0 && this.egY != 0.0 && this.egZ != 0.0) {
                    Minecraft.player.setPosition(this.egX, this.egY, this.egZ);
                    this.egX = 0.0;
                    this.egY = 0.0;
                    this.egZ = 0.0;
                    Minecraft.player.motionX = 0.0;
                    Minecraft.player.motionZ = 0.0;
                } else {
                    Minecraft.player.setPosition(Minecraft.player.posX, Minecraft.player.posY + (double)Minecraft.player.height, Minecraft.player.posZ);
                    Minecraft.player.motionY = MoveMeHelp.getBaseJumpHeight();
                    Minecraft.player.motionY += 0.164157;
                }
            } else {
                boolean oldGravity = Minecraft.player.hasNoGravity();
                Minecraft.player.fallDistance = (float)((double)Minecraft.player.fallDistance - 0.2);
                Minecraft.player.onGround = true;
                Entity.motiony = Minecraft.player.motionY = (double)-0.01f;
                Timer.forceTimer(0.2f);
                Minecraft.player.setNoGravity(oldGravity);
            }
        }
        if (OnFalling.mc.gameSettings.keyBindSneak.isKeyDown() && this.SneakBack.getBool() && Minecraft.player.fallDistance > 4.0f && this.BackMode.getMode().equalsIgnoreCase("Vulcan")) {
            Minecraft.player.onGround = true;
            Entity.motiony = -Entity.Getmotiony;
            Minecraft.player.fallDistance = 0.0f;
        }
        if (Minecraft.player.posY > 0.0) {
            if (this.FallBoost.getBool() && OnFalling.getDistanceTofall() > 5.0) {
                if ((int)Minecraft.player.fallDistance >= 4 && Minecraft.player.fallDistance < 10.0f) {
                    Minecraft.player.connection.sendPacket(new CPacketPlayer(true));
                    Minecraft.player.fallDistance += 10.0f;
                }
                if (Minecraft.player.fallDistance > 5.0f && Minecraft.player.motionY < 0.0 && Minecraft.player.hurtTime != 0) {
                    Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
                    Minecraft.player.motionY = -10.0;
                }
            }
            if (this.NoDamage.getBool()) {
                float f = Minecraft.player.fallDistance;
                int n = Minecraft.player.getHealth() > 6.0f ? 3 : 2;
                if (f > (float)n && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixOld")) {
                    Minecraft.player.fallDistance = (float)(Math.random() * 1.0E-12);
                    Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, true));
                    Minecraft.player.jumpMovementFactor = 0.0f;
                }
                if (Minecraft.player.fallDistance > 5.0f && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixNew")) {
                    Minecraft.player.fallDistance = 0.0f;
                    OnFalling.mc.timer.speed = 0.650000243527852;
                    this.fall = true;
                }
                if (OnFalling.mc.timer.speed == 0.650000243527852 && this.NoDmgMode.getMode().equalsIgnoreCase("MatrixNew") && Minecraft.player.ticksExisted % 4 == 0) {
                    OnFalling.mc.timer.speed = 1.0;
                }
                if (Minecraft.player.fallDistance >= 3.0f && this.NoDmgMode.getMode().equalsIgnoreCase("NCP")) {
                    Minecraft.player.onGround = false;
                    Minecraft.player.motionY = 0.02f;
                    for (int i = 0; i < 30; ++i) {
                        Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 110000.0, Minecraft.player.posZ, false));
                        Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 2.0, Minecraft.player.posZ, false));
                    }
                    Minecraft.player.connection.sendPacket(new CPacketPlayer(true));
                    Minecraft.player.fallDistance = 0.0f;
                }
                if (Minecraft.player.fallDistance >= 3.0f && Minecraft.player.motionY < -0.4 && Minecraft.player.motionY > -1.0 && this.NoDmgMode.getMode().equalsIgnoreCase("PreGround") && OnFalling.mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox).isEmpty() && !OnFalling.mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offsetMinDown(0.999)).isEmpty()) {
                    Minecraft.player.fallDistance = 0.0f;
                    Minecraft.player.forceUpdatePlayerServerPosition(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Minecraft.player.rotationYaw, Minecraft.player.rotationPitch, true);
                }
            } else {
                if (OnFalling.mc.timer.speed == 0.650000243527852) {
                    OnFalling.mc.timer.speed = 1.0;
                }
                this.fall = false;
            }
        }
    }

    @Override
    public void onToggled(boolean actived) {
        if (!actived) {
            this.fall = false;
        }
        super.onToggled(actived);
    }
}

