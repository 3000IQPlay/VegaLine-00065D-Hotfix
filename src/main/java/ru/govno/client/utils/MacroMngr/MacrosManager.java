package ru.govno.client.utils.MacroMngr;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.lwjgl.input.Keyboard;
import ru.govno.client.module.modules.ClientTune;

public class MacrosManager {
   public static final ArrayList<Macros> macroses = new ArrayList<>();

   public void add(String name, String key, String command) {
      this.getMacrosList().add(new Macros(name, Keyboard.getKeyIndex(key.toUpperCase()), command));
   }

   public void add(Macros macro) {
      this.getMacrosList().add(macro);
   }

   public void remove(String name) {
      this.getMacrosList().removeIf(macros -> macros.getName().equalsIgnoreCase(name));
   }

   public List<Macros> getMacrosList() {
      return macroses;
   }

   public void clear() {
      this.getMacrosList().clear();
   }

   public void onKey(int key) {
      List<Macros> macrosesHasKey = this.getMacrosList().stream().filter(macros -> macros.getKey() == key).collect(Collectors.toList());
      if (!macrosesHasKey.isEmpty()) {
         ClientTune.get.playUseMacros();
         macrosesHasKey.forEach(Macros::use);
      }
   }
}
