package ru.govno.client.soundengine.filters;

import org.lwjgl.openal.EFX10;
import ru.govno.client.soundengine.filters.BaseFilter;

public class FilterReverb
extends BaseFilter {
    public float density = 1.0f;
    public float diffusion = 1.0f;
    public float gain = 0.32f;
    public float gainHF = 0.89f;
    public float decayTime = 1.49f;
    public float decayHFRatio = 0.83f;
    public float reflectionsGain = 0.05f;
    public float reflectionsDelay = 0.007f;
    public float lateReverbGain = 1.26f;
    public float lateReverbDelay = 0.011f;
    public float airAbsorptionGainHF = 0.994f;
    public float roomRolloffFactor = 0.0f;
    public int decayHFLimit = 1;

    @Override
    public void loadFilter() {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.id = EFX10.alGenEffects();
            this.slot = EFX10.alGenAuxiliaryEffectSlots();
        }
    }

    @Override
    public void checkParameters() {
        if (this.density < 0.0f) {
            this.density = 0.0f;
        }
        if (this.density > 1.0f) {
            this.density = 1.0f;
        }
        if (this.diffusion < 0.0f) {
            this.diffusion = 0.0f;
        }
        if (this.diffusion > 1.0f) {
            this.diffusion = 1.0f;
        }
        if (this.gain < 0.0f) {
            this.gain = 0.0f;
        }
        if (this.gain > 1.0f) {
            this.gain = 1.0f;
        }
        if (this.gainHF < 0.0f) {
            this.gainHF = 0.0f;
        }
        if (this.gainHF > 1.0f) {
            this.gainHF = 1.0f;
        }
        if (this.decayTime < 0.1f) {
            this.decayTime = 0.1f;
        }
        if (this.decayTime > 20.0f) {
            this.decayTime = 20.0f;
        }
        if (this.decayHFRatio < 0.1f) {
            this.decayHFRatio = 0.1f;
        }
        if (this.decayHFRatio > 2.0f) {
            this.decayHFRatio = 2.0f;
        }
        if (this.reflectionsGain < 0.0f) {
            this.reflectionsGain = 0.0f;
        }
        if (this.reflectionsGain > 3.16f) {
            this.reflectionsGain = 3.16f;
        }
        if (this.reflectionsDelay < 0.0f) {
            this.reflectionsDelay = 0.0f;
        }
        if (this.reflectionsDelay > 0.3f) {
            this.reflectionsDelay = 0.3f;
        }
        if (this.lateReverbGain < 0.0f) {
            this.lateReverbGain = 0.0f;
        }
        if (this.lateReverbGain > 10.0f) {
            this.lateReverbGain = 10.0f;
        }
        if (this.lateReverbDelay < 0.0f) {
            this.lateReverbDelay = 0.0f;
        }
        if (this.lateReverbDelay > 0.1f) {
            this.lateReverbDelay = 0.1f;
        }
        if (this.airAbsorptionGainHF < 0.892f) {
            this.airAbsorptionGainHF = 0.892f;
        }
        if (this.airAbsorptionGainHF > 1.0f) {
            this.airAbsorptionGainHF = 1.0f;
        }
        if (this.roomRolloffFactor < 0.0f) {
            this.roomRolloffFactor = 0.0f;
        }
        if (this.roomRolloffFactor > 10.0f) {
            this.roomRolloffFactor = 10.0f;
        }
        if (this.decayHFLimit < 0) {
            this.decayHFLimit = 0;
        }
        if (this.decayHFLimit > 1) {
            this.decayHFLimit = 1;
        }
    }

    @Override
    public void loadParameters() {
        this.checkParameters();
        if (!this.isLoaded) {
            this.loadFilter();
        }
        EFX10.alAuxiliaryEffectSlotf((int)this.slot, (int)2, (float)0.0f);
        EFX10.alEffecti((int)this.id, (int)32769, (int)1);
        EFX10.alEffectf((int)this.id, (int)1, (float)this.density);
        EFX10.alEffectf((int)this.id, (int)2, (float)this.diffusion);
        EFX10.alEffectf((int)this.id, (int)3, (float)this.gain);
        EFX10.alEffectf((int)this.id, (int)4, (float)this.gainHF);
        EFX10.alEffectf((int)this.id, (int)5, (float)this.decayTime);
        EFX10.alEffectf((int)this.id, (int)6, (float)this.decayHFRatio);
        EFX10.alEffectf((int)this.id, (int)7, (float)this.reflectionsGain);
        EFX10.alEffectf((int)this.id, (int)8, (float)this.reflectionsDelay);
        EFX10.alEffectf((int)this.id, (int)9, (float)this.lateReverbGain);
        EFX10.alEffectf((int)this.id, (int)10, (float)this.lateReverbDelay);
        EFX10.alEffectf((int)this.id, (int)11, (float)this.airAbsorptionGainHF);
        EFX10.alEffectf((int)this.id, (int)12, (float)this.roomRolloffFactor);
        EFX10.alEffecti((int)this.id, (int)13, (int)this.decayHFLimit);
        EFX10.alAuxiliaryEffectSloti((int)this.slot, (int)1, (int)this.id);
        EFX10.alAuxiliaryEffectSlotf((int)this.slot, (int)2, (float)1.0f);
    }
}

