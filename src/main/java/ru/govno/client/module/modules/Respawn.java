package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.network.play.client.CPacketChatMessage;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.MusicHelper;

public class Respawn extends Module {
   public static Respawn get;
   public BoolSettings DeathCoords;
   public BoolSettings AutoSethome;
   public BoolSettings AutoHome;
   public BoolSettings AutoWand;
   public BoolSettings DeathPoint;
   public BoolSettings DeathSFX;
   boolean doAny;

   public Respawn() {
      super("Respawn", 0, Module.Category.PLAYER);
      this.settings.add(this.DeathCoords = new BoolSettings("DeathCoords", true, this));
      this.settings.add(this.AutoSethome = new BoolSettings("AutoSethome", false, this));
      this.settings.add(this.AutoHome = new BoolSettings("AutoHome", false, this));
      this.settings.add(this.AutoWand = new BoolSettings("AutoWand", false, this));
      this.settings.add(this.DeathPoint = new BoolSettings("DeathPoint", true, this));
      this.settings.add(this.DeathSFX = new BoolSettings("DeathSFX", true, this));
      get = this;
   }

   private void setDeathPoint(int[] xyz) {
      String death = "Death";
      PointTrace point = PointTrace.getPointByName(death);
      if (point != null) {
         PointTrace.points.remove(point);
      }

      PointTrace.points.add(new PointTrace(death, (double)xyz[0], (double)xyz[1], (double)xyz[2]));
   }

   @Override
   public void onMovement() {
      if (mc.currentScreen instanceof GuiGameOver && this.doAny) {
         int posx = (int)Minecraft.player.posX;
         int posy = (int)Minecraft.player.posY;
         int posz = (int)Minecraft.player.posZ;
         if (this.DeathCoords.getBool()) {
            Client.msg("§f§lModules:§r §7[§lRespawn§r§7]: координаты смерти: " + posx + "," + posy + "," + posz + ".", false);
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
