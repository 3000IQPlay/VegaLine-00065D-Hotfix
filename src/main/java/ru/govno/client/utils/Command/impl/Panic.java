package ru.govno.client.utils.Command.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClickGui;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.ClientRP;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Command.Command;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

// TODO: Translate

public class Panic extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static boolean stop = false;
   public static List<Panic.Modules> mods = new ArrayList<>();
   public static String lastCode = "";
   public static boolean callReload = true;
   private static String prevClientName;
   private static final String alphabet = "QWERTYUIOPASDFGHJKLZXCVBNM";
   private static final SecureRandom secureRandom = new SecureRandom();
   static TimerHelper timeShow = new TimerHelper();
   static AnimationUtils showAnim = new AnimationUtils(0.0F, 0.0F, 0.05F);
   static boolean runShowCode;

   public Panic() {
      super("Panic", new String[]{"panic"});
   }

   public static String randomString(int strLength) {
      StringBuilder stringBuilder = new StringBuilder(strLength);

      for (int i = 0; i < strLength; i++) {
         stringBuilder.append("QWERTYUIOPASDFGHJKLZXCVBNM".charAt(secureRandom.nextInt("QWERTYUIOPASDFGHJKLZXCVBNM".length())));
      }

      return stringBuilder.toString();
   }

   public static void enablePanic() {
      if (!stop) {
         lastCode = randomString((int)(2.0 + 3.0 * Math.random()));
         InputStream inputstream = null;
         InputStream inputstream1 = null;

         try {
            inputstream = Minecraft.getMinecraft().mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
         } catch (FileNotFoundException var7) {
            var7.printStackTrace();
         } catch (IOException var8) {
            var8.printStackTrace();
         }

         try {
            inputstream1 = Minecraft.getMinecraft().mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));
         } catch (FileNotFoundException var5) {
            var5.printStackTrace();
         } catch (IOException var6) {
            var6.printStackTrace();
         }

         try {
            Display.setIcon(new ByteBuffer[]{Minecraft.getMinecraft().readImageToBuffer(inputstream), Minecraft.getMinecraft().readImageToBuffer(inputstream1)});
         } catch (IOException var4) {
            var4.printStackTrace();
         }

         GuiNewChat.sentMessages.removeIf(sent -> sent.startsWith("."));
         ClientRP.getInstance().getDiscordRP().shutdown();
         prevClientName = Client.name;
         Client.name = "Minecraft " + ClientBrandRetriever.getVersionStr();
         Display.setTitle(Client.name);

         for (Module mod : Client.moduleManager.getModuleList()) {
            mods.add(new Panic.Modules(mod, mod.actived, mod.bind));
            if (mod.actived) {
               mod.toggleSilent(false);
               mod.bind = 0;
            }
         }

         ClickGui.instance.bind = 0;
         stop = true;
      }
   }

   public static void disablePanic() {
      if (stop) {
         lastCode = "";
         InputStream inputstream = null;
         InputStream inputstream1 = null;

         try {
            inputstream = Minecraft.getMinecraft()
               .mcDefaultResourcePack
               .getInputStream(new ResourceLocation("vegaline/system/minecraft/window/windowicons/icon64.png"));
         } catch (IOException var8) {
            var8.printStackTrace();
         }

         try {
            inputstream1 = Minecraft.getMinecraft()
               .mcDefaultResourcePack
               .getInputStream(new ResourceLocation("vegaline/system/minecraft/window/windowicons/icon32.png"));
         } catch (IOException var7) {
            var7.printStackTrace();
         }

         if (inputstream != null && inputstream1 != null) {
            try {
               Display.setIcon(
                  new ByteBuffer[]{Minecraft.getMinecraft().readImageToBuffer(inputstream), Minecraft.getMinecraft().readImageToBuffer(inputstream1)}
               );
            } catch (IOException var6) {
               var6.printStackTrace();
            }
         }

         ClientRP.getInstance().init();
         ClientRP.getInstance().getDiscordRP().start();
         ClientRP.getInstance().getDiscordRP().refresh();
         Client.name = prevClientName;
         Display.setTitle(Client.name + " " + Client.version);

         for (Module mod : Client.moduleManager.getModuleList()) {
            for (Panic.Modules ms : mods) {
               if (ms.modules == mod) {
                  mod.toggleSilent(ms.actived);
                  mod.setBind(ms.bind);
               }
            }
         }

         Client.configManager.saveConfig("temporaryPanical");
         callReload = true;
         mods.clear();
         stop = false;
      }
   }

   public static void runShowCode() {
      runShowCode = true;
      timeShow.reset();
      showAnim.to = 0.001F;
   }

   static boolean updateShow() {
      if (runShowCode && timeShow.hasReached(500.0F)) {
         showAnim.to = 1.0F;
      }

      if (timeShow.hasReached(11000.0F) || !stop) {
         showAnim.to = 0.0F;
      }

      if ((double)showAnim.getAnim() < 0.05 && showAnim.to == 0.0F) {
         runShowCode = false;
      }

      return runShowCode;
   }

   public static void onHasShowPanicCode(float x, float y) {
      if (updateShow() && stop) {
         float w = 110.0F;
         float timePC = MathUtils.clamp(((float)timeShow.getTime() - 550.0F) / 11000.0F, 0.0F, 1.0F);
         String code = ".panic " + lastCode;
         float out = 1.0F - timePC;
         if ((double)timePC < 0.8) {
            CFontRenderer fontCode = Fonts.mntsb_36;
            if ((float)(fontCode.getStringWidth(code) + 10) > w) {
               w = (float)(fontCode.getStringWidth(code) + 10);
            }

            float h = 57.0F;
            x -= w / 2.0F;
            y -= h / 2.0F;
            float aPC = showAnim.getAnim();
            int texCol = ColorUtils.swapAlpha(-1, 255.0F * aPC);
            int rectCol = ColorUtils.getColor(70, 70, 70, 210.0F * aPC);
            int rectCol2 = ColorUtils.getColor(0, 0, 0, 120.0F * aPC);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               x, y, x + w, y + h, 8.0F, 2.0F, rectCol, rectCol, rectCol, rectCol, false, true, true
            );
            CFontRenderer fontStart = Fonts.mntsb_14;
            String start2 = "введите код ниже";
            String start = "Для отключения паника";
            float xPod = x + w / 2.0F - (float)fontCode.getStringWidth(code) / 2.0F - 2.0F;
            float xPod2 = x + w / 2.0F + (float)fontCode.getStringWidth(code) / 2.0F + 2.0F;
            float yPod = y + 10.0F + 9.0F + 6.0F;
            float yPod2 = y + 10.0F + 9.0F + 20.0F + 6.0F;
            RenderUtils.drawRect((double)xPod, (double)yPod, (double)xPod2, (double)yPod2, rectCol2);
            if (255.0F * aPC >= 33.0F) {
               fontStart.drawCenteredString(start, (double)(x + w / 2.0F), (double)(y + 7.0F), texCol);
               fontStart.drawCenteredString(start2, (double)(x + w / 2.0F), (double)(y + 7.0F + 10.0F), texCol);
               fontCode.drawCenteredString(code, (double)(x + w / 2.0F), (double)(y + 7.0F + 10.0F + 9.0F), texCol);
            }

            RenderUtils.drawRect((double)(x + 4.0F), (double)(y + h - 6.0F), (double)(x + 4.0F + (w - 8.0F) * out), (double)(y + h - 4.0F), texCol);
            RenderUtils.drawRect((double)(x + 4.0F + (w - 8.0F) * out), (double)(y + h - 6.0F), (double)(x - 4.0F + w), (double)(y + h - 4.0F), rectCol2);
         } else {
            FontRenderer fontCodex = mc.fontRendererObj;
            if ((float)(fontCodex.getStringWidth(code) + 10) > w) {
               w = (float)(fontCodex.getStringWidth(code) + 10);
            }

            float h = 57.0F;
            x -= w / 2.0F;
            y -= h / 2.0F;
            float aPC = showAnim.getAnim();
            int texCol = ColorUtils.swapAlpha(-1, 255.0F * aPC);
            int rectCol = ColorUtils.getColor(70, 70, 70, 210.0F * aPC);
            int rectCol2 = ColorUtils.getColor(0, 0, 0, 120.0F * aPC);
            RenderUtils.drawAlphedRect((double)x, (double)y, (double)(x + w), (double)(y + h), rectCol);
            FontRenderer fontStart = mc.fontRendererObj;
            String start2 = "введите код ниже";
            String start = "Для отключения паника";
            float xPod = x + w / 2.0F - (float)fontCodex.getStringWidth(code) / 2.0F - 2.0F;
            float xPod2 = x + w / 2.0F + (float)fontCodex.getStringWidth(code) / 2.0F + 2.0F;
            float yPod = y + 10.0F + 9.0F + 6.0F;
            float yPod2 = y + 10.0F + 9.0F + 20.0F + 6.0F;
            RenderUtils.drawRect((double)xPod, (double)yPod, (double)xPod2, (double)yPod2, rectCol2);
            if (255.0F * aPC >= 33.0F) {
               fontStart.drawString(start, x + w / 2.0F - (float)fontStart.getStringWidth(start) / 2.0F, (double)(y + 7.0F), texCol);
               fontStart.drawString(start2, x + w / 2.0F - (float)fontStart.getStringWidth(start2) / 2.0F, (double)(y + 7.0F + 10.0F), texCol);
               fontCodex.drawString(code, x + w / 2.0F - (float)fontCodex.getStringWidth(code) / 2.0F, (double)(y + 7.0F + 10.0F + 9.0F), texCol);
            }

            RenderUtils.drawRect((double)(x + 4.0F), (double)(y + h - 6.0F), (double)(x + 4.0F + (w - 8.0F) * out), (double)(y + h - 4.0F), texCol);
            RenderUtils.drawRect((double)(x + 4.0F + (w - 8.0F) * out), (double)(y + h - 6.0F), (double)(x - 4.0F + w), (double)(y + h - 4.0F), rectCol2);
         }

         GlStateManager.enableDepth();
         GL11.glDepthMask(true);
      }
   }

   @Override
   public void onCommand(String[] args) {
      try {
         if (args[1].equalsIgnoreCase("on") && !stop) {
            enablePanic();
            runShowCode();
         }

         if ((args[1].toUpperCase().equalsIgnoreCase(lastCode) || args[1].equalsIgnoreCase("DIS")) && stop) {
            disablePanic();
         }
      } catch (Exception var3) {
         Client.msg("§2§lPanic:§r §7Комманда написана неверно.", false);
         Client.msg("§2§lPanic:§r §7panic [§lon/code§r§7]", false);
         var3.printStackTrace();
      }
   }

   public static class Modules {
      Module modules;
      boolean actived;
      int bind;

      public Modules(Module modules, boolean actived, int bind) {
         for (Module mod : Client.moduleManager.getModuleList()) {
            if (mod.actived) {
               this.modules = modules;
               this.actived = actived;
               this.bind = bind;
               break;
            }
         }
      }
   }
}
