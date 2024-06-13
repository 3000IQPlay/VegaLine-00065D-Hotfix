package ru.govno.client.module.modules;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;

public class NoRender extends Module {
   public static NoRender get;
   public BoolSettings BreakParticles;
   public BoolSettings HurtCam;
   public BoolSettings ScoreBoard;
   public BoolSettings TotemOverlay;
   public BoolSettings EatParticles;
   public BoolSettings Holograms;
   public BoolSettings ArmorLayers;
   public BoolSettings ArrowLayers;
   public BoolSettings EntityHurt;
   public BoolSettings LiquidOverlay;
   public BoolSettings CameraCollide;
   public BoolSettings BadEffects;
   public BoolSettings TitleScreen;
   public BoolSettings BossStatusBar;
   public BoolSettings EnchGlintEffect;
   public BoolSettings ExpBar;
   public BoolSettings Fire;
   public BoolSettings FireOnEntity;
   public BoolSettings HandShake;
   public BoolSettings LightShotBolt;
   public BoolSettings VanishEffect;
   public BoolSettings FogEffect;
   public BoolSettings ClientCape;
   public BoolSettings HeldTooltips;

   public NoRender() {
      super("NoRender", 0, Module.Category.RENDER);
      get = this;
      this.settings.add(this.BreakParticles = new BoolSettings("BreakParticles", true, this));
      this.settings.add(this.HurtCam = new BoolSettings("HurtCam", true, this));
      this.settings.add(this.ScoreBoard = new BoolSettings("ScoreBoard", true, this));
      this.settings.add(this.TotemOverlay = new BoolSettings("TotemOverlay", true, this));
      this.settings.add(this.EatParticles = new BoolSettings("EatParticles", true, this));
      this.settings.add(this.Holograms = new BoolSettings("Holograms", false, this));
      this.settings.add(this.ArmorLayers = new BoolSettings("ArmorLayers", false, this));
      this.settings.add(this.ArrowLayers = new BoolSettings("ArrowLayers", true, this));
      this.settings.add(this.EntityHurt = new BoolSettings("EntityHurt", false, this));
      this.settings.add(this.LiquidOverlay = new BoolSettings("LiquidOverlay", true, this));
      this.settings.add(this.CameraCollide = new BoolSettings("CameraCollide", true, this));
      this.settings.add(this.BadEffects = new BoolSettings("BadEffects", true, this));
      this.settings.add(this.TitleScreen = new BoolSettings("TitleScreen", true, this));
      this.settings.add(this.BossStatusBar = new BoolSettings("BossStatusBar", false, this));
      this.settings.add(this.EnchGlintEffect = new BoolSettings("EnchGlintEffect", false, this));
      this.settings.add(this.ExpBar = new BoolSettings("ExpBar", false, this));
      this.settings.add(this.Fire = new BoolSettings("Fire", true, this));
      this.settings.add(this.FireOnEntity = new BoolSettings("FireOnEntity", true, this));
      this.settings.add(this.HandShake = new BoolSettings("HandShake", false, this));
      this.settings.add(this.LightShotBolt = new BoolSettings("LightShotBolt", true, this));
      this.settings.add(this.VanishEffect = new BoolSettings("VanishEffect", true, this));
      this.settings.add(this.FogEffect = new BoolSettings("FogEffect", false, this));
      this.settings.add(this.ClientCape = new BoolSettings("ClientCape", false, this));
      this.settings.add(this.HeldTooltips = new BoolSettings("HeldTooltips", false, this));
   }

   @Override
   public void onUpdate() {
      if (this.CameraCollide.getBool()) {
         Minecraft.player.noClip = true;
      }
   }

   @Override
   public void onRender2D(ScaledResolution sr) {
      if (this.BadEffects.getBool()) {
         Arrays.asList(2, 4, 9, 15, 17, 18, 20, 27)
            .stream()
            .map(INT -> Potion.getPotionById(INT))
            .filter(POT -> Minecraft.player.isPotionActive(POT))
            .forEach(POT -> Minecraft.player.removeActivePotionEffect(POT));
      }
   }
}
