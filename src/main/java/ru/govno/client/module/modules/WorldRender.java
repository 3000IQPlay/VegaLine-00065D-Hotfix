package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import org.lwjgl.input.Keyboard;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventLightingCheck;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRenderChunk;
import ru.govno.client.event.events.EventRenderChunkContainer;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.GaussianBlur;

public class WorldRender extends Module {
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
   public BoolSettings WorldReTime;
   public BoolSettings SkyRecolor;
   public BoolSettings FogRedistance;
   public BoolSettings ClearWeather;
   public BoolSettings CustomCamDist;
   public BoolSettings ClientCamera;
   public BoolSettings AntiAliasing;
   public BoolSettings AltReverseCamera;
   public BoolSettings FreeLook;
   public ModeSettings SelfSkin;
   public ModeSettings BrightMode;
   public ModeSettings Time;
   public ModeSettings SkyColorMode;
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
   public ColorSettings SkyColorPick;
   public ColorSettings SkyColorPick2;
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
   public AnimationUtils gammaAnimation = new AnimationUtils(0.0F, 0.0F, 0.02F);
   AnimationUtils orientAnim = new AnimationUtils(0.0F, 0.0F, 0.0333F);
   public AnimationUtils fovMultiplier = new AnimationUtils(1.0F, 1.0F, 0.08F);
   public boolean isItemPhisics = false;
   protected AnimationUtils spinnedTime = new AnimationUtils(0.0F, 0.0F, 0.075F);
   protected float current = 0.0F;
   boolean smoothingTime;
   float oldTime = -1.2398746E8F;
   boolean rend = true;
   public float oldGamma;
   private final HashMap<RenderChunk, AtomicLong> renderChunkMap = new HashMap<>();

