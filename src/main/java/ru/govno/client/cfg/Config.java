package ru.govno.client.cfg;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.lwjgl.input.Keyboard;
import ru.govno.client.Client;
import ru.govno.client.friendsystem.Friend;
import ru.govno.client.module.Module;
import ru.govno.client.module.modules.ClickGui;
import ru.govno.client.module.modules.PointTrace;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.MacroMngr.Macros;
import ru.govno.client.utils.MacroMngr.MacrosManager;

public final class Config implements ConfigUpdater {
   private final String name;
   private final File file;

   public Config(String name) {
      this.name = name;
      this.file = new File(ConfigManager.configDirectory, name + ".vls");
      if (!this.file.exists()) {
         try {
            this.file.createNewFile();
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }
   }

   public File getFile() {
      return this.file;
   }

   public String getName() {
      return this.name;
   }

   public JsonObject getJsonFromMacros(Macros macros) {
      JsonObject object = new JsonObject();
      object.addProperty("Name", macros.getName());
      object.addProperty("KeyBound", macros.getKey());
      object.addProperty("Massage", macros.getMassage());
      return object;
   }

   public void loadAllMacrosesFromJson(JsonObject object) {
      Client.macrosManager.getMacrosList().clear();
      String defaultName = "Macros-№";

      for (int index = 0; index < Integer.MAX_VALUE; index++) {
         String groupName = defaultName + index;
         String groupNameNext = defaultName + (index + 1);
         if (object.has(groupName)) {
            JsonObject propertiesObject = object.getAsJsonObject(groupName);
            if (propertiesObject == null) {
               continue;
            }

            JsonElement elementName = propertiesObject.get("Name");
            JsonElement elementKeyBound = propertiesObject.get("KeyBound");
            JsonElement elementMassage = propertiesObject.get("Massage");
            Client.macrosManager.add(new Macros(elementName.getAsString(), elementKeyBound.getAsInt(), elementMassage.getAsString()));
         }

         if (!object.has(groupNameNext)) {
            break;
         }
      }
   }

   public JsonObject getJsonFromPointTrace(PointTrace pointTrace) {
      JsonObject object = new JsonObject();
      object.addProperty("Name", pointTrace.getName());
      object.addProperty("ServerName", pointTrace.getServerName());
      object.addProperty("XPosition", PointTrace.getX(pointTrace));
      object.addProperty("YPosition", PointTrace.getY(pointTrace));
      object.addProperty("ZPosition", PointTrace.getZ(pointTrace));
      object.addProperty("Dimension", PointTrace.getDemension(pointTrace));
      object.addProperty("Index", pointTrace.getIndex());
      return object;
   }

   public void loadAllPointTracesFromJson(JsonObject object) {
      PointTrace.getPointList().clear();
      String defaultName = "PointTrace-№";

      for (int index = 0; index < Integer.MAX_VALUE; index++) {
         String groupName = defaultName + index;
         String groupNameNext = defaultName + (index + 1);
         if (object.has(groupName)) {
            JsonObject propertiesObject = object.getAsJsonObject(groupName);
            if (propertiesObject == null) {
               continue;
            }

            JsonElement elementName = propertiesObject.get("Name");
            JsonElement elementServerName = propertiesObject.get("ServerName");
            JsonElement elementXPosition = propertiesObject.get("XPosition");
            JsonElement elementYPosition = propertiesObject.get("YPosition");
            JsonElement elementZPosition = propertiesObject.get("ZPosition");
            JsonElement elementDimension = propertiesObject.get("Dimension");
            JsonElement elementIndex = propertiesObject.get("Index");
            PointTrace.getPointList()
               .add(
                  new PointTrace(
                     elementName.getAsString(),
                     elementServerName.getAsString(),
                     elementXPosition.getAsDouble(),
                     elementYPosition.getAsDouble(),
                     elementZPosition.getAsDouble(),
                     elementDimension.getAsInt(),
                     elementIndex.getAsInt()
                  )
               );
         }

         if (!object.has(groupNameNext)) {
            break;
         }
      }
   }

   public JsonObject getJsonFromFriend(Friend friend) {
      JsonObject object = new JsonObject();
      object.addProperty("Name", friend.getName());
      return object;
   }

   public void loadAllFriendsFromJson(JsonObject object) {
      Client.friendManager.clearFriends();
      String defaultName = "Friend-№";

      for (int index = 0; index < Integer.MAX_VALUE; index++) {
         String groupName = defaultName + index;
         String groupNameNext = defaultName + (index + 1);
         if (object.has(groupName)) {
            JsonObject propertiesObject = object.getAsJsonObject(groupName);
            if (propertiesObject == null) {
               continue;
            }

            JsonElement elementName = propertiesObject.get("Name");
            Client.friendManager.addFriend(elementName.getAsString());
         }

         if (!object.has(groupNameNext)) {
            break;
         }
      }
   }

   public JsonObject getJsonFromSettings(Module module) {
      JsonObject object = new JsonObject();
      JsonObject propertiesObject = new JsonObject();
      if (!(module instanceof ClickGui)) {
         object.addProperty("EnabledState", module.isActived());
      }

      if (module.getBind() != 0) {
         object.addProperty("KeyBound", module.getBind());
      }

      module.getSettings().forEach(set -> {
         if (set instanceof BoolSettings boolSet) {
            propertiesObject.addProperty(set.getName(), boolSet.getBool());
         } else if (set instanceof FloatSettings floatSet) {
            propertiesObject.addProperty(set.getName(), floatSet.getFloat());
         } else if (set instanceof ModeSettings modeSet) {
            propertiesObject.addProperty(set.getName(), modeSet.getMode());
         } else if (set instanceof ColorSettings colorSet) {
            propertiesObject.addProperty(set.getName(), colorSet.getCol());
         }

         object.add("AllSets", propertiesObject);
      });
      List<BoolSettings> bindedBools = module.getSettings()
         .stream()
         .map(set -> set instanceof BoolSettings ? (BoolSettings)set : null)
         .filter(Objects::nonNull)
         .filter(boolSet -> boolSet.isBinded())
         .collect(Collectors.toList());
      if (!bindedBools.isEmpty()) {
         JsonObject propertiesBound = new JsonObject();
         bindedBools.forEach(boolSet -> {
            propertiesBound.addProperty(boolSet.getName(), Keyboard.getKeyName(boolSet.getBind()));
            object.add("BoolKeys", propertiesBound);
         });
      }

      return object;
   }

   public void loadSettingsFromJson(JsonObject object, Module module) {
      if (object != null) {
         if (object.has("EnabledState")) {
            module.toggleSilent(object.get("EnabledState").getAsBoolean());
         }

         if (object.has("KeyBound")) {
            module.setBind(object.get("KeyBound").getAsInt());
         } else {
            module.setBind(0);
         }

         JsonObject propertiesObject = object.getAsJsonObject("AllSets");
         JsonObject propertiesBound = object.getAsJsonObject("BoolKeys");
         if (propertiesObject != null) {
            module.getSettings().stream().filter(set -> propertiesObject.has(set.getName())).forEach(set -> {
               JsonElement value = propertiesObject.get(set.getName());
               if (set instanceof BoolSettings boolSet) {
                  if (value.getAsString().contains("abled")) {
                     boolSet.setBool(value.getAsString().startsWith("en"));
                  } else {
                     boolSet.setBool(value.getAsBoolean());
                  }

                  if (propertiesBound != null) {
                     JsonElement keyValue = propertiesBound.get(set.getName());
                     if (keyValue == null) {
                        boolSet.setBind(0);
                     } else {
                        boolSet.setBind(Keyboard.getKeyIndex(keyValue.getAsString()));
                     }
                  } else {
                     boolSet.setBind(0);
                  }
               } else if (set instanceof FloatSettings floatSet) {
                  floatSet.setFloat(value.getAsFloat());
               } else if (set instanceof ModeSettings modeSet) {
                  modeSet.setMode(value.getAsString());
               } else if (set instanceof ColorSettings colorSet) {
                  colorSet.setCol(value.getAsInt());
               }
            });
         }
      }
   }

   @Override
   public JsonObject save() {
      JsonObject jsonObject = new JsonObject();
      JsonObject modulesObject = new JsonObject();
      JsonObject macrosesObject = new JsonObject();
      JsonObject pointsObject = new JsonObject();
      JsonObject friendsObject = new JsonObject();
      Client.moduleManager.getModuleList().forEach(mod -> modulesObject.add(mod.getName(), this.getJsonFromSettings(mod)));
      jsonObject.add("AllMods", modulesObject);
      if (!MacrosManager.macroses.isEmpty()) {
         int macrosIndex = 0;

         for (Macros macros : MacrosManager.macroses) {
            macrosesObject.add("Macros-№" + macrosIndex, this.getJsonFromMacros(macros));
            macrosIndex++;
         }

         jsonObject.add("AllMacroses", macrosesObject);
      }

      if (!PointTrace.getPointList().isEmpty()) {
         int pointIndex = 0;

         for (PointTrace pointTrace : PointTrace.getPointList()) {
            pointsObject.add("PointTrace-№" + pointIndex, this.getJsonFromPointTrace(pointTrace));
            pointIndex++;
         }

         jsonObject.add("AllPointTraces", pointsObject);
      }

      if (!Client.friendManager.getFriends().isEmpty()) {
         int friendIndex = 0;

         for (Friend friend : Client.friendManager.getFriends()) {
            friendsObject.add("Friend-№" + friendIndex, this.getJsonFromFriend(friend));
            friendIndex++;
         }

         jsonObject.add("AllFriends", friendsObject);
      }

      return jsonObject;
   }

   @Override
   public void load(JsonObject object) {
      Client.moduleManager.getModuleList().forEach(Module::disableSilent);
      if (object.has("AllMods")) {
         JsonObject modulesObject = object.getAsJsonObject("AllMods");
         Client.moduleManager.getModuleList().forEach(mod -> this.loadSettingsFromJson(modulesObject.getAsJsonObject(mod.getName()), mod));
      }

      if (object.has("AllMacroses")) {
         JsonObject macrosesObject = object.getAsJsonObject("AllMacroses");
         this.loadAllMacrosesFromJson(macrosesObject);
         System.out.println("mac-sucess >><");
      } else {
         Client.macrosManager.getMacrosList().clear();
      }

      if (object.has("AllPointTraces")) {
         JsonObject pointTracesObject = object.getAsJsonObject("AllPointTraces");
         this.loadAllPointTracesFromJson(pointTracesObject);
         System.out.println("point-sucess >><");
      } else {
         PointTrace.getPointList().clear();
      }

      if (object.has("AllFriends")) {
         JsonObject pointTracesObject = object.getAsJsonObject("AllFriends");
         this.loadAllFriendsFromJson(pointTracesObject);
         System.out.println("friend-sucess >><");
      } else {
         Client.friendManager.clearFriends();
      }
   }
}
