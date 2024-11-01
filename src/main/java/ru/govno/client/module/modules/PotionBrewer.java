package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBrewingStand;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.BrewUtil;

public class PotionBrewer
extends Module {
    public static PotionBrewer get;
    private final ModeSettings potionToBrewMode = new ModeSettings("PotionToBrew", "Strength", this, new String[]{"Strength", "Speed", "Resistance", "Healing", "Regen"});
    private final BoolSettings doSplashPotion;
    private final BoolSettings doEffectBoost;
    private static final BrewUtil brewUtil;

    public PotionBrewer() {
        super("PotionBrewer", 0, Module.Category.MISC);
        this.settings.add(this.potionToBrewMode);
        this.doSplashPotion = new BoolSettings("BrewSplash", true, this);
        this.settings.add(this.doSplashPotion);
        this.doEffectBoost = new BoolSettings("BrewBoostEffect", true, this);
        this.settings.add(this.doEffectBoost);
        get = this;
    }

    @Override
    public boolean isBetaModule() {
        return true;
    }

    @Override
    public void onUpdate() {
        long clickDelay = 0L;
        Container container = Minecraft.player.openContainer;
        if (container instanceof ContainerBrewingStand) {
            ContainerBrewingStand containerBrewingStand = (ContainerBrewingStand)container;
            brewUtil.handleBrewingStand(containerBrewingStand, this.doSplashPotion.getBool(), this.doEffectBoost.getBool(), this.potionToBrewMode.getMode(), 0L);
        }
    }

    static {
        brewUtil = new BrewUtil();
    }
}

