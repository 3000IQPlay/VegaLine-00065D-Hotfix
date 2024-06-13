package ru.govno.client.module.modules;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMove2;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Wrapper;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class JesusSpeed extends Module {
   public static JesusSpeed get;
   public ModeSettings JesusMode;
   public BoolSettings SolidSpeed;
   public FloatSettings SpeedMatrix;
   public FloatSettings SpeedSolid;
   public FloatSettings SpeedZoom;
   public FloatSettings SpeedMatrix6_9_2;
   TimerHelper timer = new TimerHelper();
   TimerHelper timerMZ = new TimerHelper();
   public static double distOfFall = 0.0;
   public static float invertY = 0.0F;
   public static boolean canUp = false;
   public static boolean canSwim = false;
   public static int flagsCount = 0;
   public static boolean canSaver = false;
   public static boolean isSwimming = false;
   public static boolean jesusTick;
   public static boolean swap;
   public static int ticks;
   boolean perWater;
   public static int ticksLinked;
   TimerHelper timerSwim = new TimerHelper();
   public static boolean isJesused = false;

   public JesusSpeed() {
      super("JesusSpeed", 0, Module.Category.MOVEMENT);
      get = this;
      this.settings
         .add(
            this.JesusMode = new ModeSettings(
               "Jesus Mode",
               "MatrixBoost",
               this,
               new String[]{
                  "MatrixBoost",
                  "MatrixZoom",
                  "MatrixZoom2",
                  "MatrixSolid",
                  "MatrixSolid2",
                  "MatrixSolid3",
                  "MatrixPixel",
                  "Matrix6.9.2",
                  "Solid",
                  "Ncp",
                  "NcpStatic",
                  "NcpNew",
                  "AacJetPack",
                  "AAC",
                  "MatrixJump",
                  "Vulcan",
                  "Matrix7.0.2"
               }
            )
         );
      this.settings
         .add(this.SpeedMatrix = new FloatSettings("SpeedMatrix", 0.3F, 2.0F, 0.0F, this, () -> this.JesusMode.currentMode.equalsIgnoreCase("MatrixBoost")));
      this.settings.add(this.SpeedSolid = new FloatSettings("SpeedSolid", 1.0F, 1.3F, 0.0F, this, () -> this.JesusMode.currentMode.equalsIgnoreCase("Solid")));
      this.settings
         .add(
            this.SpeedZoom = new FloatSettings(
               "SpeedZoom",
               3.0F,
               10.0F,
               0.5F,
               this,
               () -> this.JesusMode.currentMode.equalsIgnoreCase("MatrixZoom")
                     || this.JesusMode.currentMode.equalsIgnoreCase("MatrixZoom2")
                     || this.JesusMode.currentMode.equalsIgnoreCase("MatrixPixel")
            )
         );
      this.settings.add(this.SolidSpeed = new BoolSettings("SolidSpeed", false, this, () -> this.JesusMode.currentMode.equalsIgnoreCase("Matrix6.9.2")));
      this.settings
         .add(
            this.SpeedMatrix6_9_2 = new FloatSettings(
               "SpeedMatrix6.9.2", 0.6F, 1.0F, 0.01F, this, () -> this.JesusMode.currentMode.equalsIgnoreCase("Matrix6.9.2") && !this.SolidSpeed.getBool()
            )
         );
   }

   public static float getM7Speed() {
      int depth = EnchantmentHelper.getDepthStriderModifier(Minecraft.player);
      int spl = Minecraft.player.getActivePotionEffect(MobEffects.SPEED) != null
         ? Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1
         : 0;
      float speed1 = 1.16F;
      if (depth > 0) {
         speed1 = 1.41F;
         if (spl != 0) {
            speed1 = 1.6099F;
         }
      }

      float speed2 = 0.16F;
      if (depth > 0) {
         speed2 = 0.41F;
         if (spl != 0) {
            speed2 = 0.6099F;
         }
      }

      if (EntityLivingBase.isMatrixDamaged) {
         speed1 = depth > 0 ? (spl > 0 ? 3.0F : 2.8F) : 2.5F;
         speed2 = depth > 0 ? (float)MathUtils.clamp(MoveMeHelp.getCuttingSpeed() * 1.3F, 1.8F, spl > 0 ? 2.9F : 2.8F) : 1.5F;
         if (TargetStrafe.goStrafe()) {
            speed1 = 1.25F;
            speed2 = 1.09F;
         }
      }

      return (ticksLinked % 2 == 1 ? speed2 : speed1) / (float)(Minecraft.player.isInWeb ? 5 : 1);
   }

   @EventTarget
   public void onEvent1(EventMove2 move) {
      if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("MatrixPixel")) {
         isSwimming = false;
         BlockPos pos = new BlockPos(move.from());
         Block block = mc.world.getBlockState(pos).getBlock();
         if (block instanceof BlockLiquid) {
            move.motion().yCoord = 0.19;
            move.motion().xCoord = 0.0;
            move.motion().zCoord = 0.0;
         } else if (mc.world.getBlockState(new BlockPos(move.to())).getBlock() instanceof BlockLiquid) {
            isSwimming = true;
            Minecraft.player.setPosY((double)((int)Minecraft.player.posY));
            Minecraft.player.setSprinting(true);
            MoveMeHelp.setSpeed(
               (double)(
                  ticks % 3 == 0 && Minecraft.player.posY == (double)((int)Minecraft.player.posY) && (double)Minecraft.player.fallDistance >= 0.08
                     ? this.currentFloatValue("SpeedZoom") - 0.01F
                     : 0.14F
               )
            );
            if (Minecraft.player.posY % 1.0 == 0.0 && (!Minecraft.player.isCollidedHorizontally || !Minecraft.player.isJumping())) {
               move.motion().yCoord = 0.0;
            }

            swap = true;
            move.motion().xCoord = Minecraft.player.motionX;
            move.motion().zCoord = Minecraft.player.motionZ;
            Minecraft.player.motionZ = 0.0;
            Minecraft.player.motionX = 0.0;
            Minecraft.player.motionY = 0.0;
            if ((
                  Speed.posBlock(Minecraft.player.posX - 0.301, Minecraft.player.posY - 0.1, Minecraft.player.posZ)
                     || Speed.posBlock(Minecraft.player.posX + 0.301, Minecraft.player.posY - 0.1, Minecraft.player.posZ)
                     || Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ - 0.301)
                     || Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ + 0.301)
               )
               && Minecraft.player.isJumping()) {
               move.collisionOffset().yCoord = 0.0;
               move.motion().yCoord = 0.04;
               Minecraft.player.motionY = 0.14;
               MoveMeHelp.setSpeed(0.24);
            } else {
               move.collisionOffset().yCoord = -0.7;
            }
         }
      }
   }

   private double getSlSpeedMatrix6(boolean ticked, float speedPCT, boolean solidSpeed) {
      boolean isLocalStopped = Minecraft.player.isHandActive() && Minecraft.player.getItemInUseMaxCount() > 3 || Minecraft.player.isSneaking();
      double spd = solidSpeed ? (double)this.getSolidSpeed() : 9.953 * (double)speedPCT;
      return ticked ? spd : (isLocalStopped ? 0.07 : 0.1);
   }

   float getSolidSpeed() {
      Enchantment depth = Enchantments.DEPTH_STRIDER;
      int depthLvl = EnchantmentHelper.getEnchantmentLevel(depth, Minecraft.player.inventory.armorItemInSlot(0));
      boolean isSpeedPot = Minecraft.player.isPotionActive(MobEffects.SPEED);
      int speedLvl = 0;
      if (isSpeedPot) {
         speedLvl = Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
      }

      if (depthLvl > 0) {
         if (isSpeedPot) {
            return speedLvl == 0 ? 1.51F : 1.6099F;
         } else {
            return 1.41F;
         }
      } else {
         return 1.16F;
      }
   }

   @Override
   public void onMovement() {
      Enchantment depth = Enchantments.DEPTH_STRIDER;
      int depthLvl = EnchantmentHelper.getEnchantmentLevel(depth, Minecraft.player.inventory.armorItemInSlot(0));
      boolean isSpeedPot = Minecraft.player.isPotionActive(MobEffects.SPEED) && Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() > 0;
      float speed = 1.16F;
      if (isSpeedPot && depthLvl > 0) {
         speed = (float)((double)speed + 0.443);
      }

      boolean stopped = TargetStrafe.get.actived && TargetStrafe.target != null && HitAura.TARGET_ROTS != null;
      if (this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid3") && Minecraft.player.ticksExisted > 4) {
         if (canUp && !isSwimming) {
            Entity.motiony = 0.189;
         }

         if ((Minecraft.player.motionY == 0.032F || Minecraft.player.motionY == -0.032F)
            && !stopped
            && (!Minecraft.player.isCollidedHorizontally || !Minecraft.player.isJumping())) {
            MoveMeHelp.setCuttingSpeed((double)this.getSolidSpeed() / 1.06);
            MoveMeHelp.setSpeed(0.23);
            isSwimming = false;
         }
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid2") && isSwimming && !stopped) {
         MoveMeHelp.setCuttingSpeed(
            (
                  (double)Minecraft.player.fallDistance < 2.9
                     ? (
                        Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping()
                           ? 0.4
                           : ((double)Minecraft.player.fallDistance >= 0.08 ? (double)speed : 0.122F)
                     )
                     : 0.1F
               )
               / 1.06
         );
      }
   }

   public boolean getBlockWithExpand(float expand, double x, double y, double z, Block block) {
      return mc.world.getBlockState(new BlockPos(x, y + 0.03, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z - (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z - (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x, y, z - (double)expand)).getBlock() == block;
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      if (!FreeCam.get.actived) {
         isJesused = isSwimming;
         if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("MatrixPixel")) {
            if (swap) {
               if (MoveMeHelp.moveKeysPressed() && (double)Minecraft.player.fallDistance >= 0.08) {
                  e.setPosY(e.getPosY() + (ticks % 3 == 0 ? -0.0011 : (ticks % 3 == 1 ? 0.0011 : 0.0101)));
                  if (ticks % 3 == 0) {
                     Minecraft.player.fallDistance = (float)((double)Minecraft.player.fallDistance + 0.0124);
                  }
               } else {
                  e.setPosY(e.getPosY() + (double)(ticks % 2 == 0 ? -0.032F : 0.032F));
                  if (ticks % 2 == 0) {
                     ticks = 0;
                     Minecraft.player.fallDistance += 0.064F;
                  }
               }

               ticks++;
               if (e.getPosY() % 1.0 == 0.0 && MoveMeHelp.isMoving()) {
                  e.setPosY(e.getPosY() - 0.07);
               }

               e.ground = false;
               Minecraft.player.onGround = false;
            }

            swap = false;
         }

         double x = Minecraft.player.posX;
         double y = Minecraft.player.posY;
         double z = Minecraft.player.posZ;
         if (this.actived && this.currentMode("Jesus Mode").equalsIgnoreCase("Vulcan")) {
            float w = Minecraft.player.width / 2.0F - 1.0E-5F;
            if (this.getBlockWithExpand(w, x, y + 2.0, z, Blocks.WATER)) {
               Minecraft.player.fallDistance = 0.0F;
               Minecraft.player.motionY = 0.00782;
            }

            if (this.getBlockWithExpand(w, x, y - 0.1, z, Blocks.WATER)) {
               MoveMeHelp.setCuttingSpeed(EntityLivingBase.isMatrixDamaged ? 1.2 : 0.3);
            }
         }

         if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("Matrix6.9.2")) {
            x = e.getX();
            y = Minecraft.player.posY;
            z = e.getZ();
            canUp = false;
            float wx = Minecraft.player.width / 2.0F - 1.0E-5F;
            if (mc.world.getBlockState(new BlockPos(x, y + 0.24, z)).getBlock() == Blocks.WATER
               || mc.world.getBlockState(new BlockPos(x, y + 0.24, z)).getBlock() == Blocks.LAVA) {
               canUp = true;
               isSwimming = false;
            } else if (Minecraft.player.onGround) {
               isSwimming = false;
               ticksLinked = -1;
            } else {
               isSwimming = Minecraft.player.posY == (double)((int)Minecraft.player.posY) + 0.99
                  && (
                     mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER
                        || mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA
                  );
            }

            if (canUp) {
               MoveMeHelp.setSpeed(0.0);
               Minecraft.player.jumpMovementFactor = 0.0F;
               Minecraft.player.motionY = 0.19;
            } else if (isSwimming) {
               Minecraft.player.motionY = 0.0;
               if (Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping() && MoveMeHelp.moveKeysPressed()) {
                  Minecraft.player.motionY = 0.26;
                  MoveMeHelp.setSpeed(0.0);
                  Minecraft.player.jumpMovementFactor = !Minecraft.player.isHandActive() && !Minecraft.player.isSneaking() ? 0.4F : 0.03F;
                  isSwimming = false;
                  return;
               }

               ticksLinked++;
               e.ground = false;
               Minecraft.player.onGround = false;
               int tick = ticksLinked;
               float ext = tick % 3 < 2 ? 0.032F * (float)(tick % 3 == 1 ? 1 : -1) : (tick <= 0 ? 0.0F : 0.032F);
               e.setPosY(Minecraft.player.posY + (double)ext);
               if ((double)Minecraft.player.fallDistance < 0.2) {
                  Minecraft.player.fallDistance = 0.2F;
               }

               MoveMeHelp.setSpeed(0.0);
               Minecraft.player.jumpMovementFactor = 0.0F;
               boolean isOlded = this.SolidSpeed.getBool();
               if ((MoveMeHelp.getCuttingSpeed() < 0.1 && !MoveMeHelp.moveKeysPressed() || MoveMeHelp.getCuttingSpeed() == 0.0)
                  && (
                     MathUtils.getDifferenceOf(Minecraft.player.rotationYawHead, Minecraft.player.lastReportedYaw) > 0.0
                        || MathUtils.getDifferenceOf(Minecraft.player.rotationPitchHead, EntityPlayerSP.lastReportedPitch) > 0.0
                  )) {
                  Entity.motionx = -Math.random() * 0.05 + 0.05 * Math.random();
                  Entity.motionz = -Math.random() * 0.05 + 0.05 * Math.random();
               }

               if (MoveMeHelp.moveKeysPressed()) {
                  MoveMeHelp.setCuttingSpeed(this.getSlSpeedMatrix6(tick % 3 == 2, this.SpeedMatrix6_9_2.getFloat(), isOlded) / 1.06);
               }

               Minecraft.player.posX = Minecraft.player.posX + (Minecraft.player.prevPosX - Minecraft.player.posX) / 2.75;
               Minecraft.player.posZ = Minecraft.player.posZ + (Minecraft.player.prevPosZ - Minecraft.player.posZ) / 2.75;
            } else if (!this.getBlockWithExpand(wx, x, y + 0.4F, z, Blocks.WATER)
               && !this.getBlockWithExpand(wx, x, y + 0.4F, z, Blocks.LAVA)
               && (
                  this.getBlockWithExpand(wx, x, y - 0.5, z, Blocks.WATER) && this.getBlockWithExpand(wx, x, y, z, Blocks.WATER)
                     || this.getBlockWithExpand(wx, x, y - 0.5, z, Blocks.LAVA) && this.getBlockWithExpand(wx, x, y, z, Blocks.LAVA)
               )) {
               e.ground = false;
               Minecraft.player.onGround = false;
               Minecraft.player.setPosY((double)((int)Minecraft.player.posY) + 0.99);
               Minecraft.player.motionY = 0.0;
            }
         }

         if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("NcpNew")) {
            if (mc.world
                     .getBlockState(
                        new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (Minecraft.player.isJumping() ? 0.01 : 0.45), Minecraft.player.posZ)
                     )
                     .getBlock()
                  == Blocks.WATER
               && !Minecraft.player.isInWater()) {
               invertY = (float)((double)invertY + 1.0E-10);
               e.ground = false;
               if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.001, Minecraft.player.posZ)).getBlock() == Blocks.WATER
                  && Minecraft.player.isJumping()) {
                  Minecraft.player.motionY = 0.42F;
               }

               Minecraft.player.onGround = false;
               Minecraft.player.motionX = 0.0;
               Minecraft.player.motionZ = 0.0;
               if (!Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
                  Minecraft.player.jumpMovementFactor = 0.2865F;
               }

               if (Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
                  Minecraft.player.jumpMovementFactor = 0.4005F;
               }
            } else {
               invertY = 0.0F;
            }
         }

         if (this.actived
            && this.JesusMode.currentMode.equalsIgnoreCase("AAC")
            && mc.world
                  .getBlockState(
                     new BlockPos(Minecraft.player.posX, Minecraft.player.posY - (Minecraft.player.isJumping() ? 0.01 : 0.45), Minecraft.player.posZ)
                  )
                  .getBlock()
               == Blocks.WATER
            && !Minecraft.player.isInWater()) {
            e.ground = false;
            if (Minecraft.player.onGround && Minecraft.player.ticksExisted % 2 == 0) {
               Minecraft.player.isAirBorne = e.ground;
            }
         }

         if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("MatrixZoom2")) {
            boolean out = false;
            if (!Minecraft.player.isCollidedHorizontally) {
               this.timer.reset();
            } else if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ)).getBlock()
                  == Blocks.WATER
               && this.timer.hasReached(Minecraft.player.isJumping() ? 0.0 : 1000000.0)
               && mc.gameSettings.keyBindJump.isKeyDown()) {
               out = true;
            }

            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.1, Minecraft.player.posZ)).getBlock() == Blocks.WATER) {
               Minecraft.player.motionY = mc.world
                        .getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.0, Minecraft.player.posZ))
                        .getBlock()
                     != Blocks.WATER
                  ? (Minecraft.player.isCollidedHorizontally ? 0.1 : 0.2)
                  : ((double)Minecraft.player.fallDistance > 0.2 ? 0.14 : 0.2);
            }

            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1202, Minecraft.player.posZ)).getBlock() == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY - 0.1202, Minecraft.player.posZ + 0.3)).getBlock()
                  == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY - 0.1202, Minecraft.player.posZ - 0.3)).getBlock()
                  == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY - 0.1202, Minecraft.player.posZ - 0.3)).getBlock()
                  == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY - 0.1202, Minecraft.player.posZ + 0.3)).getBlock()
                  == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY - 0.1202, Minecraft.player.posZ)).getBlock()
                  == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY - 0.1202, Minecraft.player.posZ)).getBlock()
                  == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1202, Minecraft.player.posZ - 0.3)).getBlock()
                  == Blocks.WATER
               && (
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY + 0.01, Minecraft.player.posZ + 0.3)).getBlock()
                        != Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY + 0.01, Minecraft.player.posZ - 0.3)).getBlock()
                        != Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY + 0.01, Minecraft.player.posZ - 0.3)).getBlock()
                        != Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY + 0.01, Minecraft.player.posZ + 0.3)).getBlock()
                        != Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY + 0.01, Minecraft.player.posZ)).getBlock()
                        != Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY + 0.01, Minecraft.player.posZ)).getBlock()
                        != Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.01, Minecraft.player.posZ + 0.3)).getBlock()
                        != Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.01, Minecraft.player.posZ - 0.3)).getBlock()
                        != Blocks.WATER
               )
               && (
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.01, Minecraft.player.posZ)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY - 0.01, Minecraft.player.posZ + 0.3)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY - 0.01, Minecraft.player.posZ - 0.3)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY - 0.01, Minecraft.player.posZ)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY - 0.01, Minecraft.player.posZ)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.01, Minecraft.player.posZ + 0.3)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.01, Minecraft.player.posZ - 0.3)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX + 0.3, Minecraft.player.posY - 0.01, Minecraft.player.posZ - 0.3)).getBlock()
                        != Blocks.WATERLILY
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX - 0.3, Minecraft.player.posY - 0.01, Minecraft.player.posZ + 0.3)).getBlock()
                        != Blocks.WATERLILY
               )) {
               Minecraft.player.fallDistance = Minecraft.player.fallDistance + (Minecraft.player.ticksExisted % 2 == 0 ? 0.02F : 0.01F);
               Minecraft.player.setVelocity(0.0, 0.0, 0.0);
               if (!out) {
                  if (Minecraft.player.ticksExisted % 2 != 0
                     && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() == Blocks.WATER) {
                     Minecraft.player.setPosition(Minecraft.player.posX, (double)((int)Minecraft.player.posY + 1), Minecraft.player.posZ);
                  }

                  if (Minecraft.player.ticksExisted % 2 != 0
                     && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.1, Minecraft.player.posZ)).getBlock()
                        != Blocks.WATER) {
                     Minecraft.player.setPosition(Minecraft.player.posX, (double)((int)Minecraft.player.posY), Minecraft.player.posZ);
                  }

                  Minecraft.player.setPosition(Minecraft.player.posX, Minecraft.player.posY - 1.0E-8, Minecraft.player.posZ);
                  Minecraft.player.jumpMovementFactor = 0.0F;
                  Minecraft.player.onGround = false;
                  e.y = e.y + (Minecraft.player.ticksExisted % 2 == 0 ? 0.03 : -0.03);
                  isSwimming = true;
                  if (Minecraft.player.ticksExisted % 2 == 0) {
                     MoveMeHelp.setSpeed((double)(this.SpeedZoom.getFloat() / 1.345F));
                  } else {
                     MoveMeHelp.setSpeed(0.2);
                  }
               } else {
                  Minecraft.player.motionY = Minecraft.player.isJumping() ? 1.0 : 0.0;
                  Minecraft.player.jumpMovementFactor = Minecraft.player.isJumping() ? 0.5F : 0.0F;
                  Minecraft.player.setPosition(Minecraft.player.posX, Minecraft.player.posY + 0.0, Minecraft.player.posZ);
                  this.timer.reset();
               }
            } else {
               isSwimming = false;
            }
         }

         if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid")) {
            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ)).getBlock() == Blocks.WATER
               || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ)).getBlock() == Blocks.LAVA) {
               Minecraft.player.jumpMovementFactor = 0.0F;
               if (canSaver) {
                  Minecraft.player.setVelocity(Minecraft.player.motionX, 0.42, Minecraft.player.motionZ);
               }
            } else if (canSaver) {
               canSaver = false;
            }

            if ((
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.008, Minecraft.player.posZ)).getBlock() == Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.008, Minecraft.player.posZ)).getBlock()
                        == Blocks.LAVA
               )
               && !Minecraft.player.onGround) {
               boolean isUp = mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.03, Minecraft.player.posZ)).getBlock()
                     == Blocks.WATER
                  || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.03, Minecraft.player.posZ)).getBlock() == Blocks.LAVA;
               Minecraft.player.jumpMovementFactor = 0.0F;
               float yport = 0.032F;
               Minecraft.player
                  .setVelocity(
                     Minecraft.player.motionX,
                     Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping() ? 0.2105F : (double)(isUp ? yport : -yport),
                     Minecraft.player.motionZ
                  );
               if (Minecraft.player.posY > (double)((int)Minecraft.player.posY) + 0.935 && Minecraft.player.posY <= (double)((int)Minecraft.player.posY + 1)) {
                  Minecraft.player.posY = (double)((int)Minecraft.player.posY + 1) + 1.0E-45;
                  if (!Minecraft.player.isInWater() || !Minecraft.player.isInLava()) {
                     Enchantment depth = Enchantments.DEPTH_STRIDER;
                     int depthLvl = EnchantmentHelper.getEnchantmentLevel(depth, Minecraft.player.inventory.armorItemInSlot(0));
                     boolean isSpeedPot = Minecraft.player.isPotionActive(MobEffects.SPEED)
                        && Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() > 0;
                     float speed = 1.16F;
                     if (isSpeedPot && depthLvl > 0) {
                        speed = (float)((double)speed + 0.443);
                     }

                     speed = 0.99F;
                     MoveMeHelp.setSpeed(
                        Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping() ? 0.4 : (isUp ? (double)speed - 1.0E-4 : (double)speed),
                        Minecraft.player.isCollidedHorizontally ? 0.1F : 0.8F
                     );
                     isSwimming = true;
                  }
               }
            } else {
               isSwimming = false;
            }

            if (Minecraft.player.isInWater()
               || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.15, Minecraft.player.posZ)).getBlock() == Blocks.WATER
               || Minecraft.player.isInLava()
               || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.15, Minecraft.player.posZ)).getBlock() == Blocks.LAVA) {
               Minecraft.player.motionY = 0.16;
               if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 2.0, Minecraft.player.posZ)).getBlock() == Blocks.AIR) {
                  Minecraft.player.motionY = 0.12;
               }

               if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.0, Minecraft.player.posZ)).getBlock() == Blocks.AIR) {
                  Minecraft.player.motionY = 0.18;
               }
            }
         }

         if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid2")) {
            double falld = 2.9;
            isSwimming = false;
            if (this.getBlockWithExpand(Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ, Blocks.WATER)
               || this.getBlockWithExpand(Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ, Blocks.LAVA)
               )
             {
               Minecraft.player.jumpMovementFactor = 0.0F;
               if (canSaver) {
                  Minecraft.player.jumpMovementFactor = 0.0F;
                  Minecraft.player.setVelocity(0.0, 0.42, 0.0);
               }
            } else if (canSaver) {
               canSaver = false;
            }

            boolean waterAdobe = (
                  this.getBlockWithExpand(Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Blocks.WATER)
                     || this.getBlockWithExpand(Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Blocks.LAVA)
               )
               && !this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.1, Minecraft.player.posZ, Blocks.WATER
               )
               && !this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.1, Minecraft.player.posZ, Blocks.LAVA
               );
            if (waterAdobe && !Minecraft.player.onGround) {
               boolean isUp = this.getBlockWithExpand(
                     Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.03, Minecraft.player.posZ, Blocks.WATER
                  )
                  || this.getBlockWithExpand(
                     Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.03, Minecraft.player.posZ, Blocks.LAVA
                  );
               Minecraft.player.jumpMovementFactor = 0.0F;
               float yport = MoveMeHelp.isMoving() ? 0.005F : 0.032F;
               if (!MoveMeHelp.isMoving() || Minecraft.player.motionY == -0.032F) {
                  this.timerSwim.reset();
               }

               Minecraft.player
                  .setVelocity(
                     Minecraft.player.motionX,
                     (double)Minecraft.player.fallDistance < falld
                        ? (Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping() ? 0.2105F : (double)(isUp ? yport : -yport))
                        : -0.5,
                     Minecraft.player.motionZ
                  );
               if (Minecraft.player.posY > (double)((int)Minecraft.player.posY) + 0.89 && Minecraft.player.posY <= (double)((int)Minecraft.player.posY + 1)
                  || waterAdobe && !Minecraft.player.onGround) {
                  if (!Minecraft.player.isInWater() && !Minecraft.player.isInLava()) {
                     isSwimming = true;
                  }

                  boolean stopped = TargetStrafe.goStrafe();
                  Minecraft.player.multiplyMotionXZ(0.6F);
                  if (isSwimming && !stopped) {
                     MoveMeHelp.setMotionSpeed(
                        true,
                        true,
                        (double)Minecraft.player.fallDistance < 2.9
                           ? (
                              Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping()
                                 ? 0.4
                                 : ((double)Minecraft.player.fallDistance >= 0.08 ? (double)this.getSolidSpeed() : 0.122F)
                           )
                           : 0.1F
                     );
                  }
               }

               Minecraft.player.posY = (double)((int)Minecraft.player.posY + 1) + 1.0E-45;
            }

            if (Minecraft.player.isInWater()
               || this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.15, Minecraft.player.posZ, Blocks.WATER
               )
               || Minecraft.player.isInLava()
               || this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.15, Minecraft.player.posZ, Blocks.LAVA
               )) {
               Minecraft.player.motionY = 0.16;
               if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 2.0, Minecraft.player.posZ)).getBlock() == Blocks.AIR) {
                  Minecraft.player.motionY = 0.12;
               }

               if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.0, Minecraft.player.posZ)).getBlock() == Blocks.AIR) {
                  Minecraft.player.motionY = 0.18;
               }
            }
         }

         if (this.actived && this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid3") && Minecraft.player.ticksExisted > 4) {
            boolean isLiquded = !this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.08, Minecraft.player.posZ, Blocks.WATER
               )
               && !this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.08, Minecraft.player.posZ, Blocks.LAVA
               );
            boolean waterAdobex = (
                  this.getBlockWithExpand(Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Blocks.WATER)
                     || this.getBlockWithExpand(Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Blocks.LAVA)
               )
               && isLiquded;
            boolean isUpx = this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.035, Minecraft.player.posZ, Blocks.WATER
               )
               || this.getBlockWithExpand(
                  Minecraft.player.width / 2.0F, Minecraft.player.posX, Minecraft.player.posY + 0.035, Minecraft.player.posZ, Blocks.LAVA
               );
            canUp = (
                  Minecraft.player.isInWater()
                     || this.getBlockWithExpand(
                        Minecraft.player.width / 2.0F - 0.05F, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Blocks.WATER
                     )
                     || Minecraft.player.isInLava()
                     || this.getBlockWithExpand(
                        Minecraft.player.width / 2.0F - 0.05F, Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Blocks.LAVA
                     )
               )
               && !isSwimming;
            if (canUp && !isSwimming && Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping() && Minecraft.player.motionY <= 0.19) {
               Minecraft.player.motionY = 0.19;
            }

            if (waterAdobex) {
               if ((double)Minecraft.player.fallDistance > distOfFall) {
                  distOfFall = (double)Minecraft.player.fallDistance;
               } else {
                  Minecraft.player.fallDistance = (float)distOfFall;
               }

               e.ground = false;
               Minecraft.player.onGround = false;
               float yportx = 0.032F;
               Minecraft.player.motionY = (double)(
                  Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping() ? 0.2105F : (isUpx ? yportx : -yportx)
               );
               Minecraft.player.posY = (double)((int)Minecraft.player.posY + 1) + 1.0E-15;
               MoveMeHelp.setSpeed(0.0);
               Minecraft.player.jumpMovementFactor = Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping()
                  ? (!Minecraft.player.isHandActive() && !Minecraft.player.isSneaking() ? 0.195F : 0.01F)
                  : 0.0F;
            } else {
               distOfFall = (double)Minecraft.player.fallDistance;
            }

            isSwimming = waterAdobex;
            if (isSwimming) {
               canUp = false;
            }
         } else {
            isSwimming = false;
            canUp = false;
            distOfFall = 0.0;
         }
      }
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (Minecraft.player != null && mc.world != null && !Minecraft.player.isDead && this.actived && event.getPacket() instanceof CPacketConfirmTeleport) {
         canUp = false;
         canSwim = false;
         flagsCount++;
         if (flagsCount > 1) {
            canSaver = true;
            flagsCount = 0;
         }
      }
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.JesusMode.currentMode);
   }

   @Override
   public void onUpdate() {
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;
      if (this.JesusMode.currentMode.equalsIgnoreCase("Matrix7.0.2")) {
         if (mc.world.getBlockState(new BlockPos(x, y + 0.3, z)).getBlock() != Blocks.WATER) {
            if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER) {
               if (Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping()) {
                  Minecraft.player.onGround = false;
                  ticksLinked = 0;
                  isJesused = false;
                  Minecraft.player.setPosY(y + 0.35);
                  Minecraft.player.motionY = -0.078;
                  Minecraft.player.jumpMovementFactor = 0.0F;
               } else {
                  isJesused = true;
                  if (isJesused) {
                     ticksLinked++;
                     Minecraft.player.stepHeight = 0.0F;
                  }
               }
            } else {
               if (Minecraft.player.onGround) {
                  this.perWater = false;
               }

               Minecraft.player.stepHeight = 0.6F;
               isJesused = false;
               ticksLinked = 0;
            }
         } else {
            Minecraft.player.motionY = !Minecraft.player.onGround
                  && mc.world.getBlockState(new BlockPos(x, y + 1.8, z)).getBlock() != Blocks.WATER
                  && !Speed.posBlock(x, y - Float.MIN_VALUE, z)
               ? 0.19
               : 0.8;
            Minecraft.player.stepHeight = 0.0F;
            this.perWater = !Speed.posBlock(x, y - 0.999, z);
            MoveMeHelp.setCuttingSpeed(0.0);
            MoveMeHelp.setSpeed(0.0);
         }

         if (isJesused) {
            Minecraft.player.onGround = false;
            Minecraft.player.setPosY((double)((int)y) + 0.8874999 + (ticksLinked % 2 == 0 ? 0.0625 : 0.0));
            Minecraft.player.fallDistance = Minecraft.player.fallDistance < 0.1F ? 0.1F : Minecraft.player.fallDistance;
            Minecraft.player.motionY = -0.02156;
            Minecraft.player.jumpMovementFactor = 0.0F;
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed((double)getM7Speed() / 1.06);
            if (ticksLinked > 2) {
               this.perWater = false;
            }
         } else if (mc.world.getBlockState(new BlockPos(x, y - 0.1, z)).getBlock() == Blocks.WATER && !Minecraft.player.onGround) {
            Minecraft.player.jumpMovementFactor = 0.0F;
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(Float.MIN_VALUE);
            Minecraft.player
               .forceUpdatePlayerServerPosition(
                  Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, Minecraft.player.rotationYaw, Minecraft.player.rotationPitch, true
               );
            Minecraft.player.connection.sendPacket(new CPacketPlayer(true));
         }
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("MatrixBoost") && mc.world.getBlockState(new BlockPos(x, y + 0.99, z)).getBlock() == Blocks.WATER) {
         Minecraft.player.motionY = 0.42;
         MoveMeHelp.setSpeed((double)this.SpeedMatrix.getFloat());
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("Solid")) {
         if (mc.world.getBlockState(new BlockPos(x, y + 1.0E-7F, z)).getBlock() == Blocks.WATER) {
            Minecraft.player.fallDistance = 0.0F;
            Minecraft.player.motionY = 0.06F;
            float Speed = this.SpeedSolid.getFloat();
            Minecraft.player.jumpMovementFactor = Speed;
         }

         if (mc.world.getBlockState(new BlockPos(x, y + 1.0E-7F, z)).getBlock() == Blocks.LAVA) {
            Minecraft.player.fallDistance = 0.0F;
            Minecraft.player.motionY = 0.06F;
            float Speed = this.SpeedSolid.getFloat();
            Minecraft.player.jumpMovementFactor = Speed;
         }
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("MatrixZoom")) {
         if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x + 0.3, y, z)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x - 0.3, y, z)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x, y, z + 0.3)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x, y, z - 0.3)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x + 0.3, y, z + 0.3)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x - 0.3, y, z - 0.3)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x - 0.3, y, z + 0.3)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x + 0.3, y, z - 0.3)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x + 0.3, y, z)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x - 0.3, y, z)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x, y, z + 0.3)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x, y, z - 0.3)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x + 0.3, y, z + 0.3)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x - 0.3, y, z - 0.3)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x - 0.3, y, z + 0.3)).getBlock() != Blocks.LAVA
            && mc.world.getBlockState(new BlockPos(x + 0.3, y, z - 0.3)).getBlock() != Blocks.LAVA) {
            if (Minecraft.player.speedInAir == this.SpeedZoom.getFloat() / 1.345F) {
               Minecraft.player.speedInAir = 0.02F;
            }
         } else {
            if (!Minecraft.player.isInWater() || !Minecraft.player.isInLava()) {
               Minecraft.player.motionX = 0.0;
               Minecraft.player.motionZ = 0.0;
            }

            Minecraft.player.motionY = 0.0391F;
            Minecraft.player.speedInAir = this.SpeedZoom.getFloat() / 1.345F;
            Minecraft.player.jumpMovementFactor = 0.0F;
            if (Minecraft.player.fallDistance >= 3.0F) {
               Minecraft.player.motionY = -0.42;
            }

            if (Minecraft.player.motionY == -0.42 && Minecraft.player.fallDistance == 0.0F) {
               Minecraft.player.motionY = 0.185;
            }

            if (Minecraft.player.isCollidedHorizontally) {
               if (Minecraft.player.isJumping) {
                  Minecraft.player.motionY = 0.419999024;
               }

               Minecraft.player.speedInAir = 0.02F;
               Minecraft.player.jumpMovementFactor = 0.26F;
            }
         }

         if (Minecraft.player.isInWater() || Minecraft.player.isInLava()) {
            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.5, Minecraft.player.posZ)).getBlock() != Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.5, Minecraft.player.posZ)).getBlock() != Blocks.LAVA) {
               Minecraft.player.motionY += 0.167;
            } else {
               Minecraft.player.motionY += 0.099;
            }
         }
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("Ncp")) {
         if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER) {
            Minecraft.player.motionY = 0.0391;
            Minecraft.player.onGround = false;
            if (!TargetStrafe.goStrafe()) {
               Minecraft.player.motionX = 0.0;
               Minecraft.player.motionZ = 0.0;
            }

            if (!Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
               Minecraft.player.jumpMovementFactor = Minecraft.player.isMoving() ? 0.2865F : 0.294F;
            }

            if (Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
               Minecraft.player.jumpMovementFactor = 0.41F;
            }

            if (Minecraft.player.isCollidedHorizontally
               && mc.gameSettings.keyBindForward.isKeyDown()
               && !Minecraft.player.isInWater()
               && !Minecraft.player.isInLava()
               && mc.gameSettings.keyBindJump.isKeyDown()) {
               Minecraft.player.jump();
            }
         }

         if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA) {
            Minecraft.player.motionY = 0.04;
            Minecraft.player.onGround = false;
            if (!TargetStrafe.goStrafe()) {
               Minecraft.player.motionX = 0.0;
               Minecraft.player.motionZ = 0.0;
            }

            if (!Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
               Minecraft.player.jumpMovementFactor = 0.2865F;
            }

            if (Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
               Minecraft.player.jumpMovementFactor = 0.4005F;
            }

            if (Minecraft.player.isCollidedHorizontally
               && mc.gameSettings.keyBindForward.isKeyDown()
               && !Minecraft.player.isInWater()
               && !Minecraft.player.isInLava()
               && mc.gameSettings.keyBindJump.isKeyDown()) {
               Minecraft.player.jump();
            }
         }

         if (Minecraft.player.isInWater() || Minecraft.player.isInLava()) {
            Minecraft.player.motionX = 0.0;
            Minecraft.player.motionZ = 0.0;
            if (!mc.gameSettings.keyBindJump.isKeyDown()) {
               Minecraft.player.motionY += 0.07;
            }
         }
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("NcpStatic")) {
         if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA) {
            Speed.iceGo = false;
         }

         if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x + 0.3, y, z)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x - 0.3, y, z)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x, y, z + 0.3)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x, y, z - 0.3)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x + 0.3, y, z + 0.3)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x - 0.3, y, z - 0.3)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x - 0.3, y, z + 0.3)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x + 0.3, y, z - 0.3)).getBlock() == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x + 0.3, y, z)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x - 0.3, y, z)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x, y, z + 0.3)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x, y, z - 0.3)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x + 0.3, y, z + 0.3)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x - 0.3, y, z - 0.3)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x - 0.3, y, z + 0.3)).getBlock() == Blocks.LAVA
            || mc.world.getBlockState(new BlockPos(x + 0.3, y, z - 0.3)).getBlock() == Blocks.LAVA) {
            if (Minecraft.player.isJumping || Minecraft.player.isCollidedHorizontally) {
               if (Minecraft.player.isCollidedHorizontally) {
                  Minecraft.player.setPosition(x, y + 0.2, z);
               }

               Minecraft.player.onGround = true;
            }

            Minecraft.player.motionX = 0.0;
            Minecraft.player.motionZ = 0.0;
            Minecraft.player.motionY = 0.04;
            if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.LAVA
               && Minecraft.player.fallDistance != 0.0F
               && Minecraft.player.motionX == 0.0
               && Minecraft.player.motionZ == 0.0) {
               Minecraft.player.setPosition(x, y - 0.0400005, z);
               if ((double)Minecraft.player.fallDistance < 0.08) {
                  Minecraft.player.setPosition(x, y + 0.2, z);
               }
            }

            if (!Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
               Minecraft.player.jumpMovementFactor = 0.2865F;
            }

            if (Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
               Minecraft.player.jumpMovementFactor = 0.4005F;
            }
         }

         if (!mc.gameSettings.keyBindJump.isKeyDown() && (Minecraft.player.isInWater() || Minecraft.player.isInLava())) {
            Minecraft.player.motionY = 0.12;
            if (Minecraft.player.isInWater()
               && mc.world.getBlockState(new BlockPos(x, y + 0.9, z)).getBlock() == Blocks.WATER
               && mc.world.getBlockState(new BlockPos(x, y + 1.0, z)).getBlock() == Blocks.AIR
               && mc.world.getBlockState(new BlockPos(x, y - 1.0, z)).getBlock() != Blocks.WATER) {
               Minecraft.player.posY += 0.1;
               Minecraft.player.motionY = 0.42;
            }
         }
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("AacJetPack") && (Minecraft.player.isInWater() || Minecraft.player.isInLava())) {
         Minecraft.player.motionY += 0.22F;
         Minecraft.player.speedInAir = 0.04F;
      }

      if (this.JesusMode.currentMode.equalsIgnoreCase("MatrixJump")
         && (
            mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER || mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA
         )) {
         Minecraft.player.jump();
         Minecraft.player.onGround = true;
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (Minecraft.player != null) {
         if (actived) {
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY;
            double z = Minecraft.player.posZ;
            if (this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid3")
               && Minecraft.player.onGround
               && Minecraft.player.isInWater()
               && !Speed.posBlock(x, y + 1.0, z)) {
               boolean upWater = mc.world.getBlockState(new BlockPos(x, y + 1.2, z)).getBlock() == Blocks.WATER;
               if (upWater) {
                  Minecraft.player.setPosY(Minecraft.player.posY + 0.25);
                  Minecraft.player.motionY = 0.42;
               } else {
                  Minecraft.player.setPosY(Minecraft.player.posY + 0.2);
                  Minecraft.player.motionY = 0.42;
               }
            }

            if (this.JesusMode.currentMode.equalsIgnoreCase("NcpStatic") && (Minecraft.player.isInWater() || Minecraft.player.isInLava())) {
               Minecraft.player.motionY += 0.1;
               if (mc.gameSettings.keyBindJump.isKeyDown()) {
                  Minecraft.player.motionY -= 0.15;
               }
            }
         } else {
            double xx = Minecraft.player.posX;
            double yx = Minecraft.player.posY;
            double zx = Minecraft.player.posZ;
            if (this.JesusMode.currentMode.equalsIgnoreCase("Matrix7.0.2") && mc.world.getBlockState(new BlockPos(xx, yx - 0.1, zx)).getBlock() == Blocks.WATER
               )
             {
               MoveMeHelp.setSpeed(0.0);
               Minecraft.player.jumpMovementFactor = 0.0F;
               Minecraft.player.stepHeight = 0.6F;
               Minecraft.player.motionY = -0.12;
               ticksLinked = 0;
            }

            isJesused = false;
            isSwimming = false;
            flagsCount = 0;
            if (!this.actived
               && (this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid") || this.JesusMode.currentMode.equalsIgnoreCase("MatrixSolid2"))
               && mc.world.getBlockState(new BlockPos(xx, yx - 0.1, zx)).getBlock() == Blocks.WATER) {
               Minecraft.player.motionX /= 15.0;
               Minecraft.player.motionZ /= 15.0;
               if (Minecraft.player.isCollidedHorizontally) {
                  Minecraft.player.setPosition(xx, yx + 0.15, zx);
               }
            }

            if (this.JesusMode.currentMode.equalsIgnoreCase("MatrixZoom2")
               && (
                  mc.world.getBlockState(new BlockPos(xx, yx - 0.1, zx)).getBlock() == Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(xx, yx - 0.1, zx)).getBlock() == Blocks.LAVA
               )) {
               MoveMeHelp.setSpeed(0.23);
               if (Minecraft.player.isCollidedHorizontally) {
                  Minecraft.player.setPosition(xx, yx + 0.01, zx);
               }
            }

            Minecraft.player.speedInAir = 0.02F;
         }
      }

      isJesused = false;
      super.onToggled(actived);
   }
}
