package ru.govno.client.module.modules;

import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.TPSDetect;
import ru.govno.client.utils.Math.MathUtils;

public class GameSyncTPS extends Module {
   public static GameSyncTPS instance;
   public FloatSettings SyncPercent;
   public BoolSettings OnlyAura;

   public GameSyncTPS() {
      super("GameSyncTPS", 0, Module.Category.PLAYER);
      instance = this;
      this.settings.add(this.SyncPercent = new FloatSettings("SyncPercent", 0.15F, 1.5F, 0.0F, this));
      this.settings.add(this.OnlyAura = new BoolSettings("OnlyAura", true, this));
   }

   public static double getConpenseMath(double val, float strenghZeroToOne) {
      double out = val - MathUtils.getDifferenceOf((double)(TPSDetect.getTPSServer() / 20.0F), val) * (double)strenghZeroToOne;
      return out < 0.075F ? 0.075F : val;
   }

   public static double getGameConpense(double prevTimerSpeed, float percentCompense) {
      return !instance.actived || instance.OnlyAura.getBool() && HitAura.TARGET == null ? prevTimerSpeed : getConpenseMath(prevTimerSpeed, percentCompense);
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByDouble((double)this.SyncPercent.getFloat());
   }
}