   public WorldRender() {
      super("WorldRender", 0, Module.Category.RENDER);
      this.settings.add(this.ClientPlayersSkins = new BoolSettings("ClientPlayersSkins", true, this));
      this.settings
         .add(
            this.SelfSkin = new ModeSettings(
               "SelfSkin",
               "Skin4",
               this,
               new String[]{
                  "Skin1",
                  "Skin2",
                  "Skin3",
                  "Skin4",
                  "Skin5",
                  "Skin6",
                  "Skin7",
                  "Skin8",
                  "Skin9",
                  "Skin10",
                  "Skin11",
                  "Skin12",
                  "Skin13",
                  "Skin14",
                  "Skin15",
                  "Skin16"
               },
               () -> this.ClientPlayersSkins.getBool()
            )
         );
      this.settings.add(this.FastWorldLoad = new BoolSettings("FastWorldLoad", false, this));
      this.settings.add(this.RenderBarrier = new BoolSettings("RenderBarrier", false, this));
      this.settings.add(this.ItemPhisics = new BoolSettings("ItemPhisics", true, this));
      this.settings.add(this.ChunksDebuger = new BoolSettings("ChunksDebuger", false, this));
      this.settings.add(this.ChunkAnim = new BoolSettings("ChunkAnim", false, this));
      this.settings.add(this.FullBright = new BoolSettings("FullBright", false, this));
      this.settings.add(this.BrightMode = new ModeSettings("BrightMode", "Vision", this, new String[]{"Vision", "Gamma"}, () -> this.FullBright.getBool()));
      this.settings.add(this.BlockLightFix = new BoolSettings("BlockLightFix", true, this));
      this.settings.add(this.WorldBloom = new BoolSettings("WorldBloom", true, this));
      this.settings.add(this.BloomPower = new FloatSettings("BloomPower", 0.4F, 0.75F, 0.05F, this, () -> this.WorldBloom.getBool()));
      this.settings.add(this.CustomParticles = new BoolSettings("CustomParticles", false, this));
      this.settings.add(this.ParticleSpeed = new FloatSettings("ParticleSpeed", 0.5F, 2.0F, 0.1F, this, () -> this.CustomParticles.getBool()));
      this.settings.add(this.ParticleCount = new FloatSettings("ParticleCount", 0.85F, 5.0F, 0.25F, this, () -> this.CustomParticles.getBool()));
      this.settings.add(this.WorldReTime = new BoolSettings("WorldReTime", true, this));
      this.settings
         .add(
            this.Time = new ModeSettings(
               "Time",
               "Night",
               this,
               new String[]{"Evening", "Night", "Morning", "Day", "SpinTime", "Custom", "RealWorldTime"},
               () -> this.WorldReTime.getBool()
            )
         );
      this.settings
         .add(
            this.TimeCustom = new FloatSettings(
               "TimeCustom", 14000.0F, 24000.0F, 0.0F, this, () -> this.WorldReTime.getBool() && this.Time.currentMode.equalsIgnoreCase("Custom")
            )
         );
      this.settings
         .add(
            this.TimeSpinSpeed = new FloatSettings(
               "TimeSpinSpeed", 1.0F, 3.0F, 0.1F, this, () -> this.WorldReTime.getBool() && this.Time.currentMode.equalsIgnoreCase("SpinTime")
            )
         );
      this.settings.add(this.SkyRecolor = new BoolSettings("SkyRecolor", false, this));
      this.settings
         .add(
            this.SkyColorMode = new ModeSettings(
               "SkyColorMode", "Colored", this, new String[]{"Colored", "Fade", "Client", "ReBright"}, () -> this.SkyRecolor.getBool()
            )
         );
      this.settings
         .add(
            this.SkyColorPick = new ColorSettings(
               "SkyColorPick",
               ColorUtils.getColor(40, 40, 255, 140),
               this,
               () -> this.SkyRecolor.getBool()
                     && (this.SkyColorMode.currentMode.equalsIgnoreCase("Colored") || this.SkyColorMode.currentMode.equalsIgnoreCase("Fade"))
            )
         );
      this.settings
         .add(
            this.SkyColorPick2 = new ColorSettings(
               "SkyColorPick2",
               ColorUtils.getColor(40, 40, 255, 60),
               this,
               () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("Fade")
            )
         );
      this.settings
         .add(
            this.SkyFadeSpeed = new FloatSettings(
               "SkyFadeSpeed", 0.35F, 1.5F, 0.1F, this, () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("Fade")
            )
         );
      this.settings
         .add(
            this.SkyClientColBright = new FloatSettings(
               "SkyClientColBright", 0.6F, 1.0F, 0.05F, this, () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("Client")
            )
         );
      this.settings
         .add(
            this.SkyBright = new FloatSettings(
               "SkyBright", 0.4F, 1.0F, 0.0F, this, () -> this.SkyRecolor.getBool() && this.SkyColorMode.currentMode.equalsIgnoreCase("ReBright")
            )
         );
      this.settings.add(this.FogRedistance = new BoolSettings("FogRedistance", false, this));
      this.settings.add(this.FogDistanceCustom = new FloatSettings("FogDistanceCustom", 40.0F, 120.0F, 15.0F, this, () -> this.FogRedistance.getBool()));
      this.settings.add(this.ClearWeather = new BoolSettings("ClearWeather", true, this));
      this.settings.add(this.CustomCamDist = new BoolSettings("CustomCamDist", true, this));
      this.settings.add(this.CameraRedistance = new FloatSettings("CameraRedistance", 4.0F, 15.0F, 1.0F, this, () -> this.CustomCamDist.getBool()));
      this.settings.add(this.ClientCamera = new BoolSettings("ClientCamera", false, this));
      this.settings.add(this.AntiAliasing = new BoolSettings("AntiAliasing", true, this));
      this.settings.add(this.AltReverseCamera = new BoolSettings("AltReverseCamera", false, this));
      this.settings.add(this.FreeLook = new BoolSettings("FreeLook", false, this));
      get = this;
   }

