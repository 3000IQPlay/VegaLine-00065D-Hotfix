package ru.govno.client.module.modules;

import net.minecraft.block.BlockWeb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.Event3D;
import ru.govno.client.event.events.EventAction;
import ru.govno.client.event.events.EventMove2;
import ru.govno.client.event.events.EventPostMove;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Movement.MatrixStrafeMovement;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Render.RenderUtils;

public class TargetStrafe extends Module {
   public static TargetStrafe get;
   public static int b = 1;
   private static double speed = 0.23F;
   public static boolean needSprintState;
   public FloatSettings Distance;
   public FloatSettings SpeedF;
   public FloatSettings DamageSpeed;
   public BoolSettings SmartSpeed;
   public BoolSettings DamageBoost;
   public BoolSettings AutoJump;
   public BoolSettings RenderCurrentDist;
   public BoolSettings SmartReverse;
   static EntityLivingBase target = null;

   public TargetStrafe() {
      super("TargetStrafe", 0, Module.Category.MOVEMENT);
      get = this;
      this.settings.add(this.Distance = new FloatSettings("Distance", 2.0F, 12.0F, 1.0F, this));
      this.settings.add(this.SpeedF = new FloatSettings("Speed", 0.24F, 1.0F, 0.0F, this, () -> !this.SmartSpeed.getBool()));
      this.settings.add(this.SmartSpeed = new BoolSettings("SmartSpeed", true, this));
      this.settings.add(this.DamageBoost = new BoolSettings("DamageBoost", false, this));
      this.settings.add(this.DamageSpeed = new FloatSettings("DamageSpeed", 0.6F, 2.0F, 0.0F, this, () -> this.DamageBoost.getBool()));
      this.settings.add(this.AutoJump = new BoolSettings("AutoJump", true, this));
      this.settings.add(this.RenderCurrentDist = new BoolSettings("RenderCurrentDist", true, this));
      this.settings.add(this.SmartReverse = new BoolSettings("SmartReverse", true, this));
   }

   boolean onCanReverseBecauseChecks(Entity ent) {
      double dx = Minecraft.player.posX - Minecraft.player.lastTickPosX;
      double dz = Minecraft.player.posZ - Minecraft.player.lastTickPosZ;
      if (ent != null && (double)((int)ent.posY) >= Minecraft.player.posY - 1.2) {
         double downPadding = 5.0;
         AxisAlignedBB seflAABBDown = Minecraft.player.boundingBox.offsetMinDown(downPadding);
         double selfSpeedOffsetBox = 1.0;
         if (mc.world != null
            && mc.world.getCollisionBoxes(Minecraft.player, seflAABBDown.addExpandXZ(-0.25)).isEmpty()
            && !mc.world.getCollisionBoxes(Minecraft.player, seflAABBDown.addExpandXZ(selfSpeedOffsetBox)).isEmpty()) {
            return this.SmartReverse.getBool();
         }
      }

      if (!Minecraft.player.isInWeb) {
         for (double minOffsetXZMul = 1.0; minOffsetXZMul < 3.0; minOffsetXZMul += 0.25) {
            double dxT = -dx * minOffsetXZMul;
            double dzT = -dz * minOffsetXZMul;
            BlockPos predictPos = new BlockPos(Minecraft.player.posX - dxT, Minecraft.player.posY, Minecraft.player.posZ - dzT);
            if (mc.world != null
               && (
                  mc.world.getBlockState(predictPos).getBlock() instanceof BlockWeb || mc.world.getBlockState(predictPos.down()).getBlock() instanceof BlockWeb
               )) {
               return this.SmartReverse.getBool();
            }
         }
      }

      if (!Minecraft.player.isInLava() && !Minecraft.player.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
         for (double minOffsetXZMulx = 1.0; minOffsetXZMulx < 3.0; minOffsetXZMulx += 0.25) {
            double dxT = -dx * minOffsetXZMulx;
            double dzT = -dz * minOffsetXZMulx;
            BlockPos predictPos = new BlockPos(Minecraft.player.posX - dxT, Minecraft.player.posY, Minecraft.player.posZ - dzT);
            if (mc.world != null
               && (
                  mc.world.getBlockState(predictPos).getBlock() == Blocks.LAVA
                     || mc.world.getBlockState(predictPos.down()).getBlock() == Blocks.LAVA
                     || mc.world.getBlockState(predictPos).getBlock() == Blocks.FIRE
                     || mc.world.getBlockState(predictPos.down()).getBlock() == Blocks.FIRE
               )) {
               return this.SmartReverse.getBool();
            }
         }
      }

      return false;
   }

