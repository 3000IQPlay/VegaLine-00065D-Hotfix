package ru.govno.client.module.modules;

import dev.intave.NewPhisicsFixes;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Mouse;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;

public class MiddleClick extends Module {
   public static MiddleClick get;
   public BoolSettings NoHitFriend;
   public BoolSettings MCFriend;
   public BoolSettings MCPearl;
   public BoolSettings PearlFromInventory;
   public BoolSettings PearlBlockInject;
   public BoolSettings MCExpSneakPitch;
   public BoolSettings MCExpMega;
   TimerHelper timerHelper = new TimerHelper();
   private boolean callThrowPearl;
   private boolean callThrowPearl2;
   private float lastEventYaw;
   private float lastEventPitch;

   public MiddleClick() {
      super("MiddleClick", 0, Module.Category.MISC);
      this.settings.add(this.NoHitFriend = new BoolSettings("NoHitFriend", true, this));
      this.settings.add(this.MCFriend = new BoolSettings("MCFriend", true, this));
      this.settings.add(this.MCPearl = new BoolSettings("MCPearl", true, this));
      this.settings.add(this.PearlFromInventory = new BoolSettings("PearlFromInventory", true, this, () -> this.MCPearl.getBool()));
      this.settings.add(this.PearlBlockInject = new BoolSettings("PearlBlockInject", true, this, () -> this.MCPearl.getBool()));
      this.settings.add(this.MCExpSneakPitch = new BoolSettings("MCExpSneakPitch", true, this));
      this.settings.add(this.MCExpMega = new BoolSettings("MCExpMega", true, this, () -> this.MCExpSneakPitch.getBool()));
      get = this;
   }

   private boolean hasEXPFarm() {
      return this.MCExpSneakPitch.getBool()
         && Minecraft.player.isSneaking()
         && Minecraft.player.rotationPitch > 83.0F
         && (InventoryUtil.getItemInHotbar(Items.EXPERIENCE_BOTTLE) != -1 || Minecraft.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE);
   }

   private void callThrowPearl() {
      this.callThrowPearl = true;
   }

