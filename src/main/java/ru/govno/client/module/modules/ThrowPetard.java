package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class ThrowPetard extends Module {
   private final List<ThrowPetard.Petard> PETARDS = new ArrayList<>();
   private final ResourceLocation BLOOM_TEX = new ResourceLocation("vegaline/modules/throwpetard/bloom.png");
   List<ThrowPetard.BlowupBLOOMVF> blowupBloomVFXList = new ArrayList<>();
   Tessellator tessellator = Tessellator.getInstance();
   BufferBuilder buffer = this.tessellator.getBuffer();
   private final List<ThrowPetard.NanoSpark> nanoSparks = new ArrayList<>();

   public ThrowPetard() {
      super("ThrowPetard", 0, Module.Category.MISC);
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         this.PETARDS
            .add(
               new ThrowPetard.Petard(
                  Minecraft.player.getPositionEyes(1.0F).addVector(0.0, -0.4, 0.0),
                  Minecraft.player.rotationYaw,
                  (float)(0.35F + MoveMeHelp.getSpeed()) + (Minecraft.player.isJumping() && Minecraft.player.motionY > 0.18 ? 0.5F : 0.0F),
                  1300.0F
               )
            );
         this.toggleSilent(false);
      }

      super.onToggled(actived);
   }

   @Override
   public void alwaysUpdate() {
      if (!this.blowupBloomVFXList.isEmpty()) {
         this.blowupBloomVFXList.removeIf(ThrowPetard.BlowupBLOOMVF::isToRemove);
      }

      if (!this.PETARDS.isEmpty()) {
         this.PETARDS.removeIf(ThrowPetard.Petard::isToRemove);
         if (!this.PETARDS.isEmpty()) {
            this.PETARDS.forEach(ThrowPetard.Petard::update);
         }
      }
   }

   @Override
   public void alwaysRender3D(float partialTicks) {
      if (!this.PETARDS.isEmpty()) {
         this.PETARDS.forEach(petard -> petard.drawModel(partialTicks));
      }

      if (!this.nanoSparks.isEmpty()) {
         this.nanoSparks.removeIf(ThrowPetard.NanoSpark::isToRemove);
         if (!this.nanoSparks.isEmpty()) {
            this.glSetDrawing(true);
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_CONSTANT_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GL11.glPointSize(2.25F);
            GL11.glBegin(0);
            this.nanoSparks.forEach(ThrowPetard.NanoSpark::drawAndUpdate);
            GL11.glEnd();
            GL11.glPointSize(1.0F);
            this.glSetDrawing(false);
         }
      }

      if (!this.blowupBloomVFXList.isEmpty()) {
         this.blowupBloomVFXList.forEach(blowup -> blowup.draw());
      }
   }

   private void glSetDrawing(boolean pre) {
      double glX = RenderManager.viewerPosX;
      double glY = RenderManager.viewerPosY;
      double glZ = RenderManager.viewerPosZ;
      if (pre) {
         GL11.glPushMatrix();
         mc.entityRenderer.disableLightmap();
         GL11.glEnable(3042);
         GL11.glLineWidth(1.0F);
         GL11.glDisable(3553);
         GL11.glDisable(2884);
         GL11.glDisable(2896);
         GL11.glDepthMask(false);
         GL11.glShadeModel(7425);
         GL11.glTranslated(-glX, -glY, -glZ);
      } else {
         GL11.glTranslated(glX, glY, glZ);
         GL11.glLineWidth(1.0F);
         GL11.glShadeModel(7424);
         GL11.glEnable(3553);
         GL11.glEnable(2884);
         GL11.glDepthMask(true);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         GlStateManager.resetColor();
         GL11.glPopMatrix();
      }
   }

   private class BlowupBLOOMVF {
      Vec3d pos;
      long startTime = System.currentTimeMillis();
      float maxTime = 300.0F;

      public BlowupBLOOMVF(Vec3d pos) {
         this.pos = pos;
      }

      float timePC() {
         return (float)(System.currentTimeMillis() - this.startTime) / this.maxTime;
      }

      boolean isToRemove() {
         return this.timePC() >= 1.0F;
      }

      public void draw() {
         float scale = 1.0F;
         float timePC = this.timePC();
         float aPC = 1.0F - timePC;
         if (!(timePC > 1.0F)) {
            aPC = (float)MathUtils.easeInCircle((double)aPC);
            if (!(aPC * 255.0F < 1.0F)) {
               ThrowPetard.this.glSetDrawing(true);
               GL11.glTranslated(this.pos.xCoord, this.pos.yCoord, this.pos.zCoord);
               GlStateManager.rotate(Module.mc.getRenderManager().playerViewY + WorldRender.get.offYawOrient, 0.0F, -1.0F, 0.0F);
               GlStateManager.rotate(
                  Module.mc.getRenderManager().playerViewX + WorldRender.get.offPitchOrient,
                  Module.mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F,
                  0.0F,
                  0.0F
               );
               GL11.glScalef(scale, scale, scale);
               GL11.glEnable(3553);
               Module.mc.getTextureManager().bindTexture(ThrowPetard.this.BLOOM_TEX);
               int blowupColor = ColorUtils.getColor(255, 180, 80, 255.0F * aPC);
               GL11.glBlendFunc(770, 32772);

               for (int i = 0; i < 50; i++) {
                  ThrowPetard.this.buffer.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
                  ThrowPetard.this.buffer.pos((double)(-scale), (double)(-scale)).tex(0.0, 0.0).color(blowupColor).endVertex();
                  ThrowPetard.this.buffer.pos((double)scale, (double)(-scale)).tex(1.0, 0.0).color(blowupColor).endVertex();
                  ThrowPetard.this.buffer.pos((double)scale, (double)scale).tex(1.0, 1.0).color(blowupColor).endVertex();
                  ThrowPetard.this.buffer.pos((double)(-scale), (double)scale).tex(0.0, 1.0).color(blowupColor).endVertex();
                  ThrowPetard.this.tessellator.draw();
                  scale *= 1.4F;
                  blowupColor = ColorUtils.swapAlpha(blowupColor, (float)ColorUtils.getAlphaFromColor(blowupColor) / 1.1F);
               }

               GL11.glBlendFunc(770, 771);
               ThrowPetard.this.glSetDrawing(false);
            }
         }
      }
   }

   private class NanoSpark {
      Vec3d pos;
      float ext;
      float yaw = (float)Math.toRadians(Math.random() * 360.0);
      float pitch = (float)Math.toRadians(-90.0 + Math.random() * 180.0);
      float maxTime;
      long startTime = System.currentTimeMillis();
      int baseColor;

      public NanoSpark(Vec3d pos, float minExt, float maxExt, float maxTime, int baseColor) {
         this.pos = pos;
         this.ext = MathUtils.lerp(minExt, maxExt, (float)Math.random());
         this.maxTime = maxTime;
         this.baseColor = baseColor;
      }

      public NanoSpark(Vec3d pos, float maxTime) {
         this.pos = pos;
         this.ext = MathUtils.lerp(0.2F, 0.5F, (float)Math.random());
         this.maxTime = maxTime;
         this.baseColor = ColorUtils.getColor(255, 135, 25);
      }

      public float getTimePC() {
         float pc = (float)(System.currentTimeMillis() - this.startTime) / this.maxTime;
         return pc > 1.0F ? 1.0F : pc;
      }

      public void drawAndUpdate() {
         float timePC = this.getTimePC();
         RenderUtils.glColor(ColorUtils.swapAlpha(this.baseColor, (float)ColorUtils.getAlphaFromColor(this.baseColor) * (1.0F - timePC)));
         Vec3d vec = this.pos
            .addVector(
               Math.sin((double)this.yaw) * (double)this.ext * (double)timePC,
               Math.cos((double)this.pitch) * (double)this.ext * (double)timePC,
               -Math.cos((double)this.yaw) * (double)this.ext * (double)timePC
            );
         GL11.glVertex3d(vec.xCoord, vec.yCoord, vec.zCoord);
      }

      public boolean isToRemove() {
         return this.getTimePC() == 1.0F;
      }
   }

   private class Petard {
      private boolean inLiquid;
      private boolean onGround;
      public boolean removeable;
      public boolean blowuped;
      private final long spawnTime = System.currentTimeMillis();
      private final float wickTimeout;
      private final float yaw;
      private float speed;
      private double x;
      private double y;
      private double z;
      private double px;
      private double py;
      private double pz;
      private double mx;
      private double my;
      private double mz;
      private final double scale = 0.2F;

      private Petard(Vec3d spawnIn, float yaw, float flySpeed, float wickTimeout) {
         this.x = this.px = spawnIn.xCoord;
         this.y = this.py = spawnIn.yCoord;
         this.z = this.pz = spawnIn.zCoord;
         this.yaw = yaw + (float)(-5.0 + Math.random() * 10.0);
         this.speed = flySpeed * (1.0F - Minecraft.player.rotationPitch / 90.0F);
         this.wickTimeout = wickTimeout;
         this.mx = -Math.sin(Math.toRadians((double)this.yaw));
         this.mz = Math.cos(Math.toRadians((double)this.yaw));
         this.my = (double)(-Minecraft.player.rotationPitch / 90.0F / 2.0F);
      }

      private boolean hasCollide(double x, double y, double z, double scaleXYZ) {
         return Module.mc.world != null
            && !Module.mc
               .world
               .getCollisionBoxes(
                  null,
                  new AxisAlignedBB(x - scaleXYZ / 2.0, y - scaleXYZ / 2.0, z - scaleXYZ / 2.0, x + scaleXYZ / 2.0, y + scaleXYZ / 2.0, z + scaleXYZ / 2.0)
               )
               .isEmpty();
      }

      private boolean hasLiquid(double x, double y, double z) {
         return Module.mc.world != null && Module.mc.world.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.WATER;
      }

      public void update() {
         boolean timeOuted = this.wickIsTimeouted();
         this.onGround = this.hasCollide(this.x, this.y + this.my - 0.01F, this.z, 0.2F);
         this.speed = this.speed * (this.onGround ? 0.7F : 0.98F);
         boolean collideX = this.hasCollide(this.x + this.mx, this.y + 0.1, this.z, 0.2F);
         boolean collideZ = this.hasCollide(this.x, this.y + 0.1, this.z + this.mz, 0.2F);
         this.inLiquid = this.hasLiquid(this.x, this.y, this.z)
            || this.hasLiquid(this.x, this.y + 0.2F, this.z)
            || this.hasLiquid(this.x, this.y + 0.1F, this.z);
         if (collideX) {
            this.mx *= -0.2F;
         }

         if (!this.onGround) {
            this.my -= 0.0225F;
            if (this.my < 0.0) {
               this.my *= 1.075F;
            } else {
               this.my /= 1.1125F;
            }
         }

         if (collideZ) {
            this.mz *= -0.2F;
         }

         if (this.inLiquid) {
            this.mx /= 1.4F;
            this.my /= 1.4F;
            this.mz /= 1.4F;
            if (this.onGround) {
               this.my = 0.0;
            }
         } else if (this.onGround) {
            this.my *= -0.3F;
         }

         this.px = this.x;
         this.py = this.y;
         this.pz = this.z;
         this.x = this.x + this.mx * (double)this.speed;
         this.y = this.y + this.my;
         this.z = this.z + this.mz * (double)this.speed;
         if (timeOuted && !this.blowuped) {
            ThrowPetard.this.blowupBloomVFXList.add(ThrowPetard.this.new BlowupBLOOMVF(new Vec3d(this.x, this.y + 0.1F, this.z)));
            String sfxLoc = "petard";
            if (this.inLiquid) {
               sfxLoc = sfxLoc + "liq";
            }

            int var = 1;
            sfxLoc = sfxLoc + "var" + var + ".wav";
            float volumePC = (float)(1.0 - Minecraft.player.getDistance(this.x, this.y, this.z) / 20.0);
            if (volumePC < 0.0F) {
               volumePC = 0.0F;
            }

            MusicHelper.playSound(sfxLoc, 0.1F + 0.5F * volumePC);
            this.removeable = true;
            this.blowuped = true;

            for (int i = 0; i < 500; i++) {
               ThrowPetard.this.nanoSparks
                  .add(
                     ThrowPetard.this.new NanoSpark(
                        new Vec3d(this.x, this.y, this.z), 0.1F, 0.6F, 150.0F + 250.0F * (float)Math.random(), ColorUtils.getColor(255, 185, 25)
                     )
                  );
            }

            if (Module.mc.isSingleplayer()) {
               Vec3d popVec = new Vec3d(this.x, this.y, this.z);

               for (EntityLivingBase base : Module.mc
                  .world
                  .getLoadedEntityList()
                  .stream()
                  .<EntityLivingBase>map(Entity::getLivingBaseOf)
                  .filter(Objects::nonNull)
                  .filter(basex -> basex.getDistance(this.x, this.y, this.z) < 7.0)
                  .collect(Collectors.toList())) {
                  if (base.canEntityBeSeenCoords(this.x, this.y, this.z)
                     && (!(base instanceof EntityPlayerSP) || !Velocity.get.actived || !Velocity.get.OnKnockBack.getBool())) {
                     double dst = base.getDistance(this.x, this.y, this.z);
                     double motionVal = MathUtils.clamp((7.0 - dst) / 7.0, 0.0, 1.0)
                        * (double)(this.inLiquid ? 1.0F : (Minecraft.player.isCreative() ? 4.0F : 1.5F));
                     float[] rotToEnt = RotationUtil.getVecNeeded(popVec, base.getPositionVector().addVector(0.0, 3.0, 0.0));
                     float sin2 = (float)(Math.sin(Math.toRadians((double)rotToEnt[1])) * motionVal);
                     motionVal *= (dst - 0.5) / 3.5;
                     if (motionVal < 0.0) {
                        motionVal = 0.0;
                     }

                     float sin = (float)(Math.sin(Math.toRadians((double)rotToEnt[0])) * motionVal);
                     float cos = (float)(-Math.cos(Math.toRadians((double)rotToEnt[0])) * motionVal);
                     base.motionX += (double)sin;
                     base.motionY += (double)sin2;
                     base.motionZ += (double)cos;
                     base.isAirBorne = true;
                  }
               }
            }
         }
      }

      public double renderX(float pTicks) {
         return MathUtils.lerp(this.px, this.x, (double)pTicks);
      }

      public double renderY(float pTicks) {
         return MathUtils.lerp(this.py, this.y, (double)pTicks);
      }

      public double renderZ(float pTicks) {
         return MathUtils.lerp(this.pz, this.z, (double)pTicks);
      }

      public void drawModel(float pTicks) {
         ThrowPetard.this.glSetDrawing(true);
         Vec3d renderPos = new Vec3d(this.renderX(pTicks), this.renderY(pTicks), this.renderZ(pTicks));

         for (int i = 0; i < 3; i++) {
            ThrowPetard.this.nanoSparks
               .add(ThrowPetard.this.new NanoSpark(renderPos.addVector(0.0, 0.30000000447034836, 0.0), 0.0F, 0.1F, 150.0F, ColorUtils.getColor(255, 185, 25)));
         }

         float height = 0.3F;
         float width = 0.075F;
         GL11.glTranslated(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord);
         if (!this.wickIsTimeouted()) {
            int vecStep = 20;
            int colorCylinder = ColorUtils.getColor(67, 22, 20);
            int colorStopers = ColorUtils.getColor(30, 28, 44);
            RenderUtils.glColor(colorStopers);
            GL11.glBegin(6);
            GL11.glVertex3d(0.0, 0.0, 0.0);

            for (int i = 0; i <= 360; i += vecStep) {
               double xExt = Math.sin(Math.toRadians((double)i)) * (double)width;
               double zExt = -Math.cos(Math.toRadians((double)i)) * (double)width;
               GL11.glVertex3d(xExt, 0.0, zExt);
            }

            GL11.glEnd();
            RenderUtils.glColor(colorCylinder);
            GL11.glBegin(8);

            for (int i = 0; i <= 360; i += vecStep) {
               double xExt = Math.sin(Math.toRadians((double)i)) * (double)width;
               double zExt = -Math.cos(Math.toRadians((double)i)) * (double)width;
               GL11.glVertex3d(xExt, 0.0, zExt);
               GL11.glVertex3d(xExt, (double)height, zExt);
            }

            GL11.glEnd();
            RenderUtils.glColor(colorStopers);
            GL11.glBegin(6);
            GL11.glVertex3d(0.0, (double)height, 0.0);

            for (int i = 0; i <= 360; i += vecStep) {
               double xExt = Math.sin(Math.toRadians((double)i)) * (double)width;
               double zExt = -Math.cos(Math.toRadians((double)i)) * (double)width;
               GL11.glVertex3d(xExt, (double)height, zExt);
            }

            GL11.glEnd();
            GlStateManager.resetColor();
         }

         GL11.glTranslated(-renderPos.xCoord, -renderPos.yCoord, -renderPos.zCoord);
         ThrowPetard.this.glSetDrawing(false);
      }

      private boolean wickIsTimeouted() {
         return (float)(System.currentTimeMillis() - this.spawnTime) >= this.wickTimeout;
      }

      public boolean isToRemove() {
         return this.removeable;
      }
   }
}
