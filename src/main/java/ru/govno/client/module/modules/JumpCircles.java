package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class JumpCircles extends Module {
   public static JumpCircles get;
   List<List<ResourceLocation>> animatedGroups = Arrays.asList(new ArrayList(), new ArrayList(), new ArrayList());
   FloatSettings MaxTime;
   FloatSettings Range;
   ModeSettings Texture;
   ModeSettings ColorMode;
   ColorSettings PickColor1;
   ColorSettings PickColor2;
   BoolSettings DeepestLight;
   private final String staticLoc = "vegaline/modules/jumpcircles/default/";
   private final String animatedLoc = "vegaline/modules/jumpcircles/animated/";
   private ResourceLocation JUMP_CIRCLE = new ResourceLocation("vegaline/modules/jumpcircles/default/circle.png");
   private ResourceLocation JUMP_KONCHAL = new ResourceLocation("vegaline/modules/jumpcircles/default/konchal.png");
   private ResourceLocation JUMP_INSUAL = new ResourceLocation("vegaline/modules/jumpcircles/default/inusual.png");
   private static final List<JumpCircles.JumpRenderer> circles = new ArrayList<>();
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();

   private ResourceLocation jumpTexture(int index, float progress) {
      String tex = this.Texture.currentMode;
      if (!tex.equalsIgnoreCase("CubicalPieces") && !tex.equalsIgnoreCase("Leeches") && !tex.equalsIgnoreCase("Emission")) {
         return tex.equalsIgnoreCase("Circle") ? this.JUMP_CIRCLE : (tex.equalsIgnoreCase("KonchalEbal") ? this.JUMP_KONCHAL : this.JUMP_INSUAL);
      } else {
         List<ResourceLocation> currentGroupTextures = tex.equalsIgnoreCase("CubicalPieces")
            ? this.animatedGroups.get(0)
            : (tex.equalsIgnoreCase("Emission") ? this.animatedGroups.get(1) : this.animatedGroups.get(2));
         boolean animateByProgress = tex.equalsIgnoreCase("Leeches");
         if (tex.equalsIgnoreCase("Leeches")) {
            progress += 0.6F;
         }

         float frameOffset01 = progress % 1.0F;
         if (!animateByProgress) {
            int ms = 1500 / (tex.equalsIgnoreCase("Emission") ? 2 : 1);
            frameOffset01 = (float)((System.currentTimeMillis() + (long)index) % (long)ms) / (float)ms;
         }

         return currentGroupTextures.get((int)Math.min(frameOffset01 * ((float)currentGroupTextures.size() - 0.5F), (float)currentGroupTextures.size()));
      }
   }

   private ResourceLocation upscaleTexture(ResourceLocation resLoc) {
      try {
         DynamicTexture dynamicTexture = new DynamicTexture(
            TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(resLoc).getInputStream())
         );
         dynamicTexture.setBlurMipmap(true, false);
         resLoc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(dynamicTexture.toString(), dynamicTexture);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return resLoc;
   }

   public JumpCircles() {
      super("JumpCircles", 0, Module.Category.RENDER);
      get = this;
      int[] groupsFramesLength = new int[]{100, 60, 200};
      String[] groupsFramesFormat = new String[]{"jpeg", "png", "png"};

      for (int groupIndex = groupsFramesLength.length - 1; groupIndex >= 0; groupIndex--) {
         int framesCounter = 0;

         while (framesCounter < groupsFramesLength[groupIndex]) {
            ResourceLocation loc;
            mc.getTextureManager()
               .bindTexture(
                  loc = this.upscaleTexture(
                     new ResourceLocation(
                        "vegaline/modules/jumpcircles/animated/animation"
                           + (groupIndex + 1)
                           + "/circleframe_"
                           + ++framesCounter
                           + "."
                           + groupsFramesFormat[groupIndex]
                     )
                  )
               );
            this.animatedGroups.get(groupIndex).add(loc);
         }
      }

      mc.getTextureManager().bindTexture(this.JUMP_CIRCLE = this.upscaleTexture(this.JUMP_CIRCLE));
      mc.getTextureManager().bindTexture(this.JUMP_KONCHAL = this.upscaleTexture(this.JUMP_KONCHAL));
      mc.getTextureManager().bindTexture(this.JUMP_INSUAL = this.upscaleTexture(this.JUMP_INSUAL));
      this.settings.add(this.MaxTime = new FloatSettings("MaxTime", 3500.0F, 8000.0F, 2000.0F, this));
      this.settings.add(this.Range = new FloatSettings("Range", 2.0F, 3.0F, 1.0F, this));
      this.settings
         .add(
            this.Texture = new ModeSettings("Texture", "Circle", this, new String[]{"Circle", "KonchalEbal", "Inusual", "CubicalPieces", "Emission", "Leeches"})
         );
      this.settings.add(this.ColorMode = new ModeSettings("ColorMode", "Rainbow", this, new String[]{"Client", "Rainbow", "Picker", "PickerFade"}));
      this.settings
         .add(this.PickColor1 = new ColorSettings("PickColor1", ColorUtils.getColor(255, 80, 0), this, () -> this.ColorMode.currentMode.contains("Picker")));
      this.settings
         .add(this.PickColor2 = new ColorSettings("PickColor2", ColorUtils.getColor(255, 142, 0), this, () -> this.ColorMode.currentMode.endsWith("Fade")));
      this.settings.add(this.DeepestLight = new BoolSettings("DeepestLight", true, this));
   }

   public static void onEntityMove(Entity entityIn, Vec3d prev) {
      if (entityIn instanceof EntityPlayerSP base && base.isEntityAlive()) {
         double motionY = entityIn.posY - prev.yCoord;
         double[] motions = new double[]{0.42F, 0.20000004768365898};
         if (MoveMeHelp.isBlockAboveHead(entityIn)) {
            motions = new double[]{0.42F, 0.20000004768365898, 0.20000005F, 0.07840000152587834};
         }

         boolean spawn = false;
         double[] var7 = motions;
         int var8 = motions.length;

         for (int var9 = 0; var9 < var8; var9++) {
            Double cur = var7[var9];
            if (MathUtils.getDifferenceOf(motionY, cur) < 0.001) {
               spawn = true;
            }
         }

         if (!entityIn.onGround && entityIn.onGround != entityIn.rayGround && motionY > 0.0 || spawn) {
            addCircleForEntity(entityIn);
         }

         entityIn.rayGround = entityIn.onGround;
      }
   }

   private static void addCircleForEntity(Entity entity) {
      Vec3d vec = getVec3dFromEntity(entity).addVector(0.0, 0.001, 0.0);
      BlockPos pos = new BlockPos(vec);
      IBlockState state = mc.world.getBlockState(pos);
      if (state.getBlock() == Blocks.SNOW_LAYER) {
         vec = vec.addVector(0.0, 0.125, 0.0);
      }

      circles.add(new JumpCircles.JumpRenderer(vec, circles.size()));
   }

   @EventTarget
   public void onRender3d(Event3D event) {
      if (circles.size() != 0) {
         circles.removeIf(circle -> (double)circle.getDeltaTime() >= 1.0);
         if (!circles.isEmpty()) {
            boolean preBindTex = this.Texture.currentMode.equalsIgnoreCase("CubicalPieces") || this.Texture.currentMode.equalsIgnoreCase("Emission");
            float deepestLightAnim = this.DeepestLight.getAnimation();
            float immersiveStrengh = 0.0F;
            if (deepestLightAnim >= 0.003921569F) {
               String finalImmersiveStrengh = this.Texture.currentMode;
               switch (finalImmersiveStrengh) {
                  case "Circle":
                  case "Emission":
                     immersiveStrengh = 0.1F;
                     break;
                  case "KonchalEbal":
                  case "CubicalPieces":
                  case "Inusual":
                     immersiveStrengh = 0.075F;
                     break;
                  case "Leeches":
                     immersiveStrengh = 0.2F;
               }
            }

            float finalImmersiveStrengh = immersiveStrengh;
            this.setupDraw(
               () -> circles.forEach(
                     circle -> this.doCircle(
                           circle.pos,
                           (double)this.Range.getFloat(),
                           1.0F - circle.getDeltaTime(),
                           circle.getIndex() * 30,
                           !preBindTex,
                           deepestLightAnim,
                           finalImmersiveStrengh
                        )
                  ),
               preBindTex
            );
         }
      }
   }

   private int getColor(int index, float alphaPC) {
      String colorMode = this.ColorMode.currentMode;
      int color = 0;
      switch (colorMode) {
         case "Client":
            color = ClientColors.getColor1(index, alphaPC);
            break;
         case "Rainbow":
            color = ColorUtils.swapAlpha(ColorUtils.rainbowGui(0, (long)index), 255.0F * alphaPC);
            break;
         case "Picker":
            color = ColorUtils.swapAlpha(this.PickColor1.color, (float)ColorUtils.getAlphaFromColor(this.PickColor1.color) * alphaPC);
            break;
         case "PickerFade":
            color = ColorUtils.fadeColor(
               ColorUtils.swapAlpha(this.PickColor1.color, (float)ColorUtils.getAlphaFromColor(this.PickColor1.color) * alphaPC),
               ColorUtils.swapAlpha(this.PickColor2.color, (float)ColorUtils.getAlphaFromColor(this.PickColor2.color) * alphaPC),
               0.3F,
               (int)((float)index / 0.3F / 8.0F)
            );
      }

      return color;
   }

   private void doCircle(Vec3d pos, double maxRadius, float deltaTime, int index, boolean doBindTex, float immersiveShift, float immersiveIntense) {
      boolean immersive = immersiveShift >= 0.003921569F;
      float waveDelta = MathUtils.valWave01(1.0F - deltaTime);
      float alphaPC = (float)MathUtils.easeOutCirc((double)MathUtils.valWave01(1.0F - deltaTime));
      if (deltaTime < 0.5F) {
         alphaPC *= (float)MathUtils.easeInOutExpo((double)alphaPC);
      }

      float radius = (float)(
         (deltaTime > 0.5F ? MathUtils.easeOutElastic((double)(waveDelta * waveDelta)) : MathUtils.easeOutBounce((double)waveDelta)) * maxRadius
      );
      double rotate = MathUtils.easeInOutElastic((double)waveDelta) * 90.0 / (1.0 + (double)waveDelta);
      if (doBindTex) {
         mc.getTextureManager().bindTexture(this.jumpTexture(index, deltaTime));
      }

      this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      this.buffer.pos(0.0, 0.0).tex(0.0, 0.0).color(this.getColor(index, alphaPC)).endVertex();
      this.buffer.pos(0.0, (double)radius).tex(0.0, 1.0).color(this.getColor((int)(324.0F + (float)index), alphaPC)).endVertex();
      this.buffer.pos((double)radius, (double)radius).tex(1.0, 1.0).color(this.getColor((int)(648.0F + (float)index), alphaPC)).endVertex();
      this.buffer.pos((double)radius, 0.0).tex(1.0, 0.0).color(this.getColor((int)(972.0F + (float)index), alphaPC)).endVertex();
      GL11.glPushMatrix();
      GL11.glTranslated(pos.xCoord - (double)radius / 2.0, pos.yCoord, pos.zCoord - (double)radius / 2.0);
      GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
      RenderUtils.customRotatedObject2D(0.0F, 0.0F, radius, radius, rotate);
      this.tessellator.draw();
      GL11.glPopMatrix();
      if (immersive) {
         int[] colors = new int[]{
            this.getColor(index, alphaPC),
            this.getColor((int)(324.0F + (float)index), alphaPC),
            this.getColor((int)(648.0F + (float)index), alphaPC),
            this.getColor((int)(972.0F + (float)index), alphaPC)
         };
         this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         float polygons = 40.0F;
         float extMaxY = radius / 3.5F;
         float extMaxXZ = radius / 7.0F;
         float maxAPC = alphaPC;
         float minAPC = immersiveIntense * immersiveShift;

         for (int i = 1; i < (int)polygons; i++) {
            float iPC = (float)i / polygons;
            float extY = extMaxY * (float)i / polygons - extMaxY / polygons;
            float aPC;
            if (!((aPC = MathUtils.lerp(maxAPC * minAPC, 0.0F, iPC)) * 255.0F < 1.0F)) {
               float radiusPost = radius + (float)MathUtils.easeOutCirc((double)MathUtils.valWave01(iPC - 1.5F / polygons)) * extMaxXZ;
               this.buffer
                  .pos((double)(-radiusPost / 2.0F), (double)extY, (double)(-radiusPost / 2.0F))
                  .tex(0.0, 0.0)
                  .color(ColorUtils.toDark(colors[0], aPC))
                  .endVertex();
               this.buffer
                  .pos((double)(-radiusPost / 2.0F), (double)extY, (double)(radiusPost / 2.0F))
                  .tex(0.0, 1.0)
                  .color(ColorUtils.toDark(colors[1], aPC))
                  .endVertex();
               this.buffer
                  .pos((double)(radiusPost / 2.0F), (double)extY, (double)(radiusPost / 2.0F))
                  .tex(1.0, 1.0)
                  .color(ColorUtils.toDark(colors[2], aPC))
                  .endVertex();
               this.buffer
                  .pos((double)(radiusPost / 2.0F), (double)extY, (double)(-radiusPost / 2.0F))
                  .tex(1.0, 0.0)
                  .color(ColorUtils.toDark(colors[3], aPC))
                  .endVertex();
            }
         }

         GL11.glPushMatrix();
         GL11.glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
         GL11.glRotated(rotate, 0.0, -1.0, 0.0);
         this.tessellator.draw();
         GL11.glPopMatrix();
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived) {
         circles.clear();
      }

      super.onToggled(actived);
   }

   private static Vec3d getVec3dFromEntity(Entity entityIn) {
      float PT = mc.getRenderPartialTicks();
      double dx = entityIn.posX - entityIn.lastTickPosX;
      double dy = entityIn.posY - entityIn.lastTickPosY;
      double dz = entityIn.posZ - entityIn.lastTickPosZ;
      return new Vec3d(
         entityIn.lastTickPosX + dx * (double)PT + dx * 2.0, entityIn.lastTickPosY + dy * (double)PT, entityIn.lastTickPosZ + dz * (double)PT + dz * 2.0
      );
   }

   private void setupDraw(Runnable render, boolean preBindTex) {
      EntityRenderer renderer = mc.entityRenderer;
      Vec3d revert = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
      boolean light = GL11.glIsEnabled(2896);
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glEnable(3008);
      GL11.glAlphaFunc(516, 0.0F);
      GL11.glDepthMask(false);
      GL11.glDisable(2884);
      if (light) {
         GL11.glDisable(2896);
      }

      GL11.glShadeModel(7425);
      GL11.glBlendFunc(770, 32772);
      renderer.disableLightmap();
      GL11.glTranslated(-revert.xCoord, -revert.yCoord, -revert.zCoord);
      if (preBindTex) {
         mc.getTextureManager().bindTexture(this.jumpTexture(0, 0.0F));
      }

      render.run();
      GL11.glTranslated(revert.xCoord, revert.yCoord, revert.zCoord);
      GL11.glBlendFunc(770, 771);
      GL11.glColor3f(1.0F, 1.0F, 1.0F);
      GL11.glShadeModel(7424);
      if (light) {
         GL11.glEnable(2896);
      }

      GL11.glEnable(2884);
      GL11.glDepthMask(true);
      GL11.glAlphaFunc(516, 0.1F);
      GL11.glEnable(3008);
      GL11.glPopMatrix();
   }

   private static final class JumpRenderer {
      private final long time = System.currentTimeMillis();
      private final Vec3d pos;
      int index;

      private JumpRenderer(Vec3d pos, int index) {
         this.pos = pos;
         this.index = index;
      }

      private float getDeltaTime() {
         return (float)(System.currentTimeMillis() - this.time) / JumpCircles.get.MaxTime.getFloat();
      }

      private int getIndex() {
         return this.index;
      }
   }
}
