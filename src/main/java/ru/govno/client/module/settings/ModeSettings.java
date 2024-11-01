package ru.govno.client.module.settings;

import java.util.function.Supplier;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.Settings;

public class ModeSettings
extends Settings {
    public String[] modes;
    public String currentMode;

    public ModeSettings(String name, String currentMode, Module module, String[] modes) {
        this.name = name;
        this.currentMode = currentMode;
        this.module = module;
        this.modes = modes;
        this.category = Settings.Category.String_Massive;
    }

    public ModeSettings(String name, String currentMode, Module module, String[] modes, Supplier<Boolean> visible) {
        this.name = name;
        this.currentMode = currentMode;
        this.module = module;
        this.modes = modes;
        this.category = Settings.Category.String_Massive;
        this.visible = visible;
    }

    public void setMode(String value) {
        this.currentMode = value;
    }

    public String getMode() {
        return this == null ? "" : this.currentMode;
    }
}