   public void updateFreeLookRotation(float yawPlus, float pitchPlus, float partialTicks) {
      this.prevCYawOff = this.cYawOff;
      this.prevCPitchOff = this.cPitchOff;
      this.cYawOff += yawPlus;
      this.cPitchOff += pitchPlus;
      if (this.cPitchOff >= 90.0F - Minecraft.player.rotationPitch) {
         this.cPitchOff = 90.0F - Minecraft.player.rotationPitch;
      }

      if (this.cPitchOff <= -90.0F - Minecraft.player.rotationPitch) {
         this.cPitchOff = -90.0F - Minecraft.player.rotationPitch;
      }

      this.offYawOrient = MathUtils.lerp(this.prevCYawOff, this.cYawOff, partialTicks);
      this.offPitchOrient = MathUtils.lerp(this.prevCPitchOff, this.cPitchOff, partialTicks);
   }

   public void updateFreeLookState(boolean enabledModule) {
      if ((!this.FreeLook.getBool() || !enabledModule) && this.freeLookState) {
         this.freeLookState = false;
         mc.gameSettings.thirdPersonView = 0;
      } else {
         if (!this.FreeLook.getBool()) {
            return;
         }

         if (this.actived) {
            this.freeLookState = mc.gameSettings.keyBindTogglePerspective.isKeyDown();
            mc.gameSettings.thirdPersonView = this.freeLookState ? 1 : 0;
         }
      }

      if (this.freeLookState != this.prevFreeLookState) {
         if (this.freeLookState) {
            this.sYaw = Minecraft.player.rotationYaw;
            this.sPitch = Minecraft.player.rotationPitch;
         } else {
            this.sYaw = 0.0F;
            this.sPitch = 0.0F;
            this.cPitchOff = 0.0F;
            this.cYawOff = 0.0F;
            this.offYawOrient = 0.0F;
            this.offPitchOrient = 0.0F;
         }
      }

      if (this.sYaw != 0.0F || this.sPitch != 0.0F) {
         Minecraft.player.rotationYaw = this.sYaw;
         Minecraft.player.rotationPitch = this.sPitch;
      }

      this.prevFreeLookState = this.freeLookState;
   }

   private int getClientSkinsCount() {
      return 16;
   }

   public ResourceLocation updatedResourceSkin(ResourceLocation prevResource, Entity entity) {
      if (get != null && this.actived && entity != null && entity instanceof EntityPlayer player && get.ClientPlayersSkins.getBool()) {
         int index = (
                  !(player instanceof EntityPlayerSP) && player != FreeCam.fakePlayer
                     ? player.getEntityId() * 3
                     : Integer.parseInt(this.SelfSkin.currentMode.replace("Skin", ""))
               )
               % this.getClientSkinsCount()
            + 1;
         prevResource = new ResourceLocation("vegaline/modules/worldrender/skins/default/skin" + index + ".png");
      }

      return prevResource;
   }

   public float setupedGammaNightVision() {
      this.gammaAnimation.to = !Panic.stop && get != null && this.FullBright.getBool() && this.BrightMode.currentMode.equalsIgnoreCase("Vision") ? 1.0F : 0.0F;
      float gamma = !Panic.stop ? this.gammaAnimation.getAnim() : 0.0F;
      return (double)gamma < 0.02 ? 0.0F : ((double)gamma > 0.98 ? 1.0F : gamma);
   }

   public boolean isReverseCamera() {
      return !Panic.stop && get != null && this.actived && this.AltReverseCamera.getBool() && Keyboard.isKeyDown(56) && mc.currentScreen == null;
   }

   public float orientCustom(float partialTicks) {
      if (!Panic.stop && get != null && get.actived && this.ClientCamera.getBool()) {
         this.orientAnim.to = MathUtils.clamp(Minecraft.player.rotationYaw - Minecraft.player.PreYaw, -90.0F, 90.0F) / 8.0F;
         return this.orientAnim.getAnim();
      } else {
         return 0.0F;
      }
   }

