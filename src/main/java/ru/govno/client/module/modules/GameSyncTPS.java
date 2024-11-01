package ru.govno.client.module.modules;

import ru.govno.client.module.Module;
import ru.govno.client.module.modules.HitAura;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.TPSDetect;

public class GameSyncTPS
extends Module {
    public static GameSyncTPS instance;
    public FloatSettings SyncPercent = new FloatSettings("SyncPercent", 0.15f, 1.5f, 0.0f, this);
    public BoolSettings OnlyAura;

    public GameSyncTPS() {
        super("GameSyncTPS", 0, Module.Category.PLAYER);
        this.settings.add(this.SyncPercent);
        this.OnlyAura = new BoolSettings("OnlyAura", true, this);
        this.settings.add(this.OnlyAura);
        instance = this;
    }

    public static double getConpenseMath(double val, float strenghZeroToOne) {
        double out = val - (double)((1.0f - TPSDetect.getTPSServer() / 20.0f) * strenghZeroToOne);
        return out < (double)0.075f ? (double)0.075f : out;
    }

    public static double getGameConpense(double prevTimerSpeed, float percentCompense) {
        return instance.isActived() && (!GameSyncTPS.instance.OnlyAura.getBool() || HitAura.TARGET != null) ? GameSyncTPS.getConpenseMath(prevTimerSpeed, percentCompense) : prevTimerSpeed;
    }

    @Override
    public String getDisplayName() {
        return this.getDisplayByDouble(this.SyncPercent.getFloat());
    }
}

