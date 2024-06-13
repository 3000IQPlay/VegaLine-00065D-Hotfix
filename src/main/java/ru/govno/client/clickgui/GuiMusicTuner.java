package ru.govno.client.clickgui;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Command.impl.Panic;
import ru.govno.client.utils.Render.AnimationUtils;

public class GuiMusicTuner {
   String musicName;
   String forceMusicName;
   String path = "/assets/minecraft/vegaline/sounds/";
   String format = ".wav";
   AnimationUtils volume = new AnimationUtils(0.0F, 0.0F, 0.01F);
   float maxVolume;
   boolean wantToChangeTrack;
   Clip clip;
   AudioInputStream stream;
   String temporaryTrackLoc;

   public GuiMusicTuner(String musicName, float normalVolume) {
      this.musicName = musicName;
      this.maxVolume = normalVolume;
   }

   public void setVolumePC(float value) {
      this.volume.setAnim(value * this.getMaxVolumeVal());
      this.volume.to = value * this.getMaxVolumeVal();
   }

   public void setPlaying(boolean playing) {
      this.setVolumeSmoothPC(playing ? 1.0F : 0.0F);
   }

   public float getMaxVolumeVal() {
      return this.maxVolume;
   }

   public void setMaxVolume(float value) {
      this.maxVolume = value / 4.0F;
   }

   public void multipleVolume(float mul) {
      this.maxVolume *= mul;
      this.maxVolume = this.maxVolume < 0.0F ? 0.0F : (this.maxVolume > 1.0F ? 1.0F : this.maxVolume);
   }

   public void setVolumeSmoothPC(float value) {
      this.volume.speed = (value == 1.0F ? 5.0E-4F + 0.02F * this.volume.getAnim() : 0.004F) * (this.wantToChangeTrack ? 1.25F : 0.75F);
      this.volume.to = value * this.getMaxVolumeVal();
      if (this.volume.to == this.getMaxVolumeVal() && this.getVolumeVal() == 0.0F) {
         this.volume.setAnim(this.getMaxVolumeVal() / 5.0F);
      }
   }

   public void setVolumeChangeSpeed(float value) {
      this.volume.speed = value;
   }

   public void setTrackName(String name) {
      if (this.forceMusicName == null) {
         this.forceMusicName = name;
      }

      this.wantToChangeTrack = !this.forceMusicName.equalsIgnoreCase(this.musicName);
      this.forceMusicName = name;
   }

   public void setTrackNameForce(String name) {
      this.musicName = name;
      this.forceMusicName = name;
   }

   public String getTrackLoc() {
      return this.path + this.musicName + this.format;
   }

   public String getForceTrackLoc() {
      return this.path + this.forceMusicName + this.format;
   }

   public float getVolumeVal() {
      float volume = this.volume.getAnim();
      return (double)volume < 1.0E-4 ? 0.0F : ((double)volume > 0.9999 ? 1.0F : volume);
   }

   float getVolumeForMixer() {
      return (float)(Math.log((double)this.getVolumeVal()) / Math.log(10.0) * 20.0);
   }

   public boolean canPlayTrack() {
      return this.getVolumeVal() != 0.0F;
   }

   public void controlTrackUpdater() {
      if (Panic.stop) {
         this.setPlaying(false);
      }

      if (this.wantToChangeTrack) {
         this.setVolumeSmoothPC(0.0F);
         if (this.getVolumeVal() == 0.0F || this.musicName == null) {
            this.setTrackNameForce(this.forceMusicName);
            this.wantToChangeTrack = false;
         }
      }

      String trackLoc = this.getTrackLoc();
      float volume = this.getVolumeForMixer();
      boolean play = this.canPlayTrack()
         || this.clip != null
            && this.clip.isRunning()
            && this.clip.isControlSupported(Type.MASTER_GAIN)
            && ((FloatControl)this.clip.getControl(Type.MASTER_GAIN)).getValue() != ((FloatControl)this.clip.getControl(Type.MASTER_GAIN)).getMinimum();
      if (play) {
         try {
            if (this.stream == null) {
               InputStream inputStream = MusicHelper.class.getResourceAsStream(trackLoc);
               BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
               this.stream = AudioSystem.getAudioInputStream(bufferedInputStream);
            }
         } catch (Exception var8) {
         }

         if (this.stream != null) {
            try {
               if (this.clip == null || this.clip != null && !this.clip.isOpen()) {
                  this.clip = AudioSystem.getClip();
               }
            } catch (Exception var9) {
            }
         }

         if (this.clip != null) {
            if (!this.clip.isOpen()) {
               try {
                  this.clip.open(this.stream);
               } catch (Exception var7) {
               }
            } else if (!this.clip.isRunning() || this.clip.getMicrosecondPosition() == this.clip.getMicrosecondLength()) {
               this.clip.setMicrosecondPosition(0L);
               ((FloatControl)this.clip.getControl(Type.MASTER_GAIN)).setValue((float)((int)volume));
               this.clip.start();
            }
         }
      } else if (this.clip != null && this.clip.isRunning()) {
         this.clip.stop();
         this.clip.close();
         this.clip = null;
         if (this.stream != null) {
            try {
               this.stream.close();
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }

         this.stream = null;
      }

      FloatControl volumeControl = this.clip == null ? null : (FloatControl)this.clip.getControl(Type.MASTER_GAIN);
      if (volumeControl != null && volumeControl.getValue() != (float)((int)volume)) {
         volumeControl.setValue((float)((int)volume));
      }

      if (this.temporaryTrackLoc == null && trackLoc != null) {
         this.temporaryTrackLoc = trackLoc;
      }

      if (trackLoc != null && this.temporaryTrackLoc != null && !this.temporaryTrackLoc.equalsIgnoreCase(trackLoc)) {
         this.temporaryTrackLoc = trackLoc;
         if (this.clip != null) {
            if (this.clip.isRunning()) {
               this.clip.stop();
            }

            if (this.clip.isOpen()) {
               this.clip.close();
            }

            this.clip = null;
            this.stream = null;
         }
      }
   }
}
