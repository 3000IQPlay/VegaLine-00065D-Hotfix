package ru.govno.client.utils.Render;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec2f;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;
import ru.govno.client.Client;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.glsandbox.animbackground;

public class RenderUtils {
   protected static Minecraft mc = Minecraft.getMinecraft();
   private static final Frustum frustrum = new Frustum();
   private static final FloatBuffer COLOR_BUFFER = GLAllocation.createDirectFloatBuffer(4);
   private static final Vec3d LIGHT0_POS = new Vec3d(0.2F, 1.0, -0.7F).normalize();
   private static final Vec3d LIGHT1_POS = new Vec3d(-0.2F, 1.0, 0.7F).normalize();
   private static final Frustum frustum = new Frustum();
   public static ShaderUtility roundedShader = new ShaderUtility("roundedRect");
   public static ShaderUtility roundedOutlineShader = new ShaderUtility("roundRectOutline");
   public static Tessellator tessellator = Tessellator.getInstance();
   public static BufferBuilder buffer = tessellator.getBuffer();
   private static final ResourceLocation ITEM_WARN_DUR = new ResourceLocation("vegaline/system/durablitywarn/itemwarn.png");

   public static void anialisON(boolean line, boolean polygon, boolean point) {
      if (line) {
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
      }

      if (polygon) {
         GL11.glEnable(2881);
         GL11.glHint(3155, 4354);
      }

      if (point) {
         GL11.glEnable(2832);
         GL11.glHint(3153, 4354);
      }
   }

   public static void anialisOFF(boolean line, boolean polygon, boolean point) {
      if (line) {
         GL11.glHint(3154, 4352);
         GL11.glDisable(2848);
      }

      if (polygon) {
         GL11.glHint(3155, 4352);
         GL11.glDisable(2881);
      }

      if (point) {
         GL11.glHint(3153, 4352);
         GL11.glDisable(2832);
      }
   }

   public static int red(int color) {
      return color >> 16 & 0xFF;
   }

   public static int green(int color) {
      return color >> 8 & 0xFF;
   }

   public static int blue(int color) {
      return color & 0xFF;
   }

   public static int alpha(int color) {
      return color >> 24 & 0xFF;
   }

