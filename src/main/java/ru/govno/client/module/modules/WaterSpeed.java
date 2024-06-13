package ru.govno.client.module.modules;

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
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.TPSDetect;
import ru.govno.client.utils.Wrapper;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class WaterSpeed extends Module {
   public static WaterSpeed get;
   public ModeSettings Mode;
   public BoolSettings PotionCheck;
   public BoolSettings MotionUp;
   public BoolSettings MotionDown;
   public BoolSettings SmoothOutput;
   public BoolSettings MoveInLava;
   public BoolSettings NoGravity;
   public BoolSettings MultiplySpeed;
   public BoolSettings DamageBoost;
   public FloatSettings Speeds;
   public FloatSettings SpeedUp;
   public FloatSettings SpeedDown;
   public FloatSettings Multiply;
   public static boolean isFlowingWater;
   public static boolean halfBoost = false;
   public static double speedInWater = 0.0;
   float moveYaw;
   protected static int ticksWaterMoving = 0;

   public WaterSpeed() {
      super("WaterSpeed", 0, Module.Category.MOVEMENT);
      get = this;
      this.settings.add(this.Mode = new ModeSettings("Mode", "Matrix", this, new String[]{"Custom", "Matrix", "Vulcan&NCP"}));
      this.settings.add(this.PotionCheck = new BoolSettings("PotionCheck", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Custom")));
      this.settings.add(this.Speeds = new FloatSettings("Speed", 0.45F, 1.0F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Custom")));
      this.settings.add(this.MotionUp = new BoolSettings("MotionUp", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Custom")));
      this.settings.add(this.MotionDown = new BoolSettings("MotionDown", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Custom")));
      this.settings.add(this.SpeedUp = new FloatSettings("SpeedUp", 1.85F, 5.0F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Custom")));
      this.settings.add(this.SpeedDown = new FloatSettings("SpeedDown", 2.0F, 5.0F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Custom")));
      this.settings.add(this.SmoothOutput = new BoolSettings("SmoothOutput", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Custom")));
      this.settings.add(this.MoveInLava = new BoolSettings("MoveInLava", true, this));
      this.settings
         .add(
            this.NoGravity = new BoolSettings(
               "NoGravity", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Matrix") || this.Mode.currentMode.equalsIgnoreCase("Vulcan&NCP")
            )
         );
      this.settings.add(this.MultiplySpeed = new BoolSettings("MultiplySpeed", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Matrix")));
      this.settings
         .add(
            this.Multiply = new FloatSettings(
               "Multiply", 0.9F, 1.5F, 0.0F, this, () -> this.Mode.currentMode.equalsIgnoreCase("Matrix") && this.MultiplySpeed.getBool()
            )
         );
      this.settings.add(this.DamageBoost = new BoolSettings("DamageBoost", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Matrix")));
   }

   @Override
   public void onMovement() {
      if (this.Mode.currentMode.equalsIgnoreCase("Matrix")) {
         if (JesusSpeed.get.actived && JesusSpeed.get.JesusMode.currentMode.equalsIgnoreCase("Matrix7.0.2")) {
            return;
         }

         if (isFlowingWater) {
            boolean var10000;
            label402: {
               if (mc.world.getBlockState(BlockUtils.getEntityBlockPos(Minecraft.player)).getBlock() instanceof BlockLiquid liq
                  && liq.getDepth(mc.world.getBlockState(BlockUtils.getEntityBlockPos(Minecraft.player))) > 1) {
                  var10000 = true;
                  break label402;
               }

               var10000 = false;
            }

            isFlowingWater = var10000;
         }

         boolean conflictSpeedFlowingWater = false;
         double speed = 0.0;
         float yport = 0.0101F;
         if (Minecraft.player.isPotionActive(Potion.getPotionById(1)) && Minecraft.player.getActivePotionEffect(Potion.getPotionById(1)).getDuration() > 7) {
            Minecraft.player.getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
         } else {
            byte var31 = -1;
         }

         if (mc.isSingleplayer() || mc.getCurrentServerData() == null) {
            String var32 = "";
         }

         float mYaw = MoveMeHelp.moveYaw(Minecraft.player.rotationYaw) % 360.0F;
         this.moveYaw = mYaw * 1.01F;
         boolean isInLiq = mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.075, Minecraft.player.posZ)).getBlock()
               == Blocks.WATER
            || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.075, Minecraft.player.posZ)).getBlock() == Blocks.LAVA
               && this.MoveInLava.getBool();
         if ((Minecraft.player.isInWater() || this.MoveInLava.getBool() && Minecraft.player.isInLava())
            && (Minecraft.player == null || !Minecraft.player.capabilities.isFlying)) {
            boolean isShar = Minecraft.player.getHeldItemOffhand().getDisplayName().contains("Шар скорости");
            boolean isShar2 = Minecraft.player.getHeldItemOffhand().getDisplayName().contains("Шар скорости 2")
               || Minecraft.player.getHeldItemOffhand().getDisplayName().contains("Шар скорости 3");
            boolean uppedWater = (
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() == Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() == Blocks.LAVA
                        && this.MoveInLava.getBool()
               )
               && (
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.1, Minecraft.player.posZ)).getBlock() == Blocks.WATER
                     || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.1, Minecraft.player.posZ)).getBlock()
                           == Blocks.LAVA
                        && this.MoveInLava.getBool()
               );
            boolean isWaterGround = Minecraft.player.onGround && Minecraft.player.isCollidedVertically;
            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.WATER
               && Minecraft.player.motionY < -0.1) {
               boolean var34 = true;
            } else {
               boolean var33 = false;
            }

            boolean antiNoGravity = mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.2, Minecraft.player.posZ)).getBlock()
                     != Blocks.WATER
                  && (
                     !this.MoveInLava.getBool()
                        || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.2, Minecraft.player.posZ)).getBlock()
                           != Blocks.LAVA
                  )
                  && (
                     mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)).getBlock() != Blocks.WATER
                           && (
                              !this.MoveInLava.getBool()
                                 || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)).getBlock()
                                    != Blocks.LAVA
                           )
                        || Minecraft.player.isCollidedHorizontally && Minecraft.player.isJumping()
                  )
               || Minecraft.player.isSneaking()
               || Minecraft.player.isJumping();
            Enchantment depth = Enchantments.DEPTH_STRIDER;
            int depthLvl = EnchantmentHelper.getEnchantmentLevel(depth, Minecraft.player.inventory.armorItemInSlot(0));
            Minecraft.player.serverSprintState = false;
            if (depthLvl > 0) {
               double speedSP_GR_UP = 0.75;
               double speedSP_NO_GR = 0.6;
               double noSpeed_GR = 0.358;
               double noSpeed_NO_GR = 0.2699;
               speed = Minecraft.player.isPotionActive(Potion.getPotionById(1))
                  ? (isWaterGround ? (uppedWater ? speedSP_GR_UP : speedSP_NO_GR) : speedSP_NO_GR)
                  : (isWaterGround ? noSpeed_GR : noSpeed_NO_GR);
            } else {
               boolean waterAdobe = mc.world
                        .getBlockState(
                           new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (Minecraft.player.onGround ? 1.0 : 0.6), Minecraft.player.posZ)
                        )
                        .getBlock()
                     == Blocks.WATER
                  || this.MoveInLava.getBool()
                     && mc.world
                           .getBlockState(
                              new BlockPos(Minecraft.player.posX, Minecraft.player.posY + (Minecraft.player.onGround ? 1.0 : 0.6), Minecraft.player.posZ)
                           )
                           .getBlock()
                        == Blocks.LAVA;
               if (mc.gameSettings.keyBindJump.isKeyDown() && waterAdobe) {
                  double var36 = 0.03;
               } else {
                  double var35 = 0.0;
               }

               speed = Minecraft.player.onGround
                  ? (!waterAdobe && !Minecraft.player.isJumping() ? 0.1199 : (Minecraft.player.isJumping() ? 0.13 : 0.2199))
                  : (waterAdobe ? 0.1199 : 0.075);
            }

            if (isShar && uppedWater) {
               speed *= isShar2 ? 1.299 : 1.149;
            }

            Entity.motiony = this.NoGravity.getBool() && !isWaterGround && !antiNoGravity
               ? (double)(0.0101F * (float)(Minecraft.player.ticksExisted % 3 <= 1 ? 1 : -2))
               : Entity.Getmotiony;
            if (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) {
               Entity.motiony = Minecraft.player.isCollidedHorizontally ? (uppedWater ? 0.19 : 0.55) : -0.0101F;
            } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
               if (Minecraft.player.onGround
                  && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, (double)((int)Minecraft.player.posY + 1), Minecraft.player.posZ)).getBlock()
                     != Blocks.WATER
                  && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, (double)((int)Minecraft.player.posY) + 0.99, Minecraft.player.posZ)).getBlock()
                     == Blocks.WATER) {
                  halfBoost = true;
               } else if (!Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 0.45, Minecraft.player.posZ)) {
                  halfBoost = false;
               }

               if (halfBoost) {
                  Minecraft.player.motionY = Minecraft.player.onGround ? 0.4 : 0.16;
               } else if (Entity.Getmotiony < 0.0) {
                  Minecraft.player.motionY -= 0.1F;
               }

               if (mc.world
                        .getBlockState(
                           new BlockPos(
                              Minecraft.player.posX,
                              Minecraft.player.posY + (!Minecraft.player.onGround && !(Entity.Getmotiony < 0.0) ? 0.5 : 1.0),
                              Minecraft.player.posZ
                           )
                        )
                        .getBlock()
                     != Blocks.WATER
                  && (
                     !this.MoveInLava.getBool()
                        || mc.world
                              .getBlockState(
                                 new BlockPos(
                                    Minecraft.player.posX,
                                    Minecraft.player.posY + (!Minecraft.player.onGround && !(Entity.Getmotiony < 0.0) ? 0.5 : 1.0),
                                    Minecraft.player.posZ
                                 )
                              )
                              .getBlock()
                           != Blocks.LAVA
                  )) {
                  boolean var38 = false;
               } else {
                  boolean var37 = true;
               }

               if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.6, Minecraft.player.posZ)).getBlock() != Blocks.WATER
                  && (
                     !this.MoveInLava.getBool()
                        || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.6, Minecraft.player.posZ)).getBlock()
                           != Blocks.LAVA
                  )) {
                  boolean var40 = false;
               } else {
                  boolean var39 = true;
               }

               if (!halfBoost
                  && (!Minecraft.player.isJumping() || !Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ) || uppedWater)
                  )
                {
                  Entity.motiony = 0.19;
                  Minecraft.player.motionY = 0.19;
               }
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
               Entity.motiony = -0.4;
            }

            if (isFlowingWater && !conflictSpeedFlowingWater) {
               isFlowingWater = false;
               double boostFlow = !uppedWater && !isWaterGround ? 0.6 : 0.651;
               if (speed < boostFlow) {
                  speed = boostFlow;
               }
            }

            if (this.MultiplySpeed.getBool()) {
               speed *= (double)this.Multiply.getFloat();
            }

            if (EntityLivingBase.isMatrixDamaged && !Minecraft.player.ticker.hasReached(1200.0) && this.DamageBoost.getBool()) {
               speed = depthLvl > 0 ? 1.7 : 1.0;
            }

            speedInWater = speed;
         } else if (!isInLiq) {
            speedInWater = 0.0;
         }

         double yawDiff = MathUtils.getDifferenceOf(Minecraft.player.PreYaw, MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
         speedInWater *= 1.0 - yawDiff / 60.0 / 360.0;
         if (speedInWater != 0.0) {
            MoveMeHelp.setMotionSpeed(true, true, speedInWater / 1.06, this.moveYaw);
            MoveMeHelp.setMotionSpeed(false, true, speedInWater, this.moveYaw);
         } else if (speedInWater == 0.0 && isInLiq) {
            MoveMeHelp.multiplySpeed(0.7F);
         }
      }
   }

   @Override
   public String getDisplayName() {
      return this.Mode.currentMode.equalsIgnoreCase("Custom")
         ? this.getDisplayByDouble((double)this.Speeds.getFloat())
         : this.getDisplayByMode(this.Mode.currentMode);
   }

   public static double AIMoveWaterSpeedMultiply(double prevAI, EntityLivingBase base) {
      if (base instanceof EntityPlayerSP && get.actived && get.Mode.currentMode.equalsIgnoreCase("Vulcan&NCP") && !base.isSprinting()) {
         prevAI *= 1.300001;
      }

      return prevAI;
   }

   public static double getWaterSlowDownMultiply(double prevSlow, EntityLivingBase base) {
      if (base instanceof EntityPlayerSP && get.actived && get.Mode.currentMode.equalsIgnoreCase("Vulcan&NCP")) {
         int depthLvL = EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, Minecraft.player.inventory.armorItemInSlot(0));
         double curSpeed = MoveMeHelp.getSpeed();
         int speedLvL = base.isPotionActive(MobEffects.SPEED) ? base.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1 : 0;
         float pcExt = (!Minecraft.player.isMoving() || (!MoveMeHelp.a() || MoveMeHelp.d()) && (MoveMeHelp.a() || !MoveMeHelp.d()) ? 1.0F : 0.98F)
            * (ticksWaterMoving == -2 ? (speedLvL == 2 ? 1.05F : 1.0F) : MathUtils.clamp((float)ticksWaterMoving / 7.0F, 0.0F, 1.0F));
         pcExt = (float)((double)pcExt * (Minecraft.player.isJumping() && ticksWaterMoving > 2 ? 0.8 : 1.0));
         pcExt *= TPSDetect.getTPSServer() / 20.0F;
         float plus = depthLvL == 3
            ? (speedLvL == 0 ? 0.499F : (speedLvL == 1 ? 0.65F : 0.58F))
            : (depthLvL == 2 ? (speedLvL == 0 ? 0.32F : 0.3F) : (depthLvL == 1 ? 0.3F : 0.0F));
         plus *= pcExt;
         prevSlow *= (double)(ticksWaterMoving % 2 == 0 && ticksWaterMoving > -2 ? 1.0F : 1.0F + plus);
      }

      return prevSlow;
   }

   private double minWaterSpeedNcp() {
      double bps = 0.0;
      int depthLvL = EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, Minecraft.player.inventory.armorItemInSlot(0));
      if (Minecraft.player.isPotionActive(MobEffects.SPEED)) {
         int var10000 = Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1;
      } else {
         boolean var5 = false;
      }

      if (depthLvL == 0) {
         bps = 1.0;
      }

      return MoveMeHelp.getSpeedByBPS(bps);
   }

   @Override
   public void onUpdate() {
      if (this.Mode.currentMode.equalsIgnoreCase("Vulcan&NCP")) {
         if (JesusSpeed.get.actived && JesusSpeed.get.JesusMode.currentMode.equalsIgnoreCase("Matrix7.0.2")) {
            return;
         }

         boolean legsInAir = mc.world.isAirBlock(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.5, Minecraft.player.posZ))
            && !Minecraft.player.capabilities.isFlying;
         boolean inWater = Minecraft.player.isInWater();
         boolean onWater = !Minecraft.player.capabilities.isFlying
            && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.2, Minecraft.player.posZ)).getBlock() instanceof BlockLiquid;
         double curSpeed = MoveMeHelp.getSpeed();
         ticksWaterMoving = inWater && MoveMeHelp.isMoving() ? ticksWaterMoving + 1 : (Minecraft.player.isJumping() ? -3 : -1);
         if (onWater) {
            Minecraft.player.setSprinting(false);
            MoveMeHelp.setSpeed(MoveMeHelp.getSpeed(), 0.6F);
         }

         if (onWater) {
            int depthLvL = EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, Minecraft.player.inventory.armorItemInSlot(0));
            boolean frictionStapple = depthLvL != 0 && legsInAir && ticksWaterMoving > 4;
            if ((depthLvL == 0 && legsInAir || frictionStapple) && Minecraft.player.isJumping()) {
               Minecraft.player.multiplyMotionXZ(frictionStapple ? 0.68F : 0.95F);
            }
         }

         if (inWater) {
            double yPort = this.NoGravity.getBool() && !Minecraft.player.onGround && !legsInAir
               ? (Minecraft.player.ticksExisted % 2 == 0 ? 0.01 : -0.01)
               : Minecraft.player.motionY;
            Minecraft.player.motionY = Minecraft.player.isJumping()
               ? (
                  Minecraft.player.isCollidedHorizontally && legsInAir
                     ? 0.3
                     : ((double)Minecraft.player.fallDistance > 0.1 && Minecraft.player.motionY < 0.0 ? -0.06 : 0.09)
               )
               : (Minecraft.player.isSneaking() ? Minecraft.player.motionY - 0.15 : yPort);
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("Custom")) {
         if (JesusSpeed.get.actived && JesusSpeed.get.JesusMode.currentMode.equalsIgnoreCase("Matrix7.0.2")) {
            return;
         }

         boolean gs = Speed.get.actived && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Guardian");
         if ((!this.PotionCheck.getBool() || Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1)))
            && (Minecraft.player.isInWater() || this.MoveInLava.getBool() && Minecraft.player.isInLava())
            && !gs
            && (
               !Minecraft.player.isJumping()
                  || !Minecraft.player.isCollidedHorizontally
                  || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.0, Minecraft.player.posZ)).getBlock() != Blocks.AIR
                  || !this.SmoothOutput.getBool()
            )) {
            if (this.MotionUp.getBool() && Minecraft.player.isJumping()) {
               Minecraft.player.motionY = (double)(this.SpeedUp.getFloat() / 5.0F * (gs ? 3.0F : 1.0F));
            }

            if (this.MotionDown.getBool() && Minecraft.player.isSneaking()) {
               Minecraft.player.motionY = (double)(-(this.SpeedDown.getFloat() / 5.0F * (gs ? 3.0F : 1.0F)));
            }

            if (this.SmoothOutput.getBool()
               && Minecraft.player.isCollidedHorizontally
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 1.0, Minecraft.player.posZ)).getBlock() == Blocks.AIR) {
               return;
            }

            if (Speed.get.actived && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Guardian")) {
               if (!mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                  Minecraft.player.motionY = 0.0;
               }

               return;
            }

            MoveMeHelp.setSpeed((double)this.Speeds.getFloat());
         }
      }
   }
}
