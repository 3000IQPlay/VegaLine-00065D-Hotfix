package ru.govno.client.module.settings;

import java.util.function.Supplier;
import ru.govno.client.module.Module;

public class Settings {
    public String name;
    public Category category;
    public Module module;
    public Supplier<Boolean> visible = () -> Boolean.TRUE;

    public boolean isVisible() {
        return this.visible.get();
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getName() {
        return this.name;
    }

    public Module getModule() {
        return this.module;
    }

    public static enum Category {
        Boolean,
        Float,
        String_Massive,
        Color;

    }
}

