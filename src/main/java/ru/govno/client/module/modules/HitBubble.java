package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class HitBubble extends Module {
   public static HitBubble get;
   static final ArrayList<HitBubble.Bubble> bubbles = new ArrayList<>();
   BoolSettings MoreEffects;
   ModeSettings ColorMode;
   ColorSettings PickColor1;
   ColorSettings PickColor2;
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();
   private final ResourceLocation BUBBLE_TEXTURE = new ResourceLocation("vegaline/modules/hitbubble/bubble.png");
   private final Random RAND = new Random(9012739L);

   public HitBubble() {
      super("HitBubble", 0, Module.Category.RENDER);
      this.settings.add(this.ColorMode = new ModeSettings("ColorMode", "Client", this, new String[]{"Rainbow", "Client", "Picker", "DoublePicker"}));
      this.settings
         .add(this.PickColor1 = new ColorSettings("PickColor1", ColorUtils.getColor(100, 255, 100), this, () -> this.ColorMode.currentMode.contains("Picker")));
      this.settings
         .add(
            this.PickColor2 = new ColorSettings("PickColor2", ColorUtils.getColor(60, 60, 255), this, () -> this.ColorMode.currentMode.endsWith("DoublePicker"))
         );
      this.settings.add(this.MoreEffects = new BoolSettings("MoreEffects", true, this));
      get = this;
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

   @Override
   public void onToggled(boolean actived) {
      this.stateAnim.to = actived ? 1.0F : 0.0F;
      super.onToggled(actived);
   }

   @Override
   public void onUpdate() {
      this.stateAnim.to = 1.0F;
   }

   private float getAlphaPC() {
      return this.stateAnim.getAnim();
   }

   private static float getMaxTime() {
      return (double)Minecraft.player.getCooledAttackStrength(0.0F) > 0.8 ? 3000.0F : 1700.0F;
   }

   public static void onAttack(Entity entity) {
      if (entity instanceof EntityLivingBase base) {
         if (base == null || !base.isEntityAlive()) {
            return;
         }

         Vec3d to = base.getPositionVector().addVector(0.0, (double)(base.height / 1.55F), 0.0);
         addBubble(to);
      }
   }

   private static void addBubble(Vec3d addToCoord) {
      RenderManager manager = mc.getRenderManager();
      bubbles.add(new HitBubble.Bubble(manager.playerViewX, -manager.playerViewY, addToCoord));
   }

   private void setupDrawsBubbles3D(Runnable render) {
      RenderManager manager = mc.getRenderManager();
      Vec3d conpense = new Vec3d(manager.getRenderPosX(), manager.getRenderPosY(), manager.getRenderPosZ());
      GL11.glDisable(2896);
      mc.entityRenderer.disableLightmap();
      GL11.glDepthMask(false);
      GL11.glDisable(2884);
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      GlStateManager.shadeModel(7425);
      GL11.glTranslated(-conpense.xCoord, -conpense.yCoord, -conpense.zCoord);
      mc.getTextureManager().bindTexture(this.BUBBLE_TEXTURE);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      render.run();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GL11.glTranslated(conpense.xCoord, conpense.yCoord, conpense.zCoord);
      GlStateManager.shadeModel(7424);
      GL11.glEnable(3008);
      GL11.glDepthMask(true);
      GL11.glEnable(2884);
      GlStateManager.resetColor();
   }

   private void drawBubble(HitBubble.Bubble bubble, float alphaPC) {
      GL11.glPushMatrix();
      Vec3d bXYZ = bubble.pos;
      GL11.glTranslated(bXYZ.xCoord, bXYZ.yCoord, bXYZ.zCoord);
      float extS = bubble.getDeltaTime();
      GlStateManager.translate(
         -Math.sin(Math.toRadians((double)bubble.viewPitch)) * (double)extS / 3.0,
         Math.sin(Math.toRadians((double)bubble.viewYaw)) * (double)extS / 2.0,
         -Math.cos(Math.toRadians((double)bubble.viewPitch)) * (double)extS / 3.0
      );
      GL11.glNormal3d(1.0, 1.0, 1.0);
      GL11.glRotated((double)bubble.viewPitch, 0.0, 1.0, 0.0);
      GL11.glRotated((double)bubble.viewYaw, mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0, 0.0, 0.0);
      GL11.glScaled(-0.1, -0.1, 0.1);
      this.drawBeginsNullCoord(bubble, alphaPC);
      GL11.glPopMatrix();
   }

   private void drawBeginsNullCoord(HitBubble.Bubble bubble, float alphaPC) {
      float aPC = (float)MathUtils.easeInOutQuadWave((double)(MathUtils.clamp(bubble.getDeltaTime() + 0.1F, 0.0F, 1.0F) * alphaPC)) * 2.0F;
      aPC = aPC > 1.0F ? 1.0F : aPC;
      if ((double)bubble.getDeltaTime() > 0.5) {
         aPC *= aPC;
      }

      aPC *= alphaPC;
      float r = 12.5F * aPC;
      int speedRotate = 4;
      float III = (float)(System.currentTimeMillis() % (long)(3600 / speedRotate)) / 10.0F * (float)speedRotate;
      this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(stateColor(0, aPC)).endVertex();
      this.buffer.pos(0.0, (double)r, 0.0).tex(0.0, 1.0).color(stateColor(90, aPC)).endVertex();
      this.buffer.pos((double)r, (double)r, 0.0).tex(1.0, 1.0).color(stateColor(180, aPC)).endVertex();
      this.buffer.pos((double)r, 0.0, 0.0).tex(1.0, 0.0).color(stateColor(270, aPC)).endVertex();
      RenderUtils.customRotatedObject2D(-1.0F, -1.0F, 2.0F, 2.0F, (double)(-III));
      GlStateManager.translate(-r / 2.0F, -r / 2.0F, 0.0F);
      this.tessellator.draw();
      GlStateManager.translate(r / 2.0F, r / 2.0F, 0.0F);
      RenderUtils.customRotatedObject2D(-1.0F, -1.0F, 2.0F, 2.0F, (double)III);
      GlStateManager.translate(-r / 2.0F, -r / 2.0F, 0.0F);
      if (!bubble.parts.isEmpty()) {
         this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);

         for (HitBubble.BubblePart part : bubble.parts) {
            float timePC = part.getDeltaTime();
            float radian = (float)part.getRotate(timePC);
            float radiusPC = part.getRPC(timePC) * 1.1F;
            float px = (float)Math.sin((double)radian) * radiusPC * r * 2.0F + r / 2.0F;
            float py = (float)(-Math.cos((double)radian)) * radiusPC * r * 2.0F + r / 2.0F;
            float extend = part.getExtend(timePC);
            float partAlphaPC = (1.0F - timePC) * (1.0F - timePC) * aPC * aPC;
            partAlphaPC = (float)MathUtils.easeInOutQuadWave((double)partAlphaPC);
            int col = stateColor((int)(radian * 180.0F + III), partAlphaPC);
            col = ColorUtils.getOverallColorFrom(col, ColorUtils.getColor(255, 255, 255, ColorUtils.getAlphaFromColor(col)), timePC / 2.0F);
            this.buffer.pos((double)px, (double)py, (double)(-extend * r / 4.0F)).color(col).endVertex();
            this.buffer.pos((double)px, (double)py, (double)(extend * r / 4.0F)).color(col).endVertex();
         }

         Vec3d cameraPos = new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
         double dst = cameraPos.getDistanceAtEyeByVec(Minecraft.player, bubble.pos.xCoord, bubble.pos.yCoord, bubble.pos.zCoord);
         float partScale = 0.025F + 13.0F * (float)MathUtils.clamp(1.0 - dst / 5.0, 0.0, 1.0);
         GL11.glDisable(3553);
         GL11.glPointSize(partScale);
         this.tessellator.draw();
         GL11.glPointSize(1.0F);
         GL11.glEnable(3553);
      }

      GlStateManager.translate(r / 2.0F, r / 2.0F, 0.0F);
   }

   @Override
   public void alwaysRender3D() {
      float aPC = this.getAlphaPC();
      if (!((double)aPC < 0.05)) {
         if (!bubbles.isEmpty()) {
            this.removeAuto();
            if (this.MoreEffects.getBool()) {
               this.addAuto();
            }

            this.setupDrawsBubbles3D(() -> bubbles.forEach(bubble -> {
                  if (bubble != null && bubble.getDeltaTime() <= 1.0F) {
                     this.drawBubble(bubble, aPC);
                  }
               }));
         }
      }
   }

   private void removeAuto() {
      bubbles.removeIf(bubble -> bubble.getDeltaTime() >= 1.0F);
      bubbles.forEach(bubble -> bubble.parts.removeIf(part -> part.getDeltaTime() >= 1.0F));
   }

   private void addAuto() {
      int addHZ = 1;
      bubbles.forEach(bubble -> {
         for (int i = 0; i < addHZ; i++) {
            bubble.parts.add(new HitBubble.BubblePart(0.45F, 0.65F, bubble.maxTime / 4.0F * (0.5F + this.RAND.nextFloat(0.5F))));
         }
      });
   }

   private static final class Bubble {
      private final ArrayList<HitBubble.BubblePart> parts = new ArrayList<>();
      Vec3d pos;
      long time = System.currentTimeMillis();
      float maxTime = HitBubble.getMaxTime();
      float viewYaw;
      float viewPitch;

      public Bubble(float viewYaw, float viewPitch, Vec3d pos) {
         this.viewYaw = viewYaw;
         this.viewPitch = viewPitch;
         this.pos = pos;
      }

      private float getDeltaTime() {
         return (float)(System.currentTimeMillis() - this.time) / this.maxTime;
      }
   }

   private class BubblePart {
      float startRPC;
      float endRPC;
      float randRotate = HitBubble.this.RAND.nextFloat(360.0F);
      float rotateOffset = HitBubble.this.RAND.nextFloat(-180.0F, 0.0F);
      float extend = HitBubble.this.RAND.nextFloat(0.5F, 1.5F);
      long time = System.currentTimeMillis();
      float maxTime;

      public BubblePart(float startRPC, float endRPC, float maxTime) {
         this.startRPC = startRPC;
         this.endRPC = endRPC;
         this.maxTime = maxTime;
      }

      private float getDeltaTime() {
         return (float)(System.currentTimeMillis() - this.time) / this.maxTime;
      }

      private double getRotate(float deltaTime) {
         return Math.toRadians((double)(this.randRotate + this.rotateOffset * deltaTime) % 360.0);
      }

      private float getRPC(float deltaTime) {
         return (float)MathUtils.easeInCircle((double)MathUtils.lerp(this.startRPC, this.endRPC, deltaTime));
      }

      private float getExtend(float deltaTime) {
         return (float)MathUtils.easeInCircle((double)deltaTime) * this.extend;
      }
   }
}
