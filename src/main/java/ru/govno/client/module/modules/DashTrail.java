package ru.govno.client.module.modules;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class DashTrail extends Module {
   public static DashTrail dash;
   public final BoolSettings Self;
   public final BoolSettings Players;
   public final BoolSettings Friends;
   private final ModeSettings ColorMode;
   private final ColorSettings PickColor;
   private final FloatSettings MaxDistToEntity;
   private final BoolSettings MotionsSmoothing;
   private final BoolSettings DashSegments;
   private final BoolSettings DashDots;
   private final BoolSettings Lighting;
   private final FloatSettings DashLength;
   private static final String locBloomsFolder = "vegaline/modules/dashtrail/";
   private static final String locCubicsFolder = "vegaline/modules/dashtrail/dashcubics/";
   private static final String locGroupTextures = "vegaline/modules/dashtrail/dashcubics/group_dashs/";
   private static final String format = ".png";
   private ResourceLocation DASH_CUBIC_BLOOM_TEX = new ResourceLocation("vegaline/modules/dashtrail/dashbloomsample.png");
   private final List<DashTrail.ResourceLocationWithSizes> DASH_CUBIC_TEXTURES = new ArrayList<>();
   private final List<List<DashTrail.ResourceLocationWithSizes>> DASH_CUBIC_ANIMATED_TEXTURES = new ArrayList<>();
   private final Random RANDOM = new Random();
   private boolean isRandomPaletteColor;
   private boolean isClientColor;
   private boolean isPickerColor;
   private int anotherColor = -1;
   private final List<DashTrail.DashCubic> DASH_CUBICS = new ArrayList<>();
   private final Tessellator tessellator = Tessellator.getInstance();
   private final BufferBuilder buffer = this.tessellator.getBuffer();
   ResourceLocation lastBinded = null;

   private void addAll_DASH_CUBIC_TEXTURES() {
      if (!this.DASH_CUBIC_TEXTURES.isEmpty()) {
         this.DASH_CUBIC_TEXTURES.clear();
      }

      int dashTexturesCount = 21;
      int ct = 0;

      while (ct < dashTexturesCount) {
         ResourceLocation resourceLocation = new ResourceLocation("vegaline/modules/dashtrail/dashcubics/dashcubic" + ++ct + ".png");
         this.DASH_CUBIC_TEXTURES.add(new DashTrail.ResourceLocationWithSizes(resourceLocation));
         mc.getTextureManager().bindTexture(resourceLocation);
         System.out.println("vegaline/modules/dashtrail/dashcubics/dashcubic" + ct + ".png");
      }

      try {
         DynamicTexture dynamicTexture = new DynamicTexture(
            TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(this.DASH_CUBIC_BLOOM_TEX).getInputStream())
         );
         dynamicTexture.setBlurMipmap(true, false);
         this.DASH_CUBIC_BLOOM_TEX = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(dynamicTexture.toString(), dynamicTexture);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   private void addAll_DASH_CUBIC_ANIMATED_TEXTURES() {
      if (!this.DASH_CUBIC_ANIMATED_TEXTURES.isEmpty()) {
         this.DASH_CUBIC_ANIMATED_TEXTURES.clear();
      }

      int[] dashGroupsNumber = new int[]{11, 23, 32, 16, 32};
      int packageNumber = 0;
      int[] var3 = dashGroupsNumber;
      int var4 = dashGroupsNumber.length;

      for (int var5 = 0; var5 < var4; var5++) {
         Integer dashFragsNumber = var3[var5];
         packageNumber++;
         List<DashTrail.ResourceLocationWithSizes> animatedTexuresList = new ArrayList<>();
         int fragNumber = 0;

         while (fragNumber < dashFragsNumber) {
            ResourceLocation resourceLocation = new ResourceLocation(
               "vegaline/modules/dashtrail/dashcubics/group_dashs/group" + packageNumber + "/dashcubic" + ++fragNumber + ".png"
            );
            animatedTexuresList.add(new DashTrail.ResourceLocationWithSizes(resourceLocation));
            mc.getTextureManager().bindTexture(resourceLocation);
            System.out.println("vegaline/modules/dashtrail/dashcubics/group_dashs/group" + packageNumber + "/dashcubic" + fragNumber + ".png");
         }

         if (!animatedTexuresList.isEmpty()) {
            this.DASH_CUBIC_ANIMATED_TEXTURES.add(animatedTexuresList);
         }
      }
   }

   public DashTrail() {
      super("DashTrail", 0, Module.Category.RENDER);
      this.addAll_DASH_CUBIC_TEXTURES();
      this.addAll_DASH_CUBIC_ANIMATED_TEXTURES();
      dash = this;
      this.settings.add(this.Self = new BoolSettings("Self", true, this));
      this.settings.add(this.Players = new BoolSettings("Players", false, this));
      this.settings.add(this.Friends = new BoolSettings("Friends", true, this));
      this.settings
         .add(
            this.ColorMode = new ModeSettings(
               "ColorMode",
               "RandomPalette",
               this,
               new String[]{"RandomPalette", "Custom", "Client", "Rainbow"},
               () -> this.Self.getBool() || this.Players.getBool() || this.Friends.getBool()
            )
         );
      this.settings
         .add(
            this.PickColor = new ColorSettings(
               "PickColor",
               ColorUtils.getColor(170, 40, 255),
               this,
               () -> this.ColorMode.currentMode.equalsIgnoreCase("Custom") && (this.Self.getBool() || this.Players.getBool() || this.Friends.getBool())
            )
         );
      this.settings
         .add(this.MaxDistToEntity = new FloatSettings("MaxDistToEntity", 25.0F, 100.0F, 15.0F, this, () -> this.Players.getBool() || this.Friends.getBool()));
      this.settings
         .add(
            this.MotionsSmoothing = new BoolSettings(
               "MotionsSmoothing", false, this, () -> this.Self.getBool() || this.Players.getBool() || this.Friends.getBool()
            )
         );
      this.settings
         .add(this.DashSegments = new BoolSettings("DashSegments", false, this, () -> this.Self.getBool() || this.Players.getBool() || this.Friends.getBool()));
      this.settings
         .add(this.DashDots = new BoolSettings("DashDots", true, this, () -> this.Self.getBool() || this.Players.getBool() || this.Friends.getBool()));
      this.settings
         .add(this.Lighting = new BoolSettings("Lighting", true, this, () -> this.Self.getBool() || this.Players.getBool() || this.Friends.getBool()));
      this.settings
         .add(
            this.DashLength = new FloatSettings(
               "DashLength", 0.75F, 1.5F, 0.5F, this, () -> this.Self.getBool() || this.Players.getBool() || this.Friends.getBool()
            )
         );
      this.RANDOM.setSeed(1234567891L);
   }

   private int getColorDashCubic() {
      return this.isRandomPaletteColor
         ? Color.getHSBColor((float)this.RANDOM.nextInt(255) / 255.0F, 1.0F, 1.0F).getRGB()
         : (this.isClientColor ? ClientColors.getColor1() : (this.isPickerColor ? this.PickColor.color : this.anotherColor));
   }

   private int[] getTextureResolution(ResourceLocation location) {
      try {
         InputStream stream = mc.getResourceManager().getResource(location).getInputStream();
         BufferedImage image = ImageIO.read(stream);
         return new int[]{image.getWidth(), image.getHeight()};
      } catch (Exception var4) {
         var4.printStackTrace();
         return new int[]{0, 0};
      }
   }

   private int randomTextureNumber() {
      return this.RANDOM.nextInt(this.DASH_CUBIC_TEXTURES.size());
   }

   private int randomAnimatedTexturesGroupNumber() {
      return this.RANDOM.nextInt(this.DASH_CUBIC_ANIMATED_TEXTURES.size());
   }

   private DashTrail.ResourceLocationWithSizes getDashCubicTextureRandom(int random) {
      return this.DASH_CUBIC_TEXTURES.get(random);
   }

   private List<DashTrail.ResourceLocationWithSizes> getDashCubicAnimatedTextureGroupRandom(int random) {
      return this.DASH_CUBIC_ANIMATED_TEXTURES.get(random);
   }

   private boolean hasChancedAnimatedTexutreSet() {
      return this.RANDOM.nextInt(100) > 40;
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

   private void setDashElementsRender(Runnable render, boolean texture2d, boolean bloom) {
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glAlphaFunc(516, 0.003921569F);
      GlStateManager.resetColor();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA,
         bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
         GlStateManager.SourceFactor.ONE,
         GlStateManager.DestFactor.ZERO
      );
      GL11.glEnable(3042);
      GL11.glLineWidth(1.0F);
      if (!texture2d) {
         GL11.glDisable(3553);
      } else {
         GL11.glEnable(3553);
      }

      GlStateManager.disableLight(0);
      GlStateManager.disableLight(1);
      GlStateManager.disableColorMaterial();
      mc.entityRenderer.disableLightmap();
      GL11.glDisable(2896);
      GL11.glShadeModel(7425);
      GL11.glDisable(3008);
      GL11.glDisable(2884);
      GL11.glDepthMask(false);
      render.run();
      GL11.glDepthMask(true);
      GL11.glEnable(2884);
      GL11.glAlphaFunc(516, 0.1F);
      GL11.glEnable(3008);
      GL11.glLineWidth(1.0F);
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      GlStateManager.resetColor();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GL11.glPopMatrix();
   }

   private List<DashTrail.DashCubic> DASH_CUBICS_FILTERED() {
      return this.DASH_CUBICS.stream().filter(Objects::nonNull).filter(dashCubic -> dashCubic.alphaPC.getAnim() > 0.05F).toList();
   }

   @Override
   public void alwaysUpdate() {
      if (this.actived) {
         this.stateAnim.to = 1.0F;
      }

      if (!(this.stateAnim.getAnim() < 0.05F)) {
         this.DASH_CUBICS
            .stream()
            .filter(dashCubicx -> dashCubicx.getTimePC() >= 1.0F && dashCubicx.alphaPC.to != 0.0F)
            .forEach(dashCubicx -> dashCubicx.alphaPC.to = 0.0F);
         this.DASH_CUBICS
            .removeIf(dashCubicx -> dashCubicx.getTimePC() >= 1.0F && dashCubicx.alphaPC.to == 0.0F && (double)dashCubicx.alphaPC.getAnim() < 0.02);
         List<DashTrail.DashCubic> filteredCubics = this.DASH_CUBICS_FILTERED();
         int next = 0;
         int max = this.MotionsSmoothing.getBool() ? filteredCubics.size() : -1;

         for (DashTrail.DashCubic dashCubic : filteredCubics) {
            next++;
            dashCubic.motionCubicProcess(next < max ? filteredCubics.get(next) : null);
         }
      }
   }

   private int getRandomTimeAnimationPerTime() {
      return (int)((float)(550 + this.RANDOM.nextInt(300)) * this.DashLength.getFloat());
   }

   public void onEntityMove(EntityLivingBase baseIn, Vec3d prev) {
      if (this.actived) {
         if (baseIn != null) {
            if (!this.Self.getBool() && !this.Players.getBool() && !this.Friends.getBool()) {
               this.toggle(false);
               Client.msg("§f§lModules:§r §7[§l" + this.getName() + "§r§7]: §7First enable something in the settings.", false);
            } else if (this.Self.getBool() && baseIn instanceof EntityPlayerSP
               || (this.Players.getBool() || this.Friends.getBool())
                  && baseIn instanceof EntityOtherPlayerMP mp
                  && mc.getRenderViewEntity().getDistanceToEntity(mp) <= this.MaxDistToEntity.getFloat()
                  && (
                     this.Players.getBool() && !Client.friendManager.isFriend(mp.getName())
                        || this.Friends.getBool() && Client.friendManager.isFriend(mp.getName())
                  )) {
               float dashVelocitySpeed = 0.04F;
               Vec3d pos = baseIn.getPositionVector();
               double dx = pos.xCoord - prev.xCoord;
               double dy = pos.yCoord - prev.yCoord;
               double dz = pos.zCoord - prev.zCoord;
               double entitySpeed = Math.sqrt(dx * dx + dy * dy + dz * dz);
               double entitySpeedXZ = Math.sqrt(dx * dx + dz * dz);
               if (!(entitySpeedXZ < 0.04F)) {
                  boolean animated = true;
                  if (baseIn != null) {
                     boolean[] dashDops = this.getDashPops();
                     int countMax = MathUtils.clamp((int)(entitySpeed / 0.045), 1, 16);

                     for (int count = 0; count < countMax; count++) {
                        this.DASH_CUBICS
                           .add(
                              new DashTrail.DashCubic(
                                 new DashTrail.DashBase(
                                    baseIn, 0.04F, new DashTrail.DashTexture(animated), (float)count / (float)countMax, this.getRandomTimeAnimationPerTime()
                                 ),
                                 dashDops[0] || dashDops[1]
                              )
                           );
                     }
                  }
               }
            }
         }
      }
   }

   boolean[] getDashPops() {
      return new boolean[]{this.DashSegments.getBool(), this.DashDots.getBool()};
   }

   @Override
   public void alwaysRender3D(float partialTicks) {
      float alphaPC;
      if (!((alphaPC = this.stateAnim.getAnim()) < 0.05F)) {
         Frustum frustum = new Frustum(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
         this.isRandomPaletteColor = this.ColorMode.currentMode.equalsIgnoreCase("RandomPalette");
         this.isClientColor = this.ColorMode.currentMode.equalsIgnoreCase("Client");
         this.isPickerColor = this.ColorMode.currentMode.equalsIgnoreCase("Custom");
         if (!this.isRandomPaletteColor && !this.isClientColor && !this.isPickerColor) {
            this.anotherColor = Color.getHSBColor((float)(System.currentTimeMillis() % 1000L) / 1000.0F, 0.8F, 1.0F).getRGB();
         }

         boolean[] dashDops = this.getDashPops();
         List<DashTrail.DashCubic> FILTERED_LEVEL2_CUBICS = this.DASH_CUBICS_FILTERED()
            .stream()
            .filter(
               dashCubic -> frustum.isBoundingBoxInFrustum(
                     new AxisAlignedBB(dashCubic.getRenderPosX(partialTicks), dashCubic.getRenderPosY(partialTicks), dashCubic.getRenderPosZ(partialTicks))
                        .expandXyz(0.2 * (double)dashCubic.alphaPC.getAnim())
                  )
            )
            .toList();
         if (dashDops[0] || dashDops[1]) {
            GL11.glTranslated(-RenderManager.viewerPosX, -RenderManager.viewerPosY, -RenderManager.viewerPosZ);
            if (dashDops[1] && !FILTERED_LEVEL2_CUBICS.isEmpty()) {
               this.setDashElementsRender(
                  () -> {
                     GL11.glEnable(2832);
                     GL11.glPointSize(2.1F);
                     this.buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
                     FILTERED_LEVEL2_CUBICS.forEach(
                        dashCubic -> {
                           double[] renderDashPos = new double[]{
                              dashCubic.getRenderPosX(partialTicks), dashCubic.getRenderPosY(partialTicks), dashCubic.getRenderPosZ(partialTicks)
                           };
                           dashCubic.DASH_SPARKS_LIST
                              .forEach(
                                 spark -> {
                                    double[] renderSparkPos = new double[]{
                                       spark.getRenderPosX(partialTicks), spark.getRenderPosY(partialTicks), spark.getRenderPosZ(partialTicks)
                                    };
                                    float aPC = (float)(spark.alphaPC() * (double)dashCubic.alphaPC.anim);
                                    aPC = (float)MathUtils.easeInOutQuadWave((double)aPC);
                                    int c = ColorUtils.getOverallColorFrom(
                                       dashCubic.color, ColorUtils.swapAlpha(-1, (float)ColorUtils.getAlphaFromColor(dashCubic.color)), aPC
                                    );
                                    c = ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) * aPC / 1.33333F);
                                    this.buffer
                                       .pos(renderSparkPos[0] + renderDashPos[0], renderSparkPos[1] + renderDashPos[1], renderSparkPos[2] + renderDashPos[2])
                                       .color(c)
                                       .endVertex();
                                    this.buffer
                                       .pos(-renderSparkPos[0] + renderDashPos[0], -renderSparkPos[1] + renderDashPos[1], -renderSparkPos[2] + renderDashPos[2])
                                       .color(c)
                                       .endVertex();
                                 }
                              );
                        }
                     );
                     this.tessellator.draw();
                  },
                  false,
                  false
               );
            }

            if (dashDops[0]) {
               this.setDashElementsRender(
                  () -> FILTERED_LEVEL2_CUBICS.forEach(
                        dashCubic -> {
                           double[] renderDashPos = new double[]{
                              dashCubic.getRenderPosX(partialTicks), dashCubic.getRenderPosY(partialTicks), dashCubic.getRenderPosZ(partialTicks)
                           };
                           this.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                           dashCubic.DASH_SPARKS_LIST
                              .forEach(
                                 spark -> {
                                    double[] renderSparkPos = new double[]{
                                       spark.getRenderPosX(partialTicks), spark.getRenderPosY(partialTicks), spark.getRenderPosZ(partialTicks)
                                    };
                                    float aPC = (float)(spark.alphaPC() * (double)dashCubic.alphaPC.anim);
                                    aPC = (float)MathUtils.easeInOutQuadWave((double)aPC);
                                    int c = ColorUtils.getOverallColorFrom(
                                       dashCubic.color, ColorUtils.swapAlpha(-1, (float)ColorUtils.getAlphaFromColor(dashCubic.color)), 1.0F - aPC
                                    );
                                    c = ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) * aPC / 3.0F);
                                    this.buffer
                                       .pos(renderSparkPos[0] + renderDashPos[0], renderSparkPos[1] + renderDashPos[1], renderSparkPos[2] + renderDashPos[2])
                                       .color(c)
                                       .endVertex();
                                    this.buffer
                                       .pos(-renderSparkPos[0] + renderDashPos[0], -renderSparkPos[1] + renderDashPos[1], -renderSparkPos[2] + renderDashPos[2])
                                       .color(c)
                                       .endVertex();
                                 }
                              );
                           this.tessellator.draw();
                        }
                     ),
                  false,
                  true
               );
            }

            GL11.glTranslated(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
         }

         if (!FILTERED_LEVEL2_CUBICS.isEmpty()) {
            float lightingPC = this.Lighting.getAnimation();
            this.setDashElementsRender(() -> {
               GL11.glTranslated(-RenderManager.viewerPosX, -RenderManager.viewerPosY, -RenderManager.viewerPosZ);
               FILTERED_LEVEL2_CUBICS.forEach(dashCubic -> dashCubic.drawDash(partialTicks, false, alphaPC, lightingPC));
               this.bindResource(this.DASH_CUBIC_BLOOM_TEX);
               FILTERED_LEVEL2_CUBICS.forEach(dashCubic -> dashCubic.drawDash(partialTicks, true, alphaPC, lightingPC));
            }, true, true);
         }
      }
   }

   private boolean bindResource(ResourceLocation toBind) {
      if (toBind == this.lastBinded) {
         return false;
      } else {
         mc.getTextureManager().bindTexture(this.lastBinded = toBind);
         return true;
      }
   }

   private void addBindedTextureQuad(float x, float y, float x2, float y2, int c, int c2, int c3, int c4) {
      this.buffer.pos((double)x, (double)y).tex(0.0, 0.0).color(c).endVertex();
      this.buffer.pos((double)x, (double)y2).tex(0.0, 1.0).color(c2).endVertex();
      this.buffer.pos((double)x2, (double)y2).tex(1.0, 1.0).color(c3).endVertex();
      this.buffer.pos((double)x2, (double)y).tex(1.0, 0.0).color(c4).endVertex();
   }

   private void addBindedTextureQuad(float x, float y, float x2, float y2, int c) {
      this.addBindedTextureQuad(x, y, x2, y2, c, c, c, c);
   }

   private void drawQuads(boolean preVecs) {
      if (preVecs) {
         this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      } else {
         this.tessellator.draw();
      }
   }

   private void set3dDashPos(double[] renderPos, Runnable renderPart, float[] rotateImageValues) {
      GL11.glPushMatrix();
      GL11.glTranslated(renderPos[0], renderPos[1], renderPos[2]);
      GL11.glRotated((double)(-rotateImageValues[0]), 0.0, 1.0, 0.0);
      GL11.glRotated((double)rotateImageValues[1], mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0, 0.0, 0.0);
      GL11.glScaled(-0.1F, -0.1F, 0.1F);
      renderPart.run();
      GL11.glPopMatrix();
   }

   void addDashSparks(DashTrail.DashCubic cubic) {
      cubic.DASH_SPARKS_LIST.add(new DashTrail.DashSpark());
   }

   void dashSparksRemoveAuto(DashTrail.DashCubic cubic) {
      if (!cubic.DASH_SPARKS_LIST.isEmpty()) {
         if (cubic.addDops) {
            cubic.DASH_SPARKS_LIST.removeIf(DashTrail.DashSpark::toRemove);
         } else {
            cubic.DASH_SPARKS_LIST.clear();
         }
      }
   }

   private class DashBase {
      private EntityLivingBase entity;
      private double motionX;
      private double motionY;
      private double motionZ;
      private double posX;
      private double posY;
      private double posZ;
      private double prevPosX;
      private double prevPosY;
      private double prevPosZ;
      private int rMTime;
      private DashTrail.DashTexture dashTexture;

      private double eMotionX() {
         return -(this.entity.prevPosX - this.entity.posX);
      }

      private double eMotionY() {
         return -(this.entity.prevPosY - this.entity.posY);
      }

      private double eMotionZ() {
         return -(this.entity.prevPosZ - this.entity.posZ);
      }

      private DashBase(EntityLivingBase entity, float speedDash, DashTrail.DashTexture dashTexture, float offsetTickPC, int rmTime) {
         if (entity != null) {
            this.rMTime = rmTime;
            this.entity = entity;
            this.motionX = this.eMotionX();
            this.motionY = this.eMotionY();
            this.motionZ = this.eMotionZ();
            double randomizeVal = 0.7F;
            this.posX = entity.lastTickPosX - this.motionX * (double)offsetTickPC + -0.0875F + 0.175F * Math.random();
            this.posY = entity.lastTickPosY
               - this.motionY * (double)offsetTickPC
               + (double)entity.height / (entity.isLay ? 2.4 : 1.0) / 3.0
               + (double)entity.height / (entity.isLay ? 2.4 : 1.0) / 4.0 * Math.random() * 0.7F;
            this.posZ = entity.lastTickPosZ - this.motionZ * (double)offsetTickPC + -0.0875F + 0.175F * Math.random();
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionX *= (double)speedDash;
            this.motionY *= (double)speedDash;
            this.motionZ *= (double)speedDash;
            this.dashTexture = dashTexture;
         }
      }

      private int getMotionYaw() {
         int motionYaw = (int)Math.toDegrees(Math.atan2(this.motionZ, this.motionX) - 90.0);
         return motionYaw < 0 ? motionYaw + 360 : motionYaw;
      }
   }

   private class DashCubic {
      private final AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.035F);
      private final long startTime = System.currentTimeMillis();
      private final DashTrail.DashBase base;
      private final int color = DashTrail.this.getColorDashCubic();
      private final float[] rotate = new float[]{0.0F, 0.0F};
      List<DashTrail.DashSpark> DASH_SPARKS_LIST = new ArrayList<>();
      private final boolean addDops;

      private DashCubic(DashTrail.DashBase base, boolean addDops) {
         this.base = base;
         this.addDops = addDops;
         if (Math.sqrt(base.motionX * base.motionX + base.motionZ * base.motionZ) < 5.0E-4) {
            this.rotate[0] = (float)(360.0 * Math.random());
            this.rotate[1] = Module.mc.getRenderManager().playerViewX;
         } else {
            float motionYaw = (float)base.getMotionYaw();
            this.rotate[0] = motionYaw - 45.0F - 15.0F - (base.entity.prevRotationYaw - base.entity.rotationYaw) * 3.0F;
            float yawDiff = RotationUtil.getAngleDifference(motionYaw + 26.3F, base.entity.rotationYaw);
            this.rotate[1] = !(yawDiff < 10.0F) && !(yawDiff > 160.0F) ? Module.mc.getRenderManager().playerViewX : -90.0F;
         }
      }

      private double getRenderPosX(float pTicks) {
         return this.base.prevPosX + (this.base.posX - this.base.prevPosX) * (double)pTicks;
      }

      private double getRenderPosY(float pTicks) {
         return this.base.prevPosY + (this.base.posY - this.base.prevPosY) * (double)pTicks;
      }

      private double getRenderPosZ(float pTicks) {
         return this.base.prevPosZ + (this.base.posZ - this.base.prevPosZ) * (double)pTicks;
      }

      private float getTimePC() {
         return (float)(System.currentTimeMillis() - this.startTime) / (float)this.base.rMTime;
      }

      private boolean prevPosesIsNaN() {
         return this.base.prevPosX == 0.0 && this.base.prevPosY == 0.0 && this.base.prevPosZ == 0.0;
      }

      private double getPredictCoord(double pos1, double pos2, double predVal) {
         return pos1 + (pos1 - pos2) * (1.0 + predVal);
      }

      private void motionCubicProcess(@Nullable DashTrail.DashCubic nextCubic) {
         if (nextCubic != null && nextCubic.base.entity.getEntityId() != this.base.entity.getEntityId()) {
            nextCubic = null;
         }

         this.base.prevPosX = this.base.posX;
         this.base.prevPosY = this.base.posY;
         this.base.prevPosZ = this.base.posZ;
         float shareSpeed = 1.05F;
         float restandSpeed = 5.0F;
         this.base.posX = this.base.posX + 5.0 * (this.base.motionX = (nextCubic != null ? nextCubic.base.motionX : this.base.motionX) / 1.05F);
         this.base.posY = this.base.posY
            + 5.0 * (this.base.motionY = (nextCubic != null ? nextCubic.base.motionY : this.base.motionY) / 1.05F) / (this.base.motionY < 0.0 ? 1.0 : 3.5);
         this.base.posZ = this.base.posZ + 5.0 * (this.base.motionZ = (nextCubic != null ? nextCubic.base.motionZ : this.base.motionZ) / 1.05F);
         if (Math.sqrt(this.base.motionX * this.base.motionX + this.base.motionZ * this.base.motionZ) < 5.0E-4) {
            this.rotate[0] = (float)(360.0 * Math.random());
            this.rotate[1] = Module.mc.getRenderManager().playerViewX;
         } else {
            float motionYaw = (float)this.base.getMotionYaw();
            this.rotate[0] = motionYaw - 45.0F - 15.0F - (this.base.entity.prevRotationYaw - this.base.entity.rotationYaw) * 3.0F;
            float yawDiff = RotationUtil.getAngleDifference(motionYaw + 26.3F, this.base.entity.rotationYaw);
            this.rotate[1] = !(yawDiff < 10.0F) && !(yawDiff > 160.0F) ? Module.mc.getRenderManager().playerViewX : -90.0F;
         }

         if (this.addDops) {
            if ((double)this.getTimePC() < 0.3 && DashTrail.this.RANDOM.nextInt(12) > 5) {
               for (int i = 0; i < (DashTrail.this.getDashPops()[0] ? 1 : 3); i++) {
                  DashTrail.this.addDashSparks(this);
               }
            }

            this.DASH_SPARKS_LIST.forEach(DashTrail.DashSpark::motionSparkProcess);
         }

         DashTrail.this.dashSparksRemoveAuto(this);
      }

      private void drawDash(float partialTicks, boolean isBloomRenderer, float alphaPC, float lightingPC) {
         DashTrail.ResourceLocationWithSizes texureSized;
         if ((texureSized = this.base.dashTexture.getResourceWithSizes()) != null) {
            float aPC = this.alphaPC.anim * alphaPC;
            float scale = 0.03F * aPC;
            float extX = (float)texureSized.getResolution()[0] * scale;
            float extY = (float)texureSized.getResolution()[1] * scale;
            double[] renderPos = new double[]{this.getRenderPosX(partialTicks), this.getRenderPosY(partialTicks), this.getRenderPosZ(partialTicks)};
            if (isBloomRenderer) {
               DashTrail.this.set3dDashPos(
                  renderPos,
                  () -> {
                     float extXY = (float)Math.sqrt((double)(extX * extX + extY * extY));
                     float timePcOf = 1.0F - this.getTimePC();
                     timePcOf = timePcOf > 1.0F ? 1.0F : (timePcOf < 0.0F ? 0.0F : timePcOf);
                     DashTrail.this.drawQuads(true);
                     DashTrail.this.addBindedTextureQuad(
                        -extXY * 2.0F,
                        -extXY * 2.0F,
                        extXY * 2.0F,
                        extXY * 2.0F,
                        ColorUtils.swapAlpha(
                           ColorUtils.getOverallColorFrom(this.color, -1, 0.15F),
                           MathUtils.lerp(12.0F, 6.0F, lightingPC) * timePcOf * alphaPC + MathUtils.lerp(5.0F, 4.0F, lightingPC) * alphaPC
                        )
                     );
                     if (lightingPC != 0.0F) {
                        extXY *= 2.0F + 2.5F * timePcOf;
                        DashTrail.this.addBindedTextureQuad(
                           -extXY * 2.0F,
                           -extXY * 2.0F,
                           extXY * 2.0F,
                           extXY * 2.0F,
                           ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(this.color, -1, 0.15F), 2.0F * timePcOf * alphaPC + 2.0F * alphaPC * lightingPC)
                        );
                     }

                     DashTrail.this.drawQuads(false);
                  },
                  new float[]{Module.mc.getRenderManager().playerViewY, Module.mc.getRenderManager().playerViewX}
               );
            } else {
               DashTrail.this.set3dDashPos(
                  renderPos,
                  () -> {
                     DashTrail.this.bindResource(texureSized.getResource());
                     DashTrail.this.drawQuads(true);
                     DashTrail.this.addBindedTextureQuad(
                        -extX / 2.0F,
                        -extY / 2.0F,
                        extX / 2.0F,
                        extY / 2.0F,
                        ColorUtils.toDark(ColorUtils.getOverallColorFrom(this.color, -1, 0.4F), (float)MathUtils.easeInOutQuad((double)this.alphaPC.anim))
                     );
                     DashTrail.this.drawQuads(false);
                  },
                  this.rotate
               );
            }
         }
      }
   }

   private class DashSpark {
      double posX;
      double posY;
      double posZ;
      double prevPosX;
      double prevPosY;
      double prevPosZ;
      double speed = Math.random() / 50.0;
      double radianYaw = Math.random() * 360.0;
      double radianPitch = -90.0 + Math.random() * 180.0;
      long startTime = System.currentTimeMillis();

      DashSpark() {
      }

      double timePC() {
         return (double)MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / 1000.0F, 0.0F, 1.0F);
      }

      double alphaPC() {
         return 1.0 - this.timePC();
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

   private class DashTexture {
      private final List<DashTrail.ResourceLocationWithSizes> TEXTURES;
      private final boolean animated;
      private long timeAfterSpawn;
      private long animationPerTime;

      private boolean isAnimated() {
         return this.animated;
      }

      private DashTexture(boolean animated) {
         this.animated = animated && DashTrail.this.hasChancedAnimatedTexutreSet();
         if (this.animated) {
            this.timeAfterSpawn = System.currentTimeMillis();
            this.TEXTURES = DashTrail.this.getDashCubicAnimatedTextureGroupRandom(DashTrail.this.randomAnimatedTexturesGroupNumber());
            this.animationPerTime = (long)DashTrail.this.getRandomTimeAnimationPerTime();
         } else {
            this.TEXTURES = new ArrayList<>();
            this.TEXTURES.add(DashTrail.this.getDashCubicTextureRandom(DashTrail.this.randomTextureNumber()));
         }
      }

      private DashTrail.ResourceLocationWithSizes getResourceWithSizes() {
         if (this.isAnimated()) {
            float fragCount = (float)this.TEXTURES.size();
            if (fragCount > 0.0F) {
               int timeOfSpawn = (int)(System.currentTimeMillis() - this.timeAfterSpawn);
               float timePC = (float)(timeOfSpawn % (int)this.animationPerTime) / (float)this.animationPerTime;
               int fragNumber = (int)MathUtils.clamp(timePC * fragCount, 0.0F, fragCount);
               DashTrail.ResourceLocationWithSizes fragTexure = this.TEXTURES.get(fragNumber);
               if (fragTexure != null) {
                  return fragTexure;
               }
            }
         }

         return this.TEXTURES.get(0);
      }
   }

   private class ResourceLocationWithSizes {
      private ResourceLocation source;
      private final int[] resolution;

      private ResourceLocationWithSizes(ResourceLocation source) {
         this.source = source;

         try {
            DynamicTexture dynamicTexture = new DynamicTexture(
               TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(source).getInputStream())
            );
            dynamicTexture.setBlurMipmap(true, false);
            this.source = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(dynamicTexture.toString(), dynamicTexture);
         } catch (Exception var4) {
            var4.printStackTrace();
         }

         this.resolution = DashTrail.this.getTextureResolution(source);
      }

      private ResourceLocation getResource() {
         return this.source;
      }

      private int[] getResolution() {
         return this.resolution;
      }
   }
}
