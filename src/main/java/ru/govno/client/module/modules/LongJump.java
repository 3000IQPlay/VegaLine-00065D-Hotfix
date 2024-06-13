package ru.govno.client.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Wrapper;
import ru.govno.client.utils.Combat.EntityUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;
import ru.govno.client.utils.Movement.MovementHelper;

public class LongJump extends Module {
   public static boolean isFallDamage;
   private int ticks;
   private double packetMotionY;
   private float speed;
   public static boolean doSpeed;
   public static boolean doBow;
   public static boolean stopBow;
   public static Item oldSlot = null;
   private final TimerHelper timerHelper = new TimerHelper();
   public static LongJump get;
   public ModeSettings Type;
   public BoolSettings AutoBow;

   public LongJump() {
      super("LongJump", 0, Module.Category.MOVEMENT);
      this.settings
         .add(this.Type = new ModeSettings("Type", "LongJump", this, new String[]{"LongJump", "BowBoost", "Solid", "DamageFly", "InstantLong", "FlagBoost"}));
      this.settings.add(this.AutoBow = new BoolSettings("AutoBow", true, this, () -> this.Type.currentMode.equalsIgnoreCase("DamageFly")));
      get = this;
   }

   void flagHop() {
      Minecraft.player.motionY = 0.4229;
      MoveMeHelp.setSpeed(1.953);
   }

   @EventTarget
   public void onReceivePacket(EventReceivePacket event) {
      if (event.getPacket() instanceof SPacketPlayerPosLook look && this.Type.currentMode.equalsIgnoreCase("FlagBoost")) {
         Minecraft.player.setPosition(look.getX(), look.getY(), look.getZ());
         Minecraft.player.connection.sendPacket(new CPacketConfirmTeleport(look.getTeleportId()));
         this.flagHop();
         event.setCancelled(true);
      }

      if (!isFallDamage) {
         if (event.getPacket() instanceof SPacketEntityVelocity && ((SPacketEntityVelocity)event.getPacket()).getEntityID() == Minecraft.player.getEntityId()) {
            this.packetMotionY = (double)((SPacketEntityVelocity)event.getPacket()).motionY / 8000.0;
         }

         if (event.getPacket() instanceof SPacketEntityStatus sPacketEntityStatus
            && sPacketEntityStatus.getOpCode() == 2
            && sPacketEntityStatus.getEntity(mc.world) == Minecraft.player) {
            doSpeed = true;
         }
      } else {
         EntityLivingBase.isMatrixDamaged = false;
         doSpeed = false;
         isFallDamage = false;
         stopBow = true;
      }
   }

