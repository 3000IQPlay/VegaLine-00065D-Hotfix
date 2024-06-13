package ru.govno.client.module.modules;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import ru.govno.client.Client;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.TimerHelper;

public class StormHVHHelper extends Module {
   public static StormHVHHelper get;
   BoolSettings AutoDuel;
   BoolSettings AutoResellDuel;
   BoolSettings OnlySneakDuel;
   BoolSettings SmartDuel;
   BoolSettings AutoKitSelect;
   BoolSettings NoPlayersOnSpawn;
   ModeSettings DuelType;
   ModeSettings KitSelect;
   private static final List<StormHVHHelper.BPWID> verifyBlocks = Arrays.asList(
      new StormHVHHelper.BPWID(new BlockPos(-321, 102, 161), 252),
      new StormHVHHelper.BPWID(new BlockPos(-387, 98, 159), 38),
      new StormHVHHelper.BPWID(new BlockPos(-453, 98, 161), Block.getIdFromBlock(Blocks.PLANKS)),
      new StormHVHHelper.BPWID(new BlockPos(-417, 99, 97), 38),
      new StormHVHHelper.BPWID(new BlockPos(-255, 98, 110), 38),
      new StormHVHHelper.BPWID(new BlockPos(-244, 98, 182), 38),
      new StormHVHHelper.BPWID(new BlockPos(-375, 99, 280), 38),
      new StormHVHHelper.BPWID(new BlockPos(-422, 100, 248), 38)
   );
   boolean goSword = false;
   boolean goDuel = false;
   TimerHelper waitResell = new TimerHelper();

   public StormHVHHelper() {
      super("StormHVHHelper", 0, Module.Category.MISC);
      this.settings.add(this.AutoDuel = new BoolSettings("AutoDuel", true, this));
      this.settings.add(this.AutoResellDuel = new BoolSettings("AutoResellDuel", true, this, () -> this.AutoDuel.getBool()));
      this.settings.add(this.OnlySneakDuel = new BoolSettings("OnlySneakDuel", true, this, () -> this.AutoDuel.getBool()));
      this.settings.add(this.SmartDuel = new BoolSettings("SmartDuel", true, this, () -> this.AutoDuel.getBool()));
      String[] kits = new String[]{"Standart", "Thorns", "MSTNW", "Shield", "NetheriteOP", "Reallyworld", "Sunrise", "Crystals", "Craftyou", "Prostocraft"};
      this.settings.add(this.DuelType = new ModeSettings("DuelType", kits[7], this, kits, () -> this.AutoDuel.getBool()));
      this.settings.add(this.AutoKitSelect = new BoolSettings("AutoKitSelect", true, this));
      this.settings.add(this.KitSelect = new ModeSettings("KitSelect", "Duped", this, new String[]{"Duped", "Standart"}, () -> this.AutoKitSelect.getBool()));
      this.settings.add(this.NoPlayersOnSpawn = new BoolSettings("NoPlayersOnSpawn", true, this));
      get = this;
   }

   public static boolean noRenderPlayersInWorld() {
      return get != null && get.actived && get.NoPlayersOnSpawn.getBool();
   }

   private boolean canSelectKit(boolean dupe) {
      return Minecraft.player.inventory.getStackInSlot(dupe ? 0 : 8).getItem() == Items.ENCHANTED_BOOK;
   }

