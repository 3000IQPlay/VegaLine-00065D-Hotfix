package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
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
import ru.govno.client.utils.Math.BlockUtils;
import ru.govno.client.utils.Math.TimerHelper;

public class AntiCrystal
extends Module {
    private final TimerHelper timer = new TimerHelper();
    private final FloatSettings Range;
    private final FloatSettings PlaceDelay;
    private final BoolSettings IgnoreWalls;
    private final BoolSettings UseInventory;
    public static AntiCrystal get;
    private final int rangecheck = 16;
    private final int maxBlocksCache = 8;
    private final List<BlockPos> blackStates = new ArrayList<BlockPos>();
    private BlockPos lastPlacedPos;

    public AntiCrystal() {
        super("AntiCrystal", 0, Module.Category.COMBAT);
        this.Range = new FloatSettings("Range", 4.5f, 5.0f, 2.0f, this);
        this.settings.add(this.Range);
        this.PlaceDelay = new FloatSettings("Delay", 100.0f, 500.0f, 50.0f, this);
        this.settings.add(this.PlaceDelay);
        this.IgnoreWalls = new BoolSettings("IgnoreWalls", true, this);
        this.settings.add(this.IgnoreWalls);
        this.UseInventory = new BoolSettings("UseInventory", false, this);
        this.settings.add(this.UseInventory);
        get = this;
    }

    public boolean isCached(BlockPos pos) {
        return pos != null && !this.blackStates.isEmpty() && this.blackStates.stream().anyMatch(pos2 -> pos2.equals(pos));
    }

    public void addCache(BlockPos pos) {
        if (this.isCached(pos)) {
            return;
        }
        if (this.blackStates.size() >= 8) {
            this.blackStates.remove(0);
        }
        this.blackStates.add(pos);
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
        if (!this.actived) {
            return;
        }
        Packet packet = eventReceive.getPacket();
        if (packet instanceof SPacketBlockChange) {
            SPacketBlockChange changeBlockPacket = (SPacketBlockChange)packet;
            BlockPos pos = changeBlockPacket.getBlockPosition();
            IBlockState state = changeBlockPacket.getBlockState();
            if (Minecraft.player.getDistanceToBlockPos(pos) <= 16.0 && this.canAddPosToCache(state, pos)) {
                this.addCache(pos);
            }
        }
    }

    private boolean canAddPosToCache(IBlockState state, BlockPos pos) {
        IBlockState stateDown = AntiCrystal.mc.world.getBlockState(pos.down());
        Block block = state.getBlock();
        Block blockDown = stateDown.getBlock();
        return BlockUtils.canPlaceBlock(pos) && (AntiCrystal.mc.objectMouseOver == null || pos != AntiCrystal.mc.objectMouseOver.getBlockPos()) && blockDown != Blocks.OBSIDIAN && blockDown != Blocks.BEDROCK;
    }

    private boolean stackIsBlock(ItemStack stack) {
        ItemBlock block;
        Item item;
        return stack != null && (item = stack.getItem()) instanceof ItemBlock && (block = (ItemBlock)item).getBlock().isCollidable();
    }

    private int getBlockSlot(boolean invUse) {
        for (int i = 0; i < (invUse ? 44 : 8); ++i) {
            if (!this.stackIsBlock(Minecraft.player.inventory.getStackInSlot(i))) continue;
            return i;
        }
        return -1;
    }

    private boolean isValidBlockPos(IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if ((double)pos.getY() < Minecraft.player.posY + 1.0 && (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) && BlockUtils.canPlaceBlock(pos.up())) {
            return pos != CrystalField.forCrystalPos && pos != CrystalField.forObsidianPos && (CrystalField.crystal == null || pos != BlockUtils.getEntityBlockPos(CrystalField.crystal).down());
        }
        return false;
    }

    private boolean isValidBlockPos(BlockPos pos) {
        IBlockState state = AntiCrystal.mc.world.getBlockState(pos);
        Block block = state.getBlock();
        if ((double)pos.getY() < Minecraft.player.posY + 1.0 && (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) && BlockUtils.canPlaceBlock(pos.up()) && (this.IgnoreWalls.getBool() || BlockUtils.canPosBeSeenCoord(Minecraft.player.getPositionEyes(1.0f), (double)pos.getY() + 0.5, (double)pos.getY() + 0.75, (double)pos.getZ() + 0.5))) {
            return pos != CrystalField.forCrystalPos && pos != CrystalField.forObsidianPos && (CrystalField.crystal == null || pos != BlockUtils.getEntityBlockPos(CrystalField.crystal).down());
        }
        return false;
    }

    private void rClickPos(BlockPos pos, EnumHand hand) {
        AntiCrystal.mc.playerController.processRightClickBlock(Minecraft.player, AntiCrystal.mc.world, pos, EnumFacing.UP, new Vec3d(pos), hand);
        Minecraft.player.swingArm(hand);
    }

    private void switchForActions(int slotTo, Runnable action) {
        boolean invSwap;
        if (slotTo <= -1) {
            if (slotTo == -2) {
                action.run();
            }
            return;
        }
        boolean bl = invSwap = slotTo > 8;
        if (invSwap) {
            AntiCrystal.mc.playerController.windowClick(0, slotTo, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
            AntiCrystal.mc.playerController.syncCurrentPlayItem();
            action.run();
            if (this.PlaceDelay.getFloat() >= 100.0f) {
                AntiCrystal.mc.playerController.windowClickMemory(0, slotTo, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player, 100);
            } else {
                AntiCrystal.mc.playerController.windowClick(0, slotTo, Minecraft.player.inventory.currentItem, ClickType.SWAP, Minecraft.player);
            }
            return;
        }
        int handSlot = Minecraft.player.inventory.currentItem;
        Minecraft.player.inventory.currentItem = slotTo;
        AntiCrystal.mc.playerController.syncCurrentPlayItem();
        action.run();
        Minecraft.player.inventory.currentItem = handSlot;
        AntiCrystal.mc.playerController.syncCurrentPlayItem();
    }

    @Override
    public String getDisplayName() {
        return this.getDisplayByDouble(this.Range.getFloat());
    }

    @EventTarget
    public void onPlayerMotionUpdate(EventPlayerMotionUpdate event) {
        this.removeCache(this.lastPlacedPos);
        this.lastPlacedPos = null;
        if (!this.timer.hasReached(this.PlaceDelay.getFloat())) {
            return;
        }
        int blockSlot = -999;
        if (this.stackIsBlock(Minecraft.player.getHeldItemOffhand())) {
            blockSlot = -2;
        }
        if (blockSlot != -2 && (blockSlot = this.getBlockSlot(this.UseInventory.getBool())) == -1 || blockSlot == -999) {
            return;
        }
        BlockPos pos = this.getCache().stream().filter(pos2 -> Minecraft.player.getDistanceAtEye((double)pos2.getX() + 0.5, (double)pos2.getY() + 0.5, (double)pos2.getZ() + 0.5) < (double)this.Range.getFloat()).filter(pos2 -> this.isValidBlockPos((BlockPos)pos2)).findAny().orElse(null);
        if (pos == null) {
            return;
        }
        int copyBlockSlot = blockSlot;
        this.switchForActions(blockSlot, () -> {
            this.rClickPos(pos, copyBlockSlot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            this.lastPlacedPos = pos;
            this.timer.reset();
        });
    }
}

