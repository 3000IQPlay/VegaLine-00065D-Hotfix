package ru.govno.client.module.modules;

import java.io.Serializable;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec2f;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.Crosshair;
import ru.govno.client.module.modules.ElytraBoost;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.GameSyncTPS;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;
import ru.govno.client.utils.TPSDetect;

public class Timer
extends Module {
    public static Timer get;
    public FloatSettings TX;
    public FloatSettings TY;
    public FloatSettings Increase;
    public FloatSettings Randomize;
    public FloatSettings TimeOutMS;
    public FloatSettings BoundUp;
    public BoolSettings TimeOut;
    public BoolSettings Stamina;
    public BoolSettings NCPBypass;
    public BoolSettings PhantomDash;
    public BoolSettings SmoothWastage;
    public BoolSettings DrawSmart;
    public ModeSettings StaminaMode;
    public ModeSettings Render;
    public ModeSettings TimerSFX;
    private static final TimerHelper afkWait;
    private static final TimerHelper timeOutWait;
    private boolean afk = true;
    private boolean phantomIsRegening;
    private float yaw;
    private float pitch;
    private static float forceTimer;
    private boolean smartGo;
    private boolean critical;
    private boolean panicRegen;
    public static double percent;
    public static AnimationUtils percentSmooth;
    public static AnimationUtils smoothInt9;
    private static final AnimationUtils toShowPC;
    private final AnimationUtils maxTriggerAnim = new AnimationUtils(0.0f, 0.0f, 0.03f);
    private final AnimationUtils minTriggerAnim = new AnimationUtils(0.0f, 0.0f, 0.03f);
    public static boolean isRegening;
    public static boolean forceWastage;
    private final TimerHelper sfxDelay = new TimerHelper();
    public static boolean cancel;
    public static float x;
    public static float y;
    protected static final ResourceLocation BATTARY_BASE;
    protected static final ResourceLocation BATTARY_OVERLAY;
    protected static final ResourceLocation WAIST_BASE;
    protected static final ResourceLocation WAIST_OVERLAY;
    protected final Tessellator tessellator = Tessellator.getInstance();
    protected final BufferBuilder buffer = this.tessellator.getBuffer();

    public Timer() {
        super("Timer", 0, Module.Category.MOVEMENT);
        this.TX = new FloatSettings("TX", 0.5f, 1.0f, 0.0f, this, () -> false);
        this.settings.add(this.TX);
        this.TY = new FloatSettings("TY", 0.8f, 1.0f, 0.0f, this, () -> false);
        this.settings.add(this.TY);
        this.Increase = new FloatSettings("Increase", 2.0f, 4.0f, 0.1f, this, () -> !this.NCPBypass.getBool() || this.Stamina.getBool());
        this.settings.add(this.Increase);
        this.Randomize = new FloatSettings("Randomize", 0.7f, 3.0f, 0.0f, this, () -> !this.NCPBypass.getBool() || this.Stamina.getBool());
        this.settings.add(this.Randomize);
        this.TimeOut = new BoolSettings("TimeOut", false, this);
        this.settings.add(this.TimeOut);
        this.TimeOutMS = new FloatSettings("TimeOutMS", 220.0f, 1000.0f, 1.0f, this, () -> this.TimeOut.getBool());
        this.settings.add(this.TimeOutMS);
        this.Stamina = new BoolSettings("Stamina", true, this);
        this.settings.add(this.Stamina);
        this.BoundUp = new FloatSettings("BoundUp", 0.05f, 0.9f, 0.0f, this, () -> this.Stamina.getBool());
        this.settings.add(this.BoundUp);
        this.NCPBypass = new BoolSettings("NCPBypass", false, this, () -> !this.Stamina.getBool());
        this.settings.add(this.NCPBypass);
        this.StaminaMode = new ModeSettings("StaminaMode", "Matrix", this, new String[]{"Matrix", "NCP", "Other", "Vulcan"}, () -> this.Stamina.getBool());
        this.settings.add(this.StaminaMode);
        this.SmoothWastage = new BoolSettings("SmoothWastage", false, this, () -> this.Stamina.getBool());
        this.settings.add(this.SmoothWastage);
        this.PhantomDash = new BoolSettings("PhantomDash", true, this, () -> this.Stamina.getBool());
        this.settings.add(this.PhantomDash);
        this.DrawSmart = new BoolSettings("DrawSmart", true, this, () -> this.Stamina.getBool());
        this.settings.add(this.DrawSmart);
        this.Render = new ModeSettings("Render", "SmoothNine", this, new String[]{"Line", "Plate", "Circle", "SmoothNine"}, () -> this.Stamina.getBool() && this.DrawSmart.getBool());
        this.settings.add(this.Render);
        this.TimerSFX = new ModeSettings("TimerSFX", "SF", this, new String[]{"None", "Dev", "SF"}, () -> this.Stamina.getBool());
        this.settings.add(this.TimerSFX);
        get = this;
    }

    private float getPhantomSneakSlowing() {
        return 0.5f;
    }

    private boolean canPhantomSlowing() {
        return Minecraft.player.onGround && Minecraft.player.isSneaking() && !Minecraft.player.isJumping() && percent < 1.0 && !this.afk;
    }

    private static void forceWastage() {
        forceWastage = true;
    }

    public void setTempCancel() {
        cancel = true;
    }

    public static void forceTimer(float value) {
        if (!Timer.get.Stamina.getBool()) {
            return;
        }
        afkWait.reset();
        Timer.get.afk = false;
        forceTimer = value;
        Timer.forceWastage();
    }

    public static boolean canDrawTimer() {
        return get != null && Timer.get.Stamina.getBool() && Timer.get.DrawSmart.getBool();
    }

    public static float getWidth() {
        return Timer.get.Render.currentMode.equalsIgnoreCase("SmoothNine") ? 18.0f : (Timer.get.Render.currentMode.equalsIgnoreCase("Plate") ? 28.0f : (Timer.get.Render.currentMode.equalsIgnoreCase("Line") ? 40.0f : 19.0f));
    }

    public static float getHeight() {
        float ext;
        float f = ext = Timer.mc.currentScreen instanceof GuiChat ? 6.0f : 0.0f;
        return Timer.get.Render.currentMode.equalsIgnoreCase("SmoothNine") ? 18.0f : (Timer.get.Render.currentMode.equalsIgnoreCase("Plate") ? 40.0f : (Timer.get.Render.currentMode.equalsIgnoreCase("Line") ? 1.5f + toShowPC.getAnim() * 3.0f + ext : 19.0f));
    }

    public static float[] getCoordsSettings() {
        return new float[]{Timer.get.TX.getFloat(), Timer.get.TY.getFloat()};
    }

    public static float getX(ScaledResolution sr) {
        return (float)sr.getScaledWidth() * Timer.getCoordsSettings()[0] - Timer.getWidth() / 2.0f;
    }

    public static float getY(ScaledResolution sr) {
        return (float)sr.getScaledHeight() * Timer.getCoordsSettings()[1] - Timer.getHeight() / 2.0f;
    }

    public static void setSetsX(float set) {
        ((FloatSettings)Timer.get.settings.get(0)).setFloat(set);
    }

    public static void setSetsY(float set) {
        ((FloatSettings)Timer.get.settings.get(1)).setFloat(set);
    }

    public static boolean isHoveredToTimer(int mouseX, int mouseY, ScaledResolution sr) {
        return Timer.canDrawTimer() && RenderUtils.isHovered(mouseX, mouseY, Timer.getX(sr), Timer.getY(sr), Timer.getWidth(), Timer.getHeight());
    }

    @Override
    public void alwaysRender2D(ScaledResolution sr) {
        float alphaPC = this.Stamina.getAnimation() * this.DrawSmart.getAnimation();
        if (alphaPC != 0.0f) {
            float dy;
            boolean middle;
            String mode = this.Render.currentMode;
            float x = Timer.getX(sr);
            float y = Timer.getY(sr);
            float w = Timer.getWidth();
            float h = Timer.getHeight();
            float dx = (float)sr.getScaledWidth() / 2.0f - (x + w / 2.0f);
            boolean bl = middle = Math.sqrt(dx * dx + (dy = (float)sr.getScaledHeight() / 2.0f - (y + h / 2.0f)) * dy) < 2.0 && !mode.equalsIgnoreCase("Plate");
            if (middle) {
                if (mode.equalsIgnoreCase("Circle")) {
                    h /= 1.25f;
                    w /= 1.25f;
                }
                x = (float)sr.getScaledWidth() / 2.0f - w / 2.0f;
                y = (float)sr.getScaledHeight() / 2.0f - h / 2.0f - 0.25f;
                x += Crosshair.get.crossPosMotions[0];
                y += Crosshair.get.crossPosMotions[1];
            }
            float x2 = x + w;
            float y2 = y + h;
            float pc = percentSmooth.getAnim();
            switch (mode) {
                case "Line": {
                    int colStep = (int)(150.0f * pc);
                    int c1 = ClientColors.getColor1(0, pc * alphaPC);
                    int c2 = ClientColors.getColor1(colStep, (0.5f + pc * 0.5f) * alphaPC);
                    int c3 = ClientColors.getColor1(colStep, (0.75f + pc * 0.25f) * alphaPC);
                    int c4 = ClientColors.getColor1(colStep * 3, alphaPC);
                    float extX = 0.0f;
                    float extY = this.maxTriggerAnim.getAnim() * this.maxTriggerAnim.anim * 1.5f;
                    RenderUtils.drawLightContureRect(x - extX, y - extY, x2 + extX, y2 + extY, ColorUtils.swapAlpha(Integer.MIN_VALUE, 190.0f * alphaPC));
                    RenderUtils.drawWaveGradient(x - extX, y - extY, x + w * pc + extX, y2 + extY, 1.0f, c1, c2, c3, c4, true, false);
                    c1 = ColorUtils.swapAlpha(c1, (float)ColorUtils.getAlphaFromColor(c1) / 10.0f);
                    c2 = ColorUtils.swapAlpha(c2, (float)ColorUtils.getAlphaFromColor(c2) / 10.0f);
                    c3 = ColorUtils.swapAlpha(c3, (float)ColorUtils.getAlphaFromColor(c3) / 10.0f);
                    c4 = ColorUtils.swapAlpha(c4, (float)ColorUtils.getAlphaFromColor(c4) / 10.0f);
                    RenderUtils.drawWaveGradient(x - extX, y - extY, x + w + extX, y2 + extY, 0.6f, c1, c2, c3, c4, true, false);
                    float showPC = toShowPC.getAnim();
                    boolean show = (double)showPC > 0.05;
                    Object str = "Timer";
                    CFontRenderer font = Fonts.mntsb_10;
                    float strW = font.getStringWidth((String)str);
                    float texX = x + w / 2.0f - strW / 2.0f;
                    float texY = y + 4.0f - extY;
                    int texCol = ColorUtils.swapAlpha(-1, 255.0f * alphaPC);
                    if (Timer.mc.currentScreen instanceof GuiChat) {
                        font.drawStringWithShadow((String)str, texX, texY, texCol);
                        break;
                    }
                    if (!show) break;
                    str = percent == 0.0 || percent == 1.0 ? "" + (int)(percent * 100.0) : ((double)percentSmooth.getAnim() > percent ? "-" : "") + (int)(percent * 100.0) + ((double)percentSmooth.getAnim() < percent ? "+" : "");
                    strW = font.getStringWidth((String)str);
                    texX = x + (w - strW) * pc;
                    texY = y - 1.5f - showPC * 2.5f;
                    float texAlpha = 255.0f * showPC * (0.5f + showPC * 0.5f) * (0.75f + pc * 0.25f);
                    texCol = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), texAlpha);
                    if (!(texAlpha > 32.0f)) break;
                    font.drawStringWithShadow((String)str, texX, texY, texCol);
                    break;
                }
                case "Circle": {
                    int texCol;
                    RenderUtils.resetBlender();
                    float extS = MathUtils.clamp((middle ? toShowPC.getAnim() : 1.0f) + (this.minTriggerAnim.getAnim() + this.maxTriggerAnim.getAnim()) / 5.0f, 0.0f, 1.0f);
                    boolean crit = this.critical;
                    if (!middle || Timer.mc.currentScreen instanceof GuiChat) {
                        RenderUtils.drawSmoothCircle(x + w / 2.0f, y + h / 2.0f, w / 2.0f + 1.0f, ColorUtils.getColor(0, 0, 0, 60.0f * alphaPC));
                    }
                    GL11.glEnable((int)3042);
                    if (middle && Timer.mc.currentScreen instanceof GuiChat && Mouse.isButtonDown((int)0) && MathUtils.getDifferenceOf(sr.getScaledWidth(), Mouse.getX()) < (double)w && MathUtils.getDifferenceOf(sr.getScaledHeight(), Mouse.getY()) < (double)h && ColorUtils.getAlphaFromColor(texCol = ColorUtils.swapAlpha(-1, 255.0f * alphaPC)) > 32) {
                        Fonts.comfortaaBold_12.drawStringWithOutline("Timer indicator has centered", x + w / 2.0f - (float)Fonts.comfortaaBold_12.getStringWidth("Timer indicator has centered") / 2.0f, y - 11.0f, texCol);
                    }
                    if ((double)percentSmooth.getAnim() >= 0.01 || this.minTriggerAnim.getAnim() > 0.0f) {
                        if (middle && extS > 0.03f && Timer.mc.gameSettings.thirdPersonView == 0) {
                            RenderUtils.drawCircledTHud(x + w / 2.0f, y + h / 2.0f, w / 2.25f * extS * (middle ? 0.6f + 0.4f * pc : 1.0f), 1.0f, Integer.MIN_VALUE, extS * extS * 195.0f * (0.5f + 0.5f * pc) * extS * alphaPC, 2.0f * extS + 0.05f);
                        }
                        if (Timer.mc.gameSettings.thirdPersonView == 0 || !middle) {
                            RenderUtils.drawClientCircle(x + w / 2.0f, y + h / 2.0f, w / 2.25f * extS * (middle ? 0.6f + 0.4f * pc : 1.0f), percentSmooth.getAnim() * 359.0f, middle ? 3.0f : 3.5f + 3.0f * pc, extS * extS * (0.5f + 0.5f * pc) * alphaPC);
                        }
                    }
                    if (!middle && extS > 0.03f) {
                        RenderUtils.drawSmoothCircle(x + w / 2.0f, y + h / 2.0f, w / 2.5f + 1.0f, ColorUtils.getColor(0, 0, 0, 150.0f * alphaPC));
                    }
                    if (this.minTriggerAnim.getAnim() != 0.0f || this.maxTriggerAnim.getAnim() != 0.0f) {
                        float aPCT = MathUtils.clamp(this.minTriggerAnim.getAnim() + this.maxTriggerAnim.getAnim(), 0.0f, 1.0f);
                        float tR = w / 2.25f - (middle ? 4.0f : 3.0f) + 4.0f * toShowPC.getAnim();
                        RenderUtils.drawClientCircleWithOverallToColor(x + w / 2.0f, y + h / 2.0f, tR += aPCT * 2.0f, 359.0f, aPCT * 5.0f, aPCT, ColorUtils.getOverallColorFrom(ColorUtils.swapAlpha(-1, 255.0f * alphaPC), ColorUtils.getColor(255, 0, 0, 255.0f * alphaPC), this.minTriggerAnim.getAnim()), aPCT);
                    }
                    String pppc = "" + (Serializable)(crit ? "*-*" : Integer.valueOf((int)(percent * 100.0)));
                    float strW2 = Fonts.mntsb_10.getStringWidth(pppc);
                    if (!middle) {
                        if (pppc.equalsIgnoreCase("100")) {
                            GL11.glPushMatrix();
                            float timePC = (float)(System.currentTimeMillis() % 1200L) / 1200.0f;
                            float timePC2 = ((double)timePC > 0.5 ? 1.0f - timePC : timePC) * 2.0f;
                            RenderUtils.customRotatedObject2D(x, y, w, h, timePC * 360.0f + 90.0f);
                            RenderUtils.drawCircledTHud(x + w / 2.0f, y + h / 2.0f, 3.0f, timePC2, -1, 145.0f * alphaPC, 0.75f);
                            RenderUtils.drawCircledTHud(x + w / 2.0f, y + h / 2.0f, 4.5f, 1.0f - timePC2, -1, 115.0f * alphaPC, 1.1f);
                            float time2PC = (float)((System.currentTimeMillis() + 600L) % 1000L) / 1000.0f;
                            float time2PC2 = ((double)time2PC > 0.5 ? 1.0f - time2PC : time2PC) * 2.0f;
                            RenderUtils.drawCircledTHud(x + w / 2.0f, y + h / 2.0f, 4.0f + 2.5f * time2PC, 1.0f, -1, 85.0f * time2PC2 * alphaPC, 0.1f + time2PC2 * 3.5f);
                            GL11.glPopMatrix();
                        } else {
                            int c = crit ? ClientColors.getColor1() : ColorUtils.getOverallColorFrom(Integer.MAX_VALUE, -1, (float)percent);
                            if (ColorUtils.getAlphaFromColor(c = ColorUtils.swapAlpha(c, (float)ColorUtils.getAlphaFromColor(c) * alphaPC)) > 32) {
                                Fonts.mntsb_10.drawString(pppc, x + w / 2.0f - strW2 / 2.0f, y + 9.5f, c);
                            }
                        }
                    }
                    RenderUtils.resetBlender();
                    break;
                }
                case "Plate": {
                    int col1 = ClientColors.getColorQ(1, alphaPC);
                    int col2 = ClientColors.getColorQ(2, alphaPC);
                    int col3 = ClientColors.getColorQ(3, alphaPC);
                    int col4 = ClientColors.getColorQ(4, alphaPC);
                    int white = ColorUtils.swapAlpha(-1, 255.0f * alphaPC);
                    int red = ColorUtils.getColor(255, 0, 0, 255.0f * alphaPC);
                    if (this.maxTriggerAnim.getAnim() > 0.0f) {
                        col1 = ColorUtils.getOverallColorFrom(col1, white, this.maxTriggerAnim.anim);
                        col2 = ColorUtils.getOverallColorFrom(col2, white, this.maxTriggerAnim.anim);
                        col3 = ColorUtils.getOverallColorFrom(col3, white, this.maxTriggerAnim.anim);
                        col4 = ColorUtils.getOverallColorFrom(col4, white, this.maxTriggerAnim.anim);
                    }
                    if (this.minTriggerAnim.getAnim() > 0.0f) {
                        col1 = ColorUtils.getOverallColorFrom(col1, red, this.minTriggerAnim.anim);
                        col2 = ColorUtils.getOverallColorFrom(col2, red, this.minTriggerAnim.anim);
                        col3 = ColorUtils.getOverallColorFrom(col3, red, this.minTriggerAnim.anim);
                        col4 = ColorUtils.getOverallColorFrom(col4, red, this.minTriggerAnim.anim);
                    }
                    GL11.glDisable((int)3008);
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.enableTexture2D();
                    GlStateManager.shadeModel(7425);
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GL11.glTranslated((double)0.0, (double)(-(-this.minTriggerAnim.anim + this.maxTriggerAnim.anim) * 2.0f), (double)0.0);
                    mc.getTextureManager().bindTexture(BATTARY_BASE);
                    this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    this.buffer.pos(x, y).tex(0.0, 0.0).color(col1).endVertex();
                    this.buffer.pos(x, y2).tex(0.0, 1.0).color(col2).endVertex();
                    this.buffer.pos(x2, y2).tex(1.0, 1.0).color(col3).endVertex();
                    this.buffer.pos(x2, y).tex(1.0, 0.0).color(col4).endVertex();
                    this.tessellator.draw();
                    mc.getTextureManager().bindTexture(BATTARY_OVERLAY);
                    StencilUtil.initStencilToWrite();
                    if ((double)pc > 0.99) {
                        RenderUtils.drawAlphedRect(x, y + 2.5f + 34.0f * (1.0f - pc), x2, y2 - 2.0f, white);
                    } else {
                        float Y1 = y + 2.5f + 34.0f * (1.0f - pc);
                        float Y2 = y2 - 2.0f;
                        float X1 = x + 2.0f;
                        float X2 = x2 - 2.0f;
                        float waveDelay = 2000.0f;
                        float waveStep = 0.15f;
                        float waveHeight = 12.0f * (1.0f - pc) * pc;
                        int vertexXStep = 2;
                        GlStateManager.disableTexture2D();
                        ArrayList<Vec2f> vectors = new ArrayList<Vec2f>();
                        vectors.add(new Vec2f(X1, Y2));
                        int vecIndex = 0;
                        for (float waveX = X1; waveX <= X2; waveX += (float)vertexXStep) {
                            float timePC = (float)((System.currentTimeMillis() + (long)((float)vecIndex * waveStep * waveDelay / (float)vertexXStep)) % (long)((int)waveDelay)) / waveDelay;
                            float waveY = Y1 - waveHeight / 2.0f + (float)MathUtils.easeInOutQuadWave(timePC) * waveHeight;
                            vectors.add(new Vec2f(waveX, waveY));
                            ++vecIndex;
                        }
                        vectors.add(new Vec2f(X2, Y2));
                        RenderUtils.drawSome(vectors, white, 9);
                    }
                    GlStateManager.enableTexture2D();
                    StencilUtil.readStencilBuffer(1);
                    this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    this.buffer.pos(x, y).tex(0.0, 0.0).color(col1).endVertex();
                    this.buffer.pos(x, y2).tex(0.0, 1.0).color(col2).endVertex();
                    this.buffer.pos(x2, y2).tex(1.0, 1.0).color(col3).endVertex();
                    this.buffer.pos(x2, y).tex(1.0, 0.0).color(col4).endVertex();
                    this.tessellator.draw();
                    StencilUtil.uninitStencilBuffer();
                    GL11.glTranslated((double)0.0, (double)((-this.minTriggerAnim.getAnim() + this.maxTriggerAnim.getAnim()) * 2.0f), (double)0.0);
                    GlStateManager.shadeModel(7424);
                    GlStateManager.depthMask(true);
                    GL11.glEnable((int)3008);
                    GlStateManager.resetColor();
                    break;
                }
                case "Waist": {
                    int col1 = ClientColors.getColor1(0, alphaPC);
                    int col2 = ClientColors.getColor2(-324, alphaPC);
                    int col3 = ClientColors.getColor2(0, alphaPC);
                    int col4 = ClientColors.getColor1(972, alphaPC);
                    int black = ColorUtils.getColor(0, 0, 0, 140.0f * alphaPC);
                    GL11.glDisable((int)3008);
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.shadeModel(7425);
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    mc.getTextureManager().bindTexture(WAIST_BASE);
                    this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    this.buffer.pos(x, y).tex(0.0, 0.0).color(black).endVertex();
                    this.buffer.pos(x, y2).tex(0.0, 1.0).color(black).endVertex();
                    this.buffer.pos(x2, y2).tex(1.0, 1.0).color(black).endVertex();
                    this.buffer.pos(x2, y).tex(1.0, 0.0).color(black).endVertex();
                    this.tessellator.draw();
                    mc.getTextureManager().bindTexture(WAIST_OVERLAY);
                    this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    this.buffer.pos(x, y).tex(0.0, 0.0).color(col1).endVertex();
                    this.buffer.pos(x, y2).tex(0.0, 1.0).color(col2).endVertex();
                    this.buffer.pos(x2, y2).tex(1.0, 1.0).color(col3).endVertex();
                    this.buffer.pos(x2, y).tex(1.0, 0.0).color(col4).endVertex();
                    StencilUtil.initStencilToWrite();
                    float overlayX1 = x + 22.0f;
                    float overlayX2 = overlayX1 + 45.0f * MathUtils.clamp(pc * 1.005f, 0.0f, 1.0f);
                    int white = ColorUtils.swapAlpha(-1, 255.0f * alphaPC);
                    RenderUtils.drawRect(overlayX1, y, overlayX2, y2, white);
                    StencilUtil.readStencilBuffer(1);
                    this.tessellator.draw();
                    StencilUtil.uninitStencilBuffer();
                    GlStateManager.shadeModel(7424);
                    GlStateManager.depthMask(true);
                    GL11.glEnable((int)3008);
                    GlStateManager.resetColor();
                    CFontRenderer font = Fonts.comfortaaBold_14;
                    if (ColorUtils.getAlphaFromColor(white) <= 32) break;
                    font.drawString((int)(percent * 100.0) + "%", x + 3.0f, y + 5.5f, white);
                    break;
                }
                case "SmoothNine": {
                    float cosY;
                    int ecoC;
                    int bgCol = ColorUtils.getColor(18, 18, 18, 255.0f * alphaPC);
                    int bgCol2 = ColorUtils.getColor(36, 36, 36, 255.0f * alphaPC);
                    Timer.smoothInt9.to = (int)MathUtils.clamp(pc * 9.0f + 0.5f, 0.0f, 9.0f);
                    float smooth9 = smoothInt9.getAnim();
                    int startRad = ecoC = 30;
                    int endRad = (int)((float)startRad + (360.0f - (float)ecoC * 2.0f) * pc);
                    int endRadBG = (int)((float)startRad + (360.0f - (float)ecoC * 2.0f));
                    float circleW = 3.0f;
                    float circleRange = h - 6.0f - circleW / 2.0f;
                    circleRange /= 2.0f;
                    int rad = startRad;
                    float trAPC = this.maxTriggerAnim.getAnim() + this.minTriggerAnim.getAnim();
                    float f = trAPC > 1.0f ? 1.0f : (trAPC = trAPC < 0.0f ? 0.0f : trAPC);
                    if (trAPC > 0.0f) {
                        int trCol = ColorUtils.getOverallColorFrom(ColorUtils.getColor(255, (int)MathUtils.clamp(255.0f * trAPC * alphaPC, 0.0f, 255.0f)), ColorUtils.getColor(255, 0, 0, MathUtils.clamp(255.0f * trAPC * alphaPC, 0.0f, 255.0f)), (double)this.minTriggerAnim.anim > 0.03 ? 1.0f : 0.0f);
                        RenderUtils.drawSmoothCircle(x + w / 2.0f, y + h / 2.0f, circleRange + 5.0f, trCol);
                    }
                    RenderUtils.drawSmoothCircle(x + w / 2.0f, y + h / 2.0f, circleRange + 5.0f - 2.0f * trAPC, bgCol);
                    RenderUtils.enableGL2D();
                    RenderUtils.glColor(-1);
                    GL11.glDisable((int)2852);
                    GL11.glLineWidth((float)(circleW + 1.0f));
                    StencilUtil.initStencilToWrite();
                    GL11.glBegin((int)3);
                    for (rad = endRadBG; rad > startRad; rad -= 6) {
                        float sinX = (float)((double)(x + w / 2.0f) - Math.sin(Math.toRadians(rad)) * (double)(circleRange + circleW / 2.0f));
                        cosY = (float)((double)(y + h / 2.0f) + Math.cos(Math.toRadians(rad)) * (double)(circleRange + circleW / 2.0f));
                        GL11.glVertex2d((double)sinX, (double)cosY);
                    }
                    GL11.glEnd();
                    StencilUtil.readStencilBuffer(1);
                    GL11.glPointSize((float)circleW);
                    RenderUtils.glColor(bgCol2);
                    GL11.glBegin((int)0);
                    for (rad = endRadBG; rad > endRad; rad -= 6) {
                        float sinX = (float)((double)(x + w / 2.0f) - Math.sin(Math.toRadians(rad)) * (double)(circleRange + circleW / 2.0f));
                        cosY = (float)((double)(y + h / 2.0f) + Math.cos(Math.toRadians(rad)) * (double)(circleRange + circleW / 2.0f));
                        GL11.glVertex2d((double)sinX, (double)cosY);
                    }
                    GL11.glEnd();
                    GL11.glBegin((int)0);
                    for (rad = endRad; rad > startRad; rad -= 6) {
                        int cccc = ClientColors.getColor1(4800 - rad * 6, (1.0f - trAPC / 3.0f) / 2.0f * alphaPC);
                        RenderUtils.glColor(cccc);
                        float sinX = (float)((double)(x + w / 2.0f) - Math.sin(Math.toRadians(rad)) * (double)(circleRange + circleW / 2.0f));
                        float cosY2 = (float)((double)(y + h / 2.0f) + Math.cos(Math.toRadians(rad)) * (double)(circleRange + circleW / 2.0f));
                        GL11.glVertex2d((double)sinX, (double)cosY2);
                    }
                    GL11.glEnd();
                    GL11.glPointSize((float)1.0f);
                    StencilUtil.uninitStencilBuffer();
                    GL11.glLineWidth((float)1.0f);
                    RenderUtils.disableGL2D();
                    GL11.glEnable((int)3042);
                    GL11.glEnable((int)3553);
                    Timer.smoothInt9.speed = 0.1f;
                    if (MathUtils.getDifferenceOf(Timer.smoothInt9.to, smoothInt9.getAnim()) < 0.1) {
                        smoothInt9.setAnim(Timer.smoothInt9.to);
                    }
                    int col1 = ClientColors.getColor1(0, (1.0f - trAPC / 3.0f) * alphaPC);
                    int col2 = ClientColors.getColor2(0, (1.0f - trAPC / 3.0f) * alphaPC);
                    CFontRenderer font = Fonts.mntsb_14;
                    StencilUtil.initStencilToWrite();
                    RenderUtils.drawSmoothCircle(x + w / 2.0f, y + h / 2.0f, 3.5f, -1);
                    StencilUtil.readStencilBuffer(1);
                    for (int i = 10; i > 0; --i) {
                        float aPCT = (float)MathUtils.clamp(1.0 - MathUtils.getDifferenceOf(y + h / 2.0f + (float)(i - 1) * 7.0f - smooth9 * 7.0f, y + h / 2.0f) / 4.0 / 2.0, 0.0, 1.0) * alphaPC;
                        if (!(aPCT > 0.3f) || !((float)ColorUtils.getAlphaFromColor(col1) * aPCT >= 33.0f)) continue;
                        float tx = x + w / 2.0f - (float)font.getStringWidth(String.valueOf((int)((float)i - 0.5f))) / 2.0f;
                        float ty = y + h / 2.0f - 1.5f + (float)(i - 1) * 7.0f - smooth9 * 7.0f;
                        font.drawVGradientString(String.valueOf((int)((float)i - 0.5f)), tx, ty, ColorUtils.swapAlpha(col2, (float)ColorUtils.getAlphaFromColor(col2) * aPCT), ColorUtils.swapAlpha(col1, (float)ColorUtils.getAlphaFromColor(col1) * aPCT));
                    }
                    StencilUtil.uninitStencilBuffer();
                    break;
                }
            }
        }
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        Packet packet;
        if ((this.actived || forceWastage) && this.smartGo && (packet = event.getPacket()) instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook TP = (SPacketPlayerPosLook)packet;
            if (Minecraft.player.getDistance(TP.getX(), TP.getY(), TP.getZ()) > 20.0 || this.isNcpTimerDisabler()) {
                return;
            }
            this.panicRegen = true;
            this.smartGo = false;
            percent /= 1.5;
            this.critical = true;
        }
    }

    private double[] timerArgs(String mode, boolean flaged, double tpsPC20, double timerSpeed) {
        double chargeSP = 1.0;
        double dropSP = 0.0;
        double regenSP = 0.0;
        double chargeMul = this.phantomIsRegening ? (double)this.getPhantomSneakSlowing() : 1.0;
        switch (mode) {
            case "Matrix": {
                chargeSP = 0.07 / tpsPC20 * chargeMul;
                dropSP = 0.021 * timerSpeed * tpsPC20;
                regenSP = 0.33333 / tpsPC20 * chargeMul;
                break;
            }
            case "NCP": {
                chargeSP = 0.05 / tpsPC20 * chargeMul;
                dropSP = 0.046 * timerSpeed * tpsPC20;
                regenSP = 0.75 / tpsPC20 * chargeMul;
                break;
            }
            case "Other": {
                chargeSP = 0.08 * tpsPC20 * chargeMul;
                dropSP = 0.046 * timerSpeed * tpsPC20;
                regenSP = 0.85 / tpsPC20 * chargeMul;
                break;
            }
            case "Vulcan": {
                chargeSP = 0.1 / tpsPC20 * chargeMul;
                dropSP = 0.11 * timerSpeed * tpsPC20;
                regenSP = tpsPC20 * chargeMul;
            }
        }
        if (flaged) {
            chargeSP /= 1.425;
            regenSP /= 3.5;
        }
        return new double[]{chargeSP, dropSP, regenSP};
    }

    private boolean updateAfkStatus(TimerHelper timer) {
        boolean FORCE_RECHARGE;
        if (!timer.hasReached(100.0)) {
            this.yaw = Minecraft.player.lastReportedYaw;
            this.pitch = EntityPlayerSP.lastReportedPitch;
        }
        double player3DSpeed = Math.sqrt(Entity.Getmotionx * Entity.Getmotionx + Entity.Getmotiony * Entity.Getmotiony + Entity.Getmotionz * Entity.Getmotionz);
        boolean bl = FORCE_RECHARGE = Minecraft.player.ticksExisted == 1 || Minecraft.player.isDead;
        if (!FORCE_RECHARGE) {
            boolean bl2 = FORCE_RECHARGE = FreeCam.get != null && FreeCam.get.actived;
        }
        if (FORCE_RECHARGE || this.yaw == Minecraft.player.lastReportedYaw && this.pitch == EntityPlayerSP.lastReportedPitch && (player3DSpeed == 0.0784000015258789 || player3DSpeed == 0.0 || player3DSpeed == 0.02) && !forceWastage) {
            if (timer.hasReached(150.0)) {
                timer.reset();
                this.afk = true;
            }
        } else {
            this.afk = false;
            timer.reset();
        }
        if (Minecraft.player.ticksExisted == 1 || Minecraft.player.isDead) {
            this.afk = true;
            percent = 1.0;
            Timer.percentSmooth.to = (float)1.0;
            percentSmooth.setAnim(MathUtils.lerp(Timer.percentSmooth.anim, Timer.percentSmooth.to, 0.66666f));
            this.critical = false;
        }
        return this.afk;
    }

    private double updateTimerPercent(double[] args, boolean isAfk, float boundUp) {
        boolean phantomRegen;
        boolean bl = phantomRegen = this.phantomIsRegening && !this.actived;
        if (percent < 1.0 && isAfk != phantomRegen) {
            percent += args[0] / (double)(1.0f - boundUp);
            isRegening = true;
            if (!phantomRegen) {
                this.critical = false;
            }
        } else if (!isAfk && percent < 1.0 && !this.actived && Timer.mc.timer.speed <= 1.0) {
            double upped = (float)(args[0] * (double)0.2f) / 5.0f;
            if (args[2] / (double)(1.0f - boundUp) > upped + percent - (double)0.02f - (double)boundUp && !forceWastage) {
                percent += upped;
            }
            this.critical = false;
        }
        if (this.panicRegen && percent == 1.0) {
            this.panicRegen = false;
            if (this.critical) {
                this.critical = false;
            }
        }
        if (!isAfk && percent > (double)boundUp && (this.smartGo || forceWastage)) {
            percent = Math.max(percent - args[1], (double)boundUp);
        }
        percent = MathUtils.clamp(percent, 0.0, 1.0);
        return percent;
    }

    private boolean canDisableByTimeOut(boolean timeOutEnabled, int timeOutMS) {
        return this.actived && timeOutEnabled && timeOutWait.hasReached(timeOutMS);
    }

    private boolean canAbuseTimerSpeed(boolean isSmart) {
        String ebMode;
        boolean FORCE_STOP = false;
        if (ElytraBoost.get.actived && ElytraBoost.canElytra() && ((ebMode = ElytraBoost.get.Mode.currentMode).equalsIgnoreCase("MatrixFly2") && !ElytraBoost.get.NoTimerDefunction.getBool() || ebMode.equalsIgnoreCase("MatrixFly3"))) {
            FORCE_STOP = true;
        }
        return forceWastage && percent > (double)this.BoundUp.getFloat() || this.actived && (this.smartGo && !this.critical || !isSmart) && !FORCE_STOP;
    }

    private double getTimerBoostSpeed(boolean can, boolean smart, float boundUp) {
        double speed = 1.0;
        if (can) {
            float timer = this.NCPBypass.getBool() && !this.Stamina.getBool() ? 2.0f : this.Increase.getFloat();
            speed = smart && timer > 1.0f && this.SmoothWastage.getBool() ? (double)(1.0f + (timer - 1.0f) / 6.0f) + (double)((timer - 1.0f) / 1.1666666f) * (percent - (double)boundUp) : (double)timer;
            float randomVal = this.Randomize.getFloat();
            double randomize = randomVal * (float)(-1 + 2 * (Minecraft.player.ticksExisted % 2));
            if (randomize != 0.0) {
                speed += randomize > 0.0 ? randomize : randomize / 2.0;
            }
            if (forceWastage) {
                speed = forceTimer;
            }
            forceTimer = 1.0f;
        }
        return can ? MathUtils.clamp(speed, 0.025, 20.0) : 1.0;
    }

    private boolean isNcpTimerDisabler() {
        return this.NCPBypass.getBool() && !this.Stamina.getBool();
    }

    private int timerSFXSleepMS() {
        return this.smartGo ? 40 : 50;
    }

    @Override
    public void alwaysUpdate() {
        if (afkWait == null || Minecraft.player == null) {
            return;
        }
        if (cancel) {
            cancel = false;
            return;
        }
        boolean smartTimer = this.Stamina.getBool();
        float boundUp = this.BoundUp.getFloat();
        boolean canABB = this.canAbuseTimerSpeed(smartTimer);
        String sfxMode = this.TimerSFX.currentMode;
        boolean doSfx = !sfxMode.equalsIgnoreCase("None");
        double speed = this.getTimerBoostSpeed(canABB, smartTimer, boundUp);
        if (smartTimer) {
            double prevPercent = percent;
            double[] ARGS = this.timerArgs(this.StaminaMode.currentMode, this.panicRegen, TPSDetect.getTPSServer() / 20.0f, Timer.mc.timer.speed * GameSyncTPS.getGameConpense(1.0, GameSyncTPS.instance.SyncPercent.getFloat()) - 1.0);
            if (this.PhantomDash.getBool()) {
                this.phantomIsRegening = this.canPhantomSlowing();
                speed *= this.phantomIsRegening ? (double)this.getPhantomSneakSlowing() : 1.0;
            } else if (this.phantomIsRegening) {
                this.phantomIsRegening = false;
            }
            this.smartGo = this.updateTimerPercent(ARGS, this.updateAfkStatus(afkWait), boundUp) > (double)boundUp && !this.afk && !this.critical;
            Timer.percentSmooth.to = (float)percent;
            Timer.toShowPC.to = percent > (double)boundUp && percent < 1.0 || this.minTriggerAnim.getAnim() > 0.0f || this.maxTriggerAnim.getAnim() > 0.0f ? 1.0f : 0.0f;
            Timer.toShowPC.speed = 0.1f;
            if (doSfx && prevPercent != percent && (percent == (double)boundUp || percent == 1.0)) {
                MusicHelper.playSound((percent < prevPercent ? "timerlow" : "timermax") + sfxMode.toLowerCase() + ".wav", sfxMode.equalsIgnoreCase("Dev") ? 0.8f : 0.3f);
                if (percent < prevPercent) {
                    this.minTriggerAnim.to = 1.01f;
                } else {
                    this.maxTriggerAnim.to = 1.01f;
                }
            }
            if (doSfx && this.sfxDelay.hasReached(this.timerSFXSleepMS()) && (int)(prevPercent * 100.0) != (int)(percent * 100.0)) {
                if (prevPercent > percent && percent != 0.0 && this.smartGo) {
                    MusicHelper.playSound(this.smartGo ? "timertickdrop.wav" : "timertickcharge.wav", 0.165f);
                    this.sfxDelay.reset();
                }
                if (prevPercent < percent && percent != 1.0) {
                    MusicHelper.playSound("timertickcharge.wav", 0.165f);
                    this.sfxDelay.reset();
                }
            }
            if (this.maxTriggerAnim.getAnim() > 1.0f) {
                this.maxTriggerAnim.setAnim(1.0f);
                this.maxTriggerAnim.to = 0.0f;
            }
            if (this.minTriggerAnim.getAnim() > 1.0f) {
                this.minTriggerAnim.setAnim(1.0f);
                this.minTriggerAnim.to = 0.0f;
            }
            if (this.maxTriggerAnim.to == 0.0f && (double)this.maxTriggerAnim.getAnim() < 0.03) {
                this.maxTriggerAnim.setAnim(0.0f);
            }
            if (this.minTriggerAnim.to == 0.0f && (double)this.minTriggerAnim.getAnim() < 0.03) {
                this.minTriggerAnim.setAnim(0.0f);
            }
            this.minTriggerAnim.speed = 0.1f;
            this.maxTriggerAnim.speed = 0.075f;
        } else {
            if (this.actived && (double)Minecraft.player.ticksExisted % ((Timer.mc.timer.speed - 1.0) * 25.0) == 0.0 && this.isNcpTimerDisabler()) {
                Minecraft.player.connection.sendPacket(new CPacketPlayer.PositionRotation(Minecraft.player.posX, Minecraft.player.posY - (Minecraft.player.onGround ? 0.1 : 1.1), Minecraft.player.posZ, Minecraft.player.rotationYaw, Minecraft.player.rotationPitch, Minecraft.player.onGround));
            }
            if (percent != 1.0) {
                this.smartGo = false;
                Timer.percentSmooth.to = 1.0f;
                percent = 1.0;
                Timer.toShowPC.to = 0.0f;
            }
        }
        if (this.canDisableByTimeOut(this.TimeOut.getBool(), this.TimeOutMS.getInt())) {
            this.toggle(false);
            return;
        }
        Timer.mc.timer.speed = speed;
        forceWastage = false;
    }

    @Override
    public String getDisplayName() {
        return this.Stamina.getBool() ? this.getName() + this.getSuff() + (String)(this.panicRegen || this.critical ? "Flagged" : "Smart" + (String)(percent >= (double)this.BoundUp.getFloat() ? "-Max" : (int)(percent * 100.0) + "%")) : this.getDisplayByDouble(this.Increase.getFloat());
    }

    @Override
    public void onToggled(boolean actived) {
        if (actived) {
            timeOutWait.reset();
        } else {
            Timer.mc.timer.speed = 1.0;
        }
        super.onToggled(actived);
    }

    static {
        afkWait = new TimerHelper();
        timeOutWait = new TimerHelper();
        forceTimer = 1.0f;
        percent = 1.0;
        percentSmooth = new AnimationUtils(1.0f, 1.0f, 0.12f);
        smoothInt9 = new AnimationUtils(9.0f, 9.0f, 0.06f);
        toShowPC = new AnimationUtils(0.0f, 0.0f, 0.15f);
        isRegening = false;
        forceWastage = false;
        cancel = false;
        BATTARY_BASE = new ResourceLocation("vegaline/modules/timer/battary_base.png");
        BATTARY_OVERLAY = new ResourceLocation("vegaline/modules/timer/battary_overlay.png");
        WAIST_BASE = new ResourceLocation("vegaline/modules/timer/waist_base.png");
        WAIST_OVERLAY = new ResourceLocation("vegaline/modules/timer/waist_overlay.png");
    }
}

