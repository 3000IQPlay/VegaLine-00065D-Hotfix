package ru.govno.client.module.modules;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;

public class NoRender
extends Module {
    public static NoRender get;
    public BoolSettings BreakParticles = new BoolSettings("BreakParticles", true, this);
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
    public BoolSettings AllParticles;

    public NoRender() {
        super("NoRender", 0, Module.Category.RENDER);
        this.settings.add(this.BreakParticles);
        this.HurtCam = new BoolSettings("HurtCam", true, this);
        this.settings.add(this.HurtCam);
        this.ScoreBoard = new BoolSettings("ScoreBoard", true, this);
        this.settings.add(this.ScoreBoard);
        this.TotemOverlay = new BoolSettings("TotemOverlay", true, this);
        this.settings.add(this.TotemOverlay);
        this.EatParticles = new BoolSettings("EatParticles", true, this);
        this.settings.add(this.EatParticles);
        this.Holograms = new BoolSettings("Holograms", false, this);
        this.settings.add(this.Holograms);
        this.ArmorLayers = new BoolSettings("ArmorLayers", false, this);
        this.settings.add(this.ArmorLayers);
        this.ArrowLayers = new BoolSettings("ArrowLayers", true, this);
        this.settings.add(this.ArrowLayers);
        this.EntityHurt = new BoolSettings("EntityHurt", false, this);
        this.settings.add(this.EntityHurt);
        this.LiquidOverlay = new BoolSettings("LiquidOverlay", true, this);
        this.settings.add(this.LiquidOverlay);
        this.CameraCollide = new BoolSettings("CameraCollide", true, this);
        this.settings.add(this.CameraCollide);
        this.BadEffects = new BoolSettings("BadEffects", true, this);
        this.settings.add(this.BadEffects);
        this.TitleScreen = new BoolSettings("TitleScreen", true, this);
        this.settings.add(this.TitleScreen);
        this.BossStatusBar = new BoolSettings("BossStatusBar", false, this);
        this.settings.add(this.BossStatusBar);
        this.EnchGlintEffect = new BoolSettings("EnchGlintEffect", false, this);
        this.settings.add(this.EnchGlintEffect);
        this.ExpBar = new BoolSettings("ExpBar", false, this);
        this.settings.add(this.ExpBar);
        this.Fire = new BoolSettings("Fire", true, this);
        this.settings.add(this.Fire);
        this.FireOnEntity = new BoolSettings("FireOnEntity", true, this);
        this.settings.add(this.FireOnEntity);
        this.HandShake = new BoolSettings("HandShake", false, this);
        this.settings.add(this.HandShake);
        this.LightShotBolt = new BoolSettings("LightShotBolt", true, this);
        this.settings.add(this.LightShotBolt);
        this.VanishEffect = new BoolSettings("VanishEffect", true, this);
        this.settings.add(this.VanishEffect);
        this.FogEffect = new BoolSettings("FogEffect", false, this);
        this.settings.add(this.FogEffect);
        this.ClientCape = new BoolSettings("ClientCape", false, this);
        this.settings.add(this.ClientCape);
        this.HeldTooltips = new BoolSettings("HeldTooltips", false, this);
        this.settings.add(this.HeldTooltips);
        this.AllParticles = new BoolSettings("AllParticles", false, this);
        this.settings.add(this.AllParticles);
        get = this;
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
            Arrays.asList(9, 15, 17, 20, 27).stream().map(INT -> Potion.getPotionById(INT)).filter(POT -> Minecraft.player.isPotionActive((Potion)POT)).forEach(POT -> Minecraft.player.removeActivePotionEffect((Potion)POT));
        }
    }
}

