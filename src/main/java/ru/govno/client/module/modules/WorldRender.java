package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventLightingCheck;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRenderChunk;
import ru.govno.client.event.events.EventRenderChunkContainer;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.GaussianBlur;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.URender.other.NoiseAnimation;

public class WorldRender
extends Module {
    public static WorldRender get;
    public BoolSettings ClientPlayersSkins;
    public BoolSettings FastWorldLoad;
    public BoolSettings RenderBarrier;
    public BoolSettings ItemPhisics;
    public BoolSettings ChunksDebuger;
    public BoolSettings ChunkAnim;
    public BoolSettings FullBright;
    public BoolSettings BlockLightFix;
    public BoolSettings WorldBloom;
    public BoolSettings CustomParticles;
    public BoolSettings DecreaseTotemParticle;
    public BoolSettings WorldReTime;
    public BoolSettings SkyRecolor;
    public BoolSettings FogRedistance;
    public BoolSettings ClearWeather;
    public BoolSettings CustomCamDist;
    public BoolSettings AntiAliasing;
    public BoolSettings AltReverseCamera;
    public BoolSettings FreeLook;
    public BoolSettings CustomViewBobbing;
    public BoolSettings CameraTweaks;
    public BoolSettings CameraFovRework;
    public BoolSettings SpawnAnimations;
    public BoolSettings BlurBlocks;
    public ModeSettings SelfSkin;
    public ModeSettings BrightMode;
    public ModeSettings Time;
    public ModeSettings SkyColorMode;
    public ModeSettings ViewShakingType;
    public FloatSettings BloomPower;
    public FloatSettings ParticleSpeed;
    public FloatSettings ParticleCount;
    public FloatSettings TimeCustom;
    public FloatSettings TimeSpinSpeed;
    public FloatSettings SkyFadeSpeed;
    public FloatSettings SkyClientColBright;
    public FloatSettings SkyBright;
    public FloatSettings FogDistanceCustom;
    public FloatSettings CameraRedistance;
    public FloatSettings CameraSmoothing;
    public ColorSettings SkyColorPick;
    public ColorSettings SkyColorPick2;
    private ResourceLocation PS_SHAPES_TEXTURE = null;
    private final NoiseAnimation altWorldLoading = new NoiseAnimation();
    private double dx;
    private double dy;
    private double dz;
    private double pdx;
    private double pdy;
    private double pdz;
    private double lx;
    private double ly;
    private double lz;
    private Vec3d lastTranslated;
    public boolean freeLookState;
    public boolean prevFreeLookState;
    private float sYaw;
    private float sPitch;
    private float cYawOff;
    private float cPitchOff;
    private float prevCYawOff;
    private float prevCPitchOff;
    public float offYawOrient;
    public float offPitchOrient;
    AnimationUtils orientYawAnim = new AnimationUtils(0.0f, 0.0f, 0.2f);
    AnimationUtils orientPitchAnim = new AnimationUtils(0.0f, 0.0f, 0.2f);
    public AnimationUtils fovMultiplier = new AnimationUtils(1.0f, 1.0f, 0.08f);
    public boolean isItemPhisics = false;
    protected AnimationUtils spinnedTime = new AnimationUtils(0.0f, 0.0f, 0.075f);
    protected float current = 0.0f;
    boolean smoothingTime;
    float oldTime = -1.2398746E8f;
    boolean rend = true;
    public float oldGamma;
    private final HashMap<RenderChunk, AtomicLong> renderChunkMap = new HashMap();
    private boolean wantToCustomLoading;
    private final TimerHelper soundDelay = TimerHelper.TimerHelperReseted();
    private int shakeType = 0;
    private final int[] COLORS_SHAPES = new int[]{ColorUtils.getColor(0, 125, 255), ColorUtils.getColor(255, 0, 45), ColorUtils.getColor(255, 0, 232), ColorUtils.getColor(0, 255, 130)};
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder buffer = this.tessellator.getBuffer();
    private final Random RANDOM = new Random();
    private final List<PSParticle> PS_PARTICLES_LIST = new ArrayList<PSParticle>();

    public WorldRender() {
        super("WorldRender", 0, Module.Category.RENDER);
        this.ClientPlayersSkins = new BoolSettings("ClientPlayersSkins", true, this);
        this.settings.add(this.ClientPlayersSkins);
        this.SelfSkin = new ModeSettings("SelfSkin", "Skin4", this, new String[]{"Skin1", "Skin2", "Skin3", "Skin4", "Skin5", "Skin6", "Skin7", "Skin8", "Skin9", "Skin10", "Skin11", "Skin12", "Skin13", "Skin14", "Skin15", "Skin16"}, () -> this.ClientPlayersSkins.getBool());
        this.settings.add(this.SelfSkin);
        this.FastWorldLoad = new BoolSettings("FastWorldLoad", false, this);
        this.settings.add(this.FastWorldLoad);
        this.RenderBarrier = new BoolSettings("RenderBarrier", false, this);
        this.settings.add(this.RenderBarrier);
        this.ItemPhisics = new BoolSettings("ItemPhisics", true, this);
        this.settings.add(this.ItemPhisics);
        this.ChunksDebuger = new BoolSettings("ChunksDebuger", false, this);
        this.settings.add(this.ChunksDebuger);
        this.ChunkAnim = new BoolSettings("ChunkAnim", false, this);
        this.settings.add(this.ChunkAnim);
        this.FullBright = new BoolSettings("FullBright", false, this);
        this.settings.add(this.FullBright);
        this.BrightMode = new ModeSettings("BrightMode", "Vision", this, new String[]{"Vision", "Gamma"}, () -> this.FullBright.getBool());
        this.settings.add(this.BrightMode);
        this.BlockLightFix = new BoolSettings("BlockLightFix", true, this);
        this.settings.add(this.BlockLightFix);
        this.WorldBloom = new BoolSettings("WorldBloom", true, this);
        this.settings.add(this.WorldBloom);
        this.BloomPower = new FloatSettings("BloomPower", 0.4f, 0.75f, 0.05f, this, () -> this.WorldBloom.getBool());
        this.settings.add(this.BloomPower);
        this.CustomParticles = new BoolSettings("CustomParticles", false, this);
        this.settings.add(this.CustomParticles);
        this.ParticleSpeed = new FloatSettings("ParticleSpeed", 0.5f, 2.0f, 0.1f, this, () -> this.CustomParticles.getBool());
        this.settings.add(this.ParticleSpeed);
        this.ParticleCount = new FloatSettings("ParticleCount", 0.85f, 5.0f, 0.25f, this, () -> this.CustomParticles.getBool());
        this.settings.add(this.ParticleCount);
        this.DecreaseTotemParticle = new BoolSettings("DecreaseTotemParticle", true, this, () -> this.CustomParticles.getBool());
        this.settings.add(this.DecreaseTotemParticle);
        this.WorldReTime = new BoolSettings("WorldReTime", true, this);
        this.settings.add(this.WorldReTime);
        this.Time = new ModeSettings("Time", "Night", this, new String[]{"Evening", "Night", "Morning", "Day", "SpinTime", "Custom", "RealWorldTime"}, () -> this.WorldReTime.getBool());
        this.settings.add(this.Time);
        this.TimeCustom = new FloatSettings("TimeCustom", 14000.0f, 24000.0f, 0.0f, this, () -> this.WorldReTime.getBool() && this.Time.currentMode.equalsIgnoreCase("Custom"));
        this.settings.add(this.TimeCustom);
        this.TimeSpinSpeed = new FloatSettings("TimeSpinSpeed", 1.0f, 3.0f, 0.1f, this, () -> this.WorldReTime.getBool() && this.Time.currentMode.equalsIgnoreCase("SpinTime"));
        this.settings.add(this.TimeSpinSpeed);
        this.SkyRecolor = new BoolSettings("SkyRecolor", false, this);
        this.settings.add(this.SkyRecolor);
        this.SkyColorMode = new ModeSettings("SkyColorMode", "Colored", this, new String[]{"Colored", "Fade", "Client", "ReBright"}, () -> this.SkyRecolor.getBool());
        this.settings.add(this.SkyColorMode);
        this.SkyColorPick = new ColorSettings("SkyColorPick", ColorUtils.getColor(40, 40, 255, 140), this, () -> this.SkyRecolor.getBool() && (this.SkyColorMode.currentMode.equalsIgnoreCase("Colored") || this.SkyColorMode.currentMode.equalsIgnoreCase("Fade")));
        this.settings.add(this.SkyColorPick);
        this.SkyColorPick2 = new ColorSettings("SkyColorPick2", ColorUtils.getColor(40, 40, 255, 60), this, () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("Fade"));
        this.settings.add(this.SkyColorPick2);
        this.SkyFadeSpeed = new FloatSettings("SkyFadeSpeed", 0.35f, 1.5f, 0.1f, this, () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("Fade"));
        this.settings.add(this.SkyFadeSpeed);
        this.SkyClientColBright = new FloatSettings("SkyClientColBright", 0.6f, 1.0f, 0.05f, this, () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("Client"));
        this.settings.add(this.SkyClientColBright);
        this.SkyBright = new FloatSettings("SkyBright", 0.4f, 1.0f, 0.0f, this, () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("ReBright"));
        this.settings.add(this.SkyBright);
        this.FogRedistance = new BoolSettings("FogRedistance", false, this);
        this.settings.add(this.FogRedistance);
        this.FogDistanceCustom = new FloatSettings("FogDistanceCustom", 40.0f, 120.0f, 15.0f, this, () -> this.FogRedistance.getBool());
        this.settings.add(this.FogDistanceCustom);
        this.ClearWeather = new BoolSettings("ClearWeather", true, this);
        this.settings.add(this.ClearWeather);
        this.CustomCamDist = new BoolSettings("CustomCamDist", true, this);
        this.settings.add(this.CustomCamDist);
        this.CameraRedistance = new FloatSettings("CameraRedistance", 4.0f, 15.0f, 1.0f, this, () -> this.CustomCamDist.getBool());
        this.settings.add(this.CameraRedistance);
        this.AntiAliasing = new BoolSettings("AntiAliasing", true, this);
        this.settings.add(this.AntiAliasing);
        this.AltReverseCamera = new BoolSettings("AltReverseCamera", false, this);
        this.settings.add(this.AltReverseCamera);
        this.FreeLook = new BoolSettings("FreeLook", false, this);
        this.settings.add(this.FreeLook);
        this.CustomViewBobbing = new BoolSettings("CustomViewBobbing", false, this);
        this.settings.add(this.CustomViewBobbing);
        this.ViewShakingType = new ModeSettings("ViewShakingType", "Increased", this, new String[]{"Increased", "StableCamera", "SpeedLike", "Agressive"}, () -> this.CustomViewBobbing.getBool());
        this.settings.add(this.ViewShakingType);
        this.CameraTweaks = new BoolSettings("CameraTweaks", false, this);
        this.settings.add(this.CameraTweaks);
        this.CameraSmoothing = new FloatSettings("CameraSmoothing", 1.3f, 2.0f, 0.5f, this, () -> this.CameraTweaks.getBool());
        this.settings.add(this.CameraSmoothing);
        this.CameraFovRework = new BoolSettings("CameraFovRework", false, this, () -> this.CameraTweaks.getBool());
        this.settings.add(this.CameraFovRework);
        this.SpawnAnimations = new BoolSettings("SpawnAnimations", false, this);
        this.settings.add(this.SpawnAnimations);
        this.BlurBlocks = new BoolSettings("BlurBlocks", false, this);
        this.settings.add(this.BlurBlocks);
        try {
            DynamicTexture dynamicTexture = new DynamicTexture(TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("vegaline/modules/worldrender/particles/particleshapesatlas.png")).getInputStream()));
            dynamicTexture.setBlurMipmap(true, false);
            this.PS_SHAPES_TEXTURE = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(dynamicTexture.toString(), dynamicTexture);
            mc.getTextureManager().bindTexture(this.PS_SHAPES_TEXTURE);
        }
        catch (Exception e) {
            e.fillInStackTrace();
        }
        get = this;
    }

    @Override
    public boolean isBetaModule() {
        return true;
    }

    public static boolean decreaseTotemParticles() {
        return get.isActived() && WorldRender.get.CustomParticles.getBool() && WorldRender.get.DecreaseTotemParticle.getBool();
    }

    private void updateTranslationCamera() {
        if (!this.isActived() || !this.CameraTweaks.getBool()) {
            return;
        }
        this.pdx = (this.dx + this.lx) / 2.0;
        this.pdy = (this.dy + this.ly) / 2.0;
        this.pdz = (this.dz + this.lz) / 2.0;
        this.lx = Minecraft.player.posX - Minecraft.player.lastTickPosX;
        this.ly = Minecraft.player.posY - Minecraft.player.lastTickPosY;
        this.lz = Minecraft.player.posZ - Minecraft.player.lastTickPosZ;
        this.dx = this.lx;
        this.dy = this.ly;
        this.dz = this.lz;
    }

    public Vec3d getLastTranslated() {
        return this.isActived() && this.CameraTweaks.getBool() ? this.lastTranslated : Vec3d.ZERO;
    }

    public void translationCamera(float partialTicks) {
        if (Panic.stop || !this.isActived() || !this.CameraTweaks.getBool()) {
            return;
        }
        double tx = MathUtils.lerp(this.pdx, this.dx, (double)partialTicks);
        double ty = MathUtils.lerp(this.pdy, this.dy, (double)partialTicks);
        double tz = MathUtils.lerp(this.pdz, this.dz, (double)partialTicks);
        float mulSmoothTick = 0.55f * this.CameraSmoothing.getFloat();
        this.lastTranslated = new Vec3d(tx *= (double)mulSmoothTick, ty *= (double)mulSmoothTick, tz *= (double)mulSmoothTick);
        GlStateManager.translate(tx, ty, tz);
    }

    public void updateFreeLookRotation(float yawPlus, float pitchPlus, float partialTicks) {
        this.prevCYawOff = this.cYawOff;
        this.prevCPitchOff = this.cPitchOff;
        this.cYawOff += yawPlus;
        this.cPitchOff += pitchPlus;
        if (this.cPitchOff >= 90.0f - Minecraft.player.rotationPitch) {
            this.cPitchOff = 90.0f - Minecraft.player.rotationPitch;
        }
        if (this.cPitchOff <= -90.0f - Minecraft.player.rotationPitch) {
            this.cPitchOff = -90.0f - Minecraft.player.rotationPitch;
        }
        this.offYawOrient = MathUtils.lerp(this.prevCYawOff, this.cYawOff, partialTicks);
        this.offPitchOrient = MathUtils.lerp(this.prevCPitchOff, this.cPitchOff, partialTicks);
    }

    public void updateFreeLookState(boolean enabledModule) {
        if (!(this.FreeLook.getBool() && enabledModule || !this.freeLookState)) {
            this.freeLookState = false;
            WorldRender.mc.gameSettings.thirdPersonView = 0;
        } else {
            if (!this.FreeLook.getBool()) {
                return;
            }
            if (this.actived) {
                this.freeLookState = WorldRender.mc.gameSettings.keyBindTogglePerspective.isKeyDown();
                int n = WorldRender.mc.gameSettings.thirdPersonView = this.freeLookState ? 1 : 0;
            }
        }
        if (this.freeLookState != this.prevFreeLookState) {
            if (this.freeLookState) {
                this.sYaw = Minecraft.player.rotationYaw;
                this.sPitch = Minecraft.player.rotationPitch;
            } else {
                this.sYaw = 0.0f;
                this.sPitch = 0.0f;
                this.cPitchOff = 0.0f;
                this.cYawOff = 0.0f;
                this.offYawOrient = 0.0f;
                this.offPitchOrient = 0.0f;
            }
        }
        if (this.sYaw != 0.0f || this.sPitch != 0.0f) {
            Minecraft.player.rotationYaw = this.sYaw;
            Minecraft.player.rotationPitch = this.sPitch;
        }
        this.prevFreeLookState = this.freeLookState;
    }

    private int getClientSkinsCount() {
        return 16;
    }

    public ResourceLocation updatedResourceSkin(ResourceLocation prevResource, Entity entity) {
        if (get != null && this.actived && entity != null && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            if (WorldRender.get.ClientPlayersSkins.getBool()) {
                int index = (player instanceof EntityPlayerSP || player == FreeCam.fakePlayer ? Integer.parseInt(this.SelfSkin.currentMode.replace("Skin", "")) : player.getEntityId() * 3) % this.getClientSkinsCount() + 1;
                prevResource = new ResourceLocation("vegaline/modules/worldrender/skins/default/skin" + index + ".png");
            }
        }
        return prevResource;
    }

    public float setupedGammaNightVision() {
        float gamma = 0.0f;
        if (this.isActived() && this.FullBright.canBeRender() && this.BrightMode.currentMode.equalsIgnoreCase("Vision")) {
            gamma = this.FullBright.getAnimation();
        }
        return gamma;
    }

    public boolean isReverseCamera() {
        return !Panic.stop && get != null && this.actived && this.AltReverseCamera.getBool() && Keyboard.isKeyDown((int)56) && WorldRender.mc.currentScreen == null;
    }

    public float[] orientCustom(float partialTicks) {
        if (!Panic.stop && get != null && WorldRender.get.actived && this.CameraTweaks.getBool()) {
            float cameraSpeed;
            float smPC = this.CameraSmoothing.getFloat() / 2.0f;
            this.orientYawAnim.speed = cameraSpeed = 0.6f - 0.5f * smPC;
            this.orientPitchAnim.speed = cameraSpeed;
            float yaw = MathUtils.lerp(Minecraft.player.prevRotationYaw, Minecraft.player.rotationYaw, Math.abs(partialTicks));
            float pitch = MathUtils.lerp(Minecraft.player.prevRotationPitch, Minecraft.player.rotationPitch, Math.abs(partialTicks));
            this.orientYawAnim.to = yaw;
            this.orientPitchAnim.to = pitch;
            float transYaw = (partialTicks >= 0.0f ? this.orientYawAnim.getAnim() : this.orientYawAnim.anim) - yaw;
            float transPitch = (partialTicks >= 0.0f ? this.orientPitchAnim.getAnim() : this.orientPitchAnim.anim) - pitch;
            return new float[]{transYaw, transPitch};
        }
        return new float[]{0.0f, 0.0f};
    }

    public float getClientFovMul(float prevVal, float partialTicks) {
        if (!this.altWorldLoading.hasFinished() && this.SpawnAnimations.getBool()) {
            float progressNoise = this.altWorldLoading.getNoiseProgress();
            float waveProgressNoise = (float)MathUtils.easeInOutQuadWave(progressNoise);
            if (this.shakeType == 0) {
                prevVal += 1.0f - progressNoise;
            } else if (this.shakeType == 1) {
                prevVal = (float)((double)prevVal + (1.0 - MathUtils.easeInOutQuad(progressNoise)) * 3.0);
            } else if (this.shakeType == 2 || this.shakeType == 3) {
                prevVal = (float)((double)prevVal + MathUtils.easeOutBounce(Math.min(waveProgressNoise, 1.0f)));
            }
        }
        if (!Panic.stop && get.isActived() && WorldRender.get.CameraTweaks.getBool() && this.CameraFovRework.getBool()) {
            float fovTo = 0.0f;
            this.fovMultiplier.speed = 0.04f;
            if (Minecraft.player != null) {
                if (WorldRender.mc.gameSettings.thirdPersonView == 1 && Minecraft.player.isSneaking() && MoveMeHelp.getSpeed() == 0.0 && Minecraft.player.posY == Minecraft.player.lastTickPosY) {
                    fovTo = 0.05f;
                } else {
                    if (WorldRender.mc.pointedEntity != null) {
                        fovTo += 0.01f;
                    }
                    if (Minecraft.player.isBowing() || Minecraft.player.isDrinking()) {
                        fovTo += (0.05f + MathUtils.clamp(((float)Minecraft.player.getItemInUseMaxCount() + partialTicks) / (Minecraft.player.isDrinking() ? 32.0f : 21.0f), 0.0f, 1.0f)) * (Minecraft.player.isDrinking() ? -0.015f : 0.5f);
                    }
                }
            }
            this.fovMultiplier.getAnim();
            this.fovMultiplier.to = fovTo;
            return prevVal + this.fovMultiplier.anim;
        }
        return prevVal;
    }

    public double cameraRedistance(double prevDistance) {
        if (get != null) {
            WorldRender.get.stateAnim.to = get.isActived() ? 1.0f : 0.0f;
        }
        return !Panic.stop && get != null ? MathUtils.lerp(prevDistance, (double)this.CameraRedistance.getAnimation(), (double)(this.CustomCamDist.getAnimation() * WorldRender.get.stateAnim.getAnim())) : prevDistance;
    }

    public float weatherReStrengh(float prevStrengh) {
        return !Panic.stop && get != null && WorldRender.get.actived && this.ClearWeather.getBool() ? 0.0f : prevStrengh;
    }

    public float[] getSkyColorRGB(float prevRed, float prevGreen, float prevBlue) {
        WorldRender MOD = get;
        if (!Panic.stop && MOD != null && MOD.actived && this.SkyRecolor.canBeRender()) {
            String mode = this.SkyColorMode.currentMode;
            float renderAnim = this.SkyRecolor.getAnimation();
            float red = prevRed;
            float green = prevGreen;
            float blue = prevBlue;
            switch (mode) {
                case "Colored": {
                    int pick1 = this.SkyColorPick.color;
                    float aLP = ColorUtils.getGLAlphaFromColor(pick1);
                    float[] rgbFloat = new float[]{ColorUtils.getGLRedFromColor(pick1) * aLP, ColorUtils.getGLGreenFromColor(pick1) * aLP, ColorUtils.getGLBlueFromColor(pick1) * aLP};
                    red = rgbFloat[0];
                    green = rgbFloat[1];
                    blue = rgbFloat[2];
                    break;
                }
                case "Fade": {
                    int pick1 = this.SkyColorPick.color;
                    int pick2 = this.SkyColorPick2.color;
                    float fadeSpeed = this.SkyFadeSpeed.getFloat() * 0.5f;
                    int pickFadedColor = ColorUtils.fadeColorIndexed(pick1, pick2, fadeSpeed, 0);
                    float aLP = ColorUtils.getGLAlphaFromColor(pickFadedColor);
                    float[] rgbFloat = new float[]{ColorUtils.getGLRedFromColor(pickFadedColor) * aLP, ColorUtils.getGLGreenFromColor(pickFadedColor) * aLP, ColorUtils.getGLBlueFromColor(pickFadedColor) * aLP};
                    red = rgbFloat[0];
                    green = rgbFloat[1];
                    blue = rgbFloat[2];
                    break;
                }
                case "Client": {
                    int col = ClientColors.getColor1(0, this.SkyClientColBright.getFloat());
                    float aLP = ColorUtils.getGLAlphaFromColor(col);
                    float[] rgbFloat = new float[]{ColorUtils.getGLRedFromColor(col) * aLP, ColorUtils.getGLGreenFromColor(col) * aLP, ColorUtils.getGLBlueFromColor(col) * aLP};
                    red = rgbFloat[0];
                    green = rgbFloat[1];
                    blue = rgbFloat[2];
                    break;
                }
                case "ReBright": {
                    float bright = MathUtils.clamp(this.SkyBright.getFloat(), 0.0f, 1.0f);
                    red *= bright;
                    green *= bright;
                    blue *= bright;
                }
            }
            return new float[]{MathUtils.lerp(prevRed, red, renderAnim), MathUtils.lerp(prevGreen, green, renderAnim), MathUtils.lerp(prevBlue, blue, renderAnim)};
        }
        return new float[]{prevRed, prevGreen, prevBlue};
    }

    public float getRedistanceFogValue(float prevMaxDstSq) {
        return !Panic.stop && WorldRender.get.actived && this.FogRedistance.canBeRender() ? MathUtils.lerp(prevMaxDstSq, this.FogDistanceCustom.getAnimation(), this.FogRedistance.getAnimation()) : prevMaxDstSq;
    }

    private static float[] getSmoothRealTime() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        float smoothSec = (float)(date.getSeconds() - 1) + (float)(System.currentTimeMillis() % 1000L) / 1000.0f;
        float smoothMins = (float)(date.getMinutes() - 1) + smoothSec / 60.0f;
        float smoothHours = (float)(date.getHours() - 1) + smoothMins / 60.0f;
        return new float[]{smoothSec, smoothMins, smoothHours};
    }

    private float getWorldTimeByRealTime(float[] realTime) {
        float smoothHourMC = (realTime[2] + 15.0f) % 24.0f;
        return smoothHourMC * 1000.0f;
    }

    public long getWorldReTime(long oldTime) {
        boolean sataFlag;
        WorldRender mod = get;
        boolean enabled = mod != null && mod.actived && this.WorldReTime.getBool();
        boolean bl = sataFlag = enabled || MathUtils.getDifferenceOf(this.spinnedTime.anim, oldTime % 24000L) > 80.0;
        if (enabled) {
            String mode = this.Time.currentMode;
            if (mode != null) {
                switch (mode) {
                    case "Evening": {
                        this.current = 12800.0f;
                        break;
                    }
                    case "Night": {
                        this.current = 18000.0f;
                        break;
                    }
                    case "Morning": {
                        this.current = 23500.0f;
                        break;
                    }
                    case "Day": {
                        this.current = 6000.0f;
                        break;
                    }
                    case "SpinTime": {
                        this.current = (float)(System.currentTimeMillis() % (long)((int)(10000.0f / this.TimeSpinSpeed.getFloat()))) / (10000.0f / this.TimeSpinSpeed.getFloat()) * 24000.0f;
                        if (!(MathUtils.getDifferenceOf(this.spinnedTime.anim, this.current) > 23000.0)) break;
                        this.spinnedTime.setAnim(this.current * 0.9f);
                        break;
                    }
                    case "Custom": {
                        this.current = this.TimeCustom.getFloat();
                        break;
                    }
                    case "RealWorldTime": {
                        this.current = this.getWorldTimeByRealTime(WorldRender.getSmoothRealTime());
                    }
                }
                this.spinnedTime.to = this.current;
                this.smoothingTime = true;
            } else {
                this.spinnedTime.to = oldTime % 24000L;
            }
        } else {
            this.spinnedTime.to = (this.oldTime != -1.2398746E8f ? this.oldTime : (float)oldTime) % 24000.0f;
            if (this.smoothingTime && !sataFlag) {
                this.smoothingTime = false;
            }
        }
        return this.smoothingTime ? (long)this.spinnedTime.getAnim() : oldTime;
    }

    public int particleReCount(int prevCount) {
        float count = 1.0f;
        if (get != null) {
            WorldRender mod = get;
            if (mod.actived && this.CustomParticles.getBool()) {
                count *= this.ParticleCount.getFloat();
            }
        }
        return (int)((float)prevCount * count);
    }

    public float particleReSpeed(float prevParticleSpeed) {
        float speed = 1.0f;
        if (get != null) {
            WorldRender mod = get;
            if (mod.actived && this.CustomParticles.getBool()) {
                speed *= this.ParticleSpeed.getFloat();
            }
        }
        return prevParticleSpeed * speed;
    }

    public boolean isRenderBloom() {
        if (get != null && WorldRender.get.actived && this.WorldBloom.canBeRender()) {
            return this.BloomPower.getFloat() > 0.0f && this.BloomPower.getFloat() <= 1.0f;
        }
        return false;
    }

    public void drawWorldBloom() {
        GaussianBlur.renderBlur(0.8f - this.BloomPower.getAnimation() * this.WorldBloom.getAnimation() / 2.0f);
    }

    @EventTarget
    public void onLightingCheck(EventLightingCheck event) {
        if (this.BlockLightFix.getBool() && (event.getEnumSkyBlock() == EnumSkyBlock.SKY || event.getEnumSkyBlock() == EnumSkyBlock.BLOCK && Minecraft.player != null && Minecraft.player.getDistanceToBlockPos(event.getPos()) > 64.0 || event.getEnumSkyBlock() == EnumSkyBlock.SKY && event.getPos().getY() >= 253)) {
            event.cancel();
        }
    }

    @Override
    public void onUpdate() {
        if (this.SpawnAnimations.getBool()
                && Minecraft.player != null
                && Minecraft.player.ticksExisted < 20
                && !this.wantToCustomLoading
                && !this.altWorldLoading.hasFinished()
                && mc.world != null) {
            int tick = this.shakeType == 0 ? 3 : (this.shakeType == 1 ? 3 : (this.shakeType == 2 ? 11 : 13));
            int longestTicks = this.shakeType == 0 ? 5 : (this.shakeType == 1 ? 6 : (this.shakeType == 2 ? 3 : 4));
            if (Minecraft.player.ticksExisted >= tick + 1 && Minecraft.player.ticksExisted <= tick + 1 + longestTicks) {
                this.genPSParticles((int)(2500.0F / (float)longestTicks), 4.0F, Minecraft.player.getPositionVector(), 3250L);
            }
        }

        this.updatePSParticlesList();
        this.updateTranslationCamera();
        if (this.CustomViewBobbing.getBool() && !mc.gameSettings.viewBobbing) {
            Client.msg("§f§lModules:§r §7[§lWorldRender§r§7]: Для использования CustomViewBobbing был включен ViewBobbing в игре.", false);
            mc.gameSettings.viewBobbing = true;
        }

        this.updateFreeLookState(this.actived);
        this.isItemPhisics = this.ItemPhisics.getBool();
        if (this.ChunksDebuger.getBool() && Minecraft.player.ticksExisted < 7 && Minecraft.player.ticksExisted > 5) {
            mc.renderGlobal.loadRenderers();
        }

        if (Minecraft.player.getActivePotionEffect(Potion.getPotionById(16)) != null
                && Minecraft.player.getActivePotionEffect(Potion.getPotionById(16)).getDuration() >= 16345) {
            Minecraft.player.removeActivePotionEffect(Potion.getPotionById(16));
        }

        if (this.rend != this.RenderBarrier.getBool()) {
            mc.renderGlobal.loadRenderers();
            this.rend = this.RenderBarrier.getBool();
        }

        if (this.FullBright.getBool() && this.BrightMode.currentMode.equalsIgnoreCase("Gamma")) {
            if (mc.gameSettings.gammaSetting != 1000.0F) {
                this.oldGamma = mc.gameSettings.gammaSetting;
            }

            mc.gameSettings.gammaSetting = 1000.0F;
        } else if (this.oldGamma != -1.0F) {
            mc.gameSettings.gammaSetting = this.oldGamma;
            this.oldGamma = -1.0F;
        }
    }

    @Override
    public void onToggled(boolean actived) {
        this.updateFreeLookState(false);
        if (this.RenderBarrier.getBool()) {
            WorldRender.mc.renderGlobal.loadRenderers();
        }
        if (this.FullBright.getBool()) {
            if (actived) {
                if (this.BrightMode.currentMode.equalsIgnoreCase("Gamma")) {
                    this.oldGamma = WorldRender.mc.gameSettings.gammaSetting;
                }
            } else {
                this.isItemPhisics = false;
                if (this.BrightMode.currentMode.equalsIgnoreCase("Gamma") && this.oldGamma != -1.0f) {
                    WorldRender.mc.gameSettings.gammaSetting = this.oldGamma;
                    this.oldGamma = -1.0f;
                }
            }
        }
        super.onToggled(actived);
    }

    @EventTarget
    private void onRenderChunk(EventRenderChunk event) {
        if (this.ChunkAnim.getBool() && Minecraft.player != null && !this.renderChunkMap.containsKey(event.getRenderChunk())) {
            this.renderChunkMap.put(event.getRenderChunk(), new AtomicLong(-1L));
        }
    }

    @EventTarget
    private void onChunkRender(EventRenderChunkContainer event) {
        if (this.ChunkAnim.getBool() && this.renderChunkMap.containsKey(event.getRenderChunk())) {
            float timeOf;
            long timeDifference;
            AtomicLong timeAlive = this.renderChunkMap.get(event.getRenderChunk());
            long timeClone = timeAlive.get();
            if (timeClone == -1L) {
                timeClone = System.currentTimeMillis();
                timeAlive.set(timeClone);
            }
            if ((float)(timeDifference = System.currentTimeMillis() - timeClone) <= (timeOf = 450.0f)) {
                double easeQuad = MathUtils.easeInOutQuad((float)timeDifference / timeOf);
                Vec3d chunkVec = new Vec3d(event.getRenderChunk().getPosition().getX(), event.getRenderChunk().getPosition().getY(), event.getRenderChunk().getPosition().getZ());
                List<Vec3d> sidesVecs = Arrays.asList(chunkVec.addVector(0.0, 0.0, 0.0), chunkVec.addVector(16.0, 0.0, 0.0), chunkVec.addVector(0.0, 0.0, 16.0), chunkVec.addVector(16.0, 0.0, 16.0));
                Vec3d cameraPos = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
                sidesVecs.sort(Comparator.comparing(sideVec -> sideVec.distanceTo(cameraPos)));
                Vec3d nearedPos = sidesVecs.get(0);
                GlStateManager.translate(-chunkVec.xCoord + nearedPos.xCoord, 0.0, -chunkVec.zCoord + nearedPos.zCoord);
                GlStateManager.scale(easeQuad, 1.0, easeQuad);
                GlStateManager.translate(-(-chunkVec.xCoord + nearedPos.xCoord), 0.0, -(-chunkVec.zCoord + nearedPos.zCoord));
            }
        }
    }

    @EventTarget
    public void onTimeUpdatePacket(EventReceivePacket event) {
        Packet packet = event.getPacket();
        if (packet instanceof SPacketTimeUpdate) {
            SPacketTimeUpdate packetTime = (SPacketTimeUpdate)packet;
            if (this.actived && this.WorldReTime.getBool()) {
                this.oldTime = packetTime.getWorldTime();
            }
        }
    }

    public void worldLoadHookCancel(WorldClient worldClient, Runnable doLoadWorld) {
        if (this.isActived() && this.SpawnAnimations.getBool() && worldClient != null) {
            this.wantToCustomLoading = true;
            try {
                if (this.wantToCustomLoading && this.soundDelay.hasReached(700.0)) {
                    this.shakeType = (int)((float)(System.currentTimeMillis() % 200L) / 50.0f);
                    MusicHelper.playSound("loadworld" + this.shakeType + ".wav", 0.4f * WorldRender.mc.gameSettings.getSoundLevel(SoundCategory.MASTER));
                    this.soundDelay.reset();
                }
            }
            catch (Exception e) {
                e.fillInStackTrace();
            }
        }
    }

    public void updateCustomLoadWorld() {
        if (!this.SpawnAnimations.getBool()) {
            if (this.wantToCustomLoading) {
                this.wantToCustomLoading = false;
            }
            return;
        }
        float speedMul = this.shakeType == 0 ? 0.65f : (this.shakeType == 1 ? 0.5f : (this.shakeType == 2 ? 0.45f : (this.shakeType == 3 ? 0.425f : 1.0f)));
        this.altWorldLoading.update((this.wantToCustomLoading ? 0.12f : 0.055f) * speedMul, this.wantToCustomLoading);
        ScaledResolution sr = new ScaledResolution(mc);
        this.altWorldLoading.insertRender2D(() -> RenderUtils.drawRect(0.0, 0.0, sr.getScaledWidth(), sr.getScaledHeight(), ColorUtils.getColor(0)), sr, 16);
        if (this.altWorldLoading.hasFinished()) {
            this.wantToCustomLoading = false;
        }
    }

    private float[] getPsShapesUVMinMax(int indexTexture) {
        float[] uv = new float[4];
        uv[0] = 0.0f;
        uv[2] = 1.0f;
        uv[1] = (float)indexTexture / 4.0f;
        uv[3] = uv[1] + 0.25f;
        return uv;
    }

    private int getPsShapeColor(int indexTexture, float toWhite, float alphaPC) {
        int color = this.COLORS_SHAPES[indexTexture];
        if (toWhite != 0.0f) {
            color = ColorUtils.getOverallColorFrom(color, -1, toWhite);
        }
        if (alphaPC != 1.0f) {
            color = ColorUtils.swapAlpha(color, alphaPC * 255.0f);
        }
        return color;
    }

    private void shapeQuadWithOutBegin(float x, float y, float x2, float y2, float[] uv, int color) {
        this.buffer.pos(x, y).tex(uv[0], uv[1]).color(color).endVertex();
        this.buffer.pos(x2, y).tex(uv[2], uv[1]).color(color).endVertex();
        this.buffer.pos(x2, y2).tex(uv[2], uv[3]).color(color).endVertex();
        this.buffer.pos(x, y2).tex(uv[0], uv[3]).color(color).endVertex();
    }

    private void updatePSParticlesList() {
        if (this.PS_PARTICLES_LIST.isEmpty()) {
            return;
        }
        try {
            this.PS_PARTICLES_LIST.removeIf(PSParticle::isToRemove);
            if (this.PS_PARTICLES_LIST.isEmpty()) {
                return;
            }
            this.PS_PARTICLES_LIST.forEach(PSParticle::update);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderPSParticles(float partialTicks, float scale) {
        if (this.PS_PARTICLES_LIST.isEmpty()) {
            return;
        }
        List<PSParticle> psParts = this.PS_PARTICLES_LIST.stream().filter(psPart -> !psPart.isToRemove()).toList();
        if (psParts.isEmpty()) {
            return;
        }
        double glX = RenderManager.viewerPosX;
        double glY = RenderManager.viewerPosY;
        double glZ = RenderManager.viewerPosZ;
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        WorldRender.mc.entityRenderer.disableLightmap();
        GL11.glAlphaFunc((int)516, (float)0.003921569f);
        GL11.glLineWidth((float)1.0f);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2896);
        GL11.glShadeModel((int)7425);
        GL11.glDisable((int)3008);
        GL11.glDisable((int)2884);
        GL11.glDepthMask((boolean)false);
        GL11.glTranslated((double)(-glX), (double)(-glY), (double)(-glZ));
        GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        mc.getTextureManager().bindTexture(this.PS_SHAPES_TEXTURE);
        psParts.forEach(psPart -> psPart.draw(partialTicks, scale, 1.0f));
        GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        GL11.glTranslated((double)glX, (double)glY, (double)glZ);
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2884);
        GL11.glAlphaFunc((int)516, (float)0.1f);
        GL11.glLineWidth((float)1.0f);
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3553);
        GlStateManager.resetColor();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glPopMatrix();
    }

    private void genPSParticles(int crateSount, float maxDistance, Vec3d ofPos, long lifeTime) {
        try {
            for (int counter = 0; counter < crateSount; ++counter) {
                float dst = maxDistance * this.RANDOM.nextFloat(1.0f);
                float yaw = this.RANDOM.nextFloat() * 360.0f;
                float pitch = this.RANDOM.nextFloat(-90.0f, 0.0f);
                double xOff = Math.sin(Math.toRadians(yaw)) * (double)dst;
                double zOff = -Math.cos(Math.toRadians(yaw)) * (double)dst;
                double yOff = Math.sin(Math.toRadians(pitch)) * (double)dst;
                Vec3d partPos = ofPos.addVector(xOff, yOff, zOff);
                this.PS_PARTICLES_LIST.add(new PSParticle(partPos, (long)((float)lifeTime * this.RANDOM.nextFloat(0.5f, 1.5f))));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void alwaysRender3D(float partialTicks) {
        if (!this.isActived()) {
            return;
        }
        this.renderPSParticles(partialTicks, 1.5f);
    }

    @EventTarget
    public void onPacketReceive(EventReceivePacket event) {
        if (this.actived && this.SpawnAnimations.getBool()) {
            Packet packet = event.getPacket();
            if (packet instanceof SPacketSoundEffect) {
                SPacketSoundEffect effectSound = (SPacketSoundEffect)packet;
                if (Minecraft.player != null && Minecraft.player.ticksExisted <= 15 && (effectSound.getCategory() != SoundCategory.PLAYERS && effectSound.getCategory() != SoundCategory.BLOCKS || effectSound.getCategory() != SoundCategory.WEATHER)) {
                    event.cancel();
                }
            }
            if ((packet = event.getPacket()) instanceof SPacketPlayerPosLook) {
                double dstXZ;
                SPacketPlayerPosLook lookPacket = (SPacketPlayerPosLook)packet;
                if (Minecraft.player != null && (dstXZ = Minecraft.player.getDistance(lookPacket.getX(), Minecraft.player.posY, lookPacket.getZ())) > 256.0 && Minecraft.player.getSpeed() < dstXZ / 5.0) {
                    this.wantToCustomLoading = true;
                    this.altWorldLoading.update(this.wantToCustomLoading ? 0.2f : 0.06f, this.wantToCustomLoading);
                }
            }
        }
    }

    private class PSParticle {
        private double xPos;
        private double yPos;
        private double zPos;
        private double prevXPos;
        private double prevYPos;
        private double prevZPos;
        private double motionX;
        private double motionY;
        private double motionZ;
        private final long spawnTime;
        private final long maxTime;
        private final byte variant;
        private final float[] uv;

        public PSParticle(Vec3d pos, long maxTime) {
            this.motionX = WorldRender.this.RANDOM.nextFloat(-0.4f, 0.4f);
            this.motionY = WorldRender.this.RANDOM.nextFloat(0.7f);
            this.motionZ = WorldRender.this.RANDOM.nextFloat(-0.4f, 0.4f);
            this.spawnTime = System.currentTimeMillis();
            this.variant = (byte)WorldRender.this.RANDOM.nextInt(4);
            this.uv = WorldRender.this.getPsShapesUVMinMax(this.variant);
            this.xPos = pos.xCoord;
            this.yPos = pos.yCoord;
            this.zPos = pos.zCoord;
            this.prevXPos = pos.xCoord;
            this.prevYPos = pos.yCoord;
            this.prevZPos = pos.zCoord;
            this.maxTime = maxTime;
        }

        public float getTimePC() {
            return (float)Math.min(System.currentTimeMillis() - this.spawnTime, this.maxTime) / (float)this.maxTime;
        }

        public boolean isToRemove() {
            return this.getTimePC() == 1.0f;
        }

        public void update() {
            this.prevXPos = this.xPos;
            this.prevYPos = this.yPos;
            this.prevZPos = this.zPos;
            this.xPos += this.motionX;
            this.yPos += this.motionY;
            this.zPos += this.motionZ;
            if (Module.mc.world != null) {
                BlockPos bposX = new BlockPos(this.xPos + this.motionX, this.yPos + this.motionY, this.zPos);
                BlockPos bposY = new BlockPos(this.xPos, this.yPos + this.motionY, this.zPos);
                BlockPos bposZ = new BlockPos(this.xPos, this.yPos + this.motionY, this.zPos + this.motionZ);
                IBlockState stX = Module.mc.world.getBlockState(bposX);
                IBlockState stY = Module.mc.world.getBlockState(bposY);
                IBlockState stZ = Module.mc.world.getBlockState(bposZ);
                if (stX.getMaterial().blocksMovement()) {
                    this.motionX *= -0.5;
                }
                if (stY.getMaterial().blocksMovement()) {
                    this.motionY *= (double)-0.6f;
                    this.motionX *= (double)0.9f;
                    this.motionZ *= (double)0.9f;
                }
                if (stZ.getMaterial().blocksMovement()) {
                    this.motionZ *= -0.5;
                }
            }
            this.motionX *= (double)0.99f;
            this.motionY *= (double)0.97f;
            this.motionY -= (double)0.02f;
            this.motionZ *= (double)0.99f;
        }

        public double getXPos(float partialTicks) {
            return this.prevXPos + (this.xPos - this.prevXPos) * (double)partialTicks;
        }

        public double getYPos(float partialTicks) {
            return this.prevYPos + (this.yPos - this.prevYPos) * (double)partialTicks;
        }

        public double getZPos(float partialTicks) {
            return this.prevZPos + (this.zPos - this.prevZPos) * (double)partialTicks;
        }

        public void draw(float partialTicks, float scale, float alphaPC) {
            if ((alphaPC *= Math.min((1.0f - this.getTimePC()) * 2.0f, 1.0f)) * 255.0f < 1.0f) {
                return;
            }
            scale *= alphaPC;
            float toWhite = 1.0f - Math.min(this.getTimePC() * 1.5f, 1.0f);
            toWhite = (float)MathUtils.easeInOutQuad(toWhite);
            toWhite *= toWhite;
            int color = WorldRender.this.getPsShapeColor(this.variant, toWhite, alphaPC);
            GL11.glPushMatrix();
            GL11.glTranslated((double)this.getXPos(partialTicks), (double)this.getYPos(partialTicks), (double)this.getZPos(partialTicks));
            GL11.glRotated((double)(Module.mc.getRenderManager().playerViewY + WorldRender.get.offYawOrient), (double)0.0, (double)-1.0, (double)0.0);
            GL11.glRotated((double)(Module.mc.getRenderManager().playerViewX + WorldRender.get.offPitchOrient), (double)(Module.mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0), (double)0.0, (double)0.0);
            GL11.glScaled((double)-0.1f, (double)-0.1f, (double)0.1f);
            WorldRender.this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            WorldRender.this.shapeQuadWithOutBegin(-scale / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, this.uv, color);
            WorldRender.this.tessellator.draw();
            GL11.glPopMatrix();
        }
    }
}