   float[] getRotations(Entity ent) {
      double x = RenderUtils.interpolate(ent.posX, ent.lastTickPosX, (double)mc.getRenderPartialTicks());
      double y = RenderUtils.interpolate(ent.posY, ent.lastTickPosY, (double)mc.getRenderPartialTicks()) * (double)ent.getEyeHeight();
      double z = RenderUtils.interpolate(ent.posZ, ent.lastTickPosZ, (double)mc.getRenderPartialTicks());
      return this.getRotationFromPosition(x, z, y);
   }

   float[] getRotationFromPosition(double x, double z, double y) {
      double xDiff = x - Minecraft.player.posX;
      double zDiff = z - Minecraft.player.posZ;
      double yDiff = y - Minecraft.player.posY - 1.8;
      double dist = (double)MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff);
      float yaw = (float)(Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-(Math.atan2(yDiff, dist) * 180.0 / Math.PI));
      return new float[]{yaw, pitch};
   }

   private static float getMaxRange() {
      return 20.0F;
   }

   private static boolean isSmartKeep() {
      return true;
   }

   private static int keepPercent100() {
      return 45;
   }

   void Motion(double d, float f, double d2, double d3, boolean onMove, boolean smartKeep) {
      double d4 = d3;
      double d5 = d2;
      float keep = 90.0F - (float)keepPercent100() * 0.9F;
      float dst = Minecraft.player.getSmoothDistanceToEntityXZ(target);
      float cdst = this.Distance.getFloat();
      float rng = getMaxRange();
      if (smartKeep) {
         float pcDel = (float)MoveMeHelp.getCuttingSpeed() / 0.65F;
         cdst = (float)((double)cdst - MoveMeHelp.getCuttingSpeed() / 10.0);
         keep = MathUtils.clamp(1.0F - (float)MathUtils.getDifferenceOf(dst, cdst), 0.0F, 1.0F) * 90.0F;
      }

      float f2 = f;
      if (d3 != 0.0 || d2 != 0.0) {
         if (d3 != 0.0) {
            if (d2 > 0.0) {
               f2 = f + (d3 > 0.0 ? -keep : keep);
            } else if (d2 < 0.0) {
               f2 = f + (d3 > 0.0 ? keep : -keep);
            }

            d5 = 0.0;
            if (d3 > 0.0) {
               d4 = 1.0;
            } else if (d3 < 0.0) {
               d4 = -1.0;
            }
         }

         double d6 = Math.cos(Math.toRadians((double)(f2 + 93.5F)));
         double d7 = Math.sin(Math.toRadians((double)(f2 + 93.5F)));
         if (onMove) {
            Entity.motionx = (d4 * d * d6 + d5 * d * d7) / 1.06;
            Entity.motionz = (d4 * d * d7 - d5 * d * d6) / 1.06;
         } else {
            Minecraft.player.motionX = d4 * d * d6 + d5 * d * d7;
            Minecraft.player.motionZ = d4 * d * d7 - d5 * d * d6;
         }
      }
   }

   void Motion2(EventMove2 move, double d, float f, double d2, double d3, boolean smartKeep) {
      double d4 = d3;
      double d5 = d2;
      float keep = 90.0F - (float)keepPercent100() * 0.9F;
      float dst = Minecraft.player.getSmoothDistanceToEntityXZ(target);
      float cdst = this.Distance.getFloat();
      float rng = getMaxRange();
      if (smartKeep) {
         cdst = (float)((double)cdst - MoveMeHelp.getCuttingSpeed());
         double dstPardon = 1.0 + MoveMeHelp.getCuttingSpeed() / 9.953;
         keep = (float)MathUtils.clamp(dstPardon - MathUtils.getDifferenceOf(dst, cdst), 0.0, 1.0) * 90.0F;
      }

      float f2 = f;
      if (d3 == 0.0 && d2 == 0.0) {
         MatrixStrafeMovement.oldSpeed = 0.0;
         move.motion().xCoord = 0.0;
         move.motion().zCoord = 0.0;
      } else {
         if (d3 != 0.0) {
            if (d2 > 0.0) {
               f2 = f + (d3 > 0.0 ? -keep : keep);
            } else if (d2 < 0.0) {
               f2 = f + (d3 > 0.0 ? keep : -keep);
            }

            d5 = 0.0;
            if (d3 > 0.0) {
               d4 = 1.0;
            } else if (d3 < 0.0) {
               d4 = -1.0;
            }
         }

         double d6 = Math.cos(Math.toRadians((double)(f2 + 90.0F)));
         double d7 = Math.sin(Math.toRadians((double)(f2 + 90.0F)));
         move.motion().xCoord = d4 * d * d6 + d5 * d * d7;
         move.motion().zCoord = d4 * d * d7 - d5 * d * d6;
      }
   }

   static double getCurrentSpeed() {
      if (Minecraft.player == null) {
         return 0.0;
      } else {
         TargetStrafe targetStrafe = get;
         double speed1 = (double)targetStrafe.SpeedF.getFloat();
         if (Minecraft.player.hurtTime != 0 && targetStrafe.DamageBoost.getBool()) {
            speed1 = (double)targetStrafe.DamageSpeed.getFloat();
         }

         if (targetStrafe.SmartSpeed.getBool()) {
            speed1 = speed;
            if (Speed.get.actived
               && (
                  Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Guardian") && EntityLivingBase.isSunRiseDamaged
                     || Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Matrix") && Speed.get.DamageBoost.getBool() && EntityLivingBase.isMatrixDamaged
               )) {
               if (Minecraft.player.onGround) {
                  speed1 *= speed < 0.62 ? 1.64 : 1.53;
               } else if (Minecraft.player.isJumping()
                  && Speed.get.actived
                  && Speed.get.AntiCheat.getMode().equalsIgnoreCase("Guardian")
                  && (!targetStrafe.DamageBoost.getBool() || !EntityLivingBase.isMatrixDamaged)) {
                  speed1 *= Minecraft.player.fallDistance == 0.0F
                     ? 1.001
                     : (
                        !Speed.canMatrixBoost()
                              || Minecraft.player.isHandActive()
                              || !(speed < 0.4) && (!((double)Minecraft.player.fallDistance > 0.65) || !(speed < 0.6))
                           ? 1.0
                           : 1.9
                     );
               }

               speed1 = MathUtils.clamp(
                  speed1,
                  0.2499 - (Minecraft.player.ticksExisted % 2 == 0 ? 5.0E-7 : 0.0),
                  1.17455998 - (Minecraft.player.ticksExisted % 2 == 0 ? 1.0E-7 : 0.0)
               );
            }

            boolean elytra = ElytraBoost.get.actived
               && (ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixSpeed2") || ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixFly3"))
               && ElytraBoost.canElytra();
            if (elytra) {
               if (ElytraBoost.flSpeed > speed) {
                  speed1 = ElytraBoost.flSpeed / 1.011;
               }
            } else if (Speed.get.actived
               && (
                  Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Guardian") && EntityLivingBase.isSunRiseDamaged
                     || Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Matrix") && Speed.get.DamageBoost.getBool() && EntityLivingBase.isMatrixDamaged
               )) {
               if (Minecraft.player.onGround) {
                  speed1 *= speed1 < 0.62 ? 1.64 : 1.528;
               } else if (Minecraft.player.isJumping() && Speed.get.actived && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Guardian")) {
                  speed1 *= Minecraft.player.fallDistance == 0.0F
                     ? 1.001
                     : (
                        !Speed.canMatrixBoost()
                              || Minecraft.player.isHandActive()
                              || !(speed1 < 0.4) && (!((double)Minecraft.player.fallDistance > 0.65) || !(speed1 < 0.6))
                           ? 1.0
                           : 1.9
                     );
               }

               speed1 = MathUtils.clamp(speed1, 0.2499 - (Minecraft.player.ticksExisted % 2 == 0 ? 5.0E-7 : 0.0), 1.3);
            }

            if (Speed.get.actived
               && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("NCP")
               && (double)Speed.ncpSpeed > speed1
               && Speed.get.DamageBoost.getBool()) {
               speed1 = (double)Speed.ncpSpeed;
            }

            if (elytra && ElytraBoost.flSpeed > speed1) {
               speed1 = ElytraBoost.flSpeed;
            }

            if (Speed.iceGo) {
               speed1 = (double)((float)(Minecraft.player.isPotionActive(Potion.getPotionById(1)) ? 0.91 : 0.63) * 1.07F);
            }

            if (WaterSpeed.get.actived && WaterSpeed.get.Mode.getMode().equalsIgnoreCase("Matrix") && WaterSpeed.speedInWater / 1.061 > speed1) {
               speed1 = WaterSpeed.speedInWater / 1.061;
            }

            if (Minecraft.player.isElytraFlying() && (double)EntityLivingBase.getElytraSpeed > speed1) {
               speed1 = (double)EntityLivingBase.getElytraSpeed;
            }

            if (Fly.get.actived && MathUtils.clamp(Fly.flySpeed, 0.195F, 1.2F) > speed1) {
               speed1 = MathUtils.clamp(Fly.flySpeed, 0.195F, 1.2F);
            }
         }

         if (Speed.get.actived
            && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Vulcan")
            && speed > 0.1
            && Minecraft.player.onGround
            && Minecraft.player.ticksExisted % 3 == 0) {
            Minecraft.player.motionY = 0.0391;
         }

         if (JesusSpeed.get.actived && JesusSpeed.isJesused) {
            if ((double)Minecraft.player.fallDistance > 0.02) {
               Enchantment depth = Enchantments.DEPTH_STRIDER;
               int depthLvl = EnchantmentHelper.getEnchantmentLevel(depth, Minecraft.player.inventory.armorItemInSlot(0));
               boolean isSpeedPot = Minecraft.player.isPotionActive(MobEffects.SPEED)
                  && Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() > 0;
               speed1 = 1.16F;
               if (isSpeedPot && depthLvl > 0) {
                  speed1 += 0.443;
               }
            } else {
               speed1 = 0.12;
            }
         }

         if (JesusSpeed.get.actived && JesusSpeed.get.JesusMode.currentMode.equalsIgnoreCase("Matrix7.0.2") && JesusSpeed.ticksLinked > 1) {
            speed1 = (double)JesusSpeed.getM7Speed();
         }

         if (Minecraft.player.isInWeb) {
            speed1 /= 2.0;
         }

         return speed1;
      }
   }

   void targetStrafeElement() {
      if (Minecraft.player.isInWater()) {
         mc.gameSettings.keyBindJump.pressed = true;
      } else if (mc.gameSettings.keyBindJump.pressed && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
         mc.gameSettings.keyBindJump.pressed = false;
      }

      Minecraft.player.jumpMovementFactor = 0.0F;
      if (Minecraft.player.onGround
         && !Minecraft.player.isInWater()
         && !Minecraft.player.isInLava()
         && !Minecraft.player.isInWeb
         && this.AutoJump.getBool()
         && !Minecraft.player.isJumping()) {
         Minecraft.player.jump();
      }

      if (Minecraft.player.isCollidedHorizontally && Minecraft.player.ticksExisted % 2 == 0) {
         b = -b;
      }

      b = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) ? 1 : (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()) ? -1 : b);
   }

   public static final boolean goStrafe() {
      return target != null && !LongJump.get.actived && !PearlFlight.go ? get.actived : false;
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByDouble(getCurrentSpeed());
   }

   @Override
   public void onUpdate() {
      target = HitAura.TARGET_ROTS != null
         ? HitAura.TARGET_ROTS
         : (mc.world.getLoadedEntityList().stream().anyMatch(entity -> entity == target) ? target : null);
      if (target != null
         && (!HitAura.get.actived || target != null && target.getHealth() == 0.0F || Minecraft.player.getSmoothDistanceToEntity(target) > getMaxRange())) {
         target = null;
      }

      if (!goStrafe() && mc.currentScreen == null) {
         mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
      }

      if (goStrafe()) {
         this.targetStrafeElement();
         if (target != null && this.onCanReverseBecauseChecks(target)) {
            b = -b;
         }
      }

      if (target == null) {
         target = null;
      }
   }

   @Override
   public void onMovement() {
      if (goStrafe()) {
         this.targetStrafeElement();
         if (!this.SmartSpeed.getBool()) {
            this.getStrafe(getCurrentSpeed(), Minecraft.player.getSmoothDistanceToEntityXZ(target), this.Distance.getFloat(), true, isSmartKeep());
         }
      }
   }

   @EventTarget
   public void onMovementHui(EventPostMove move) {
      if (this.SmartSpeed.getBool() && this.actived) {
         MatrixStrafeMovement.postMove(move.getHorizontalMove());
      }
   }

   @EventTarget
   public void onMovementHui2(EventAction move) {
      if (goStrafe() && this.SmartSpeed.getBool() && this.actived && !HitAura.get.noRotateTick) {
         MatrixStrafeMovement.actionEvent(move);
      }
   }

   @EventTarget
   public void onMovements(EventMove2 move) {
      if (this.actived) {
         boolean canBoost = false;
         double x = Minecraft.player.posX;
         double y = Minecraft.player.posY;
         double z = Minecraft.player.posZ;
         if (!mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.addCoord(0.0, -0.09, 0.0)).isEmpty()) {
            canBoost = true;
         }

         boolean matrixSpeedDamageHop = Speed.get.actived
            && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Matrix")
            && Speed.get.StrafeDamageHop.getBool()
            && EntityLivingBase.isMatrixDamaged;
         speed = MatrixStrafeMovement.calculateSpeed(
            matrixSpeedDamageHop && Minecraft.player.fallDistance > 0.0F || Strafe.get.Mode.currentMode.equalsIgnoreCase("Strict"),
            move,
            matrixSpeedDamageHop || EntityLivingBase.isMatrixDamaged && this.DamageBoost.getBool() && canBoost,
            matrixSpeedDamageHop ? 0.15 : (double)this.DamageSpeed.getFloat()
         );
         if (EntityLivingBase.isMatrixDamaged && canBoost) {
            speed = MathUtils.clamp(speed, speed, 2.9F);
         }

         boolean elytra = ElytraBoost.get.actived
            && (ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixSpeed2") || ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixFly3"))
            && ElytraBoost.canElytra();
         if (elytra && ElytraBoost.flSpeed > speed) {
            speed = ElytraBoost.flSpeed;
         }

         if (Speed.iceGo) {
            speed = (double)((float)(Minecraft.player.isPotionActive(Potion.getPotionById(1)) ? 0.91 : 0.63) * 1.07F);
         }

         if (WaterSpeed.get.actived && WaterSpeed.get.Mode.getMode().equalsIgnoreCase("Matrix") && WaterSpeed.speedInWater / 1.06 > speed) {
            speed = WaterSpeed.speedInWater / 1.06;
         }

         if (Minecraft.player.isElytraFlying() && (double)EntityLivingBase.getElytraSpeed > speed) {
            speed = (double)EntityLivingBase.getElytraSpeed;
         }

         if (Fly.get.actived && Fly.flySpeed > speed) {
            speed = Fly.flySpeed / 5.0;
         }

         if (Minecraft.player.isHandActive() && !Minecraft.player.isJumping() && Minecraft.player.onGround) {
         }

         if (JesusSpeed.get.actived && JesusSpeed.get.JesusMode.currentMode.equalsIgnoreCase("Matrix7.0.2") && JesusSpeed.ticksLinked > 0) {
            speed = (double)JesusSpeed.getM7Speed();
         }

         double finalSpeed = getCurrentSpeed();
         if (goStrafe() && this.SmartSpeed.getBool() && this.actived) {
            float dst = Minecraft.player.getSmoothDistanceToEntityXZ(target);
            float current = this.Distance.getFloat();
            float pardon = 0.05F;
            int feya = dst > current + pardon ? 1 : (dst < current - pardon ? -1 : 0);
            float yaw = this.getRotations(target)[0];
            this.Motion2(move, finalSpeed, yaw, (double)b, (double)feya, isSmartKeep());
         }
      }
   }

   void getStrafe(double speed, float getDist, float dist, boolean onMove, boolean smartKeep) {
      float pardon = 0.05F;
      int feya = getDist > dist + pardon ? 1 : (getDist < dist - pardon ? -1 : 0);
      float yaw = this.getRotations(target)[0];
      this.Motion(speed, yaw, (double)b, (double)feya, onMove, smartKeep);
   }

   @EventTarget
   public void onRender3D(Event3D event) {
      if (target != null && Minecraft.player.getSmoothDistanceToEntityXZ(target) <= getMaxRange() && this.RenderCurrentDist.getBool()) {
         float xzDistance = this.Distance.getFloat();
         float sataDistance = Minecraft.player.getSmoothDistanceToEntityXZ(target) / 2.0F;
         double eX = target.lastTickPosX + (target.posX - target.lastTickPosX) * (double)event.getPartialTicks();
         double eY = target.lastTickPosY + (target.posY - target.lastTickPosY) * (double)event.getPartialTicks();
         double eZ = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double)event.getPartialTicks();
         double meX = Minecraft.player.lastTickPosX + (Minecraft.player.posX - Minecraft.player.lastTickPosX) * (double)event.getPartialTicks();
         double meY = Minecraft.player.lastTickPosY + (Minecraft.player.posY - Minecraft.player.lastTickPosY) * (double)event.getPartialTicks();
         double meZ = Minecraft.player.lastTickPosZ + (Minecraft.player.posZ - Minecraft.player.lastTickPosZ) * (double)event.getPartialTicks();
         Vec3d overSataVec = BlockUtils.getOverallVec3d(new Vec3d(eX, eY, eZ), new Vec3d(meX, meY, meZ), 0.5F);
         double sataX = overSataVec.xCoord;
         double sataY = overSataVec.yCoord;
         double sataZ = overSataVec.zCoord;
         double glX = RenderManager.viewerPosX;
         double glY = RenderManager.viewerPosY;
         double glZ = RenderManager.viewerPosZ;
         boolean bloom = false;
         GL11.glPushMatrix();
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            bloom ? GlStateManager.DestFactor.ONE : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         mc.entityRenderer.disableLightmap();
         GL11.glEnable(3042);
         GL11.glEnable(2832);
         GL11.glLineWidth(1.0F);
         GL11.glDisable(3553);
         GL11.glDisable(2929);
         GL11.glDisable(2896);
         GL11.glShadeModel(7425);
         GL11.glTranslated(-glX, -glY, -glZ);
         GL11.glBegin(3);

         for (int i = 0; i <= 360; i += 6) {
            double r = (double)sataDistance;
            double x = sataX - Math.sin(Math.toRadians((double)i)) * r;
            double z = sataZ + Math.cos(Math.toRadians((double)i)) * r;
            int c = ClientColors.getColor1((int)((float)i * 3.0F));
            RenderUtils.glColor(c);
            GL11.glVertex3d(x, sataY, z);
         }

         GL11.glEnd();
         GL11.glBegin(3);

         for (int i = 0; i <= 360; i += 6) {
            double r = (double)(sataDistance * 2.0F);
            double x = eX - Math.sin(Math.toRadians((double)i)) * r;
            double z = eZ + Math.cos(Math.toRadians((double)i)) * r;
            int c = ClientColors.getColor1((int)((float)i * 3.0F), 0.333333F);
            RenderUtils.glColor(c);
            GL11.glVertex3d(x, sataY, z);
         }

         GL11.glEnd();
         GL11.glBegin(3);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glVertex3d(eX, sataY, eZ);
         GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glVertex3d(eX - (eX - meX) / 2.0, sataY, eZ - (eZ - meZ) / 2.0);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glVertex3d(meX, sataY, meZ);
         GL11.glEnd();
         GL11.glPointSize(8.0F);
         GL11.glBegin(0);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glVertex3d(eX, sataY, eZ);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glVertex3d(meX, sataY, meZ);
         GL11.glEnd();
         GL11.glPointSize(1.0F);
         double yaw = Math.toDegrees(Math.atan2(Minecraft.player.posZ - Minecraft.player.prevPosZ, Minecraft.player.posX - Minecraft.player.prevPosX));
         yaw = yaw < 0.0 ? yaw + 360.0 : yaw;
         yaw -= (yaw - yaw) * (double)event.getPartialTicks();
         double dx = Minecraft.player.posX - Minecraft.player.prevPosX;
         double dz = Minecraft.player.posZ - Minecraft.player.prevPosZ;
         double antsRange = Math.sqrt(dx * dx + dz * dz);
         GL11.glBegin(3);
         GL11.glVertex3d(meX, sataY, meZ);
         GL11.glVertex3d(meX - Math.sin(Math.toRadians(yaw - 90.0)) * antsRange, sataY, meZ + Math.cos(Math.toRadians(yaw - 90.0)) * antsRange);
         GL11.glEnd();
         GL11.glPointSize(8.0F);
         GL11.glBegin(0);
         GL11.glVertex3d(meX - Math.sin(Math.toRadians(yaw - 90.0)) * antsRange, sataY, meZ + Math.cos(Math.toRadians(yaw - 90.0)) * antsRange);
         GL11.glEnd();
         yaw = Math.toDegrees(Math.atan2(target.posZ - Minecraft.player.prevPosZ, target.posX - target.prevPosX));
         yaw = yaw < 0.0 ? yaw + 360.0 : yaw;
         yaw -= (yaw - yaw) * (double)event.getPartialTicks();
         dx = target.posX - target.prevPosX;
         dz = target.posZ - target.prevPosZ;
         antsRange = Math.sqrt(dx * dx + dz * dz);
         GL11.glBegin(3);
         GL11.glVertex3d(eX, sataY, eZ);
         GL11.glVertex3d(eX - Math.sin(Math.toRadians(yaw - 90.0)) * antsRange, sataY, eZ + Math.cos(Math.toRadians(yaw - 90.0)) * antsRange);
         GL11.glEnd();
         GL11.glPointSize(8.0F);
         GL11.glBegin(0);
         GL11.glVertex3d(eX - Math.sin(Math.toRadians(yaw - 90.0)) * antsRange, sataY, eZ + Math.cos(Math.toRadians(yaw - 90.0)) * antsRange);
         GL11.glEnd();
         GL11.glPointSize(1.0F);
         GL11.glTranslated(glX, glY, glZ);
         GL11.glLineWidth(1.0F);
         GL11.glShadeModel(7424);
         GL11.glEnable(3553);
         GL11.glEnable(2929);
         GlStateManager.enableAlpha();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
         );
         GlStateManager.resetColor();
         GL11.glPopMatrix();
      }
   }
}