   public float getClientFovMul(float prevVal, float partialTicks) {
      if (!Panic.stop && get.isActived() && get.ClientCamera.getBool()) {
         float fovTo = 0.0F;
         if (Minecraft.player != null) {
            if (mc.gameSettings.thirdPersonView == 1 && Minecraft.player.isSneaking()) {
               fovTo = 1.01F;
            } else {
               if (mc.pointedEntity != null) {
                  fovTo += 0.01F;
               }

               if (Minecraft.player.isBowing() || Minecraft.player.isDrinking()) {
                  fovTo += (
                        0.05F
                           + MathUtils.clamp(
                              ((float)Minecraft.player.getItemInUseMaxCount() + partialTicks) / (Minecraft.player.isDrinking() ? 32.0F : 21.0F), 0.0F, 1.0F
                           )
                     )
                     * (Minecraft.player.isDrinking() ? -0.015F : 0.5F);
               }
            }
         }

         this.fovMultiplier.to = fovTo;
         if (MathUtils.getDifferenceOf(this.fovMultiplier.getAnim(), this.fovMultiplier.to) < 0.001F) {
            this.fovMultiplier.setAnim(this.fovMultiplier.to);
         }

         return prevVal + this.fovMultiplier.anim;
      } else {
         return prevVal;
      }
   }

   public double cameraRedistance(double prevDistance) {
      return !Panic.stop && get != null && get.actived && this.CustomCamDist.getBool() ? (double)this.CameraRedistance.getFloat() : prevDistance;
   }

   public float weatherReStrengh(float prevStrengh) {
      return !Panic.stop && get != null && get.actived && this.ClearWeather.getBool() ? 0.0F : prevStrengh;
   }

   public float[] getSkyColorRGB(float prevRed, float prevGreen, float prevBlue) {
      Module MOD = get;
      if (!Panic.stop && MOD != null && MOD.actived && this.SkyRecolor.getBool()) {
         String mode = this.SkyColorMode.currentMode;
         switch (mode) {
            case "Colored": {
               int pick1 = this.SkyColorPick.color;
               float aLP = ColorUtils.getGLAlphaFromColor(pick1);
               float[] rgbFloat = new float[]{
                  ColorUtils.getGLRedFromColor(pick1) * aLP, ColorUtils.getGLGreenFromColor(pick1) * aLP, ColorUtils.getGLBlueFromColor(pick1) * aLP
               };
               prevRed = rgbFloat[0];
               prevGreen = rgbFloat[1];
               prevBlue = rgbFloat[2];
               break;
            }
            case "Fade": {
               int pick1 = this.SkyColorPick.color;
               int pick2 = this.SkyColorPick2.color;
               float fadeSpeed = this.SkyFadeSpeed.getFloat() * 0.5F;
               int pickFadedColor = ColorUtils.fadeColorIndexed(pick1, pick2, fadeSpeed, 0);
               float aLP = ColorUtils.getGLAlphaFromColor(pickFadedColor);
               float[] rgbFloat = new float[]{
                  ColorUtils.getGLRedFromColor(pickFadedColor) * aLP,
                  ColorUtils.getGLGreenFromColor(pickFadedColor) * aLP,
                  ColorUtils.getGLBlueFromColor(pickFadedColor) * aLP
               };
               prevRed = rgbFloat[0];
               prevGreen = rgbFloat[1];
               prevBlue = rgbFloat[2];
               break;
            }
            case "Client": {
               int col = ClientColors.getColor1(0, this.SkyClientColBright.getFloat());
               float aLP = ColorUtils.getGLAlphaFromColor(col);
               float[] rgbFloat = new float[]{
                  ColorUtils.getGLRedFromColor(col) * aLP, ColorUtils.getGLGreenFromColor(col) * aLP, ColorUtils.getGLBlueFromColor(col) * aLP
               };
               prevRed = rgbFloat[0];
               prevGreen = rgbFloat[1];
               prevBlue = rgbFloat[2];
               break;
            }
            case "ReBright":
               float bright = MathUtils.clamp(this.SkyBright.getFloat(), 0.0F, 1.0F);
               prevRed *= bright;
               prevGreen *= bright;
               prevBlue *= bright;
         }
      }

      return new float[]{prevRed, prevGreen, prevBlue};
   }

