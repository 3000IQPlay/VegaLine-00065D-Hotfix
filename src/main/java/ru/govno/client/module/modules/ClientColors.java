package ru.govno.client.module.modules;

import java.awt.Color;
import java.util.Arrays;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.govno.client.clickgui.ClickGuiScreen;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.module.settings.Settings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class ClientColors extends Module {
   private static final long currentTime = System.currentTimeMillis();
   private static final int[] COLLECT_COLORS = new int[360];
   private static boolean FADE_STATUS;
   private static int TIMED_INDEX;
   public String[] presetsList = new String[ClientColors.PresetColors.values().length];
   public String[] modesList = new String[]{"Astolfo", "Rainbow", "Colored", "TwoColored", "Fade", "Presets"};
   public static ClientColors get;
   public FloatSettings XPos;
   public FloatSettings YPos;
   public ModeSettings Mode;
   public ModeSettings Preset;
   public ColorSettings Pick1;
   public ColorSettings Pick2;
   private static ClientColors.PresetColors prevPreset;
   private static final AnimationUtils presetStepAnim = new AnimationUtils(0.0F, 0.0F, 0.035F);
   static boolean smoothPresets;
   static float presetStepAnimGetAnim = 0.0F;
   static String prevMode;
   static AnimationUtils modeStepAnim = new AnimationUtils(0.0F, 0.0F, 0.025F);
   static float modeStepAnimGetAnim = 0.0F;

   public static void alwaysColorUpdate() {
      if (get != null) {
         int colorDelay = 1800;
         float timePC = (float)(System.currentTimeMillis() % (long)colorDelay) / (float)colorDelay;
         TIMED_INDEX = (int)(timePC * 360.0F);
         String mode = get.Mode.currentMode;
         if (prevMode == null) {
            prevMode = mode;
         }

         FADE_STATUS = mode.equalsIgnoreCase("Astolfo")
            || mode.equalsIgnoreCase("Rainbow")
            || mode.equalsIgnoreCase("Fade")
            || mode.equalsIgnoreCase("Presets") && getPresetByName(get.Preset.currentMode).twoColored;
         if (FADE_STATUS) {
            for (int colorIndex = 0; colorIndex < 360; colorIndex++) {
               COLLECT_COLORS[colorIndex] = getColors(colorIndex)[0];
            }
         } else {
            for (int colorIndex = 0; colorIndex < 360; colorIndex++) {
               int[] colors = getColors(colorIndex);
               int color = ColorUtils.getOverallColorFrom(colors[0], colors[1], (float)colorIndex / 360.0F);
               COLLECT_COLORS[colorIndex] = color;
            }
         }
      }
   }

   private static int getCollectedColors(int index) {
      return COLLECT_COLORS[(index + 360000) % 360];
   }

   private static int getCollectedColor(int index) {
      return getCollectedColors(index / 3);
   }

   private static int getCollectedColor(int index, float alphaPC) {
      int color = getCollectedColors(index / 3);
      return ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC);
   }

   private static int getCollectedColorFade(int index) {
      return getCollectedColors(index / 3 + TIMED_INDEX);
   }

   private static int getCollectedColorFade(int index, float alphaPC) {
      int color = getCollectedColors(index / 3 + TIMED_INDEX);
      return ColorUtils.swapAlpha(color, (float)ColorUtils.getAlphaFromColor(color) * alphaPC);
   }

   public ClientColors() {
      super("ClientColors", 0, Module.Category.MISC, false, () -> false);
      float w = (float)new ScaledResolution(mc).getScaledWidth();
      float h = (float)new ScaledResolution(mc).getScaledHeight();
      this.settings.add(this.XPos = new FloatSettings("XPos", (w - 115.0F) / w, 1.0F, 0.0F, this));
      this.settings.add(this.YPos = new FloatSettings("YPos", 55.0F / h, 1.0F, 0.0F, this));
      this.settings.add(this.Mode = new ModeSettings("Mode", "Colored", this, this.modesList));
      int i = 0;

      for (ClientColors.PresetColors preset : ClientColors.PresetColors.values()) {
         this.presetsList[i] = preset.name;
         i++;
      }

      this.settings.add(this.Preset = new ModeSettings("Preset", this.presetsList[0], this, this.presetsList));
      this.settings.add(this.Pick1 = new ColorSettings("Pick1", ColorUtils.getColor(254, 39, 171), this));
      this.settings.add(this.Pick2 = new ColorSettings("Pick2", ColorUtils.getColor(255, 173, 139), this));
      get = this;
   }

   public static ClientColors.PresetColors getPresetByName(String name) {
      return Arrays.asList(ClientColors.PresetColors.values()).stream().filter(preset -> preset.name.equalsIgnoreCase(name)).findAny().orElse(null);
   }

   public static ClientColors.PresetColors getCurrentPreset() {
      ClientColors.PresetColors preset = getPresetByName(get.Preset.currentMode);
      if (prevPreset == null) {
         prevPreset = preset;
      }

      return preset;
   }

   public static int[] getColorsByMode(String mode, int index) {
      float indexPC = (float)((index + 360000) % 360) / 360.0F;
      int col1 = -1;
      int col2 = -1;
      switch (mode) {
         case "Presets":
            indexPC = (float)MathUtils.easeInOutQuadWave((double)indexPC);
            ClientColors.PresetColors preset = getCurrentPreset();
            if (presetStepAnimGetAnim != 0.0F) {
               if (ClientColors.prevPreset == null) {
                  ClientColors.prevPreset = preset;
               }

               ClientColors.PresetColors prevPreset = getPresetByName(ClientColors.prevPreset.name());
               int prevColor1 = ColorUtils.getOverallColorFrom(prevPreset.color1, prevPreset.color2, indexPC);
               int color2 = ColorUtils.getOverallColorFrom(preset.color1, preset.color2, indexPC);
               int var13 = ColorUtils.getOverallColorFrom(prevColor1, color2, presetStepAnimGetAnim);
               col2 = col1 = ColorUtils.getOverallColorFrom(var13, 0, (float)MathUtils.easeInOutQuadWave((double)presetStepAnimGetAnim));
            } else {
               col2 = col1 = ColorUtils.getOverallColorFrom(preset.color1, preset.color2, indexPC);
            }
            break;
         case "Colored":
            col1 = get.Pick1.color;
            col2 = get.Pick1.color;
            break;
         case "TwoColored":
            indexPC = (float)MathUtils.easeInOutQuadWave((double)indexPC);
            col2 = col1 = ColorUtils.getOverallColorFrom(get.Pick1.color, get.Pick2.color, indexPC);
            break;
         case "Fade":
            col2 = col1 = ColorUtils.getOverallColorFrom(get.Pick1.color, get.Pick2.color, (float)MathUtils.easeInOutQuadWave((double)indexPC));
            break;
         case "Astolfo":
            col1 = Color.HSBtoRGB(0.55F + (float)MathUtils.easeInOutQuadWave((double)indexPC) * 0.45F, 0.6F, 1.0F);
            col2 = Color.HSBtoRGB(0.55F + (0.5F + (float)MathUtils.easeInOutQuadWave((double)indexPC)) % 1.0F * 0.45F, 0.6F, 1.0F);
            break;
         case "Rainbow":
            col1 = Color.getHSBColor(indexPC, 0.8F, 1.0F).getRGB();
            col2 = Color.getHSBColor((indexPC + 0.5F) % 1.0F, 0.8F, 1.0F).getRGB();
      }

      return new int[]{col1, col2};
   }

   public static int[] getColors(int index) {
      String mode = get.Mode.currentMode;
      if (prevMode == null) {
         prevMode = mode;
      }

      if (modeStepAnimGetAnim != 0.0F) {
         int[] colors2 = getColorsByMode(mode, index);
         int[] colors1 = prevMode != mode ? getColorsByMode(prevMode, index) : colors2;
         int c1 = ColorUtils.getOverallColorFrom(colors1[0], colors2[0], modeStepAnimGetAnim);
         int c2 = ColorUtils.getOverallColorFrom(colors1[1], colors2[1], modeStepAnimGetAnim);
         return new int[]{c1, c2};
      } else {
         return getColorsByMode(get.Mode.currentMode, index);
      }
   }

   public static int getColor1(int index) {
      return FADE_STATUS ? getCollectedColorFade(index) : getCollectedColor(index);
   }

   public static int getColor2(int index) {
      index += 180;
      return FADE_STATUS ? getCollectedColorFade(index) : getCollectedColor(index);
   }

   public static int getColor1(int index, float aPC) {
      return FADE_STATUS ? getCollectedColorFade(index, aPC) : getCollectedColor(index, aPC);
   }

   public static int getColor2(int index, float aPC) {
      index += 180;
      return FADE_STATUS ? getCollectedColorFade(index, aPC) : getCollectedColor(index, aPC);
   }

   public static int getColorQ(int num) {
      return getColor1(-num * 90 * 3 + 90);
   }

   public static int getColorQ(int num, float alphaPC) {
      return getColor1(-num * 90 * 3 + 90, alphaPC);
   }

   public static int getColor1() {
      return getColor1(0);
   }

   public static int getColor2() {
      return getColor1(0);
   }

   public static class ClientColorsHud {
      AnimationUtils heightAnimation = new AnimationUtils(22.0F, 22.0F, 0.125F);
      AnimationUtils widthAnimation = new AnimationUtils(22.0F, 22.0F, 0.125F);
      String className = "Client color ui";
      float x;
      float y;
      AnimationUtils xRepos;
      AnimationUtils yRepos;
      AnimationUtils rotate;
      boolean dragging;
      float dragX;
      float dragY;
      boolean open;
      TimerHelper soundTicker;
      float offsetX;
      float offsetY;
      float offsetX2;
      float offsetX3;
      boolean dragginggr;
      boolean dragginghsb;
      boolean draggingalpha;
      float offsetXs;
      float offsetYs;
      float offsetX2s;
      float offsetX3s;
      boolean dragginggrs;
      boolean dragginghsbs;
      boolean draggingalphas;
      public boolean hover;

      public void setXYMenu(float x, float y, ScaledResolution sr) {
         ((FloatSettings)ClientColors.get.settings.get(0)).setFloat(x / (float)sr.getScaledWidth());
         ((FloatSettings)ClientColors.get.settings.get(1)).setFloat(y / (float)sr.getScaledHeight());
         this.x = x;
         this.y = y;
      }

      public boolean isHover(float x, float y, float w, float h, int mouseX, int mouseY) {
         return (float)mouseX <= x + w && (float)mouseX >= x && (float)mouseY <= y + h && (float)mouseY >= y;
      }

      public float getWidth() {
         this.widthAnimation.to = 100.0F;
         return this.widthAnimation.getAnim();
      }

      public float getRotate() {
         this.rotate.to = this.x - this.getX();
         return MathUtils.clamp(this.rotate.getAnim() / 2.25F, -90.0F, 90.0F);
      }

      public float toHeight() {
         return this.open ? MathUtils.clamp(this.getTotalElementsHeight(), 106.0F, 202.0F) : 22.0F;
      }

      public void heightCalculate() {
         this.heightAnimation.to = this.toHeight();
      }

      public float getHeight() {
         return this.heightAnimation.getAnim();
      }

      public void updatePosXY() {
         this.xRepos.to = this.x;
         this.yRepos.to = this.y;
      }

      public float getX() {
         return this.xRepos.getAnim();
      }

      public float getY() {
         return this.yRepos.getAnim();
      }

      public float getScrollY() {
         return ClickGuiScreen.scrollAnimation.getAnim();
      }

      public void dragUpdate(int mouseX, int mouseY) {
         ScaledResolution sr = new ScaledResolution(Module.mc);
         if (!this.dragging) {
            this.setXYMenu(ClientColors.get.XPos.getFloat() * (float)sr.getScaledWidth(), ClientColors.get.YPos.getFloat() * (float)sr.getScaledHeight(), sr);
            this.dragX = (float)mouseX - this.x;
            this.dragY = (float)mouseY - this.y;
         } else {
            this.setXYMenu((float)mouseX - this.dragX, (float)mouseY - this.dragY, sr);
         }
      }

      float getModesHeight() {
         float yStep = 10.0F;
         ModeSettings setting = (ModeSettings)ClientColors.get.settings.get(2);

         for (String mode : setting.modes) {
            yStep += 10.0F;
         }

         return yStep;
      }

      public void drawColorModes(float x, float y, float w, float h, ScaledResolution sr, float alphaPC) {
         ModeSettings setting = (ModeSettings)ClientColors.get.settings.get(2);
         String currentMode = ClientColors.get.Mode.currentMode;
         RenderUtils.drawAlphedRect((double)x, (double)y, (double)(x + w), (double)(y + h), ColorUtils.getColor(0, 0, 0, 60.0F * alphaPC));
         RenderUtils.drawLightContureRect((double)x, (double)y, (double)(x + w), (double)(y + h), ColorUtils.getColor(255, 255, 255, 60.0F * alphaPC));
         int czC1 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(180)), 55.0F * alphaPC);
         int czC2 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2()), 55.0F * alphaPC);
         int czC3 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2(180)), 25.0F * alphaPC);
         int czC4 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(0)), 25.0F * alphaPC);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x, y, x + w, y + h, 1.0F, 9.0F, czC1, czC2, czC3, czC4, true, false, true
         );
         if (255.0F * alphaPC >= 28.0F && this.isInRenderZone(y, 10.0F)) {
            Fonts.mntsb_15
               .drawString("Color mode", (double)(x + 3.0F), (double)(y + 3.0F), ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC));
         }

         float yStep = 10.0F;

         for (String mode : setting.modes) {
            if (this.isInRenderZone(y + yStep, 10.0F)) {
               int color1 = mode.equalsIgnoreCase(currentMode) ? ClientColors.getColor1(0) : -1;
               int color2 = mode.equalsIgnoreCase(currentMode) ? ClientColors.getColor2(0) : -1;
               int colorSelect1 = ColorUtils.getColor(255, 255, 255, 20.0F * alphaPC);
               int colorSelect2 = ColorUtils.getColor(255, 255, 255, 20.0F * alphaPC);
               RenderUtils.drawFullGradientRectPro(x, y + yStep, x + w, y + yStep + 10.0F, colorSelect1, colorSelect2, colorSelect2, colorSelect1, false);
               if (mode.equalsIgnoreCase(currentMode)) {
                  float changePC = ClientColors.modeStepAnimGetAnim;
                  if ((double)changePC < 0.5) {
                     changePC *= 2.0F;
                     changePC = (double)changePC > 0.5 ? 1.0F - changePC : changePC;
                     changePC *= 2.0F;
                  } else {
                     changePC = 0.0F;
                  }

                  colorSelect1 = ColorUtils.swapAlpha(color1, (45.0F + changePC * 50.0F) * alphaPC);
                  colorSelect2 = ColorUtils.swapAlpha(color2, (45.0F + changePC * 50.0F) * alphaPC);
                  float WMM = w / 2.0F - (float)Fonts.comfortaaBold_14.getStringWidth(mode) / 2.0F;
                  RenderUtils.drawFullGradientRectPro(x, y + yStep, x + w, y + yStep + 10.0F, colorSelect1, colorSelect2, colorSelect2, colorSelect1, true);
                  changePC = ClientColors.modeStepAnimGetAnim;
                  changePC = (double)changePC > 0.5 ? 1.0F - changePC : changePC;
                  changePC *= 2.0F;
                  RenderUtils.drawAlphedRect(
                     (double)x,
                     (double)(y + yStep),
                     (double)(x + 1.0F),
                     (double)(y + yStep + 10.0F * ClientColors.modeStepAnimGetAnim),
                     ColorUtils.swapAlpha(-1, 255.0F * changePC * alphaPC)
                  );
                  RenderUtils.drawAlphedRect(
                     (double)(x + w - 1.0F),
                     (double)(y + yStep + 10.0F * (1.0F - ClientColors.modeStepAnimGetAnim)),
                     (double)(x + w),
                     (double)(y + yStep + 10.0F),
                     ColorUtils.swapAlpha(-1, 255.0F * changePC * alphaPC)
                  );
                  RenderUtils.drawAlphedSideways(
                     (double)x,
                     (double)(y + yStep),
                     (double)(x + w * ClientColors.modeStepAnimGetAnim),
                     (double)(y + yStep + 10.0F),
                     0,
                     ColorUtils.swapAlpha(-1, 255.0F * changePC * alphaPC),
                     true
                  );
                  RenderUtils.drawAlphedSideways(
                     (double)(x + w * ClientColors.modeStepAnimGetAnim),
                     (double)(y + yStep),
                     (double)(x + w),
                     (double)(y + yStep + 10.0F),
                     ColorUtils.swapAlpha(-1, 255.0F * changePC * alphaPC),
                     0,
                     true
                  );
               }

               int i = 0;

               for (char c : mode.toCharArray()) {
                  int modeColor = mode.equalsIgnoreCase(currentMode)
                     ? ColorUtils.getOverallColorFrom(
                        ClientColors.getColor1(i), ClientColors.getColor2(i), (float)(i / Fonts.comfortaaBold_14.getStringWidth(mode))
                     )
                     : -1;
                  if (255.0F * alphaPC >= 33.0F) {
                     Fonts.comfortaaBold_14
                        .drawStringWithShadow(
                           String.valueOf(c),
                           (double)(x + w / 2.0F - (float)(Fonts.comfortaaBold_14.getStringWidth(mode) / 2) + (float)i),
                           (double)(y + yStep + 3.0F),
                           ColorUtils.swapAlpha(modeColor, 255.0F * alphaPC)
                        );
                  }

                  i += Fonts.comfortaaBold_14.getStringWidth(String.valueOf(c));
               }
            }

            yStep += 10.0F;
         }
      }

      public boolean isInRenderZone(float y1, float height) {
         float y = this.yRepos.anim + 23.0F - height;
         float y2 = this.yRepos.anim + this.heightAnimation.anim - 23.0F;
         return y1 >= y && y1 - height <= y2;
      }

      public void drawPanel(int mouseX, int mouseY, float partialTicks, float x, float y, float w, float h, boolean opennd, ScaledResolution sr, float alphaPC) {
         CFontRenderer font = Fonts.mntsb_18;
         CFontRenderer icon = Fonts.iconswex_24;
         String currentMode = ClientColors.get.Mode.currentMode;
         float x2 = x + w;
         float y2 = y + h;
         int bgColor = ColorUtils.getColor(0, 0, 0, 200.0F * alphaPC);
         int bgColor2 = ColorUtils.getColor(0, 0, 0, 140.0F * alphaPC);
         int bgColorOutline = ColorUtils.getColor(255, 255, 255, 40.0F * alphaPC);
         int shadowCol = ColorUtils.getColor(0, 0, 0, 70.0F * alphaPC);
         int textColor = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC);
         int lineCol = ColorUtils.getColor(255, 255, 255, 255.0F * alphaPC);
         int lineCol2 = ColorUtils.getColor(255, 255, 255, 130.0F * alphaPC);
         int lineCol3 = ColorUtils.getColor(255, 255, 255, 50.0F * alphaPC);
         alphaPC *= alphaPC;
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x, y, x2, y2, 3.0F, 6.0F, shadowCol, shadowCol, shadowCol, shadowCol, false, false, true
         );
         StencilUtil.initStencilToWrite();
         RenderUtils.drawAlphedRect((double)x, (double)(y + 18.5F), (double)x2, (double)y2, -1);
         StencilUtil.readStencilBuffer(0);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x, y, x2, y2, 4.0F, 6.0F, bgColor, bgColor, bgColor, bgColor, false, true, false
         );
         StencilUtil.readStencilBuffer(1);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x, y, x2, y2, 3.0F, 6.0F, bgColor2, bgColor2, bgColor2, bgColor2, false, true, false
         );
         RenderUtils.drawTwoAlphedSideways((double)(x + 0.5F), (double)(y + 19.0F), (double)(x2 - 0.5F), (double)(y + 20.0F), lineCol, lineCol3, false);
         StencilUtil.uninitStencilBuffer();
         StencilUtil.initStencilToWrite();
         RenderUtils.drawRect((double)x, (double)(y2 - 2.0F), (double)x2, (double)y2, -1);
         StencilUtil.readStencilBuffer(1);
         RenderUtils.drawTwoAlphedSideways((double)(x + 1.0F), (double)(y2 - 2.0F), (double)(x2 - 1.0F), (double)(y2 - 1.5F), lineCol, lineCol3, false);
         RenderUtils.drawTwoAlphedSideways((double)(x + 1.5F), (double)(y2 - 1.5F), (double)(x2 - 1.5F), (double)(y2 - 1.0F), lineCol, lineCol3, false);
         RenderUtils.drawTwoAlphedSideways((double)(x + 2.0F), (double)(y2 - 1.0F), (double)(x2 - 2.0F), (double)(y2 - 0.5F), lineCol2, lineCol3, false);
         StencilUtil.uninitStencilBuffer();
         RenderUtils.drawOutsideAndInsideFullRoundedFullGradientShadowRectWithBloomBoolShadowsBoolChangeShadowSize(
            x, y, x2, y2, 0.0F, 4.0F, 0.0F, lineCol2, lineCol2, lineCol3, lineCol3, false, true, false
         );
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x, y, x2, y2, 4.0F, 1.5F, lineCol2, lineCol2, lineCol3, lineCol3, false, false, true
         );
         StencilUtil.initStencilToWrite();
         RenderUtils.drawRect((double)x, (double)y, (double)x2, (double)y2, -1);
         StencilUtil.readStencilBuffer(1);
         if (255.0F * alphaPC >= 28.0F) {
            font.drawString(this.className, (double)(x + 23.0F), (double)(y + 7.0F), textColor);
            icon.drawString("G", (double)(x + 6.0F), (double)(y + 8.0F), textColor);
         }

         StencilUtil.uninitStencilBuffer();
         if (this.getHeight() >= 23.0F) {
            StencilUtil.initStencilToWrite();
            RenderUtils.drawRect((double)x, (double)(y + 20.0F), (double)x2, (double)(y2 - 2.0F), -1);
            StencilUtil.readStencilBuffer(1);
            y -= this.getScrollY();
            float modesX = x + 5.0F;
            float modesY = y + 25.0F;
            float modesW = w - 10.0F;
            float modesH = this.getModesHeight();
            if (this.isInRenderZone(modesY + 6.5F, modesH)) {
               this.drawColorModes(modesX, modesY, modesW, modesH, sr, alphaPC);
            }

            if (this.hasSettings() && this.isInRenderZone(modesY + modesH + 6.5F, 0.5F)) {
               RenderUtils.drawTwoAlphedSideways(
                  (double)(x + 7.0F), (double)(modesY + modesH + 5.0F), (double)(x + w - 7.0F), (double)(modesY + modesH + 5.5F), lineCol2, lineCol3, false
               );
            }

            float presetsX = x + 5.0F;
            float presetsY = modesY + modesH + 11.0F;
            float presetsW = w - 10.0F;
            float presetsH = this.getPresetsHeight();
            float pickerX = x + 5.0F;
            float pickerY = modesY + modesH + 11.0F;
            float pickerW = w - 10.0F;
            float pickerH = this.getPickerHeight();
            float pickerY2 = pickerY + pickerH + 11.0F;
            if (currentMode.equalsIgnoreCase("Presets") && this.isInRenderZone(presetsY, presetsH)) {
               this.drawColorPresets(presetsX, presetsY, presetsW, presetsH, sr, alphaPC);
            }

            if ((currentMode.equalsIgnoreCase("Colored") || currentMode.equalsIgnoreCase("TwoColored") || currentMode.equalsIgnoreCase("Fade"))
               && this.isInRenderZone(pickerY, pickerH)) {
               this.drawColorPicker(pickerX, pickerY, pickerW, pickerH, sr, mouseX, mouseY, alphaPC);
            }

            if (currentMode.equalsIgnoreCase("TwoColored") || currentMode.equalsIgnoreCase("Fade")) {
               if (this.isInRenderZone(pickerY - 19.0F + pickerH, 1.0F)) {
                  RenderUtils.drawTwoAlphedSideways(
                     (double)(x + 7.0F),
                     (double)(pickerY + pickerH + 5.0F),
                     (double)(x + w - 7.0F),
                     (double)(pickerY + pickerH + 5.5F),
                     lineCol2,
                     lineCol3,
                     false
                  );
               }

               if (this.isInRenderZone(pickerY - 19.0F + pickerH + 9.0F, 1.0F)) {
                  this.drawColorPicker2(pickerX, pickerY + 11.0F + pickerH, pickerW, pickerH, sr, mouseX, mouseY, alphaPC);
               }
            }

            float flatY = y + this.getTotalElementsHeight() + 29.0F;
            if (this.isInRenderZone(flatY - 20.0F, 5.0F)) {
               float flatStepX = 1.0F;
               float flatsCount = 90.0F;
               int flatsTimeOn = 600;
               int flatsTimeStep = 15;
               float flatX = x + this.getWidth() / 2.0F - flatStepX * (flatsCount - 1.0F) / 2.0F;
               if (alphaPC * 255.0F >= 32.0F) {
                  for (int c = 0; (float)c < flatsCount; c++) {
                     float timePC = (float)((System.currentTimeMillis() + (long)(flatsTimeStep * c)) % (long)flatsTimeOn) / (float)flatsTimeOn;
                     timePC = (double)timePC > 0.5 ? 1.0F - timePC : timePC;
                     timePC *= 2.0F;
                     float wavePC = (float)MathUtils.easeInOutQuadWave((double)timePC);
                     RenderUtils.drawSmoothCircle(
                        (double)flatX, (double)(flatY + (wavePC - 0.25F) * 2.0F), 1.25F * alphaPC, ClientColors.getColor1(c * 9, alphaPC * alphaPC)
                     );
                     flatX += flatStepX;
                  }
               }
            }

            StencilUtil.uninitStencilBuffer();
         }
      }

      private double getMouseSpeed() {
         float dX = (float)Mouse.getDX();
         float dY = (float)Mouse.getDX();
         return Math.sqrt((double)(dX * dX + dY * dY));
      }

      private long getTimerateSoundMove(double mouseSpeed) {
         if (mouseSpeed == 0.0) {
            return Long.MAX_VALUE;
         } else {
            long time = 1000L;
            return (long)((double)time / MathUtils.clamp(mouseSpeed * 10.0, 1.0, 100.0));
         }
      }

      private void updateSliderSounds() {
         if (this.soundTicker.hasReached((float)this.getTimerateSoundMove(this.getMouseSpeed()))) {
            ClientTune.get.playGuiSliderMoveSong();
            this.soundTicker.reset();
         }
      }

      public void drawColorPicker(float x, float y, float w, float h, ScaledResolution sr, int mouseX, int mouseY, float alphaPC) {
         if (!(255.0F * alphaPC <= 26.0F)) {
            ColorSettings setting = (ColorSettings)ClientColors.get.settings.get(4);
            boolean doublePicker = false;
            RenderUtils.drawAlphedRect((double)x, (double)(y + 15.0F), (double)(x + w), (double)(y + h), ColorUtils.getColor(0, 0, 0, 60.0F * alphaPC));
            RenderUtils.drawLightContureRect((double)x, (double)y, (double)(x + w), (double)(y + h), ColorUtils.getColor(255, 255, 255, 60.0F * alphaPC));
            RenderUtils.drawAlphedRect((double)x, (double)y, (double)(x + w), (double)(y + 15.0F), ColorUtils.getColor(0, 0, 0, 90.0F * alphaPC));
            int czC1 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(180)), 55.0F * alphaPC);
            int czC2 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2()), 55.0F * alphaPC);
            int czC3 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2(180)), 25.0F * alphaPC);
            int czC4 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(0)), 25.0F * alphaPC);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               x, y, x + w, y + h, 1.0F, 9.0F, czC1, czC2, czC3, czC4, true, false, true
            );
            if (255.0F * alphaPC >= 28.0F && this.isInRenderZone(y, 15.0F)) {
               Fonts.mntsb_15
                  .drawString("Pick color", (double)(x + 14.0F), (double)(y + 6.0F), ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC));
               Fonts.stylesicons_20
                  .drawString("M", (double)(x + 3.0F), (double)(y + 6.5F), ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC));
            }

            int finalColor = ColorUtils.swapAlpha(setting.color, (float)ColorUtils.getAlphaFromColor(setting.color) * alphaPC);
            int finalColor2 = ColorUtils.swapAlpha(setting.color, (float)ColorUtils.getAlphaFromColor(setting.color) / 2.5F * alphaPC);
            if (this.isInRenderZone(y, 15.0F)) {
               RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                  x + w - 18.0F, y + 6.0F, x + w - 18.0F + 11.0F, y + 11.0F, 2.0F, 0.5F, finalColor, finalColor, finalColor, finalColor, false, true, true
               );
               RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                  x + w - 18.0F, y + 6.0F, x + w - 18.0F + 11.0F, y + 11.0F, 2.0F, 6.0F, finalColor2, finalColor2, finalColor2, finalColor2, true, false, true
               );
            }

            int colHSB = -1;
            float hsbX = x + 6.0F;
            float hsbY = y + 68.0F;
            float hsbW = w - 12.0F;
            float hsbH = 6.0F;
            float alphaX = x + 6.0F;
            float alphaY = y + 86.0F;
            float alphaW = w - 12.0F;
            float alphaH = 6.0F;
            float grX = x + 5.0F;
            float grY = y + 20.0F;
            float grH = 35.0F;
            float grW = w - 10.0F;
            float draggXgr = grX + this.offsetX;
            float draggYgr = grY + this.offsetY;
            float percXgr = this.offsetX / grW;
            float percYgr = this.offsetY / grH;
            if (this.dragginggr) {
               this.dragginghsb = false;
               this.draggingalpha = false;
               this.updateSliderSounds();
               this.offsetX = MathUtils.clamp((float)mouseX - grX, 0.0F, grW);
               this.offsetY = MathUtils.clamp((float)mouseY - grY, 0.0F, grH);
            } else if (ColorUtils.getSaturateFromColor(setting.color) != 0.0F) {
               this.offsetX = ColorUtils.getSaturateFromColor(setting.color) * grW;
               this.offsetY = grH - ColorUtils.getBrightnessFromColor(setting.color) * grH;
            }

            if (this.isInRenderZone(grY, grH)) {
               RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                  grX,
                  grY,
                  grX + grW,
                  grY + grH,
                  2.0F,
                  1.0F,
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC),
                  ColorUtils.swapAlpha(Color.getHSBColor(this.offsetX2 / hsbW, 1.0F, 1.0F).getRGB(), 255.0F * alphaPC),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC),
                  false,
                  true,
                  true
               );
               RenderUtils.resetBlender();
            }

            RenderUtils.drawLightContureRect(
               (double)hsbX,
               (double)hsbY,
               (double)(hsbX + hsbW),
               (double)(hsbY + hsbH),
               ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
            );
            float draggXhsb = hsbX + this.offsetX2;
            float HSBCC = this.offsetX2 / hsbW * 360.0F % 360.0F;
            if (HSBCC < 0.0F) {
               HSBCC = 360.0F - HSBCC;
            }

            float percXhsb = HSBCC / 360.0F;
            float percXalpha = this.offsetX3 / alphaW;
            Fonts.comfortaaBold_12
               .drawStringWithShadow(
                  "Hsb - " + String.format("%.2f", HSBCC),
                  (double)(hsbX + 1.0F),
                  (double)(hsbY - 7.0F),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );

            for (int i = 0; i < 359; i++) {
               float hsb = (float)i / 360.0F;
               hsb = hsb < 0.0F ? 0.0F : (hsb > 1.0F ? 1.0F : hsb);
               colHSB = Color.getHSBColor(hsb, 1.0F, 1.0F).getRGB();
               float pc = (float)i / 360.0F * (hsbW - 0.5F);
               RenderUtils.drawAlphedRect(
                  (double)(hsbX + pc), (double)hsbY, (double)(hsbX + pc + 0.5F), (double)(hsbY + hsbH), ColorUtils.swapAlpha(colHSB, 255.0F * alphaPC)
               );
            }

            if (this.isInRenderZone(alphaY - 16.0F, alphaH)) {
               RenderUtils.drawLightContureRect(
                  (double)alphaX,
                  (double)alphaY,
                  (double)(alphaX + alphaW),
                  (double)(alphaY + alphaH),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );
            }

            if (255.0F * alphaPC >= 28.0F && this.isInRenderZone(alphaY - alphaH - 16.0F, alphaH)) {
               Fonts.comfortaaBold_12
                  .drawStringWithShadow(
                     "Alpha - " + (int)(percXalpha * 255.0F),
                     (double)(alphaX + 1.0F),
                     (double)(alphaY - 7.0F),
                     ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
                  );
            }

            if (this.isInRenderZone(alphaY - 16.0F, alphaH)) {
               alphaW *= 2.0F;
               alphaH *= 2.0F;

               for (int i2 = 0; (float)i2 < alphaH; i2 += 4) {
                  for (int i = 0; (float)i < alphaW; i += 4) {
                     int colTest = (i % 8 != 0 || i2 == 4) && (i2 != 4 || i % 8 == 0)
                        ? ColorUtils.getColor(200, 200, 200, 125)
                        : ColorUtils.getColor(125, 125, 125, 125);
                     int colAlpha = ColorUtils.getOverallColorFrom(colTest, ColorUtils.swapAlpha(setting.color, 255.0F), (float)i / alphaW);
                     RenderUtils.drawAlphedRect(
                        (double)(alphaX + (float)(i / 2)),
                        (double)(alphaY + (float)(i2 / 2)),
                        (double)(alphaX + (float)(i / 2) + 2.0F),
                        (double)(alphaY + (float)(i2 / 2) + 2.0F),
                        ColorUtils.swapAlpha(colAlpha, 255.0F * alphaPC)
                     );
                  }
               }

               alphaW /= 2.0F;
               alphaH /= 2.0F;
            }

            if (this.isInRenderZone(alphaY - 16.0F, alphaH)) {
               float alphaXCursor = alphaX + this.offsetX3;
               float alphaYCursor = alphaY + alphaH / 2.0F;
               RenderUtils.drawAlphedRect(
                  (double)(alphaXCursor - 1.5F),
                  (double)(alphaYCursor - 2.5F),
                  (double)(alphaXCursor + 1.5F),
                  (double)(alphaYCursor + 2.5F),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC)
               );
               RenderUtils.drawAlphedRect(
                  (double)(alphaXCursor - 1.0F),
                  (double)(alphaYCursor - 2.0F),
                  (double)(alphaXCursor + 1.0F),
                  (double)(alphaYCursor + 2.0F),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );
            }

            RenderUtils.drawAlphedRect(
               (double)(draggXgr - 1.5F),
               (double)(draggYgr - 1.5F),
               (double)(draggXgr + 1.5F),
               (double)(draggYgr + 1.5F),
               ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC)
            );
            RenderUtils.drawAlphedRect(
               (double)(draggXgr - 1.0F),
               (double)(draggYgr - 1.0F),
               (double)(draggXgr + 1.0F),
               (double)(draggYgr + 1.0F),
               ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
            );
            float hsbXCursor = hsbX + this.offsetX2;
            float hsbYCursor = hsbY + hsbH / 2.0F;
            RenderUtils.drawAlphedRect(
               (double)(hsbXCursor - 1.5F),
               (double)(hsbYCursor - 2.5F),
               (double)(hsbXCursor + 1.5F),
               (double)(hsbYCursor + 2.5F),
               ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC)
            );
            RenderUtils.drawAlphedRect(
               (double)(hsbXCursor - 1.0F),
               (double)(hsbYCursor - 2.0F),
               (double)(hsbXCursor + 1.0F),
               (double)(hsbYCursor + 2.0F),
               ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
            );
            if (this.dragginghsb) {
               this.dragginggr = false;
               this.draggingalpha = false;
               this.offsetX2 = MathUtils.clamp((float)mouseX - hsbX, 0.0F, hsbW);
               this.updateSliderSounds();
            } else if (ColorUtils.getHueFromColor(setting.color) != 0 && !this.dragginggr) {
               this.offsetX2 = (float)ColorUtils.getHueFromColor(setting.color) / 360.0F * hsbW;
            } else if (this.offsetX2 == -1.2312312E8F) {
               this.offsetX2 = 0.0F;
            }

            if (this.draggingalpha) {
               this.dragginggr = false;
               this.dragginghsb = false;
               this.offsetX3 = MathUtils.clamp((float)mouseX - alphaX, 0.0F, alphaW);
               this.updateSliderSounds();
            } else {
               this.offsetX3 = (float)ColorUtils.getAlphaFromColor(setting.color) / 255.0F * alphaW;
            }

            int col = Color.getHSBColor(MathUtils.clamp(percXhsb, 0.0F, 0.999F), percXgr, 1.0F - percYgr).getRGB();
            col = ColorUtils.swapAlpha(col, 255.0F * percXalpha);
            if (col != 0 && Mouse.isButtonDown(0)) {
               setting.color = col;
            }
         }
      }

      public void drawColorPicker2(float x, float y, float w, float h, ScaledResolution sr, int mouseX, int mouseY, float alphaPC) {
         if (!(255.0F * alphaPC <= 26.0F)) {
            ColorSettings setting = (ColorSettings)ClientColors.get.settings.get(5);
            boolean doublePicker = false;
            RenderUtils.drawAlphedRect((double)x, (double)(y + 15.0F), (double)(x + w), (double)(y + h), ColorUtils.getColor(0, 0, 0, 60.0F * alphaPC));
            RenderUtils.drawLightContureRect((double)x, (double)y, (double)(x + w), (double)(y + h), ColorUtils.getColor(255, 255, 255, 60.0F * alphaPC));
            if (this.isInRenderZone(y - 8.0F, 15.0F)) {
               RenderUtils.drawAlphedRect((double)x, (double)y, (double)(x + w), (double)(y + 15.0F), ColorUtils.getColor(0, 0, 0, 90.0F * alphaPC));
            }

            int czC1 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(180)), 55.0F * alphaPC);
            int czC2 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2()), 55.0F * alphaPC);
            int czC3 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2(180)), 25.0F * alphaPC);
            int czC4 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(0)), 25.0F * alphaPC);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               x, y, x + w, y + h, 1.0F, 9.0F, czC1, czC2, czC3, czC4, true, false, true
            );
            if (this.isInRenderZone(y - 8.0F, 15.0F)) {
               if (255.0F * alphaPC >= 28.0F) {
                  Fonts.mntsb_15
                     .drawString(
                        "Pick color 2", (double)(x + 14.0F), (double)(y + 6.0F), ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
                     );
                  Fonts.stylesicons_20
                     .drawString("M", (double)(x + 3.0F), (double)(y + 6.5F), ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC));
               }

               int finalColor = ColorUtils.swapAlpha(setting.color, (float)ColorUtils.getAlphaFromColor(setting.color) * alphaPC);
               int finalColor2 = ColorUtils.swapAlpha(setting.color, (float)ColorUtils.getAlphaFromColor(setting.color) / 2.5F * alphaPC);
               RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                  x + w - 18.0F, y + 6.0F, x + w - 18.0F + 11.0F, y + 11.0F, 2.0F, 0.5F, finalColor, finalColor, finalColor, finalColor, false, true, true
               );
               RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                  x + w - 18.0F, y + 6.0F, x + w - 18.0F + 11.0F, y + 11.0F, 2.0F, 6.0F, finalColor2, finalColor2, finalColor2, finalColor2, true, false, true
               );
            }

            int colHSB = -1;
            float hsbX = x + 6.0F;
            float hsbY = y + 68.0F;
            float hsbW = w - 12.0F;
            float hsbH = 6.0F;
            float alphaX = x + 6.0F;
            float alphaY = y + 86.0F;
            float alphaW = w - 12.0F;
            float alphaH = 6.0F;
            float grX = x + 5.0F;
            float grY = y + 20.0F;
            float grH = 35.0F;
            float grW = w - 10.0F;
            float draggXgr = grX + this.offsetXs;
            float draggYgr = grY + this.offsetYs;
            float percXgr = this.offsetXs / grW;
            float percYgr = this.offsetYs / grH;
            if (this.dragginggrs) {
               this.dragginghsbs = false;
               this.draggingalphas = false;
               this.updateSliderSounds();
               this.offsetXs = MathUtils.clamp((float)mouseX - grX, 0.0F, grW);
               this.offsetYs = MathUtils.clamp((float)mouseY - grY, 0.0F, grH);
            } else if (ColorUtils.getSaturateFromColor(setting.color) != 0.0F) {
               this.offsetXs = ColorUtils.getSaturateFromColor(setting.color) * grW;
               this.offsetYs = grH - ColorUtils.getBrightnessFromColor(setting.color) * grH;
            }

            if (this.isInRenderZone(grY + 12.0F, grH)) {
               RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                  grX,
                  grY,
                  grX + grW,
                  grY + grH,
                  2.0F,
                  1.0F,
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC),
                  ColorUtils.swapAlpha(Color.getHSBColor(this.offsetX2s / hsbW, 1.0F, 1.0F).getRGB(), 255.0F * alphaPC),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC),
                  false,
                  true,
                  true
               );
            }

            RenderUtils.resetBlender();
            if (this.isInRenderZone(hsbY - 10.0F, hsbH)) {
               RenderUtils.drawLightContureRect(
                  (double)hsbX,
                  (double)hsbY,
                  (double)(hsbX + hsbW),
                  (double)(hsbY + hsbH),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );
            }

            float draggXhsb = hsbX + this.offsetX2s;
            float HSBCC = this.offsetX2s / hsbW * 360.0F % 360.0F;
            if (HSBCC < 0.0F) {
               HSBCC = 360.0F - HSBCC;
            }

            float percXhsb = HSBCC / 360.0F;
            float percXalpha = this.offsetX3s / alphaW;
            if (255.0F * alphaPC >= 28.0F && this.isInRenderZone(hsbY - hsbH - 16.0F, 8.0F)) {
               Fonts.comfortaaBold_12
                  .drawStringWithShadow(
                     "Hsb - " + String.format("%.2f", HSBCC),
                     (double)(hsbX + 1.0F),
                     (double)(hsbY - 7.0F),
                     ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
                  );
            }

            if (this.isInRenderZone(hsbY - 10.0F, hsbH)) {
               for (int i = 0; i < 359; i++) {
                  float hsb = (float)i / 360.0F;
                  hsb = hsb < 0.0F ? 0.0F : (hsb > 1.0F ? 1.0F : hsb);
                  colHSB = Color.getHSBColor(hsb, 1.0F, 1.0F).getRGB();
                  float pc = (float)i / 360.0F * (hsbW - 0.5F);
                  RenderUtils.drawAlphedRect(
                     (double)(hsbX + pc), (double)hsbY, (double)(hsbX + pc + 0.5F), (double)(hsbY + hsbH), ColorUtils.swapAlpha(colHSB, 255.0F * alphaPC)
                  );
               }
            }

            GlStateManager.enableBlend();
            if (this.isInRenderZone(alphaY - 10.0F, alphaH)) {
               RenderUtils.drawLightContureRect(
                  (double)alphaX,
                  (double)alphaY,
                  (double)(alphaX + alphaW),
                  (double)(alphaY + alphaH),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );
            }

            if (255.0F * alphaPC >= 28.0F && this.isInRenderZone(alphaY - alphaH - 16.0F, alphaH)) {
               Fonts.comfortaaBold_12
                  .drawStringWithShadow(
                     "Alpha - " + (int)(percXalpha * 255.0F),
                     (double)(alphaX + 1.0F),
                     (double)(alphaY - 7.0F),
                     ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
                  );
            }

            if (this.isInRenderZone(alphaY - 10.0F, alphaH)) {
               alphaW *= 2.0F;
               alphaH *= 2.0F;

               for (int i2 = 0; (float)i2 < alphaH; i2 += 4) {
                  for (int i = 0; (float)i < alphaW; i += 4) {
                     int colTest = (i % 8 != 0 || i2 == 4) && (i2 != 4 || i % 8 == 0)
                        ? ColorUtils.getColor(200, 200, 200, 125)
                        : ColorUtils.getColor(125, 125, 125, 125);
                     int colAlpha = ColorUtils.getOverallColorFrom(colTest, ColorUtils.swapAlpha(setting.color, 255.0F), (float)i / alphaW);
                     RenderUtils.drawAlphedRect(
                        (double)(alphaX + (float)(i / 2)),
                        (double)(alphaY + (float)(i2 / 2)),
                        (double)(alphaX + (float)(i / 2) + 2.0F),
                        (double)(alphaY + (float)(i2 / 2) + 2.0F),
                        ColorUtils.swapAlpha(colAlpha, 255.0F * alphaPC)
                     );
                  }
               }

               alphaW /= 2.0F;
               alphaH /= 2.0F;
            }

            if (this.isInRenderZone(alphaY - 10.0F, alphaH)) {
               float alphaXCursor = alphaX + this.offsetX3s;
               float alphaYCursor = alphaY + alphaH / 2.0F;
               RenderUtils.drawAlphedRect(
                  (double)(alphaXCursor - 1.5F),
                  (double)(alphaYCursor - 2.5F),
                  (double)(alphaXCursor + 1.5F),
                  (double)(alphaYCursor + 2.5F),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC)
               );
               RenderUtils.drawAlphedRect(
                  (double)(alphaXCursor - 1.0F),
                  (double)(alphaYCursor - 2.0F),
                  (double)(alphaXCursor + 1.0F),
                  (double)(alphaYCursor + 2.0F),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );
            }

            if (this.isInRenderZone(draggYgr - 1.5F - 16.0F, 3.0F)) {
               RenderUtils.drawAlphedRect(
                  (double)(draggXgr - 1.5F),
                  (double)(draggYgr - 1.5F),
                  (double)(draggXgr + 1.5F),
                  (double)(draggYgr + 1.5F),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC)
               );
               RenderUtils.drawAlphedRect(
                  (double)(draggXgr - 1.0F),
                  (double)(draggYgr - 1.0F),
                  (double)(draggXgr + 1.0F),
                  (double)(draggYgr + 1.0F),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );
            }

            if (this.isInRenderZone(hsbY - 10.0F, hsbH)) {
               float hsbXCursor = hsbX + this.offsetX2s;
               float hsbYCursor = hsbY + hsbH / 2.0F;
               RenderUtils.drawAlphedRect(
                  (double)(hsbXCursor - 1.5F),
                  (double)(hsbYCursor - 2.5F),
                  (double)(hsbXCursor + 1.5F),
                  (double)(hsbYCursor + 2.5F),
                  ColorUtils.swapAlpha(ColorUtils.getColor(0, 0, 0), 255.0F * alphaPC)
               );
               RenderUtils.drawAlphedRect(
                  (double)(hsbXCursor - 1.0F),
                  (double)(hsbYCursor - 2.0F),
                  (double)(hsbXCursor + 1.0F),
                  (double)(hsbYCursor + 2.0F),
                  ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC)
               );
            }

            if (this.dragginghsbs) {
               this.dragginggrs = false;
               this.draggingalphas = false;
               this.offsetX2s = MathUtils.clamp((float)mouseX - hsbX, 0.0F, hsbW);
               this.updateSliderSounds();
            } else if (ColorUtils.getHueFromColor(setting.color) != 0 && !this.dragginggrs) {
               this.offsetX2s = (float)ColorUtils.getHueFromColor(setting.color) / 360.0F * hsbW;
            } else if (this.offsetX2s == -1.2312312E8F) {
               this.offsetX2s = 0.0F;
            }

            if (this.draggingalphas) {
               this.dragginggrs = false;
               this.dragginghsbs = false;
               this.offsetX3s = MathUtils.clamp((float)mouseX - alphaX, 0.0F, alphaW);
               this.updateSliderSounds();
            } else {
               this.offsetX3s = (float)ColorUtils.getAlphaFromColor(setting.color) / 255.0F * alphaW;
            }

            int col = Color.getHSBColor(MathUtils.clamp(percXhsb, 0.0F, 0.999F), percXgr, 1.0F - percYgr).getRGB();
            col = ColorUtils.swapAlpha(col, 255.0F * percXalpha);
            if (col != 0 && Mouse.isButtonDown(0)) {
               setting.color = col;
            }
         }
      }

      public void drawColorPresets(float x, float y, float w, float h, ScaledResolution sr, float alphaPC) {
         RenderUtils.drawAlphedRect((double)x, (double)(y + 15.0F), (double)(x + w), (double)(y + h), ColorUtils.getColor(0, 0, 0, 60.0F * alphaPC));
         RenderUtils.drawLightContureRect((double)x, (double)y, (double)(x + w), (double)(y + h), ColorUtils.getColor(255, 255, 255, 60.0F * alphaPC));
         if (this.isInRenderZone(y, 15.0F)) {
            RenderUtils.drawAlphedRect((double)x, (double)y, (double)(x + w), (double)(y + 15.0F), ColorUtils.getColor(0, 0, 0, 90.0F * alphaPC));
         }

         int czC1 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(180)), 55.0F * alphaPC);
         int czC2 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2()), 55.0F * alphaPC);
         int czC3 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor2(180)), 25.0F * alphaPC);
         int czC4 = ColorUtils.swapAlpha(ColorUtils.getOverallColorFrom(-1, ClientColors.getColor1(0)), 25.0F * alphaPC);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            x, y, x + w, y + h, 1.0F, 9.0F, czC1, czC2, czC3, czC4, true, false, true
         );
         if (255.0F * alphaPC >= 33.0F && this.isInRenderZone(y, 15.0F)) {
            Fonts.mntsb_15
               .drawString("Presets", (double)(x + 14.0F), (double)(y + 6.0F), ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC));
            Fonts.stylesicons_20
               .drawString("M", (double)(x + 3.0F), (double)(y + 6.5F), ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * alphaPC));
         }

         CFontRenderer font = Fonts.noise_15;
         float yStep = 15.0F;

         for (ClientColors.PresetColors colors : ClientColors.PresetColors.values()) {
            if (this.isInRenderZone(y + yStep, 20.0F)) {
               int bgElementCol = colors.equals(ClientColors.getCurrentPreset())
                  ? ColorUtils.swapAlpha(
                     ColorUtils.getOverallColorFrom(
                        ColorUtils.getOverallColorFrom(ClientColors.getColor1(), ClientColors.getColor2()),
                        -1,
                        ClientColors.presetStepAnimGetAnim == 0.0F ? 1.0F : ClientColors.presetStepAnimGetAnim
                     ),
                     (30.0F + (ClientColors.presetStepAnimGetAnim == 0.0F ? 0.0F : 1.0F - ClientColors.presetStepAnimGetAnim) * 225.0F) * alphaPC
                  )
                  : 0;
               RenderUtils.drawAlphedRect(
                  (double)(x + 1.0F), (double)(y + yStep + 1.0F), (double)(x + w - 1.0F), (double)(y + yStep + 20.0F - 1.0F), bgElementCol
               );
               if (colors.equals(ClientColors.getCurrentPreset())) {
                  RenderUtils.drawAlphedSideways(
                     (double)(x + 3.0F), (double)(y + yStep + 1.0F), (double)(x + w / 2.0F), (double)(y + yStep + 20.0F - 1.0F), bgElementCol, 0, true
                  );
                  RenderUtils.drawAlphedSideways(
                     (double)(x + 1.0F), (double)(y + yStep + 1.0F), (double)(x + 3.0F), (double)(y + yStep + 20.0F - 1.0F), 0, bgElementCol, true
                  );
               }

               if (colors.equals(ClientColors.getCurrentPreset()) && ClientColors.presetStepAnimGetAnim != 0.0F) {
                  float changePC = ClientColors.presetStepAnimGetAnim;
                  changePC = (double)changePC > 0.5 ? 1.0F - changePC : changePC;
                  changePC *= 2.0F;
                  int waveC = ColorUtils.swapAlpha(-1, changePC * 255.0F * alphaPC);
                  if (ColorUtils.getAlphaFromColor(waveC) > 26) {
                     RenderUtils.drawAlphedSideways(
                        (double)(x + 1.0F),
                        (double)(y + yStep + 2.0F),
                        (double)(x + (w - 1.0F) * ClientColors.presetStepAnimGetAnim),
                        (double)(y + yStep + 20.0F - 2.0F),
                        0,
                        waveC,
                        true
                     );
                     RenderUtils.drawAlphedSideways(
                        (double)(x + (w - 1.0F) * ClientColors.presetStepAnimGetAnim),
                        (double)(y + yStep + 2.0F),
                        (double)(x + w - 1.0F),
                        (double)(y + yStep + 20.0F - 2.0F),
                        waveC,
                        0,
                        true
                     );
                     RenderUtils.drawAlphedRect(
                        (double)(x + (w - 1.0F) - (w - 1.0F) * ClientColors.presetStepAnimGetAnim),
                        (double)(y + yStep + 1.0F),
                        (double)(x + w - 1.0F),
                        (double)(y + yStep + 2.0F),
                        waveC
                     );
                     RenderUtils.drawAlphedRect(
                        (double)(x + (w - 1.0F) - (w - 1.0F) * ClientColors.presetStepAnimGetAnim),
                        (double)(y + yStep + 20.0F - 2.0F),
                        (double)(x + w - 1.0F),
                        (double)(y + yStep + 20.0F - 1.0F),
                        waveC
                     );
                     RenderUtils.drawAlphedRect(
                        (double)(x + 1.0F),
                        (double)(y + yStep + 1.0F),
                        (double)(x + 2.0F),
                        (double)(y + yStep + 20.0F * ClientColors.presetStepAnimGetAnim - 1.0F),
                        waveC
                     );
                     RenderUtils.drawAlphedRect(
                        (double)(x + (w - 2.0F)),
                        (double)(y + yStep + 1.0F + 20.0F - 20.0F * ClientColors.presetStepAnimGetAnim),
                        (double)(x + (w - 1.0F)),
                        (double)(y + yStep + 20.0F - 1.0F),
                        waveC
                     );
                     GL11.glPushMatrix();
                     GL11.glTranslated((double)(2.0F * changePC), 0.0, 0.0);
                     changePC = MathUtils.clamp(ClientColors.presetStepAnimGetAnim, 0.0F, 0.5F) * 2.0F;
                     changePC = (double)changePC > 0.5 ? 1.0F - changePC : changePC;
                     changePC *= 2.0F;
                     RenderUtils.customRotatedObject2D(x + 6.0F, y + yStep, (float)font.getStringWidth(colors.name), 20.0F, (double)(changePC * 20.0F));
                  }
               }

               int i = 0;

               for (char c : colors.name.toCharArray()) {
                  int textColor = ColorUtils.getOverallColorFrom(
                     ColorUtils.getFixedWhiteColor(),
                     ColorUtils.getOverallColorFrom(colors.color1, colors.color2, (float)i / (float)font.getStringWidth(colors.name)),
                     0.6F
                  );
                  if (255.0F * alphaPC >= 33.0F) {
                     font.drawStringWithOutline(
                        String.valueOf(c), (double)(x + 6.0F + (float)i), (double)(y + yStep + 8.0F), ColorUtils.swapAlpha(textColor, 255.0F * alphaPC)
                     );
                  }

                  i += font.getStringWidth(String.valueOf(c));
               }

               if (colors.equals(ClientColors.getCurrentPreset()) && ClientColors.presetStepAnimGetAnim != 0.0F) {
                  GL11.glPopMatrix();
               }

               float ocX = w - 35.0F;
               float tcX = w - 20.0F;
               float otcSize = 10.0F;
               float otxExtY = 5.0F;
               int c1 = ColorUtils.swapAlpha(colors.color1, 255.0F * alphaPC);
               int c2 = ColorUtils.swapAlpha(colors.color2, 255.0F * alphaPC);
               int cBGC = ColorUtils.getColor(0, 0, 0, 120.0F * alphaPC);
               RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                  x + ocX - 1.0F,
                  y + yStep + otxExtY - 1.0F,
                  x + tcX + otcSize + 1.0F,
                  y + yStep + otcSize + otxExtY + 1.0F,
                  3.0F,
                  2.0F,
                  cBGC,
                  cBGC,
                  cBGC,
                  cBGC,
                  false,
                  true,
                  true
               );
               if (c1 == c2) {
                  boolean scalling = colors.equals(ClientColors.getCurrentPreset()) && ClientColors.presetStepAnimGetAnim != 0.0F;
                  float scale = 0.0F;
                  if (scalling) {
                     scale = ClientColors.presetStepAnimGetAnim;
                     scale = (double)scale > 0.5 ? 1.0F - scale : scale;
                     scale *= 2.0F;
                     scale = 1.0F - 0.25F * scale;
                  }

                  if (scale != 0.0F) {
                     GL11.glPushMatrix();
                     RenderUtils.customScaledObject2D(x + ocX, y + yStep + otxExtY, tcX - ocX + otcSize, otcSize, scale);
                  }

                  RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                     x + ocX,
                     y + yStep + otxExtY,
                     x + tcX + otcSize,
                     y + yStep + otcSize + otxExtY,
                     2.0F,
                     8.0F,
                     ColorUtils.swapAlpha(c1, 65.0F * alphaPC),
                     ColorUtils.swapAlpha(c2, 85.0F * alphaPC),
                     ColorUtils.swapAlpha(c2, 85.0F * alphaPC),
                     ColorUtils.swapAlpha(c1, 65.0F * alphaPC),
                     true,
                     false,
                     true
                  );
                  RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                     x + ocX, y + yStep + otxExtY, x + tcX + otcSize, y + yStep + otcSize + otxExtY, 2.0F, 1.0F, c1, c2, c2, c1, false, true, true
                  );
                  if (scale != 0.0F) {
                     GL11.glPopMatrix();
                  }
               } else {
                  boolean rotate = colors.equals(ClientColors.getCurrentPreset()) && ClientColors.presetStepAnimGetAnim != 0.0F;
                  float rot = 0.0F;
                  if (rotate) {
                     GL11.glPushMatrix();
                     rot = ClientColors.presetStepAnimGetAnim;
                     rot *= 180.0F;
                  }

                  if (rot != 0.0F) {
                     RenderUtils.customRotatedObject2D(x + ocX, y + yStep + otxExtY, otcSize, otcSize, (double)rot);
                  }

                  RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                     x + ocX,
                     y + yStep + otxExtY,
                     x + ocX + otcSize,
                     y + yStep + otcSize + otxExtY,
                     2.0F,
                     8.0F,
                     ColorUtils.swapAlpha(c1, 65.0F * alphaPC),
                     ColorUtils.swapAlpha(c1, 75.0F * alphaPC),
                     ColorUtils.swapAlpha(c1, 75.0F * alphaPC),
                     ColorUtils.swapAlpha(c1, 65.0F * alphaPC),
                     true,
                     false,
                     true
                  );
                  RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                     x + ocX, y + yStep + otxExtY, x + ocX + otcSize, y + yStep + otcSize + otxExtY, 2.0F, 1.0F, c1, c1, c1, c1, false, true, true
                  );
                  if (rot != 0.0F) {
                     RenderUtils.customRotatedObject2D(x + ocX, y + yStep + otxExtY, otcSize, otcSize, (double)(-rot));
                  }

                  if (rot != 0.0F) {
                     RenderUtils.customRotatedObject2D(x + tcX, y + yStep + otxExtY, otcSize, otcSize, (double)rot);
                  }

                  RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                     x + tcX,
                     y + yStep + otxExtY,
                     x + tcX + otcSize,
                     y + yStep + otcSize + otxExtY,
                     2.0F,
                     8.0F,
                     ColorUtils.swapAlpha(c2, 75.0F * alphaPC),
                     ColorUtils.swapAlpha(c2, 85.0F * alphaPC),
                     ColorUtils.swapAlpha(c2, 85.0F * alphaPC),
                     ColorUtils.swapAlpha(c2, 75.0F * alphaPC),
                     true,
                     false,
                     true
                  );
                  RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
                     x + tcX, y + yStep + otxExtY, x + tcX + otcSize, y + yStep + otcSize + otxExtY, 2.0F, 1.0F, c2, c2, c2, c2, false, true, true
                  );
                  if (rotate) {
                     GL11.glPopMatrix();
                  }
               }
            }

            yStep += 20.0F;
         }
      }

      public float getPresetsHeight() {
         int height = 15;

         for (ClientColors.PresetColors colors : ClientColors.PresetColors.values()) {
            height += 20;
         }

         return ClientColors.get.Mode.currentMode.equalsIgnoreCase("Presets") ? (float)height : 0.0F;
      }

      public boolean pickerIsDouble() {
         String currentMode = ClientColors.get.Mode.currentMode;
         return currentMode.equalsIgnoreCase("TwoColored") || currentMode.equalsIgnoreCase("Fade");
      }

      public float getPickerHeight() {
         return 98.0F;
      }

      public boolean isHovered() {
         return this.hover;
      }

      public void updateHover(int mouseX, int mouseY) {
         this.hover = this.isHover(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY);
      }

      public boolean hasSettings() {
         String mode = ClientColors.get.Mode.currentMode;
         return mode.equalsIgnoreCase("Presets") || mode.equalsIgnoreCase("Colored") || mode.equalsIgnoreCase("TwoColored") || mode.equalsIgnoreCase("Fade");
      }

      public float getTotalElementsHeight() {
         String mode = ClientColors.get.Mode.currentMode;
         boolean hasPicker = mode.equalsIgnoreCase("Colored") || mode.equalsIgnoreCase("TwoColored") || mode.equalsIgnoreCase("Fade");
         boolean hasPresets = mode.equalsIgnoreCase("Presets");
         boolean hasPickerDouble = hasPicker && this.pickerIsDouble();
         float getPickerHeight = hasPicker ? (hasPickerDouble ? 22.0F + this.getPickerHeight() * 2.0F : 12.0F + this.getPickerHeight()) : 0.0F;
         float getPresetsHeight = hasPresets ? 11.0F + this.getPresetsHeight() : 0.0F;
         float getModesHeight = this.getModesHeight();
         return getModesHeight + getPresetsHeight + getPickerHeight;
      }

      public void drawScreen(int mouseX, int mouseY, float partialTicks) {
         this.updateHover(mouseX, mouseY);
         if (ClientColors.modeStepAnim.to == 1.0F) {
            ClientColors.modeStepAnimGetAnim = ClientColors.modeStepAnim.getAnim();
            if ((double)ClientColors.modeStepAnim.getAnim() > 0.99) {
               ClientColors.modeStepAnim.setAnim(0.0F);
               ClientColors.modeStepAnim.to = 0.0F;
            }
         } else {
            ClientColors.modeStepAnimGetAnim = 0.0F;
         }

         ClientColors.presetStepAnim.speed = 0.03F;
         if (ClientColors.prevPreset != null && ClientColors.presetStepAnim.to == 1.0F) {
            ClientColors.presetStepAnimGetAnim = ClientColors.presetStepAnim.getAnim();
            if ((double)ClientColors.presetStepAnim.getAnim() > 0.99) {
               ClientColors.presetStepAnim.setAnim(0.0F);
               ClientColors.presetStepAnim.to = 0.0F;
            }
         } else {
            ClientColors.presetStepAnimGetAnim = 0.0F;
         }

         GlStateManager.disableDepth();
         this.updatePosXY();
         this.heightCalculate();
         this.dragUpdate(mouseX, mouseY);
         float alphaPC = ClickGuiScreen.globalAlpha.anim / 255.0F;
         alphaPC *= 1.025F;
         if (alphaPC > 1.0F) {
            alphaPC = 1.0F;
         }

         alphaPC = (float)MathUtils.easeInOutQuad((double)alphaPC);
         GL11.glPushMatrix();
         RenderUtils.customScaledObject2D(this.getX(), this.getY(), this.getWidth(), this.getHeight(), ClickGuiScreen.scale.anim * alphaPC);
         RenderUtils.customRotatedObject2D(this.getX(), this.getY(), this.dragX * 2.0F, this.dragY * 2.0F, (double)this.getRotate());
         this.drawPanel(
            mouseX, mouseY, partialTicks, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.open, new ScaledResolution(Module.mc), alphaPC
         );
         GL11.glPopMatrix();
         GlStateManager.disableDepth();
         if (this.isHover(this.getX() - 5.0F, this.getY() - 5.0F, this.getWidth() + 10.0F, this.getHeight() + 10.0F, mouseX, mouseY)) {
            ClickGuiScreen.resetHolds();
         }
      }

      public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
         float yPos = this.getY();
         float xPos = this.getX();
         if (this.isHover(xPos, yPos, this.getWidth(), 20.0F, mouseX, mouseY)) {
            if (mouseButton == 0) {
               this.dragging = true;
            } else if (mouseButton == 1) {
               this.open = !this.open;
               ClientTune.get.playGuiColorsScreenOpenOrCloseSong(this.open);
            }
         }

         if (!(this.getHeight() < 30.0F)) {
            yPos -= this.getScrollY();
            if (this.isHover(this.getX(), this.getY() + 20.0F, this.getWidth(), this.getHeight(), mouseX, mouseY)) {
               if (mouseButton == 0 && this.isHover(xPos + 5.0F, yPos + 35.0F, this.getModesWidth(), this.getModesHeight() - 10.0F, mouseX, mouseY)) {
                  int curMode = -1;
                  float height = 0.0F;
                  ModeSettings setting = (ModeSettings)ClientColors.get.settings.get(2);
                  if (setting.category == Settings.Category.String_Massive && this.open) {
                     for (String mode : setting.modes) {
                        height += 10.0F;
                        if (this.isHover(xPos + 5.0F, yPos + 25.0F + height, this.getModesWidth() - 10.0F, 10.0F, mouseX, mouseY)) {
                           curMode = (int)(height / 10.0F - 1.0F);
                        }
                     }
                  }

                  try {
                     ClickGuiScreen.scrollAnimation.to = 0.0F;
                     ClickGuiScreen.dWhell = 0.0F;
                     if (setting.currentMode != setting.modes[curMode] && curMode != -1) {
                        ClientColors.prevMode = setting.currentMode;
                        setting.currentMode = setting.modes[curMode];
                        ClientTune.get.playGuiClientcolorsChangeModeSong();
                        ClientColors.modeStepAnim.setAnim(0.0F);
                        ClientColors.modeStepAnim.to = 1.0F;
                        ClientColors.modeStepAnimGetAnim = 0.001F;
                     }
                  } catch (Exception var21) {
                     var21.printStackTrace();
                  }
               }

               if (ClientColors.get.Mode.currentMode.equalsIgnoreCase("Presets")
                  && mouseButton == 0
                  && this.isHover(
                     xPos + 5.0F,
                     yPos + 35.0F + this.getModesHeight() - 10.0F + 11.0F + 15.0F,
                     this.getPresetsWidth(),
                     this.getPresetsHeight() - 15.0F,
                     mouseX,
                     mouseY
                  )) {
                  int curMode = -1;
                  float height = 0.0F;
                  ModeSettings setting = (ModeSettings)ClientColors.get.settings.get(3);
                  if (setting.category == Settings.Category.String_Massive && this.open) {
                     for (String modex : setting.modes) {
                        height += 20.0F;
                        if (this.isHover(
                           xPos + 5.0F,
                           yPos + 15.0F + this.getModesHeight() - 10.0F + 11.0F + 15.0F + height,
                           this.getModesWidth() - 15.0F,
                           20.0F,
                           mouseX,
                           mouseY
                        )) {
                           curMode = (int)(height / 20.0F - 1.0F);
                        }
                     }
                  }

                  try {
                     if (setting.currentMode != setting.modes[curMode] && curMode != -1) {
                        ClientColors.prevPreset = ClientColors.getCurrentPreset();
                        setting.currentMode = setting.modes[curMode];
                        ClientTune.get.playGuiClientcolorsChangePresetSong();
                        ClientColors.presetStepAnim.setAnim(0.0F);
                        ClientColors.presetStepAnim.to = 1.0F;
                        ClientColors.presetStepAnimGetAnim = 0.01F;
                     }
                  } catch (Exception var20) {
                     var20.printStackTrace();
                  }
               }
            }

            if ((
                  ClientColors.get.Mode.currentMode.equalsIgnoreCase("Colored")
                     || ClientColors.get.Mode.currentMode.equalsIgnoreCase("TwoColored")
                     || ClientColors.get.Mode.currentMode.equalsIgnoreCase("Fade")
               )
               && mouseButton == 0
               && this.isHover(
                  xPos + 5.0F,
                  yPos + 35.0F + this.getModesHeight() - 10.0F + 11.0F + 15.0F,
                  this.getPickerWidth(),
                  this.getPickerHeight() - 15.0F,
                  mouseX,
                  mouseY
               )) {
               float posX = xPos + 5.0F;
               float posY = yPos + 35.0F + this.getModesHeight() - 10.0F + 11.0F;
               float hsbX = posX + 6.0F;
               float hsbY = posY + 68.0F;
               float hsbW = this.getPickerWidth() - 12.0F;
               float hsbH = 6.0F;
               float alphaX = posX + 6.0F;
               float alphaY = posY + 86.0F;
               float alphaW = this.getPickerWidth() - 12.0F;
               float alphaH = 6.0F;
               float grX = posX + 5.0F;
               float grY = posY + 20.0F;
               float grH = 35.0F;
               float grW = this.getPickerWidth() - 10.0F;
               if (mouseButton == 0 && this.isHover(grX, grY, grW, grH, mouseX, mouseY)) {
                  this.dragginggr = true;
               }

               if (mouseButton == 0 && this.isHover(hsbX, hsbY, hsbW, hsbH, mouseX, mouseY)) {
                  this.dragginghsb = true;
               }

               if (mouseButton == 0 && this.isHover(alphaX, alphaY, alphaW, alphaH, mouseX, mouseY)) {
                  this.draggingalpha = true;
               }
            }

            if ((ClientColors.get.Mode.currentMode.equalsIgnoreCase("TwoColored") || ClientColors.get.Mode.currentMode.equalsIgnoreCase("Fade"))
               && mouseButton == 0
               && this.isHover(
                  this.x + 5.0F,
                  yPos + 35.0F + this.getModesHeight() - 10.0F + 11.0F + 15.0F + 11.0F + this.getPickerHeight(),
                  this.getPickerWidth(),
                  this.getPickerHeight() - 15.0F,
                  mouseX,
                  mouseY
               )) {
               float posXx = xPos + 5.0F;
               float posYx = yPos + 35.0F + this.getModesHeight() - 10.0F + 11.0F + 11.0F + this.getPickerHeight();
               float hsbXx = posXx + 6.0F;
               float hsbYx = posYx + 68.0F;
               float hsbWx = this.getPickerWidth() - 12.0F;
               float hsbHx = 6.0F;
               float alphaXx = posXx + 6.0F;
               float alphaYx = posYx + 86.0F;
               float alphaWx = this.getPickerWidth() - 12.0F;
               float alphaHx = 6.0F;
               float grXx = posXx + 5.0F;
               float grYx = posYx + 20.0F;
               float grHx = 35.0F;
               float grWx = this.getPickerWidth() - 10.0F;
               if (mouseButton == 0 && this.isHover(grXx, grYx, grWx, grHx, mouseX, mouseY)) {
                  this.dragginggrs = true;
               }

               if (mouseButton == 0 && this.isHover(hsbXx, hsbYx, hsbWx, hsbHx, mouseX, mouseY)) {
                  this.dragginghsbs = true;
               }

               if (mouseButton == 0 && this.isHover(alphaXx, alphaYx, alphaWx, alphaHx, mouseX, mouseY)) {
                  this.draggingalphas = true;
               }
            }
         }
      }

      public float getModesWidth() {
         return this.getWidth() - 10.0F;
      }

      public float getPresetsWidth() {
         return this.getWidth() - 10.0F;
      }

      public float getPickerWidth() {
         return this.getWidth() - 10.0F;
      }

      public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
         if (mouseButton == 0) {
            this.dragginggr = false;
            this.dragginghsb = false;
            this.draggingalpha = false;
            this.dragginggrs = false;
            this.dragginghsbs = false;
            this.draggingalphas = false;
            this.dragging = false;
         }
      }

      public void keyTyped(char typedChar, int keyCode) {
      }

      public void initGui() {
         this.hover = false;
      }

      public void onGuiClosed() {
         this.hover = false;
         this.draggingalpha = false;
         this.dragginggr = false;
         this.dragginghsb = false;
         this.draggingalphas = false;
         this.dragginggrs = false;
         this.dragginghsbs = false;
         this.dragging = false;
      }

      public ClientColorsHud() {
         this.xRepos = new AnimationUtils(this.x, this.x, 0.15F);
         this.yRepos = new AnimationUtils(this.y, this.y, 0.15F);
         this.rotate = new AnimationUtils(0.0F, 0.0F, 0.125F);
         this.dragging = false;
         this.dragX = 0.0F;
         this.dragY = 0.0F;
         this.open = false;
         this.soundTicker = new TimerHelper();
         this.offsetX = -1.2312312E8F;
         this.offsetY = -1.2312312E8F;
         this.offsetX2 = -1.2312312E8F;
         this.offsetX3 = -1.2312312E8F;
         this.dragginggr = false;
         this.dragginghsb = false;
         this.draggingalpha = false;
         this.offsetXs = -1.2312312E8F;
         this.offsetYs = -1.2312312E8F;
         this.offsetX2s = -1.2312312E8F;
         this.offsetX3s = -1.2312312E8F;
         this.dragginggrs = false;
         this.dragginghsbs = false;
         this.draggingalphas = false;
      }
   }

   static enum PresetColors {
      WHITE(-1, -1, false, "White"),
      PINK(ColorUtils.getColor(255, 115, 220), ColorUtils.getColor(255, 0, 190), true, "Pink"),
      WINTER(ColorUtils.getColor(165, 235, 255), ColorUtils.getColor(240, 155, 255), true, "Winter"),
      BLOODY(ColorUtils.getColor(255, 0, 0), ColorUtils.getColor(135, 0, 0), true, "Bloody"),
      HERO(ColorUtils.getColor(255, 0, 0), ColorUtils.getColor(0, 255, 0), true, "Hero"),
      EARLBLUE(ColorUtils.getColor(50, 100, 50), ColorUtils.getColor(0, 0, 255), true, "EarlBlue"),
      FLAME(ColorUtils.getColor(255, 95, 0), ColorUtils.getColor(255, 195, 0), true, "Flame"),
      NEON(ColorUtils.getColor(160, 100, 255), ColorUtils.getColor(75, 60, 185), true, "Neon"),
      STELL(ColorUtils.getColor(118, 114, 232), ColorUtils.getColor(59, 59, 122), true, "Stell"),
      GRAY(ColorUtils.getColor(90, 92, 112), ColorUtils.getColor(90, 92, 112), false, "Gray"),
      NIGHTFALL(ColorUtils.getColor(115, 0, 255), ColorUtils.getColor(255, 80, 0), true, "NightFall"),
      BROWN(ColorUtils.getColor(87, 43, 0), ColorUtils.getColor(87, 43, 0), false, "Brown"),
      BREATH(ColorUtils.getColor(120, 255, 227), ColorUtils.getColor(0, 186, 255), true, "Breath"),
      OCEAN(ColorUtils.getColor(251, 255, 38), ColorUtils.getColor(95, 255, 38), true, "Ocean"),
      BLUESKY(ColorUtils.getColor(60, 205, 255), ColorUtils.getColor(60, 205, 255), false, "BlueSky"),
      CANDY(ColorUtils.getColor(255, 117, 252), ColorUtils.getColor(255, 45, 45), true, "Candy"),
      CURRANT(ColorUtils.getColor(0, 40, 255), ColorUtils.getColor(255, 0, 70), true, "Currant"),
      AZURE(ColorUtils.getColor(100, 255, 100), ColorUtils.getColor(100, 255, 100), false, "Azure"),
      DAWN(ColorUtils.getColor(255, 80, 0), ColorUtils.getColor(31, 37, 111), true, "Dawn"),
      SWAMP(ColorUtils.getColor(77, 142, 132), ColorUtils.getColor(0, 235, 255), true, "Swamp");

      boolean twoColored;
      int color1;
      int color2;
      String name;

      private PresetColors(int color1, int color2, boolean twoColored, String name) {
         this.color1 = color1;
         this.color2 = color2;
         this.twoColored = twoColored;
         this.name = name;
      }
   }
}
