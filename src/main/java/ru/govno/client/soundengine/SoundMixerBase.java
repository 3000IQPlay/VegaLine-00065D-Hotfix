package ru.govno.client.soundengine;

import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import ru.govno.client.soundengine.ModifiedLWJGLOpenALLibrary;
import ru.govno.client.soundengine.filters.FilterLowPass;
import ru.govno.client.soundengine.filters.FilterReverb;
import ru.govno.client.utils.Math.MathUtils;

public class SoundMixerBase {
    private float echoPercent;
    private float reflectPercent;
    private float lowPassGain;
    private float lowPassGainHF;
    private final FilterLowPass lowPassFilter = new FilterLowPass();
    private final FilterReverb reverbFilter = new FilterReverb();
    private boolean locked;

    public float getEchoPercent() {
        return this.echoPercent;
    }

    public void setEchoPercent(float echoPercent) {
        this.echoPercent = echoPercent;
    }

    public float getReflectPercent() {
        return this.reflectPercent;
    }

    public void setReflectPercent(float reflectPercent) {
        this.reflectPercent = reflectPercent;
    }

    public float getLowPassGain() {
        return this.lowPassGain;
    }

    public void setLowPassGain(float lowPassGain) {
        this.lowPassGain = lowPassGain;
    }

    public float getLowPassGainHF() {
        return this.lowPassGainHF;
    }

    public void setLowPassGainHF(float lowPassGainHF) {
        this.lowPassGainHF = lowPassGainHF;
    }

    public FilterLowPass getLowPassFilter() {
        return this.lowPassFilter;
    }

    public FilterReverb getReverbFilter() {
        return this.reverbFilter;
    }

    public SoundMixerBase(Class libraryClass) {
        if (SoundSystemConfig.getLibraries() != null) {
            SoundSystemConfig.getLibraries().clear();
        }
        try {
            SoundSystemConfig.addLibrary((Class)libraryClass);
        }
        catch (SoundSystemException e) {
            e.fillInStackTrace();
        }
    }

    public static SoundMixerBase loadMixer() {
        return new SoundMixerBase(ModifiedLWJGLOpenALLibrary.class);
    }

    public void setEchoEffect(float longest, float reflect) {
        if (this.locked) {
            return;
        }
        this.echoPercent = MathUtils.lerp(this.echoPercent, longest, this.echoPercent > longest ? 0.75f : 0.55f);
        this.reflectPercent = MathUtils.lerp(this.reflectPercent, reflect, this.reflectPercent > reflect ? 0.75f : 0.55f);
    }

    public void setLowPass(float gain, float gainHF) {
        if (this.locked) {
            return;
        }
        this.lowPassGain = gain;
        this.lowPassGainHF = gainHF;
    }

    public void cleanupEffects() {
        if (this.locked) {
            return;
        }
        this.echoPercent = 0.0f;
        this.reflectPercent = 0.0f;
        this.lowPassGain = 1.0f;
        this.lowPassGainHF = 1.0f;
    }

    public void lockup() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }
}

