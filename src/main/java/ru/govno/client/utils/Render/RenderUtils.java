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
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.GaussianBlur;
import ru.govno.client.utils.Render.ShaderUtility;
import ru.govno.client.utils.Render.StencilUtil;
import ru.govno.client.utils.Render.Vec2fColored;
import ru.govno.client.utils.Render.glsandbox.animbackground;

public class RenderUtils {
    protected static Minecraft mc = Minecraft.getMinecraft();
    private static final Frustum frustrum = new Frustum();
    private static final FloatBuffer COLOR_BUFFER = GLAllocation.createDirectFloatBuffer(4);
    private static final Vec3d LIGHT0_POS = new Vec3d(0.2f, 1.0, -0.7f).normalize();
    private static final Vec3d LIGHT1_POS = new Vec3d(-0.2f, 1.0, 0.7f).normalize();
    private static final Frustum frustum = new Frustum();
    public static ShaderUtility roundedShader = new ShaderUtility("roundedRect");
    public static ShaderUtility roundedOutlineShader = new ShaderUtility("roundRectOutline");
    public static Tessellator tessellator = Tessellator.getInstance();
    public static BufferBuilder buffer = tessellator.getBuffer();
    private static final ResourceLocation ITEM_WARN_DUR = new ResourceLocation("vegaline/system/durablitywarn/itemwarn.png");

    public static void anialisON(boolean line, boolean polygon, boolean point) {
        if (line) {
            GL11.glEnable((int)2848);
            GL11.glHint((int)3154, (int)4354);
        }
        if (polygon) {
            GL11.glEnable((int)2881);
            GL11.glHint((int)3155, (int)4354);
        }
        if (point) {
            GL11.glEnable((int)2832);
            GL11.glHint((int)3153, (int)4354);
        }
    }

