package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.network.play.client.CPacketChatMessage;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.PointTrace;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.MusicHelper;

public class Respawn
extends Module {
    public static Respawn get;
    public BoolSettings DeathCoords = new BoolSettings("DeathCoords", true, this);
    public BoolSettings AutoSethome;
    public BoolSettings AutoHome;
    public BoolSettings AutoWand;
    public BoolSettings DeathPoint;
    public BoolSettings DeathSFX;
    boolean doAny;

    public Respawn() {
        super("Respawn", 0, Module.Category.PLAYER);
        this.settings.add(this.DeathCoords);
        this.AutoSethome = new BoolSettings("AutoSethome", false, this);
        this.settings.add(this.AutoSethome);
        this.AutoHome = new BoolSettings("AutoHome", false, this);
        this.settings.add(this.AutoHome);
        this.AutoWand = new BoolSettings("AutoWand", false, this);
        this.settings.add(this.AutoWand);
        this.DeathPoint = new BoolSettings("DeathPoint", true, this);
        this.settings.add(this.DeathPoint);
        this.DeathSFX = new BoolSettings("DeathSFX", true, this);
        this.settings.add(this.DeathSFX);
        get = this;
    }

    private void setDeathPoint(int[] xyz) {
        String death = "Death";
        PointTrace point = PointTrace.getPointByName(death);
        if (point != null) {
            PointTrace.points.remove(point);
        }
        PointTrace.points.add(new PointTrace(death, xyz[0], xyz[1], xyz[2]));
    }

    @Override
    public void onMovement() {
        if (Respawn.mc.currentScreen instanceof GuiGameOver && this.doAny) {
            int posx = (int)Minecraft.player.posX;
            int posy = (int)Minecraft.player.posY;
            int posz = (int)Minecraft.player.posZ;
            if (this.DeathCoords.getBool()) {
                Client.msg("\u00a7f\u00a7lModules:\u00a7r \u00a77[\u00a7lRespawn\u00a7r\u00a77]: \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0441\u043c\u0435\u0440\u0442\u0438: " + posx + "," + posy + "," + posz + ".", false);
            }
            if (this.DeathPoint.getBool()) {
                this.setDeathPoint(new int[]{posx, posy, posz});
            }
            if (this.AutoSethome.getBool()) {
                Minecraft.player.connection.sendPacket(new CPacketChatMessage("/sethome home"));
            }
            Minecraft.player.respawnPlayer();
            if (this.AutoHome.getBool()) {
                Minecraft.player.connection.sendPacket(new CPacketChatMessage("/home home"));
            }
            if (this.AutoWand.getBool()) {
                Minecraft.player.connection.sendPacket(new CPacketChatMessage("//wand"));
            }
            if (this.DeathSFX.getBool()) {
                MusicHelper.playSound("ohno.wav");
            }
            Minecraft.player.closeScreen();
            this.doAny = false;
        } else {
            this.doAny = true;
        }
    }
}