   public static void drawClientHudRect3(float x, float y, float x2, float y2, float alphaPC, float extend, boolean manyGlows) {
      int cli1 = ClientColors.getColorQ(1, alphaPC);
      int cli2 = ClientColors.getColorQ(2, alphaPC);
      int cli3 = ClientColors.getColorQ(3, alphaPC);
      int cli4 = ClientColors.getColorQ(4, alphaPC);
      float alphaPCM = 0.3F;
      int cc1 = ColorUtils.getOverallColorFrom(
         ColorUtils.swapAlpha(cli1, (float)alpha(cli1) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0F * alphaPC / 2.55F), 0.2F
      );
      int cc2 = ColorUtils.getOverallColorFrom(
         ColorUtils.swapAlpha(cli2, (float)alpha(cli2) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0F * alphaPC / 2.55F), 0.2F
      );
      int cc3 = ColorUtils.getOverallColorFrom(
         ColorUtils.swapAlpha(cli3, (float)alpha(cli3) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0F * alphaPC / 2.55F), 0.75F
      );
      int cc4 = ColorUtils.getOverallColorFrom(
         ColorUtils.swapAlpha(cli4, (float)alpha(cli4) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0F * alphaPC / 2.55F), 0.75F
      );
      int cs1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC), 0.1F);
      int cs2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC), 0.1F);
      int cs5 = ColorUtils.getOverallColorFrom(cli3, cc2, 0.75F);
      int cs6 = ColorUtils.getOverallColorFrom(cli4, cc1, 0.75F);
      int cs7 = ColorUtils.getOverallColorFrom(cli3, cc3, 0.85F);
      int cs8 = ColorUtils.getOverallColorFrom(cli4, cc4, 0.85F);
      if (alphaPC >= 0.05F) {
         GaussianBlur.drawBlur(1.0F + alphaPC * 4.0F, () -> drawRect((double)x, (double)(y + extend), (double)x2, (double)y2, -1));
      }

      drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + 0.5F, y + 0.5F + extend, x2 - 0.5F, y2 - 0.5F, 0.0F, 0.0F, cc1, cc2, cc3, cc4, false, true, false
      );
      drawLightContureRectFullGradient(x + 0.5F, y + extend + 0.5F, x2 - 0.5F, y2 - 0.5F, cs6, cs5, cs7, cs8, false);
      drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y + extend, 0.0F, 0.0F, cs1, cs2, cs5, cs6, false, true, false);
      if (manyGlows) {
         cc1 = ColorUtils.swapAlpha(cc1, (float)ColorUtils.getAlphaFromColor(cc1) * 0.65F);
         cc2 = ColorUtils.swapAlpha(cc2, (float)ColorUtils.getAlphaFromColor(cc2) * 0.65F);
         cc3 = ColorUtils.swapAlpha(cc3, (float)ColorUtils.getAlphaFromColor(cc3) * 0.65F);
         cc4 = ColorUtils.swapAlpha(cc4, (float)ColorUtils.getAlphaFromColor(cc4) * 0.65F);
      }

      drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 0.0F, 6.0F, cc1, cc2, cc3, cc4, true, false, true);
      if (manyGlows) {
         cc1 = ColorUtils.swapAlpha(cc1, (float)ColorUtils.getAlphaFromColor(cc1) * 0.8F);
         cc2 = ColorUtils.swapAlpha(cc2, (float)ColorUtils.getAlphaFromColor(cc2) * 0.8F);
         cc3 = ColorUtils.swapAlpha(cc3, (float)ColorUtils.getAlphaFromColor(cc3) * 0.8F);
         cc4 = ColorUtils.swapAlpha(cc4, (float)ColorUtils.getAlphaFromColor(cc4) * 0.8F);
         drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 0.0F, 25.0F, cc1, cc2, cc3, cc4, true, false, true);
      }
   }

   public static void drawClientHudRect3(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
      drawClientHudRect3(x, y, x2, y2, alphaPC, 12.0F, manyGlows);
   }

   public static void drawClientHudRect4(float x, float y, float x2, float y2, float alphaPC, float extend, boolean manyGlows) {
      int cli1 = ClientColors.getColorQ(1, alphaPC);
      int cli2 = ClientColors.getColorQ(2, alphaPC);
      int cli3 = ClientColors.getColorQ(3, alphaPC);
      int cli4 = ClientColors.getColorQ(4, alphaPC);
      float alphaPCM = 0.25F;
      int clc1 = ClientColors.getColorQ(1, alphaPC * alphaPCM);
      int clc2 = ClientColors.getColorQ(2, alphaPC * alphaPCM);
      int clc3 = ClientColors.getColorQ(3, alphaPC * alphaPCM);
      int clc4 = ClientColors.getColorQ(4, alphaPC * alphaPCM);
      int bgC = ColorUtils.swapAlpha(Integer.MIN_VALUE, 30.0F);
      float colAToB = 0.7F;
      int clb1 = ColorUtils.getOverallColorFrom(cli1, bgC, colAToB);
      int clb2 = ColorUtils.getOverallColorFrom(cli2, bgC, colAToB);
      int clb3 = ColorUtils.getOverallColorFrom(cli3, bgC, colAToB);
      int clb4 = ColorUtils.getOverallColorFrom(cli4, bgC, colAToB);
      drawFullGradientRectPro(x, y, x2, y2, clb4, clb3, clb2, clb1, false);
      float lw = 0.5F;
      drawAlphedVGradient(
         (double)x,
         (double)y,
         (double)(x + lw),
         (double)(y + (y2 - y) / 2.0F),
         ColorUtils.swapAlpha(cli1, 0.0F),
         ColorUtils.getOverallColorFrom(cli1, cli4),
         true
      );
      drawAlphedVGradient(
         (double)x,
         (double)(y + (y2 - y) / 2.0F),
         (double)(x + lw),
         (double)y2,
         ColorUtils.getOverallColorFrom(cli1, cli4),
         ColorUtils.swapAlpha(cli4, 0.0F),
         true
      );
      drawAlphedVGradient(
         (double)(x2 - lw),
         (double)y,
         (double)x2,
         (double)(y + (y2 - y) / 2.0F),
         ColorUtils.swapAlpha(cli2, 0.0F),
         ColorUtils.getOverallColorFrom(cli2, cli3),
         true
      );
      drawAlphedVGradient(
         (double)(x2 - lw),
         (double)(y + (y2 - y) / 2.0F),
         (double)x2,
         (double)y2,
         ColorUtils.getOverallColorFrom(cli2, cli3),
         ColorUtils.swapAlpha(cli3, 0.0F),
         true
      );
      drawAlphedSideways(
         (double)x,
         (double)y,
         (double)(x + (x2 - x) / 2.0F),
         (double)(y + lw),
         ColorUtils.swapAlpha(cli1, 0.0F),
         ColorUtils.getOverallColorFrom(cli1, cli2),
         true
      );
      drawAlphedSideways(
         (double)(x + (x2 - x) / 2.0F),
         (double)y,
         (double)x2,
         (double)(y + lw),
         ColorUtils.getOverallColorFrom(cli1, cli2),
         ColorUtils.swapAlpha(cli2, 0.0F),
         true
      );
      drawAlphedSideways(
         (double)x,
         (double)(y2 - lw),
         (double)(x + (x2 - x) / 2.0F),
         (double)y2,
         ColorUtils.swapAlpha(cli4, 0.0F),
         ColorUtils.getOverallColorFrom(cli4, cli3),
         true
      );
      drawAlphedSideways(
         (double)(x + (x2 - x) / 2.0F),
         (double)(y2 - lw),
         (double)x2,
         (double)y2,
         ColorUtils.getOverallColorFrom(cli4, cli3),
         ColorUtils.swapAlpha(cli3, 0.0F),
         true
      );
      drawRoundedFullGradientShadow(x, y, x2, y2, 0.0F, 7.0F, clc1, clc2, clc3, clc4, true);
      if (manyGlows) {
         clc1 = ColorUtils.swapAlpha(clc1, (float)ColorUtils.getAlphaFromColor(clc1) * 0.45F);
         clc2 = ColorUtils.swapAlpha(clc2, (float)ColorUtils.getAlphaFromColor(clc2) * 0.45F);
         clc3 = ColorUtils.swapAlpha(clc3, (float)ColorUtils.getAlphaFromColor(clc3) * 0.45F);
         clc4 = ColorUtils.swapAlpha(clc4, (float)ColorUtils.getAlphaFromColor(clc4) * 0.45F);
         drawRoundedFullGradientShadow(x, y, x2, y2, 0.0F, 25.0F, clc1, clc2, clc3, clc4, true);
      }

      glRenderStart();
      GL11.glEnable(2832);
      GL11.glPointSize(2.3F);
      buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);

      for (float i = x + 5.0F; i < x2 - 3.0F; i += 3.0F) {
         float wPC = (i - x) / (x2 - x);
         float wPCCenter = (wPC > 0.5F ? 1.0F - wPC : wPC) * 2.0F;
         int c = ColorUtils.getOverallColorFrom(
            ColorUtils.getOverallColorFrom(cli1, cli4, extend / (y2 - y)), ColorUtils.getOverallColorFrom(cli2, cli3, extend / (y2 - y)), wPC
         );
         buffer.pos((double)i, (double)(y + extend)).color(ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) * wPCCenter)).endVertex();
      }

      tessellator.draw();
      GlStateManager.resetColor();
      glRenderStop();
   }

   public static void drawClientHudRect4(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
      drawClientHudRect4(x, y, x2, y2, alphaPC, 13.0F, manyGlows);
   }

   public static void drawClientHudRect2(float x, float y, float x2, float y2, float alphaPC, float extend, boolean manyGlows) {
      float extALL = 1.5F;
      float extY = extend - extALL;
      float extIns = -0.5F;
      int cli1 = ClientColors.getColorQ(1, alphaPC);
      int cli2 = ClientColors.getColorQ(2, alphaPC);
      int cli3 = ClientColors.getColorQ(3, alphaPC);
      int cli4 = ClientColors.getColorQ(4, alphaPC);
      int cc1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC / 2.55F), 0.5F);
      int cc2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC / 2.55F), 0.5F);
      int cc3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC / 2.55F), 0.65F);
      int cc4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC / 2.55F), 0.65F);
      int cs1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC), 0.1F);
      int cs2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC), 0.1F);
      int cs3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC), 0.45F);
      int cs4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, 0, 0, 160.0F * alphaPC), 0.45F);
      StencilUtil.initStencilToWrite();
      drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + extALL, y + extALL + extY, x2 - extALL, y2 - extALL, 3.0F, 0.0F, -1, -1, -1, -1, false, true, false
      );
      StencilUtil.readStencilBuffer(0);
      drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 5.0F, 1.5F, cs1, cs2, cs3, cs4, false, true, true);
      if (manyGlows) {
         cs1 = ColorUtils.swapAlpha(cs1, (float)ColorUtils.getAlphaFromColor(cs1) * 0.2F);
         cs2 = ColorUtils.swapAlpha(cs2, (float)ColorUtils.getAlphaFromColor(cs2) * 0.2F);
         cs3 = ColorUtils.swapAlpha(cs3, (float)ColorUtils.getAlphaFromColor(cs3) * 0.2F);
         cs4 = ColorUtils.swapAlpha(cs4, (float)ColorUtils.getAlphaFromColor(cs4) * 0.2F);
         drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 5.0F, 20.0F, cs1, cs2, cs3, cs4, false, false, true);
      }

      StencilUtil.uninitStencilBuffer();
      fullRoundFG(x + extALL + extIns, y + extALL + extY + extIns, x2 - extALL - extIns, y2 - extALL - extIns, 6.0F, cc1, cc2, cc3, cc4, false);
   }

   public static void drawClientHudRect2(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
      drawClientHudRect2(x, y, x2, y2, alphaPC, 13.0F, manyGlows);
   }

   public static void drawClientHudRect2(float x, float y, float x2, float y2, boolean manyGlows) {
      drawClientHudRect2(x, y, x2, y2, 1.0F, manyGlows);
   }

   public static void drawClientHudRect(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
      int cli1 = ClientColors.getColorQ(1, alphaPC);
      int cli2 = ClientColors.getColorQ(2, alphaPC);
      int cli3 = ClientColors.getColorQ(3, alphaPC);
      int cli4 = ClientColors.getColorQ(4, alphaPC);
      int cc1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, (int)(160.0F * alphaPC)), 0.7F);
      int cc2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, (int)(160.0F * alphaPC)), 0.7F);
      int cc3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, (int)(160.0F * alphaPC)), 0.75F);
      int cc4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, (int)(160.0F * alphaPC)), 0.75F);
      int cs1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, (int)((manyGlows ? 90.0F : 160.0F) * alphaPC)), 0.4F);
      int cs2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, (int)((manyGlows ? 90.0F : 160.0F) * alphaPC)), 0.4F);
      int cs3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, (int)((manyGlows ? 90.0F : 160.0F) * alphaPC)), 0.7F);
      int cs4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, (int)((manyGlows ? 90.0F : 160.0F) * alphaPC)), 0.7F);
      int cm = ColorUtils.getOverallColorFrom(cli1, cli2);
      drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 2.375F, 0.5F, cc1, cc2, cc3, cc4, false, true, true);
      drawRoundedFullGradientShadow(x, y, x2, y2, 2.5F, 7.5F, cs1, cs2, cs3, cs4, true);
      if (manyGlows) {
         int cm1 = ColorUtils.swapAlpha(cs1, (float)ColorUtils.getAlphaFromColor(cs1) * 0.55F);
         int cm2 = ColorUtils.swapAlpha(cs2, (float)ColorUtils.getAlphaFromColor(cs2) * 0.55F);
         int cm3 = ColorUtils.swapAlpha(cs3, (float)ColorUtils.getAlphaFromColor(cs3) * 0.55F);
         int cm4 = ColorUtils.swapAlpha(cs4, (float)ColorUtils.getAlphaFromColor(cs4) * 0.55F);
         drawRoundedFullGradientShadow(x, y, x2, y2, 2.5F, 22.0F, cm1, cm2, cm3, cm4, true);
      }

      drawRoundedFullGradientInsideShadow(x, y, x2, y2, 5.0F, cs1, cs2, cs3, cs4, true);
      drawAlphedSideways((double)(x + 2.0F), (double)(y + 1.5F), (double)(x + (x2 - x) / 2.0F), (double)(y + 3.0F), ColorUtils.swapAlpha(cli1, 0.0F), cm, true);
      drawAlphedSideways((double)(x + (x2 - x) / 2.0F), (double)(y + 1.5F), (double)(x2 - 2.0F), (double)(y + 3.0F), cm, ColorUtils.swapAlpha(cli2, 0.0F), true);
   }

   public static void drawClientHudRect(float x, float y, float x2, float y2, boolean manyGlows) {
      drawClientHudRect(x, y, x2, y2, 1.0F, manyGlows);
   }

   public static void hudRectWithString(float x, float y, float x2, float y2, String elementName, String renderMode, float alphaPC, boolean manyGlows) {
      if (!renderMode.isEmpty()) {
         float extYText = 0.0F;
         switch (renderMode) {
            case "Glow":
               drawClientHudRect(x, y, x2, y2, alphaPC, manyGlows);
               extYText = 7.0F;
               break;
            case "Window":
               drawClientHudRect2(x, y, x2, y2, alphaPC, manyGlows);
               extYText = 4.5F;
               break;
            case "Plain":
               drawClientHudRect3(x, y, x2, y2, alphaPC, manyGlows);
               extYText = 4.0F;
               break;
            case "Stipple":
               drawClientHudRect4(x, y, x2, y2, alphaPC, manyGlows);
               extYText = 4.5F;
               break;
            default:
               return;
         }

         if (!(255.0F * alphaPC < 33.0F)) {
            int texCol = ColorUtils.swapAlpha(-1, 255.0F * alphaPC);
            Fonts.mntsb_16.drawStringWithShadow(elementName, (double)(x + 3.0F), (double)(y + extYText), texCol);
            texCol = ColorUtils.swapAlpha(-1, 65.0F * alphaPC);
            if (!(65.0F * alphaPC < 33.0F)) {
               String draw;
               switch (elementName) {
                  case "Potions":
                     draw = "C";
                     break;
                  case "Staff list":
                     draw = "B";
                     break;
                  case "Keybinds":
                     draw = "L";
                     break;
                  case "Pickups list":
                     draw = "M";
                     break;
                  default:
                     return;
               }

               if (draw != null) {
                  Fonts.stylesicons_18.drawString(draw, (double)(x2 - 12.5F), (double)(y + extYText + 0.5F), texCol);
               }
            }
         }
      }
   }

   public static void hudRectWithString(float x, float y, float x2, float y2, String elementName, String renderMode, boolean manyGlows) {
      hudRectWithString(x, y, x2, y2, elementName, renderMode, 1.0F, manyGlows);
   }

   public static final void setup3dForBlockPos(Runnable render, boolean bloom) {
      double glX = RenderManager.viewerPosX;
      double glY = RenderManager.viewerPosY;
      double glZ = RenderManager.viewerPosZ;
      GL11.glPushMatrix();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA,
         bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
         GlStateManager.SourceFactor.ONE,
         GlStateManager.DestFactor.ZERO
      );
      mc.entityRenderer.disableLightmap();
      GL11.glEnable(3042);
      GL11.glLineWidth(1.0F);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDisable(2896);
      GL11.glShadeModel(7425);
      GL11.glTranslated(-glX, -glY, -glZ);
      render.run();
      GL11.glTranslated(glX, glY, glZ);
      GL11.glLineWidth(1.0F);
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.resetColor();
      GL11.glPopMatrix();
   }

   public static void drawCircledTHud(float cx, double cy, float r, float percent, int color, float alpha, float lineWidth) {
      GL11.glPushMatrix();
      GL11.glEnable(2848);
      cx *= 2.0F;
      cy *= 2.0;
      GlStateManager.glLineWidth(2.0F);
      float theta = 0.0175F;
      float p = (float)Math.cos((double)theta);
      float s = (float)Math.sin((double)theta);
      float var18;
      float x = var18 = r * 2.0F;
      float y = 0.0F;
      enableGL2D();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      GL11.glLineWidth(lineWidth);
      int[] counter = new int[]{1};
      buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for (float ii = 0.0F; ii < 360.0F * percent; ii++) {
         buffer.pos((double)(x + cx), (double)y + cy).color(ColorUtils.swapAlpha(color, alpha)).endVertex();
         float t = x;
         x = p * x - s * y;
         y = s * t + p * y;
         counter[0]++;
      }

      tessellator.draw();
      GL11.glDisable(2848);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      disableGL2D();
      GlStateManager.resetColor();
      GlStateManager.glLineWidth(1.0F);
      resetBlender();
      GlStateManager.enableBlend();
      GL11.glPopMatrix();
   }

   public static void drawCircledTHudWithOverallColor(
      float cx, double cy, float r, float percent, int color, float alpha, float lineWidth, int color2, float pcColor2
   ) {
      GL11.glPushMatrix();
      GL11.glEnable(2848);
      cx *= 2.0F;
      cy *= 2.0;
      GlStateManager.glLineWidth(2.0F);
      float theta = 0.0175F;
      float p = (float)Math.cos((double)theta);
      float s = (float)Math.sin((double)theta);
      float var20;
      float x = var20 = r * 2.0F;
      float y = 0.0F;
      enableGL2D();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      GL11.glLineWidth(lineWidth);
      int[] counter = new int[]{1};
      buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for (float ii = 0.0F; ii < 360.0F * percent; ii++) {
         setupColor(ColorUtils.getOverallColorFrom(color, color2, pcColor2), alpha);
         buffer.pos((double)(x + cx), (double)y + cy).color(ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color, color2, pcColor2), alpha)).endVertex();
         float t = x;
         x = p * x - s * y;
         y = s * t + p * y;
         counter[0]++;
      }

      tessellator.draw();
      GL11.glDisable(2848);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      disableGL2D();
      GlStateManager.resetColor();
      GlStateManager.glLineWidth(1.0F);
      resetBlender();
      GL11.glPopMatrix();
   }

   public static void enableGUIStandardItemLighting() {
      GlStateManager.pushMatrix();
      GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);
      enableStandardItemLighting();
      GlStateManager.popMatrix();
   }

   public static void disableStandardItemLighting() {
      GlStateManager.disableLighting();
      GlStateManager.disableLight(0);
      GlStateManager.disableLight(1);
      GlStateManager.disableColorMaterial();
   }

   public static void enableStandardItemLighting() {
      GlStateManager.enableLighting();
      GlStateManager.enableLight(0);
      GlStateManager.enableLight(1);
      GlStateManager.enableColorMaterial();
      GlStateManager.colorMaterial(1032, 5634);
      GlStateManager.glLight(16384, 4611, setColorBuffer(LIGHT0_POS.xCoord, LIGHT0_POS.yCoord, LIGHT0_POS.zCoord, 0.0));
      GlStateManager.glLight(16384, 4609, setColorBuffer(0.6F, 0.6F, 0.6F, 1.0F));
      GlStateManager.glLight(16384, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GlStateManager.glLight(16384, 4610, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GlStateManager.glLight(16385, 4611, setColorBuffer(LIGHT1_POS.xCoord, LIGHT1_POS.yCoord, LIGHT1_POS.zCoord, 0.0));
      GlStateManager.glLight(16385, 4609, setColorBuffer(0.6F, 0.6F, 0.6F, 1.0F));
      GlStateManager.glLight(16385, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GlStateManager.glLight(16385, 4610, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GlStateManager.shadeModel(7424);
      GlStateManager.glLightModel(2899, setColorBuffer(0.4F, 0.4F, 0.4F, 1.0F));
   }

   private static FloatBuffer setColorBuffer(double p_74517_0_, double p_74517_2_, double p_74517_4_, double p_74517_6_) {
      return setColorBuffer((float)p_74517_0_, (float)p_74517_2_, (float)p_74517_4_, (float)p_74517_6_);
   }

   public static FloatBuffer setColorBuffer(float p_74521_0_, float p_74521_1_, float p_74521_2_, float p_74521_3_) {
      COLOR_BUFFER.clear();
      COLOR_BUFFER.put(p_74521_0_).put(p_74521_1_).put(p_74521_2_).put(p_74521_3_);
      COLOR_BUFFER.flip();
      return COLOR_BUFFER;
   }

   public static void drawClientCircle(float cx, double cy, float r, float minus, float lineW, float alphaPC) {
      enableGL2D();
      GL11.glPointSize(lineW);
      GL11.glEnable(2832);
      buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
      int ii = 180;

      while ((float)ii <= minus + 180.0F) {
         ii += 6;
         double x1 = (double)cx + Math.sin((double)ii * Math.PI / 180.0) * (double)r;
         double y1 = cy + Math.cos((double)ii * Math.PI / 180.0) * (double)r;
         buffer.pos(x1, y1).color(ClientColors.getColor1(ii * 3, 0.75F * alphaPC)).endVertex();
         GlStateManager.resetColor();
      }

      tessellator.draw();
      GL11.glDisable(2832);
      GL11.glPointSize(1.0F);
      disableGL2D();
      GL11.glEnable(3042);
      GlStateManager.resetColor();
      GlStateManager.glLineWidth(1.0F);
   }

   public static void drawClientCircleWithOverallToColor(float cx, double cy, float r, float minus, float lineW, float alphaPC, int color2, float pcColor2) {
      enableGL2D();
      GL11.glPointSize(lineW);
      GL11.glEnable(2832);
      buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
      int ii = 180;

      while ((float)ii <= minus + 180.0F) {
         ii += 6;
         double x1 = (double)cx + Math.sin((double)ii * Math.PI / 180.0) * (double)r;
         double y1 = cy + Math.cos((double)ii * Math.PI / 180.0) * (double)r;
         buffer.pos(x1, y1)
            .color(
               ColorUtils.getOverallColorFrom(
                  ClientColors.getColor1(ii * 3, 0.75F * alphaPC),
                  ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) * alphaPC),
                  pcColor2
               )
            )
            .endVertex();
      }

      tessellator.draw();
      GL11.glDisable(2832);
      GL11.glPointSize(1.0F);
      disableGL2D();
      GL11.glEnable(3042);
      GlStateManager.resetColor();
      GlStateManager.glLineWidth(1.0F);
   }

   public static void drawClientCircle(float cx, double cy, float r, float minus, float lineW) {
      drawClientCircle(cx, cy, r, minus, lineW, 1.0F);
   }

   public static void drawCanisterBox(
      AxisAlignedBB axisalignedbb, boolean outlineBox, boolean decussationBox, boolean fullBox, int outlineColor, int decussationColor, int fullColor
   ) {
      GlStateManager.pushMatrix();
      GlStateManager.glLineWidth(0.01F);
      GL11.glDisable(3008);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      if (outlineBox) {
         glColor(outlineColor);
         buffer.begin(2, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
      }

      if (decussationBox) {
         glColor(decussationColor);
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
      }

      if (outlineBox) {
         glColor(outlineColor);
         buffer.begin(2, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
      }

      if (decussationBox) {
         glColor(decussationColor);
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
      }

      if (outlineBox) {
         glColor(outlineColor);
         buffer.begin(2, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
      }

      if (decussationBox) {
         glColor(decussationColor);
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
      }

      if (outlineBox) {
         glColor(outlineColor);
         buffer.begin(2, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
      }

      if (decussationBox) {
         glColor(decussationColor);
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
      }

      if (outlineBox) {
         glColor(outlineColor);
         buffer.begin(2, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
      }

      if (decussationBox) {
         glColor(decussationColor);
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
      }

      if (outlineBox) {
         glColor(outlineColor);
         buffer.begin(2, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
      }

      if (decussationBox) {
         glColor(decussationColor);
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
         buffer.begin(1, DefaultVertexFormats.POSITION);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
      }

      if (fullBox) {
         glColor(fullColor);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
         buffer.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
         tessellator.draw();
      }

      GL11.glEnable(3008);
      GL11.glHint(3154, 4352);
      GL11.glDisable(2848);
      GlStateManager.resetColor();
      GlStateManager.popMatrix();
   }

   public static void drawGradientAlphaBox(AxisAlignedBB bb, boolean outlineBox, boolean fullBox, int outlineColor, int fullColor) {
      GlStateManager.pushMatrix();
      GL11.glDisable(3008);
      GL11.glDisable(2884);
      GL11.glShadeModel(7425);
      GL11.glEnable(2848);
      double x1 = bb.minX;
      double y1 = bb.minY;
      double z1 = bb.minZ;
      double x2 = bb.maxX;
      double y2 = bb.maxY;
      double z2 = bb.maxZ;
      double wx = x2 - x1;
      double wy = y2 - y1;
      double wz = z2 - z1;
      if (outlineBox) {
         GlStateManager.glLineWidth(1.0F);
         buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z1).color(outlineColor).endVertex();
         buffer.pos(x2, y1, z1).color(outlineColor).endVertex();
         buffer.pos(x2, y1, z2).color(outlineColor).endVertex();
         buffer.pos(x1, y1, z2).color(outlineColor).endVertex();
         tessellator.draw();
         buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z1).color(outlineColor).endVertex();
         buffer.pos(x1, y2, z1).color(0).endVertex();
         tessellator.draw();
         buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x2, y1, z1).color(outlineColor).endVertex();
         buffer.pos(x2, y2, z1).color(0).endVertex();
         tessellator.draw();
         buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z2).color(outlineColor).endVertex();
         buffer.pos(x1, y2, z2).color(0).endVertex();
         tessellator.draw();
         buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x2, y1, z2).color(outlineColor).endVertex();
         buffer.pos(x2, y2, z2).color(0).endVertex();
         tessellator.draw();
      }

      if (fullBox) {
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z1).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z1).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z1 + wz / 2.0).color(0).endVertex();
         buffer.pos(x1, y1, z1 + wz / 2.0).color(fullColor).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x2, y1, z1).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z1).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z1 + wz / 2.0).color(0).endVertex();
         buffer.pos(x2, y1, z1 + wz / 2.0).color(fullColor).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x2, y1, z2).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z2).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z1 + wz / 2.0).color(0).endVertex();
         buffer.pos(x2, y1, z1 + wz / 2.0).color(fullColor).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z2).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z2).color(fullColor).endVertex();
         buffer.pos(x1 + wx / 2.0, y1, z1 + wz / 2.0).color(0).endVertex();
         buffer.pos(x1, y1, z1 + wz / 2.0).color(fullColor).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z1).color(fullColor).endVertex();
         buffer.pos(x1, y2, z1).color(0).endVertex();
         buffer.pos(x2, y2, z1).color(0).endVertex();
         buffer.pos(x2, y1, z1).color(fullColor).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z1).color(fullColor).endVertex();
         buffer.pos(x1, y2, z1).color(0).endVertex();
         buffer.pos(x1, y2, z2).color(0).endVertex();
         buffer.pos(x1, y1, z2).color(fullColor).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x1, y1, z2).color(fullColor).endVertex();
         buffer.pos(x1, y2, z2).color(0).endVertex();
         buffer.pos(x2, y2, z2).color(0).endVertex();
         buffer.pos(x2, y1, z2).color(fullColor).endVertex();
         tessellator.draw();
         buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(x2, y1, z1).color(fullColor).endVertex();
         buffer.pos(x2, y2, z1).color(0).endVertex();
         buffer.pos(x2, y2, z2).color(0).endVertex();
         buffer.pos(x2, y1, z2).color(fullColor).endVertex();
         tessellator.draw();
      }

      GL11.glDisable(2848);
      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      GL11.glEnable(2884);
      GlStateManager.glLineWidth(1.0F);
      GlStateManager.popMatrix();
   }

   public static void drawGradientAlphaBoxWithBooleanDownPool(
      AxisAlignedBB bb, boolean outlineBox, boolean fullBox, boolean downPull, int outlineColor, int fullColor
   ) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder vertexbuffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      GL11.glDisable(3008);
      GL11.glDisable(2884);
      GL11.glShadeModel(7425);
      GL11.glEnable(2848);
      double x1 = bb.minX;
      double y1 = bb.minY;
      double z1 = bb.minZ;
      double x2 = bb.maxX;
      double y2 = bb.maxY;
      double z2 = bb.maxZ;
      double wx = x2 - x1;
      double wy = y2 - y1;
      double wz = z2 - z1;
      if (outlineBox) {
         GlStateManager.glLineWidth(2.0F);
         glColor(outlineColor);
         GL11.glBegin(2);
         GL11.glVertex3d(x1, y1, z1);
         GL11.glVertex3d(x2, y1, z1);
         GL11.glVertex3d(x2, y1, z2);
         GL11.glVertex3d(x1, y1, z2);
         GL11.glEnd();
         glColor(outlineColor);
         GL11.glBegin(3);
         GL11.glVertex3d(x1, y1, z1);
         glColor(0);
         GL11.glVertex3d(x1, y2, z1);
         GL11.glEnd();
         glColor(outlineColor);
         GL11.glBegin(3);
         GL11.glVertex3d(x2, y1, z1);
         glColor(0);
         GL11.glVertex3d(x2, y2, z1);
         GL11.glEnd();
         glColor(outlineColor);
         GL11.glBegin(3);
         GL11.glVertex3d(x1, y1, z2);
         glColor(0);
         GL11.glVertex3d(x1, y2, z2);
         GL11.glEnd();
         glColor(outlineColor);
         GL11.glBegin(3);
         GL11.glVertex3d(x2, y1, z2);
         glColor(0);
         GL11.glVertex3d(x2, y2, z2);
         GL11.glEnd();
      }

      if (fullBox) {
         if (downPull) {
            glColor(fullColor);
            GL11.glBegin(7);
            GL11.glVertex3d(x1, y1, z1);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z1);
            glColor(0);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z1 + wz / 2.0);
            glColor(fullColor);
            GL11.glVertex3d(x1, y1, z1 + wz / 2.0);
            GL11.glEnd();
            glColor(fullColor);
            GL11.glBegin(7);
            GL11.glVertex3d(x2, y1, z1);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z1);
            glColor(0);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z1 + wz / 2.0);
            glColor(fullColor);
            GL11.glVertex3d(x2, y1, z1 + wz / 2.0);
            GL11.glEnd();
            glColor(fullColor);
            GL11.glBegin(7);
            GL11.glVertex3d(x2, y1, z2);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z2);
            glColor(0);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z1 + wz / 2.0);
            glColor(fullColor);
            GL11.glVertex3d(x2, y1, z1 + wz / 2.0);
            GL11.glEnd();
            glColor(fullColor);
            GL11.glBegin(7);
            GL11.glVertex3d(x1, y1, z2);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z2);
            glColor(0);
            GL11.glVertex3d(x1 + wx / 2.0, y1, z1 + wz / 2.0);
            glColor(fullColor);
            GL11.glVertex3d(x1, y1, z1 + wz / 2.0);
            GL11.glEnd();
         }

         glColor(fullColor);
         GL11.glBegin(7);
         GL11.glVertex3d(x1, y1, z1);
         glColor(0);
         GL11.glVertex3d(x1, y2, z1);
         GL11.glVertex3d(x2, y2, z1);
         glColor(fullColor);
         GL11.glVertex3d(x2, y1, z1);
         GL11.glEnd();
         glColor(fullColor);
         GL11.glBegin(7);
         GL11.glVertex3d(x1, y1, z1);
         glColor(0);
         GL11.glVertex3d(x1, y2, z1);
         GL11.glVertex3d(x1, y2, z2);
         glColor(fullColor);
         GL11.glVertex3d(x1, y1, z2);
         GL11.glEnd();
         glColor(fullColor);
         GL11.glBegin(7);
         GL11.glVertex3d(x1, y1, z2);
         glColor(0);
         GL11.glVertex3d(x1, y2, z2);
         GL11.glVertex3d(x2, y2, z2);
         glColor(fullColor);
         GL11.glVertex3d(x2, y1, z2);
         GL11.glEnd();
         glColor(fullColor);
         GL11.glBegin(7);
         GL11.glVertex3d(x2, y1, z1);
         glColor(0);
         GL11.glVertex3d(x2, y2, z1);
         GL11.glVertex3d(x2, y2, z2);
         glColor(fullColor);
         GL11.glVertex3d(x2, y1, z2);
         GL11.glEnd();
      }

      GL11.glDisable(2848);
      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      GlStateManager.glLineWidth(1.0F);
      GlStateManager.popMatrix();
   }

   public static void enableGL2D3() {
      GL11.glDisable(2929);
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glDepthMask(true);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glHint(3155, 4354);
   }

   public static void disableGL2D3() {
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glEnable(2929);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glHint(3155, 4352);
   }

   public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
      Minecraft mc = Minecraft.getMinecraft();
      if (framebuffer != null && framebuffer.framebufferWidth == mc.displayWidth && framebuffer.framebufferHeight == mc.displayHeight) {
         return framebuffer;
      } else {
         if (framebuffer != null) {
            framebuffer.deleteFramebuffer();
         }

         return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
      }
   }

   public static boolean isInView(Entity ent) {
      frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      return ent instanceof EntityPlayerSP || frustum.isBoundingBoxInFrustum(ent.getEntityBoundingBox()) || ent.ignoreFrustumCheck;
   }

   public static void drawRound(float x, float y, float width, float height, float radius, int color) {
      GL11.glPushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      roundedShader.init();
      ShaderUtility.setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
      roundedShader.setUniformi("blur", 0);
      roundedShader.setUniformf(
         "color",
         ColorUtils.getGLRedFromColor(color),
         ColorUtils.getGLGreenFromColor(color),
         ColorUtils.getGLBlueFromColor(color),
         ColorUtils.getGLAlphaFromColor(color)
      );
      ShaderUtility.drawQuads(x - 1.0F, y - 1.0F, width + 2.0F, height + 2.0F);
      roundedShader.unload();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GL11.glPopMatrix();
   }

   public static void drawRoundOutline(
      float x, float y, float width, float height, float radius, float outlineThickness, int color, int outlineColor, ScaledResolution sr
   ) {
      GlStateManager.resetColor();
      GlStateManager.enableBlend();
      GL11.glDisable(3008);
      roundedOutlineShader.init();
      ShaderUtility.setupRoundedRectUniforms(x, y, width, height, radius, roundedOutlineShader);
      if ((float)roundedOutlineShader.getUniform("outlineThickness") != outlineThickness * (float)ScaledResolution.getScaleFactor()) {
         roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * (float)ScaledResolution.getScaleFactor());
      }

      roundedOutlineShader.setUniformColor("color", color);
      roundedOutlineShader.setUniformColor("outlineColor", outlineColor);
      ShaderUtility.drawQuads(
         x - (2.0F + outlineThickness), y - (2.0F + outlineThickness), width + 4.0F + outlineThickness * 2.0F, height + 4.0F + outlineThickness * 2.0F
      );
      roundedOutlineShader.unload();
      GL11.glEnable(3008);
   }

   public static void dispose() {
      GL11.glDisable(2960);
      GlStateManager.disableAlpha();
      GlStateManager.disableBlend();
   }

   public static void drawPlayerPing(float x, float y, EntityPlayer entity, float alpha) {
      if (Minecraft.player.connection.getPlayerInfo(entity.getUniqueID()) != null) {
         Gui gui = new Gui();
         NetworkPlayerInfo networkPlayerInfoIn = Minecraft.player.connection.getPlayerInfo(entity.getUniqueID());
         ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
         setupColor(ColorUtils.getFixedWhiteColor(), alpha);
         mc.getTextureManager().bindTexture(ICONS);
         int i = 0;
         int j;
         if (networkPlayerInfoIn.getResponseTime() < 0) {
            j = 5;
         } else if (networkPlayerInfoIn.getResponseTime() < 150) {
            j = 0;
         } else if (networkPlayerInfoIn.getResponseTime() < 300) {
            j = 1;
         } else if (networkPlayerInfoIn.getResponseTime() < 600) {
            j = 2;
         } else if (networkPlayerInfoIn.getResponseTime() < 1000) {
            j = 3;
         } else {
            j = 4;
         }

         GL11.glEnable(3042);
         GlStateManager.disableDepth();
         gui.zLevel += 100.0F;
         gui.drawTexturedModalRect(x, y, 0, 176 + j * 8, 10, 8);
         gui.zLevel -= 100.0F;
         GlStateManager.enableDepth();
         GlStateManager.resetColor();
      }
   }

   public static void fastRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius) {
      float z = 0.0F;
      if (paramXStart > paramXEnd) {
         z = paramXStart;
         paramXStart = paramXEnd;
         paramXEnd = z;
      }

      if (paramYStart > paramYEnd) {
         z = paramYStart;
         paramYStart = paramYEnd;
         paramYEnd = z;
      }

      double x1 = (double)(paramXStart + radius);
      double y1 = (double)(paramYStart + radius);
      double x2 = (double)(paramXEnd - radius);
      double y2 = (double)(paramYEnd - radius);
      GL11.glEnable(2848);
      GL11.glLineWidth(1.0F);
      buffer.begin(9, DefaultVertexFormats.POSITION);
      double degree = Math.PI / 180.0;

      for (double i = 0.0; i <= 90.0; i += 3.0) {
         buffer.pos(x2 + Math.sin(i * degree) * (double)radius, y2 + Math.cos(i * degree) * (double)radius).endVertex();
      }

      for (double i = 90.0; i <= 180.0; i += 3.0) {
         buffer.pos(x2 + Math.sin(i * degree) * (double)radius, y1 + Math.cos(i * degree) * (double)radius).endVertex();
      }

      for (double i = 180.0; i <= 270.0; i += 3.0) {
         buffer.pos(x1 + Math.sin(i * degree) * (double)radius, y1 + Math.cos(i * degree) * (double)radius).endVertex();
      }

      for (double i = 270.0; i <= 360.0; i += 3.0) {
         buffer.pos(x1 + Math.sin(i * degree) * (double)radius, y2 + Math.cos(i * degree) * (double)radius).endVertex();
      }

      tessellator.draw();
      GL11.glDisable(2848);
   }

   public static void erase(boolean invert) {
      GL11.glStencilFunc(invert ? 514 : 517, 1, 65535);
      GL11.glStencilOp(7680, 7680, 7681);
      GlStateManager.colorMask(true, true, true, true);
      GlStateManager.enableAlpha();
      GlStateManager.enableBlend();
      GL11.glAlphaFunc(516, 0.0F);
   }

   public static void write(boolean renderClipLayer) {
      checkSetupFBO1();
      GL11.glClearStencil(0);
      GL11.glClear(1024);
      GL11.glEnable(2960);
      GL11.glStencilFunc(519, 1, 65535);
      GL11.glStencilOp(7680, 7680, 7681);
      if (!renderClipLayer) {
         GlStateManager.colorMask(false, false, false, false);
      }
   }

   public static void write(boolean renderClipLayer, Framebuffer fb, boolean clearStencil, boolean invert) {
      checkSetupFBO(fb);
      if (clearStencil) {
         GL11.glClearStencil(0);
         GL11.glClear(1024);
         GL11.glEnable(2960);
      }

      GL11.glStencilFunc(519, invert ? 0 : 1, 65535);
      GL11.glStencilOp(7680, 7680, 7681);
      if (!renderClipLayer) {
         GlStateManager.colorMask(false, false, false, false);
      }
   }

   public static void checkSetupFBO1() {
      Framebuffer fbo = mc.getFramebuffer();
      if (fbo != null && fbo.depthBuffer > -1) {
         setupFBO1(fbo);
         fbo.depthBuffer = -1;
      }
   }

   public static void checkSetupFBO(Framebuffer fbo) {
      if (fbo != null && fbo.depthBuffer > -1) {
         setupFBO1(fbo);
         fbo.depthBuffer = -1;
      }
   }

   public static void setupFBO1(Framebuffer fbo) {
      EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
      int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
      EXTFramebufferObject.glBindRenderbufferEXT(36161, stencil_depth_buffer_ID);
      EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
      EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencil_depth_buffer_ID);
      EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencil_depth_buffer_ID);
   }

   public static void drawTexture(ResourceLocation rs, double f, double f2, double f3, double f4, double f5, double f6, double f7, double f8) {
      GL11.glPushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.99F);
      Minecraft.getMinecraft().getTextureManager().bindTexture(rs);
      double f9 = 1.0 / f7;
      double f10 = 1.0 / f8;
      buffer.begin(6, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
      buffer.pos(f, f2 + f6, 0.0).tex(f3 * f9, (f4 + f6) * f10).endVertex();
      buffer.pos(f + f5, f2 + f6, 0.0).tex((f3 + f5) * f9, (f4 + f6) * f10).endVertex();
      buffer.pos(f + f5, f2, 0.0).tex((f3 + f5) * f9, f4 * f10).endVertex();
      buffer.pos(f, f2, 0.0).tex(f3 * f9, f4 * f10).endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GL11.glPopMatrix();
   }

   public static void drawWaveGradient(
      float x, float y, float x2, float y2, float aPC, int colorStep1, int colorStep2, int colorStep3, int colorStep4, boolean blend, boolean toNull
   ) {
      float rect1X2 = x + (x2 - x) / 3.0F;
      float rect2X2 = x + (x2 - x) / 3.0F * 2.0F;
      float rect3X1 = x + (x2 - x) / 3.0F * 2.0F;
      int c1 = ColorUtils.getOverallColorFrom(colorStep1, ColorUtils.swapAlpha(-1, aPC * (float)ColorUtils.getAlphaFromColor(colorStep1)), aPC);
      int c2 = ColorUtils.getOverallColorFrom(colorStep2, ColorUtils.swapAlpha(-1, aPC * (float)ColorUtils.getAlphaFromColor(colorStep2)), aPC);
      int c3 = ColorUtils.getOverallColorFrom(colorStep3, ColorUtils.swapAlpha(-1, aPC * (float)ColorUtils.getAlphaFromColor(colorStep3)), aPC);
      int c4 = ColorUtils.getOverallColorFrom(colorStep4, ColorUtils.swapAlpha(-1, aPC * (float)ColorUtils.getAlphaFromColor(colorStep4)), aPC);
      drawFullGradientRectPro(rect1X2, y2, x, y, toNull ? 0 : colorStep2, toNull ? 0 : colorStep1, c1, c2, blend);
      drawFullGradientRectPro(rect2X2, y2, rect1X2, y, toNull ? 0 : colorStep3, toNull ? 0 : colorStep2, c2, c3, blend);
      drawFullGradientRectPro(x2, y2, rect3X1, y, toNull ? 0 : colorStep4, toNull ? 0 : colorStep3, c3, c4, blend);
   }

   public static void drawCircleAkrien(float cx, double cy, float radius, float c360, boolean astolfo, float lineWidthPercent) {
      GL11.glPushMatrix();
      customRotatedObject2D(cx, (float)cy, 0.0F, 0.0F, -90.0);
      cx *= 2.0F;
      cy *= 2.0;
      GlStateManager.glLineWidth(2.5F * lineWidthPercent);
      float theta = -0.0175F;
      float p = (float)Math.cos((double)theta);
      float s = (float)Math.sin((double)theta);
      float var17;
      float x = var17 = radius * 2.0F;
      float y = 0.0F;
      enableGL2D();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for (float ii = 0.0F; ii < c360; ii++) {
         int color = astolfo ? ColorUtils.astolfoColorsCool(1, 1000 + (int)(ii / 2.2F)) : ColorUtils.getColor(42, 42, 42);
         buffer.pos((double)(x + cx), (double)y + cy).color(color).endVertex();
         float t = x;
         x = p * x - s * y;
         y = s * t + p * y;
      }

      tessellator.draw();
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      disableGL2D();
      GlStateManager.resetColor();
      GlStateManager.glLineWidth(1.0F);
      GL11.glPopMatrix();
   }

   public static void drawCircle2D(float cx, double cy, float radius, float c360, int color, float lineWidth, boolean astolfo) {
      GL11.glPushMatrix();
      customRotatedObject2D(cx, (float)cy, 0.0F, 0.0F, -90.0);
      cx *= 2.0F;
      cy *= 2.0;
      float theta = -0.0175F;
      float p = (float)Math.cos((double)theta);
      float s = (float)Math.sin((double)theta);
      float var18;
      float x = var18 = radius * 2.0F;
      float y = 0.0F;
      enableGL2D();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      GlStateManager.glLineWidth(lineWidth);
      buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for (float ii = 0.0F; ii < c360; ii += 6.0F) {
         buffer.pos((double)(x + cx), (double)y + cy)
            .color(astolfo ? ColorUtils.swapAlpha(ColorUtils.astolfoColorsCool(1, 1000 + (int)(ii / 2.2F)), (float)ColorUtils.getAlphaFromColor(color)) : color)
            .endVertex();
         float t = x;

         for (float i = 0.0F; i < 6.0F; i++) {
            x = p * x - s * y;
            y = s * t + p * y;
         }
      }

      tessellator.draw();
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      disableGL2D();
      GlStateManager.resetColor();
      GlStateManager.glLineWidth(1.0F);
      GL11.glPopMatrix();
   }

   public static void drawSome(List<Vec2f> pos, int color) {
      if (!pos.isEmpty()) {
         GL11.glPushMatrix();
         GL11.glPushAttrib(1048575);
         GL11.glEnable(3042);
         GL11.glDisable(2884);
         GL11.glDisable(3553);
         GL11.glDisable(3008);
         buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);

         for (Vec2f vec2f : pos) {
            buffer.pos((double)vec2f.x, (double)vec2f.y).color(color).endVertex();
         }

         tessellator.draw();
         GL11.glEnable(3008);
         GL11.glEnable(3553);
         GL11.glEnable(2884);
         GL11.glPopAttrib();
         GL11.glPopMatrix();
      }
   }

   public static void drawSome(List<Vec2f> pos, int color, int begin) {
      if (!pos.isEmpty()) {
         GL11.glPushMatrix();
         GL11.glPushAttrib(1048575);
         GL11.glEnable(3042);
         GL11.glDisable(2884);
         GL11.glDisable(3553);
         GL11.glDisable(3008);
         buffer.begin(begin, DefaultVertexFormats.POSITION_COLOR);

         for (Vec2f vec2f : pos) {
            buffer.pos((double)vec2f.x, (double)vec2f.y).color(color).endVertex();
         }

         tessellator.draw();
         GL11.glEnable(3008);
         GL11.glEnable(3553);
         GL11.glEnable(2884);
         GL11.glPopAttrib();
         GL11.glPopMatrix();
      }
   }

   public static void drawSome2(List<net.minecraft.util.math.Vec2f> pos, int color, int begin) {
      if (!pos.isEmpty()) {
         GL11.glPushMatrix();
         GL11.glPushAttrib(1048575);
         GL11.glEnable(3042);
         GL11.glDisable(2884);
         GL11.glDisable(3553);
         GL11.glDisable(3008);
         buffer.begin(begin, DefaultVertexFormats.POSITION_COLOR);

         for (net.minecraft.util.math.Vec2f vec2f : pos) {
            buffer.pos((double)vec2f.x, (double)vec2f.y).color(color).endVertex();
         }

         tessellator.draw();
         GL11.glEnable(3008);
         GL11.glEnable(3553);
         GL11.glEnable(2884);
         GL11.glPopAttrib();
         GL11.glPopMatrix();
      }
   }

   public static void drawVec2Colored(List<Vec2fColored> pos) {
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glDisable(2884);
      GL11.glDisable(3553);
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      anialisON(false, true, false);
      buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);

      for (Vec2fColored vec : pos) {
         buffer.pos((double)vec.getX(), (double)vec.getY()).color(vec.getColor()).endVertex();
      }

      tessellator.draw();
      anialisOFF(false, true, false);
      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      GL11.glEnable(3553);
      GL11.glEnable(2884);
      GL11.glPopMatrix();
   }

   public static void drawPolygonPartsGlowBackSAlpha(double x, double y, int radius, int part, int color, int endcolor, float Alpha) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha1 = (float)(endcolor >> 24 & 0xFF) / 255.0F;
      float red1 = (float)(endcolor >> 16 & 0xFF) / 255.0F;
      float green1 = (float)(endcolor >> 8 & 0xFF) / 255.0F;
      float blue1 = (float)(endcolor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, y, 0.0).color(red, green, blue, Alpha).endVertex();
      double TWICE_PI = Math.PI * 8;

      for (int i = part * 90; i <= part * 90 + 90; i += 6) {
         double angle = TWICE_PI * (double)i / 360.0 + Math.toRadians(30.0);
         bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(red1, green1, blue1, alpha1).endVertex();
      }

      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawPolygonPartsGlowBackSAlpha(double x, double y, float radius, int part, int color, int endcolor, float Alpha, boolean bloom) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha1 = (float)(endcolor >> 24 & 0xFF) / 255.0F;
      float red1 = (float)(endcolor >> 16 & 0xFF) / 255.0F;
      float green1 = (float)(endcolor >> 8 & 0xFF) / 255.0F;
      float blue1 = (float)(endcolor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, y, 0.0).color(red, green, blue, Alpha).endVertex();
      double TWICE_PI = Math.PI * 8;

      for (int i = part * 90; i <= part * 90 + 90; i += 6) {
         double angle = TWICE_PI * (double)i / 360.0 + Math.toRadians(30.0);
         bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(red1, green1, blue1, alpha1).endVertex();
      }

      tessellator.draw();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void fixShadows() {
      GlStateManager.enableBlend();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F);
   }

   public static void drawFullGradientRect(float x, float y, float w, float h, int color, int color2, int color3, int color4) {
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableTexture2D();
      GL11.glShadeModel(7425);
      GL11.glDisable(3008);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos((double)x, (double)(y + h)).color(color).endVertex();
      buffer.pos((double)(x + w), (double)(y + h)).color(color2).endVertex();
      buffer.pos((double)(x + w), (double)y).color(color3).endVertex();
      buffer.pos((double)x, (double)y).color(color4).endVertex();
      tessellator.draw();
      GL11.glEnable(3008);
      GL11.glShadeModel(7424);
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      resetColor();
   }

   public static void drawFullGradientRectPro(float x, float y, float x2, float y2, int color, int color2, int color3, int color4, boolean blend) {
      GlStateManager.enableBlend();
      if (blend) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE_MINUS_CONSTANT_ALPHA,
            GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GL11.glShadeModel(7425);
      GL11.glDisable(3008);
      buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos((double)x2, (double)y, 0.0).color(color3).endVertex();
      buffer.pos((double)x, (double)y, 0.0).color(color4).endVertex();
      buffer.pos((double)x, (double)y2, 0.0).color(color).endVertex();
      buffer.pos((double)x2, (double)y2, 0.0).color(color2).endVertex();
      tessellator.draw();
      if (blend) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GL11.glEnable(3008);
      GL11.glShadeModel(7424);
      GlStateManager.enableTexture2D();
      GlStateManager.resetColor();
   }

   public static void resetBlender() {
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableTexture2D();
      GL11.glShadeModel(7425);
      GL11.glDisable(3008);
      GL11.glEnable(3008);
      GL11.glShadeModel(7424);
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.resetColor();
   }

   public static void drawMinecraftRect(int left, int top, int right, int bottom, int startColor, int endColor) {
      float f = (float)(startColor >> 24 & 0xFF) / 255.0F;
      float f1 = (float)(startColor >> 16 & 0xFF) / 255.0F;
      float f2 = (float)(startColor >> 8 & 0xFF) / 255.0F;
      float f3 = (float)(startColor & 0xFF) / 255.0F;
      float f4 = (float)(endColor >> 24 & 0xFF) / 255.0F;
      float f5 = (float)(endColor >> 16 & 0xFF) / 255.0F;
      float f6 = (float)(endColor >> 8 & 0xFF) / 255.0F;
      float f7 = (float)(endColor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)right, (double)top, 300.0).color(f1, f2, f3, f).endVertex();
      bufferbuilder.pos((double)left, (double)top, 300.0).color(f1, f2, f3, f).endVertex();
      bufferbuilder.pos((double)left, (double)bottom, 300.0).color(f5, f6, f7, f4).endVertex();
      bufferbuilder.pos((double)right, (double)bottom, 300.0).color(f5, f6, f7, f4).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static Vec3d getRenderPos(double x, double y, double z) {
      x -= RenderManager.viewerPosX;
      y -= RenderManager.viewerPosY;
      z -= RenderManager.viewerPosZ;
      return new Vec3d(x, y, z);
   }

   public static void drawBorderedRect(float x, float y, float width, float height, float outlineThickness, int rectColor, int outlineColor) {
      Gui.drawRect2((double)x, (double)y, (double)width, (double)height, rectColor);
      GL11.glEnable(2848);
      GLUtil.setup2DRendering(() -> {
         color(outlineColor);
         GL11.glLineWidth(outlineThickness);
         float cornerValue = (float)((double)outlineThickness * 0.19);
         GLUtil.render(1, () -> {
            GL11.glVertex2d((double)x, (double)(y - cornerValue));
            GL11.glVertex2d((double)x, (double)(y + height + cornerValue));
            GL11.glVertex2d((double)(x + width), (double)(y + height + cornerValue));
            GL11.glVertex2d((double)(x + width), (double)(y - cornerValue));
            GL11.glVertex2d((double)x, (double)y);
            GL11.glVertex2d((double)(x + width), (double)y);
            GL11.glVertex2d((double)x, (double)(y + height));
            GL11.glVertex2d((double)(x + width), (double)(y + height));
         });
      });
      GL11.glDisable(2848);
   }

   public static void renderRoundedRect(float x, float y, float width, float height, float radius, int color) {
      drawAlphedRect((double)(x + radius / 2.0F), (double)(y + radius / 2.0F), (double)(x + width - radius / 2.0F), (double)(y + height - radius / 2.0F), color);
   }

   public static void fullRoundFG(float x, float y, float x2, float y2, float r, int c, int c2, int c3, int c4, boolean bloom) {
      drawFullGradientRectPro(x + r / 2.0F, y + r / 2.0F, x2 - r / 2.0F, y2 - r / 2.0F, c4, c3, c2, c, bloom);
      drawFullGradientRectPro(x + r / 2.0F, y, x2 - r / 2.0F, y + r / 2.0F, c, c2, c2, c, bloom);
      drawFullGradientRectPro(x + r / 2.0F, y2 - r / 2.0F, x2 - r / 2.0F, y2, c4, c3, c3, c4, bloom);
      drawFullGradientRectPro(x, y + r / 2.0F, x + r / 2.0F, y2 - r / 2.0F, c4, c4, c, c, bloom);
      drawFullGradientRectPro(x2 - r / 2.0F, y + r / 2.0F, x2, y2 - r / 2.0F, c3, c3, c2, c2, bloom);
      StencilUtil.initStencilToWrite();
      drawRect((double)x, (double)(y + r / 2.0F), (double)x2, (double)(y2 - r / 2.0F), -1);
      drawRect((double)(x + r / 2.0F), (double)y, (double)(x2 - r / 2.0F), (double)y2, -1);
      StencilUtil.readStencilBuffer(0);
      drawSmoothCircle((double)(x + r / 2.0F), (double)(y + r / 2.0F + 0.125F), r / 2.0F, c, bloom);
      drawSmoothCircle((double)(x2 - r / 2.0F), (double)(y + r / 2.0F + 0.125F), r / 2.0F, c2, bloom);
      drawSmoothCircle((double)(x2 - r / 2.0F), (double)(y2 - r / 2.0F + 0.125F), r / 2.0F, c3, bloom);
      drawSmoothCircle((double)(x + r / 2.0F), (double)(y2 - r / 2.0F + 0.125F), r / 2.0F, c4, bloom);
      StencilUtil.uninitStencilBuffer();
   }

   public static void drawSmoothCircle(double x, double y, float radius, int color) {
      runGLColor(color);
      setup2D(() -> {
         GL11.glDisable(3008);
         GL11.glEnable(2832);
         GL11.glPointSize(radius * (float)(2 * Minecraft.getMinecraft().gameSettings.guiScale));
         renderObj(0, () -> GL11.glVertex2d(x, y));
         GL11.glEnable(3008);
      });
      GlStateManager.resetColor();
   }

   public static void drawSmoothCircle(double x, double y, float radius, int color, boolean bloom) {
      runGLColor(color);
      setup2D(
         () -> {
            if (bloom) {
               GlStateManager.tryBlendFuncSeparate(
                  GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
               );
            }

            GL11.glDisable(3008);
            GL11.glEnable(2832);
            GL11.glHint(3153, 4354);
            new ScaledResolution(mc);
            float scale = (float)((double)ScaledResolution.getScaleFactor() / Math.pow((double)ScaledResolution.getScaleFactor(), 2.0));
            GL11.glPointSize(radius / scale * 2.0F);
            renderObj(0, () -> GL11.glVertex2d(x, y));
            GL11.glEnable(3008);
            if (bloom) {
               GlStateManager.tryBlendFuncSeparate(
                  GlStateManager.SourceFactor.SRC_ALPHA,
                  GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                  GlStateManager.SourceFactor.ONE,
                  GlStateManager.DestFactor.ZERO
               );
            }
         }
      );
   }

   public static void drawCroneRect(double x, double y, double width, double height, int color) {
      resetColor();
      setup2D(() -> renderObj(7, () -> {
            runGLColor(color);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x, y + height);
            GL11.glVertex2d(x + width, y + height);
            GL11.glVertex2d(x + width, y);
         }));
   }

   public static void setup2D(Runnable f) {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3553);
      f.run();
      GL11.glEnable(3553);
   }

   public static void renderObj(int mode, Runnable render) {
      GL11.glBegin(mode);
      render.run();
      GL11.glEnd();
   }

   public static void runGLColor(int orRGB) {
      float c1 = (float)(orRGB >> 16 & 0xFF) / 255.0F;
      float c2 = (float)(orRGB >> 8 & 0xFF) / 255.0F;
      float c3 = (float)(orRGB & 0xFF) / 255.0F;
      float c4 = (float)(orRGB >> 24 & 0xFF) / 255.0F;
      GL11.glColor4f(c1, c2, c3, c4);
   }

   public static void scale(float x, float y, float scale, Runnable data) {
      GL11.glPushMatrix();
      GL11.glTranslatef(x, y, 0.0F);
      GL11.glScalef(scale, scale, 1.0F);
      GL11.glTranslatef(-x, -y, 0.0F);
      data.run();
      GL11.glPopMatrix();
   }

   public static void scaleStart(float x, float y, float scale) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 0.0F);
      GlStateManager.scale(scale, scale, 1.0F);
      GlStateManager.translate(-x, -y, 0.0F);
   }

   public static void scaleEnd() {
      GlStateManager.popMatrix();
   }

   public static void fakeCircleGlow(float posX, float posY, float radius, Color color, float maxAlpha) {
      setAlphaLimit(0.0F);
      GL11.glShadeModel(7425);
      GLUtil.setup2DRendering(() -> GLUtil.render(6, () -> {
            color(color.getRGB(), maxAlpha);
            GL11.glVertex2d((double)posX, (double)posY);
            color(color.getRGB(), 0.0F);

            for (int i = 0; i <= 100; i += 2) {
               double angle = (double)i * 0.06283 + 3.1415;
               double x2 = Math.sin(angle) * (double)radius;
               double y2 = Math.cos(angle) * (double)radius;
               GL11.glVertex2d((double)posX + x2, (double)posY + y2);
            }
         }));
      GL11.glShadeModel(7424);
      setAlphaLimit(1.0F);
   }

   public static double animate(double endPoint, double current, double speed) {
      boolean shouldContinueAnimation = endPoint > current;
      if (speed < 0.0) {
         speed = 0.0;
      } else if (speed > 1.0) {
         speed = 1.0;
      }

      double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
      double factor = dif * speed;
      return current + (shouldContinueAnimation ? factor : -factor);
   }

   public static void drawCircleNotSmooth(double x, double y, double radius, int color) {
      radius /= 2.0;
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glDisable(2884);
      color(color);
      GL11.glBegin(6);

      for (double i = 0.0; i <= 30.0; i += 3.0) {
         double angle = i * 0.01745 * 12.0;
         GL11.glVertex2d(x + radius * Math.cos(angle) + radius, y + radius * Math.sin(angle) + radius);
      }

      GL11.glEnd();
      GL11.glEnable(2884);
      GL11.glEnable(3553);
   }

   public static void scissor(double x, double y, double width, double height, Runnable data) {
      GL11.glEnable(3089);
      scissor(x, y, width, height);
      data.run();
      GL11.glDisable(3089);
   }

   public static void scissor(double x, double y, double width, double height) {
      Minecraft mc = Minecraft.getMinecraft();
      ScaledResolution sr = new ScaledResolution(mc);
      double scale = (double)ScaledResolution.getScaleFactor();
      double finalHeight = height * scale;
      double finalY = ((double)sr.getScaledHeight() - y) * scale;
      double finalX = x * scale;
      double finalWidth = width * scale;
      GL11.glScissor((int)finalX, (int)(finalY - finalHeight), (int)finalWidth, (int)finalHeight);
   }

   public static void scissorRected(double x, double y, double x2, double y2) {
      Minecraft mc = Minecraft.getMinecraft();
      ScaledResolution sr = new ScaledResolution(mc);
      double scale = (double)ScaledResolution.getScaleFactor();
      double finalHeight = (y2 - y) * scale;
      double finalY = ((double)sr.getScaledHeight() - y) * scale;
      double finalX = x * scale;
      double finalWidth = (x2 - y) * scale;
      GL11.glScissor((int)finalX, (int)(finalY - finalHeight), (int)finalWidth, (int)finalHeight);
   }

   public static void scissorCoord(double x, double y, double x2, double y2) {
      Minecraft mc = Minecraft.getMinecraft();
      double xPos1 = x < x2 ? x : x2;
      double xPos2 = x2 > x ? x2 : x;
      double yPos1 = y < y2 ? y : y2;
      double yPos2 = y2 > y ? y2 : y;
      GL11.glScissor((int)xPos1, (int)xPos2, (int)yPos1, (int)yPos2);
   }

   public static void setAlphaLimit(float limit) {
      GlStateManager.enableAlpha();
      GlStateManager.alphaFunc(516, (float)((double)limit * 0.01));
   }

   public static void color(int color, float alpha) {
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      GlStateManager.color(r, g, b, alpha);
   }

   public static void bindTexture(int texture) {
      GL11.glBindTexture(3553, texture);
   }

   public static void resetColor() {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
      return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
   }

   public static void drawLightShot(float x, float y, int colorIn, int colorOut) {
      glRenderStart();
      GL11.glEnable(2848);
      setupColor(colorIn, (float)colorOut);
      GL11.glTranslated((double)x, (double)y, 0.0);
      GL11.glBegin(7);
      GL11.glVertex2d(0.0, 0.0);
      GL11.glVertex2d(5.0, 6.0);
      GL11.glVertex2d(-5.0, -6.0);
      GL11.glVertex2d(0.0, 6.0);
      GL11.glEnd();
      GL11.glTranslated(-1.0, 9.5, 0.0);
      GL11.glBegin(7);
      GL11.glVertex2d(0.0, 0.0);
      GL11.glVertex2d(-5.0, -6.0);
      GL11.glVertex2d(5.0, 6.0);
      GL11.glVertex2d(0.0, -6.0);
      GL11.glEnd();
      GL11.glDisable(2848);
      glRenderStop();
   }

   public static void drawBorder(
      double left, double top, double right, double bottom, double borderWidth, int insideColor, int borderColor, boolean borderIncludedInBounds
   ) {
      drawRect(
         left - (!borderIncludedInBounds ? borderWidth : 0.0),
         top - (!borderIncludedInBounds ? borderWidth : 0.0),
         right + (!borderIncludedInBounds ? borderWidth : 0.0),
         bottom + (!borderIncludedInBounds ? borderWidth : 0.0),
         borderColor
      );
      drawRect(
         left + (borderIncludedInBounds ? borderWidth : 0.0),
         top + (borderIncludedInBounds ? borderWidth : 0.0),
         right - (borderIncludedInBounds ? borderWidth : 0.0),
         bottom - (borderIncludedInBounds ? borderWidth : 0.0),
         insideColor
      );
   }

   public static void renderOne() {
      checkSetupFBO();
      GL11.glPushAttrib(1048575);
      GL11.glDisable(3008);
      GL11.glDisable(3553);
      GL11.glDisable(2896);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glLineWidth(3.0F);
      GL11.glEnable(2848);
      GL11.glEnable(2960);
      GL11.glClear(1024);
      GL11.glClearStencil(15);
      GL11.glStencilFunc(512, 1, 15);
      GL11.glStencilOp(7681, 7681, 7681);
      GL11.glPolygonMode(1032, 6913);
   }

   public static void renderTwo() {
      GL11.glStencilFunc(512, 0, 15);
      GL11.glStencilOp(7681, 7681, 7681);
      GL11.glPolygonMode(1032, 6914);
   }

   public static void renderThree() {
      GL11.glStencilFunc(514, 1, 15);
      GL11.glStencilOp(7680, 7680, 7680);
      GL11.glPolygonMode(1032, 6913);
   }

   public static void renderFour() {
      setColor(new Color(255, 255, 255));
      GL11.glDepthMask(false);
      GL11.glDisable(2929);
      GL11.glEnable(10754);
      GL11.glPolygonOffset(1.0F, -2000000.0F);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
   }

   public static void renderFive() {
      GL11.glPolygonOffset(1.0F, 2000000.0F);
      GL11.glDisable(10754);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glDisable(2960);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glEnable(3042);
      GL11.glEnable(2896);
      GL11.glEnable(3553);
      GL11.glEnable(3008);
      GL11.glPopAttrib();
   }

   public static void scissorRect(float x, float y, float width, double height) {
      ScaledResolution sr = new ScaledResolution(mc);
      int factor = ScaledResolution.getScaleFactor();
      GL11.glScissor(
         (int)(x * (float)factor),
         (int)(((double)((float)sr.getScaledHeight()) - height) * (double)((float)factor)),
         (int)((width - x) * (float)factor),
         (int)((height - (double)y) * (double)((float)factor))
      );
   }

   public static void setColor(Color c) {
      GL11.glColor4d(
         (double)((float)c.getRed() / 255.0F),
         (double)((float)c.getGreen() / 255.0F),
         (double)((float)c.getBlue() / 255.0F),
         (double)((float)c.getAlpha() / 255.0F)
      );
   }

   public static void drawSkeetRect(float x, float y, float right, float bottom) {
      drawRect((double)(x - 46.5F), (double)(y - 66.5F), (double)(right + 46.5F), (double)(bottom + 66.5F), new Color(0, 0, 0, 255).getRGB());
      drawRect((double)(x - 46.0F), (double)(y - 66.0F), (double)(right + 46.0F), (double)(bottom + 66.0F), new Color(48, 48, 48, 255).getRGB());
      drawRect((double)(x - 44.5F), (double)(y - 64.5F), (double)(right + 44.5F), (double)(bottom + 64.5F), new Color(33, 33, 33, 255).getRGB());
      drawRect((double)(x - 43.5F), (double)(y - 63.5F), (double)(right + 43.5F), (double)(bottom + 63.5F), new Color(0, 0, 0, 255).getRGB());
      drawRect((double)(x - 43.0F), (double)(y - 63.0F), (double)(right + 43.0F), (double)(bottom + 63.0F), new Color(9, 9, 9, 255).getRGB());
      drawRect((double)(x - 40.5F), (double)(y - 60.5F), (double)(right + 40.5F), (double)(bottom + 60.5F), new Color(48, 48, 48, 255).getRGB());
      drawRect((double)(x - 40.0F), (double)(y - 60.0F), (double)(right + 40.0F), (double)(bottom + 60.0F), new Color(17, 17, 17, 255).getRGB());
   }

   public static void drawSkeetButton(float x, float y, float right, float bottom) {
      drawRect((double)(x - 31.0F), (double)(y - 43.0F), (double)(right + 31.0F), (double)(bottom - 30.0F), new Color(0, 0, 0, 255).getRGB());
      drawRect((double)(x - 30.5F), (double)(y - 42.5F), (double)(right + 30.5F), (double)(bottom - 30.5F), new Color(45, 45, 45, 255).getRGB());
   }

   public static void checkSetupFBO() {
      Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
      if (fbo != null && fbo.depthBuffer > -1) {
         setupFBO(fbo);
         fbo.depthBuffer = -1;
      }
   }

   public static void drawPolygonParts(double x, double y, int radius, int part, int color, int endcolor) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha1 = (float)(endcolor >> 24 & 0xFF) / 255.0F;
      float red1 = (float)(endcolor >> 16 & 0xFF) / 255.0F;
      float green1 = (float)(endcolor >> 8 & 0xFF) / 255.0F;
      float blue1 = (float)(endcolor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
      double TWICE_PI = Math.PI * 2;

      for (int i = part * 90; i <= part * 90 + 90; i += 6) {
         double angle = (Math.PI * 2) * (double)i / 360.0 + Math.toRadians(180.0);
         bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(red1, green1, blue1, alpha1).endVertex();
      }

      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawPolygonParts(double x, double y, float radius, int part, int color, int endcolor, boolean bloom) {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      } else {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, y, 0.0).color(color).endVertex();
      double TWICE_PI = Math.PI * 2;

      for (int i = part * 90; i <= part * 90 + 90; i += 18) {
         double angle = (Math.PI * 2) * (double)i / 360.0 + Math.toRadians(180.0);
         bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(endcolor).endVertex();
      }

      tessellator.draw();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawTenasityRect(float left, float top, float right, float bottom, float smoth, int color) {
      GL11.glEnable(3042);
      GL11.glEnable(2848);
      drawRect((double)left, (double)top, (double)right, (double)bottom, color);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      drawPolygonParts((double)(left * 2.0F), (double)(top * 2.0F), (int)smoth, 0, color, color);
      drawPolygonParts((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 5, color, color);
      drawPolygonParts((double)(right * 2.0F), (double)(top * 2.0F), (int)smoth, 7, color, color);
      drawPolygonParts((double)(right * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 6, color, color);
      drawAlphedRect((double)(left * 2.0F - smoth), (double)(top * 2.0F), (double)(left * 2.0F), (double)(bottom * 2.0F - smoth), color);
      drawAlphedRect((double)(left * 2.0F), (double)(top * 2.0F - smoth), (double)(right * 2.0F), (double)(top * 2.0F), color);
      drawAlphedRect((double)(right * 2.0F), (double)(top * 2.0F), (double)(right * 2.0F + smoth), (double)(bottom * 2.0F - smoth), color);
      drawAlphedRect((double)(left * 2.0F), (double)(bottom * 2.0F), (double)(right * 2.0F), (double)(bottom * 2.0F), color);
      GL11.glDisable(3042);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
   }

   public static void drawCircledTo(float r, int c) {
      GL11.glPushMatrix();
      GlStateManager.glLineWidth(2.0F);
      float theta = 0.0175F;
      float p = (float)Math.cos((double)theta);
      float s = (float)Math.sin((double)theta);
      float var9;
      float x = var9 = r * 2.0F;
      float y = 0.0F;
      enableGL2D();
      GL11.glDisable(3008);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for (int ii = 0; ii < 90; ii++) {
         buffer.pos((double)x, (double)y).color(c).endVertex();
         float t = x;
         x = p * x - s * y;
         y = s * t + p * y;
      }

      tessellator.draw();
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      GL11.glEnable(3008);
      disableGL2D();
      GlStateManager.resetColor();
      GlStateManager.glLineWidth(1.0F);
      GL11.glPopMatrix();
   }

   public static void roundedFullRoundedOutline(float x, float y, float x2, float y2, float round1, float round2, float round3, float round4, int color) {
      GL11.glPushMatrix();
      GL11.glTranslated((double)(x + round1), (double)(y + round1), 0.0);
      GL11.glRotated(-180.0, 0.0, 0.0, 180.0);
      drawCircledTo(round1, color);
      fixShadows();
      GL11.glRotated(180.0, 0.0, 0.0, -180.0);
      GL11.glTranslated((double)(-x - round1), (double)(-y - round1), 0.0);
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      GL11.glTranslated((double)(x2 - round2), (double)(y + round2), 0.0);
      GL11.glRotated(-90.0, 0.0, 0.0, 90.0);
      drawCircledTo(round2, color);
      fixShadows();
      GL11.glRotated(90.0, 0.0, 0.0, -90.0);
      GL11.glTranslated((double)(-x2 + round2), (double)(-y - round2), 0.0);
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      GL11.glTranslated((double)(x2 - round3), (double)(y2 - round3), 0.0);
      GL11.glRotated(-360.0, 0.0, 0.0, 360.0);
      drawCircledTo(round3, color);
      fixShadows();
      GL11.glRotated(360.0, 0.0, 0.0, -360.0);
      GL11.glTranslated((double)(-x2 + round3), (double)(-y2 + round3), 0.0);
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      GL11.glTranslated((double)(x + round4), (double)(y2 - round4), 0.0);
      GL11.glRotated(-270.0, 0.0, 0.0, 270.0);
      drawCircledTo(round4, color);
      fixShadows();
      GL11.glRotated(270.0, 0.0, 0.0, -270.0);
      GL11.glTranslated((double)(-x - round4), (double)(-y2 + round4), 0.0);
      GL11.glPopMatrix();
      drawAlphedRect((double)(x + round1 - 1.0F), (double)(y - 0.5F), (double)(x2 - round2), (double)(y + 0.5F), color);
      drawAlphedRect((double)(x2 - 0.5F), (double)(y + round2 - 1.0F), (double)(x2 + 0.5F), (double)(y2 - round3), color);
      drawAlphedRect((double)(x - 0.5F), (double)(y + round1), (double)(x + 0.5F), (double)(y2 - round4), color);
      drawAlphedRect((double)(x + round4), (double)(y2 - 0.5F), (double)(x2 - round3 + 1.0F), (double)(y2 + 0.5F), color);
   }

   public static void drawAlphedVGradient(double x, double y, double x2, double y2, int col1, int col2) {
      drawAlphedVGradient(x, y, x2, y2, col1, col2, false);
   }

   public static void drawAlphedVGradient(double x, double y, double x2, double y2, int col1, int col2, boolean bloom) {
      float f = (float)(col1 >> 24 & 0xFF) / 255.0F;
      float f1 = (float)(col1 >> 16 & 0xFF) / 255.0F;
      float f2 = (float)(col1 >> 8 & 0xFF) / 255.0F;
      float f3 = (float)(col1 & 0xFF) / 255.0F;
      float f4 = (float)(col2 >> 24 & 0xFF) / 255.0F;
      float f5 = (float)(col2 >> 16 & 0xFF) / 255.0F;
      float f6 = (float)(col2 >> 8 & 0xFF) / 255.0F;
      float f7 = (float)(col2 & 0xFF) / 255.0F;
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, bloom ? '耄' : 771);
      GL11.glEnable(2848);
      GL11.glShadeModel(7425);
      GL11.glPushMatrix();
      GL11.glDisable(3008);
      GL11.glBegin(7);
      GL11.glColor4f(f1, f2, f3, f);
      GL11.glVertex2d(x2, y);
      GL11.glVertex2d(x, y);
      GL11.glColor4f(f5, f6, f7, f4);
      GL11.glVertex2d(x, y2);
      GL11.glVertex2d(x2, y2);
      GL11.glEnd();
      if (bloom) {
         GL11.glBlendFunc(770, 771);
      }

      GL11.glEnable(3008);
      GL11.glPopMatrix();
      GL11.glEnable(3553);
      GL11.glDisable(2848);
      GL11.glShadeModel(7424);
   }

   public static void drawRoundedFullGradient(
      float left, float top, float right, float bottom, float smoth, int color, int color2, int color3, int color4, boolean bloom
   ) {
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      GL11.glEnable(3042);
      GL11.glEnable(2848);
      drawFullGradientRect(left, top, right - left, bottom - top - smoth / 2.0F, color3, color4, color2, color);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      drawPolygonParts((double)(left * 2.0F), (double)(top * 2.0F), (int)smoth, 0, color, color);
      drawPolygonParts((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 5, color, color3);
      drawPolygonParts((double)(right * 2.0F), (double)(top * 2.0F), (int)smoth, 7, color, color2);
      drawPolygonParts((double)(right * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 6, color, color4);
      drawAlphedVGradient((double)(left * 2.0F - smoth), (double)(top * 2.0F), (double)(left * 2.0F), (double)(bottom * 2.0F - smoth), color, color3);
      drawAlphedSideways((double)(left * 2.0F), (double)(top * 2.0F - smoth), (double)(right * 2.0F), (double)(top * 2.0F), color, color2);
      drawAlphedVGradient((double)(right * 2.0F), (double)(top * 2.0F), (double)(right * 2.0F + smoth), (double)(bottom * 2.0F - smoth), color2, color4);
      drawAlphedSideways((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (double)(right * 2.0F), (double)(bottom * 2.0F), color3, color4);
      GL11.glDisable(3042);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }
   }

   public static void drawRoundedFullGradientPro(
      float left, float top, float right, float bottom, float smoth, int color, int color2, int color3, int color4, boolean bloom
   ) {
      left += smoth;
      right -= smoth;
      top += smoth;
      GL11.glPushMatrix();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      GL11.glEnable(3042);
      GL11.glEnable(2848);
      drawFullGradientRect(left, top, right - left, bottom - top - smoth / 2.0F, color3, color4, color2, color);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      drawPolygonParts((double)(left * 2.0F), (double)(top * 2.0F), (int)smoth, 0, color, color);
      drawPolygonParts((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 5, color, color3);
      drawPolygonParts((double)(right * 2.0F), (double)(top * 2.0F), (int)smoth, 7, color, color2);
      drawPolygonParts((double)(right * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 6, color, color4);
      drawAlphedVGradient((double)(left * 2.0F - smoth), (double)(top * 2.0F), (double)(left * 2.0F), (double)(bottom * 2.0F - smoth), color, color3);
      drawAlphedSideways((double)(left * 2.0F), (double)(top * 2.0F - smoth), (double)(right * 2.0F), (double)(top * 2.0F), color, color2);
      drawAlphedVGradient((double)(right * 2.0F), (double)(top * 2.0F), (double)(right * 2.0F + smoth), (double)(bottom * 2.0F - smoth), color2, color4);
      drawAlphedSideways((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (double)(right * 2.0F), (double)(bottom * 2.0F), color3, color4);
      GL11.glDisable(3042);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.resetColor();
      GL11.glPopMatrix();
   }

   public static void drawRoundedFullGradient(float left, float top, float right, float bottom, float smoth, int color, int color2, boolean bloom) {
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      GL11.glEnable(3042);
      GL11.glEnable(2848);
      drawFullGradientRect(left, top, right - left, bottom - top - smoth / 2.0F, color2, color, color2, color);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      drawPolygonParts((double)(left * 2.0F), (double)(top * 2.0F), (int)smoth, 0, color, color);
      drawPolygonParts((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 5, color, color2);
      drawPolygonParts((double)(right * 2.0F), (double)(top * 2.0F), (int)smoth, 7, color, color2);
      drawPolygonParts((double)(right * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 6, color, color);
      drawAlphedVGradient((double)(left * 2.0F - smoth), (double)(top * 2.0F), (double)(left * 2.0F), (double)(bottom * 2.0F - smoth), color, color2);
      drawAlphedSideways((double)(left * 2.0F), (double)(top * 2.0F - smoth), (double)(right * 2.0F), (double)(top * 2.0F), color, color2);
      drawAlphedVGradient((double)(right * 2.0F), (double)(top * 2.0F), (double)(right * 2.0F + smoth), (double)(bottom * 2.0F - smoth), color2, color);
      drawAlphedSideways((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (double)(right * 2.0F), (double)(bottom * 2.0F), color2, color);
      GL11.glDisable(3042);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }
   }

   public static void drawRoundedFullGradientRectPro(
      float x, float y, float x2, float y2, float round, int color, int color2, int color3, int color4, boolean bloom
   ) {
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      drawFullGradientRectPro(x + round / 2.0F, y + round / 2.0F, x2 - round / 2.0F, y2 - round / 2.0F, color4, color3, color2, color, bloom);
      drawPolygonParts((double)(x + round / 2.0F), (double)(y + round / 2.0F), round / 2.0F, 0, color, color, bloom);
      drawPolygonParts((double)(x + round / 2.0F), (double)(y2 - round / 2.0F), round / 2.0F, 5, color4, color4, bloom);
      drawPolygonParts((double)(x2 - round / 2.0F), (double)(y + round / 2.0F), round / 2.0F, 7, color2, color2, bloom);
      drawPolygonParts((double)(x2 - round / 2.0F), (double)(y2 - round / 2.0F), round / 2.0F, 6, color3, color3, bloom);
      drawFullGradientRectPro(x, y + round / 2.0F, x + round / 2.0F, y2 - round / 2.0F, color4, color4, color, color, bloom);
      drawFullGradientRectPro(x + round / 2.0F, y, x2 - round / 2.0F, y + round / 2.0F, color, color2, color2, color, bloom);
      drawFullGradientRectPro(x2 - round / 2.0F, y + round / 2.0F, x2, y2 - round / 2.0F, color3, color3, color2, color2, bloom);
      drawFullGradientRectPro(x + round / 2.0F, y2 - round / 2.0F, x2 - round / 2.0F, y2, color4, color3, color3, color4, bloom);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }
   }

   public static void drawTenasityGlichOBF(float left, float top, float right, float bottom, float smoth, int color, int twocolor, int threecolor, int four) {
      GL11.glEnable(3042);
      GL11.glEnable(2848);
      GL11.glLineWidth(3.75F);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      drawPolygonPartss((double)(left * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 5, color, color);
      drawPolygonPartss((double)(left * 2.0F), (double)(top * 2.0F), (int)smoth, 0, four, four);
      drawPolygonPartss((double)(right * 2.0F), (double)(top * 2.0F), (int)smoth, 7, color, color);
      drawPolygonPartss((double)(right * 2.0F), (double)(bottom * 2.0F - smoth), (int)smoth, 6, four, four);
      drawGlichRect3OBF((double)(left * 2.0F - smoth), (double)(top * 2.0F), (double)(left * 2.0F - smoth), (double)(bottom * 2.0F - smoth), color, four);
      drawGlichRect2OBF((double)(left * 2.0F), (double)(top * 2.0F - smoth), (double)(right * 2.0F), (double)(top * 2.0F - smoth), four, color);
      drawGlichRect3OBF((double)(right * 2.0F + smoth), (double)(top * 2.0F), (double)(right * 2.0F + smoth), (double)(bottom * 2.0F - smoth), four, color);
      drawGlichRect2OBF((double)(left * 2.0F), (double)(bottom * 2.0F), (double)(right * 2.0F), (double)(bottom * 2.0F), color, four);
      GL11.glDisable(3042);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
   }

   public static void drawGlichRect3OBF(double x, double y, double width, double height, int color, int twocolor) {
      if (x < width) {
         float i = (float)x;
         x = width;
         width = (double)i;
      }

      if (y < height) {
         float j = (float)y;
         y = height;
         height = (double)j;
      }

      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha2 = (float)(twocolor >> 24 & 0xFF) / 255.0F;
      float red2 = (float)(twocolor >> 16 & 0xFF) / 255.0F;
      float green2 = (float)(twocolor >> 8 & 0xFF) / 255.0F;
      float blue2 = (float)(twocolor & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      GlStateManager.disableAlpha();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, height, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(width, height, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(width, y, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(x, y, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      tessellator.draw();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, height, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(width, height, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(width, y, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.shadeModel(7424);
      GlStateManager.enableAlpha();
   }

   public static void drawGlichRect2OBF(double x, double y, double width, double height, int color, int twocolor) {
      if (x < width) {
         float i = (float)x;
         x = width;
         width = (double)i;
      }

      if (y < height) {
         float j = (float)y;
         y = height;
         height = (double)j;
      }

      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha2 = (float)(twocolor >> 24 & 0xFF) / 255.0F;
      float red2 = (float)(twocolor >> 16 & 0xFF) / 255.0F;
      float green2 = (float)(twocolor >> 8 & 0xFF) / 255.0F;
      float blue2 = (float)(twocolor & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      GlStateManager.disableAlpha();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, height, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(width, height, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(width, y, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(x, y, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      tessellator.draw();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, height, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(width, height, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(width, y, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(x, y, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.shadeModel(7424);
      GlStateManager.enableAlpha();
   }

   public static void drawPolygonPartss(double x, double y, int radius, int part, int color, int endcolor) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha1 = (float)(endcolor >> 24 & 0xFF) / 255.0F;
      float red1 = (float)(endcolor >> 16 & 0xFF) / 255.0F;
      float green1 = (float)(endcolor >> 8 & 0xFF) / 255.0F;
      float blue1 = (float)(endcolor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      double TWICE_PI = Math.PI * 2;

      for (int i = part * 90; i <= part * 90 + 90; i += 6) {
         double angle = (Math.PI * 2) * (double)i / 360.0 + Math.toRadians(180.0);
         bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(red1, green1, blue1, alpha1).endVertex();
      }

      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void customScaledObject2D(float oXpos, float oYpos, float oWidth, float oHeight, float oScale) {
      GL11.glTranslated((double)(oWidth / 2.0F), (double)(oHeight / 2.0F), 1.0);
      GL11.glTranslated((double)(-oXpos * oScale + oXpos + oWidth / 2.0F * -oScale), (double)(-oYpos * oScale + oYpos + oHeight / 2.0F * -oScale), 1.0);
      GL11.glScaled((double)oScale, (double)oScale, 0.0);
   }

   public static void customScaledObject2DCoords(float oXpos, float oYpos, float oXpos2, float oYpos2, float oScale) {
      customScaledObject2D(oXpos, oYpos, oXpos2 - oXpos, oYpos2 - oYpos, oScale);
   }

   public static void customScaledObject2DPro(float oXpos, float oYpos, float oWidth, float oHeight, float oScaleX, float oScaleY) {
      GL11.glTranslated((double)(oWidth / 2.0F), (double)(oHeight / 2.0F), 1.0);
      GL11.glTranslated((double)(-oXpos * oScaleX + oXpos + oWidth / 2.0F * -oScaleX), (double)(-oYpos * oScaleY + oYpos + oHeight / 2.0F * -oScaleY), 1.0);
      GL11.glScaled((double)oScaleX, (double)oScaleY, 0.0);
   }

   public static void customRotatedObject2D(float oXpos, float oYpos, float oWidth, float oHeight, double rotate) {
      GL11.glTranslated((double)(oXpos + oWidth / 2.0F), (double)(oYpos + oHeight / 2.0F), 0.0);
      GL11.glRotated(rotate, 0.0, 0.0, 1.0);
      GL11.glTranslated((double)(-oXpos - oWidth / 2.0F), (double)(-oYpos - oHeight / 2.0F), 0.0);
   }

   public static void setupFBO(Framebuffer fbo) {
      EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
      int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
      EXTFramebufferObject.glBindRenderbufferEXT(36161, stencil_depth_buffer_ID);
      EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
      EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencil_depth_buffer_ID);
      EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencil_depth_buffer_ID);
   }

   public static void drawRect(double x, double y, double d, double e, int color) {
      glRenderStart();
      glColor(color);
      GL11.glBegin(7);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(d, y);
      GL11.glVertex2d(d, e);
      GL11.glVertex2d(x, e);
      GL11.glEnd();
      glRenderStop();
   }

   public static void drawRectSs(double left, double top, double right, double bottom, int color) {
      right += left;
      bottom += top;
      float f3 = (float)(color >> 24 & 0xFF) / 255.0F;
      float f = (float)(color >> 16 & 0xFF) / 255.0F;
      float f1 = (float)(color >> 8 & 0xFF) / 255.0F;
      float f2 = (float)(color & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.color(f, f1, f2, f3);
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
      bufferbuilder.pos(left, bottom, 0.0).endVertex();
      bufferbuilder.pos(right, bottom, 0.0).endVertex();
      bufferbuilder.pos(right, top, 0.0).endVertex();
      bufferbuilder.pos(left, top, 0.0).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }

   public static void drawFpsRect(float xPos, float yPos, float x2Pos, float y2Pos, int color) {
      float x = xPos < x2Pos ? xPos : x2Pos;
      float y = yPos < y2Pos ? xPos : x2Pos;
      float y2 = yPos > y2Pos ? yPos : y2Pos;
      float x2 = xPos > x2Pos ? xPos : x2Pos;
      float dx = Math.abs(x2 - x) > Math.abs(x - x2) ? Math.abs(x - x2) : Math.abs(x2 - x);
      float dy = Math.abs(y2 - y) > Math.abs(y - y2) ? Math.abs(y - y2) : Math.abs(y2 - y);
      float lw = dx > dy ? dy : dx;
      GL11.glEnable(3042);
      GL11.glDisable(2884);
      GL11.glDisable(3553);
      GL11.glDisable(3008);
      GL11.glColor4d(
         (double)(color >> 16 & 0xFF) / 255.0, (double)(color >> 8 & 0xFF) / 255.0, (double)(color & 0xFF) / 255.0, (double)(color >> 24 & 0xFF) / 255.0
      );
      GL11.glLineWidth(lw * (float)mc.gameSettings.guiScale);
      GL11.glBegin(1);
      GL11.glVertex2d(lw == dx ? (double)(x + dx / 2.0F) : (double)x, lw == dy ? (double)(y + dy / 2.0F) : (double)y);
      GL11.glVertex2d(lw == dx ? (double)(x + dx / 2.0F) : (double)x2, lw == dy ? (double)(y + dy / 2.0F) : (double)y2);
      GL11.glEnd();
      GL11.glLineWidth(1.0F);
      GL11.glEnable(3008);
      GL11.glEnable(3553);
      GL11.glEnable(2884);
      GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
   }

   public static void drawLineW(float xPos, float yPos, float x2Pos, float h, int color, int color2) {
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      GL11.glLineWidth(h * (float)mc.gameSettings.guiScale + 0.25F);
      GL11.glBegin(1);
      glColor(color);
      GL11.glVertex2d((double)xPos, (double)(yPos + h / 2.0F));
      glColor(color2);
      GL11.glVertex2d((double)x2Pos, (double)(yPos + h / 2.0F));
      GL11.glEnd();
      GL11.glShadeModel(7424);
      GL11.glLineWidth(1.0F);
      GL11.glEnable(3553);
      GL11.glEnable(3008);
      GlStateManager.resetColor();
   }

   public static void drawLineH(float xPos, float yPos, float y2Pos, float w, int color, int color2) {
      GL11.glEnable(3042);
      GL11.glDisable(2884);
      GL11.glDisable(3553);
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      GL11.glLineWidth(w * (float)mc.gameSettings.guiScale);
      GL11.glBegin(1);
      glColor(color);
      GL11.glVertex2d((double)(xPos + w / 2.0F), (double)yPos);
      glColor(color2);
      GL11.glVertex2d((double)(xPos + w / 2.0F), (double)y2Pos);
      GL11.glEnd();
      GL11.glShadeModel(7424);
      GL11.glLineWidth(1.0F);
      GL11.glEnable(3008);
      GL11.glEnable(3553);
      GL11.glEnable(2884);
      GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
   }

   public static void drawLightContureRect(double x, double y, double x2, double y2, int color) {
      drawAlphedRect(x - 0.5, y - 0.5, x2 + 0.5, y, color);
      drawAlphedRect(x - 0.5, y2, x2 + 0.5, y2 + 0.5, color);
      drawAlphedRect(x - 0.5, y, x, y2, color);
      drawAlphedRect(x2, y, x2 + 0.5, y2, color);
   }

   public static void drawLightContureRectSmooth(double x, double y, double x2, double y2, int color) {
      drawAlphedRect(x, y - 0.5, x2, y, color);
      drawAlphedRect(x, y2, x2, y2 + 0.5, color);
      drawAlphedRect(x - 0.5, y, x, y2, color);
      drawAlphedRect(x2, y, x2 + 0.5, y2, color);
   }

   public static void drawLightContureRectSideways(double x, double y, double x2, double y2, int color, int color2) {
      drawAlphedSideways(x - 0.5, y - 0.5, x2 + 0.5, y, color, color2);
      drawAlphedSideways(x - 0.5, y2, x2 + 0.5, y2 + 0.5, color, color2);
      drawAlphedRect(x - 0.5, y, x, y2, color);
      drawAlphedRect(x2, y, x2 + 0.5, y2, color2);
   }

   public static void drawLightContureRectSidewaysSmooth(double x, double y, double x2, double y2, int color, int color2) {
      drawAlphedSideways(x, y - 0.5, x2, y, color, color2);
      drawAlphedSideways(x, y2, x2, y2 + 0.5, color, color2);
      drawAlphedRect(x - 0.5, y, x, y2, color);
      drawAlphedRect(x2, y, x2 + 0.5, y2, color2);
   }

   public static void drawLightContureRectFullGradient(float x, float y, float x2, float y2, int c1, int c2, boolean bloom) {
      drawFullGradientRectPro(x - 0.5F, y - 0.5F, x2 + 0.5F, y, c1, c2, c2, c1, bloom);
      drawFullGradientRectPro(x - 0.5F, y2, x2 + 0.5F, y2 + 0.5F, c2, c1, c1, c2, bloom);
      drawFullGradientRectPro(x - 0.5F, y, x, y2, c2, c2, c1, c1, bloom);
      drawFullGradientRectPro(x2, y, x2 + 0.5F, y2, c1, c1, c2, c2, bloom);
   }

   public static void drawLightContureRectFullGradient(float x, float y, float x2, float y2, int c1, int c2, int c3, int c4, boolean bloom) {
      drawFullGradientRectPro(x - 0.5F, y - 0.5F, x2 + 0.5F, y, c1, c2, c2, c1, bloom);
      drawFullGradientRectPro(x - 0.5F, y2, x2 + 0.5F, y2 + 0.5F, c4, c3, c3, c4, bloom);
      drawFullGradientRectPro(x - 0.5F, y, x, y2, c4, c4, c1, c1, bloom);
      drawFullGradientRectPro(x2, y, x2 + 0.5F, y2, c3, c3, c2, c2, bloom);
   }

   public static void drawLightContureRectSmoothFullGradient(float x, float y, float x2, float y2, int c1, int c2, int c3, int c4, boolean bloom) {
      drawFullGradientRectPro(x, y - 0.5F, x2, y, c1, c2, c2, c1, bloom);
      drawFullGradientRectPro(x, y2, x2, y2 + 0.5F, c4, c3, c3, c4, bloom);
      drawFullGradientRectPro(x - 0.5F, y, x, y2, c4, c4, c1, c1, bloom);
      drawFullGradientRectPro(x2, y, x2 + 0.5F, y2, c3, c3, c2, c2, bloom);
   }

   public static void startVertexRect() {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
   }

   public static void drawVertexRect(double x, double y, double x2, double y2, int c) {
      double xPos1 = x > x2 ? x2 : x;
      double yPos1 = y > y2 ? y2 : y;
      double xPos2 = x2 > x ? x2 : x;
      double yPos2 = y2 > y ? y2 : y;
      float a = (float)(c >> 24 & 0xFF) / 255.0F;
      float r = (float)(c >> 16 & 0xFF) / 255.0F;
      float g = (float)(c >> 8 & 0xFF) / 255.0F;
      float b = (float)(c & 0xFF) / 255.0F;
      buffer.pos(xPos2, yPos2, 300.0).color(r, g, b, a).endVertex();
      buffer.pos(xPos1, yPos2, 300.0).color(r, g, b, a).endVertex();
      buffer.pos(xPos1, yPos1, 300.0).color(r, g, b, a).endVertex();
      buffer.pos(xPos2, yPos1, 300.0).color(r, g, b, a).endVertex();
   }

   public static void stopVertexRect() {
      Tessellator.getInstance().draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.resetColor();
   }

   public static void drawAlphedRect(double x, double y, double d, double e, int color) {
      glRenderStart();
      GL11.glDisable(3008);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y).color(color).endVertex();
      buffer.pos(d, y).color(color).endVertex();
      buffer.pos(d, e).color(color).endVertex();
      buffer.pos(x, e).color(color).endVertex();
      tessellator.draw();
      GL11.glEnable(3008);
      glRenderStop();
   }

   public static void drawAlphedRectWithBloom(double x, double y, double x2, double y2, int color, boolean bloom) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.shadeModel(7424);
      GL11.glDisable(3008);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      } else {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y2).color(color).endVertex();
      buffer.pos(x2, y2).color(color).endVertex();
      buffer.pos(x2, y).color(color).endVertex();
      buffer.pos(x, y).color(color).endVertex();
      tessellator.draw();
      GL11.glEnable(3008);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GlStateManager.shadeModel(7424);
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.resetColor();
   }

   public static void drawAlphedGradientRectWithBloom(double x, double y, double x2, double y2, int color, int color2, boolean bloom) {
      glRenderStart();
      GL11.glDisable(3008);
      GL11.glShadeModel(7425);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y2).color(color2).endVertex();
      buffer.pos(x2, y2).color(color2).endVertex();
      buffer.pos(x2, y).color(color).endVertex();
      buffer.pos(x, y).color(color).endVertex();
      tessellator.draw();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GL11.glShadeModel(7424);
      GL11.glEnable(3008);
      glRenderStop();
   }

   public static void drawAlphedGradient(double x, double y, double x2, double y2, int col1, int col2) {
      float f = (float)(col1 >> 24 & 0xFF) / 255.0F;
      float f1 = (float)(col1 >> 16 & 0xFF) / 255.0F;
      float f2 = (float)(col1 >> 8 & 0xFF) / 255.0F;
      float f3 = (float)(col1 & 0xFF) / 255.0F;
      float f4 = (float)(col2 >> 24 & 0xFF) / 255.0F;
      float f5 = (float)(col2 >> 16 & 0xFF) / 255.0F;
      float f6 = (float)(col2 >> 8 & 0xFF) / 255.0F;
      float f7 = (float)(col2 & 0xFF) / 255.0F;
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glShadeModel(7425);
      GL11.glPushMatrix();
      GL11.glDisable(3008);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x2, y).color(col1).endVertex();
      buffer.pos(x, y).color(col1).endVertex();
      buffer.pos(x, y2).color(col2).endVertex();
      buffer.pos(x2, y2).color(col2).endVertex();
      tessellator.draw();
      GL11.glEnable(3008);
      GL11.glPopMatrix();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glDisable(2848);
      GL11.glShadeModel(7424);
   }

   public static void drawShadowRect(double startX, double startY, double endX, double endY, int radius) {
      drawGradientRect(startX, startY - (double)radius, endX, startY, false, true, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
      drawGradientRect(startX, endY, endX, endY + (double)radius, false, false, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
      drawSector2(endX, endY, 0, 90, radius);
      drawSector2(endX, startY, 90, 180, radius);
      drawSector2(startX, startY, 180, 270, radius);
      drawSector2(startX, endY, 270, 360, radius);
      drawGradientRect(startX - (double)radius, startY, startX, endY, true, true, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
      drawGradientRect(endX, startY, endX + (double)radius, endY, true, false, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
   }

   public static void drawShadowRectColored(double startX, double startY, double endX, double endY, int radius, int color, int alpha) {
      drawGradientRect2(startX, startY - (double)radius, endX, startY, false, true, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      drawGradientRect2(startX, endY, endX, endY + (double)radius, false, false, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      drawSector3(endX, endY, 0, 90, (float)radius, color, alpha);
      drawSector3(endX, startY, 90, 180, (float)radius, color, alpha);
      drawSector3(startX, startY, 180, 270, (float)radius, color, alpha);
      drawSector3(startX, endY, 270, 360, (float)radius, color, alpha);
      drawGradientRect2(startX - (double)radius, startY, startX, endY, true, true, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      drawGradientRect2(endX, startY, endX + (double)radius, endY, true, false, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
   }

   public static void drawShadowRectColored(double startX, double startY, double endX, double endY, float radius, int color, int alpha) {
      drawGradientRect2(startX, startY - (double)radius, endX, startY, false, true, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      drawGradientRect2(startX, endY, endX, endY + (double)radius, false, false, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      drawSector3(endX, endY, 0, 90, radius, color, alpha);
      drawSector3(endX, startY, 90, 180, radius, color, alpha);
      drawSector3(startX, startY, 180, 270, radius, color, alpha);
      drawSector3(startX, endY, 270, 360, radius, color, alpha);
      drawGradientRect2(startX - (double)radius, startY, startX, endY, true, true, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      drawGradientRect2(endX, startY, endX + (double)radius, endY, true, false, ColorUtils.swapAlpha(color, (float)alpha), 0, alpha);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
   }

   public static void drawFullGradientShadowRectColored(
      double startX, double startY, double endX, double endY, float radius, int color1, int color2, int color3, int color4, int alpha
   ) {
      drawFullGradientRectPro(
         (float)startX,
         (float)startY - radius,
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)startY,
         color1,
         ColorUtils.getOverallColorFrom(color1, color2),
         0,
         0,
         true
      );
      drawFullGradientRectPro(
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)startY - radius,
         (float)endX,
         (float)startY,
         ColorUtils.getOverallColorFrom(color1, color2),
         color2,
         0,
         0,
         true
      );
      drawFullGradientRectPro(
         (float)startX,
         (float)endY,
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)endY + radius,
         0,
         0,
         ColorUtils.getOverallColorFrom(color3, color4),
         color4,
         true
      );
      drawFullGradientRectPro(
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)endY,
         (float)endX,
         (float)endY + radius,
         0,
         0,
         color3,
         ColorUtils.getOverallColorFrom(color3, color4),
         true
      );
      drawSector3(endX, endY, 0, 90, radius, color3, alpha);
      drawSector3(endX, startY, 90, 180, radius, color2, alpha);
      drawSector3(startX, startY, 180, 270, radius, color1, alpha);
      drawSector3(startX, endY, 270, 360, radius, color4, alpha);
      drawFullGradientRectPro(
         (float)startX - radius,
         (float)startY,
         (float)startX,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         0,
         ColorUtils.getOverallColorFrom(color4, color1),
         color1,
         0,
         true
      );
      drawFullGradientRectPro(
         (float)startX - radius,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         (float)startX,
         (float)endY,
         0,
         color4,
         ColorUtils.getOverallColorFrom(color4, color1),
         0,
         true
      );
      drawFullGradientRectPro(
         (float)endX,
         (float)startY,
         (float)endX + radius,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         ColorUtils.getOverallColorFrom(color3, color2),
         0,
         0,
         color2,
         true
      );
      drawFullGradientRectPro(
         (float)endX,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         (float)endX + radius,
         (float)endY,
         color3,
         0,
         0,
         ColorUtils.getOverallColorFrom(color3, color2),
         true
      );
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
   }

   public static void drawFullGradientShadowRectColored(
      double startX, double startY, double endX, double endY, float radius, int color1, int color2, int color3, int color4, int alpha, boolean bloom
   ) {
      drawFullGradientRectPro(
         (float)startX,
         (float)startY - radius,
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)startY,
         color1,
         ColorUtils.getOverallColorFrom(color1, color2),
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color1, color2), 0.0F),
         ColorUtils.swapAlpha(color1, 0.0F),
         bloom
      );
      drawFullGradientRectPro(
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)startY - radius,
         (float)endX,
         (float)startY,
         ColorUtils.getOverallColorFrom(color1, color2),
         color2,
         ColorUtils.swapAlpha(color2, 0.0F),
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color1, color2), 0.0F),
         bloom
      );
      drawFullGradientRectPro(
         (float)startX,
         (float)endY,
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)endY + radius,
         ColorUtils.swapAlpha(color4, 0.0F),
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color4), 0.0F),
         ColorUtils.getOverallColorFrom(color3, color4),
         color4,
         bloom
      );
      drawFullGradientRectPro(
         (float)startX + ((float)endX - (float)startX) / 2.0F,
         (float)endY,
         (float)endX,
         (float)endY + radius,
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color4), 0.0F),
         ColorUtils.swapAlpha(color3, 0.0F),
         color3,
         ColorUtils.getOverallColorFrom(color3, color4),
         bloom
      );
      drawSector4(endX, endY, 0, 90, radius, color3, alpha, bloom);
      drawSector4(endX, startY, 90, 180, radius, color2, alpha, bloom);
      drawSector4(startX, startY, 180, 270, radius, color1, alpha, bloom);
      drawSector4(startX, endY, 270, 360, radius, color4, alpha, bloom);
      drawFullGradientRectPro(
         (float)startX - radius,
         (float)startY,
         (float)startX,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color4, color1), 0.0F),
         ColorUtils.getOverallColorFrom(color4, color1),
         color1,
         ColorUtils.swapAlpha(color1, 0.0F),
         bloom
      );
      drawFullGradientRectPro(
         (float)startX - radius,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         (float)startX,
         (float)endY,
         ColorUtils.swapAlpha(color4, 0.0F),
         color4,
         ColorUtils.getOverallColorFrom(color4, color1),
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color4, color1), 0.0F),
         bloom
      );
      drawFullGradientRectPro(
         (float)endX,
         (float)startY,
         (float)endX + radius,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         ColorUtils.getOverallColorFrom(color3, color2),
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color2), 0.0F),
         ColorUtils.swapAlpha(color2, 0.0F),
         color2,
         bloom
      );
      drawFullGradientRectPro(
         (float)endX,
         (float)startY + ((float)endY - (float)startY) / 2.0F,
         (float)endX + radius,
         (float)endY,
         color3,
         ColorUtils.swapAlpha(color3, 0.0F),
         ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color2), 0.0F),
         ColorUtils.getOverallColorFrom(color3, color2),
         bloom
      );
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
   }

   public static void drawBloomedFullShadowFullGradientRect(
      float xpos, float ypos, float x2pos, float y2pos, float radius, int color1, int color2, int color3, int color4, int alpha, boolean bloom
   ) {
      float w = x2pos - xpos;
      float h = y2pos - ypos;
      int colorid1 = ColorUtils.swapAlpha(color1, (float)alpha);
      int colorid2 = ColorUtils.swapAlpha(color2, (float)alpha);
      int colorid3 = ColorUtils.swapAlpha(color3, (float)alpha);
      int colorid4 = ColorUtils.swapAlpha(color4, (float)alpha);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      drawFullGradientShadowRectColored(
         (double)xpos, (double)ypos, (double)(xpos + w), (double)(ypos + h), radius, colorid1, colorid2, colorid3, colorid4, alpha, bloom
      );
      drawFullGradientRectPro(
         xpos,
         ypos,
         xpos + w / 2.0F,
         ypos + h / 2.0F,
         ColorUtils.getOverallColorFrom(colorid1, colorid4),
         ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
         ColorUtils.getOverallColorFrom(colorid1, colorid2),
         colorid1,
         bloom
      );
      drawFullGradientRectPro(
         xpos,
         ypos + h / 2.0F,
         xpos + w / 2.0F,
         ypos + h,
         colorid4,
         ColorUtils.getOverallColorFrom(colorid3, colorid4),
         ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
         ColorUtils.getOverallColorFrom(colorid1, colorid4),
         bloom
      );
      drawFullGradientRectPro(
         xpos + w / 2.0F,
         ypos + h / 2.0F,
         xpos + w,
         ypos + h,
         ColorUtils.getOverallColorFrom(colorid3, colorid4),
         colorid3,
         ColorUtils.getOverallColorFrom(colorid3, colorid2),
         ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
         bloom
      );
      drawFullGradientRectPro(
         xpos + w / 2.0F,
         ypos,
         xpos + w,
         ypos + h / 2.0F,
         ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
         ColorUtils.getOverallColorFrom(colorid3, colorid2),
         colorid2,
         ColorUtils.getOverallColorFrom(colorid2, colorid1),
         bloom
      );
   }

   public static void drawBloomedFullShadowFullGradientRectBool(
      float xpos,
      float ypos,
      float x2pos,
      float y2pos,
      float radius,
      int color1,
      int color2,
      int color3,
      int color4,
      int alpha,
      boolean bloom,
      boolean rect,
      boolean shadow
   ) {
      float w = x2pos - xpos;
      float h = y2pos - ypos;
      int colorid1 = ColorUtils.swapAlpha(color1, (float)alpha);
      int colorid2 = ColorUtils.swapAlpha(color2, (float)alpha);
      int colorid3 = ColorUtils.swapAlpha(color3, (float)alpha);
      int colorid4 = ColorUtils.swapAlpha(color4, (float)alpha);
      if (shadow) {
         drawFullGradientShadowRectColored(
            (double)xpos, (double)ypos, (double)(xpos + w), (double)(ypos + h), radius, colorid1, colorid2, colorid3, colorid4, alpha, bloom
         );
      }

      if (rect) {
         drawFullGradientRectPro(
            xpos,
            ypos,
            xpos + w / 2.0F,
            ypos + h / 2.0F,
            ColorUtils.getOverallColorFrom(colorid1, colorid4),
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            ColorUtils.getOverallColorFrom(colorid1, colorid2),
            colorid1,
            bloom
         );
         drawFullGradientRectPro(
            xpos,
            ypos + h / 2.0F,
            xpos + w / 2.0F,
            ypos + h,
            colorid4,
            ColorUtils.getOverallColorFrom(colorid3, colorid4),
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            ColorUtils.getOverallColorFrom(colorid1, colorid4),
            bloom
         );
         drawFullGradientRectPro(
            xpos + w / 2.0F,
            ypos + h / 2.0F,
            xpos + w,
            ypos + h,
            ColorUtils.getOverallColorFrom(colorid3, colorid4),
            colorid3,
            ColorUtils.getOverallColorFrom(colorid3, colorid2),
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            bloom
         );
         drawFullGradientRectPro(
            xpos + w / 2.0F,
            ypos,
            xpos + w,
            ypos + h / 2.0F,
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            ColorUtils.getOverallColorFrom(colorid3, colorid2),
            colorid2,
            ColorUtils.getOverallColorFrom(colorid2, colorid1),
            bloom
         );
      }
   }

   public static void drawBloomedFullShadowFullGradientRectBool(
      float xpos,
      float ypos,
      float x2pos,
      float y2pos,
      float radius,
      int color1,
      int color2,
      int color3,
      int color4,
      int alphaRect,
      int alphaGlow,
      boolean bloom,
      boolean rect,
      boolean shadow
   ) {
      float w = x2pos - xpos;
      float h = y2pos - ypos;
      int colorid1 = ColorUtils.swapAlpha(color1, (float)alphaRect);
      int colorid2 = ColorUtils.swapAlpha(color2, (float)alphaRect);
      int colorid3 = ColorUtils.swapAlpha(color3, (float)alphaRect);
      int colorid4 = ColorUtils.swapAlpha(color4, (float)alphaRect);
      int colorid5 = ColorUtils.swapAlpha(color1, (float)alphaGlow);
      int colorid6 = ColorUtils.swapAlpha(color2, (float)alphaGlow);
      int colorid7 = ColorUtils.swapAlpha(color3, (float)alphaGlow);
      int colorid8 = ColorUtils.swapAlpha(color4, (float)alphaGlow);
      if (shadow) {
         drawFullGradientShadowRectColored(
            (double)xpos, (double)ypos, (double)(xpos + w), (double)(ypos + h), radius, colorid5, colorid6, colorid7, colorid8, alphaGlow, bloom
         );
      }

      if (rect) {
         drawFullGradientRectPro(
            xpos,
            ypos,
            xpos + w / 2.0F,
            ypos + h / 2.0F,
            ColorUtils.getOverallColorFrom(colorid1, colorid4),
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            ColorUtils.getOverallColorFrom(colorid1, colorid2),
            colorid1,
            bloom
         );
         drawFullGradientRectPro(
            xpos,
            ypos + h / 2.0F,
            xpos + w / 2.0F,
            ypos + h,
            colorid4,
            ColorUtils.getOverallColorFrom(colorid3, colorid4),
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            ColorUtils.getOverallColorFrom(colorid1, colorid4),
            bloom
         );
         drawFullGradientRectPro(
            xpos + w / 2.0F,
            ypos + h / 2.0F,
            xpos + w,
            ypos + h,
            ColorUtils.getOverallColorFrom(colorid3, colorid4),
            colorid3,
            ColorUtils.getOverallColorFrom(colorid3, colorid2),
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            bloom
         );
         drawFullGradientRectPro(
            xpos + w / 2.0F,
            ypos,
            xpos + w,
            ypos + h / 2.0F,
            ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))),
            ColorUtils.getOverallColorFrom(colorid3, colorid2),
            colorid2,
            ColorUtils.getOverallColorFrom(colorid2, colorid1),
            bloom
         );
      }
   }

   public static void drawRoundedFullGradientOutsideShadow(
      float x, float y, float x2, float y2, float round, float shadowSize, int color, int color2, int color3, int color4, boolean bloom
   ) {
      x += round;
      x2 -= round;
      y += round;
      y2 -= round;
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      drawCroneShadow((double)x, (double)y, -180, -90, round, shadowSize, color, bloom ? 0 : ColorUtils.swapAlpha(color, 0.0F), bloom);
      drawFullGradientRectPro(
         x,
         y - round - shadowSize,
         x2,
         y - round,
         color,
         color2,
         bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0F),
         bloom ? 0 : ColorUtils.swapAlpha(color, 0.0F),
         bloom
      );
      drawCroneShadow((double)x2, (double)y, 90, 180, round, shadowSize, color2, bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0F), bloom);
      drawFullGradientRectPro(
         x2 + round,
         y,
         x2 + round + shadowSize,
         y2,
         color3,
         bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0F),
         bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0F),
         color2,
         bloom
      );
      drawCroneShadow((double)x2, (double)y2, 0, 90, round, shadowSize, color3, bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0F), bloom);
      drawFullGradientRectPro(
         x,
         y2 + round,
         x2,
         y2 + round + shadowSize,
         bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0F),
         bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0F),
         color3,
         color4,
         bloom
      );
      drawCroneShadow((double)x, (double)y2, -90, 0, round, shadowSize, color4, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0F), bloom);
      drawFullGradientRectPro(
         x - round - shadowSize,
         y,
         x - round,
         y2,
         bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0F),
         color4,
         color,
         bloom ? 0 : ColorUtils.swapAlpha(color, 0.0F),
         bloom
      );
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }
   }

   public static void drawInsideFullRoundedFullGradientShadowRectWithBloomBool(
      float x, float y, float x2, float y2, float round, float shadowSize, int c1, int c2, int c3, int c4, boolean bloom
   ) {
      drawCroneShadow((double)(x + shadowSize + round), (double)(y + shadowSize + round), -180, -90, round, shadowSize, 0, c1, bloom);
      drawCroneShadow((double)(x2 - shadowSize - round), (double)(y + shadowSize + round), -270, -180, round, shadowSize, 0, c2, bloom);
      drawCroneShadow((double)(x2 - shadowSize - round), (double)(y2 - shadowSize - round), 0, 90, round, shadowSize, 0, c3, bloom);
      drawCroneShadow((double)(x + shadowSize + round), (double)(y2 - shadowSize - round), -90, 0, round, shadowSize, 0, c4, bloom);
      drawFullGradientRectPro(x + shadowSize + round, y, x2 - shadowSize - round, y + shadowSize, 0, 0, c2, c1, bloom);
      drawFullGradientRectPro(x + shadowSize + round, y2 - shadowSize, x2 - shadowSize - round, y2, c4, c3, 0, 0, bloom);
      drawFullGradientRectPro(x, y + shadowSize + round, x + shadowSize, y2 - shadowSize - round, c4, 0, 0, c1, bloom);
      drawFullGradientRectPro(x2 - shadowSize, y + shadowSize + round, x2, y2 - shadowSize - round, 0, c3, c2, 0, bloom);
   }

   public static void drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBoolShadowsBoolChangeShadowSize(
      float x,
      float y,
      float x2,
      float y2,
      float round,
      float shadowSizeInside,
      float shadowSizeOutside,
      int c1,
      int c2,
      int c3,
      int c4,
      boolean bloom,
      boolean insideShadow,
      boolean outsideShadow
   ) {
      if (insideShadow) {
         drawInsideFullRoundedFullGradientShadowRectWithBloomBool(x, y, x2, y2, round, shadowSizeInside, c1, c2, c3, c4, bloom);
      }

      if (outsideShadow) {
         drawRoundedFullGradientOutsideShadow(x, y, x2, y2, round + round / 2.0F, shadowSizeOutside, c1, c2, c3, c4, bloom);
      }
   }

   public static void drawFullGradientFullsideShadowRectWithBloomBool(
      float x, float y, float x2, float y2, float shadowSize, int c1, int c2, int c3, int c4, boolean bloom
   ) {
      drawFullGradientRectPro(x, y, x + shadowSize, y + shadowSize, c1, 0, c1, c1, bloom);
      customRotatedObject2D(x2 - shadowSize, y, shadowSize, shadowSize, 90.0);
      drawFullGradientRectPro(x2 - shadowSize, y, x2, y + shadowSize, c2, 0, c2, c2, bloom);
      customRotatedObject2D(x2 - shadowSize, y, shadowSize, shadowSize, -90.0);
      drawFullGradientRectPro(x2 - shadowSize, y2 - shadowSize, x2, y2, c3, c3, c3, 0, bloom);
      customRotatedObject2D(x, y2 - shadowSize, shadowSize, shadowSize, 90.0);
      drawFullGradientRectPro(x, y2 - shadowSize, x + shadowSize, y2, c4, c4, c4, 0, bloom);
      customRotatedObject2D(x, y2 - shadowSize, shadowSize, shadowSize, -90.0);
      drawFullGradientRectPro(x + shadowSize, y, x2 - shadowSize, y + shadowSize, 0, 0, c2, c1, bloom);
      drawFullGradientRectPro(x, y + shadowSize, x + shadowSize, y2 - shadowSize, c4, 0, 0, c1, bloom);
      drawFullGradientRectPro(x + shadowSize, y2 - shadowSize, x2 - shadowSize, y2, c4, c3, 0, 0, bloom);
      drawFullGradientRectPro(x2 - shadowSize, y + shadowSize, x2, y2 - shadowSize, 0, c3, c2, 0, bloom);
      drawRoundedFullGradientOutsideShadow(x, y, x2, y2, 0.0F, shadowSize, c1, c2, c3, c4, bloom);
   }

   public static void drawGradientRect(double startX, double startY, double endX, double endY, boolean sideways, int startColor, int endColor) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.shadeModel(7425);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(startX, startY).color(startColor).endVertex();
      buffer.pos(startX, endY).color(sideways ? startColor : endColor).endVertex();
      buffer.pos(endX, endY).color(endColor).endVertex();
      buffer.pos(endX, startY).color(sideways ? endColor : startColor).endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawGradientRect(double startX, double startY, double endX, double endY, boolean sideways, boolean reversed, int startColor, int endColor) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      endColor = ColorUtils.swapAlpha(endColor, 0.0F);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      if (sideways) {
         if (reversed) {
            buffer.pos(endX, endY).color(startColor).endVertex();
            buffer.pos(endX, startY).color(startColor).endVertex();
            buffer.pos(startX, startY).color(endColor).endVertex();
            buffer.pos(startX, endY).color(endColor).endVertex();
         } else {
            buffer.pos(startX, startY).color(startColor).endVertex();
            buffer.pos(startX, endY).color(startColor).endVertex();
            buffer.pos(endX, endY).color(endColor).endVertex();
            buffer.pos(endX, startY).color(endColor).endVertex();
         }
      } else if (reversed) {
         buffer.pos(endX, endY).color(startColor).endVertex();
         buffer.pos(endX, startY).color(startColor).endVertex();
         buffer.pos(startX, startY).color(endColor).endVertex();
         buffer.pos(startX, endY).color(startColor).endVertex();
      } else {
         buffer.pos(startX, startY).color(startColor).endVertex();
         buffer.pos(startX, endY).color(endColor).endVertex();
         buffer.pos(endX, endY).color(endColor).endVertex();
         buffer.pos(endX, startY).color(startColor).endVertex();
      }

      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawGradientRect2(
      double startX, double startY, double endX, double endY, boolean sideways, boolean reversed, int startColor, int endColor, int alpha
   ) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GL11.glDisable(3008);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
      );
      GlStateManager.shadeModel(7425);
      startColor = ColorUtils.swapAlpha(startColor, (float)alpha);
      endColor = ColorUtils.swapAlpha(endColor, 0.0F);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      if (sideways) {
         if (reversed) {
            buffer.pos(endX, endY).color(startColor).endVertex();
            buffer.pos(endX, startY).color(startColor).endVertex();
            buffer.pos(startX, startY).color(endColor).endVertex();
            buffer.pos(startX, endY).color(endColor).endVertex();
         } else {
            buffer.pos(startX, startY).color(startColor).endVertex();
            buffer.pos(startX, endY).color(startColor).endVertex();
            buffer.pos(endX, endY).color(endColor).endVertex();
            buffer.pos(endX, startY).color(endColor).endVertex();
         }
      } else if (reversed) {
         buffer.pos(endX, endY).color(startColor).endVertex();
         buffer.pos(endX, startY).color(startColor).endVertex();
         buffer.pos(startX, startY).color(endColor).endVertex();
         buffer.pos(startX, endY).color(startColor).endVertex();
      } else {
         buffer.pos(startX, startY).color(startColor).endVertex();
         buffer.pos(startX, endY).color(endColor).endVertex();
         buffer.pos(endX, endY).color(endColor).endVertex();
         buffer.pos(endX, startY).color(startColor).endVertex();
      }

      tessellator.draw();
      GL11.glEnable(3008);
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawSector2(double x, double y, int startAngle, int endAngle, int radius) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y).color(0.0F, 0.0F, 0.0F, 0.4F).endVertex();

      for (int i = startAngle; i <= endAngle; i += 6) {
         buffer.pos(x + Math.sin((double)i * Math.PI / 180.0) * (double)radius, y + Math.cos((double)i * Math.PI / 180.0) * (double)radius)
            .color(0)
            .endVertex();
      }

      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawSector3(double x, double y, int startAngle, int endAngle, float radius, int color, int alpha) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GL11.glDisable(3008);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
      );
      GlStateManager.shadeModel(7425);
      buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y).color(ColorUtils.swapAlpha(color, (float)alpha)).endVertex();

      for (int i = startAngle; i <= endAngle; i += 6) {
         buffer.pos(x + Math.sin((double)i * Math.PI / 180.0) * (double)radius, y + Math.cos((double)i * Math.PI / 180.0) * (double)radius)
            .color(0)
            .endVertex();
      }

      tessellator.draw();
      GL11.glEnd();
      GL11.glEnable(3008);
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawSector4(double x, double y, int startAngle, int endAngle, float radius, int color, int alpha, boolean bloom) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableAlpha();
      GL11.glDepthMask(false);
      GL11.glDisable(3008);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
         );
      }

      GlStateManager.shadeModel(7425);
      buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y).color(ColorUtils.swapAlpha(color, (float)alpha)).endVertex();

      for (int i = startAngle; i <= endAngle; i += 30) {
         buffer.pos(x + Math.sin((double)i * Math.PI / 180.0) * (double)radius, y + Math.cos((double)i * Math.PI / 180.0) * (double)radius)
            .color(ColorUtils.swapAlpha(color, 0.0F))
            .endVertex();
      }

      tessellator.draw();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ZERO,
            GlStateManager.DestFactor.ONE
         );
      }

      GL11.glEnable(3008);
      GL11.glDepthMask(true);
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(7424);
   }

   public static void drawCroneShadow(double x, double y, int startAngle, int endAngle, float radius, float shadowSize, int color, int endColor, boolean bloom) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.shadeModel(7425);
      GL11.glDisable(3008);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
         );
      }

      GlStateManager.shadeModel(7425);
      buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

      for (int i = startAngle; i <= endAngle; i += 18) {
         double x1 = x + Math.sin((double)i * Math.PI / 180.0) * (double)radius;
         double y1 = y + Math.cos((double)i * Math.PI / 180.0) * (double)radius;
         double x2 = x + Math.sin((double)i * Math.PI / 180.0) * (double)(radius + shadowSize);
         double y2 = y + Math.cos((double)i * Math.PI / 180.0) * (double)(radius + shadowSize);
         buffer.pos(x1, y1).color(color).endVertex();
         buffer.pos(x2, y2).color(endColor).endVertex();
      }

      tessellator.draw();
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ZERO,
            GlStateManager.DestFactor.ONE
         );
      }

      GL11.glEnable(3008);
      GlStateManager.enableTexture2D();
      GlStateManager.shadeModel(7424);
      GlStateManager.resetColor();
   }

   public static void drawRoundedFullGradientInsideShadow(
      float x, float y, float x2, float y2, float round, int color, int color2, int color3, int color4, boolean bloom
   ) {
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      float rd = round / 2.0F;
      drawPolygonParts((double)(x + rd), (double)(y + rd), rd, 0, 0, color, bloom);
      drawPolygonParts((double)(x + rd), (double)(y2 - rd), rd, 5, 0, color4, bloom);
      drawPolygonParts((double)(x2 - rd), (double)(y + rd), rd, 7, 0, color2, bloom);
      drawPolygonParts((double)(x2 - rd), (double)(y2 - rd), rd, 6, 0, color3, bloom);
      drawFullGradientRectPro(x, y + rd, x + rd, y2 - rd, color4, 0, 0, color, bloom);
      drawFullGradientRectPro(x + rd, y, x2 - rd, y + rd, 0, 0, color2, color, bloom);
      drawFullGradientRectPro(x2 - rd, y + rd, x2, y2 - rd, 0, color3, color2, 0, bloom);
      drawFullGradientRectPro(x + rd, y2 - rd, x2 - rd, y2, color4, color3, 0, 0, bloom);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }
   }

   public static void drawRoundedShadow(float x, float y, float x2, float y2, float round, float shadowSize, int color, boolean bloom) {
      x += round;
      x2 -= round;
      y += round;
      y2 -= round;
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      drawCroneShadow((double)x, (double)y, -180, -90, round, shadowSize, color, 0, bloom);
      drawFullGradientRectPro(x, y - round - shadowSize, x2, y - round, color, color, 0, 0, bloom);
      drawCroneShadow((double)x2, (double)y, 90, 180, round, shadowSize, color, 0, bloom);
      drawFullGradientRectPro(x2 + round, y, x2 + round + shadowSize, y2, color, 0, 0, color, bloom);
      drawCroneShadow((double)x2, (double)y2, 0, 90, round, shadowSize, color, 0, bloom);
      drawFullGradientRectPro(x, y2 + round, x2, y2 + round + shadowSize, 0, 0, color, color, bloom);
      drawCroneShadow((double)x, (double)y2, -90, 0, round, shadowSize, color, 0, bloom);
      drawFullGradientRectPro(x - round - shadowSize, y, x - round, y2, 0, color, color, 0, bloom);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }
   }

   public static void drawRoundedFullGradientShadow(
      float x, float y, float x2, float y2, float round, float shadowSize, int color, int color2, int color3, int color4, boolean bloom
   ) {
      x += round;
      x2 -= round;
      y += round;
      y2 -= round;
      GL11.glDepthMask(false);
      GL11.glDisable(2929);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      drawCroneShadow((double)x, (double)y, -180, -90, round, shadowSize, color, bloom ? 0 : ColorUtils.swapAlpha(color, 0.0F), bloom);
      drawFullGradientRectPro(
         x,
         y - round - shadowSize,
         x2,
         y - round,
         color,
         color2,
         bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0F),
         bloom ? 0 : ColorUtils.swapAlpha(color, 0.0F),
         bloom
      );
      drawCroneShadow((double)x2, (double)y, 90, 180, round, shadowSize, color2, bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0F), bloom);
      drawFullGradientRectPro(
         x2 + round,
         y,
         x2 + round + shadowSize,
         y2,
         color3,
         bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0F),
         bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0F),
         color2,
         bloom
      );
      drawCroneShadow((double)x2, (double)y2, 0, 90, round, shadowSize, color3, bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0F), bloom);
      drawFullGradientRectPro(
         x,
         y2 + round,
         x2,
         y2 + round + shadowSize,
         bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0F),
         bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0F),
         color3,
         color4,
         bloom
      );
      drawCroneShadow((double)x, (double)y2, -90, 0, round, shadowSize, color4, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0F), bloom);
      drawFullGradientRectPro(
         x - round - shadowSize,
         y,
         x - round,
         y2,
         bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0F),
         color4,
         color,
         bloom ? 0 : ColorUtils.swapAlpha(color, 0.0F),
         bloom
      );
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      GL11.glEnable(2929);
   }

   public static void drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
      float x,
      float y,
      float x2,
      float y2,
      float round,
      float shadowSize,
      int color,
      int color2,
      int color3,
      int color4,
      boolean bloom,
      boolean rect,
      boolean shadow
   ) {
      GlStateManager.disableDepth();
      if (shadow) {
         drawRoundedFullGradientShadow(x, y, x2, y2, round, shadowSize, color, color2, color3, color4, bloom);
      }

      if (rect) {
         drawRoundedFullGradientRectPro(x, y, x2, y2, round * 2.0F, color, color2, color3, color4, bloom);
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F);
   }

   public static void drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(
      float x, float y, float x2, float y2, float round, int color, int color2, int color3, int color4, boolean bloom
   ) {
      GlStateManager.disableDepth();
      drawRoundedFullGradientShadow(x, y, x2, y2, round, round, color, color2, color3, color4, bloom);
      drawRoundedFullGradientInsideShadow(x, y, x2, y2, round * 2.0F, color, color2, color3, color4, bloom);
      GlStateManager.enableDepth();
   }

   public static void smoothAngleRect(float xPos, float yPos, float x2Pos, float y2Pos, int color) {
      drawRect((double)(xPos + 3.0F), (double)(yPos - 3.0F), (double)(x2Pos - 3.0F), (double)(yPos - 2.5F), color);
      drawRect((double)(xPos + 2.0F), (double)(yPos - 2.5F), (double)(x2Pos - 2.0F), (double)(yPos - 2.0F), color);
      drawRect((double)(xPos + 1.5F), (double)(yPos - 2.0F), (double)(x2Pos - 1.5F), (double)(yPos - 1.5F), color);
      drawRect((double)(xPos + 1.0F), (double)(yPos - 1.5F), (double)(x2Pos - 1.0F), (double)(yPos - 1.0F), color);
      drawRect((double)(xPos + 0.5F), (double)(yPos - 1.0F), (double)(x2Pos - 0.5F), (double)yPos, color);
      drawRect((double)xPos, (double)yPos, (double)x2Pos, (double)y2Pos, color);
      drawRect((double)(xPos + 2.0F), (double)(y2Pos + 2.5F), (double)(x2Pos - 2.0F), (double)(y2Pos + 2.0F), color);
      drawRect((double)(xPos + 1.5F), (double)(y2Pos + 2.0F), (double)(x2Pos - 1.5F), (double)(y2Pos + 1.5F), color);
      drawRect((double)(xPos + 1.0F), (double)(y2Pos + 1.5F), (double)(x2Pos - 1.0F), (double)(y2Pos + 1.0F), color);
      drawRect((double)(xPos + 0.5F), (double)(y2Pos + 1.0F), (double)(x2Pos - 0.5F), (double)y2Pos, color);
      drawRect((double)(xPos + 3.0F), (double)(y2Pos + 3.0F), (double)(x2Pos - 3.0F), (double)(y2Pos + 2.5F), color);
   }

   public static void color(int argb) {
      float alpha = (float)(argb >> 24 & 0xFF) / 255.0F;
      float red = (float)(argb >> 16 & 0xFF) / 255.0F;
      float green = (float)(argb >> 8 & 0xFF) / 255.0F;
      float blue = (float)(argb & 0xFF) / 255.0F;
      GL11.glColor4f(red, green, blue, alpha);
   }

   public static int setColor(int colorHex) {
      float alpha = (float)(colorHex >> 24 & 0xFF) / 255.0F;
      float red = (float)(colorHex >> 16 & 0xFF) / 255.0F;
      float green = (float)(colorHex >> 8 & 0xFF) / 255.0F;
      float blue = (float)(colorHex & 0xFF) / 255.0F;
      GL11.glColor4f(red, green, blue, alpha == 0.0F ? 1.0F : alpha);
      return colorHex;
   }

   public static int glColor(int color) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      GL11.glColor4f(red, green, blue, alpha);
      return color;
   }

   public static void drawPenisOnEntity(EntityPlayer player, double x, double y, double z) {
      GL11.glDisable(2896);
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(2929);
      GL11.glEnable(2848);
      GL11.glDepthMask(true);
      GL11.glLineWidth(1.0F);
      GL11.glTranslated(x, y, z);
      GL11.glRotatef(-player.rotationYaw, 0.0F, player.height, 0.0F);
      GL11.glTranslated(-x, -y, -z);
      GL11.glTranslated(x, y + (double)(player.height / 2.0F) - 0.225F, z);
      GL11.glColor4f(0.38F, 0.55F, 0.38F, 1.0F);
      GL11.glLineWidth(2.0F);
      GL11.glTranslated(0.0, 0.0, 0.075F);
      Cylinder shaft = new Cylinder();
      shaft.setDrawStyle(100011);
      shaft.draw(0.1F, 0.11F, 0.4F, 25, 20);
      GL11.glColor4f(0.38F, 0.85F, 0.38F, 1.0F);
      GL11.glLineWidth(2.0F);
      GL11.glTranslated(0.0, 0.0, -0.12500000298023223);
      GL11.glTranslated(-0.09000000074505805, 0.0, 0.0);
      Sphere right = new Sphere();
      right.setDrawStyle(100011);
      right.draw(0.14F, 10, 20);
      GL11.glTranslated(0.16000000149011612, 0.0, 0.0);
      Sphere left = new Sphere();
      left.setDrawStyle(100011);
      left.draw(0.14F, 10, 20);
      GL11.glColor4f(0.35F, 0.0F, 0.0F, 1.0F);
      GL11.glLineWidth(2.0F);
      GL11.glTranslated(-0.07000000074505806, 0.0, 0.589999952316284);
      Sphere tip = new Sphere();
      tip.setDrawStyle(100013);
      tip.draw(0.13F, 15, 20);
      GL11.glDepthMask(true);
      GL11.glDisable(2848);
      GL11.glEnable(2929);
      GL11.glDisable(3042);
      GL11.glEnable(2896);
      GL11.glEnable(3553);
   }

   public static void enableSmoothLine(float width) {
      GL11.glDisable(3008);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glEnable(2884);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glHint(3155, 4354);
      GL11.glLineWidth(width);
   }

   public static void disableSmoothLine() {
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDisable(3042);
      GL11.glEnable(3008);
      GL11.glDepthMask(true);
      GL11.glCullFace(1029);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glHint(3155, 4352);
   }

   public static void startSmooth() {
      GL11.glEnable(2848);
      GL11.glEnable(2881);
      GL11.glEnable(2832);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glHint(3154, 4354);
      GL11.glHint(3155, 4354);
      GL11.glHint(3153, 4354);
   }

   public static void endSmooth() {
      GL11.glDisable(2848);
      GL11.glDisable(2881);
      GL11.glEnable(2832);
   }

   public static void setupColor(int color, float alpha) {
      float f = (float)(color >> 16 & 0xFF) / 255.0F;
      float f1 = (float)(color >> 8 & 0xFF) / 255.0F;
      float f2 = (float)(color & 0xFF) / 255.0F;
      GL11.glColor4f(f, f1, f2, alpha / 255.0F);
   }

   public static void render2D(int mode, VertexFormat formats, float lineWidth, Runnable runnable) {
      boolean isLines = mode == 6913 || mode == 2 || mode == 3 || mode == 1;
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.disableTexture2D();
      if (isLines) {
         GL11.glEnable(2848);
         GlStateManager.glLineWidth(lineWidth);
      }

      buffer.begin(mode, formats);
      runnable.run();
      tessellator.draw();
      if (isLines) {
         GL11.glDisable(2848);
      }

      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.resetColor();
   }

   public static double interpolate(double current, double old, double scale) {
      return old + (current - old) * scale;
   }

   public static void renderItem(ItemStack itemStack, float x, float y) {
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.enableDepth();
      RenderHelper.enableGUIStandardItemLighting();
      mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (int)x, (int)y);
      mc.getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, itemStack, (int)x, (int)y);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableDepth();
   }

   public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder vertexbuffer = tessellator.getBuffer();
      vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
      tessellator.draw();
      vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
      tessellator.draw();
      vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      tessellator.draw();
   }

   public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
      drawFullGradientRectPro((float)left, (float)top, (float)right, (float)bottom, col1, col2, col2, col1, false);
   }

   public static void drawAlphedSideways(double left, double top, double right, double bottom, int col1, int col2, boolean bloom) {
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
      }

      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glEnable(2848);
      GL11.glShadeModel(7425);
      GL11.glDisable(3008);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(left, top).color(col1).endVertex();
      buffer.pos(left, bottom).color(col1).endVertex();
      buffer.pos(right, bottom).color(col2).endVertex();
      buffer.pos(right, top).color(col2).endVertex();
      tessellator.draw();
      GL11.glEnable(3008);
      GL11.glEnable(3553);
      if (bloom) {
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
      }

      resetBlender();
   }

   public static void drawAlphedSideways(double left, double top, double right, double bottom, int col1, int col2) {
      drawAlphedSideways(left < right ? left : right, top, left >= right ? left : right, bottom, col1, col2, false);
   }

   public static void drawTwoAlphedSideways(double left, double top, double right, double bottom, int col1, int col2, boolean bloom) {
      drawAlphedSideways(left, top, left + (right - left) / 2.0, bottom, col2, col1, bloom);
      drawAlphedSideways(left + (right - left) / 2.0, top, right, bottom, col1, col2, bloom);
   }

   public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
      GL11.glDisable(2929);
      GL11.glEnable(3042);
      GL11.glDepthMask(false);
      OpenGlHelper.glBlendFunc(770, 771, 1, 0);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      mc.getTextureManager().bindTexture(image);
      Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0.0F, 0.0F, (int)width, (int)height, (float)((int)width), (float)((int)height));
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
      GL11.glEnable(2929);
   }

   public static void drawImageWithAlpha(ResourceLocation image, float x, float y, float width, float height, int color, int alpha) {
      GL11.glEnable(3042);
      GL11.glDepthMask(false);
      GL11.glDisable(3008);
      mc.getTextureManager().bindTexture(image);
      setupColor(color, (float)alpha);
      GL11.glTranslated((double)x, (double)y, 0.0);
      Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, (int)width, (int)height, (float)((int)width), (float)((int)height));
      GL11.glTranslated((double)(-x), (double)(-y), 0.0);
      GlStateManager.resetColor();
      GL11.glEnable(3008);
      GL11.glDepthMask(true);
   }

   public static void drawPolygonPart(double x, double y, int radius, int part, int color, int endcolor) {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y, 0.0).color(color).endVertex();
      double TWICE_PI = Math.PI * 2;
      double r180 = Math.toRadians(180.0);

      for (int i = part * 90; i <= part * 90 + 90; i++) {
         double angle = (Math.PI * 2) * (double)i / 360.0 + r180;
         buffer.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(endcolor).endVertex();
      }

      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawPolygonPartBloom(double x, double y, int radius, int part, int color, int endcolor) {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
      );
      GlStateManager.shadeModel(7425);
      buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x, y, 0.0).color(color).endVertex();
      double TWICE_PI = Math.PI * 2;
      double r180 = Math.toRadians(180.0);

      for (int i = part * 90; i <= part * 90 + 90; i++) {
         double angle = (Math.PI * 2) * (double)i / 360.0 + r180;
         buffer.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(endcolor).endVertex();
      }

      tessellator.draw();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawVGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7425);
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos((double)right, (double)top).color(startColor).endVertex();
      buffer.pos((double)left, (double)top).color(startColor).endVertex();
      buffer.pos((double)left, (double)bottom).color(endColor).endVertex();
      buffer.pos((double)right, (double)bottom).color(endColor).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawVGradientRectBloom(float left, float top, float right, float bottom, int startColor, int endColor) {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.shadeModel(7425);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
      );
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)right, (double)top).color(startColor).endVertex();
      bufferbuilder.pos((double)left, (double)top).color(startColor).endVertex();
      bufferbuilder.pos((double)left, (double)bottom).color(endColor).endVertex();
      bufferbuilder.pos((double)right, (double)bottom).color(endColor).endVertex();
      tessellator.draw();
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.shadeModel(7424);
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void glRenderStart() {
      GL11.glPushMatrix();
      GL11.glPushAttrib(1048575);
      GL11.glEnable(3042);
      GL11.glDisable(2884);
      GL11.glDisable(3553);
   }

   public static void glRenderStop() {
      GL11.glEnable(3553);
      GL11.glEnable(2884);
      GL11.glPopAttrib();
      GL11.glPopMatrix();
   }

   public static void disableGL2D() {
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glEnable(2929);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glHint(3155, 4352);
   }

   public static void enableGL2D() {
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glHint(3155, 4354);
   }

   public static void drawItemWarnIfLowDur(ItemStack stack, float x, float y, float alphaPC, float scale) {
      drawItemWarnIfLowDur(stack, x, y, alphaPC, scale, 1);
   }

   public static void drawItemWarnIfLowDur(ItemStack stack, float x, float y, float alphaPC, float scale, int count) {
      float dmgPC;
      if (stack.isItemDamaged() && (double)(dmgPC = (float)stack.getItemDamage() / (float)stack.getMaxDamage()) >= 0.7) {
         long timeDelay = (long)(1000.0F - 650.0F * (dmgPC - 0.9F) * 10.0F);
         float timePC = (float)(System.currentTimeMillis() % timeDelay) / (float)timeDelay;
         timePC = ((double)timePC > 0.5 ? 1.0F - timePC : timePC) * 2.0F;
         if ((double)(timePC * alphaPC) < 0.02) {
            return;
         }

         int color = ColorUtils.getColor(255, 40, 0, MathUtils.clamp(510.0F * timePC * alphaPC, 0.0F, 255.0F));
         mc.getTextureManager().bindTexture(ITEM_WARN_DUR);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 32772);
         if (x != 0.0F || y != 0.0F) {
            GL11.glTranslated((double)x, (double)y, 0.0);
         }

         glColor(color);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);

         for (int i = 0; i < count; i++) {
            Gui.drawModalRectWithCustomSizedTexture(-2, -2, 0.0F, 0.0F, 20, 20, 20.0F, 20.0F);
         }

         GL11.glDepthMask(true);
         GL11.glEnable(2929);
         GlStateManager.resetColor();
         if (x != 0.0F || y != 0.0F) {
            GL11.glTranslated((double)(-x), (double)(-y), 0.0);
         }

         GL11.glBlendFunc(770, 771);
      }
   }

   public static void drawScreenShaderBackground(ScaledResolution sr, int mouseX, int mouseY) {
      if (Client.screenshader == null) {
         Client.screenshader = new animbackground("/assets/minecraft/vegaline/ui/mainmenu/shaders/backgroundshader.fsh");
      }

      if (Client.screenshader != null && Display.isVisible()) {
         GlStateManager.disableCull();
         GlStateManager.disableDepth();
         GlStateManager.enableTexture2D();
         resetBlender();
         Client.screenshader
            .useShader(mc.displayWidth, mc.displayHeight, (float)mouseX, (float)mouseY, (float)(System.currentTimeMillis() - Client.initTime) / 1000.0F);
         GL11.glBegin(7);
         GL11.glVertex2f(-1.0F, -1.0F);
         GL11.glVertex2f(-1.0F, 1.0F);
         GL11.glVertex2f(1.0F, 1.0F);
         GL11.glVertex2f(1.0F, -1.0F);
         GL11.glEnd();
         GL20.glUseProgram(0);
         GlStateManager.enableDepth();
         GlStateManager.enableCull();
         GlStateManager.enableBlend();
         if (Client.mainGuiNoise != null) {
            Client.mainGuiNoise.setPlaying(!Panic.stop && GuiMainMenu.quit.to == 0.0F && GuiMainMenu.quit2.to == 0.0F);
         }
      }
   }
}
