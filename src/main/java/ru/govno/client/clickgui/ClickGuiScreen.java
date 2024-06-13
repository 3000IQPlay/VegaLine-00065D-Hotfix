package ru.govno.client.clickgui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import optifine.Config;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.cfg.GuiConfig;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClickGui;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.module.settings.Settings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.CTextField;
import ru.govno.client.utils.HoverUtils;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class ClickGuiScreen extends GuiScreen {
   private static Clip clip;
   private static AudioInputStream stream;
   boolean playBindHold = false;
   public static AnimationUtils scale = new AnimationUtils(0.0F, 0.0F, 0.1F);
   public static AnimationUtils globalAlpha = new AnimationUtils(0.0F, 0.0F, 0.1F);
   static AnimationUtils scrolled = new AnimationUtils(0.0F, 0.0F, 0.15F);
   public List<Panel> panels = Arrays.asList(
      new Panel(Module.Category.COMBAT),
      new Panel(Module.Category.MOVEMENT),
      new Panel(Module.Category.RENDER),
      new Panel(Module.Category.PLAYER),
      new Panel(Module.Category.MISC)
   );
   public static AnimationUtils cfgScale = new AnimationUtils(0.0F, 0.0F, 0.1F);
   public static boolean colose = false;
   private final List<ClickGuiScreen.Parts> parts = new CopyOnWriteArrayList<>();
   private final List<ClickGuiScreen.FallPartsEffect> partsEff = new CopyOnWriteArrayList<>();
   static final CTextField textFieldSearch = new CTextField(1, Fonts.mntsb_20, 5, 5, 100, 20);
   static AnimationUtils searchAnim = new AnimationUtils(0.0F, 0.0F, 0.1F);
   static AnimationUtils searchStringWAnim = new AnimationUtils(0.0F, 0.0F, 0.1F);
   public static float scrollSmoothX;
   public static float scrollSmoothY;
   public static float dWhell;
   public static AnimationUtils scrollAnimation = new AnimationUtils(0.0F, 0.0F, 0.125F);
   static AnimationUtils imageScale = new AnimationUtils(0.0F, 0.0F, 0.1F);
   static AnimationUtils imageRender = new AnimationUtils(0.0F, 0.0F, 0.1F);
   String imageMode = null;
   static AnimationUtils waveAlphaRender = new AnimationUtils(0.0F, 0.0F, 0.1F);
   static AnimationUtils waveAlphaHRender = new AnimationUtils(0.0F, 0.0F, 0.1F);
   static AnimationUtils blurStrenghRender = new AnimationUtils(0.0F, 0.0F, 0.03F);
   static Module.Category colorCategory;
   static String descriptionName;
   float xn;
   float yn;
   static AnimationUtils keyCfgAnimScale = new AnimationUtils(1.0F, 1.0F, 0.15F);
   static AnimationUtils keyCfgAnimRotate = new AnimationUtils(0.0F, 0.0F, 0.15F);
   boolean openCfgKey;
   boolean isClickedCfgKey;
   static AnimationUtils crossRotate = new AnimationUtils(0.0F, 0.0F, 0.05F);
   static AnimationUtils crossClick = new AnimationUtils(0.0F, 0.0F, 0.06F);
   boolean checkCustomCross = false;
   static ResourceLocation MOUSE_TEXTURE_BASE = new ResourceLocation("vegaline/ui/clickgui/mousecrosshair/mousebase32.png");
   static ResourceLocation MOUSE_TEXTURE_OVERLAY = new ResourceLocation("vegaline/ui/clickgui/mousecrosshair/mouseoverlay32.png");
   static AnimationUtils darkness = new AnimationUtils(0.0F, 0.0F, 0.01F);
   public static AnimationUtils epilepsy = new AnimationUtils(0.0F, 0.0F, 0.05F);
   public static AnimationUtils scanLines = new AnimationUtils(0.0F, 0.0F, 0.05F);
   static final ResourceLocation BOUND_UP = new ResourceLocation("vegaline/ui/clickgui/cornerbounds/cornersectionup.png");
   static final ResourceLocation BOUND_DOWN = new ResourceLocation("vegaline/ui/clickgui/cornerbounds/cornersectiondown.png");
   public static final ResourceLocation BOUND_CONFLICT = new ResourceLocation("vegaline/ui/clickgui/components/mod/binding/bindconflict.png");
   static Tessellator tessellator = Tessellator.getInstance();
   static BufferBuilder builder = tessellator.getBuffer();
   static AnimationUtils boundsToggleAnim = new AnimationUtils(
      ClickGui.instance.ScreenBounds.getBool() ? 1.0F : 0.0F, ClickGui.instance.ScreenBounds.getBool() ? 1.0F : 0.0F, 0.075F
   );
   static AnimationUtils keySaveSound = new AnimationUtils(
      ClickGui.instance.MusicInGui.getBool() ? 1.0F : 0.0F, ClickGui.instance.MusicInGui.getBool() ? 1.0F : 0.0F, 0.075F
   );
   static AnimationUtils keySaveSoundToggle = new AnimationUtils(
      ClickGui.instance.SaveMusic.getBool() ? 1.0F : 0.0F, ClickGui.instance.SaveMusic.getBool() ? 1.0F : 0.0F, 0.1F
   );
   private static final ResourceLocation MUSIC_SAVE_BUTTON = new ResourceLocation("vegaline/ui/clickgui/musictuner/buttons/musicsavebutton.png");
   public TimerHelper timeMouse0Hold = new TimerHelper();
   public TimerHelper timeMouse1Hold = new TimerHelper();
   public TimerHelper timeMouse2Hold = new TimerHelper();
   public static boolean checkMouse0Hold;
   public static boolean checkMouse1Hold;
   public static boolean checkMouse2Hold;
   public static boolean clickedMouse0;
   public static boolean clickedMouse1;
   public static boolean clickedMouse2;
   static ArrayList<ClickGuiScreen.searchParticle> particles = new ArrayList<>();
   private final ResourceLocation BLOOM_TEX = new ResourceLocation("vegaline/ui/clickgui/bloomsimulate/bloom.png");

   public void setPlayBindHold(boolean canPlay) {
      if (!ClientTune.get.actived || !ClientTune.get.actived || !ClientTune.get.ClickGui.getBool()) {
         canPlay = false;
      }

      if (this.playBindHold != canPlay) {
         this.playBindHold = canPlay;
         ClientTune.get.playGuiModuleBindingHoldStatusSong(!canPlay);
         if (canPlay) {
            try {
               String resourcePath = "/assets/minecraft/vegaline/sounds/guibindhold.wav";
               InputStream inputStream = MusicHelper.class.getResourceAsStream(resourcePath);
               BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
               stream = AudioSystem.getAudioInputStream(bufferedInputStream);
            } catch (Exception var8) {
            }

            try {
               clip = AudioSystem.getClip();
            } catch (LineUnavailableException var7) {
               var7.printStackTrace();
            }

            if (stream == null) {
               return;
            }

            try {
               clip.open(stream);
            } catch (LineUnavailableException var5) {
               var5.printStackTrace();
            } catch (IOException var6) {
               var6.printStackTrace();
            }

            clip.start();
         } else if (clip != null) {
            clip.stop();
            clip = null;
         }
      }
   }

   void updatePlayingBinding() {
      this.setPlayBindHold(
         this.panels
            .stream()
            .anyMatch(panel -> panel.mods.stream().anyMatch(mod -> mod.binding && mod.bindHoldAnim.to != 0.0F || mod.sets.stream().anyMatch(set -> {
                     if (set instanceof CheckBox checkBox && checkBox.binding && checkBox.bindHoldAnim.to != 0.0F && checkBox.holdBindTimer.hasReached(50.0F)) {
                        return true;
                     }

                     return false;
                  })))
      );
   }

   public ClickGuiScreen() {
      int x = 80;

      for (Module.Category category : Module.Category.values()) {
         Panel panel = new Panel(category);
         panel.X = (float)x;
         panel.Y = 20.0F;
         panel.posX.setAnim(panel.X);
         panel.posX.to = panel.X;
         panel.posY.setAnim(panel.Y);
         panel.posY.to = panel.Y;
         x += 135;
      }
   }

   @Override
   public void updateScreen() {
      textFieldSearch.updateCursorCounter();
   }

   void searchClickReader(float x, float y, float xPw, float yPw, int mouseX, int mouseY) {
      if (HoverUtils.isHovered((int)x, (int)y, (int)xPw, (int)yPw, mouseX, mouseY)) {
         textFieldSearch.setFocused(!textFieldSearch.isFocused());
      }
   }

   void searchUpdate(ScaledResolution sr, int mouseX, int mouseY, boolean isFocused, String text, float x, float y, float xPw, float yPw) {
      if (textFieldSearch.isFocused() && text != "" && isFocused && Keyboard.isKeyDown(13)) {
         textFieldSearch.setFocused(false);
      }

      Keyboard.enableRepeatEvents(true);
      textFieldSearch.setMaxStringLength(170);
   }

   void forSearch(ScaledResolution sr, int mouseX, int mouseY) {
      float percentAlpha = globalAlpha.anim / 255.0F;
      searchAnim.to = textFieldSearch.isFocused() && !colose ? 1.0F : 0.0F;
      searchStringWAnim.to = (float)MathUtils.clamp(15 + Fonts.mntsb_20.getStringWidth(textFieldSearch.getText()), 21, sr.getScaledWidth() - 30)
         * searchAnim.getAnim();
      float w = 20.0F + searchStringWAnim.getAnim();
      float h = 20.0F;
      float x = 5.0F;
      float y = 5.0F;
      float x2 = 5.0F + w;
      float y2 = 25.0F;
      this.searchUpdate(sr, mouseX, mouseY, textFieldSearch.isFocused(), textFieldSearch.getText(), 5.0F + w - 20.0F, 5.0F, 5.0F + w, 25.0F);
      int cs1 = ColorUtils.getColor(0, 255, 135, (int)(170.0F * percentAlpha * percentAlpha));
      int cs2 = ColorUtils.getColor(140, 255, 0, (int)(55.0F * percentAlpha * percentAlpha));
      int cs3 = ColorUtils.getColor(0, 255, 135, (int)(70.0F * percentAlpha));
      int cs4 = ColorUtils.getColor(140, 255, 0, (int)(20.0F * percentAlpha));
      int cr1 = ColorUtils.getColor(0, 0, 0, (int)(110.0F * percentAlpha));
      int cr2 = ColorUtils.getColor(0, 0, 0, (int)(160.0F * percentAlpha));
      int ct1 = ColorUtils.getColor(255, 255, 254, (int)MathUtils.clamp(255.0F * percentAlpha, 0.0F, 255.0F));
      float round = 4.0F + 2.0F * (1.0F - percentAlpha);
      float shadownSize = 3.0F + (1.0F - percentAlpha) * 2.0F;
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         5.0F, 5.0F, x2, 25.0F, round, shadownSize, cs1, cs1, cs2, cs2, false, false, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         5.0F, 5.0F, x2, 25.0F, round, shadownSize, cs3, cs3, cs4, cs4, false, true, false
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x2 - 20.0F + 3.0F, 8.0F, x2 - 3.0F, 22.0F, round, shadownSize * 0.75F, cr1, cr1, cr2, cr2, false, true, true
      );
      GlStateManager.enableBlend();
      if (ColorUtils.getAlphaFromColor(ct1) >= 26) {
         if (scale.anim >= 1.0F) {
            Fonts.stylesicons_20.drawStringWithOutline(textFieldSearch.isFocused() ? "I" : "G", (double)(x2 - 14.5F), 13.5, ct1);
         } else {
            Fonts.stylesicons_20.drawStringWithShadow(textFieldSearch.isFocused() ? "I" : "G", (double)(x2 - 14.5F), 13.5, ct1);
         }
      }

      GL11.glEnable(3089);
      RenderUtils.scissorRected(5.0, 5.0, (double)(x2 - 20.0F), 25.0);
      textFieldSearch.drawTextBox(Fonts.mntsb_20, 0.1F + searchAnim.getAnim() * 0.9F);
      GL11.glDisable(3089);
   }

   boolean moduleHasEqualSearch(Module module) {
      return !textFieldSearch.getText().equalsIgnoreCase("")
         && textFieldSearch.isFocused()
         && module.getName().toLowerCase().contains(textFieldSearch.getText().toLowerCase());
   }

   public static void scrolls() {
      int whells = MathUtils.clamp(Mouse.getDWheel() * 1000, -1, 1);
      if (Mouse.hasWheel()) {
         dWhell -= (float)whells * 10.0F;
      }

      dWhell = MathUtils.clamp(
         dWhell,
         0.0F,
         Client.clientColosUI.getTotalElementsHeight() > Client.clientColosUI.getHeight() - 24.0F
            ? Client.clientColosUI.getTotalElementsHeight() - (Client.clientColosUI.getHeight() - 35.0F)
            : 0.0F
      );
      if (MathUtils.getDifferenceOf(scrollAnimation.to, dWhell) > 1.0) {
         ClientTune.get.playGuiScreenScrollSong();
      }

      scrollAnimation.to = dWhell;
   }

   public void scroll(int mouseX, int mouseY) {
      if (Client.clientColosUI.isHovered() && Client.clientColosUI.getHeight() >= 23.0F) {
         scrolls();
      }

      int whell = MathUtils.clamp(Mouse.getDWheel() * 1000, -1, 1);
      boolean shift = Keyboard.isKeyDown(42);
      scrolled.getAnim();
      if (Mouse.hasWheel()) {
         scrolled.to = Mouse.hasWheel() ? (float)whell * 25.0F : scrolled.to;
      }

      if (shift) {
         scrollSmoothY = 0.0F;
         scrollSmoothX = scrolled.anim / 4.0F;
      } else {
         scrollSmoothY = scrolled.anim / 4.0F;
         scrollSmoothX = 0.0F;
      }

      if (whell != 0) {
         ClientTune.get.playGuiScreenScrollSong();
      }

      this.panels.forEach(panel -> {
         if (shift) {
            panel.X = panel.X + scrolled.anim;
         } else {
            panel.Y = panel.Y + scrolled.anim;
         }
      });
   }

   public static int getColor(int step, Module.Category category) {
      float categoryFactor = ClickGui.categoryColorFactor.getAnim();
      boolean CCFade = true;
      int c = ClientColors.getColor1(step);
      if (category != null && (double)categoryFactor > 0.03) {
         int cc = category == Module.Category.COMBAT
            ? ColorUtils.getColor(255, 50, 50)
            : (
               category == Module.Category.MOVEMENT
                  ? ColorUtils.getColor(50, 115, 255)
                  : (
                     category == Module.Category.RENDER
                        ? ColorUtils.getColor(255, 170, 50)
                        : (
                           category == Module.Category.PLAYER
                              ? ColorUtils.getColor(50, 255, 160)
                              : (category == Module.Category.MISC ? ColorUtils.getColor(255, 90, 210) : 0)
                        )
                  )
            );
         if (CCFade) {
            cc = ColorUtils.fadeColor(cc, ColorUtils.toDark(cc, 0.5F), 0.333333F, (int)((float)step * 3.3333F));
         }

         return (double)ClickGui.categoryColorFactor.getAnim() < 0.97 ? ColorUtils.getOverallColorFrom(c, cc, categoryFactor) : cc;
      } else {
         return c;
      }
   }

   @Override
   protected void mouseReleased(int mouseX, int mouseY, int state) {
      resetHolds();
      if (!colose) {
         for (Panel panel : this.panels) {
            panel.mouseReleased(mouseX, mouseY, state);
         }

         Client.clientColosUI.mouseReleased(mouseX, mouseY, state);
      }
   }

   void renderImages(ScaledResolution sr, boolean render) {
      imageRender.to = render ? 1.0F : 0.0F;
      if ((double)imageRender.getAnim() > 0.05) {
         String mode = ClickGui.instance.Image.getMode();
         if (this.imageMode != mode) {
            if (this.imageMode == null) {
               Arrays.asList(((ModeSettings)ClickGui.instance.getSetting("Image", Settings.Category.String_Massive)).modes)
                  .stream()
                  .filter(str -> !str.equalsIgnoreCase(mode))
                  .forEach(another -> {
                     if (!another.equalsIgnoreCase("playstationsfw") && !another.equalsIgnoreCase("playstationnsfw")) {
                        this.mc.getTextureManager().bindTexture(new ResourceLocation("vegaline/modules/clickgui/images/" + another.toLowerCase() + ".png"));
                     } else {
                        String assetStore = "vegaline/modules/clickgui/images/playstation/" + another.toLowerCase();
                        this.mc.getTextureManager().bindTexture(new ResourceLocation(assetStore + "_base.png"));
                        this.mc.getTextureManager().bindTexture(new ResourceLocation(assetStore + "_overlay.png"));
                     }
                  });
            }

            imageScale.to = 0.1F;
            if ((double)imageScale.getAnim() > 0.0995) {
               this.imageMode = mode;
            }
         } else if ((double)imageScale.getAnim() >= 0.09) {
            imageScale.to = 0.0F;
         }

         String imageName = this.imageMode.toLowerCase();
         if (imageName != null) {
            ResourceLocation[] images = new ResourceLocation[]{new ResourceLocation("vegaline/modules/clickgui/images/" + imageName + ".png"), null};
            boolean isPS = imageName.equalsIgnoreCase("playstationsfw") || imageName.equalsIgnoreCase("playstationnsfw");
            if (isPS) {
               String assetStore = "vegaline/modules/clickgui/images/playstation/" + imageName.toLowerCase();
               images[0] = new ResourceLocation(assetStore + "_base.png");
               images[1] = new ResourceLocation(assetStore + "_overlay.png");
            }

            if (images[0] != null) {
               int size = (int)(512.0F * (1.0F + imageScale.getAnim()) + 0.001F);
               float x1 = (float)(sr.getScaledWidth() - size);
               float x2 = (float)sr.getScaledWidth();
               float y1 = (float)(sr.getScaledHeight() - size);
               float y2 = (float)sr.getScaledHeight();
               y1 -= (imageRender.getAnim() - 1.0F) * 50.0F;
               y2 -= (imageRender.getAnim() - 1.0F) * 50.0F;
               x1 -= (imageRender.getAnim() - 1.0F) * 150.0F;
               x2 -= (imageRender.getAnim() - 1.0F) * 150.0F;
               int alpha = (int)(
                  MathUtils.clamp(255.0F - imageScale.getAnim() * 10.0F * 255.0F, 0.0F, 255.0F)
                     * imageRender.getAnim()
                     * MathUtils.clamp((globalAlpha.anim - 26.0F) / 255.0F * 1.1019608F * MathUtils.clamp(scale.anim, 0.0F, 1.0F), 0.0F, 1.0F)
               );
               if (alpha > 1) {
                  if (isPS) {
                     GL11.glPushMatrix();
                     float yAnimDelay = 2200.0F;
                     float yAnimationPixExtract = 18.0F;
                     float yAnimPC = (float)(System.currentTimeMillis() % (long)((int)yAnimDelay)) / yAnimDelay;
                     yAnimPC = (yAnimPC > 0.5F ? 1.0F - yAnimPC : yAnimPC) * 2.0F;
                     yAnimPC /= 1.0F + yAnimPC / 2.0F;
                     GlStateManager.translate(0.0F, yAnimationPixExtract * yAnimPC, 0.0F);
                  }

                  int color = ColorUtils.swapDark(ColorUtils.getFixedWhiteColor(), (float)alpha / 255.0F / 2.0F + 0.5F);
                  RenderUtils.drawImageWithAlpha(images[0], x1, y1, x2 - x1, y2 - y1, color, alpha);
                  if (images[1] != null) {
                     color = ColorUtils.getFixedWhiteColor();
                     boolean animated = true;
                     if (animated) {
                        float animationDelay = 6000.0F;
                        float perTD = (float)(System.currentTimeMillis() % (long)((int)animationDelay)) / animationDelay;
                        float var30;
                        perTD = (var30 = (perTD > 0.5F ? 1.0F - perTD : perTD) * 2.0F) * (0.5F + var30 / 2.0F);
                        if ((float)alpha * perTD >= 1.0F) {
                           GlStateManager.tryBlendFuncSeparate(
                              GlStateManager.SourceFactor.SRC_ALPHA,
                              GlStateManager.DestFactor.ONE_MINUS_CONSTANT_COLOR,
                              GlStateManager.SourceFactor.ONE,
                              GlStateManager.DestFactor.ZERO
                           );
                           RenderUtils.drawImageWithAlpha(images[1], x1, y1, x2 - x1, y2 - y1, color, (int)((float)alpha * perTD));
                           GlStateManager.tryBlendFuncSeparate(
                              GlStateManager.SourceFactor.SRC_ALPHA,
                              GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                              GlStateManager.SourceFactor.ONE,
                              GlStateManager.DestFactor.ZERO
                           );
                        }
                     }
                  }

                  if (isPS) {
                     GL11.glPopMatrix();
                  }
               }
            }
         }
      }
   }

   void drawWaveRect(ScaledResolution sr) {
      waveAlphaHRender.to = ClickGui.instance.GradientAlpha.getFloat() / 255.0F;
      waveAlphaRender.to = ClickGui.instance.Gradient.getBool() ? 1.0F : 0.0F;
      waveAlphaRender.speed = 0.0333F;
      float alphaColosePercent = MathUtils.clamp(globalAlpha.anim / 255.0F * scale.anim, 0.0F, 1.0F) * waveAlphaRender.getAnim();
      int step = 200;
      int c1 = ColorUtils.getOverallColorFrom(getColor(step, Module.Category.COMBAT), getColor(step * 2, Module.Category.MOVEMENT));
      int c2 = ColorUtils.getOverallColorFrom(getColor(step * 3, Module.Category.RENDER), getColor(step * 4, Module.Category.PLAYER));
      int c3 = ColorUtils.getOverallColorFrom(getColor(step * 5, Module.Category.MISC), ColorUtils.getOverallColorFrom(c1, c2));
      float alphaButtom = waveAlphaHRender.getAnim() * alphaColosePercent;
      int cb = ColorUtils.getOverallColorFrom(ColorUtils.getOverallColorFrom(c1, c2), c3);
      int colorButtom = ColorUtils.swapAlpha(cb, (float)ColorUtils.getAlphaFromColor(cb) * alphaButtom);
      int alpha = (int)(alphaButtom * 255.0F);
      int col1 = ColorUtils.swapAlpha(getColor(step, Module.Category.COMBAT), (float)alpha);
      int col2 = ColorUtils.swapAlpha(getColor(step * 10, Module.Category.MOVEMENT), (float)alpha);
      int col3 = ColorUtils.swapAlpha(getColor(step * 20, Module.Category.RENDER), (float)alpha);
      int col4 = ColorUtils.swapAlpha(
         ColorUtils.getOverallColorFrom(getColor(step * 30, Module.Category.COMBAT), getColor(step * 40, Module.Category.COMBAT)), (float)alpha
      );
      float x = 0.0F;
      float x2 = (float)sr.getScaledWidth();
      float y = (float)sr.getScaledHeight() / (1.0F + waveAlphaHRender.getAnim());
      float y2 = (float)sr.getScaledHeight();
      if ((double)waveAlphaRender.getAnim() > 0.02) {
         RenderUtils.drawWaveGradient(x, y, x2, y2, alphaColosePercent / 8.0F, col1, col2, col3, col4, true, true);
      }
   }

   void drawBlur(ScaledResolution sr) {
      blurStrenghRender.to = ClickGui.instance.BlurBackground.getBool() ? 1.0F : 0.0F;
      if (!Config.isShaders()) {
         float percentBlur = globalAlpha.anim / 255.0F * blurStrenghRender.getAnim();
         float blurStrengh = ClickGui.instance.BlurStrengh.getFloat() * 15.0F;
         float blurStrengh2 = ClickGui.instance.BlurStrengh.getFloat() / 2.0F;
         if ((double)percentBlur > 0.05) {
            Client.blur
               .blur(
                  0.0F, 0.0F, (float)sr.getScaledWidth(), (float)sr.getScaledHeight(), (float)((int)MathUtils.clamp(blurStrengh * percentBlur, 0.5F, 100.0F))
               );
         }
      }
   }

   void renderDescriptions() {
      ScaledResolution sr = new ScaledResolution(this.mc);
      this.xn = MathUtils.lerp(this.xn, Mod.xn, (float)Minecraft.frameTime * (MathUtils.getDifferenceOf(this.xn, Mod.xn) < 20.0 ? 0.0075F : 0.0125F));
      this.yn = MathUtils.lerp(this.yn, Mod.yn, (float)Minecraft.frameTime * (MathUtils.getDifferenceOf(this.yn, Mod.yn) < 20.0 ? 0.0075F : 0.0125F));
      if (Mod.xn - 15.0F == (float)(sr.getScaledWidth() / 2) && Mod.yn - 15.0F == (float)(sr.getScaledHeight() / 2)) {
         this.xn = Mod.xn;
         this.yn = Mod.yn;
      }

      if (Mod.alphaD < 10.0F) {
         Mod.xn = 0.0F;
         Mod.yn = 0.0F;
      } else {
         CFontRenderer font = Fonts.comfortaaBold_14;
         int i = 0;
         float alpha = Mod.alphaD * (globalAlpha.anim / 255.0F) * MathUtils.clamp(scale.anim, 0.0F, 1.0F);
         String text = MathUtils.getStringPercent(Mod.descript, alpha / 255.0F * (alpha / 255.0F) * 5.0F);
         GL11.glDisable(2929);

         for (char c : text.toCharArray()) {
            i += font.getStringWidth(String.valueOf(c));
         }

         int bgC = ColorUtils.getOverallColorFrom(Integer.MIN_VALUE, getColor(i, colorCategory), 0.15F);
         bgC = ColorUtils.swapAlpha(bgC, (float)ColorUtils.getAlphaFromColor(bgC) * (alpha / 255.0F));
         int shC = getColor(i, colorCategory);
         shC = ColorUtils.swapAlpha(shC, (float)ColorUtils.getAlphaFromColor(shC) * (alpha / 5.0F / 255.0F));
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            this.xn - 2.0F,
            this.yn - 3.0F,
            this.xn + (float)i + 2.0F,
            this.yn + (float)font.getHeight() + 2.0F,
            2.0F,
            0.5F,
            bgC,
            bgC,
            bgC,
            bgC,
            false,
            true,
            true
         );
         RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBool(
            this.xn - 2.0F, this.yn - 3.0F, this.xn + (float)i + 2.0F, this.yn + (float)font.getHeight() + 2.0F, 2.0F, shC, shC, shC, shC, true
         );
         int n = 0;

         for (char c : text.toCharArray()) {
            if (alpha >= 26.0F) {
               GL11.glTranslated((double)(this.xn + (float)n), (double)this.yn, 0.0);
               font.drawStringWithShadow(String.valueOf(c), 0.0, 0.0, ColorUtils.swapAlpha(getColor(10000 - n * 3, colorCategory), alpha));
               GL11.glTranslated((double)(-(this.xn + (float)n)), (double)(-this.yn), 0.0);
            }

            n += font.getStringWidth(String.valueOf(c));
         }

         Mod.alphaD = Mod.alphaD > 0.0F ? Mod.alphaD - 5.0F : 0.0F;
         GL11.glEnable(2929);
      }
   }

   void cfgGuiOpenner(int mouseX, int mouseY) {
      ScaledResolution sr = new ScaledResolution(this.mc);
      cfgScale.to = this.openCfgKey ? 1.0F : 0.0F;
      int size = 32;
      float x = (float)(sr.getScaledWidth() - size - 10);
      float y = 10.0F;
      keyCfgAnimScale.to = HoverUtils.isHovered((int)x, (int)y, (int)x + size, (int)y + size, mouseX, mouseY) ? 1.2F : 1.0F;
      keyCfgAnimRotate.to = System.currentTimeMillis() % 1000L < 150L && HoverUtils.isHovered((int)x, (int)y, (int)x + size, (int)y + size, mouseX, mouseY)
         ? -15.0F
         : 0.0F;
      if (Mouse.isButtonDown(0)) {
         if (HoverUtils.isHovered((int)x, (int)y, (int)x + size, (int)y + size, mouseX, mouseY) && this.isClickedCfgKey) {
            this.openCfgKey = !this.openCfgKey;
         }

         this.isClickedCfgKey = false;
      } else {
         this.isClickedCfgKey = true;
      }

      GL11.glPushMatrix();
      RenderUtils.customScaledObject2D(x, y, (float)size, (float)size, keyCfgAnimScale.getAnim() * scale.anim);
      RenderUtils.customRotatedObject2D(x, y, (float)size, (float)size, (double)keyCfgAnimRotate.getAnim());
      RenderUtils.drawImageWithAlpha(
         new ResourceLocation("vegaline/ui/clickgui/config/buttons/images/cfgicon.png"),
         x,
         y,
         (float)size,
         (float)size,
         ColorUtils.fadeColor(ColorUtils.getColor(255, 0, 55), ColorUtils.getColor(255, 255, 255), 0.2F),
         (int)((100.0F + (keyCfgAnimScale.getAnim() - 1.0F) * 5.0F * 55.0F) * scale.anim)
      );
      GL11.glPopMatrix();
      if ((double)cfgScale.getAnim() > 0.95 && this.openCfgKey) {
         GuiConfig.cfgScale.setAnim(1.0F);
         this.mc.displayGuiScreen(new GuiConfig());
         this.openCfgKey = false;
      }

      if ((double)cfgScale.getAnim() > 0.05) {
         RenderUtils.drawRect(0.0, 0.0, 10000.0, 10000.0, ColorUtils.getColor(0, 0, 0, (int)(255.0F * cfgScale.getAnim())));
      }
   }

   @Override
   public void onGuiClosed() {
      if (!ClickGui.instance.SaveMusic.getBool()) {
         Client.clickGuiMusic.setPlaying(false);
      }

      this.panels.forEach(panel -> panel.onCloseGui());
      this.setPlayBindHold(false);
      Keyboard.enableRepeatEvents(false);
      Client.clientColosUI.onGuiClosed();
      resetHolds();
   }

   void drawCrosshair(boolean render, ScaledResolution scaled) {
      if (render) {
         GlStateManager.disableDepth();
         double x = (double)Mouse.getX() / 2.0;
         double y = (double)scaled.getScaledHeight() - (double)Mouse.getY() / 2.0;
         float dx = (float)Mouse.getDX();
         float dy = (float)Mouse.getDY();
         boolean ofScreen = !Mouse.isInsideWindow() || x < 0.0 || x > (double)scaled.getScaledWidth() || y < 0.0 || y > (double)scaled.getScaledHeight();
         if (ofScreen) {
            if (Mouse.isGrabbed()) {
               Mouse.setCursorPosition(Mouse.getX() + (int)dx, Mouse.getY() + (int)dy);
            }

            Mouse.setGrabbed(false);
            crossClick.setAnim(-1.0F);
         } else {
            if (crossClick.getAnim() >= -0.001F && crossClick.getAnim() < 0.0F) {
               Mouse.setGrabbed(true);
            }

            if (Mouse.isGrabbed()) {
               x--;
               y--;
               double texW = 16.0;
               double texH = 16.0;
               double x2 = x + texW;
               double y2 = y + texH;
               crossRotate.speed = 0.08F;
               if (MathUtils.getDifferenceOf(crossClick.getAnim(), crossClick.to) < 0.05F) {
                  crossClick.to = 0.0F;
                  crossClick.speed = 0.01F;
               } else {
                  crossClick.speed = 0.15F;
               }

               crossRotate.to = MathUtils.clamp((float)(Mouse.getDX() + Mouse.getDY()) * 6.0F, -800.0F, 800.0F);
               float angle = -crossRotate.getAnim() / 3.0F;
               if (MathUtils.getDifferenceOf(angle, 0.0F) < 1.0) {
                  angle = 0.0F;
               }

               float alpha = globalAlpha.anim * MathUtils.clamp(scale.anim, 0.0F, 1.0F);
               int colorCross = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), MathUtils.clamp(alpha + Math.abs(angle) / 90.0F, 0.0F, 255.0F));
               int colorOverlay = ColorUtils.swapAlpha(
                  ColorUtils.getFixedWhiteColor(), MathUtils.clamp(alpha * (crossClick.getAnim() + Math.abs(angle) / 45.0F), 0.0F, 255.0F)
               );
               GL11.glEnable(3553);
               GL11.glEnable(3042);
               GL11.glDisable(2929);
               GL11.glBlendFunc(770, 32772);
               Tessellator tessellator = Tessellator.getInstance();
               BufferBuilder buffer = tessellator.getBuffer();
               this.mc.getTextureManager().bindTexture(MOUSE_TEXTURE_BASE);
               buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
               buffer.pos(x, y).tex(0.0, 0.0).color(colorCross).endVertex();
               buffer.pos(x, y2).tex(0.0, 1.0).color(colorCross).endVertex();
               buffer.pos(x2, y2).tex(1.0, 1.0).color(colorCross).endVertex();
               buffer.pos(x2, y).tex(1.0, 0.0).color(colorCross).endVertex();
               GL11.glPushMatrix();
               RenderUtils.customRotatedObject2D((float)x, (float)y, (float)texW / 1.5F, (float)texW / 1.5F, (double)(angle / 4.0F));
               RenderUtils.customScaledObject2D(
                  (float)x, (float)y, (float)texW / 1.5F, (float)texW / 1.5F, 1.0F - crossClick.getAnim() / 1.75F + Math.abs(angle) / 160.0F / 4.0F
               );
               tessellator.draw();
               GL11.glPopMatrix();
               this.mc.getTextureManager().bindTexture(MOUSE_TEXTURE_OVERLAY);
               buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
               buffer.pos(x, y).tex(0.0, 0.0).color(colorOverlay).endVertex();
               buffer.pos(x, y2).tex(0.0, 1.0).color(colorOverlay).endVertex();
               buffer.pos(x2, y2).tex(1.0, 1.0).color(colorOverlay).endVertex();
               buffer.pos(x2, y).tex(1.0, 0.0).color(colorOverlay).endVertex();
               GL11.glPushMatrix();
               RenderUtils.customRotatedObject2D((float)x, (float)y, (float)texW / 1.5F, (float)texW / 1.5F, (double)(angle * 1.25F / 4.0F));
               RenderUtils.customScaledObject2D((float)x, (float)y, (float)texW / 1.35F, (float)texH / 1.35F, 1.0F + Math.abs(angle) / 80.0F / 4.0F);
               tessellator.draw(2);
               GL11.glPopMatrix();
               GL11.glBlendFunc(770, 771);
               GlStateManager.enableDepth();
               if (this.checkCustomCross != render) {
                  this.checkCustomCross = render;
                  Mouse.setCursorPosition((int)x, (int)y);
               }
            }
         }
      }
   }

   void drawDark(ScaledResolution scaled) {
      darkness.to = ClickGui.instance.Darkness.getBool() && !colose ? ClickGui.instance.DarkOpacity.getFloat() : 0.0F;
      float alphaColosePercent = MathUtils.clamp(globalAlpha.anim / 255.0F * scale.anim, 0.0F, 1.0F);
      int upC = ColorUtils.swapAlpha(0, darkness.getAnim() * alphaColosePercent);
      int downC = ColorUtils.swapAlpha(0, darkness.anim * alphaColosePercent * alphaColosePercent);
      RenderUtils.drawFullGradientRectPro(0.0F, 0.0F, (float)scaled.getScaledWidth(), (float)scaled.getScaledHeight(), downC, downC, upC, upC, false);
   }

   private void drawParts(float alphaPC) {
      if (this.parts.size() < 20) {
         this.parts.add(new ClickGuiScreen.Parts());
      }

      if (this.parts.size() != 0 || this.partsEff.size() != 0) {
         this.startDrawsParts();
      }

      for (ClickGuiScreen.Parts part : this.parts) {
         if (part != null) {
            part.updatePhisics();
            if (part.canDrawPart()) {
               part.drawPart(alphaPC);
            }

            part.removeAuto(part);
         }
      }

      this.partsEff.forEach(partx -> {
         partx.update();
         partx.draw(alphaPC);
      });
      if (this.parts.size() != 0 || this.partsEff.size() != 0) {
         this.stopDrawsParts();
      }
   }

   void drawScanLines(ScaledResolution sr, float scanLinesDistance, float scanLinesWidth, float alphaPC, int intervalTime, int scanTower) {
      scanLines.to = ClickGui.instance.ScanLinesOverlay.getBool() ? 1.0F : 0.0F;
      if (!((double)(alphaPC = alphaPC * scanLines.getAnim()) < 0.03)) {
         float timeExtendStopPC = 1.0F;
         float timePC = (float)(System.currentTimeMillis() % (long)((int)((float)intervalTime + timeExtendStopPC * (float)intervalTime))) / (float)intervalTime;
         timePC = timePC > 1.0F ? 1.0F : timePC;
         int color = ColorUtils.getColor(255, 255, 255);
         float w = (float)sr.getScaledWidth();
         float h = (float)sr.getScaledHeight() + (float)scanTower * 4.0F;
         int linesCount = (int)h - 1;
         float x = 0.0F;
         float y = (float)(-scanTower) * 2.0F;
         float x2 = x + w;
         float y2 = y + h;
         float alphaXExt = 40.0F;
         y += scanLinesDistance / 2.0F;
         GL11.glEnable(3042);
         GL11.glDisable(3553);
         GL11.glDisable(3008);
         GL11.glShadeModel(7425);
         GL11.glBlendFunc(770, 32772);
         float prevAlphaPC = alphaPC;
         alphaPC *= MathUtils.clamp((float)MathUtils.easeInOutQuadWave((double)timePC), 0.0F, 1.0F);
         alphaPC *= 0.3F;
         alphaPC = alphaPC < 0.25F ? 0.25F : alphaPC;

         for (int index = 0; index < linesCount; index++) {
            float diffY = (float)MathUtils.getDifferenceOf(timePC * (float)linesCount, y);
            float aPC = 1.0F
               - MathUtils.clamp(diffY < scanLinesWidth ? 0.0F : diffY / (float)scanTower * (y < timePC * (float)linesCount ? 0.25F : 1.0F), 0.0F, 1.0F);
            aPC = (aPC < 0.2F ? 0.2F : aPC) * prevAlphaPC;
            if (aPC != 0.0F) {
               float alphaXExtInc = alphaXExt + alphaXExt / 3.0F * aPC * aPC;
               float sellPC = MathUtils.clamp(alphaPC * ((diffY < scanLinesWidth * 2.0F ? 1.06F : 1.0F) / 2.0F) * 2.0F, 0.0F, 1.0F);
               GL11.glLineWidth(scanLinesWidth);
               GL11.glBegin(3);
               RenderUtils.setupColor(color, 0.0F);
               GL11.glVertex2d((double)x, (double)y);
               RenderUtils.setupColor(color, 255.0F * sellPC * aPC);
               GL11.glVertex2d((double)(x + alphaXExtInc), (double)y);
               GL11.glVertex2d((double)(x2 - alphaXExtInc), (double)y);
               RenderUtils.setupColor(color, 0.0F);
               GL11.glVertex2d((double)x2, (double)y);
               GL11.glEnd();
               GL11.glLineWidth(scanLinesDistance + scanLinesWidth * 2.0F);
               GL11.glBegin(3);
               RenderUtils.setupColor(color, 0.0F);
               GL11.glVertex2d((double)x, (double)y);
               RenderUtils.setupColor(color, 95.0F * sellPC * aPC);
               GL11.glVertex2d((double)(x + alphaXExtInc), (double)y);
               GL11.glVertex2d((double)(x2 - alphaXExtInc), (double)y);
               RenderUtils.setupColor(color, 0.0F);
               GL11.glVertex2d((double)x2, (double)y);
               GL11.glEnd();
            }

            y += scanLinesDistance;
         }

         GL11.glShadeModel(7424);
         GL11.glLineWidth(1.0F);
         GL11.glEnable(3008);
         GlStateManager.resetColor();
         GL11.glEnable(3553);
         GL11.glBlendFunc(770, 771);
      }
   }

   void drawBounds(ScaledResolution sr, float alphaPC, int xAnimationDelay) {
      alphaPC = alphaPC > 1.0F ? 1.0F : (alphaPC < 0.0F ? 0.0F : alphaPC);
      boundsToggleAnim.to = ClickGui.instance.ScreenBounds.getBool() ? 1.0F : 0.0F;
      alphaPC *= boundsToggleAnim.getAnim();
      if (!((double)alphaPC < 0.05)) {
         float timePC = (float)(System.currentTimeMillis() % (long)xAnimationDelay) / (float)xAnimationDelay;
         int texsScaleX = 15;
         int texsScaleY = 60;
         float xUpBound = (float)(-texsScaleX) * 2.0F + (float)texsScaleX * timePC;
         float yUpBound = (float)(-texsScaleY) * (1.0F - alphaPC * 0.5F);
         float xDownBound = (float)(-texsScaleX) - (float)texsScaleX * timePC;
         float yDownBound = (float)sr.getScaledHeight() - (float)texsScaleY * alphaPC * 0.5F;
         int boundsUpCount = (int)((float)sr.getScaledWidth() / (float)texsScaleX + 2.0F);
         int boundsDownCount = boundsUpCount;
         GL11.glEnable(3553);
         GL11.glEnable(3042);
         GL11.glShadeModel(7425);
         GL11.glDisable(3008);
         GL11.glBlendFunc(770, 32772);
         int color = ColorUtils.getColor(255, 255, 255, 225.0F * alphaPC);
         int color2 = ColorUtils.getColor(255, 255, 255, 5.0F * alphaPC);
         this.mc.getTextureManager().bindTexture(BOUND_UP);

         while (boundsUpCount > 0) {
            float texPosX = xUpBound + (float)boundsUpCount * (float)texsScaleX;
            builder.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
            builder.pos((double)(texPosX + (float)texsScaleX), (double)(yUpBound + (float)texsScaleY)).tex(1.0, 1.0).color(color).endVertex();
            builder.pos((double)(texPosX + (float)texsScaleX), (double)yUpBound).tex(1.0, 0.0).color(color2).endVertex();
            builder.pos((double)texPosX, (double)yUpBound).tex(0.0, 0.0).color(color2).endVertex();
            builder.pos((double)texPosX, (double)(yUpBound + (float)texsScaleY)).tex(0.0, 1.0).color(color).endVertex();
            tessellator.draw();
            boundsUpCount--;
         }

         this.mc.getTextureManager().bindTexture(BOUND_DOWN);

         while (boundsDownCount > 0) {
            float texPosX = xDownBound + (float)boundsDownCount * (float)texsScaleX;
            builder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            builder.pos((double)(texPosX + (float)texsScaleX), (double)(yDownBound + (float)texsScaleY)).tex(1.0, 1.0).color(color2).endVertex();
            builder.pos((double)(texPosX + (float)texsScaleX), (double)yDownBound).tex(1.0, 0.0).color(color).endVertex();
            builder.pos((double)texPosX, (double)yDownBound).tex(0.0, 0.0).color(color).endVertex();
            builder.pos((double)texPosX, (double)(yDownBound + (float)texsScaleY)).tex(0.0, 1.0).color(color2).endVertex();
            tessellator.draw();
            boundsDownCount--;
         }

         GlStateManager.resetColor();
         GL11.glShadeModel(7424);
         GL11.glBlendFunc(770, 771);
         GL11.glEnable(3008);
      }
   }

   private void drawSaveMusicButton(int mouseX, int mouseY, ScaledResolution sr, float alphaPC) {
      keySaveSound.to = ClickGui.instance.MusicInGui.getBool() ? 0.8F + (this.isHoverToSaveMusicIco(mouseX, mouseY) ? 0.2F : 0.0F) : 0.0F;
      if ((double)(keySaveSound.getAnim() * alphaPC) >= 0.03) {
         keySaveSoundToggle.to = ClickGui.instance.SaveMusic.getBool() ? 1.0F : 0.0F;
         int color = ColorUtils.getOverallColorFrom(
            ColorUtils.getColor(110, 110, 110, 190.0F * alphaPC * keySaveSound.anim),
            ColorUtils.getColor(255, 255, 255, 255.0F * alphaPC * keySaveSound.anim),
            keySaveSoundToggle.getAnim()
         );
         float rot = 360.0F
            * ((double)keySaveSoundToggle.anim > 0.5 ? 1.0F - keySaveSoundToggle.anim : keySaveSoundToggle.anim)
            * 2.0F
            * (keySaveSoundToggle.to == 0.0F ? -1.0F : 1.0F);
         float scalePlus = 0.4F * ((double)keySaveSoundToggle.anim > 0.5 ? 1.0F - keySaveSoundToggle.anim : keySaveSoundToggle.anim) * 2.0F;
         this.mc.getTextureManager().bindTexture(MUSIC_SAVE_BUTTON);
         float x = 10.0F;
         float y = (float)(sr.getScaledHeight() - 38) - boundsToggleAnim.anim * 27.0F;
         float size = 28.0F;
         builder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         builder.pos((double)(x + size), (double)(y + size)).tex(1.0, 1.0).color(color).endVertex();
         builder.pos((double)(x + size), (double)y).tex(1.0, 0.0).color(color).endVertex();
         builder.pos((double)x, (double)y).tex(0.0, 0.0).color(color).endVertex();
         builder.pos((double)x, (double)(y + size)).tex(0.0, 1.0).color(color).endVertex();
         GL11.glEnable(3553);
         GL11.glEnable(3042);
         GL11.glShadeModel(7425);
         GL11.glDisable(3008);
         GL11.glBlendFunc(770, 32772);
         GL11.glPushMatrix();
         RenderUtils.customRotatedObject2D(x, y, size, size, (double)(-rot));
         RenderUtils.customScaledObject2D(x, y, size, size, 1.0F + scalePlus);
         tessellator.draw(2);
         GL11.glPopMatrix();
         GlStateManager.resetColor();
         GL11.glShadeModel(7424);
         GL11.glBlendFunc(770, 771);
         GL11.glEnable(3008);
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.updatePlayingBinding();
      globalAlpha.speed = 0.05F;
      scale.speed = colose && scale.anim <= 1.0F ? 0.03F : (scale.to == 1.0F && scale.anim > 1.0F ? 0.15F : 0.05F);
      if (!colose) {
         this.scroll(mouseX, mouseY);
      }

      scale.getAnim();
      globalAlpha.getAnim();
      if ((double)scale.anim > 1.04) {
         scale.to = colose ? 0.25F : 1.0F;
         if (!colose && !ClickGui.instance.MusicInGui.getBool()) {
            ClientTune.get.playGuiScreenFoneticSong();
         }

         globalAlpha.to = colose ? 0.0F : 255.0F;
      }

      if (!colose && scale.to == 1.0F && (double)scale.anim < 1.001) {
         scale.setAnim(1.0F);
      }

      if ((double)scale.anim < 0.35) {
         Minecraft.player.closeScreen();
         colose = false;
      }

      ScaledResolution sr = new ScaledResolution(this.mc);
      this.drawDark(sr);
      epilepsy.to = ClickGui.instance.Epilepsy.getBool() ? 1.0F : 0.0F;
      if ((double)epilepsy.getAnim() > 0.03) {
         GL11.glPushMatrix();
         GL11.glDepthMask(false);
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
         );
         float r = 10.0F;
         float size = 273.0F;
         float aPC = MathUtils.clamp(scale.anim * (globalAlpha.anim / 255.0F) * epilepsy.getAnim(), 0.0F, 1.0F);
         float extXY = 0.0F;

         for (int ii = 0; (float)ii < r; ii++) {
            for (int i = 0; (float)i < r; i++) {
               float pc = (float)i / r;
               float pc2 = (float)ii / r;
               ScaledResolution s = new ScaledResolution(this.mc);
               float x = extXY + ((float)s.getScaledWidth() - size / 1.5F - extXY * 2.0F) * pc;
               float y = extXY + ((float)s.getScaledHeight() - size / 1.5F - extXY * 2.0F) * pc2;
               float dx = x - ((float)s.getScaledWidth() - size) / 2.0F;
               float dy = y - ((float)s.getScaledHeight() - size) / 2.0F;
               int cInd = i * 100 - ii * 60 * (ii % 2 == 0 ? 1 : -1);
               float xpc = x / ((float)sr.getScaledWidth() - size);
               Module.Category cat = (double)xpc <= 0.2
                  ? Module.Category.COMBAT
                  : (
                     (double)xpc <= 0.4
                        ? Module.Category.MOVEMENT
                        : ((double)xpc <= 0.6 ? Module.Category.RENDER : ((double)xpc <= 0.8 ? Module.Category.PLAYER : Module.Category.MISC))
                  );
               int e = getColor(cInd, cat);
               int c = ColorUtils.fadeColorIndexed(e, 0, 0.3F, (int)((float)cInd * 0.3F));
               c = ColorUtils.swapAlpha(c, MathUtils.clamp((float)ColorUtils.getAlphaFromColor(c) * aPC * 1.2F, 0.0F, 255.0F));
               this.drawImage(this.BLOOM_TEX, x, y, size, size, c);
            }
         }

         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         GL11.glDepthMask(true);
         GL11.glPopMatrix();
      }

      this.drawBlur(sr);
      this.drawScanLines(sr, 4.0F, 1.0F, MathUtils.clamp(globalAlpha.anim / 255.0F * scale.anim, 0.0F, 1.0F), 3500, 23);
      this.drawBounds(sr, MathUtils.clamp(globalAlpha.anim / 255.0F * scale.anim * scale.anim, 0.0F, 1.0F), 300);
      if (ClickGui.instance.Particles.getBool()) {
         GL11.glPushMatrix();
         this.drawParts(MathUtils.clamp(scale.anim * scale.anim, 0.0F, 1.0F));
         GL11.glPopMatrix();
      } else if (this.parts.size() != 0) {
         this.startDrawsParts();

         for (ClickGuiScreen.Parts part : this.parts) {
            if (part != null) {
               part.alphaPC.to = 0.0F;
               part.removeAuto(part);
               if (part.canDrawPart()) {
                  part.drawPart(MathUtils.clamp(scale.anim * scale.anim, 0.0F, 1.0F) * part.alphaPC.getAnim());
               }
            }
         }

         this.stopDrawsParts();
      }

      this.drawWaveRect(sr);
      this.forSearch(sr, mouseX, mouseY);
      this.renderImages(sr, ClickGui.instance.Images.getBool());
      GlStateManager.pushMatrix();
      RenderUtils.customScaledObject2D(
         0.0F,
         0.0F,
         (float)sr.getScaledWidth(),
         (float)sr.getScaledHeight(),
         1.0F + MathUtils.clamp((1.0F - scale.anim) * (scale.anim > 1.0F ? 1.0F : 24.0F * (colose ? 1.0F : 0.2F)), -0.1F, 25.0F * (colose ? 1.0F : 0.2F))
      );

      for (Panel panel : this.panels) {
         panel.drawScreen(mouseX, mouseY, partialTicks);
      }

      GlStateManager.popMatrix();
      this.renderDescriptions();
      this.cfgGuiOpenner(mouseX, mouseY);
      this.particleRender();
      particleRemoveAuto();
      RenderUtils.fixShadows();
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.drawSaveMusicButton(mouseX, mouseY, sr, MathUtils.clamp(scale.anim * scale.anim * globalAlpha.anim / 255.0F, 0.0F, 1.0F));
      Client.clientColosUI.drawScreen(mouseX, mouseY, partialTicks);
      this.drawCrosshair(ClickGui.instance.CustomCursor.getBool(), sr);
      this.updateHolds(mouseX, mouseY, 600.0F);
   }

   public static void resetHolds() {
      clickedMouse0 = false;
      clickedMouse1 = false;
      clickedMouse2 = false;
   }

   private void updateHolds(int mouseX, int mouseY, float timeCheckHold) {
      if (colose) {
         resetHolds();
      }

      if (clickedMouse0) {
         checkMouse0Hold = true;
      } else {
         this.timeMouse0Hold.reset();
      }

      if (clickedMouse1) {
         checkMouse1Hold = true;
      } else {
         this.timeMouse1Hold.reset();
      }

      if (clickedMouse2) {
         checkMouse2Hold = true;
      } else {
         this.timeMouse2Hold.reset();
      }

      if (this.timeMouse0Hold.hasReached((double)timeCheckHold) && checkMouse0Hold) {
         this.panels.forEach(panel -> {
            int i = 26;

            for (Mod mod : panel.mods) {
               mod.mouseClicked((int)panel.X, (int)panel.Y + i, mouseX, mouseY, 0);
               i = (int)((float)i + mod.openAnim.anim + 1.0F);
            }
         });
         Client.clientColosUI.mouseClicked(mouseX, mouseY, 0);
         checkMouse0Hold = false;
      }

      if (this.timeMouse1Hold.hasReached((double)timeCheckHold) && checkMouse1Hold) {
         this.panels.forEach(panel -> {
            int i = 26;

            for (Mod mod : panel.mods) {
               mod.mouseClicked((int)panel.X, (int)panel.Y + i, mouseX, mouseY, 1);
               i = (int)((float)i + mod.openAnim.getAnim() + 1.0F);
            }
         });
         Client.clientColosUI.mouseClicked(mouseX, mouseY, 1);
         checkMouse1Hold = false;
      }

      if (this.timeMouse2Hold.hasReached((double)timeCheckHold) && checkMouse2Hold) {
         this.panels.forEach(panel -> {
            int i = 26;

            for (Mod mod : panel.mods) {
               mod.mouseClicked((int)panel.X, (int)panel.Y + i, mouseX, mouseY, 2);
               i = (int)((float)i + mod.openAnim.getAnim() + 1.0F);
            }
         });
         Client.clientColosUI.mouseClicked(mouseX, mouseY, 2);
         checkMouse2Hold = false;
      }
   }

   private boolean isHoverToSaveMusicIco(int mouseX, int mouseY) {
      if (!ClickGui.instance.MusicInGui.getBool()) {
         return false;
      } else {
         ScaledResolution sr = new ScaledResolution(this.mc);
         float dx = 24.0F - (float)mouseX;
         float dy = (float)sr.getScaledHeight() - 10.0F - 14.0F - boundsToggleAnim.anim * 27.0F - (float)mouseY;
         return Math.sqrt((double)(dx * dx + dy * dy)) <= 11.0;
      }
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      if (mouseButton == 0) {
         clickedMouse0 = true;
      }

      if (mouseButton == 1) {
         clickedMouse1 = true;
      }

      if (mouseButton == 2) {
         clickedMouse2 = true;
      }

      if (!colose && !Client.clientColosUI.isHovered()) {
         if (this.isHoverToSaveMusicIco(mouseX, mouseY) && mouseButton == 0) {
            ClickGui.instance.SaveMusic.toggleBool();
            ClientTune.get.playGuiScreenMusicSaveToggleSong(ClickGui.instance.SaveMusic.getBool());
         }

         if (ClickGui.instance.CustomCursor.getBool()) {
            crossClick.to = mouseButton == 0 ? 0.55F : 0.35F;
         }

         if (mouseButton == 0) {
            float w = 20.0F + searchStringWAnim.getAnim();
            float h = 20.0F;
            float x = 5.0F;
            float y = 5.0F;
            float x2 = 5.0F + w;
            float y2 = 25.0F;
            this.searchClickReader(x2 - 20.0F, 5.0F, x2, 25.0F, mouseX, mouseY);
         }

         List<Panel> reversedPanels = new ArrayList<>(this.panels);
         Collections.reverse(reversedPanels);

         for (Panel panel : reversedPanels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
            if (this.panels.stream().anyMatch(panel2 -> panel2.callClicked)) {
               this.panels.stream().forEach(panel3 -> panel3.callClicked = false);
               break;
            }
         }

         if (mouseButton == 0) {
            Mouse.setGrabbed(ClickGui.instance.CustomCursor.getBool());
         }
      }

      Client.clientColosUI.mouseClicked(mouseX, mouseY, mouseButton);
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (!colose) {
         textFieldSearch.textboxKeyTyped(typedChar, keyCode);
         if ((Keyboard.isKeyDown(28) || Keyboard.isKeyDown(1)) && textFieldSearch.isFocused()) {
            textFieldSearch.setText("");
            textFieldSearch.setFocused(false);
         } else if (Keyboard.isKeyDown(29) && Keyboard.isKeyDown(33)) {
            textFieldSearch.setFocused(!textFieldSearch.isFocused());
         }

         if (!textFieldSearch.isFocused()) {
            textFieldSearch.setText("");
         }

         if ((keyCode == 1 || keyCode == ClickGui.instance.getBind() && keyCode != 29) && !colose) {
            this.mc.setIngameFocus();
            Mouse.setGrabbed(true);
            colose = true;
            Client.configManager.saveConfig("Default");
            if (!ClickGui.instance.SaveMusic.getBool()) {
               Client.clickGuiMusic.setPlaying(false);
            }

            Keyboard.enableRepeatEvents(false);
            scale.to = 1.08F;
            ClientTune.get.playGuiScreenOpenOrCloseSong(false);
         }

         for (Panel panel : this.panels) {
            panel.keyPressed(keyCode);
         }

         Client.clientColosUI.keyTyped(typedChar, keyCode);
      }
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   @Override
   public void initGui() {
      super.initGui();
      resetHolds();
      colose = false;
      globalAlpha.setAnim(26.0F);
      globalAlpha.to = 255.0F;
      scale.setAnim(0.8F);
      scale.to = 1.08F;
      scale.speed = 0.075F;
      if (ClickGui.instance.CustomCursor.getBool()) {
         Mouse.setGrabbed(true);
      }

      Client.clientColosUI.initGui();
      ClientTune.get.playGuiScreenOpenOrCloseSong(true);
   }

   public void particleRender() {
      for (int i = 0; i < particles.size(); i++) {
         if (particles.get(i) != null) {
            RenderUtils.drawClientCircle(
               particles.get(i).x,
               (double)particles.get(i).y,
               particles.get(i).radius.getAnim(),
               359.0F,
               (1.0F - particles.get(i).radius.getAnim() / particles.get(i).radius.to) * 5.0F + 2.0F,
               particles.get(i).alpha.getAnim() / 180.0F * (globalAlpha.anim / 255.0F)
            );
            RenderUtils.fixShadows();
         }
      }
   }

   static void particleRemoveAuto() {
      for (int i = 0; i < particles.size(); i++) {
         if (particles.get(i).radius.getAnim() >= particles.get(i).radius.to - 0.25F || particles.get(i).alpha.getAnim() <= 26.0F) {
            particles.remove(i);
         }
      }
   }

   static void spawnParticleRandPos(float x, float y, float randomizeValX, float randomizeValY, long spawnDelay) {
      x = (float)((double)x + MathUtils.getRandomInRange((double)(-randomizeValX), (double)randomizeValX));
      y = (float)((double)y + MathUtils.getRandomInRange((double)(-randomizeValY), (double)randomizeValY));
      if (System.currentTimeMillis() % spawnDelay == 0L) {
         particles.add(new ClickGuiScreen.searchParticle(x, y));
      }
   }

   private void startDrawsParts() {
      GlStateManager.enableBlend();
      GL11.glDisable(3008);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GL11.glDepthMask(false);
   }

   private void stopDrawsParts() {
      GL11.glDepthMask(true);
      GlStateManager.resetColor();
      GlStateManager.enableBlend();
      GL11.glEnable(3008);
      GlStateManager.tryBlendFuncSeparate(
         GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
      );
      GlStateManager.enableAlpha();
   }

   private void drawImage(ResourceLocation imageIII, float x, float y, float w, float h, int color) {
      this.mc.getTextureManager().bindTexture(imageIII);
      RenderUtils.glColor(color);
      GlStateManager.translate(x, y, 1.0F);
      Gui.drawModalRectWithCustomSizedTexture(0.0F, 0.0F, 0.0F, 0.0F, w, h, w, h);
      GlStateManager.translate(-x, -y, -1.0F);
   }

   private class FallPartsEffect {
      AnimationUtils alphaPC = new AnimationUtils(0.5F, 1.0F, 0.1F);
      ClickGuiScreen.Parts part;

      public FallPartsEffect(ClickGuiScreen.Parts part) {
         this.part = part;
      }

      public void update() {
         if ((double)this.alphaPC.getAnim() > 0.9995 && this.alphaPC.to != 0.0F) {
            this.alphaPC.to = 0.0F;
            this.alphaPC.speed = 0.05F;
         }

         ClickGuiScreen.this.partsEff.removeIf(part -> part != null && (double)part.alphaPC.getAnim() < 0.1 && part.alphaPC.to == 0.0F);
      }

      public void draw(float alphaPC) {
         float alpha = MathUtils.clamp(alphaPC * this.alphaPC.getAnim(), 0.0F, 1.0F);
         GL11.glPushMatrix();
         RenderUtils.customScaledObject2D(this.part.pos.x, this.part.pos.y, 16.0F, 16.0F, alpha);
         ClickGuiScreen.this.drawImage(
            ClickGuiScreen.this.BLOOM_TEX,
            this.part.pos.x - 8.0F,
            this.part.pos.y - 8.0F,
            32.0F,
            32.0F,
            ColorUtils.swapAlpha(ClickGuiScreen.getColor(this.part.randomIndex, this.part.getColorCategory()), 255.0F * alpha)
         );
         ClickGuiScreen.this.drawImage(
            ClickGuiScreen.this.BLOOM_TEX,
            this.part.pos.x,
            this.part.pos.y,
            16.0F,
            16.0F,
            ColorUtils.swapAlpha(ClickGuiScreen.getColor(this.part.randomIndex, this.part.getColorCategory()), 255.0F * alpha)
         );
         ClickGuiScreen.this.drawImage(
            ClickGuiScreen.this.BLOOM_TEX,
            this.part.pos.x + 4.0F,
            this.part.pos.y + 4.0F,
            8.0F,
            8.0F,
            ColorUtils.swapAlpha(ClickGuiScreen.getColor(this.part.randomIndex, this.part.getColorCategory()), 255.0F * alpha)
         );
         GL11.glPopMatrix();
      }
   }

   private class PartTrail {
      float indexPart = 0.0F;
      float maxSizeIndex;
      ClickGuiScreen.Parts part;
      float x;
      float y;

      public PartTrail(ClickGuiScreen.Parts part, int maxSizeIndex, float vertexX, float vertexY) {
         this.part = part;
         this.maxSizeIndex = (float)maxSizeIndex;
         this.x = vertexX;
         this.y = vertexY;
      }

      public void updateIndex() {
         this.indexPart++;
      }

      public void drawVertex(float alphaPC) {
         float pc = this.getIndexOfMax();
         pc *= MathUtils.clamp((this.getIndexPart() - 5.0F) / 10.0F, 0.0F, 1.0F);
         pc = pc > 0.5F ? 1.0F - pc : pc;
         pc *= 2.0F;
         alphaPC *= pc;
         RenderUtils.glColor(ColorUtils.swapAlpha(ClickGuiScreen.getColor(0, this.part.getColorCategory()), 255.0F * alphaPC));
         GL11.glVertex2f(this.x, this.y);
      }

      public float getIndexPart() {
         return this.indexPart;
      }

      public float getMaxSizeIndexPart() {
         return this.maxSizeIndex;
      }

      public float getIndexOfMax() {
         return MathUtils.clamp(this.getIndexPart() / this.getMaxSizeIndexPart(), 0.0F, 1.0F);
      }
   }

   private class Parts {
      List<ClickGuiScreen.PartTrail> partTrails = new ArrayList<>();
      Vec2f pos = this.getPos();
      AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.02F);
      ResourceLocation part = this.getNewPart();
      double motionX = this.getMotionsStart()[0];
      double motionY = this.getMotionsStart()[1];
      int randomIndex = (int)(Math.random() * 12000.0);
      double rotateNumb = this.getRotateStart();
      double rotate = 0.0;
      long timeOfSpawn = System.currentTimeMillis();
      int randomInt = (int)(1.0 + Math.random() * 4.5);
      Module.Category colorCategory = this.randomInt == 1
         ? Module.Category.COMBAT
         : (
            this.randomInt == 2
               ? Module.Category.MOVEMENT
               : (
                  this.randomInt == 3
                     ? Module.Category.RENDER
                     : (
                        this.randomInt == 4
                           ? Module.Category.PLAYER
                           : (this.randomInt == 5 ? Module.Category.MISC : (this.randomInt == 1 ? Module.Category.COMBAT : null))
                     )
               )
         );

      public Parts() {
      }

      private boolean canDrawPart() {
         return this.part != null && (!((double)this.alphaPC.getAnim() < 0.05) || this.alphaPC.to != 0.0F);
      }

      public Module.Category getColorCategory() {
         return this.colorCategory;
      }

      private double getRotateStart() {
         return -(Math.random() * 20.0) + Math.random() * 40.0;
      }

      private double[] getMotionsStart() {
         return new double[]{-(Math.random() * 3.0) + Math.random() * 6.0, -(0.25 * Math.random()) + Math.random() * 1.0};
      }

      private boolean[] isColiddedByPart(ClickGuiScreen.Parts part) {
         if (part != null && part.getTime() > 200L) {
            boolean xgN = MathUtils.getDifferenceOf((double)(this.pos.x + 8.0F) + this.motionX / 4.0, (double)(part.pos.x + 8.0F) + part.motionX / 4.0) < 16.0;
            boolean ygN = MathUtils.getDifferenceOf((double)(this.pos.y + 8.0F) + this.motionX / 4.0, (double)(part.pos.y + 8.0F) + part.motionX / 4.0) < 16.0;
            boolean xg = MathUtils.getDifferenceOf((double)(this.pos.x + 8.0F) + this.motionX / 4.0, (double)(part.pos.x + 8.0F) + part.motionX / 4.0) < 12.0;
            boolean yg = MathUtils.getDifferenceOf((double)(this.pos.y + 8.0F) + this.motionX / 4.0, (double)(part.pos.y + 8.0F) + part.motionX / 4.0) < 12.0;
            if (xg || xgN || ygN || yg) {
               return new boolean[]{xg && ygN, yg && xgN};
            }
         }

         return new boolean[]{false, false};
      }

      private void updatePhisics() {
         ScaledResolution sr = new ScaledResolution(ClickGuiScreen.this.mc);
         if (this.alphaPC.to != 0.0F) {
            this.pos.x = (float)((double)this.pos.x + this.motionX);
            this.pos.y = (float)((double)this.pos.y + this.motionY);
            this.motionY += 0.02F;
         }

         this.rotate = this.rotate + this.rotateNumb;
         boolean collideX = (double)this.pos.x + this.motionX / 2.0 + 5.0 <= 0.0
            || (double)this.pos.x + this.motionX / 2.0 + 16.0 - 5.0 >= (double)sr.getScaledWidth();
         boolean collideY = (double)this.pos.y + this.motionY / 2.0 + 5.0 <= 0.0
            || (double)this.pos.y + this.motionY / 2.0 + 16.0 - 5.0 >= (double)sr.getScaledHeight();
         this.motionX *= collideX ? -0.98F : 0.9995F;
         this.motionY *= collideY ? -0.98F : 0.9995F;
         if (this.getSpeed() > 3.0 && (collideX || collideY) && this.alphaPC.to != 0.0F) {
            this.alphaPC.to = 0.0F;
            this.timeOfSpawn += 10000000L;
            if (!ClickGuiScreen.this.partsEff.stream().anyMatch(p -> p.part == this)) {
               ClickGuiScreen.this.partsEff.add(ClickGuiScreen.this.new FallPartsEffect(this));
            }
         }

         if (collideX) {
            this.motionY *= 0.9F;
         }

         if (collideY || this.getSpeed() < 0.01) {
            this.motionX *= 0.97F;
            if (this.getSpeed() < 0.2F && this.alphaPC.to != 0.0F) {
               this.alphaPC.to = 0.0F;
               this.timeOfSpawn += 10000000L;
               if (!ClickGuiScreen.this.partsEff.stream().anyMatch(p -> p.part == this)) {
                  ClickGuiScreen.this.partsEff.add(ClickGuiScreen.this.new FallPartsEffect(this));
               }
            }
         }

         if (this.getSpeed() < 0.15 && this.alphaPC.to != 0.0F) {
            this.alphaPC.to = 0.0F;
            this.timeOfSpawn += 10000000L;
         }

         for (ClickGuiScreen.Parts part : ClickGuiScreen.this.parts) {
            boolean[] collidePart = this.isColiddedByPart(part);
            boolean collidePartX = part != null && part != this && this.alphaPC.to != 0.0F && collidePart[0];
            boolean collidePartY = part != null && part != this && this.alphaPC.to != 0.0F && collidePart[1];
            if (this.alphaPC.to != 0.0F && (collidePartX || collidePartY) && part != this) {
               ClickGuiScreen.Parts best = part.getSpeed() > this.getSpeed() ? part : this;
               ClickGuiScreen.Parts lose = part.getSpeed() > this.getSpeed() ? this : part;
               if (collidePartX) {
                  best.motionX *= 0.95F;
                  lose.motionX *= 1.1F;
               }

               if (collidePartY) {
                  best.motionY *= 0.95F;
                  lose.motionY *= 1.1F;
               }

               if (collidePartX) {
                  lose.motionX *= -1.0;
                  best.motionX *= -1.0;
               }

               if (collidePartY) {
                  best.motionY *= -1.0;
                  lose.motionY *= -1.0;
               }

               best.rotateNumb *= -0.9F;
               lose.rotateNumb *= -1.05F;
               lose.timeOfSpawn += 150L;
               best.timeOfSpawn += 50L;
               if ((double)best.getTimePC() >= 0.8 && !ClickGuiScreen.this.partsEff.stream().anyMatch(p -> p.part == best)) {
                  ClickGuiScreen.this.partsEff.add(ClickGuiScreen.this.new FallPartsEffect(best));
               }

               if (!ClickGuiScreen.this.partsEff.stream().anyMatch(p -> p.part == lose)) {
                  ClickGuiScreen.this.partsEff.add(ClickGuiScreen.this.new FallPartsEffect(lose));
               }

               if (best.getSpeed() < 0.4) {
                  if (lose.getSpeed() < 0.5) {
                     if (collidePartX) {
                        lose.motionX = best.motionX / 2.0;
                     }

                     if (collidePartY) {
                        lose.motionY = best.motionY / 2.0;
                     }

                     lose.motionY += 0.02F;
                     lose.timeOfSpawn += 500L;
                  }

                  if (collidePartX) {
                     best.motionX /= 2.0;
                  }

                  if (collidePartY) {
                     best.motionY /= 2.0;
                     lose.motionY += 0.1F;
                     best.motionY += 0.02F;
                  }

                  lose.timeOfSpawn += 100L;
               }

               if (best.getSpeed() > 4.7F && lose.getSpeed() > 4.7F) {
                  if (best.alphaPC.to != 0.0F) {
                     best.alphaPC.to = 0.0F;
                     best.timeOfSpawn += 10000000L;
                     if (!ClickGuiScreen.this.partsEff.stream().anyMatch(p -> p.part == best)) {
                        ClickGuiScreen.this.partsEff.add(ClickGuiScreen.this.new FallPartsEffect(best));
                     }
                  }

                  if (lose.alphaPC.to != 0.0F) {
                     lose.alphaPC.to = 0.0F;
                     lose.timeOfSpawn += 10000000L;
                     if (!ClickGuiScreen.this.partsEff.stream().anyMatch(p -> p.part == lose)) {
                        ClickGuiScreen.this.partsEff.add(ClickGuiScreen.this.new FallPartsEffect(lose));
                     }
                  }
               }
            }
         }

         this.rotateNumb *= !collideY && !collideX ? 0.9993F : -0.9F;
         if (collideY || collideX) {
            this.rotate = this.rotate + this.rotateNumb * 1.25;
            if (this.alphaPC.to == 1.0F && this.alphaPC.getAnim() > 0.75F) {
               this.alphaPC.setAnim(0.75F);
            }
         }

         this.controlPartTrailsCount();
      }

      private double getSpeed() {
         return Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY);
      }

      private long getCurrentTime() {
         return System.currentTimeMillis();
      }

      private long getSpawnTime() {
         return this.timeOfSpawn;
      }

      private long getTime() {
         return this.getCurrentTime() - this.getSpawnTime();
      }

      private float getMaxTime() {
         return 20000.0F;
      }

      private float getTimePC() {
         return MathUtils.clamp((float)this.getTime() / this.getMaxTime(), 0.0F, 1.0F);
      }

      private Vec2f getPos() {
         ScaledResolution sr = new ScaledResolution(ClickGuiScreen.this.mc);
         return new Vec2f((float)((double)sr.getScaledWidth() * Math.random()), (float)((double)sr.getScaledHeight() * Math.random()));
      }

      private ResourceLocation getNewPart() {
         String numb = "vegaline/ui/clickgui/particles/recochetparticles/default/part";
         int randNumb = (int)MathUtils.clamp(Math.random() * 7.5, 1.0, 7.0);
         numb = numb + randNumb;
         return new ResourceLocation(numb + ".png");
      }

      private void removeAuto(ClickGuiScreen.Parts part) {
         if (part != null && part.getTimePC() >= 1.0F) {
            part.alphaPC.to = 0.0F;
         }

         ClickGuiScreen.this.parts.removeIf(part2 -> part2 != null && (double)part2.alphaPC.getAnim() < 0.1 && part2.alphaPC.to == 0.0F);
      }

      private void draw(float alphaPC) {
         float alpha = MathUtils.clamp(alphaPC * this.alphaPC.getAnim(), 0.0F, 1.0F);
         RenderUtils.customRotatedObject2D(this.pos.x, this.pos.y, 16.0F, 16.0F, (double)((float)this.rotate));
         GL11.glPushMatrix();
         RenderUtils.customScaledObject2D(this.pos.x, this.pos.y, 16.0F, 16.0F, alpha);
         ClickGuiScreen.this.drawImage(
            this.part,
            this.pos.x,
            this.pos.y,
            16.0F,
            16.0F,
            ColorUtils.swapAlpha(ClickGuiScreen.getColor(this.randomIndex, this.getColorCategory()), 255.0F * alpha)
         );
         GL11.glPopMatrix();
         RenderUtils.customRotatedObject2D(this.pos.x, this.pos.y, 16.0F, 16.0F, (double)((float)(-this.rotate)));
      }

      private void drawPart(float alphaPC) {
         this.draw(alphaPC);
         this.drawPartTrails(alphaPC);
      }

      private int partTrailsMaxLength() {
         return (int)((7.0 + MathUtils.clamp(10.0 * this.getSpeed(), 0.0, 50.0)) * (double)this.alphaPC.getAnim());
      }

      private void controlPartTrailsCount() {
         int maxLength = this.partTrailsMaxLength();
         if (maxLength == 0) {
            this.partTrails.clear();
         } else {
            this.partTrails
               .add(
                  ClickGuiScreen.this.new PartTrail(
                     this, this.partTrails.size(), this.pos.x + 8.0F - (float)this.motionX * 3.0F, this.pos.y + 8.0F - (float)this.motionY * 3.0F
                  )
               );
            this.partTrails.forEach(ClickGuiScreen.PartTrail::updateIndex);
            this.partTrails.removeIf(part -> part.indexPart >= (float)maxLength);
         }
      }

      private void drawPartTrails(float alphaPC) {
         if (!this.partTrails.isEmpty()) {
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glDisable(3553);
            GL11.glShadeModel(7425);
            GL11.glLineWidth(1.0E-4F + 6.0F * this.alphaPC.getAnim() * (float)MathUtils.clamp(this.getSpeed() / 5.0, 0.5, 1.0));
            GL11.glBegin(3);
            this.partTrails.forEach(trail -> trail.drawVertex(alphaPC * this.alphaPC.getAnim()));
            GL11.glEnd();
            GL11.glLineWidth(1.0F);
            GL11.glShadeModel(7424);
            GL11.glEnable(3553);
            GL11.glHint(3154, 4352);
            GL11.glDisable(2848);
         }
      }
   }

   static class searchParticle {
      final float x;
      final float y;
      final AnimationUtils radius = new AnimationUtils(0.0F, 12.5F, 0.02F);
      final AnimationUtils alpha = new AnimationUtils(180.0F, 0.0F, 0.02F);

      public searchParticle(float x, float y) {
         this.x = x;
         this.y = y;
      }
   }
}
