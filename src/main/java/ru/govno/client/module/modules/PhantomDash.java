package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import ru.govno.client.module.Module;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class PhantomDash
extends Module {
    private int pushTicks;
    private int prevPushTicks;
    private int dashTicks;
    private int slowingTicks;
    public static double tempSpeed = 1.0;

    private int getMaxDashTicks() {
        return 20;
    }

    private int getPushLimitTicks() {
        return 3;
    }

    public PhantomDash() {
        super("PhantomDash", 0, Module.Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        this.prevPushTicks = this.pushTicks++;
        if (Minecraft.player.isSneaking()) {
            if (this.pushTicks == 1) {
                this.slowingTrigger();
            }
        } else {
            if (this.pushTicks != 0 && this.pushTicks < this.getMaxDashTicks()) {
                this.dashingTrigger();
            }
            this.pushTicks = 0;
        }
        this.updateDashFactor();
    }

    private double speedFactor() {
        return 4.5;
    }

    private void slowingTrigger() {
        this.slowingTicks = this.getPushLimitTicks();
    }

    private void dashingTrigger() {
        if (this.pushTicks < this.getMaxDashTicks()) {
            this.dashTicks = (int)((float)this.getMaxDashTicks() * (1.0f - (float)this.slowingTicks / (float)this.getPushLimitTicks()));
        }
    }

    private void updateDashFactor() {
        if (this.slowingTicks > 0) {
            --this.slowingTicks;
        }
        if (this.pushTicks >= this.getMaxDashTicks()) {
            this.dashTicks = 0;
        }
        if (this.dashTicks > 0) {
            --this.dashTicks;
        }
        boolean slowing = this.slowingTicks > 0 && this.pushTicks > this.prevPushTicks;
        boolean dash = this.dashTicks > 0;
        tempSpeed = 1.0;
        if (slowing) {
            tempSpeed = 1.0 / this.speedFactor();
            return;
        }
        if (dash) {
            tempSpeed = this.speedFactor();
        }
    }

    @Override
    public void onRender2D(ScaledResolution sr) {
        float w = 70.0f;
        float h = 10.0f;
        float x = (float)sr.getScaledWidth() / 2.0f - w / 2.0f;
        float y = (float)sr.getScaledHeight() / 4.0f - h / 2.0f;
        int color = -1;
        float pTicks = mc.getRenderPartialTicks();
        float smoothPush = MathUtils.lerp(this.prevPushTicks, this.pushTicks, pTicks);
        float smoothDashTime = this.dashTicks == 0 ? 0.0f : (float)this.dashTicks + 1.0f - pTicks;
        RenderUtils.drawAlphedRect(x, y, x + w * MathUtils.clamp(smoothPush / ((float)this.getPushLimitTicks() + 1.0f), 0.0f, 1.0f), y + h / 2.0f, color);
        RenderUtils.drawAlphedRect(x, y + h / 2.0f, x + w * (smoothDashTime / (float)this.getMaxDashTicks()), y + h, color);
        int bgColor = ColorUtils.getColor(0, 0, 0);
        RenderUtils.drawLightContureRect(x, y, x + w, y + h, bgColor);
    }
}

