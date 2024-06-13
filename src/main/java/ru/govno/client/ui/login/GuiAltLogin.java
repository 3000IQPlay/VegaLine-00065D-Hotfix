package ru.govno.client.ui.login;

import dev.intave.viamcp.ViaMCP;
import java.io.IOException;
import java.security.SecureRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.CTextField;
import ru.govno.client.utils.ClientRP;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;

public class GuiAltLogin extends GuiScreen {
   String oldName = "";
   String newName = "";
   private final GuiScreen previousScreen;
   private AltLoginThread thread;
   private static final String alphabet = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
   private final long initTime = System.currentTimeMillis();
   private static final SecureRandom secureRandom = new SecureRandom();
   static CTextField textFieldName = new CTextField(0, Fonts.mntsb_20, 0, 0, 0, 0);
   static CTextField textFieldPass = new CTextField(1, Fonts.mntsb_20, 1, 1, 1, 1);
   AnimationUtils alphaName = new AnimationUtils(0.35F, 0.35F, 0.05F);
   AnimationUtils alphaPass = new AnimationUtils(0.35F, 0.35F, 0.05F);
   AnimationUtils keyLogin = new AnimationUtils(0.0F, 0.0F, 0.1F);
   AnimationUtils keyRandom = new AnimationUtils(0.0F, 0.0F, 0.1F);
   AnimationUtils keyExit = new AnimationUtils(0.0F, 0.0F, 0.1F);
   int mouseX;
   int mouseY;

   public GuiAltLogin(GuiScreen previousScreen) {
      this.previousScreen = previousScreen;
   }

   public static String randomString(int strLength) {
      StringBuilder stringBuilder = new StringBuilder(strLength);

      for (int i = 0; i < strLength; i++) {
         stringBuilder.append(
            "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
               .charAt(secureRandom.nextInt("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890".length()))
         );
      }

      return stringBuilder.toString();
   }

   void clickReader() {
      if (textFieldName.isFocused()) {
         textFieldPass.setFocused(false);
         textFieldPass.setText("");
      }

      if (textFieldPass.isFocused()) {
         textFieldName.setFocused(false);
      } else if (!textFieldName.isFocused()) {
         textFieldName.setText("");
      }
   }

   void nameClickReader(float x, float y, float xPw, float yPw, int mouseX, int mouseY) {
      if (RenderUtils.isHovered((float)mouseX, (float)mouseY, (float)((int)x), (float)((int)y), (float)((int)xPw), (float)((int)yPw))) {
         textFieldName.setFocused(!textFieldName.isFocused());
         textFieldPass.setFocused(false);
         if (!textFieldName.isFocused()) {
            textFieldName.setText("");
         }

         this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }

   void passClickReader(float x, float y, float xPw, float yPw, int mouseX, int mouseY) {
      if (RenderUtils.isHovered((float)mouseX, (float)mouseY, (float)((int)x), (float)((int)y), (float)((int)xPw), (float)((int)yPw))) {
         textFieldPass.setFocused(!textFieldPass.isFocused());
         textFieldName.setFocused(false);
         if (!textFieldPass.isFocused()) {
            textFieldPass.setText("");
         }

         this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }

   void updateFieldsAlpha() {
      this.alphaName.to = textFieldName.isFocused() ? 1.0F : 0.35F;
      this.alphaPass.to = textFieldPass.isFocused() ? 1.0F : 0.35F;
   }

   void drawKey(float x, float y, float w, float h, float anim, String name, int color1, int color2) {
      int c1 = ColorUtils.swapAlpha(color1, (155.0F + anim * 100.0F) / 3.0F);
      int c2 = ColorUtils.swapAlpha(color2, (155.0F + anim * 100.0F) / 3.0F);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x - anim, y - anim, x + w + anim, y + h + anim, 5.0F + anim * 2.01F, 0.5F + anim * 1.51F, c1, c2, c2, c1, false, true, true
      );
      Fonts.comfortaaRegular_18
         .drawStringWithShadow(name, (double)(x + w / 2.0F - (float)(Fonts.comfortaaRegular_18.getStringWidth(name) / 2)), (double)(y + h / 2.0F - 3.0F), -1);
   }

   void drawKeyGhost(float x, float y, float w, float h, float anim) {
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x - anim, y - anim, x + w + anim, y + h + anim, 5.0F + anim * 2.0F, 0.5F + anim, -1, -1, -1, -1, false, true, false
      );
   }

