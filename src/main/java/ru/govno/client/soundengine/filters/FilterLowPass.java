package ru.govno.client.soundengine.filters;

import org.lwjgl.openal.EFX10;
import ru.govno.client.soundengine.filters.BaseFilter;

public class FilterLowPass
extends BaseFilter {
    public float gain = 1.0f;
    public float gainHF = 1.0f;

    @Override
    public void loadFilter() {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.slot = this.id = EFX10.alGenFilters();
            EFX10.alFilteri((int)this.id, (int)32769, (int)1);
        }
    }

    @Override
    public void checkParameters() {
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
    }

    @Override
    public void loadParameters() {
        this.checkParameters();
        this.loadFilter();
        EFX10.alFilterf((int)this.id, (int)1, (float)this.gain);
        EFX10.alFilterf((int)this.id, (int)2, (float)this.gainHF);
    }
}

