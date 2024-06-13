package ru.govno.client.module.modules;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemElytra;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Movement.MovementHelper;

public class Speed extends Module {
   public static Speed get;
   public ModeSettings AntiCheat;
   public FloatSettings SpeedF;
   public BoolSettings OnlyMove;
   public BoolSettings Bhop;
   public BoolSettings BhopOnlyDamage;
   public BoolSettings DamageBoost;
   public BoolSettings IceSpeed;
   public BoolSettings AirBoost;
   public BoolSettings GroundBoost;
   public BoolSettings Yport;
   public BoolSettings SnowBoost;
   public BoolSettings StrafeDamageHop;
   public BoolSettings LongHop;
   public BoolSettings OnGround;
   public BoolSettings UseTimer;
   public BoolSettings Caress;
   private boolean enabledWithModeVanillaAir;
   public static boolean snowGo = false;
   public static boolean snowGround = false;
   private final TimerHelper areaTimer = new TimerHelper();
   public boolean cancelStrafe;
   public static float ncpSpeed = 0.0F;
   public static boolean iceGo;
   private final TimerHelper ncpIceTimer = new TimerHelper();
   private final TimerHelper forGuardianTimer = new TimerHelper();
   private final TimerHelper forRipServerTimer = new TimerHelper();

   public Speed() {
      super("Speed", 0, Module.Category.MOVEMENT);
      get = this;
      this.settings
         .add(
            this.AntiCheat = new ModeSettings(
               "AntiCheat",
               "Matrix",
               this,
               new String[]{"Matrix", "AAC", "NCP", "Guardian", "RipServer", "Intave", "Vanilla", "Vulcan", "Strict", "Grim", "VanillaAir"}
            )
         );
      this.settings.add(this.SpeedF = new FloatSettings("Speed", 0.8F, 2.0F, 0.23F, this, () -> this.AntiCheat.getMode().contains("Vanilla")));
      this.settings.add(this.OnlyMove = new BoolSettings("OnlyMove", false, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Vanilla")));
      this.settings.add(this.Bhop = new BoolSettings("Bhop", true, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix")));
      this.settings
         .add(
            this.BhopOnlyDamage = new BoolSettings(
               "BhopOnlyDamage", false, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix") && this.Bhop.getBool()
            )
         );
      this.settings
         .add(
            this.DamageBoost = new BoolSettings(
               "DamageBoost", true, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix") || this.AntiCheat.getMode().equalsIgnoreCase("NCP")
            )
         );
      this.settings.add(this.IceSpeed = new BoolSettings("IceSpeed", true, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("NCP")));
      this.settings.add(this.AirBoost = new BoolSettings("AirBoost", false, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix")));
      this.settings.add(this.GroundBoost = new BoolSettings("GroundBoost", false, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix")));
      this.settings
         .add(
            this.Yport = new BoolSettings(
               "Yport", false, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix") || this.AntiCheat.getMode().equalsIgnoreCase("NCP")
            )
         );
      this.settings.add(this.SnowBoost = new BoolSettings("SnowBoost", false, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix")));
      this.settings.add(this.StrafeDamageHop = new BoolSettings("StrafeDamageHop", true, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Matrix")));
      this.settings.add(this.LongHop = new BoolSettings("LongHop", true, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("AAC")));
      this.settings.add(this.OnGround = new BoolSettings("OnGround", true, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("AAC")));
      this.settings.add(this.UseTimer = new BoolSettings("UseTimer", true, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("NCP")));
      this.settings.add(this.Caress = new BoolSettings("Caress", false, this, () -> this.AntiCheat.getMode().equalsIgnoreCase("Intave")));
   }

   public static boolean posBlock(double x, double y, double z) {
      return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WATER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.LAVA
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BED
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CAKE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.TALLGRASS
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE_BUTTON
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WOODEN_BUTTON
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.FLOWER_POT
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CHORUS_FLOWER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.RED_FLOWER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.YELLOW_FLOWER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SAPLING
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.VINE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.NETHER_BRICK_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE_GATE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ENCHANTING_TABLE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.END_PORTAL_FRAME
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DOUBLE_PLANT
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STANDING_SIGN
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WALL_SIGN
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SKULL
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DAYLIGHT_DETECTOR
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DAYLIGHT_DETECTOR_INVERTED
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.PURPUR_SLAB
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE_SLAB
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WOODEN_SLAB
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CARPET
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DEADBUSH
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_WIRE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WALL_BANNER
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REEDS
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.UNLIT_REDSTONE_TORCH
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.TORCH
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_WIRE
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WATERLILY
         && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SNOW_LAYER;
   }

   public static boolean canMatrixBoost() {
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;
      float ex = 0.39F;
      if ((
            mc.world.getBlockState(new BlockPos(x, y - (double)ex, z)).getBlock() == Blocks.PURPUR_SLAB
               || mc.world.getBlockState(new BlockPos(x, y - (double)ex, z)).getBlock() == Blocks.STONE_SLAB2
               || mc.world.getBlockState(new BlockPos(x, y - (double)ex, z)).getBlock() == Blocks.STONE_SLAB
               || mc.world.getBlockState(new BlockPos(x, y - (double)ex, z)).getBlock() == Blocks.WOODEN_SLAB
         )
         && Minecraft.player.posY + 0.5 >= (double)((int)Minecraft.player.posY)) {
         ex += 0.62F;
      }

      return (double)Minecraft.player.fallDistance > 0.1
            && !(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)
            && (
               posBlock(x, y - (double)ex, z)
                  || posBlock(x - 0.3, y - (double)ex, z - 0.3)
                  || posBlock(x + 0.3, y - (double)ex, z + 0.3)
                  || posBlock(x - 0.3, y - (double)ex, z + 0.3)
                  || posBlock(x + 0.3, y - (double)ex, z - 0.3)
                  || posBlock(x + 0.3, y - (double)ex, z)
                  || posBlock(x - 0.3, y - (double)ex, z)
                  || posBlock(x, y - (double)ex, z - 0.3)
                  || posBlock(x, y - (double)ex, z + 0.3)
            )
         ? MoveMeHelp.getSpeed() > MoveMeHelp.getSpeedByBPS(4.3)
         : false;
   }

   @Override
   public void onMovement() {
      if (Minecraft.player != null && mc.world != null) {
         try {
            this.speedMove(this.AntiCheat.currentMode);
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.AntiCheat.currentMode);
   }

   @Override
   public void onUpdate() {
      if (Minecraft.player != null && mc.world != null) {
         try {
            this.speed(this.AntiCheat.currentMode);
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived) {
         this.onDisableSpeed();
      }

      if (actived && this.AntiCheat.currentMode.equalsIgnoreCase("VanillaAir")) {
         this.enabledWithModeVanillaAir = true;
      }

      super.onToggled(actived);
   }

   private void onDisableSpeed() {
      if (this.AntiCheat.currentMode.equalsIgnoreCase("NCP")) {
         this.forNCPoff(this.UseTimer.getBool());
      }

      if (this.AntiCheat.currentMode.equalsIgnoreCase("AAC") && mc.timer.speed == 1.2) {
         mc.timer.speed = 1.0;
      }

      if ((this.AntiCheat.currentMode.equalsIgnoreCase("Intave") || this.AntiCheat.currentMode.equalsIgnoreCase("Strict")) && mc.timer.speed != 1.0) {
         this.forIntaveOrStrictOff();
      }

      if (this.AntiCheat.currentMode.equalsIgnoreCase("Matrix")) {
         snowGo = false;
         snowGround = false;
      }

      if (this.AntiCheat.currentMode.equalsIgnoreCase("VanillaAir") || this.enabledWithModeVanillaAir) {
         this.forVanillaAirOff();
      }

      this.cancelStrafe = false;
   }

   private void speed(String antiCheat) {
      if (antiCheat != null && (MoveMeHelp.moveKeysPressed() || antiCheat.equalsIgnoreCase("NCP") && TargetStrafe.goStrafe())) {
         if (antiCheat.equalsIgnoreCase("Matrix")) {
            this.forMatrix(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ);
         }

         if (antiCheat.equalsIgnoreCase("AAC")) {
            this.forAAC();
         }

         if (antiCheat.equalsIgnoreCase("NCP")) {
            this.forNCP(
               this.UseTimer.getBool(),
               this.DamageBoost.getBool(),
               this.IceSpeed.getBool(),
               this.Yport.getBool(),
               Minecraft.player.posX,
               Minecraft.player.posY,
               Minecraft.player.posZ
            );
         } else if (Minecraft.player.speedInAir == 0.06F || Minecraft.player.speedInAir == 0.05F) {
            Minecraft.player.speedInAir = 0.02F;
         }

         if (antiCheat.equalsIgnoreCase("Guardian")) {
            this.forGuardian();
         }

         if (antiCheat.equalsIgnoreCase("RipServer")) {
            this.forRipServer();
         }

         if (antiCheat.equalsIgnoreCase("Intave")) {
            this.forIntave(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ, this.Caress.getBool());
         }

         if (antiCheat.equalsIgnoreCase("Vulcan")) {
            this.forVulcan();
         }

         if (antiCheat.equalsIgnoreCase("Strict")) {
            this.forStrict();
         }

         if (antiCheat.equalsIgnoreCase("Grim")) {
            this.forGrim();
         }

         if (antiCheat.equalsIgnoreCase("VanillaAir")) {
            this.forVanillaAir();
         }
      }
   }

   private void forVulcan() {
      PotionEffect active = Minecraft.player.getActivePotionEffect(MobEffects.SPEED);
      boolean isSpeed = active != null && active.getAmplifier() >= 0 && active.getDuration() > 9;
      boolean isSpeed2 = active != null && active.getAmplifier() >= 1 && active.getDuration() > 9;
      int ticks = Minecraft.player.ticksExisted;
      AxisAlignedBB bx = Minecraft.player.boundingBox;
      if (Minecraft.player.movementInput.jump) {
         if (Minecraft.player.onGround) {
            return;
         }

         if (Minecraft.player.fallDistance != 0.0F && (double)Minecraft.player.fallDistance < 0.5) {
            Minecraft.player.motionY -= 0.1F;
            mc.timer.field_194147_b = 0.1F;
         }

         if (!isSpeed && !isSpeed2) {
            MoveMeHelp.setSpeed(MathUtils.clamp(MoveMeHelp.getSpeed(), 0.29F, MoveMeHelp.getSpeedByBPS(5.25)));
            return;
         }

         MoveMeHelp.setSpeed(
            MathUtils.clamp(MoveMeHelp.getSpeed(), Minecraft.player.onGround ? 0.12F : (isSpeed ? 0.374F : (isSpeed2 ? 0.449F : 0.29F)), MoveMeHelp.getSpeed())
         );
      } else if (!mc.world.getCollisionBoxes(Minecraft.player, new AxisAlignedBB(bx.minX, bx.minY - 0.08F, bx.minZ, bx.maxX, bx.minY, bx.maxZ)).isEmpty()
         && mc.world.getCollisionBoxes(Minecraft.player, bx).isEmpty()
         && MoveMeHelp.getSpeed() > 0.1) {
         if (Criticals.get.actived
            && Criticals.get.EntityHit.getBool()
            && Criticals.get.HitMode.currentMode.equalsIgnoreCase("VanillaHop")
            && HitAura.TARGET != null
            && !Minecraft.player.isJumping()
            && !Minecraft.player.isInWater()) {
            float time = HitAura.get.msCooldown() - (float)HitAura.cooldown.getTime();
            if (time < 300.0F) {
               return;
            }
         }

         double speed = isSpeed2 ? 0.4 : (isSpeed ? 0.35 : 0.29);
         if (Minecraft.player.onGround && Minecraft.player.ticksExisted % 3 == 0) {
            Minecraft.player.motionY = 0.0391;
            speed = isSpeed2 ? 0.59 : 0.49;
         }

         if (ticks % 3 == 2) {
            mc.timer.field_194147_b = 0.1F;
         }

         MoveMeHelp.setSpeed(speed);
      }
   }

   private void forStrict() {
      mc.timer.field_194147_b = Minecraft.player.ticksExisted % 6 == 0 ? 1.3F : 0.0F;
      if (Minecraft.player.isJumping() && Entity.Getmotiony < 0.1 && Minecraft.player.fallDistance <= 1.0F) {
         Entity.motiony = Entity.Getmotiony - 0.079;
      }
   }

   private void speedMove(String antiCheat) {
      if (antiCheat != null) {
         if (antiCheat.equalsIgnoreCase("Vanilla")) {
            this.forVanilla();
         }

         if (antiCheat.equalsIgnoreCase("RipServer")) {
            this.forRipServerMove();
         }
      }
   }

   private void forVanilla() {
      MoveMeHelp.setMotionSpeed(true, this.OnlyMove.getBool(), (double)(this.SpeedF.getFloat() * (Minecraft.player.ticksExisted % 2 == 0 ? 1.0F : 0.99F)));
   }

   private void forIntave(double x, double y, double z, boolean caress) {
      mc.timer.speed = Minecraft.player.fallDistance == 0.0F && !Minecraft.player.onGround ? 1.12F : 1.08F;
      if (!caress) {
         if ((double)Minecraft.player.fallDistance > 0.1) {
            Minecraft.player.jumpMovementFactor = (float)((double)Minecraft.player.jumpMovementFactor + 1.3E-4);
         }

         float ex = 0.38F;
         Minecraft.player.setSprinting(Minecraft.player.fallDistance != 0.0F);
         Minecraft.player.serverSprintState = Minecraft.player.isSprinting();
         if (Minecraft.player.serverSprintState) {
            for (int i = 0; i < 2; i++) {
               Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SPRINTING));
            }
         }

         Minecraft.player.jumpTicks = 0;
         if (Minecraft.player.isCollidedVertically && MoveMeHelp.getSpeed() >= 0.4) {
            Minecraft.player.onGround = false;
         }

         if ((double)Minecraft.player.fallDistance > 0.1 && posBlock(x, y - (double)ex, z) && Minecraft.player.isJumping()) {
            mc.timer.speed = 1.5;
            Minecraft.player.fallDistance = 0.0F;
            Minecraft.player.fall(16.0F, 20.0F);
            Minecraft.player.onGround = true;
            Minecraft.player.motionY /= 1.002F;
            Minecraft.player.posY -= 0.0034;
         }
      }

      if ((!Minecraft.player.onGround || !Minecraft.player.isJumping()) && !Minecraft.player.isMoving()) {
         Minecraft.player.multiplyMotionXZ(1.0013F);
      }

      if (Minecraft.player.onGround && !Minecraft.player.isJumping() && !Minecraft.player.isMoving() && !this.Caress.getBool()) {
         MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 1.016);
      }
   }

   private void forIntaveOrStrictOff() {
      if (mc.timer.speed != 1.0) {
         mc.timer.speed = 1.0;
      }
   }

   private void forVanillaAirOff() {
      Minecraft.player.speedInAir = 0.02F;
      this.enabledWithModeVanillaAir = false;
   }

   private void forMatrix(double x, double y, double z) {
      float w = Minecraft.player.width / 2.0F - 0.025F;
      boolean posed = posBlock(x, y - 1.0E-10, z)
         || posBlock(x + (double)w, y - 1.0E-10, z + (double)w)
         || posBlock(x - (double)w, y - 1.0E-10, z - (double)w)
         || posBlock(x + (double)w, y - 1.0E-10, z - (double)w)
         || posBlock(x - (double)w, y - 1.0E-10, z + (double)w)
         || posBlock(x + (double)w, y - 1.0E-10, z)
         || posBlock(x - (double)w, y - 1.0E-10, z)
         || posBlock(x, y - 1.0E-10, z + (double)w)
         || posBlock(x, y - 1.0E-10, z - (double)w);
      boolean yPort = !Minecraft.player.onGround
         && (double)Minecraft.player.fallDistance >= 0.068
         && posBlock(x, y - (this.AirBoost.getBool() ? 0.9 : 0.5), z)
         && this.Yport.getBool();
      boolean bHop = this.Bhop.getBool()
         && (EntityLivingBase.isMatrixDamaged || !this.BhopOnlyDamage.getBool())
         && Minecraft.player.isJumping()
         && (canMatrixBoost() || yPort)
         && !Minecraft.player.onGround
         && !Minecraft.player.isSneaking();
      boolean dBoost = this.DamageBoost.getBool()
         && EntityLivingBase.isMatrixDamaged
         && (canMatrixBoost() && bHop || Minecraft.player.onGround && Minecraft.player.isCollidedVertically && posed)
         && MoveMeHelp.getCuttingSpeed() < 1.2;
      boolean airBoost = Minecraft.player.fallDistance == 0.0F
         && posBlock(x, y - 1.0, z)
         && this.AirBoost.getBool()
         && Minecraft.player.isJumping()
         && MoveMeHelp.isMoving()
         && !Minecraft.player.isSneaking()
         && (!Minecraft.player.isCollidedVertically || Minecraft.player.posY == (double)((int)Minecraft.player.posY) || Minecraft.player.ticksExisted % 2 != 0)
         && !EntityLivingBase.isMatrixDamaged;
      boolean gBoost = Minecraft.player.onGround
         && Minecraft.player.isCollidedVertically
         && !Minecraft.player.isJumping()
         && this.GroundBoost.getBool()
         && posBlock(x, y - 1.0E-10, z);
      boolean snowBoost = this.SnowBoost.getBool();
      if (bHop && !dBoost) {
         double bSpeed = MoveMeHelp.getSpeed() * 1.9987;
         MoveMeHelp.setSpeed(bSpeed);
         MoveMeHelp.setCuttingSpeed(bSpeed / 1.06F);
      }

      if (dBoost) {
         if (bHop) {
            double bSpeed = MoveMeHelp.getSpeed() * 2.461F;
            MoveMeHelp.setSpeed(bSpeed);
            MoveMeHelp.setCuttingSpeed(bSpeed / 1.06F);
            if (Minecraft.player.stepHeight == 0.0F) {
               Minecraft.player.stepHeight = 0.6F;
            }
         } else if (!NoClip.get.actived) {
            if (Minecraft.player.stepHeight == 0.6F) {
               Minecraft.player.stepHeight = 0.0F;
            }

            float dir1 = (float)(-Math.sin((double)MovementHelper.getDirection())) * (mc.gameSettings.keyBindBack.isKeyDown() ? -1.0F : 1.0F);
            float dir2 = (float)Math.cos((double)MovementHelper.getDirection()) * (mc.gameSettings.keyBindBack.isKeyDown() ? -1.0F : 1.0F);
            if (MoveMeHelp.isMoving()) {
               if (MoveMeHelp.getSpeed() < 0.08) {
                  MoveMeHelp.setSpeed(0.42);
               } else {
                  Minecraft.player.addVelocity((double)dir1 * 9.8 / 25.0, 0.0, (double)dir2 * 9.8 / 25.0);
                  MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
               }
            }
         }
      } else if (Minecraft.player.stepHeight == 0.0F) {
         Minecraft.player.stepHeight = 0.6F;
      }

      if (airBoost && !dBoost) {
         Minecraft.player.onGround = true;
      }

      if (gBoost && !dBoost && Minecraft.player.onGround) {
         Minecraft.player.motionY--;
         if (!Minecraft.player.isJumping && Minecraft.player.ticksExisted % 3 == 0) {
            Minecraft.player.multiplyMotionXZ(1.35F);
            Minecraft.player.setPosition(x, y + 9.234E-7, z);
            Minecraft.player.posY -= 9.234E-7;
         }
      }

      if (yPort) {
         Minecraft.player.motionY = -0.22;
         Entity.motiony = -4.76;
      }

      if (snowBoost) {
         if (MoveMeHelp.getSpeed() < 0.36) {
            snowGround = false;
         }

         if ((
               mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.SNOW_LAYER
                  || mc.world.getBlockState(new BlockPos(x, y - 1.0, z)).getBlock() == Blocks.SNOW_LAYER
            )
            && (
               mc.world.getBlockState(new BlockPos(x, y - 1.0E-10, z)).getBlock() != Blocks.SNOW_LAYER
                  || Minecraft.player.isJumping() && (Minecraft.player.fallDistance != 0.0F || Minecraft.player.onGround)
            )
            && Minecraft.player.getItemInUseMaxCount() == 0
            && !Minecraft.player.isSneaking
            && (!snowGo || !(MoveMeHelp.getSpeed() > 0.4) || !Minecraft.player.isJumping)) {
            if (!Minecraft.player.isJumping() && Minecraft.player.onGround) {
               snowGround = true;
            } else if (MoveMeHelp.getSpeed() > 0.35 && snowGround) {
               MoveMeHelp.setSpeed(0.35);
            }

            if (snowGround) {
               MoveMeHelp.setCuttingSpeed(0.6205);
               snowGo = true;
            } else if (Minecraft.player.onGround && MoveMeHelp.getSpeed() < 0.14) {
               MoveMeHelp.setSpeed(0.18);
            }
         } else {
            snowGo = false;
         }
      }
   }

   private void forAAC() {
      boolean longHop = this.LongHop.getBool() && (Minecraft.player.isJumping() || Minecraft.player.fallDistance != 0.0F);
      boolean onGround = this.OnGround.getBool()
         && !Minecraft.player.isJumping()
         && Minecraft.player.onGround
         && Minecraft.player.isCollidedVertically
         && MoveMeHelp.getSpeed() < 0.9;
      mc.timer.speed = 1.2;
      if (longHop) {
         Minecraft.player.jumpMovementFactor = 0.17F;
         Minecraft.player.multiplyMotionXZ(1.005F);
      }

      if (onGround) {
         Minecraft.player.multiplyMotionXZ(1.212F);
      }
   }

   private void forNCP(boolean timer, boolean damageBoost, boolean iceSpeed, boolean yPort, double x, double y, double z) {
      if (timer) {
         Timer.forceTimer(1.075F);
      }

      double speed = 0.0;
      if (yPort) {
         speed = MoveMeHelp.getSpeed();
         if (Minecraft.player.isPotionActive(Potion.getPotionById(1))) {
            Minecraft.player.speedInAir = 0.06F;
         } else {
            Minecraft.player.speedInAir = 0.05F;
         }

         if (Minecraft.player.onGround) {
            Minecraft.player.jump();
            if (Minecraft.player.isPotionActive(Potion.getPotionById(1))) {
               Minecraft.player.jump();
            }

            Minecraft.player.motionY /= 1.05;
         } else {
            if (!Minecraft.player.isCollidedHorizontally) {
               Minecraft.player.motionY--;
            }

            if (Minecraft.player.isPotionActive(Potion.getPotionById(1))) {
               speed = 0.45;
            } else {
               speed = 0.32;
            }
         }

         MoveMeHelp.setSpeed(speed);
      } else if (Minecraft.player.speedInAir == 0.06F || Minecraft.player.speedInAir == 0.05F) {
         Minecraft.player.speedInAir = 0.02F;
      }

      if (EntityLivingBase.isNcpDamaged && !Minecraft.player.onGround && !Minecraft.player.isInWeb && (MoveMeHelp.isMoving() || TargetStrafe.goStrafe())) {
         speed = 0.55F;
      }

      if (speed != 0.0 && !TargetStrafe.goStrafe()) {
         MoveMeHelp.setCuttingSpeed(speed / 1.06);
      }

      ncpSpeed = (float)speed;
      if (iceSpeed) {
         if (mc.world.getBlockState(new BlockPos(x, y - 0.51, z)).getBlock() != Block.getBlockById(212)
            && mc.world.getBlockState(new BlockPos(x, y - 0.51, z)).getBlock() != Block.getBlockById(79)
            && mc.world.getBlockState(new BlockPos(x, y - 0.51, z)).getBlock() != Block.getBlockById(174)
            && mc.world.getBlockState(new BlockPos(x, y - 0.95, z)).getBlock() != Block.getBlockById(212)
            && mc.world.getBlockState(new BlockPos(x, y - 0.95, z)).getBlock() != Block.getBlockById(79)
            && mc.world.getBlockState(new BlockPos(x, y - 0.95, z)).getBlock() != Block.getBlockById(174)) {
            if (this.ncpIceTimer.hasReached(800.0)) {
               iceGo = false;
            }
         } else if (!BlockUtils.getBlockWithExpand(0.3, BlockUtils.getEntityBlockPos(Minecraft.player), Blocks.WATER)
            && !BlockUtils.getBlockWithExpand(0.3, BlockUtils.getEntityBlockPos(Minecraft.player), Blocks.LAVA)) {
            this.ncpIceTimer.reset();
            iceGo = true;
         }
      } else {
         iceGo = false;
      }

      if (iceSpeed && iceGo) {
         boolean isSpeedPot2 = Minecraft.player.isPotionActive(MobEffects.SPEED)
            && Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() >= 1;
         if (Minecraft.player.isJumping) {
            if (mc.gameSettings.keyBindForward.isKeyDown()) {
               MoveMeHelp.setSpeed(isSpeedPot2 ? 0.879 : 0.623);
            } else {
               MoveMeHelp.setSpeed(isSpeedPot2 ? 0.91 : 0.63);
            }
         } else {
            MoveMeHelp.setSpeed(isSpeedPot2 ? 0.9685 : 0.687);
         }
      }
   }

   private void forNCPoff(boolean timer) {
      if (Minecraft.player.speedInAir == 0.06F || Minecraft.player.speedInAir == 0.05F) {
         Minecraft.player.speedInAir = 0.02F;
      }

      ncpSpeed = 0.0F;
      iceGo = false;
   }

   private void forGuardian() {
      if ((!Strafe.get.actived || !Strafe.get.Mode.currentMode.equalsIgnoreCase("Matrix5") || !Strafe.moves()) && EntityLivingBase.isSunRiseDamaged) {
         if (MoveMeHelp.moveKeysPressed()) {
            double speed = MathUtils.clamp(MoveMeHelp.getSpeed(), 0.2499, 9.9);
            if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
               speed *= 1.8;
            } else if (Minecraft.player.isJumping()) {
               speed *= canMatrixBoost() ? 1.8 : 1.0;
            }

            speed = MathUtils.clamp(speed, 0.2499, 1.17455998);
            MoveMeHelp.setSpeed(speed);
            MoveMeHelp.setCuttingSpeed(speed / 1.06);
         }
      } else {
         this.forGuardianTimer.reset();
      }
   }

   private void forRipServer() {
      if (this.forRipServerTimer.hasReached(10.0)
         && (
            !ElytraBoost.get.actived
               || !ElytraBoost.get.currentMode("Mode").equalsIgnoreCase("MatrixFly") && !ElytraBoost.get.currentMode("Mode").equalsIgnoreCase("MatrixSpeed")
               || !ElytraBoost.canElytra()
         )) {
         if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
            MoveMeHelp.setSpeed(
               MathUtils.clamp(
                  MoveMeHelp.getSpeed() * (Minecraft.player.rayGround ? 1.8 : 0.8), 0.2, MoveMeHelp.w() && Minecraft.player.isSprinting() ? 1.7155F : 1.745F
               )
            );
            Minecraft.player.rayGround = Minecraft.player.onGround;
         } else {
            Minecraft.player.serverSprintState = true;
            MoveMeHelp.setSpeed(
               MathUtils.clamp(MoveMeHelp.getSpeed() * (!Minecraft.player.onGround && !Minecraft.player.rayGround ? 1.2 : 1.0), 0.195, 1.823585F), 0.12F
            );
            Minecraft.player.rayGround = Minecraft.player.onGround;
         }

         this.forRipServerTimer.reset();
      }
   }

   private void forRipServerMove() {
      if (MoveMeHelp.isMoving()) {
         MoveMeHelp.setCuttingSpeed(MoveMeHelp.getCuttingSpeed() / 1.06F);
      }
   }

   private void forGrim() {
      if (Minecraft.player.onGround || Minecraft.player.motionY < 0.0 && !Minecraft.player.onGround) {
         Timer.forceTimer(Minecraft.player.isJumping() ? 1.02F : 1.015F);
      }

      Minecraft.player.rotationYaw = Minecraft.player.rotationYaw + (Minecraft.player.ticksExisted % 2 == 0 ? -0.25F : 0.25F);
      if (Minecraft.player.ticksExisted % 2 == 0 && Minecraft.player.fallDistance != 0.0F) {
         Minecraft.player.motionY -= 0.003F;
      }

      Minecraft.player.motionX = Minecraft.player.motionX * (Minecraft.player.onGround && !Minecraft.player.isJumping() ? 1.02844F : 1.002446F);
      Minecraft.player.motionZ = Minecraft.player.motionZ * (Minecraft.player.onGround && !Minecraft.player.isJumping() ? 1.02844F : 1.002446F);
   }

   private void forVanillaAir() {
      if (!Minecraft.player.isJumping) {
         Minecraft.player.onGround = false;
      }

      if (Minecraft.player.isJumping && Minecraft.player.onGround) {
         Minecraft.player.motionY = 0.42F;
      }

      Minecraft.player.speedInAir = this.SpeedF.getFloat();
      if (!Minecraft.player.onGround) {
         Minecraft.player.motionX /= 5.0;
         Minecraft.player.motionZ /= 5.0;
      }
   }
}