    public static void anialisOFF(boolean line, boolean polygon, boolean point) {
        if (line) {
            GL11.glHint((int)3154, (int)4352);
            GL11.glDisable((int)2848);
        }
        if (polygon) {
            GL11.glHint((int)3155, (int)4352);
            GL11.glDisable((int)2881);
        }
        if (point) {
            GL11.glHint((int)3153, (int)4352);
            GL11.glDisable((int)2832);
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
        float alphaPCM = 0.3f;
        int cc1 = ColorUtils.getOverallColorFrom(ColorUtils.swapAlpha(cli1, (float)RenderUtils.alpha(cli1) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0f * alphaPC / 2.55f), 0.2f);
        int cc2 = ColorUtils.getOverallColorFrom(ColorUtils.swapAlpha(cli2, (float)RenderUtils.alpha(cli2) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0f * alphaPC / 2.55f), 0.2f);
        int cc3 = ColorUtils.getOverallColorFrom(ColorUtils.swapAlpha(cli3, (float)RenderUtils.alpha(cli3) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0f * alphaPC / 2.55f), 0.75f);
        int cc4 = ColorUtils.getOverallColorFrom(ColorUtils.swapAlpha(cli4, (float)RenderUtils.alpha(cli4) * alphaPCM), ColorUtils.getColor(0, 0, 0, 95.0f * alphaPC / 2.55f), 0.75f);
        int cs1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC), 0.1f);
        int cs2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC), 0.1f);
        int cs5 = ColorUtils.getOverallColorFrom(cli3, cc2, 0.75f);
        int cs6 = ColorUtils.getOverallColorFrom(cli4, cc1, 0.75f);
        int cs7 = ColorUtils.getOverallColorFrom(cli3, cc3, 0.85f);
        int cs8 = ColorUtils.getOverallColorFrom(cli4, cc4, 0.85f);
        if (alphaPC >= 0.05f) {
            GaussianBlur.drawBlur(1.0f + alphaPC * 4.0f, () -> RenderUtils.drawRect(x, y + extend, x2, y2, -1));
        }
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x + 0.5f, y + 0.5f + extend, x2 - 0.5f, y2 - 0.5f, 0.0f, 0.0f, cc1, cc2, cc3, cc4, false, true, false);
        RenderUtils.drawLightContureRectFullGradient(x + 0.5f, y + extend + 0.5f, x2 - 0.5f, y2 - 0.5f, cs6, cs5, cs7, cs8, false);
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y + extend, 0.0f, 0.0f, cs1, cs2, cs5, cs6, false, true, false);
        if (manyGlows) {
            cc1 = ColorUtils.swapAlpha(cc1, (float)ColorUtils.getAlphaFromColor(cc1) * 0.65f);
            cc2 = ColorUtils.swapAlpha(cc2, (float)ColorUtils.getAlphaFromColor(cc2) * 0.65f);
            cc3 = ColorUtils.swapAlpha(cc3, (float)ColorUtils.getAlphaFromColor(cc3) * 0.65f);
            cc4 = ColorUtils.swapAlpha(cc4, (float)ColorUtils.getAlphaFromColor(cc4) * 0.65f);
        }
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 0.0f, 6.0f, cc1, cc2, cc3, cc4, true, false, true);
        if (manyGlows) {
            cc1 = ColorUtils.swapAlpha(cc1, (float)ColorUtils.getAlphaFromColor(cc1) * 0.8f);
            cc2 = ColorUtils.swapAlpha(cc2, (float)ColorUtils.getAlphaFromColor(cc2) * 0.8f);
            cc3 = ColorUtils.swapAlpha(cc3, (float)ColorUtils.getAlphaFromColor(cc3) * 0.8f);
            cc4 = ColorUtils.swapAlpha(cc4, (float)ColorUtils.getAlphaFromColor(cc4) * 0.8f);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 0.0f, 25.0f, cc1, cc2, cc3, cc4, true, false, true);
        }
    }

    public static void drawClientHudRect3(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
        RenderUtils.drawClientHudRect3(x, y, x2, y2, alphaPC, 12.0f, manyGlows);
    }

    public static void drawClientHudRect4(float x, float y, float x2, float y2, float alphaPC, float extend, boolean manyGlows) {
        int cli1 = ClientColors.getColorQ(1, alphaPC);
        int cli2 = ClientColors.getColorQ(2, alphaPC);
        int cli3 = ClientColors.getColorQ(3, alphaPC);
        int cli4 = ClientColors.getColorQ(4, alphaPC);
        float alphaPCM = 0.25f;
        int clc1 = ClientColors.getColorQ(1, alphaPC * alphaPCM);
        int clc2 = ClientColors.getColorQ(2, alphaPC * alphaPCM);
        int clc3 = ClientColors.getColorQ(3, alphaPC * alphaPCM);
        int clc4 = ClientColors.getColorQ(4, alphaPC * alphaPCM);
        int bgC = ColorUtils.swapAlpha(Integer.MIN_VALUE, 30.0f);
        float colAToB = 0.7f;
        int clb1 = ColorUtils.getOverallColorFrom(cli1, bgC, colAToB);
        int clb2 = ColorUtils.getOverallColorFrom(cli2, bgC, colAToB);
        int clb3 = ColorUtils.getOverallColorFrom(cli3, bgC, colAToB);
        int clb4 = ColorUtils.getOverallColorFrom(cli4, bgC, colAToB);
        RenderUtils.drawFullGradientRectPro(x, y, x2, y2, clb4, clb3, clb2, clb1, false);
        float lw = 0.5f;
        RenderUtils.drawAlphedVGradient(x, y, x + lw, y + (y2 - y) / 2.0f, ColorUtils.swapAlpha(cli1, 0.0f), ColorUtils.getOverallColorFrom(cli1, cli4), true);
        RenderUtils.drawAlphedVGradient(x, y + (y2 - y) / 2.0f, x + lw, y2, ColorUtils.getOverallColorFrom(cli1, cli4), ColorUtils.swapAlpha(cli4, 0.0f), true);
        RenderUtils.drawAlphedVGradient(x2 - lw, y, x2, y + (y2 - y) / 2.0f, ColorUtils.swapAlpha(cli2, 0.0f), ColorUtils.getOverallColorFrom(cli2, cli3), true);
        RenderUtils.drawAlphedVGradient(x2 - lw, y + (y2 - y) / 2.0f, x2, y2, ColorUtils.getOverallColorFrom(cli2, cli3), ColorUtils.swapAlpha(cli3, 0.0f), true);
        RenderUtils.drawAlphedSideways(x, y, x + (x2 - x) / 2.0f, y + lw, ColorUtils.swapAlpha(cli1, 0.0f), ColorUtils.getOverallColorFrom(cli1, cli2), true);
        RenderUtils.drawAlphedSideways(x + (x2 - x) / 2.0f, y, x2, y + lw, ColorUtils.getOverallColorFrom(cli1, cli2), ColorUtils.swapAlpha(cli2, 0.0f), true);
        RenderUtils.drawAlphedSideways(x, y2 - lw, x + (x2 - x) / 2.0f, y2, ColorUtils.swapAlpha(cli4, 0.0f), ColorUtils.getOverallColorFrom(cli4, cli3), true);
        RenderUtils.drawAlphedSideways(x + (x2 - x) / 2.0f, y2 - lw, x2, y2, ColorUtils.getOverallColorFrom(cli4, cli3), ColorUtils.swapAlpha(cli3, 0.0f), true);
        RenderUtils.drawRoundedFullGradientShadow(x, y, x2, y2, 0.0f, 7.0f, clc1, clc2, clc3, clc4, true);
        if (manyGlows) {
            clc1 = ColorUtils.swapAlpha(clc1, (float)ColorUtils.getAlphaFromColor(clc1) * 0.45f);
            clc2 = ColorUtils.swapAlpha(clc2, (float)ColorUtils.getAlphaFromColor(clc2) * 0.45f);
            clc3 = ColorUtils.swapAlpha(clc3, (float)ColorUtils.getAlphaFromColor(clc3) * 0.45f);
            clc4 = ColorUtils.swapAlpha(clc4, (float)ColorUtils.getAlphaFromColor(clc4) * 0.45f);
            RenderUtils.drawRoundedFullGradientShadow(x, y, x2, y2, 0.0f, 25.0f, clc1, clc2, clc3, clc4, true);
        }
        RenderUtils.glRenderStart();
        GL11.glEnable((int)2832);
        GL11.glPointSize((float)2.3f);
        buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        for (float i = x + 5.0f; i < x2 - 3.0f; i += 3.0f) {
            float wPC = (i - x) / (x2 - x);
            float wPCCenter = (wPC > 0.5f ? 1.0f - wPC : wPC) * 2.0f;
            int c = ColorUtils.getOverallColorFrom(ColorUtils.getOverallColorFrom(cli1, cli4, extend / (y2 - y)), ColorUtils.getOverallColorFrom(cli2, cli3, extend / (y2 - y)), wPC);
            buffer.pos(i, y + extend).color(ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) * wPCCenter)).endVertex();
        }
        tessellator.draw();
        GlStateManager.resetColor();
        RenderUtils.glRenderStop();
    }

    public static void drawClientHudRect4(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
        RenderUtils.drawClientHudRect4(x, y, x2, y2, alphaPC, 13.0f, manyGlows);
    }

    public static void drawClientHudRect2(float x, float y, float x2, float y2, float alphaPC, float extend, boolean manyGlows) {
        float extALL = 1.5f;
        float extY = extend - extALL;
        float extIns = -0.5f;
        int cli1 = ClientColors.getColorQ(1, alphaPC);
        int cli2 = ClientColors.getColorQ(2, alphaPC);
        int cli3 = ClientColors.getColorQ(3, alphaPC);
        int cli4 = ClientColors.getColorQ(4, alphaPC);
        int cc1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC / 2.55f), 0.5f);
        int cc2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC / 2.55f), 0.5f);
        int cc3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC / 2.55f), 0.65f);
        int cc4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC / 2.55f), 0.65f);
        int cs1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC), 0.1f);
        int cs2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC), 0.1f);
        int cs3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC), 0.45f);
        int cs4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, 0, 0, 160.0f * alphaPC), 0.45f);
        StencilUtil.initStencilToWrite();
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x + extALL, y + extALL + extY, x2 - extALL, y2 - extALL, 3.0f, 0.0f, -1, -1, -1, -1, false, true, false);
        StencilUtil.readStencilBuffer(0);
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 5.0f, 1.5f, cs1, cs2, cs3, cs4, false, true, true);
        if (manyGlows) {
            cs1 = ColorUtils.swapAlpha(cs1, (float)ColorUtils.getAlphaFromColor(cs1) * 0.2f);
            cs2 = ColorUtils.swapAlpha(cs2, (float)ColorUtils.getAlphaFromColor(cs2) * 0.2f);
            cs3 = ColorUtils.swapAlpha(cs3, (float)ColorUtils.getAlphaFromColor(cs3) * 0.2f);
            cs4 = ColorUtils.swapAlpha(cs4, (float)ColorUtils.getAlphaFromColor(cs4) * 0.2f);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 5.0f, 20.0f, cs1, cs2, cs3, cs4, false, false, true);
        }
        StencilUtil.uninitStencilBuffer();
        RenderUtils.fullRoundFG(x + extALL + extIns, y + extALL + extY + extIns, x2 - extALL - extIns, y2 - extALL - extIns, 6.0f, cc1, cc2, cc3, cc4, false);
    }

    public static void drawClientHudRect2(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
        RenderUtils.drawClientHudRect2(x, y, x2, y2, alphaPC, 13.0f, manyGlows);
    }

    public static void drawClientHudRect2(float x, float y, float x2, float y2, boolean manyGlows) {
        RenderUtils.drawClientHudRect2(x, y, x2, y2, 1.0f, manyGlows);
    }

    public static void drawClientHudRect(float x, float y, float x2, float y2, float alphaPC, boolean manyGlows) {
        int cli1 = ClientColors.getColorQ(1, alphaPC);
        int cli2 = ClientColors.getColorQ(2, alphaPC);
        int cli3 = ClientColors.getColorQ(3, alphaPC);
        int cli4 = ClientColors.getColorQ(4, alphaPC);
        int cc1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, (int)(160.0f * alphaPC)), 0.7f);
        int cc2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, (int)(160.0f * alphaPC)), 0.7f);
        int cc3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, (int)(160.0f * alphaPC)), 0.75f);
        int cc4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, (int)(160.0f * alphaPC)), 0.75f);
        int cs1 = ColorUtils.getOverallColorFrom(cli1, ColorUtils.getColor(0, (int)((manyGlows ? 90.0f : 160.0f) * alphaPC)), 0.4f);
        int cs2 = ColorUtils.getOverallColorFrom(cli2, ColorUtils.getColor(0, (int)((manyGlows ? 90.0f : 160.0f) * alphaPC)), 0.4f);
        int cs3 = ColorUtils.getOverallColorFrom(cli3, ColorUtils.getColor(0, (int)((manyGlows ? 90.0f : 160.0f) * alphaPC)), 0.7f);
        int cs4 = ColorUtils.getOverallColorFrom(cli4, ColorUtils.getColor(0, (int)((manyGlows ? 90.0f : 160.0f) * alphaPC)), 0.7f);
        int cm = ColorUtils.getOverallColorFrom(cli1, cli2);
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 2.375f, 0.5f, cc1, cc2, cc3, cc4, false, true, true);
        RenderUtils.drawRoundedFullGradientShadow(x, y, x2, y2, 2.5f, 7.5f, cs1, cs2, cs3, cs4, true);
        if (manyGlows) {
            int cm1 = ColorUtils.swapAlpha(cs1, (float)ColorUtils.getAlphaFromColor(cs1) * 0.55f);
            int cm2 = ColorUtils.swapAlpha(cs2, (float)ColorUtils.getAlphaFromColor(cs2) * 0.55f);
            int cm3 = ColorUtils.swapAlpha(cs3, (float)ColorUtils.getAlphaFromColor(cs3) * 0.55f);
            int cm4 = ColorUtils.swapAlpha(cs4, (float)ColorUtils.getAlphaFromColor(cs4) * 0.55f);
            RenderUtils.drawRoundedFullGradientShadow(x, y, x2, y2, 2.5f, 22.0f, cm1, cm2, cm3, cm4, true);
        }
        RenderUtils.drawRoundedFullGradientInsideShadow(x, y, x2, y2, 5.0f, cs1, cs2, cs3, cs4, true);
        RenderUtils.drawAlphedSideways(x + 2.0f, y + 1.5f, x + (x2 - x) / 2.0f, y + 3.0f, ColorUtils.swapAlpha(cli1, 0.0f), cm, true);
        RenderUtils.drawAlphedSideways(x + (x2 - x) / 2.0f, y + 1.5f, x2 - 2.0f, y + 3.0f, cm, ColorUtils.swapAlpha(cli2, 0.0f), true);
    }

    public static void drawClientHudRect(float x, float y, float x2, float y2, boolean manyGlows) {
        RenderUtils.drawClientHudRect(x, y, x2, y2, 1.0f, manyGlows);
    }

    public static void hudRectWithString(float x, float y, float x2, float y2, String elementName, String renderMode, float alphaPC, boolean manyGlows) {
        String draw;
        if (renderMode.isEmpty()) {
            return;
        }
        float extYText = 0.0f;
        switch (renderMode) {
            case "Glow": {
                RenderUtils.drawClientHudRect(x, y, x2, y2, alphaPC, manyGlows);
                extYText = 7.0f;
                break;
            }
            case "Window": {
                RenderUtils.drawClientHudRect2(x, y, x2, y2, alphaPC, manyGlows);
                extYText = 4.5f;
                break;
            }
            case "Plain": {
                RenderUtils.drawClientHudRect3(x, y, x2, y2, alphaPC, manyGlows);
                extYText = 4.0f;
                break;
            }
            case "Stipple": {
                RenderUtils.drawClientHudRect4(x, y, x2, y2, alphaPC, manyGlows);
                extYText = 4.5f;
                break;
            }
            default: {
                return;
            }
        }
        if (255.0f * alphaPC < 33.0f) {
            return;
        }
        int texCol = ColorUtils.swapAlpha(-1, 255.0f * alphaPC);
        Fonts.mntsb_16.drawStringWithShadow(elementName, x + 3.0f, y + extYText, texCol);
        texCol = ColorUtils.swapAlpha(-1, 65.0f * alphaPC);
        if (65.0f * alphaPC < 33.0f) {
            return;
        }
        switch (elementName) {
            case "Potions": {
                draw = "C";
                break;
            }
            case "Staff list": {
                draw = "B";
                break;
            }
            case "Keybinds": {
                draw = "L";
                break;
            }
            case "Pickups list": {
                draw = "M";
                break;
            }
            default: {
                return;
            }
        }
        if (draw != null) {
            Fonts.stylesicons_18.drawString(draw, x2 - 12.5f, y + extYText + 0.5f, texCol);
        }
    }

    public static void hudRectWithString(float x, float y, float x2, float y2, String elementName, String renderMode, boolean manyGlows) {
        RenderUtils.hudRectWithString(x, y, x2, y2, elementName, renderMode, 1.0f, manyGlows);
    }

    public static final void setup3dForBlockPos(Runnable render, boolean bloom) {
        double glX = RenderManager.viewerPosX;
        double glY = RenderManager.viewerPosY;
        double glZ = RenderManager.viewerPosZ;
        GL11.glPushMatrix();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderUtils.mc.entityRenderer.disableLightmap();
        GL11.glEnable((int)3042);
        GL11.glLineWidth((float)1.0f);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDisable((int)2896);
        GL11.glShadeModel((int)7425);
        GL11.glTranslated((double)(-glX), (double)(-glY), (double)(-glZ));
        render.run();
        GL11.glTranslated((double)glX, (double)glY, (double)glZ);
        GL11.glLineWidth((float)1.0f);
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.resetColor();
        GL11.glPopMatrix();
    }

    public static void drawCircledTHud(float cx, double cy, float r, float percent, int color, float alpha, float lineWidth) {
        GL11.glPushMatrix();
        GL11.glEnable((int)2848);
        cx *= 2.0f;
        cy *= 2.0;
        GlStateManager.glLineWidth(2.0f);
        float theta = 0.0175f;
        float p = (float)Math.cos(theta);
        float s = (float)Math.sin(theta);
        float x = r *= 2.0f;
        float y = 0.0f;
        RenderUtils.enableGL2D();
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        GL11.glLineWidth((float)lineWidth);
        int[] counter = new int[]{1};
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        for (float ii = 0.0f; ii < 360.0f * percent; ii += 1.0f) {
            buffer.pos(x + cx, (double)y + cy).color(ColorUtils.swapAlpha(color, alpha)).endVertex();
            float t = x;
            x = p * x - s * y;
            y = s * t + p * y;
            counter[0] = counter[0] + 1;
        }
        tessellator.draw();
        GL11.glDisable((int)2848);
        GL11.glScalef((float)2.0f, (float)2.0f, (float)2.0f);
        RenderUtils.disableGL2D();
        GlStateManager.resetColor();
        GlStateManager.glLineWidth(1.0f);
        RenderUtils.resetBlender();
        GlStateManager.enableBlend();
        GL11.glPopMatrix();
    }

    public static void drawCircledTHudWithOverallColor(float cx, double cy, float r, float percent, int color, float alpha, float lineWidth, int color2, float pcColor2) {
        GL11.glPushMatrix();
        GL11.glEnable((int)2848);
        cx *= 2.0f;
        cy *= 2.0;
        GlStateManager.glLineWidth(2.0f);
        float theta = 0.0175f;
        float p = (float)Math.cos(theta);
        float s = (float)Math.sin(theta);
        float x = r *= 2.0f;
        float y = 0.0f;
        RenderUtils.enableGL2D();
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        GL11.glLineWidth((float)lineWidth);
        int[] counter = new int[]{1};
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        for (float ii = 0.0f; ii < 360.0f * percent; ii += 1.0f) {
            RenderUtils.setupColor(ColorUtils.getOverallColorFrom(color, color2, pcColor2), alpha);
            buffer.pos(x + cx, (double)y + cy).color(ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color, color2, pcColor2), alpha)).endVertex();
            float t = x;
            x = p * x - s * y;
            y = s * t + p * y;
            counter[0] = counter[0] + 1;
        }
        tessellator.draw();
        GL11.glDisable((int)2848);
        GL11.glScalef((float)2.0f, (float)2.0f, (float)2.0f);
        RenderUtils.disableGL2D();
        GlStateManager.resetColor();
        GlStateManager.glLineWidth(1.0f);
        RenderUtils.resetBlender();
        GL11.glPopMatrix();
    }

    public static void enableGUIStandardItemLighting() {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(165.0f, 1.0f, 0.0f, 0.0f);
        RenderUtils.enableStandardItemLighting();
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
        GlStateManager.glLight(16384, 4611, RenderUtils.setColorBuffer(RenderUtils.LIGHT0_POS.xCoord, RenderUtils.LIGHT0_POS.yCoord, RenderUtils.LIGHT0_POS.zCoord, 0.0));
        GlStateManager.glLight(16384, 4609, RenderUtils.setColorBuffer(0.6f, 0.6f, 0.6f, 1.0f));
        GlStateManager.glLight(16384, 4608, RenderUtils.setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.glLight(16384, 4610, RenderUtils.setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.glLight(16385, 4611, RenderUtils.setColorBuffer(RenderUtils.LIGHT1_POS.xCoord, RenderUtils.LIGHT1_POS.yCoord, RenderUtils.LIGHT1_POS.zCoord, 0.0));
        GlStateManager.glLight(16385, 4609, RenderUtils.setColorBuffer(0.6f, 0.6f, 0.6f, 1.0f));
        GlStateManager.glLight(16385, 4608, RenderUtils.setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.glLight(16385, 4610, RenderUtils.setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.shadeModel(7424);
        GlStateManager.glLightModel(2899, RenderUtils.setColorBuffer(0.4f, 0.4f, 0.4f, 1.0f));
    }

    private static FloatBuffer setColorBuffer(double p_74517_0_, double p_74517_2_, double p_74517_4_, double p_74517_6_) {
        return RenderUtils.setColorBuffer((float)p_74517_0_, (float)p_74517_2_, (float)p_74517_4_, (float)p_74517_6_);
    }

    public static FloatBuffer setColorBuffer(float p_74521_0_, float p_74521_1_, float p_74521_2_, float p_74521_3_) {
        COLOR_BUFFER.clear();
        COLOR_BUFFER.put(p_74521_0_).put(p_74521_1_).put(p_74521_2_).put(p_74521_3_);
        COLOR_BUFFER.flip();
        return COLOR_BUFFER;
    }

    public static void drawClientCircle(float cx, double cy, float r, float minus, float lineW, float alphaPC) {
        RenderUtils.enableGL2D();
        GL11.glPointSize((float)lineW);
        GL11.glEnable((int)2832);
        buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        int ii = 180;
        while ((float)ii <= minus + 180.0f) {
            double x1 = (double)cx + Math.sin((double)(ii += 6) * Math.PI / 180.0) * (double)r;
            double y1 = cy + Math.cos((double)ii * Math.PI / 180.0) * (double)r;
            buffer.pos(x1, y1).color(ClientColors.getColor1(ii * 3, 0.75f * alphaPC)).endVertex();
            GlStateManager.resetColor();
        }
        tessellator.draw();
        GL11.glDisable((int)2832);
        GL11.glPointSize((float)1.0f);
        RenderUtils.disableGL2D();
        GL11.glEnable((int)3042);
        GlStateManager.resetColor();
        GlStateManager.glLineWidth(1.0f);
    }

    public static void drawClientCircleWithOverallToColor(float cx, double cy, float r, float minus, float lineW, float alphaPC, int color2, float pcColor2) {
        RenderUtils.enableGL2D();
        GL11.glPointSize((float)lineW);
        GL11.glEnable((int)2832);
        buffer.begin(0, DefaultVertexFormats.POSITION_COLOR);
        int ii = 180;
        while ((float)ii <= minus + 180.0f) {
            double x1 = (double)cx + Math.sin((double)(ii += 6) * Math.PI / 180.0) * (double)r;
            double y1 = cy + Math.cos((double)ii * Math.PI / 180.0) * (double)r;
            buffer.pos(x1, y1).color(ColorUtils.getOverallColorFrom(ClientColors.getColor1(ii * 3, 0.75f * alphaPC), ColorUtils.swapAlpha(color2, (float)ColorUtils.getAlphaFromColor(color2) * alphaPC), pcColor2)).endVertex();
        }
        tessellator.draw();
        GL11.glDisable((int)2832);
        GL11.glPointSize((float)1.0f);
        RenderUtils.disableGL2D();
        GL11.glEnable((int)3042);
        GlStateManager.resetColor();
        GlStateManager.glLineWidth(1.0f);
    }

    public static void drawClientCircle(float cx, double cy, float r, float minus, float lineW) {
        RenderUtils.drawClientCircle(cx, cy, r, minus, lineW, 1.0f);
    }

    public static void drawCanisterBox(AxisAlignedBB axisalignedbb, boolean outlineBox, boolean decussationBox, boolean fullBox, int outlineColor, int decussationColor, int fullColor) {
        AxisAlignedBB boundingBox = axisalignedbb;
        GlStateManager.pushMatrix();
        GlStateManager.glLineWidth(0.01f);
        GL11.glDisable((int)3008);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        if (outlineBox) {
            RenderUtils.glColor(outlineColor);
            buffer.begin(2, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
            tessellator.draw();
        }
        if (decussationBox) {
            RenderUtils.glColor(decussationColor);
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            tessellator.draw();
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            tessellator.draw();
        }
        if (outlineBox) {
            RenderUtils.glColor(outlineColor);
            buffer.begin(2, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
            tessellator.draw();
        }
        if (decussationBox) {
            RenderUtils.glColor(decussationColor);
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
            tessellator.draw();
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
            tessellator.draw();
        }
        if (outlineBox) {
            RenderUtils.glColor(outlineColor);
            buffer.begin(2, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
            tessellator.draw();
        }
        if (decussationBox) {
            RenderUtils.glColor(decussationColor);
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
            tessellator.draw();
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
            tessellator.draw();
        }
        if (outlineBox) {
            RenderUtils.glColor(outlineColor);
            buffer.begin(2, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
            tessellator.draw();
        }
        if (decussationBox) {
            RenderUtils.glColor(decussationColor);
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            tessellator.draw();
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            tessellator.draw();
        }
        if (outlineBox) {
            RenderUtils.glColor(outlineColor);
            buffer.begin(2, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
            tessellator.draw();
        }
        if (decussationBox) {
            RenderUtils.glColor(decussationColor);
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
            tessellator.draw();
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            tessellator.draw();
        }
        if (outlineBox) {
            RenderUtils.glColor(outlineColor);
            buffer.begin(2, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
            tessellator.draw();
        }
        if (decussationBox) {
            RenderUtils.glColor(decussationColor);
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
            tessellator.draw();
            buffer.begin(1, DefaultVertexFormats.POSITION);
            buffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
            buffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
            tessellator.draw();
        }
        if (fullBox) {
            RenderUtils.glColor(fullColor);
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
        GL11.glEnable((int)3008);
        GL11.glHint((int)3154, (int)4352);
        GL11.glDisable((int)2848);
        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    }

    public static void drawGradientAlphaBox(AxisAlignedBB bb, boolean outlineBox, boolean fullBox, int outlineColor, int fullColor) {
        AxisAlignedBB ab = bb;
        GlStateManager.pushMatrix();
        GL11.glDisable((int)3008);
        GL11.glDisable((int)2884);
        GL11.glShadeModel((int)7425);
        GL11.glEnable((int)2848);
        double x1 = ab.minX;
        double y1 = ab.minY;
        double z1 = ab.minZ;
        double x2 = ab.maxX;
        double y2 = ab.maxY;
        double z2 = ab.maxZ;
        double wx = x2 - x1;
        double wy = y2 - y1;
        double wz = z2 - z1;
        if (outlineBox) {
            GlStateManager.glLineWidth(1.0f);
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
        GL11.glDisable((int)2848);
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3008);
        GL11.glEnable((int)2884);
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.popMatrix();
    }

    public static void drawGradientAlphaBoxWithBooleanDownPool(AxisAlignedBB bb, boolean outlineBox, boolean fullBox, boolean downPull, int outlineColor, int fullColor) {
        AxisAlignedBB ab = bb;
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.pushMatrix();
        GL11.glDisable((int)3008);
        GL11.glDisable((int)2884);
        GL11.glShadeModel((int)7425);
        GL11.glEnable((int)2848);
        double x1 = ab.minX;
        double y1 = ab.minY;
        double z1 = ab.minZ;
        double x2 = ab.maxX;
        double y2 = ab.maxY;
        double z2 = ab.maxZ;
        double wx = x2 - x1;
        double wy = y2 - y1;
        double wz = z2 - z1;
        if (outlineBox) {
            GlStateManager.glLineWidth(2.0f);
            RenderUtils.glColor(outlineColor);
            GL11.glBegin((int)2);
            GL11.glVertex3d((double)x1, (double)y1, (double)z1);
            GL11.glVertex3d((double)x2, (double)y1, (double)z1);
            GL11.glVertex3d((double)x2, (double)y1, (double)z2);
            GL11.glVertex3d((double)x1, (double)y1, (double)z2);
            GL11.glEnd();
            RenderUtils.glColor(outlineColor);
            GL11.glBegin((int)3);
            GL11.glVertex3d((double)x1, (double)y1, (double)z1);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x1, (double)y2, (double)z1);
            GL11.glEnd();
            RenderUtils.glColor(outlineColor);
            GL11.glBegin((int)3);
            GL11.glVertex3d((double)x2, (double)y1, (double)z1);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x2, (double)y2, (double)z1);
            GL11.glEnd();
            RenderUtils.glColor(outlineColor);
            GL11.glBegin((int)3);
            GL11.glVertex3d((double)x1, (double)y1, (double)z2);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x1, (double)y2, (double)z2);
            GL11.glEnd();
            RenderUtils.glColor(outlineColor);
            GL11.glBegin((int)3);
            GL11.glVertex3d((double)x2, (double)y1, (double)z2);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x2, (double)y2, (double)z2);
            GL11.glEnd();
        }
        if (fullBox) {
            if (downPull) {
                RenderUtils.glColor(fullColor);
                GL11.glBegin((int)7);
                GL11.glVertex3d((double)x1, (double)y1, (double)z1);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)z1);
                RenderUtils.glColor(0);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)(z1 + wz / 2.0));
                RenderUtils.glColor(fullColor);
                GL11.glVertex3d((double)x1, (double)y1, (double)(z1 + wz / 2.0));
                GL11.glEnd();
                RenderUtils.glColor(fullColor);
                GL11.glBegin((int)7);
                GL11.glVertex3d((double)x2, (double)y1, (double)z1);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)z1);
                RenderUtils.glColor(0);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)(z1 + wz / 2.0));
                RenderUtils.glColor(fullColor);
                GL11.glVertex3d((double)x2, (double)y1, (double)(z1 + wz / 2.0));
                GL11.glEnd();
                RenderUtils.glColor(fullColor);
                GL11.glBegin((int)7);
                GL11.glVertex3d((double)x2, (double)y1, (double)z2);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)z2);
                RenderUtils.glColor(0);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)(z1 + wz / 2.0));
                RenderUtils.glColor(fullColor);
                GL11.glVertex3d((double)x2, (double)y1, (double)(z1 + wz / 2.0));
                GL11.glEnd();
                RenderUtils.glColor(fullColor);
                GL11.glBegin((int)7);
                GL11.glVertex3d((double)x1, (double)y1, (double)z2);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)z2);
                RenderUtils.glColor(0);
                GL11.glVertex3d((double)(x1 + wx / 2.0), (double)y1, (double)(z1 + wz / 2.0));
                RenderUtils.glColor(fullColor);
                GL11.glVertex3d((double)x1, (double)y1, (double)(z1 + wz / 2.0));
                GL11.glEnd();
            }
            RenderUtils.glColor(fullColor);
            GL11.glBegin((int)7);
            GL11.glVertex3d((double)x1, (double)y1, (double)z1);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x1, (double)y2, (double)z1);
            GL11.glVertex3d((double)x2, (double)y2, (double)z1);
            RenderUtils.glColor(fullColor);
            GL11.glVertex3d((double)x2, (double)y1, (double)z1);
            GL11.glEnd();
            RenderUtils.glColor(fullColor);
            GL11.glBegin((int)7);
            GL11.glVertex3d((double)x1, (double)y1, (double)z1);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x1, (double)y2, (double)z1);
            GL11.glVertex3d((double)x1, (double)y2, (double)z2);
            RenderUtils.glColor(fullColor);
            GL11.glVertex3d((double)x1, (double)y1, (double)z2);
            GL11.glEnd();
            RenderUtils.glColor(fullColor);
            GL11.glBegin((int)7);
            GL11.glVertex3d((double)x1, (double)y1, (double)z2);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x1, (double)y2, (double)z2);
            GL11.glVertex3d((double)x2, (double)y2, (double)z2);
            RenderUtils.glColor(fullColor);
            GL11.glVertex3d((double)x2, (double)y1, (double)z2);
            GL11.glEnd();
            RenderUtils.glColor(fullColor);
            GL11.glBegin((int)7);
            GL11.glVertex3d((double)x2, (double)y1, (double)z1);
            RenderUtils.glColor(0);
            GL11.glVertex3d((double)x2, (double)y2, (double)z1);
            GL11.glVertex3d((double)x2, (double)y2, (double)z2);
            RenderUtils.glColor(fullColor);
            GL11.glVertex3d((double)x2, (double)y1, (double)z2);
            GL11.glEnd();
        }
        GL11.glDisable((int)2848);
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3008);
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.popMatrix();
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        Minecraft mc = Minecraft.getMinecraft();
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return framebuffer;
    }

    public static boolean isInView(Entity ent) {
        frustum.setPosition(RenderUtils.mc.getRenderViewEntity().posX, RenderUtils.mc.getRenderViewEntity().posY, RenderUtils.mc.getRenderViewEntity().posZ);
        return ent instanceof EntityPlayerSP || frustum.isBoundingBoxInFrustum(ent.getEntityBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public static void drawRound(float x, float y, float width, float height, float radius, int color) {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        roundedShader.init();
        ShaderUtility.setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", 0);
        roundedShader.setUniformf("color", ColorUtils.getGLRedFromColor(color), ColorUtils.getGLGreenFromColor(color), ColorUtils.getGLBlueFromColor(color), ColorUtils.getGLAlphaFromColor(color));
        ShaderUtility.drawQuads(x - 1.0f, y - 1.0f, width + 2.0f, height + 2.0f);
        roundedShader.unload();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GL11.glPopMatrix();
    }

    public static void drawRoundOutline(float x, float y, float width, float height, float radius, float outlineThickness, int color, int outlineColor, ScaledResolution sr) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();
        GL11.glDisable((int)3008);
        roundedOutlineShader.init();
        ShaderUtility.setupRoundedRectUniforms(x, y, width, height, radius, roundedOutlineShader);
        if ((float)roundedOutlineShader.getUniform("outlineThickness") != outlineThickness * (float)ScaledResolution.getScaleFactor()) {
            roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * (float)ScaledResolution.getScaleFactor());
        }
        roundedOutlineShader.setUniformColor("color", color);
        roundedOutlineShader.setUniformColor("outlineColor", outlineColor);
        ShaderUtility.drawQuads(x - (2.0f + outlineThickness), y - (2.0f + outlineThickness), width + (4.0f + outlineThickness * 2.0f), height + (4.0f + outlineThickness * 2.0f));
        roundedOutlineShader.unload();
        GL11.glEnable((int)3008);
    }

    public static void dispose() {
        GL11.glDisable((int)2960);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    public static void drawPlayerPing(float x, float y, EntityPlayer entity, float alpha) {
        if (Minecraft.player.connection.getPlayerInfo(entity.getUniqueID()) == null) {
            return;
        }
        Gui gui = new Gui();
        NetworkPlayerInfo networkPlayerInfoIn = Minecraft.player.connection.getPlayerInfo(entity.getUniqueID());
        ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
        RenderUtils.setupColor(ColorUtils.getFixedWhiteColor(), alpha);
        mc.getTextureManager().bindTexture(ICONS);
        boolean i = false;
        int j = networkPlayerInfoIn.getResponseTime() < 0 ? 5 : (networkPlayerInfoIn.getResponseTime() < 150 ? 0 : (networkPlayerInfoIn.getResponseTime() < 300 ? 1 : (networkPlayerInfoIn.getResponseTime() < 600 ? 2 : (networkPlayerInfoIn.getResponseTime() < 1000 ? 3 : 4))));
        GL11.glEnable((int)3042);
        GlStateManager.disableDepth();
        gui.zLevel += 100.0f;
        gui.drawTexturedModalRect(x, y, 0, 176 + j * 8, 10, 8);
        gui.zLevel -= 100.0f;
        GlStateManager.enableDepth();
        GlStateManager.resetColor();
    }

    public static void write(boolean renderClipLayer) {
        RenderUtils.checkSetupFBO1();
        GL11.glClearStencil((int)0);
        GL11.glClear((int)1024);
        GL11.glEnable((int)2960);
        GL11.glStencilFunc((int)519, (int)1, (int)65535);
        GL11.glStencilOp((int)7680, (int)7680, (int)7681);
        if (!renderClipLayer) {
            GlStateManager.colorMask(false, false, false, false);
        }
    }

    public static void write(boolean renderClipLayer, Framebuffer fb, boolean clearStencil, boolean invert) {
        RenderUtils.checkSetupFBO(fb);
        if (clearStencil) {
            GL11.glClearStencil((int)0);
            GL11.glClear((int)1024);
            GL11.glEnable((int)2960);
        }
        GL11.glStencilFunc((int)519, (int)(invert ? 0 : 1), (int)65535);
        GL11.glStencilOp((int)7680, (int)7680, (int)7681);
        if (!renderClipLayer) {
            GlStateManager.colorMask(false, false, false, false);
        }
    }

    public static void checkSetupFBO1() {
        Framebuffer fbo = mc.getFramebuffer();
        if (fbo != null && fbo.depthBuffer > -1) {
            RenderUtils.setupFBO1(fbo);
            fbo.depthBuffer = -1;
        }
    }

    public static void checkSetupFBO(Framebuffer fbo) {
        if (fbo != null && fbo.depthBuffer > -1) {
            RenderUtils.setupFBO1(fbo);
            fbo.depthBuffer = -1;
        }
    }

    public static void setupFBO1(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT((int)fbo.depthBuffer);
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT((int)36161, (int)stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT((int)36161, (int)34041, (int)Minecraft.getMinecraft().displayWidth, (int)Minecraft.getMinecraft().displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT((int)36160, (int)36128, (int)36161, (int)stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT((int)36160, (int)36096, (int)36161, (int)stencil_depth_buffer_ID);
    }

    public static void drawWaveGradient(float x, float y, float x2, float y2, float aPC, int colorStep1, int colorStep2, int colorStep3, int colorStep4, boolean blend, boolean toNull) {
        float rect1X1 = x;
        float rect1X2 = x + (x2 - x) / 3.0f;
        float rect1Y1 = y;
        float rect1Y2 = y2;
        float rect2X1 = rect1X2;
        float rect2X2 = x + (x2 - x) / 3.0f * 2.0f;
        float rect2Y1 = y;
        float rect2Y2 = y2;
        float rect3X1 = x + (x2 - x) / 3.0f * 2.0f;
        float rect3X2 = x2;
        float rect3Y1 = y;
        float rect3Y2 = y2;
        float bright = aPC;
        int c1 = ColorUtils.getOverallColorFrom(colorStep1, ColorUtils.swapAlpha(-1, bright * (float)ColorUtils.getAlphaFromColor(colorStep1)), bright);
        int c2 = ColorUtils.getOverallColorFrom(colorStep2, ColorUtils.swapAlpha(-1, bright * (float)ColorUtils.getAlphaFromColor(colorStep2)), bright);
        int c3 = ColorUtils.getOverallColorFrom(colorStep3, ColorUtils.swapAlpha(-1, bright * (float)ColorUtils.getAlphaFromColor(colorStep3)), bright);
        int c4 = ColorUtils.getOverallColorFrom(colorStep4, ColorUtils.swapAlpha(-1, bright * (float)ColorUtils.getAlphaFromColor(colorStep4)), bright);
        RenderUtils.drawFullGradientRectPro(rect1X2, rect1Y2, rect1X1, rect1Y1, toNull ? 0 : colorStep2, toNull ? 0 : colorStep1, c1, c2, blend);
        RenderUtils.drawFullGradientRectPro(rect2X2, rect2Y2, rect2X1, rect2Y1, toNull ? 0 : colorStep3, toNull ? 0 : colorStep2, c2, c3, blend);
        RenderUtils.drawFullGradientRectPro(rect3X2, rect3Y2, rect3X1, rect3Y1, toNull ? 0 : colorStep4, toNull ? 0 : colorStep3, c3, c4, blend);
    }

    public static void drawSome(List<Vec2f> pos, int color) {
        if (pos.isEmpty()) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glPushAttrib((int)1048575);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)3008);
        buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
        for (Vec2f vec2f : pos) {
            buffer.pos(vec2f.x, vec2f.y).color(color).endVertex();
        }
        tessellator.draw();
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2884);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void drawSome(List<Vec2f> pos, int color, int begin) {
        if (pos.isEmpty()) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glPushAttrib((int)1048575);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)3008);
        buffer.begin(begin, DefaultVertexFormats.POSITION_COLOR);
        for (Vec2f vec2f : pos) {
            buffer.pos(vec2f.x, vec2f.y).color(color).endVertex();
        }
        tessellator.draw();
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2884);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void drawVec2Colored(List<Vec2fColored> pos) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)3008);
        GL11.glShadeModel((int)7425);
        RenderUtils.anialisON(false, true, false);
        buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
        for (Vec2fColored vec : pos) {
            buffer.pos(vec.getX(), vec.getY()).color(vec.getColor()).endVertex();
        }
        tessellator.draw();
        RenderUtils.anialisOFF(false, true, false);
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2884);
        GL11.glPopMatrix();
    }

    public static void drawPolygonPartsGlowBackSAlpha(double x, double y, float radius, int part, int color, int endcolor, float Alpha, boolean bloom) {
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float alpha1 = (float)(endcolor >> 24 & 0xFF) / 255.0f;
        float red1 = (float)(endcolor >> 16 & 0xFF) / 255.0f;
        float green1 = (float)(endcolor >> 8 & 0xFF) / 255.0f;
        float blue1 = (float)(endcolor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void fixShadows() {
        GlStateManager.enableBlend();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    public static void drawFullGradientRectPro(float x, float y, float x2, float y2, int color, int color2, int color3, int color4, boolean blend) {
        GlStateManager.enableBlend();
        if (blend) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_CONSTANT_ALPHA, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glShadeModel((int)7425);
        GL11.glDisable((int)3008);
        buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x2, y, 0.0).color(color3).endVertex();
        buffer.pos(x, y, 0.0).color(color4).endVertex();
        buffer.pos(x, y2, 0.0).color(color).endVertex();
        buffer.pos(x2, y2, 0.0).color(color2).endVertex();
        tessellator.draw();
        if (blend) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GL11.glEnable((int)3008);
        GL11.glShadeModel((int)7424);
        GlStateManager.enableTexture2D();
        GlStateManager.resetColor();
    }

    public static void resetBlender() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableTexture2D();
        GL11.glShadeModel((int)7425);
        GL11.glDisable((int)3008);
        GL11.glEnable((int)3008);
        GL11.glShadeModel((int)7424);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void drawMinecraftRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(startColor & 0xFF) / 255.0f;
        float f4 = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float f5 = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(endColor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 300.0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, 300.0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, 300.0).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, 300.0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void fullRoundFG(float x, float y, float x2, float y2, float r, int c, int c2, int c3, int c4, boolean bloom) {
        RenderUtils.drawFullGradientRectPro(x + r / 2.0f, y + r / 2.0f, x2 - r / 2.0f, y2 - r / 2.0f, c4, c3, c2, c, bloom);
        RenderUtils.drawFullGradientRectPro(x + r / 2.0f, y, x2 - r / 2.0f, y + r / 2.0f, c, c2, c2, c, bloom);
        RenderUtils.drawFullGradientRectPro(x + r / 2.0f, y2 - r / 2.0f, x2 - r / 2.0f, y2, c4, c3, c3, c4, bloom);
        RenderUtils.drawFullGradientRectPro(x, y + r / 2.0f, x + r / 2.0f, y2 - r / 2.0f, c4, c4, c, c, bloom);
        RenderUtils.drawFullGradientRectPro(x2 - r / 2.0f, y + r / 2.0f, x2, y2 - r / 2.0f, c3, c3, c2, c2, bloom);
        StencilUtil.initStencilToWrite();
        RenderUtils.drawRect(x, y + r / 2.0f, x2, y2 - r / 2.0f, -1);
        RenderUtils.drawRect(x + r / 2.0f, y, x2 - r / 2.0f, y2, -1);
        StencilUtil.readStencilBuffer(0);
        RenderUtils.drawSmoothCircle(x + r / 2.0f, y + r / 2.0f + 0.125f, r / 2.0f, c, bloom);
        RenderUtils.drawSmoothCircle(x2 - r / 2.0f, y + r / 2.0f + 0.125f, r / 2.0f, c2, bloom);
        RenderUtils.drawSmoothCircle(x2 - r / 2.0f, y2 - r / 2.0f + 0.125f, r / 2.0f, c3, bloom);
        RenderUtils.drawSmoothCircle(x + r / 2.0f, y2 - r / 2.0f + 0.125f, r / 2.0f, c4, bloom);
        StencilUtil.uninitStencilBuffer();
    }

    public static void drawSmoothCircle(double x, double y, float radius, int color) {
        RenderUtils.runGLColor(color);
        RenderUtils.setup2D(() -> {
            GL11.glDisable((int)3008);
            GL11.glEnable((int)2832);
            GL11.glPointSize((float)(radius * (float)(2 * Minecraft.getMinecraft().gameSettings.guiScale)));
            RenderUtils.renderObj(0, () -> GL11.glVertex2d((double)x, (double)y));
            GL11.glEnable((int)3008);
        });
        GlStateManager.resetColor();
    }

    public static void drawSmoothCircle(double x, double y, float radius, int color, boolean bloom) {
        RenderUtils.runGLColor(color);
        RenderUtils.setup2D(() -> {
            if (bloom) {
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }
            GL11.glDisable((int)3008);
            GL11.glEnable((int)2832);
            GL11.glHint((int)3153, (int)4354);
            ScaledResolution rs = new ScaledResolution(mc);
            float scale = (float)((double)ScaledResolution.getScaleFactor() / Math.pow(ScaledResolution.getScaleFactor(), 2.0));
            GL11.glPointSize((float)(radius / scale * 2.0f));
            RenderUtils.renderObj(0, () -> GL11.glVertex2d((double)x, (double)y));
            GL11.glEnable((int)3008);
            if (bloom) {
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }
        });
    }

    public static void setup2D(Runnable f) {
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        f.run();
        GL11.glEnable((int)3553);
    }

    public static void renderObj(int mode, Runnable render) {
        GL11.glBegin((int)mode);
        render.run();
        GL11.glEnd();
    }

    public static void runGLColor(int orRGB) {
        float c1 = (float)(orRGB >> 16 & 0xFF) / 255.0f;
        float c2 = (float)(orRGB >> 8 & 0xFF) / 255.0f;
        float c3 = (float)(orRGB & 0xFF) / 255.0f;
        float c4 = (float)(orRGB >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f((float)c1, (float)c2, (float)c3, (float)c4);
    }

    public static void scale(float x, float y, float scale, Runnable data) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)0.0f);
        GL11.glScalef((float)scale, (float)scale, (float)1.0f);
        GL11.glTranslatef((float)(-x), (float)(-y), (float)0.0f);
        data.run();
        GL11.glPopMatrix();
    }

    public static void scissor(double x, double y, double width, double height, Runnable data) {
        GL11.glEnable((int)3089);
        RenderUtils.scissor(x, y, width, height);
        data.run();
        GL11.glDisable((int)3089);
    }

    public static void scissor(double x, double y, double width, double height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        double scale = ScaledResolution.getScaleFactor();
        double finalHeight = height * scale;
        double finalY = ((double)sr.getScaledHeight() - y) * scale;
        double finalX = x * scale;
        double finalWidth = width * scale;
        GL11.glScissor((int)((int)finalX), (int)((int)(finalY - finalHeight)), (int)((int)finalWidth), (int)((int)finalHeight));
    }

    public static void scissorRected(double x, double y, double x2, double y2) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        double scale = ScaledResolution.getScaleFactor();
        double finalHeight = (y2 - y) * scale;
        double finalY = ((double)sr.getScaledHeight() - y) * scale;
        double finalX = x * scale;
        double finalWidth = (x2 - y) * scale;
        GL11.glScissor((int)((int)finalX), (int)((int)(finalY - finalHeight)), (int)((int)finalWidth), (int)((int)finalHeight));
    }

    public static void scissorCoord(double x, double y, double x2, double y2) {
        Minecraft mc = Minecraft.getMinecraft();
        double xPos1 = x < x2 ? x : x2;
        double xPos2 = x2 > x ? x2 : x;
        double yPos1 = y < y2 ? y : y2;
        double yPos2 = y2 > y ? y2 : y;
        GL11.glScissor((int)((int)xPos1), (int)((int)xPos2), (int)((int)yPos1), (int)((int)yPos2));
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float)((double)limit * 0.01));
    }

    public static void color(int color, float alpha) {
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, alpha);
    }

    public static void bindTexture(int texture) {
        GL11.glBindTexture((int)3553, (int)texture);
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static void scissorRect(float x, float y, float width, double height) {
        ScaledResolution sr = new ScaledResolution(mc);
        int factor = ScaledResolution.getScaleFactor();
        GL11.glScissor((int)((int)(x * (float)factor)), (int)((int)(((double)sr.getScaledHeight() - height) * (double)factor)), (int)((int)((width - x) * (float)factor)), (int)((int)((height - (double)y) * (double)factor)));
    }

    public static void setColor(Color c) {
        GL11.glColor4d((double)((float)c.getRed() / 255.0f), (double)((float)c.getGreen() / 255.0f), (double)((float)c.getBlue() / 255.0f), (double)((float)c.getAlpha() / 255.0f));
    }

    public static void drawSkeetRect(float x, float y, float right, float bottom) {
        RenderUtils.drawRect(x - 46.5f, y - 66.5f, right + 46.5f, bottom + 66.5f, new Color(0, 0, 0, 255).getRGB());
        RenderUtils.drawRect(x - 46.0f, y - 66.0f, right + 46.0f, bottom + 66.0f, new Color(48, 48, 48, 255).getRGB());
        RenderUtils.drawRect(x - 44.5f, y - 64.5f, right + 44.5f, bottom + 64.5f, new Color(33, 33, 33, 255).getRGB());
        RenderUtils.drawRect(x - 43.5f, y - 63.5f, right + 43.5f, bottom + 63.5f, new Color(0, 0, 0, 255).getRGB());
        RenderUtils.drawRect(x - 43.0f, y - 63.0f, right + 43.0f, bottom + 63.0f, new Color(9, 9, 9, 255).getRGB());
        RenderUtils.drawRect(x - 40.5f, y - 60.5f, right + 40.5f, bottom + 60.5f, new Color(48, 48, 48, 255).getRGB());
        RenderUtils.drawRect(x - 40.0f, y - 60.0f, right + 40.0f, bottom + 60.0f, new Color(17, 17, 17, 255).getRGB());
    }

    public static void drawSkeetButton(float x, float y, float right, float bottom) {
        RenderUtils.drawRect(x - 31.0f, y - 43.0f, right + 31.0f, bottom - 30.0f, new Color(0, 0, 0, 255).getRGB());
        RenderUtils.drawRect(x - 30.5f, y - 42.5f, right + 30.5f, bottom - 30.5f, new Color(45, 45, 45, 255).getRGB());
    }

    public static void checkSetupFBO() {
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
        if (fbo != null && fbo.depthBuffer > -1) {
            RenderUtils.setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    public static void drawPolygonParts(double x, double y, float radius, int part, int color, int endcolor, boolean bloom) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        } else {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0.0).color(color).endVertex();
        double TWICE_PI = Math.PI * 2;
        for (int i = part * 90; i <= part * 90 + 90; i += 18) {
            double angle = Math.PI * 2 * (double)i / 360.0 + Math.toRadians(180.0);
            bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(endcolor).endVertex();
        }
        tessellator.draw();
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawCircledTo(float r, int c) {
        GL11.glPushMatrix();
        GlStateManager.glLineWidth(2.0f);
        float theta = 0.0175f;
        float p = (float)Math.cos(theta);
        float s = (float)Math.sin(theta);
        float x = r *= 2.0f;
        float y = 0.0f;
        RenderUtils.enableGL2D();
        GL11.glDisable((int)3008);
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        for (int ii = 0; ii < 90; ++ii) {
            buffer.pos(x, y).color(c).endVertex();
            float t = x;
            x = p * x - s * y;
            y = s * t + p * y;
        }
        tessellator.draw();
        GL11.glScalef((float)2.0f, (float)2.0f, (float)2.0f);
        GL11.glEnable((int)3008);
        RenderUtils.disableGL2D();
        GlStateManager.resetColor();
        GlStateManager.glLineWidth(1.0f);
        GL11.glPopMatrix();
    }

    public static void roundedFullRoundedOutline(float x, float y, float x2, float y2, float round1, float round2, float round3, float round4, int color) {
        GL11.glPushMatrix();
        GL11.glTranslated((double)(x + round1), (double)(y + round1), (double)0.0);
        GL11.glRotated((double)-180.0, (double)0.0, (double)0.0, (double)180.0);
        RenderUtils.drawCircledTo(round1, color);
        RenderUtils.fixShadows();
        GL11.glRotated((double)180.0, (double)0.0, (double)0.0, (double)-180.0);
        GL11.glTranslated((double)(-x - round1), (double)(-y - round1), (double)0.0);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated((double)(x2 - round2), (double)(y + round2), (double)0.0);
        GL11.glRotated((double)-90.0, (double)0.0, (double)0.0, (double)90.0);
        RenderUtils.drawCircledTo(round2, color);
        RenderUtils.fixShadows();
        GL11.glRotated((double)90.0, (double)0.0, (double)0.0, (double)-90.0);
        GL11.glTranslated((double)(-x2 + round2), (double)(-y - round2), (double)0.0);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated((double)(x2 - round3), (double)(y2 - round3), (double)0.0);
        GL11.glRotated((double)-360.0, (double)0.0, (double)0.0, (double)360.0);
        RenderUtils.drawCircledTo(round3, color);
        RenderUtils.fixShadows();
        GL11.glRotated((double)360.0, (double)0.0, (double)0.0, (double)-360.0);
        GL11.glTranslated((double)(-x2 + round3), (double)(-y2 + round3), (double)0.0);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated((double)(x + round4), (double)(y2 - round4), (double)0.0);
        GL11.glRotated((double)-270.0, (double)0.0, (double)0.0, (double)270.0);
        RenderUtils.drawCircledTo(round4, color);
        RenderUtils.fixShadows();
        GL11.glRotated((double)270.0, (double)0.0, (double)0.0, (double)-270.0);
        GL11.glTranslated((double)(-x - round4), (double)(-y2 + round4), (double)0.0);
        GL11.glPopMatrix();
        RenderUtils.drawAlphedRect(x + round1 - 1.0f, y - 0.5f, x2 - round2, y + 0.5f, color);
        RenderUtils.drawAlphedRect(x2 - 0.5f, y + round2 - 1.0f, x2 + 0.5f, y2 - round3, color);
        RenderUtils.drawAlphedRect(x - 0.5f, y + round1, x + 0.5f, y2 - round4, color);
        RenderUtils.drawAlphedRect(x + round4, y2 - 0.5f, x2 - round3 + 1.0f, y2 + 0.5f, color);
    }

    public static void drawAlphedVGradient(double x, double y, double x2, double y2, int col1, int col2) {
        RenderUtils.drawAlphedVGradient(x, y, x2, y2, col1, col2, false);
    }

    public static void drawAlphedVGradient(double x, double y, double x2, double y2, int col1, int col2, boolean bloom) {
        float f = (float)(col1 >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(col1 >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(col1 >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(col1 & 0xFF) / 255.0f;
        float f4 = (float)(col2 >> 24 & 0xFF) / 255.0f;
        float f5 = (float)(col2 >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(col2 >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(col2 & 0xFF) / 255.0f;
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)(bloom ? 32772 : 771));
        GL11.glEnable((int)2848);
        GL11.glShadeModel((int)7425);
        GL11.glPushMatrix();
        GL11.glDisable((int)3008);
        GL11.glBegin((int)7);
        GL11.glColor4f((float)f1, (float)f2, (float)f3, (float)f);
        GL11.glVertex2d((double)x2, (double)y);
        GL11.glVertex2d((double)x, (double)y);
        GL11.glColor4f((float)f5, (float)f6, (float)f7, (float)f4);
        GL11.glVertex2d((double)x, (double)y2);
        GL11.glVertex2d((double)x2, (double)y2);
        GL11.glEnd();
        if (bloom) {
            GL11.glBlendFunc((int)770, (int)771);
        }
        GL11.glEnable((int)3008);
        GL11.glPopMatrix();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2848);
        GL11.glShadeModel((int)7424);
    }

    public static void drawRoundedFullGradientRectPro(float x, float y, float x2, float y2, float round, int color, int color2, int color3, int color4, boolean bloom) {
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        RenderUtils.drawFullGradientRectPro(x + round / 2.0f, y + round / 2.0f, x2 - round / 2.0f, y2 - round / 2.0f, color4, color3, color2, color, bloom);
        RenderUtils.drawPolygonParts(x + round / 2.0f, y + round / 2.0f, round / 2.0f, 0, color, color, bloom);
        RenderUtils.drawPolygonParts(x + round / 2.0f, y2 - round / 2.0f, round / 2.0f, 5, color4, color4, bloom);
        RenderUtils.drawPolygonParts(x2 - round / 2.0f, y + round / 2.0f, round / 2.0f, 7, color2, color2, bloom);
        RenderUtils.drawPolygonParts(x2 - round / 2.0f, y2 - round / 2.0f, round / 2.0f, 6, color3, color3, bloom);
        RenderUtils.drawFullGradientRectPro(x, y + round / 2.0f, x + round / 2.0f, y2 - round / 2.0f, color4, color4, color, color, bloom);
        RenderUtils.drawFullGradientRectPro(x + round / 2.0f, y, x2 - round / 2.0f, y + round / 2.0f, color, color2, color2, color, bloom);
        RenderUtils.drawFullGradientRectPro(x2 - round / 2.0f, y + round / 2.0f, x2, y2 - round / 2.0f, color3, color3, color2, color2, bloom);
        RenderUtils.drawFullGradientRectPro(x + round / 2.0f, y2 - round / 2.0f, x2 - round / 2.0f, y2, color4, color3, color3, color4, bloom);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    }

    public static void drawGlichRect3OBF(double x, double y, double width, double height, int color, int twocolor) {
        if (x < width) {
            float i = (float)x;
            x = width;
            width = i;
        }
        if (y < height) {
            float j = (float)y;
            y = height;
            height = j;
        }
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float alpha2 = (float)(twocolor >> 24 & 0xFF) / 255.0f;
        float red2 = (float)(twocolor >> 16 & 0xFF) / 255.0f;
        float green2 = (float)(twocolor >> 8 & 0xFF) / 255.0f;
        float blue2 = (float)(twocolor & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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
            width = i;
        }
        if (y < height) {
            float j = (float)y;
            y = height;
            height = j;
        }
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        float alpha2 = (float)(twocolor >> 24 & 0xFF) / 255.0f;
        float red2 = (float)(twocolor >> 16 & 0xFF) / 255.0f;
        float green2 = (float)(twocolor >> 8 & 0xFF) / 255.0f;
        float blue2 = (float)(twocolor & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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
        float alpha1 = (float)(endcolor >> 24 & 0xFF) / 255.0f;
        float red1 = (float)(endcolor >> 16 & 0xFF) / 255.0f;
        float green1 = (float)(endcolor >> 8 & 0xFF) / 255.0f;
        float blue1 = (float)(endcolor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        double TWICE_PI = Math.PI * 2;
        for (int i = part * 90; i <= part * 90 + 90; i += 6) {
            double angle = Math.PI * 2 * (double)i / 360.0 + Math.toRadians(180.0);
            bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(red1, green1, blue1, alpha1).endVertex();
        }
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void customScaledObject2D(float oXpos, float oYpos, float oWidth, float oHeight, float oScale) {
        GL11.glTranslated((double)(oWidth / 2.0f), (double)(oHeight / 2.0f), (double)1.0);
        GL11.glTranslated((double)(-oXpos * oScale + oXpos + oWidth / 2.0f * -oScale), (double)(-oYpos * oScale + oYpos + oHeight / 2.0f * -oScale), (double)1.0);
        GL11.glScaled((double)oScale, (double)oScale, (double)0.0);
    }

    public static void customScaledObject2DCoords(float oXpos, float oYpos, float oXpos2, float oYpos2, float oScale) {
        RenderUtils.customScaledObject2D(oXpos, oYpos, oXpos2 - oXpos, oYpos2 - oYpos, oScale);
    }

    public static void customScaledObject2DPro(float oXpos, float oYpos, float oWidth, float oHeight, float oScaleX, float oScaleY) {
        GL11.glTranslated((double)(oWidth / 2.0f), (double)(oHeight / 2.0f), (double)1.0);
        GL11.glTranslated((double)(-oXpos * oScaleX + oXpos + oWidth / 2.0f * -oScaleX), (double)(-oYpos * oScaleY + oYpos + oHeight / 2.0f * -oScaleY), (double)1.0);
        GL11.glScaled((double)oScaleX, (double)oScaleY, (double)0.0);
    }

    public static void customRotatedObject2D(float oXpos, float oYpos, float oWidth, float oHeight, double rotate) {
        GL11.glTranslated((double)(oXpos + oWidth / 2.0f), (double)(oYpos + oHeight / 2.0f), (double)0.0);
        GL11.glRotated((double)rotate, (double)0.0, (double)0.0, (double)1.0);
        GL11.glTranslated((double)(-oXpos - oWidth / 2.0f), (double)(-oYpos - oHeight / 2.0f), (double)0.0);
    }

    public static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT((int)fbo.depthBuffer);
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT((int)36161, (int)stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT((int)36161, (int)34041, (int)Minecraft.getMinecraft().displayWidth, (int)Minecraft.getMinecraft().displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT((int)36160, (int)36128, (int)36161, (int)stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT((int)36160, (int)36096, (int)36161, (int)stencil_depth_buffer_ID);
    }

    public static void drawRect(double x, double y, double d, double e, int color) {
        RenderUtils.glRenderStart();
        RenderUtils.glColor(color);
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)x, (double)y);
        GL11.glVertex2d((double)d, (double)y);
        GL11.glVertex2d((double)d, (double)e);
        GL11.glVertex2d((double)x, (double)e);
        GL11.glEnd();
        RenderUtils.glRenderStop();
    }

    public static void drawLineH(float xPos, float yPos, float y2Pos, float w, int color, int color2) {
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)3008);
        GL11.glShadeModel((int)7425);
        GL11.glLineWidth((float)(w * (float)RenderUtils.mc.gameSettings.guiScale));
        GL11.glBegin((int)1);
        RenderUtils.glColor(color);
        GL11.glVertex2d((double)(xPos + w / 2.0f), (double)yPos);
        RenderUtils.glColor(color2);
        GL11.glVertex2d((double)(xPos + w / 2.0f), (double)y2Pos);
        GL11.glEnd();
        GL11.glShadeModel((int)7424);
        GL11.glLineWidth((float)1.0f);
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2884);
        GL11.glColor4d((double)1.0, (double)1.0, (double)1.0, (double)1.0);
    }

    public static void drawLightContureRect(double x, double y, double x2, double y2, int color) {
        RenderUtils.drawAlphedRect(x - 0.5, y - 0.5, x2 + 0.5, y, color);
        RenderUtils.drawAlphedRect(x - 0.5, y2, x2 + 0.5, y2 + 0.5, color);
        RenderUtils.drawAlphedRect(x - 0.5, y, x, y2, color);
        RenderUtils.drawAlphedRect(x2, y, x2 + 0.5, y2, color);
    }

    public static void drawLightContureRectSmooth(double x, double y, double x2, double y2, int color) {
        RenderUtils.drawAlphedRect(x, y - 0.5, x2, y, color);
        RenderUtils.drawAlphedRect(x, y2, x2, y2 + 0.5, color);
        RenderUtils.drawAlphedRect(x - 0.5, y, x, y2, color);
        RenderUtils.drawAlphedRect(x2, y, x2 + 0.5, y2, color);
    }

    public static void drawLightContureRectSidewaysSmooth(double x, double y, double x2, double y2, int color, int color2) {
        RenderUtils.drawAlphedSideways(x, y - 0.5, x2, y, color, color2);
        RenderUtils.drawAlphedSideways(x, y2, x2, y2 + 0.5, color, color2);
        RenderUtils.drawAlphedRect(x - 0.5, y, x, y2, color);
        RenderUtils.drawAlphedRect(x2, y, x2 + 0.5, y2, color2);
    }

    public static void drawLightContureRectFullGradient(float x, float y, float x2, float y2, int c1, int c2, boolean bloom) {
        RenderUtils.drawFullGradientRectPro(x - 0.5f, y - 0.5f, x2 + 0.5f, y, c1, c2, c2, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x - 0.5f, y2, x2 + 0.5f, y2 + 0.5f, c2, c1, c1, c2, bloom);
        RenderUtils.drawFullGradientRectPro(x - 0.5f, y, x, y2, c2, c2, c1, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x2, y, x2 + 0.5f, y2, c1, c1, c2, c2, bloom);
    }

    public static void drawLightContureRectFullGradient(float x, float y, float x2, float y2, int c1, int c2, int c3, int c4, boolean bloom) {
        RenderUtils.drawFullGradientRectPro(x - 0.5f, y - 0.5f, x2 + 0.5f, y, c1, c2, c2, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x - 0.5f, y2, x2 + 0.5f, y2 + 0.5f, c4, c3, c3, c4, bloom);
        RenderUtils.drawFullGradientRectPro(x - 0.5f, y, x, y2, c4, c4, c1, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x2, y, x2 + 0.5f, y2, c3, c3, c2, c2, bloom);
    }

    public static void drawLightContureRectSmoothFullGradient(float x, float y, float x2, float y2, int c1, int c2, int c3, int c4, boolean bloom) {
        RenderUtils.drawFullGradientRectPro(x, y - 0.5f, x2, y, c1, c2, c2, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x, y2, x2, y2 + 0.5f, c4, c3, c3, c4, bloom);
        RenderUtils.drawFullGradientRectPro(x - 0.5f, y, x, y2, c4, c4, c1, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x2, y, x2 + 0.5f, y2, c3, c3, c2, c2, bloom);
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
        RenderUtils.glRenderStart();
        GL11.glDisable((int)3008);
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y).color(color).endVertex();
        buffer.pos(d, y).color(color).endVertex();
        buffer.pos(d, e).color(color).endVertex();
        buffer.pos(x, e).color(color).endVertex();
        tessellator.draw();
        GL11.glEnable((int)3008);
        RenderUtils.glRenderStop();
    }

    public static void drawAlphedRectWithBloom(double x, double y, double x2, double y2, int color, boolean bloom) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(7424);
        GL11.glDisable((int)3008);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        } else {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y2).color(color).endVertex();
        buffer.pos(x2, y2).color(color).endVertex();
        buffer.pos(x2, y).color(color).endVertex();
        buffer.pos(x, y).color(color).endVertex();
        tessellator.draw();
        GL11.glEnable((int)3008);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.shadeModel(7424);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void drawAlphedGradientRectWithBloom(double x, double y, double x2, double y2, int color, int color2, boolean bloom) {
        RenderUtils.glRenderStart();
        GL11.glDisable((int)3008);
        GL11.glShadeModel((int)7425);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y2).color(color2).endVertex();
        buffer.pos(x2, y2).color(color2).endVertex();
        buffer.pos(x2, y).color(color).endVertex();
        buffer.pos(x, y).color(color).endVertex();
        tessellator.draw();
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3008);
        RenderUtils.glRenderStop();
    }

    public static void drawAlphedGradient(double x, double y, double x2, double y2, int col1, int col2) {
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glShadeModel((int)7425);
        GL11.glPushMatrix();
        GL11.glDisable((int)3008);
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x2, y).color(col1).endVertex();
        buffer.pos(x, y).color(col1).endVertex();
        buffer.pos(x, y2).color(col2).endVertex();
        buffer.pos(x2, y2).color(col2).endVertex();
        tessellator.draw();
        GL11.glEnable((int)3008);
        GL11.glPopMatrix();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glShadeModel((int)7424);
    }

    public static void drawShadowRect(double startX, double startY, double endX, double endY, int radius) {
        RenderUtils.drawGradientRect(startX, startY - (double)radius, endX, startY, false, true, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
        RenderUtils.drawGradientRect(startX, endY, endX, endY + (double)radius, false, false, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
        RenderUtils.drawSector2(endX, endY, 0, 90, radius);
        RenderUtils.drawSector2(endX, startY, 90, 180, radius);
        RenderUtils.drawSector2(startX, startY, 180, 270, radius);
        RenderUtils.drawSector2(startX, endY, 270, 360, radius);
        RenderUtils.drawGradientRect(startX - (double)radius, startY, startX, endY, true, true, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
        RenderUtils.drawGradientRect(endX, startY, endX + (double)radius, endY, true, false, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 0).getRGB());
    }

    public static void drawFullGradientShadowRectColored(double startX, double startY, double endX, double endY, float radius, int color1, int color2, int color3, int color4, int alpha, boolean bloom) {
        RenderUtils.drawFullGradientRectPro((float)startX, (float)startY - radius, (float)startX + ((float)endX - (float)startX) / 2.0f, (float)startY, color1, ColorUtils.getOverallColorFrom(color1, color2), ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color1, color2), 0.0f), ColorUtils.swapAlpha(color1, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro((float)startX + ((float)endX - (float)startX) / 2.0f, (float)startY - radius, (float)endX, (float)startY, ColorUtils.getOverallColorFrom(color1, color2), color2, ColorUtils.swapAlpha(color2, 0.0f), ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color1, color2), 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro((float)startX, (float)endY, (float)startX + ((float)endX - (float)startX) / 2.0f, (float)endY + radius, ColorUtils.swapAlpha(color4, 0.0f), ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color4), 0.0f), ColorUtils.getOverallColorFrom(color3, color4), color4, bloom);
        RenderUtils.drawFullGradientRectPro((float)startX + ((float)endX - (float)startX) / 2.0f, (float)endY, (float)endX, (float)endY + radius, ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color4), 0.0f), ColorUtils.swapAlpha(color3, 0.0f), color3, ColorUtils.getOverallColorFrom(color3, color4), bloom);
        RenderUtils.drawSector4(endX, endY, 0, 90, radius, color3, alpha, bloom);
        RenderUtils.drawSector4(endX, startY, 90, 180, radius, color2, alpha, bloom);
        RenderUtils.drawSector4(startX, startY, 180, 270, radius, color1, alpha, bloom);
        RenderUtils.drawSector4(startX, endY, 270, 360, radius, color4, alpha, bloom);
        RenderUtils.drawFullGradientRectPro((float)startX - radius, (float)startY, (float)startX, (float)startY + ((float)endY - (float)startY) / 2.0f, ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color4, color1), 0.0f), ColorUtils.getOverallColorFrom(color4, color1), color1, ColorUtils.swapAlpha(color1, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro((float)startX - radius, (float)startY + ((float)endY - (float)startY) / 2.0f, (float)startX, (float)endY, ColorUtils.swapAlpha(color4, 0.0f), color4, ColorUtils.getOverallColorFrom(color4, color1), ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color4, color1), 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro((float)endX, (float)startY, (float)endX + radius, (float)startY + ((float)endY - (float)startY) / 2.0f, ColorUtils.getOverallColorFrom(color3, color2), ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color2), 0.0f), ColorUtils.swapAlpha(color2, 0.0f), color2, bloom);
        RenderUtils.drawFullGradientRectPro((float)endX, (float)startY + ((float)endY - (float)startY) / 2.0f, (float)endX + radius, (float)endY, color3, ColorUtils.swapAlpha(color3, 0.0f), ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(color3, color2), 0.0f), ColorUtils.getOverallColorFrom(color3, color2), bloom);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void drawBloomedFullShadowFullGradientRect(float xpos, float ypos, float x2pos, float y2pos, float radius, int color1, int color2, int color3, int color4, int alpha, boolean bloom) {
        float x = xpos;
        float y = ypos;
        float w = x2pos - xpos;
        float h = y2pos - ypos;
        int colorid1 = ColorUtils.swapAlpha(color1, alpha);
        int colorid2 = ColorUtils.swapAlpha(color2, alpha);
        int colorid3 = ColorUtils.swapAlpha(color3, alpha);
        int colorid4 = ColorUtils.swapAlpha(color4, alpha);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        RenderUtils.drawFullGradientShadowRectColored(x, y, x + w, y + h, radius, colorid1, colorid2, colorid3, colorid4, alpha, bloom);
        RenderUtils.drawFullGradientRectPro(x, y, x + w / 2.0f, y + h / 2.0f, ColorUtils.getOverallColorFrom(colorid1, colorid4), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid1, colorid2), colorid1, bloom);
        RenderUtils.drawFullGradientRectPro(x, y + h / 2.0f, x + w / 2.0f, y + h, colorid4, ColorUtils.getOverallColorFrom(colorid3, colorid4), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid1, colorid4), bloom);
        RenderUtils.drawFullGradientRectPro(x + w / 2.0f, y + h / 2.0f, x + w, y + h, ColorUtils.getOverallColorFrom(colorid3, colorid4), colorid3, ColorUtils.getOverallColorFrom(colorid3, colorid2), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), bloom);
        RenderUtils.drawFullGradientRectPro(x + w / 2.0f, y, x + w, y + h / 2.0f, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid3, colorid2), colorid2, ColorUtils.getOverallColorFrom(colorid2, colorid1), bloom);
    }

    public static void drawBloomedFullShadowFullGradientRectBool(float xpos, float ypos, float x2pos, float y2pos, float radius, int color1, int color2, int color3, int color4, int alpha, boolean bloom, boolean rect, boolean shadow) {
        float x = xpos;
        float y = ypos;
        float w = x2pos - xpos;
        float h = y2pos - ypos;
        int colorid1 = ColorUtils.swapAlpha(color1, alpha);
        int colorid2 = ColorUtils.swapAlpha(color2, alpha);
        int colorid3 = ColorUtils.swapAlpha(color3, alpha);
        int colorid4 = ColorUtils.swapAlpha(color4, alpha);
        if (shadow) {
            RenderUtils.drawFullGradientShadowRectColored(x, y, x + w, y + h, radius, colorid1, colorid2, colorid3, colorid4, alpha, bloom);
        }
        if (rect) {
            RenderUtils.drawFullGradientRectPro(x, y, x + w / 2.0f, y + h / 2.0f, ColorUtils.getOverallColorFrom(colorid1, colorid4), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid1, colorid2), colorid1, bloom);
            RenderUtils.drawFullGradientRectPro(x, y + h / 2.0f, x + w / 2.0f, y + h, colorid4, ColorUtils.getOverallColorFrom(colorid3, colorid4), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid1, colorid4), bloom);
            RenderUtils.drawFullGradientRectPro(x + w / 2.0f, y + h / 2.0f, x + w, y + h, ColorUtils.getOverallColorFrom(colorid3, colorid4), colorid3, ColorUtils.getOverallColorFrom(colorid3, colorid2), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), bloom);
            RenderUtils.drawFullGradientRectPro(x + w / 2.0f, y, x + w, y + h / 2.0f, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid3, colorid2), colorid2, ColorUtils.getOverallColorFrom(colorid2, colorid1), bloom);
        }
    }

    public static void drawBloomedFullShadowFullGradientRectBool(float xpos, float ypos, float x2pos, float y2pos, float radius, int color1, int color2, int color3, int color4, int alphaRect, int alphaGlow, boolean bloom, boolean rect, boolean shadow) {
        float x = xpos;
        float y = ypos;
        float w = x2pos - xpos;
        float h = y2pos - ypos;
        int colorid1 = ColorUtils.swapAlpha(color1, alphaRect);
        int colorid2 = ColorUtils.swapAlpha(color2, alphaRect);
        int colorid3 = ColorUtils.swapAlpha(color3, alphaRect);
        int colorid4 = ColorUtils.swapAlpha(color4, alphaRect);
        int colorid5 = ColorUtils.swapAlpha(color1, alphaGlow);
        int colorid6 = ColorUtils.swapAlpha(color2, alphaGlow);
        int colorid7 = ColorUtils.swapAlpha(color3, alphaGlow);
        int colorid8 = ColorUtils.swapAlpha(color4, alphaGlow);
        if (shadow) {
            RenderUtils.drawFullGradientShadowRectColored(x, y, x + w, y + h, radius, colorid5, colorid6, colorid7, colorid8, alphaGlow, bloom);
        }
        if (rect) {
            RenderUtils.drawFullGradientRectPro(x, y, x + w / 2.0f, y + h / 2.0f, ColorUtils.getOverallColorFrom(colorid1, colorid4), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid1, colorid2), colorid1, bloom);
            RenderUtils.drawFullGradientRectPro(x, y + h / 2.0f, x + w / 2.0f, y + h, colorid4, ColorUtils.getOverallColorFrom(colorid3, colorid4), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid1, colorid4), bloom);
            RenderUtils.drawFullGradientRectPro(x + w / 2.0f, y + h / 2.0f, x + w, y + h, ColorUtils.getOverallColorFrom(colorid3, colorid4), colorid3, ColorUtils.getOverallColorFrom(colorid3, colorid2), ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), bloom);
            RenderUtils.drawFullGradientRectPro(x + w / 2.0f, y, x + w, y + h / 2.0f, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid3, ColorUtils.getOverallColorFrom(colorid1, colorid2))), ColorUtils.getOverallColorFrom(colorid3, colorid2), colorid2, ColorUtils.getOverallColorFrom(colorid2, colorid1), bloom);
        }
    }

    public static void drawRoundedFullGradientOutsideShadow(float x, float y, float x2, float y2, float round, float shadowSize, int color, int color2, int color3, int color4, boolean bloom) {
        x += round;
        x2 -= round;
        y += round;
        y2 -= round;
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        RenderUtils.drawCroneShadow(x, y, -180, -90, round, shadowSize, color, bloom ? 0 : ColorUtils.swapAlpha(color, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x, y - round - shadowSize, x2, y - round, color, color2, bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0f), bloom ? 0 : ColorUtils.swapAlpha(color, 0.0f), bloom);
        RenderUtils.drawCroneShadow(x2, y, 90, 180, round, shadowSize, color2, bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x2 + round, y, x2 + round + shadowSize, y2, color3, bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0f), bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0f), color2, bloom);
        RenderUtils.drawCroneShadow(x2, y2, 0, 90, round, shadowSize, color3, bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x, y2 + round, x2, y2 + round + shadowSize, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0f), bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0f), color3, color4, bloom);
        RenderUtils.drawCroneShadow(x, y2, -90, 0, round, shadowSize, color4, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x - round - shadowSize, y, x - round, y2, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0f), color4, color, bloom ? 0 : ColorUtils.swapAlpha(color, 0.0f), bloom);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    }

    public static void drawInsideFullRoundedFullGradientShadowRectWithBloomBool(float x, float y, float x2, float y2, float round, float shadowSize, int c1, int c2, int c3, int c4, boolean bloom) {
        RenderUtils.drawCroneShadow(x + shadowSize + round, y + shadowSize + round, -180, -90, round, shadowSize, 0, c1, bloom);
        RenderUtils.drawCroneShadow(x2 - shadowSize - round, y + shadowSize + round, -270, -180, round, shadowSize, 0, c2, bloom);
        RenderUtils.drawCroneShadow(x2 - shadowSize - round, y2 - shadowSize - round, 0, 90, round, shadowSize, 0, c3, bloom);
        RenderUtils.drawCroneShadow(x + shadowSize + round, y2 - shadowSize - round, -90, 0, round, shadowSize, 0, c4, bloom);
        RenderUtils.drawFullGradientRectPro(x + shadowSize + round, y, x2 - shadowSize - round, y + shadowSize, 0, 0, c2, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x + shadowSize + round, y2 - shadowSize, x2 - shadowSize - round, y2, c4, c3, 0, 0, bloom);
        RenderUtils.drawFullGradientRectPro(x, y + shadowSize + round, x + shadowSize, y2 - shadowSize - round, c4, 0, 0, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x2 - shadowSize, y + shadowSize + round, x2, y2 - shadowSize - round, 0, c3, c2, 0, bloom);
    }

    public static void drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBoolShadowsBoolChangeShadowSize(float x, float y, float x2, float y2, float round, float shadowSizeInside, float shadowSizeOutside, int c1, int c2, int c3, int c4, boolean bloom, boolean insideShadow, boolean outsideShadow) {
        if (insideShadow) {
            RenderUtils.drawInsideFullRoundedFullGradientShadowRectWithBloomBool(x, y, x2, y2, round, shadowSizeInside, c1, c2, c3, c4, bloom);
        }
        if (outsideShadow) {
            RenderUtils.drawRoundedFullGradientOutsideShadow(x, y, x2, y2, round + round / 2.0f, shadowSizeOutside, c1, c2, c3, c4, bloom);
        }
    }

    public static void drawFullGradientFullsideShadowRectWithBloomBool(float x, float y, float x2, float y2, float shadowSize, int c1, int c2, int c3, int c4, boolean bloom) {
        RenderUtils.drawFullGradientRectPro(x, y, x + shadowSize, y + shadowSize, c1, 0, c1, c1, bloom);
        RenderUtils.customRotatedObject2D(x2 - shadowSize, y, shadowSize, shadowSize, 90.0);
        RenderUtils.drawFullGradientRectPro(x2 - shadowSize, y, x2, y + shadowSize, c2, 0, c2, c2, bloom);
        RenderUtils.customRotatedObject2D(x2 - shadowSize, y, shadowSize, shadowSize, -90.0);
        RenderUtils.drawFullGradientRectPro(x2 - shadowSize, y2 - shadowSize, x2, y2, c3, c3, c3, 0, bloom);
        RenderUtils.customRotatedObject2D(x, y2 - shadowSize, shadowSize, shadowSize, 90.0);
        RenderUtils.drawFullGradientRectPro(x, y2 - shadowSize, x + shadowSize, y2, c4, c4, c4, 0, bloom);
        RenderUtils.customRotatedObject2D(x, y2 - shadowSize, shadowSize, shadowSize, -90.0);
        RenderUtils.drawFullGradientRectPro(x + shadowSize, y, x2 - shadowSize, y + shadowSize, 0, 0, c2, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x, y + shadowSize, x + shadowSize, y2 - shadowSize, c4, 0, 0, c1, bloom);
        RenderUtils.drawFullGradientRectPro(x + shadowSize, y2 - shadowSize, x2 - shadowSize, y2, c4, c3, 0, 0, bloom);
        RenderUtils.drawFullGradientRectPro(x2 - shadowSize, y + shadowSize, x2, y2 - shadowSize, 0, c3, c2, 0, bloom);
        RenderUtils.drawRoundedFullGradientOutsideShadow(x, y, x2, y2, 0.0f, shadowSize, c1, c2, c3, c4, bloom);
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
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        endColor = ColorUtils.swapAlpha(endColor, 0.0f);
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

    public static void drawGradientRect2(double startX, double startY, double endX, double endY, boolean sideways, boolean reversed, int startColor, int endColor, int alpha) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GL11.glDisable((int)3008);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.shadeModel(7425);
        startColor = ColorUtils.swapAlpha(startColor, alpha);
        endColor = ColorUtils.swapAlpha(endColor, 0.0f);
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
        GL11.glEnable((int)3008);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(7424);
    }

    public static void drawSector2(double x, double y, int startAngle, int endAngle, int radius) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y).color(0.0f, 0.0f, 0.0f, 0.4f).endVertex();
        for (int i = startAngle; i <= endAngle; i += 6) {
            buffer.pos(x + Math.sin((double)i * Math.PI / 180.0) * (double)radius, y + Math.cos((double)i * Math.PI / 180.0) * (double)radius).color(0).endVertex();
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
        GL11.glDisable((int)3008);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.shadeModel(7425);
        buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y).color(ColorUtils.swapAlpha(color, alpha)).endVertex();
        for (int i = startAngle; i <= endAngle; i += 6) {
            buffer.pos(x + Math.sin((double)i * Math.PI / 180.0) * (double)radius, y + Math.cos((double)i * Math.PI / 180.0) * (double)radius).color(0).endVertex();
        }
        tessellator.draw();
        GL11.glEnd();
        GL11.glEnable((int)3008);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(7424);
    }

    public static void drawSector4(double x, double y, int startAngle, int endAngle, float radius, int color, int alpha, boolean bloom) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)3008);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        }
        GlStateManager.shadeModel(7425);
        buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y).color(ColorUtils.swapAlpha(color, alpha)).endVertex();
        for (int i = startAngle; i <= endAngle; i += 30) {
            buffer.pos(x + Math.sin((double)i * Math.PI / 180.0) * (double)radius, y + Math.cos((double)i * Math.PI / 180.0) * (double)radius).color(ColorUtils.swapAlpha(color, 0.0f)).endVertex();
        }
        tessellator.draw();
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        }
        GL11.glEnable((int)3008);
        GL11.glDepthMask((boolean)true);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(7424);
    }

    public static void drawCroneShadow(double x, double y, int startAngle, int endAngle, float radius, float shadowSize, int color, int endColor, boolean bloom) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(7425);
        GL11.glDisable((int)3008);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
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
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        }
        GL11.glEnable((int)3008);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7424);
        GlStateManager.resetColor();
    }

    public static void drawRoundedFullGradientInsideShadow(float x, float y, float x2, float y2, float round, int color, int color2, int color3, int color4, boolean bloom) {
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        float rd = round / 2.0f;
        RenderUtils.drawPolygonParts(x + rd, y + rd, rd, 0, 0, color, bloom);
        RenderUtils.drawPolygonParts(x + rd, y2 - rd, rd, 5, 0, color4, bloom);
        RenderUtils.drawPolygonParts(x2 - rd, y + rd, rd, 7, 0, color2, bloom);
        RenderUtils.drawPolygonParts(x2 - rd, y2 - rd, rd, 6, 0, color3, bloom);
        RenderUtils.drawFullGradientRectPro(x, y + rd, x + rd, y2 - rd, color4, 0, 0, color, bloom);
        RenderUtils.drawFullGradientRectPro(x + rd, y, x2 - rd, y + rd, 0, 0, color2, color, bloom);
        RenderUtils.drawFullGradientRectPro(x2 - rd, y + rd, x2, y2 - rd, 0, color3, color2, 0, bloom);
        RenderUtils.drawFullGradientRectPro(x + rd, y2 - rd, x2 - rd, y2, color4, color3, 0, 0, bloom);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    }

    public static void drawRoundedShadow(float x, float y, float x2, float y2, float round, float shadowSize, int color, boolean bloom) {
        x += round;
        x2 -= round;
        y += round;
        y2 -= round;
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        RenderUtils.drawCroneShadow(x, y, -180, -90, round, shadowSize, color, 0, bloom);
        RenderUtils.drawFullGradientRectPro(x, y - round - shadowSize, x2, y - round, color, color, 0, 0, bloom);
        RenderUtils.drawCroneShadow(x2, y, 90, 180, round, shadowSize, color, 0, bloom);
        RenderUtils.drawFullGradientRectPro(x2 + round, y, x2 + round + shadowSize, y2, color, 0, 0, color, bloom);
        RenderUtils.drawCroneShadow(x2, y2, 0, 90, round, shadowSize, color, 0, bloom);
        RenderUtils.drawFullGradientRectPro(x, y2 + round, x2, y2 + round + shadowSize, 0, 0, color, color, bloom);
        RenderUtils.drawCroneShadow(x, y2, -90, 0, round, shadowSize, color, 0, bloom);
        RenderUtils.drawFullGradientRectPro(x - round - shadowSize, y, x - round, y2, 0, color, color, 0, bloom);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    }

    public static void drawRoundedFullGradientShadow(float x, float y, float x2, float y2, float round, float shadowSize, int color, int color2, int color3, int color4, boolean bloom) {
        x += round;
        x2 -= round;
        y += round;
        y2 -= round;
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)2929);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        RenderUtils.drawCroneShadow(x, y, -180, -90, round, shadowSize, color, bloom ? 0 : ColorUtils.swapAlpha(color, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x, y - round - shadowSize, x2, y - round, color, color2, bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0f), bloom ? 0 : ColorUtils.swapAlpha(color, 0.0f), bloom);
        RenderUtils.drawCroneShadow(x2, y, 90, 180, round, shadowSize, color2, bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x2 + round, y, x2 + round + shadowSize, y2, color3, bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0f), bloom ? 0 : ColorUtils.swapAlpha(color2, 0.0f), color2, bloom);
        RenderUtils.drawCroneShadow(x2, y2, 0, 90, round, shadowSize, color3, bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x, y2 + round, x2, y2 + round + shadowSize, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0f), bloom ? 0 : ColorUtils.swapAlpha(color3, 0.0f), color3, color4, bloom);
        RenderUtils.drawCroneShadow(x, y2, -90, 0, round, shadowSize, color4, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0f), bloom);
        RenderUtils.drawFullGradientRectPro(x - round - shadowSize, y, x - round, y2, bloom ? 0 : ColorUtils.swapAlpha(color4, 0.0f), color4, color, bloom ? 0 : ColorUtils.swapAlpha(color, 0.0f), bloom);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GL11.glEnable((int)2929);
    }

    public static void drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(float x, float y, float x2, float y2, float round, float shadowSize, int color, int color2, int color3, int color4, boolean bloom, boolean rect, boolean shadow) {
        GlStateManager.disableDepth();
        if (shadow) {
            RenderUtils.drawRoundedFullGradientShadow(x, y, x2, y2, round, shadowSize, color, color2, color3, color4, bloom);
        }
        if (rect) {
            RenderUtils.drawRoundedFullGradientRectPro(x, y, x2, y2, round * 2.0f, color, color2, color3, color4, bloom);
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    public static void drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(float x, float y, float x2, float y2, float round, int color, int color2, int color3, int color4, boolean bloom) {
        GlStateManager.disableDepth();
        RenderUtils.drawRoundedFullGradientShadow(x, y, x2, y2, round, round, color, color2, color3, color4, bloom);
        RenderUtils.drawRoundedFullGradientInsideShadow(x, y, x2, y2, round * 2.0f, color, color2, color3, color4, bloom);
        GlStateManager.enableDepth();
    }

    public static void smoothAngleRect(float xPos, float yPos, float x2Pos, float y2Pos, int color) {
        RenderUtils.drawRect(xPos + 3.0f, yPos - 3.0f, x2Pos - 3.0f, yPos - 2.5f, color);
        RenderUtils.drawRect(xPos + 2.0f, yPos - 2.5f, x2Pos - 2.0f, yPos - 2.0f, color);
        RenderUtils.drawRect(xPos + 1.5f, yPos - 2.0f, x2Pos - 1.5f, yPos - 1.5f, color);
        RenderUtils.drawRect(xPos + 1.0f, yPos - 1.5f, x2Pos - 1.0f, yPos - 1.0f, color);
        RenderUtils.drawRect(xPos + 0.5f, yPos - 1.0f, x2Pos - 0.5f, yPos, color);
        RenderUtils.drawRect(xPos, yPos, x2Pos, y2Pos, color);
        RenderUtils.drawRect(xPos + 2.0f, y2Pos + 2.5f, x2Pos - 2.0f, y2Pos + 2.0f, color);
        RenderUtils.drawRect(xPos + 1.5f, y2Pos + 2.0f, x2Pos - 1.5f, y2Pos + 1.5f, color);
        RenderUtils.drawRect(xPos + 1.0f, y2Pos + 1.5f, x2Pos - 1.0f, y2Pos + 1.0f, color);
        RenderUtils.drawRect(xPos + 0.5f, y2Pos + 1.0f, x2Pos - 0.5f, y2Pos, color);
        RenderUtils.drawRect(xPos + 3.0f, y2Pos + 3.0f, x2Pos - 3.0f, y2Pos + 2.5f, color);
    }

    public static void color(int argb) {
        float alpha = (float)(argb >> 24 & 0xFF) / 255.0f;
        float red = (float)(argb >> 16 & 0xFF) / 255.0f;
        float green = (float)(argb >> 8 & 0xFF) / 255.0f;
        float blue = (float)(argb & 0xFF) / 255.0f;
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
    }

    public static int glColor(int color) {
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        return color;
    }

    public static void drawPenisOnEntity(EntityPlayer player, double x, double y, double z) {
        if (player.isChild() || player.isInvisible() || player instanceof EntityPlayerSP) {
            return;
        }
        int c1 = ColorUtils.getColor(191, 123, 67);
        int c2 = ColorUtils.getColor(199, 111, 67);
        int c3 = ColorUtils.getColor(198, 73, 99);
        GL11.glPushMatrix();
        GL11.glDisable((int)2896);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glDepthMask((boolean)true);
        GL11.glLineWidth((float)1.0f);
        GL11.glTranslated((double)x, (double)y, (double)z);
        GL11.glRotatef((float)(-(player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * mc.getRenderPartialTicks())), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslated((double)0.0, (double)0.1f, (double)(player.isSneaking() ? (double)0.2f : 0.5));
        GL11.glRotated((double)20.0, (double)-1.0, (double)0.0, (double)0.0);
        GL11.glTranslated((double)x, (double)(y + (double)(player.height / 2.0f) - (double)0.225f), (double)z);
        RenderUtils.glColor(c1);
        GL11.glLineWidth((float)2.0f);
        GL11.glTranslated((double)0.0, (double)0.0, (double)0.075f);
        Cylinder shaft = new Cylinder();
        shaft.setDrawStyle(100012);
        shaft.draw(0.1f, 0.11f, 0.4f, 25, 20);
        RenderUtils.glColor(c2);
        GL11.glLineWidth((float)2.0f);
        GL11.glTranslated((double)0.0, (double)0.0, (double)-0.12500000298023223);
        GL11.glTranslated((double)-0.09000000074505805, (double)0.0, (double)0.0);
        Sphere right = new Sphere();
        right.setDrawStyle(100012);
        right.draw(0.14f, 10, 20);
        GL11.glTranslated((double)0.16000000149011612, (double)0.0, (double)0.0);
        Sphere left = new Sphere();
        left.setDrawStyle(100012);
        left.draw(0.14f, 10, 20);
        RenderUtils.glColor(c3);
        GL11.glLineWidth((float)2.0f);
        GL11.glTranslated((double)-0.07000000074505806, (double)0.0, (double)0.589999952316284);
        Sphere tip = new Sphere();
        tip.setDrawStyle(100012);
        tip.draw(0.13f, 15, 20);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)2848);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2896);
        GL11.glEnable((int)3553);
        GL11.glPopMatrix();
    }

    public static void setupColor(int color, float alpha) {
        float f = (float)(color >> 16 & 0xFF) / 255.0f;
        float f1 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f2 = (float)(color & 0xFF) / 255.0f;
        GL11.glColor4f((float)f, (float)f1, (float)f2, (float)(alpha / 255.0f));
    }

    public static void render2D(int mode, VertexFormat formats, float lineWidth, Runnable runnable) {
        boolean isLines = mode == 6913 || mode == 2 || mode == 3 || mode == 1;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        if (isLines) {
            GL11.glEnable((int)2848);
            GlStateManager.glLineWidth(lineWidth);
        }
        buffer.begin(mode, formats);
        runnable.run();
        tessellator.draw();
        if (isLines) {
            GL11.glDisable((int)2848);
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
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
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
        RenderUtils.drawFullGradientRectPro((float)left, (float)top, (float)right, (float)bottom, col1, col2, col2, col1, false);
    }

    public static void drawAlphedSideways(double left, double top, double right, double bottom, int col1, int col2, boolean bloom) {
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glShadeModel((int)7425);
        GL11.glDisable((int)3008);
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(left, top).color(col1).endVertex();
        buffer.pos(left, bottom).color(col1).endVertex();
        buffer.pos(right, bottom).color(col2).endVertex();
        buffer.pos(right, top).color(col2).endVertex();
        tessellator.draw();
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
        if (bloom) {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        RenderUtils.resetBlender();
    }

    public static void drawAlphedSideways(double left, double top, double right, double bottom, int col1, int col2) {
        RenderUtils.drawAlphedSideways(left < right ? left : right, top, left >= right ? left : right, bottom, col1, col2, false);
    }

    public static void drawTwoAlphedSideways(double left, double top, double right, double bottom, int col1, int col2, boolean bloom) {
        RenderUtils.drawAlphedSideways(left, top, left + (right - left) / 2.0, bottom, col2, col1, bloom);
        RenderUtils.drawAlphedSideways(left + (right - left) / 2.0, top, right, bottom, col1, col2, bloom);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glDepthMask((boolean)false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0.0f, 0.0f, (int)width, (int)height, (float)((int)width), (float)((int)height));
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2929);
    }

    public static void drawImageWithAlpha(ResourceLocation image, float x, float y, float width, float height, int color, int alpha) {
        GL11.glEnable((int)3042);
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)3008);
        mc.getTextureManager().bindTexture(image);
        RenderUtils.setupColor(color, alpha);
        GL11.glTranslated((double)x, (double)y, (double)0.0);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0f, 0.0f, (int)width, (int)height, (float)((int)width), (float)((int)height));
        GL11.glTranslated((double)(-x), (double)(-y), (double)0.0);
        GlStateManager.resetColor();
        GL11.glEnable((int)3008);
        GL11.glDepthMask((boolean)true);
    }

    public static void drawPolygonPart(double x, double y, int radius, int part, int color, int endcolor) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0.0).color(color).endVertex();
        double TWICE_PI = Math.PI * 2;
        double r180 = Math.toRadians(180.0);
        for (int i = part * 90; i <= part * 90 + 90; ++i) {
            double angle = Math.PI * 2 * (double)i / 360.0 + r180;
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
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.shadeModel(7425);
        buffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0.0).color(color).endVertex();
        double TWICE_PI = Math.PI * 2;
        double r180 = Math.toRadians(180.0);
        for (int i = part * 90; i <= part * 90 + 90; ++i) {
            double angle = Math.PI * 2 * (double)i / 360.0 + r180;
            buffer.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).color(endcolor).endVertex();
        }
        tessellator.draw();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawVGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top).color(startColor).endVertex();
        buffer.pos(left, top).color(startColor).endVertex();
        buffer.pos(left, bottom).color(endColor).endVertex();
        buffer.pos(right, bottom).color(endColor).endVertex();
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
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top).color(startColor).endVertex();
        bufferbuilder.pos(left, top).color(startColor).endVertex();
        bufferbuilder.pos(left, bottom).color(endColor).endVertex();
        bufferbuilder.pos(right, bottom).color(endColor).endVertex();
        tessellator.draw();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void glRenderStart() {
        GL11.glPushMatrix();
        GL11.glPushAttrib((int)1048575);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3553);
    }

    public static void glRenderStop() {
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2884);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void disableGL2D() {
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2929);
        GL11.glDisable((int)2848);
        GL11.glHint((int)3154, (int)4352);
        GL11.glHint((int)3155, (int)4352);
    }

    public static void enableGL2D() {
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glHint((int)3155, (int)4354);
    }

    public static void drawItemWarnIfLowDur(ItemStack stack, float x, float y, float alphaPC, float scale) {
        RenderUtils.drawItemWarnIfLowDur(stack, x, y, alphaPC, scale, 1);
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
        if (Client.screenshader == null || !Display.isVisible()) {
            return;
        }
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        RenderUtils.resetBlender();
        Client.screenshader.useShader(RenderUtils.mc.displayWidth, RenderUtils.mc.displayHeight, mouseX, mouseY, (float)(System.currentTimeMillis() - Client.initTime) / 1000.0f);
        GL11.glBegin((int)7);
        GL11.glVertex2f((float)-1.0f, (float)-1.0f);
        GL11.glVertex2f((float)-1.0f, (float)1.0f);
        GL11.glVertex2f((float)1.0f, (float)1.0f);
        GL11.glVertex2f((float)1.0f, (float)-1.0f);
        GL11.glEnd();
        GL20.glUseProgram((int)0);
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        if (Client.mainGuiNoise != null) {
            Client.mainGuiNoise.setPlaying(!Panic.stop && GuiMainMenu.quit.to == 0.0f && GuiMainMenu.quit2.to == 0.0f);
        }
    }
}

