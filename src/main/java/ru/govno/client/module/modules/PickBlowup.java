package ru.govno.client.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.TextureRenderer;

public class PickBlowup extends Module {
   public static PickBlowup get;
   private final Random RANDOM = new Random(123947126L);
   ModeSettings ColorMode;
   ColorSettings PickColor1;
   ColorSettings PickColor2;
   private final List<PickBlowup.BlowupElement> BLOWUP_ELEMENTS = new ArrayList<>();
   private final Tessellator tessellator = Tessellator.getInstance();

   public PickBlowup() {
      super("PickBlowup", 0, Module.Category.RENDER);
      this.settings.add(this.ColorMode = new ModeSettings("ColorMode", "Client", this, new String[]{"Random", "Client", "Picker", "DoublePicker"}));
      this.settings
         .add(this.PickColor1 = new ColorSettings("PickColor1", ColorUtils.getColor(100, 255, 100), this, () -> this.ColorMode.getMode().contains("Picker")));
      this.settings
         .add(
            this.PickColor2 = new ColorSettings("PickColor2", ColorUtils.getColor(60, 60, 255), this, () -> this.ColorMode.getMode().endsWith("DoublePicker"))
         );
      get = this;
   }

   private void hCirclePolygonBegin(Vec3d pos, float startRad, float endRad, int vertexCount, int color1, int color2, float alphaPC) {
      if (vertexCount >= 3 && startRad != endRad) {
         float r1 = startRad < endRad ? startRad : endRad;
         float r2 = startRad >= endRad ? startRad : endRad;
         color1 = ColorUtils.swapAlpha(color1, (float)ColorUtils.getAlphaFromColor(color1) * alphaPC);
         color2 = ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) * alphaPC);
         this.tessellator.getBuffer().begin(8, DefaultVertexFormats.POSITION_COLOR);

         for (float rad = 0.0F; rad <= 360.0F; rad += 360.0F / (float)vertexCount) {
            double rad1;
            this.tessellator
               .getBuffer()
               .pos(pos.addVector(Math.sin(rad1 = Math.toRadians((double)rad)) * (double)r1, 0.0, Math.cos(rad1) * (double)r1))
               .color(color1)
               .endVertex();
            this.tessellator
               .getBuffer()
               .pos(pos.addVector(Math.sin(rad1 = Math.toRadians((double)rad)) * (double)r2, 0.0, Math.cos(rad1) * (double)r2))
               .color(color2)
               .endVertex();
         }

