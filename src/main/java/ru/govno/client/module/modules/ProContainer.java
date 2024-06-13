package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.utils.Math.TimerHelper;

public class ProContainer extends Module {
   public static ProContainer get;
   public BoolSettings ContainerInfo;
   public BoolSettings MouseTweaks;
   public BoolSettings QuickSwap;
   public BoolSettings ScrollItems;
   public BoolSettings CraftSlotsSafe;
   public BoolSettings CraftSlotsManager;
   public BoolSettings CtrlRDroper;
   public BoolSettings AutoArmor;
   public BoolSettings StackUnlimit;
   public BoolSettings InvDesyncFix;
   public BoolSettings AllowUiParagraph;
   public BoolSettings BetterSwapHands;
   public BoolSettings HandElytraSwap;
   public static boolean allowParagraphToRepairUi;
   public static boolean autoArmorOFF;
   public final TimerHelper timer = new TimerHelper();
   boolean autoArmorIgnoreForElytra;
   public short action;

   public ProContainer() {
      super("ProContainer", 0, Module.Category.PLAYER);
      this.settings.add(this.ContainerInfo = new BoolSettings("ContainerInfo", true, this));
      this.settings.add(this.MouseTweaks = new BoolSettings("MouseTweaks", true, this));
      this.settings.add(this.QuickSwap = new BoolSettings("QuickSwap", true, this));
      this.settings.add(this.ScrollItems = new BoolSettings("ScrollItems", true, this));
      this.settings.add(this.CraftSlotsSafe = new BoolSettings("CraftSlotsSafe", false, this));
      this.settings.add(this.CraftSlotsManager = new BoolSettings("CraftSlotsManager", false, this, () -> this.CraftSlotsSafe.getBool()));
      this.settings.add(this.CtrlRDroper = new BoolSettings("CtrlRDroper", false, this));
      this.settings.add(this.AutoArmor = new BoolSettings("AutoArmor", false, this));
      this.settings.add(this.StackUnlimit = new BoolSettings("StackUnlimit", true, this));
      this.settings.add(this.InvDesyncFix = new BoolSettings("InvDesyncFix", true, this));
      this.settings.add(this.AllowUiParagraph = new BoolSettings("AllowUiParagraph", true, this));
      this.settings.add(this.BetterSwapHands = new BoolSettings("BetterSwapHands", true, this));
      this.settings.add(this.HandElytraSwap = new BoolSettings("HandElytraSwap", true, this));
      get = this;
   }

   public void onKey(int key) {
      if (this.BetterSwapHands.getBool() && key == mc.gameSettings.keyBindSwapHands.getKeyCode()) {
         Minecraft.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
         ItemStack main = Minecraft.player.getHeldItemMainhand();
         ItemStack off = Minecraft.player.getHeldItemOffhand();
         Minecraft.player.setHeldItem(EnumHand.MAIN_HAND, off);
         Minecraft.player.setHeldItem(EnumHand.OFF_HAND, main);
         OffHand.oldSlot = null;
      }
   }

   @EventTarget
   public void onPacket(EventSendPacket event) {
      if (this.actived || Minecraft.player != null) {
         if (this.InvDesyncFix.getBool() && event.getPacket() instanceof SPacketHeldItemChange change) {
            int slot = change.getHeldItemHotbarIndex();
            if (slot < 0 || slot > 8) {
               Minecraft.player.connection.preSendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
               event.cancel();
            }
         }

         if (event.getPacket() instanceof CPacketCloseWindow win && this.CraftSlotsSafe.getBool() && win.windowId == 0) {
            event.setCancelled(true);
         }
      }
   }

