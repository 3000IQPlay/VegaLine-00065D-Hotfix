package ru.govno.client.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.TimerHelper;

public class UProfiler {
    private final TimerHelper timer = new TimerHelper();
    private long calcDelay;
    private final String nameProfiler;
    private long nanoMS;
    private long tempMS;
    private boolean paused = true;
    private boolean pausedByDelay;
    private final List<Property> properties = new ArrayList<Property>();
    private final List<String> toShow = new ArrayList<String>();

    public void setUpdateDelay(long updateDelay) {
        this.calcDelay = updateDelay;
    }

    private UProfiler(long delayCalc, String nameProfiler) {
        this.calcDelay = delayCalc;
        this.nameProfiler = nameProfiler;
    }

    public long getUpdateDelay() {
        return this.calcDelay;
    }

    public String getNameProfiler() {
        return this.nameProfiler;
    }

    public static UProfiler build(long delayCalc, String nameProfiler) {
        return new UProfiler(delayCalc, nameProfiler);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void startCalc() {
        if (this.paused || this.timer == null || (this.pausedByDelay = !this.timer.hasReached(this.getUpdateDelay()))) {
            return;
        }
        this.nanoMS = System.nanoTime();
        this.properties.clear();
    }

    public void addObj(String name) {
        if (this.paused || this.pausedByDelay) {
            return;
        }
        this.properties.add(new Property(name, System.nanoTime() - (this.properties.isEmpty() ? this.nanoMS : this.tempMS)));
        this.tempMS = System.nanoTime();
    }

    public void endCalc(boolean sort) {
        if (this.paused || this.pausedByDelay) {
            return;
        }
        if (sort && this.properties.size() > 1) {
            this.properties.sort(Comparator.comparingLong(Property::getNanoTime));
        }
        if (!this.toShow.isEmpty()) {
            this.toShow.clear();
        }
        for (Property property : this.properties) {
            double ms = (double)property.getNanoTime() / 1000000.0;
            if (ms < 0.001) continue;
            this.toShow.add(property.getName() + " " + TextFormatting.WHITE + String.format("%.4f", ms) + "ms");
        }
        if (!this.toShow.isEmpty()) {
            this.toShow.add(0, "");
            this.toShow.add(0, "~Sum all delays ~" + String.format("%.4f", (double)(System.nanoTime() - this.nanoMS) / 1000000.0) + "ms");
        }
        this.timer.reset();
    }

    public float drawingResultsHeight(float x, float y, float scale) {
        if (this.paused) {
            return 0.0f;
        }
        float h = 18.0f;
        float mX = x;
        float mY = y;
        x /= scale;
        y /= scale;
        GL11.glPushMatrix();
        GL11.glScaled((double)scale, (double)scale, (double)1.0);
        if (this.toShow.isEmpty()) {
            Fonts.minecraftia_16.drawString("No results in " + this.nameProfiler, mX, mY - 1.0f, -1);
        } else {
            GL11.glScaled((double)(1.0f / scale), (double)(1.0f / scale), (double)1.0);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Profiler " + this.nameProfiler, mX, mY - 1.0f, -1);
            GL11.glScaled((double)scale, (double)scale, (double)1.0);
            int index = 0;
            int bgCol = ColorUtils.getColor(0, 0, 0, 130);
            for (String info : this.toShow) {
                float iY = y + (float)index * 9.0f + 9.0f / scale;
                RenderUtils.drawRect(x, iY, x + 2.0f + (float)Fonts.minecraftia_16.getStringWidth(info), iY + 9.0f, info.startsWith("~") ? ColorUtils.getColor(100, 33, 100) : bgCol);
                Fonts.minecraftia_16.drawString(info, x + 1.0f, iY + 1.0f, info.startsWith("~") ? ColorUtils.getColor(0, 255, 255) : ColorUtils.getOverallColorFrom(ColorUtils.getColor(155, 255, 155), ColorUtils.getColor(255, 100, 100), (float)index / (float)this.toShow.size()));
                h += 9.0f * scale;
                ++index;
            }
        }
        GL11.glPopMatrix();
        return h;
    }

    public void cleanup() {
        this.toShow.clear();
        this.properties.clear();
        this.paused = true;
    }

    private class Property {
        private final String name;
        private final long nanoTime;

        public Property(String name, long nanoTime) {
            this.name = name;
            this.nanoTime = nanoTime;
        }

        public String getName() {
            return this.name;
        }

        public long getNanoTime() {
            return this.nanoTime;
        }
    }
}

