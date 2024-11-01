package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.WorldRender;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;

public class DistantAlpha
extends Module {
    public static DistantAlpha get;
    public ModeSettings AlphaCurve;
    public FloatSettings MinAlpha;
    public FloatSettings KillDistance;
    public FloatSettings StartDistance;
    private final byte ONCE = 1;
    private float TEMP_APC = 1.0f;
    private boolean TEMP_BRIGHTED;
    private final float[] tempMulRGBA = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    public DistantAlpha() {
        super("DistantAlpha", 0, Module.Category.RENDER);
        this.AlphaCurve = new ModeSettings("AlphaCurve", "Linear", this, new String[]{"Linear", "InCirc", "OutCirc", "InOutQuad", "InOutExpo", "OutCubic"});
        this.settings.add(this.AlphaCurve);
        this.MinAlpha = new FloatSettings("MinAlpha", 0.2f, 0.5f, 0.0f, this);
        this.settings.add(this.MinAlpha);
        this.StartDistance = new FloatSettings("StartDistance", 1.0f, 2.0f, 1.1f, this);
        this.settings.add(this.StartDistance);
        this.KillDistance = new FloatSettings("KillDistance", 0.4f, 1.0f, 0.15f, this);
        this.settings.add(this.KillDistance);
        get = this;
    }

    private Vec3d cameraPos() {
        EntityPlayerSP player;
        if (DistantAlpha.mc.world != null && (player = Minecraft.player) != null) {
            float partialTicks = mc.getRenderPartialTicks();
            float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks + (double)player.getEyeHeight();
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
            f += WorldRender.get.offPitchOrient;
            f1 += WorldRender.get.offYawOrient;
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
                int sideMul = Minecraft.getMinecraft().gameSettings.thirdPersonView == 1 ? 1 : -1;
                double camDist = WorldRender.get.cameraRedistance(4.0) * (double)sideMul;
                d0 += Math.sin(Math.toRadians(f1)) * camDist;
                d1 += Math.sin(Math.toRadians(f)) * camDist;
                d2 += -Math.cos(Math.toRadians(f1)) * camDist;
            }
            return new Vec3d(d0, d1, d2).add(WorldRender.get.getLastTranslated());
        }
        return new Vec3d(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ).add(WorldRender.get.getLastTranslated());
    }

    private double getDistanceAtCamera(double x, double y, double z) {
        return Math.sqrt(this.cameraPos().squareDistanceTo(RenderManager.renderPosX + x, RenderManager.renderPosY + y, RenderManager.renderPosZ + z));
    }

    private float calcAlphaPC(double x, double y, double z) {
        float linear = (float)(Math.max(this.getDistanceAtCamera(x, y, z) - (double)this.KillDistance.getFloat(), 0.0) / (double)(this.StartDistance.getFloat() - this.KillDistance.getFloat()));
        linear = MathUtils.clamp(linear, 0.0f, 1.0f);
        float minAlpha = Math.max(this.MinAlpha.getFloat() + 0.0999f, 0.0f);
        linear = minAlpha + Math.max(linear - minAlpha, 0.0f);
        switch (this.AlphaCurve.getMode()) {
            case "Linear": {
                return linear;
            }
            case "InOutQuad": {
                return linear < 0.5f ? 2.0f * linear * linear : 1.0f - (float)Math.pow(-2.0f * linear + 2.0f, 2.0) / 2.0f;
            }
            case "InCirc": {
                return 1.0f - (float)Math.sqrt(1.0 - Math.pow(linear, 2.0));
            }
            case "OutCirc": {
                return (float)Math.sqrt(1.0 - Math.pow(linear - 1.0f, 2.0));
            }
            case "InOutExpo": {
                return linear < 0.5f ? (float)Math.pow(2.0, 20.0f * linear - 10.0f) / 2.0f : (2.0f - (float)Math.pow(2.0, -20.0f * linear + 10.0f)) / 2.0f;
            }
            case "OutCubic": {
                return 1.0f - (float)Math.pow(1.0f - linear, 3.0);
            }
        }
        return 1.0f;
    }

    private boolean notClearWhite(float[] rgba) {
        return (float)ColorUtils.getColor((int)(rgba[0] * 255.0f), (int)(rgba[1] * 255.0f), (int)(rgba[2] * 255.0f), rgba[3] * 255.0f) != 1.0f;
    }

    public void onRenderEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, byte pass) {
        if (!get.isActived() || entity instanceof EntityPlayerSP && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            return;
        }
        switch (pass) {
            case 0: {
                this.TEMP_APC = MathUtils.clamp(this.calcAlphaPC(x, y + (double)entity.getEyeHeight(), z), 0.0f, 1.0f);
                this.TEMP_BRIGHTED = this.notClearWhite(this.tempMulRGBA);
                if (this.TEMP_APC == 1.0f) break;
                if (!GL11.glIsEnabled((int)3042)) {
                    GL11.glEnable((int)3042);
                }
                if (this.TEMP_APC < 0.1f) {
                    GL11.glAlphaFunc((int)516, (float)0.0f);
                }
                if (this.TEMP_BRIGHTED) {
                    GL11.glColor4f((float)this.tempMulRGBA[0], (float)this.tempMulRGBA[1], (float)this.tempMulRGBA[2], (float)(this.TEMP_APC * this.tempMulRGBA[3]));
                    break;
                }
                GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)this.TEMP_APC);
                break;
            }
            case 1: {
                if (this.TEMP_APC == 1.0f) break;
                if (!GL11.glIsEnabled((int)3042)) {
                    GL11.glEnable((int)3042);
                }
                if (this.TEMP_BRIGHTED) {
                    GL11.glColor4f((float)this.tempMulRGBA[0], (float)this.tempMulRGBA[1], (float)this.tempMulRGBA[2], (float)(this.TEMP_APC * this.tempMulRGBA[3]));
                    break;
                }
                GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)this.TEMP_APC);
                break;
            }
            case 2: {
                if (this.TEMP_APC == 1.0f) break;
                GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                GL11.glAlphaFunc((int)516, (float)0.1f);
                if (GL11.glIsEnabled((int)3042)) {
                    GL11.glDisable((int)3042);
                }
                this.TEMP_APC = 1.0f;
            }
        }
    }

    public void setTempEntityBrightness(float r, float g, float b, float a) {
        this.tempMulRGBA[0] = r;
        this.tempMulRGBA[1] = g;
        this.tempMulRGBA[2] = b;
        this.tempMulRGBA[3] = a;
    }

    public void setTempEntityBrightness(float[] rgba) {
        this.tempMulRGBA[0] = rgba[0];
        this.tempMulRGBA[1] = rgba[1];
        this.tempMulRGBA[2] = rgba[2];
        this.tempMulRGBA[3] = rgba[3];
    }

    public void unsetTempEntityBrightness() {
        this.tempMulRGBA[0] = 1.0f;
        this.tempMulRGBA[1] = 1.0f;
        this.tempMulRGBA[2] = 1.0f;
        this.tempMulRGBA[3] = 1.0f;
    }

    public boolean dontSetEntityBrightness(Entity entity) {
        float partialTicks = mc.getRenderPartialTicks();
        return this.isActived() && entity != null && (!(entity instanceof EntityPlayerSP) || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) && this.calcAlphaPC(entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks - RenderManager.renderPosX, entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)entity.getEyeHeight() - RenderManager.renderPosY, entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks - RenderManager.renderPosZ) < 1.0f;
    }
}