   public float getRedistanceFogValue(float prevMaxDstSq) {
      return !Panic.stop && get.actived && this.FogRedistance.getBool() ? this.FogDistanceCustom.getFloat() : prevMaxDstSq;
   }

   private static float[] getSmoothRealTime() {
      Calendar calendar = Calendar.getInstance();
      Date date = calendar.getTime();
      float smoothSec = (float)(date.getSeconds() - 1) + (float)(System.currentTimeMillis() % 1000L) / 1000.0F;
      float smoothMins = (float)(date.getMinutes() - 1) + smoothSec / 60.0F;
      float smoothHours = (float)(date.getHours() - 1) + smoothMins / 60.0F;
      return new float[]{smoothSec, smoothMins, smoothHours};
   }

   private float getWorldTimeByRealTime(float[] realTime) {
      float smoothHourMC = (realTime[2] + 15.0F) % 24.0F;
      return smoothHourMC * 1000.0F;
   }

   public long getWorldReTime(long oldTime) {
      Module mod = get;
      boolean enabled = mod != null && mod.actived && this.WorldReTime.getBool();
      boolean sataFlag = enabled || MathUtils.getDifferenceOf(this.spinnedTime.anim, (float)(oldTime % 24000L)) > 80.0;
      if (enabled) {
         String mode = this.Time.currentMode;
         if (mode != null) {
            switch (mode) {
               case "Evening":
                  this.current = 12800.0F;
                  break;
               case "Night":
                  this.current = 18000.0F;
                  break;
               case "Morning":
                  this.current = 23500.0F;
                  break;
               case "Day":
                  this.current = 6000.0F;
                  break;
               case "SpinTime":
                  this.current = (float)(System.currentTimeMillis() % (long)((int)(10000.0F / this.TimeSpinSpeed.getFloat())))
                     / (10000.0F / this.TimeSpinSpeed.getFloat())
                     * 24000.0F;
                  if (MathUtils.getDifferenceOf(this.spinnedTime.anim, this.current) > 23000.0) {
                     this.spinnedTime.setAnim(this.current * 0.9F);
                  }
                  break;
               case "Custom":
                  this.current = this.TimeCustom.getFloat();
                  break;
               case "RealWorldTime":
                  this.current = this.getWorldTimeByRealTime(getSmoothRealTime());
            }

            this.spinnedTime.to = this.current;
            this.smoothingTime = true;
         } else {
            this.spinnedTime.to = (float)(oldTime % 24000L);
         }
      } else {
         this.spinnedTime.to = (this.oldTime != -1.2398746E8F ? this.oldTime : (float)oldTime) % 24000.0F;
         if (this.smoothingTime && !sataFlag) {
            this.smoothingTime = false;
         }
      }

      return this.smoothingTime ? (long)this.spinnedTime.getAnim() : oldTime;
   }

   public int particleReCount(int prevCount) {
      float count = 1.0F;
      if (get != null) {
         Module mod = get;
         if (mod.actived && this.CustomParticles.getBool()) {
            count *= this.ParticleCount.getFloat();
         }
      }

      return (int)((float)prevCount * count);
   }

   public float particleReSpeed(float prevParticleSpeed) {
      float speed = 1.0F;
      if (get != null) {
         Module mod = get;
         if (mod.actived && this.CustomParticles.getBool()) {
            speed *= this.ParticleSpeed.getFloat();
         }
      }

      return prevParticleSpeed * speed;
   }

   public boolean isRenderBloom() {
      return get != null && get.actived && this.WorldBloom.getBool() ? this.BloomPower.getFloat() > 0.0F && this.BloomPower.getFloat() <= 1.0F : false;
   }

   public void drawWorldBloom() {
      GaussianBlur.renderBlur(0.8F - this.BloomPower.getFloat() / 2.0F);
   }

