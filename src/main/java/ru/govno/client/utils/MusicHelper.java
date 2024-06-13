package ru.govno.client.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.FloatControl.Type;

public class MusicHelper {
   private static AudioInputStream lastCreatedStream;
   private static final List<Clip> CLIPS_LIST = new ArrayList<>();
   private static final String packagePath = "/assets/minecraft/vegaline/sounds/";
   private static AudioFormat prevFormat;
   private static Info lastData;

   public static void playSound(String location, float volume) {
      CLIPS_LIST.stream().filter(Objects::nonNull).filter(clip -> clip.isOpen()).filter(clip -> !clip.isRunning()).forEach(Line::close);
      CLIPS_LIST.stream().filter(Objects::nonNull).filter(clip -> !clip.isOpen() || !clip.isRunning()).forEach(DataLine::stop);
      CLIPS_LIST.stream().filter(Objects::nonNull).toList().forEach(clip -> {
         if (!clip.isRunning()) {
            CLIPS_LIST.remove(clip);
         }
      });
      if ((lastCreatedStream = getAudioInputStreamAsResLoc("/assets/minecraft/vegaline/sounds/" + location)) != null) {
         Clip createdClip;
         if ((createdClip = createClip(lastCreatedStream)) != null) {
            CLIPS_LIST.add(createdClip);
         }

         CLIPS_LIST.stream().filter(Objects::nonNull).filter(clip -> !clip.isOpen()).forEach(clip -> {
            try {
               clip.open(lastCreatedStream);
               setClipVolume(clip, volume);
               clip.start();
            } catch (IOException | LineUnavailableException var3) {
               var3.fillInStackTrace();
            }
         });
      }
   }

   public static void playSound(String location) {
      playSound(location, 0.45F);
   }

   private static Clip createClip(AudioInputStream stream) {
      AudioFormat format = stream.getFormat();
      if (prevFormat != format) {
         lastData = new Info(Clip.class, stream.getFormat());
         prevFormat = format;
      }

      try {
         return (Clip)AudioSystem.getLine(lastData);
      } catch (LineUnavailableException var3) {
         var3.fillInStackTrace();
         return null;
      }
   }

   private static void setClipVolume(Clip clip, float volume) {
      if (clip.isControlSupported(Type.MASTER_GAIN)) {
         FloatControl volumeControl = (FloatControl)clip.getControl(Type.MASTER_GAIN);
         volumeControl.setValue((float)(Math.log(Math.max(Math.min((double)volume, 1.0), 0.0)) / Math.log(10.0) * 20.0));
      }
   }

   private static AudioInputStream getAudioInputStreamAsResLoc(String resLoc) {
      try {
         return AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(MusicHelper.class.getResourceAsStream(resLoc))));
      } catch (IOException | UnsupportedAudioFileException var2) {
         var2.fillInStackTrace();
         return null;
      }
   }
}