   private int getPearlSlot() {
      if (Minecraft.player.getHeldItemOffhand().getItem() instanceof ItemEnderPearl) {
         mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.OFF_HAND);
         return -2;
      } else {
         int s = -1;

         for (int i = 0; i <= (this.PearlFromInventory.getBool() ? 36 : 8); i++) {
            if (Minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemEnderPearl) {
               s = i;
               break;
            }
         }

         return s;
      }
   }

   private void updateThrowPearl(EventPlayerMotionUpdate event) {
      if (this.callThrowPearl2) {
         this.throwPearl();
         this.callThrowPearl2 = false;
      }

      if (this.callThrowPearl) {
         event.setYaw(Minecraft.player.rotationYaw);
         event.setPitch(Minecraft.player.rotationPitch);
         HitAura.get.noRotateTick = true;
         RotationUtil.Yaw = Minecraft.player.rotationYaw;
         RotationUtil.Pitch = Minecraft.player.rotationPitch;
         HitAura.get.rotations = new float[]{Minecraft.player.rotationYaw, Minecraft.player.rotationPitch};
         Minecraft.player.rotationYawHead = Minecraft.player.rotationYaw;
         Minecraft.player.rotationPitchHead = Minecraft.player.rotationPitch;
         this.callThrowPearl = false;
         this.callThrowPearl2 = true;
      }
   }

   private void throwPearl() {
      if (!this.timerHelper.hasReached(1000.0) && !NewPhisicsFixes.isOldVersion()) {
         ClientTune.get.playMiddleMouseSong();
      } else {
         int slot = this.getPearlSlot();
         if (slot != -1) {
            boolean off = slot == -2;
            if (off) {
               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.OFF_HAND);
            } else {
               boolean inv = slot > 8;
               Minecraft.player.activeItemStack = Minecraft.player.inventory.getStackInSlot(Minecraft.player.inventory.currentItem);
               int oldSlot = Minecraft.player.inventory.currentItem;
               if (inv) {
                  mc.playerController.windowClick(0, slot, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
               } else {
                  Minecraft.player.inventory.currentItem = slot;
                  mc.playerController.syncCurrentPlayItem();
               }

               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.MAIN_HAND);
               this.timerHelper.reset();
               if (inv) {
                  mc.playerController.windowClickMemory(0, slot, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player, 100);
               } else {
                  Minecraft.player.inventory.currentItem = oldSlot;
                  mc.playerController.syncCurrentPlayItem();
               }
            }
         }
      }
   }

   @Override
   public void onMouseClick(int mouseButton) {
      if (mouseButton == 2 && Minecraft.player != null && (this.MCFriend.getBool() || this.MCPearl.getBool()) && !this.hasEXPFarm() && mc.currentScreen == null
         )
       {
         if (this.MCFriend.getBool() && mc.pointedEntity != null) {
            if (mc.pointedEntity instanceof EntityLivingBase) {
               String name = mc.pointedEntity.getDisplayName() == null ? mc.pointedEntity.getName() : mc.pointedEntity.getDisplayName().getUnformattedText();
               if (Client.friendManager.getFriends().stream().anyMatch(paramFriend -> paramFriend.getName().equals(mc.pointedEntity.getName()))) {
                  Client.friendManager.getFriends().remove(Client.friendManager.getFriend(mc.pointedEntity.getName()));
                  Client.msg("§f§lModules:§r §7[§lMiddleClick§r§7]: §r" + name + " §cудалён из друзей!§r", false);
                  if (Notifications.get.actived) {
                     Notifications.Notify.spawnNotify(name, Notifications.type.FDEL);
                  }

                  ClientTune.get.playFriendUpdateSong(false);
               } else {
                  Client.friendManager.addFriend(mc.pointedEntity.getName());
                  Client.msg("§f§lModules:§r §7[§lMiddleClick§r§7]: §r" + name + " §aдобавлен в друзья!§r", false);
                  if (Notifications.get.actived) {
                     Notifications.Notify.spawnNotify(name, Notifications.type.FADD);
                  }

                  ClientTune.get.playFriendUpdateSong(true);
               }
            }

            return;
         }

         if (this.MCPearl.getBool()) {
            double yawDiff = MathUtils.getDifferenceOf(Minecraft.player.rotationYaw, HitAura.get.rotations[0]);
            double pitchDiff = MathUtils.getDifferenceOf(Minecraft.player.rotationPitch, HitAura.get.rotations[1]);
            double rotDiff = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
            if (rotDiff > 1.0) {
               this.callThrowPearl();
            } else {
               this.throwPearl();
            }
         }
      }
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      if (this.isActived()) {
         this.lastEventYaw = e.getYaw();
         this.lastEventPitch = e.getPitch();
         this.updateThrowPearl(e);
      }
   }

   @Override
   public void onUpdate() {
      if (this.hasEXPFarm() && Mouse.isButtonDown(2)) {
         if (Minecraft.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            for (int c = 0; c < (this.MCExpMega.getBool() ? 3 : 1); c++) {
               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.OFF_HAND);
            }

            return;
         }

         int slot = InventoryUtil.getItemInHotbar(Items.EXPERIENCE_BOTTLE);
         boolean last = Minecraft.player.inventory.getStackInSlot(slot).stackSize == 1;
         if (slot != -1) {
            boolean inHand = Minecraft.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE;
            int oldSlot = Minecraft.player.inventory.currentItem;
            if (!inHand) {
               Minecraft.player.inventory.currentItem = slot;
               mc.playerController.syncCurrentPlayItem();
            }

            for (int c = 0; c < (this.MCExpMega.getBool() ? 3 : 1); c++) {
               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.MAIN_HAND);
            }

            if (!inHand) {
               Minecraft.player.inventory.currentItem = oldSlot;
               mc.playerController.syncCurrentPlayItem();
            }
         }

         if (last) {
            for (int i = 9; i < 44; i++) {
               if (Minecraft.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                  i = i < 9 && i != -1 ? i + 36 : i;
                  mc.playerController.windowClick(0, i, 1, ClickType.QUICK_MOVE, Minecraft.player);
                  break;
               }
            }
         }
      }
   }
}
