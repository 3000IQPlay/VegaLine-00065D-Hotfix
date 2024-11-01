package ru.govno.client.utils;

import com.sun.management.OperatingSystemMXBean;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.FrameCounter;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.UProfiler;

public class UProfilers {
    private final List<UProfiler> profilers = new ArrayList<UProfiler>();
    private boolean enabled;
    private final UProfiler DELAULT_PROFILER = UProfiler.build(10000000000L, "NONE");
    private long delay;
    private final FrameCounter frameCounter = FrameCounter.build();
    private long lastReachedMS;
    private final ArrayList<String> pcProperties = new ArrayList();

    public boolean isEnabled() {
        return this.enabled;
    }

    public long getDelay() {
        return this.delay;
    }

    private UProfilers(long delayProfilers, String ... profilersToAdd) {
        for (String profilerName : profilersToAdd) {
            this.addProfiler(delayProfilers, profilerName);
        }
        this.delay = delayProfilers;
    }

    public static UProfilers build(long delayProfilers, String ... profilersToAdd) {
        return new UProfilers(delayProfilers, profilersToAdd);
    }

    public void addProfiler(long delayProfilers, String profilerName) {
        this.profilers.add(UProfiler.build(delayProfilers, profilerName));
    }

    public UProfiler getProfiler(String profileName) {
        UProfiler current = this.profilers.stream().filter(profiler -> profiler.getNameProfiler().equalsIgnoreCase(profileName)).findAny().orElse(null);
        return current == null ? this.DELAULT_PROFILER : current;
    }

    public List<UProfiler> getProfilers() {
        return this.profilers;
    }

    public UProfiler getProfiler(int index) {
        return this.profilers.get(index) == null ? this.DELAULT_PROFILER : this.profilers.get(index);
    }

    public void start() {
        for (UProfiler profiler : this.profilers) {
            profiler.setPaused(false);
        }
        this.enabled = true;
    }

    public void stop() {
        for (UProfiler profiler : this.profilers) {
            profiler.setPaused(true);
            profiler.cleanup();
        }
        this.enabled = false;
    }

    public void setDelays(long delayProfilers) {
        for (UProfiler profiler : this.profilers) {
            profiler.setUpdateDelay(delayProfilers);
        }
        this.delay = delayProfilers;
    }

    public void drawIn2D(ScaledResolution sr) {
        float x;
        float y;
        if (!this.enabled || this.profilers.isEmpty()) {
            return;
        }
        float preY = y = (x = 5.0f);
        for (UProfiler profiler : this.profilers) {
            y += profiler.drawingResultsHeight(x, y, 0.5f);
        }
        y = preY;
        this.frameCounter.renderThreadRead((int)MathUtils.clamp(this.frameCounter.getFps() / (double)3.33333f, 10.0, 50.0));
        String counterData = this.frameCounter.getFpsString(true) + ", " + this.frameCounter.getAverageFpsString(true) + ", " + this.frameCounter.getMinFpsString(true) + ", " + this.frameCounter.getLatencyString(true);
        float fpsPC = (float)MathUtils.clamp(this.frameCounter.getFps() / 200.0, 0.0, 1.0);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(counterData, (float)sr.getScaledWidth() - x - (float)Minecraft.getMinecraft().fontRendererObj.getStringWidth(counterData), y, ColorUtils.getOverallColorFrom(ColorUtils.getColor(255, 40, 40), ColorUtils.getColor(100, 255, 255), fpsPC));
        float offXLines = this.frameCounter.getLatencyWindowWidth();
        this.frameCounter.drawLatencyLine((float)sr.getScaledWidth() - x - offXLines, y += 10.0f);
        ArrayList<String> pcData = this.getPCData();
        y = (float)sr.getScaledHeight() / 2.0f - (float)pcData.size() * 7.5f;
        int indexLine = 0;
        int gray = -10197916;
        int black = -16777216;
        for (String data : pcData) {
            float strW = (float)Fonts.noise_14.getStringWidth(data) + 4.0f;
            float xPos = (float)sr.getScaledWidth() - x - strW - 2.0f;
            float yPos = y + 15.0f * (float)indexLine;
            RenderUtils.drawRect(xPos - 1.0f, yPos - 1.0f, xPos + strW + 1.0f, yPos + 11.0f, -1);
            RenderUtils.drawRect(xPos - 0.5f, yPos - 0.5f, xPos + strW + 0.5f, yPos + 10.5f, black);
            RenderUtils.drawRect(xPos, yPos, xPos + strW, yPos + 10.0f, gray);
            Fonts.noise_14.drawString(data, xPos + 2.0f, yPos + 3.0f, black);
            ++indexLine;
        }
    }

    public ArrayList<String> getPCData() {
        if (this.lastReachedMS < System.currentTimeMillis() && Display.isVisible()) {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            this.pcProperties.clear();
            String jvmString = System.getProperty("java.vendor") + "/" + String.format("Java: %s %dbit", System.getProperty("java.version"), Minecraft.getMinecraft().isJava64bit() ? 64 : 32);
            String recommendedVM = "17.0.6";
            if (Client.badJavaVersion) {
                jvmString = jvmString + TextFormatting.DARK_RED + " (not recommended java version, update your java to " + recommendedVM + " version)";
            }
            this.pcProperties.add("Current_JVM: " + jvmString);
            double jvmCpuLoad = osBean.getProcessCpuLoad();
            this.pcProperties.add("JVM_cpu_load: " + String.format("%.2f", jvmCpuLoad * 100.0) + "%");
            long maxMem = Runtime.getRuntime().maxMemory();
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            this.pcProperties.add("JVM_dram_load: " + String.format("%2d%% %03d/%03dMB", (totalMem - freeMem) * 100L / maxMem, (totalMem - freeMem) / 1024L / 1024L, maxMem / 1024L / 1024L));
            this.pcProperties.add("Sys_dram_load: " + String.format("%2d%% %03d/%02dMB", (osBean.getTotalMemorySize() - osBean.getFreeMemorySize()) * 100L / osBean.getTotalMemorySize(), (osBean.getTotalMemorySize() - osBean.getFreeMemorySize()) / 1024L / 1024L, osBean.getTotalMemorySize() / 1024L / 1024L));
            this.pcProperties.add("JVM_started_OS: " + System.getProperty("os.name") + " | ver: " + System.getProperty("os.version"));
            this.pcProperties.add("PC_CPU: " + String.format("%s", OpenGlHelper.getCpu()).trim());
            this.pcProperties.add("PC_GPU: " + GL11.glGetString((int)7937));
            this.pcProperties.add("VGA_Driver: " + GL11.glGetString((int)7938));
            GraphicsDevice firstDisplay = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
            int pixWidth = firstDisplay.getDefaultConfiguration().getBounds().width;
            int pixHeight = firstDisplay.getDefaultConfiguration().getBounds().height;
            int refreshRate = firstDisplay.getDisplayMode().getRefreshRate();
            this.pcProperties.add("Display_Standard: " + pixWidth + "x" + pixHeight + " (" + refreshRate + "hz)");
            this.lastReachedMS = System.currentTimeMillis() + 1000L;
        }
        return this.pcProperties;
    }
}