   @Override
   public void onUpdate() {
      if (Minecraft.player != null) {
         if (Minecraft.player.openContainer instanceof ContainerPlayer && this.CraftSlotsSafe.getBool() && this.CraftSlotsManager.getBool()) {
            List<Integer> emptyItemSlotsINV = new ArrayList<>();
            List<Integer> notEmptyItemSlotsINV = new ArrayList<>();
            List<Slot> inventorySlots = Minecraft.player.inventoryContainer.inventorySlots;

            for (int Islot = 9; Islot < 44; Islot++) {
               Slot slot = inventorySlots.get(Islot);
               int slotNum = slot.slotNumber;
               ItemStack stackInSlot = slot.getStack();
               Item itemInStack = stackInSlot.getItem();
               if (itemInStack instanceof ItemAir) {
                  emptyItemSlotsINV.add(slotNum);
               } else {
                  notEmptyItemSlotsINV.add(slot.slotNumber);
               }
            }

            List<Integer> notEmptyItemSlotsCRAFT = new ArrayList<>();
            List<Integer> emptyItemSlotsCRAFT = new ArrayList<>();

            for (int Islotx = 4; Islotx > 0; Islotx--) {
               Slot slot = inventorySlots.get(Islotx);
               ItemStack stackInSlot = slot.getStack();
               Item itemInStack = stackInSlot.getItem();
               if (itemInStack instanceof ItemAir) {
                  emptyItemSlotsCRAFT.add(slot.slotNumber);
               } else {
                  notEmptyItemSlotsCRAFT.add(slot.slotNumber);
               }
            }

            List<EntityItem> entityItemsToPickup = mc.world
               .getLoadedEntityList()
               .stream()
               .filter(entity -> entity instanceof EntityItem)
               .map(entity -> (EntityItem)entity)
               .filter(Objects::nonNull)
               .filter(
                  eItem -> !eItem.cannotPickup()
                        && eItem.ticksExisted > 10
                        && eItem.delayBeforeCanPickup == 0
                        && (eItem.getOwner() == null || 6000 - eItem.getAge() <= 200)
                        && (
                           Minecraft.player.getDistanceToEntity(eItem) <= 1.0F + Minecraft.player.width / 2.0F
                              || Minecraft.player.getDistanceAtEye(eItem.posX, eItem.posY, eItem.posZ) <= (double)(1.0F + Minecraft.player.width / 2.0F)
                        )
               )
               .collect(Collectors.toList());
            if (!entityItemsToPickup.isEmpty()
               && !emptyItemSlotsCRAFT.isEmpty()
               && !notEmptyItemSlotsINV.isEmpty()
               && emptyItemSlotsINV.size() < entityItemsToPickup.size()) {
               mc.playerController.windowClick(0, notEmptyItemSlotsINV.get(0), 0, ClickType.PICKUP, Minecraft.player);
               mc.playerController.windowClick(0, emptyItemSlotsCRAFT.get(0), 0, ClickType.PICKUP, Minecraft.player);
            }

            if (entityItemsToPickup.isEmpty() && !notEmptyItemSlotsCRAFT.isEmpty() && !emptyItemSlotsINV.isEmpty()) {
               mc.playerController.windowClick(0, notEmptyItemSlotsCRAFT.get(0), 1, ClickType.QUICK_MOVE, Minecraft.player);
            }
         }

         boolean allowParagraph = mc.currentScreen instanceof GuiRepair && this.AllowUiParagraph.getBool();
         if (allowParagraphToRepairUi != allowParagraph) {
            allowParagraphToRepairUi = allowParagraph;
         }

         if (Mouse.isButtonDown(1)
            && (mc.rightClickDelayTimer == 4 || mc.rightClickDelayTimer == 0)
            && mc.currentScreen == null
            && Minecraft.player.openContainer instanceof ContainerPlayer
            && this.currentBooleanValue("HandElytraSwap")) {
            ItemStack stackInHand = Minecraft.player.inventory.getCurrentItem();
            ItemStack stackOnChestplate = Minecraft.player.inventory.armorItemInSlot(2);
            Item itemInHand = stackInHand.getItem();
            Item itemOnChestplate = stackOnChestplate.getItem();
            if (itemInHand instanceof ItemArmor && itemOnChestplate instanceof ItemElytra
               || itemInHand instanceof ItemElytra && itemOnChestplate instanceof ItemArmor) {
               this.autoArmorIgnoreForElytra = itemInHand instanceof ItemElytra;
               if (this.autoArmorIgnoreForElytra) {
                  this.timer.reset();
               }

               int mouseKeyToWinClick = stackOnChestplate.stackSize == 1 ? 1 : 0;
               mc.playerController.windowClick(0, 6, mouseKeyToWinClick, ClickType.PICKUP, Minecraft.player);
               mc.playerController.processRightClick(Minecraft.player, mc.world, EnumHand.MAIN_HAND);
               mc.playerController
                  .windowClickMemory(0, Minecraft.player.inventory.currentItem + 36, mouseKeyToWinClick, ClickType.PICKUP, Minecraft.player, 100);
               mc.gameSettings.keyBindUseItem.pressed = false;
               Minecraft.player.resetActiveHand();
               mc.playerController.onStoppedUsingItem(Minecraft.player);
            }
         }

         if (this.autoArmorIgnoreForElytra && !this.HandElytraSwap.getBool()) {
            this.autoArmorIgnoreForElytra = false;
         }

         if (this.InvDesyncFix.getBool()
            && (Minecraft.player.inventory.currentItem < 0 || Minecraft.player.inventory.currentItem > 8 || Minecraft.player.ticksExisted == 1)) {
            Minecraft.player.inventory.currentItem = 0;
         }

         if ((mc.currentScreen == null || mc.currentScreen instanceof GuiInventory)
            && !autoArmorOFF
            && this.AutoArmor.getBool()
            && (!ElytraBoost.get.actived || !ElytraBoost.canElytra())) {
            if (!this.timer.hasReached(200.0)) {
               return;
            }

            this.timer.reset();
            InventoryPlayer var12 = Minecraft.player.inventory;
            int[] var14 = new int[4];
            int[] var17 = new int[4];

            for (int var20 = 0; var20 < 4; var20++) {
               var14[var20] = -1;
               ItemStack stack = var12.armorItemInSlot(var20);
               if (!isNullOrEmpty(stack) && stack.getItem() instanceof ItemArmor item) {
                  var17[var20] = this.getArmorValue(item, stack);
               }
            }

            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = var12.getStackInSlot(slot);
               if (!isNullOrEmpty(stack)) {
                  Item var35 = stack.getItem();
                  if (var35 instanceof ItemArmor) {
                     ItemArmor item = (ItemArmor)var35;
                     int armorType = item.armorType.getIndex();
                     int armorValue = this.getArmorValue(item, stack);
                     if (armorValue > var17[armorType]) {
                        var14[armorType] = slot;
                        if (this.autoArmorIgnoreForElytra && var12.armorInventory.get(armorType).getItem() instanceof ItemElytra) {
                           var14[armorType] = -1;
                        }

                        var17[armorType] = armorValue;
                     }
                  }
               }
            }

            ArrayList<Integer> types = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
            Collections.shuffle(types);

            for (int i : types) {
               int j = var14[i];
               if (j != -1) {
                  ItemStack oldArmor = var12.armorItemInSlot(i);
                  if (isNullOrEmpty(oldArmor) || var12.getFirstEmptyStack() != -1) {
                     if (j < 9) {
                        j += 36;
                     }

                     if (!isNullOrEmpty(oldArmor)) {
                        mc.playerController.windowClick(0, 8 - i, 1, ClickType.QUICK_MOVE, Minecraft.player);
                     }

                     mc.playerController.windowClick(0, j, 1, ClickType.QUICK_MOVE, Minecraft.player);
                     break;
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacket(EventReceivePacket event) {
      if (this.actived && Minecraft.player != null) {
         if (this.InvDesyncFix.getBool()) {
            if (event.getPacket() instanceof SPacketConfirmTransaction wrapper) {
               Container inventory = Minecraft.player.inventoryContainer;
               if (inventory != null && wrapper != null && wrapper.getWindowId() == inventory.windowId) {
                  this.action = wrapper.getActionNumber();
                  if (this.action > 0 && this.action < inventory.transactionID) {
                     inventory.transactionID = (short)(this.action + 1);
                  }
               }
            }

            if (event.getPacket() instanceof SPacketHeldItemChange held && held.getHeldItemHotbarIndex() != Minecraft.player.inventory.currentItem) {
               Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(held.getHeldItemHotbarIndex()));
               Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
               event.cancel();
            }
         }
      }
   }

   private int getArmorValue(ItemArmor item, ItemStack stack) {
      int armorPoints = item.damageReduceAmount;
      int prtPoints = 0;
      int armorToughness = (int)item.toughness;
      int armorType = item.getArmorMaterial().getDamageReductionAmount(item.armorType);
      Enchantment protection = Enchantments.PROTECTION;
      int prtLvl = EnchantmentHelper.getEnchantmentLevel(protection, stack);
      DamageSource dmgSource = DamageSource.causePlayerDamage(Minecraft.player);
      prtPoints = protection.calcModifierDamage(prtLvl, dmgSource);
      return armorPoints * 5 + prtPoints * 3 + armorToughness + armorType;
   }

   public static boolean isNullOrEmpty(ItemStack stack) {
      return stack == null || stack.isEmpty();
   }

   @Override
   public void onToggled(boolean actived) {
      this.timer.reset();
      allowParagraphToRepairUi = false;
      super.onToggled(actived);
   }

   private void syncSlot() {
      Minecraft.player.connection.sendPacket(new CPacketHeldItemChange((Minecraft.player.inventory.currentItem + 1) % 9));
      Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
      mc.playerController.syncCurrentPlayItem();
   }
}