   private void selectKitAuto(boolean dupe) {
      int slot = dupe ? 0 : 8;
      if (this.canSelectKit(dupe)) {
         if (Minecraft.player.inventory.currentItem != slot) {
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(slot));
         }

         Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
         if (Minecraft.player.inventory.currentItem != slot) {
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
         }
      }
   }

   private boolean isAutoDuel() {
      return this.AutoDuel.getBool();
   }

   private boolean canClickSword() {
      return Minecraft.player.inventory.getStackInSlot(0).getItem() == Items.DIAMOND_SWORD;
   }

   private void clickSword() {
      if (this.canClickSword()) {
         Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(0));
         Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
         Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
      }
   }

   private boolean canClickRedstone() {
      return Minecraft.player.inventory.getStackInSlot(8).getItem() == Items.REDSTONE;
   }

   private void clickRedstone() {
      if (this.canClickRedstone()) {
         if (Minecraft.player.inventory.currentItem != 8) {
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(8));
         }

         Minecraft.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
         if (Minecraft.player.inventory.currentItem != 8) {
            Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
         }
      }
   }

   private boolean canClickSlot(String duelType, boolean smart) {
      return this.getSlotByDuel(duelType, smart) != -1;
   }

   private int getSlotByDuel(String duelType, boolean smart) {
      int slot = -1;
      int smartSlot = -1;

      for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); index++) {
         if (Minecraft.player.openContainer.inventorySlots.get(index).getHasStack()) {
            ItemStack stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack();
            Item item = stack.getItem();
            if (item != Items.air && item != Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE) && item != Items.ARROW && stack.stackSize == 2) {
               smartSlot = index;
            }
         }
      }

      if (smart && smartSlot != -1) {
         slot = smartSlot;
      } else {
         switch (duelType) {
            case "Standart":
               slot = 20;
               break;
            case "Thorns":
               slot = 21;
               break;
            case "MSTNW":
               slot = 22;
               break;
            case "Shield":
               slot = 23;
               break;
            case "NetheriteOP":
               slot = 24;
               break;
            case "Reallyworld":
               slot = 29;
               break;
            case "Sunrise":
               slot = 30;
               break;
            case "Crystals":
               slot = 31;
               break;
            case "Craftyou":
               slot = 32;
               break;
            case "Prostocraft":
               slot = 33;
         }
      }

      return slot;
   }

   private void clickSlot(int slot) {
      mc.playerController.windowClick(Minecraft.player.openContainer.windowId, slot, 1, ClickType.PICKUP, Minecraft.player);
      Minecraft.player.closeScreen();
      mc.currentScreen = null;
   }

   private boolean isInSelectDuelMenu() {
      boolean hasArrow1 = false;
      int countGlass = 0;
      if (mc.currentScreen instanceof GuiContainer
         && !(mc.currentScreen instanceof GuiInventory)
         && Minecraft.player.openContainer instanceof ContainerChest
         && Minecraft.player.openContainer.inventorySlots.size() == 90) {
         for (int index = 0; index < Minecraft.player.openContainer.inventorySlots.size(); index++) {
            if (Minecraft.player.openContainer.inventorySlots.get(index).getHasStack()) {
               ItemStack stack = Minecraft.player.openContainer.inventorySlots.get(index).getStack();
               Item item = stack.getItem();
               if (item == Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE) && stack.stackSize == 1) {
                  countGlass++;
               }

               if (item instanceof ItemArrow && stack.stackSize == 1) {
                  hasArrow1 = true;
               }
            }
         }
      }

      return countGlass == 24 && hasArrow1;
   }

   public static boolean isInStormServer() {
      return !mc.isSingleplayer()
         && mc.getCurrentServerData() != null
         && mc.getCurrentServerData().serverIP != null
         && mc.getCurrentServerData().serverIP.toLowerCase().contains("stormhvh");
   }

   private static boolean hasBlockVerifyInWorld(StormHVHHelper.BPWID bpwid) {
      boolean has = false;
      if (bpwid != null && bpwid.getPos() != null) {
         IBlockState state = mc.world.getBlockState(bpwid.getPos());
         if (Block.getIdFromBlock(state.getBlock()) == bpwid.getCurID()) {
            has = true;
         }
      }

      return has;
   }

   public static boolean isInStormSpawn() {
      return verifyBlocks.stream().filter(toVer -> hasBlockVerifyInWorld(toVer)).toList().size() >= 1;
   }

   @Override
   public void onUpdate() {
      if (isInStormServer() && isInStormSpawn() && noRenderPlayersInWorld() && mc.world != null) {
         for (Entity entity : mc.world.getLoadedEntityList()) {
            if (entity instanceof EntityOtherPlayerMP) {
               EntityOtherPlayerMP mp = (EntityOtherPlayerMP)entity;
               if (mp != FreeCam.fakePlayer && mp.getEntityId() != 462462998 && !Client.friendManager.isFriend(mp.getName())) {
                  mc.world.removeEntityFromWorld(mp.getEntityId());
               }
            }
         }

         mc.world.playerEntities.clear();
      }

      if (this.AutoKitSelect.getBool()
         && isInStormServer()
         && !isInStormSpawn()
         && mc.currentScreen == null
         && this.canSelectKit(this.KitSelect.currentMode.equalsIgnoreCase("Duped"))) {
         this.selectKitAuto(this.KitSelect.currentMode.equalsIgnoreCase("Duped"));
      }

      if (this.isAutoDuel()
         && isInStormServer()
         && isInStormSpawn()
         && this.canClickSword()
         && mc.currentScreen == null
         && (!this.OnlySneakDuel.getBool() || Minecraft.player.isSneaking())) {
         this.goSword = true;
      }

      if (this.goSword) {
         this.clickSword();
         this.goDuel = true;
         this.goSword = false;
      }

      if (this.goDuel && this.isInSelectDuelMenu() && this.canClickSlot(this.DuelType.currentMode, this.SmartDuel.getBool())) {
         this.clickSlot(this.getSlotByDuel(this.DuelType.currentMode, this.SmartDuel.getBool()));
      }

      if (this.AutoResellDuel.getBool()) {
         if (Minecraft.player.inventory.getStackInSlot(8).getItem() == Items.REDSTONE) {
            if (this.waitResell.hasReached(200.0)) {
               this.clickRedstone();
               this.waitResell.reset();
            }
         } else {
            this.waitResell.reset();
         }
      }

      if (!this.isInSelectDuelMenu() && (Minecraft.player.inventory.getStackInSlot(8).getItem() instanceof ItemRedstone || !isInStormSpawn())) {
         this.goDuel = false;
      }
   }

   private static class BPWID {
      private final BlockPos pos;
      private final int curID;

      public BPWID(BlockPos pos, int curID) {
         this.pos = pos;
         this.curID = curID;
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public int getCurID() {
         return this.curID;
      }
   }
}
