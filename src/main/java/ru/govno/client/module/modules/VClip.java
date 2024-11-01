package ru.govno.client.module.modules;

import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Command.impl.Clip;
import ru.govno.client.utils.InventoryUtil;

public class VClip
extends Module {
    FloatSettings Power = new FloatSettings("Power", 60.0f, 200.0f, -70.0f, this);
    boolean clipped;

    public VClip() {
        super("VClip", 0, Module.Category.MOVEMENT);
        this.settings.add(this.Power);
    }

    @Override
    public void onToggled(boolean actived) {
        if (actived) {
            Clip.runClip(this.Power.getFloat(), 0.0, InventoryUtil.getElytra() != -1);
            this.toggleSilent(false);
            this.clipped = true;
        } else if (this.clipped) {
            Client.msg("\u00a7f\u00a7lModules:\u00a7r [\u00a7l" + this.name + "\u00a7r\u00a77] \u00a77\u0442\u0435\u043f\u0430\u044e \u043d\u0430" + (String)(this.Power.getFloat() != 0.0f ? " " + this.Power.getFloat() + (this.Power.getFloat() > 0.0f ? " \u0432\u0432\u0435\u0440\u0445" : " \u0432\u043d\u0438\u0437") : "\u0445\u0443\u0439") + ".", false);
            this.clipped = false;
        }
        super.onToggled(actived);
    }
}