   @Override
   public void onUpdate() {
      if (this.Type.currentMode.equalsIgnoreCase("FlagBoost")) {
         if (Minecraft.player.motionY != -0.0784000015258789) {
            this.timerHelper.reset();
         }

         if (!MoveMeHelp.isMoving()) {
            this.timerHelper.setTime(this.timerHelper.getCurrentMS() + 50L);
         }

         if (this.timerHelper.hasReached(100.0)) {
            this.flagHop();
            Entity.motiony = 1.0;
         }
      }

      if (this.Type.currentMode.equalsIgnoreCase("InstantLong") && Minecraft.player.hurtTime == 7) {
         MoveMeHelp.setCuttingSpeed(6.603774F);
         Minecraft.player.motionY = 0.42;
      }

      if (this.Type.currentMode.equalsIgnoreCase("BowBoost")) {
         if (Minecraft.player.onGround && doSpeed) {
            float dir1 = (float)(-Math.sin((double)MovementHelper.getDirection())) * (float)(mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
            float dir2 = (float)Math.cos((double)MovementHelper.getDirection()) * (float)(mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
            if (MovementHelper.isMoving() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()) {
               if (MoveMeHelp.getSpeed() < 0.08) {
                  MoveMeHelp.setSpeed(0.42);
               } else {
                  Minecraft.player.addVelocity((double)dir1 * 9.8 / 25.0, 0.0, (double)dir2 * 9.8 / 25.0);
                  MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
               }
            } else if (Minecraft.player.isInWater()) {
               Minecraft.player.addVelocity((double)dir1 * 8.5 / 25.0, 0.0, (double)dir2 * 9.5 / 25.0);
               MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
            } else if (!Minecraft.player.onGround) {
               if (MoveMeHelp.getSpeed() < 0.22) {
                  MoveMeHelp.setSpeed(0.22);
               } else {
                  MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * (Minecraft.player.isMoving() ? 1.0082 : 1.0088));
               }
            }

            if (mc.gameSettings.keyBindJump.isKeyDown() && MoveMeHelp.getSpeed() > 0.7 && Minecraft.player.fallDistance == 0.0F) {
               MoveMeHelp.setSpeed(0.7);
            }
         } else {
            MoveMeHelp.setCuttingSpeed(0.0);
         }
      }

      if (this.Type.currentMode.equalsIgnoreCase("LongJump")) {
         if (EntityLivingBase.isMatrixDamaged) {
            Minecraft.player.speedInAir = 0.3F;
         } else if (Minecraft.player.speedInAir == 0.3F) {
            Minecraft.player.speedInAir = 0.02F;
         }
      }

      if (this.Type.currentMode.equalsIgnoreCase("Solid")) {
         if (Minecraft.player.onGround) {
            this.ticks++;
         } else {
            this.ticks = 0;
         }

         if (EntityLivingBase.isMatrixDamaged) {
            Minecraft.player.stepHeight = 0.0F;
            if (this.ticks > 1
               && MoveMeHelp.getSpeed() < 1.2
               && !mc.world.getCollisionBoxes(Minecraft.player, Minecraft.player.getEntityBoundingBox().offset(0.0, Minecraft.player.motionY, 0.0)).isEmpty()) {
               float dir1x = (float)(-Math.sin((double)MovementHelper.getDirection())) * (float)(mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
               float dir2x = (float)Math.cos((double)MovementHelper.getDirection()) * (float)(mc.gameSettings.keyBindBack.isKeyDown() ? -1 : 1);
               if (MovementHelper.isMoving() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()) {
                  if (MoveMeHelp.getSpeed() < 0.08) {
                     MoveMeHelp.setSpeed(0.42);
                  } else {
                     Minecraft.player.addVelocity((double)dir1x * 9.8 / 25.0, 0.0, (double)dir2x * 9.8 / 25.0);
                     MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
                  }
               } else if (Minecraft.player.isInWater()) {
                  Minecraft.player.addVelocity((double)dir1x * 8.5 / 15.0, 0.0, (double)dir2x * 9.5 / 15.0);
                  MoveMeHelp.setSpeed(MoveMeHelp.getSpeed());
               } else if (!Minecraft.player.onGround) {
                  if (MoveMeHelp.getSpeed() < 0.22) {
                     MoveMeHelp.setSpeed(0.22);
                  } else {
                     MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * (Minecraft.player.isMoving() ? 1.0082 : 1.0088));
                  }
               }

               if (mc.gameSettings.keyBindJump.isKeyDown() && MoveMeHelp.getSpeed() > 0.7 && Minecraft.player.fallDistance == 0.0F) {
                  MoveMeHelp.setSpeed(0.7);
               }
            } else if (Speed.canMatrixBoost()) {
               MoveMeHelp.setSpeed(MoveMeHelp.getSpeed() * 2.0);
            }

            if (this.timerHelper.hasReached(1350.0)) {
               doSpeed = false;
               Minecraft.player.stepHeight = 0.6F;
               Minecraft.player.speedInAir = 0.02F;
               this.timerHelper.reset();
               mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
            }
         }
      }
   }

   public int oldSlot() {
      for (int i = 0; i < 9; i++) {
         ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
         if (itemStack.getItem() == oldSlot) {
            return i;
         }
      }

      return -1;
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      if (this.Type.currentMode.equalsIgnoreCase("DamageFly") && this.actived) {
         if (this.AutoBow.getBool() && !stopBow && !doSpeed) {
            if (!stopBow) {
               for (int i = 0; i < 9; i++) {
                  if (Minecraft.player.inventory.currentItem != EntityUtil.getBowAtHotbar() && !doBow) {
                     oldSlot = Minecraft.player.inventoryContainer.getSlot(Minecraft.player.inventory.currentItem).getStack().getItem();
                  }

                  if (Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemBow && !doSpeed && doBow) {
                     Minecraft.player.inventory.currentItem = EntityUtil.getBowAtHotbar();
                     EventPlayerMotionUpdate.pitch = -90.0F;
                     Minecraft.player.rotationPitchHead = EventPlayerMotionUpdate.pitch;
                  }
               }

               if (Minecraft.player.inventory.currentItem == EntityUtil.getBowAtHotbar() && !doSpeed && doBow && e.getPitch() == -90.0F) {
                  mc.gameSettings.keyBindUseItem.pressed = (double)Minecraft.player.getItemInUseMaxCount() < 2.1;
                  if ((double)Minecraft.player.getItemInUseMaxCount() >= 2.1) {
                     doBow = false;
                  }
               }
            }

            if (!doBow && oldSlot != null && Minecraft.player.inventory.currentItem != this.oldSlot()) {
               Minecraft.player.inventory.currentItem = this.oldSlot();
               oldSlot = null;
               stopBow = true;
            }
         }

         if (!doSpeed && this.AutoBow.getBool() && Minecraft.player.onGround) {
            MoveMeHelp.setSpeed(0.0);
            e.ground = Minecraft.player.onGround;
            Minecraft.player.onGround = false;
            Minecraft.player.jumpMovementFactor = 0.0F;
            doBow = true;
         }

         if (doSpeed && !MoveMeHelp.isBlockAboveHead()) {
            stopBow = false;
            this.ticks = 0;
            if (this.AutoBow.getBool()) {
               doBow = false;
            }

            if (EntityLivingBase.isMatrixDamaged) {
               if (Minecraft.player.onGround && !Minecraft.player.isJumping()) {
                  Minecraft.player.jump();
               }

               if (!doBow) {
                  Minecraft.player.motionY = Minecraft.player.onGround ? 0.42 : this.packetMotionY;
                  Minecraft.player.jumpMovementFactor = 0.415F;
                  MoveMeHelp.setCuttingSpeed(MoveMeHelp.getSpeed() / 1.06);
                  stopBow = false;
               }
            } else if (doSpeed) {
               doSpeed = false;
               if (Minecraft.player.onGround) {
                  doBow = true;
               }
            }
         }
      }

      if (this.Type.currentMode.equalsIgnoreCase("BowBoost") && this.actived) {
         this.speed = MathUtils.lerp(this.speed, doSpeed ? 0.8F : 0.0F, 0.2F);

         for (int i = 0; i < 9; i++) {
            if (Minecraft.player.inventory.currentItem != EntityUtil.getBowAtHotbar() && !doBow) {
               oldSlot = Minecraft.player.inventoryContainer.getSlot(Minecraft.player.inventory.currentItem).getStack().getItem();
            }

            if (Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemBow && !doSpeed && doBow) {
               Minecraft.player.inventory.currentItem = EntityUtil.getBowAtHotbar();
            }
         }

         if (!doBow && oldSlot != null && Minecraft.player.inventory.currentItem != this.oldSlot()) {
            Minecraft.player.inventory.currentItem = this.oldSlot();
            oldSlot = null;
         }

         if (Minecraft.player.inventory.currentItem == EntityUtil.getBowAtHotbar() && !doSpeed && doBow) {
            mc.gameSettings.keyBindUseItem.pressed = Minecraft.player.getItemInUseMaxCount() < 4;
            if ((double)Minecraft.player.getItemInUseMaxCount() > 2.5) {
               EventPlayerMotionUpdate.pitch = Wrapper.getPlayer().isPotionActive(Potion.getPotionById(1)) ? -30.0F : -45.0F;
               Minecraft.player.rotationPitchHead = EventPlayerMotionUpdate.pitch;
            }
         }

         if ((double)Minecraft.player.getItemInUseMaxCount() > 3.5) {
            doBow = false;
         }

         if (doBow && Minecraft.player.hurtTime != 0) {
            mc.gameSettings.keyBindUseItem.pressed = false;
            doBow = false;
         }

         if (Minecraft.player.hurtTime != 0) {
            doSpeed = true;
            if (Minecraft.player.hurtTime > 7) {
               this.timerHelper.reset();
            }
         }

         if (doSpeed) {
            MoveMeHelp.setSpeed(doSpeed ? (double)this.speed : 0.0);
         }

         if (this.timerHelper.hasReached(1300.0)) {
            doSpeed = false;
            if (this.timerHelper.hasReached(1460.0) && mc.gameSettings.keyBindForward.isKeyDown()) {
               doBow = true;
               this.timerHelper.reset();
            }
         }
      }
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByMode(this.Type.currentMode);
   }

   @Override
   public void onToggled(boolean actived) {
      if (!actived) {
         stopBow = false;
         this.ticks = 0;
         isFallDamage = false;
         mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
         if (this.Type.currentMode.equalsIgnoreCase("BowBoost")) {
            mc.gameSettings.keyBindUseItem.pressed = false;
         }

         oldSlot = null;
         doSpeed = false;
         doBow = false;
         Minecraft.player.stepHeight = 0.6F;
         Minecraft.player.speedInAir = 0.02F;
      }

      super.onToggled(actived);
   }
}
