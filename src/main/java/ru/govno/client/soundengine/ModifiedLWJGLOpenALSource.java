package ru.govno.client.soundengine;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import paulscode.sound.FilenameURL;
import paulscode.sound.SoundBuffer;
import paulscode.sound.Source;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.SourceLWJGLOpenAL;
import ru.govno.client.soundengine.SoundMixerBase;
import ru.govno.client.soundengine.filters.BaseFilter;
import ru.govno.client.soundengine.filters.FilterException;
import ru.govno.client.soundengine.filters.FilterLowPass;
import ru.govno.client.soundengine.filters.FilterReverb;

public class ModifiedLWJGLOpenALSource
extends SourceLWJGLOpenAL {
    public ModifiedLWJGLOpenALSource(FloatBuffer listenerPosition, IntBuffer myBuffer, Source old, SoundBuffer soundBuffer) {
        super(listenerPosition, myBuffer, old, soundBuffer);
    }

    public ModifiedLWJGLOpenALSource(FloatBuffer listenerPosition, IntBuffer myBuffer, boolean priority, boolean toStream, boolean toLoop, String sourcename, FilenameURL filenameURL, SoundBuffer soundBuffer, float x, float y, float z, int attModel, float distOrRoll, boolean temporary) {
        super(listenerPosition, myBuffer, priority, toStream, toLoop, sourcename, filenameURL, soundBuffer, x, y, z, attModel, distOrRoll, temporary);
    }

    public ModifiedLWJGLOpenALSource(FloatBuffer listenerPosition, AudioFormat audioFormat, boolean priority, String sourcename, float x, float y, float z, int attModel, float distOrRoll) {
        super(listenerPosition, audioFormat, priority, sourcename, x, y, z, attModel, distOrRoll);
    }

    public boolean stopped() {
        boolean stopped = super.stopped();
        if (this.channel != null && this.channel.attachedSource == this && !stopped && !this.paused()) {
            this.updateFilters();
        }
        return stopped;
    }

    private void updateFilters() {
        boolean canUseFilters = (!this.toStream || this.position.x != 0.0f || this.position.y != 0.0f || this.position.z != 0.0f || this.attModel != 0) && !Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().world != null;
        ChannelLWJGLOpenAL alChannel = (ChannelLWJGLOpenAL)this.channel;
        SoundMixerBase mixer = Minecraft.getMinecraft().sndHandleEdit.getMixer();
        FilterReverb reverbFilter = mixer.getReverbFilter();
        FilterLowPass lowPassFilter = mixer.getLowPassFilter();
        if (canUseFilters) {
            if (reverbFilter.reflectionsDelay > 0.0f && reverbFilter.lateReverbDelay > 0.0f) {
                reverbFilter.enable();
                reverbFilter.loadParameters();
            } else {
                reverbFilter.disable();
            }
            if (this.attModel != 0 && (lowPassFilter.gain != 1.0f || lowPassFilter.gainHF != 1.0f)) {
                lowPassFilter.enable();
                lowPassFilter.loadParameters();
            } else {
                lowPassFilter.disable();
            }
            try {
                BaseFilter.loadSourceFilter(alChannel.ALSource.get(0), 131077, lowPassFilter);
                BaseFilter.load3SourceFilters(alChannel.ALSource.get(0), 131078, reverbFilter, null, lowPassFilter);
            }
            catch (FilterException var9) {
                CrashReport crashreport = CrashReport.makeCrashReport(var9, "Updating Sound Filters");
                throw new ReportedException(crashreport);
            }
        }
        lowPassFilter.disable();
        reverbFilter.disable();
        try {
            BaseFilter.loadSourceFilter(alChannel.ALSource.get(0), 131077, null);
            BaseFilter.load3SourceFilters(alChannel.ALSource.get(0), 131078, null, null, null);
        }
        catch (FilterException e) {
            CrashReport crashreport = CrashReport.makeCrashReport(e, "Updating Sound Filters");
            throw new ReportedException(crashreport);
        }
    }
}

