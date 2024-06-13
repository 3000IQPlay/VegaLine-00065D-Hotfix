package ru.govno.client.module.modules;

import java.util.List;
import net.minecraft.util.text.TextFormatting;
import ru.govno.client.module.Module;
import ru.govno.client.ui.login.GuiAltLogin;
import ru.govno.client.utils.Command.impl.Panic;

public class NameSecurity extends Module {
   public static Module get;

   public NameSecurity() {
      super("NameSecurity", 0, Module.Category.PLAYER);
      get = this;
   }

   public static Module me() {
      return get;
   }

   public static String replacedName() {
      String uni = "♡";
      return TextFormatting.LIGHT_PURPLE + uni + "LoveSex" + uni + TextFormatting.RESET;
   }

   private static List<String> namesToReplaceList() {
      return List.of(mc.session.getUsername());
   }

   public static String replacedIfActive(String name) {
      if (get != null && !Panic.stop && me().actived && !(mc.currentScreen instanceof GuiAltLogin)) {
         for (String Name : namesToReplaceList()) {
            name = name.replace(Name, replacedName());
         }

         return name;
      } else {
         return name;
      }
   }
}