         this.tessellator.draw();
      }
   }

   private void nanoSparkPrintsBegin(List<PickBlowup.Nano3DSpark> sparks, float alphaPC) {
      this.tessellator.getBuffer().begin(0, DefaultVertexFormats.POSITION_COLOR);
      sparks.stream()
         .filter(spark -> !spark.wantToRemove())
         .forEach(
            spark -> spark.poses
                  .forEach(
                     pos -> this.tessellator
                           .getBuffer()
                           .pos(BlockUtils.getOverallVec3d(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), spark.getBlowPos(), (1.0F - alphaPC) / 4.0F))
                           .color(ColorUtils.swapAlpha(pos.getColor(), (float)ColorUtils.getAlphaFromColor(pos.getColor()) * spark.getAlphaPC() * alphaPC))
                           .endVertex()
                  )
         );
      this.tessellator.draw();
   }

   private void setup3DRender(Runnable drawable) {
      GL11.glPushMatrix();
      GL11.glBlendFunc(770, 32772);
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glEnable(2929);
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      GL11.glHint(3154, 4354);
      GL11.glEnable(2832);
      GL11.glDepthMask(false);
      GL11.glPointSize(1.575F);
      GL11.glDisable(2896);
      GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);
      drawable.run();
      RenderUtils.resetBlender();
      GL11.glBlendFunc(770, 771);
      GL11.glPointSize(1.0F);
      GL11.glDepthMask(true);
      GL11.glShadeModel(7424);
      GL11.glHint(3154, 4352);
      GL11.glEnable(3008);
      GL11.glEnable(2929);
      GL11.glPopMatrix();
   }

   private int stateColor() {
      String var1 = this.ColorMode.getMode();
      switch (var1) {
         case "Random":
            return Color.HSBtoRGB((float)this.RANDOM.nextInt(360) / 360.0F, 1.0F, 1.0F);
         case "Client":
            return ClientColors.getColor1(this.RANDOM.nextInt(360), 1.0F);
         case "Picker":
            return this.PickColor1.getCol();
         case "DoublePicker":
            return ColorUtils.getOverallColorFrom(this.PickColor1.getCol(), this.PickColor2.getCol(), (float)this.RANDOM.nextInt(360) / 360.0F);
         default:
            return -1;
      }
   }

   public void onCollect(EntityItem entityItem, EntityLivingBase whoPicked) {
      if (whoPicked != null && whoPicked instanceof EntityPlayerSP player) {
         ItemStack stack = entityItem.getItem();
         if (stack.func_190926_b()) {
            return;
         }

         this.BLOWUP_ELEMENTS
            .add(
               new PickBlowup.BlowupElement(
                  entityItem.getPositionVector().addVector(0.0, 0.01F - entityItem.posY + (double)((int)entityItem.posY), 0.0),
                  0.05F,
                  0.25F,
                  2900L,
                  this.stateColor()
               )
            );
      }
   }

   @Override
   public void onUpdate() {
      if (!this.BLOWUP_ELEMENTS.isEmpty()) {
         this.BLOWUP_ELEMENTS.forEach(PickBlowup.BlowupElement::update);
         this.BLOWUP_ELEMENTS.removeIf(PickBlowup.BlowupElement::wantToRemove);
      }
   }

   @EventTarget
   public void onRender3D(Event3D event3D) {
      if (!this.BLOWUP_ELEMENTS.isEmpty()) {
         this.setup3DRender(() -> this.BLOWUP_ELEMENTS.forEach(PickBlowup.BlowupElement::draw));
      }
   }

   @Override
   public void onToggled(boolean enable) {
      if (this.BLOWUP_ELEMENTS.isEmpty()) {
         this.BLOWUP_ELEMENTS.clear();
      }

      super.onToggled(enable);
   }

   private class BlowupElement {
      private final long startTime = System.currentTimeMillis();
      private final long maxTime;
      private final Vec3d pos;
      private final List<PickBlowup.Nano3DSpark> sparks = new ArrayList<>();
      private final float[] radiuses = new float[2];
      private final int color;

      public BlowupElement(Vec3d pos, float startRad, float endRad, long maxTime, int color) {
         this.pos = pos;
         this.maxTime = maxTime;
         this.radiuses[0] = startRad;
         this.radiuses[1] = endRad;
         this.color = color;
      }

      public void update() {
         List<TextureRenderer.Vec3dColored> sparkPoses = new ArrayList<>();
         int spawnCount = 10;

         for (int i = 0; i < spawnCount; i++) {
            double yawOff = Math.toRadians((double)PickBlowup.this.RANDOM.nextInt(360));
            double radOff = (double)MathUtils.lerp(this.radiuses[0], this.radiuses[1], PickBlowup.this.RANDOM.nextFloat(1.0F));
            float alphaPC = (float)(radOff / (double)this.radiuses[1]);
            alphaPC = (float)MathUtils.easeInOutQuadWave((double)alphaPC);
            sparkPoses.add(
               new TextureRenderer.Vec3dColored(
                  this.pos.xCoord + Math.sin(yawOff) * radOff,
                  this.pos.yCoord,
                  this.pos.zCoord + Math.cos(yawOff) * radOff,
                  ColorUtils.swapAlpha(this.color, (float)ColorUtils.getAlphaFromColor(this.color) * alphaPC)
               )
            );
         }

         this.sparks.add(PickBlowup.this.new Nano3DSpark(this.pos, sparkPoses, (float)this.maxTime / 2.0F));
         this.sparks.removeIf(PickBlowup.Nano3DSpark::wantToRemove);
      }

      public void draw() {
         float alphaPC = this.getAlphaPC();
         if (alphaPC != 0.0F) {
            if (!this.sparks.isEmpty()) {
               PickBlowup.this.nanoSparkPrintsBegin(this.sparks, alphaPC);
            }

            float radius = this.getRadius(this.radiuses[0], this.radiuses[1], 2.0F);
            float width = (this.radiuses[1] - this.radiuses[0]) * radius;
            float rMin = radius - width / 2.0F;
            float rMax = radius + width / 2.0F;
            float dear = 5.0F;
            PickBlowup.this.hCirclePolygonBegin(this.pos, rMin / dear, radius / dear, 15, 0, this.color, alphaPC);
            PickBlowup.this.hCirclePolygonBegin(this.pos, radius / dear, rMax / dear, 15, this.color, 0, alphaPC);
         }
      }

      public float getTimePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / (float)this.maxTime, 0.0F, 1.0F);
      }

      public float getRadius(float minRadius, float maxRadius, float pre) {
         return MathUtils.lerp(minRadius, maxRadius, MathUtils.clamp(this.getTimePC() * pre, 0.0F, 1.0F));
      }

      public float getAlphaPC() {
         return (float)MathUtils.easeInOutQuadWave((double)(1.0F - this.getTimePC()));
      }

      public boolean wantToRemove() {
         return this.getTimePC() == 1.0F;
      }

      public Vec3d getPos() {
         return this.pos;
      }
   }

   private class Nano3DSpark {
      private final List<TextureRenderer.Vec3dColored> poses;
      private final Vec3d blowPos;
      private final long startTime = System.currentTimeMillis();
      private final float maxTime;

      public Nano3DSpark(Vec3d blowPos, List<TextureRenderer.Vec3dColored> poses, float maxTime) {
         this.blowPos = blowPos;
         this.poses = poses;
         this.maxTime = maxTime;
      }

      public float getAlphaPC() {
         return 1.0F - MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0F, 1.0F);
      }

      public boolean wantToRemove() {
         return this.getAlphaPC() == 0.0F;
      }

      public Vec3d getBlowPos() {
         return this.blowPos;
      }
   }
}
