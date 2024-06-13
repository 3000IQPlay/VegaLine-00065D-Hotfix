package ru.govno.client.module.modules;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import optifine.Config;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventTransformSideFirstPerson;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.BloomUtil;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.ShaderUtility;

public class ViewModel extends Module {
   public static ViewModel get;
   public ModeSettings Animation;
   public ModeSettings BlendFunction;
   public ModeSettings GlowColor;
   public FloatSettings AnimationSpeed;
   public FloatSettings ItemsScale;
   public FloatSettings GlowRadius;
   public FloatSettings GlowExplosure;
   public BoolSettings ResizeItems;
   public BoolSettings HandsGlow;
   public BoolSettings GlowFill;
   public BoolSettings GlowBloom;
   public ColorSettings GlowPickColor;
   public static Framebuffer glowBufferLHand = ShaderUtility.createFrameBuffer(
      new Framebuffer(new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth(), new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight(), true)
   );
   public static Framebuffer glowBufferRHand = ShaderUtility.createFrameBuffer(
      new Framebuffer(new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth(), new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight(), true)
   );

   public ViewModel() {
      super("ViewModel", 0, Module.Category.RENDER);
      this.settings
         .add(this.Animation = new ModeSettings("Animation", "Smooth", this, new String[]{"None", "Smooth", "Smooth2", "Lower", "Tower", "VEbalo", "Plunge"}));
      this.settings
         .add(this.AnimationSpeed = new FloatSettings("AnimationSpeed", 0.8F, 1.25F, 0.15F, this, () -> !this.Animation.currentMode.equalsIgnoreCase("None")));
      this.settings.add(this.BlendFunction = new ModeSettings("BlendFunction", "None", this, new String[]{"None", "Saturation", "BrightToAlpha"}));
      this.settings.add(this.ResizeItems = new BoolSettings("ResizeItems", false, this));
      this.settings.add(this.ItemsScale = new FloatSettings("ItemsScale", 0.4F, 1.25F, 0.025F, this, () -> this.ResizeItems.getBool()));
      this.settings.add(this.HandsGlow = new BoolSettings("HandsGlow", false, this));
      this.settings
         .add(this.GlowColor = new ModeSettings("GlowColor", "PickColor", this, new String[]{"PickColor", "ClientColor"}, () -> this.HandsGlow.getBool()));
      this.settings
         .add(
            this.GlowPickColor = new ColorSettings(
               "GlowPickColor",
               ColorUtils.getColor(124, 124, 124, 226),
               this,
               () -> this.HandsGlow.getBool() && this.GlowColor.currentMode.equalsIgnoreCase("PickColor")
            )
         );
      this.settings.add(this.GlowRadius = new FloatSettings("GlowRadius", 10.0F, 50.0F, 1.0F, this, () -> this.HandsGlow.getBool()));
      this.settings.add(this.GlowExplosure = new FloatSettings("GlowExplosure", 2.0F, 5.0F, 0.1F, this, () -> this.HandsGlow.getBool()));
      this.settings.add(this.GlowFill = new BoolSettings("GlowFill", false, this, () -> this.HandsGlow.getBool()));
      this.settings.add(this.GlowBloom = new BoolSettings("GlowBloom", false, this, () -> this.HandsGlow.getBool()));
      get = this;
   }

   public static Framebuffer getBuffer(EnumHand hand) {
      return hand == EnumHand.MAIN_HAND ? glowBufferRHand : glowBufferLHand;
   }

   public static void readBuffers(EnumHand hand) {
      if (hand == EnumHand.MAIN_HAND) {
         glowBufferRHand = null;
      } else {
         glowBufferLHand = null;
      }
   }

   public static void updateBuffer(Framebuffer buffer) {
      if (buffer == null) {
         buffer = ShaderUtility.createFrameBuffer(
            new Framebuffer(
               new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth(), new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight(), true
            )
         );
      }

      if (buffer != null && (buffer.framebufferWidth != mc.displayWidth || buffer.framebufferHeight != mc.displayHeight)) {
         buffer.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
      }
   }

   public static void startBuffer(Framebuffer buffer) {
      if (buffer != null) {
         GlStateManager.enableDepth();
         GlStateManager.enableAlpha();
         GL11.glDepthMask(true);
         updateBuffer(buffer);
         buffer.framebufferClear();
         buffer.bindFramebuffer(false);
      }
   }

