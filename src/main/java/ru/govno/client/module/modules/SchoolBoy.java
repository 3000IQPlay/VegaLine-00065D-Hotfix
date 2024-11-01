package ru.govno.client.module.modules;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;

public class SchoolBoy
extends Module {
    public static SchoolBoy get;
    public ModeSettings Targets;
    public BoolSettings ChangeFirstHeight;

    public SchoolBoy() {
        super("SchoolBoy", 0, Module.Category.RENDER);
        get = this;
        this.Targets = new ModeSettings("Targets", "Self", this, new String[]{"Self", "Friends", "Self&Friends", "All", "Self&All", "FullAll"});
        this.settings.add(this.Targets);
        this.ChangeFirstHeight = new BoolSettings("ChangeFirstHeight", true, this, () -> this.Targets.currentMode.equalsIgnoreCase("FullAll") || this.Targets.currentMode.contains("Self"));
        this.settings.add(this.ChangeFirstHeight);
    }

    public static boolean isSetEyeHeightReduce(Entity forEntity) {
        EntityPlayerSP sp;
        return get != null && SchoolBoy.get.actived && forEntity instanceof EntityPlayerSP && (sp = (EntityPlayerSP)forEntity).isChild() && SchoolBoy.get.ChangeFirstHeight.getBool();
    }
}

