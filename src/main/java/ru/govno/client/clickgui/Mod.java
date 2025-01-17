package ru.govno.client.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.clickgui.CheckBox;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.clickgui.Colors;
import ru.govno.client.clickgui.Comp;
import ru.govno.client.clickgui.Modes;
import ru.govno.client.clickgui.Panel;
import ru.govno.client.clickgui.Set;
import ru.govno.client.clickgui.Slider;
import ru.govno.client.clickgui.TriangleGroup;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClickGui;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.module.settings.Settings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class Mod
        extends Comp {
    boolean binding;
    boolean prevHover;
    AnimationUtils bindShowAnim = new AnimationUtils(0.0f, 0.0f, 0.1f);
    AnimationUtils bindingAnim = new AnimationUtils(0.0f, 0.0f, 0.1f);
    AnimationUtils bindHoldAnim = new AnimationUtils(0.0f, 0.0f, 0.1f);
    AnimationUtils bindWaveAnim = new AnimationUtils(0.0f, 0.0f, 0.05f);
    TimerHelper holdBindTimer = new TimerHelper();
    int keyBindToSet = -1;
    AnimationUtils setsAnim = new AnimationUtils(0.0f, 0.0f, 0.1f);
    AnimationUtils alpha = new AnimationUtils(0.0f, 0.0f, 0.1f);
    AnimationUtils toggleAnim = new AnimationUtils(0.0f, 0.0f, 0.06f);
    ArrayList<Set> sets = new ArrayList();
    boolean open = false;
    AnimationUtils openAnim = new AnimationUtils(this.getHeight(), this.getHeight(), 0.125f);
    AnimationUtils sdvig = new AnimationUtils(0.0f, 0.0f, 0.125f);
    AnimationUtils bindConflictAnim = new AnimationUtils(0.0f, 0.0f, 0.075f);
    AnimationUtils bindConflictAngleAnim = new AnimationUtils(0.0f, 0.0f, 0.05f);
    Module module;
    boolean last;
    boolean first;
    TriangleGroup triangleGroup;
    float tgExt;
    float tgH;
    List<NanoBindParticle> nanoBindParticlesList = new ArrayList<NanoBindParticle>();
    static float xn;
    static float yn;
    static float alphaD;
    static String descript;
    float height;
    public boolean wantToClick = true;
    public boolean wantToClick2 = true;

    float maxBindTime() {
        return this.keyBindToSet == 211 ? 700.0f : 800.0f;
    }

    void updateBinding() {
        if (!this.binding || this.keyBindToSet == -1 || !Keyboard.isKeyDown((int)this.keyBindToSet)) {
            this.bindHoldAnim.to = 0.0f;
            this.holdBindTimer.reset();
        }
        if (this.binding) {
            if (this.keyBindToSet != -1 && this.holdBindTimer.hasReached(this.maxBindTime()) && this.bindHoldAnim.getAnim() > 0.9722222f) {
                int prevBind = this.module.getBind();
                this.module.setBind(this.keyBindToSet == 211 ? 0 : this.keyBindToSet);
                if (prevBind != this.module.getBind()) {
                    ClientTune.get.playGuiModuleBindSong(this.module.getBind() != 0);
                    this.bindWaveAnim.setAnim(1.0f);
                }
                this.binding = false;
            }
            this.bindHoldAnim.to = MathUtils.clamp((float)this.holdBindTimer.getTime() / this.maxBindTime(), 0.0f, 1.0f);
        }
        this.bindingAnim.to = this.binding || this.bindWaveAnim.getAnim() > 0.004f ? 1.0f : 0.0f;
        this.bindHoldAnim.speed = 0.1f;
        this.nanoBindParticlesRemoveAuto();
    }

    @Override
    public void keyPressed(int key) {
        super.keyPressed(key);
        if (this.binding && !this.holdBindTimer.hasReached(50.0) && key != 42 && key != 56 && key != 58 && key != 1) {
            this.keyBindToSet = key;
        }
        if (!ClickGuiScreen.colose) {
            this.sets.forEach(set -> set.keyPressed(key));
        }
    }

    public void onGuiClosed() {
        this.binding = false;
        this.keyBindToSet = -1;
        this.sets.forEach(set -> set.onGuiClosed());
    }

    private boolean hasBindConflict() {
        return this.module.getBind() != 0 && Client.clickGuiScreen.panels.stream().anyMatch(panel -> panel.mods.stream().anyMatch(mod -> mod != this && mod.module.getBind() != 0 && mod.module.getBind() == this.module.getBind()));
    }

    public Mod(Module module, boolean last, boolean first) {
        this.last = last;
        this.first = first;
        this.module = module;
        for (Settings setting : module.settings) {
            if (setting instanceof BoolSettings) {
                BoolSettings boolSet = (BoolSettings)setting;
                this.sets.add(new CheckBox(boolSet));
                continue;
            }
            if (setting instanceof FloatSettings) {
                FloatSettings floatSet = (FloatSettings)setting;
                this.sets.add(new Slider(floatSet));
                continue;
            }
            if (setting instanceof ModeSettings) {
                ModeSettings modeSet = (ModeSettings)setting;
                this.sets.add(new Modes(modeSet));
                continue;
            }
            if (!(setting instanceof ColorSettings)) continue;
            ColorSettings colorSet = (ColorSettings)setting;
            this.sets.add(new Colors(colorSet));
        }
        this.tgExt = 20.0f;
        float setsH = 0.0f;
        for (Set set : this.sets) {
            float h = 0.0f;
            if (set.setting.category == Settings.Category.Boolean) {
                h = set.getHeight();
            } else if (set.setting.category == Settings.Category.Float) {
                h = set.getHeight();
            } else if (set.setting.category == Settings.Category.String_Massive) {
                int n;
                Settings settings = set.setting;
                if (settings instanceof ModeSettings) {
                    ModeSettings mode = (ModeSettings)settings;
                    n = mode.modes.length;
                } else {
                    n = 0;
                }
                h = 18.0f + 13.0f * (float)n;
            } else if (set.setting.category == Settings.Category.Color) {
                h = 58.0f;
            }
            setsH += h + 1.5f;
        }
        this.tgH = setsH;
    }

    void addNanoBindParticle(float circleX, float circleY, float holdProgress, float ofRange) {
        this.nanoBindParticlesList.add(new NanoBindParticle(circleX, circleY, holdProgress, ofRange));
    }

    void nanoBindParticlesRemoveAuto() {
        if (!this.nanoBindParticlesList.isEmpty()) {
            this.nanoBindParticlesList.removeIf(NanoBindParticle::toRemove);
        }
    }

    void drawAllBindNanoParticles(float alphaPC) {
        this.nanoBindParticlesList.forEach(nanoBindParticle -> nanoBindParticle.drawAndMovement(alphaPC));
    }

    String getKeyName(int key, boolean staples) {
        return key == 0 ? (staples ? "[NONE]" : "-") : (staples ? "[" : "") + Keyboard.getKeyName((int)key).toUpperCase() + (staples ? "]" : "");
    }

    @Override
    public void drawScreen(float x, float y, int step, int mouseX, int mouseY, float partialTicks) {
        int clC;
        int cr;
        int modCol;
        int warnColor;
        boolean hasSets;
        boolean hover;
        boolean canRenderModule;
        this.height = this.getHeight();
        this.openAnim.getAnim();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        boolean bl = canRenderModule = !(x < -this.getWidth() - 40.0f) && !(x > (float)(sr.getScaledWidth() + 40)) && !(y < -this.height - 40.0f) && !(y > (float)(sr.getScaledHeight() + 40)) || MathUtils.getDifferenceOf(this.openAnim.anim, this.height) > (double)0.001f;
        if (!canRenderModule) {
            return;
        }
        String modulename = this.module.getName();
        int ScaledAlpha = (int)ClickGuiScreen.globalAlpha.anim + 1;
        float ScaledAlphaPercent = (float)ScaledAlpha / 255.0f;
        super.drawScreen(x, y, step, mouseX, mouseY, partialTicks);
        if (this.toggleAnim.to == 1.0f && (double)this.toggleAnim.getAnim() > 0.99) {
            this.toggleAnim.setAnim(1.0f);
            this.toggleAnim.to = 0.0f;
        }
        if (this.toggleAnim.to == 0.0f && (double)this.toggleAnim.getAnim() < 0.001) {
            this.toggleAnim.setAnim(0.0f);
        }
        int i = 20;
        String bindShowText = this.getKeyName(this.module.getBind(), true);
        this.openAnim.to = this.height;
        this.openAnim.speed = 0.15f + (Math.abs(MathUtils.getDifferenceOf(this.openAnim.anim, this.height)) < 5.5 || this.height < this.openAnim.anim && this.open ? 0.5f : 0.0f);
        this.bindShowAnim.to = Keyboard.isKeyDown((int)56) && this.module.bind != 0 ? 1.0f : 0.0f;
        boolean bl2 = hover = this.ishover(x, y, x + this.getWidth(), y + this.height, mouseX, mouseY) && !Client.clientColosUI.isHovered();
        if (this.prevHover != hover) {
            if (hover && !this.binding && !(this.bindingAnim.anim > 0.003f) && !this.open && !(MathUtils.getDifferenceOf(this.openAnim.anim, this.openAnim.to) > 0.0) && MathUtils.getDifferenceOf(ClickGuiScreen.scrollSmoothX, 0.0f) < 0.1 && MathUtils.getDifferenceOf(ClickGuiScreen.scrollSmoothY, 0.0f) < 0.1) {
                ClientTune.get.playGuiScreenModuleHoveringSong();
            }
            this.prevHover = hover;
        }
        float f = this.module.actived ? 0.05f : (this.alpha.speed = hover ? 0.1f : 0.02f);
        this.alpha.to = this.module.actived ? ScaledAlphaPercent * 255.0f : (hover || this.open || this.binding ? 100.0f : 0.0f);
        this.alpha.getAnim();
        this.sdvig.to = (float)(this.binding ? -1 : (this.ishover(x, y, x + this.getWidth(), y + 16.0f, mouseX, mouseY) && !this.open && !Client.clientColosUI.isHovered() && !Client.clickGuiScreen.moduleHasEqualSearch(this.module) ? 5 : 0)) + (Client.clickGuiScreen.moduleHasEqualSearch(this.module) ? (System.currentTimeMillis() % 700L >= 550L ? 3.5f : 1.0f) : 0.0f);
        this.sdvig.speed = this.ishover(x, y, x + this.getWidth(), y + 16.0f, mouseX, mouseY) || this.binding ? 0.3f : 0.05f;
        this.sdvig.getAnim();
        if (this.alpha.anim > 1.0f) {
            float alphaPC = MathUtils.clamp(this.alpha.anim / 255.0f * (ClickGuiScreen.globalAlpha.anim / 255.0f) * ClickGuiScreen.scale.anim, 0.0f, 255.0f);
            int col1 = ClickGuiScreen.getColor((int)y, this.module.category);
            int col2 = ClickGuiScreen.getColor((int)(this.getWidth() + y), this.module.category);
            int col3 = ClickGuiScreen.getColor((int)(this.getWidth() + y + 20.0f), this.module.category);
            int col4 = ClickGuiScreen.getColor((int)(y + 20.0f), this.module.category);
            int pCol1 = col1;
            int pCol2 = col2;
            int pCol3 = col3;
            int pCol4 = col4;
            float gc = 1.5f;
            col1 = ColorUtils.swapAlpha(col1, (float)ColorUtils.getAlphaFromColor(col1) * alphaPC / gc);
            col2 = ColorUtils.swapAlpha(col2, (float)ColorUtils.getAlphaFromColor(col2) * alphaPC / gc);
            col3 = ColorUtils.swapAlpha(col3, (float)ColorUtils.getAlphaFromColor(col3) * alphaPC / gc);
            col4 = ColorUtils.swapAlpha(col4, (float)ColorUtils.getAlphaFromColor(col4) * alphaPC / gc);
            if (this.openAnim.anim > 21.0f) {
                float radius = (float)MathUtils.clamp(MathUtils.getDifferenceOf(this.openAnim.anim, 21.0f), 0.0, 2.5);
                StencilUtil.initStencilToWrite();
                RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x + 2.5f, y + 17.5f, x + this.getWidth() - 1.5f, y + this.openAnim.anim - 1.5f, radius, 0.0f, -1, -1, -1, -1, false, true, false);
                StencilUtil.readStencilBuffer(1);
                RenderUtils.drawInsideFullRoundedFullGradientShadowRectWithBloomBool(x + 2.5f, y + 17.5f, x + this.getWidth() - 1.5f, y + this.openAnim.anim - 1.5f, 0.0f, radius, col1, col2, col3, col4, true);
                StencilUtil.readStencilBuffer(0);
            }
            if (!this.last && !this.first) {
                RenderUtils.drawFullGradientRectPro(x + 1.0f, y + 0.5f, x + this.getWidth() + 0.5f, y + this.openAnim.anim + 1.0f, col4, col3, col2, col1, true);
                if ((double)this.sdvig.anim > 0.25) {
                    float xs;
                    float centerDiff;
                    float ys;
                    float h = 17.0f;
                    RenderUtils.glRenderStart();
                    GL11.glPointSize((float)0.25f);
                    GL11.glBegin((int)0);
                    for (ys = y + 1.0f; ys < y + h; ys += 0.5f) {
                        centerDiff = (ys - (y + 0.5f)) / (h - 1.0f);
                        centerDiff = (float)MathUtils.easeInOutQuadWave(centerDiff);
                        xs = x + 1.5f + MathUtils.clamp(this.sdvig.anim / 1.5f - 0.5f, 0.0f, 10.0f) * centerDiff;
                        RenderUtils.glColor(ColorUtils.swapAlpha(-1, 255.0f * alphaPC * MathUtils.clamp(this.sdvig.anim / 3.0f, 0.0f, 1.0f) * MathUtils.clamp(0.5f + centerDiff / 2.0f, 0.0f, 1.0f)));
                        GL11.glVertex2d((double)xs, (double)ys);
                    }
                    GL11.glEnd();
                    GL11.glPointSize((float)1.0f);
                    GL11.glBegin((int)9);
                    RenderUtils.glColor(ColorUtils.swapAlpha(-1, this.alpha.anim / 4.0f * alphaPC * MathUtils.clamp(this.sdvig.anim / 2.0f, 0.0f, 1.0f)));
                    GL11.glVertex2d((double)(x + 1.0f), (double)(y + 0.5f));
                    for (ys = y + 0.5f; ys < y + 0.5f + h; ys += 0.5f) {
                        centerDiff = (ys - (y + 0.5f)) / h;
                        centerDiff = (float)MathUtils.easeInOutQuadWave(centerDiff);
                        xs = x + 1.0f + this.sdvig.anim / 1.5f * centerDiff;
                        GL11.glVertex2d((double)xs, (double)ys);
                    }
                    GL11.glVertex2d((double)(x + 1.0f), (double)(y + 0.5f + h));
                    GL11.glEnd();
                    RenderUtils.glRenderStop();
                }
            }
            if (this.last) {
                final float r = 3.0f;
                RenderUtils.drawFullGradientRectPro(x + 1.0f, y + 0.5f, x + this.getWidth() + 0.5f, y + this.openAnim.anim + 1.0f - r, col4, col3, col2, col1, true);
                RenderUtils.drawFullGradientRectPro(x + 1.0f + r, y + this.openAnim.anim + 1.0f - r, x + this.getWidth() + 0.5f - r, y + this.openAnim.anim + 1.0f, col4, col3, col3, col4, true);
                RenderUtils.drawCroneShadow(x + 1.0f + r, y + this.openAnim.anim + 1.0f - r, -90, 0, 0.0f, r - 0.5f, col4, col4, true);
                RenderUtils.drawCroneShadow(x + 1.0f + r, y + this.openAnim.anim + 1.0f - r, -90, 0, r - 0.5f, 0.5f, col4, ColorUtils.swapAlpha(col4, 0.0f), true);
                RenderUtils.drawCroneShadow(x + this.getWidth() + 0.5f - r, y + this.openAnim.anim + 1.0f - r, 0, 90, 0.0f, r - 0.5f, col3, col3, true);
                RenderUtils.drawCroneShadow(x + this.getWidth() + 0.5f - r, y + this.openAnim.anim + 1.0f - r, 0, 90, r - 0.5f, 0.5f, col3, ColorUtils.swapAlpha(col3, 0.0f), true);
            } else if (this.first) {
                final float r = 3.0f;
                RenderUtils.drawFullGradientRectPro(x + 1.0f, y + 0.5f + r, x + this.getWidth() + 0.5f, y + this.openAnim.anim + 1.0f, col4, col3, col2, col1, true);
                RenderUtils.drawFullGradientRectPro(x + 1.0f + r, y + 0.5f, x + this.getWidth() + 0.5f - r, y + 0.5f + r, col1, col2, col2, col1, true);
                RenderUtils.drawCroneShadow(x + 1.0f + r, y + 0.5f + r, 180, 270, 0.0f, r - 0.5f, col1, col1, true);
                RenderUtils.drawCroneShadow(x + 1.0f + r, y + 0.5f + r, 180, 270, r - 0.5f, 0.5f, col1, ColorUtils.swapAlpha(col1, 0.0f), true);
                RenderUtils.drawCroneShadow(x + this.getWidth() + 0.5f - r, y + 0.5f + r, -270, -180, 0.0f, r - 0.5f, col2, col2, true);
                RenderUtils.drawCroneShadow(x + this.getWidth() + 0.5f - r, y + 0.5f + r, -270, -180, r - 0.5f, 0.5f, col2, ColorUtils.swapAlpha(col2, 0.0f), true);
            }
            if (this.toggleAnim.getAnim() != 0.0f && this.toggleAnim.to != 0.0f) {
                float togPC = this.toggleAnim.anim;
                float togAlphaPC = ((double)this.toggleAnim.anim > 0.5 ? 1.0f - this.toggleAnim.anim : this.toggleAnim.anim) * 4.0f;
                togAlphaPC = togAlphaPC > 1.0f ? 1.0f : togAlphaPC;
                int togColor = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(col1, this.module.isLocked() ? ColorUtils.getColor(255, 0, 0) : -1, 0.4f), Math.min(ScaledAlphaPercent * ScaledAlphaPercent * togAlphaPC * this.alpha.anim * (this.module.isLocked() ? 6.0f : 1.0f), 255.0f));
                if (ColorUtils.getAlphaFromColor(togColor) >= 1) {
                    float x1 = x + 1.0f;
                    float x2 = x + this.getWidth() + 0.5f;
                    float x2WPC = x1 + (x2 - x1) * (this.module.actived ? togPC : 1.0f - togPC);
                    RenderUtils.drawAlphedSideways(x1, y + 0.5f, x2WPC, y + this.openAnim.anim + 1.0f, this.module.actived ? togColor : 0, togColor, true);
                    RenderUtils.drawAlphedSideways(x2WPC, y + 0.5f, x2, y + this.openAnim.anim + 1.0f, togColor, 0, true);
                    if (this.module.actived) {
                        RenderUtils.drawAlphedSideways(x2WPC, y + 0.5f, x2, y + 1.5f, ColorUtils.swapAlpha(togColor, 0.0f), togColor, true);
                        RenderUtils.drawAlphedSideways(x2WPC, y + this.openAnim.anim, x2, y + this.openAnim.anim + 1.0f, ColorUtils.swapAlpha(togColor, 0.0f), togColor, true);
                        RenderUtils.drawAlphedSideways(x2 - 1.0f, y + 1.5f, x2, y + this.openAnim.anim, togColor, togColor, true);
                    } else {
                        RenderUtils.drawAlphedSideways(x1, y + 0.5f, x2WPC, y + 1.5f, togColor, ColorUtils.swapAlpha(togColor, 0.0f), true);
                        RenderUtils.drawAlphedSideways(x1, y + this.openAnim.anim, x2WPC, y + this.openAnim.anim + 1.0f, togColor, ColorUtils.swapAlpha(togColor, 0.0f), true);
                        RenderUtils.drawAlphedSideways(x1, y + 1.5f, x1 + 1.0f, y + this.openAnim.anim, togColor, togColor, true);
                    }
                }
            }
            StencilUtil.readStencilBuffer(1);
            if (this.openAnim.anim > 21.0f) {
                this.tgExt = 16.0f;
                if (this.triangleGroup == null) {
                    this.triangleGroup = TriangleGroup.gen(-this.tgExt / 2.0f, -this.tgExt - 12.0f, this.getWidth() + this.tgExt * 1.5f, this.tgH + this.tgExt + 60.0f, this.tgExt, 0.01f, 100L, 1800L, 0.75f);
                }
            } else if (this.triangleGroup != null) {
                this.triangleGroup = null;
            }
            if (this.openAnim.anim > 21.0f) {
                if (this.triangleGroup != null) {
                    float togAlphaPC = this.toggleAnim.to == 0.0f ? 0.0f : ((double)this.toggleAnim.anim > 0.5 ? 1.0f - this.toggleAnim.anim : this.toggleAnim.anim) * 4.0f;
                    togAlphaPC = togAlphaPC > 1.0f ? 1.0f : togAlphaPC;
                    float tLeft = this.module.actived ? 0.0f : togAlphaPC;
                    float tRight = this.module.actived ? togAlphaPC : 0.0f;
                    this.triangleGroup.setColors(ColorUtils.getOverallColorFrom(pCol1, ColorUtils.swapAlpha(-1, ColorUtils.getAlphaFromColor(pCol1)), tLeft / 2.0f), ColorUtils.getOverallColorFrom(pCol2, ColorUtils.swapAlpha(-1, ColorUtils.getAlphaFromColor(pCol2)), tRight / 3.0f), ColorUtils.getOverallColorFrom(pCol3, ColorUtils.swapAlpha(-1, ColorUtils.getAlphaFromColor(pCol3)), tRight / 3.0f), ColorUtils.getOverallColorFrom(pCol4, ColorUtils.swapAlpha(-1, ColorUtils.getAlphaFromColor(pCol4)), tLeft / 2.0f));
                    GL11.glTranslated((double)x, (double)y, (double)0.0);
                    this.triangleGroup.drawAllInZone(-10.0f, 16.0f, this.getWidth() + 20.0f, this.height + 12.0f, alphaPC / 5.0f, true);
                    GL11.glTranslated((double)(-x), (double)(-y), (double)0.0);
                }
                int n = i = this.module.isLocked() ? 35 : 19;
                if (this.module.isLocked()) {
                    Fonts.neverlose500_18.drawString("Settings was locked", x + this.getWidth() / 2.0f - (float)Fonts.neverlose500_18.getStringWidth("Settings was locked") / 2.0f, y + 24.5f, ColorUtils.getColor(155, (int)(155.0f * ScaledAlphaPercent * ScaledAlphaPercent)));
                } else {
                    for (Set set2 : this.sets) {
                        if (!set2.setting.isVisible()) continue;
                        float h = set2.getHeight();
                        if ((float)i + h / 4.0f < this.openAnim.anim && y + (float)i >= -h && y + (float)i <= (float)(sr.getScaledHeight() + 1) && x + set2.getWidth() >= 0.0f && x <= (float)(sr.getScaledWidth() + 1)) {
                            set2.drawScreen(x + 1.0f, y + (float)i, step + 2, mouseX, mouseY, partialTicks);
                        }
                        i = (int)((float)i + (h + 1.0f));
                    }
                }
            }
            StencilUtil.uninitStencilBuffer();
        }
        this.updateBinding();
        float bindX = x + this.getWidth() - 15.5f;
        float bindY = y + 1.0f;
        float bindX2 = x + this.getWidth();
        float bindY2 = y + 16.5f;
        float bindW = bindX2 - bindX;
        float bindH = bindY2 - bindY;
        float bindCircleX = bindX + bindW / 2.0f;
        float bindCircleY = bindY + bindH / 2.0f;
        float bindAnim = this.bindingAnim.getAnim();
        if (bindAnim > 0.01f) {
            float bindAlpha = bindAnim * 255.0f * ScaledAlphaPercent * ScaledAlphaPercent;
            float effectAPC = this.bindWaveAnim.getAnim() * ScaledAlphaPercent * ScaledAlphaPercent;
            this.bindWaveAnim.speed = 0.025f;
            int bindCircleColor = ColorUtils.swapAlpha(-1, bindAlpha);
            int bindCircleBGColor = ColorUtils.swapAlpha(0, bindAlpha / 4.25f);
            int bindTextColor = ColorUtils.swapAlpha(-1, bindAlpha);
            float holdProgress = this.bindHoldAnim.getAnim();
            if ((double)holdProgress < 0.025) {
                holdProgress = 0.0f;
            }
            int bindCircleProrgress360 = (int)(360.0f * holdProgress);
            float bindCircleWidth = 1.0f + 2.5f * holdProgress;
            float bindCircleBGWidth = 1.5f + 2.5f * holdProgress;
            float bindProgressCircleRange = bindW * bindAnim / 2.0f - bindCircleWidth / 2.0f;
            float bindProgressCircleBGRange = bindW * bindAnim / 2.0f - bindCircleBGWidth / 2.0f;
            CFontRenderer bindFont = Fonts.comfortaaBold_14;
            String moduleBindText = this.getKeyName(this.module.getBind(), false).replace("-", "");
            if (moduleBindText.length() > 2) {
                moduleBindText = MathUtils.getStringPercent(moduleBindText, bindAnim * 2.0f);
            }
            float bindTextW = bindFont.getStringWidth(moduleBindText);
            float bindTextX = MathUtils.clamp(bindX + bindW / 2.0f - bindTextW / 2.0f, x, x + (moduleBindText.length() > 2 ? this.getWidth() - (bindW + 2.0f + bindTextW) : this.getWidth() - bindTextW - 1.0f));
            float bindTextY = bindY + bindH / 2.0f - 1.5f;
            if (bindAlpha >= 33.0f) {
                bindFont.drawStringWithOutline(moduleBindText, bindTextX, bindTextY, bindTextColor);
            }
            if ((double)holdProgress > 0.05) {
                this.addNanoBindParticle(0.0f, 0.0f, holdProgress, bindProgressCircleRange);
                this.addNanoBindParticle(0.0f, 0.0f, holdProgress + 0.9444444f, bindProgressCircleRange);
            }
            if (effectAPC <= 0.01f && moduleBindText.isEmpty()) {
                this.addNanoBindParticle(0.0f, 0.0f, 0.0f, 0.0f);
            }
            RenderUtils.drawClientCircleWithOverallToColor(bindCircleX, bindCircleY, bindProgressCircleRange, 359.0f, bindCircleBGWidth, bindAlpha / 255.0f, bindCircleBGColor, 1.0f);
            if ((float)bindCircleProrgress360 > 1.0f && this.binding) {
                RenderUtils.drawClientCircleWithOverallToColor(bindCircleX, bindCircleY, bindProgressCircleRange, bindCircleProrgress360, bindCircleWidth, bindAlpha / 255.0f, bindCircleColor, 1.0f);
            }
            if (effectAPC > 0.01f) {
                StencilUtil.uninitStencilBuffer();
                float effectAlpha = (double)effectAPC > 0.5 ? 1.0f - effectAPC : effectAPC;
                effectAlpha = effectAlpha < 0.0f ? 0.0f : ((effectAlpha *= 2.0f) > 1.0f ? 1.0f : effectAPC);
                bindCircleColor = ColorUtils.swapAlpha(-1, bindAlpha * effectAlpha);
                for (int ii = 0; ii <= 12; ++ii) {
                    this.addNanoBindParticle(0.0f, 0.0f, (float)ii / 12.0f - 0.083333336f * (float)(MathUtils.easeOutElastic(effectAlpha) + MathUtils.easeInCircle(effectAlpha)) / 2.0f * 6.0f, bindProgressCircleRange - 3.0f + (1.0f - effectAPC * effectAPC) * 8.0f + 10.0f * ((double)effectAlpha > 0.5 ? 1.0f - effectAlpha : effectAlpha) * 2.0f);
                }
                float bindCircleEffWidth = 1.0f + 14.0f * (float)MathUtils.easeInOutQuadWave(effectAlpha);
                float bindEffectCircleRange = bindProgressCircleBGRange * 2.0f * effectAPC;
                int effCol = ColorUtils.swapAlpha(ColorUtils.swapDark(bindCircleColor, effectAPC), (float)ColorUtils.getAlphaFromColor(bindCircleColor) * effectAPC);
                RenderUtils.drawClientCircleWithOverallToColor(bindCircleX, bindCircleY, bindEffectCircleRange, 359.0f, bindCircleEffWidth, effectAPC, effCol, 1.0f);
                if (bindAlpha >= 33.0f) {
                    String newModuleBindName = this.getKeyName(this.keyBindToSet, false).replace("DELETE", "");
                    float bindNewTextW = bindFont.getStringWidth(newModuleBindName);
                    float bindTextNewX = MathUtils.clamp(bindX + bindW / 2.0f - bindTextW / 2.0f, x, x + (moduleBindText.length() > 2 ? this.getWidth() - (bindW + 2.0f + bindNewTextW) : this.getWidth() - bindNewTextW - 1.0f));
                    GL11.glPushMatrix();
                    RenderUtils.customScaledObject2D(bindX, bindY, bindW, bindH, 1.0f + effectAPC * 4.0f);
                    RenderUtils.customRotatedObject2D(bindX, bindY, bindW, bindH, -effectAPC * 30.0f);
                    if (bindAlpha * effectAlpha >= 33.0f) {
                        bindFont.drawStringWithShadow(newModuleBindName, bindTextNewX, bindTextY, ColorUtils.swapAlpha(-1, bindAlpha * effectAlpha));
                    }
                    GL11.glPopMatrix();
                }
            }
        }
        GL11.glTranslated((double)bindCircleX, (double)bindCircleY, (double)0.0);
        this.drawAllBindNanoParticles(MathUtils.clamp(ClickGuiScreen.globalAlpha.anim / 255.0f * ClickGuiScreen.scale.anim, 0.0f, 1.0f));
        GL11.glTranslated((double)(-bindCircleX), (double)(-bindCircleY), (double)0.0);
        float bindDePC = 1.0f - bindAnim;
        boolean bl3 = hasSets = this.module.isVisible() && this.sets.stream().anyMatch(set -> set.setting.isVisible());
        if (hasSets && (float)((int)(this.alpha.anim * 0.87058824f + 36.0f)) * bindDePC > 32.0f) {
            float cur;
            GL11.glPushMatrix();
            int setsCol = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), MathUtils.clamp((this.alpha.anim * 0.87058824f * ScaledAlphaPercent + 45.0f * ScaledAlphaPercent) * bindDePC, 2.0f, 255.0f));
            int setsColBG = ColorUtils.swapAlpha(0, MathUtils.clamp(this.alpha.anim * 0.87058824f * ScaledAlphaPercent * bindDePC / 5.5f, 0.0f, 255.0f));
            float settsX = x + this.getWidth() - 8.0f + 0.5f;
            float settsY = y + 9.0f + 0.5f;
            this.setsAnim.to = cur = (float)(this.open ? 360 : 0);
            this.setsAnim.getAnim();
            RenderUtils.drawAlphedRect(x + this.getWidth() - 16.5f, y + 1.0f, x + this.getWidth(), y + 16.5f, setsColBG);
            RenderUtils.drawSmoothCircle(x + this.getWidth() - 8.0f, y + 9.0f, 5.25f + (this.open ? 1.5f : 0.0f), setsColBG);
            RenderUtils.customScaledObject2D(settsX, settsY, 0.0f, 0.0f, ScaledAlphaPercent * ScaledAlphaPercent * bindDePC);
            if (MathUtils.getDifferenceOf(cur, this.setsAnim.anim) >= 1.0) {
                RenderUtils.customRotatedObject2D(settsX, settsY, 0.0f, 0.0f, this.setsAnim.anim);
            }
            if (ColorUtils.getAlphaFromColor(setsCol) >= 32) {
                Fonts.iconswex_24.drawStringWithShadow("h", x + this.getWidth() - 12.0f, y + 8.0f, setsCol);
            }
            RenderUtils.customScaledObject2D(settsX, settsY, 0.0f, 0.0f, 1.0f / (ScaledAlphaPercent * ScaledAlphaPercent * bindDePC));
            if (MathUtils.getDifferenceOf(cur, this.setsAnim.anim) >= 1.0) {
                RenderUtils.customRotatedObject2D(settsX, settsY, 0.0f, 0.0f, -this.setsAnim.anim);
            }
            GL11.glPopMatrix();
            if (this.open) {
                this.addNanoBindParticle(0.0f, 0.0f, (float)Math.random(), 2.5f);
            }
        }
        this.bindConflictAnim.to = this.hasBindConflict() ? 1.0f : 0.0f;
        float bindConflictAnim = this.bindConflictAnim.getAnim();
        if ((double)bindConflictAnim > 0.03 && ColorUtils.getAlphaFromColor(warnColor = ColorUtils.swapAlpha(-1, MathUtils.clamp(this.alpha.anim * ScaledAlphaPercent * ScaledAlphaPercent, 85.0f, 255.0f))) >= 1) {
            float warnSize = 18.0f;
            float warnX = x + this.getWidth() - 0.5f - 16.0f * (hasSets ? 1.0f : bindAnim) - warnSize;
            float warnY = y - 0.5f;
            int pc90to360 = (int)((float)(System.currentTimeMillis() % 2000L) / 500.0f + 500.0f) * 90;
            this.bindConflictAngleAnim.to = pc90to360;
            float bindConflictAngleAnim = this.bindConflictAngleAnim.getAngleAnim();
            GL11.glPushMatrix();
            RenderUtils.customScaledObject2D(warnX, warnY, warnSize, warnSize, bindConflictAnim);
            RenderUtils.customRotatedObject2D(warnX, warnY, warnSize, warnSize, bindConflictAngleAnim);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ClickGuiScreen.BOUND_CONFLICT);
            GL11.glTranslated((double)warnX, (double)warnY, (double)0.0);
            RenderUtils.glColor(warnColor);
            GL11.glBlendFunc((int)770, (int)32772);
            Gui.drawModalRectWithCustomSizedTexture((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)warnSize, (float)warnSize, (float)warnSize, (float)warnSize);
            GL11.glBlendFunc((int)770, (int)771);
            GlStateManager.resetColor();
            GL11.glPopMatrix();
        }
        if (ColorUtils.getAlphaFromColor(modCol = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(cr = ColorUtils.getColor((int)(85.0f + this.alpha.anim / 255.0f * 110.0f)), clC = ClickGuiScreen.getColor((int)(y + 20.0f), this.module.category), 0.15f), MathUtils.clamp((float)ColorUtils.getAlphaFromColor(cr) * ScaledAlphaPercent * ScaledAlphaPercent, 0.0f, 255.0f))) >= 33) {
            float moduleNameStringX = x + (this.sdvig.anim + (float)(Fonts.roboto_16.getStringWidth(bindShowText) + 5) * this.bindShowAnim.getAnim()) + 4.0f;
            float moduleNameStringY = y + 8.75f - (float)(Fonts.roboto_16.getHeight() / 2) - 0.5f;
            moduleNameStringX = (float)((int)(moduleNameStringX * 2.0f)) / 2.0f;
            moduleNameStringY = (float)((int)(moduleNameStringY * 2.0f)) / 2.0f;
            String modNameString = (Client.clickGuiScreen.moduleHasEqualSearch(this.module) ? "\u00a7c->\u00a7e " : "") + modulename;
            if (this.module.isActived()) {
                Fonts.comfortaaRegular_16.drawStringWithShadow(modNameString, moduleNameStringX, moduleNameStringY, modCol);
            } else {
                Fonts.comfortaaRegular_16.drawString(modNameString, moduleNameStringX, moduleNameStringY, modCol);
            }
            if (this.module.isLocked()) {
                this.drawIcon(moduleNameStringX + (float)Fonts.comfortaaRegular_16.getStringWidth(modNameString) + 1.0f, moduleNameStringY - 2.0f, ScaledAlphaPercent, "locked");
            } else if (this.module.isBetaModule()) {
                this.drawIcon(moduleNameStringX + (float)Fonts.comfortaaRegular_16.getStringWidth(modNameString) + 1.0f, moduleNameStringY - 2.0f, ScaledAlphaPercent, "beta");
            }
        }
        if (Client.clickGuiScreen.moduleHasEqualSearch(this.module)) {
            ClickGuiScreen.spawnParticleRandPos(x + this.getWidth() / 2.0f, y + 8.0f, this.getWidth() / 3.0f, 4.0f, 30L);
        }
        if (this.bindShowAnim.getAnim() > 0.1f) {
            float bindWidthText = Fonts.roboto_16.getStringWidth(bindShowText) + 5;
            float bindExtX = -bindWidthText * (1.0f - this.bindShowAnim.anim);
            StencilUtil.initStencilToWrite();
            RenderUtils.drawRect(x + 2.0f, y, x + this.getWidth(), y + 16.0f, -1);
            StencilUtil.readStencilBuffer(1);
            float bindX1 = MathUtils.clamp(x + 2.5f + bindExtX, x + 1.0f, x + this.getWidth());
            float bindX2S = MathUtils.clamp(x + 3.0f + bindExtX + (float)Fonts.roboto_16.getStringWidth(bindShowText) + 2.5f, x + 1.0f, x + this.getWidth());
            RenderUtils.drawAlphedRect(bindX1, y + 2.0f, bindX2S, y + 15.5f, ColorUtils.getColor(0, (int)(this.alpha.anim / 7.0f)));
            RenderUtils.drawLightContureRectSmooth(bindX1, y + 2.0f, bindX2S, y + 15.5f, ColorUtils.getColor(0, (int)(this.alpha.anim / 3.0f)));
            Fonts.roboto_16.drawString(bindShowText, x + 4.0f + bindExtX, y + 9.5f - (float)Fonts.roboto_16.getHeight() / 2.0f, ColorUtils.swapAlpha(ColorUtils.getColor(255, 255, 80), (int)(255.0f * ScaledAlphaPercent)));
            StencilUtil.uninitStencilBuffer();
        }
        this.setupDescriptions(x, y, mouseX, mouseY);
    }

    void setupDescriptions(float x, float y, int mouseX, int mouseY) {
        if (ClickGui.instance.Descriptions.getBool()) {
            xn = mouseX + 15;
            yn = mouseY + 15;
            boolean render = false;
            for (Panel panel : Client.clickGuiScreen.panels) {
                if (panel.dragging) {
                    return;
                }
                if (!panel.open || !(xn - 15.0f > panel.X) || !(xn - 15.0f < panel.posX.anim + panel.getWidth()) || !(yn - 15.0f > panel.Y + 24.0f) || !(yn - 15.0f < panel.posY.anim + panel.animOpen.anim)) continue;
                render = true;
            }
            if (this.ishover(x, y, x + this.getWidth(), y + 16.0f, mouseX, mouseY) && this.module != null && this.module.name != null && render) {
                ClickGuiScreen.descriptionName = this.module.name;
                ClickGuiScreen.colorCategory = this.module.category;
                String description = "";
                for (String descriptions : Client.moduleManager.getModuleDescriptionList()) {
                    if (ClickGuiScreen.descriptionName == null || !descriptions.startsWith(ClickGuiScreen.descriptionName)) continue;
                    description = descriptions.replace(ClickGuiScreen.descriptionName, "");
                }
                descript = description;
                if (alphaD < 255.0f) {
                    alphaD = 255.0f;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.wantToClick = true;
        this.wantToClick2 = true;
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (this.open) {
            for (Set set : this.sets) {
                set.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(x, y, mouseX, mouseY, mouseButton);
        boolean lock = this.module.isLocked();
        if (this.wantToClick || this.wantToClick2) {
            if (this.ishover(x + 1, y - 2, (float)x + this.getWidth(), y + 14, mouseX, mouseY)) {
                if (mouseButton == 2 && !lock) {
                    boolean bl = this.binding = !this.binding;
                    if (this.binding && this.module.getBind() == 0) {
                        for (int s = 0; s < 20; ++s) {
                            float r = (float)Math.random();
                            this.addNanoBindParticle(0.0f, 0.0f, r, 2.0f);
                            this.addNanoBindParticle(0.0f, 0.0f, r, -3.0f);
                        }
                    }
                    ClientTune.get.playGuiModuleBindingToggleSong(this.binding);
                }
                if (this.ishover((float)x + this.getWidth() - 16.0f, y - 2, (float)x + this.getWidth(), y + 14, mouseX, mouseY) && (mouseButton == 1 || mouseButton == 0) && this.module.settings.stream().filter(Settings::isVisible).collect(Collectors.toList()).size() > 0) {
                    boolean bl = this.open = !this.open;
                    if (this.open) {
                        Client.clickGuiScreen.panels.stream().filter(panel -> panel.open).forEach(panel -> panel.mods.stream().filter(mod -> mod != this).filter(mod -> mod.open).forEach(mod -> {
                            mod.open = false;
                            ClientTune.get.playGuiModuleOpenOrCloseSong(false);
                        }));
                    } else {
                        this.sets.stream().map(Set::getHasModes).filter(Objects::nonNull).filter(mode -> mode.open).forEach(mode -> {
                            mode.open = false;
                            mode.playClose = true;
                        });
                        this.sets.stream().map(Set::getHasColors).filter(Objects::nonNull).filter(color -> color.open).forEach(color -> {
                            color.open = false;
                            color.playClose = true;
                        });
                    }
                    ClientTune.get.playGuiModuleOpenOrCloseSong(this.open);
                    if (this.binding) {
                        this.binding = false;
                        ClientTune.get.playGuiModuleBindingToggleSong(this.binding);
                    }
                }
                if (!(this.ishover((float)x + this.getWidth() - 16.0f, y - 2, (float)x + this.getWidth(), y + 14, mouseX, mouseY) && this.module.settings.stream().filter(Settings::isVisible).collect(Collectors.toList()).size() != 0 || mouseButton != 0)) {
                    this.module.toggle(!this.module.actived);
                    if (this.module.isActived() && this.open) {
                        this.open = false;
                        ClientTune.get.playGuiModuleOpenOrCloseSong(false);
                        this.height = this.getHeight();
                    }
                    this.toggleAnim.setAnim(0.0f);
                    this.toggleAnim.to = 1.0f;
                    if (this.binding) {
                        this.binding = false;
                    }
                    float deOff = 1.0f;
                    float deX = this.getWidth() - 7.75f;
                    float deY = 8.25f;
                    float detW = this.getWidth() + deOff * 2.0f;
                    float detH = this.height + deOff * 2.0f;
                    int s = 0;
                    while ((float)s < detW / 1.5f) {
                        this.addNanoBindParticle(detW * (float)Math.random() - deX - deOff, -deY - deOff, 0.0f, 1.0f);
                        this.addNanoBindParticle(detW * (float)Math.random() - deX - deOff, detH - deY - deOff, 0.0f, 1.0f);
                        ++s;
                    }
                    s = 0;
                    while ((float)s < detH / 1.5f) {
                        this.addNanoBindParticle(-deX - deOff, detH * (float)Math.random() - deY - deOff, 0.0f, 1.0f);
                        this.addNanoBindParticle(detW - deX - deOff, detH * (float)Math.random() - deY - deOff, 0.0f, 1.0f);
                        ++s;
                    }
                }
            }
            if (this.open) {
                int i = 16;
                for (Set set : this.sets) {
                    if (!set.setting.isVisible()) continue;
                    set.mouseClicked(x + 1, y + i, mouseX, mouseY, mouseButton);
                    i = (int)((float)i + (set.getHeight() + 1.0f));
                }
            }
            if (this.ishover(x + 1, y - 2, (float)x + this.getWidth(), (float)y + this.getHeight() - 2.0f, mouseX, mouseY)) {
                this.wantToClick2 = false;
                this.wantToClick = false;
            }
        }
    }

    @Override
    public float getHeight() {
        int i = 16 + (this.open ? 6 : 0);
        if (this.open) {
            if (this.module.isLocked()) {
                i = (int)((float)i + 16.0f);
            } else {
                for (Set set : this.sets) {
                    i = (int)((float)i + (set != null && set.setting.isVisible() ? set.getHeight() + 1.0f : 0.0f));
                }
            }
        }
        return i;
    }

    private void drawIcon(float x, float y, float alphaPC, String str) {
        if ((double)alphaPC < 0.1) {
            return;
        }
        int bgColor = ColorUtils.getColor(9, 9, 17, 100.0f * alphaPC);
        int outBgColL = ColorUtils.getColor(180, 180, 130, 30.0f * alphaPC);
        int outBgColR = ColorUtils.getColor(180, 180, 130, 80.0f * alphaPC);
        CFontRenderer font = Fonts.mntsb_10;
        float strW = font.getStringWidth(str);
        float xOff = 0.5f;
        float yOff = 1.0f;
        float strH = (float)font.getHeight() + 1.5f;
        float x2 = x + xOff * 2.0f + strW;
        float y2 = y + yOff * 2.0f + strH;
        float delayOffset = 7.0f;
        float timeDelay = 700.0f * delayOffset;
        if (ColorUtils.getAlphaFromColor(bgColor) >= 20) {
            // empty if block
        }
        if (ColorUtils.getAlphaFromColor(outBgColL) >= 5) {
            RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(x, y, x2, y2, (y2 - y) / 2.0f, outBgColL, outBgColR, outBgColR, outBgColL, true);
        }
        float textX = x + xOff + 1.0f;
        float textY = y + yOff + 1.0f;
        int charIndex = 0;
        int fColor = ColorUtils.getColor(255, 255, 255, 60.0f * alphaPC);
        int sColor = ColorUtils.getColor(255, 190, 0, Math.min(255.0f * alphaPC, 255.0f));
        for (char theChar : str.toCharArray()) {
            String charStr = String.valueOf(theChar);
            float charW = font.getStringWidth(charStr);
            float timePC = (float)((System.currentTimeMillis() - (long)((int)(timeDelay * ((float)charIndex / (float)str.length()) / delayOffset))) % (long)((int)timeDelay)) / timeDelay * delayOffset;
            int textColor = ColorUtils.getOverallColorFrom(fColor, sColor, timePC = (float)MathUtils.easeInOutQuadWave(MathUtils.clamp(timePC, 0.0f, 1.0f)));
            if (ColorUtils.getAlphaFromColor(textColor) >= 33) {
                font.drawStringWithShadow(charStr, textX, textY, textColor);
            }
            ++charIndex;
            textX += charW;
        }
    }

    @Override
    public float getWidth() {
        return 118.5f;
    }

    class NanoBindParticle {
        float x;
        float y;
        float speed = (float)Math.random() / 22.25f;
        float radian = (float)Math.random() * 360.0f;
        float maxTime = 450.0f + (float)((int)(150.0 * Math.random()));
        long startTime = System.currentTimeMillis();

        NanoBindParticle(float circleX, float circleY, float holdProgress, float ofRange) {
            float radian = (float)Math.toRadians(holdProgress * 360.0f + 180.0f);
            if (holdProgress == 0.0f) {
                if (ofRange != 0.0f) {
                    this.speed /= 1.3f;
                }
                if (ofRange == 1.0f) {
                    this.speed /= 3.0f;
                }
                ofRange = 0.0f;
                this.maxTime /= 2.0f;
            } else {
                this.radian = (float)((double)radian / Math.PI * 180.0) - 60.0f - 10.0f + 20.0f * (float)Math.random();
            }
            this.x = circleX + (float)Math.sin(radian) * ofRange;
            this.y = circleY + (float)Math.cos(radian) * ofRange;
            this.speed *= ofRange / 4.0f + 6.0f;
        }

        float timePC() {
            return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / this.maxTime, 0.0f, 1.0f);
        }

        float alphaPC() {
            return 1.0f - this.timePC();
        }

        boolean toRemove() {
            return this.timePC() == 1.0f;
        }

        void drawAndMovement(float alphaPC) {
            this.x += (float)Math.sin(Math.toRadians(this.radian)) * this.speed;
            this.y += (float)Math.cos(Math.toRadians(this.radian)) * this.speed;
            if ((alphaPC *= this.alphaPC()) == 0.0f) {
                return;
            }
            int color = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(ClickGuiScreen.getColor((int)((this.x + this.y) * 3.0f), Mod.this.module.category), -1, 0.5f - alphaPC * alphaPC * 0.5f), 255.0f * (float)MathUtils.easeInOutQuad(alphaPC));
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)32772);
            GL11.glDisable((int)3553);
            GL11.glDisable((int)3008);
            GL11.glEnable((int)2832);
            GL11.glPointSize((float)(alphaPC * alphaPC * (this.maxTime / 1000.0f) * 11.0f));
            RenderUtils.glColor(color);
            GL11.glBegin((int)0);
            GL11.glVertex2d((double)this.x, (double)this.y);
            GL11.glEnd();
            GL11.glPointSize((float)1.0f);
            GL11.glEnable((int)3008);
            GL11.glEnable((int)3553);
            GL11.glBlendFunc((int)770, (int)771);
            GlStateManager.resetColor();
        }
    }
}
