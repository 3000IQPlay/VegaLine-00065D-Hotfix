package ru.govno.client.module.modules;

import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventAction;
import ru.govno.client.event.events.EventMove2;
import ru.govno.client.event.events.EventMoveKeys;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventPostMove;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.event.events.EventSprintBlock;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Wrapper;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MatrixStrafeMovement;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class Strafe extends Module {
   public static Strafe get;
   public ModeSettings Mode;
   public BoolSettings TimerBoost;
   public BoolSettings PullDown;
   public BoolSettings DoNcpMin;
   public BoolSettings HeadYawMoveDir;
   public BoolSettings CollideBoost;
   private final TimerHelper noCollideTime = new TimerHelper();
   float moveYaw = -1.2312312E8F;
   float moveYawPrev;
   public static boolean needSprintState;
   public static float speed;
   public static float curSpeed = 0.0F;
   boolean doUps = false;
   float prevStrafeYaw;
   float strafeLimitBound;
   boolean m;
   boolean w;
   boolean a;
   boolean s;
   boolean d;
   boolean canGrBoost = false;
   double sp = 0.0;

   public Strafe() {
      super("Strafe", 0, Module.Category.MOVEMENT);
      this.settings
         .add(
            this.Mode = new ModeSettings(
               "Mode",
               "Matrix",
               this,
               new String[]{"Matrix", "Matrix2", "Matrix3", "Matrix4", "Matrix5", "Guardian", "NCP", "NCP2", "Strict", "Grim", "Matrix&AAC"}
            )
         );
      this.settings.add(this.TimerBoost = new BoolSettings("TimerBoost", true, this));
      this.settings
         .add(
            this.PullDown = new BoolSettings(
               "PullDown", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("NCP") || this.Mode.currentMode.equalsIgnoreCase("NCP2")
            )
         );
      this.settings.add(this.DoNcpMin = new BoolSettings("DoNcpMin", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Strict")));
      this.settings.add(this.HeadYawMoveDir = new BoolSettings("HeadYawMoveDir", true, this, () -> !this.Mode.currentMode.equalsIgnoreCase("Grim")));
      this.settings
         .add(
            this.CollideBoost = new BoolSettings(
               "CollideBoost", false, this, () -> this.Mode.currentMode.equalsIgnoreCase("Grim") || this.Mode.currentMode.equalsIgnoreCase("Matrix&AAC")
            )
         );
      get = this;
   }

   private boolean canRotate() {
      if (ThrowFollow.get.runTicks == 1 || Minecraft.player.isRiding()) {
         return false;
      } else if (MiddleClick.get.callThrowPearl
         || MiddleClick.get.callThrowPearl2
         || HitAura.get.canRotateUpdated
         || PotionThrower.get.callThrowPotions
         || PotionThrower.get.forceThrow) {
         return false;
      } else if (HitAura.get.canRotateUpdated) {
         return false;
      } else {
         ItemStack stackInHand = Minecraft.player.getHeldItemMainhand();
         if (stackInHand != null) {
            Item item = stackInHand.getItem();
            if (item instanceof ItemEnderPearl
               || item instanceof ItemBow
               || item instanceof ItemSplashPotion
               || item instanceof ItemLingeringPotion
               || item instanceof ItemFishingRod) {
               return false;
            }
         }

         return this.Mode.currentMode.equalsIgnoreCase("Grim") || this.Mode.currentMode.equalsIgnoreCase("Matrix&AAC");
      }
   }

   @EventTarget
   public void onSilentSideStrafe(EventRotationStrafe event) {
      if (this.actived && this.canRotate()) {
         event.setYaw(MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
      }
   }

   @EventTarget
   public void onMoveKeysPress(EventMoveKeys event) {
      if (this.actived
         && this.canRotate()
         && (
            (event.isForwardKeyDown() || event.isBackKeyDown()) && event.isForwardKeyDown() != event.isBackKeyDown()
               || (event.isRightKeyDown() || event.isLeftKeyDown()) && event.isRightKeyDown() != event.isLeftKeyDown()
         )) {
         event.setForwardKeyDown(true);
         event.setBackKeyDown(false);
         event.setLeftKeyDown(false);
         event.setRightKeyDown(false);
      }
   }

   @EventTarget
   public void onSilentSideJump(EventRotationJump event) {
      if (this.actived && this.canRotate()) {
         event.setYaw(MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
      }
   }

   @EventTarget
   public void onPlayerUpdate(EventPlayerMotionUpdate event) {
      if (this.actived) {
         if (this.canRotate()) {
            if (MoveMeHelp.isMoving() && (!MoveMeHelp.w() || MoveMeHelp.a() || MoveMeHelp.s() || MoveMeHelp.d())) {
               this.moveYawPrev = this.moveYaw;
               event.setYaw(MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
               if (this.Mode.currentMode.equalsIgnoreCase("Grim") || this.HeadYawMoveDir.getBool()) {
                  Minecraft.player.rotationYawHead = event.getYaw();
                  Minecraft.player.renderYawOffset = RotationUtil.getAngleDifference(event.getYaw(), Minecraft.player.rotationYaw) > 90.0F
                     ? event.getYaw()
                     : RotationUtil.calcYawOffset(event.getYaw());
               }
            } else if (this.moveYaw != -1.2312312E8F) {
               this.moveYaw = -1.2312312E8F;
            }
         } else {
            if (this.HeadYawMoveDir.getBool() && MoveMeHelp.isMoving() && this.canRotate()) {
               Minecraft.player.rotationYawHead = MoveMeHelp.moveYaw(Minecraft.player.rotationYaw);
               Minecraft.player.renderYawOffset = Minecraft.player.rotationYawHead;
            }

            if (this.moveYaw != -1.2312312E8F) {
               this.moveYaw = -1.2312312E8F;
            }
         }
      }
   }

   @EventTarget
   public void onSprintBlock(EventSprintBlock event) {
      if (this.actived && this.canRotate() && this.moveYaw != Minecraft.player.rotationYaw && MoveMeHelp.getSpeed() > 0.04) {
         event.setCancelled(true);
      }
   }

   @EventTarget
   public void onPacket(EventReceivePacket event) {
      if (Bypass.get.getIsStrafeHacked()) {
         if ((this.Mode.currentMode.equalsIgnoreCase("Matrix5") || this.Mode.currentMode.equalsIgnoreCase("Strict"))
            && this.actived
            && event.getPacket() instanceof SPacketPlayerPosLook) {
            MatrixStrafeMovement.oldSpeed = 0.0;
            speed = 0.0F;
            this.doUps = true;
            Speed.ncpSpeed = 0.0F;
         }
      }
   }

   @EventTarget
   public void onPostMove(EventPostMove post) {
      if (Bypass.get.getIsStrafeHacked()) {
         if (this.Mode.currentMode.equalsIgnoreCase("Matrix5") || this.Mode.currentMode.equalsIgnoreCase("Strict")) {
            MatrixStrafeMovement.postMove(post.getHorizontalMove());
         }
      }
   }

   @EventTarget
   public void onAction(EventAction action) {
      if (Bypass.get.getIsStrafeHacked()) {
         if ((this.Mode.currentMode.equalsIgnoreCase("Matrix5") || this.Mode.currentMode.equalsIgnoreCase("Strict")) && moves()) {
            MatrixStrafeMovement.actionEvent(action);
         }
      }
   }

   static boolean moves() {
      float ex3 = 0.3F;
      return !Speed.get.cancelStrafe
         && !Minecraft.player.capabilities.isFlying
         && !Minecraft.player.isLay
         && !Minecraft.player.isSneaking()
         && !Minecraft.player.isInLava()
         && !Minecraft.player.isInWater()
         && !Minecraft.player.isInWeb
         && (!NoClip.get.actived || NoClip.get.Mode.currentMode.equalsIgnoreCase("Sunrise"))
         && (!Speed.get.actived || !Speed.get.AntiCheat.currentMode.equalsIgnoreCase("AAC"))
         && (!Speed.get.actived || !Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Vanilla"))
         && (!Speed.get.actived || !Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Matrix") || !EntityLivingBase.isSunRiseDamaged || !NoClip.get.actived)
         && !TargetStrafe.goStrafe()
         && (
            !WaterSpeed.get.actived
               || !WaterSpeed.get.Mode.currentMode.equalsIgnoreCase("Matrix")
               || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.WATER
                  && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.LAVA
         )
         && (
            !ElytraBoost.get.actived
               || !ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixFly") && !ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixSpeed")
         )
         && !Scaffold.get.actived
         && !ScaffWalk.get.actived
         && !Fly.get.actived
         && (
            mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ)).getBlock() != Blocks.WATER
               || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.02001, Minecraft.player.posZ)).getBlock()
                  == Blocks.WATER
               || !JesusSpeed.get.actived
               || !JesusSpeed.get.JesusMode.currentMode.equalsIgnoreCase("MatrixZoom2")
               || !Minecraft.player.isCollidedHorizontally
         )
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.WATER
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.LAVA
         && !JesusSpeed.isJesused
         && (!Speed.snowGo || !Speed.get.actived)
         && (!LongJump.get.actived || LongJump.get.Type.currentMode.equalsIgnoreCase("FlagBoost") || !LongJump.doSpeed && !LongJump.doBow)
         && !((double)Minecraft.player.speedInAir > 0.05)
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.SOUL_SAND
         && (!Speed.get.actived || !Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Vulcan"))
         && (
            mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.3, Minecraft.player.posZ)).getBlock() != Blocks.WATER
                  && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.3, Minecraft.player.posZ)).getBlock() != Blocks.LAVA
               || !(mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.1, Minecraft.player.posZ)) instanceof BlockLiquid)
         )
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ)).getBlock() != Blocks.WEB
         && mc.world
               .getBlockState(new BlockPos(Minecraft.player.posX - (double)ex3, Minecraft.player.posY - 0.2, Minecraft.player.posZ - (double)ex3))
               .getBlock()
            != Blocks.WEB
         && mc.world
               .getBlockState(new BlockPos(Minecraft.player.posX + (double)ex3, Minecraft.player.posY - 0.2, Minecraft.player.posZ + (double)ex3))
               .getBlock()
            != Blocks.WEB
         && mc.world
               .getBlockState(new BlockPos(Minecraft.player.posX - (double)ex3, Minecraft.player.posY - 0.2, Minecraft.player.posZ + (double)ex3))
               .getBlock()
            != Blocks.WEB
         && mc.world
               .getBlockState(new BlockPos(Minecraft.player.posX + (double)ex3, Minecraft.player.posY - 0.2, Minecraft.player.posZ - (double)ex3))
               .getBlock()
            != Blocks.WEB
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX - (double)ex3, Minecraft.player.posY - 0.2, Minecraft.player.posZ)).getBlock()
            != Blocks.WEB
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX + (double)ex3, Minecraft.player.posY - 0.2, Minecraft.player.posZ)).getBlock()
            != Blocks.WEB
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ - (double)ex3)).getBlock()
            != Blocks.WEB
         && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ + (double)ex3)).getBlock()
            != Blocks.WEB
         && (
            !JesusSpeed.get.actived
               || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ)).getBlock() != Blocks.LAVA
                  && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.2, Minecraft.player.posZ)).getBlock() != Blocks.WATER
         )
         && (!Minecraft.player.isElytraFlying() || Minecraft.player.getTicksElytraFlying() <= 0)
         && !FreeCam.get.actived
         && (!Speed.get.actived || !Speed.get.AntiCheat.currentMode.equalsIgnoreCase("RipServer"))
         && (
            !HighJump.get.actived
               || !HighJump.toPlace && (!HighJump.get.GroundJump.getBool() || !HighJump.get.JumpMode.currentMode.equalsIgnoreCase("MatrixNew2"))
         )
         && (!ElytraBoost.get.actived || !ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixFly3") || !ElytraBoost.canElytra())
         && (!ElytraBoost.get.actived || !ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("NcpFly") || !ElytraBoost.canElytra())
         && !MoveHelper.holeTick
         && Bypass.get.getIsStrafeHacked()
         && (!Surround.get.actived || Surround.get.centered);
   }

   @Override
   public void onUpdate() {
      if (Minecraft.player.isCollidedHorizontally) {
         this.noCollideTime.reset();
      }

      if (Bypass.get.getIsStrafeHacked()) {
         if (!Fly.get.actived || !Fly.get.Mode.currentMode.equalsIgnoreCase("NCP")) {
            if (this.Mode.currentMode.equalsIgnoreCase("Matrix5") || this.Mode.currentMode.equalsIgnoreCase("Strict")) {
               if (this.doUps) {
                  if (speed == 0.0F) {
                     speed = curSpeed * 0.25F;
                  }

                  if (speed <= curSpeed) {
                     speed = speed + curSpeed / 3.0F;
                  }
               }

               if (!moves()) {
                  speed = 0.0F;
               }
            }

            if (this.Mode.currentMode.equalsIgnoreCase("Grim")
               && this.CollideBoost.getBool()
               && this.CollideBoost.getBool()
               && MoveMeHelp.isMoving()
               && MoveMeHelp.getSpeed() > 0.0) {
               List<EntityLivingBase> bases = mc.world
                  .getLoadedEntityList()
                  .stream()
                  .<EntityLivingBase>map(Entity::getLivingBaseOf)
                  .filter(Objects::nonNull)
                  .filter(
                     base -> base != Minecraft.player
                           && !(base instanceof EntityArmorStand)
                           && base.canBeCollidedWith()
                           && Minecraft.player.boundingBox.addExpandXZ(0.5 - MoveMeHelp.getSpeed()).intersectsWith(base.boundingBox)
                  )
                  .toList();
               if (!bases.isEmpty()) {
                  int boostCrate = 1;
                  double boostAddition = 0.08 * (double)boostCrate;
                  double moveYaw = Math.toRadians((double)MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
                  Minecraft.player.addVelocity(-Math.sin(moveYaw) * boostAddition, 0.0, Math.cos(moveYaw) * boostAddition);
                  speed = (float)MoveMeHelp.getSpeed();
               }
            }
         }
      }
   }

   @EventTarget
   public void onMove(EventMove2 move) {
      if (Bypass.get.getIsStrafeHacked()) {
         if ((this.Mode.currentMode.equalsIgnoreCase("Matrix5") || this.Mode.currentMode.equalsIgnoreCase("Strict")) && this.actived) {
            boolean noSlow = MoveHelper.instance.NoSlowDown.getBool();
            String slowType = MoveHelper.instance.NoSlowMode.currentMode;
            double forward = (double)MovementInput.moveForward;
            double strafe = (double)MovementInput.moveStrafe;
            float yaw = Minecraft.player.rotationYaw - (Minecraft.player.lastReportedPreYaw - Minecraft.player.rotationYaw) * 2.5F;
            if (HitAura.get.RotateMoveSide.getBool() && HitAura.TARGET != null && HitAura.get.canRotateUpdated) {
               yaw = HitAura.get.rotations[0];
            }

            if (forward == 0.0 && strafe == 0.0) {
               if (moves()) {
                  move.motion().xCoord = 0.0;
                  move.motion().zCoord = 0.0;
               }

               if (!moves()) {
                  Strafe.speed = 0.0F;
               }

               if (moves()) {
                  MatrixStrafeMovement.oldSpeed = 0.0;
               }

               this.doUps = true;
            } else if (moves()) {
               boolean f = mc.gameSettings.keyBindForward.isKeyDown();
               float rad = 45.0F;
               if (forward != 0.0) {
                  if (strafe > 0.0) {
                     yaw += forward > 0.0 ? -rad : rad;
                  } else if (strafe < 0.0) {
                     yaw += forward > 0.0 ? rad : -rad;
                  }

                  strafe = 0.0;
                  if (forward > 0.0) {
                     forward = 1.0;
                  } else if (forward < 0.0) {
                     forward = -1.0;
                  }
               }

               boolean elytra = ElytraBoost.get.actived
                  && (
                     ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixSpeed2") && HitAura.cooldown.hasReached(150.0)
                        || ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("MatrixSpeed3")
                        || ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("VulcanSpeed")
                  )
                  && ElytraBoost.canElytra();
               boolean matrixSpeedDamageHop = this.noCollideTime.hasReached(150.0)
                  && Speed.get.actived
                  && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Matrix")
                  && Speed.get.StrafeDamageHop.getBool()
                  && EntityLivingBase.isMatrixDamaged
                  && Minecraft.player.isJumping();
               double speed = MatrixStrafeMovement.calculateSpeed(
                  this.Mode.currentMode.equalsIgnoreCase("Strict") || matrixSpeedDamageHop,
                  move,
                  ElytraBoost.get.actived && ElytraBoost.get.Mode.currentMode.equalsIgnoreCase("StrafeSync") && ElytraBoost.canElytra() || matrixSpeedDamageHop,
                  matrixSpeedDamageHop
                     ? 0.14F
                     : (!Minecraft.player.isHandActive() && !Minecraft.player.isSneaking && this.noCollideTime.hasReached(150.0) ? 0.17 : 0.04)
               );
               boolean matrixSlow = noSlow
                  && (
                     slowType.equalsIgnoreCase("MatrixOld")
                        || slowType.equalsIgnoreCase("MatrixLatest")
                        || slowType.equalsIgnoreCase("NCP+")
                           && Minecraft.player.isHandActive()
                           && Minecraft.player.getItemInUseMaxCount() > 0
                           && !Minecraft.player.isInWater()
                           && !Minecraft.player.isInLava()
                           && !Minecraft.player.isInWeb
                           && !Minecraft.player.capabilities.isFlying
                           && Minecraft.player.getTicksElytraFlying() <= 1
                           && MoveMeHelp.isMoving()
                           && !EntityLivingBase.isNcpDamaged
                  );
               double cur = 0.0;
               if (mc.timer.speed != 1.0600014F
                  && this.TimerBoost.getBool()
                  && (this.Mode.currentMode.equalsIgnoreCase("Strict") || MoveMeHelp.getCuttingSpeed() < cur * 1.0601F)
                  && MoveMeHelp.isMoving()) {
                  mc.timer.speed = 1.0600014F;
                  Timer.forceTimer(1.0600014F);
               } else if (mc.timer.speed == 1.0600014F) {
                  mc.timer.speed = 1.0;
               }

               float w = Minecraft.player.width / 2.0F - 0.025F;
               double x = Minecraft.player.posX;
               double y = Minecraft.player.posY;
               double z = Minecraft.player.posZ;
               boolean posed = !matrixSpeedDamageHop
                  && (
                     Speed.posBlock(x, y - 1.0E-10, z)
                        || Speed.posBlock(x + (double)w, y - 1.0E-10, z + (double)w)
                        || Speed.posBlock(x - (double)w, y - 1.0E-10, z - (double)w)
                        || Speed.posBlock(x + (double)w, y - 1.0E-10, z - (double)w)
                        || Speed.posBlock(x - (double)w, y - 1.0E-10, z + (double)w)
                        || Speed.posBlock(x + (double)w, y - 1.0E-10, z)
                        || Speed.posBlock(x - (double)w, y - 1.0E-10, z)
                        || Speed.posBlock(x, y - 1.0E-10, z + (double)w)
                        || Speed.posBlock(x, y - 1.0E-10, z - (double)w)
                  );
               if (!matrixSpeedDamageHop
                  && Speed.get.actived
                  && (
                     Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Guardian") && EntityLivingBase.isSunRiseDamaged
                        || Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Matrix") && Speed.get.DamageBoost.getBool() && EntityLivingBase.isMatrixDamaged
                  )
                  && this.noCollideTime.hasReached(150.0)) {
                  if (Minecraft.player.onGround && posed && !NoClip.get.actived) {
                     speed *= speed < 0.62 ? 1.64 : 1.528;
                  } else if (Minecraft.player.isJumping() && Speed.get.actived && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Guardian")) {
                     speed *= Minecraft.player.fallDistance == 0.0F
                        ? 1.001
                        : (
                           !Speed.canMatrixBoost()
                                 || Minecraft.player.isHandActive()
                                 || !(speed < 0.4) && (!((double)Minecraft.player.fallDistance > 0.65) || !(speed < 0.6))
                              ? 1.0
                              : 1.9
                        );
                  }

                  speed = MathUtils.clamp(speed, 0.2499 - (Minecraft.player.ticksExisted % 2 == 0 ? 5.0E-7 : 0.0), 1.3);
               }

               curSpeed = (float)speed;
               if (!(Strafe.speed >= (float)speed) && !((double)Strafe.speed > cur)) {
                  this.doUps = true;
               } else {
                  Strafe.speed = (float)speed;
                  this.doUps = false;
               }

               double finalSpeed = (double)Strafe.speed;
               if (Minecraft.player.isHandActive() && !Minecraft.player.isJumping() && Minecraft.player.onGround && matrixSlow) {
                  finalSpeed *= Minecraft.player.ticksExisted % 2 == 0 ? 0.45 : 0.62;
               } else if (!matrixSpeedDamageHop
                  && Speed.canMatrixBoost()
                  && Speed.get.actived
                  && Speed.get.AntiCheat.currentMode.equalsIgnoreCase("Matrix")
                  && Speed.get.Bhop.getBool()
                  && (EntityLivingBase.isMatrixDamaged || !Speed.get.BhopOnlyDamage.getBool())
                  && !Minecraft.player.isSneaking()
                  && this.noCollideTime.hasReached(150.0)) {
                  finalSpeed *= 1.953;
               }

               if (Speed.get.actived
                  && Speed.get.AntiCheat.currentMode.contains("NCP")
                  && (double)Speed.ncpSpeed > finalSpeed
                  && Speed.get.DamageBoost.getBool()) {
                  finalSpeed = (double)Speed.ncpSpeed;
               }

               if (elytra && ElytraBoost.flSpeed > finalSpeed) {
                  finalSpeed = ElytraBoost.flSpeed;
               }

               if (Speed.iceGo) {
                  finalSpeed = (double)((float)(Minecraft.player.isPotionActive(Potion.getPotionById(1)) ? 0.91 : 0.63) * 1.1F);
               }

               if (this.Mode.currentMode.equalsIgnoreCase("Strict")
                  && this.DoNcpMin.getBool()
                  && !move.toGround()
                  && !Minecraft.player.onGround
                  && Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
                  double ncpMin = this.ncpMin();
                  if (finalSpeed < ncpMin) {
                     finalSpeed = ncpMin;
                  }
               }

               if (!move.toGround()
                  && Minecraft.player.onGround
                  && Minecraft.player.isJumping()
                  && NoClip.get.actived
                  && NoClip.get.Mode.currentMode.equalsIgnoreCase("Sunrise")) {
                  AxisAlignedBB B = Minecraft.player.boundingBox;
                  if (!mc.world.getCollisionBoxes(Minecraft.player, B).isEmpty()) {
                     finalSpeed /= 1.3;
                  }
               }

               if (speed != 0.0) {
                  move.motion().xCoord = forward * finalSpeed * Math.cos(Math.toRadians((double)(yaw + 90.0F)))
                     + strafe * finalSpeed * Math.sin(Math.toRadians((double)(yaw + 90.0F)));
                  move.motion().zCoord = forward * finalSpeed * Math.sin(Math.toRadians((double)(yaw + 90.0F)))
                     - strafe * finalSpeed * Math.cos(Math.toRadians((double)(yaw + 90.0F)));
                  Minecraft.player.motionX = move.motion().xCoord;
                  Minecraft.player.motionZ = move.motion().zCoord;
               }
            } else {
               if (Minecraft.player.isHandActive() || Minecraft.player.isBlocking()) {
                  move.motion().xCoord /= 1.025;
                  move.motion().zCoord /= 1.025;
               }

               if (mc.timer.speed == 1.0600014F) {
                  mc.timer.speed = 1.0;
               }
            }
         }
      }
   }

   double ncpMin() {
      double x = Minecraft.player.posX;
      double y = Minecraft.player.posY;
      double z = Minecraft.player.posZ;
      double ncpMin = 0.0;
      if (mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.WATER
         && mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.LAVA) {
         boolean hasSpeed2 = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))
            && Wrapper.getPlayer().getActivePotionEffect(Potion.getPotionById(1)).getAmplifier() >= 1;
         boolean hasSpeed = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))
            && Wrapper.getPlayer().getActivePotionEffect(Potion.getPotionById(1)).getAmplifier() == 0;
         if (Minecraft.player.isJumping) {
            if (Minecraft.player.isMoving()) {
               if (hasSpeed2) {
                  ncpMin = MoveMeHelp.getSpeed() < 0.38 ? 0.38 : MoveMeHelp.getSpeed();
               } else if (hasSpeed) {
                  ncpMin = MoveMeHelp.getSpeed() < 0.33 ? 0.33 : MoveMeHelp.getSpeed();
               } else {
                  ncpMin = MoveMeHelp.getSpeed() < 0.266 ? 0.266 : MoveMeHelp.getSpeed();
               }
            }
         } else if (mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.WATER
            && mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.LAVA) {
            if (hasSpeed2) {
               ncpMin = MoveMeHelp.getSpeed() <= (Minecraft.player.isSprinting() ? 0.238 : 0.279)
                  ? (Minecraft.player.isSprinting() ? 0.238 : 0.279)
                  : MoveMeHelp.getSpeed();
            } else {
               ncpMin = MoveMeHelp.getSpeed() <= (Minecraft.player.isSprinting() ? 0.165 : 0.1945)
                  ? (Minecraft.player.isSprinting() ? 0.165 : 0.1945)
                  : MoveMeHelp.getSpeed();
            }
         }
      }

      return ncpMin;
   }

   @Override
   public void onMovement() {
      if (!Fly.get.actived || !Fly.get.Mode.currentMode.equalsIgnoreCase("NCP")) {
         double x = Minecraft.player.posX;
         double y = Minecraft.player.posY;
         double z = Minecraft.player.posZ;
         boolean move2 = true;
         boolean move = true;
         if (Speed.get.actived || TargetStrafe.goStrafe()) {
            move2 = false;
            move = false;
         }

         if (NoClip.get.actived) {
            move2 = false;
            move = false;
         }

         if (Minecraft.player.isInWater() || Minecraft.player.isInLava() || Minecraft.player.isInWeb || Minecraft.player.isSneaking()) {
            move2 = false;
            move = false;
         }

         if (MoveHelper.holeTick) {
            move2 = false;
            move = false;
         }

         if (WaterSpeed.get.actived
            && WaterSpeed.get.Mode.currentMode.equalsIgnoreCase("Matrix")
            && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.4, Minecraft.player.posZ)).getBlock() == Blocks.WATER) {
            move = false;
         }

         if (ElytraBoost.get.actived || ScaffWalk.get.actived || Fly.get.actived) {
            move = false;
         }

         if ((this.Mode.currentMode.equalsIgnoreCase("Matrix3") || !Bypass.get.getIsStrafeHacked() && this.Mode.currentMode.equalsIgnoreCase("Matrix5"))
            && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ)).getBlock() == Blocks.WATER) {
            move = false;
         }

         if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.1, Minecraft.player.posZ)).getBlock() == Blocks.WATER
            && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY + 0.02001, Minecraft.player.posZ)).getBlock() != Blocks.WATER
            && JesusSpeed.get.actived
            && JesusSpeed.get.JesusMode.currentMode.equalsIgnoreCase("MatrixZoom2")
            && Minecraft.player.isCollidedHorizontally) {
            move = false;
         }

         if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 0.9, Minecraft.player.posZ)).getBlock() == Blocks.WATER) {
            move = false;
         }

         if (mc.world.getBlockState(new BlockPos(x, y - 0.2, z)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x - 1.0, y - 0.2, z)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x, y - 1.0, z - 1.0)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x - 1.0, y - 0.2, z - 1.0)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x + 1.0, y - 0.2, z)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x, y - 0.2, z + 1.0)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x + 1.0, y - 0.2, z + 1.0)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x + 1.0, y - 0.2, z - 1.0)).getBlock() == Blocks.WEB
            || mc.world.getBlockState(new BlockPos(x - 1.0, y - 0.2, z + 1.0)).getBlock() == Blocks.WEB) {
            move = false;
         }

         if (JesusSpeed.isSwimming || Speed.snowGo) {
            move = false;
         }

         if (Minecraft.player.isHandActive() || Minecraft.player.isSneaking()) {
            move = false;
         }

         if (!mc.gameSettings.keyBindForward.isKeyDown()
            && !mc.gameSettings.keyBindBack.isKeyDown()
            && !mc.gameSettings.keyBindLeft.isKeyDown()
            && !mc.gameSettings.keyBindRight.isKeyDown()
            && (this.Mode.currentMode.equalsIgnoreCase("Matrix3") || !Bypass.get.getIsStrafeHacked() && this.Mode.currentMode.equalsIgnoreCase("Matrix5"))) {
            move = false;
         }

         if (MoveHelper.holeTick) {
            move2 = false;
            move = false;
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Matrix&AAC")) {
            double maxRebound = Minecraft.player.onGround ? 0.224 : 0.24659 - (Minecraft.player.ticksExisted % 2 == 0 ? 1.0E-4 : 0.0);
            double speed = MoveMeHelp.getSpeed();
            double strafeStrenghRebound = 1.0;
            double strafeStrenghNormal = !Minecraft.player.onGround && !Speed.canMatrixBoost()
               ? 1.0 - MathUtils.clamp(speed / maxRebound * 1.25, 0.0, 1.0)
               : 1.0;
            boolean reboundSet = !Minecraft.player.isSneaking() || Minecraft.player.hasNewVersionMoves && Minecraft.player.isLay;
            double curSpeed = speed;
            double maxNoStrafeSpeed = Minecraft.player.onGround
               ? (
                  !Minecraft.player.isPotionActive(MobEffects.SPEED)
                        && !(Minecraft.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() > 0.111)
                     ? 0.0
                     : 0.18
               )
               : (Minecraft.player.isSprinting() ? 0.14 : 0.08);
            if (reboundSet && speed < maxRebound) {
               curSpeed = maxRebound;
            }

            if (this.CollideBoost.getBool()
               && MoveMeHelp.isMoving()
               && speed > 0.249
               && (!TargetStrafe.goStrafe() || TargetStrafe.get.SmartSpeed.getBool() || !TargetStrafe.get.CollideBoost.getBool())) {
               List<EntityLivingBase> bases = mc.world
                  .getLoadedEntityList()
                  .stream()
                  .<EntityLivingBase>map(Entity::getLivingBaseOf)
                  .filter(Objects::nonNull)
                  .filter(
                     base -> base != Minecraft.player
                           && !(base instanceof EntityArmorStand)
                           && base.canBeCollidedWith()
                           && Minecraft.player
                              .boundingBox
                              .addExpandXZ(0.4)
                              .intersectsWith(
                                 new AxisAlignedBB(
                                    base.posX - (double)base.width / 2.0,
                                    base.posY,
                                    base.posZ - (double)base.width / 2.0,
                                    base.posX + (double)base.width / 2.0,
                                    base.posY + 1.2,
                                    base.posZ + (double)base.width / 2.0
                                 )
                              )
                  )
                  .toList();
               if (!bases.isEmpty()) {
                  int boostCrate = 1;
                  double boostAddition = (Minecraft.player.onGround && !Minecraft.player.isJumping() ? 0.23 : 0.12) * (double)boostCrate;
                  double moveYaw = Math.toRadians((double)MoveMeHelp.moveYaw(Minecraft.player.rotationYaw));
                  Minecraft.player.addVelocity(-Math.sin(moveYaw) * boostAddition, 0.0, Math.cos(moveYaw) * boostAddition);
                  speed = MoveMeHelp.getSpeed();
                  if (speed > 0.8F) {
                     speed = 0.8F;
                  }

                  MoveMeHelp.setMotionSpeed(
                     false,
                     true,
                     speed,
                     (float)MoveMeHelp.getDirDiffOfMotions(Minecraft.player.motionX, Minecraft.player.motionZ) + Minecraft.player.rotationYaw
                  );
               }
            }

            if (speed >= maxNoStrafeSpeed) {
               if (speed < maxRebound) {
                  MoveMeHelp.setSmoothSpeed(curSpeed, strafeStrenghRebound, Minecraft.player.isSneaking());
               } else if (strafeStrenghNormal != 0.0) {
                  MoveMeHelp.setSmoothSpeed(speed, strafeStrenghNormal, false);
               }
            }
         }

         if (this.Mode.currentMode.equalsIgnoreCase("NCP") && !Speed.iceGo && move2) {
            if (this.TimerBoost.getBool()
               && Minecraft.player.fallDistance == 0.0F
               && HitAura.TARGET == null
               && !Minecraft.player.onGround
               && Minecraft.player.isJumping()) {
               mc.timer.speed = 1.0900015F;
            } else if (mc.timer.speed == 1.0900015F) {
               mc.timer.speed = 1.0;
            }

            Timer.forceTimer(1.0900015F);
            MoveMeHelp.setCuttingSpeed(0.0);
            if (mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.WATER
               && mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.LAVA) {
               if (!Minecraft.player.isJumping) {
                  if (mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.WATER
                     && mc.world.getBlockState(new BlockPos(x, y - 0.2F, z)).getBlock() != Blocks.LAVA) {
                     if (Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
                        MoveMeHelp.setCuttingSpeed(
                           MoveMeHelp.getSpeed() <= (Minecraft.player.isSprinting() ? 0.238 : 0.279)
                              ? (Minecraft.player.isSprinting() ? 0.238 : 0.279)
                              : MoveMeHelp.getSpeed()
                        );
                     } else {
                        MoveMeHelp.setCuttingSpeed(
                           MoveMeHelp.getSpeed() <= (Minecraft.player.isSprinting() ? 0.165 : 0.1945)
                              ? (Minecraft.player.isSprinting() ? 0.165 : 0.1945)
                              : MoveMeHelp.getSpeed()
                        );
                     }
                  }
               } else {
                  if (Minecraft.player.isMoving() || mc.gameSettings.keyBindBack.isKeyDown()) {
                     if (Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1))) {
                        MoveMeHelp.setCuttingSpeed(MoveMeHelp.getSpeed() < 0.382 ? 0.382 : MoveMeHelp.getSpeed());
                     } else {
                        MoveMeHelp.setCuttingSpeed(MoveMeHelp.getSpeed() < 0.266 ? 0.266 : MoveMeHelp.getSpeed());
                     }
                  }

                  if (!Minecraft.player.isMoving() && mc.gameSettings.keyBindForward.isKeyDown()) {
                     if (MoveMeHelp.getSpeed() < (Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1)) ? 0.395 : 0.265)) {
                        if (Minecraft.player.onGround) {
                           MoveMeHelp.setCuttingSpeed(Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1)) ? 0.35 : 0.24);
                        } else {
                           MoveMeHelp.setCuttingSpeed(Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1)) ? 0.385 : 0.26);
                        }
                     } else if (!Minecraft.player.onGround) {
                        MoveMeHelp.setCuttingSpeed(MoveMeHelp.getSpeed());
                     }
                  }
               }
            }

            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.LAVA
               && !MoveMeHelp.isBlockAboveHead()
               && Minecraft.player.isJumping()
               && Minecraft.player.motionY < 0.0
               && !((double)Minecraft.player.fallDistance >= 1.19)
               && !Minecraft.player.onGround
               && this.PullDown.getBool()) {
               Entity.motiony = Minecraft.player.motionY
                  - (double)(Minecraft.player.fallDistance < 1.0F ? 0.07549F : (Minecraft.player.fallDistance < 4.0F ? 0.0732F : 0.0F));
            }
         }

         if (this.Mode.currentMode.equalsIgnoreCase("NCP2") && !Speed.iceGo && move2) {
            if (this.TimerBoost.getBool()
               && Minecraft.player.fallDistance == 0.0F
               && HitAura.TARGET == null
               && !Minecraft.player.onGround
               && Minecraft.player.isJumping()) {
               mc.timer.speed = 1.0900015F;
            } else if (mc.timer.speed == 1.0900015F) {
               mc.timer.speed = 1.0;
            }

            double speedx = MathUtils.clamp(MoveMeHelp.getSpeed(), this.ncpMin(), MoveMeHelp.getSpeed());
            boolean isDiagonal = (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()) && Minecraft.player.isMoving();
            boolean isSpeedPot = Minecraft.player.isPotionActive(MobEffects.SPEED)
               && Minecraft.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() > 0;
            Minecraft.player.speedInAir = 0.02F
               + (isSpeedPot ? (isDiagonal ? (float)(speedx / 47.0) : (float)(speedx / 40.0)) : (isDiagonal ? 0.001F : (float)(speedx / 160.0)));
            MoveMeHelp.setSpeed(speedx);
            MoveMeHelp.setCuttingSpeed(speedx / 1.06F);
            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.WATER
               && mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() != Blocks.LAVA
               && !MoveMeHelp.isBlockAboveHead()
               && Minecraft.player.isJumping()
               && Minecraft.player.motionY < 0.0
               && !((double)Minecraft.player.fallDistance >= 1.19)) {
               if (HitAura.TARGET_ROTS != null) {
                  return;
               }

               if (!Minecraft.player.onGround && this.PullDown.getBool()) {
                  Entity.motiony = Minecraft.player.motionY
                     - (double)(Minecraft.player.fallDistance < 1.0F ? 0.07549F : (Minecraft.player.fallDistance < 4.0F ? 0.0732F : 0.0F));
               }
            }
         }

         if ((
               this.Mode.currentMode.equalsIgnoreCase("Matrix3") && (!LongJump.get.actived || !LongJump.get.Type.currentMode.equalsIgnoreCase("InstantLong"))
                  || !Bypass.get.getIsStrafeHacked() && this.Mode.currentMode.equalsIgnoreCase("Matrix5")
            )
            && move) {
            boolean gr = Minecraft.player.onGround && Minecraft.player.isCollidedVertically && !Minecraft.player.isJumping();
            if (gr) {
               if (Minecraft.player.isMoving() && mc.gameSettings.keyBindForward.isKeyDown()) {
                  float var42 = 0.2845F;
               } else {
                  float var41 = mc.gameSettings.keyBindForward.isKeyDown() ? 0.2805F : 0.26F;
               }
            } else if ((float)MoveMeHelp.getSpeed() > 0.3F) {
               float var10000 = (float)MoveMeHelp.getSpeed();
            } else {
               float var40 = 0.3F;
            }

            double WsprNumb = (double)(0.2499F - (Minecraft.player.ticksExisted % 2 == 0 ? 5.0E-4F : 0.0F));
            double speedWspr = (double)((float)(MoveMeHelp.getSpeed() > WsprNumb ? MoveMeHelp.getSpeed() : WsprNumb));
            double grP = MoveMeHelp.getSpeed();
            double valWerto = MathUtils.getDifferenceOf(Minecraft.player.rotationYaw, Minecraft.player.lastReportedYaw) > 2.0 ? 0.2305 : WsprNumb;
            if (!(MoveMeHelp.getSpeed() < valWerto) || Minecraft.player.onGround && !gr) {
               if (gr || Speed.canMatrixBoost()) {
                  MoveMeHelp.setSpeed(grP);
                  MoveMeHelp.setCuttingSpeed(grP / 1.06);
               }
            } else {
               MoveMeHelp.setSpeed(speedWspr);
               MoveMeHelp.setCuttingSpeed(speedWspr / 1.06);
            }
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Matrix4") && MoveMeHelp.isMoving() && move) {
            if (this.TimerBoost.getBool()
               && Minecraft.player.fallDistance == 0.0F
               && HitAura.TARGET == null
               && !Minecraft.player.onGround
               && Minecraft.player.isJumping()
               && MoveMeHelp.getSpeed() < 0.195
               && mc.timer.speed == 1.0) {
               mc.timer.speed = 1.0900015F;
            } else if (mc.timer.speed == 1.0900015F) {
               mc.timer.speed = 1.0;
            }

            boolean grx = Minecraft.player.onGround && Minecraft.player.isCollidedVertically && !Minecraft.player.isJumping();
            if (grx) {
               if (Minecraft.player.isMoving() && mc.gameSettings.keyBindForward.isKeyDown()) {
                  float var46 = 0.2845F;
               } else {
                  float var45 = mc.gameSettings.keyBindForward.isKeyDown() ? 0.2805F : 0.26F;
               }
            } else if ((float)MoveMeHelp.getSpeed() > 0.3F) {
               float var43 = (float)MoveMeHelp.getSpeed();
            } else {
               float var44 = 0.3F;
            }

            double WsprNumb = (double)(0.2499F - (Minecraft.player.ticksExisted % 2 == 0 ? 1.0E-6F : 0.0F));
            double speedWspr = (double)((float)(MoveMeHelp.getSpeed() > WsprNumb ? MoveMeHelp.getSpeed() : WsprNumb));
            if (MoveMeHelp.getSpeed() < 0.23 && !Minecraft.player.onGround && !grx) {
               MoveMeHelp.setSpeed(speedWspr);
               MoveMeHelp.setCuttingSpeed(speedWspr / 1.06);
            } else if (Speed.canMatrixBoost() && !grx) {
               MoveMeHelp.setSpeed(speedWspr);
               MoveMeHelp.setCuttingSpeed(speedWspr / 1.06);
            }

            if (Minecraft.player.isJumping() && MoveMeHelp.getSpeed() < 0.27 && Minecraft.player.onGround && !MoveMeHelp.isBlockAboveHead()) {
               MoveMeHelp.setSpeed(0.3);
               MoveMeHelp.setCuttingSpeed(0.2830188679245283);
            }
         }
      }
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.Mode.currentMode);
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      if (this.actived) {
         if (!Fly.get.actived || !Fly.get.Mode.currentMode.equalsIgnoreCase("NCP")) {
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY;
            double z = Minecraft.player.posZ;
            boolean move = false;
            if (mc.timer.speed == 1.100432987562) {
               mc.timer.speed = 1.0;
            }

            move = Minecraft.player.isMoving() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown();
            if (Speed.get.actived || TargetStrafe.goStrafe()) {
               move = false;
            }

            if (MoveHelper.holeTick) {
               move = false;
            }

            if (NoClip.get.actived) {
            }

            if (Minecraft.player.isInWater() || Minecraft.player.isInLava() || Minecraft.player.isInWeb) {
               move = false;
            }

            if (mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ)).getBlock() == Blocks.WATER
               || mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)).getBlock() == Blocks.WATER) {
               move = false;
            }

            if (ElytraBoost.get.actived || ScaffWalk.get.actived) {
               move = false;
            }

            if (mc.world.getBlockState(new BlockPos(x, y - 0.2, z)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x - 1.0, y - 0.2, z)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x, y - 1.0, z - 1.0)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x - 1.0, y - 0.2, z - 1.0)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x + 1.0, y - 0.2, z)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x, y - 0.2, z + 1.0)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x + 1.0, y - 0.2, z + 1.0)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x + 1.0, y - 0.2, z - 1.0)).getBlock() == Blocks.WEB
               || mc.world.getBlockState(new BlockPos(x - 1.0, y - 0.2, z + 1.0)).getBlock() == Blocks.WEB) {
               move = false;
            }

            if (this.Mode.currentMode.equalsIgnoreCase("Matrix") && move) {
               if (Minecraft.player.getItemInUseMaxCount() <= 0 && !Minecraft.player.isSneaking()) {
                  if (MoveMeHelp.getSpeed() < 0.2205F || Minecraft.player.onGround) {
                     MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
                  }

                  if (mc.timer.speed == 1.100432987562) {
                     mc.timer.speed = 1.0;
                  }

                  if ((!AirJump.get.actived || !AirJump.get.Mode.currentMode.equalsIgnoreCase("Matrix"))
                     && MoveMeHelp.getSpeed() < 0.1965F
                     && !Minecraft.player.onGround) {
                     if (this.TimerBoost.getBool() && !Minecraft.player.onGround && MoveMeHelp.getSpeed() < 0.14F && !HitAura.get.actived) {
                        mc.timer.speed = 1.100432987562;
                     }

                     MoveMeHelp.setSpeed(0.1965F);
                  }

                  if (Minecraft.player.onGround
                     && Minecraft.player.isJumping()
                     && !Minecraft.player.isSprinting()
                     && !mc.gameSettings.keyBindForward.isKeyDown()
                     && Minecraft.player.motionY < 0.1
                     && Minecraft.player.fallDistance == 0.0F) {
                     MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 1.4);
                  } else if (Minecraft.player.fallDistance != 0.0F
                     && !Minecraft.player.onGround
                     && !Minecraft.player.isSprinting()
                     && !mc.gameSettings.keyBindForward.isKeyDown()) {
                     MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 1.0071F);
                  }
               } else {
                  if (Minecraft.player.onGround) {
                     MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 1.0071F);
                  }

                  if (MoveMeHelp.getSpeed() < 0.2175F) {
                     MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 1.0071F);
                  }
               }
            }

            if (this.Mode.currentMode.equalsIgnoreCase("Matrix2")
               && move
               && !Minecraft.player.isCollidedHorizontally
               && (!AirJump.get.actived || !AirJump.get.Mode.currentMode.equalsIgnoreCase("Matrix"))
               && MoveMeHelp.getSpeed() < 0.1965F
               && !Minecraft.player.onGround) {
               if (this.TimerBoost.getBool() && !Minecraft.player.onGround && MoveMeHelp.getSpeed() < 0.14F && !HitAura.get.actived) {
                  mc.timer.speed = 1.100432987562;
               }

               MoveMeHelp.setSpeed(0.1965F);
            }

            if (this.Mode.currentMode.equalsIgnoreCase("Matrix2") && move) {
               if (MoveMeHelp.getSpeed() < 0.22) {
                  MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
               }

               if (MoveMeHelp.getSpeed() < 0.18 && !Minecraft.player.onGround) {
                  MoveMeHelp.setSpeed(0.18);
               }
            }

            if (this.Mode.currentMode.equalsIgnoreCase("Guardian") && move && MoveMeHelp.isMoving()) {
               if (MoveMeHelp.getSpeed() != 0.0) {
                  MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
               }

               if (this.TimerBoost.getBool() && !HitAura.get.actived) {
                  mc.timer.speed = 1.100432987562;
               }
            }
         }
      }
   }
}
