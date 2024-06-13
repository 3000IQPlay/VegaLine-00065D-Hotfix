package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.CrystalField;
import ru.govno.client.utils.TimerHelper;
import ru.govno.client.utils.Math.BlockUtils;

public class AntiCrystal extends Module {
   private final TimerHelper timer = new TimerHelper();
   private final FloatSettings Range;
   private final FloatSettings PlaceDelay;
   private final BoolSettings IgnoreWalls;
   private final BoolSettings UseInventory;
   public static AntiCrystal get;
   private final int rangecheck = 16;
   private final int maxBlocksCache = 8;
   private final List<BlockPos> blackStates = new ArrayList<>();
   private BlockPos lastPlacedPos;

   public AntiCrystal() {
      super("AntiCrystal", 0, Module.Category.COMBAT);
      this.settings.add(this.Range = new FloatSettings("Range", 4.5F, 5.0F, 2.0F, this));
      this.settings.add(this.PlaceDelay = new FloatSettings("Delay", 100.0F, 500.0F, 50.0F, this));
      this.settings.add(this.IgnoreWalls = new BoolSettings("IgnoreWalls", true, this));
      this.settings.add(this.UseInventory = new BoolSettings("UseInventory", false, this));
      get = this;
   }

   public boolean isCached(BlockPos pos) {
      return pos != null && !this.blackStates.isEmpty() && this.blackStates.stream().anyMatch(pos2 -> pos2.equals(pos));
   }

   public void addCache(BlockPos pos) {
      if (!this.isCached(pos)) {
         if (this.blackStates.size() >= 8) {
            this.blackStates.remove(0);
         }

         this.blackStates.add(pos);
      }
   }

   public void removeCache(BlockPos pos) {
      if (this.isCached(pos)) {
         this.blackStates.removeIf(pos2 -> pos2.equals(pos));
      }
   }

   public List<BlockPos> getCache() {
      return this.blackStates;
   }

   @EventTarget
   public void onReceivePackets(EventReceivePacket eventReceive) {
      if (this.actived) {
         if (eventReceive.getPacket() instanceof SPacketBlockChange changeBlockPacket) {
            BlockPos pos = changeBlockPacket.getBlockPosition();
            IBlockState state = changeBlockPacket.getBlockState();
            if (Minecraft.player.getDistanceToBlockPos(pos) <= 16.0 && this.canAddPosToCache(state, pos)) {
               this.addCache(pos);
            }
         }
      }
   }

   private boolean canAddPosToCache(IBlockState state, BlockPos pos) {
      IBlockState stateDown = mc.world.getBlockState(pos.down());
      Block block = state.getBlock();
      Block blockDown = stateDown.getBlock();
      return BlockUtils.canPlaceBlock(pos)
         && (mc.objectMouseOver == null || pos != mc.objectMouseOver.getBlockPos())
         && blockDown != Blocks.OBSIDIAN
         && blockDown != Blocks.BEDROCK;
   }

   private boolean stackIsBlock(ItemStack stack) {
      return stack != null && stack.getItem() instanceof ItemBlock block && block.getBlock().isCollidable();
   }

   private int getBlockSlot(boolean invUse) {
      for (int i = 0; i < (invUse ? 44 : 8); i++) {
         if (this.stackIsBlock(Minecraft.player.inventory.getStackInSlot(i))) {
            return i;
         }
      }

      return -1;
   }

   private boolean isValidBlockPos(IBlockState state, BlockPos pos) {
      Block block = state.getBlock();
      return (double)pos.getY() < Minecraft.player.posY + 1.0 && (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) && BlockUtils.canPlaceBlock(pos.up())
         ? pos != CrystalField.forCrystalPos
            && pos != CrystalField.forObsidianPos
            && (CrystalField.crystal == null || pos != BlockUtils.getEntityBlockPos(CrystalField.crystal).down())
         : false;
   }

   private boolean isValidBlockPos(BlockPos pos) {
      IBlockState state = mc.world.getBlockState(pos);
      Block block = state.getBlock();
      return (double)pos.getY() < Minecraft.player.posY + 1.0
            && (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK)
            && BlockUtils.canPlaceBlock(pos.up())
            && (
               this.IgnoreWalls.getBool()
                  || BlockUtils.canPosBeSeenCoord(
                     Minecraft.player.getPositionEyes(1.0F), (double)pos.getY() + 0.5, (double)pos.getY() + 0.75, (double)pos.getZ() + 0.5
                  )
            )
         ? pos != CrystalField.forCrystalPos
            && pos != CrystalField.forObsidianPos
            && (CrystalField.crystal == null || pos != BlockUtils.getEntityBlockPos(CrystalField.crystal).down())
         : false;
   }

   private void rClickPos(BlockPos pos, EnumHand hand) {
      mc.playerController.processRightClickBlock(Minecraft.player, mc.world, pos, EnumFacing.UP, new Vec3d(pos), hand);
      Minecraft.player.swingArm(hand);
   }

   private void switchForActions(int slotTo, Runnable action) {
      if (slotTo <= -1) {
         if (slotTo == -2) {
            action.run();
         }
      } else {
         boolean invSwap = slotTo > 8;
         if (invSwap) {
            mc.playerController.windowClick(0, slotTo, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
            mc.playerController.syncCurrentPlayItem();
            action.run();
            if (this.PlaceDelay.getFloat() >= 100.0F) {
               mc.playerController.windowClickMemory(0, slotTo, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player, 100);
            } else {
               mc.playerController.windowClick(0, slotTo, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
            }
         } else {
            int handSlot = Minecraft.player.inventory.currentItem;
            Minecraft.player.inventory.currentItem = slotTo;
            mc.playerController.syncCurrentPlayItem();
            action.run();
            Minecraft.player.inventory.currentItem = handSlot;
            mc.playerController.syncCurrentPlayItem();
         }
      }
   }

   @Override
   public String getDisplayName() {
      return this.getDisplayByDouble((double)this.Range.getFloat());
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate event) {
      this.removeCache(this.lastPlacedPos);
      this.lastPlacedPos = null;
      if (this.timer.hasReached(this.PlaceDelay.getFloat())) {
         int blockSlot = -999;
         if (this.stackIsBlock(Minecraft.player.getHeldItemOffhand())) {
            blockSlot = -2;
         }

         if ((blockSlot == -2 || (blockSlot = this.getBlockSlot(this.UseInventory.getBool())) != -1) && blockSlot != -999) {
            BlockPos pos = this.getCache()
               .stream()
               .filter(
                  pos2 -> Minecraft.player.getDistanceAtEye((double)pos2.getX() + 0.5, (double)pos2.getY() + 0.5, (double)pos2.getZ() + 0.5)
                        < (double)this.Range.getFloat()
               )
               .filter(pos2 -> this.isValidBlockPos(pos2))
               .findAny()
               .orElse(null);
            if (pos != null) {
               int copyBlockSlot = blockSlot;
               this.switchForActions(blockSlot, () -> {
                  this.rClickPos(pos, copyBlockSlot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                  this.lastPlacedPos = pos;
                  this.timer.reset();
               });
            }
         }
      }
   }
}
