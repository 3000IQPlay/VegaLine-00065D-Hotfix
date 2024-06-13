package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import ru.govno.client.module.Module;

public class PushAttack extends Module {
   public static Module get;

   public PushAttack() {
      super("PushAttack", 0, Module.Category.COMBAT);
      get = this;
   }

   @Override
   public void onUpdate() {
      if (mc.gameSettings.keyBindAttack.isKeyDown()
         && Minecraft.player.isHandActive()
         && (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK)) {
         mc.clickMouse();
         mc.gameSettings.keyBindAttack.pressed = false;
      }
   }
}