   void onKeyLogin() {
      if (!textFieldName.getText().isEmpty()) {
         this.newName = textFieldName.getText();
         this.thread = new AltLoginThread(textFieldName.getText(), textFieldPass.getText());
         this.thread.start();
      }

      for (int i = 0; i < 120; i++) {
         System.out.println(Minecraft.getMinecraft().session.getUsername());
      }

      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   void onKeyRandom() {
      textFieldName.setFocused(true);
      String name = Client.randomNickname();
      textFieldName.setText(name);
      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   void onKeyExit() {
      this.mc.displayGuiScreen(new GuiMainMenu());
      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      ScaledResolution sr = new ScaledResolution(this.mc);
      if (!Panic.stop) {
         RenderUtils.drawScreenShaderBackground(sr, mouseX, mouseY);
      }

      float w = 380.0F;
      float h = 260.0F;
      float x = (float)(sr.getScaledWidth() / 2) - w / 2.0F;
      float y = (float)(sr.getScaledHeight() / 2) - h / 2.0F;
      float x2 = x + w;
      float y2 = y + h;
      int bgc1 = ColorUtils.getColor(0, 0, 0, 160);
      int bgc2 = ColorUtils.getColor(0, 0, 0, 80);
      CFontRenderer altFont = Fonts.mntsb_20;
      RenderUtils.fullRoundFG(x, y, x2, y2, 16.0F, bgc1, bgc1, bgc1, bgc1, false);
      RenderUtils.drawRoundedShadow(x, y, x2, y2, 34.0F, 16.0F, bgc2, false);
      GL11.glEnable(3089);
      RenderUtils.scissor((double)x, (double)y, (double)(x2 - x), (double)(y2 - y));
      RenderUtils.fullRoundFG(
         x + w / 2.0F - (float)(altFont.getStringWidth("Менеджер аккаунтов") / 2) - 8.0F,
         y - 8.0F,
         x + w / 2.0F + (float)(altFont.getStringWidth("Менеджер аккаунтов") / 2) + 8.0F,
         y + 30.0F,
         16.0F,
         0,
         0,
         bgc2,
         bgc2,
         false
      );
      RenderUtils.fullRoundFG(x + w / 2.0F - 60.0F, y + 165.0F, x + w / 2.0F + 60.0F, y2 + 8.0F, 16.0F, bgc2, bgc2, 0, 0, false);
      GL11.glDisable(3089);
      StencilUtil.uninitStencilBuffer();
      altFont.drawVGradientString(
         "Менеджер аккаунтов",
         (double)(x + w / 2.0F - (float)(altFont.getStringWidth("Менеджер аккаунтов") / 2)),
         (double)(y + 14.0F),
         ColorUtils.getColor(170),
         ColorUtils.getColor(255)
      );
      int gee = ColorUtils.swapAlpha(-1, 50.0F);
      StencilUtil.initStencilToWrite();
      this.drawKeyGhost(x + w / 2.0F - 50.0F - 0.5F, y + 175.0F - 0.5F, 100.0F, 20.0F, this.keyLogin.getAnim());
      this.drawKeyGhost(x + w / 2.0F - 50.0F - 0.5F, y + 200.0F - 0.5F, 100.0F, 20.0F, this.keyRandom.getAnim());
      this.drawKeyGhost(x + w / 2.0F - 50.0F - 0.5F, y + 225.0F - 0.5F, 100.0F, 20.0F, this.keyExit.getAnim());
      StencilUtil.readStencilBuffer(1);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         (float)(mouseX - 10), (float)mouseY, (float)(mouseX + 10), (float)mouseY, 0.0F, 40.0F, gee, gee, gee, gee, true, false, true
      );
      StencilUtil.uninitStencilBuffer();
      this.keyLogin.to = RenderUtils.isHovered((float)mouseX, (float)mouseY, x + w / 2.0F - 50.0F, y + 175.0F, 100.0F, 20.0F) ? 1.0F : 0.0F;
      this.keyRandom.to = RenderUtils.isHovered((float)mouseX, (float)mouseY, x + w / 2.0F - 50.0F, y + 200.0F, 100.0F, 20.0F) ? 1.0F : 0.0F;
      this.keyExit.to = RenderUtils.isHovered((float)mouseX, (float)mouseY, x + w / 2.0F - 50.0F, y + 225.0F, 100.0F, 20.0F) ? 1.0F : 0.0F;
      this.drawKey(
         x + w / 2.0F - 50.0F,
         y + 175.0F,
         100.0F,
         20.0F,
         this.keyLogin.getAnim(),
         "Войти",
         ColorUtils.getColor(115, 253, 255),
         ColorUtils.getColor(60, 255, 161)
      );
      this.drawKey(
         x + w / 2.0F - 50.0F,
         y + 200.0F,
         100.0F,
         20.0F,
         this.keyRandom.getAnim(),
         "Случайное имя",
         ColorUtils.getColor(255, 226, 89),
         ColorUtils.getColor(255, 126, 0)
      );
      this.drawKey(
         x + w / 2.0F - 50.0F,
         y + 225.0F,
         100.0F,
         20.0F,
         this.keyExit.getAnim(),
         "Выйти из меню",
         ColorUtils.getColor(255, 135, 219),
         ColorUtils.getColor(219, 42, 255)
      );
      this.clickReader();
      this.updateFieldsAlpha();
      int cd = ColorUtils.getColor(0, 0, 0, 110);
      int c0 = ColorUtils.getColor(0, 0, 0, 60);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F, y + 50.0F, x + w / 2.0F + 110.0F, y + 75.0F, 6.0F, 2.0F, c0, c0, c0, c0, false, true, false
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F, y + 50.0F, x + w / 2.0F + 110.0F, y + 75.0F, 6.0F, 2.0F, cd, cd, cd, cd, false, false, true
      );
      RenderUtils.drawRect((double)(x + w / 2.0F - 1.0F), (double)(y + 50.0F), (double)(x + w / 2.0F + 1.0F), (double)(y + 75.0F), c0);
      Fonts.comfortaaRegular_17.drawStringWithShadow("Предыдущее имя:", (double)(x + w / 2.0F - 1.0F + 5.0F - 110.0F), (double)(y + 50.0F + 4.0F), -1);
      Fonts.comfortaaRegular_14.drawStringWithShadow(this.oldName, (double)(x + w / 2.0F - 1.0F + 5.0F - 110.0F), (double)(y + 50.0F + 16.0F), -1);
      Fonts.comfortaaRegular_17.drawStringWithShadow("Текущее имя:", (double)(x + w / 2.0F - 1.0F + 5.0F), (double)(y + 50.0F + 4.0F), -1);
      Fonts.comfortaaRegular_14.drawStringWithShadow(this.newName, (double)(x + w / 2.0F - 1.0F + 5.0F), (double)(y + 50.0F + 16.0F), -1);
      textFieldName.setMaxStringLength(16);
      textFieldPass.setMaxStringLength(32);
      String split = System.currentTimeMillis() % 1000L >= 500L ? "_" : "";
      int c1 = ColorUtils.getColor(91, 95, 255, 90);
      int c2 = ColorUtils.getColor(44, 37, 173, 30);
      int c3 = ColorUtils.getColor(0, 0, 0, 60);
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRect(
         (double)(x + w / 2.0F - 110.0F - 3.0F), (double)(y + 90.0F - 3.0F), (double)(x + w / 2.0F - 110.0F + 32.0F - 3.0F), (double)(y + 115.0F + 3.0F), -1
      );
      StencilUtil.readStencilBuffer(1);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F, y + 90.0F, x + w / 2.0F + 110.0F, y + 115.0F, 6.0F, 2.0F, c1, c1, c1, c1, true, true, true
      );
      StencilUtil.readStencilBuffer(0);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F, y + 90.0F, x + w / 2.0F + 110.0F, y + 115.0F, 6.0F, 2.0F, c1, c2, c2, c1, true, true, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F + 1.0F, y + 90.0F + 1.0F, x + w / 2.0F + 110.0F - 1.0F, y + 115.0F - 1.0F, 5.0F, 0.5F, c3, c3, c3, c3, false, true, true
      );
      StencilUtil.uninitStencilBuffer();
      Fonts.stylesicons_24
         .drawStringWithOutline(
            textFieldName.isFocused() ? "I" : "B", (double)(x + w / 2.0F - 110.0F + 9.0F), (double)(y + 90.0F + 9.5F), ColorUtils.swapAlpha(c1, 255.0F)
         );
      if (textFieldName.isFocused() || !textFieldName.getText().isEmpty() && textFieldPass.isFocused()) {
         Fonts.comfortaaRegular_18
            .drawStringWithShadow(
               textFieldName.getText()
                  + (
                     !textFieldName.isFocused() || textFieldName.getText().length() > 16 || !textFieldName.getText().isEmpty() && textFieldPass.isFocused()
                        ? ""
                        : split
                  ),
               (double)(x + w / 2.0F - 110.0F + 34.0F),
               (double)(y + 90.0F + 10.0F),
               ColorUtils.swapAlpha(-1, 255.0F * this.alphaName.getAnim())
            );
      } else if (!textFieldPass.isFocused()) {
         Fonts.comfortaaBold_18
            .drawStringWithShadow(
               "Введите никнейм", (double)(x + w / 2.0F - 110.0F + 34.0F), (double)(y + 90.0F + 9.5F), ColorUtils.getColor(255, 255, 255, 60)
            );
      }

      int c4 = ColorUtils.getColor(230, 45, 45, 90);
      int c5 = ColorUtils.getColor(157, 36, 36, 30);
      StencilUtil.initStencilToWrite();
      RenderUtils.drawRect(
         (double)(x + w / 2.0F - 110.0F - 3.0F), (double)(y + 130.0F - 3.0F), (double)(x + w / 2.0F - 110.0F + 32.0F - 3.0F), (double)(y + 155.0F + 3.0F), -1
      );
      StencilUtil.readStencilBuffer(1);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F, y + 130.0F, x + w / 2.0F + 110.0F, y + 155.0F, 6.0F, 2.0F, c4, c4, c4, c4, true, true, true
      );
      StencilUtil.readStencilBuffer(0);
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F, y + 130.0F, x + w / 2.0F + 110.0F, y + 155.0F, 6.0F, 2.0F, c4, c5, c5, c4, true, true, true
      );
      RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
         x + w / 2.0F - 110.0F + 1.0F, y + 130.0F + 1.0F, x + w / 2.0F + 110.0F - 1.0F, y + 155.0F - 1.0F, 5.0F, 0.5F, c3, c3, c3, c3, false, true, true
      );
      StencilUtil.uninitStencilBuffer();
      Fonts.stylesicons_24
         .drawStringWithOutline(
            textFieldPass.isFocused() ? "I" : "L", (double)(x + w / 2.0F - 110.0F + 9.0F), (double)(y + 130.0F + 11.0F), ColorUtils.swapAlpha(c4, 255.0F)
         );
      String pass = "";

      for (int i = 0; i < textFieldPass.getText().length(); i++) {
         pass = pass + "*";
      }

      if (textFieldPass.isFocused()) {
         Fonts.comfortaaRegular_18
            .drawStringWithShadow(
               pass + (textFieldPass.isFocused() && textFieldPass.getText().length() <= 32 ? split : ""),
               (double)(x + w / 2.0F - 110.0F + 34.0F),
               (double)(y + 130.0F + 9.5F),
               ColorUtils.swapAlpha(-1, 255.0F * this.alphaPass.getAnim())
            );
      } else {
         Fonts.comfortaaBold_18
            .drawStringWithShadow(
               "Введите пароль (лицензия)", (double)(x + w / 2.0F - 110.0F + 34.0F), (double)(y + 130.0F + 9.5F), ColorUtils.getColor(255, 255, 255, 60)
            );
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
      if (!Panic.stop) {
         ViaMCP.INSTANCE.getViaPanel().drawPanel(1.0F, mouseX, mouseY);
      }
   }

   @Override
   public void initGui() {
      ClientRP.getInstance().getDiscordRP().update("В меню менеджера аккаунтов", "Меняет никнейм");
      this.newName = "None";
      this.oldName = this.mc.session.getUsername();
      Keyboard.enableRepeatEvents(true);
      textFieldName.setFocused(true);
      textFieldPass.setFocused(false);
      textFieldName.setText("");
      textFieldPass.setText("");
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      textFieldName.textboxKeyTyped(typedChar, keyCode);
      textFieldPass.textboxKeyTyped(typedChar, keyCode);
      super.keyTyped(typedChar, keyCode);
   }

   @Override
   protected void mouseClicked(int x2, int y2, int button) {
      ScaledResolution sr = new ScaledResolution(this.mc);
      float w = 380.0F;
      float h = 260.0F;
      float x = (float)(sr.getScaledWidth() / 2) - w / 2.0F;
      float y = (float)(sr.getScaledHeight() / 2) - h / 2.0F;
      if (button == 0) {
         if (RenderUtils.isHovered((float)x2, (float)y2, x + w / 2.0F - 50.0F, y + 175.0F, 100.0F, 20.0F)) {
            this.onKeyLogin();
         } else if (RenderUtils.isHovered((float)x2, (float)y2, x + w / 2.0F - 50.0F, y + 200.0F, 100.0F, 20.0F)) {
            this.onKeyRandom();
         } else if (RenderUtils.isHovered((float)x2, (float)y2, x + w / 2.0F - 50.0F, y + 225.0F, 100.0F, 20.0F)) {
            this.onKeyExit();
         } else {
            this.nameClickReader(x + w / 2.0F - 110.0F + 1.0F, y + 90.0F + 1.0F, 25.0F, 25.0F, x2, y2);
            this.passClickReader(x + w / 2.0F - 110.0F + 1.0F, y + 130.0F + 1.0F, 25.0F, 25.0F, x2, y2);
         }
      }

      try {
         super.mouseClicked(x2, y2, button);
      } catch (IOException var10) {
         var10.printStackTrace();
      }

      if (!Panic.stop) {
         ViaMCP.INSTANCE.getViaPanel().mouseClick(this.mouseX, this.mouseY, button);
      }
   }

   @Override
   public void onGuiClosed() {
   }

   @Override
   public void updateScreen() {
      textFieldName.updateCursorCounter();
      textFieldPass.updateCursorCounter();
   }
}
