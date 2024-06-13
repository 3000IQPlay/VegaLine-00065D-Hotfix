package ru.govno.client.module.modules;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class CommandGui extends Module {
   static AnimationUtils alphaPC = new AnimationUtils(0.0F, 0.0F, 0.125F);
   public static CommandGui get;
   public FloatSettings WX;
   public FloatSettings WY;
   static int mouseX;
   static int mouseY;
   static int mouseButton;
   static boolean clicked = false;
   static String toAdd;
   static String toSend = null;
   static String selectedName = null;
   static AnimationUtils scrollNames = new AnimationUtils(0.0F, 0.0F, 0.16F);
   static CommandGui.CommandType selectedType = null;

   public CommandGui() {
      super("CommandGui", 0, Module.Category.MISC);
      this.settings.add(this.WX = new FloatSettings("WX", 0.01F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.WY = new FloatSettings("WY", 0.3F, 1.0F, 0.0F, this, () -> false));
      get = this;
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      alphaPC.to = this.actived && mc.currentScreen instanceof GuiChat ? 1.0F : 0.0F;
      if (canDrawWindow()) {
         drawWindow();
         updateAdds();
         updateName();
         float h = 70.0F;
         float size = (float)(tabList(true).size() * 10);
         if (size > 70.0F) {
            scrollNames.to = MathUtils.clamp(scrollNames.to, 0.0F, size - h);
            scrollNames.setAnim(MathUtils.clamp(scrollNames.anim, 0.0F, size - h));
         } else {
            scrollNames.to = 0.0F;
         }
      }
   }

   public static float getAlphaPC() {
      return alphaPC.getAnim();
   }

   public static boolean canDrawWindow() {
      return (double)getAlphaPC() > 0.03;
   }

   public static float[] getWindowCoord() {
      float x = 0.0F;
      float y = 0.0F;
      if (get != null) {
         ScaledResolution sr = new ScaledResolution(mc);
         x = get.currentFloatValue("WX") * (float)sr.getScaledWidth();
         y = get.currentFloatValue("WY") * (float)sr.getScaledHeight();
      }

      return new float[]{x, y};
   }

   public static float getWindowWidth() {
      return 152.0F;
   }

   public static float getWindowHeight() {
      return 100.0F;
   }

   static List<String> tabList(boolean onlyName) {
      List<String> list = new CopyOnWriteArrayList<>();

      for (NetworkPlayerInfo player : Minecraft.player.connection.getPlayerInfoMap()) {
         if (player.getGameProfile() != null) {
            String text = onlyName
               ? player.getGameProfile().getName()
               : (player.getDisplayName() == null ? player.getGameProfile().getName() : player.getDisplayName().getUnformattedText());
            text = text.replace("  ", " ").replace("§l", "").replace("[]", "").replace("§k", "").replace("§m", "").replace("§n", "").replace("§o", "");
            text = text.replace("Ａ", "A");
            text = text.replace("Ｂ", "B");
            text = text.replace("Ｃ", "C");
            text = text.replace("Ｄ", "D");
            text = text.replace("Ｅ", "E");
            text = text.replace("Ｆ", "F");
            text = text.replace("Ｇ", "G");
            text = text.replace("Ｈ", "H");
            text = text.replace("Ｉ", "I");
            text = text.replace("Ｊ", "J");
            text = text.replace("Ｋ", "K");
            text = text.replace("Ｌ", "L");
            text = text.replace("Ｍ", "M");
            text = text.replace("Ｎ", "N");
            text = text.replace("Ｏ", "O");
            text = text.replace("Ｐ", "P");
            text = text.replace("Ｑ", "Q");
            text = text.replace("Ｒ", "R");
            text = text.replace("Ｓ", "S");
            text = text.replace("Ｔ", "T");
            text = text.replace("Ｕ", "U");
            text = text.replace("Ｖ", "V");
            text = text.replace("Ｗ", "W");
            text = text.replace("Ｘ", "X");
            text = text.replace("Ｙ", "Y");
            text = text.replace("Ｚ", "Z");
            text = text.replace("▷", ">");
            text = text.replace("◁", "<");
            list.add(text);
         }
      }

      return list;
   }

   static void updateName() {
      boolean hasName = false;

      for (String name : tabList(true)) {
         if (selectedName != null && name.contains(selectedName) || name.equalsIgnoreCase(selectedName)) {
            hasName = true;
            break;
         }
      }

      if (!hasName) {
         selectedName = null;
      }
   }

   static void updateAdds() {
      if (toAdd != null) {
         toSend = toSend + toAdd;
         toAdd = null;
      }

      if (toSend != null) {
         if (toSend.contains("null")) {
            toSend = toSend.replace("null", "");
         }
      } else if (selectedType != null) {
         selectedType = null;
      }
   }

   public static void updateMousePos(int mX, int mY) {
      mouseX = mX;
      mouseY = mY;
   }

   public static void callClick(int mX, int mY, int mB) {
      mouseX = mX;
      mouseY = mY;
      mouseButton = mB;
      if (isHoveredToPanel(false) && !isHoveredToPanel(true)) {
         clicked = true;
      }

      onClickMouse();
   }

   public static boolean isHoveredToPanel(boolean onlyMove) {
      return canDrawWindow()
         && RenderUtils.isHovered(
            (float)mouseX, (float)mouseY, getWindowCoord()[0], getWindowCoord()[1], getWindowWidth(), onlyMove ? 15.0F : getWindowHeight()
         );
   }

   public static void callWhell(boolean plus) {
      if (isHoveredToPanel(false)) {
         float h = 70.0F;
         float size = (float)(tabList(true).size() * 10);
         if (size > h) {
            scrollNames.to += plus ? 10.0F : -10.0F;
         } else {
            scrollNames.to = 0.0F;
         }
      }
   }

   static void onClickMouse() {
      if (clicked && mouseButton == 0) {
         float x = getWindowCoord()[0];
         float y = getWindowCoord()[1];
         float w = getWindowWidth();
         float h = getWindowHeight();
         y += 15.0F;
         if ((float)mouseX >= x + w - 33.0F
            && (float)mouseY >= y + h - 27.0F
            && (float)mouseX <= x + w - 4.0F
            && (float)mouseY <= y + h - 17.0F
            && toSend != null
            && !toSend.isEmpty()) {
            Minecraft.player.sendChatMessage(toSend);
            return;
         }

         CommandGui.CommandType type = null;
         float yType = y;
         float xType = x + 4.0F;
         float yNames = y - scrollNames.getAnim();
         float xNames = x + 41.0F;
         if ((float)mouseX >= xType && (float)mouseX <= xType + 35.0F) {
            for (CommandGui.CommandType command : CommandGui.CommandType.values()) {
               if ((float)mouseY >= yType && (float)mouseY <= yType + 10.0F) {
                  type = command;
               }

               yType += 10.0F;
            }
         }

         String netName = null;
         if ((float)mouseX >= xNames && (float)mouseX <= xNames + 105.0F && selectedType != null) {
            for (String name : tabList(true)) {
               if ((float)mouseY >= yNames && (float)mouseY <= yNames + 10.0F && yNames < y + h - 30.0F && yNames > y - 10.0F) {
                  netName = name;
               }

               yNames += 10.0F;
            }
         }

         if (netName == null && type != null) {
            selectedType = type;
         }

         selectedName = netName;
         toSend = (selectedType != null ? selectedType.name : "") + (selectedName != null ? selectedName : "");
         clicked = false;
      }
   }

   public static void drawWindow() {
      if (canDrawWindow()) {
         GL11.glPushMatrix();
         int bgCol = ColorUtils.getColor(0, 0, 0, 160.0F * getAlphaPC());
         float x = getWindowCoord()[0];
         float y = getWindowCoord()[1];
         float w = getWindowWidth();
         float h = getWindowHeight();
         RenderUtils.customScaledObject2DPro(x, y, w, h, 1.0F, Math.min(getAlphaPC() * 1.1F, 1.0F));
         RenderUtils.drawClientHudRect(x, y, x + w, y + h + 2.0F, getAlphaPC(), Hud.get.ManyGlows.getBool());
         if (getAlphaPC() * 255.0F >= 33.0F) {
            Fonts.comfortaaRegular_15
               .drawString(
                  "CommandGui",
                  (double)(x + w / 2.0F - (float)(Fonts.comfortaaRegular_15.getStringWidth("CommandGui") / 2)),
                  (double)(y + 7.0F),
                  ColorUtils.getColor(255, 255, 255, 255.0F * getAlphaPC())
               );
         }

         StencilUtil.initStencilToWrite();
         RenderUtils.drawRect((double)(x + 4.0F), (double)(y + 15.0F), (double)(x + w - 4.0F), (double)(y + h - 14.0F), -1);
         StencilUtil.readStencilBuffer(1);
         y += 15.0F;
         float yType = y;
         float xType = x + 4.0F;
         float yNames = y - scrollNames.getAnim();
         float xNames = x + 41.0F;

         for (CommandGui.CommandType command : CommandGui.CommandType.values()) {
            boolean select = selectedType == command;
            RenderUtils.drawAlphedRect(
               (double)xType,
               (double)yType,
               (double)(xType + 35.0F),
               (double)(yType + 10.0F),
               ColorUtils.getColor(255, 55 + (select ? 0 : 200), 55 + (select ? 0 : 200), (float)(55 + (select ? 60 : 0)) * getAlphaPC())
            );
            if (getAlphaPC() * 255.0F >= 33.0F) {
               Fonts.comfortaaRegular_15
                  .drawStringWithShadow(command.name, (double)(xType + 2.0F), (double)(yType + 3.0F), ColorUtils.getColor(255, 255, 255, 255.0F * getAlphaPC()));
            }

            yType += 10.0F;
         }

         if (selectedType != null) {
            for (String name : tabList(true)) {
               if (yNames < y + h - 30.0F && yNames > y - 10.0F) {
                  boolean select = selectedName != null && name.contains(selectedName);
                  boolean moused = (float)mouseY >= yNames && (float)mouseY <= yNames + 10.0F && (float)mouseX >= xNames && (float)mouseX <= xNames + 105.0F;
                  RenderUtils.drawAlphedRect(
                     (double)xNames,
                     (double)yNames,
                     (double)(xNames + 105.0F),
                     (double)(yNames + 10.0F),
                     ColorUtils.getColor(
                        255,
                        55 + (select ? 0 : 200),
                        55 + (select ? 0 : 200),
                        (float)(55 + (!select && !moused ? 0 : (moused ? (select ? 80 : 50) : 60))) * getAlphaPC()
                     )
                  );
               }

               yNames += 10.0F;
            }

            yNames = y - scrollNames.getAnim();

            for (String name : tabList(false)) {
               if (yNames < y + h - 30.0F && yNames > y - 10.0F) {
                  if (selectedName != null && name.contains(selectedName)) {
                     boolean var26 = true;
                  } else {
                     boolean var10000 = false;
                  }

                  if (getAlphaPC() * 255.0F >= 33.0F) {
                     Fonts.comfortaaRegular_15
                        .drawStringWithShadow(name, (double)(xNames + 2.0F), (double)(yNames + 3.0F), ColorUtils.getColor(255, 255, 255, 255.0F * getAlphaPC()));
                  }
               }

               yNames += 10.0F;
            }
         }

         StencilUtil.uninitStencilBuffer();
         if (yNames > y + 10.0F) {
            float hdc = 10.181818F;
            float scrollPC = scrollNames.getAnim() / hdc / (float)tabList(true).size() / hdc * 7.0F;
            float scrollPutHC = 10.0F * ((float)tabList(true).size() / hdc);
            float scrollY1 = 0.5F + scrollPC * 69.0F;
            float scrollY2 = scrollY1 + 10.0F;
            RenderUtils.drawRect(
               (double)(x + w - 4.0F),
               (double)(y + scrollY1),
               (double)(x + w - 3.0F),
               (double)(y + scrollY2),
               ColorUtils.getColor(255, 255, 255, 255.0F * getAlphaPC())
            );
            RenderUtils.drawLightContureRect(
               (double)(x + w - 4.0F),
               (double)(y + 0.5F),
               (double)(x + w - 3.0F),
               (double)(y + 69.5F),
               ColorUtils.getColor(255, 255, 255, 110.0F * getAlphaPC())
            );
         }

         StencilUtil.initStencilToWrite();
         RenderUtils.drawAlphedRect((double)(x + 4.0F), (double)(y + h - 15.0F - 12.0F), (double)(x + w - 4.0F - 30.0F), (double)(y + h - 15.0F - 2.0F), -1);
         StencilUtil.readStencilBuffer(1);
         RenderUtils.drawAlphedRect(
            (double)(x + 4.0F),
            (double)(y + h - 15.0F - 12.0F),
            (double)(x + w - 4.0F),
            (double)(y + h - 15.0F - 2.0F),
            ColorUtils.getColor(255, 255, 255, 55.0F * getAlphaPC())
         );
         if (toSend != null && getAlphaPC() * 255.0F >= 33.0F) {
            Fonts.comfortaaRegular_15
               .drawStringWithShadow(toSend, (double)(x + 6.0F), (double)(y + h - 15.0F - 9.0F), ColorUtils.getColor(255, 255, 255, 255.0F * getAlphaPC()));
         }

         if (toSend != null && !toSend.isEmpty()) {
            StencilUtil.uninitStencilBuffer();
         }

         if (toSend == null || toSend.isEmpty()) {
            StencilUtil.readStencilBuffer(0);
         }

         RenderUtils.drawAlphedRect(
            (double)(x + w - 33.0F),
            (double)(y + h - 27.0F),
            (double)(x + w - 4.0F),
            (double)(y + h - 17.0F),
            ColorUtils.getColor(155, 255, 155, 135.0F * (toSend != null && !toSend.isEmpty() ? 1.0F : 0.3F) * getAlphaPC())
         );
         if (255.0F * (toSend != null && !toSend.isEmpty() ? 1.0F : 0.3F) * getAlphaPC() >= 26.0F && getAlphaPC() * 255.0F >= 33.0F) {
            Fonts.comfortaaRegular_15
               .drawStringWithShadow(
                  "Send",
                  (double)(x + w - 28.5F),
                  (double)(y + h - 15.0F - 9.0F),
                  ColorUtils.getColor(55, 255, 55, 255.0F * (toSend != null && !toSend.isEmpty() ? 1.0F : 0.3F) * getAlphaPC())
               );
         }

         if (toSend == null || toSend.isEmpty()) {
            StencilUtil.uninitStencilBuffer();
         }
      }

      RenderUtils.resetBlender();
      GlStateManager.enableBlend();
      GL11.glPopMatrix();
   }

   public static enum CommandType {
      DUEL("/duel "),
      TPA("/tpa ");

      String name;

      private CommandType(String name) {
         this.name = name;
      }
   }
}
