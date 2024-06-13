package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;

public class ChestStealer extends Module {
   final TimerHelper timerLoot = new TimerHelper();
   final TimerHelper timeSeen = new TimerHelper();
   private final Random RANDOM = new Random();
   FloatSettings Delay;
   BoolSettings SilentWindow;
   BoolSettings IgnoreCustomItems;
   BoolSettings RandomSlots;
   BoolSettings ChestAura;
   BoolSettings CheckCooldown;
   private final ArrayList<ChestStealer.ItemStackWithSlot> stacks = new ArrayList<>();
   private boolean hasLootProcess;
   private boolean lastSlot;
   List<BlockPos> opennedChestPoses = new ArrayList<>();
   BlockPos targetChestPos;
   BlockPos lastOpennedChest;
   boolean hasFlagOnOpen = false;

   public ChestStealer() {
      super("ChestStealer", 0, Module.Category.PLAYER);
      this.settings.add(this.Delay = new FloatSettings("Delay", 70.0F, 500.0F, 0.0F, this));
      this.settings.add(this.SilentWindow = new BoolSettings("SilentWindow", true, this));
      this.settings.add(this.IgnoreCustomItems = new BoolSettings("IgnoreCustomItems", true, this));
      this.settings.add(this.RandomSlots = new BoolSettings("RandomSlots", true, this));
      this.settings.add(this.ChestAura = new BoolSettings("ChestAura", false, this));
      this.settings.add(this.CheckCooldown = new BoolSettings("CheckCooldown", false, this));
      this.RANDOM.setSeed(1234567890L);
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByInt(this.Delay.getInt());
   }

   private boolean itemHasCustom(ItemStack stack) {
      return stack.hasDisplayName();
   }

   private ArrayList<ChestStealer.ItemStackWithSlot> getItemStackListFromContainer(ContainerChest container) {
      this.stacks.clear();
      if (container != null && container.getLowerChestInventory() != null) {
         for (int index = 0; index < container.getLowerChestInventory().getSizeInventory(); index++) {
            if (container.inventorySlots.get(index).getHasStack()) {
               this.stacks.add(new ChestStealer.ItemStackWithSlot(container.inventorySlots.get(index).getStack(), index));
            }
         }
      }

      return this.stacks;
   }

   private boolean hasCustomItemInContainer(ArrayList<ChestStealer.ItemStackWithSlot> itemStacks) {
      return itemStacks.stream().anyMatch(stackWS -> this.itemHasCustom(stackWS.getStack()));
   }

   private boolean hasEmptySlotInInventory() {
      boolean hasAir = false;

      for (int slotNum = 0; slotNum < 36; slotNum++) {
         if (Minecraft.player.inventory.getStackInSlot(slotNum).getItem() instanceof ItemAir) {
            hasAir = true;
         }
      }

      return hasAir;
   }

   private boolean checkCooldown(ItemStack stack) {
      return !this.CheckCooldown.getBool() && Minecraft.player.getCooldownTracker().getCooldown(stack.getItem(), 1.0F) != 0.0F;
   }

