package ru.govno.client.module.modules;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class TotemPopESP extends Module {
   List<TotemPopESP.Totempop> popList = new CopyOnWriteArrayList<>();
   private long time;
   private int color;
   FloatSettings TimeMs;
   static long staticTime;

   public TotemPopESP() {
      super("TotemPopESP", 0, Module.Category.RENDER);
      this.settings.add(this.TimeMs = new FloatSettings("TimeMs", 3000.0F, 6000.0F, 800.0F, this));
   }

   @EventTarget
   public void onPacket(EventReceivePacket e) {
      if (this.actived
         && e.getPacket() != null
         && e.getPacket() instanceof SPacketEntityStatus packet
         && packet != null
         && packet.getOpCode() == 35
         && packet.getEntity(mc.world) != null
         && packet.getEntity(mc.world) instanceof EntityPlayer
         && packet.getEntity(mc.world) != null) {
         EntityPlayer ent = (EntityPlayer)packet.getEntity(mc.world);
         if (ent != null && ent.isEntityAlive()) {
            this.popList
               .add(
                  new TotemPopESP.Totempop(
                     ent.posX,
                     ent.posY,
                     ent.posZ,
                     (double)ent.limbSwingAmount,
                     (double)ent.rotationYaw,
                     (double)ent.rotationPitch,
                     new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.randomUUID(), " " + (int)(Math.random() * 9.99999999E8)))
                  )
               );
         }
      }
   }

   @Override
   public void alwaysRender3DV2() {
      if (this.actived) {
         Vec3d fix = new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
         staticTime = System.currentTimeMillis();
         float maxMs = this.TimeMs.getFloat();
         if (this.popList.size() > 0) {
            for (int i = 0; i < this.popList.size(); i++) {
               if (this.popList.get(i) != null && (float)this.popList.get(i).getTime() / maxMs >= 1.0F) {
                  this.popList.remove(i);
               }
            }

            if (this.popList.size() != 0) {
               GL11.glPushMatrix();
               GL11.glEnable(3042);
               GL11.glDisable(3008);
               GL11.glBlendFunc(770, 1);
               GL11.glDisable(2929);
               GL11.glDisable(2896);
               GL11.glDisable(3553);
               GlStateManager.translate(-fix.xCoord, -fix.yCoord, -fix.zCoord);
            }

            for (TotemPopESP.Totempop pops : this.popList) {
               if (pops != null) {
                  float age = (float)pops.getTime() / maxMs;
                  if (pops.player != null) {
                     pops.limbSwingAmount += 0.01F;
                     pops.player.limbSwing = (float)pops.limbSwingAmount;
                     pops.player.limbSwingAmount = (float)pops.limbSwingAmount;
                     if ((double)age < 0.1) {
                        pops.player.rotationYaw = (float)pops.rotationYaw;
                        pops.player.rotationYawHead = (float)pops.rotationYaw;
                        pops.player.renderYawOffset = (float)pops.rotationYaw;
                        pops.player.rotationPitch = (float)pops.rotationPitch;
                        pops.player.rotationPitchHead = (float)pops.rotationPitch;
                     }

                     RenderUtils.glColor(
                        ColorUtils.swapAlpha(
                           ClientColors.getColor1(),
                           (float)ColorUtils.getAlphaFromColor(ClientColors.getColor1()) * MathUtils.clamp(1.0F - age, 0.0F, 1.0F) / 5.0F
                        )
                     );
                     if (pops.player != null) {
                        RenderLivingBase.silentMode = true;
                        mc.renderManager
                           .doRenderEntityNoShadow(pops.player, pops.x, pops.y + (double)(age * 4.0F), pops.z, pops.player.rotationYaw, 2.0F, true);
                        RenderLivingBase.silentMode = false;
                     }
                  }
               }
            }

            if (this.popList.size() != 0) {
               GL11.glEnable(2896);
               GL11.glEnable(3008);
               GlStateManager.translate(fix.xCoord, fix.yCoord, fix.zCoord);
               GL11.glEnable(3553);
               GlStateManager.resetColor();
               GL11.glEnable(2929);
               GL11.glBlendFunc(770, 771);
               GL11.glPopMatrix();
            }
         }
      }
   }

   public class Totempop {
      EntityOtherPlayerMP player;
      long time = System.currentTimeMillis();
      int color = ClientColors.getColor1();
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;
      double limbSwingAmount;
      double rotationYaw;
      double rotationPitch;

      public Totempop(double x, double y, double z, double limbSwingAmount, double rotationYaw, double rotationPitch, EntityOtherPlayerMP player) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.limbSwingAmount = limbSwingAmount;
         this.rotationYaw = rotationYaw;
         this.rotationPitch = rotationPitch;
         this.player = player;
      }

      int getColor() {
         return this.color;
      }

      long getTime() {
         return TotemPopESP.staticTime - this.time;
      }
   }
}
