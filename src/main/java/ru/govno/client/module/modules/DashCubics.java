package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class DashCubics extends Module {
   public static DashCubics get;
   private final Random RAND = new Random(192372624L);
   private final int RES_PX = 16;
   private final int RUSH_TICKS = 14;
   private final int[] WHITE_YAWS = new int[]{0, 90, 180, 270};
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();
   private final List<DashCubics.DashCubic> DASH_CUBICS_LIST = new ArrayList<>();

   public DashCubics() {
      super("DashCubics", 0, Module.Category.RENDER);
      get = this;
   }

   @Override
   public boolean isBetaModule() {
      return true;
   }

   private int getRandWhiteYaw() {
      return this.WHITE_YAWS[this.RAND.nextInt(3)];
   }

   private int getRandJumpPX(int min, int max) {
      return (int)Math.min(MathUtils.lerp((float)min, (float)max + 0.5F, this.RAND.nextFloat()), (float)max);
   }

   private int getRandJumpPX() {
      return this.getRandJumpPX(2, 12);
   }

   private long getCubicAliveTime() {
      return 2000L;
   }

   private int getCubicColor(int index, float alphaPC) {
      return ClientColors.getColor1(index, alphaPC);
   }

   private List<BlockPos> getWhiteBlockPoses(EntityLivingBase base, double dstXZMin, double dstXZMax, int offsetDown) {
      List<BlockPos> tempPoses = new ArrayList<>();
      if (base != null && mc.world != null) {
         double xE = base.posX;
         double yE = base.posY + 1.0;
         double zE = base.posZ;

         for (double x = xE - dstXZMax; x < xE + dstXZMax; x++) {
            for (double z = zE - dstXZMax; z < zE + dstXZMax; z++) {
               for (double y = yE - (double)offsetDown; y < yE; y++) {
                  BlockPos tempPos = new BlockPos(x, y, z);
                  if (tempPos != null
                     && BlockUtils.canPlaceBlock(tempPos)
                     && !BlockUtils.canPlaceBlock(tempPos.down())
                     && tempPoses.stream().noneMatch(pos -> pos.equals(tempPos))) {
                     tempPoses.add(tempPos);
                  }
               }
            }
         }
      }

      return tempPoses;
   }

   private GenRBox findWhiteRBoxOnPos(BlockPos pos, Vec3d checkDstTo, double checkMinDST, double checkMaxDST) {
      if (DashCubics.mc.world != null) {
         int attemts = 128;
         while (--attemts > 0) {
            Vec3d vec = new Vec3d(pos).addVector(this.RAND.nextDouble(), 0.0, this.RAND.nextDouble());
            double dst = vec.distanceTo(checkDstTo);
            if (!(dst >= checkMinDST) || !(dst <= checkMaxDST)) continue;
            attemts = 0;
            return new GenRBox(vec, 16);
         }
      }
      return null;
   }

   private void set3dBoxRender(Runnable render, float lineWidth, boolean glShadeGradient, boolean bloom) {
      GL11.glEnable(3042);
      GL11.glEnable(2929);
      GL11.glDisable(2884);
      GL11.glDepthMask(false);
      GL11.glDisable(3553);
      GL11.glEnable(3008);
      GL11.glShadeModel(glShadeGradient ? 7425 : 7424);
      GL11.glBlendFunc(770, bloom ? '耄' : 771);
      GL11.glDisable(2896);
      mc.entityRenderer.disableLightmap();
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(lineWidth);
      GL11.glAlphaFunc(516, 0.003921569F);
      double x = RenderManager.viewerPosX;
      double y = RenderManager.viewerPosY - 0.001;
      double z = RenderManager.viewerPosZ;
      GL11.glPushMatrix();
      GL11.glTranslated(-x, -y, -z);
      render.run();
      GL11.glPopMatrix();
      GL11.glAlphaFunc(516, 0.1F);
      GL11.glLineWidth(1.0F);
      GL11.glHint(3154, 4352);
      GL11.glDisable(2848);
      if (bloom) {
         GL11.glBlendFunc(770, 771);
      }

      if (glShadeGradient) {
         GL11.glShadeModel(7424);
      }

      GL11.glEnable(3553);
      GL11.glDepthMask(true);
      GL11.glEnable(2884);
   }

   private void drawAxisBox(AxisAlignedBB aabb, int colorOut, int colorFill) {
      if (aabb != null && (ColorUtils.getAlphaFromColor(colorOut) != 0 || ColorUtils.getAlphaFromColor(colorFill) != 0)) {
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
         this.tessellator.draw();
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
         this.tessellator.draw();
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
         this.tessellator.draw();
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
         this.tessellator.draw();
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
         this.tessellator.draw();
         this.buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
         this.tessellator.draw();
         this.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.tessellator.draw();
         this.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.tessellator.draw();
         this.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.tessellator.draw();
         this.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.tessellator.draw();
         this.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.tessellator.draw();
         this.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
         this.buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
         this.tessellator.draw();
      }
   }

   private List<EntityLivingBase> currentEntities(double maxDistanceFromSelf) {
      EntityLivingBase entitySelf = (EntityLivingBase)(FreeCam.get.isActived() && FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player);
      return mc.world
         .getLoadedEntityList()
         .stream()
         .map(Entity::getPlayerOf)
         .filter(Objects::nonNull)
         .filter(Entity::isEntityAlive)
         .filter(base -> (double)base.getDistanceToEntity(entitySelf) <= maxDistanceFromSelf)
         .collect(Collectors.toList());
   }

   @Override
   public void onUpdate() {
      List<EntityLivingBase> entities = this.currentEntities(12.0);
      if (!entities.isEmpty()) {
         for (EntityLivingBase entity : entities) {
            Vec3d entityPos = entity.getPositionVector();
            List<DashCubics.GenRBox> tempGenRBoxesList = new ArrayList<>();
            tempGenRBoxesList.clear();

            for (BlockPos wPos : this.getWhiteBlockPoses(entity, 0.6F, 2.6F, 3)) {
               DashCubics.GenRBox box = this.findWhiteRBoxOnPos(wPos, entityPos, 0.6, 2.6);
               if (box != null) {
                  tempGenRBoxesList.add(box);
               }
            }

            if (tempGenRBoxesList.isEmpty()) {
               break;
            }

            DashCubics.GenRBox box = tempGenRBoxesList.get(
               (int)MathUtils.clamp((float)(tempGenRBoxesList.size() - 1) * this.RAND.nextFloat(), 0.0F, (float)(tempGenRBoxesList.size() - 1))
            );
            if (box != null) {
               this.DASH_CUBICS_LIST.add(new DashCubics.DashCubic(box, this.getCubicAliveTime(), entity.getEntityId(), 6.0));
            }
         }

         this.DASH_CUBICS_LIST.forEach(DashCubics.DashCubic::update);
      }
   }

   @Override
   public void alwaysRender3D(float partialTicks) {
      float alphaPC;
      if ((!((alphaPC = this.stateAnim.getAnim()) < 0.003921569F) || this.actived) && !this.DASH_CUBICS_LIST.isEmpty()) {
         this.stateAnim.to = this.actived && !Panic.stop ? 1.0F : 0.0F;
         Frustum frustum = new Frustum(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
         if (frustum != null) {
            this.DASH_CUBICS_LIST.removeIf(DashCubics.DashCubic::isToRemove);
            List<DashCubics.DashCubic> filteredToDraw = this.DASH_CUBICS_LIST
               .stream()
               .filter(cubic -> cubic.box.aabb != null && frustum.isBoundingBoxInFrustum(cubic.box.aabb))
               .collect(Collectors.toList());
            if (!filteredToDraw.isEmpty()) {
               this.set3dBoxRender(() -> filteredToDraw.forEach(cubic -> cubic.drawBox(partialTicks, alphaPC)), 0.025F, false, true);
            }
         }
      }
   }

   private class DashCubic {
      private final DashCubics.GenRBox box;
      private final AnimationUtils alphaPC = new AnimationUtils(0.1F, 1.0F, 0.05F);
      private final long aliveTimeout;
      private final long spawnTime = System.currentTimeMillis();
      private final int entityID;
      private final int yawIndexed;
      private int jumpTicksMax;
      private int jumpTicks;
      private int prevJumpTicks;
      private int jumpYaw;
      private double jumpHeight;
      private double jumpProgress;
      private final double maxDistanceAtEntity;

      public DashCubic(DashCubics.GenRBox box, long aliveTimeout, int entityInID, double maxDistanceAtEntity) {
         this.box = box;
         this.aliveTimeout = aliveTimeout;
         this.entityID = entityInID;
         EntityLivingBase entity = this.getEntity();
         this.yawIndexed = entity != null && box != null
            ? (int)RotationUtil.getFacePosRemote(entity.getPositionVector(), box.centerVec)[0]
            : DashCubics.this.RAND.nextInt(360);
         this.maxDistanceAtEntity = maxDistanceAtEntity;
      }

      public void jump() {
         if (this.jumpTicks <= 0) {
            this.jumpTicksMax = this.jumpTicks = (int)(14.0F * (0.5F + 0.5F * DashCubics.this.RAND.nextFloat()));
            this.jumpHeight = (double)((float)DashCubics.this.getRandJumpPX() * 0.0625F);
            this.jumpYaw = DashCubics.this.getRandWhiteYaw();
         }
      }

      private boolean updateJumpsIsFinalTick() {
         if (this.jumpTicks > 0) {
            this.jumpTicks--;
         }

         this.jumpProgress = (double)((float)this.jumpTicks / (float)this.jumpTicksMax);
         return this.jumpTicks == 0 && this.prevJumpTicks == 1;
      }

      private void postUpdateJumps() {
         this.prevJumpTicks = this.jumpTicks;
      }

      private double getJumpYOffset(float partialTicks) {
         float gac = MathUtils.lerp((float)this.prevJumpTicks, (float)this.jumpTicks, partialTicks) / (float)this.jumpTicksMax;
         gac = (double)gac > 0.5 ? 1.0F - gac : gac;
         double val = this.jumpTicks != 0 && this.prevJumpTicks != 0
            ? MathUtils.lerp(
               MathUtils.easeInOutQuadWave((double)(MathUtils.lerp((float)this.prevJumpTicks, (float)this.jumpTicks, partialTicks) / (float)this.jumpTicksMax))
                  * this.jumpHeight,
               (double)gac * this.jumpHeight,
               0.75
            )
            : 0.0;
         return Double.isNaN(val) ? 0.0 : val;
      }

      private double getJumpProgress() {
         return this.jumpProgress;
      }

      private EntityLivingBase getEntity() {
         Entity entityIn = Module.mc.world == null ? null : Module.mc.world.getEntityByID(this.entityID);
         return entityIn == null ? null : entityIn.getLivingBaseOf();
      }

      public void update() {
         if (this.box != null) {
            this.postUpdateJumps();
            boolean jumpFinalled = this.updateJumpsIsFinalTick();
            EntityLivingBase entity = this.getEntity();
            if (entity != null
               && entity.onGround
               && entity.posY - entity.lastTickPosY < -0.1
               && MathUtils.getDifferenceOf(this.box.aabb.minY, entity.posY) < 0.2F
               && DashCubics.this.RAND.nextInt(100) > 0) {
               this.jump();
            }
         }
      }

      private float getAlphaPC(boolean update) {
         return update ? this.alphaPC.getAnim() : this.alphaPC.anim;
      }

      public void drawBox(float partialTicks, float alphaPC) {
         if ((alphaPC = alphaPC * this.getAlphaPC(false)) != 0.0F) {
            if ((double)alphaPC > 0.97) {
               alphaPC = 1.0F;
            }

            int color = DashCubics.this.getCubicColor(this.yawIndexed, alphaPC);
            AxisAlignedBB axisAlignedBB = this.box.aabb;
            double jumpProgress = (double)(MathUtils.lerp((float)this.prevJumpTicks, (float)this.jumpTicks, partialTicks) / (float)this.jumpTicksMax);
            double jumpOffset = this.getJumpYOffset(partialTicks);
            double tx = axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) / 2.0;
            double ty = axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) / 2.0;
            double tz = axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2.0;
            if (jumpOffset != 0.0) {
               GL11.glPushMatrix();
               GL11.glTranslated(0.0, jumpOffset, 0.0);
            }

            if (alphaPC != 1.0F) {
               double sizeXZ = (double)alphaPC;
               double sizeY = (double)(alphaPC * alphaPC);
               GL11.glPushMatrix();
               GL11.glTranslated(tx, ty - (axisAlignedBB.maxY - axisAlignedBB.minY) / 2.0, tz);
               GL11.glScaled(sizeXZ, sizeY, sizeXZ);
               GL11.glTranslated(-tx, -ty + (axisAlignedBB.maxY - axisAlignedBB.minY) / 2.0, -tz);
            }

            if (jumpProgress != 0.0 && this.jumpTicks != 0) {
               double pitch = (double)(90 * (int)(this.jumpHeight / 0.0625 / 4.0))
                  * MathUtils.easeInOutQuad(jumpProgress - (double)((float)(this.jumpTicks - this.prevJumpTicks) * partialTicks / (float)this.jumpTicksMax));
               GL11.glPushMatrix();
               GL11.glTranslated(tx, ty, tz);
               GL11.glRotated((double)this.jumpYaw, 0.0, -1.0, 0.0);
               GL11.glRotated(pitch, -1.0, 0.0, 0.0);
               GL11.glTranslated(-tx, -ty, -tz);
            }

            DashCubics.this.drawAxisBox(
               axisAlignedBB,
               ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 4.0F),
               ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 12.0F)
            );
            if (jumpProgress != 0.0 && this.jumpTicks != 0) {
               GL11.glPopMatrix();
            }

            if (alphaPC != 1.0F) {
               GL11.glPopMatrix();
            }

            if (jumpOffset != 0.0) {
               GL11.glPopMatrix();
            }
         }
      }

      public float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.spawnTime) / (float)this.aliveTimeout, 0.0F, 1.0F);
      }

      public boolean isToRemove() {
         if (this.alphaPC.to != 0.0F) {
            if (this.getTimePC() == 1.0F) {
               this.alphaPC.to = 0.0F;
            } else {
               EntityLivingBase entity = this.getEntity();
               if (entity == null || entity.getDistanceToVec3d(this.box.centerVec) > this.maxDistanceAtEntity) {
                  this.alphaPC.to = 0.0F;
               }
            }
         }

         return this.box == null || this.getAlphaPC(true) < 0.003921569F && this.alphaPC.to == 0.0F;
      }
   }

   private class GenRBox {
      AxisAlignedBB aabb;
      Vec3d centerVec;

      public GenRBox(Vec3d vec, int RES_PX) {
         double x = vec.xCoord;
         double y = vec.yCoord;
         double z = vec.zCoord;
         double resOff = (double)(1.0F / (float)RES_PX);
         x = (double)((float)((int)(x * (double)RES_PX)) / (float)RES_PX);
         y = (double)((float)((int)(y * (double)RES_PX)) / (float)RES_PX);
         z = (double)((float)((int)(z * (double)RES_PX)) / (float)RES_PX);
         this.aabb = new AxisAlignedBB(x, y, z, x + resOff, y + resOff, z + resOff);
         double var15;
         this.centerVec = vec.addVector(var15 = resOff / 2.0, var15, var15);
      }
   }
}
