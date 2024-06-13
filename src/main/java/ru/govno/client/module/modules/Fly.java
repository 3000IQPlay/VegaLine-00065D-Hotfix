package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.event.events.EventTransformSideFirstPerson;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.newfont.Fonts;
import ru.govno.client.utils.MusicHelper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.AnimationUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class Fly extends Module {
   public static Fly get;
   public ModeSettings Mode;
   public ModeSettings MatrixLevel;
   public BoolSettings UpWard;
   public BoolSettings DownWard;
   public BoolSettings AutoUp;
   public BoolSettings SmoothSpeed;
   public BoolSettings NoVanillaKick;
   public BoolSettings DisableOnFlag;
   public BoolSettings Visualization;
   public BoolSettings Helicopter;
   public FloatSettings SpeedY;
   public FloatSettings SpeedXZ;
   public FloatSettings SpeedCrate;
   public FloatSettings StepCrate;
   final TimerHelper ticker = new TimerHelper();
   final TimerHelper ticker2 = new TimerHelper();
   final TimerHelper timeFlying = new TimerHelper();
   int tickerFinalling;
   private boolean enableGround = false;
   public static double flySpeed = 0.0;
   private final AnimationUtils alphed = new AnimationUtils(0.0F, 0.0F, 0.075F);
   int ticksBoost = 0;
   private final TimerHelper soundTimer = new TimerHelper();

   public Fly() {
      super("Fly", 0, Module.Category.MOVEMENT);
      this.settings
         .add(
            this.Mode = new ModeSettings(
               "Mode",
               "MatrixChunk",
               this,
               new String[]{"Vanilla", "MatrixChunk", "MatrixOld", "NCP", "Jartex", "AAC", "Rage", "Motion", "Grim", "MatrixLatest"}
            )
         );
      this.settings
         .add(
            this.MatrixLevel = new ModeSettings(
               "MatrixLevel", "Lowest", this, new String[]{"Lowest", "Highest"}, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixLatest")
            )
         );
      this.settings.add(this.UpWard = new BoolSettings("UpWard", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")));
      this.settings.add(this.DownWard = new BoolSettings("DownWard", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")));
      this.settings.add(this.AutoUp = new BoolSettings("AutoUp", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")));
      this.settings.add(this.SmoothSpeed = new BoolSettings("SmoothSpeed", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")));
      this.settings
         .add(
            this.NoVanillaKick = new BoolSettings(
               "NoVanillaKick", false, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixChunk") || this.Mode.currentMode.equalsIgnoreCase("Rage")
            )
         );
      this.settings
         .add(
            this.SpeedY = new FloatSettings(
               "SpeedY", 1.0F, 10.0F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Vanilla") || this.Mode.currentMode.equalsIgnoreCase("Motion")
            )
         );
      this.settings
         .add(
            this.SpeedXZ = new FloatSettings(
               "SpeedXZ",
               4.5F,
               10.0F,
               0.0F,
               this,
               () -> this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")
                     || this.Mode.currentMode.equalsIgnoreCase("Vanilla")
                     || this.Mode.currentMode.equalsIgnoreCase("Motion")
            )
         );
      this.settings
         .add(
            this.DisableOnFlag = new BoolSettings(
               "DisableOnFlag", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixChunk") || this.Mode.currentMode.equalsIgnoreCase("MatrixOld")
            )
         );
      this.settings.add(this.SpeedCrate = new FloatSettings("SpeedCrate", 6.0F, 10.0F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Rage")));
      this.settings.add(this.StepCrate = new FloatSettings("StepCrate", 3.0F, 10.0F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Rage")));
      this.settings
         .add(
            this.Visualization = new BoolSettings(
               "Visualization", true, this, () -> !this.Mode.currentMode.equalsIgnoreCase("Jartex") && !this.Mode.currentMode.equalsIgnoreCase("NCP")
            )
         );
      this.settings.add(this.Helicopter = new BoolSettings("Helicopter", false, this, () -> !this.Mode.currentMode.equalsIgnoreCase("NCP")));
      get = this;
   }

   @Override
   public void alwaysRender3D() {
      this.render3d(this.alphed.getAnim());
   }

   void render3d(float alphaPC) {
      if (alphaPC > 0.005F) {
         GL11.glPushMatrix();
         mc.entityRenderer.disableLightmap();
         GL11.glBlendFunc(770, 771);
         GL11.glEnable(3042);
         GL11.glLineWidth(1.0E-5F);
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glDisable(3008);
         GL11.glShadeModel(7425);
         GL11.glEnable(2848);
         float ext = Minecraft.player.width / 2.0F - 0.01F;
         GL11.glBegin(9);
         RenderUtils.setupColor(ClientColors.getColor1(), 20.0F * alphaPC);
         GL11.glVertex3d((double)(-ext), 0.0, (double)(-ext));
         RenderUtils.setupColor(ClientColors.getColor1(100), 20.0F * alphaPC);
         GL11.glVertex3d((double)(-ext), 0.0, (double)ext);
         RenderUtils.setupColor(ClientColors.getColor1(200), 20.0F * alphaPC);
         GL11.glVertex3d((double)ext, 0.0, (double)ext);
         RenderUtils.setupColor(ClientColors.getColor1(300), 20.0F * alphaPC);
         GL11.glVertex3d((double)ext, 0.0, (double)(-ext));
         GL11.glEnd();
         int endCC = Minecraft.player.isCollided ? ColorUtils.getColor(255, 40, 40) : -1;
         GL11.glBegin(3);
         RenderUtils.setupColor(endCC, 170.0F * alphaPC);
         GL11.glVertex3d((double)(-ext), 0.0, (double)(-ext));
         GL11.glVertex3d((double)(-ext), 0.0, (double)ext);
         GL11.glVertex3d((double)ext, 0.0, (double)ext);
         GL11.glVertex3d((double)ext, 0.0, (double)(-ext));
         GL11.glVertex3d((double)(-ext), 0.0, (double)(-ext));
         GL11.glEnd();
         GL11.glLineWidth(1.0F);
         GL11.glShadeModel(7424);
         GL11.glEnable(3553);
         GlStateManager.resetColor();
         float sizeXZ = 0.003F;
         GL11.glDisable(2929);
         GL11.glBlendFunc(770, 771);
         GL11.glEnable(3042);
         GL11.glTranslated((double)sizeXZ, 0.0, (double)sizeXZ);
         GL11.glRotated(180.0, 0.0, 0.0, 1.0);
         GL11.glRotated(90.0, 1.0, 0.0, 0.0);
         GL11.glRotated(180.0, 0.0, 1.0, 0.0);
         GL11.glTranslated(0.0, 0.0, -1.0E-5);
         GL11.glRotated((double)((float)((int)((Minecraft.player.rotationYaw - 22.0F) / 45.0F)) * 45.0F - 180.0F), 0.0, 0.0, 1.0);
         GL11.glScaled((double)sizeXZ, (double)sizeXZ, (double)sizeXZ);
         float yaw = Minecraft.player.lastReportedYaw % 360.0F + (float)(Minecraft.player.rotationYaw < 0.0F ? 360 : 0);
         String dir = yaw > 0.0F && yaw <= 22.0F
            ? "z+"
            : (
               yaw > 22.0F && yaw <= 67.0F
                  ? "x-z+"
                  : (
                     yaw > 67.0F && yaw < 112.0F
                        ? "x-"
                        : (
                           yaw > 112.0F && yaw < 157.0F
                              ? "x-z-"
                              : (
                                 yaw > 157.0F && yaw < 202.0F
                                    ? "z-"
                                    : (
                                       yaw > 202.0F && yaw < 247.0F
                                          ? "x+z-"
                                          : (yaw > 247.0F && yaw < 292.0F ? "x+" : (yaw > 292.0F && yaw < 337.0F ? "x+z+" : "z+"))
                                    )
                              )
                        )
                  )
            );
         String speed = "§6Speed: §e" + String.format("%.1f", MoveMeHelp.getCuttingSpeed()) + "§b dir " + dir;
         if (alphaPC * 255.0F >= 34.0F) {
            Fonts.mntsb_36.drawStringWithShadow(speed, (double)(-Fonts.mntsb_36.getStringWidth(speed) / 2), -3.0, ColorUtils.swapAlpha(-1, alphaPC * 255.0F));
         }

         GL11.glEnable(2929);
         GL11.glEnable(3008);
         GL11.glEnable(2896);
         GL11.glPopMatrix();
      }

      if (this.actived && this.Helicopter.getBool() && this.Helicopter.isVisible() && this.soundTimer.hasReached(1850.0)) {
         MusicHelper.playSound("helicopter.wav", 0.3F);
         this.soundTimer.reset();
      }
   }

   void render2d(float alphaPC, ScaledResolution sr) {
      if (alphaPC * 255.0F >= 34.0F) {
         List<String> strs = new ArrayList<>();
         strs.add(
            TextFormatting.AQUA
               + "Speed"
               + TextFormatting.GRAY
               + " = "
               + TextFormatting.WHITE
               + (
                  MoveMeHelp.getCuttingSpeed() == 0.0
                     ? "0"
                     : String.format("%.2f", MoveMeHelp.getCuttingSpeed()).replace("0.00", "0").replace("0.", ".")
                        + " | "
                        + String.format("%.2f", MoveMeHelp.getCuttingSpeed() * 15.3571428571).replace("0.00", "0").replace("0.", ".")
               )
         );
         strs.add(
            TextFormatting.AQUA
               + "Ymotion"
               + TextFormatting.GRAY
               + " = "
               + TextFormatting.WHITE
               + String.format("%.2f", Entity.Getmotiony).replace("0.00", "0").replace("0.", ".")
         );
         strs.add(TextFormatting.AQUA + "Ground" + TextFormatting.GRAY + " = " + TextFormatting.WHITE + (Minecraft.player.onGround ? "YES" : "NO"));
         strs.add(
            TextFormatting.AQUA
               + "Fall dst"
               + TextFormatting.GRAY
               + " = "
               + TextFormatting.WHITE
               + String.format("%.1f", Minecraft.player.fallDistance).replace("0.00", "0").replace("0.", ".")
         );
         strs.add(
            TextFormatting.AQUA
               + "Hurt"
               + TextFormatting.GRAY
               + " = "
               + TextFormatting.WHITE
               + "M="
               + (EntityLivingBase.isMatrixDamaged ? "T" : "F")
               + ",N="
               + (EntityLivingBase.isNcpDamaged ? "T" : "F")
               + ",S="
               + (EntityLivingBase.isSunRiseDamaged ? "T" : "F")
         );
         long ms = this.timeFlying.getTime();
         float sec = (float)ms / 1000.0F;
         int mins = (int)sec / 60;
         int hors = mins / 60;
         sec -= (float)(mins * 60);
         mins -= hors * 60;
         String timeString = ms < 100L
            ? "0s"
            : (hors > 0 ? hors + "h" : "") + (mins > 0 ? mins + "m" : "") + (sec > 0.0F ? String.format("%.2f", sec) + "s" : "");
         strs.add(TextFormatting.AQUA + "Fly time" + TextFormatting.GRAY + " = " + TextFormatting.WHITE + timeString);
         int texC = ColorUtils.getColor(255, (int)(255.0F * alphaPC));
         GL11.glPushMatrix();
         float step = 9.0F;
         float x = (float)sr.getScaledWidth() / 2.0F - 16.0F;
         float y = (float)sr.getScaledHeight() / 2.0F;
         float w = 0.0F;

         for (String str : strs) {
            if (w < (float)Fonts.noise_14.getStringWidth(str)) {
               w = (float)Fonts.noise_14.getStringWidth(str);
            }
         }

         RenderUtils.customScaledObject2D(x, y, 0.0F, (float)Fonts.noise_14.getHeight(), alphaPC);
         y -= step * (float)strs.size() / 2.0F;
         int i = 0;

         for (String strx : strs) {
            Fonts.noise_14
               .drawString(
                  strx, (double)(x - w) + MathUtils.getDifferenceOf(y + step / 2.0F, (float)sr.getScaledHeight() / 2.0F) / 4.0, (double)(y + 1.5F), texC
               );
            y += step;
            i++;
         }

         float r = w - 6.0F;
         RenderUtils.drawCroneShadow(
            (double)(x - w + r),
            (double)((float)sr.getScaledHeight() / 2.0F),
            245,
            315,
            r,
            4.0F,
            ColorUtils.getColor(10, 180, 255, 0),
            ColorUtils.getColor(255, (int)(100.0F * alphaPC)),
            true
         );
         RenderUtils.drawCroneShadow(
            (double)(x - w + r),
            (double)((float)sr.getScaledHeight() / 2.0F),
            245,
            315,
            r + 4.0F,
            1.0F,
            ColorUtils.getColor(255, (int)(100.0F * alphaPC)),
            0,
            true
         );
         GL11.glPopMatrix();
      }
   }

   @Override
   public void alwaysRender2D(ScaledResolution sr) {
      boolean rule = this.actived
         && this.Visualization.getBool()
         && !this.Mode.currentMode.equalsIgnoreCase("Jartex")
         && !this.Mode.currentMode.equalsIgnoreCase("NCP");
      this.alphed.to = rule ? 1.0F : 0.0F;
      this.alphed.speed = rule ? 0.06F : 0.04F;
      this.render2d(this.alphed.getAnim(), sr);
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.Mode.currentMode);
   }

   @Override
   public void onUpdate() {
      if (this.Mode.currentMode.equalsIgnoreCase("MatrixLatest")) {
         List<BlockPos> mixPoses = new ArrayList<>();
         Vec3d ePos = new Vec3d(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ);
         float r = 4.0F;

         for (float xs = -4.0F; xs < 4.0F; xs++) {
            for (float ys = -4.0F; ys < 1.0F; ys++) {
               for (float zs = -4.0F; zs < 4.0F; zs++) {
                  BlockPos poss = new BlockPos((double)xs + ePos.xCoord, (double)ys + ePos.yCoord, (double)zs + ePos.zCoord);
                  IBlockState state = mc.world.getBlockState(poss);
                  Block block = mc.world.getBlockState(poss).getBlock();
                  if (state.getMaterial().isReplaceable()
                     && block != Blocks.AIR
                     && block != Blocks.WATER
                     && block != Blocks.LAVA
                     && poss != null
                     && Minecraft.player.getDistanceAtEye((double)poss.getX() + 0.5, (double)poss.getY() + 0.5, (double)poss.getZ() + 0.5) <= 4.0) {
                     mixPoses.add(poss);
                  }
               }
            }
         }

         mixPoses.removeIf(current -> mc.world.getBlockState(current).getBlockHardness(mc.world, current) != 0.0F);
         if (mixPoses.size() != 0) {
            mixPoses.sort(
               Comparator.comparing(
                  current -> -Minecraft.player.getDistanceAtEye((double)current.getX() + 0.5, (double)current.getY() + 0.5, (double)current.getZ() + 0.5)
               )
            );
            BlockPos pos = mixPoses.get(0);
            if (pos != null) {
               Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
               Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
               this.ticksBoost = 1;
            }
         }

         if (this.ticksBoost > 0) {
            if (this.MatrixLevel.currentMode.equalsIgnoreCase("Lowest")) {
               if (Minecraft.player.fallDistance == 0.0F) {
                  if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                     Minecraft.player.jump();
                  }
               } else {
                  boolean prevSprint = Minecraft.player.getFlag(3);
                  Minecraft.player.setFlag(3, true);
                  Minecraft.player.jump();
                  if (MathUtils.getDifferenceOf(Minecraft.player.motionY, 0.0) > 0.1 && MathUtils.getDifferenceOf(Minecraft.player.motionY, 0.0) < 0.4) {
                     Minecraft.player.motionY /= 2.0;
                  } else {
                     Minecraft.player.motionY = !mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offsetMinDown(0.2)).isEmpty()
                        ? 0.2
                        : (Minecraft.player.isJumping() ? 1.0 : (Minecraft.player.isSneaking() ? -0.2 : 0.0));
                  }

                  if (!Minecraft.player.onGround) {
                     MoveMeHelp.setSpeed(MathUtils.clamp(MoveMeHelp.getSpeed() * 1.2F + (Minecraft.player.getFlag(3) ? 0.0 : 0.2), 0.999, 3.4));
                  }

                  Minecraft.player.setFlag(3, prevSprint);
               }
            } else if (Minecraft.player.fallDistance != 0.0F) {
               Minecraft.player.motionY = 0.0;
               MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
               if (Minecraft.player.isJumping()) {
                  Minecraft.player.onGround = true;
                  MoveMeHelp.setSpeed(0.0);
                  Minecraft.player.jumpMovementFactor = 0.0F;
               }
            } else if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
               Minecraft.player.jump();
               Minecraft.player.motionY = 0.73;
               Timer.forceTimer(10.0F);
            }

            this.ticksBoost--;
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("Grim")) {
         Minecraft.player.multiplyMotionXZ(0.0F);
         int packets = 10;
         float ySpeed = 0.02F * (float)packets;
         Minecraft.player.motionY = Minecraft.player.isJumping() ? (double)ySpeed : (Minecraft.player.isSneaking() ? (double)(-ySpeed) : 0.0);
         float speed = 0.05F * (float)packets;
         MoveMeHelp.setSpeed((double)speed);

         for (int i = 0; i < packets; i++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("Rage")) {
         for (int crate = 0; crate < this.StepCrate.getInt(); crate++) {
            Minecraft.player.connection.sendPacket(new CPacketPlayer(false));
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("AAC")) {
         double motY = Minecraft.player.motionY;
         if (!Minecraft.player.isJumping() && !Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 0.044, Minecraft.player.posZ)) {
            this.enableGround = false;
         }

         if (Minecraft.player.onGround && Minecraft.player.isCollidedVertically) {
            this.enableGround = true;
         }

         double setedMotY = Minecraft.player.isJumping()
            ? (this.enableGround ? (double)Minecraft.player.getJumpUpwardsMotion() : Minecraft.player.motionY)
            : Minecraft.player.motionY;
         Minecraft.player.motionY = setedMotY;
         if (MoveMeHelp.getSpeed() > 0.19) {
            Minecraft.player.jumpMovementFactor = 0.17F;
         }

         Minecraft.player.multiplyMotionXZ(1.005F);
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixOld")) {
         boolean ground = !mc.world
            .getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offsetMinDown(0.999F).expand(0.999F, 0.0, 0.999F))
            .isEmpty();
         Minecraft.player.onGround = false;
         Minecraft.player.jump();
         MoveMeHelp.setSpeed(MathUtils.clamp((double)((float)(MoveMeHelp.getSpeed() * (double)(Minecraft.player.onGround ? 0.0F : 1.1F))), 0.1, 5.0));
         Minecraft.player.motionY = mc.gameSettings.keyBindSneak.pressed
            ? Minecraft.player.motionY * (double)(Minecraft.player.motionY > 0.0 ? -1 : 1) - 0.42
            : (Minecraft.player.isJumping() ? (this.enableGround ? 0.2 + Minecraft.player.motionY : 0.42) : (Speed.canMatrixBoost() ? -0.2 : 0.0));
         if (ground) {
            this.ticker.reset();
         }

         if (this.ticker.hasReached((double)(1500 - (this.enableGround ? 50 : 350)))) {
            this.toggle(false);
            this.ticker.reset();
         }

         flySpeed = MathUtils.clamp((double)((float)(MoveMeHelp.getCuttingSpeed() * (double)(Minecraft.player.onGround ? 0.0F : 1.1F))), 0.1, 5.0);
      }

      if (this.Mode.currentMode.equalsIgnoreCase("NCP")) {
         boolean up = Minecraft.player.isJumping();
         boolean down = Minecraft.player.isSneaking();
         boolean move = MoveMeHelp.moveKeysPressed();
         double speed = !up && !down ? (move ? MoveMeHelp.getSpeedByBPS(4.010000000000001) : 0.0) : 0.0;
         float moveYaw = (float)Math.toRadians((double)MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
         double sinSpeed = -Math.sin((double)moveYaw) * speed;
         double cosSpeed = Math.cos((double)moveYaw) * speed;
         double ySpeed = up ? 0.062 : (down ? -0.102 : (move ? 0.0 : (Minecraft.player.ticksExisted % 2 == 0 && !Minecraft.player.onGround ? 0.01 : -0.01)));
         double pX = Minecraft.player.posX + sinSpeed;
         double pY = Minecraft.player.posY + ySpeed;
         double pZ = Minecraft.player.posZ + cosSpeed;
         MoveMeHelp.setMotionSpeed(true, false, 0.0);
         MoveMeHelp.setSpeed(-speed / 5.0);
         Entity.motiony = 1.0E-45;
         Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(pX, pY, pZ, false));
         Minecraft.player.connection.sendPacket(new CPacketPlayer.Position(0.0, pY + ySpeed, 0.0, true));
      }

      if (this.Mode.currentMode.equalsIgnoreCase("Jartex")) {
         double speed = 2.0;
         MoveMeHelp.setSpeed(0.0);
         MoveMeHelp.setCuttingSpeed(0.0);
         Minecraft.player.motionY = Minecraft.player.ticksExisted % 2 == 0 ? 0.1 : -0.1;
         Minecraft.player.jumpMovementFactor = 0.0F;
         if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
            Minecraft.player.motionY = 0.425F;
         } else {
            MoveMeHelp.setCuttingSpeed(Minecraft.player.ticksExisted % 2 == 0 ? speed / 1.06 : 0.0);
         }

         flySpeed = speed;
      }
   }

   @Override
   public void onMovement() {
      if (this.Mode.currentMode.equalsIgnoreCase("Motion")) {
         Minecraft.player.multiplyMotionXZ(0.0F);
         MoveMeHelp.setCuttingSpeed((double)(9.953F * (this.SpeedXZ.getFloat() / 10.0F)) / 1.06);
         Minecraft.player.motionY = 0.0;
         Entity.motiony = (double)(
            (Float.MIN_VALUE) * (float)(Minecraft.player.ticksExisted % 2 == 0 ? 1 : -1)
               + this.SpeedY.getFloat() * (float)(Minecraft.player.isSneaking() ? -1 : (Minecraft.player.isJumping() ? 1 : 0))
         );
      }

      if (this.Mode.currentMode.equalsIgnoreCase("Rage")) {
         float crate = (float)MathUtils.clamp((int)this.StepCrate.getFloat(), 1, 10);
         float speed = 9.953F / (this.NoVanillaKick.getBool() ? 1.024F : 1.0F) * (this.SpeedCrate.getFloat() / 10.0F) * crate;
         MoveMeHelp.setSpeed((double)speed);
         MoveMeHelp.setCuttingSpeed((double)speed / 1.06);
         Minecraft.player.motionY = 0.0;
         if (this.NoVanillaKick.getBool()) {
            Entity.motiony = (double)(Minecraft.player.ticksExisted % 2 != 0 ? 0.05F : -0.05F);
         } else {
            Entity.motiony = Float.MIN_VALUE;
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixChunk") && Minecraft.player.fallDistance != 0.0F) {
         float curSpeed = this.SpeedXZ.getFloat() / 10.0F;
         double speed = this.SmoothSpeed.getBool() ? MoveMeHelp.getCuttingSpeed() / 1.3 : 0.0;
         if (Minecraft.player.fallDistance != 0.0F || !this.AutoUp.getBool()) {
            speed = (double)((Minecraft.player.isJumping() && this.UpWard.getBool() ? 8.2675F : 9.4675F) * curSpeed);
         }

         if (!MoveMeHelp.moveKeysPressed() && this.SmoothSpeed.getBool()) {
            MoveMeHelp.setSpeed(speed, 0.6F);
         } else {
            MoveMeHelp.setSpeed(speed);
            MoveMeHelp.setCuttingSpeed(speed / 1.06);
         }

         flySpeed = speed;
      }

      if (this.Mode.currentMode.equalsIgnoreCase("Vanilla")) {
         float curMotionY = this.SpeedY.getFloat();
         double speedx = (double)(this.SpeedXZ.getFloat() / 10.0F);
         double motionY = Minecraft.player.ticksExisted % 2 == 0 ? 1.0E-10 : -1.0E-10;
         if (!Minecraft.player.isJumping() && !Minecraft.player.isSneaking()) {
            this.tickerFinalling = 0;
         } else if (this.ticker.hasReached(50.0)) {
            this.tickerFinalling++;
            this.ticker.reset();
         }

         if (this.tickerFinalling < 15) {
            motionY = Minecraft.player.isJumping() ? (double)curMotionY : (Minecraft.player.isSneaking() ? (double)(-curMotionY) : motionY);
            this.ticker2.reset();
         } else if (this.ticker2.hasReached(300.0)) {
            this.tickerFinalling = 0;
            this.ticker2.reset();
         }

         Entity.motiony = motionY;
         Minecraft.player.motionY = motionY;
         if (!TargetStrafe.goStrafe() || MoveMeHelp.moveKeysPressed()) {
            MoveMeHelp.setSpeed(speedx * 9.7675);
            MoveMeHelp.setCuttingSpeed(speedx / 1.06 * 9.7675);
         }

         flySpeed = speedx * 9.7675;
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         this.timeFlying.reset();
         if (this.Mode.currentMode.equalsIgnoreCase("MatrixLatest")) {
            this.ticksBoost = 0;
         }

         if (this.Mode.currentMode.equalsIgnoreCase("MatrixOld")) {
            this.enableGround = Minecraft.player.onGround;
            this.ticker.reset();
         }

         if (this.Mode.currentMode.equalsIgnoreCase("AAC")) {
            this.enableGround = Minecraft.player.onGround;
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Vanilla")) {
            Minecraft.player.jump();
            if (!MoveMeHelp.isBlockAboveHead()) {
               Minecraft.player.setPosY(Minecraft.player.posY + 0.4);
            }
         }

         if (this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")) {
            this.tickerFinalling = 0;
         }
      } else {
         if (this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")) {
            MoveMeHelp.setSpeed(
               this.SmoothSpeed.getBool() ? MoveMeHelp.getSpeed() / 5.0 : MoveMeHelp.getSpeed() / 30.0, this.SmoothSpeed.getBool() ? 0.3F : 0.0F
            );
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Jartex")) {
            Minecraft.player.motionY = -0.023214;
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(0.0);
            Minecraft.player.jumpMovementFactor = 0.0F;
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Vanilla")) {
            MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() / 4.0);
            if (Minecraft.player.motionY > 0.0) {
               Minecraft.player.motionY /= 3.0;
            }
         }
      }

      super.onToggled(actived);
   }

   @EventTarget
   public void onPlayerUpdate(EventPlayerMotionUpdate e) {
      if (this.actived) {
         if (this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")) {
            if (Minecraft.player.onGround && Minecraft.player.fallDistance == 0.0F && this.AutoUp.getBool()) {
               MoveMeHelp.setSpeed(0.0);
               Minecraft.player.jumpMovementFactor = 0.0F;
               if (!Minecraft.player.isJumping()) {
                  Minecraft.player.motionY = (double)Minecraft.player.getJumpUpwardsMotion();
               }

               return;
            }

            if (Minecraft.player.fallDistance != 0.0F) {
               this.tickerFinalling++;
               float curSpeed = this.SpeedXZ.getFloat() / 10.0F;
               if (Minecraft.player.isJumping() && this.UpWard.getBool() && MoveMeHelp.moveKeysPressed() && MoveMeHelp.getCuttingSpeed() > 0.1) {
                  Minecraft.player.motionY = Minecraft.player.fallDistance != 0.0F ? 0.42 : (this.NoVanillaKick.getBool() ? -0.02 : 0.0);
                  if (this.tickerFinalling % 2 == 0) {
                     Entity.motiony = this.NoVanillaKick.getBool() ? -0.05 : -1.0E-6;
                  }
               } else if (!Minecraft.player.isSneaking() || !this.DownWard.getBool()) {
                  if (this.NoVanillaKick.getBool() && this.timeFlying.hasReached(50.0)) {
                     float yport = 0.035F;
                     Minecraft.player.motionY = (double)(yport * (float)(this.tickerFinalling % 2 != 1 ? -1 : 1));
                  } else {
                     Minecraft.player.motionY = 0.0;
                     Entity.motiony = -1.0E-6;
                  }
               }
            } else {
               this.tickerFinalling = 0;
               this.timeFlying.reset();
            }
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Jartex")) {
            e.ground = false;
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Vanilla")) {
            e.ground = false;
            float yport = 0.1F;
            double y = e.getPosY();
            if (Speed.posBlock(Minecraft.player.posX, e.getPosY() - 0.11, Minecraft.player.posZ) && !MoveMeHelp.isBlockAboveHead()) {
               y += 0.1;
            } else if (MoveMeHelp.isBlockAboveHead()) {
               y -= 0.3F;
               yport = (float)((double)yport + 0.1);
            }

            y += (double)(Minecraft.player.ticksExisted % 2 == 0 ? yport : -yport);
            if (Minecraft.player.ticksExisted % 2 != 0) {
               Minecraft.player.fallDistance += yport * 2.0F;
            }

            if (Minecraft.player.fallDistance > 19.0F) {
               Minecraft.player.fallDistance = 19.0F;
            }

            e.setPosY(y);
         }
      }
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (Minecraft.player != null && mc.world != null && this.actived && event.getPacket() instanceof CPacketConfirmTeleport && this.DisableOnFlag.getBool()) {
         if (this.Mode.currentMode.equalsIgnoreCase("MatrixChunk")) {
            this.toggle(false);
            MoveMeHelp.setSpeed(0.0);
            Minecraft.player.jumpMovementFactor = 0.0F;
            if (Minecraft.player.motionY == 0.0) {
               Minecraft.player.motionY = -0.0078;
            }
         }

         if (this.Mode.currentMode.equalsIgnoreCase("MatrixOld")) {
            this.toggle(false);
            if (Minecraft.player.motionY >= 0.0) {
               Minecraft.player.motionY = -0.023214;
            }
         }
      }
   }

   @EventTarget
   public void onRenderHands(EventTransformSideFirstPerson event) {
      if (this.Helicopter.getBool() && this.Helicopter.isVisible()) {
         float ventilator = (float)(System.currentTimeMillis() % 80L) / 80.0F * 360.0F;
         GlStateManager.rotate(ventilator * (float)(event.getEnumHandSide() == EnumHandSide.RIGHT ? 1 : -1), 0.0F, 1.0F, 0.0F);
      }
   }
}
