package ru.govno.client.module.modules;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;

public class SchoolBoy extends Module {
   public static SchoolBoy get;
   public ModeSettings Targets;
   public BoolSettings ChangeFirstHeight;

   public SchoolBoy() {
      super("SchoolBoy", 0, Module.Category.RENDER);
      get = this;
      this.settings
         .add(this.Targets = new ModeSettings("Targets", "Self", this, new String[]{"Self", "Friends", "Self&Friends", "All", "Self&All", "FullAll"}));
      this.settings
         .add(
            this.ChangeFirstHeight = new BoolSettings(
               "ChangeFirstHeight", true, this, () -> this.Targets.currentMode.equalsIgnoreCase("FullAll") || this.Targets.currentMode.contains("Self")
            )
         );
   }

   public static boolean isSetEyeHeightReduce(Entity forEntity) {
      if (get != null && get.actived && forEntity instanceof EntityPlayerSP sp && sp.isChild() && get.ChangeFirstHeight.getBool()) {
         return true;
      }

      return false;
   }
}
