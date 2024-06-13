package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class WallHack extends Module {
   public static WallHack get;
   BoolSettings Players;
   BoolSettings Friends;
   BoolSettings Crystals;
   BoolSettings Mobs;
   BoolSettings Self;
   BoolSettings Tiles;
   ModeSettings ColorMode;
   FloatSettings Opacity;
   ColorSettings PickColor;

   public WallHack() {
      super("WallHack", 0, Module.Category.RENDER);
      get = this;
      this.settings.add(this.Players = new BoolSettings("Players", true, this));
      this.settings.add(this.Friends = new BoolSettings("Friends", true, this));
      this.settings.add(this.Mobs = new BoolSettings("Mobs", true, this));
      this.settings.add(this.Self = new BoolSettings("Self", true, this));
      this.settings.add(this.Crystals = new BoolSettings("Crystals", true, this));
      this.settings.add(this.ColorMode = new ModeSettings("CrysColorMode", "Client", this, new String[]{"Client", "Picker"}, () -> this.Crystals.getBool()));
      this.settings
         .add(
            this.Opacity = new FloatSettings(
               "CrysOpacity", 0.7F, 1.0F, 0.05F, this, () -> this.Crystals.getBool() && this.ColorMode.currentMode.equalsIgnoreCase("Client")
            )
         );
      this.settings
         .add(
            this.PickColor = new ColorSettings(
               "CrysPickColor",
               ColorUtils.getColor(110, 160, 255, 225),
               this,
               () -> this.Crystals.getBool() && this.ColorMode.currentMode.equalsIgnoreCase("Picker")
            )
         );
      this.settings.add(this.Tiles = new BoolSettings("Tiles", true, this));
   }

   @Override
   public void onUpdate() {
      if (!this.Players.getBool() && !this.Friends.getBool() && !this.Crystals.getBool() && !this.Mobs.getBool()) {
         this.toggle(false);
         Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7]: §7включите что-нибудь в настройках.", false);
      }
   }

   private boolean[] getEtypes() {
      return new boolean[]{
         this.Players.getBool(), this.Friends.getBool(), this.Crystals.getBool(), this.Mobs.getBool(), this.Self.getBool(), this.Tiles.getBool()
      };
   }

   private boolean isCurrent(boolean[] entTypes, Entity entity) {
      if (entity == null) {
         return false;
      } else {
         if (entity instanceof EntityLivingBase base
            && (
               entTypes[4] && base instanceof EntityPlayerSP
                  || base instanceof EntityOtherPlayerMP mp && entTypes[Client.friendManager.isFriend(mp.getName()) ? 1 : 0]
                  || (base instanceof EntityAnimal || base instanceof EntityMob) && entTypes[3]
            )) {
            return base.isEntityAlive();
         }

         if (entity instanceof EntityEnderCrystal crystal && !crystal.isDead) {
            return entTypes[2];
         }

         return !(entity instanceof EntityMinecartContainer) && !(entity instanceof IProjectile) && !(entity instanceof EntityArmorStand) ? false : entTypes[5];
      }
   }

   private boolean isCurrent(boolean[] entTypes, TileEntity tileEntity) {
      return entTypes[5];
   }

   private int getChamsColor(Entity entityIn) {
      int color = 0;
      if (entityIn instanceof EntityEnderCrystal) {
         String var3 = this.ColorMode.currentMode;
         switch (var3) {
            case "Client":
               color = ClientColors.getColor1(Math.abs(entityIn.getEntityId()), this.Opacity.getFloat());
               break;
            case "Picker":
               color = this.PickColor.color;
         }
      }

      return color;
   }

   private void crystalPreChams(Runnable renderModel) {
      float hds = ((float)Minecraft.player.ticksExisted + mc.getRenderPartialTicks()) % 20.0F / 20.0F;
      hds = (float)MathUtils.easeInOutQuadWave((double)hds);
      float startScale = 1.025F;
      float endScale = 1.1F + 0.25F * hds;
      float alphaStart = 0.1F;
      float alphaEnd = 0.003921569F;
      int iterations = 5 + (int)(10.0F * hds);
      GL11.glEnable(3042);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA,
         GlStateManager.DestFactor.ONE,
         GlStateManager.SourceFactor.ONE_MINUS_CONSTANT_ALPHA,
         GlStateManager.DestFactor.ZERO
      );
      GL11.glDepthMask(false);
      GL11.glEnable(2884);
      GL11.glCullFace(1028);
      mc.entityRenderer.disableLightmap();
      GlStateManager.disableLighting();
      GL11.glAlphaFunc(516, 0.003921569F);
      ModelEnderCrystal.cancelBase = true;

      for (int index = 0; index < iterations; index++) {
         float scale = MathUtils.lerp(startScale, endScale, (float)index / (float)iterations);
         float alphaPC = MathUtils.lerp(alphaStart, alphaEnd, (float)index / (float)iterations);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, alphaPC);
         float descale = 1.0F / scale;
         float append = 0.5F;
         GL11.glTranslated(0.0, (double)append, 0.0);
         GL11.glScaled((double)scale, (double)scale, (double)scale);
         GL11.glTranslated(0.0, (double)(-append), 0.0);
         renderModel.run();
         GL11.glTranslated(0.0, (double)append, 0.0);
         GL11.glScaled((double)descale, (double)descale, (double)descale);
         GL11.glTranslated(0.0, (double)(-append), 0.0);
      }

      ModelEnderCrystal.cancelBase = false;
      GL11.glAlphaFunc(516, 0.1F);
      GlStateManager.enableLighting();
      mc.entityRenderer.enableLightmap();
      GL11.glEnable(3553);
      GL11.glCullFace(1029);
      GL11.glDepthMask(true);
      GL11.glDisable(2884);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.resetColor();
   }

   private void renderChams(boolean chamsMode, boolean pre, int col, Runnable renderModel, boolean isRenderItems) {
      if (pre) {
         if (!isRenderItems && chamsMode) {
            GlStateManager.enableBlend();
            GL11.glDisable(3553);
            GL11.glDisable(3008);
            GL11.glEnable(2884);
            GL11.glDisable(2896);
            RenderUtils.glColor(ColorUtils.swapAlpha(col, (float)ColorUtils.getAlphaFromColor(col) / 2.0F));
            GL11.glDepthMask(false);
            mc.entityRenderer.disableLightmap();
            GlStateManager.tryBlendFuncSeparate(
               GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
         }

         if (isRenderItems) {
            GL11.glEnable(3553);
         } else {
            GL11.glDepthRange(0.0, 0.01);
         }

         renderModel.run();
         GL11.glDepthRange(0.0, 1.0);
         if (!isRenderItems && chamsMode) {
            GlStateManager.tryBlendFuncSeparate(
               GlStateManager.SourceFactor.SRC_ALPHA,
               GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
               GlStateManager.SourceFactor.ONE,
               GlStateManager.DestFactor.ZERO
            );
            mc.entityRenderer.enableLightmap();
            GL11.glEnable(2896);
            GL11.glDepthMask(true);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GlStateManager.resetColor();
            GlStateManager.color(1.0F, 1.0F, 1.0F);
         }
      }
   }

   @Override
   public void preRenderLivingBase(Entity baseIn, Runnable renderModel, boolean isRenderItems) {
      if (this.isCurrent(this.getEtypes(), baseIn)) {
         mc.renderManager.renderShadow = false;
         boolean crystal = baseIn instanceof EntityEnderCrystal;
         if (Minecraft.player != null && (crystal || !Minecraft.player.canEntityBeSeen(baseIn))) {
            if (!isRenderItems && crystal && ModelEnderCrystal.canDeformate) {
               this.crystalPreChams(renderModel);
            }

            this.renderChams(crystal, true, this.getChamsColor(baseIn), renderModel, isRenderItems);
         }
      }
   }

   @Override
   public void postRenderLivingBase(Entity baseIn, Runnable renderModel, boolean isRenderItems) {
      if (this.isCurrent(this.getEtypes(), baseIn)) {
         mc.renderManager.renderShadow = mc.gameSettings.entityShadows;
         boolean crystal = baseIn instanceof EntityEnderCrystal;
         if (Minecraft.player != null && (crystal || !Minecraft.player.canEntityBeSeen(baseIn))) {
            this.renderChams(crystal, false, 0, renderModel, isRenderItems);
         }
      }
   }

   public void preRenderTileEntity(TileEntity tileIn, Runnable renderModel) {
      if (this.isCurrent(this.getEtypes(), tileIn)) {
         mc.renderManager.renderShadow = false;
         this.renderChams(false, true, 0, renderModel, false);
      }
   }

   public void postRenderTileEntity(TileEntity tileIn, Runnable renderModel) {
      if (this.isCurrent(this.getEtypes(), tileIn)) {
         mc.renderManager.renderShadow = mc.gameSettings.entityShadows;
         this.renderChams(false, true, 0, renderModel, false);
      }
   }
}