   @EventTarget
   public void onLightingCheck(EventLightingCheck event) {
      if (this.BlockLightFix.getBool()
         && (
            event.getEnumSkyBlock() == EnumSkyBlock.SKY
               || event.getEnumSkyBlock() == EnumSkyBlock.BLOCK && Minecraft.player != null && Minecraft.player.getDistanceToBlockPos(event.getPos()) > 64.0
               || event.getEnumSkyBlock() == EnumSkyBlock.SKY && event.getPos().getY() >= 253
         )) {
         event.cancel();
      }
   }

   @Override
   public void onUpdate() {
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

      this.updateSmoothingCamera();
   }

   @Override
   public void onToggled(boolean actived) {
      this.updateFreeLookState(false);
      if (this.RenderBarrier.getBool()) {
         mc.renderGlobal.loadRenderers();
      }

      if (this.FullBright.getBool()) {
         if (actived) {
            if (this.BrightMode.currentMode.equalsIgnoreCase("Gamma")) {
               this.oldGamma = mc.gameSettings.gammaSetting;
            }
         } else {
            this.isItemPhisics = false;
            if (this.BrightMode.currentMode.equalsIgnoreCase("Gamma") && this.oldGamma != -1.0F) {
               mc.gameSettings.gammaSetting = this.oldGamma;
               this.oldGamma = -1.0F;
            }
         }
      }

      this.updateSmoothingCamera();
      super.onToggled(actived);
   }

   private double easeOutCubic(double t) {
      return --t * t * t + 1.0;
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
         AtomicLong timeAlive = this.renderChunkMap.get(event.getRenderChunk());
         long timeClone = timeAlive.get();
         if (timeClone == -1L) {
            timeClone = System.currentTimeMillis();
            timeAlive.set(timeClone);
         }

         long timeDifference = System.currentTimeMillis() - timeClone;
         float timeOf = 450.0F;
         if ((float)timeDifference <= timeOf) {
            double easeQuad = MathUtils.easeInOutQuad((double)((float)timeDifference / timeOf));
            Vec3d chunkVec = new Vec3d(
               (double)event.getRenderChunk().getPosition().getX(),
               (double)event.getRenderChunk().getPosition().getY(),
               (double)event.getRenderChunk().getPosition().getZ()
            );
            List<Vec3d> sidesVecs = Arrays.asList(
               chunkVec.addVector(0.0, 0.0, 0.0), chunkVec.addVector(16.0, 0.0, 0.0), chunkVec.addVector(0.0, 0.0, 16.0), chunkVec.addVector(16.0, 0.0, 16.0)
            );
            Vec3d cameraPos = new Vec3d(RenderManager.viewerPosX, RenderManager.viewerPosY, RenderManager.viewerPosZ);
            sidesVecs.sort(Comparator.comparing(sideVec -> sideVec.distanceTo(cameraPos)));
            Vec3d nearedPos = sidesVecs.get(0);
            GlStateManager.translate(-chunkVec.xCoord + nearedPos.xCoord, 0.0, -chunkVec.zCoord + nearedPos.zCoord);
            GlStateManager.scale(easeQuad, 1.0, easeQuad);
            GlStateManager.translate(-(-chunkVec.xCoord + nearedPos.xCoord), 0.0, -(-chunkVec.zCoord + nearedPos.zCoord));
         }
      }
   }

   private void updateSmoothingCamera() {
      if (this.ClientCamera.getBool() && this.isActived()) {
         Minecraft.player.rotationYaw = Minecraft.player.rotationYaw - (Minecraft.player.rotationYaw - Minecraft.player.PreYaw) / 16.0F;
         Minecraft.player.rotationPitch = Minecraft.player.rotationPitch - (Minecraft.player.rotationPitch - Minecraft.player.PrePitch) / 16.0F;
      }
   }

   @EventTarget
   public void onTimeUpdatePacket(EventReceivePacket event) {
      if (event.getPacket() instanceof SPacketTimeUpdate packetTime && this.actived && this.WorldReTime.getBool()) {
         this.oldTime = (float)packetTime.getWorldTime();
      }
   }
}
