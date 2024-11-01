package ru.govno.client.module.modules;

import net.minecraft.util.ResourceLocation;
import ru.govno.client.clickgui.GuiMusicTuner;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;

public class BadTrip
extends Module {
    public static BadTrip get;
    public BoolSettings CrazySpider = new BoolSettings("CrazySpider", true, this);
    public BoolSettings SpreadCreeper;
    public BoolSettings FlattenPlayersHurt;
    public BoolSettings SteveChiken;
    public BoolSettings VillagerNose;
    public BoolSettings HumpPlayers;
    public BoolSettings SelfBuff;
    public BoolSettings AmbientSound;
    public BoolSettings Alkash;
    public BoolSettings PsichoTnt;
    FloatSettings SoundVolume;
    public ModeSettings HumpStrengh;
    public ModeSettings Sound;
    public static ResourceLocation STEVE;
    public final GuiMusicTuner ambientTuner;

    public BadTrip() {
        super("BadTrip", 0, Module.Category.MISC);
        this.settings.add(this.CrazySpider);
        this.PsichoTnt = new BoolSettings("PsichoTnt", true, this);
        this.settings.add(this.PsichoTnt);
        this.SpreadCreeper = new BoolSettings("SpreadCreeper", true, this);
        this.settings.add(this.SpreadCreeper);
        this.FlattenPlayersHurt = new BoolSettings("FlattenPlayersHurt", true, this);
        this.settings.add(this.FlattenPlayersHurt);
        this.SteveChiken = new BoolSettings("SteveChiken", true, this);
        this.settings.add(this.SteveChiken);
        this.VillagerNose = new BoolSettings("VillagerNose", true, this);
        this.settings.add(this.VillagerNose);
        this.HumpPlayers = new BoolSettings("HumpPlayers", false, this);
        this.settings.add(this.HumpPlayers);
        this.HumpStrengh = new ModeSettings("HumpStrengh", "Middle", this, new String[]{"Lower", "Middle", "Highest"}, () -> this.HumpPlayers.getBool());
        this.settings.add(this.HumpStrengh);
        this.SelfBuff = new BoolSettings("SelfBuff", false, this);
        this.settings.add(this.SelfBuff);
        this.AmbientSound = new BoolSettings("AmbientSound", false, this);
        this.settings.add(this.AmbientSound);
        this.Sound = new ModeSettings("Sound", "Toilet", this, new String[]{"Toilet", "HiFi-Forest"}, () -> this.AmbientSound.getBool());
        this.settings.add(this.Sound);
        this.SoundVolume = new FloatSettings("SoundVolume", 40.0f, 200.0f, 5.0f, this, () -> this.AmbientSound.getBool());
        this.settings.add(this.SoundVolume);
        this.ambientTuner = new GuiMusicTuner("ambient" + (this.Sound.currentMode.equalsIgnoreCase("Toilet") ? "1" : "2"), 0.2f);
        this.Alkash = new BoolSettings("Alkash", true, this);
        this.settings.add(this.Alkash);
        get = this;
    }

    private void updateAmbient() {
        if (this.AmbientSound.getBool()) {
            boolean isToilet = this.Sound.currentMode.equalsIgnoreCase("Toilet");
            if (this.ambientTuner.isPlaying()) {
                this.ambientTuner.setTrackName("ambient" + (isToilet ? "1" : "2"));
            } else {
                this.ambientTuner.setTrackNameForce("ambient" + (isToilet ? "1" : "2"));
            }
            this.ambientTuner.setMaxVolume(this.SoundVolume.getFloat() / 100.0f * (float)(isToilet ? 1 : 2));
        }
        this.ambientTuner.setPlaying(this.AmbientSound.getBool());
    }

    public void offAmbient() {
        this.ambientTuner.setPlaying(false);
    }

    @Override
    public void onToggled(boolean actived) {
        if (!actived) {
            this.offAmbient();
        }
        super.onToggled(actived);
    }

    @Override
    public void onUpdate() {
        this.updateAmbient();
    }

    public boolean isCrazySpider() {
        return this.actived && this.CrazySpider.getBool();
    }

    public boolean isSpreadCreeper() {
        return this.actived && this.SpreadCreeper.getBool();
    }

    public boolean isFlattenPlayersHurt() {
        return this.actived && this.FlattenPlayersHurt.getBool();
    }

    public boolean isSteveChiken() {
        return this.actived && this.SteveChiken.getBool();
    }

    public boolean isVillagerNose() {
        return this.actived && this.VillagerNose.getBool();
    }

    public boolean isPsichoTnt() {
        return this.actived && this.PsichoTnt.getBool();
    }

    public int getPlayerHumpLevel() {
        if (this.actived && this.HumpPlayers.canBeRender()) {
            for (int modeI = 0; modeI < this.HumpStrengh.modes.length; ++modeI) {
                if (!this.HumpStrengh.modes[modeI].equalsIgnoreCase(this.HumpStrengh.currentMode)) continue;
                return modeI + 1;
            }
        }
        return 0;
    }

    public boolean isSelfBuff() {
        return this.actived && this.SelfBuff.canBeRender();
    }

    public boolean isAlkash() {
        return this.actived && this.Alkash.canBeRender();
    }

    static {
        STEVE = new ResourceLocation("textures/entity/steve.png");
    }
}

