package ru.govno.client.module.modules;

import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Command.impl.Clip;

public class VClip extends Module {
   FloatSettings Power;
   boolean clipped;

   public VClip() {
      super("VClip", 0, Module.Category.MOVEMENT);
      this.settings.add(this.Power = new FloatSettings("Power", 60.0F, 200.0F, -70.0F, this));
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         Clip.runClip((double)this.Power.getFloat(), 0.0, InventoryUtil.getElytra() != -1);
         this.toggleSilent(false);
         this.clipped = true;
      } else if (this.clipped) {
         Client.msg(
            "§f§lModules:§r [§l"
               + this.name
               + "§r§7] §7тепаю на"
               + (this.Power.getFloat() != 0.0F ? " " + this.Power.getFloat() + (this.Power.getFloat() > 0.0F ? " вверх" : " вниз") : "хуй")
               + ".",
            false
         );
         this.clipped = false;
      }

      super.onToggled(actived);
   }
}
