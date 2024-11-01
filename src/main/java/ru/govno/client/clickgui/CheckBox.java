package ru.govno.client.clickgui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.clickgui.Set;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class CheckBox
extends Set {
    AnimationUtils anim2 = new AnimationUtils(0.0f, 0.0f, 0.06f);
    AnimationUtils toggleAnim = new AnimationUtils(0.0f, 0.0f, 0.12f);
    public boolean binding;
    int keyBindToSet;
    float bindingWidth;
    AnimationUtils bindingAnim = new AnimationUtils(0.0f, 0.0f, 0.1f);
    AnimationUtils bindHoldAnim = new AnimationUtils(0.0f, 0.0f, 0.1f);
    AnimationUtils bindWaveAnim = new AnimationUtils(0.0f, 0.0f, 0.05f);
    public TimerHelper holdBindTimer = new TimerHelper();
    BoolSettings setting;

    public CheckBox(BoolSettings setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void onGuiClosed() {
        this.binding = false;
        this.bindingAnim.to = 0.0f;
        this.keyBindToSet = -1;
    }

    float maxBindTime() {
        return this.keyBindToSet == 211 ? 550.0f : 650.0f;
    }

    void updateBinding() {
        if (ClickGuiScreen.colose && this.binding) {
            this.binding = false;
            this.bindingAnim.to = 0.0f;
        }
        if (!this.binding || this.keyBindToSet == -1 || !Keyboard.isKeyDown((int)this.keyBindToSet)) {
            this.holdBindTimer.reset();
        }
        if (this.binding) {
            if (this.keyBindToSet != -1 && this.holdBindTimer.hasReached(this.maxBindTime()) && this.bindHoldAnim.getAnim() > 0.9722222f) {
                int prevBind = this.setting.getBind();
                this.setting.setBind(this.keyBindToSet == 211 ? 0 : this.keyBindToSet);
                if (prevBind != this.setting.getBind()) {
                    ClientTune.get.playGuiModuleBindSong(this.setting.getBind() != 0);
                    this.bindWaveAnim.setAnim(1.0f);
                }
                this.binding = false;
            }
            this.bindHoldAnim.to = MathUtils.clamp((float)this.holdBindTimer.getTime() / this.maxBindTime(), 0.0f, 1.0f);
        } else {
            this.bindHoldAnim.setAnim(0.0f);
        }
        this.bindHoldAnim.speed = 0.15f;
        this.bindWaveAnim.speed = 0.04f;
        this.bindingAnim.getAnim();
        if (MathUtils.getDifferenceOf(this.bindingAnim.anim, this.bindingAnim.to) < 0.003) {
            this.bindingAnim.setAnim(this.bindingAnim.to);
        }
        this.bindingAnim.to = this.binding || this.bindWaveAnim.getAnim() > 0.004f ? 1.0f : 0.0f;
        this.bindHoldAnim.getAnim();
        this.bindHoldAnim.setAnim((double)this.bindHoldAnim.anim < 0.005 ? 0.0f : ((double)this.bindHoldAnim.anim > 0.999 ? 1.0f : this.bindHoldAnim.anim));
    }

    @Override
    public void drawScreen(float x, float y, int step, int mouseX, int mouseY, float partialTicks) {
        float anim;
        super.drawScreen(x, y, step, mouseX, mouseY, partialTicks);
        float scaledAlphaPercent = ClickGuiScreen.globalAlpha.anim / 255.0f;
        scaledAlphaPercent *= scaledAlphaPercent;
        this.anim2.to = anim = this.setting.getAnimation();
        this.anim2.speed = 0.0175f / (float)MathUtils.clamp(MathUtils.getDifferenceOf(this.anim2.getAnim(), anim), (double)0.1f, 2.0);
        if (this.toggleAnim.getAnim() > 1.0f) {
            this.toggleAnim.to = 0.0f;
            this.toggleAnim.setAnim(1.0f);
        }
        this.updateBinding();
        String setKeyBindName = Keyboard.getKeyName((int)this.setting.getBind()).replace("NONE", "");
        CFontRenderer boundFont = Fonts.neverlose500_13;
        float bindCircleSize = 10.0f * this.bindingAnim.anim;
        this.bindingWidth = (this.setting.getBind() != 0 ? (float)boundFont.getStringWidth(setKeyBindName) + 2.0f : 0.0f) * this.bindingAnim.anim + bindCircleSize;
        if (this.bindingWidth >= 0.5f) {
            int bindBGColor;
            float bindX = x + 4.0f;
            float bindX2 = bindX + this.bindingWidth;
            float round = MathUtils.clamp((bindX2 - bindX) / 4.0f, 0.0f, 2.0f);
            int outCol1 = ClickGuiScreen.getColor((int)((y - 15.0f) / 3.6f), this.setting.module.category);
            outCol1 = ColorUtils.swapAlpha(outCol1, 100.0f * scaledAlphaPercent * this.bindingAnim.anim);
            int outCol2 = ClickGuiScreen.getColor((int)((y - 5.0f) / 3.6f), this.setting.module.category);
            outCol2 = ColorUtils.swapAlpha(outCol2, 100.0f * scaledAlphaPercent * this.bindingAnim.anim);
            if (ColorUtils.getAlphaFromColor(outCol1) >= 10 && ColorUtils.getAlphaFromColor(outCol2) >= 10) {
                RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(bindX, y + 2.0f, bindX2, y + this.getHeight() - 2.0f, round, outCol1, outCol2, outCol2, outCol1, true);
            }
            if (ColorUtils.getAlphaFromColor(bindBGColor = ColorUtils.getColor(0, 0, 0, 45.0f * scaledAlphaPercent * this.bindingAnim.anim)) >= 10) {
                RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(bindX, y + 2.0f, bindX2, y + this.getHeight() - 2.0f, round, 0.5f, bindBGColor, bindBGColor, bindBGColor, bindBGColor, false, true, true);
            }
            String bindString = MathUtils.getStringPercent(setKeyBindName, this.bindingAnim.anim * (1.0f + bindCircleSize / (bindX2 - bindX)));
            int bindTextColor = ColorUtils.swapAlpha(-1, 255.0f * this.bindingAnim.anim * scaledAlphaPercent);
            float bindProgress = MathUtils.clamp(this.bindHoldAnim.anim + this.bindWaveAnim.anim, 0.0f, 1.0f);
            if ((double)bindProgress < 0.03) {
                bindProgress = 0.0f;
            }
            if (ColorUtils.getAlphaFromColor(bindTextColor) >= 33) {
                float dragRot = bindProgress * 100.0f % 50.0f / 50.0f;
                float dragStrengh = bindString.length() > 1 ? 10.0f : 45.0f;
                dragRot = -(dragStrengh / 2.0f * dragRot) + (float)MathUtils.easeInOutQuadWave(dragRot) * dragStrengh;
                if (MathUtils.getDifferenceOf(dragRot *= bindProgress, 0.0f) > 1.0) {
                    GL11.glPushMatrix();
                    RenderUtils.customRotatedObject2D(bindX + 1.5f, y, boundFont.getStringWidth(bindString), this.getHeight(), dragRot);
                }
                boundFont.drawStringWithShadow(bindString, bindX + 1.5f, y + 5.5f, bindTextColor);
                if ((double)this.bindWaveAnim.anim > 0.004) {
                    GL11.glPushMatrix();
                    float smoothAsWave = (float)MathUtils.easeInOutQuad(this.bindWaveAnim.anim);
                    float smoothWaveAsWave = (float)MathUtils.easeInOutQuadWave(this.bindWaveAnim.anim);
                    RenderUtils.customScaledObject2D(bindX + 1.5f, y, boundFont.getStringWidth(bindString), this.getHeight(), 1.0f + smoothAsWave * 4.0f);
                    int textEffectColor = ColorUtils.getOverallColorFrom(0, bindTextColor, MathUtils.clamp(smoothWaveAsWave * 2.0f, 0.0f, 1.0f));
                    if (ColorUtils.getAlphaFromColor(textEffectColor) >= 33) {
                        boundFont.drawStringWithShadow(bindString, bindX + 1.5f, y + 5.5f, textEffectColor);
                    }
                    GL11.glPopMatrix();
                }
                if (MathUtils.getDifferenceOf(dragRot, 0.0f) > 1.0) {
                    GL11.glPopMatrix();
                }
            }
            if (this.bindingAnim.to != 0.0f) {
                float timedWave = (float)((System.currentTimeMillis() - (long)((int)(y * 5.0f))) % 750L) / 750.0f;
                timedWave = (float)MathUtils.easeInCircle((timedWave > 0.5f ? 1.0f - timedWave : timedWave) * 2.0f);
                if ((timedWave = MathUtils.lerp(timedWave, 1.0f, MathUtils.clamp(this.bindHoldAnim.anim * 4.0f, 0.0f, 1.0f))) > 1.0f) {
                    timedWave = 1.0f;
                }
                int pointColor = ClickGuiScreen.getColor((int)((y - 10.0f) / 3.6f), this.setting.module.category);
                pointColor = ColorUtils.getOverallColorFrom(pointColor, ColorUtils.getColor(255, 255, 255), 0.25f + timedWave / 1.5f);
                pointColor = ColorUtils.swapAlpha(pointColor, 195.0f * scaledAlphaPercent * this.bindingAnim.anim);
                float circleX = bindX2 - bindCircleSize / 2.0f;
                float circleY = y + this.getHeight() / 2.0f;
                float circleR = bindCircleSize / (1.75f - bindProgress / 1.5f) * (0.75f + 0.25f * timedWave) / 2.0f;
                if (bindProgress == 0.0f || (double)this.bindWaveAnim.anim > 0.004) {
                    RenderUtils.drawSmoothCircle(circleX, circleY, MathUtils.clamp(circleR, 0.5f, 9.0f), (double)this.bindWaveAnim.anim > 0.004 ? ColorUtils.swapAlpha(pointColor, (float)ColorUtils.getAlphaFromColor(pointColor) * this.bindWaveAnim.anim) : pointColor);
                }
                RenderUtils.drawSmoothCircle(circleX, circleY, MathUtils.clamp(circleR / 1.25f, 0.5f, 9.0f), ColorUtils.toDark(pointColor, bindProgress / 2.0f));
                RenderUtils.drawSmoothCircle(circleX, circleY, MathUtils.clamp(circleR / 2.5f, 0.5f, 9.0f), ColorUtils.toDark(pointColor, 0.5f + bindProgress / 2.0f));
                if (bindProgress != 0.0f) {
                    int progressColor = ColorUtils.getOverallColorFrom(ClickGuiScreen.getColor((int)((y - 10.0f) / 3.6f), this.setting.module.category), -1, bindProgress);
                    progressColor = ColorUtils.swapAlpha(progressColor, (float)ColorUtils.getAlphaFromColor(progressColor) * scaledAlphaPercent * this.bindingAnim.anim * MathUtils.clamp(bindProgress * 2.0f, 0.0f, 1.0f));
                    RenderUtils.drawClientCircleWithOverallToColor(circleX, circleY, circleR, (double)this.bindWaveAnim.anim > 0.1 ? 360.0f : bindProgress * 361.0f, 1.0f + 0.5f * this.bindingAnim.anim * bindProgress, scaledAlphaPercent * this.bindingAnim.anim, progressColor, 1.0f);
                }
            }
        }
        this.toggleAnim.getAnim();
        float xOffset = this.getCheckBoxXOffset();
        float xPos = x + (this.ishover(x + xOffset, y + this.getHeight() / 2.0f - 6.0f, x + 21.0f + xOffset, y + this.getHeight() / 2.0f + 6.0f, mouseX, mouseY) && (double)this.bindingAnim.anim < 0.05 && !this.binding ? 6.5f : 6.0f) + this.bindingWidth;
        float yPos = y + 1.5f;
        float h = this.getHeight() - 3.0f;
        float w = 18.0f;
        float extX = 5.0f;
        float extY = 2.0f;
        float pX1 = xPos + 5.0f + 8.0f * anim;
        float pX2 = xPos + 5.0f + 8.0f * this.anim2.anim;
        float progX1 = pX1 < pX2 ? pX1 : pX2;
        float progX2 = pX1 > pX2 ? pX1 : pX2;
        int color = ClickGuiScreen.getColor((int)(y / 3.6f * 2.0f), this.setting.module.category);
        int offC = ColorUtils.getOverallColorFrom(ColorUtils.getColor(0, 0, 0, 255), color, this.anim2.anim / 3.0f + 0.33333334f);
        int onC = ColorUtils.swapAlpha(color, 255.0f);
        int colBG = ColorUtils.getOverallColorFrom(offC, onC, anim);
        int colBGShadow = ColorUtils.getOverallColorFrom(ColorUtils.getOverallColorFrom(offC, ColorUtils.getColor(0, 0, 0, 255), 0.5f), onC, 1.0f - this.anim2.anim);
        colBG = ColorUtils.swapAlpha(colBG, (float)ColorUtils.getAlphaFromColor(colBG) * scaledAlphaPercent);
        colBGShadow = ColorUtils.swapAlpha(colBGShadow, (float)ColorUtils.getAlphaFromColor(colBGShadow) * scaledAlphaPercent);
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(xPos, yPos, xPos + 18.0f, yPos + h, 4.0f, 0.5f, colBG, colBG, colBG, colBG, false, true, true);
        float r = (h - 4.0f) / 2.0f;
        RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(progX1 - r, yPos + 2.0f, progX2 + r, yPos + h - 2.0f, r, 0.5f + this.anim2.anim, colBGShadow, colBGShadow, colBGShadow, colBGShadow, false, true, true);
        CFontRenderer font = Fonts.comfortaaBold_14;
        if (255.0f * scaledAlphaPercent >= 33.0f) {
            float textX = xPos + 18.0f + 3.0f + this.toggleAnim.anim * 2.0f;
            float textY = yPos + 3.5f;
            int textColor = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0f * scaledAlphaPercent);
            for (char theChar : this.setting.getName().toCharArray()) {
                String charA = String.valueOf(theChar);
                float charW = (float)font.getStringWidth(charA) * 1.025f;
                if (textX + charW < x + this.getWidth() - 3.0f) {
                    font.drawString(charA, textX, textY, textColor);
                }
                textX += charW;
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(x, y, mouseX, mouseY, mouseButton);
        float xOffset = this.getCheckBoxXOffset();
        boolean hover = this.ishover((float)x + xOffset, (float)y + this.getHeight() / 2.0f - 6.0f, (float)x + 21.0f + xOffset, (float)y + this.getHeight() / 2.0f + 6.0f, mouseX, mouseY);
        if (hover && mouseButton == 0) {
            ClientTune.get.playGuiScreenCheckBox(this.setting.getBool());
            this.setting.toggleBool();
            this.toggleAnim.to = 1.1f;
            if (this.binding) {
                this.binding = false;
                ClientTune.get.playGuiModuleBindingToggleSong(false);
                this.bindingAnim.to = 0.0f;
                this.keyBindToSet = -1;
            }
        } else if (this.ishover((float)x + (this.binding ? 2.0f : xOffset), (float)y + this.getHeight() / 2.0f - 6.0f, (float)x + 21.0f + xOffset, (float)y + this.getHeight() / 2.0f + 6.0f, mouseX, mouseY) && mouseButton == 2) {
            this.binding = !this.binding;
            this.keyBindToSet = -1;
            this.bindingAnim.to = this.binding ? 1.0f : 0.0f;
            ClientTune.get.playGuiModuleBindingToggleSong(this.binding);
        }
    }

    @Override
    public void keyPressed(int key) {
        if (this.binding && !this.holdBindTimer.hasReached(50.0) && key != 42 && key != 56 && key != 58 && key != 1) {
            this.keyBindToSet = key;
        }
    }

    private float getCheckBoxXOffset() {
        return 3.5f + this.bindingWidth;
    }

    @Override
    public float getWidth() {
        return 118.0f;
    }

    @Override
    public float getHeight() {
        return 13.0f;
    }
}

