package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventElytraVector;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class ElytraBoost extends Module {
   public static ElytraBoost get;
   public ModeSettings Mode;
   public FloatSettings SpeedF;
   public FloatSettings SpeedXZ;
   public BoolSettings NoTimerDefunction;
   public BoolSettings StrafeDirs;
   public BoolSettings StaticYMotions;
   TimerHelper timer = new TimerHelper();
   TimerHelper timer2 = new TimerHelper();
   float moveElytraYaw;
   float moveElytraPitch;
   public static Item oldSlot = null;
   String strafeMode = null;
   boolean strafeActived = false;
   public static boolean hitTick = false;
   double curPosY;
   TimerHelper wait = new TimerHelper();
   public static double flSpeed;
   int boostTicks;

   public ElytraBoost() {
      super("ElytraBoost", 0, Module.Category.MOVEMENT);
      this.settings
         .add(
            this.Mode = new ModeSettings(
               "Mode",
               "MatrixFly",
               this,
               new String[]{
                  "MatrixFly",
                  "MatrixFly2",
                  "MatrixFly3",
                  "MatrixSpeed",
                  "MatrixSpeed2",
                  "MatrixSpeed3",
                  "NcpFly",
                  "Vanilla",
                  "StrafeSync",
                  "Firework",
                  "VulcanSpeed",
                  "VulcanPulse"
               }
            )
         );
      this.settings
         .add(
            this.SpeedF = new FloatSettings(
               "Speed",
               3.0F,
               10.0F,
               1.0F,
               this,
               () -> !this.Mode.currentMode.equalsIgnoreCase("Vanilla")
                     && !this.Mode.currentMode.equalsIgnoreCase("Firework")
                     && !this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed2")
                     && !this.Mode.currentMode.equalsIgnoreCase("MatrixFly2")
                     && !this.Mode.currentMode.equalsIgnoreCase("MatrixFly3")
                     && !this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed3")
                     && !this.Mode.currentMode.equalsIgnoreCase("VulcanSpeed")
                     && !this.Mode.currentMode.equalsIgnoreCase("StrafeSync")
            )
         );
      this.settings.add(this.SpeedXZ = new FloatSettings("SpeedXZ", 1.0F, 3.0F, 0.25F, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixFly3")));
      this.settings
         .add(this.NoTimerDefunction = new BoolSettings("NoTimerDefunction", false, this, () -> this.Mode.currentMode.equalsIgnoreCase("MatrixFly2")));
      this.settings.add(this.StrafeDirs = new BoolSettings("StrafeDirs", false, this, () -> this.Mode.currentMode.equalsIgnoreCase("Firework")));
      this.settings.add(this.StaticYMotions = new BoolSettings("StaticYMotions", true, this, () -> this.Mode.currentMode.equalsIgnoreCase("Firework")));
      get = this;
   }

   public static void eq() {
      if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
         oldSlot = Minecraft.player.inventory.armorItemInSlot(2).getItem();
      }

      if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
         if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemAir) {
            mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
         } else if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
            mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
            mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
         }
      }
   }

   public static void deq() {
      if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
         mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
         mc.playerController.windowClick(0, getOldItem(), 1, ClickType.QUICK_MOVE, Minecraft.player);
      }
   }

   boolean canFly() {
      return Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra;
   }

   boolean putFirework() {
      if (Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemFirework
         && !(Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemFirework)) {
         mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.OFF_HAND);
         return true;
      } else {
         int slot = -1;
         int handSlot = Minecraft.player.inventory.currentItem;

         for (int i = 44; i > 0; i--) {
            ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
            if (itemStack.getItem() instanceof ItemFirework) {
               slot = i;
            }
         }

         if (slot == -1) {
            return false;
         } else {
            if (Minecraft.player.getHeldItemMainhand().getItem() instanceof ItemFirework) {
               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.MAIN_HAND);
            } else if (InventoryUtil.getItemSlotInHotbar(Items.FIREWORKS) != -1) {
               slot = InventoryUtil.getItemSlotInHotbar(Items.FIREWORKS);
               if (Minecraft.player.isHandActive() && Minecraft.player.getActiveHand() == EnumHand.OFF_HAND) {
                  mc.playerController.windowClick(0, 45, slot, ClickType.SWAP, Minecraft.player);
                  mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.OFF_HAND);
                  mc.playerController.windowClickMemory(0, 45, slot, ClickType.SWAP, Minecraft.player, 100);
               } else {
                  if (handSlot != slot) {
                     Minecraft.player.inventory.currentItem = slot;
                     mc.playerController.syncCurrentPlayItem();
                  }

                  mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.MAIN_HAND);
                  if (handSlot != slot) {
                     Minecraft.player.inventory.currentItem = handSlot;
                     mc.playerController.syncCurrentPlayItem();
                  }
               }
            } else if (Minecraft.player.isHandActive() && Minecraft.player.getActiveHand() == EnumHand.MAIN_HAND) {
               mc.playerController.windowClick(0, slot, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
               mc.playerController.windowClickMemory(0, slot, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player, 100);
               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.MAIN_HAND);
            } else {
               mc.playerController.windowClick(0, slot, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
               mc.playerController.windowClickMemory(0, slot, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player, 100);
               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.MAIN_HAND);
            }

            return slot != -1;
         }
      }
   }

   void efly() {
      if (this.canFly()) {
         if (!Minecraft.player.onGround
            && (Minecraft.player.fallDistance > 0.0F || Minecraft.player.hasNewVersionMoves && MathUtils.getDifferenceOf(Minecraft.player.motionY, 0.0) < 0.1)
            && this.boostTicks == 0
            && !Minecraft.player.isElytraFlying()) {
            badPacket();
            Minecraft.player.setFlag(7, true);
            this.boostTicks = 1;
         }

         if (!Minecraft.player.isElytraFlying()) {
            this.boostTicks = 0;
         }

         if (Minecraft.player.onGround && !Minecraft.player.isJumping() && !Minecraft.player.isInWater()) {
            Minecraft.player.motionY = 0.42;
         }

         if (Minecraft.player.ticksElytraFlying > 0 && this.boostTicks >= 0) {
            if (this.boostTicks == 1) {
               this.putFirework();
            }

            this.boostTicks++;
            if ((
                  this.boostTicks > 50
                     || Math.sqrt(
                              Minecraft.player.motionX * Minecraft.player.motionX
                                 + Minecraft.player.motionY * Minecraft.player.motionY
                                 + Minecraft.player.motionZ * Minecraft.player.motionZ
                           )
                           < 1.0
                        && this.boostTicks > 28
                        && MoveMeHelp.moveKeysPressed()
                        && (double)Minecraft.player.getCooledAttackStrength(0.0F) > 0.1
                     || Minecraft.player.isHandActive()
               )
               && (HitAura.TARGET_ROTS == null || !HitAura.cooldown.hasReached((double)(HitAura.get.msCooldown() - 50.0F)))) {
               this.boostTicks = 0;
            }
         }
      } else {
         this.toggle(false);
      }
   }

   @EventTarget
   public void onElytraRotateFly(EventElytraVector event) {
      if (event.getEntityIn() instanceof EntityPlayerSP sp
         && this.actived
         && this.Mode.currentMode.equalsIgnoreCase("Firework")
         && Minecraft.player.ticksElytraFlying > 0) {
         if (this.StrafeDirs.getBool()) {
            if (MoveMeHelp.moveKeysPressed()) {
               this.moveElytraYaw = MoveMeHelp.moveYaw(sp.rotationYaw);
               this.moveElytraPitch = MathUtils.clamp(
                  sp.rotationPitch + (float)(Minecraft.player.isSneaking() ? 45 : (Minecraft.player.isJumping() ? -45 : 0)), -90.0F, 90.0F
               );
               MoveMeHelp.setSpeed(MathUtils.clamp(MoveMeHelp.getSpeed() * (MoveMeHelp.w() && !MoveMeHelp.s() ? 1.12 : 1.4), 1.0, 1.953));
            } else {
               Minecraft.player.motionX = -0.01 + 0.02 * Math.random();
               Minecraft.player.motionZ = -0.01 + 0.02 * Math.random();
            }

            event.setVectorAsYawPitch(this.moveElytraYaw, this.moveElytraPitch);
         }

         if (this.StaticYMotions.getBool() && Minecraft.player.isElytraFlying()) {
            Minecraft.player.motionY = 0.02F;
            double currentMotion = Minecraft.player.isJumping()
               ? 1.0
               : (Minecraft.player.isSneaking() ? -1.0 : (double)(0.07F * (float)(Minecraft.player.ticksExisted % 2 * 2 - 1)));
            Entity.motiony = currentMotion;
         }
      }
   }

   public static int getItemElytra() {
      for (int i = 0; i < 45; i++) {
         ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
         if (itemStack.getItem() == Items.ELYTRA) {
            return i;
         }
      }

      return -1;
   }

   public static int getOldItem() {
      for (int i = 0; i < 45; i++) {
         ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
         if (oldSlot != null && itemStack.getItem() == oldSlot) {
            return i;
         }
      }

      return -1;
   }

   public static boolean itemOne() {
      for (int i = 0; i < 45; i++) {
         ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
         if ((itemStack.getItem() instanceof ItemAir || itemStack.getItem() == oldSlot) && itemStack.stackSize == 1) {
            return true;
         }
      }

      return false;
   }

   public static void equipElytra() {
      if (Minecraft.player.inventory.armorItemInSlot(2).getItem() != Items.air) {
         mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
      }

      mc.playerController.windowClick(0, getItemElytra(), 0, ClickType.PICKUP, Minecraft.player);
      mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
   }

   public static void dequipElytra() {
      if (Minecraft.player.inventory.armorItemInSlot(2).getItem() != Items.air) {
         mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
      }

      mc.playerController.windowClick(0, getOldItem(), 0, ClickType.PICKUP, Minecraft.player);
      mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, Minecraft.player);
   }

   public static int gasItemInHotbar(Item Item) {
      int slot = -1;

      for (int i = 0; i < 8; i++) {
         if (Item != null && Minecraft.player.inventory.getStackInSlot(i).getItem() == Item) {
            slot = i;
         }
      }

      return slot;
   }

   public static boolean equipElytra2() {
      int slot = gasItemInHotbar(Items.ELYTRA);
      if (slot != -1) {
         mc.playerController.windowClick(0, 6, slot, ClickType.SWAP, Minecraft.player);
      }

      return slot != -1;
   }

   public static boolean dequipElytra2() {
      int slot = gasItemInHotbar(oldSlot);
      if (slot != -1) {
         mc.playerController.windowClick(0, 6, slot, ClickType.SWAP, Minecraft.player);
      }

      return slot != -1;
   }

   public static void badPacket() {
      mc.getConnection().sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
   }

   public static void badPacketElytra() {
      equipElytra();

      for (int i = 0; i < 2; i++) {
         badPacket();
      }

      dequipElytra();
   }

   public static boolean canElytra() {
      for (int i = 0; i < 45; i++) {
         ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
         if (itemStack.getItem() == Items.ELYTRA) {
            return true;
         }
      }

      return Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra;
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         if (this.Mode.currentMode.equalsIgnoreCase("StrafeSync")) {
            if (canElytra()) {
               if (this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed3")) {
                  equipElytra();
                  badPacket();
               }

               this.strafeMode = Strafe.get.Mode.currentMode;
               this.strafeActived = Strafe.get.actived;
               Strafe.get.toggle(true);
               Strafe.get.Mode.currentMode = "Matrix5";
            } else {
               this.toggle(false);
            }
         }

         if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
            oldSlot = Minecraft.player.inventory.armorItemInSlot(2).getItem();
         }

         if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly")
            || this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed")
            || this.Mode.currentMode.equalsIgnoreCase("StrafeSync")) {
            this.timer.reset();
            badPacketElytra();
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("VulcanPulse")) {
         if (actived) {
            this.wait.reset();
         } else {
            deq();
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("VulcanSpeed") && canElytra() && !actived) {
         deq();
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly2") && canElytra()) {
         if (actived) {
            eq();
         } else {
            deq();
         }
      }

      hitTick = false;
      if (this.Mode.currentMode.equalsIgnoreCase("Firework")) {
         if (this.actived) {
            this.moveElytraYaw = Minecraft.player.rotationYaw;
            this.moveElytraPitch = Minecraft.player.rotationPitch;
            if (Minecraft.player.inventory.armorItemInSlot(2).getItem() != Items.air) {
               oldSlot = Minecraft.player.inventory.armorItemInSlot(2).getItem();
            }

            if (gasItemInHotbar(Items.ELYTRA) != -1) {
               equipElytra2();
            } else {
               this.toggle(false);
            }
         } else if (gasItemInHotbar(oldSlot) != -1) {
            if (Minecraft.player.getFlag(7)) {
               badPacket();
               Minecraft.player.setFlag(7, false);
            }

            dequipElytra2();
         }
      }

      if (canElytra() && !actived && this.Mode.currentMode.equalsIgnoreCase("StrafeSync")) {
         if (this.strafeActived != Strafe.get.actived) {
            Strafe.get.toggle(this.strafeActived);
         }

         Strafe.get.Mode.currentMode = this.strafeMode;
      }

      flSpeed = 0.0;
      this.curPosY = Minecraft.player.posY;
      if (canElytra()) {
         if (this.Mode.currentMode.equalsIgnoreCase("NcpFly")) {
            if (actived) {
               if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
                  if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemAir) {
                     mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                  } else {
                     mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                     mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                  }
               }
            } else if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
               mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
               mc.playerController.windowClick(0, getOldItem(), 1, ClickType.QUICK_MOVE, Minecraft.player);
            }

            MoveMeHelp.setSpeed(0.0);
            Minecraft.player.jumpMovementFactor = 0.0F;
         }

         if (this.Mode.currentMode.equalsIgnoreCase("Vanilla")) {
            if (actived && this.Mode.currentMode.equalsIgnoreCase("NcpFly")) {
               equipElytra();
            } else {
               dequipElytra();
               Minecraft.player.multiplyMotionXZ(0.14F);
            }
         }

         if (!actived && (this.Mode.currentMode.equalsIgnoreCase("MatrixFly3") || this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed3"))) {
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(0.0);
            Minecraft.player.jumpMovementFactor = 0.0F;
            Minecraft.player.motionY = 0.0;
         }
      }

      if ((
            this.Mode.currentMode.equalsIgnoreCase("MatrixFly")
               || this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed")
               || this.Mode.currentMode.equalsIgnoreCase("StrafeSync")
         )
         && !actived
         && Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra
         && canElytra()) {
         dequipElytra();
         this.timer.lastMS = 1500L;
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly2")) {
         eq();
         Minecraft.player.fallDistance = 0.1F;
         if (!this.NoTimerDefunction.getBool()) {
            mc.timer.speed = 1.0;
         }

         this.wait.reset();
         if (actived) {
            if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
               if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemAir) {
                  mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
               } else {
                  mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                  mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
               }
            }

            badPacket();
         } else {
            if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
               mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
               mc.playerController.windowClick(0, getOldItem(), 1, ClickType.QUICK_MOVE, Minecraft.player);
            }

            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(0.0);
            Minecraft.player.jumpMovementFactor = 0.0F;
            Minecraft.player.motionY = -0.228;
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("NcpFly")) {
         if (actived) {
            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
         } else {
            Minecraft.player.capabilities.isFlying = false;
            Minecraft.player.capabilities.setFlySpeed(0.05F);
            if (!Minecraft.player.capabilities.isCreativeMode) {
               Minecraft.player.capabilities.allowFlying = false;
            }
         }
      }

      super.onToggled(actived);
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      if (this.actived
         && this.Mode.currentMode.equalsIgnoreCase("Vanilla")
         && Minecraft.player.isSneaking()
         && Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
         e.ground = true;
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed3") && this.actived && canElytra() && Minecraft.player.onGround) {
         e.ground = false;
         if (!Minecraft.player.isJumping()) {
            e.setPosY(e.getPosY() + (Minecraft.player.ticksExisted % 3 == 0 ? 0.00215 : 0.0));
         }
      }
   }

   @Override
   public void onMovement() {
      if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly2") && canElytra() && Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
         MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 1.12);
      }

      if (this.Mode.currentMode.equalsIgnoreCase("NcpFly")
         && (MoveMeHelp.isBlockAboveHead() ? (double)Minecraft.player.fallDistance >= 0.06 : Minecraft.player.fallDistance != 0.0F)) {
         Entity.motiony = -1.0E-45;
         mc.getConnection().sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_FALL_FLYING));
         MoveMeHelp.setSpeed((double)this.SpeedF.getFloat() * 0.3);
         Minecraft.player.motionY = 0.0;
         Minecraft.player.setSprinting(!Minecraft.player.isElytraFlying());
      }
   }

   @Override
   public void onUpdate() {
      if (this.actived && this.Mode.currentMode.equalsIgnoreCase("Firework")) {
         this.efly();
      }

      if (this.Mode.currentMode.equalsIgnoreCase("VulcanPulse") && canElytra()) {
         if (Minecraft.player.fallDistance > 0.0F
            && (double)Minecraft.player.fallDistance < 0.12
            && Minecraft.player.inventory.armorInventory.get(2).getItem() instanceof ItemElytra) {
            badPacket();
         }

         if ((double)Minecraft.player.fallDistance > 0.3 && Minecraft.player.fallDistance < 1.0F && !Minecraft.player.getFlag(7)) {
            eq();
            this.wait.reset();
            badPacket();
            Minecraft.player.setFlag(7, true);
         }

         if (!Minecraft.player.isElytraFlying()) {
            this.wait.reset();
         }

         if (Minecraft.player.inventory.armorInventory.get(2).getItem() instanceof ItemElytra) {
            if (Minecraft.player.onGround) {
               this.wait.reset();
               if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                  if (Minecraft.player.rayGround && Minecraft.player.onGround) {
                     Minecraft.player.jump();
                  }
               } else {
                  mc.gameSettings.keyBindJump.pressed = Minecraft.player.rayGround && Minecraft.player.onGround;
               }
            } else {
               Minecraft.player.onGround = false;
               if (this.wait.hasReached(100.0) && !this.wait.hasReached(150.0)) {
                  Minecraft.player.motionY = 1.4;
                  MoveMeHelp.setSpeed(3.0);
                  MoveMeHelp.setCuttingSpeed(2.830188679245283);
               }

               if (!this.wait.hasReached(100.0)) {
                  MoveMeHelp.setSpeed(0.0);
               }
            }

            Minecraft.player.rayGround = Minecraft.player.onGround;
         } else {
            eq();
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("VulcanSpeed")) {
         if (canElytra()) {
            double speed = MoveMeHelp.getSpeed() * 2.5;
            if (speed < 1.3) {
               speed = 1.3;
            }

            if (speed > 1.93) {
               speed = 1.93;
            }

            if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
               oldSlot = Minecraft.player.inventory.armorItemInSlot(2).getItem();
            }

            if ((
                  (double)Minecraft.player.fallDistance > 0.1 && (double)Minecraft.player.fallDistance < 0.3
                     || (double)Minecraft.player.fallDistance > 1.05 && (double)Minecraft.player.fallDistance < 1.3
               )
               && !Minecraft.player.onGround
               && Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY + Entity.Getmotiony, Minecraft.player.posZ)) {
               eq();
               badPacket();
               this.boostTicks = 0;
            }

            this.boostTicks++;
            if (this.boostTicks == 1) {
               MoveMeHelp.setSpeed(speed);
               flSpeed = speed;
            } else {
               flSpeed = 0.0;
            }

            if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
               if (this.boostTicks == 1) {
                  badPacket();
               }

               if (this.boostTicks > 4) {
                  deq();
               }
            }
         } else {
            this.toggle(false);
         }
      }

      if (this.actived && this.Mode.currentMode.equalsIgnoreCase("Firework")) {
         this.efly();
      }

      if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
         oldSlot = Minecraft.player.inventory.armorItemInSlot(2).getItem();
      }

      if (this.Mode.currentMode.equalsIgnoreCase("NcpFly") && Minecraft.player.onGround) {
         Minecraft.player.motionY = 0.42F;
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed3") && this.actived) {
         if (canElytra() && Minecraft.player.fallDistance < 2.0F) {
            if (Minecraft.player.ticksExisted % 2 == 0) {
               if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
                  if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemAir) {
                     mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                  } else {
                     mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                     mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                  }
               }

               this.boostTicks++;
               badPacket();
               if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
                  mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                  mc.playerController.windowClick(0, getOldItem(), 1, ClickType.QUICK_MOVE, Minecraft.player);
               }
            }

            Minecraft.player.connection.sendPacket(new CPacketCloseWindow(0));
            if (Minecraft.player.onGround && Minecraft.player.isJumping()) {
               Minecraft.player.motionY = 1.0;
               if (!Minecraft.player.isCollidedHorizontally) {
                  Entity.motiony = 1.0E-4;
               }
            } else if (!Minecraft.player.isCollidedHorizontally) {
               Minecraft.player.motionY -= 0.1;
            }

            double speedx = MathUtils.clamp(
               MoveMeHelp.getSpeed() * (Minecraft.player.isSprinting() ? 1.1 : 1.15),
               Minecraft.player.isHandActive() && Minecraft.player.isJumping() ? 0.3 : 1.0,
               Minecraft.player.isHandActive() && Minecraft.player.isJumping() ? 0.3 : 1.6
            );
            flSpeed = speedx;
            MoveMeHelp.setSpeed(flSpeed);
            MoveMeHelp.setCuttingSpeed(flSpeed / 1.06);
         } else {
            flSpeed = 0.0;
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly3") && this.actived) {
         boolean move = MoveMeHelp.moveKeysPressed() || TargetStrafe.goStrafe();
         boolean canFly = Minecraft.player.fallDistance > 0.06F || !Minecraft.player.onGround && MathUtils.getDifferenceOf(Entity.Getmotiony, 0.0) < 0.4;
         if (!canFly && canElytra()) {
            this.boostTicks = 0;
            if (Minecraft.player.onGround && !Keyboard.isKeyDown(this.bind)) {
               Minecraft.player.motionY = 0.42;
            }

            Minecraft.player.jumpMovementFactor = 0.0F;
            MoveMeHelp.setSpeed(0.0);
            MoveMeHelp.setCuttingSpeed(0.0);
         }

         if (canElytra() && canFly) {
            if (Minecraft.player.fallDistance < 1.0F && Entity.Getmotiony > 0.15) {
               Minecraft.player.fallDistance = 1.0F;
            }

            Minecraft.player.onGround = false;
            Minecraft.player.motionY = 0.0;
            Entity.motiony = Minecraft.player.ticksExisted % 3 == 0 ? 0.05 : -0.025;
            if (Minecraft.player.ticksExisted % 2 == 0) {
               if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
                  if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemAir) {
                     mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                  } else {
                     mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                     mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
                     if (flSpeed < 0.1) {
                        hitTick = true;
                     }
                  }
               }

               badPacket();
               if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
                  mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                  mc.playerController.windowClick(0, getOldItem(), 1, ClickType.QUICK_MOVE, Minecraft.player);
               }

               this.boostTicks++;
            } else {
               hitTick = false;
            }

            float ymo = this.boostTicks > 1 ? (Minecraft.player.isJumping() ? 0.93F : 0.6F) : Float.MIN_VALUE;
            if (MoveMeHelp.getSpeed() == 0.0 && !MoveMeHelp.isMoving()) {
               ymo *= 2.0F;
            }

            double motionY = Minecraft.player.isJumping() ? (double)ymo : (Minecraft.player.isSneaking() ? (double)(-ymo) : 0.0);
            boolean can = !Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)
               && !Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - 1.15, Minecraft.player.posZ);
            if (motionY != 0.0 && can) {
               Entity.motiony = motionY;
            }

            if (!can && Minecraft.player.isJumping()) {
               Minecraft.player.onGround = true;
               Entity.motiony = Entity.Getmotiony + 0.06876;
            }

            float speedVal = this.SpeedXZ.getFloat() - (Minecraft.player.ticksExisted % 2 == 0 ? 0.0F : 0.005F);
            double speedx = (double)(speedVal - 0.046F);
            if (move) {
               if (flSpeed < (TargetStrafe.goStrafe() ? TargetStrafe.getCurrentSpeed() : MoveMeHelp.getSpeed())) {
                  flSpeed = TargetStrafe.goStrafe() ? TargetStrafe.getCurrentSpeed() : MoveMeHelp.getSpeed();
               }

               flSpeed += 0.1F;
               if (flSpeed >= speedx) {
                  flSpeed = speedx;
               }
            } else {
               flSpeed = 0.09;
            }

            MoveMeHelp.setSpeed(flSpeed, 0.6F);
            if (move) {
               if (flSpeed < 1.1 && flSpeed > 0.03) {
                  Minecraft.player.jump();
               }

               MoveMeHelp.setCuttingSpeed(flSpeed / 1.06);
            } else if (MoveMeHelp.getSpeed() < 0.1) {
               MoveMeHelp.setCuttingSpeed(0.0);
            }
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly2") && canElytra()) {
         if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
            Minecraft.player.jump();
         }

         if (Minecraft.player.fallDistance != 0.0F) {
            badPacket();
            if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra && this.wait.hasReached(150.0)) {
               double speedxx = MathUtils.clamp(MoveMeHelp.getSpeed() * 1.12, 1.0, 1.953);
               flSpeed = speedxx;
               if (MoveMeHelp.isMoving()) {
                  MoveMeHelp.setSpeed(speedxx);
                  MoveMeHelp.setCuttingSpeed(speedxx / 1.06);
               }

               Minecraft.player.motionY = Minecraft.player.isJumping() ? 0.42 : (Minecraft.player.isSneaking() ? -0.42 : 0.01);
               if (!Minecraft.player.isJumping() && !Minecraft.player.isSneaking()) {
                  Entity.motiony = 1.0E-5;
               }
            } else {
               this.wait.reset();
            }

            mc.timer.speed = Minecraft.player.fallDistance != 0.0F && (!this.NoTimerDefunction.getBool() || mc.timer.speed != 0.5) ? 0.5 : 1.0;
            if (Minecraft.player.fallDistance != 0.0F && !this.NoTimerDefunction.getBool()) {
               Timer.forceTimer(0.5F);
            }
         }
      }

      if (this.actived && this.Mode.currentMode.equalsIgnoreCase("Vanilla") && canElytra()) {
         double speedxxx = MoveMeHelp.getSpeed() < 0.2 ? 0.2499 - (Minecraft.player.ticksExisted % 2 == 0 ? 0.01 : 0.0) : MoveMeHelp.getSpeed() * 1.03;
         if (Minecraft.player.ticksExisted % 10 == 0) {
            if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
               speedxxx = (double)(this.SpeedF.getFloat() / 2.0F);
               equipElytra();
               this.wait.reset();
            }
         } else if (this.wait.hasReached(100.0)
            && !this.wait.hasReached(250.0)
            && Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
            dequipElytra();
         }

         if (MoveMeHelp.isMoving()) {
            MoveMeHelp.setSpeed(speedxxx);
         }

         Minecraft.player.motionY = Minecraft.player.isJumping() ? 1.0 : (Minecraft.player.isSneaking() ? -1.0 : 0.0);
         if (Minecraft.player.isSneaking()) {
            Minecraft.player.fallDistance = 0.1F;
            Minecraft.player.onGround = true;
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed2") && canElytra()) {
         flSpeed = 0.0;
         if (Minecraft.player.isInWater() || Minecraft.player.isInLava() || Minecraft.player.isInWeb) {
            return;
         }

         if (Minecraft.player.fallDistance != 0.0F && (double)Minecraft.player.fallDistance < 0.1 && Minecraft.player.motionY < -0.1) {
            if (!(Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra)) {
               if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemAir) {
                  mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
               } else {
                  mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
                  mc.playerController.windowClick(0, getItemElytra(), 1, ClickType.QUICK_MOVE, Minecraft.player);
               }
            }

            badPacket();
            badPacket();
            if (Minecraft.player.inventory.armorItemInSlot(2).getItem() instanceof ItemElytra) {
               mc.playerController.windowClick(0, 6, 1, ClickType.QUICK_MOVE, Minecraft.player);
               mc.playerController.windowClick(0, getOldItem(), 1, ClickType.QUICK_MOVE, Minecraft.player);
            }
         }

         boolean targetStrafe = TargetStrafe.get.actived && TargetStrafe.target != null && HitAura.get.actived;
         boolean air = AirJump.get.actived && AirJump.get.Mode.currentMode.equalsIgnoreCase("Matrix");
         int ex = air ? 2 : 1;
         double cur = targetStrafe ? TargetStrafe.getCurrentSpeed() : MoveMeHelp.getCuttingSpeed();
         double speedxxxx = MathUtils.clamp(
            cur > 10.0 ? 1.96 : cur * (air ? 1.2 : 1.4),
            0.2499 - (Minecraft.player.ticksExisted % 2 == 0 ? 0.01 : 0.0),
            (
                  Minecraft.player.isHandActive() && Minecraft.player.fallDistance > 0.0F
                     ? 1.6 - MathUtils.clamp((double)(Minecraft.player.fallDistance * 2.0F), 0.0, 1.4)
                     : 1.6
               )
               / (air ? 1.45 : 1.0)
         );
         if ((double)Minecraft.player.fallDistance >= 0.15
            && (MoveMeHelp.isMoving() || targetStrafe)
            && (
               Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - (double)ex, Minecraft.player.posZ)
                  || Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - ((double)ex + 0.2), Minecraft.player.posZ)
                  || Speed.canMatrixBoost()
            )
            && !MoveMeHelp.isBlockAboveHead()
            && !Speed.posBlock(Minecraft.player.posX, Minecraft.player.posY - ((double)ex - 0.8), Minecraft.player.posZ)) {
            if (!targetStrafe) {
               MoveMeHelp.setSpeed(speedxxxx);
               MoveMeHelp.setCuttingSpeed(speedxxxx / 1.06);
            }

            flSpeed = speedxxxx / 1.01;
         }
      }

      if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly")
         || this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed")
         || this.Mode.currentMode.equalsIgnoreCase("StrafeSync")) {
         if (canElytra()) {
            if (this.Mode.currentMode.equalsIgnoreCase("MatrixFly")) {
               boolean isDowned = false;
               boolean isFeared = false;
               if (this.timer2.hasReached(1050.0)) {
                  isDowned = true;
               }

               if (this.timer2.hasReached(1100.0)) {
                  isDowned = false;
                  isFeared = true;
                  if (this.timer2.hasReached(1200.0)) {
                     this.timer2.reset();
                     badPacketElytra();
                  }
               }

               double yaws = (double)Minecraft.player.rotationYaw * 0.017453292;
               float sp = MoveMeHelp.getSpeed() < 0.3 ? 0.02F : 0.0F;
               if (Minecraft.player.ticksExisted % 2 == 0) {
                  Minecraft.player.motionX = Minecraft.player.motionX + Math.sin(yaws + 45.0) * (double)sp * 2.0;
                  Minecraft.player.motionZ = Minecraft.player.motionZ - Math.cos(yaws + 45.0) * (double)sp * 2.0;
               }

               Minecraft.player.motionX = Minecraft.player.motionX - Math.sin(yaws + 45.0) * (double)sp;
               Minecraft.player.motionZ = Minecraft.player.motionZ + Math.cos(yaws + 45.0) * (double)sp;
               Minecraft.player.setSprinting(false);
               if (Minecraft.player.isSneaking()) {
                  if (Minecraft.player.motionY > -0.2) {
                     Minecraft.player.motionY = -0.2;
                  }

                  if (Minecraft.player.motionY < -1.0) {
                     Minecraft.player.motionY -= 0.1;
                  }
               } else {
                  Minecraft.player.jump();
               }

               if (MoveMeHelp.getSpeed() < (double)(this.SpeedF.getFloat() * 0.89F) * 0.889 && MoveMeHelp.isMoving()) {
                  MoveMeHelp.setSpeed(
                     MathUtils.clamp(
                        MoveMeHelp.getSpeed() * (double)(MoveMeHelp.getSpeed() > 2.2F ? (MoveMeHelp.getSpeed() > 7.5 ? 1.1F : 1.12F) : 1.2F),
                        0.03,
                        (double)(this.SpeedF.getFloat() * 0.89F) * 0.889
                     )
                  );
               } else {
                  Minecraft.player.motionX /= 1.02;
                  Minecraft.player.motionZ /= 1.02;
               }

               float yport = 0.0765F;
               if (!Minecraft.player.isSneaking()) {
                  if (!isDowned && Minecraft.player.isJumping() || !Minecraft.player.isJumping()) {
                     Minecraft.player.motionY = Minecraft.player.isJumping()
                        ? 0.499
                        : (
                           Minecraft.player.ticksExisted % 8 == 0
                              ? (double)(yport / 2.0F)
                              : (Minecraft.player.ticksExisted % 8 == 1 ? (double)(-yport / 2.0F) : 0.0)
                        );
                  }

                  if (isDowned && Minecraft.player.isJumping()) {
                     Minecraft.player.motionY = -0.1;
                  }

                  Minecraft.player.motionY /= isFeared ? 1.05F : 1.03F;
               }

               Minecraft.player.rotationYaw = (float)((double)Minecraft.player.rotationYaw + (Minecraft.player.ticksExisted % 2 == 0 ? 1.0E-4 : -1.0E-4));
               if (Minecraft.player.ticksExisted % 3 == 0) {
                  Minecraft.player.fallDistance = 0.0F;
               } else {
                  Minecraft.player.fallDistance = (float)(1.0 + Minecraft.player.motionY);
               }
            }

            if (this.Mode.currentMode.equalsIgnoreCase("MatrixSpeed")) {
               if (!Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && Minecraft.player.onGround) {
                  Minecraft.player.motionY = 0.1;
                  Minecraft.player.onGround = false;
               }

               if (!Minecraft.player.onGround
                  && MoveMeHelp.getSpeed() < (double)(this.SpeedF.getFloat() * 0.89F)
                  && (Minecraft.player.isMoving() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown())) {
                  MoveMeHelp.setSpeed(
                     MoveMeHelp.getSpeed() * (double)(MoveMeHelp.getSpeed() > 2.2F ? (MoveMeHelp.getSpeed() > 7.5 ? 1.1F : 1.125F) : 1.2F), 0.9F
                  );
               } else {
                  Minecraft.player.motionX /= 1.36;
                  Minecraft.player.motionZ /= 1.36;
               }

               if (MoveMeHelp.getSpeed() < 0.195) {
                  MoveMeHelp.setSpeed(0.195, 1);
               }
            }
         } else {
            this.timer.lastMS = 1360L;
         }
      }
   }

   @EventTarget
   public void onPacket(EventReceivePacket event) {
      if (this.Mode.currentMode.equalsIgnoreCase("NcpFly")
         && Minecraft.player != null
         && mc.world != null
         && !Minecraft.player.isDead
         && this.actived
         && event.getPacket() instanceof SPacketEntityVelocity) {
         event.setCancelled(true);
      }
   }
}
