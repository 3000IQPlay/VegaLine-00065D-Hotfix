package ru.govno.client.cfg;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.lwjgl.input.Keyboard;
import ru.govno.client.Client;
import ru.govno.client.cfg.ConfigManager;
import ru.govno.client.cfg.ConfigUpdater;
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

public final class Config
implements ConfigUpdater {
    private final String name;
    private final File file;

    public Config(String name) {
        this.name = name;
        this.file = new File(ConfigManager.configDirectory, name + ".vls");
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            }
            catch (Exception e) {
                e.printStackTrace();
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
        object.addProperty("KeyBound", (Number)macros.getKey());
        object.addProperty("Massage", macros.getMassage());
        return object;
    }

    public void loadAllMacrosesFromJson(JsonObject object) {
        Client.macrosManager.getMacrosList().clear();
        String defaultName = "Macros-\u2116";
        for (int index = 0; index < Integer.MAX_VALUE; ++index) {
            String groupName = defaultName + index;
            String groupNameNext = defaultName + (index + 1);
            if (object.has(groupName)) {
                JsonObject propertiesObject = object.getAsJsonObject(groupName);
                if (propertiesObject == null) continue;
                JsonElement elementName = propertiesObject.get("Name");
                JsonElement elementKeyBound = propertiesObject.get("KeyBound");
                JsonElement elementMassage = propertiesObject.get("Massage");
                Client.macrosManager.add(new Macros(elementName.getAsString(), elementKeyBound.getAsInt(), elementMassage.getAsString()));
            }
            if (!object.has(groupNameNext)) break;
        }
    }

    public JsonObject getJsonFromPointTrace(PointTrace pointTrace) {
        JsonObject object = new JsonObject();
        object.addProperty("Name", pointTrace.getName());
        object.addProperty("ServerName", pointTrace.getServerName());
        object.addProperty("XPosition", (Number)PointTrace.getX(pointTrace));
        object.addProperty("YPosition", (Number)PointTrace.getY(pointTrace));
        object.addProperty("ZPosition", (Number)PointTrace.getZ(pointTrace));
        object.addProperty("Dimension", (Number)PointTrace.getDemension(pointTrace));
        object.addProperty("Index", (Number)pointTrace.getIndex());
        return object;
    }

    public void loadAllPointTracesFromJson(JsonObject object) {
        PointTrace.getPointList().clear();
        String defaultName = "PointTrace-\u2116";
        for (int index = 0; index < Integer.MAX_VALUE; ++index) {
            String groupName = defaultName + index;
            String groupNameNext = defaultName + (index + 1);
            if (object.has(groupName)) {
                JsonObject propertiesObject = object.getAsJsonObject(groupName);
                if (propertiesObject == null) continue;
                JsonElement elementName = propertiesObject.get("Name");
                JsonElement elementServerName = propertiesObject.get("ServerName");
                JsonElement elementXPosition = propertiesObject.get("XPosition");
                JsonElement elementYPosition = propertiesObject.get("YPosition");
                JsonElement elementZPosition = propertiesObject.get("ZPosition");
                JsonElement elementDimension = propertiesObject.get("Dimension");
                JsonElement elementIndex = propertiesObject.get("Index");
                PointTrace.getPointList().add(new PointTrace(elementName.getAsString(), elementServerName.getAsString(), elementXPosition.getAsDouble(), elementYPosition.getAsDouble(), elementZPosition.getAsDouble(), elementDimension.getAsInt(), elementIndex.getAsInt()));
            }
            if (!object.has(groupNameNext)) break;
        }
    }

    public JsonObject getJsonFromFriend(Friend friend) {
        JsonObject object = new JsonObject();
        object.addProperty("Name", friend.getName());
        return object;
    }

    public void loadAllFriendsFromJson(JsonObject object) {
        Client.friendManager.clearFriends();
        String defaultName = "Friend-\u2116";
        for (int index = 0; index < Integer.MAX_VALUE; ++index) {
            String groupName = defaultName + index;
            String groupNameNext = defaultName + (index + 1);
            if (object.has(groupName)) {
                JsonObject propertiesObject = object.getAsJsonObject(groupName);
                if (propertiesObject == null) continue;
                JsonElement elementName = propertiesObject.get("Name");
                Client.friendManager.addFriend(elementName.getAsString());
            }
            if (!object.has(groupNameNext)) break;
        }
    }

    public JsonObject getJsonFromSettings(Module module) {
        JsonObject object = new JsonObject();
        JsonObject propertiesObject = new JsonObject();
        if (!(module instanceof ClickGui)) {
            object.addProperty("EnabledState", Boolean.valueOf(module.isActived()));
        }
        if (module.getBind() != 0) {
            object.addProperty("KeyBound", (Number)module.getBind());
        }
        module.getSettings().forEach(set -> {
            if (set instanceof BoolSettings) {
                BoolSettings boolSet = (BoolSettings)set;
                propertiesObject.addProperty(set.getName(), Boolean.valueOf(boolSet.getBool()));
            } else if (set instanceof FloatSettings) {
                FloatSettings floatSet = (FloatSettings)set;
                propertiesObject.addProperty(set.getName(), (Number)Float.valueOf(floatSet.getFloat()));
            } else if (set instanceof ModeSettings) {
                ModeSettings modeSet = (ModeSettings)set;
                propertiesObject.addProperty(set.getName(), modeSet.getMode());
            } else if (set instanceof ColorSettings) {
                ColorSettings colorSet = (ColorSettings)set;
                propertiesObject.addProperty(set.getName(), (Number)colorSet.getCol());
            }
            object.add("AllSets", (JsonElement)propertiesObject);
        });
        List<BoolSettings> bindedBools = module.getSettings().stream().map(set -> set instanceof BoolSettings ? (BoolSettings)set : null).filter(Objects::nonNull).filter(boolSet -> boolSet.isBinded()).collect(Collectors.toList());
        if (!bindedBools.isEmpty()) {
            JsonObject propertiesBound = new JsonObject();
            bindedBools.forEach(boolSet -> {
                propertiesBound.addProperty(boolSet.getName(), Keyboard.getKeyName((int)boolSet.getBind()));
                object.add("BoolKeys", (JsonElement)propertiesBound);
            });
        }
        return object;
    }

    public void loadSettingsFromJson(JsonObject object, Module module) {
        if (object == null) {
            return;
        }
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
        if (propertiesObject == null) {
            return;
        }
        module.getSettings().stream().filter(set -> propertiesObject.has(set.getName())).forEach(set -> {
            JsonElement value = propertiesObject.get(set.getName());
            if (set instanceof BoolSettings) {
                BoolSettings boolSet = (BoolSettings)set;
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
                        boolSet.setBind(Keyboard.getKeyIndex((String)keyValue.getAsString()));
                    }
                } else {
                    boolSet.setBind(0);
                }
            } else if (set instanceof FloatSettings) {
                FloatSettings floatSet = (FloatSettings)set;
                floatSet.setFloat(value.getAsFloat());
            } else if (set instanceof ModeSettings) {
                ModeSettings modeSet = (ModeSettings)set;
                if (Arrays.stream(modeSet.modes).anyMatch(mode -> value.getAsString().contains((CharSequence)mode))) {
                    modeSet.setMode(value.getAsString());
                }
            } else if (set instanceof ColorSettings) {
                ColorSettings colorSet = (ColorSettings)set;
                colorSet.setCol(value.getAsInt());
            }
        });
    }

    @Override
    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();
        JsonObject modulesObject = new JsonObject();
        JsonObject macrosesObject = new JsonObject();
        JsonObject pointsObject = new JsonObject();
        JsonObject friendsObject = new JsonObject();
        Client.moduleManager.getModuleList().forEach(mod -> modulesObject.add(mod.getName(), (JsonElement)this.getJsonFromSettings((Module)mod)));
        jsonObject.add("AllMods", (JsonElement)modulesObject);
        if (!MacrosManager.macroses.isEmpty()) {
            int macrosIndex = 0;
            for (Macros macros : MacrosManager.macroses) {
                macrosesObject.add("Macros-\u2116" + macrosIndex, (JsonElement)this.getJsonFromMacros(macros));
                ++macrosIndex;
            }
            jsonObject.add("AllMacroses", (JsonElement)macrosesObject);
        }
        if (!PointTrace.getPointList().isEmpty()) {
            int pointIndex = 0;
            for (PointTrace pointTrace : PointTrace.getPointList()) {
                pointsObject.add("PointTrace-\u2116" + pointIndex, (JsonElement)this.getJsonFromPointTrace(pointTrace));
                ++pointIndex;
            }
            jsonObject.add("AllPointTraces", (JsonElement)pointsObject);
        }
        if (!Client.friendManager.getFriends().isEmpty()) {
            int friendIndex = 0;
            for (Friend friend : Client.friendManager.getFriends()) {
                friendsObject.add("Friend-\u2116" + friendIndex, (JsonElement)this.getJsonFromFriend(friend));
                ++friendIndex;
            }
            jsonObject.add("AllFriends", (JsonElement)friendsObject);
        }
        return jsonObject;
    }

    @Override
    public boolean load(JsonObject object) {
        JsonObject pointTracesObject;
        boolean has = false;
        Client.moduleManager.getModuleList().forEach(Module::disableSilent);
        if (object.has("AllMods")) {
            JsonObject modulesObject = object.getAsJsonObject("AllMods");
            Client.moduleManager.getModuleList().forEach(mod -> this.loadSettingsFromJson(modulesObject.getAsJsonObject(mod.getName()), (Module)mod));
            has = true;
        }
        if (object.has("AllMacroses")) {
            JsonObject macrosesObject = object.getAsJsonObject("AllMacroses");
            this.loadAllMacrosesFromJson(macrosesObject);
            System.out.println("mac-sucess >><");
            has = true;
        } else {
            Client.macrosManager.getMacrosList().clear();
        }
        if (object.has("AllPointTraces")) {
            pointTracesObject = object.getAsJsonObject("AllPointTraces");
            this.loadAllPointTracesFromJson(pointTracesObject);
            System.out.println("point-sucess >><");
            has = true;
        } else {
            PointTrace.getPointList().clear();
        }
        if (object.has("AllFriends")) {
            pointTracesObject = object.getAsJsonObject("AllFriends");
            this.loadAllFriendsFromJson(pointTracesObject);
            System.out.println("friend-sucess >><");
            has = true;
        } else {
            Client.friendManager.clearFriends();
        }
        return has;
    }
}

