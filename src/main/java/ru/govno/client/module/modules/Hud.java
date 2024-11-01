package ru.govno.client.module.modules;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec2f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRender2D;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.CFontRenderer;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.TPSDetect;
import ru.govno.client.utils.Math.FrameCounter;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.BloomUtil;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;
import ru.govno.client.utils.Render.StencilUtil;
import ru.govno.client.utils.Render.Vec2fColored;

public class Hud extends Module {
   private float expand;
   private float width1;
   private float width2;
   private float width3;
   public static Hud get;
   public BoolSettings Information;
   public BoolSettings Watermark;
   public BoolSettings Potions;
   public BoolSettings CustomHotbar;
   public BoolSettings ArmorHUD;
   public BoolSettings StaffList;
   public BoolSettings ArrayList;
   public BoolSettings KeyBinds;
   public BoolSettings PickupsList;
   public BoolSettings LagDetect;
   public BoolSettings ManyGlows;
   public BoolSettings SaturationStats;
   public ModeSettings Info;
   public ModeSettings MarkMode;
   public ModeSettings HotbarStyle;
   public ModeSettings HudRectMode;
   public FloatSettings WX;
   public FloatSettings WY;
   public FloatSettings PX;
   public FloatSettings PY;
   public FloatSettings AX;
   public FloatSettings AY;
   public FloatSettings SX;
   public FloatSettings SY;
   public FloatSettings LX;
   public FloatSettings LY;
   public FloatSettings KX;
   public FloatSettings KY;
   public FloatSettings PCX;
   public FloatSettings PCY;
   private final FrameCounter frameCounter = FrameCounter.build();
   private final Pattern validUserPattern = Pattern.compile("^\\w{3,16}$");
   TimerHelper laggCheck = new TimerHelper();
   private final AnimationUtils laggAlphaPC = new AnimationUtils(0.0F, 0.0F, 0.1F);
   private static final AnimationUtils alphaPC = new AnimationUtils(0.0F, 0.0F, 0.05F);
   public static List<Module> enabledModules;
   private static final TimerHelper arrayListLastUpdateTime = new TimerHelper();
   public static float listPosX;
   public static float listPosY;
   ArrayList<Hud.StaffPlayer> staffPlayers = new ArrayList<>();
   boolean staffDetectSound;
   boolean staffUpdateSound;
   List<Module> bindsList = new ArrayList<>();
   private final List<Hud.PickupItem> notifysList = new ArrayList<>();
   private final AnimationUtils potionsHeight = new AnimationUtils(getPotionHudHeight(), getPotionHudHeight(), 0.075F);
   private final AnimationUtils staffsHeight = new AnimationUtils(this.getStaffHudHeight(), this.getStaffHudHeight(), 0.075F);
   private final AnimationUtils keybindsHeight = new AnimationUtils(this.getKeyBindsHudHeight(), this.getKeyBindsHudHeight(), 0.075F);
   private final AnimationUtils pickupsHeight = new AnimationUtils(this.getPickupsHudHeight(), this.getPickupsHudHeight(), 0.125F);
   public static float wmPosX = 0.0F;
   public static float wmPosY = 0.0F;
   public static float wmWidth = 0.0F;
   public static float wmHeight = 0.0F;
   public static float potPosX = 100.0F;
   public static float potPosY = 8.0F;
   public static float potWidth = getPotionHudWidth();
   public static float potHeight = getPotionHudHeight();
   public static float armPosX = 5.0F;
   public static float armPosY = 5.0F;
   public static float armWidth = 16.0F;
   public static float armHeight = 16.0F;
   public static float stPosX = 5.0F;
   public static float stPosY = 55.0F;
   public static float stWidth = 16.0F;
   public static float stHeight = 16.0F;
   public static float kbPosX = 5.0F;
   public static float kbPosY = 55.0F;
   public static float kbWidth = 16.0F;
   public static float kbHeight = 16.0F;
   public static float pcPosX = 5.0F;
   public static float pcPosY = 195.0F;
   public static float pcWidth = 16.0F;
   public static float pcHeight = 16.0F;
   private final List<Hud.PotionWithString> potionsWithString = new ArrayList<>();

