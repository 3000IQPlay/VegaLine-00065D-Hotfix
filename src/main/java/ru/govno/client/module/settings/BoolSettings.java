package ru.govno.client.module.settings;

import java.util.function.Supplier;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.Settings;
import ru.govno.client.utils.Render.AnimationUtils;

public class BoolSettings
extends Settings {
    public AnimationUtils boolAnim;
    public int bind;

    public BoolSettings(String name, boolean bValue, Module module) {
        this.name = name;
        this.boolAnim = new AnimationUtils(bValue ? 1.0f : 0.0f, bValue ? 1.0f : 0.0f, 0.1f);
        this.module = module;
        this.category = Settings.Category.Boolean;
    }

    public BoolSettings(String name, boolean bValue, Module module, Supplier<Boolean> visible) {
        this.name = name;
        this.boolAnim = new AnimationUtils(bValue ? 1.0f : 0.0f, bValue ? 1.0f : 0.0f, 0.1f);
        this.module = module;
        this.category = Settings.Category.Boolean;
        this.visible = visible;
    }

    public int getBind() {
        return this.bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }

    public boolean isBinded() {
        return this.getBind() != 0;
    }

    public boolean getBool() {
        return this.boolAnim.to == 1.0f;
    }

    public int getIntBool() {
        return this.boolAnim.to == 1.0f ? 1 : 0;
    }

    public float getAnimation() {
        if (this.boolAnim.to == 0.0f && this.boolAnim.anim < 0.003921569f || this.boolAnim.to == 1.0f && this.boolAnim.anim > 0.99607843f) {
            this.boolAnim.setAnim(this.boolAnim.to);
            return this.boolAnim.anim;
        }
        return this.boolAnim.getAnim();
    }

    public boolean canBeRender() {
        return this.getAnimation() >= 0.003921569f;
    }

    public void setBool(boolean value) {
        this.boolAnim.to = value ? 1.0f : 0.0f;
    }

    public void toggleBool() {
        this.boolAnim.to = this.boolAnim.to == 0.0f ? 1.0f : 0.0f;
    }
}

