package ru.govno.client.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicHelper {
    private static AudioInputStream lastCreatedStream;
    private static final CopyOnWriteArrayList<Clip> CLIPS_LIST;
    private static final String packagePath = "/assets/minecraft/vegaline/sounds/";
    private static AudioFormat prevFormat;
    private static DataLine.Info lastData;

    public static void playSound(String location, float volume) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            lastCreatedStream = MusicHelper.getAudioInputStreamAsResLoc(packagePath + location);
            if (lastCreatedStream == null) {
                return;
            }
            CLIPS_LIST.stream().filter(Line::isOpen).filter(clip -> !clip.isRunning()).forEach(Line::close);
            CLIPS_LIST.removeIf(clip -> !clip.isRunning());
            Clip createdClip = MusicHelper.createClip(lastCreatedStream);
            if (createdClip != null) {
                CLIPS_LIST.add(createdClip);
            }
            CLIPS_LIST.stream().filter(Objects::nonNull).filter(clip -> !clip.isOpen()).forEach(clip -> {
                try {
                    clip.open(lastCreatedStream);
                    MusicHelper.setClipVolume(clip, volume);
                    clip.start();
                }
                catch (IOException | LineUnavailableException LUE) {
                    LUE.fillInStackTrace();
                }
            });
        });
    }

    public static void playSound(String location) {
        MusicHelper.playSound(location, 0.45f);
    }

    private static Clip createClip(AudioInputStream stream) {
        AudioFormat format = stream.getFormat();
        if (prevFormat != format) {
            lastData = new DataLine.Info(Clip.class, stream.getFormat());
            prevFormat = format;
        }
        try {
            return (Clip)AudioSystem.getLine(lastData);
        }
        catch (LineUnavailableException LUE) {
            LUE.fillInStackTrace();
            return null;
        }
    }

    private static void setClipVolume(Clip clip, float volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl volumeControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        volumeControl.setValue((float)(Math.log(Math.max(Math.min((double)volume, 1.0), 0.0)) / Math.log(10.0) * 20.0));
    }

    private static AudioInputStream getAudioInputStreamAsResLoc(String resLoc) {
        try {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(MusicHelper.class.getResourceAsStream(resLoc))));
        }
        catch (IOException | UnsupportedAudioFileException ULT) {
            ULT.fillInStackTrace();
            return null;
        }
    }

    static {
        CLIPS_LIST = new CopyOnWriteArrayList();
    }
}

