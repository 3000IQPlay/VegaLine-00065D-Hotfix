package ru.govno.client.module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import ru.govno.client.event.EventManager;
import ru.govno.client.module.modules.ClickGui;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.module.modules.Hud;
import ru.govno.client.module.modules.Notifications;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ColorSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.module.settings.Settings;
import ru.govno.client.utils.ClientRP;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Render.AnimationUtils;

public class Module {
   public static final Minecraft mc = Minecraft.getMinecraft();
   public boolean actived;
   public String name;
   public int bind;
   public AnimationUtils stateAnim = new AnimationUtils(0.0F, 0.0F, 0.1F);
   public Module.Category category;
   public List<Settings> settings = new ArrayList<>();
   public String display = TextFormatting.GRAY + "";
   public Supplier<Boolean> visible = () -> true;
   public boolean registered;
   public long lastEnableTime = System.currentTimeMillis();

   public Module(String name, int bind, Module.Category category) {
      this.name = name;
      this.bind = bind;
      this.category = category;
      this.actived = false;
   }

   public Module(String name, int bind, Module.Category category, boolean enabled) {
      this.name = name;
      this.bind = bind;
      this.category = category;
      this.toggleSilent(enabled);
   }

   public Module(String name, int bind, Module.Category category, boolean enabled, Supplier<Boolean> visible) {
      this.name = name;
      this.bind = bind;
      this.category = category;
      this.visible = visible;
      this.actived = enabled;
   }

   public Module(String name, int bind, Module.Category category, Supplier<Boolean> visible) {
      this.name = name;
      this.bind = bind;
      this.category = category;
      this.visible = visible;
   }

   public List<Settings> getSettings() {
      return this.settings;
   }

   public boolean isVisible() {
      return this.visible.get();
   }

   public String getName() {
      return this.name;
   }

   public boolean isActived() {
      return this != null && this.actived;
   }

   public boolean showToArrayList() {
      return (this.actived || (double)this.stateAnim.getAnim() > 0.03) && (this.bind != 0 || !Hud.get.isOnlyBound()) && this.category != Module.Category.RENDER;
   }

   public void disable() {
      this.toggle(false, false);
   }

   public void disableSilent() {
      this.toggle(false, true);
   }

   public void enable() {
      this.toggle(true, false);
   }

   public void enableSilent() {
      this.toggle(true, true);
   }

   public void toggleSilent(boolean actived) {
      this.toggle(actived, true);
   }

   public void toggle() {
      this.toggle(!this.actived);
   }

   public void toggle(boolean actived) {
      this.toggle(actived, false);
   }

   public void toggle(boolean actived, boolean silent) {
      if (this.actived != actived) {
         this.actived = actived;
         if (actived) {
            this.lastEnableTime = System.currentTimeMillis();
         }

         if (!silent && !Panic.stop) {
            if (!(this instanceof ClickGui)) {
               ClientTune.get.playModule(actived);
               if (Notifications.get.actived && this != Notifications.get) {
                  Notifications.Notify.spawnNotify(this.name, this.actived ? Notifications.type.ENABLE : Notifications.type.DISABLE);
               }
            }

            ClientRP.getInstance().getDiscordRP().refresh();
         }

         if (actived) {
            if (!this.registered) {
               EventManager.register(this);
               this.registered = true;
            }
         } else if (this.registered) {
            EventManager.unregister(this);
            this.registered = false;
         }

         if (Minecraft.player != null) {
            if (!Panic.stop && Hud.get != null && (Hud.get.isArraylist() || Hud.get.isKeyBindsHud())) {
               Hud.get.trigerSort(Hud.get.isTitles());
            }

            this.onToggled(actived && !Panic.stop);
         }
      }
   }

   public String getSuff() {
      return TextFormatting.GRAY + " - ";
   }

   public String getDisplayByDouble(double theDouble) {
      return this.name + this.getSuff() + (double)Math.round(theDouble * 100.0) / 100.0;
   }

   public String getDisplayByInt(int theInt) {
      return this.name + this.getSuff() + theInt;
   }

   public String getDisplayByMode(String mode) {
      return this.name + this.getSuff() + mode;
   }

   public String getDisplayName() {
      return this.name;
   }

   public boolean isBetaModule() {
      return false;
   }

   public Settings getSetting(String name, Settings.Category settingCategory) {
      return this.settings.stream().filter(set -> set.category == settingCategory).filter(set -> set.getName().equalsIgnoreCase(name)).findFirst().get();
   }

   public String currentMode(String name) {
      return ((ModeSettings)this.getSetting(name, Settings.Category.String_Massive)).getMode();
   }

   public float currentFloatValue(String name) {
      return ((FloatSettings)this.getSetting(name, Settings.Category.Float)).getFloat();
   }

   public boolean currentBooleanValue(String name) {
      return ((BoolSettings)this.getSetting(name, Settings.Category.Boolean)).getBool();
   }

   public int currentColorValue(String name) {
      return ((ColorSettings)this.getSetting(name, Settings.Category.Color)).getCol();
   }

   public void onUpdate() {
   }

   public void onRender2D(ScaledResolution sr) {
   }

   public void onMovement() {
   }

   public void onUpdateMovement() {
   }

   public void onRenderUpdate() {
   }

   public void alwaysRender2D(ScaledResolution sr) {
   }

   public void alwaysRender2D(float partialTicks, ScaledResolution sr) {
   }

   public void onPostRender2D(ScaledResolution sr) {
   }

   public void alwaysRender3D() {
   }

   public void alwaysRender3DV2() {
   }

   public void alwaysRender3DV2(float partialTicks) {
   }

   public void alwaysRender3D(float partialTicks) {
   }

   public void alwaysUpdate() {
   }

   public void preRenderLivingBase(Entity baseIn, Runnable renderModel, boolean isRenderItems) {
   }

   public void postRenderLivingBase(Entity baseIn, Runnable renderModel, boolean isRenderItems) {
   }

   public void onToggled(boolean actived) {
   }

   public void onMouseClick(int mouseButton) {
   }

   public int getBind() {
      return this.bind;
   }

   public void setBind(int bind) {
      if (bind == 211) {
         bind = 0;
      }

      this.bind = bind;
   }

   public static enum Category {
      COMBAT,
      MOVEMENT,
      RENDER,
      PLAYER,
      MISC;
   }
}
