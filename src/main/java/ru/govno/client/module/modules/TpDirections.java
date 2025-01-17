package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class TpDirections extends Module {
   public static TpDirections get;
   private final ArrayList<TpDirections.DirPoint> DIR_POINTS_LIST = new ArrayList<>();
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();
   private final ResourceLocation DIR_POINT_OBJECT_TEX = new ResourceLocation("vegaline/modules/tpdirections/tpdirarrowbase.png");
   private final ResourceLocation DIR_POINT_EFFECT_TEX = new ResourceLocation("vegaline/modules/tpdirections/tpdirarrowoverlay.png");
   private final ArrayList<TpDirections.EntityPosInfo> ENTITIES_POS_INFO_LIST = new ArrayList<>();

   public TpDirections() {
      super("TpDirections", 0, Module.Category.RENDER);
      get = this;
   }

   private float getMaxPointerTime() {
      return 6000.0F;
   }

   private float getMaxPointerScale() {
      return 0.75F;
   }

   private int getPointerEffectsTickInterval() {
      return 7;
   }

   private double getPointersPosStep() {
      return 0.75;
   }

   private int getMaxPointersCountSametime() {
      return 160;
   }

   private int[] getPointerColors(TpDirections.DirPoint point, int index, float alphaPC) {
      float aPC = point.getAlphaPC() * alphaPC;
      int c1 = ClientColors.getColor1(index, aPC);
      int c2 = ClientColors.getColor1(90 + index, aPC);
      return new int[]{c2, c1, c1, c2};
   }

   private float[] calculateRadiansOfPoses(Vec3d mainPos, Vec3d altPos) {
      double dx = altPos.xCoord - mainPos.xCoord;
      double dy = altPos.yCoord - mainPos.yCoord;
      double dz = altPos.zCoord - mainPos.zCoord;
      return new float[]{
         (float)Math.atan2(dz, dx) * 180.0F / (float) Math.PI + 90.0F,
         (float)(-(Math.atan2(dy, (double)MathHelper.sqrt_double(dx * dx + dz * dz)) / Math.PI * 180.0 - 180.0))
      };
   }

   private double calculateDistanceAsVecs(Vec3d first, Vec3d second, boolean allowY) {
      double dx = first.xCoord - second.xCoord;
      double dy = allowY ? first.yCoord - second.yCoord : 0.0;
      double dz = first.zCoord - second.zCoord;
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private void drawAllDirPoints(boolean isRenderingEffect, float size, boolean bloomEffect) {
      if (!this.DIR_POINTS_LIST.isEmpty()) {
         ResourceLocation loc = isRenderingEffect ? this.DIR_POINT_EFFECT_TEX : this.DIR_POINT_OBJECT_TEX;
         this.startDraws3D(loc, bloomEffect);
         this.DIR_POINTS_LIST.forEach(point -> point.draw(isRenderingEffect, size));
         this.stopDraws3D(loc, bloomEffect);
      }
   }

   private void addDirPoint(Vec3d[] posVectors, float maxTime) {
      int countMax = (int)MathUtils.clamp(posVectors[0].distanceTo(posVectors[1]) / this.getPointersPosStep(), 2.0, (double)this.getMaxPointersCountSametime());

      for (int countValue = 0; countValue < countMax; countValue++) {
         if (countValue != 0) {
            float posProgressPCNormal = MathUtils.clamp((float)countValue / (float)countMax, 0.0F, 1.0F);
            float posProgressPCFuture = MathUtils.clamp((float)countValue / (float)(countMax + 1), 0.0F, 2.0F);
            Vec3d vecPositionNormal = BlockUtils.getOverallVec3d(posVectors[0], posVectors[1], 1.0F - posProgressPCNormal);
            Vec3d vecPositionFuture = BlockUtils.getOverallVec3d(posVectors[0], posVectors[1], 1.0F - posProgressPCFuture);
            this.DIR_POINTS_LIST
               .add(
                  new TpDirections.DirPoint(
                     vecPositionNormal, vecPositionFuture, posVectors[0], posVectors[1], maxTime * (0.75F + 0.25F * posProgressPCNormal), countValue * 90
                  )
               );
         }
      }
   }

   private void dirPointsRemoveAuto() {
      if (!this.DIR_POINTS_LIST.isEmpty()) {
         this.DIR_POINTS_LIST.removeIf(TpDirections.DirPoint::isToRemove);
      }
   }

   private void dirPointsUpdate(boolean isRenderThread) {
      if (!this.DIR_POINTS_LIST.isEmpty()) {
         this.DIR_POINTS_LIST.forEach(point -> point.updatePointer(isRenderThread));
      }
   }

   private void startDraws3D(ResourceLocation resourceLoc, boolean seperateOne) {
      boolean hasTexture = resourceLoc != null;
      RenderManager manager = mc.getRenderManager();
      EntityRenderer renderer = mc.entityRenderer;
      Vec3d revert = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
      boolean light = GL11.glIsEnabled(2896);
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      GL11.glDepthMask(false);
      GL11.glDisable(2884);
      if (light) {
         GL11.glDisable(2896);
      }

      GL11.glShadeModel(7425);
      GL11.glBlendFunc(770, seperateOne ? 1 : 771);
      renderer.disableLightmap();
      if (hasTexture) {
         GL11.glEnable(3553);
         GL11.glTranslated(-revert.xCoord, -revert.yCoord, -revert.zCoord);
      } else {
         GL11.glDisable(3553);
      }

      mc.getTextureManager().bindTexture(resourceLoc);
   }

   private void stopDraws3D(ResourceLocation resourceLoc, boolean seperateOne) {
      boolean hasTexture = resourceLoc != null;
      RenderManager manager = mc.getRenderManager();
      Vec3d revert = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
      boolean light = GL11.glIsEnabled(2896);
      GL11.glTranslated(revert.xCoord, revert.yCoord, revert.zCoord);
      if (hasTexture) {
         GL11.glDisable(3553);
      }

      if (seperateOne) {
         GL11.glBlendFunc(770, 771);
      }

      GL11.glColor3f(1.0F, 1.0F, 1.0F);
      GL11.glShadeModel(7424);
      if (light) {
         GL11.glEnable(2896);
      }

      GL11.glEnable(2884);
      GL11.glDepthMask(true);
      GL11.glEnable(3008);
      RenderUtils.resetBlender();
      GL11.glPopMatrix();
   }

   private void drawCentredTextureIn3d(Vec3d vecIn, float scale, int color1, int color2, int color3, int color4, float[] angleRotates) {
      this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(color1).endVertex();
      this.buffer.pos(0.0, (double)scale, 0.0).tex(0.0, 1.0).color(color2).endVertex();
      this.buffer.pos((double)scale, (double)scale, 0.0).tex(1.0, 1.0).color(color3).endVertex();
      this.buffer.pos((double)scale, 0.0, 0.0).tex(1.0, 0.0).color(color4).endVertex();
      GL11.glTranslated(vecIn.xCoord - (double)scale / 2.0, vecIn.yCoord, vecIn.zCoord - (double)scale / 2.0);
      GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
      this.tessellator.draw();
      GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
      GL11.glTranslated(-(vecIn.xCoord - (double)scale / 2.0), -vecIn.yCoord, -(vecIn.zCoord - (double)scale / 2.0));
   }

   private void drawCentredTextureIn3d(Vec3d vecIn, float scale, int color1, float[] angleRotates) {
      this.drawCentredTextureIn3d(vecIn, scale, color1, color1, color1, color1, angleRotates);
   }

   private boolean isValidEntity(Entity entity, boolean self, boolean players, boolean mobs, int ticksAliveMin) {
      return entity != null
         && (
            self && entity instanceof EntityPlayerSP
               || players && entity instanceof EntityOtherPlayerMP
               || mobs && entity instanceof EntityLivingBase && !(entity instanceof EntityPlayerSP) && !(entity instanceof EntityOtherPlayerMP)
         )
         && entity.ticksExisted >= ticksAliveMin;
   }

   private double vanillaPosChangeRuleValue() {
      return 9.953;
   }

   private void entityPosInfoRemoveAuto() {
      this.ENTITIES_POS_INFO_LIST
         .removeIf(
            info -> !mc.world.getLoadedEntityList().stream().anyMatch(entity -> info.getEntity() == entity)
                  || !this.isValidEntity(info.getEntity(), true, true, true, 0)
         );
   }

   private Vec3d[] getVectorsAsEntityPosInfo(TpDirections.EntityPosInfo info) {
      return new Vec3d[]{new Vec3d(info.getPosX(), info.getPosY(), info.getPosZ()), new Vec3d(info.getPrevPosX(), info.getPrevPosY(), info.getPrevPosZ())};
   }

   @Override
   public void onUpdate() {
      boolean self = true;
      boolean players = true;
      boolean mobs = false;
      int minAliveTicksEntity = 8;
      List<Entity> entities = mc.world
         .getLoadedEntityList()
         .stream()
         .filter(entityx -> this.isValidEntity(entityx, true, true, false, 8))
         .collect(Collectors.toList());
      this.dirPointsUpdate(false);
      this.entityPosInfoRemoveAuto();

      for (Entity entity : entities) {
         if (!this.ENTITIES_POS_INFO_LIST.stream().anyMatch(info -> info.getEntity().getEntityId() == entity.getEntityId())) {
            this.ENTITIES_POS_INFO_LIST.add(new TpDirections.EntityPosInfo(entity));
         }
      }

      if (!this.ENTITIES_POS_INFO_LIST.isEmpty()) {
         this.ENTITIES_POS_INFO_LIST.forEach(info -> info.updatePosition());
      }

      double maxVanillaSpeed = this.vanillaPosChangeRuleValue();
      float maxDirPointAliveTime = this.getMaxPointerTime();
      this.ENTITIES_POS_INFO_LIST.forEach(info -> {
         Vec3d[] posVectors = this.getVectorsAsEntityPosInfo(info);
         if (this.calculateDistanceAsVecs(posVectors[0], posVectors[1], false) > maxVanillaSpeed) {
            this.addDirPoint(posVectors, maxDirPointAliveTime);
         }
      });
   }

   @Override
   public void alwaysRender3D() {
      if (this.actived) {
         this.dirPointsRemoveAuto();
         this.dirPointsUpdate(true);
         float expandEffectMax = this.getMaxPointerScale();
         this.drawAllDirPoints(false, expandEffectMax, true);
         this.drawAllDirPoints(true, expandEffectMax, true);
      }
   }

   private class DirPoint {
      ArrayList<TpDirections.DirPointEffect> DIR_POINTER_EFFECTS_LIST = new ArrayList<>();
      Vec3d pos;
      Vec3d prevPos;
      Vec3d ADDprevPos;
      Vec3d ADDPos;
      AnimationUtils scalePC = new AnimationUtils(0.0F, 1.25F, 0.015F);
      long startTime = System.currentTimeMillis();
      float maxTime;
      float yaw;
      float pitch;
      boolean toRemove;
      int ticksAlive;
      int index;

      void addDirPointEffect(TpDirections.DirPoint point) {
         this.DIR_POINTER_EFFECTS_LIST.add(TpDirections.this.new DirPointEffect(point));
      }

      void dirPointEffetsRemoveAuto() {
         if (!this.DIR_POINTER_EFFECTS_LIST.isEmpty()) {
            this.DIR_POINTER_EFFECTS_LIST.removeIf(TpDirections.DirPointEffect::isToRemove);
         }
      }

      DirPoint(Vec3d pos, Vec3d prevPos, Vec3d ADDprevPos, Vec3d ADDPos, float maxTime, int index) {
         this.pos = pos;
         this.prevPos = prevPos;
         this.maxTime = maxTime;
         this.ADDprevPos = ADDprevPos;
         this.ADDPos = ADDPos;
         float[] radians = TpDirections.this.calculateRadiansOfPoses(this.ADDprevPos, this.ADDPos);
         this.yaw = radians[0];
         this.pitch = radians[1];
         this.index = index;
      }

      Vec3d getPos() {
         return this.pos;
      }

      Vec3d getPrevPos() {
         return this.prevPos;
      }

      float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      float getScalePC(boolean getClamped) {
         return getClamped ? MathUtils.clamp(this.scalePC.getAnim(), 0.0F, 1.0F) : this.scalePC.getAnim();
      }

      float getAlphaPC() {
         return this.getScalePC(true);
      }

      void updatePointer(boolean isRenderThread) {
         if (isRenderThread) {
            if (this.scalePC.getAnim() >= 1.17F) {
               this.scalePC.to = 1.0F;
            }

            if (this.getTimePC() == 1.0F) {
               this.scalePC.to = 0.0F;
               if (this.scalePC.getAnim() < 0.01F) {
                  this.scalePC.setAnim(0.0F);
                  if (this.scalePC.getAnim() == 0.0F) {
                     this.toRemove = true;
                  }
               }
            }
         } else {
            this.DIR_POINTER_EFFECTS_LIST.forEach(effect -> effect.updateEffect());
            if (this.canPutEffect(++this.ticksAlive)) {
               this.putEffect();
            }

            this.dirPointEffetsRemoveAuto();
         }
      }

      boolean canPutEffect(int ofTicks) {
         int interval = TpDirections.this.getPointerEffectsTickInterval();
         interval = interval < 1 ? 1 : interval;
         return ofTicks % interval == interval - 1;
      }

      void putEffect() {
         this.addDirPointEffect(this);
      }

      boolean isToRemove() {
         return this.toRemove;
      }

      float[] getRadians() {
         return new float[]{this.yaw, this.pitch};
      }

      void draw(boolean isRenderingEffect, float size) {
         Vec3d renderindPos = this.getPrevPos().addVector(0.0, 0.01F, 0.0);
         float[] radians = this.getRadians();
         float allScale = MathUtils.clamp(this.getScalePC(false), this.scalePC.to == 0.0F ? 0.6F : 0.0F, 1.2F);
         if (!isRenderingEffect) {
            GL11.glPushMatrix();
            float alphaPC = this.getAlphaPC();
            GL11.glTranslated(renderindPos.xCoord, renderindPos.yCoord, renderindPos.zCoord);
            GL11.glRotated((double)radians[0], 0.0, -1.0, 0.0);
            GL11.glRotated((double)radians[1], -1.0, 0.0, 0.0);
            GL11.glScaled((double)allScale, 1.0, (double)allScale);
            GL11.glTranslated(-renderindPos.xCoord, -renderindPos.yCoord, -renderindPos.zCoord);
            int[] colors = TpDirections.this.getPointerColors(this, this.index, alphaPC);
            TpDirections.this.drawCentredTextureIn3d(renderindPos, size, colors[0], colors[1], colors[2], colors[3], radians);
            GL11.glPopMatrix();
         } else if (!this.DIR_POINTER_EFFECTS_LIST.isEmpty()) {
            for (TpDirections.DirPointEffect pointEffect : this.DIR_POINTER_EFFECTS_LIST) {
               float progressAnim = pointEffect.getProgress();
               float alphaPC = this.getAlphaPC() * pointEffect.getAlphaPC() * progressAnim * allScale * allScale;
               int[] colors = TpDirections.this.getPointerColors(this, this.index, alphaPC);
               int c1 = colors[0];
               int c2 = colors[1];
               float offsetOfAnimEffect = size / 6.0F * -progressAnim;
               GL11.glPushMatrix();
               GL11.glTranslated(renderindPos.xCoord, renderindPos.yCoord, renderindPos.zCoord);
               GL11.glRotated((double)radians[0], 0.0, -1.0, 0.0);
               GL11.glRotated((double)radians[1], -1.0, 0.0, 0.0);
               GL11.glTranslated(0.0, 0.0, (double)offsetOfAnimEffect);
               GL11.glScaled((double)allScale, 1.0, (double)allScale);
               GL11.glTranslated(-renderindPos.xCoord, -renderindPos.yCoord, -renderindPos.zCoord);
               TpDirections.this.drawCentredTextureIn3d(renderindPos, size, c1, c2, c2, c1, radians);
               GL11.glPopMatrix();
            }
         }
      }
   }

   private class DirPointEffect {
      TpDirections.DirPoint dirPoint;
      AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.055F);
      AnimationUtils progressAnim = new AnimationUtils(0.0F, 1.0F, 0.025F);
      boolean toRemove;

      DirPointEffect(TpDirections.DirPoint dirPoint) {
         this.dirPoint = dirPoint;
      }

      void updateEffect() {
         float alphaPCGA = this.alphaPC.getAnim();
         if (alphaPCGA > 0.99F && this.alphaPC.to != 0.0F) {
            this.alphaPC.setAnim(1.0F);
            this.alphaPC.to = 0.0F;
         }

         if (this.alphaPC.to == 0.0F && alphaPCGA < 0.01F && alphaPCGA > 0.0F) {
            this.alphaPC.setAnim(0.0F);
         }

         if (alphaPCGA == 0.0F) {
            this.toRemove = true;
         }
      }

      boolean isToRemove() {
         return this.toRemove;
      }

      float getAlphaPC() {
         return this.alphaPC.getAnim();
      }

      float getProgress() {
         return this.progressAnim.getAnim();
      }
   }

   private class EntityPosInfo {
      Entity entity;
      double posX;
      double posY;
      double posZ;
      double prevPosX;
      double prevPosY;
      double prevPosZ;

      EntityPosInfo(Entity entity) {
         this.entity = entity;
         this.posX = this.entity.posX;
         this.posY = this.entity.posY;
         this.posZ = this.entity.posZ;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
      }

      void updatePosition() {
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.posX = this.entity.posX;
         this.posY = this.entity.posY;
         this.posZ = this.entity.posZ;
      }

      Entity getEntity() {
         return this.entity;
      }

      double getPosX() {
         return this.posX;
      }

      double getPosY() {
         return this.posY;
      }

      double getPosZ() {
         return this.posZ;
      }

      double getPrevPosX() {
         return this.prevPosX;
      }

      double getPrevPosY() {
         return this.prevPosY;
      }

      double getPrevPosZ() {
         return this.prevPosZ;
      }
   }
}
