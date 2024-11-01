package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import org.lwjgl.input.Mouse;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventMovementInput;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventRotationJump;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class PotionThrower extends Module {
   public static PotionThrower get;
   FloatSettings TicksFromSpawn;
   FloatSettings MinHealth;
   BoolSettings DisableOnThrow;
   BoolSettings InPVPTime;
   BoolSettings OnSnackFloorSwing;
   BoolSettings RefillHotbar;
   BoolSettings HealthPotions;
   BoolSettings RotateMoveDir;
   public boolean forceThrow;
   public boolean callThrowPotions;
   public boolean firstThrow;
   private final TimerHelper timerThrow = new TimerHelper();
   private List<PotionThrower.Potions> toThrowList = new ArrayList<>();
   private boolean canThrow;
   private float[] rotate;

   public PotionThrower() {
      super("PotionThrower", 0, Module.Category.COMBAT);
      this.settings.add(this.TicksFromSpawn = new FloatSettings("TicksFromSpawn", 20.0F, 100.0F, 0.0F, this));
      this.settings.add(this.DisableOnThrow = new BoolSettings("DisableOnThrow", false, this));
      this.settings.add(this.InPVPTime = new BoolSettings("InPVPMode", false, this));
      this.settings.add(this.OnSnackFloorSwing = new BoolSettings("OnSnackFloorSwing", false, this));
      this.settings.add(this.RefillHotbar = new BoolSettings("RefillHotbar", true, this));
      this.settings.add(this.HealthPotions = new BoolSettings("HealthPotions", true, this));
      this.settings.add(this.MinHealth = new FloatSettings("MinHealth", 14.0F, 20.0F, 4.0F, this, () -> this.HealthPotions.getBool()));
      this.settings.add(this.RotateMoveDir = new BoolSettings("RotateMoveDir", false, this));
      get = this;
   }

   public boolean isStackPotion(ItemStack stack, PotionThrower.Potions potion) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         if (item == Items.SPLASH_POTION) {
            int id = 5;
            switch (potion) {
               case STRENGTH:
                  id = 5;
                  break;
               case SPEED:
                  id = 1;
                  break;
               case FIRERES:
                  id = 12;
                  break;
               case INSTANT_HEALTH:
                  id = 6;
                  break;
               case REGENERATION:
                  id = 10;
            }

            for (PotionEffect effect : PotionUtils.getEffectsFromStack(stack)) {
               if (effect.getPotion() == Potion.getPotionById(id)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private int getPotionSlot(PotionThrower.Potions potion, boolean inInventory) {
      for (int i = inInventory ? 44 : 8; i >= (inInventory ? 9 : 0); i--) {
         if (this.isStackPotion(Minecraft.player.inventory.getStackInSlot(i), potion)) {
            return i;
         }
      }

      return -1;
   }

   private int getPotionId(PotionThrower.Potions potion) {
      return potion == PotionThrower.Potions.STRENGTH
         ? 5
         : (
            potion == PotionThrower.Potions.SPEED
               ? 1
               : (
                  potion == PotionThrower.Potions.FIRERES
                     ? 12
                     : (potion == PotionThrower.Potions.REGENERATION ? 10 : (potion == PotionThrower.Potions.INSTANT_HEALTH ? 6 : 5))
               )
         );
   }

   private boolean isActivePotion(PotionThrower.Potions potion) {
      Potion eff = Potion.getPotionById(this.getPotionId(potion));
      return Minecraft.player != null && Minecraft.player.isPotionActive(eff) && Minecraft.player.getActivePotionEffect(eff).getDuration() > 10;
   }

   private List<PotionThrower.Potions> potsList(boolean healthPot, float healthMin) {
      return healthPot && Minecraft.player.getHealth() < healthMin
         ? Arrays.asList(
            PotionThrower.Potions.SPEED,
            PotionThrower.Potions.STRENGTH,
            PotionThrower.Potions.FIRERES,
            PotionThrower.Potions.REGENERATION,
            PotionThrower.Potions.INSTANT_HEALTH
         )
         : Arrays.asList(PotionThrower.Potions.SPEED, PotionThrower.Potions.STRENGTH, PotionThrower.Potions.FIRERES, PotionThrower.Potions.REGENERATION);
   }

   private List<PotionThrower.Potions> potsListToThrow(boolean healthPot, float healthMin) {
      return this.potsList(healthPot, healthMin)
         .stream()
         .filter(potion -> this.getPotionSlot(potion, false) != -1 && !this.isActivePotion(potion))
         .collect(Collectors.toList());
   }

   private boolean inPvpTime() {
      return !GuiBossOverlay.mapBossInfos2.isEmpty()
         && !GuiBossOverlay.mapBossInfos2
            .values()
            .stream()
            .map(BossInfo::getName)
            .<String>map(ITextComponent::getUnformattedText)
            .map(String::toLowerCase)
            .filter(name -> name.contains("pvp") || name.contains("пвп") || name.contains("сек."))
            .filter(Objects::nonNull)
            .toList()
            .isEmpty();
   }

   private boolean canThrowing(int ticksAlive, List<PotionThrower.Potions> toThrow, float delay, boolean checkPvp, boolean onlySNP) {
      return !toThrow.isEmpty()
         && (
            Minecraft.player.isCollidedVertically
               || Minecraft.player.isCollidedHorizontally
               || this.forceThrow
               || (double)Minecraft.player.fallDistance > 0.2
                  && !mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.boundingBox.offsetMinDown(1.0)).isEmpty()
         )
         && Minecraft.player.ticksExisted >= ticksAlive
         && this.timerThrow.hasReached((double)delay)
         && (mc.currentScreen == null || !(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory)
         && MathUtils.getDifferenceOf(Entity.Getmotiony, 0.0) < 1.5
         && !FreeCam.get.actived
         && (!Minecraft.player.isHandActive() || Minecraft.player.getActiveHand() == null || !Minecraft.player.getActiveHand().equals(EnumHand.MAIN_HAND))
         && !Minecraft.player.isElytraFlying()
         && (!mc.playerController.getIsHittingBlock() || !((double)mc.playerController.curBlockDamageMP > 0.5))
         && (!JesusSpeed.get.actived || !JesusSpeed.isSwimming && !JesusSpeed.isJesused)
         && (!Speed.get.actived || !Speed.snowGround || !Speed.snowGo)
         && (
            !checkPvp && (!onlySNP || Minecraft.player.isSneaking() && Minecraft.player.rotationPitch > 80.0F && Mouse.isButtonDown(0))
               || checkPvp
                  && this.inPvpTime()
                  && mc.world
                     .playerEntities
                     .stream()
                     .anyMatch(
                        player -> {
                           if (player instanceof EntityOtherPlayerMP mp
                              && mp.isEntityAlive()
                              && mp != FakePlayer.fakePlayer
                              && mp.getDistanceToEntity(Minecraft.player) < 10.0F
                              && mp.canEntityBeSeen(Minecraft.player)
                              && !Client.friendManager.isFriend(mp.getName())) {
                              return true;
                           }

                           return false;
                        }
                     )
               || !onlySNP
               || Minecraft.player.isSneaking() && Minecraft.player.rotationPitch > 80.0F && Mouse.isButtonDown(0)
         );
   }

   private boolean hasEmptySlotsInHotbar() {
      for (int i = 0; i <= 8; i++) {
         if (Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemAir) {
            return true;
         }
      }

      return false;
   }

   @EventTarget
   public void onSend(EventSendPacket event) {
      if (this.actived && this.firstThrow && event.getPacket() instanceof CPacketConfirmTransaction) {
         this.firstThrow = false;
      }
   }

   @Override
   public void onUpdate() {
      if (Minecraft.player.ticksExisted <= 1) {
         this.firstThrow = true;
      }

      boolean reFill = this.RefillHotbar.getBool() && !Minecraft.player.isCreative();
      this.toThrowList = this.potsListToThrow(this.HealthPotions.getBool(), this.MinHealth.getFloat());
      this.canThrow = this.canThrowing(this.TicksFromSpawn.getInt(), this.toThrowList, 400.0F, this.InPVPTime.getBool(), this.OnSnackFloorSwing.getBool());
      if (this.callThrowPotions) {
         if (!this.toThrowList.isEmpty()) {
            this.setHead(null, this.rotate[0], this.rotate[1]);
            int prevHandSlot = Minecraft.player.inventory.currentItem;
            int lastSlot = prevHandSlot;

            for (PotionThrower.Potions potion : this.toThrowList) {
               int potionSlot = this.getPotionSlot(potion, false);
               if (potionSlot != -1) {
                  boolean isStackedPotion = Minecraft.player.inventory.getStackInSlot(potionSlot).stackSize > 1;
                  if (Minecraft.player.inventory.currentItem != potionSlot) {
                     Minecraft.player.inventory.currentItem = potionSlot;
                     mc.playerController.syncCurrentPlayItem();
                     lastSlot = potionSlot;
                  }

                  Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                  if (reFill && !isStackedPotion) {
                     int invPotionSlot = this.getPotionSlot(potion, true);
                     if (invPotionSlot > 8) {
                        mc.playerController.windowClickMemory(0, invPotionSlot, potionSlot, ClickType.SWAP, Minecraft.player, 150);
                     }
                  }
               }
            }

            if (prevHandSlot != lastSlot) {
               Minecraft.player.inventory.currentItem = prevHandSlot;
               mc.playerController.syncCurrentPlayItem();
            }

            this.forceThrow = false;
         }

         if (this.DisableOnThrow.getBool()) {
            this.toggle();
         }

         this.callThrowPotions = false;
      }

      super.onUpdate();
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         this.forceThrow = false;
         this.callThrowPotions = false;
      } else {
         this.canThrow = false;
         this.forceThrow = false;
         this.callThrowPotions = false;
      }

      super.onToggled(actived);
   }

   private void setHead(EventPlayerMotionUpdate event, float yaw, float pitch) {
      Minecraft.player.rotationYawHead = yaw;
      Minecraft.player.renderYawOffset = yaw;
      Minecraft.player.rotationPitchHead = pitch;
      HitAura.get.rotations = new float[]{yaw, pitch};
      if (this.RotateMoveDir.getBool() && Minecraft.player.toCancelSprintTicks <= 1 && MathUtils.getDifferenceOf(Minecraft.player.rotationYaw, yaw) >= 45.0) {
         Minecraft.player.toCancelSprintTicks = 2;
      }

      if (event != null) {
         event.setYaw(yaw);
         event.setPitch(pitch);
      }
   }

   private double getXVec(Vec3d vec) {
      return vec != null ? vec.xCoord : 0.0;
   }

   private double getYVec(Vec3d vec) {
      return vec != null ? vec.zCoord : 0.0;
   }

   private double getZVec(Vec3d vec) {
      return vec != null ? vec.zCoord : 0.0;
   }

   private Vec3d posToRotate() {
      ArrayList<Vec3d> nonAirList = new ArrayList<>();
      float xzR = 1.15F;
      int yrP = 2;
      int yrM = 1;
      double xMe = Minecraft.player.getPositionVector().xCoord;
      double yMe = Minecraft.player.getPositionVector().yCoord;
      double zMe = Minecraft.player.getPositionVector().zCoord;
      if (mc.world == null) {
         return null;
      } else {
         for (double x = xMe - (double)xzR; x < xMe + (double)xzR; x++) {
            for (double z = zMe - (double)xzR; z < zMe + (double)xzR; z++) {
               for (double y = yMe + (double)yrP; y > yMe - (double)yrM; y--) {
                  BlockPos pos = new BlockPos(x, y, z);
                  if (pos != null && BlockUtils.blockMaterialIsCurrent(pos)) {
                     nonAirList.add(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5));
                  }
               }
            }
         }

         if (nonAirList != null && nonAirList.size() > 1) {
            nonAirList.sort(
               Comparator.comparing(nonAir -> -Minecraft.player.getDistanceAtEye(this.getXVec(nonAir), this.getYVec(nonAir), this.getZVec(nonAir), 0.8F))
            );
         }

         return nonAirList != null && nonAirList.size() != 0 ? nonAirList.get(0) : null;
      }
   }

   private float calculatePitch() {
      double speed = MoveMeHelp.getSpeed();
      float max = 89.0F;
      double delta = MathUtils.clamp(Math.sqrt(speed * speed) / 0.55F, 0.0, 0.5)
         - MathUtils.clamp(Math.abs(Math.sqrt(Entity.Getmotiony * Entity.Getmotiony)) * 9.0, 0.0, 0.333333);
      return (float)(89.0 - (double)max * MathUtils.clamp(delta * 3.0, 0.0, 1.0));
   }

   private float[] getRotate(Vec3d to) {
      float moveYaw = MoveMeHelp.getCuttingSpeed() > 0.05 ? (float)MoveMeHelp.getMotionYaw() + 30.0F : Minecraft.player.rotationYaw;
      float[] rotate = new float[]{moveYaw, this.calculatePitch()};
      if (!MoveMeHelp.isBlockAboveHeadSolo() && MoveMeHelp.getSpeed() > 0.2 && Minecraft.player.onGround && to == null) {
         rotate = new float[]{moveYaw, this.calculatePitch()};
      } else if (MoveMeHelp.isBlockAboveHeadSolo()) {
         rotate = new float[]{moveYaw, -89.0F};
      } else if (to != null) {
         rotate = RotationUtil.getNeededFacing(to, false, Minecraft.player, false);
      }

      return rotate;
   }

   @EventTarget
   public void onPlayerUpdate(EventPlayerMotionUpdate event) {
      if (this.canThrow) {
         this.rotate = this.getRotate(this.posToRotate());
         if (this.rotate != null) {
            if (!Minecraft.player.isSneaking() || !(Minecraft.player.rotationPitch > 85.0F) || !Mouse.isButtonDown(0)) {
               if (this.firstThrow) {
                  if (Minecraft.player.onGround) {
                     Minecraft.player.connection.sendPacket(new Rotation(this.rotate[0], this.rotate[1], event.onGround()));
                  }

                  this.firstThrow = false;
               }

               this.setHead(event, this.rotate[0], this.rotate[1]);
            }

            this.timerThrow.reset();
            this.canThrow = false;
            this.callThrowPotions = true;
         }
      }
   }

   @EventTarget
   public void onMovementInput(EventMovementInput event) {
      if (this.canThrow && this.RotateMoveDir.getBool() && this.rotate != null) {
         MoveMeHelp.fixDirMove(event, this.rotate[0]);
      }
   }

   @EventTarget
   public void onSilentStrafe(EventRotationStrafe event) {
      if (this.canThrow && this.RotateMoveDir.getBool() && this.rotate != null) {
         event.setYaw(this.rotate[0]);
      }
   }

   @EventTarget
   public void onSilentJump(EventRotationJump event) {
      if (this.canThrow && this.RotateMoveDir.getBool() && this.rotate != null) {
         event.setYaw(this.rotate[0]);
      }
   }

   @EventTarget
   public void onReceivePacket(EventReceivePacket event) {
      if (event.getPacket() instanceof SPacketEntityStatus status
         && Minecraft.player != null
         && status.entityId == Minecraft.player.getEntityId()
         && status.getOpCode() == 35) {
         this.forceThrow = true;
      }
   }

   private static enum Potions {
      STRENGTH,
      SPEED,
      FIRERES,
      INSTANT_HEALTH,
      REGENERATION;
   }
}
