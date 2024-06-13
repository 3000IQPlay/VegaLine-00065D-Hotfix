package ru.govno.client.module.modules;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.CrystalField;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.GaussianBlur;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class TargetHUD extends Module {
   public static TargetHUD get;
   public static EntityLivingBase curTarget = null;
   public static EntityLivingBase soundTarget = null;
   static ArrayList<TargetHUD.particle> particles = new ArrayList<>();
   public static float xPosHud;
   public static float yPosHud;
   public static float widthHud;
   public static float heightHud;
   AnimationUtils Scale = new AnimationUtils(1.0F, 1.0F, 0.07F);
   private float armorHealth;
   public ModeSettings Mode;
   public FloatSettings THudX;
   public FloatSettings THudY;
   public BoolSettings PreRangedTarget;
   public BoolSettings RaycastTarget;
   public BoolSettings CastPosition;
   public BoolSettings TargettingSFX;
   float hpRectAnim = 0.0F;
   private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
   private final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
   private final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
   String targetName = "";
   AnimationUtils alphaHp = new AnimationUtils(0.0F, 0.0F, 0.075F);
   final AnimationUtils hpAnim = new AnimationUtils(1.0F, 1.0F, 0.05F);
   final AnimationUtils absorbAnim = new AnimationUtils(0.0F, 0.0F, 0.05F);
   final AnimationUtils hurtHpAnim = new AnimationUtils(1.0F, 1.0F, 0.05F);
   private final int[] lastUpdatedResColors = new int[4];
   public static ResourceLocation skin = null;
   public static ResourceLocation OldSkin = null;
   int targetHurt;

   float Scale() {
      return this.Scale.getAnim();
   }

   float Scaleclamp() {
      return MathUtils.clamp(this.Scale(), 0.0F, 1.0F);
   }

   public TargetHUD() {
      super("TargetHUD", 0, Module.Category.COMBAT);
      this.settings
         .add(this.Mode = new ModeSettings("Mode", "Light", this, new String[]{"Light", "WetWorn", "Neomoin", "Modern", "Bushy", "Subtle", "Entire"}));
      this.settings.add(this.THudX = new FloatSettings("T-Hud X", 0.45F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.THudY = new FloatSettings("T-Hud Y", 0.6F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.PreRangedTarget = new BoolSettings("PreRangedTarget", true, this));
      this.settings.add(this.RaycastTarget = new BoolSettings("RaycastTarget", true, this));
      this.settings.add(this.CastPosition = new BoolSettings("CastPosition", false, this));
      this.settings.add(this.TargettingSFX = new BoolSettings("TargettingSFX", true, this));
      get = this;
   }

   private Vector3d project2D(int scaleFactor, double x, double y, double z) {
      GL11.glGetFloat(2982, this.modelview);
      GL11.glGetFloat(2983, this.projection);
      GL11.glGetInteger(2978, this.viewport);
      return GLU.gluProject((float)x, (float)y, (float)z, this.modelview, this.projection, this.viewport, this.vector)
         ? new Vector3d(
            (double)(this.vector.get(0) / (float)scaleFactor),
            (double)(((float)Display.getHeight() - this.vector.get(1)) / (float)scaleFactor),
            (double)this.vector.get(2)
         )
         : null;
   }

   float[] castPosition(EventRender2D event) {
      float xn = -1.0F;
      float yn = -1.0F;
      EntityLivingBase entity = curTarget;
      if (entity != null && entity != Minecraft.player) {
         ScaledResolution scaledResolution = event.getResolution();
         int scaleFactor = ScaledResolution.getScaleFactor();
         double x = RenderUtils.interpolate(entity.posX, entity.prevPosX, (double)event.getPartialTicks());
         double y = RenderUtils.interpolate(entity.posY, entity.prevPosY, (double)event.getPartialTicks());
         double z = RenderUtils.interpolate(entity.posZ, entity.prevPosZ, (double)event.getPartialTicks());
         double height = (double)entity.getEyeHeight() / (entity.isChild() ? 1.80042 : 1.0);
         AxisAlignedBB aabb = new AxisAlignedBB(x, y, z, x, y + height, z);
         Vector3d[] vectors = new Vector3d[]{
            new Vector3d(aabb.minX, aabb.minY, aabb.minZ),
            new Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
            new Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
            new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
            new Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
            new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
            new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
            new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
         };
         mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 1);
         Vector4d position = null;
         Vector3d[] vecList = vectors;
         int vecLength = vectors.length;

         for (int l = 0; l < vecLength; l++) {
            Vector3d vector = this.project2D(
               scaleFactor, vecList[l].x - RenderManager.viewerPosX, vecList[l].y - RenderManager.viewerPosY, vecList[l].z - RenderManager.viewerPosZ
            );
            if (vector != null && vector.z >= 0.0 && vector.z < 1.0) {
               if (position == null) {
                  position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
               }

               position.x = Math.min(vector.x, position.x);
               position.y = Math.min(vector.y, position.y);
               position.z = Math.max(vector.x, position.z);
               position.w = Math.max(vector.y, position.w);
            }
         }

         mc.entityRenderer.setupOverlayRendering();
         if (position != null) {
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;
            xn = (float)(posX + (endPosX - posX) / 2.0) - widthHud / 2.0F;
            yn = (float)posY + heightHud / 2.0F;
         }
      }

      return new float[]{xn, yn};
   }

   private boolean castPosIsValid(float[] position) {
      return position[0] != -1.0F && position[1] != -1.0F;
   }

   void updatePosition(EventRender2D event) {
      float animSpeed = (float)Minecraft.frameTime * 0.035F;
      float animSpeedCast = (float)Minecraft.frameTime * 0.0075F;
      float[] castedPosition = new float[]{-1.0F, -1.0F};
      if (this.CastPosition.getBool()) {
         castedPosition = this.castPosition(event);
      }

      if (this.CastPosition.getBool()
         && castedPosition[0] > 0.0F
         && castedPosition[0] < (float)event.getResolution().getScaledWidth() - widthHud
         && castedPosition[1] > 0.0F
         && castedPosition[1] < (float)event.getResolution().getScaledHeight() - heightHud
         && !(mc.currentScreen instanceof GuiChat)
         && getTarget() != null
         && this.castPosIsValid(castedPosition)) {
         xPosHud = MathUtils.harp(xPosHud, castedPosition[0], animSpeedCast);
         yPosHud = MathUtils.harp(yPosHud, castedPosition[1], animSpeedCast);
      } else {
         xPosHud = MathUtils.harp(xPosHud, this.THudX.getFloat() * (float)event.getResolution().getScaledWidth() - widthHud / 2.0F, animSpeed);
         yPosHud = MathUtils.harp(yPosHud, this.THudY.getFloat() * (float)event.getResolution().getScaledHeight() - heightHud / 2.0F, animSpeed);
      }
   }

   void renderLight(EntityLivingBase target) {
      ResourceLocation res = OldSkin != null ? OldSkin : (skin != null ? skin : null);
      String hp = target.getHealth() == 0.0F ? "" : String.format("%.1f", this.hpRectAnim * target.getMaxHealth() + target.getAbsorptionAmount()) + "hp";
      float w = (float)((int)widthHud);
      float h = (float)((int)heightHud);
      CFontRenderer namefont = Fonts.mntsb_16;
      float x = xPosHud;
      float y = yPosHud;
      float texX = x + h;
      widthHud = MathUtils.clamp(texX - x + (float)namefont.getStringWidth(this.targetName) + 4.0F, 100.0F, 900.0F);
      heightHud = 38.0F;
      this.hpRectAnim = MathUtils.lerp(
         this.hpRectAnim, MathUtils.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F), 0.015F * (float)Minecraft.frameTime
      );
      float percScaled = MathUtils.clamp(this.Scale() * this.Scale(), 0.0F, 1.0F);
      int bgC = ColorUtils.swapAlpha(-1, 255.0F * percScaled);
      int roundC = ColorUtils.swapAlpha(-1, 225.0F * percScaled * percScaled);
      int bgSH = ColorUtils.swapAlpha(-1, 30.0F * percScaled);
      int bgSH2 = ColorUtils.swapAlpha(-1, 85.0F * percScaled * percScaled);
      int hpbgC = ColorUtils.swapAlpha(0, 55.0F * percScaled);
      int hpSC = ColorUtils.getColor(0, 0, 0, this.alphaHp.getAnim() * percScaled * percScaled);
      int texC = ColorUtils.swapAlpha(ColorUtils.getColor(20, 20, 20), 255.0F * percScaled);
      int itC = ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * percScaled);
      int hpC = ColorUtils.swapAlpha(ClientColors.getColor1(0), 255.0F * percScaled);
      int hpC2 = ColorUtils.getOverallColorFrom(hpC, ColorUtils.swapAlpha(ClientColors.getColor2(30), 255.0F * percScaled), this.hpRectAnim);
      int hpbgC2 = ColorUtils.swapAlpha(hpC, 85.0F * percScaled);
      int hpbgC3 = ColorUtils.swapAlpha(hpC2, 85.0F * percScaled);
      GL11.glPushMatrix();
      GL11.glTranslated(0.0, (double)(((this.Scale() > 1.0F ? this.Scale() * this.Scale() : this.Scale()) - 1.0F) * -8.0F), 0.0);
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         2.5F, 2.5F, h - 2.5F, h - 2.5F, 5.0F, 0.0F, -1, -1, -1, -1, false, true, false
      );
      StencilUtil.readStencilBuffer(0);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x + w, y + h, 5.0F, 1.25F, bgC, bgC, bgC, bgC, true, true, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x + w, y + h, 5.0F, 18.0F, bgSH, bgSH, bgSH, bgSH, true, false, true
      );
      StencilUtil.uninitStencilBuffer();
      GlStateManager.enableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );

      for (int i = 0; i < particles.size(); i++) {
         TargetHUD.particle particle = particles.get(i);
         float timePC = (float)particle.getTime() / 700.0F;
         if (timePC >= 1.0F) {
            particles.remove(particle);
         } else {
            int pc = ColorUtils.getOverallColorFrom(hpC, hpC2, timePC);
            int pColor = ColorUtils.swapAlpha(pc, 255.0F * (1.0F - timePC) * percScaled);
            particle.update(pColor);
         }
      }

      if (target.hurtTime > 8) {
         for (int count = 0; count < 2; count++) {
            particles.add(new TargetHUD.particle(x + h / 2.0F, y + h / 2.0F));
         }
      }

      if (res != null) {
         GL11.glTranslated((double)x, (double)y, 0.0);
         mc.getTextureManager().bindTexture(res);
         StencilUtil.initStencilToWrite();
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            2.5F, 2.5F, h - 2.5F, h - 2.5F, 5.0F, 0.0F, -1, -1, -1, -1, false, true, false
         );
         StencilUtil.readStencilBuffer(1);
         RenderUtils.glColor(
            ColorUtils.swapAlpha(
               ColorUtils.swapDark(ColorUtils.getColor(255, 255 - 80 * (target.hurtTime / 9), 255 - 80 * (target.hurtTime / 9)), 0.5F + percScaled / 2.0F),
               255.0F * percScaled * (0.5F + percScaled / 2.0F)
            )
         );
         Gui.drawScaledCustomSizeModalRect2(2.5F, 2.5F, 8.0F, 8.0F, 8.0F, 8.0F, h - 5.0F, h - 5.0F, 64.0F, 64.0F);
         StencilUtil.uninitStencilBuffer();
         GL11.glTranslated((double)(-x), (double)(-y), 0.0);
         RenderUtils.roundedFullRoundedOutline(x + 2.5F, y + 2.5F, x + h - 2.5F, y + h - 2.5F, 5.1F, 5.25F, 5.25F, 5.25F, roundC);
      }

      if (ColorUtils.getAlphaFromColor(texC) >= 28) {
         namefont.drawStringWithShadow(this.targetName, (double)texX, (double)(y + 4.5F), texC);
      }

      float hpX1 = x + h + 1.0F;
      float hpX2 = MathUtils.clamp(x + h + 1.0F + (w - h - 5.0F) * this.hpRectAnim, hpX1 + 4.0F, x + h + 1.0F + (w - h - 5.0F) * this.hpRectAnim);
      float hpX3 = x + h + 1.0F + (w - h - 5.0F);
      this.alphaHp.to = MathUtils.getDifferenceOf(hpX2, hpX3) > 9.0 ? 255.0F : 0.0F;
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         hpX1, y + h - 8.5F, hpX3, y + h - 4.0F, 2.0F, 1.0F, hpbgC, hpbgC, hpbgC, hpbgC, false, true, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         hpX1, y + h - 8.5F, hpX2, y + h - 4.0F, 2.0F, 3.0F, hpbgC2, hpbgC3, hpbgC3, hpbgC2, false, true, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         hpX1, y + h - 8.5F, hpX2, y + h - 4.0F, 2.0F, 0.5F, hpC, hpC2, hpC2, hpC, false, true, true
      );
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         hpX1, y + h - 8.5F, hpX3, y + h - 3.5F, 2.0F, 0.5F, -1, -1, -1, -1, false, true, false
      );
      StencilUtil.readStencilBuffer(1);
      if (ColorUtils.getAlphaFromColor(hpSC) >= 28) {
         Fonts.mntsb_12.drawString(hp, (double)(hpX2 + 1.0F), (double)(y + h - 7.0F), hpSC);
      }

      StencilUtil.uninitStencilBuffer();
      int stacksCount = 0;
      ItemStack offhand = target.getHeldItemOffhand();
      ItemStack boots = target.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      ItemStack leggings = target.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
      ItemStack body = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      ItemStack helm = target.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
      ItemStack inHand = target.getHeldItemMainhand();
      ItemStack[] stuff = new ItemStack[]{offhand, inHand, boots, leggings, body, helm};
      ArrayList<ItemStack> stacks = new ArrayList<>();
      int j = 0;

      for (ItemStack ix : stuff) {
         if (ix != null) {
            ix.getItem();
            stacks.add(ix);
         }
      }

      for (ItemStack stack : stacks) {
         if (!stack.isEmpty()) {
            stacksCount++;
         }
      }

      if (stacksCount != 0) {
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x + h + 2.0F, y + h - 23.0F, x + h + 4.0F + (float)stacksCount * 8.6F, y + h - 13.0F, 4.0F, 0.5F, itC, itC, itC, itC, false, true, true
         );
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x + h + 2.0F, y + h - 23.0F, x + h + 4.0F + (float)stacksCount * 8.6F, y + h - 13.0F, 4.0F, 5.5F, hpbgC, hpbgC, hpbgC, hpbgC, false, false, true
         );
      }

      RenderItem itemRender = mc.getRenderItem();
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableDepth();
      float xn = h - 5.0F;
      float yn = 16.0F;
      GL11.glTranslated((double)x, (double)y, 0.0);
      xn *= 2.0F;
      yn *= 2.0F;
      GL11.glScaled(0.5, 0.5, 0.5);

      for (ItemStack stackx : stacks) {
         if (!stackx.isEmpty()) {
            xn += 17.0F;
         }

         GL11.glTranslated((double)xn, (double)yn, 0.0);
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled((double)(percScaled * percScaled), (double)(percScaled * percScaled), 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         itemRender.zLevel = 200.0F;
         itemRender.renderItemAndEffectIntoGUI(stackx, 0, 0);
         if (255.0F * percScaled >= 26.0F) {
            itemRender.renderItemOverlayIntoGUIWithTextColor(
               Fonts.minecraftia_16,
               stackx,
               0,
               0,
               stackx.getCount(),
               ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), MathUtils.clamp(255.0F * percScaled, 30.0F, 255.0F))
            );
         }

         RenderUtils.drawItemWarnIfLowDur(stackx, 0.0F, 0.0F, percScaled, 1.0F);
         itemRender.zLevel = 0.0F;
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled((double)(1.0F / (percScaled * percScaled)), (double)(1.0F / (percScaled * percScaled)), 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         GL11.glTranslated((double)(-xn), (double)(-yn), 0.0);
      }

      GL11.glScaled(2.0, 2.0, 2.0);
      xn /= 2.0F;
      yn /= 2.0F;
      GL11.glTranslated((double)(-x), (double)(-y), 0.0);
      RenderHelper.disableStandardItemLighting();
      GL11.glPopMatrix();
   }

   void renderWetWorn(EntityLivingBase target) {
      new ScaledResolution(mc);
      ResourceLocation res = OldSkin != null ? OldSkin : (skin != null ? skin : null);
      float aPC = this.Scaleclamp();
      String hp = target.getHealth() == 0.0F ? "" : String.format("%.1f", this.hpRectAnim * target.getMaxHealth() + target.getAbsorptionAmount()) + "hp";
      float w = widthHud;
      float h = heightHud;
      CFontRenderer namefont = Fonts.mntsb_15;
      float x = xPosHud;
      float y = yPosHud;
      float texX = x + h;
      widthHud = MathUtils.clamp(texX - x + (float)namefont.getStringWidth(this.targetName) + 4.0F, 100.0F, 900.0F);
      heightHud = 36.0F;
      this.hpRectAnim = MathUtils.lerp(
         this.hpRectAnim, MathUtils.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F), 0.015F * (float)Minecraft.frameTime
      );
      this.armorHealth = MathUtils.lerp(this.armorHealth, this.getArmorPercent01(target), 0.015F * (float)Minecraft.frameTime);
      int stacksCount = 0;
      ItemStack offhand = target.getHeldItemOffhand();
      ItemStack boots = target.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      ItemStack leggings = target.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
      ItemStack body = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      ItemStack helm = target.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
      ItemStack inHand = target.getHeldItemMainhand();
      ItemStack[] stuff = new ItemStack[]{offhand, inHand, boots, leggings, body, helm};
      ArrayList<ItemStack> stacks = new ArrayList<>();
      int j = 0;

      for (ItemStack i : stuff) {
         if (i != null) {
            i.getItem();
            stacks.add(i);
         }
      }

      for (ItemStack stack : stacks) {
         if (!stack.isEmpty()) {
            stacksCount++;
         }
      }

      int bgOutC = ColorUtils.swapAlpha(ClientColors.getColor1(), (float)ColorUtils.getAlphaFromColor(ClientColors.getColor1()) * 0.85F * aPC * aPC);
      int bgOutC2 = ColorUtils.swapAlpha(ClientColors.getColor2(50), (float)ColorUtils.getAlphaFromColor(ClientColors.getColor2()) * 0.85F * aPC * aPC);
      int bgOutC3 = ColorUtils.swapAlpha(ClientColors.getColor2(150), (float)ColorUtils.getAlphaFromColor(ClientColors.getColor2()) * 0.85F * aPC * aPC);
      int bgOutC4 = ColorUtils.swapAlpha(ClientColors.getColor1(100), (float)ColorUtils.getAlphaFromColor(ClientColors.getColor1()) * 0.85F * aPC * aPC);
      int bgC1 = ColorUtils.getOverallColorFrom(bgOutC, ColorUtils.getColor(0, 0, 0, 100.0F * aPC), 0.7F);
      int bgC2 = ColorUtils.getOverallColorFrom(bgOutC2, ColorUtils.getColor(0, 0, 0, 100.0F * aPC), 0.7F);
      int hpBG = ColorUtils.getColor(0, 0, 0, 50.0F * aPC);
      int texC = ColorUtils.swapAlpha(-1, 200.0F * aPC * aPC);
      int texC2 = ColorUtils.swapAlpha(0, 175.0F * aPC);
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, 5.0F - aPC * 5.0F, 0.0F);
      RenderUtils.customScaledObject2D(x, y, w, h, this.Scale());
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x + w, y + h, 4.0F, 2.5F, bgOutC, bgOutC2, bgOutC3, bgOutC4, false, false, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x + w, y + h, 4.0F, 1.0F, bgC1, bgC2, bgC2, bgC1, false, true, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y, x + w, y + h, 4.0F, 1.5F, bgC1, bgC2, bgC2, bgC1, false, false, true
      );
      if (target.hurtTime > 4) {
         float rt = (float)(-5.0 * Math.random() + 10.0 * Math.random());
         float rt2 = (float)(-5.0 * Math.random() + 10.0 * Math.random());
         particles.add(new TargetHUD.particle(x + h / 2.0F + rt, y + h / 2.0F + rt2));
      }

      for (int ix = 0; ix < particles.size(); ix++) {
         TargetHUD.particle particle = particles.get(ix);
         float timePC = (float)particle.getTime() / 700.0F;
         if (timePC >= 1.0F) {
            particles.remove(particle);
         } else {
            int pc = ColorUtils.getOverallColorFrom(bgOutC, bgOutC2, timePC);
            int pColor = ColorUtils.swapAlpha(pc, 255.0F * (1.0F - timePC) * aPC);
            particle.update(pColor);
         }
      }

      RenderUtils.customScaledObject2D(x + 4.0F, y + 4.0F, h - 4.0F, h - 4.0F, 0.8F + aPC / 5.0F);
      if (res != null) {
         GL11.glTranslated((double)x, (double)y, 0.0);
         mc.getTextureManager().bindTexture(res);
         StencilUtil.initStencilToWrite();
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            4.0F, 4.0F, h - 4.0F, h - 4.0F, 4.5F, 1.0F, -1, -1, -1, -1, false, true, false
         );
         StencilUtil.readStencilBuffer(1);
         RenderUtils.glColor(
            ColorUtils.swapAlpha(
               ColorUtils.swapDark(
                  ColorUtils.getColor(255, (int)(255.0F - 80.0F * ((float)target.hurtTime / 9.0F)), (int)(255.0F - 80.0F * ((float)target.hurtTime / 9.0F))),
                  0.5F + aPC / 2.0F
               ),
               255.0F * aPC * aPC
            )
         );
         Gui.drawScaledCustomSizeModalRect2(4.0F, 4.0F, 8.0F, 8.0F, 8.0F, 8.0F, h - 8.0F, h - 8.0F, 64.0F, 64.0F);
         StencilUtil.uninitStencilBuffer();
         GL11.glTranslated((double)(-x), (double)(-y), 0.0);
         RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBoolShadowsBoolChangeShadowSize(
            x + 4.0F, y + 4.0F, x + h - 4.0F, y + h - 4.0F, 3.0F, 1.5F, 1.0F, bgOutC, bgOutC2, bgOutC3, bgOutC4, false, true, true
         );
      } else {
         Fonts.noise_24.drawString("?", (double)(x + h / 2.0F - 2.5F), (double)(y + h / 2.0F - 5.0F), bgOutC);
      }

      RenderUtils.customScaledObject2D(x + 4.0F, y + 4.0F, h - 4.0F, h - 4.0F, 1.0F / (0.8F + aPC / 5.0F));
      RenderUtils.customScaledObject2D(x + h + 2.0F, y + h - 13.0F, x + w - 4.0F - (x + h + 2.0F), 9.0F, 0.8F + aPC / 5.0F);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + h + 2.0F, y + h - 13.0F, x + w - 4.0F, y + h - 4.0F, 2.5F, 0.5F, hpBG, hpBG, hpBG, hpBG, false, true, true
      );
      float hp2x = x + h + 3.0F + (x + w - 5.0F - (x + h + 3.0F)) * this.hpRectAnim;
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRect((double)(x + h + 2.0F), (double)(y + h - 12.5F), (double)(hp2x + 1.0F), (double)(y + h - 4.5F), -1);
      StencilUtil.readStencilBuffer(1);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + h + 3.0F, y + h - 12.0F, x + w - 5.0F, y + h - 5.0F, 2.0F, 1.0F, bgOutC, bgOutC2, bgOutC3, bgOutC4, false, true, true
      );
      if (ColorUtils.getAlphaFromColor(texC2) >= 33) {
         Fonts.mntsb_12
            .drawString(
               hp,
               (double)MathUtils.clamp(
                  hp2x - (float)Fonts.mntsb_12.getStringWidth(hp),
                  x + h + 3.5F,
                  x + h + 3.0F + (x + w - 5.0F - (x + h + 3.0F)) * 0.5F - (float)(Fonts.mntsb_12.getStringWidth(hp) / 2) + 1.0F
               ),
               (double)(y + h - 9.5F),
               texC2
            );
      }

      StencilUtil.uninitStencilBuffer();
      RenderUtils.customScaledObject2D(x + h + 2.0F, y + h - 13.0F, x + w - 4.0F - (x + h + 2.0F), 9.0F, 1.0F / (0.8F + aPC / 5.0F));
      if (ColorUtils.getAlphaFromColor(texC) >= 33) {
         if (stacksCount == 0) {
            y += 5.0F;
         }

         RenderUtils.customScaledObject2D(
            x + h + 3.0F + (x + w - 5.0F - (x + h + 3.0F)) * 0.5F - (float)(Fonts.mntsb_13.getStringWidth(this.targetName) / 2),
            y + 6.0F,
            (float)Fonts.mntsb_13.getStringWidth(this.targetName),
            (float)(Fonts.mntsb_13.getStringHeight(this.targetName) / 2),
            0.8F + aPC / 5.0F
         );
         namefont.drawStringWithShadow(
            this.targetName,
            (double)(x + h + 3.0F + (x + w - 5.0F - (x + h + 3.0F)) * 0.5F - (float)namefont.getStringWidth(this.targetName) / 2.0F - 1.0F),
            (double)(y + 5.0F),
            texC
         );
         RenderUtils.customScaledObject2D(
            x + h + 3.0F + (x + w - 5.0F - (x + h + 3.0F)) * 0.5F - (float)(Fonts.mntsb_13.getStringWidth(this.targetName) / 2),
            y + 6.0F,
            (float)Fonts.mntsb_13.getStringWidth(this.targetName),
            (float)(Fonts.mntsb_13.getStringHeight(this.targetName) / 2),
            1.0F / (0.8F + aPC / 5.0F)
         );
         if (stacksCount == 0) {
            y -= 5.0F;
         }
      }

      RenderItem itemRender = mc.getRenderItem();
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableDepth();
      float xn = h - 5.0F;
      float yn = 12.0F;
      GL11.glTranslated((double)x, (double)y, 0.0);
      xn *= 2.0F;
      yn *= 2.0F;
      GL11.glScaled(0.5, 0.5, 0.5);

      for (ItemStack stackx : stacks) {
         if (!stackx.isEmpty()) {
            xn += 17.0F;
         }

         GL11.glTranslated((double)xn, (double)yn, 0.0);
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled((double)aPC, (double)aPC, 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         itemRender.zLevel = 200.0F;
         itemRender.renderItemAndEffectIntoGUI(stackx, 0, 0);
         if (ColorUtils.getAlphaFromColor(texC) >= 33) {
            itemRender.renderItemOverlayIntoGUIWithTextColor(Fonts.minecraftia_16, stackx, 0, 0, stackx.getCount(), texC);
         }

         RenderUtils.drawItemWarnIfLowDur(stackx, 0.0F, 0.0F, aPC, 1.0F);
         itemRender.zLevel = 0.0F;
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled((double)(1.0F / aPC), (double)(1.0F / aPC), 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         GL11.glTranslated((double)(-xn), (double)(-yn), 0.0);
      }

      GL11.glScaled(2.0, 2.0, 2.0);
      xn /= 2.0F;
      yn /= 2.0F;
      GL11.glTranslated((double)(-x), (double)(-y), 0.0);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.popMatrix();
   }

   private void neomoinCircle(float x, float y, float r, float lineWHP, float lineWDMG, float aPC, float pcHP, float pcDMG, boolean isBG, int colBG) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GlStateManager.shadeModel(7425);
      GL11.glScaled(0.5, 0.5, 0.5);
      GL11.glTranslated((double)x, (double)y, 0.0);
      GL11.glEnable(2832);
      GL11.glDisable(3008);
      GL11.glPointSize(lineWHP);
      GL11.glBegin(0);
      float startHP = isBG ? 0.0F : 180.0F;
      float endHP = isBG ? 360.0F : 180.0F + pcHP * 360.0F;
      float endDMG = endHP + (pcDMG - pcHP) * 360.0F;
      int step = isBG ? 15 : 3;
      float end = 0.0F;

      for (float i = startHP; i < endHP; i += (float)step) {
         end++;
         int c = isBG ? colBG : ClientColors.getColor1((int)(i * 3.0F));
         int color = ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) * aPC * aPC * aPC * 0.5F);
         double x1 = (double)x + Math.sin((double)i * Math.PI / 180.0) * (double)r * 2.0;
         double y1 = (double)y + Math.cos((double)i * Math.PI / 180.0) * (double)r * 2.0;
         RenderUtils.glColor(color);
         GL11.glVertex2d(x1, y1);
      }

      GL11.glEnd();
      GL11.glPointSize(lineWDMG);
      GL11.glBegin(0);
      if (!isBG) {
         float cucan = 0.0F;

         for (float i = endHP; i < endDMG; i += (float)step) {
            cucan++;
         }

         float cucan2 = 0.0F;

         for (float i = endHP; i < endDMG; i += (float)step) {
            int c = ClientColors.getColor1((int)(i * 3.0F));
            int color = ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) * aPC * aPC * aPC * (cucan2 / cucan) * 0.5F);
            double x1 = (double)x + Math.sin((double)i * Math.PI / 180.0) * (double)r * 2.0;
            double y1 = (double)y + Math.cos((double)i * Math.PI / 180.0) * (double)r * 2.0;
            RenderUtils.glColor(color);
            GL11.glVertex2d(x1, y1);
            cucan2++;
         }
      }

      GL11.glEnd();
      GL11.glTranslated((double)(-x), (double)(-y), 0.0);
      GL11.glScaled(2.0, 2.0, 2.0);
      GL11.glPointSize(1.0F);
      GL11.glDisable(2832);
      GL11.glEnable(3008);
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
      GlStateManager.resetColor();
   }

   private final void renderNeomoin(final EntityLivingBase target) {
      final ScaledResolution scale = new ScaledResolution(TargetHUD.mc);
      final ResourceLocation res = TargetHUD.skin;
      final float aPC = this.Scaleclamp();
      final String hp = (target.getHealth() == 0.0f) ? "" : (String.format("%.1f", this.hpRectAnim * target.getMaxHealth() + target.getAbsorptionAmount()) + "hp");
      final float w = TargetHUD.widthHud;
      final float h = TargetHUD.heightHud;
      final CFontRenderer namefont = Fonts.mntsb_13;
      final float x = TargetHUD.xPosHud;
      final float y = TargetHUD.yPosHud + (1.0f - aPC) * 3.0f;
      final float texX = x + h * 2.0f - 4.0f;
      int stacksCount = 0;
      final ItemStack offhand = target.getHeldItemOffhand();
      final ItemStack boots = target.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      final ItemStack leggings = target.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
      final ItemStack body = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      final ItemStack helm = target.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
      final ItemStack inHand = target.getHeldItemMainhand();
      final ItemStack[] stuff = { offhand, inHand, boots, leggings, body, helm };
      final ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
      int j;
      ItemStack[] array;
      int length;
      ItemStack i;
      for (j = 0, length = (array = stuff).length, j = 0; j < length; ++j) {
         i = array[j];
         if (i != null) {
            i.getItem();
            stacks.add(i);
         }
      }
      for (final ItemStack stack : stacks) {
         if (!stack.isEmpty()) {
            ++stacksCount;
         }
      }
      float wHD = texX - x + namefont.getStringWidth(this.targetName) + 4.0f;
      if (h * 2.0f + 8.5f * stacksCount + 5.0f > wHD) {
         wHD = h * 2.0f + 8.5f * stacksCount + 5.0f;
      }
      TargetHUD.widthHud = MathUtils.clamp(MathUtils.lerp(TargetHUD.widthHud, wHD, 0.01f * (float)Minecraft.frameTime), h * 2.0f, 900.0f);
      TargetHUD.heightHud = 30.0f;
      this.hpAnim.speed = 0.075f;
      this.hurtHpAnim.speed = 0.1f;
      this.hpAnim.to = MathUtils.clamp(target.getHealth() / target.getMaxHealth(), 0.0f, 1.0f);
      if (target.hurtTime < 3) {
         this.hurtHpAnim.to = MathUtils.clamp(target.getHealth() / target.getMaxHealth(), 0.0f, 1.0f);
      }
      if (this.hurtHpAnim.getAnim() < this.hpAnim.getAnim()) {
         this.hurtHpAnim.setAnim(this.hpAnim.getAnim());
      }
      final float hpRectAnim = this.hpAnim.getAnim();
      final float hurtRectAnim = this.hurtHpAnim.getAnim();
      final int accentV1 = ColorUtils.swapAlpha(ClientColors.getColor1(), ColorUtils.getAlphaFromColor(ClientColors.getColor1()) * aPC);
      final int accentV2 = ColorUtils.swapAlpha(ClientColors.getColor2(), ColorUtils.getAlphaFromColor(ClientColors.getColor2()) * aPC);
      final int accent1 = ColorUtils.swapAlpha(accentV1, ColorUtils.getAlphaFromColor(accentV1) * aPC);
      final int accent2 = ColorUtils.swapAlpha(accentV2, ColorUtils.getAlphaFromColor(accentV2) * aPC);
      final int bgc1 = ColorUtils.getOverallColorFrom(accent1, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(accent1)), 0.8f);
      final int bgc2 = ColorUtils.getOverallColorFrom(accent2, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(accent2)), 0.8f);
      final int bg1 = ColorUtils.swapAlpha(bgc1, ColorUtils.getAlphaFromColor(bgc1) * aPC / 2.2f);
      final int bg2 = ColorUtils.swapAlpha(bgc2, ColorUtils.getAlphaFromColor(bgc2) * aPC / 2.2f);
      final int bgOut1 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(accent1, bgc1, 0.6f), ColorUtils.getAlphaFromColor(bgc1) * aPC);
      final int bgOut2 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(accent2, bgc2, 0.6f), ColorUtils.getAlphaFromColor(bgc2) * aPC);
      final int headCol = ColorUtils.swapAlpha(ColorUtils.swapDark(ColorUtils.getColor(255, (int)(255.0f - 80.0f * (target.hurtTime / 9.0f)), (int)(255.0f - 80.0f * (target.hurtTime / 9.0f))), 0.5f + aPC / 2.0f), 255.0f * aPC * aPC);
      final int hpBgC = ColorUtils.getColor(11, 11, 11, 60.0f * aPC);
      final int hpBgCOut = ColorUtils.getColor(11, 11, 11, 100.0f * aPC);
      final int texCol = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0f * aPC * aPC);
      final int itemsBgCol = ColorUtils.getColor(11, 11, 11, 80.0f * aPC);
      GL11.glPushMatrix();
      RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBoolShadowsBoolChangeShadowSize(x, y, x + w, y + h, 3.5f, 2.0f, 6.0f, bg1, bg2, bg2, bg1, true, true, true);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x + w, y + h, 5.0f, 1.0f, bg1, bg2, bg2, bg1, false, true, true);
      if (target.hurtTime > 7) {
         final float rt = (float)(-5.0 * Math.random() + 10.0 * Math.random());
         final float rt2 = (float)(-5.0 * Math.random() + 10.0 * Math.random());
         TargetHUD.particles.add(new particle(x + h / 2.0f + rt, y + h / 2.0f + rt2));
      }
      for (int k = 0; k < TargetHUD.particles.size(); ++k) {
         final particle particle = TargetHUD.particles.get(k);
         final float timePC = particle.getTime() / 700.0f;
         if (timePC >= 1.0f) {
            TargetHUD.particles.remove(particle);
         }
         else {
            final int pC = ColorUtils.swapAlpha(ClientColors.getColor1(k * 10), ColorUtils.getAlphaFromColor(ClientColors.getColor1(k * 10)) * aPC);
            final int pc1 = ColorUtils.swapAlpha(pC, ColorUtils.getAlphaFromColor(pC) * aPC * aPC);
            final int pColor = ColorUtils.swapAlpha(pc1, 255.0f * (1.0f - timePC) * aPC);
            particle.update(pColor);
         }
      }
      if (res != null) {
         StencilUtil.renderInStencil(() -> RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x + 3.0f, y + 3.0f, x + h - 3.0f, y + h - 3.0f, 3.8f, 0.0f, -1, -1, -1, -1, false, true, false), () -> {
            GL11.glTranslated(x, y, 0.0);
            TargetHUD.mc.getTextureManager().bindTexture(res);
            RenderUtils.glColor(headCol);
            Gui.drawScaledCustomSizeModalRect2(3.0f, 3.0f, 8.0f, 8.0f, 8.0f, 8.0f, h - 6.0f, h - 6.0f, 64.0f, 64.0f);
            GL11.glTranslated(-x, -y, 0.0);
            return;
         }, 1);
         RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBoolShadowsBoolChangeShadowSize(x + 3.0f, y + 3.0f, x + h - 3.0f, y + h - 3.0f, 3.0f, 1.0f, 1.0f, accent1, ColorUtils.getOverallColorFrom(accent1, accent2, h / w), ColorUtils.getOverallColorFrom(accent1, accent2, h / w), accent1, false, true, true);
      }
      else if (ColorUtils.getAlphaFromColor(bgOut1) >= 33) {
         Fonts.noise_24.drawString("?", x + h / 2.0f - 2.5f, y + h / 2.0f - 5.0f, bgOut1);
      }
      final float rPlus = MathUtils.clamp((hurtRectAnim - hpRectAnim) * (target.getMaxHealth() / 3.0f), 0.0f, 3.0f) * 1.2f;
      final float cx = x + w - h / 2.0f;
      final float cy = y + h / 2.0f;
      final float cr = h / 2.0f - 4.0f + rPlus;
      final float cwHP = 3.5f + rPlus;
      final float cwDMG = 2.0f + rPlus;
      this.neomoinCircle(cx, cy, cr, cwHP, cwDMG, aPC, 1.0f, 1.0f, true, hpBgCOut);
      this.neomoinCircle(cx, cy, cr, cwHP, cwDMG, aPC, hpRectAnim, hurtRectAnim, false, 0);
      final float r = 8.5f;
      RenderUtils.drawSmoothCircle(x + w - h / 2.0f, y + h / 2.0f, r, hpBgC);
      final String hpStr = String.format("%.1f", hpRectAnim * target.getMaxHealth()).replace(".", ",");
      if (ColorUtils.getAlphaFromColor(texCol) >= 33) {
         Fonts.mntsb_12.drawString(hpStr, x + w - h / 2.0f - Fonts.mntsb_12.getStringWidth(hpStr) / 2, y + h / 2.0f - 1.0f, texCol);
      }
      if (ColorUtils.getAlphaFromColor(texCol) >= 33) {
         namefont.drawString(this.targetName, x + h + 1.0f, y + 5.0f, texCol);
      }
      final RenderItem itemRender = TargetHUD.mc.getRenderItem();
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableDepth();
      float xn = h - 5.0f;
      float yn = 16.0f;
      if (stacksCount != 0) {
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x + h + 2.5f, y + yn - 1.0f, x + h + 4.0f + 8.5f * stacksCount, y + yn + 9.0f, 2.0f, 1.0f, itemsBgCol, itemsBgCol, itemsBgCol, itemsBgCol, false, true, true);
      }
      GlStateManager.enableDepth();
      GL11.glTranslated(x, y, 0.0);
      xn *= 2.0f;
      yn *= 2.0f;
      GL11.glScaled(0.5, 0.5, 0.5);
      for (final ItemStack stack2 : stacks) {
         if (!stack2.isEmpty()) {
            xn += 17.0f;
         }
         GL11.glTranslated(xn, yn, 0.0);
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled(aPC, aPC, 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         itemRender.zLevel = 200.0f;
         itemRender.renderItemAndEffectIntoGUI(stack2, 0, 0);
         if (ColorUtils.getAlphaFromColor(texCol) >= 32) {
            itemRender.renderItemOverlayIntoGUIWithTextColor(Fonts.minecraftia_16, stack2, 0, 0, stack2.getCount(), texCol);
         }
         RenderUtils.drawItemWarnIfLowDur(stack2, 0.0f, 0.0f, aPC, 1.0f);
         itemRender.zLevel = 0.0f;
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled(1.0f / aPC, 1.0f / aPC, 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         GL11.glTranslated(-xn, -yn, 0.0);
      }
      GL11.glScaled(2.0, 2.0, 2.0);
      xn /= 2.0f;
      yn /= 2.0f;
      GL11.glTranslated(-x, -y, 0.0);
      RenderHelper.disableStandardItemLighting();
      GL11.glPopMatrix();
   }

   private void renderModern(EntityLivingBase target) {
      new ScaledResolution(mc);
      if (OldSkin == null) {
         ResourceLocation var10000 = skin != null ? skin : null;
      }

      float aPC = this.Scaleclamp();
      float w = widthHud;
      float h = heightHud;
      CFontRenderer namefont = Fonts.noise_17;
      CFontRenderer hpfont = Fonts.mntsb_10;
      float x = xPosHud;
      float y = yPosHud + (1.0F - aPC) * 3.0F;
      widthHud = 86.0F;
      heightHud = 21.0F;
      float hOf = 14.0F;
      int texCol = ColorUtils.swapAlpha(-1, 255.0F * aPC * aPC);
      int bgOutC = ColorUtils.swapAlpha(0, 150.0F * aPC);
      int bgOutCBG = ColorUtils.swapAlpha(0, 45.0F * aPC);
      this.absorbAnim.to = target.getAbsorptionAmount();
      float hpMax = target.getMaxHealth() + this.absorbAnim.getAnim();
      float hpALLPC = (target.smoothHealth.getAnim() + this.absorbAnim.getAnim()) / hpMax;
      float absALLPC = this.absorbAnim.getAnim() / hpMax;
      float var35 = hpALLPC - absALLPC;
      String hpStr = target.getHealth() == 0.0F ? "" : String.format("%.1f", hpALLPC * hpMax) + "hp";
      GL11.glPushMatrix();
      float sclaePC = 0.45F + aPC * 0.55F;
      RenderUtils.customScaledObject2D(x, y, w, h, sclaePC);
      float round = 2.0F;
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y + hOf, x + w, y + h, round, 1.5F, bgOutC, bgOutC, bgOutC, bgOutC, false, false, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x, y + hOf, x + w, y + h, round, 0.0F, bgOutCBG, bgOutCBG, bgOutCBG, bgOutCBG, false, true, false
      );
      if (target.getHealth() != 0.0F) {
         float offsetX = 2.5F;
         float offsetY = 2.5F;
         float offsetDC = 0.0F;
         float hpYMin = y + hOf + offsetY;
         float hpYMax = y + h - offsetY;
         float hpMinX = x + offsetX;
         float hpMaxX = x + w - offsetX;
         float hpX1 = hpMinX + (w - offsetX * 2.0F) * var35;
         float hpX2 = hpMinX + (w - offsetX * 2.0F) * hpALLPC;
         int hpCol = ColorUtils.getColor(0, 255, 120, 255.0F * aPC);
         int absCol = ColorUtils.getColor(255, 220, 0, 255.0F * aPC);
         float grandX1 = MathUtils.clamp(hpX1 - offsetDC, hpMinX, hpMaxX);
         float grandX2 = MathUtils.clamp(hpX1 + offsetDC, hpMinX, hpMaxX);
         RenderUtils.drawRect((double)hpMinX, (double)hpYMin, (double)grandX1, (double)hpYMax, hpCol);
         hpCol = ColorUtils.swapAlpha(hpCol, (float)ColorUtils.getAlphaFromColor(hpCol) / 1.7F);
         RenderUtils.drawLightContureRectSidewaysSmooth((double)hpMinX, (double)hpYMin, (double)grandX1, (double)hpYMax, hpCol, 0);
         RenderUtils.drawRect((double)MathUtils.clamp(hpX1 + offsetDC, hpMinX, hpMaxX), (double)hpYMin, (double)hpX2, (double)hpYMax, absCol);
         absCol = ColorUtils.swapAlpha(absCol, (float)ColorUtils.getAlphaFromColor(hpCol) / 1.7F);
         RenderUtils.drawLightContureRectSidewaysSmooth(
            (double)MathUtils.clamp(hpX1 + offsetDC, hpMinX, hpMaxX), (double)hpYMin, (double)hpX2, (double)hpYMax, 0, absCol
         );
         if ((float)ColorUtils.getAlphaFromColor(texCol) / 2.0F >= 33.0F) {
            StencilUtil.initStencilToWrite();
            RenderUtils.drawRect((double)(x + 0.5F), (double)(y + hOf), (double)(x + w - 0.5F), (double)(y + h), -1);
            StencilUtil.readStencilBuffer(1);
            hpfont.drawString(
               hpStr, (double)(hpX2 + 2.0F), (double)(y + hOf + 3.5F), ColorUtils.swapAlpha(texCol, (float)ColorUtils.getAlphaFromColor(texCol) / 2.0F)
            );
            StencilUtil.uninitStencilBuffer();
         }
      } else if (ColorUtils.getAlphaFromColor(texCol) >= 33) {
         hpfont.drawString("KILLED", (double)(x + w / 2.0F - (float)hpfont.getStringWidth("KILLED") / 2.0F), (double)(y + hOf + 3.5F), texCol);
      }

      RenderUtils.customScaledObject2DPro(x, y, w, h, sclaePC * sclaePC, 1.0F);
      if (ColorUtils.getAlphaFromColor(texCol) >= 33) {
         namefont.drawStringWithShadow(
            this.targetName, (double)(x + w / 2.0F - (float)namefont.getStringWidth(this.targetName) / 2.0F), (double)(y + 2.0F), texCol
         );
      }

      GL11.glPopMatrix();
   }

   private void renderBushy(final EntityLivingBase target) {
      GL11.glPushMatrix();
      final ScaledResolution scale = new ScaledResolution(TargetHUD.mc);
      int stacksCount = 0;
      final ItemStack offhand = target.getHeldItemOffhand();
      final ItemStack boots = target.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      final ItemStack leggings = target.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
      final ItemStack body = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      final ItemStack helm = target.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
      final ItemStack inHand = target.getHeldItemMainhand();
      final ItemStack[] stuff = { offhand, inHand, boots, leggings, body, helm };
      final ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
      int j;
      ItemStack[] array;
      int length;
      ItemStack i;
      for (j = 0, length = (array = stuff).length, j = 0; j < length; ++j) {
         i = array[j];
         if (i != null) {
            i.getItem();
            stacks.add(i);
         }
      }
      for (final ItemStack stack : stacks) {
         if (!stack.isEmpty()) {
            ++stacksCount;
         }
      }
      final ResourceLocation res = (TargetHUD.OldSkin != null) ? TargetHUD.OldSkin : ((TargetHUD.skin != null) ? TargetHUD.skin : null);
      float alphaPC = this.Scaleclamp();
      alphaPC *= alphaPC;
      final CFontRenderer nameFont = Fonts.comfortaaBold_12;
      final float w = TargetHUD.widthHud;
      final float h = TargetHUD.heightHud;
      final float x = TargetHUD.xPosHud;
      final float y = TargetHUD.yPosHud;
      float texOrItemW = stacksCount * 8.0f + 0.5f;
      final float strW = (float)nameFont.getStringWidth(this.targetName);
      if (texOrItemW < strW) {
         texOrItemW = strW;
      }
      TargetHUD.widthHud = MathUtils.clamp(19.0f + texOrItemW, 60.0f, 1000.0f);
      TargetHUD.heightHud = 18.0f;
      final float hpHeight = 1.5f;
      final int colBG1 = ColorUtils.getColor(0, 0, 0, 180.0f * alphaPC / 2.0f);
      final int colBG2 = ColorUtils.getColor(0, 0, 0, 100.0f * alphaPC / 2.0f);
      final int texCol = ColorUtils.getColor(255, 255, 255, 255.0f * alphaPC);
      GL11.glTranslated(0.0, 10.0f - this.Scale() * 10.0f, 0.0);
      if (res != null) {
         RenderUtils.setupColor(ColorUtils.getOverallColorFrom(-1, ColorUtils.getColor(155, 0, 0), target.hurtTime / 15.0f), 255.0f * alphaPC * alphaPC);
         GL11.glTranslated(x, y, 0.0);
         GL11.glDisable(3008);
         GL11.glEnable(3042);
         Gui.drawScaledHead(res, 0, 0, 16, 16);
         GL11.glEnable(3008);
         GL11.glTranslated(-x, -y, 0.0);
         GlStateManager.resetColor();
         RenderUtils.drawAlphedRect(x, y + 16.0f, x + 16.0f, y + 16.5f, colBG1);
      }
      else if (255.0f * alphaPC >= 33.0f) {
         Fonts.mntsb_15.drawString("?", x + 5.0f, y + 6.0f, texCol);
      }
      RenderUtils.drawAlphedRect(x + ((res == null) ? 0 : 16), y, x + w, y + h - hpHeight, colBG1);
      RenderUtils.drawLightContureRectSmooth(x, y, x + w, y + h, colBG1);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x + w, y + h, 0.0f, 4.0f, colBG2, colBG2, colBG2, colBG2, false, false, true);
      if (255.0f * alphaPC >= 33.0f) {
         final float tw = w / 2.0f - 16.0f + 3.0f - strW / 2.0f + 16.0f + 5.5f;
         nameFont.drawString(this.targetName, x + tw, y + ((stacksCount == 0) ? 6.5f : 2.5f), texCol);
      }
      final RenderItem itemRender = TargetHUD.mc.getRenderItem();
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableDepth();
      float xn = 7.5f;
      float yn = 7.5f;
      final int itemsBgCol = ColorUtils.getColor(0, 0, 0, 40.0f * alphaPC);
      if (stacksCount != 0) {
         RenderUtils.drawAlphedRect(x + 16.5f, y + 7.5f, x + 16.5f + stacksCount * 8.5f, y + 16.0f, itemsBgCol);
      }
      GlStateManager.enableDepth();
      GL11.glTranslated(x, y, 0.0);
      xn *= 2.0f;
      yn *= 2.0f;
      GL11.glScaled(0.5, 0.5, 0.5);
      for (final ItemStack stack2 : stacks) {
         if (!stack2.isEmpty()) {
            xn += 17.0f;
         }
         GL11.glTranslated(xn, yn, 0.0);
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled(alphaPC, alphaPC, 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         itemRender.zLevel = 200.0f;
         itemRender.renderItemAndEffectIntoGUI(stack2, 0, 0);
         if (ColorUtils.getAlphaFromColor(texCol) >= 32) {
            itemRender.renderItemOverlayIntoGUIWithTextColor(Fonts.minecraftia_16, stack2, 0, 0, stack2.getCount(), texCol);
         }
         RenderUtils.drawItemWarnIfLowDur(stack2, 0.0f, 0.0f, alphaPC, 1.0f);
         itemRender.zLevel = 0.0f;
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled(1.0f / alphaPC, 1.0f / alphaPC, 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         GL11.glTranslated(-xn, -yn, 0.0);
      }
      GL11.glScaled(2.0, 2.0, 2.0);
      xn /= 2.0f;
      yn /= 2.0f;
      GL11.glTranslated(-x, -y, 0.0);
      RenderHelper.disableStandardItemLighting();
      this.absorbAnim.to = target.getAbsorptionAmount();
      final float hpMax = target.getMaxHealth() + this.absorbAnim.getAnim();
      final float hpALLPC2;
      float hpALLPC = hpALLPC2 = (target.smoothHealth.getAnim() + this.absorbAnim.getAnim()) / hpMax;
      final float absALLPC = this.absorbAnim.getAnim() / hpMax;
      hpALLPC -= absALLPC;
      final String hpStr = (target.getHealth() == 0.0f) ? "" : (String.format("%.1f", hpALLPC2 * hpMax) + "hp");
      final float hOf = TargetHUD.heightHud - hpHeight;
      final float offsetX = 0.0f;
      final float offsetY = 0.0f;
      final float offsetDC = 0.0f;
      final float hpYMin = y + hOf + offsetY;
      final float hpYMax = y + h - offsetY;
      final float hpMinX = x + offsetX;
      final float hpMaxX = x + w - offsetX;
      final float hpX1 = hpMinX + (w - offsetX * 2.0f) * hpALLPC;
      final float hpX2 = hpMinX + (w - offsetX * 2.0f) * hpALLPC2;
      final int hpCol = ColorUtils.getColor(0, 255, 120, 255.0f * alphaPC);
      final int absCol = ColorUtils.getColor(255, 220, 0, 255.0f * alphaPC);
      final float grandX1 = MathUtils.clamp(hpX1 - offsetDC, hpMinX, hpMaxX);
      final float grandX2 = MathUtils.clamp(hpX1 + offsetDC, hpMinX, hpMaxX);
      RenderUtils.drawAlphedRect(hpMinX, hpYMin, grandX1, hpYMax, hpCol);
      RenderUtils.drawAlphedRect(MathUtils.clamp(hpX1 + offsetDC, hpMinX, hpMaxX), hpYMin, hpX2, hpYMax, absCol);
      RenderUtils.drawAlphedRect(hpX2, hpYMin, x + w, hpYMax, colBG1);
      GL11.glPopMatrix();
   }

   private void renderSubtle(final EntityLivingBase target) {
      final ResourceLocation res = (TargetHUD.OldSkin != null) ? TargetHUD.OldSkin : ((TargetHUD.skin != null) ? TargetHUD.skin : null);
      this.absorbAnim.to = target.getAbsorptionAmount();
      final float targetHealth = target.getSmoothHealth() + this.absorbAnim.getAnim();
      final float targetHealtMax = target.getMaxHealth() + this.absorbAnim.anim;
      final float targetHealthPC = Math.max(Math.min(targetHealth / targetHealtMax, 1.0f), 0.0f);
      final String hpStr = String.format("%.1f", targetHealth).replace(".0", "") + "HP";
      final CFontRenderer hpFont = Fonts.comfortaaBold_12;
      if (target.getDisplayName() != null) {
         this.targetName = target.getDisplayName().getFormattedText().replace("  ", " ").replace("§l", "").replace("[]", "").replace("§k", "").replace("§m", "").replace("§n", "").replace("§o", "");
      }
      int stacksCount = 0;
      final ItemStack offhand = target.getHeldItemOffhand();
      final ItemStack boots = target.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      final ItemStack leggings = target.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
      final ItemStack body = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      final ItemStack helm = target.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
      final ItemStack inHand = target.getHeldItemMainhand();
      final ItemStack[] stuff = { offhand, inHand, boots, leggings, body, helm };
      final ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
      int j;
      ItemStack[] array;
      int length;
      ItemStack i;
      for (j = 0, length = (array = stuff).length, j = 0; j < length; ++j) {
         i = array[j];
         if (i != null) {
            i.getItem();
            stacks.add(i);
         }
      }
      for (final ItemStack stack : stacks) {
         if (!stack.isEmpty()) {
            ++stacksCount;
         }
      }
      float alphaPC = this.Scaleclamp();
      alphaPC *= alphaPC;
      final CFontRenderer nameFont = Fonts.comfortaaBold_12;
      final float w = TargetHUD.widthHud;
      final float h = TargetHUD.heightHud;
      final float x = (int)(TargetHUD.xPosHud * 2.0f) / 2.0f;
      final float y = TargetHUD.yPosHud;
      float texOrItemW = stacksCount * 8.0f + 0.5f;
      final float strW = (float)(nameFont.getStringWidth(this.targetName) - 22);
      if (texOrItemW < strW) {
         texOrItemW = strW;
      }
      TargetHUD.widthHud = MathUtils.clamp(h + 16.0f + hpFont.getStringWidth(hpStr) + texOrItemW, 100.0f, 1000.0f);
      TargetHUD.heightHud = 34.0f;
      final int col1 = ColorUtils.swapAlpha(ClientColors.getColor1(0), 40.0f * alphaPC);
      final int col2 = ColorUtils.swapAlpha(ClientColors.getColor2(-324), 60.0f * alphaPC);
      final int col3 = ColorUtils.swapAlpha(ClientColors.getColor2(0), 60.0f * alphaPC);
      final int col4 = ColorUtils.swapAlpha(ClientColors.getColor1(972), 60.0f * alphaPC);
      final int colS1 = ColorUtils.swapAlpha(ClientColors.getColor1(0), 50.0f * alphaPC);
      final int colS2 = ColorUtils.swapAlpha(ClientColors.getColor2(-324), 50.0f * alphaPC);
      final int colS3 = ColorUtils.swapAlpha(ClientColors.getColor2(0), 50.0f * alphaPC);
      final int colS4 = ColorUtils.swapAlpha(ClientColors.getColor1(972), 50.0f * alphaPC);
      final float round = 6.0f;
      final float shadow = 7.0f * alphaPC;
      final float blur = 1.0f + 3.0f * alphaPC;
      if (alphaPC < 0.1) {
         return;
      }
      GL11.glPushMatrix();
      this.hudScale(x, y, w, h);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x + w, y + h, round, 0.0f, col1, col2, col3, col4, false, true, false);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x + w, y + h, round, shadow, colS1, colS2, colS3, colS4, true, false, true);
      GL11.glPushMatrix();
      RenderUtils.customScaledObject2D(x, y, w, h, 1.0f / this.Scale());
      final float aPC = alphaPC;
      GaussianBlur.drawBlur(blur, () -> RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x + w * (1.0f - aPC) / 2.0f, y + h * (1.0f - aPC) / 2.0f, x + w - w * (1.0f - aPC) / 2.0f, y + h - h * (1.0f - aPC) / 2.0f, round * aPC, 0.0f, -1, -1, -1, -1, false, true, false));
      GL11.glPopMatrix();
      RenderUtils.drawInsideFullRoundedFullGradientShadowRectWithBloomBool(x, y, x + w, y + h, round - 2.0f, 2.0f, colS1, colS2, colS3, colS4, true);
      final int headSize = 24;
      final float headX = x + 5.0f;
      final float headY = y + 5.0f;
      if (res == null) {
         RenderUtils.drawRect(headX, headY, headX + headSize, headY + headSize, ColorUtils.getColor(0, 0, 0, 90.0f * alphaPC));
         Fonts.comfortaaBold_18.drawString("?", x + headSize / 2.0f + 2.5f, y + headSize / 2.0f + 2.0f, ColorUtils.getColor(255, 255, 255, 255.0f * alphaPC));
      }
      else {
         GlStateManager.enableTexture2D();
         RenderUtils.glColor(ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ColorUtils.getColor(255, 190, 190), MathUtils.clamp(target.hurtTime - TargetHUD.mc.getRenderPartialTicks(), 0.0f, 10.0f) / 10.0f), 255.0f * alphaPC));
         TargetHUD.mc.getTextureManager().bindTexture(res);
         GL11.glTranslated(headX, headY, 0.0);
         Gui.drawScaledCustomSizeModalRect(0.0f, 0.0f, 8.0f, 8.0f, 8.0f, 8.0f, (float)headSize, (float)headSize, 64.0f, 64.0f);
         Gui.drawScaledCustomSizeModalRect(-2.0f, -2.0f, 39.0f, 8.0f, 10.0f, 8.0f, headSize + 4.0f, headSize + 4.0f, 64.0f, 64.0f);
         GL11.glTranslated(-headX, -headY, 0.0);
         GlStateManager.resetColor();
         if (target.getHealth() == 0.0f) {
            RenderUtils.drawRect(x + 3.0f, y + 10.0f, x + headSize + 8.0f, y + h - 11.0f, ColorUtils.getColor(0, 0, 0, 195.0f * alphaPC));
            Fonts.mntsb_18.drawStringWithShadow("DEAD", x + 3.5f, y + headSize / 2.0f + 2.0f, ColorUtils.getColor(255, 65, 65, 255.0f * alphaPC));
         }
      }
      final float postHeadX = headX + headSize + 4.0f;
      RenderUtils.drawTwoAlphedSideways(postHeadX, y + 4.5f, postHeadX + 2.0f, y + h - 4.5f, ColorUtils.getColor(0, 0, 0, 70.0f * alphaPC), 0, false);
      if (255.0f * alphaPC >= 33.0f) {
         final float nameTextX = postHeadX + 7.0f + (w - (postHeadX - x) - 12.0f) / 2.0f - nameFont.getStringWidth(this.targetName) / 2.0f;
         nameFont.drawStringWithShadow(this.targetName, nameTextX, y + 5.5f, ColorUtils.getColor(255, 255, 255, 255.0f * alphaPC));
      }
      final float hpWidth = w - (postHeadX - x) - 12.0f;
      final float hpX1 = postHeadX + 7.0f;
      final float hpX2 = hpX1 + hpWidth * targetHealthPC;
      final float hpX3 = hpX1 + hpWidth;
      final int hpCol1 = ColorUtils.swapAlpha(ClientColors.getColor1(0), 255.0f * alphaPC);
      final int hpCol2 = ColorUtils.swapAlpha(ClientColors.getColor2(-324), 255.0f * alphaPC);
      final int hpBGCol = ColorUtils.swapAlpha(0, 65.0f * alphaPC);
      final float hpRoundRect = 1.5f;
      final float hpShadowRect = 1.0f;
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(hpX1 - 0.5f, y + h - 9.0f, hpX3 + 0.5f, y + h - 4.5f, hpRoundRect, hpShadowRect + 0.5f, hpBGCol, hpBGCol, hpBGCol, hpBGCol, false, true, true);
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRect(hpX1 - 0.5f, y + h - 12.0f, hpX2 + 0.5f, y + h - 4.5f, -1);
      StencilUtil.readStencilBuffer(1);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(hpX1, y + h - 8.5f, hpX3, y + h - 5.0f, hpRoundRect, hpShadowRect, hpCol1, hpCol2, hpCol2, hpCol1, true, true, true);
      StencilUtil.uninitStencilBuffer();
      if (255.0f * alphaPC >= 33.0f) {
         hpFont.drawString(hpStr, postHeadX + 6.0f, y + 16.5f, ColorUtils.getColor(255, 255, 255, 255.0f * alphaPC));
      }
      final float postHpTextX = postHeadX + 7.5f + hpFont.getStringWidth(hpStr);
      if (stacksCount > 0) {
         RenderUtils.drawVGradientRect(postHpTextX + 0.5f, y + 13.0f, postHpTextX + 1.0f, y + 22.0f - 4.0f, ColorUtils.getColor(255, 255, 255, 10.0f * alphaPC), ColorUtils.getColor(255, 255, 255, 255.0f * alphaPC));
         RenderUtils.drawVGradientRect(postHpTextX + 0.5f, y + 13.0f + 4.0f, postHpTextX + 1.0f, y + 22.0f, ColorUtils.getColor(255, 255, 255, 255.0f * alphaPC), ColorUtils.getColor(255, 255, 255, 10.0f * alphaPC));
      }
      final RenderItem itemRender = TargetHUD.mc.getRenderItem();
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableDepth();
      float xn = postHpTextX - x - 5.5f;
      float yn = 13.0f;
      final int itemsBgCol = ColorUtils.getColor(0, 0, 0, 40.0f * alphaPC);
      GlStateManager.enableDepth();
      GL11.glTranslated(x, y, 0.0);
      xn *= 2.0f;
      yn *= 2.0f;
      GL11.glScaled(0.5, 0.5, 0.5);
      for (final ItemStack stack2 : stacks) {
         if (!stack2.isEmpty()) {
            xn += 17.0f;
         }
         GL11.glTranslated(xn, yn, 0.0);
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled(alphaPC, alphaPC, 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         itemRender.zLevel = 200.0f;
         itemRender.renderItemAndEffectIntoGUI(stack2, 0, 0);
         if (255.0f * alphaPC >= 33.0f) {
            itemRender.renderItemOverlayIntoGUIWithTextColor(Fonts.minecraftia_16, stack2, 0, 0, stack2.getCount(), ColorUtils.getColor(255, 255, 255, 255.0f * alphaPC));
         }
         RenderUtils.drawItemWarnIfLowDur(stack2, 0.0f, 0.0f, alphaPC, 1.0f);
         itemRender.zLevel = 0.0f;
         GL11.glTranslated(8.0, 8.0, 0.0);
         GL11.glScaled(1.0f / alphaPC, 1.0f / alphaPC, 1.0);
         GL11.glTranslated(-8.0, -8.0, 0.0);
         GL11.glTranslated(-xn, -yn, 0.0);
      }
      GL11.glScaled(2.0, 2.0, 2.0);
      xn /= 2.0f;
      yn /= 2.0f;
      GL11.glTranslated(-x, -y, 0.0);
      RenderHelper.disableStandardItemLighting();
      GL11.glPopMatrix();
   }

   private void renderEntire(EntityLivingBase target) {
      ResourceLocation res = OldSkin != null ? OldSkin : (skin != null ? skin : null);
      float targetHealth = target.getSmoothHealth() + this.absorbAnim.getAnim();
      float targetHealtMax = target.getMaxHealth() + this.absorbAnim.anim;
      float targetHealthPC = Math.max(Math.min(targetHealth / targetHealtMax, 1.0F), 0.0F);
      String hpStr = String.format("%.1f", targetHealth).replace(".0", "");
      CFontRenderer hpFont = Fonts.comfortaaBold_12;
      if (target.getDisplayName() != null) {
         this.targetName = target.getDisplayName()
            .getFormattedText()
            .replace("  ", " ")
            .replace("§l", "")
            .replace("[]", "")
            .replace("§k", "")
            .replace("§m", "")
            .replace("§n", "")
            .replace("§o", "");
      }

      int stacksCount = 0;
      ItemStack offhand = target.getHeldItemOffhand();
      ItemStack boots = target.getItemStackFromSlot(EntityEquipmentSlot.FEET);
      ItemStack leggings = target.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
      ItemStack body = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      ItemStack helm = target.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
      ItemStack inHand = target.getHeldItemMainhand();
      ItemStack[] stuff = new ItemStack[]{offhand, inHand, boots, leggings, body, helm};
      ArrayList<ItemStack> stacks = new ArrayList<>();
      int j = 0;

      for (ItemStack i : stuff) {
         if (i != null) {
            i.getItem();
            stacks.add(i);
         }
      }

      for (ItemStack stack : stacks) {
         if (!stack.isEmpty()) {
            stacksCount++;
         }
      }

      float alphaPC = this.Scaleclamp();
      alphaPC *= alphaPC;
      CFontRenderer nameFont = Fonts.comfortaaRegular_13;
      float w = widthHud;
      float h = heightHud;
      float x = (float)((int)(xPosHud * 2.0F)) / 2.0F;
      float y = yPosHud;
      float textOrItemW = (float)stacksCount * 8.0F + 0.5F;
      float strW = (float)nameFont.getStringWidth(this.targetName);
      if (textOrItemW < strW) {
         textOrItemW = strW;
      }

      float exts = 2.0F;
      float hpLineH = 4.0F;
      float alphaF = alphaPC * (0.5F + alphaPC / 2.0F);
      widthHud = MathUtils.clamp(24.0F + exts * 3.0F + textOrItemW + 1.0F, 80.0F, 180.0F);
      heightHud = 24.0F + exts * 3.0F + hpLineH;
      float round = 6.0F;
      float shadow = 7.0F * alphaPC;
      if (!((double)alphaPC < 0.1)) {
         GL11.glPushMatrix();
         this.hudScale(x, y, w, h);
         int[] headColors = this.getColorsFromLastResource(res, alphaPC / 3.25F);
         int bgCol = ColorUtils.swapAlpha(0, 130.0F * alphaF);
         int bg1 = ColorUtils.getOverallColorFrom(headColors[0], bgCol);
         int bg2 = ColorUtils.getOverallColorFrom(headColors[1], bgCol);
         int bg3 = ColorUtils.getOverallColorFrom(headColors[2], bgCol);
         int bg4 = ColorUtils.getOverallColorFrom(headColors[3], bgCol);
         RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(
            x, y, x + w, y + h, 8.0F, headColors[0], headColors[1], headColors[2], headColors[3], true
         );
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x, y, x + w, y + h, 0.0F, 0.5F, bg1, bg2, bg3, bg4, false, true, true
         );
         RenderUtils.drawLightContureRectSmoothFullGradient(x, y, x + w, y + h, bg1, bg2, bg3, bg4, true);
         float hpRect1X = x + exts;
         float hpRect1X2 = x + exts + (w - exts * 2.0F) * targetHealthPC;
         float hpRectY2 = y + h - exts;
         float hpRectY = hpRectY2 - hpLineH;
         if (target.hurtTime <= 1) {
            if (target.hurtTime == 0) {
               this.hurtHpAnim.setAnim(targetHealthPC);
            }

            this.hurtHpAnim.to = targetHealthPC;
         }

         float hurtHpAnim = this.hurtHpAnim.getAnim();
         hurtHpAnim = Math.max(hurtHpAnim, targetHealthPC);
         float hpRect2X2 = x + exts + (w - exts * 2.0F) * hurtHpAnim;
         float hpRect3X2 = x + w - exts;
         int hpRectCol1 = ColorUtils.swapAlpha(headColors[3], 255.0F * alphaF);
         int hpRectCol2 = ColorUtils.swapAlpha(headColors[2], 255.0F * alphaF);
         if (MathUtils.getDifferenceOf(hpRect1X, hpRect1X2) >= 0.5) {
            int c2 = ColorUtils.getOverallColorFrom(hpRectCol1, hpRectCol2, targetHealthPC);
            RenderUtils.drawAlphedSideways((double)hpRect1X, (double)hpRectY, (double)hpRect1X2, (double)hpRectY2, hpRectCol1, c2, false);
         }

         if (MathUtils.getDifferenceOf(hpRect1X2, hpRect2X2) >= 0.5) {
            int c1 = ColorUtils.getOverallColorFrom(hpRectCol1, hpRectCol2, targetHealthPC);
            int c2 = ColorUtils.getOverallColorFrom(c1, hpRectCol2, hurtHpAnim);
            c1 = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) / 2.0F);
            c2 = ColorUtils.swapAlpha(c2, (float)ColorUtils.getAlphaFromColor(c2) / 2.0F);
            RenderUtils.drawAlphedSideways((double)hpRect1X2, (double)hpRectY, (double)hpRect2X2, (double)hpRectY2, c1, c2, false);
         }

         RenderUtils.drawLightContureRectSmooth((double)hpRect1X, (double)hpRectY, (double)hpRect3X2, (double)hpRectY2, bgCol);
         int textCol = ColorUtils.swapAlpha(headColors[0], 255.0F * alphaF);
         if (ColorUtils.getAlphaFromColor(textCol) >= 33) {
            GlStateManager.tryBlendFuncSeparate(
               GlStateManager.SourceFactor.SRC_ALPHA,
               GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA,
               GlStateManager.SourceFactor.ONE,
               GlStateManager.DestFactor.ZERO
            );
            nameFont.drawStringWithShadow(this.targetName, (double)(x + h - (exts + hpLineH) + 1.0F), (double)(y + 4.0F), textCol);
            GlStateManager.tryBlendFuncSeparate(
               GlStateManager.SourceFactor.SRC_ALPHA,
               GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
               GlStateManager.SourceFactor.ONE,
               GlStateManager.DestFactor.ZERO
            );
         }

         float headX = x + exts;
         float headY = y + exts;
         float headSize = 24.0F;
         if (res != null) {
            GlStateManager.enableTexture2D();
            RenderUtils.glColor(
               ColorUtils.swapAlpha(
                  ColorUtils.getOverallColorFrom(
                     -1, ColorUtils.getColor(255, 190, 190), MathUtils.clamp((float)target.hurtTime - mc.getRenderPartialTicks(), 0.0F, 10.0F) / 10.0F
                  ),
                  255.0F * alphaF * alphaPC
               )
            );
            mc.getTextureManager().bindTexture(res);
            GL11.glTranslated((double)headX, (double)headY, 0.0);
            Gui.drawScaledCustomSizeModalRect(0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F, headSize, headSize, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect(-2.0F, -2.0F, 39.0F, 8.0F, 10.0F, 8.0F, headSize + 4.0F, headSize + 4.0F, 64.0F, 64.0F);
            GL11.glTranslated((double)(-headX), (double)(-headY), 0.0);
            GlStateManager.resetColor();
            if (target.getHealth() == 0.0F && 255.0F * alphaPC * alphaPC >= 33.0F) {
               Fonts.mntsb_16
                  .drawStringWithShadow(
                     "ЛОХ",
                     (double)(x + exts + headSize / 2.0F - (float)Fonts.mntsb_16.getStringWidth("ЛОХ") / 2.0F),
                     (double)(y + headSize / 2.0F - 1.0F),
                     ColorUtils.getColor(255, 65, 65, 255.0F * alphaPC * alphaPC)
                  );
            }
         } else {
            RenderUtils.drawRect(
               (double)headX, (double)headY, (double)(headX + headSize), (double)(headY + headSize), ColorUtils.getColor(0, 0, 0, 90.0F * alphaPC)
            );
            Fonts.comfortaaBold_18
               .drawString(
                  "?", (double)(x + headSize / 2.0F + 2.5F), (double)(y + headSize / 2.0F + 2.0F), ColorUtils.getColor(255, 255, 255, 255.0F * alphaPC)
               );
         }

         RenderItem itemRender = mc.getRenderItem();
         RenderHelper.enableGUIStandardItemLighting();
         GlStateManager.enableDepth();
         float xn = h - exts - hpLineH - 8.0F;
         float yn = h - exts * 2.0F - hpLineH - 9.0F;
         GlStateManager.enableDepth();
         GL11.glTranslated((double)x, (double)y, 0.0);
         xn *= 2.0F;
         yn *= 2.0F;
         GL11.glScaled(0.5, 0.5, 0.5);

         for (ItemStack stackx : stacks) {
            if (!stackx.isEmpty()) {
               xn += 17.0F;
            }

            GL11.glTranslated((double)xn, (double)yn, 0.0);
            GL11.glTranslated(8.0, 8.0, 0.0);
            GL11.glScaled((double)alphaPC, (double)alphaPC, 1.0);
            GL11.glTranslated(-8.0, -8.0, 0.0);
            itemRender.zLevel = 200.0F;
            itemRender.renderItemAndEffectIntoGUI(stackx, 0, 0);
            if (255.0F * alphaPC >= 33.0F) {
               itemRender.renderItemOverlayIntoGUIWithTextColor(
                  Fonts.minecraftia_16, stackx, 0, 0, stackx.getCount(), ColorUtils.getColor(255, 255, 255, 255.0F * alphaPC)
               );
            }

            RenderUtils.drawItemWarnIfLowDur(stackx, 0.0F, 0.0F, alphaPC, 1.0F);
            itemRender.zLevel = 0.0F;
            GL11.glTranslated(8.0, 8.0, 0.0);
            GL11.glScaled((double)(1.0F / alphaPC), (double)(1.0F / alphaPC), 1.0);
            GL11.glTranslated(-8.0, -8.0, 0.0);
            GL11.glTranslated((double)(-xn), (double)(-yn), 0.0);
         }

         RenderHelper.disableStandardItemLighting();
         GL11.glPopMatrix();
      }
   }

   private BufferedImage getBufferedImage(ResourceLocation res) {
      BufferedImage buffer = null;
      if (res != null) {
         try (InputStream inputStream = mc.getResourceManager().getResource(res).getInputStream()) {
            buffer = ImageIO.read(inputStream);
         } catch (IOException var8) {
            var8.printStackTrace();
         }
      }

      return buffer;
   }

   private int getColorFromTextureCoord(BufferedImage img, int texX, int texY) {
      return img == null ? 0 : img.getRGB(texX, texY);
   }

   private int[] getColorsFromLastResource(ResourceLocation res) {
      BufferedImage bufferedImage;
      if (res != null && (bufferedImage = this.getBufferedImage(res)) != null) {
         float texW = (float)bufferedImage.getWidth();
         float texH = (float)bufferedImage.getHeight();
         float defS = 64.0F;
         int[] uPix = new int[]{(int)(8.0F / defS * texW), (int)(15.0F / defS * texW), (int)(15.0F / defS * texW), (int)(8.0F / defS * texW)};
         int[] vPix = new int[]{(int)(8.0F / defS * texH), (int)(8.0F / defS * texH), (int)(15.0F / defS * texH), (int)(15.0F / defS * texH)};
         this.lastUpdatedResColors[0] = this.getColorFromTextureCoord(bufferedImage, uPix[0], vPix[0]);
         this.lastUpdatedResColors[1] = this.getColorFromTextureCoord(bufferedImage, uPix[1], vPix[1]);
         this.lastUpdatedResColors[2] = this.getColorFromTextureCoord(bufferedImage, uPix[2], vPix[2]);
         this.lastUpdatedResColors[3] = this.getColorFromTextureCoord(bufferedImage, uPix[3], vPix[3]);
      }

      return this.lastUpdatedResColors;
   }

   private int[] getColorsFromLastResource(ResourceLocation res, float alphaPC) {
      int[] colors = this.getColorsFromLastResource(res);
      colors[0] = ColorUtils.swapAlpha(colors[0], (float)ColorUtils.getAlphaFromColor(colors[0]) * alphaPC);
      colors[1] = ColorUtils.swapAlpha(colors[1], (float)ColorUtils.getAlphaFromColor(colors[1]) * alphaPC);
      colors[2] = ColorUtils.swapAlpha(colors[2], (float)ColorUtils.getAlphaFromColor(colors[2]) * alphaPC);
      colors[3] = ColorUtils.swapAlpha(colors[3], (float)ColorUtils.getAlphaFromColor(colors[3]) * alphaPC);
      return colors;
   }

   public static EntityLivingBase getTarget() {
      boolean pre = get.PreRangedTarget.getBool();
      if ((pre ? HitAura.TARGET_ROTS : HitAura.TARGET) != null) {
         return pre ? HitAura.TARGET_ROTS : HitAura.TARGET;
      } else {
         if (get.RaycastTarget.getBool()) {
            EntityLivingBase base = MathUtils.getPointedEntity(new Vector2f(Minecraft.player.rotationYaw, Minecraft.player.rotationPitch), 60.0, 1.0F, false);
            if (base != null
               && base != Minecraft.player
               && (FreeCam.fakePlayer == null || base != FreeCam.fakePlayer)
               && base.isEntityAlive()
               && !Client.friendManager.isFriend(base.getName())) {
               return base;
            }
         }

         if (BowAimbot.target != null) {
            return BowAimbot.target;
         } else if (CrystalField.getTargets() != null && CrystalField.getTargets().size() != 0) {
            return CrystalField.getTargets().get(0);
         } else {
            return mc.currentScreen instanceof GuiChat ? Minecraft.player : null;
         }
      }
   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      EntityLivingBase target = getTarget();
      this.updatePosition(event);
      float curScale = target == null ? 0.0F : 1.0F;
      if (curScale == 0.0F) {
         if (this.Scale.to == 1.0F) {
            this.Scale.to = (double)this.Scale.getAnim() > 0.995 ? 1.15F : 0.0F;
            if (MathUtils.getDifferenceOf(this.Scale.getAnim(), 1.0F) < 0.05) {
               this.Scale.setAnim(1.0F);
            }
         } else if (this.Scale() >= (curTarget != null && curTarget != Minecraft.player && !curTarget.isEntityAlive() ? 1.1499F : 1.075F)) {
            this.Scale.to = 0.0F;
         }
      } else {
         if (this.Scale() < 0.75F) {
            this.Scale.setAnim(0.75F);
         }

         if (this.Scale() >= 1.075F) {
            this.Scale.to = 1.0F;
         } else if (this.Scale.to == 0.0F) {
            this.Scale.to = 1.15F;
         }
      }

      if (target != null && (target != Minecraft.player || mc.currentScreen instanceof GuiChat)) {
         curTarget = target;
      }

      if (!((double)this.Scale() < 0.002)) {
         if (soundTarget != target && this.TargettingSFX.getBool()) {
            if (target != null && target != Minecraft.player) {
               ClientTune.get.playTargetSelect();
            }

            soundTarget = target;
         }

         if (curTarget != null) {
            if (this.targetHurt != curTarget.hurtTime) {
               this.targetHurt = curTarget.hurtTime;
            }

            if (skin != null && OldSkin != skin && target != null) {
               OldSkin = skin;
            }
         }

         if (Minecraft.player != null
            && getTarget() == Minecraft.player
            && mc.currentScreen instanceof GuiChat
            && Minecraft.player.connection.getPlayerInfo(Minecraft.player.getName()) != null) {
            OldSkin = WorldRender.get
               .updatedResourceSkin(Minecraft.player.connection.getPlayerInfo(Minecraft.player.getName()).getLocationSkin(), Minecraft.player);
         }

         if (curTarget == null) {
            curTarget = Minecraft.player;
         }

         this.targetName = curTarget.getName().replace("  ", " ");
         String var4 = this.Mode.currentMode;
         switch (var4) {
            case "Light":
               this.renderLight(curTarget);
               break;
            case "WetWorn":
               this.renderWetWorn(curTarget);
               break;
            case "Neomoin":
               this.renderNeomoin(curTarget);
               break;
            case "Modern":
               this.renderModern(curTarget);
               break;
            case "Bushy":
               this.renderBushy(curTarget);
               break;
            case "Subtle":
               this.renderSubtle(curTarget);
               break;
            case "Entire":
               this.renderEntire(curTarget);
         }
      }
   }

   public void hudScale(float x, float y, float width, float height) {
      if (MathUtils.getDifferenceOf(this.Scale(), 1.0F) > 0.01) {
         RenderUtils.customScaledObject2D(x, y, width, height, this.Scale());
      }
   }

   public void hudScalePro(float x, float y, float width, float height) {
      float scale = MathUtils.clamp(this.Scale(), 0.0F, 2.0F) / 4.0F + 0.75F;
      RenderUtils.customScaledObject2DPro(x, y, width, height, scale, scale);
   }

   private final float getArmorPercent01(EntityLivingBase entity) {
      float armPC = 0.0F;

      for (ItemStack armorElement : entity.getArmorInventoryList()) {
         if (!armorElement.isEmpty() && armorElement != null) {
            float maxDurable = (float)armorElement.getMaxDamage();
            float armorHealth = maxDurable - (float)armorElement.getItemDamage();
            float durrablePC = armorHealth / maxDurable;
            armPC += durrablePC;
         }
      }

      return MathUtils.clamp(armPC / 4.0F, 0.0F, 1.0F);
   }

   private class particle {
      long time = System.currentTimeMillis();
      float x;
      float y;
      AnimationUtils xs = new AnimationUtils(0.0F, 0.0F, 0.0075F);
      AnimationUtils ys = new AnimationUtils(0.0F, 0.0F, 0.0075F);
      float motionX;
      float motionY;

      public particle(float x, float y) {
         this.x = x;
         this.y = y;
         float rand = 0.003F * Math.max((float)Minecraft.getDebugFPS(), 5.0F) * 15.0F;
         this.motionX = (float)MathUtils.getRandomInRange(-1.0, 1.0);
         this.motionY = (float)MathUtils.getRandomInRange(-1.0, 1.0);
         this.xs.setAnim(x);
         this.ys.setAnim(y);
         this.xs.to = this.motionX * 100.0F + x;
         this.ys.to = this.motionY * 100.0F + y;
      }

      public long getTime() {
         return System.currentTimeMillis() - this.time;
      }

      public void update(int color) {
         float rand = 0.0035F * Math.max((float)Minecraft.getDebugFPS(), 5.0F) * 15.0F;
         this.x = this.x + (this.motionX = this.motionX = (float)((double)this.motionX / 1.02)) * rand;
         this.y = this.y + (this.motionY = this.motionY = (float)((double)this.motionY / 1.02)) * rand;
         float ss = 3.0F * ((float)ColorUtils.getAlphaFromColor(color) / 255.0F);
         float x = this.xs.getAnim();
         float y = this.ys.getAnim();
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x - ss, y - ss, x + ss, y + ss, ss, 1.0F, color, color, color, color, false, true, true
         );
      }
   }
}
