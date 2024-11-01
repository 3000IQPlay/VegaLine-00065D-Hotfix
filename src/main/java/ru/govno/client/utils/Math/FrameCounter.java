package ru.govno.client.utils.Math;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.opengl.GL11;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class FrameCounter {
    private double lastPassedTime = (double)System.nanoTime() / 1000000.0;
    private double fps;
    private double averageFPS;
    private double minFps;
    private double latency;
    private long lastReachedMS;
    private final List<Double> framesLatency = Lists.newArrayList();
    private final List<Double> fpsAVBuffer = Lists.newArrayList();

    private FrameCounter() {
    }

    public static FrameCounter build() {
        return new FrameCounter();
    }

    public void renderThreadRead(int scaleFramesCheck) {
        if (scaleFramesCheck >= 1) {
            double ms = (double)System.nanoTime() / 1000000.0;
            double lastLatency = ms - this.lastPassedTime;
            this.lastPassedTime = ms;
            long curLongMS = System.currentTimeMillis();
            this.framesLatency.add(lastLatency);

            while (this.framesLatency.size() > scaleFramesCheck) {
                this.framesLatency.remove(0);
            }

            this.fps = 0.0;
            this.minFps = -1.0;
            this.latency = 0.0;

            for (Double latency : this.framesLatency) {
                double currentLattFP = 1000.0 / Math.max(latency, 0.001);
                if (this.minFps < 0.0 || this.minFps > currentLattFP) {
                    this.minFps = currentLattFP;
                }

                this.fps += currentLattFP / (double)scaleFramesCheck;
                this.latency = this.latency + latency / (double)scaleFramesCheck;
            }

            this.fpsAVBuffer.add(this.fps);
            if (this.lastReachedMS < curLongMS) {
                while ((double)this.fpsAVBuffer.size() > this.fps * 5.0) {
                    this.fpsAVBuffer.remove(0);
                }

                this.averageFPS = this.fpsAVBuffer.stream().mapToDouble(Double::doubleValue).sum() / (double)this.fpsAVBuffer.size();
                this.lastReachedMS = curLongMS + 5000L;
            }
        }
    }

    public void reset() {
        this.fps = 0.0;
        this.latency = 1.0;
        this.framesLatency.clear();
    }

    public double getFps() {
        return this.fps;
    }

    public String getFpsString(boolean append) {
        String fps = (int)(this.fps + 0.5) + "";
        if (append) {
            fps = fps + "fps";
        }

        return fps;
    }

    public double getAverageFps() {
        return Double.isNaN(this.averageFPS) ? 0.0 : this.averageFPS;
    }

    public String getAverageFpsString(boolean append) {
        String average = String.format("%.1f", this.averageFPS);
        if (append) {
            average = average + "(avg)fps";
        }

        return average.replace(".0", "");
    }

    public double getMinFps() {
        return this.minFps;
    }

    public String getMinFpsString(boolean append) {
        String min = String.format("%.1f", this.minFps);
        if (append) {
            min = min + "(min)fps";
        }

        return min.replace(".0", "");
    }

    public double getLatency() {
        return this.latency;
    }

    public String getLatencyString(boolean append) {
        String latency = String.format("%.2f", this.latency);
        if (append) {
            latency = latency + "ms";
        }

        return latency;
    }

    public float getLatencyWindowWidth() {
        return (float)this.framesLatency.size() * 2.0F;
    }

    public void drawLatencyLine(float x, float y) {
        float w = (float)this.framesLatency.size() * 2.0F;
        RenderUtils.drawRect((double)x, (double)y, (double)(x + w), (double)(y + 0.5F), -1);
        List<Vec2f> vecsList = Lists.newArrayList();
        int index = 0;
        float hDelta = 1.0F / (float)this.getFps() * w * 4.0F;

        for (Double latency : this.framesLatency) {
            float iX = x + 1.0F + w * ((float)index / (float)this.framesLatency.size());
            float iY = (float)((double)y + latency * (double)hDelta);
            vecsList.add(new Vec2f(iX, iY));
            index++;
        }

        RenderUtils.glRenderStart();
        RenderUtils.glColor(-1);
        GL11.glShadeModel(7425);
        GL11.glHint(3154, 4354);
        GL11.glEnable(2848);
        GL11.glLineWidth(0.05F);
        GL11.glBegin(3);
        vecsList.forEach(vec -> GL11.glVertex2d((double)vec.x, (double)vec.y));
        GL11.glEnd();
        GL11.glHint(3154, 4352);
        GL11.glDisable(2848);
        GL11.glBegin(1);
        vecsList.forEach(vec -> {
            RenderUtils.glColor(-1);
            GL11.glVertex2d((double)vec.x, (double)(y + 1.0F));
            RenderUtils.glColor(0);
            GL11.glVertex2d((double)vec.x, (double)vec.y);
        });
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
        RenderUtils.glColor(ColorUtils.getColor(255, 0, 0));
        GL11.glPointSize(0.5F);
        GL11.glBegin(0);
        vecsList.forEach(vec -> GL11.glVertex2d((double)vec.x, (double)vec.y));
        GL11.glEnd();
        GL11.glPointSize(1.0F);
        GL11.glShadeModel(7424);
        RenderUtils.glRenderStop();
    }
}