   public static void endBuffer(Framebuffer buffer) {
      if (buffer != null) {
         buffer.unbindFramebuffer();
      }
   }

   public static boolean readRenderHandStart(EntityLivingBase baseIn, EnumHand hand) {
      if (!(baseIn instanceof EntityPlayerSP)) {
         return false;
      } else if (get.actived && get.HandsGlow.getBool()) {
         startBuffer(getBuffer(hand));
         return true;
      } else {
         return false;
      }
   }

   public static void readRenderHandStop(EnumHand hand) {
      endBuffer(getBuffer(hand));
   }

   public static boolean readRenderHandNullateOnEmpty(EnumHand hand, boolean silent) {
      ItemStack heldStack = Minecraft.player.getHeldItem(hand);
      if (heldStack != null) {
         Item stackItem = heldStack.getItem();
         if (stackItem == Items.air || mc.gameSettings.thirdPersonView != 0) {
            if (!silent) {
               getBuffer(hand).framebufferClear();
            }

            return true;
         }
      }

      return false;
   }

   public static void draw2dBuffers(float radius, float explosure, boolean fillTexture, int colorLeft, int colorRight, boolean bloom) {
      Arrays.asList(EnumHand.OFF_HAND, EnumHand.MAIN_HAND)
         .forEach(
            hand -> {
               Framebuffer buffer = getBuffer(hand);
               int color = hand.equals(EnumHand.OFF_HAND) ? colorLeft : colorRight;
               if (buffer != null) {
                  if (buffer.framebufferTexture != 0 && !readRenderHandNullateOnEmpty(hand, true)) {
                     GlStateManager.tryBlendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        bloom ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO
                     );
                     GlStateManager.disableAlpha();
                     BloomUtil.renderBlur(buffer.framebufferTexture, (float)((int)radius + 1), 1, color, explosure, !fillTexture);
                     GlStateManager.enableAlpha();
                     if (bloom) {
                        GlStateManager.tryBlendFuncSeparate(
                           GlStateManager.SourceFactor.SRC_ALPHA,
                           GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                           GlStateManager.SourceFactor.ONE,
                           GlStateManager.DestFactor.ZERO
                        );
                     }
                  }
               }
            }
         );
   }

   public static void drawGlowHands() {
      if (get.actived && get.HandsGlow.getBool()) {
         if (Config.isShaders()) {
            get.HandsGlow.setBool(false);
            ClientTune.get.playGuiScreenCheckBox(false);
            Client.msg("§f§lModules:§r §7[§l" + get.name + "§r§7]: выключите шейдеры для использования GlowHands.", false);
         }

         int pickC = get.GlowPickColor.color;
         int clientC1 = ClientColors.getColor1();
         int clientC2 = ClientColors.getColor2();
         boolean isPickCol = get.GlowColor.currentMode.equalsIgnoreCase("PickColor");
         int colorLeft = isPickCol ? pickC : clientC1;
         int colorRight = isPickCol ? pickC : clientC2;
         float radius = get.GlowRadius.getFloat();
         float explosure = get.GlowExplosure.getFloat();
         boolean fill = get.GlowFill.getBool();
         boolean bloom = get.GlowBloom.getBool();
         draw2dBuffers(radius, explosure, fill, colorLeft, colorRight, bloom);
      }
   }

   public static int getSwingSpeed(EntityLivingBase baseIn, int prevSpeed) {
      return !Panic.stop
            && baseIn instanceof EntityPlayerSP
            && get.actived
            && get.AnimationSpeed.getFloat() != 1.0F
            && !get.Animation.currentMode.equalsIgnoreCase("None")
         ? (int)((float)prevSpeed / MathUtils.clamp(get.AnimationSpeed.getFloat(), 0.5F, 3.0F))
         : prevSpeed;
   }

   public static void setupTransperentModel(EntityLivingBase baseIn, boolean isStart) {
      String func = get.BlendFunction.currentMode;
      if (baseIn instanceof EntityPlayerSP) {
         if (!Panic.stop && get.actived && !func.isEmpty() && !func.equalsIgnoreCase("Default")) {
            if (isStart) {
               switch (func) {
                  case "Default":
                     break;
                  case "Saturation":
                     GL11.glBlendFunc(768, 771);
                     break;
                  case "BrightToAlpha":
                     GL11.glBlendFunc(768, 1);
                     break;
                  default:
                     return;
               }

               return;
            }

            GL11.glBlendFunc(770, 771);
         }
      }
   }

   public static void translate(String mode, float f, float f1, int i) {
      if (!mode.isEmpty()) {
         switch (mode) {
            case "None":
            default:
               break;
            case "Smooth":
               GlStateManager.rotate(f1 * -80.0F + f * 20.0F, 310.0F, 40.0F + f * 53.0F + f * 100.0F * (float)i, -140.0F * (float)i);
               break;
            case "Smooth2":
               GlStateManager.translate(0.0F, -f1 * 0.1F, 0.0F);
               GlStateManager.rotate(f1 * -100.0F * (0.5F + f), 310.0F, 40.0F, -110.0F * (float)i);
               break;
            case "Lower":
               GlStateManager.translate((double)(0.1F * (float)i), 0.2F, -0.125);
               GlStateManager.rotate((float)(70 * i), 0.0F, 1.0F, 0.0F);
               GlStateManager.translate(0.0, 0.038F, 0.008);
               GlStateManager.rotate(-90.0F - 5.0F * f * f1 * 10.0F * f1, 1.0F, 0.0F, 0.0F);
               GlStateManager.translate(0.0, -0.038F, -0.008);
               break;
            case "Tower":
               GlStateManager.translate(0.4 * (double)i, 0.1, -0.4);
               GlStateManager.translate(0.0, 0.038F, 0.008);
               GlStateManager.rotate(10.0F * f * f1 * 15.0F * f1 * (float)i, 0.0F, 1.0F, 1.0F);
               GlStateManager.translate(0.0, -0.038F, -0.008);
               GlStateManager.rotate(-115.0F, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate((float)(90 * i), 0.0F, 0.0F, 1.0F);
               GlStateManager.rotate((float)(30 * i), 0.0F, 1.0F, 0.0F);
               break;
            case "VEbalo":
               GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
               GlStateManager.translate(0.0F, 0.2F + f1, 0.0F);
               GlStateManager.translate(-0.2F, -0.2F, 0.0F);
               GlStateManager.scale(1.0F, 1.0F + f1 * 2.0F, 1.0F);
               GlStateManager.translate(0.2F, 0.2F, 0.0F);
               break;
            case "Plunge":
               GlStateManager.translate(0.2 * (double)i, 0.1, -0.2);
               GlStateManager.rotate(-115.0F, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate((float)(10 * i), 0.0F, 1.0F, 0.0F);
               GlStateManager.translate(0.0, 0.038F, 0.008);
               GlStateManager.translate(0.0F, 0.0F, -f * 0.1F * (float)i);
               GlStateManager.rotate((f1 * 60.0F - (1.0F - f) * 20.0F - 10.0F) * (float)i, 0.0F, -1.0F, 0.0F);
               GlStateManager.rotate(f * 40.0F * (float)i, 1.0F, 1.0F, -1.0F);
               GlStateManager.translate(0.0, -0.038F, -0.008);
               GlStateManager.rotate((float)(90 * i), 0.0F, 0.0F, 1.0F);
         }
      }
   }

   public static boolean animate(boolean silent, float f, float f1, int i) {
      if (!Panic.stop && get.actived && (!Fly.get.isActived() || !Fly.get.Helicopter.getBool() || !Fly.get.Helicopter.isVisible())) {
         String animMode = get.Animation.currentMode;
         if (!animMode.equalsIgnoreCase("None")) {
            int sideIndex = Minecraft.player.getPrimaryHand() == EnumHandSide.RIGHT ? 1 : -1;
            if (!silent && i == sideIndex) {
               translate(animMode, f, f1, i);
            }

            return true;
         }
      }

      return false;
   }

   @EventTarget
   public void onSidePerson(EventTransformSideFirstPerson event) {
      if (this.ResizeItems.getBool() && !mc.runScreenshot) {
         GlStateManager.translate(0.0F, 0.35F, 0.0F);
         GlStateManager.scale(this.ItemsScale.getFloat(), this.ItemsScale.getFloat(), this.ItemsScale.getFloat());
         GlStateManager.translate(0.0F, -0.35F, 0.0F);
      }
   }
}
