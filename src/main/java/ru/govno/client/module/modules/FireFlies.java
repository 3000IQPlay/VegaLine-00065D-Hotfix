package ru.govno.client.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class FireFlies extends Module {
   ModeSettings ColorMode;
   FloatSettings SpawnDelay;
   ColorSettings PickColor;
   BoolSettings DarkImprint;
   BoolSettings Lighting;
   private final ArrayList<FireFlies.FirePart> FIRE_PARTS_LIST = new ArrayList<>();
   private final ResourceLocation FIRE_PART_TEX = new ResourceLocation("vegaline/modules/fireflies/firepart.png");
   private final ResourceLocation BLOOM_TEX = new ResourceLocation("vegaline/modules/fireflies/bloom.png");
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();

   public FireFlies() {
      super("FireFlies", 0, Module.Category.RENDER);
      this.settings.add(this.ColorMode = new ModeSettings("ColorMode", "RandomPalette", this, new String[]{"RandomPalette", "Custom", "Client"}));
      this.settings
         .add(
            this.PickColor = new ColorSettings("PickColor", ColorUtils.getColor(255, 70, 0), this, () -> this.ColorMode.currentMode.equalsIgnoreCase("Custom"))
         );
      this.settings.add(this.DarkImprint = new BoolSettings("DarkImprint", false, this));
      this.settings.add(this.Lighting = new BoolSettings("Lighting", false, this));
      this.settings.add(this.SpawnDelay = new FloatSettings("SpawnDelay", 3.0F, 10.0F, 1.0F, this));
   }

   private long getMaxPartAliveTime() {
      return 5500L;
   }

   private int getPartColor() {
      int color = this.PickColor.color;
      if (this.ColorMode.currentMode.equalsIgnoreCase("RandomPalette")) {
         color = Color.getHSBColor((float)Math.random(), 1.0F, 1.0F).getRGB();
      } else if (this.ColorMode.currentMode.equalsIgnoreCase("Client")) {
         color = ClientColors.getColor1();
      }

      return color;
   }

   private boolean canBeDraw(FireFlies.FirePart flie, float pTicks, Frustum frustum) {
      boolean fAxIn = false;
      boolean sAxIn = false;
      if (flie != null) {
         fAxIn = frustum.isBoundingBoxInFrustum(
            new AxisAlignedBB(
                  flie.prevPos
                     .addVector(
                        (flie.pos.xCoord - flie.prevPos.xCoord) * (double)pTicks,
                        (flie.pos.yCoord - flie.prevPos.yCoord) * (double)pTicks,
                        (flie.pos.zCoord - flie.prevPos.zCoord) * (double)pTicks
                     )
               )
               .expandXyz(2.0)
         );
         FireFlies.TrailPart tPart;
         if (!flie.TRAIL_PARTS.isEmpty() && (tPart = flie.TRAIL_PARTS.get(0)) != null) {
            sAxIn = frustum.isBoundingBoxInFrustum(new AxisAlignedBB(tPart.x, tPart.y, tPart.z).expandXyz(0.1));
         }
      }

      return fAxIn || sAxIn;
   }

   private float getRandom(double min, double max) {
      return (float)MathUtils.getRandomInRange(min, max);
   }

   private Vec3d generateVecForPart(double rangeXZ, double rangeY) {
      Vec3d pos = Minecraft.player
         .getPositionVector()
         .addVector((double)this.getRandom(-rangeXZ, rangeXZ), (double)this.getRandom(-rangeY / 2.0, rangeY), (double)this.getRandom(-rangeXZ, rangeXZ));

      for (int i = 0; i < 30; i++) {
         if (Speed.posBlock(pos.xCoord, pos.yCoord, pos.zCoord) || Minecraft.player.getDistanceAtEyeXZ(pos.xCoord, pos.zCoord) < rangeXZ / 3.0) {
            pos = Minecraft.player
               .getPositionVector()
               .addVector((double)this.getRandom(-rangeXZ, rangeXZ), (double)this.getRandom(-rangeY / 2.0, rangeY), (double)this.getRandom(-rangeXZ, rangeXZ));
         }
      }

      return pos;
   }

   private void setupGLDrawsFireParts(Runnable partsRender, boolean tex) {
      double glX = RenderManager.viewerPosX;
      double glY = RenderManager.viewerPosY;
      double glZ = RenderManager.viewerPosZ;
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      mc.entityRenderer.disableLightmap();
      GL11.glAlphaFunc(516, 0.003921569F);
      GL11.glLineWidth(1.0F);
      if (tex) {
         GL11.glEnable(3553);
      } else {
         GL11.glDisable(3553);
      }

      GL11.glDisable(2896);
      GL11.glShadeModel(7425);
      GL11.glDisable(3008);
      GL11.glDisable(2884);
      GL11.glDepthMask(false);
      GL11.glTranslated(-glX, -glY, -glZ);
      partsRender.run();
      GL11.glTranslated(glX, glY, glZ);
      GL11.glDepthMask(true);
      GL11.glEnable(2884);
      GL11.glAlphaFunc(516, 0.1F);
      GL11.glLineWidth(1.0F);
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      GlStateManager.resetColor();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GL11.glPopMatrix();
   }

   private void bindResource(ResourceLocation toBind) {
      mc.getTextureManager().bindTexture(toBind);
   }

   private void drawBindedTexture(float x, float y, float x2, float y2, int c, int c2, int c3, int c4) {
      this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.buffer.pos((double)x, (double)y).tex(0.0, 0.0).color(c).endVertex();
      this.buffer.pos((double)x, (double)y2).tex(0.0, 1.0).color(c).endVertex();
      this.buffer.pos((double)x2, (double)y2).tex(1.0, 1.0).color(c).endVertex();
      this.buffer.pos((double)x2, (double)y).tex(1.0, 0.0).color(c).endVertex();
      this.tessellator.draw();
   }

   private void drawBindedTexture(float x, float y, float x2, float y2, int c) {
      this.drawBindedTexture(x, y, x2, y2, c, c, c, c);
   }

   private void drawPart(FireFlies.FirePart part, float pTicks, float alphaPC) {
      int color = ColorUtils.swapAlpha(part.color, (float)ColorUtils.getAlphaFromColor(part.color) * part.getAlphaPC() * alphaPC);
      alphaPC *= part.getAlphaPC();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      this.drawTrailPartsList(this.DarkImprint.getBool() ? ColorUtils.toDark(color, 0.3F) : color, part, alphaPC);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      this.drawSparkPartsList(color, part, alphaPC, pTicks);
      Vec3d pos = part.getRenderPosVec(pTicks);
      GL11.glPushMatrix();
      GL11.glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
      GL11.glNormal3d(1.0, 1.0, 1.0);
      GL11.glRotated((double)(mc.getRenderManager().playerViewY + WorldRender.get.offYawOrient), 0.0, -1.0, 0.0);
      GL11.glRotated((double)(mc.getRenderManager().playerViewX + WorldRender.get.offPitchOrient), mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0, 0.0, 0.0);
      GL11.glScaled(-0.1, -0.1, 0.1);
      float scale = 2.5F * alphaPC;
      this.drawBindedTexture(
         -scale / 2.0F,
         -scale / 2.0F,
         scale / 2.0F,
         scale / 2.0F,
         ColorUtils.getOverallColorFrom(
            color,
            ColorUtils.swapAlpha(-1, (float)ColorUtils.getAlphaFromColor(color)),
            (float)ColorUtils.getAlphaFromColor(color) / 255.0F * ColorUtils.getBrightnessFromColor(color) * 0.5F
         )
      );
      if (this.Lighting.getBool()) {
         this.bindResource(this.BLOOM_TEX);
         color = ColorUtils.getOverallColorFrom(
            color,
            ColorUtils.swapAlpha(-1, (float)ColorUtils.getAlphaFromColor(color)),
            (float)ColorUtils.getAlphaFromColor(color) / 255.0F * ColorUtils.getBrightnessFromColor(color) * 0.3F
         );
         scale *= 16.0F;
         this.drawBindedTexture(
            -scale / 2.0F, -scale / 2.0F, scale / 2.0F, scale / 2.0F, ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) / 18.0F)
         );
      }

      GL11.glPopMatrix();
   }

   @Override
   public void onUpdate() {
      if (Minecraft.player != null && Minecraft.player.ticksExisted == 1) {
         this.FIRE_PARTS_LIST.forEach(part -> part.setToRemove());
      }

      this.FIRE_PARTS_LIST.forEach(FireFlies.FirePart::updatePart);
      this.FIRE_PARTS_LIST.removeIf(FireFlies.FirePart::isToRemove);
      if (Minecraft.player.ticksExisted % (this.SpawnDelay.getInt() + 1) == 0) {
         int color = this.getPartColor();
         this.FIRE_PARTS_LIST.add(new FireFlies.FirePart(this.generateVecForPart(10.0, 4.0), (float)this.getMaxPartAliveTime(), color));
         this.FIRE_PARTS_LIST.add(new FireFlies.FirePart(this.generateVecForPart(6.0, 5.0), (float)this.getMaxPartAliveTime(), color));
      }
   }

   @Override
   public void alwaysRender3D(float partialTicks) {
      if (!this.FIRE_PARTS_LIST.isEmpty() && this.stateAnim.getAnim() > 0.003921569F) {
         Frustum frustum = new Frustum(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
         List<FireFlies.FirePart> fireParts = this.FIRE_PARTS_LIST
            .stream()
            .filter(part -> this.canBeDraw(part, partialTicks, frustum))
            .collect(Collectors.toList());
         if (fireParts.isEmpty()) {
            return;
         }

         this.setupGLDrawsFireParts(() -> fireParts.forEach(part -> {
               this.bindResource(this.FIRE_PART_TEX);
               this.drawPart(part, partialTicks, this.stateAnim.getAnim());
            }), true);
      }
   }

   @Override
   public void onToggled(boolean actived) {
      this.stateAnim.to = actived ? 1.0F : 0.0F;
      super.onToggled(actived);
   }

   private void drawSparkPartsList(int color, FireFlies.FirePart firePart, float alphaPC, float partialTicks) {
      if (firePart.SPARK_PARTS.size() >= 2) {
         GL11.glDisable(3553);
         GL11.glEnable(3042);
         GL11.glDisable(3008);
         GL11.glEnable(2832);
         float pointsScale = 0.25F
            + 2.5F
               * MathUtils.clamp(
                  1.0F
                     - (
                           Minecraft.player
                                 .getSmoothDistanceToCoord(
                                    (float)firePart.getPosVec().xCoord, (float)firePart.getPosVec().yCoord + 1.6F, (float)firePart.getPosVec().zCoord
                                 )
                              - 3.0F
                        )
                        / 10.0F,
                  0.0F,
                  1.0F
               );
         GL11.glPointSize(pointsScale);
         GL11.glBegin(0);

         for (FireFlies.SparkPart spark : firePart.SPARK_PARTS) {
            float timePC = (float)spark.timePC();
            int c = ColorUtils.swapAlpha(
               ColorUtils.getOverallColorFrom(-1, color, timePC), (float)ColorUtils.getAlphaFromColor(color) * alphaPC * (1.0F - timePC * timePC * timePC)
            );
            RenderUtils.glColor(c);
            GL11.glVertex3d(spark.getRenderPosX(partialTicks), spark.getRenderPosY(partialTicks), spark.getRenderPosZ(partialTicks));
         }

         GL11.glEnd();
         GlStateManager.resetColor();
         GL11.glEnable(3008);
         GL11.glEnable(3553);
      }
   }

   private void drawTrailPartsList(int color, FireFlies.FirePart firePart, float alphaPC) {
      if (firePart.TRAIL_PARTS.size() >= 2) {
         GL11.glDisable(3553);
         GL11.glLineWidth(
            1.0E-5F
               + 3.0F
                  * MathUtils.clamp(
                     1.0F
                        - (
                              Minecraft.player
                                    .getSmoothDistanceToCoord(
                                       (float)firePart.getPosVec().xCoord, (float)firePart.getPosVec().yCoord + 1.6F, (float)firePart.getPosVec().zCoord
                                    )
                                 - 3.0F
                           )
                           / 20.0F,
                     0.0F,
                     1.0F
                  )
         );
         GL11.glEnable(3042);
         GL11.glDisable(3008);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         int point = 0;
         int pointsCount = firePart.TRAIL_PARTS.size();
         GL11.glBegin(3);

         for (FireFlies.TrailPart trail : firePart.TRAIL_PARTS) {
            float sizePC = (float)point / (float)pointsCount;
            int c = ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC * (float)MathUtils.easeInOutQuadWave((double)sizePC));
            c = ColorUtils.getOverallColorFrom(
               c,
               ColorUtils.swapAlpha(-1, (float)ColorUtils.getAlphaFromColor(c)),
               (float)ColorUtils.getAlphaFromColor(c) / 255.0F * ColorUtils.getBrightnessFromColor(c) * 0.4F * sizePC
            );
            RenderUtils.glColor(c);
            GL11.glVertex3d(trail.x, trail.y, trail.z);
            point++;
         }

         GL11.glEnd();
         GlStateManager.resetColor();
         GL11.glEnable(3008);
         GL11.glDisable(2848);
         GL11.glHint(3154, 4352);
         GL11.glLineWidth(1.0F);
         GL11.glEnable(3553);
      }
   }

   private class FirePart {
      List<FireFlies.TrailPart> TRAIL_PARTS = new ArrayList<>();
      List<FireFlies.SparkPart> SPARK_PARTS = new ArrayList<>();
      Vec3d prevPos;
      Vec3d pos;
      AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.02F);
      int msChangeSideRate = this.getMsChangeSideRate();
      int color;
      float moveYawSet = FireFlies.this.getRandom(0.0, 360.0);
      float speed = FireFlies.this.getRandom(0.08, 0.175);
      float yMotion = FireFlies.this.getRandom(-0.075, 0.1);
      float moveYaw = this.moveYawSet;
      float maxAlive;
      long startTime = System.currentTimeMillis();
      long rateTimer = this.startTime;
      boolean toRemove;

      public FirePart(Vec3d pos, float maxAlive, int color) {
         this.color = color;
         this.pos = pos;
         this.prevPos = pos;
         this.maxAlive = maxAlive;
      }

      public float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxAlive, 0.0F, 1.0F);
      }

      public void setAlphaPCTo(float to) {
         this.alphaPC.to = to;
      }

      public float getAlphaPC() {
         return this.alphaPC.getAnim();
      }

      public Vec3d getPosVec() {
         return this.pos;
      }

      public Vec3d getRenderPosVec(float pTicks) {
         Vec3d pos = this.getPosVec();
         return pos.addVector(
            -(this.prevPos.xCoord - pos.xCoord) * (double)pTicks,
            -(this.prevPos.yCoord - pos.yCoord) * (double)pTicks,
            -(this.prevPos.zCoord - pos.zCoord) * (double)pTicks
         );
      }

      public void updatePart() {
         if (System.currentTimeMillis() - this.rateTimer >= (long)this.msChangeSideRate) {
            this.msChangeSideRate = this.getMsChangeSideRate();
            this.rateTimer = System.currentTimeMillis();
            this.moveYawSet = FireFlies.this.getRandom(0.0, 360.0);
         }

         this.moveYaw = MathUtils.lerp(this.moveYaw, this.moveYawSet, 0.065F);
         float motionX = -((float)Math.sin(Math.toRadians((double)this.moveYaw))) * (this.speed /= 1.005F);
         float motionZ = (float)Math.cos(Math.toRadians((double)this.moveYaw)) * this.speed;
         this.prevPos = this.pos;
         float scaleBox = 0.1F;
         float delente = !Module.mc
               .world
               .getCollisionBoxes(
                  null,
                  new AxisAlignedBB(
                     this.pos.xCoord - (double)(scaleBox / 2.0F),
                     this.pos.yCoord,
                     this.pos.zCoord - (double)(scaleBox / 2.0F),
                     this.pos.xCoord + (double)(scaleBox / 2.0F),
                     this.pos.yCoord + (double)scaleBox,
                     this.pos.zCoord + (double)(scaleBox / 2.0F)
                  )
               )
               .isEmpty()
            ? 0.3F
            : 1.0F;
         this.pos = this.pos.addVector((double)(motionX / delente), (double)((this.yMotion /= 1.02F) / delente), (double)(motionZ / delente));
         if (this.getTimePC() >= 1.0F) {
            this.setAlphaPCTo(0.0F);
            if (this.getAlphaPC() < 0.003921569F) {
               this.setToRemove();
            }
         }

         this.TRAIL_PARTS.add(FireFlies.this.new TrailPart(this, 500));
         if (!this.TRAIL_PARTS.isEmpty()) {
            this.TRAIL_PARTS.removeIf(FireFlies.TrailPart::toRemove);
         }

         for (int i = 0; i < 8; i++) {
            this.SPARK_PARTS.add(FireFlies.this.new SparkPart(this, 600));
         }

         this.SPARK_PARTS.forEach(FireFlies.SparkPart::motionSparkProcess);
         if (!this.SPARK_PARTS.isEmpty()) {
            this.SPARK_PARTS.removeIf(FireFlies.SparkPart::toRemove);
         }
      }

      public void setToRemove() {
         this.toRemove = true;
      }

      public boolean isToRemove() {
         return this.toRemove;
      }

      int getMsChangeSideRate() {
         return (int)FireFlies.this.getRandom(300.5, 900.5);
      }
   }

   private class SparkPart {
      double posX;
      double posY;
      double posZ;
      double prevPosX;
      double prevPosY;
      double prevPosZ;
      double speed = Math.random() / 50.0;
      double radianYaw = Math.random() * 360.0;
      double radianPitch = -45.0 + Math.random() * 90.0;
      long startTime = System.currentTimeMillis();
      int maxTime;

      SparkPart(FireFlies.FirePart part, int maxTime) {
         this.maxTime = maxTime;
         this.posX = part.getPosVec().xCoord;
         this.posY = part.getPosVec().yCoord;
         this.posZ = part.getPosVec().zCoord;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
      }

      double timePC() {
         return (double)MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / (float)this.maxTime, 0.0F, 1.0F);
      }

      boolean toRemove() {
         return this.timePC() == 1.0;
      }

      void motionSparkProcess() {
         double radYaw = Math.toRadians(this.radianYaw);
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.posX = this.posX + Math.sin(radYaw) * this.speed;
         this.posY = this.posY + Math.cos(Math.toRadians(this.radianPitch - 90.0)) * this.speed;
         this.posZ = this.posZ + Math.cos(radYaw) * this.speed;
         this.speed /= 1.2F;
      }

      double getRenderPosX(float partialTicks) {
         return this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks;
      }

      double getRenderPosY(float partialTicks) {
         return this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks;
      }

      double getRenderPosZ(float partialTicks) {
         return this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks;
      }
   }

   private class TrailPart {
      double x;
      double y;
      double z;
      long startTime = System.currentTimeMillis();
      int maxTime;

      public TrailPart(FireFlies.FirePart part, int maxTime) {
         this.maxTime = maxTime;
         this.x = part.getPosVec().xCoord;
         this.y = part.getPosVec().yCoord;
         this.z = part.getPosVec().zCoord;
      }

      public float getTimePC() {
         return MathUtils.clamp((float)((System.currentTimeMillis() - this.startTime) / (long)this.maxTime), 0.0F, 1.0F);
      }

      public boolean toRemove() {
         return this.getTimePC() == 1.0F;
      }
   }
}
