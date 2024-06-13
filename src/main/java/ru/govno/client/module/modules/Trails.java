package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Trails extends Module {
   public ArrayList<Trails.Trailses> tr = new ArrayList<>();
   public static Trails get;
   public Map<Entity, List<Trails.AkrienTr>> tra = new HashMap<>();
   public ModeSettings Targets;
   public ModeSettings Mode;
   public ModeSettings Particle;
   public FloatSettings MaxDistance;
   public FloatSettings CountCrate;
   public FloatSettings Alpha;
   public FloatSettings Ticks;
   public BoolSettings BloomEffect;
   public BoolSettings NoFirstPerson;

   public Trails() {
      super("Trails", 0, Module.Category.RENDER);
      this.settings.add(this.Targets = new ModeSettings("Targets", "Self", this, new String[]{"Self", "All&Self", "All"}));
      this.settings
         .add(this.Mode = new ModeSettings("Mode", "Points", this, new String[]{"Points", "Default", "Default2", "Russia", "Particles", "BackTracks"}));
      this.settings
         .add(
            this.Particle = new ModeSettings(
               "Particle", "Fire", this, new String[]{"Fire", "Ench", "Spark"}, () -> this.Mode.getMode().equalsIgnoreCase("Particles")
            )
         );
      this.settings
         .add(
            this.MaxDistance = new FloatSettings(
               "MaxDistance",
               5.0F,
               10.0F,
               0.5F,
               this,
               () -> !this.Mode.getMode().equalsIgnoreCase("Particles") && !this.Mode.getMode().equalsIgnoreCase("BackTracks")
            )
         );
      this.settings.add(this.CountCrate = new FloatSettings("CountCrate", 2.0F, 5.0F, 1.0F, this, () -> this.Mode.getMode().equalsIgnoreCase("Particles")));
      this.settings.add(this.Alpha = new FloatSettings("Alpha", 200.0F, 255.0F, 0.0F, this, () -> !this.Mode.getMode().equalsIgnoreCase("Particles")));
      this.settings
         .add(
            this.BloomEffect = new BoolSettings(
               "BloomEffect", true, this, () -> !this.Mode.getMode().equalsIgnoreCase("Particles") && !this.Mode.getMode().equalsIgnoreCase("BackTracks")
            )
         );
      this.settings.add(this.Ticks = new FloatSettings("Ticks", 3.0F, 15.0F, 1.0F, this, () -> this.Mode.getMode().equalsIgnoreCase("BackTracks")));
      this.settings.add(this.NoFirstPerson = new BoolSettings("NoFirstPerson", true, this, () -> this.Targets.getMode().contains("Self")));
      get = this;
   }

   int getTracksSize(EntityPlayer player) {
      return (int)this.Ticks.getFloat() + 1;
   }

   Vec3d getVecByPlayer(EntityPlayer player) {
      double x = player.posX;
      double y = player.posY;
      double z = player.posZ;
      return new Vec3d(x, y, z);
   }

   void updateTrack(EntityPlayer player) {
      boolean playerMove = MathUtils.getDifferenceOf(player.posX, player.lastTickPosX) > 0.03
         || MathUtils.getDifferenceOf(player.posY, player.lastTickPosY) > 0.01
         || MathUtils.getDifferenceOf(player.posZ, player.lastTickPosZ) > 0.03;
      if (playerMove && doRenderTrail(player)) {
         player.tracks
            .add(
               new Trails.Track(
                  this.getVecByPlayer(player), player.rotationYaw, player.rotationPitch, player.prevLimbSwingAmount, player.limbSwingAmount, player.limbSwing
               )
            );
      } else if (player.tracks.size() > 0) {
         player.tracks.remove(player.tracks.get(0));
      }

      if (player.tracks.size() >= this.getTracksSize(player)) {
         player.tracks.remove(player.tracks.get(0));
      }
   }

   List<EntityPlayer> players() {
      List<EntityPlayer> p = new ArrayList<>();

      for (EntityPlayer e : mc.world.playerEntities) {
         if (e != null && e.getHealth() != 0.0F && !e.getPosition().equals(new BlockPos(0, 0, 0))) {
            p.add(e);
         }
      }

      return p;
   }

   void startDraws() {
      GL11.glPushMatrix();
      GL11.glDisable(2896);
      mc.entityRenderer.disableLightmap();
      GL11.glEnable(3042);
      GlStateManager.enableAlpha();
      GL11.glDisable(3008);
      GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);
      GL11.glBlendFunc(770, 1);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
   }

   void stopDraws() {
      mc.entityRenderer.enableLightmap();
      GL11.glEnable(2929);
      GL11.glEnable(3553);
      GL11.glEnable(3008);
      GL11.glBlendFunc(770, 771);
      GL11.glTranslated(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
      GL11.glPopMatrix();
   }

   Vec3d getPosByAABB(AxisAlignedBB aabb) {
      return new Vec3d(aabb.minX + (aabb.maxX - aabb.minX) / 2.0, aabb.minY, aabb.minZ + (aabb.maxZ - aabb.minZ) / 2.0);
   }

   void drawModel(EntityPlayer player, float pticks) {
      if (player != null) {
         int i = 0;
         this.startDraws();

         for (Trails.Track track : player.tracks) {
            Vec3d vec = track.pos;
            Vec3d prevVec = vec;
            if (i - 1 > 0 && player.tracks.get(i - 1) != null && player.tracks.get(i - 1).pos != null) {
               prevVec = player.tracks.get(i - 1).pos;
            }

            double xDiff = vec.xCoord - prevVec.xCoord;
            double yDiff = vec.yCoord - prevVec.yCoord;
            double zDiff = vec.zCoord - prevVec.zCoord;
            i++;
            Vec3d posVec = vec.addVector(xDiff * (double)pticks, yDiff * (double)pticks, zDiff * (double)pticks);
            int color = ClientColors.getColor1((int)((float)i * 50.0F));
            float alphaPC = (float)i / (float)player.tracks.size();
            alphaPC = alphaPC > 0.5F ? 1.0F - alphaPC : alphaPC;
            RenderUtils.glColor(ClientColors.getColor1((int)((float)i * 50.0F), this.Alpha.getFloat() / 255.0F * alphaPC * 0.2F));
            if (Minecraft.player.connection != null && Minecraft.player.connection.getPlayerInfo(player.getUniqueID()) != null) {
               GameType gm = Minecraft.player.connection.getPlayerInfo(player.getUniqueID()).getGameType();
               player.setGameType(GameType.SPECTATOR);
               player.noRenderArms = true;
               float prevPitch = player.rotationPitch;
               player.rotationPitch = track.pitch;
               float prevYaw = player.rotationYaw;
               player.rotationYaw = track.yaw;
               RenderLivingBase.silentMode = true;
               mc.renderManager.doRenderEntityNoShadow(player, posVec.xCoord, posVec.yCoord, posVec.zCoord, track.yaw, pticks, true);
               RenderLivingBase.silentMode = false;
               player.rotationPitch = prevPitch;
               player.rotationYaw = prevYaw;
               player.noRenderArms = false;
               player.setGameType(gm);
            }

            GlStateManager.resetColor();
         }

         this.stopDraws();
      }
   }

   @Override
   public void alwaysRender3DV2(float partialTicks) {
      if (this.actived && this.Mode.getMode().equalsIgnoreCase("BackTracks")) {
         for (EntityPlayer player : this.players()) {
            if (player.tracks != null && player.tracks.size() != 0) {
               this.drawModel(player, partialTicks);
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      DashTrail dash = DashTrail.dash;
      if (dash.isActived()) {
         String targets = this.Targets.getMode();
         boolean self = targets.endsWith("Self");
         boolean all = targets.startsWith("All");
         if (dash.Self.getBool() && self || (dash.Players.getBool() || dash.Friends.getBool()) && all) {
            this.toggle(false);
            Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7]: §7you will have to enable the Target setting in DashTrails.", false);
            return;
         }
      }

      if (this.Mode.getMode().equalsIgnoreCase("BackTracks")) {
         for (EntityPlayer player : this.players()) {
            if (player != null && player.tracks != null) {
               this.updateTrack(player);
            }
         }
      } else {
         for (EntityPlayer playerx : this.players()) {
            if (playerx.tracks.size() > 0) {
               playerx.tracks.clear();
            }
         }
      }

      if (this.Mode.getMode().equalsIgnoreCase("Particles")) {
         int oldSetting = mc.gameSettings.particleSetting;
         boolean oldSetting2 = mc.gameSettings.ofFireworkParticles;
         boolean oldSetting3 = mc.gameSettings.ofPortalParticles;
         boolean oldSetting4 = mc.gameSettings.ofPotionParticles;
         boolean oldSetting5 = mc.gameSettings.ofVoidParticles;
         boolean oldSetting6 = mc.gameSettings.ofWaterParticles;
         mc.gameSettings.particleSetting = 1;
         mc.gameSettings.ofFireworkParticles = true;
         mc.gameSettings.ofPortalParticles = true;
         mc.gameSettings.ofPotionParticles = true;
         mc.gameSettings.ofVoidParticles = true;
         mc.gameSettings.ofWaterParticles = true;

         for (EntityPlayer e : mc.world.playerEntities) {
            if (doRenderTrail(e)) {
               double yaw = (double)e.rotationYaw * 0.017453292;
               int ex = e == Minecraft.player ? 1 : 0;
               float x = (float)(e.lastTickPosX + (e.posX - e.lastTickPosX) * (double)mc.getRenderPartialTicks());
               float y = (float)(e.lastTickPosY + (e.posY - e.lastTickPosY) * (double)mc.getRenderPartialTicks());
               float z = (float)(e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double)mc.getRenderPartialTicks());

               for (int i = 0; i < (int)this.CountCrate.getFloat(); i++) {
                  if (this.Particle.getMode().equalsIgnoreCase("Fire")) {
                     mc.world
                        .spawnParticle(
                           EnumParticleTypes.LAVA,
                           (double)x - Math.sin(yaw) * -0.45 * (double)ex,
                           (double)y + 0.1,
                           (double)z + Math.cos(yaw) * -0.45 * (double)ex,
                           0.0,
                           0.0,
                           0.0,
                           new int[]{1}
                        );
                  } else if (this.Particle.getMode().equalsIgnoreCase("Ench")) {
                     for (float h = 0.0F; h < e.height - 0.05F; h += 0.1F) {
                        mc.world
                           .spawnParticle(
                              EnumParticleTypes.ENCHANTMENT_TABLE,
                              (double)x - Math.sin(yaw) * -0.45 * (double)ex,
                              (double)(y + h),
                              (double)z + Math.cos(yaw) * -0.45 * (double)ex,
                              0.0,
                              0.0,
                              0.0,
                              new int[]{1}
                           );
                     }
                  } else if (this.Particle.getMode().equalsIgnoreCase("Spark")) {
                     mc.world
                        .spawnParticle(
                           EnumParticleTypes.FIREWORKS_SPARK,
                           (double)x - Math.sin(yaw) * -0.45 * (double)ex,
                           (double)y + 0.4,
                           (double)z + Math.cos(yaw) * -0.45 * (double)ex,
                           0.0,
                           0.0,
                           0.0,
                           new int[]{1}
                        );
                  }
               }
            }
         }

         mc.gameSettings.particleSetting = oldSetting;
         mc.gameSettings.ofFireworkParticles = oldSetting2;
         mc.gameSettings.ofPortalParticles = oldSetting3;
         mc.gameSettings.ofPotionParticles = oldSetting4;
         mc.gameSettings.ofVoidParticles = oldSetting5;
         mc.gameSettings.ofWaterParticles = oldSetting6;
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived) {
         for (EntityPlayer player : this.players()) {
            if (player != null) {
               player.tracks.clear();
            }
         }

         for (EntityPlayer e : mc.world.playerEntities) {
            e.trail.clear();
         }

         this.tr.clear();
      }

      super.onToggled(actived);
   }

   public static double getInterpolationSpeed(double speed) {
      return MathUtils.clamp(
         speed * 100.0 / (double)Minecraft.getDebugFPS(),
         0.0 / (speed * 100.0) - (double)Minecraft.getDebugFPS(),
         speed * 100.0 / (double)Minecraft.getDebugFPS()
      );
   }

   public static boolean doRenderTrail(Entity entityIn) {
      if (!get.Targets.getMode().equalsIgnoreCase("Self")
         || !(entityIn instanceof EntityPlayerSP)
         || mc.gameSettings.thirdPersonView == 0 && get.NoFirstPerson.getBool()) {
         return get.Targets.getMode().equalsIgnoreCase("All&Self")
            ? !(entityIn instanceof EntityPlayerSP) || mc.gameSettings.thirdPersonView != 0 || !get.NoFirstPerson.getBool()
            : get.Targets.getMode().equalsIgnoreCase("All") && entityIn != Minecraft.player;
      } else {
         return true;
      }
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      int GET_MAX_SIZE = (int)(this.MaxDistance.getFloat() * 20.0F);
      int GET_MAX_SIZE2 = (int)(this.MaxDistance.getFloat() * 100.0F);

      for (EntityPlayer e : mc.world.playerEntities) {
         double posX = e.posX;
         double prevX = posX - e.prevPosX;
         double posY = e.posY;
         double prevY = posY - e.prevPosY;
         double posZ = e.posZ;
         double prevZ = posZ - e.prevPosZ;
         double bps = Math.sqrt(prevX * prevX + prevY * prevY + prevZ * prevZ);
         float x = (float)(e.lastTickPosX + (e.posX - e.lastTickPosX) * (double)mc.getRenderPartialTicks());
         float y = (float)(e.lastTickPosY + (e.posY - e.lastTickPosY) * (double)mc.getRenderPartialTicks());
         float z = (float)(e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double)mc.getRenderPartialTicks());
         if (bps != 0.0 && doRenderTrail(e) && !this.Mode.getMode().equalsIgnoreCase("Particles") && !this.Mode.getMode().equalsIgnoreCase("BackTracks")) {
            if (!this.Mode.getMode().equalsIgnoreCase("Default")
               && !this.Mode.getMode().equalsIgnoreCase("Default2")
               && !this.Mode.getMode().equalsIgnoreCase("Russia")) {
               this.tr.add(new Trails.Trailses(x, y + 0.2F, z));
            } else {
               boolean DO = !(e instanceof EntityPlayerSP) || mc.gameSettings.thirdPersonView != 0 || !this.NoFirstPerson.getBool();
               if (DO) {
                  e.trail.add(new Trails.AkrienTr((double)x, (double)y, (double)z));
               } else {
                  e.trail.clear();
               }
            }
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            this.BloomEffect.getBool() ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         GlStateManager.shadeModel(7425);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glDisable(3008);
         GlStateManager.disableCull();
         mc.entityRenderer.disableLightmap();
         GL11.glBegin(5);
         int color = -1;
         float alpha = 0.0F;
         int step = 0;

         for (Trails.AkrienTr t : e.trail) {
            long deltaTime = Minecraft.getSystemTime() - t.spawnTime;
            if (deltaTime >= (long)GET_MAX_SIZE2 || !e.isEntityAlive()) {
               e.trail.remove(t);
            }

            t.age = (double)(deltaTime / (long)GET_MAX_SIZE2);
         }

         if (this.Mode.getMode().equalsIgnoreCase("Russia")) {
            int[] colors = new int[]{ColorUtils.getColor(255, 0, 0), ColorUtils.getColor(0, 0, 255), -1};

            for (int i = 0; i < 3; i++) {
               if (i != 0) {
                  GL11.glBegin(5);
               }

               float h = e.height / (e.isChild() ? 1.8F : 1.0F) / 3.0F;

               for (Trails.AkrienTr t : e.trail) {
                  step = e.trail.indexOf(t) * -3 + 1000;
                  alpha = (float)e.trail.indexOf(t)
                     / (float)e.trail.size()
                     * ((float)e.trail.indexOf(t) / (float)e.trail.size())
                     * this.Alpha.getFloat()
                     * ((float)ColorUtils.getAlphaFromColor(color) / 255.0F);
                  RenderUtils.setupColor(colors[i], alpha);
                  GL11.glVertex3d(t.posX, t.posY + (double)(h * (float)i) + (double)h, t.posZ);
                  GL11.glVertex3d(t.posX, t.posY + (double)(h * (float)i), t.posZ);
               }

               GL11.glEnd();
            }
         } else {
            for (Trails.AkrienTr t : e.trail) {
               step = e.trail.indexOf(t) * -3 + 1000;
               color = ClientColors.getColor1((int)((float)step * 2.25F));
               alpha = (float)e.trail.indexOf(t)
                  / (float)e.trail.size()
                  * ((float)e.trail.indexOf(t) / (float)e.trail.size())
                  * this.Alpha.getFloat()
                  * ((float)ColorUtils.getAlphaFromColor(color) / 255.0F);
               RenderUtils.setupColor(color, this.Mode.getMode().equalsIgnoreCase("Default2") ? alpha : 0.0F);
               float h = e.height / (e.isChild() ? 1.8F : 1.0F);
               GL11.glVertex3d(t.posX, t.posY + (double)h, t.posZ);
               RenderUtils.setupColor(color, alpha);
               GL11.glVertex3d(t.posX, t.posY, t.posZ);
            }

            GL11.glEnd();
            if (this.Mode.getMode().equalsIgnoreCase("Default2")) {
               GL11.glBegin(5);

               for (Trails.AkrienTr t : e.trail) {
                  step = e.trail.indexOf(t) * -3 + 1000;
                  color = ClientColors.getColor1((int)((float)step * 2.25F));
                  alpha = (float)e.trail.indexOf(t)
                     / (float)e.trail.size()
                     * ((float)e.trail.indexOf(t) / (float)e.trail.size())
                     * this.Alpha.getFloat()
                     * ((float)ColorUtils.getAlphaFromColor(color) / 255.0F);
                  RenderUtils.setupColor(color, alpha);
                  float h = e.height / (e.isChild() ? 1.8F : 1.0F);
                  float ss = (float)e.trail.indexOf(t) / (float)e.trail.size();
                  GL11.glVertex3d(t.posX, t.posY + (double)h, t.posZ);
                  GL11.glVertex3d(t.posX, t.posY + (double)h - 0.07 * (double)ss, t.posZ);
               }

               GL11.glEnd();
               GL11.glBegin(5);

               for (Trails.AkrienTr t : e.trail) {
                  step = e.trail.indexOf(t) * -3 + 1000;
                  color = ClientColors.getColor1((int)((float)step * 2.25F));
                  alpha = (float)e.trail.indexOf(t)
                     / (float)e.trail.size()
                     * ((float)e.trail.indexOf(t) / (float)e.trail.size())
                     * this.Alpha.getFloat()
                     * ((float)ColorUtils.getAlphaFromColor(color) / 255.0F);
                  RenderUtils.setupColor(color, alpha);
                  float ss = (float)e.trail.indexOf(t) / (float)e.trail.size();
                  GL11.glVertex3d(t.posX, t.posY, t.posZ);
                  GL11.glVertex3d(t.posX, t.posY + 0.07 * (double)ss, t.posZ);
               }

               GL11.glEnd();
            }
         }

         mc.entityRenderer.enableLightmap();
         GlStateManager.shadeModel(7424);
         GL11.glEnable(3008);
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         GlStateManager.resetColor();
         GlStateManager.enableCull();
         GlStateManager.enableTexture2D();
         GlStateManager.depthMask(true);
         GlStateManager.enableAlpha();
         GlStateManager.resetColor();
         GlStateManager.popMatrix();
      }

      if (!this.Mode.getMode().equalsIgnoreCase("Default") && !this.Mode.getMode().equalsIgnoreCase("Default2")) {
         this.tr.removeIf(tx -> tx.size >= (float)GET_MAX_SIZE);
         if (this.tr.size() > 0) {
            mc.getTextureManager().bindTexture(new ResourceLocation("vegaline/modules/trails/points/bloom.png"));
         }

         for (Trails.Trailses p : this.tr) {
            int color = -1;
            int step = (int)p.size * -2 + 1000;
            color = ColorUtils.getOverallColorFrom(
               ClientColors.getColor2(step), ClientColors.getColor1(step), MathUtils.clamp(p.size / (float)this.tr.size(), 0.0F, 1.0F)
            );
            color = ColorUtils.swapAlpha(color, (float)((int)this.Alpha.getFloat()));
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glDisable(3008);
            GlStateManager.disableCull();
            mc.entityRenderer.disableLightmap();
            GL11.glTranslated((double)p.x - RenderManager.renderPosX, (double)p.y - RenderManager.renderPosY, (double)p.z - RenderManager.renderPosZ);
            GlStateManager.rotate(mc.getRenderManager().playerViewY + WorldRender.get.offYawOrient, 0.0F, -1.0F, 0.0F);
            GlStateManager.rotate(
               mc.getRenderManager().playerViewX + WorldRender.get.offPitchOrient, mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F
            );
            float enfo = ((1.0F - p.size / (float)GET_MAX_SIZE) / 1.5F + 0.5F) / 2.0F;
            GL11.glScalef(enfo, enfo, enfo);
            int c = ColorUtils.swapAlpha(
               color, (float)ColorUtils.getAlphaFromColor(color) * (1.0F - p.size / (float)GET_MAX_SIZE) * (this.Alpha.getFloat() / 255.0F)
            );
            GL11.glBlendFunc(770, 32772);
            GlStateManager.enableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(6, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(-0.5, -0.5).tex(0.0, 0.0).color(c).endVertex();
            buffer.pos(0.5, -0.5).tex(1.0, 0.0).color(c).endVertex();
            buffer.pos(0.5, 0.5).tex(1.0, 1.0).color(c).endVertex();
            buffer.pos(-0.5, 0.5).tex(0.0, 1.0).color(c).endVertex();
            tessellator.draw();
            GL11.glBlendFunc(770, 771);
            mc.entityRenderer.enableLightmap();
            GlStateManager.shadeModel(7424);
            GL11.glEnable(3008);
            GlStateManager.tryBlendFuncSeparate(
               GlStateManager.SourceFactor.SRC_ALPHA,
               GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
               GlStateManager.SourceFactor.ONE,
               GlStateManager.DestFactor.ZERO
            );
            GlStateManager.resetColor();
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            GlStateManager.resetColor();
            GlStateManager.popMatrix();
            p.size++;
         }
      } else {
         for (Entity entity : this.tra.keySet()) {
            EntityPlayer e = (EntityPlayer)entity;
            double posXx = e.posX;
            double prevXx = posXx - e.prevPosX;
            double posYx = e.posY;
            double prevYx = posYx - e.prevPosY;
            double posZx = e.posZ;
            double prevZx = posZx - e.prevPosZ;
            double bpsx = Math.sqrt(prevXx * prevXx + prevYx * prevYx + prevZx * prevZx);
            this.tra.get(entity).removeIf(tx -> entity == null || tx.age >= (double)(GET_MAX_SIZE * 2));
         }
      }
   }

   public class AkrienTr {
      public double age;
      double posX;
      double posY;
      double posZ;
      public final long spawnTime;

      public AkrienTr(double posX, double posY, double posZ) {
         this.posX = posX;
         this.posY = posY;
         this.posZ = posZ;
         this.spawnTime = Minecraft.getSystemTime();
      }

      public void updateAge() {
         this.age++;
      }
   }

   public class Track {
      Vec3d pos;
      float yaw;
      float pitch;
      float prevLimbSwingAmount;
      float limbSwingAmount;
      float limbSwing;

      Track(Vec3d pos, float yaw, float pitch, float prevLimbSwingAmount, float limbSwingAmount, float limbSwing) {
         this.pos = pos;
         this.yaw = yaw;
         this.pitch = pitch;
         this.prevLimbSwingAmount = prevLimbSwingAmount;
         this.limbSwingAmount = limbSwingAmount;
         this.limbSwing = limbSwing;
      }
   }

   public class Trailses {
      float x;
      float y;
      float z;
      float size;

      public Trailses(float x, float y, float z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }
}
