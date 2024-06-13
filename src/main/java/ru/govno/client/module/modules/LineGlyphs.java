package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class LineGlyphs extends Module {
   public static LineGlyphs get;
   public FloatSettings GlyphsCount;
   public BoolSettings SlowSpeed;
   public ModeSettings ColorMode;
   public ColorSettings PickColor1;
   public ColorSettings PickColor2;
   private final Random RAND = new Random(93882L);
   private final List<Vec3d> temp3dVecs = new ArrayList<>();
   private static final Tessellator tessellator = Tessellator.getInstance();
   private final List<LineGlyphs.GliphsVecGen> GLIPHS_VEC_GENS = new ArrayList<>();

   public LineGlyphs() {
      super("LineGlyphs", 0, Module.Category.RENDER);
      get = this;
      this.settings.add(this.GlyphsCount = new FloatSettings("GlyphsCount", 70.0F, 200.0F, 10.0F, this));
      this.settings.add(this.SlowSpeed = new BoolSettings("SlowSpeed", false, this));
      this.settings.add(this.ColorMode = new ModeSettings("ColorMode", "Client", this, new String[]{"Rainbow", "Client", "Picker", "DoublePicker"}));
      this.settings
         .add(this.PickColor1 = new ColorSettings("PickColor1", ColorUtils.getColor(100, 255, 100), this, () -> this.ColorMode.currentMode.contains("Picker")));
      this.settings
         .add(
            this.PickColor2 = new ColorSettings("PickColor2", ColorUtils.getColor(60, 60, 255), this, () -> this.ColorMode.currentMode.endsWith("DoublePicker"))
         );
   }

   private static int stateColor(int index, float alphaPC) {
      int color = -1;
      if (get != null) {
         String var3 = get.ColorMode.getMode();
         switch (var3) {
            case "Rainbow":
               color = ColorUtils.rainbowGui(0, (long)index);
               break;
            case "Client":
               color = ClientColors.getColor1(index, 1.0F);
               break;
            case "Picker":
               color = get.PickColor1.getCol();
               break;
            case "DoublePicker":
               color = ColorUtils.fadeColor(get.PickColor1.getCol(), get.PickColor2.getCol(), 0.3F, (int)((float)index / 0.3F / 8.0F));
         }
      }

      return ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC);
   }

   private int[] lineMoveSteps() {
      return new int[]{0, 3};
   }

   private int[] lineStepsAmount() {
      return new int[]{7, 12};
   }

   private int[] spawnRanges() {
      return new int[]{6, 24, 0, 12};
   }

   private int maxObjCount() {
      return this.GlyphsCount.getInt();
   }

   private int minGliphsJointDst() {
      return 8;
   }

   private int getR360X() {
      return this.RAND.nextInt(0, 4) * 90;
   }

   private int getR360Y() {
      return this.RAND.nextInt(-2, 2) * 90;
   }

   private int[] getR360XY() {
      return new int[]{this.RAND.nextInt(0, 4) * 90, this.RAND.nextInt(-1, 1) * 90};
   }

   private int[] getA90R(int[] outdated) {
      int a = outdated[0];
      int ao = a;
      int b = outdated[1];
      int bo = b;

      for (int maxAttempt = 150; maxAttempt > 0 && Math.abs(b - bo) != 90; maxAttempt--) {
         b = this.getR360Y();
      }

      for (int var7 = 5; var7 > 0 && (Math.abs(a - ao) != 90 || Math.abs(a - ao) != 270); var7--) {
         a = this.getR360X();
      }

      return new int[]{a, b};
   }

   private Vec3i offsetFromRXYR(Vec3i vec3i, int[] rxy, int r) {
      double yawR = Math.toRadians((double)rxy[0]);
      double pitchR = Math.toRadians((double)rxy[1]);
      double r1 = (double)r;
      int ry = (int)(Math.sin(pitchR) * r1);
      if (pitchR != 0.0) {
         r1 = 0.0;
      }

      int rx = (int)(-(Math.sin(yawR) * r1));
      int rz = (int)(Math.cos(yawR) * r1);
      int xi = vec3i.getX() + rx;
      int yi = vec3i.getY() + ry;
      int zi = vec3i.getZ() + rz;
      return new Vec3i(xi, yi, zi);
   }

   private float moveAdvanceFromTicks(int ticksSet, int ticksExpiring, float pTicks) {
      return Math.min(Math.max(1.0F - ((float)ticksExpiring - pTicks) / (float)ticksSet, 0.0F), 1.0F);
   }

   private List<Vec3d> getSmoothTickedFromList(List<Vec3i> vec3is, float moveAdvance) {
      if (!this.temp3dVecs.isEmpty()) {
         this.temp3dVecs.clear();
      }

      for (Vec3i vec3i : vec3is) {
         double x = (double)vec3i.getX();
         double y = (double)vec3i.getY();
         double z = (double)vec3i.getZ();
         if (vec3is.size() >= 1 && vec3i == vec3is.get(vec3is.size() - 1)) {
            Vec3i prevVec3i = vec3is.get(vec3is.size() - 2);
            x = MathUtils.lerp((double)prevVec3i.getX(), x, (double)moveAdvance);
            y = MathUtils.lerp((double)prevVec3i.getY(), y, (double)moveAdvance);
            z = MathUtils.lerp((double)prevVec3i.getZ(), z, (double)moveAdvance);
         }

         this.temp3dVecs.add(new Vec3d(x, y, z));
      }

      return this.temp3dVecs;
   }

   private Vec3i randGliphSpawnPos() {
      int[] spawnRanges = this.spawnRanges();
      double dst = (double)this.RAND.nextInt(spawnRanges[0], spawnRanges[1]);
      double fov = (double)mc.gameSettings.fovSetting;
      double radianYaw = Math.toRadians(
         (double)this.RAND.nextInt((int)((double)Minecraft.player.rotationYaw - fov * 0.75), (int)((double)Minecraft.player.rotationYaw + fov * 0.75))
      );
      int randXOff = (int)(-(Math.sin(radianYaw) * dst));
      int randYOff = this.RAND.nextInt(-spawnRanges[2], spawnRanges[3]);
      int randZOff = (int)(Math.cos(radianYaw) * dst);
      return new Vec3i(RenderManager.viewerPosX + (double)randXOff, RenderManager.viewerPosY + (double)randYOff, RenderManager.viewerPosZ + (double)randZOff);
   }

   private Vec3i genWhiteGliphPos(List<LineGlyphs.GliphsVecGen> gliphVecGens, Frustum frustum) {
      Vec3i tempVec = this.randGliphSpawnPos();
      if (gliphVecGens.size() >= 2) {
         Vec3i finalTempVec = tempVec;

         for (int maxAttempt = 1;
            maxAttempt > 0
               && gliphVecGens.stream()
                  .filter(gliphVecGen -> gliphVecGen.vecGens.size() >= 2)
                  .anyMatch(
                     gliphVecGen -> gliphVecGen.vecGens.get(0) != null
                           && Math.sqrt(gliphVecGen.vecGens.get(0).distanceSq(finalTempVec)) <= (double)this.minGliphsJointDst()
                  );
            maxAttempt--
         ) {
            Vec3i vec3i = this.randGliphSpawnPos();
            if (!frustum.isBoundingBoxInFrustum(aabbFromVec3d(new Vec3d((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ())))) {
               tempVec = this.randGliphSpawnPos();
            }
         }
      }

      return tempVec;
   }

   private void addAllGliphs(int countCap, Frustum frustum) {
      for (int maxAttempt = 8;
         maxAttempt > 0 && this.GLIPHS_VEC_GENS.stream().filter(gliphsVecGen -> gliphsVecGen.alphaPC.to != 0.0F).count() < (long)countCap;
         maxAttempt--
      ) {
         int[] lineStepsAmount = this.lineStepsAmount();

         while (this.GLIPHS_VEC_GENS.size() < countCap) {
            Vec3i pos = this.randGliphSpawnPos();
            this.GLIPHS_VEC_GENS.add(new LineGlyphs.GliphsVecGen(pos, this.RAND.nextInt(lineStepsAmount[0], lineStepsAmount[1])));
         }
      }
   }

   private void gliphsRemoveAuto(float moduleAlphaPC, Frustum frustum) {
      this.GLIPHS_VEC_GENS.removeIf(gliphsVecGen -> gliphsVecGen.isToRemove(moduleAlphaPC, frustum));
   }

   private void gliphsUpdate() {
      if (!this.GLIPHS_VEC_GENS.isEmpty()) {
         this.GLIPHS_VEC_GENS.forEach(LineGlyphs.GliphsVecGen::update);
      }
   }

   private void gliphsClear() {
      if (!this.GLIPHS_VEC_GENS.isEmpty()) {
         this.GLIPHS_VEC_GENS.clear();
      }
   }

   private static AxisAlignedBB aabbFromVec3d(Vec3d pos) {
      return new AxisAlignedBB(pos).expandXyz(0.1);
   }

   private void drawAllGliphs(float alphaPC, float pTicks, Frustum frustum) {
      if (!this.GLIPHS_VEC_GENS.isEmpty()) {
         List<LineGlyphs.GliphsVecGen> filteredGens = this.GLIPHS_VEC_GENS
            .stream()
            .filter(gliphsVecGen -> alphaPC * gliphsVecGen.getAlphaPC() * 255.0F >= 1.0F)
            .toList();
         if (!filteredGens.isEmpty()) {
            LineGlyphs.GliphVecRenderer.set3DRendering(true, () -> {
               int colorIndex = 0;

               for (LineGlyphs.GliphsVecGen filteredGen : filteredGens) {
                  LineGlyphs.GliphVecRenderer.clientColoredBegin(filteredGen, ++colorIndex, 180, alphaPC * filteredGen.alphaPC.anim, pTicks, frustum);
               }
            });
         }
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         this.stateAnim.to = 1.0F;
      } else {
         this.stateAnim.to = 0.0F;
      }

      super.onToggled(actived);
   }

   @Override
   public void onUpdate() {
      this.gliphsUpdate();
      this.addAllGliphs(this.maxObjCount(), new Frustum(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ));
   }

   @Override
   public void alwaysRender3D(float partialTicks) {
      float alphaPC;
      if (this.actived) {
         this.stateAnim.to = 1.0F;
         alphaPC = this.stateAnim.getAnim();
      } else {
         if (this.stateAnim.anim < 0.03F && this.stateAnim.to == 0.0F) {
            this.gliphsClear();
            return;
         }

         this.stateAnim.to = 0.0F;
         alphaPC = this.stateAnim.getAnim();
      }

      Frustum frustum = new Frustum(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
      this.gliphsRemoveAuto(alphaPC, frustum);
      this.drawAllGliphs(alphaPC, partialTicks, frustum);
   }

   private class GliphVecRenderer {
      private static void set3DRendering(boolean bloom, Runnable render) {
         GL11.glPushMatrix();
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            bloom ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         GL11.glEnable(3042);
         GL11.glLineWidth(1.0F);
         GL11.glPointSize(1.0F);
         GL11.glEnable(2832);
         GL11.glDisable(3553);
         Module.mc.entityRenderer.disableLightmap();
         GL11.glDisable(2896);
         GL11.glShadeModel(7425);
         GL11.glAlphaFunc(516, 0.003921569F);
         GL11.glDisable(2884);
         GL11.glDepthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glTranslated(-RenderManager.viewerPosX, -RenderManager.viewerPosY, -RenderManager.viewerPosZ);
         render.run();
         GL11.glLineWidth(1.0F);
         GL11.glHint(3154, 4352);
         GL11.glDepthMask(true);
         GL11.glEnable(2884);
         GL11.glAlphaFunc(516, 0.1F);
         GL11.glLineWidth(1.0F);
         GL11.glPointSize(1.0F);
         GL11.glShadeModel(7424);
         GL11.glEnable(3553);
         GlStateManager.resetColor();
         if (bloom) {
            GlStateManager.tryBlendFuncSeparate(
               GlStateManager.SourceFactor.SRC_ALPHA,
               GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
               GlStateManager.SourceFactor.ONE,
               GlStateManager.DestFactor.ZERO
            );
         }

         GL11.glPopMatrix();
      }

      private static float calcLineWidth(LineGlyphs.GliphsVecGen gliphVecGen) {
         Vec3d cameraPos = new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
         Vec3i pos = gliphVecGen.vecGens
            .stream()
            .sorted(Comparator.comparingDouble(vec3i -> -vec3i.distanceTo(cameraPos)))
            .findAny()
            .orElse(new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
         double dst = cameraPos.getDistanceAtEyeByVec(Minecraft.player, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         return 1.0E-4F + 3.0F * (float)MathUtils.clamp(1.0 - dst / 20.0, 0.0, 1.0);
      }

      private static void clientColoredBegin(
         LineGlyphs.GliphsVecGen gliphVecGen, int objIndex, int colorIndexStep, float alphaPC, float pTicks, Frustum frustum
      ) {
         if (!(alphaPC * 255.0F < 1.0F) && gliphVecGen.vecGens.size() >= 2) {
            float lineWidth = calcLineWidth(gliphVecGen);
            GL11.glLineWidth(lineWidth);
            LineGlyphs.tessellator.getBuffer().begin(3, DefaultVertexFormats.POSITION_COLOR);
            int colorIndex = objIndex;
            int index = 0;

            for (Vec3d vec3d : gliphVecGen.getPosVectors(pTicks)) {
               float aPC = alphaPC * (0.25F + (float)index / (float)gliphVecGen.vecGens.size() / 1.75F);
               LineGlyphs.tessellator.getBuffer().pos(vec3d).color(LineGlyphs.stateColor(colorIndex, aPC)).endVertex();
               colorIndex += colorIndexStep;
               index++;
            }

            LineGlyphs.tessellator.draw();
            GL11.glPointSize(lineWidth * 3.0F);
            LineGlyphs.tessellator.getBuffer().begin(0, DefaultVertexFormats.POSITION_COLOR);
            colorIndex = objIndex;
            index = 0;

            for (Vec3d vec3d : gliphVecGen.getPosVectors(pTicks)) {
               float aPC = alphaPC * (0.25F + (float)index / (float)gliphVecGen.vecGens.size() / 1.75F);
               LineGlyphs.tessellator.getBuffer().pos(vec3d).color(LineGlyphs.stateColor(colorIndex, aPC)).endVertex();
               colorIndex += colorIndexStep;
               index++;
            }

            LineGlyphs.tessellator.draw();
         }
      }
   }

   private class GliphsVecGen {
      private final List<Vec3i> vecGens = new ArrayList<>();
      private int currentStepTicks;
      private int lastStepSet;
      private int stepsAmount;
      private int[] lastYawPitch;
      private final AnimationUtils alphaPC = new AnimationUtils(0.1F, 1.0F, 0.075F);

      public GliphsVecGen(Vec3i spawnPos, int maxStepsAmount) {
         this.vecGens.add(spawnPos);
         this.lastYawPitch = LineGlyphs.this.getR360XY();
         this.stepsAmount = maxStepsAmount;
      }

      private void update() {
         if (this.stepsAmount == 0) {
            this.alphaPC.to = 0.0F;
         }

         if (this.currentStepTicks > 0) {
            this.currentStepTicks = this.currentStepTicks - (LineGlyphs.this.SlowSpeed.getBool() ? 1 : 2);
            if (this.currentStepTicks < 0) {
               this.currentStepTicks = 0;
            }
         } else {
            this.vecGens
               .add(
                  LineGlyphs.this.offsetFromRXYR(
                     this.vecGens.get(this.vecGens.size() - 1),
                     this.lastYawPitch = LineGlyphs.this.getA90R(this.lastYawPitch),
                     this.lastStepSet = this.currentStepTicks = LineGlyphs.this.RAND
                        .nextInt(LineGlyphs.this.lineMoveSteps()[0], LineGlyphs.this.lineMoveSteps()[1])
                  )
               );
            this.stepsAmount--;
         }
      }

      public List<Vec3d> getPosVectors(float pTicks) {
         return LineGlyphs.this.getSmoothTickedFromList(this.vecGens, LineGlyphs.this.moveAdvanceFromTicks(this.lastStepSet, this.currentStepTicks, pTicks));
      }

      public float getAlphaPC() {
         return MathUtils.clamp(this.alphaPC.getAnim(), 0.0F, 1.0F);
      }

      public void setWantToRemove() {
         this.stepsAmount = 0;
      }

      public boolean isToRemove(float moduleAlphaPC, Frustum frustum) {
         return moduleAlphaPC * (this.alphaPC.to == 0.0F ? this.getAlphaPC() : 1.0F) * 255.0F < 1.0F;
      }
   }
}