   private boolean lootSlotsFromListStacks(
      ArrayList<ChestStealer.ItemStackWithSlot> itemStacks, ContainerChest container, boolean randomSlots, TimerHelper delayController, long delay
   ) {
      if (container != null && container.getLowerChestInventory() != null && !itemStacks.isEmpty()) {
         int currectSlot = randomSlots && itemStacks.size() > 2 ? MathUtils.clamp(this.RANDOM.nextInt(itemStacks.size()), 0, itemStacks.size()) : 0;
         ChestStealer.ItemStackWithSlot currentISWS = itemStacks.get(currectSlot);
         if (itemStacks.get(currectSlot) != null) {
            ItemStack stackInSlot = currentISWS.getStack();
            int slot = currentISWS.getSlot();
            if (delayController.hasReached((double)delay)) {
               if (this.checkCooldown(stackInSlot) || !this.getHasLootAction(stackInSlot, slot, container)) {
                  return false;
               }

               delayController.reset();
               itemStacks.remove(currectSlot);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean getHasLootAction(ItemStack stackIn, int slotIn, ContainerChest chestIn) {
      mc.playerController.windowClick(chestIn.windowId, slotIn, 0, ClickType.QUICK_MOVE, Minecraft.player);
      return true;
   }

   @Override
   public void onUpdate() {
      Container opennedContainer = Minecraft.player.openContainer;
      if (opennedContainer != null && opennedContainer instanceof ContainerChest) {
         ContainerChest chestContainer = (ContainerChest)opennedContainer;
         ArrayList<ItemStackWithSlot> iswsList = this.getItemStackListFromContainer(chestContainer);
         ArrayList iswsListFiltered = (ArrayList)iswsList.stream().filter(isws -> !this.IgnoreCustomItems.getBool() || !this.itemHasCustom(isws.getStack())).collect(Collectors.toList());
         if (!this.timeSeen.hasReached(100.0)) {
            return;
         }
         if (this.hasEmptySlotInInventory() && !iswsListFiltered.isEmpty()) {
            if (this.lootSlotsFromListStacks(iswsListFiltered, chestContainer, this.RandomSlots.getBool(), this.timerLoot, this.Delay.getInt())) {
               this.hasLootProcess = true;
               if (this.SilentWindow.getBool() && ChestStealer.mc.currentScreen instanceof GuiContainer && !(ChestStealer.mc.currentScreen instanceof GuiInventory) && this.hasLootProcess) {
                  ChestStealer.mc.currentScreen = null;
                  mc.setIngameFocus();
               }
            }
         } else if (this.hasLootProcess || this.timeSeen.hasReached(200.0)) {
            this.hasLootProcess = false;
            Minecraft.player.closeScreen();
            ChestStealer.mc.currentScreen = null;
            this.timeSeen.reset();
         }
      } else {
         this.timerLoot.reset();
         this.timeSeen.reset();
      }
   }

   List<BlockPos> allChestPoses(double inRange) {
      return mc.world
         .getLoadedTileEntityList()
         .stream()
         .filter(tile -> tile instanceof TileEntityChest)
         .filter(tile -> Minecraft.player.getDistanceAtEye((double)tile.getX() + 0.5, (double)tile.getY() + 0.5, (double)tile.getZ() + 0.5) < inRange)
         .map(TileEntity::getPos)
         .filter(pos -> !this.opennedChestPoses.stream().anyMatch(blackPos -> blackPos.equals(pos)))
         .filter(
            pos -> {
               RayTraceResult result = mc.world
                  .rayTraceBlocks(
                     Minecraft.player.getPositionEyes(1.0F),
                     new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5),
                     false,
                     true,
                     false
                  );
               return result != null && result.getBlockPos() != null && result.getBlockPos().equals(pos);
            }
         )
         .collect(Collectors.toList());
   }

   void nullateAll() {
      this.lastOpennedChest = null;
      this.hasFlagOnOpen = false;
      this.targetChestPos = null;
      this.opennedChestPoses.clear();
   }

   @Override
   public void onToggled(boolean actived) {
      this.nullateAll();
      super.onToggled(actived);
   }

   @EventTarget
   public void onUpdateEntitySelf(EventPlayerMotionUpdate event) {
      if (this.ChestAura.getBool()) {
         if (Minecraft.player.ticksExisted < 4) {
            this.nullateAll();
            return;
         }

         if (this.lastOpennedChest != null && this.hasFlagOnOpen) {
            for (int i = 0; i < this.opennedChestPoses.size(); i++) {
               if (this.opennedChestPoses.get(i) != null && this.opennedChestPoses.get(i).equals(this.lastOpennedChest)) {
                  this.opennedChestPoses.remove(i);
               }
            }

            this.hasFlagOnOpen = false;
         }

         if (!this.hasEmptySlotInInventory()) {
            return;
         }

         List<BlockPos> targetChests = this.allChestPoses(4.6);
         if (targetChests.isEmpty() || HitAura.get.actived && HitAura.TARGET_ROTS == null) {
            this.targetChestPos = null;
         } else {
            this.targetChestPos = targetChests.get(0);
         }

         if (this.targetChestPos != null && mc.currentScreen == null && !this.hasLootProcess) {
            float[] rotate = RotationUtil.getNeededFacing(new Vec3d(this.targetChestPos).addVector(0.5, 0.5, 0.5), true, Minecraft.player, false);
            event.setYaw(rotate[0]);
            event.setPitch(rotate[1]);
            float prevYaw = Minecraft.player.rotationYaw;
            float prevPitch = Minecraft.player.rotationPitch;
            Minecraft.player.rotationYaw = rotate[0];
            Minecraft.player.rotationPitch = rotate[1];
            Minecraft.player.renderYawOffset = rotate[0];
            Minecraft.player.rotationYawHead = rotate[0];
            Minecraft.player.rotationPitchHead = rotate[1];
            mc.entityRenderer.getMouseOver(mc.getRenderPartialTicks());
            RayTraceResult result = mc.objectMouseOver;
            Minecraft.player.rotationYaw = prevYaw;
            Minecraft.player.rotationPitch = prevPitch;
            mc.entityRenderer.getMouseOver(mc.getRenderPartialTicks());
            if (result != null && result.getBlockPos() != null) {
               BlockPos pos = result.getBlockPos();
               if (pos.equals(this.targetChestPos)) {
                  boolean canClick = result.hitVec != null && result.sideHit != null;
                  if (canClick) {
                     boolean clientIsSneaking = Minecraft.player.isSneaking();
                     if (clientIsSneaking) {
                        mc.getConnection().sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
                     }

                     if (mc.playerController.processRightClickBlock(Minecraft.player, mc.world, pos, result.sideHit, result.hitVec, EnumHand.MAIN_HAND)
                        == EnumActionResult.SUCCESS) {
                        this.opennedChestPoses.add(pos);
                        this.lastOpennedChest = pos;
                        this.targetChestPos = null;
                     }

                     if (clientIsSneaking) {
                        mc.getConnection().sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SNEAKING));
                     }
                  }
               }
            }
         }
      } else {
         this.nullateAll();
      }
   }

   @EventTarget
   public void onReceive(EventReceivePacket event) {
      if (event.getPacket() instanceof SPacketCloseWindow close
         && Minecraft.player.openContainer != null
         && close.windowId == Minecraft.player.openContainer.windowId) {
         this.hasFlagOnOpen = true;
      }
   }

   private class ItemStackWithSlot {
      private final ItemStack stack;
      private final int slot;

      public ItemStackWithSlot(ItemStack stack, int slot) {
         this.stack = stack;
         this.slot = slot;
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public int getSlot() {
         return this.slot;
      }
   }
}
