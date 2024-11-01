package ru.govno.client.soundengine;

import net.minecraft.client.Minecraft;
import ru.govno.client.module.modules.ClientTune;
import ru.govno.client.soundengine.SoundMixerBase;
import ru.govno.client.soundengine.SoundSurroundTool;
import ru.govno.client.soundengine.filters.FilterLowPass;
import ru.govno.client.soundengine.filters.FilterReverb;
import ru.govno.client.utils.Math.MathUtils;

public class SoundMixFilter {
    private final SoundMixerBase mixer = SoundMixerBase.loadMixer();
    private final SoundSurroundTool surround = SoundSurroundTool.build();
    private boolean state = true;

    public SoundMixerBase getMixer() {
        return this.mixer;
    }

    public SoundSurroundTool getSurround() {
        return this.surround;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    private SoundMixFilter() {
    }

    public static SoundMixFilter makeDistorterMixer() {
        return new SoundMixFilter();
    }

    public void updateMixer() {
        float[] args = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
        this.setState(ClientTune.get.getRTSoundSurround());
        if (this.state) {
            this.getSurround().setRtxDebug(ClientTune.get.getIsRTXDebugView());
            this.getSurround().setPlayer(Minecraft.player);
            this.getSurround().setTooPerfomance(ClientTune.get.getRTPerfomanceMode());
            args = this.getSurround().getGainArgsFromWorld();
        } else if (!this.getSurround().getListOfTestVecs().isEmpty()) {
            int sz = this.getSurround().getListOfTestVecs().size();
            int i = 0;
            while ((float)i < (float)sz / 2.0f) {
                if (!this.getSurround().getListOfTestVecs().isEmpty()) {
                    this.getSurround().getListOfTestVecs().remove(0);
                }
                ++i;
            }
        } else {
            this.getSurround().setRtxDebug(false);
        }
        this.getMixer().setEchoEffect(args[0] * 1.25f * (this.getSurround().isTooPerfomance() ? 0.333333f : 1.0f), args[1]);
        this.getMixer().setLowPass(args[2], args[3]);
        this.updateFiltersData();
    }

    private void updateFiltersData() {
        SoundMixerBase mixer = this.getMixer();
        FilterReverb reverbFilter = mixer.getReverbFilter();
        FilterLowPass lowPassFilter = mixer.getLowPassFilter();
        float echoDelay = mixer.getEchoPercent() / 1.75f;
        float echoRev = mixer.getReflectPercent() / 1.25f;
        reverbFilter.decayTime = echoDelay;
        reverbFilter.reflectionsGain = echoRev * (0.05f + 0.05f * echoDelay);
        reverbFilter.reflectionsDelay = 0.125f * echoDelay;
        reverbFilter.lateReverbGain = echoRev * (1.26f + 0.2f * echoDelay);
        reverbFilter.lateReverbDelay = 0.01f * echoDelay;
        reverbFilter.checkParameters();
        float lLevelGain = mixer.getLowPassGain();
        float lLevelGainHF = mixer.getLowPassGainHF();
        lowPassFilter.gain = MathUtils.lerp(lowPassFilter.gain, lLevelGain, lowPassFilter.gainHF > lLevelGainHF ? 0.05f : 0.1f);
        lowPassFilter.gainHF = MathUtils.lerp(lowPassFilter.gainHF, lLevelGainHF, lowPassFilter.gainHF > lLevelGainHF ? 0.3f : 0.12f);
        lowPassFilter.checkParameters();
    }

    public boolean getHasMixerLoaded() {
        return this.mixer != null;
    }

    public void init() {
    }

    public void unload() {
        this.mixer.cleanupEffects();
    }
}

