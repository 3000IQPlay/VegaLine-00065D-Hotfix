package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Command.impl.Clip;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class AWP extends Module {
   public static AWP get;
   private final AnimationUtils usingProgress = new AnimationUtils(0.0F, 0.0F, 0.1F);
   private float indicatorScale = 0.0F;
   private float radiusPlus = 0.0F;
   BoolSettings MatrixElytra;
   BoolSettings Massage;
   FloatSettings Packets;
   private final TimerHelper wait = new TimerHelper();

   public AWP() {
      super("AWP", 0, Module.Category.COMBAT);
      this.settings.add(this.MatrixElytra = new BoolSettings("MatrixElytra", false, this));
      this.settings.add(this.Packets = new FloatSettings("Packets", 45.0F, 100.0F, 10.0F, this, () -> !this.MatrixElytra.getBool()));
      this.settings.add(this.Massage = new BoolSettings("Massage", true, this));
   }

   private float getCurrentLongUseDamage(float packetsCount) {
      return 2.24F + packetsCount * 0.092159994F;
   }

   @EventTarget
   public void onSend(EventSendPacket event) {
      if (event.getPacket() instanceof CPacketPlayerDigging packet) {
         if (packet != null && packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && this.correctUseMod() && this.wait.hasReached(100.0)) {
            this.damageMultiply(this.hasTautString(this.getTautPercent()), this.Packets.getInt(), this.Massage.getBool());
            this.wait.reset();
         }
      }
   }

   private boolean correctUseMod() {
      return Minecraft.player.isBowing() && this.actived;
   }

   private void drawIndicator(float scaling, ScaledResolution sr) {
      this.usingProgress.to = this.getTautPercent();
      float progress = MathUtils.clamp(this.usingProgress.getAnim(), 0.05F, 1.0F);
      float plusRad = this.radiusPlus;
      float width = 100.0F - 50.0F * plusRad;
      float height = 4.0F;
      float extendY = 30.0F;
      float x = (float)(sr.getScaledWidth() / 2) - width / 2.0F;
      float x2 = (float)(sr.getScaledWidth() / 2) + width / 2.0F;
      float x3 = (float)(sr.getScaledWidth() / 2) - width / 2.0F + width * progress;
      float y = (float)(sr.getScaledHeight() / 2) + 30.0F;
      float y2 = y + 4.0F;
      float alphed = scaling * scaling;
      int colorShadow = ColorUtils.getColor(5, (int)(plusRad * 255.0F), 14, (int)((90.0F + plusRad * 45.0F) * alphed));
      int colorLeft = ColorUtils.getOverallColorFrom(
         ColorUtils.getColor(255, 110, 70, (int)(140.0F * alphed)), ColorUtils.swapAlpha(colorShadow, alphed * 80.0F), plusRad
      );
      int colorRight = ColorUtils.getOverallColorFrom(
         ColorUtils.getColor(140, 255, 255, (int)(120.0F * alphed)), ColorUtils.swapAlpha(colorShadow, alphed * 95.0F), plusRad
      );
      GlStateManager.pushMatrix();
      RenderUtils.customScaledObject2D(x, y, width, 4.0F, scaling);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x3, y2, 2.0F, 0.0F, colorLeft, colorRight, colorRight, colorLeft, false, true, false
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x2, y2, 2.0F, 2.0F + plusRad * 3.25F, colorShadow, colorShadow, colorShadow, colorShadow, false, false, true
      );
      GlStateManager.popMatrix();
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      if (this.correctUseMod() && this.indicatorScale != 1.0F || !this.correctUseMod() && this.indicatorScale != 0.0F) {
         this.indicatorScale = MathUtils.harp(this.indicatorScale, this.correctUseMod() ? 1.0F : 0.0F, (float)Minecraft.frameTime * 0.005F);
      }

      if (this.indicatorScale != 0.0F) {
         this.radiusPlus = MathUtils.harp(
            this.radiusPlus,
            this.hasTautString(this.getTautPercent()) && (double)this.usingProgress.getAnim() > 0.995 ? 1.0F : 0.0F,
            (float)Minecraft.frameTime * 0.01F
         );
         this.drawIndicator(this.indicatorScale, sr);
      }
   }

   private float getTautPercent() {
      return (float)Minecraft.player.getItemInUseMaxCount() / this.getCurrentLongUseDamage(this.MatrixElytra.getBool() ? 26.0F : this.Packets.getFloat())
            >= 1.0F
         ? 1.0F
         : (float)Minecraft.player.getItemInUseMaxCount() / this.getCurrentLongUseDamage(this.MatrixElytra.getBool() ? 26.0F : this.Packets.getFloat());
   }

   private boolean hasTautString(float used) {
      return used == 1.0F;
   }

   private void damageMultiply(boolean successfully, int packetsCount, boolean sendFakeMassage) {
      if (!successfully) {
         if (sendFakeMassage) {
            Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Can't increase bow damage.", false);
         }
      } else {
         float yaw = BowAimbot.get.getTarget() != null ? BowAimbot.getVirt()[0] : Minecraft.player.rotationYaw;
         float pitch = BowAimbot.get.getTarget() != null ? BowAimbot.getVirt()[1] : Minecraft.player.rotationPitch;
         Clip.goClip(0.0, 0.0, false);
         if (this.MatrixElytra.getBool()) {
            if (ElytraBoost.canElytra()) {
               ElytraBoost.equipElytra();
               ElytraBoost.badPacket();
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING));
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));
               ElytraBoost.badPacket();
               ElytraBoost.dequipElytra();
               Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));

               for (int packet = 0; packet < 26; packet++) {
                  Minecraft.player
                     .connection
                     .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 0.2, Minecraft.player.posZ, false));
                  Minecraft.player
                     .connection
                     .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, false));
               }

               Minecraft.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, 4.2F, false));
            } else {
               Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: You don't have elytra in your inventory.", false);
            }
         } else {
            Minecraft.player.connection.preSendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));

            for (int packet = 0; packet < packetsCount / 2; packet++) {
               Minecraft.player
                  .connection
                  .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 0.002F, Minecraft.player.posZ, false));
               Minecraft.player
                  .connection
                  .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY - 0.002F, Minecraft.player.posZ, true));
            }

            Minecraft.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, (float)MathUtils.clamp((double)pitch * 1.0001, -89.9, 89.9), false));
         }

         if (sendFakeMassage) {
            Client.msg("§f§lModules:§r §7[§l" + this.name + "§r§7]: Increase bow damage.", false);
         }

         this.usingProgress.setAnim(0.0F);
      }
   }
}