   public Hud() {
      super("Hud", 0, Module.Category.RENDER);
      this.settings.add(this.WX = new FloatSettings("WX", 0.003F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.WY = new FloatSettings("WY", 0.006F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.Information = new BoolSettings("Information", true, this));
      this.settings
         .add(this.Info = new ModeSettings("Info", "Sleek", this, new String[]{"Sleek", "Akrien", "Plastic", "Modern"}, () -> this.Information.getBool()));
      this.settings.add(this.Watermark = new BoolSettings("Watermark", true, this));
      this.settings
         .add(this.MarkMode = new ModeSettings("MarkMode", "Sweet", this, new String[]{"Default", "Chess", "Sweet", "Bloom", "Clock", "Wonderful", "Plate"}));
      this.settings.add(this.Potions = new BoolSettings("Potions", true, this));
      this.settings.add(this.PX = new FloatSettings("PX", 0.008F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.PY = new FloatSettings("PY", 0.1F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.CustomHotbar = new BoolSettings("CustomHotbar", true, this));
      this.settings.add(this.HotbarStyle = new ModeSettings("HotbarStyle", "Sleek", this, new String[]{"Sleek", "Modern"}, () -> this.CustomHotbar.getBool()));
      this.settings.add(this.ArmorHUD = new BoolSettings("ArmorHUD", false, this));
      this.settings.add(this.AX = new FloatSettings("AX", 0.005F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.AY = new FloatSettings("AY", 0.7F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.StaffList = new BoolSettings("StaffList", false, this));
      this.settings.add(this.SX = new FloatSettings("SX", 0.008F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.SY = new FloatSettings("SY", 0.5F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.ArrayList = new BoolSettings("ArrayList", false, this));
      this.settings.add(this.LX = new FloatSettings("LX", 0.999F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.LY = new FloatSettings("LY", 0.004F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.KeyBinds = new BoolSettings("KeyBinds", true, this));
      this.settings.add(this.KX = new FloatSettings("KX", 0.008F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.KY = new FloatSettings("KY", 0.3F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.PickupsList = new BoolSettings("PickupsList", false, this));
      this.settings.add(this.PCX = new FloatSettings("PCX", 0.008F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.PCY = new FloatSettings("PCY", 0.8F, 1.0F, 0.0F, this, () -> false));
      this.settings.add(this.LagDetect = new BoolSettings("LagDetect", false, this));
      this.settings
         .add(
            this.HudRectMode = new ModeSettings(
               "HudRectMode",
               "Stipple",
               this,
               new String[]{"Glow", "Window", "Plain", "Stipple"},
               () -> this.Potions.getBool() || this.StaffList.getBool() || this.KeyBinds.getBool() || this.PickupsList.getBool()
            )
         );
      this.settings
         .add(
            this.ManyGlows = new BoolSettings(
               "ManyGlows", true, this, () -> this.Potions.getBool() || this.StaffList.getBool() || this.KeyBinds.getBool() || this.PickupsList.getBool()
            )
         );
      this.settings.add(this.SaturationStats = new BoolSettings("SaturationStats", true, this));
      get = this;
   }

   public boolean showSaturationStats() {
      return this.isActived() && this.SaturationStats.getBool();
   }

   private void clientRect(float x, float y, float x2, float y2, String element) {
      if (get != null && this.HudRectMode.currentMode != null) {
         RenderUtils.hudRectWithString(x, y, x2, y2, element, this.HudRectMode.currentMode, this.ManyGlows.getBool());
      }
   }

   @EventTarget
   public void onPacket(EventReceivePacket event) {
      if (this.actived && this.LagDetect.getBool()) {
         this.laggCheck.reset();
         this.laggAlphaPC.to = 0.0F;
      }
   }

   public static final String getModName(Module mod, boolean titles) {
      return titles ? mod.getDisplayName() : mod.getName();
   }

   public static final List<Module> getMods() {
      return enabledModules;
   }

   public static final CFontRenderer font() {
      return Fonts.comfortaaBold_13;
   }

   public static void onTriger(Module mod) {
      if ((double)mod.stateAnim.getAnim() < 0.1 && mod.stateAnim.to == 0.0F) {
         mod.stateAnim.setAnim(0.0F);
      }

      mod.stateAnim.to = mod.actived ? 1.0F : 0.0F;
      if (MathUtils.getDifferenceOf(mod.stateAnim.to, mod.stateAnim.anim) > 0.1) {
         arrayListLastUpdateTime.reset();
      }
   }

   public boolean isTitles() {
      return true;
   }

   public boolean isOnlyBound() {
      return false;
   }

   public void setupArrayListCoords(ScaledResolution sr) {
      float curX = this.LX.getFloat() * (float)sr.getScaledWidth();
      float curY = this.LY.getFloat() * (float)sr.getScaledHeight();
      float extY = 0.0F;
      if (Minecraft.player != null && (Minecraft.player == null || Minecraft.player.getActivePotionEffects().size() != 0) && !this.isPotsCustom()) {
         boolean a = false;
         boolean b = false;

         for (PotionEffect apf : Minecraft.player.getActivePotionEffects()) {
            a = true;
            if (apf.getPotion().isBadEffect()) {
               b = true;
            }
         }

         extY = !a && !b
            ? 0.0F
            : (float)(
               26
                  * (b ? 2 : 1)
                  * (curX > (float)(sr.getScaledWidth() - (Minecraft.player == null ? 0 : Minecraft.player.getActivePotionEffects().size()) * 26) ? 1 : 0)
            );
      }

      curY += extY;
      float speedAnim = (float)Minecraft.frameTime * 0.04F;
      if (listPosX == 0.0F) {
         listPosX = curX;
      }

      if (listPosY == 0.0F) {
         listPosY = curY;
      }

      listPosX = MathUtils.harp(listPosX, curX, speedAnim);
      listPosY = MathUtils.harp(listPosY, curY, speedAnim);
   }

   public void trigerSort(boolean titles) {
      if (this.isKeyBindsHud()) {
         this.setupBindsList();
      }

      if (get != null && get.actived && this.ArrayList.getBool()) {
         List<String> matches = Arrays.asList("nointeract", "procontainer", "respawn", "namesecurity", "srpspoofer", "middleclick", "onfalling");
         ScaledResolution sr = new ScaledResolution(mc);
         CFontRenderer font = font();
         boolean reverseY = this.isReverseY(sr);
         enabledModules = Client.moduleManager
            .getModuleList()
            .stream()
            .filter(Module::showToArrayList)
            .filter(m -> !m.getName().toLowerCase().contains("helper") && !matches.stream().anyMatch(mat -> mat.contains(m.getName().toLowerCase())))
            .sorted(Comparator.comparingDouble(e -> {
               onTriger(e);
               return (double)(font.getStringWidth(getModName(e, titles)) * (reverseY ? 1 : -1));
            }))
            .collect(Collectors.toList());
      }
   }

   private void setupAlphaPC() {
      int cur = this.actived && this.ArrayList.getBool() ? 1 : 0;
      if (alphaPC.to != (float)cur) {
         alphaPC.to = (float)cur;
      }

      if (MathUtils.getDifferenceOf(alphaPC.getAnim(), (float)cur) < 0.01F) {
         alphaPC.setAnim((float)cur);
      }
   }

   private float getAlphaPC() {
      return alphaPC.getAnim();
   }

   private boolean canRenderList() {
      return enabledModules != null && enabledModules.size() != 0 && this.getAlphaPC() != 0.0F;
   }

   public boolean isReverseY(ScaledResolution sr) {
      return listPosY + this.getArrayHeight() / 2.0F > (float)sr.getScaledHeight() / 2.0F;
   }

   public boolean isReverseX(ScaledResolution sr) {
      return listPosX - this.getArrayWidth(sr) / 2.0F < (float)sr.getScaledWidth() / 2.0F;
   }

   public float getArrayWidth(ScaledResolution sr) {
      if (enabledModules != null && (enabledModules == null || enabledModules.size() != 0)) {
         Module toCheck = this.isReverseY(sr) ? enabledModules.get(enabledModules.size() - 1) : enabledModules.get(0);
         return toCheck == null ? 0.0F : (float)font().getStringWidth(getModName(toCheck, this.isTitles())) + 3.5F + 1.0F + 1.5F;
      } else {
         return 0.0F;
      }
   }

   public float getArrayHeight() {
      if (enabledModules != null && (enabledModules == null || enabledModules.size() != 0)) {
         float h = 0.0F;

         for (Module mod : enabledModules) {
            h += this.getArrayStep() * mod.stateAnim.getAnim();
         }

         return h + 0.5F;
      } else {
         return 0.0F;
      }
   }

   private float getArrayStep() {
      return 10.001F;
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      this.setupAlphaPC();
      if (!this.canRenderList()) {
         if (enabledModules == null) {
            this.trigerSort(this.isTitles());
         }
      } else {
         if (System.currentTimeMillis() % 300L < 50L || mc.currentScreen instanceof GuiChat) {
            this.trigerSort(this.isTitles());
         }

         this.setupArrayListCoords(sr);
         int extY = 0;
         float x = listPosX;
         float y = listPosY;
         GL11.glPushMatrix();
         GL11.glDepthMask(false);
         float size = (float)enabledModules.size();
         float aPC = this.getAlphaPC();
         this.drawAList(x, y, false, aPC, sr);
         GlStateManager.enableDepth();
         RenderUtils.fixShadows();
         GL11.glDepthMask(true);
         GL11.glPopMatrix();
      }
   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      this.frameCounter.renderThreadRead((int)MathUtils.clamp(this.frameCounter.getFps() / 3.33333F, 10.0, 50.0));
      ScaledResolution sr = event.getResolution();
      if (this.Information.getBool() && this.Info.currentMode.equalsIgnoreCase("Sleek") && this.actived) {
         CFontRenderer font = Fonts.roboto_13;
         String coords = "§7XYZ:§r "
            + (int)Minecraft.player.posX
            + ","
            + (Minecraft.player.posY == (double)((int)Minecraft.player.posY) ? (int)Minecraft.player.posY : String.format("%.1f", Minecraft.player.posY))
            + ","
            + (int)Minecraft.player.posZ;
         double posX = Minecraft.player.posX;
         double prevX = posX - Minecraft.player.prevPosX;
         double posZ = Minecraft.player.posZ;
         double prevZ = posZ - Minecraft.player.prevPosZ;
         int bgColorForShadow = ColorUtils.getColor(10, 10, 10, 100);
         double bps = Math.sqrt(prevX * prevX + prevZ * prevZ) * 15.3571428571;
         String speed = "§7BPS:§r " + String.format("%.2f", bps);
         String myName = "§7NAME:§r " + Minecraft.player.getName();
         if (mc.currentScreen instanceof GuiChat) {
            this.expand = MathUtils.harp(this.expand, 15.0F, 0.4F);
         } else {
            this.expand = MathUtils.harp(this.expand, 0.0F, 0.2F);
         }

         this.width1 = MathUtils.harp(this.width1, (float)font.getStringWidth(coords), 0.15F);
         this.width2 = MathUtils.harp(this.width2, (float)font.getStringWidth(speed), 0.15F);
         this.width3 = MathUtils.harp(this.width3, (float)font.getStringWidth(myName), 0.15F);
         GL11.glPushMatrix();
         GL11.glTranslated(1.0, (double)(-this.expand), 0.0);
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            3.5F,
            (float)sr.getScaledHeight() - 11.5F,
            6.5F + this.width1,
            (float)(sr.getScaledHeight() - 4),
            0.0F,
            2.0F,
            bgColorForShadow,
            bgColorForShadow,
            bgColorForShadow,
            bgColorForShadow,
            false,
            true,
            true
         );
         font.drawString(coords, 5.0, (double)((float)sr.getScaledHeight() - 8.5F), ColorUtils.getColor(155, 155, 155, 155));
         font.drawString(coords, 5.0, (double)(sr.getScaledHeight() - 9), ColorUtils.getColor(255, 255, 255));
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            3.5F + this.width1 + 7.0F,
            (float)sr.getScaledHeight() - 11.5F,
            6.5F + this.width1 + this.width2 + 7.0F,
            (float)(sr.getScaledHeight() - 4),
            0.0F,
            2.0F,
            bgColorForShadow,
            bgColorForShadow,
            bgColorForShadow,
            bgColorForShadow,
            false,
            true,
            true
         );
         font.drawString(speed, (double)(5.0F + this.width1 + 7.0F), (double)((float)sr.getScaledHeight() - 8.5F), ColorUtils.getColor(155, 155, 155, 155));
         font.drawString(speed, (double)(5.0F + this.width1 + 7.0F), (double)(sr.getScaledHeight() - 9), ColorUtils.getColor(255, 255, 255));
         RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
            3.5F + this.width1 + 7.0F + this.width2 + 7.0F,
            (float)sr.getScaledHeight() - 11.5F,
            6.5F + this.width1 + this.width2 + 7.0F + this.width3 + 7.0F,
            (float)(sr.getScaledHeight() - 4),
            0.0F,
            2.0F,
            bgColorForShadow,
            bgColorForShadow,
            bgColorForShadow,
            bgColorForShadow,
            false,
            true,
            true
         );
         font.drawString(
            myName,
            (double)(5.0F + this.width1 + 7.0F + this.width2 + 7.0F),
            (double)((float)sr.getScaledHeight() - 8.5F),
            ColorUtils.getColor(155, 155, 155, 155)
         );
         font.drawString(
            myName, (double)(5.0F + this.width1 + 7.0F + this.width2 + 7.0F), (double)(sr.getScaledHeight() - 9), ColorUtils.getColor(255, 255, 255)
         );
         GL11.glPopMatrix();
      }

      if (this.Information.getBool() && this.Info.currentMode.equalsIgnoreCase("Plastic") && this.actived) {
         CFontRenderer font = Fonts.mntsb_12;
         String coords = "§7XYZ:§r "
            + (int)Minecraft.player.posX
            + ", "
            + (Minecraft.player.posY == (double)((int)Minecraft.player.posY) ? (int)Minecraft.player.posY : String.format("%.1f", Minecraft.player.posY))
            + ", "
            + (int)Minecraft.player.posZ;
         double prevX = Minecraft.player.posX - Minecraft.player.prevPosX;
         double prevZ = Minecraft.player.posZ - Minecraft.player.prevPosZ;
         int bgColorForShadow = ColorUtils.getColor(10, 10, 10, 100);
         double bps = Math.sqrt(prevX * prevX + prevZ * prevZ) * 15.3571428571;
         if (Speed.get.actived && Speed.get.AntiCheat.getMode().equalsIgnoreCase("Strict")) {
            bps *= (double)(1.0F + (Minecraft.player.ticksExisted % 6 > 2 ? 0.4F : 0.0F));
         }

         bps *= mc.timer.speed * mc.timer.tempSpeed;
         String speed = "§7BPS:§r " + (bps == 0.0 ? "0.0" : String.format("%.2f", bps));
         String myName = "§7NAME:§r " + Minecraft.player.getName();
         String ping = "§7PING:§r "
            + (
               Minecraft.player.connection.getPlayerInfo(Minecraft.player.getUniqueID()) != null
                  ? Minecraft.player.connection.getPlayerInfo(Minecraft.player.getUniqueID()).getResponseTime()
                  : "0"
            );
         String tps = "§7TPS:§r " + TPSDetect.getTpsString();
         String uid = "§7UID:§r " + Client.uid;
         if (mc.currentScreen instanceof GuiChat) {
            if (MathUtils.getDifferenceOf(this.expand, 16.0F) != 0.0) {
               this.expand = MathUtils.harp(this.expand, 16.0F, (float)Minecraft.frameTime * 0.12F);
            }
         } else if (MathUtils.getDifferenceOf(this.expand, 0.0F) != 0.0) {
            this.expand = MathUtils.harp(this.expand, 0.0F, (float)Minecraft.frameTime * 0.12F);
         }

         int y = sr.getScaledHeight() - 19 - (int)this.expand;
         font.drawStringWithShadow(speed, 3.5, (double)y, -1);
         y += 6;
         font.drawStringWithShadow(coords, 3.5, (double)y, -1);
         y += 6;
         font.drawStringWithShadow(myName, 3.5, (double)y, -1);
         y -= 7;
         font.drawStringWithShadow(tps, (double)(sr.getScaledWidth() - 3 - Fonts.mntsb_12.getStringWidth(tps)), (double)y, -1);
         y += 7;
         font.drawStringWithShadow(ping, (double)((float)sr.getScaledWidth() - 14.5F - (float)Fonts.mntsb_12.getStringWidth(ping)), (double)y, -1);
         RenderUtils.drawPlayerPing((float)(sr.getScaledWidth() - 13), (float)y - 4.0F, Minecraft.player, 255.0F);
         y -= 14;
         font.drawStringWithShadow(uid, (double)(sr.getScaledWidth() - 3 - Fonts.mntsb_12.getStringWidth(uid)), (double)y, -1);
      }

      if (this.Information.getBool() && this.Info.currentMode.equalsIgnoreCase("Akrien") && this.actived) {
         if (mc.currentScreen instanceof GuiChat) {
            this.expand = MathUtils.harp(this.expand, 16.0F, 0.4F);
         } else {
            this.expand = MathUtils.harp(this.expand, 0.0F, 0.2F);
         }

         GL11.glPushMatrix();
         GL11.glTranslated(1.0, (double)(-this.expand), 0.0);
         String coordsx = TextFormatting.WHITE
            + "Coords: "
            + TextFormatting.GREEN
            + (int)Minecraft.player.posX
            + ","
            + (int)Minecraft.player.posY
            + ","
            + (int)Minecraft.player.posZ
            + " "
            + TextFormatting.RED
            + "("
            + (int)Minecraft.player.posX / 8
            + ","
            + (int)Minecraft.player.posY
            + ","
            + (int)Minecraft.player.posZ / 8
            + ")";
         String coords2 = "Coords: "
            + (int)Minecraft.player.posX
            + ","
            + (int)Minecraft.player.posY
            + ","
            + (int)Minecraft.player.posZ
            + " ("
            + (int)Minecraft.player.posX / 8
            + ","
            + (int)Minecraft.player.posY
            + ","
            + (int)Minecraft.player.posZ / 8
            + ")";
         if (mc.world.getBiome(new BlockPos(Minecraft.player)).getBiomeName().equalsIgnoreCase("Hell")) {
            coordsx = TextFormatting.WHITE
               + "Coords: "
               + TextFormatting.GREEN
               + (int)(Minecraft.player.posX * 8.0)
               + ","
               + (int)Minecraft.player.posY
               + ","
               + (int)(Minecraft.player.posZ * 8.0)
               + " "
               + TextFormatting.RED
               + "("
               + (int)Minecraft.player.posX
               + ","
               + (int)Minecraft.player.posY
               + ","
               + (int)Minecraft.player.posZ
               + ")";
            coords2 = "Coords: "
               + (int)(Minecraft.player.posX * 8.0)
               + ","
               + (int)Minecraft.player.posY
               + ","
               + (int)(Minecraft.player.posZ * 8.0)
               + " ("
               + (int)Minecraft.player.posX
               + ","
               + (int)Minecraft.player.posY
               + ","
               + (int)Minecraft.player.posZ
               + ")";
         }

         int colorOutline = ColorUtils.getColor(20, 20, 20, 100);
         Fonts.roboto_13.drawString(coords2, 2.5, (double)(sr.getScaledHeight() - 7), colorOutline);
         Fonts.roboto_13.drawString(coords2, 1.5, (double)(sr.getScaledHeight() - 7), colorOutline);
         Fonts.roboto_13.drawString(coords2, 2.0, (double)((float)sr.getScaledHeight() - 6.5F), colorOutline);
         Fonts.roboto_13.drawString(coords2, 2.0, (double)((float)sr.getScaledHeight() - 7.5F), colorOutline);
         Fonts.roboto_13.drawString(coordsx, 2.0, (double)(sr.getScaledHeight() - 7), -1);
         double posX = Minecraft.player.posX;
         double prevXx = posX - Minecraft.player.prevPosX;
         double posZ = Minecraft.player.posZ;
         double prevZx = posZ - Minecraft.player.prevPosZ;
         float bpsx = (float)((int)(Math.sqrt(prevXx * prevXx + prevZx * prevZx) * 15.3571428571 * 2.0));
         String speed = "Blocks/s: " + TextFormatting.GRAY + String.format("%.1f", bpsx / 2.0F);
         String speed2 = "Blocks/s: " + String.format("%.1f", bpsx / 2.0F);
         Fonts.roboto_13.drawString(speed2, 2.5, (double)(sr.getScaledHeight() - 15), colorOutline);
         Fonts.roboto_13.drawString(speed2, 1.5, (double)(sr.getScaledHeight() - 15), colorOutline);
         Fonts.roboto_13.drawString(speed2, 2.0, (double)((float)sr.getScaledHeight() - 14.5F), colorOutline);
         Fonts.roboto_13.drawString(speed2, 2.0, (double)((float)sr.getScaledHeight() - 15.5F), colorOutline);
         Fonts.roboto_13.drawString(speed, 2.0, (double)(sr.getScaledHeight() - 15), -1);
         GlStateManager.resetColor();
         GL11.glPopMatrix();
      }

      if (this.Information.getBool() && this.Info.currentMode.equalsIgnoreCase("Modern") && this.actived) {
         CFontRenderer fontx = Fonts.noise_14;
         float xl = 7.0F;
         float xOld = 7.0F;
         if (mc.currentScreen instanceof GuiChat) {
            this.expand = MathUtils.harp(this.expand, 16.0F, (float)Minecraft.frameTime * 0.06F);
         } else {
            this.expand = MathUtils.harp(this.expand, 0.0F, (float)Minecraft.frameTime * 0.06F);
         }

         float y = (float)(sr.getScaledHeight() - 6 - fontx.getHeight()) - this.expand;
         String coordsx = (int)Minecraft.player.posX + "," + (int)Minecraft.player.posY + "," + (int)Minecraft.player.posZ;
         double posX = Minecraft.player.posX;
         double prevXx = posX - Minecraft.player.prevPosX;
         double posZ = Minecraft.player.posZ;
         double prevZx = posZ - Minecraft.player.prevPosZ;
         int bgColorForShadowx = ColorUtils.getColor(10, 10, 10, 100);
         double bpsx = Math.sqrt(prevXx * prevXx + prevZx * prevZx) * 15.3571428571;
         String speed = String.format("%.2f", bpsx);
         String myName = this.frameCounter.getFpsString(false);
         int colorN = ColorUtils.getColor(0, 0, 50, 65);
         int colorP = ColorUtils.getColor(0, 0, 45, 45);
         RenderUtils.resetBlender();
         RenderUtils.drawAlphedRect(
            (double)(xl - 1.5F),
            (double)(y - 3.5F),
            (double)(xl + (float)fontx.getStringWidth("XYZ: ") - 1.0F),
            (double)(y + (float)fontx.getHeight()) + 1.5,
            colorN
         );
         fontx.drawVGradientString("XYZ: ", (double)xl, (double)y, ColorUtils.getColor(100, 100, 255), ColorUtils.getColor(255, 100, 255));
         xl += (float)fontx.getStringWidth("XYZ: ");
         RenderUtils.drawAlphedRect(
            (double)(xl - 1.0F),
            (double)(y - 3.5F),
            (double)(xl + (float)fontx.getStringWidth(coordsx) + 0.5F),
            (double)(y + (float)fontx.getHeight()) + 1.5,
            colorP
         );
         fontx.drawVHGradientString(
            coordsx,
            (double)xl,
            (double)y,
            ColorUtils.getColor(100, 100, 255),
            ColorUtils.getColor(255, 255, 90),
            ColorUtils.getColor(255, 20, 0),
            ColorUtils.getColor(255, 100, 255)
         );
         xl += (float)fontx.getStringWidth(coordsx);
         xl++;
         RenderUtils.drawAlphedRect(
            (double)(xl - 1.0F),
            (double)(y - 3.5F),
            (double)(xl + (float)fontx.getStringWidth("BPS: ") - 1.0F),
            (double)(y + (float)fontx.getHeight()) + 1.5,
            colorN
         );
         fontx.drawVGradientString("BPS: ", (double)xl, (double)y, ColorUtils.getColor(255, 255, 90), ColorUtils.getColor(255, 0, 0));
         xl += (float)fontx.getStringWidth("BPS: ");
         RenderUtils.drawAlphedRect(
            (double)(xl - 1.0F),
            (double)(y - 3.5F),
            (double)(xl + (float)fontx.getStringWidth(speed) + 0.5F),
            (double)(y + (float)fontx.getHeight()) + 1.5,
            colorP
         );
         fontx.drawVHGradientString(
            speed,
            (double)xl,
            (double)y,
            ColorUtils.getColor(255, 255, 90),
            ColorUtils.getColor(90, 255, 90),
            ColorUtils.getColor(0, 55, 255),
            ColorUtils.getColor(255, 0, 0)
         );
         xl += (float)fontx.getStringWidth(speed);
         xl++;
         RenderUtils.drawAlphedRect(
            (double)(xl - 1.0F),
            (double)(y - 3.5F),
            (double)(xl + (float)fontx.getStringWidth("FPS: ") - 1.0F),
            (double)(y + (float)fontx.getHeight()) + 1.5,
            colorN
         );
         fontx.drawVGradientString("FPS: ", (double)xl, (double)y, ColorUtils.getColor(90, 255, 90), ColorUtils.getColor(0, 55, 255));
         xl += (float)fontx.getStringWidth("FPS: ");
         RenderUtils.drawAlphedRect(
            (double)(xl - 1.0F),
            (double)(y - 3.5F),
            (double)(xl + (float)fontx.getStringWidth(myName) + 2.0F),
            (double)(y + (float)fontx.getHeight()) + 1.5,
            colorP
         );
         fontx.drawVHGradientString(
            myName,
            (double)xl,
            (double)y,
            ColorUtils.getColor(90, 255, 90),
            ColorUtils.getColor(100, 155, 255),
            ColorUtils.getColor(155, 255, 255),
            ColorUtils.getColor(0, 55, 255)
         );
         xl += (float)fontx.getStringWidth(myName);
         RenderUtils.drawBloomedFullShadowFullGradientRectBool(
            xOld - 1.5F, y - 3.5F, xl + 2.0F, y + (float)fontx.getHeight() + 1.5F, 2.5F, 0, 0, 0, 0, 0, 40, false, false, true
         );
         RenderUtils.resetBlender();
      }

      if (this.Watermark.getBool()) {
         float speedAnim = (float)Minecraft.frameTime * 0.04F;
         float curX = this.WX.getFloat() * (float)sr.getScaledWidth();
         float curY = this.WY.getFloat() * (float)sr.getScaledHeight();
         float curW = 0.0F;
         float curH = 0.0F;
         float x = wmPosX;
         float y = wmPosY;
         float x2 = x + wmWidth;
         float y2 = y + wmHeight;
         String mode = this.MarkMode.currentMode;
         if (mode.equalsIgnoreCase("Default")) {
            CFontRenderer fontx = Fonts.neverlose500_13;
            String wm = "Вегуля :3 | " + this.frameCounter.getFpsString(true);
            curW = (float)fontx.getStringWidth(wm) + 13.5F;
            curH = 9.5F;
            int c1 = ColorUtils.getColor(0, 0, 0, 140);
            int c2 = ColorUtils.getColor(255, 255, 255);
            ArrayList<Vec2f> vecs = new ArrayList<>();
            vecs.add(new Vec2f(x + curH / 2.0F + 0.5F, y));
            vecs.add(new Vec2f(x + curH - 1.5F, y));
            vecs.add(new Vec2f(x + curH / 2.0F - 1.5F, y2));
            vecs.add(new Vec2f(x + 0.5F, y2));
            RenderUtils.drawSome(vecs, c2);
            vecs.clear();
            vecs.add(new Vec2f(x + curH / 2.0F + 2.0F, y));
            vecs.add(new Vec2f(x2, y));
            vecs.add(new Vec2f(x2 - curH / 2.0F, y2));
            vecs.add(new Vec2f(x + 2.0F, y2));
            RenderUtils.drawSome(vecs, c1);
            vecs.clear();
            fontx.drawStringWithShadow(wm, (double)(x + 8.0F), (double)(y + 3.5F), -1);
         } else if (mode.equalsIgnoreCase("Chess")) {
            CFontRenderer fontx = Fonts.roadrage_36;
            String name = "VEGALINE";
            float index = 0.0F;
            int col1 = ColorUtils.getColor(130, 130, 130);
            int col2 = ColorUtils.getColor(255, 255, 255);
            curW = (float)(fontx.getStringWidth("VEGALINE") + 2);
            curH = 23.0F;

            for (char lolik : "VEGALINE".toCharArray()) {
               int c = ColorUtils.fadeColor(col2, col1, 0.3F, 1000 - (int)(index * 5.0F));
               fontx.drawStringWithShadow(lolik + "", (double)(x + index), (double)y + 0.5, c);
               index += (float)fontx.getStringWidth(String.valueOf(lolik));
            }
         } else if (mode.equalsIgnoreCase("Sweet")) {
            CFontRenderer fontx = Fonts.noise_18;
            CFontRenderer font2 = Fonts.comfortaa_12;
            int c = ColorUtils.getOverallColorFrom(ClientColors.getColor1(), ColorUtils.getColor(0), 0.8F);
            int c2 = ColorUtils.getOverallColorFrom(ClientColors.getColor2(), ColorUtils.getColor(0, 100), 0.8F);
            int c5 = ClientColors.getColor1();
            int c6 = ClientColors.getColor2();
            float w1 = (float)fontx.getStringWidth("Вегуля [" + this.frameCounter.getFpsString(true) + "]") + 2.5F;
            float w2 = (float)font2.getStringWidth("как всегда на высоте!") + 2.5F;
            float w = w1 > w2 ? w1 : w2;
            curW = w;
            curH = 17.0F;
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(x, y, x2, y2, 3.0F, 0.5F, c, c2, c2, c, false, true, true);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               x + 1.5F, y + 2.0F, x + 2.0F, y2 - 2.0F, 0.0F, 1.0F, c5, c5, c6, c6, true, true, true
            );
            fontx.drawClientColoredString("Вегуля [" + this.frameCounter.getFpsString(true) + "]", (double)(x + 4.5F), (double)(y + 1.5F), 1.0F, false);
            font2.drawClientColoredString("как всегда на высоте!", (double)(x + 5.0F), (double)(y + 11.0F), 1.0F, false);
            GlStateManager.enableDepth();
            GL11.glDepthMask(true);
         } else if (mode.equalsIgnoreCase("Bloom")) {
            CFontRenderer fontx = Fonts.comfortaaRegular_22;
            CFontRenderer font2 = Fonts.comfortaaRegular_12;
            String str = "VEGALINE";
            String str2 = "v" + Client.version.replace("#00", "") + " : " + this.frameCounter.getFpsString(true);
            float w = (float)fontx.getStringWidth("VEGALINE");
            float w2 = (float)font2.getStringWidth(str2);
            curH = 21.5F;
            curW = w + 3.0F;
            float e = x + w / 2.0F > (float)(sr.getScaledWidth() / 2) ? w - w2 - 3.0F : 0.0F;
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO);
            int col = ClientColors.getColor1((int)(x * 4.0F + w / 2.0F));
            int col2 = ClientColors.getColor2((int)(x * 4.0F + w / 2.0F));
            int color = ColorUtils.getOverallColorFrom(col, col2, 0.5F);
            BloomUtil.renderShadow(() -> {
               fontx.drawString("VEGALINE", (double)x, (double)(y + 3.0F), -1);
               font2.drawString(str2, (double)(x + e + 2.5F), (double)(y + 15.0F), -1);
            }, color, 5, 0, 2.8F, false);
            fontx.drawString("VEGALINE", (double)x, (double)(y + 3.0F), color);
            font2.drawString(str2, (double)(x + e + 2.5F), (double)(y + 15.0F), color);
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.enableDepth();
         } else if (mode.equalsIgnoreCase("Clock")) {
            Calendar celendar = Calendar.getInstance();
            String hour = String.valueOf(celendar.getTime().getHours());
            if (Integer.parseInt(hour) < 10) {
               hour = "0" + hour;
            }

            String mins = String.valueOf(celendar.getTime().getMinutes());
            if (Integer.parseInt(mins) < 10) {
               mins = "0" + mins;
            }

            String sec = String.valueOf(celendar.getTime().getSeconds());
            if (Integer.parseInt(sec) < 10) {
               sec = "0" + sec;
            }

            String time1 = hour + ":" + mins;
            String ampm = Integer.parseInt(hour) < 12 ? "AM" : "PM";
            CFontRenderer fontx = Fonts.time_30;
            CFontRenderer font2 = Fonts.time_17;
            CFontRenderer font3 = Fonts.time_14;
            float w = (float)fontx.getStringWidth(time1);
            float w2 = (float)font2.getStringWidth(sec);
            if ((float)font3.getStringWidth(ampm) > w2) {
               w2 = (float)font3.getStringWidth(ampm);
            }

            int c = ClientColors.getColor1();
            fontx.drawClientColoredString(time1, (double)(x + 1.0F), (double)(y + 7.0F), 1.0F, false);
            font2.drawClientColoredString(sec, (double)(x + w + 2.0F), (double)(y + 12.0F), 1.0F, false, (int)(w * 5.0F), true);
            font3.drawClientColoredString(ampm, (double)(x + w + 2.0F), (double)(y + 6.5F), 1.0F, false, (int)(w * 5.0F), true);
            curH = 17.5F;
            curW = w + w2 + 3.0F;
         } else if (!mode.equalsIgnoreCase("Wonderful")) {
            if (mode.equalsIgnoreCase("Plate")) {
               CFontRenderer fontx = Fonts.smallestpixel_24;
               CFontRenderer font2 = Fonts.smallestpixel_16;
               String name = "ВЕГУЛЯ " + this.frameCounter.getFpsString(true);
               String addTop = "ЛУЧШИЙ ИЗ ЛУЧШИХ";
               float w = (float)fontx.getStringWidth(name);
               float w2 = (float)font2.getStringWidth(addTop);
               if (w2 > w) {
                  w = w2;
               }

               w += 5.0F;
               w2 += 5.0F;
               int cli1 = ClientColors.getColor1(0);
               int cli2 = ClientColors.getColor2(-324);
               int cli3 = ClientColors.getColor2(0);
               int cli4 = ClientColors.getColor1(972);
               RenderUtils.resetBlender();
               StencilUtil.initStencilToWrite();
               fontx.drawString(name, (double)(x + 4.0F), (double)(y + 2.0F), -1);
               font2.drawString(addTop, (double)(x + 4.0F), (double)(y + 12.5F), -1);
               StencilUtil.readStencilBuffer(0);
               RenderUtils.drawVGradientRect(x, y + 2.0F, x + 2.0F, y2 - 2.0F, cli1, cli4);
               RenderUtils.drawAlphedSideways((double)x, (double)(y2 - 2.0F), (double)(x2 - 2.0F), (double)y2, cli4, cli3);
               RenderUtils.drawLightContureRectSmooth((double)x, (double)(y + 2.0F), (double)(x2 - 2.0F), (double)y2, ColorUtils.getColor(0, 0, 0, 90));
               cli1 = ColorUtils.getOverallColorFrom(cli1, -1, 0.7F);
               cli2 = ColorUtils.getOverallColorFrom(cli2, -1, 0.7F);
               cli3 = ColorUtils.getOverallColorFrom(cli3, -1, 0.8F);
               cli4 = ColorUtils.getOverallColorFrom(cli4, -1, 0.8F);
               RenderUtils.drawFullGradientRectPro(x + 2.0F, y, x2, y2 - 2.0F, cli2, cli1, cli3, cli4, false);
               RenderUtils.drawLightContureRectSmooth((double)(x + 2.0F), (double)y, (double)x2, (double)(y2 - 2.0F), ColorUtils.getColor(0, 0, 0, 90));
               StencilUtil.uninitStencilBuffer();
               curH = 19.5F;
               curW = w;
            }
         } else {
            float w = wmWidth;
            float h = wmHeight;
            float[] smoothTime = this.getSmoothTimeValues();
            float cRange = 11.0F;
            float cx = x + cRange;
            float cy = y + cRange;
            float apc = 0.15F;
            int bgCol1 = ColorUtils.getOverallColorFrom(ClientColors.getColor1(0, apc), ColorUtils.getColor(0, (int)(255.0F * apc)), 0.35F);
            int bgCol2 = ColorUtils.getOverallColorFrom(ClientColors.getColor2(0, apc), ColorUtils.getColor(0, (int)(255.0F * apc)), 0.35F);
            RenderUtils.drawRoundedFullGradientShadowFullGradientRoundedFullGradientRectWithBloomBool(
               x, y, x2, y2, cRange, 1.0F, bgCol1, bgCol2, bgCol2, bgCol1, false, true, true
            );
            GL11.glPushMatrix();
            GL11.glDisable(3553);
            GL11.glEnable(3042);
            GL11.glDisable(3008);
            GL11.glEnable(2832);
            GL11.glPointSize(2.0F);
            int radiansMax = 320;
            int radian360 = 220;
            GL11.glBegin(0);

            while (radian360 < radiansMax) {
               float pcAlpha = 1.0F - (float)MathUtils.getDifferenceOf(270, radian360) / 40.0F;
               RenderUtils.setupColor(-1, 225.0F * pcAlpha + 30.0F);
               double calcX = -Math.sin(Math.toRadians((double)radian360)) * (double)(cRange + 2.0F);
               double calcY = Math.cos(Math.toRadians((double)radian360)) * (double)(cRange + 2.0F);
               GL11.glVertex2d((double)cx + calcX, (double)cy + calcY);
               radian360 += 2;
            }

            GL11.glEnd();
            GL11.glPointSize(1.0F);
            GlStateManager.resetColor();
            GL11.glEnable(3008);
            GL11.glEnable(3553);
            GL11.glPopMatrix();
            this.drawClockPoints(cx, cy, 2.5F, ColorUtils.getColor(255, 195), cRange - 1.5F);
            this.drawClockArrow(
               cx, cy, 2.5F, ColorUtils.getColor(155, 190, 255), ColorUtils.getColor(155, 190, 255, 125), cRange - 4.0F, smoothTime[0] * 30.0F + 180.0F
            );
            this.drawClockArrow(
               cx, cy, 1.75F, ColorUtils.getColor(255, 255, 0), ColorUtils.getColor(255, 255, 0, 125), cRange - 3.0F, smoothTime[1] * 6.0F + 180.0F
            );
            this.drawClockArrow(
               cx, cy, 1.75F, ColorUtils.getColor(255, 10, 10), ColorUtils.getColor(255, 50, 50, 125), cRange - 2.0F, smoothTime[2] * 6.0F + 180.0F
            );
            RenderUtils.drawSmoothCircle((double)cx, (double)cy, 1.0F, ColorUtils.getColor(185, 125));
            CFontRenderer fontx = Fonts.comfortaaBold_18;
            String str = "Вегуля / \t" + this.frameCounter.getFpsString(true);
            fontx.drawStringWithShadow(str, (double)(x + cRange * 2.0F + 6.0F), (double)(y + 8.0F), -1);
            curH = 22.0F;
            curW = cRange * 2.0F + 11.0F + (float)fontx.getStringWidth(str);
         }

         wmPosX = MathUtils.harp(wmPosX, curX, speedAnim);
         wmPosY = MathUtils.harp(wmPosY, curY, speedAnim);
         wmWidth = MathUtils.harp(wmWidth, curW, speedAnim);
         wmHeight = MathUtils.harp(wmHeight, curH, speedAnim);
      }

      if (this.Potions.getBool()) {
         this.updatePotionsList();
         float curX = this.PX.getFloat() * (float)sr.getScaledWidth();
         float curY = this.PY.getFloat() * (float)sr.getScaledHeight();
         this.potionsHeight.to = getPotionHudHeight();
         float x = potPosX;
         float y = potPosY;
         float w = getPotionHudWidth();
         float wd = w;
         CFontRenderer fontName = Fonts.mntsb_16;
         CFontRenderer fontPot = Fonts.mntsb_12;
         int icoOffset = 9;
         int icoSize = 9;
         int durrCircleSize = 6;

         for (Hud.PotionWithString pot : this.potionsWithString) {
            float offX = (float)durrCircleSize * (0.5F + pot.alphaPC.getAnim() * 0.5F);
            if (this.onDoDrawPotionEffectIcon(true, x + 4.0F, 0.0F, (float)icoSize, (float)icoSize, pot.getPotion().getPotion(), true)) {
               offX += (float)icoOffset * (0.5F + pot.alphaPC.getAnim() * 0.5F);
            }

            if (!((float)fontPot.getStringWidth(pot.getName()) + offX < wd)) {
               wd = (float)fontPot.getStringWidth(pot.getName()) + offX;
            }
         }

         if (wd + 8.0F > w) {
            w = wd + 8.0F;
         }

         this.potionsHeight.speed = 0.2F;
         float h = this.potionsHeight.getAnim();
         int bgC = ColorUtils.getColor(7, 7, 7, 140);
         int gradC1 = ClientColors.getColor1();
         int gradC2 = ClientColors.getColor2(50);
         this.clientRect(x, y, x + w, y + h, "Potions");
         float yp = y + 25.0F - 10.0F;
         if (this.potionsWithString.size() == 0) {
            String lots = TextFormatting.DARK_GRAY + "Potions is empty";
            fontPot.drawStringWithShadow(lots, (double)(x + 3.0F), (double)(yp + 2.5F), -1);
         } else {
            GL11.glPushMatrix();

            for (Hud.PotionWithString pot : this.potionsWithString) {
               if (pot != null) {
                  float alphaPC = MathUtils.clamp(pot.alphaPC.anim * 1.3333F, 0.0F, 1.0F);
                  int textCol = ColorUtils.getColor(255, (int)(255.0F * alphaPC));
                  float icoSize2 = (float)icoSize * alphaPC;
                  float durrCircleScale = (float)durrCircleSize * (0.5F + alphaPC * 0.5F);
                  float offXx = 3.0F;
                  if (this.onDoDrawPotionEffectIcon(
                     true,
                     x + 4.0F + (float)icoSize / 4.0F,
                     yp + 0.5F + (float)icoSize / 4.0F,
                     icoSize2,
                     (float)icoSize * 2.0F,
                     pot.getPotion().getPotion(),
                     false
                  )) {
                     offXx += (float)icoOffset * (0.5F + alphaPC * 0.5F);
                  }

                  String name = MathUtils.getStringPercent(pot.getName(), alphaPC * 1.1F);
                  float cx = x + w - 3.0F - (float)durrCircleSize / 2.0F;
                  float cy = yp + (durrCircleScale / 2.0F + 0.5F) * alphaPC;
                  RenderUtils.drawSmoothCircle((double)cx, (double)cy, (float)durrCircleSize / 2.0F, ColorUtils.swapAlpha(0, 60.0F * alphaPC));
                  RenderUtils.drawClientCircleWithOverallToColor(
                     cx,
                     (double)cy,
                     ((float)durrCircleSize / 2.0F - 1.0F) * alphaPC,
                     360.0F * pot.getDurrationPC(),
                     1.0E-4F + 2.0F * alphaPC,
                     alphaPC / 2.3F,
                     ColorUtils.getOverallColorFrom(ColorUtils.getProgressColor(pot.getDurrationPC()).getRGB(), -1),
                     1.0F
                  );
                  if (pot.getPotion().getPotion().isBadEffect()) {
                     RenderUtils.drawSmoothCircle(
                        (double)cx, (double)cy, (float)durrCircleSize / 2.0F - 2.0F, ColorUtils.getColor(255, 60, 60, 255.0F * alphaPC)
                     );
                  }

                  if (255.0F * alphaPC >= 33.0F) {
                     RenderUtils.customScaledObject2DPro(
                        x + offXx + (float)fontPot.getStringWidth(name), yp, (float)fontPot.getStringWidth(name), 6.0F, 1.0F, alphaPC
                     );
                     fontPot.drawStringWithShadow(name, (double)(x + offXx), (double)(yp + 2.5F), textCol);
                     RenderUtils.customScaledObject2DPro(
                        x + offXx + (float)fontPot.getStringWidth(name), yp, (float)fontPot.getStringWidth(name), 6.0F, 1.0F, 1.0F / alphaPC
                     );
                  }

                  yp += 9.0F * alphaPC;
               }
            }

            GL11.glPopMatrix();
         }

         GlStateManager.enableDepth();
         GL11.glDepthMask(true);
         float speedAnim = (float)Minecraft.frameTime * 0.04F;
         potPosX = MathUtils.harp(potPosX, curX, speedAnim);
         potPosY = MathUtils.harp(potPosY, curY, speedAnim);
         potWidth = MathUtils.harp(potWidth, w, speedAnim);
         potHeight = MathUtils.harp(potHeight, h, speedAnim);
      }

      if (this.ArmorHUD.getBool() && Minecraft.player != null) {
         float curX = this.AX.getFloat() * (float)sr.getScaledWidth();
         float curY = this.AY.getFloat() * (float)sr.getScaledHeight();
         int itemScalePix = 16;
         List<ItemStack> stacks = new CopyOnWriteArrayList<>();
         boolean isVertical = MathUtils.getDifferenceOf(curX, 0.0F) < (double)itemScalePix && curY > 12.0F;

         for (int i = 0; i < 4; i++) {
            stacks.add(Minecraft.player.inventory.armorItemInSlot(isVertical ? 3 - i : i));
         }

         if (MathUtils.getDifferenceOf(curX + 8.0F, (float)sr.getScaledWidth()) < (double)(stacks.size() * itemScalePix) && curY > 12.0F) {
            isVertical = true;
            Collections.reverse(stacks);
         }

         float xPos = armPosX;
         float yPos = armPosY;
         GlStateManager.disableDepth();
         float indexColor = 0.5F;
         if (!isVertical
            && yPos > 6.0F
            && (!(yPos > (float)(sr.getScaledHeight() - 58)) || !(MathUtils.getDifferenceOf(curX, (float)sr.getScaledWidth() / 2.0F) < 94.0))) {
            float texW = (float)Fonts.minecraftia_16.getStringWidth("Armor");
            GlStateManager.pushMatrix();
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glTranslated((double)xPos * 2.0 + (double)itemScalePix * 4.0, (double)yPos * 2.0, 0.0);
            int bg = ColorUtils.getColor(0, 80);
            int c1 = ClientColors.getColor1(0, 0.6F);
            int c2 = ClientColors.getColor2(0, 0.6F);
            int c3 = ClientColors.getColor1(0, 0.1F);
            int c4 = ClientColors.getColor2(0, 0.1F);
            int col1 = ColorUtils.getOverallColorFrom(c1, c2, indexColor - 0.275F);
            int col2 = ColorUtils.getOverallColorFrom(c1, c2, indexColor + 0.275F);
            int col3 = ColorUtils.getOverallColorFrom(c3, c4, indexColor + 0.275F);
            int col4 = ColorUtils.getOverallColorFrom(c3, c4, indexColor - 0.275F);
            List<Vec2fColored> vertexes = Arrays.asList(
               new Vec2fColored(-texW / 2.0F - 4.0F, -13.0F, col1),
               new Vec2fColored(texW / 2.0F + 4.0F, -13.0F, col2),
               new Vec2fColored(texW / 2.0F + 10.0F, 0.0F, col3),
               new Vec2fColored(-texW / 2.0F - 10.0F, 0.0F, col4)
            );
            RenderUtils.drawVec2Colored(vertexes);
            vertexes = Arrays.asList(
               new Vec2fColored(-texW / 2.0F - 3.0F, -12.0F, bg),
               new Vec2fColored(texW / 2.0F + 3.0F, -12.0F, bg),
               new Vec2fColored(texW / 2.0F + 9.0F, 0.0F, bg),
               new Vec2fColored(-texW / 2.0F - 9.0F, 0.0F, bg)
            );
            RenderUtils.drawVec2Colored(vertexes);
            Fonts.minecraftia_16.drawClientColoredString("Armor", (double)(-texW / 2.0F), -10.0, 1.0F, true, (int)(armWidth / 2.0F - texW / 2.0F));
            GlStateManager.popMatrix();
         }

         GlStateManager.pushMatrix();
         RenderItem itemRender = mc.getRenderItem();
         float zlevel = itemRender.zLevel;
         RenderUtils.enableGUIStandardItemLighting();
         GlStateManager.enableDepth();
         if (stacks.size() != 0 && isVertical) {
            yPos -= (float)itemScalePix;
         } else if (stacks.size() != 0) {
            xPos -= (float)itemScalePix;
         }

         int indexTex = isVertical ? 4 : 9;
         int index = 0;
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferbuilder = tessellator.getBuffer();

         for (ItemStack stack : stacks) {
            indexTex += isVertical ? 1 : -1;
            indexColor = ((float)index + 0.5F) / (float)stacks.size();
            index++;
            if (stack != null) {
               if (isVertical) {
                  yPos += (float)itemScalePix;
               } else {
                  xPos += (float)itemScalePix;
               }

               String count = stack.stackSize > 1 ? stack.stackSize + "" : "";
               itemRender.zLevel = 200.0F;
               GlStateManager.translate(xPos, yPos, 0.0F);
               RenderUtils.disableStandardItemLighting();
               int c1 = ClientColors.getColor1(0);
               int c2 = ClientColors.getColor2(0);
               int c3 = ClientColors.getColor1(0, 0.4F);
               int c4 = ClientColors.getColor2(0, 0.4F);
               int col1 = ColorUtils.getOverallColorFrom(c1, c2, indexColor);
               int col2 = ColorUtils.getOverallColorFrom(c3, c4, indexColor);
               RenderUtils.drawLightContureRectSmooth(1.0, 1.0, 15.0, 15.0, col1);
               RenderUtils.drawAlphedRect(1.5, 1.5, 14.5, 14.5, ColorUtils.toDark(col2, 0.6F));
               if (stack.getItem() != Items.air) {
                  RenderUtils.enableStandardItemLighting();
                  itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
                  itemRender.renderItemOverlayIntoGUI(mc.fontRendererObj, stack, 0, 0, "");
                  RenderUtils.disableStandardItemLighting();
                  GlStateManager.scale(0.5, 0.5, 0.5);
                  if (stack != null && stack.isItemDamaged()) {
                     float armDamage = ((float)stack.getMaxDamage() - (float)stack.getItemDamage()) / (float)stack.getMaxDamage();
                     String armInfo = (int)(armDamage * 100.0F) + "%";
                     int colorStatus = ColorUtils.getProgressColor(armDamage * (0.75F + armDamage / 4.0F)).getRGB();
                     if (armDamage * 100.0F <= 5.0F) {
                        colorStatus = ColorUtils.fadeColor(ColorUtils.getColor(255, 20, 20), -1, 2.0F);
                     }

                     GlStateManager.disableDepth();
                     Fonts.minecraftia_16
                        .drawStringWithOutline(armInfo, (double)(16.0F - (float)(Fonts.minecraftia_16.getStringWidth(armInfo) / 2)), 12.0, colorStatus);
                     GlStateManager.enableDepth();
                  }

                  if (stack.stackSize > 1) {
                     GlStateManager.disableDepth();
                     Fonts.minecraftia_16.drawStringWithShadow(count, 18.0, 18.0, -1);
                     GlStateManager.enableDepth();
                  }

                  int reserveArmCounter = 0;
                  if (stack.getItem() != Items.air) {
                     for (int i = 9; i < 36; i++) {
                        ItemStack checkStack = Minecraft.player.inventory.getStackInSlot(i);
                        if (checkStack != stack && checkStack.getItem() == stack.getItem()) {
                           reserveArmCounter += checkStack.stackSize;
                           if (reserveArmCounter >= 64) {
                              break;
                           }
                        }
                     }
                  }

                  if (reserveArmCounter != 0) {
                     if (reserveArmCounter > 64) {
                        reserveArmCounter = 64;
                     }

                     int speedMS = reserveArmCounter > 1 ? 1500 : 500;
                     float colTimePC = ((float)System.currentTimeMillis() + indexColor * armWidth) % (float)speedMS / (float)speedMS;
                     int countC = ColorUtils.getOverallColorFrom(col1, -1, ((double)colTimePC > 0.5 ? 1.0F - colTimePC : colTimePC) * 2.0F);
                     GlStateManager.disableDepth();
                     RenderUtils.drawSmoothCircle(5.5, 5.5, 3.25F, countC);
                     RenderUtils.drawSmoothCircle(5.5, 5.5, 2.75F, ColorUtils.getColor(0, 190));
                     Fonts.minecraftia_16
                        .drawStringWithShadow(
                           reserveArmCounter + "", (double)(6.0F - (float)Fonts.minecraftia_16.getStringWidth(reserveArmCounter + "") / 2.0F), 2.0, countC
                        );
                     GlStateManager.enableDepth();
                  }

                  GlStateManager.scale(2.0F, 2.0F, 2.0F);
               }

               String iconName;
               if (stack.func_190926_b() && (iconName = ((Slot)Minecraft.player.inventoryContainer.inventorySlots.get(indexTex)).getSlotTexture()) != null) {
                  mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                  GlStateManager.enableBlend();
                  GL11.glBlendFunc(770, 1);
                  GL11.glShadeModel(7425);
                  TextureAtlasSprite textureSprite = mc.getTextureMapBlocks().getAtlasSprite(iconName);
                  bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                  bufferbuilder.pos(4.0, 12.0).tex((double)textureSprite.getMinU(), (double)textureSprite.getMaxV()).color(col1).endVertex();
                  bufferbuilder.pos(12.0, 12.0).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMaxV()).color(col1).endVertex();
                  bufferbuilder.pos(12.0, 4.0).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMinV()).color(col2).endVertex();
                  bufferbuilder.pos(4.0, 4.0).tex((double)textureSprite.getMinU(), (double)textureSprite.getMinV()).color(col2).endVertex();
                  tessellator.draw();
                  GL11.glShadeModel(7424);
                  GL11.glBlendFunc(770, 771);
               }

               RenderUtils.drawItemWarnIfLowDur(stack, 0.0F, 0.0F, 1.0F, 1.0F);
               GlStateManager.translate(-xPos, -yPos, 0.0F);
            }
         }

         itemRender.zLevel = zlevel;
         if (stacks.size() != 0 && isVertical) {
            yPos -= (float)(itemScalePix * stacks.size());
         } else if (stacks.size() != 0) {
            xPos -= (float)(itemScalePix * stacks.size());
         }

         GlStateManager.enableAlpha();
         RenderUtils.resetBlender();
         GL11.glEnable(3042);
         GlStateManager.popMatrix();
         float curW = (float)(itemScalePix * (!isVertical ? stacks.size() : 1));
         float curH = (float)(itemScalePix * (isVertical ? stacks.size() : 1));
         stacks.clear();
         float speedAnim = (float)Minecraft.frameTime * 0.04F;
         armPosX = MathUtils.harp(armPosX, curX, speedAnim);
         armPosY = MathUtils.harp(armPosY, curY, speedAnim);
         armWidth = MathUtils.harp(armWidth, curW, speedAnim);
         armHeight = MathUtils.harp(armHeight, curH, speedAnim);
      }

      if (this.StaffList.getBool()) {
         float curX = this.SX.getFloat() * (float)sr.getScaledWidth();
         float curY = this.SY.getFloat() * (float)sr.getScaledHeight();
         this.staffsHeight.to = this.getStaffHudHeight();
         float x = stPosX;
         float y = stPosY;
         float w = this.getStaffHudWidth();
         float wd = w;
         CFontRenderer fontStaff = Fonts.mntsb_12;

         for (Hud.StaffPlayer staff : this.getStaffList()) {
            String sName = this.staffDisplay(staff);
            sName = sName.replace("  ", " ").replace("§l", "").replace("[]", "").replace("§k", "").replace("§m", "").replace("§n", "").replace("§o", "");
            float nameW = (float)((staff.getSkinLoc() != null ? 9 : 0) + fontStaff.getStringWidth(sName));
            if (!(nameW < wd)) {
               wd = nameW;
            }
         }

         if (wd + 8.0F > w) {
            w = wd + 8.0F;
         }

         this.staffsHeight.speed = 0.4F;
         float h = this.staffsHeight.getAnim();
         this.clientRect(x, y, x + w, y + h, "Staff list");
         float yp = y + 25.0F - 10.0F;
         GlStateManager.disableDepth();
         if (this.getStaffList().size() == 0) {
            String lots = TextFormatting.DARK_GRAY + "Staffs is empty";
            fontStaff.drawStringWithShadow(lots, (double)(x + 3.0F), (double)(yp + 2.5F), -1);
         } else {
            GL11.glPushMatrix();

            for (Hud.StaffPlayer staffx : this.getStaffList()) {
               staffx.alphaPC.getAnim();
               String sName = this.staffDisplay(staffx);
               sName = sName.replace("  ", " ").replace("§l", "").replace("[]", "").replace("§k", "").replace("§m", "").replace("§n", "").replace("§o", "");
               float ext = 1.5F * (1.0F - staffx.alphaPC.getAnim()) + 0.5F;
               int ALP = (int)(255.0F * staffx.alphaPC.getAnim());
               if (staffx.toRemove) {
                  ALP /= 2;
               }

               float xp = x;
               if (ALP > 10 && staffx.getSkinLoc() != null) {
                  mc.getTextureManager().bindTexture(staffx.getSkinLoc());
                  GL11.glPushMatrix();
                  RenderUtils.setupColor(-1, (float)ALP / 1.25F);
                  GL11.glDisable(3008);
                  GL11.glEnable(3553);
                  GL11.glDisable(2929);
                  float headScale = 8.0F;
                  float headExtOverlay = 0.5F;
                  RenderUtils.customScaledObject2DPro(x + 3.5F, yp, headScale, headScale, staffx.alphaPC.anim, staffx.alphaPC.anim);
                  GL11.glTranslated((double)(x + 3.5F), (double)yp, 0.0);
                  Gui.drawScaledCustomSizeModalRect(0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F, headScale, headScale, 64.0F, 64.0F);
                  Gui.drawScaledCustomSizeModalRect(
                     -headExtOverlay,
                     -headExtOverlay,
                     39.0F,
                     8.0F,
                     10.0F,
                     8.0F,
                     headScale + headExtOverlay * 2.0F,
                     headScale + headExtOverlay * 2.0F,
                     64.0F,
                     64.0F
                  );
                  GL11.glEnable(3008);
                  GL11.glEnable(2929);
                  GlStateManager.resetColor();
                  GL11.glPopMatrix();
                  xp = x + 9.0F;
               }

               if (ALP >= 33) {
                  RenderUtils.customScaledObject2DPro(xp, yp, 0.0F, 9.0F, 1.0F, staffx.alphaPC.anim);
                  fontStaff.drawStringWithShadow(
                     MathUtils.getStringPercent(sName, staffx.alphaPC.anim * 1.1F),
                     (double)(xp + 4.0F),
                     (double)(yp + 3.0F - ext),
                     ColorUtils.getColor(255, 255, 255, ALP)
                  );
                  RenderUtils.customScaledObject2DPro(xp, yp, 0.0F, 9.0F, 1.0F, 1.0F / staffx.alphaPC.anim);
               }

               yp += 9.0F * staffx.alphaPC.getAnim();
            }

            GL11.glPopMatrix();
         }

         GlStateManager.enableDepth();
         GL11.glDepthMask(true);
         float speedAnim = (float)Minecraft.frameTime * 0.04F;
         stPosX = MathUtils.harp(stPosX, curX, speedAnim);
         stPosY = MathUtils.harp(stPosY, curY, speedAnim);
         stWidth = MathUtils.harp(stWidth, w, speedAnim);
         stHeight = MathUtils.harp(stHeight, h, speedAnim);
      }

      if (this.KeyBinds.getBool()) {
         if (System.currentTimeMillis() % 250L <= 50L) {
            this.setupBindsList();
         }

         float curX = this.KX.getFloat() * (float)sr.getScaledWidth();
         float curY = this.KY.getFloat() * (float)sr.getScaledHeight();
         this.keybindsHeight.to = this.getKeyBindsHudHeight();
         float x = kbPosX;
         float y = kbPosY;
         float w = this.getKeyBindsHudWidth();
         float wd = w;
         CFontRenderer fontName = Fonts.mntsb_16;
         CFontRenderer fontKB = Fonts.mntsb_12;

         for (Module mod : this.bindsList) {
            if (!((float)fontKB.getStringWidth(this.getKeyBingModName(mod)) < wd)) {
               wd = (float)fontKB.getStringWidth(this.getKeyBingModName(mod));
            }
         }

         if (wd + 8.0F > w) {
            w = wd + 8.0F;
         }

         this.keybindsHeight.speed = 0.35F;
         float h = this.keybindsHeight.getAnim();
         int bgC = ColorUtils.getColor(7, 7, 7, 140);
         int gradC1 = ClientColors.getColor1();
         int gradC2 = ClientColors.getColor2(50);
         GL11.glPushMatrix();
         GL11.glDepthMask(false);
         this.clientRect(x, y, x + w, y + h, "Keybinds");
         float yp = y + 25.0F - 10.0F;
         if (this.bindsList.size() != 0 && (this.bindsList.size() != 1 || !(this.bindsList.get(0).stateAnim.getAnim() < 0.15F))) {
            int ix = 0;
            int rd = 200;

            for (Module modx : this.bindsList) {
               float addSO = MathUtils.clamp(modx.stateAnim.getAnim() * 1.05F, 0.0F, 1.0F);
               ix++;
               if ((float)rd * addSO >= 33.0F) {
                  float extx = 4.5F * (1.0F - modx.stateAnim.anim);
                  if ((float)rd * addSO >= 33.0F) {
                     fontKB.drawStringWithShadow(
                        MathUtils.getStringPercent(modx.getName(), addSO),
                        (double)(x + 4.0F),
                        (double)(yp + 3.0F - extx),
                        ColorUtils.getColor(255, 255, 255, (float)rd * addSO)
                     );
                  }

                  RenderUtils.customScaledObject2D(
                     x + w - 4.0F - (float)fontKB.getStringWidth("{" + Keyboard.getKeyName(modx.getBind()) + "}"),
                     yp - extx,
                     (float)fontKB.getStringWidth("{" + Keyboard.getKeyName(modx.getBind()) + "}"),
                     8.0F,
                     addSO
                  );
                  if ((float)rd * addSO >= 33.0F) {
                     fontKB.drawStringWithShadow(
                        "{" + Keyboard.getKeyName(modx.getBind()) + "}",
                        (double)(x + w - 4.0F - (float)fontKB.getStringWidth("{" + Keyboard.getKeyName(modx.getBind()) + "}")),
                        (double)(yp + 2.5F - extx),
                        ColorUtils.getColor(255, 255, 255, (float)rd * addSO)
                     );
                  }

                  RenderUtils.customScaledObject2D(
                     x + w - 4.0F - (float)fontKB.getStringWidth("{" + Keyboard.getKeyName(modx.getBind()) + "}"),
                     yp - extx,
                     (float)fontKB.getStringWidth("{" + Keyboard.getKeyName(modx.getBind()) + "}"),
                     8.0F,
                     1.0F / addSO
                  );
               }

               yp += 9.0F * addSO;
            }
         } else {
            String lots = TextFormatting.DARK_GRAY + "Binds not enabled";
            fontKB.drawStringWithShadow(lots, (double)(x + 3.0F), (double)(yp + 2.5F), -1);
         }

         GL11.glDepthMask(true);
         GL11.glEnable(2929);
         GL11.glPopMatrix();
         float speedAnim = (float)Minecraft.frameTime * 0.04F;
         kbPosX = MathUtils.harp(kbPosX, curX, speedAnim);
         kbPosY = MathUtils.harp(kbPosY, curY, speedAnim);
         kbWidth = MathUtils.harp(kbWidth, w, speedAnim);
         kbHeight = MathUtils.harp(kbHeight, h, speedAnim);
      }

      if (this.PickupsList.getBool()) {
         float curX = this.PCX.getFloat() * (float)sr.getScaledWidth();
         float curY = this.PCY.getFloat() * (float)sr.getScaledHeight();
         this.pickupsHeight.to = this.getPickupsHudHeight();
         float x = pcPosX;
         float y = pcPosY;
         float w = this.getPickupsHudWidth();
         CFontRenderer fontKB = Fonts.mntsb_12;
         float h = this.pickupsHeight.getAnim();
         GL11.glPushMatrix();
         GL11.glDepthMask(false);
         this.clientRect(x, y, x + w, y + h, "Pickups list");
         float yp = y + 25.0F - 10.0F;
         if (this.notifysList.size() != 0 && (this.notifysList.size() != 1 || !(this.notifysList.get(0).alphaPC.getAnim() < 0.15F))) {
            float textureSizeFinal = 8.0F;
            int counter = 0;
            float prevXx = x;
            RenderItem itemRenderx = mc.getRenderItem();
            y = yp;

            for (Hud.PickupItem pick : this.notifysList) {
               if (y <= (float)sr.getScaledHeight() && x <= (float)sr.getScaledWidth()) {
                  this.drawPickStack(pick.stack, pick.alphaPC.getAnim(), x + 1.0F, y, itemRenderx);
               }

               if (counter % 10 == 0) {
                  yp += textureSizeFinal;
               }

               if (counter % 10 != 9) {
                  x += textureSizeFinal * (pick.alphaPC.to == 1.0F ? 1.0F : pick.alphaPC.getAnim());
               } else {
                  y += 8.0F * pick.alphaPC.getAnim();
                  x = prevXx;
               }

               counter++;
            }

            yp += textureSizeFinal * 2.0F;
         } else {
            String lots = TextFormatting.DARK_GRAY + "Picks is empty";
            fontKB.drawStringWithShadow(lots, (double)(x + 3.0F), (double)(yp + 2.5F), -1);
         }

         GL11.glDepthMask(true);
         GL11.glEnable(2929);
         GL11.glPopMatrix();
         float speedAnim = (float)Minecraft.frameTime * 0.04F;
         pcPosX = MathUtils.harp(pcPosX, curX, speedAnim);
         pcPosY = MathUtils.harp(pcPosY, curY, speedAnim);
         pcWidth = MathUtils.harp(pcWidth, w, speedAnim);
         pcHeight = MathUtils.harp(pcHeight, h, speedAnim);
      }

      if (this.LagDetect.getBool() && this.laggAlphaPC.to == 0.0F && this.laggCheck.hasReached(1000.0) && !mc.isGamePaused()) {
         this.laggAlphaPC.to = 1.0F;
      }

      if (this.LagDetect.getBool() && this.laggAlphaPC.getAnim() * 255.0F > 31.0F) {
         String str = (mc.isSingleplayer() ? "Loading client" : "Server lagged") + " " + (int)(this.laggCheck.getTime() / 1000L) + "s";
         CFontRenderer fontx = Fonts.mntsb_36;
         float w = (float)fontx.getStringWidth(str);
         float h = (float)fontx.getStringHeight(str);
         float x = (float)(sr.getScaledWidth() / 2) - w / 2.0F;
         float y = (float)(sr.getScaledHeight() / 8);
         int c = ColorUtils.swapAlpha(ColorUtils.getFixedWhiteColor(), 255.0F * this.laggAlphaPC.getAnim() * this.laggAlphaPC.getAnim());
         float APC = (float)ColorUtils.getAlphaFromColor(c) / 255.0F;
         if (APC * 255.0F > 31.0F) {
            GL11.glPushMatrix();
            GL11.glDepthMask(false);
            RenderUtils.customScaledObject2DPro(x, y, w, h, 1.2F - 0.2F * APC, APC);
            fontx.drawString(str, (double)x, (double)y, c);
            RenderUtils.customScaledObject2DPro(x, y, w, h, 1.0F / (1.2F - 0.2F * APC), 1.0F / APC);
            RenderUtils.customScaledObject2DPro(x, y, w, h, 1.1F - 0.1F * APC, APC);
            BloomUtil.renderShadow(() -> fontx.drawString(str, (double)x, (double)y, c), c, 4 + (int)(6.0F * APC), 0, 1.75F * APC * APC, false);
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
         }
      }
   }

   private void drawAList(float x, float y, boolean silent, float alphaPC, ScaledResolution sr) {
      boolean resersedX = this.isReverseX(sr);
      float arrayWidth = this.getArrayWidth(sr);
      int index = 0;

      for (Module mod : enabledModules) {
         this.drawModule(x, y, mod, this.getArrayStep(), index, silent, alphaPC, sr, resersedX, arrayWidth);
         y += this.getArrayStep() * mod.stateAnim.getAnim();
         index++;
      }
   }

   public boolean isArraylist() {
      return get != null && this.ArrayList.getBool() && enabledModules != null && enabledModules.size() != 0;
   }

   public boolean isHoverToArrayList(int mouseX, int mouseY, ScaledResolution sr) {
      float x1 = listPosX - this.getArrayWidth(sr);
      float x2 = listPosX;
      float y1 = listPosY;
      float y2 = listPosY + this.getArrayHeight();
      return this.actived && this.isArraylist() && RenderUtils.isHovered((float)mouseX, (float)mouseY, x1, y1, x2 - x1, y2 - y1);
   }

   private void drawModule(
      float x, float y, Module mod, float shag, int index, boolean silent, float alphaPC, ScaledResolution sr, boolean reversedX, float arrayWidth
   ) {
      alphaPC *= mod.stateAnim.getAnim() / 2.0F + 0.5F;
      float gd = MathUtils.clamp(Math.abs((float)index / (float)enabledModules.size()), 0.0F, 1.0F);
      float extX = 2.0F;
      String modName = getModName(mod, this.isTitles());
      float w = (float)font().getStringWidth(modName) + extX * 2.0F;
      int colTex = ColorUtils.getOverallColorFrom(
         ClientColors.getColor1((int)((float)index * shag), alphaPC), ClientColors.getColor2((int)((float)index * shag), alphaPC), gd
      );
      int col1 = ColorUtils.getOverallColorFrom(colTex, ColorUtils.getColor(0, 0, 0, 100.0F * alphaPC), 0.4F);
      int col2 = ColorUtils.getOverallColorFrom(colTex, ColorUtils.getColor(0, 0, 0, 100.0F * alphaPC), 0.7F);
      int c1 = ColorUtils.swapAlpha(col1, 70.0F * alphaPC);
      int c2 = ColorUtils.swapAlpha(col2, 155.0F * alphaPC);
      x -= reversedX ? arrayWidth : w + 1.0F;
      float ext = shag * mod.stateAnim.getAnim();
      if (silent) {
         RenderUtils.drawRect((double)x, (double)y, (double)(x + w), (double)(y + ext), -1);
      } else {
         RenderUtils.drawAlphedSideways((double)x, (double)y, (double)(x + w), (double)(y + ext), reversedX ? c2 : c1, reversedX ? c1 : c2, true);
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_CONSTANT_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderUtils.drawAlphedRect((double)(x + (reversedX ? -0.5F : w)), (double)y, (double)(x + (reversedX ? -0.5F : w) + 1.0F), (double)(y + ext), colTex);
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      }

      if (!silent && 255.0F * alphaPC > 26.0F) {
         float scaleText = MathUtils.clamp(mod.stateAnim.getAnim() * 1.005F, 0.1F, 1.0F);
         if (scaleText == 0.1F) {
            return;
         }

         RenderUtils.customScaledObject2D(x, y, w, shag, scaleText);
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_CONSTANT_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         if (255.0F * alphaPC >= 33.0F) {
            font()
               .drawStringWithShadow(
                  modName,
                  (double)(x + extX),
                  (double)(y + shag / 2.0F - 2.0F - (1.0F - mod.stateAnim.getAnim()) * shag / 3.0F),
                  ColorUtils.swapAlpha(colTex, MathUtils.clamp(255.0F * alphaPC, 33.0F, 255.0F))
               );
         }

         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderUtils.customScaledObject2D(x, y, w, shag, 1.0F / scaleText);
      }
   }

   @Override
   public void onUpdate() {
      if (this.PickupsList.getBool()) {
         this.picksRemoveAuto();
      }

      if (this.StaffList.getBool() && Minecraft.player != null) {
         this.getStaffList().forEach(Hud.StaffPlayer::update);
         this.updateStaffsGetedList(this.updatedStaffsToGetList());
      }
   }

   boolean isIsNotNulledString(String stringIn) {
      return stringIn != null && !stringIn.isEmpty();
   }

   boolean lowerContains(String first, String second) {
      return first.toLowerCase().contains(second.toLowerCase());
   }

   boolean stringOverlapHasResult(List<String> strings, String string, boolean checkToLower) {
      String opString = this.isIsNotNulledString(string) && checkToLower ? string.toLowerCase() : string;
      return strings.size() != 0 && (strings.size() == 1 ? this.isIsNotNulledString(strings.get(0)) && this.lowerContains(strings.get(0), opString) : strings.stream().filter(inList -> this.isIsNotNulledString((String)inList) && this.lowerContains((String)inList, opString)).collect(Collectors.toList()).size() != 0);
   }

   private String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
      return networkPlayerInfoIn.getDisplayName() != null
         ? networkPlayerInfoIn.getDisplayName().getFormattedText()
         : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
   }

   private boolean hasPrefix(NetworkPlayerInfo networkPlayerInfoIn) {
      return networkPlayerInfoIn.getPlayerTeam() != null && !networkPlayerInfoIn.getPlayerTeam().getColorPrefix().isEmpty();
   }

   private boolean hasPrefix(ScorePlayerTeam playerTeam) {
      return playerTeam.getColorPrefix() != null && !playerTeam.getColorPrefix().isEmpty();
   }

   String getDecoloredPrefix(String prefix) {
      return prefix.replace("§0", "")
         .replace("§1", "")
         .replace("§2", "")
         .replace("§3", "")
         .replace("§4", "")
         .replace("§5", "")
         .replace("§6", "")
         .replace("§7", "")
         .replace("§8", "")
         .replace("§9", "")
         .replace("§a", "")
         .replace("§b", "")
         .replace("§c", "")
         .replace("§d", "")
         .replace("§e", "")
         .replace("§f", "")
         .replace("§k", "")
         .replace("§l", "")
         .replace("§m", "")
         .replace("§n", "")
         .replace("§o", "")
         .replace("§r", "");
   }

   boolean isStaffInPlayerInfo(NetworkPlayerInfo info, List<String> staffEqualsPrefix, List<String> staffEqualsName) {
      if (info == null) {
         return false;
      } else if (info.getPlayerTeam() == null || info.getPlayerTeam().getTeamName().length() < 2) {
         return false;
      } else if (info.getDisplayName() != null && this.isIsNotNulledString(info.getDisplayName().getUnformattedText())) {
         String name = this.getPlayerName(info).toLowerCase();
         String prefix = this.getDecoloredPrefix(info.getPlayerTeam().getColorPrefix()).toLowerCase();
         if (!this.isIsNotNulledString(prefix)) {
            return false;
         } else {
            boolean prefixHandle = staffEqualsPrefix.stream().anyMatch(pref -> prefix.contains(pref.toLowerCase()));
            boolean nameHandle = staffEqualsName.stream().anyMatch(named -> name.contains(named.toLowerCase()));
            return name != null && (this.hasPrefix(info) && prefixHandle || nameHandle);
         }
      } else {
         return false;
      }
   }

   boolean isStaffInScoreboard(ScorePlayerTeam playerTeam, List<String> staffEqualsPrefix, List<String> staffEqualsName, List<String> onlinePlayersNames) {
      if (playerTeam != null && this.isIsNotNulledString(playerTeam.getTeamName())) {
         String name = playerTeam.getRegisteredName();
         String prefix = this.getDecoloredPrefix(playerTeam.getColorPrefix()).toLowerCase();
         if (!this.isIsNotNulledString(prefix)) {
            return false;
         } else {
            boolean prefixHandle = staffEqualsPrefix.stream().anyMatch(pref -> prefix.contains(pref.toLowerCase()));
            boolean nameHandle = staffEqualsName.stream().anyMatch(named -> name.contains(named.toLowerCase()));
            return name != null && this.hasPrefix(playerTeam) && (prefixHandle || nameHandle);
         }
      } else {
         return false;
      }
   }

   void addStaffList(ArrayList<Hud.StaffPlayer> staffsList, NetworkPlayerInfo infoNet) {
      if (infoNet != null && infoNet.getDisplayName() != null) {
         String displayName = (infoNet.getPlayerTeam() == null
               ? infoNet.getDisplayName().getFormattedText()
               : infoNet.getPlayerTeam().getColorPrefix() + infoNet.getGameProfile().getName() + infoNet.getPlayerTeam().getColorSuffix())
            .replace("  ", " ")
            .replace("§l", "")
            .replace("[", "")
            .replace("]", "")
            .replace("§k", "")
            .replace("§m", "")
            .replace("§n", "")
            .replace("§o", "");
         staffsList.add(
            new Hud.StaffPlayer(
               displayName,
               infoNet.getGameProfile().getName(),
               false,
               infoNet.getGameType() == null ? GameType.SURVIVAL : infoNet.getGameType(),
               infoNet.getLocationSkin()
            )
         );
      }
   }

   void addStaffList(ArrayList<Hud.StaffPlayer> staffsList, ScorePlayerTeam playerTeam) {
      if (playerTeam != null && this.isIsNotNulledString(playerTeam.getColorPrefix())) {
         String name = Arrays.asList(playerTeam.getMembershipCollection().stream().toArray()).toString();
         staffsList.add(
            new Hud.StaffPlayer(
               (playerTeam.getColorPrefix() + name + playerTeam.getColorSuffix()).replace("[", "").replace("]", ""),
               name.replace("[", "").replace("]", ""),
               true,
               GameType.SURVIVAL,
               null
            )
         );
      }
   }

   void addStaffList(Hud.StaffPlayer staff) {
      this.getStaffList().add(staff);
   }

   List<String> getStaffEqualPrefixes() {
      return Arrays.asList("help", "mod", "adm", "yt", "мод", "адм", "хелп", "стаж", "власт", "барон", "мажор", "аладин", "hydra", "господ", "правит");
   }

   List<String> getStaffWarnEqualPrefixes() {
      return Arrays.asList("гл.", "ст.", "st.", "mod", "adm", "yt");
   }

   List<String> getStaffNameEqualsList() {
      return new ArrayList<>();
   }

   ArrayList<Hud.StaffPlayer> updatedStaffsToGetList() {
      ArrayList<Hud.StaffPlayer> staffsSpawned = new ArrayList<>();
      Collection<ScorePlayerTeam> teamsPlayers = mc.world.getScoreboard().getTeams();
      List<String> staffEqualPrefixes = this.getStaffEqualPrefixes();
      List<String> nameEqualsList = this.getStaffNameEqualsList();
      List<String> onlinePlayersNames = Minecraft.player
         .connection
         .getPlayerInfoMap()
         .stream()
         .map(NetworkPlayerInfo::getGameProfile)
         .<String>map(GameProfile::getName)
         .filter(profileName -> this.validUserPattern.matcher(profileName).matches())
         .collect(Collectors.toList());
      List<NetworkPlayerInfo> onlinePlayers = Minecraft.player
         .connection
         .getPlayerInfoMap()
         .stream()
         .filter(profileName -> this.validUserPattern.matcher(profileName.getGameProfile().getName()).matches())
         .collect(Collectors.toList());
      onlinePlayers.stream()
         .filter(player -> this.isStaffInPlayerInfo(player, staffEqualPrefixes, nameEqualsList))
         .forEach(staff -> this.addStaffList(staffsSpawned, staff));
      List<Hud.StaffPlayer> notSpec = staffsSpawned.stream().filter(staff -> !staff.vanished).collect(Collectors.toList());
      teamsPlayers.stream()
         .filter(team -> this.isStaffInScoreboard(team, staffEqualPrefixes, nameEqualsList, onlinePlayersNames))
         .forEach(
            staff -> {
               if (notSpec.stream()
                  .noneMatch(
                     testStaff -> Arrays.asList(staff.getMembershipCollection().stream().toArray())
                           .stream()
                           .anyMatch(score -> this.getDecoloredPrefix(score.toString()).contains(testStaff.name))
                  )) {
                  this.addStaffList(staffsSpawned, staff);
               }
            }
         );
      return staffsSpawned;
   }

   void updateStaffsGetedList(ArrayList<Hud.StaffPlayer> getedStaffs) {
      int maxListSise = 20;
      boolean isNotifications = Notifications.get.isActived();
      if (this.staffPlayers.size() < 20) {
         List<String> staffPlayerNamesGeted = this.staffPlayers.stream().map(Hud.StaffPlayer::getName).collect(Collectors.toList());
         getedStaffs.stream().filter(staffToGet -> !this.stringOverlapHasResult(staffPlayerNamesGeted, staffToGet.name, false)).forEach(staffToGet -> {
            this.addStaffList(staffToGet);
            if (isNotifications) {
               Notifications.Notify.spawnNotify(TextFormatting.RED + "Обнаружен " + TextFormatting.RESET + staffToGet.getName(), Notifications.type.STAFF);
               this.staffDetectSound = true;
            }
         });
      }

      if (this.staffDetectSound) {
         MusicHelper.playSound("staffDetected.wav", 0.3333F);
         this.staffDetectSound = false;
      }

      if (this.staffPlayers.size() != 0) {
         for (Hud.StaffPlayer staff : this.staffPlayers.stream().filter(staffx -> staffx.remove).toList()) {
            staff.setSkinLoc(null);
            if (isNotifications) {
               Notifications.Notify.spawnNotify(
                  TextFormatting.RED + "Стафф " + TextFormatting.RESET + staff.getName() + TextFormatting.RESET + TextFormatting.GREEN + " вышел",
                  Notifications.type.STAFF
               );
               this.staffUpdateSound = true;
            }
         }

         this.staffPlayers.removeIf(staffx -> staffx.remove);
         List<NetworkPlayerInfo> onlinePlayers = Minecraft.player
            .connection
            .getPlayerInfoMap()
            .stream()
            .filter(profileName -> this.validUserPattern.matcher(profileName.getGameProfile().getName()).matches())
            .collect(Collectors.toList());

         for (Hud.StaffPlayer staffGeted : this.staffPlayers) {
            getedStaffs.stream()
               .filter(toGet -> staffGeted.name.equalsIgnoreCase(toGet.name))
               .forEach(
                  toGet -> {
                     boolean trueVanish = toGet.vanished
                        && !onlinePlayers.stream()
                           .anyMatch(player -> player.getGameProfile() != null && player.getGameProfile().getName().equalsIgnoreCase(staffGeted.getName()));
                     GameType trueGameType = toGet.gamemode;
                     GameType gmPass = onlinePlayers.stream()
                        .filter(player -> player.getGameProfile() != null && player.getGameProfile().getName().equalsIgnoreCase(staffGeted.getName()))
                        .map(player -> player.getGameType())
                        .findAny()
                        .orElse(null);
                     if (gmPass != null) {
                        trueGameType = gmPass;
                     }

                     if (staffGeted.vanished != trueVanish) {
                        staffGeted.vanished = trueVanish;
                        if (isNotifications && staffGeted.getTime() > 500L) {
                           Notifications.Notify.spawnNotify(
                              TextFormatting.RED
                                 + "Cтафф "
                                 + TextFormatting.RESET
                                 + staffGeted.getDisplayName()
                                 + " "
                                 + TextFormatting.RESET
                                 + (trueVanish ? TextFormatting.RED + "вошёл в ваниш" : TextFormatting.GREEN + "вышел с ваниша"),
                              Notifications.type.STAFF
                           );
                           this.staffUpdateSound = true;
                        }
                     }

                     if (staffGeted.gamemode != trueGameType) {
                        staffGeted.gamemode = trueGameType;
                        if (gmPass != null && isNotifications && staffGeted.getTime() > 500L) {
                           String gmColor = TextFormatting.GRAY + "";
                           switch (gmPass.getID()) {
                              case -1:
                                 gmColor = TextFormatting.GRAY + "";
                                 break;
                              case 0:
                                 gmColor = TextFormatting.GREEN + "";
                                 break;
                              case 1:
                                 gmColor = TextFormatting.LIGHT_PURPLE + "";
                                 break;
                              case 2:
                                 gmColor = TextFormatting.GOLD + "";
                                 break;
                              case 3:
                                 gmColor = TextFormatting.AQUA + "";
                           }

                           Notifications.Notify.spawnNotify(
                              TextFormatting.RED
                                 + "Cтафф "
                                 + TextFormatting.RESET
                                 + staffGeted.getDisplayName()
                                 + " "
                                 + TextFormatting.RESET
                                 + TextFormatting.GRAY
                                 + "вошёл в "
                                 + gmColor
                                 + "Gm"
                                 + gmPass.getID(),
                              Notifications.type.STAFF
                           );
                           this.staffUpdateSound = true;
                        }
                     }

                     if (!staffGeted.vanished) {
                        ResourceLocation skinLoc = onlinePlayers.stream()
                           .filter(player -> player.getGameProfile() != null && player.getGameProfile().getName().equalsIgnoreCase(staffGeted.getName()))
                           .map(player -> player.getLocationSkin())
                           .findAny()
                           .orElse(null);
                        if (skinLoc != null) {
                           staffGeted.setSkinLoc(skinLoc);
                        }
                     }
                  }
               );
            staffGeted.toRemove = !this.stringOverlapHasResult(
               getedStaffs.stream().map(staffx -> staffx.name).collect(Collectors.toList()), staffGeted.name, false
            );
         }

         if (this.staffUpdateSound) {
            MusicHelper.playSound("staffUpdated.wav", 0.3333F);
            this.staffUpdateSound = false;
         }
      }
   }

   private ArrayList<Hud.StaffPlayer> getStaffList() {
      return this.staffPlayers;
   }

   private String staffDisplay(Hud.StaffPlayer staff) {
      return staff.renderString();
   }

   private List<String> staffDisplays(List<Hud.StaffPlayer> staffs) {
      return staffs.stream().map(Hud.StaffPlayer::renderString).collect(Collectors.toList());
   }

   private void drawPickStack(ItemStack stack, float alphaPC, float x, float y, RenderItem renderer) {
      if (!((double)alphaPC < 0.05)) {
         GL11.glPushMatrix();
         GL11.glScaled(0.5, 0.5, 1.0);
         GL11.glTranslated((double)(x * 2.0F), (double)(y * 2.0F), 1.0);
         GL11.glDepthMask(true);
         RenderUtils.enableGUIStandardItemLighting();
         GlStateManager.enableDepth();
         if ((double)alphaPC > 0.95) {
            alphaPC = 1.0F;
         }

         if (alphaPC != 1.0F) {
            RenderUtils.customScaledObject2D(0.0F, 0.0F, 16.0F, 16.0F, alphaPC);
         }

         renderer.zLevel = 209.0F;
         renderer.renderItemAndEffectIntoGUI(stack, 0, 0);
         GL11.glDepthMask(false);
         GlStateManager.disableDepth();
         Fonts.minecraftia_16
            .drawString(
               stack.getCount(),
               (double)(11.0F - (float)Fonts.minecraftia_16.getStringWidth(stack.getCount()) / 2.0F),
               5.0,
               ColorUtils.getColor(0, 0, 0, 255.0F * alphaPC)
            );
         Fonts.minecraftia_16
            .drawString(
               stack.getCount(),
               (double)(9.0F - (float)Fonts.minecraftia_16.getStringWidth(stack.getCount()) / 2.0F),
               5.0,
               ColorUtils.getColor(0, 0, 0, 255.0F * alphaPC)
            );
         Fonts.minecraftia_16
            .drawString(
               stack.getCount(),
               (double)(10.0F - (float)Fonts.minecraftia_16.getStringWidth(stack.getCount()) / 2.0F),
               5.0,
               ColorUtils.getColor(0, 0, 0, 255.0F * alphaPC)
            );
         Fonts.minecraftia_16
            .drawString(
               stack.getCount(),
               (double)(10.0F - (float)Fonts.minecraftia_16.getStringWidth(stack.getCount()) / 2.0F),
               7.0,
               ColorUtils.getColor(0, 0, 0, 255.0F * alphaPC)
            );
         Fonts.minecraftia_16
            .drawString(
               stack.getCount(),
               (double)(10.0F - (float)Fonts.minecraftia_16.getStringWidth(stack.getCount()) / 2.0F),
               6.0,
               ColorUtils.getColor(255, 255, 255, 255.0F * alphaPC)
            );
         renderer.zLevel = 200.0F;
         GL11.glDepthMask(true);
         GL11.glDisable(3008);
         RenderUtils.disableStandardItemLighting();
         GlStateManager.disableLighting();
         GL11.glPopMatrix();
      }
   }

   private float[] getSmoothTimeValues() {
      Calendar clock = Calendar.getInstance();
      Date date = clock.getTime();
      float sSex = (float)date.getSeconds() + (float)(System.currentTimeMillis() % 1000L) / 1000.0F;
      float sMins = (float)date.getMinutes() + sSex / 60.0F;
      float sHours = (float)date.getHours() + sMins / 60.0F;
      return new float[]{sHours, sMins, sSex};
   }

   private void drawClockArrow(float x, float y, float lineW, int color, int color2, float rangeAtMiddle, float radian360) {
      GL11.glPushMatrix();
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glShadeModel(7425);
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      GL11.glLineWidth(lineW);
      RenderUtils.glColor(color);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(3);
      GL11.glVertex2d((double)x, (double)y);
      RenderUtils.glColor(color2);
      double calcX = -Math.sin(Math.toRadians((double)radian360)) * (double)rangeAtMiddle;
      double calcY = Math.cos(Math.toRadians((double)radian360)) * (double)rangeAtMiddle;
      GL11.glVertex2d((double)x + calcX, (double)y + calcY);
      GL11.glEnd();
      GL11.glHint(3154, 4352);
      GL11.glDisable(2848);
      GlStateManager.resetColor();
      GL11.glLineWidth(1.0F);
      GL11.glEnable(3008);
      GL11.glShadeModel(7424);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glEnable(3553);
      GL11.glPopMatrix();
   }

   private void drawClockPoints(float x, float y, float pointSize, int color, float rangeAtMiddle) {
      GL11.glPushMatrix();
      GL11.glDepthMask(false);
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      GL11.glEnable(2832);
      GL11.glPointSize(pointSize);
      GL11.glDisable(3553);
      int pointsCount = 12;
      int radiansMax = 360;
      int radian360 = 0;
      RenderUtils.glColor(color);
      GL11.glBegin(0);

      while (radian360 < radiansMax) {
         double calcX = -Math.sin(Math.toRadians((double)radian360)) * (double)rangeAtMiddle;
         double calcY = Math.cos(Math.toRadians((double)radian360)) * (double)rangeAtMiddle;
         GL11.glVertex2d((double)x + calcX, (double)y + calcY);
         radian360 += radiansMax / pointsCount;
      }

      GL11.glEnd();
      GL11.glEnable(3553);
      GL11.glPointSize(1.0F);
      GlStateManager.resetColor();
      GL11.glEnable(3008);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glPopMatrix();
   }

   public boolean isPotsCustom() {
      return this.actived && this.Potions != null && this.Potions.getBool();
   }

   public boolean isCustomHotbar() {
      return this.actived && this.CustomHotbar != null && this.CustomHotbar.getBool();
   }

   public boolean isSleekHotbar() {
      return this.settings.get(10) != null && ((ModeSettings)this.settings.get(10)).currentMode.equalsIgnoreCase("Sleek");
   }

   public boolean isArmorHud() {
      return this.actived && this.ArmorHUD != null && this.ArmorHUD.getBool();
   }

   public boolean isStaffListHud() {
      return this.actived && this.StaffList != null && this.StaffList.getBool();
   }

   public boolean isKeyBindsHud() {
      return this.actived && this.KeyBinds != null && this.KeyBinds.getBool();
   }

   public boolean isPickupHud() {
      return this.actived && this.PickupsList != null && this.PickupsList.getBool();
   }

   public boolean isHoveredToPotionsHUD(int mouseX, int mouseY) {
      return this.isPotsCustom() && RenderUtils.isHovered((float)mouseX, (float)mouseY, potPosX, potPosY, potWidth, potHeight);
   }

   public boolean isHoveredToArmorHUD(int mouseX, int mouseY) {
      return this.isArmorHud() && RenderUtils.isHovered((float)mouseX, (float)mouseY, armPosX, armPosY, armWidth, armHeight);
   }

   public boolean isHoveredStaffListHUD(int mouseX, int mouseY) {
      return this.isStaffListHud() && RenderUtils.isHovered((float)mouseX, (float)mouseY, stPosX, stPosY, stWidth, stHeight);
   }

   public boolean isHoveredKeyBindsHUD(int mouseX, int mouseY) {
      return this.isKeyBindsHud() && RenderUtils.isHovered((float)mouseX, (float)mouseY, kbPosX, kbPosY, kbWidth, kbHeight);
   }

   public boolean isHoveredPickupsHUD(int mouseX, int mouseY) {
      return this.isPickupHud() && RenderUtils.isHovered((float)mouseX, (float)mouseY, pcPosX, pcPosY, pcWidth, pcHeight);
   }

   private static float getPotionHudHeight() {
      float h = 16.0F;
      if (Minecraft.player != null && get != null && get.potionsWithString != null) {
         float height = 0.0F;

         for (Hud.PotionWithString potion : get.potionsWithString) {
            float aPC = MathUtils.clamp(potion.alphaPC.getAnim() * 1.1F, 0.0F, 1.0F);
            height += potion.alphaPC.to == 0.0F ? ((double)aPC > 0.3 ? 4.5F + 4.5F * aPC : 9.0F * aPC) : 6.0F + 3.0F * aPC;
         }

         height = height < 9.0F ? 9.0F : height;
         h += height;
      }

      return h;
   }

   private static float getPotionHudWidth() {
      return 75.0F;
   }

   private float getStaffHudHeight() {
      float h = 16.0F;
      float r = 0.0F;
      if (Minecraft.player != null) {
         if (this.getStaffList().size() == 0) {
            r += 9.0F;
         }

         for (Hud.StaffPlayer staff : this.getStaffList()) {
            r += 9.0F * staff.alphaPC.getAnim();
         }
      }

      r = r < 9.0F ? 9.0F : r;
      return h + r;
   }

   private float getStaffHudWidth() {
      return 75.0F;
   }

   String getKeyBingModName(Module mod) {
      return mod.getName() + " [" + Keyboard.getKeyName(mod.getBind()) + "]";
   }

   List<Module> keyBindsMods() {
      return Client.moduleManager
         .getModuleList()
         .stream()
         .filter(m -> (m.actived || m.stateAnim.getAnim() > 0.02F) && m.getBind() != 0)
         .sorted(Comparator.comparingLong(e -> -e.lastEnableTime))
         .toList();
   }

   void setupBindsList() {
      this.bindsList = this.keyBindsMods();
      this.bindsList.forEach(module -> {
         if (module.stateAnim.anim < 0.02F && module.stateAnim.to == 0.0F) {
            module.stateAnim.setAnim(0.0F);
         }

         if (module.stateAnim.anim > 0.98F && module.stateAnim.to == 1.0F) {
            module.stateAnim.setAnim(1.0F);
         }

         module.stateAnim.to = module.actived ? 1.0F : 0.0F;
      });
   }

   private float getKeyBindsHudWidth() {
      float w = 75.0F;

      for (Module mod : this.bindsList) {
         if ((mod.actived || mod.stateAnim.getAnim() > 0.02F) && (float)Fonts.mntsb_12.getStringWidth(this.getKeyBingModName(mod)) > w) {
            w = (float)Fonts.mntsb_12.getStringWidth(this.getKeyBingModName(mod));
         }
      }

      return MathUtils.clamp(w, 75.0F, 175.0F);
   }

   private float getKeyBindsHudHeight() {
      float h = 16.0F;
      float h2 = 0.0F;
      if (this.bindsList.size() == 0) {
         h2 += 9.0F;
      }

      for (Module mod : this.bindsList) {
         h2 += 9.0F * mod.stateAnim.getAnim();
      }

      if (h2 < 9.0F) {
         h2 = 9.0F;
      }

      return h + h2;
   }

   private float getPickupsHudHeight() {
      float h = 16.0F;
      float h2 = 0.0F;
      int counter = 0;

      for (Hud.PickupItem pick : this.notifysList) {
         if (counter % 10 == 0) {
            h2 += 8.0F * (pick.alphaPC.to == 1.0F ? 1.0F : pick.alphaPC.getAnim());
         }

         counter++;
      }

      if (h2 < 9.0F) {
         h2 = 9.0F;
      }

      return h + h2;
   }

   private float getPickupsHudWidth() {
      return 83.0F;
   }

   private int maxNotifyTime() {
      return 20000;
   }

   public void onCollect(EntityItem entityItem, EntityLivingBase whoPicked) {
      if (whoPicked != null && whoPicked instanceof EntityPlayerSP player && this.isPickupHud()) {
         ItemStack stack = entityItem.getItem();
         if (stack.func_190926_b()) {
            return;
         }

         this.notifysList.add(0, new Hud.PickupItem(stack, this.maxNotifyTime()));
      }
   }

   private void picksRemoveAuto() {
      this.notifysList.forEach(pickupx -> {
         if (pickupx.timePC() == 1.0F) {
            pickupx.alphaPC.to = 0.0F;
            pickupx.toRemove = (double)pickupx.alphaPC.getAnim() < 0.01 && pickupx.alphaPC.to == 0.0F;
         }
      });

      for (int i = 0; i < this.notifysList.size(); i++) {
         if (this.notifysList.get(i) == null) {
            return;
         }

         Hud.PickupItem pickup = this.notifysList.get(i);
         if (pickup != null && pickup.toRemove) {
            this.notifysList.remove(i);
         }
      }
   }

   private boolean onDoDrawPotionEffectIcon(boolean bindTex, float x, float y, float size, float extXY, Potion potion, boolean silent) {
      if (potion == null) {
         return false;
      } else if (potion.hasStatusIcon()) {
         if (!silent) {
            if (bindTex) {
               mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
            }

            int indexTex = potion.getStatusIconIndex();
            GL11.glPushMatrix();
            GlStateManager.disableLighting();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslated((double)x, (double)y, 0.0);
            GL11.glScaled(0.05555555555555555, 0.05555555555555555, 1.0);
            GL11.glTranslated((double)extXY / 2.0, (double)extXY / 2.0, 0.0);
            GL11.glScaled((double)size, (double)size, 1.0);
            GL11.glTranslated((double)(-extXY) / 2.0, (double)(-extXY) / 2.0, 0.0);
            new Gui().drawTexturedModalRect(0, 0, indexTex % 8 * 18, 198 + indexTex / 8 * 18, 18, 18);
            GL11.glPopMatrix();
         }

         return true;
      } else {
         return false;
      }
   }

   public void updatePotionsList() {
      Collection<PotionEffect> activeEffectsCollect = Minecraft.player.getActivePotionEffects();
      List<PotionEffect> activeEffectsList = Arrays.asList(activeEffectsCollect.toArray())
         .stream()
         .map(obj -> (PotionEffect)obj)
         .filter(Objects::nonNull)
         .toList();

      for (PotionEffect effect : activeEffectsList) {
         boolean isAdded = this.potionsWithString.stream().anyMatch(addedx -> addedx.equals(effect));
         if (!this.potionsWithString.stream().anyMatch(addedx -> addedx.getPotion().equals(effect))) {
            this.potionsWithString.add(new Hud.PotionWithString(effect));
         }
      }

      for (Hud.PotionWithString added : this.potionsWithString) {
         PotionEffect searched = activeEffectsList.stream().filter(effectx -> added != null && effectx.equals(added.getPotion())).findFirst().orElse(null);
         boolean isSerched = searched != null;
         added.setToRemove(!isSerched);
         if (isSerched) {
            added.updateDurration(searched);
         }
      }

      this.potionsWithString.stream().forEach(Hud.PotionWithString::updateRemoveStatus);
      this.potionsWithString.removeIf(Hud.PotionWithString::isWantToRemove);
   }

   private class PickupItem {
      ItemStack stack;
      boolean toRemove = false;
      AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.1F);
      long startTime = System.currentTimeMillis();
      int maxTime = 8000;

      public PickupItem(ItemStack stack, int maxTime) {
         this.maxTime = maxTime;
         this.stack = stack;
      }

      float timePC() {
         return MathUtils.clamp((float)(System.currentTimeMillis() - this.startTime) / (float)this.maxTime, 0.0F, 1.0F);
      }
   }

   private class PotionWithString {
      AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.04F);
      String name;
      PotionEffect potion;
      boolean toRemove;
      boolean remove;
      int maxTicksDurration;
      int durration;

      public PotionWithString(PotionEffect potion) {
         this.potion = potion;
         String name = potion.getEffectName();
         if (name.contains("luck")) {
            name = "§aLuck§r";
         }

         if (name.contains("moveSpeed")) {
            name = "§bSpeed§r";
         }

         if (name.contains("moveSlowdown")) {
            name = "§7Slowness§r";
         }

         if (name.contains("jump")) {
            name = "§aJumpBoost§r";
         }

         if (name.contains("digSpeed")) {
            name = "§6Haste§r ";
         }

         if (name.contains("digSlowDown")) {
            name = "§7SlowHand§r";
         }

         if (name.contains("damageBoost")) {
            name = "§4Strength§r";
         }

         if (name.contains("heal")) {
            name = "§cInstantHeal§r";
         }

         if (name.contains("harm")) {
            name = "§4InstantDamage§r";
         }

         if (name.contains("confusion")) {
            name = "§7Nausea§r";
         }

         if (name.contains("regeneration")) {
            name = "§dRegeneration§r";
         }

         if (name.contains("resistance")) {
            name = "§eResistance§r";
         }

         if (name.contains("absorption")) {
            name = "§eAbsorption§r";
         }

         if (name.contains("fireResistance")) {
            name = "§6FireResistance§r";
         }

         if (name.contains("waterBreathing")) {
            name = "§3Breathing§r";
         }

         if (name.contains("invisibility")) {
            name = "§fInvisibility§r";
         }

         if (name.contains("blindness")) {
            name = "§7Blindness§r";
         }

         if (name.contains("nightVision")) {
            name = "§9NightVision§r";
         }

         if (name.contains("weakness")) {
            name = "§7Weakness§r";
         }

         if (name.contains("poison")) {
            name = "§2Poison§r";
         }

         if (potion.getAmplifier() != 0) {
            name = name + " ";
         }

         name = name
            + TextFormatting.GRAY
            + I18n.format("enchantment.level." + (potion.getAmplifier() == 0 ? 0 : potion.getAmplifier() + 1), new Object[0])
               .replace("enchantment.level.0", "")
               .replace("enchantment.level.", "");
         name = name.replace("256", "").replace("  ", " ");
         String ampf = "";
         if (potion.getDuration() < 50) {
            ampf = ampf + TextFormatting.RED;
         } else {
            ampf = ampf + TextFormatting.GRAY;
         }

         ampf = ampf + Potion.getPotionDurationString(potion, 1.0F);
         name = name + " ";
         name = name + ampf;
         name = name.replace("  ", " ");
         this.name = name;
         this.maxTicksDurration = potion.getDuration();
      }

      public void updateDurration(PotionEffect updatedPotionEffect) {
         if (updatedPotionEffect != null) {
            if (updatedPotionEffect.getDuration() > this.potion.getDuration()) {
               this.potion = updatedPotionEffect;
               this.maxTicksDurration = this.potion.getDuration();
            }

            String name = this.potion.getEffectName();
            if (name.contains("luck")) {
               name = "§aLuck§r";
            }

            if (name.contains("moveSpeed")) {
               name = "§bSpeed§r";
            }

            if (name.contains("moveSlowdown")) {
               name = "§7Slowness§r";
            }

            if (name.contains("jump")) {
               name = "§aJumpBoost§r";
            }

            if (name.contains("digSpeed")) {
               name = "§6Haste§r ";
            }

            if (name.contains("digSlowDown")) {
               name = "§7SlowHand§r";
            }

            if (name.contains("damageBoost")) {
               name = "§4Strength§r";
            }

            if (name.contains("heal")) {
               name = "§cInstantHeal§r";
            }

            if (name.contains("harm")) {
               name = "§4InstantDamage§r";
            }

            if (name.contains("confusion")) {
               name = "§7Nausea§r";
            }

            if (name.contains("regeneration")) {
               name = "§dRegeneration§r";
            }

            if (name.contains("resistance")) {
               name = "§eResistance§r";
            }

            if (name.contains("absorption")) {
               name = "§eAbsorption§r";
            }

            if (name.contains("fireResistance")) {
               name = "§6FireResistance§r";
            }

            if (name.contains("waterBreathing")) {
               name = "§3Breathing§r";
            }

            if (name.contains("invisibility")) {
               name = "§fInvisibility§r";
            }

            if (name.contains("blindness")) {
               name = "§7Blindness§r";
            }

            if (name.contains("nightVision")) {
               name = "§9NightVision§r";
            }

            if (name.contains("weakness")) {
               name = "§7Weakness§r";
            }

            if (name.contains("poison")) {
               name = "§2Poison§r";
            }

            if (this.potion.getAmplifier() != 0) {
               name = name + " ";
            }

            name = name
               + TextFormatting.GRAY
               + I18n.format("enchantment.level." + (this.potion.getAmplifier() == 0 ? 0 : this.potion.getAmplifier() + 1), new Object[0])
                  .replace("enchantment.level.0", "")
                  .replace("enchantment.level.", "");
            name = name.replace("256", "").replace("  ", " ");
            String ampf = "";
            if (this.potion.getDuration() < 50) {
               ampf = ampf + TextFormatting.RED;
            } else {
               ampf = ampf + TextFormatting.GRAY;
            }

            ampf = ampf + Potion.getPotionDurationString(this.potion, 1.0F);
            name = name + " ";
            name = name + ampf;
            name = name.replace("  ", " ");
            this.name = name;
         }

         this.durration = this.potion.getDuration();
      }

      public float getDurrationPC() {
         return MathUtils.clamp((float)this.durration / (float)this.maxTicksDurration, 0.0F, 1.0F);
      }

      public void setToRemove(boolean doRemove) {
         if (doRemove) {
            this.toRemove = true;
            this.alphaPC.to = 0.0F;
         } else {
            if (this.alphaPC.to == 0.0F || this.toRemove) {
               this.toRemove = false;
               this.alphaPC.to = 1.0F;
            }
         }
      }

      public void updateRemoveStatus() {
         if (this.alphaPC.to == 0.0F && (double)this.alphaPC.getAnim() < 0.1) {
            this.remove = true;
         }
      }

      public boolean isWantToRemove() {
         return this.remove;
      }

      public PotionEffect getPotion() {
         return this.potion;
      }

      public String getName() {
         return this.name;
      }
   }

   private class StaffPlayer {
      String displayName;
      String name;
      boolean vanished;
      GameType gamemode = GameType.SURVIVAL;
      long startTime = System.currentTimeMillis();
      long startSpecTime = System.currentTimeMillis();
      long startTimeQuit = 0L;
      AnimationUtils alphaPC = new AnimationUtils(0.0F, 1.0F, 0.05F);
      boolean toRemove;
      boolean remove;
      ResourceLocation skinLoc;

      public StaffPlayer(String displayName, String name, boolean vanished, GameType gamemode, ResourceLocation skinLoc) {
         this.displayName = displayName;
         this.name = name;
         this.vanished = vanished;
         if (gamemode != null) {
            this.gamemode = gamemode;
         }

         this.toRemove = this.displayName.length() == 0;
         this.alphaPC.setAnim(0.0F);
         this.skinLoc = skinLoc;
      }

      String getName() {
         return this.name;
      }

      ResourceLocation getSkinLoc() {
         return this.skinLoc;
      }

      void setSkinLoc(ResourceLocation skinLoc) {
         this.skinLoc = skinLoc;
      }

      String getDisplayName() {
         return this.displayName;
      }

      long getSpecTime() {
         if (!this.vanished) {
            this.startSpecTime = System.currentTimeMillis();
         }

         return System.currentTimeMillis() - this.startSpecTime;
      }

      String getSpecTimeString() {
         int sec = (int)(this.getSpecTime() / 1000L);
         int mins = sec / 60;
         int hors = mins / 60;
         sec -= mins * 60;
         mins -= hors * 60;
         return this.getSpecTime() >= 500L
            ? " " + TextFormatting.RED + (hors > 0 ? hors + "h" : "") + (mins > 0 ? mins + "m" : "") + (sec > 0 ? sec + "s" : "") + TextFormatting.RESET
            : "";
      }

      long getTime() {
         return System.currentTimeMillis() - this.startTime;
      }

      String getTimeString() {
         int sec = (int)(this.getTime() / 1000L);
         int mins = sec / 60;
         int hors = mins / 60;
         sec -= mins * 60;
         mins -= hors * 60;
         return " " + TextFormatting.WHITE + (hors > 0 ? hors + "h" : "") + (mins > 0 ? mins + "m" : "") + (sec > 0 ? sec + "s" : "") + TextFormatting.RESET;
      }

      long getQuitTime() {
         return this.toRemove ? System.currentTimeMillis() - this.startTimeQuit : 0L;
      }

      String getQuitString() {
         return this.toRemove
            ? " "
               + TextFormatting.GRAY
               + "Quit "
               + TextFormatting.DARK_GRAY
               + (this.getQuitTime() > 5000L ? "" : (int)(5L - this.getQuitTime() / 1000L) + "s")
               + TextFormatting.RESET
            : "";
      }

      void update() {
         if (this.toRemove) {
            if (this.startTimeQuit == 0L) {
               this.startTimeQuit = System.currentTimeMillis();
            }

            this.startTime = System.currentTimeMillis();
            this.startSpecTime = System.currentTimeMillis();
         } else if (this.startTimeQuit != 0L) {
            this.startTimeQuit = 0L;
         }

         if (this.toRemove && this.alphaPC.to == 1.0F && this.getQuitTime() >= 5000L) {
            this.alphaPC.to = 0.0F;
         }

         if ((double)this.alphaPC.getAnim() < 0.03) {
            this.remove = true;
         }
      }

      String renderString() {
         int gm = this.gamemode == GameType.SURVIVAL
            ? 0
            : (this.gamemode == GameType.CREATIVE ? 1 : (this.gamemode == GameType.ADVENTURE ? 2 : (this.gamemode == GameType.SPECTATOR ? 3 : -1)));
         String pref = "";
         boolean spec = this.vanished;
         if (!this.toRemove) {
            boolean warn = Hud.this.getStaffWarnEqualPrefixes().stream().anyMatch(prefix -> Hud.this.lowerContains(this.getDisplayName(), prefix));
            if (warn) {
               pref = pref + TextFormatting.AQUA + "[!]" + TextFormatting.RESET;
            }

            if (!spec) {
               List<EntityLivingBase> neared = Module.mc
                  .world
                  .getLoadedEntityList()
                  .stream()
                  .filter(Objects::nonNull)
                  .<EntityLivingBase>map(Entity::getLivingBaseOf)
                  .filter(Objects::nonNull)
                  .filter(e -> e instanceof EntityOtherPlayerMP)
                  .toList();
               boolean hasNear = neared.stream().<String>map(Entity::getName).anyMatch(match -> Hud.this.lowerContains(match, this.name));
               if (hasNear) {
                  int dst = neared.stream()
                     .filter(entity -> Hud.this.lowerContains(entity.getName(), this.name))
                     .map(entity -> Minecraft.player.getDistanceToEntity(entity))
                     .findFirst()
                     .get()
                     .intValue();
                  boolean seen = neared.stream()
                     .filter(entity -> Hud.this.lowerContains(entity.getName(), this.name))
                     .map(entity -> RenderUtils.isInView(entity))
                     .findFirst()
                     .get();
                  pref = pref
                     + TextFormatting.YELLOW
                     + "[N] "
                     + (dst != 0 ? "- " + dst + "m " : " ")
                     + (seen ? TextFormatting.BLUE + "[Seen] " + TextFormatting.RESET : TextFormatting.RESET);
               } else if (Hud.this.lowerContains(Minecraft.player.getName(), this.name)) {
                  pref = pref + TextFormatting.GREEN + "[ME]" + TextFormatting.RESET;
               }
            }

            if (gm != 0) {
               pref = pref + (gm == 1 ? TextFormatting.LIGHT_PURPLE : (gm == 2 ? TextFormatting.GOLD : (gm == 3 ? TextFormatting.AQUA : TextFormatting.GRAY)));
               pref = pref + "[G" + gm + "] " + TextFormatting.RESET;
            }
         }

         if (spec) {
            pref = pref + TextFormatting.RED + "[S] " + TextFormatting.RESET;
         }

         String time = this.getSpecTime() >= 500L && MathUtils.getDifferenceOf((float)this.getSpecTime(), (float)this.getTime()) < 1000.0
            ? this.getSpecTimeString()
            : this.getTimeString() + this.getSpecTimeString();
         String title = "";
         if (this.toRemove) {
            title = this.getQuitString();
         } else {
            title = (double)((float)this.getTime() / 1000.0F) > 0.5 ? time : "";
         }

         return (pref + this.displayName + title).trim().replace("  ", " ");
      }
   }
}
