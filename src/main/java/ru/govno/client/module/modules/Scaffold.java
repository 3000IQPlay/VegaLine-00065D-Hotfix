package ru.govno.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventCanPlaceBlock;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.event.events.EventRotationStrafe;
import ru.govno.client.event.events.EventSafeWalk;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.RotationUtils;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Math.TimerHelper;
import ru.govno.client.utils.Movement.MoveMeHelp;

public class Scaffold extends Module {
   public static Scaffold get;
   public FloatSettings Delay;
   public FloatSettings SpeedI;
   public FloatSettings TimerWalk;
   public FloatSettings TimerTower;
   public BoolSettings BlockCount;
   public BoolSettings Sprint;
   public BoolSettings Swing;
   public BoolSettings TowerMotion;
   public BoolSettings TimerUse;
   public BoolSettings SilentSwitch;
   public ModeSettings Sneak;
   public ModeSettings Tower;
   private final List<Block> BLOCK_BLACKLIST = Arrays.asList(
      Blocks.ENCHANTING_TABLE,
      Blocks.CHEST,
      Blocks.ENDER_CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.ANVIL,
      Blocks.SAND,
      Blocks.WEB,
      Blocks.TORCH,
      Blocks.CRAFTING_TABLE,
      Blocks.FURNACE,
      Blocks.WATERLILY,
      Blocks.DISPENSER,
      Blocks.STONE_BRICK_STAIRS,
      Blocks.WOODEN_PRESSURE_PLATE,
      Blocks.NOTEBLOCK,
      Blocks.DROPPER,
      Blocks.TNT,
      Blocks.STANDING_BANNER,
      Blocks.WALL_BANNER,
      Blocks.REDSTONE_TORCH,
      Blocks.TRAPDOOR,
      Blocks.IRON_TRAPDOOR
   );
   Vec3d targetBlock;
   List<Vec3d> placePossibilities = new ArrayList<>();
   TimerHelper delayUtils = new TimerHelper();
   Scaffold.EnumFacingOffset enumFacing;
   BlockPos blockFace;
   float yaw;
   float pitch;
   public static int ticksOnAir;
   public static int oldSlot;
   public static int offGroundTicks;
   public static int blocksPlaced;
   public static int blockCount;
   public static int slotIndex;
   boolean sneaking;
   public static float animationDelta;

   public Scaffold() {
      super("Scaffold", 0, Module.Category.PLAYER);
      this.settings.add(this.Delay = new FloatSettings("Delay", 100.0F, 500.0F, 0.0F, this));
      this.settings.add(this.BlockCount = new BoolSettings("BlockCount", true, this));
      this.settings.add(this.Sprint = new BoolSettings("Sprint", false, this));
      this.settings.add(this.Sneak = new ModeSettings("Sneak", "Packet", this, new String[]{"Packet", "Client", "None"}));
      this.settings.add(this.Swing = new BoolSettings("Swing", false, this));
      this.settings.add(this.TowerMotion = new BoolSettings("TowerMotion", true, this));
      this.settings.add(this.Tower = new ModeSettings("Tower", "Matrix", this, new String[]{"Matrix", "NCP", "Strict"}, () -> this.TowerMotion.getBool()));
      this.settings.add(this.SpeedI = new FloatSettings("Speed", 0.58F, 1.0F, 0.01F, this));
      this.settings.add(this.TimerUse = new BoolSettings("TimerUse", true, this));
      this.settings.add(this.TimerWalk = new FloatSettings("TimerWalk", 1.0F, 2.0F, 0.5F, this, () -> this.TimerUse.getBool()));
      this.settings.add(this.TimerTower = new FloatSettings("TimerTower", 1.8F, 2.0F, 0.5F, this, () -> this.TimerUse.getBool()));
      this.settings.add(this.SilentSwitch = new BoolSettings("SilentSwitch", false, this));
      get = this;
   }

   @Override
   public void onToggled(boolean actived) {
      if (actived) {
         if (Minecraft.player == null || mc.world == null) {
            return;
         }

         oldSlot = Minecraft.player.inventory.currentItem;
         this.delayUtils.reset();
         this.blockFace = new BlockPos(Minecraft.player.posX, Minecraft.player.posY, Minecraft.player.posZ);
         this.yaw = Minecraft.player.rotationYaw;
         this.pitch = Minecraft.player.rotationPitch;
         this.targetBlock = null;
         this.placePossibilities.clear();
      } else {
         blocksPlaced = 0;
         mc.timer.speed = 1.0;
         if (Minecraft.player == null || mc.world == null) {
            return;
         }

         Minecraft.player.inventory.currentItem = oldSlot;
         this.delayUtils.reset();
         this.placePossibilities.clear();
         mc.gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
         if (this.sneaking) {
            this.sneaking = false;
            if (this.Sneak.getMode().equalsIgnoreCase("Packet")) {
               Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            if (this.Sneak.getMode().equalsIgnoreCase("Client")) {
               mc.gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
            }
         }
      }

      super.onToggled(actived);
   }

   @EventTarget
   public void onStrafe(EventRotationStrafe event) {
      if (this.actived) {
         float yaw = Minecraft.player.rotationYaw % 360.0F;
         int ticks = 6;
         int yawing = 6;
         float prevDiff = yaw + (float)(Minecraft.player.ticksExisted % ticks < ticks / 2 ? yawing : -yawing);
         event.setYaw(prevDiff);
      }
   }

   @EventTarget
   public void on(EventSafeWalk eventSafeWalk) {
      eventSafeWalk.setCancelled(true);
   }

   public static float getAnimationState(float animation, float finalState, float speed) {
      float add = (float)((double)animationDelta * (double)speed);
      return animation < finalState
         ? ((double)animation + (double)add < (double)finalState ? animation + add : finalState)
         : ((double)animation - (double)add > (double)finalState ? animation - add : finalState);
   }

   private final EnumFacing getPlaceableSide(BlockPos pos) {
      for (EnumFacing facing : EnumFacing.values()) {
         BlockPos offset = pos.offset(facing);
         if (mc.world.getBlockState(offset).getBlock().canCollideCheck(mc.world.getBlockState(offset), false)) {
            IBlockState state = mc.world.getBlockState(offset);
            if (!state.getMaterial().isReplaceable()) {
               return facing;
            }
         }
      }

      return null;
   }

   private void placeAction() {
      BlockPos toPlacePos = new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ);
      EnumFacing v4 = this.getPlaceableSide(toPlacePos);
      if (v4 == null) {
         v4 = EnumFacing.UP;
      }

      EnumFacing v2 = v4.getOpposite();
      BlockPos v1 = toPlacePos.offset(v4);
      Vec3d v3 = new Vec3d(v1).addVector(0.5, 0.5, 0.5).add(new Vec3d(v2.getDirectionVec()).scale(0.5));
      mc.playerController.processRightClickBlock(Minecraft.player, mc.world, v1, v2, v3, EnumHand.MAIN_HAND);
   }

   @Override
   public void onUpdate() {
      boolean isTowered = this.TowerMotion.getBool() && Minecraft.player.isJumping() && blockCount > 0;
      if (this.TimerUse.getBool()) {
         mc.timer.speed = isTowered ? (double)this.TimerTower.getFloat() : (double)this.TimerWalk.getFloat();
         Timer.forceTimer((float)mc.timer.speed);
      }

      if (!isTowered && !this.sneaking) {
         Minecraft.player.multiplyMotionXZ(MathUtils.clamp(this.SpeedI.getFloat(), 0.0F, 1.0F));
      }

      if (this.TowerMotion.getBool() && blockCount > 0) {
         if (this.Tower.getMode().equalsIgnoreCase("Matrix") && Minecraft.player.isJumping() && !this.delayUtils.hasReached(10.0)) {
            Minecraft.player.jumpTicks = 0;
            double x = Minecraft.player.posX;
            double y = Minecraft.player.posY - 1.0;
            double z = Minecraft.player.posZ;
            if (mc.world.getBlockState(new BlockPos(x, y - 0.01, z)).getBlock() != Blocks.AIR) {
               Minecraft.player.onGround = true;
            }
         }

         if (this.Tower.getMode().equalsIgnoreCase("Strict") && Minecraft.player.isJumping() && !MoveMeHelp.moveKeysPressed() && Minecraft.player.onGround) {
            Minecraft.player.motionX = 0.0;
            Minecraft.player.motionZ = 0.0;
            MoveMeHelp.setCuttingSpeed(0.0);
            Minecraft.player
               .connection
               .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 0.41999998688698, Minecraft.player.posZ, false));
            Minecraft.player
               .connection
               .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 0.7531999805211997, Minecraft.player.posZ, false));
            Minecraft.player
               .connection
               .sendPacket(new CPacketPlayer.Position(Minecraft.player.posX, Minecraft.player.posY + 1.00133597911214, Minecraft.player.posZ, false));
            Minecraft.player.setPosY(Minecraft.player.posY + 1.0);
            if (slotIndex != -1) {
               int oldSwitch = Minecraft.player.inventory.currentItem;
               if (this.SilentSwitch.getBool()) {
                  Minecraft.player.inventory.currentItem = slotIndex;
               } else {
                  Minecraft.player.inventory.currentItem = slotIndex;
               }

               this.placeAction();
               if (this.SilentSwitch.getBool()) {
                  Minecraft.player.inventory.currentItem = oldSwitch;
               }

               if (this.Swing.getBool()) {
                  Minecraft.player.swingArm(EnumHand.MAIN_HAND);
               } else {
                  Minecraft.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
               }

               GuiIngame.trottleScaff = (float)((double)GuiIngame.trottleScaff + 0.3);
               blocksPlaced++;
            }

            Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
         }

         if (this.Tower.getMode().equalsIgnoreCase("NCP") && mc.gameSettings.keyBindJump.isKeyDown()) {
            if (Minecraft.player.onGround) {
               Minecraft.player.motionY = 0.48;
            } else {
               Minecraft.player.motionY *= 1.016;
            }

            if (Minecraft.player.motionY < 0.16) {
               Minecraft.player.motionY -= 10.0;
            }
         }
      }

      if (this.enumFacing != null && this.blockFace != null) {
         BlockPos pos = this.blockFace;
         double x = (double)pos.getX() + 0.5;
         double y = (double)pos.getY() - 0.08;
         double z = (double)pos.getZ() + 0.5;
         y -= 2.0;
         float[] rotate = RotationUtils.setRotationsToVec3d(new Vec3d(x, y, z));
         this.yaw = rotate[0] + (float)MathUtils.getRandomInRange(-2, -2);
         this.pitch = rotate[1] + (float)MathUtils.getRandomInRange(-1, -1);
         float mouseDelta = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
         float matrixFixed = mouseDelta * mouseDelta * mouseDelta * 1.2F;
         this.yaw = this.yaw - this.yaw % matrixFixed;
         this.pitch = this.pitch - this.pitch % matrixFixed;
      }
   }

   @EventTarget
   public void onPlayerMotionUpdate(EventPlayerMotionUpdate e) {
      e.setYaw(this.yaw);
      e.setPitch(this.pitch);
      if (this.getBlockRelativeToPlayer(0.0, -1.0, 0.0) instanceof BlockAir) {
         ticksOnAir++;
      } else {
         ticksOnAir = 0;
      }

      if (Minecraft.player.onGround) {
         offGroundTicks = 0;
      } else {
         offGroundTicks++;
      }

      int blocks = 0;

      for (int i = 36; i < 45; i++) {
         ItemStack itemStack = Minecraft.player.inventoryContainer.getSlot(i).getStack();
         if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
            Block block = ((ItemBlock)itemStack.getItem()).getBlock();
            if (!this.BLOCK_BLACKLIST.contains(block)) {
               blocks += itemStack.stackSize;
            }
         }
      }

      blockCount = blocks;
      this.placePossibilities = this.getPlacePossibilities();
      if (!this.placePossibilities.isEmpty()) {
         this.placePossibilities.sort(Comparator.comparingDouble(vec3 -> Minecraft.player.getDistance(vec3.xCoord, vec3.yCoord + 1.0, vec3.zCoord)));
         this.targetBlock = this.placePossibilities.get(0);
         this.enumFacing = this.getEnumFacing();
         if (this.enumFacing != null) {
            BlockPos position = new BlockPos(this.targetBlock.xCoord, this.targetBlock.yCoord, this.targetBlock.zCoord);
            this.blockFace = position.add(this.enumFacing.getOffset().xCoord, this.enumFacing.getOffset().yCoord, this.enumFacing.getOffset().zCoord);
            if (!(
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)).getBlock() instanceof BlockAir
               )
               && !(
                  mc.world.getBlockState(new BlockPos(Minecraft.player.posX, Minecraft.player.posY - 1.0, Minecraft.player.posZ)).getBlock() instanceof BlockLiquid
               )) {
               if (this.sneaking) {
                  if (!this.Sneak.getMode().equalsIgnoreCase("None")) {
                     this.sneaking = false;
                  }

                  if (this.Sneak.getMode().equalsIgnoreCase("Packet")) {
                     Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING));
                  }

                  if (this.Sneak.getMode().equalsIgnoreCase("Client")) {
                     mc.gameSettings.keyBindSneak.pressed = false;
                  }
               }
            } else if (!this.sneaking) {
               if (!this.Sneak.getMode().equalsIgnoreCase("None")) {
                  this.sneaking = true;
               }

               if (this.Sneak.getMode().equalsIgnoreCase("Packet")) {
                  Minecraft.player.connection.sendPacket(new CPacketEntityAction(Minecraft.player, CPacketEntityAction.Action.START_SNEAKING));
               }

               if (this.Sneak.getMode().equalsIgnoreCase("Client")) {
                  mc.gameSettings.keyBindSneak.pressed = true;
               }
            }

            if (!this.Sprint.getBool()) {
               mc.gameSettings.keyBindSprint.pressed = false;
               Minecraft.player.setSprinting(false);
            }

            e.setYaw(this.yaw);
            e.setPitch(this.pitch);
            Minecraft.player.rotationYawHead = Minecraft.player.renderYawOffset = this.yaw;
            Minecraft.player.rotationPitchHead = this.pitch;
            if (!this.placePossibilities.isEmpty() && this.targetBlock != null && this.enumFacing != null && this.blockFace != null) {
               if (mc.gameSettings.keyBindJump.isKeyDown()
                  && Minecraft.player.onGround
                  && !(this.getBlockRelativeToPlayer(0.0, -1.0, 0.0) instanceof BlockAir)
                  && !(this.getBlockRelativeToPlayer(0.0, -1.0, 0.0) instanceof BlockLiquid)) {
                  Minecraft.player.jump();
               }
            }
         }
      }
   }

   @EventTarget
   public void can(EventCanPlaceBlock event) {
      if (!this.Tower.getMode().equalsIgnoreCase("Strict") || !Minecraft.player.isJumping() || MoveMeHelp.moveKeysPressed()) {
         this.placeBlock();
      }
   }

   private void placeBlock() {
      if (!this.placePossibilities.isEmpty() && this.targetBlock != null && this.enumFacing != null && this.blockFace != null) {
         RayTraceResult movingObjectPosition = Minecraft.player
            .rayTraceCustom((double)mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks(), this.yaw, this.pitch);
         Vec3d hitVec = movingObjectPosition.hitVec;
         slotIndex = -1;

         for (int i = 0; i < 9; i++) {
            ItemStack itemStack = Minecraft.player.inventory.getStackInSlot(i);
            Item block = itemStack.getItem();
            if (block instanceof ItemBlock) {
               ItemBlock iBlock = (ItemBlock)block;
               Block blockx = iBlock.getBlock();
               if (!this.BLOCK_BLACKLIST.contains(blockx)) {
                  slotIndex = i;
               }
            }
         }

         boolean isTowered = this.TowerMotion.getBool() && Minecraft.player.isJumping() && blockCount > 0;
         if ((offGroundTicks == 0 || (Minecraft.player.fallDistance > 0.0F || isTowered) && offGroundTicks <= 3 || offGroundTicks > 5)
            && ticksOnAir > 0
            && this.lookingAtBlock()) {
            if (!this.lookingAtBlock()) {
               hitVec.yCoord = (double)this.blockFace.getY();
               hitVec.zCoord = (double)this.blockFace.getZ();
               hitVec.xCoord = (double)this.blockFace.getX();
            }

            if (this.delayUtils.hasReached(isTowered ? 0.0 : (double)((int)this.Delay.getFloat()))) {
               if (slotIndex != -1) {
                  int oldSwitch = Minecraft.player.inventory.currentItem;
                  if (this.SilentSwitch.getBool()) {
                     Minecraft.player.inventory.currentItem = slotIndex;
                  } else {
                     Minecraft.player.inventory.currentItem = slotIndex;
                  }

                  mc.playerController
                     .processRightClickBlock(Minecraft.player, mc.world, this.blockFace, this.enumFacing.getEnumFacing(), hitVec, EnumHand.MAIN_HAND);
                  if (this.SilentSwitch.getBool()) {
                     Minecraft.player.inventory.currentItem = oldSwitch;
                  }

                  if (this.Swing.getBool()) {
                     Minecraft.player.swingArm(EnumHand.MAIN_HAND);
                  } else {
                     Minecraft.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                  }

                  GuiIngame.trottleScaff = (float)((double)GuiIngame.trottleScaff + 0.3);
                  blocksPlaced++;
               }

               this.delayUtils.reset();
            }
         }
      }
   }

   Scaffold.EnumFacingOffset getEnumFacing() {
      for (int x2 = -1; x2 <= 1; x2 += 2) {
         if (!(
               mc.world.getBlockState(new BlockPos(this.targetBlock.xCoord + (double)x2, this.targetBlock.yCoord, this.targetBlock.zCoord)).getBlock() instanceof BlockAir
            )
            && !(
               mc.world.getBlockState(new BlockPos(this.targetBlock.xCoord + (double)x2, this.targetBlock.yCoord, this.targetBlock.zCoord)).getBlock() instanceof BlockLiquid
            )) {
            if (x2 > 0) {
               return new Scaffold.EnumFacingOffset(EnumFacing.WEST, new Vec3d((double)x2, 0.0, 0.0));
            }

            return new Scaffold.EnumFacingOffset(EnumFacing.EAST, new Vec3d((double)x2, 0.0, 0.0));
         }
      }

      for (int y2 = -1; y2 <= 1; y2 += 2) {
         if (!(
               mc.world.getBlockState(new BlockPos(this.targetBlock.xCoord, this.targetBlock.yCoord + (double)y2, this.targetBlock.zCoord)).getBlock() instanceof BlockAir
            )
            && !(
               mc.world.getBlockState(new BlockPos(this.targetBlock.xCoord, this.targetBlock.yCoord + (double)y2, this.targetBlock.zCoord)).getBlock() instanceof BlockLiquid
            )
            && y2 < 0) {
            return new Scaffold.EnumFacingOffset(EnumFacing.UP, new Vec3d(0.0, (double)y2, 0.0));
         }
      }

      for (int z2 = -1; z2 <= 1; z2 += 2) {
         if (!(
               mc.world.getBlockState(new BlockPos(this.targetBlock.xCoord, this.targetBlock.yCoord, this.targetBlock.zCoord + (double)z2)).getBlock() instanceof BlockAir
            )
            && !(
               mc.world.getBlockState(new BlockPos(this.targetBlock.xCoord, this.targetBlock.yCoord, this.targetBlock.zCoord + (double)z2)).getBlock() instanceof BlockLiquid
            )) {
            if (z2 < 0) {
               return new Scaffold.EnumFacingOffset(EnumFacing.SOUTH, new Vec3d(0.0, 0.0, (double)z2));
            }

            return new Scaffold.EnumFacingOffset(EnumFacing.NORTH, new Vec3d(0.0, 0.0, (double)z2));
         }
      }

      return null;
   }

   List<Vec3d> getPlacePossibilities() {
      List<Vec3d> possibilities = new ArrayList<>();
      int range = (int)Math.ceil(6.0);

      for (int x = -range; x <= range; x++) {
         for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
               Block block = this.getBlockRelativeToPlayer((double)x, (double)y, (double)z);
               if (!(block instanceof BlockAir)) {
                  for (int x2 = -1; x2 <= 1; x2 += 2) {
                     possibilities.add(
                        new Vec3d(Minecraft.player.posX + (double)x + (double)x2, Minecraft.player.posY + (double)y, Minecraft.player.posZ + (double)z)
                     );
                  }

                  for (int y2 = -1; y2 <= 1; y2 += 2) {
                     possibilities.add(
                        new Vec3d(Minecraft.player.posX + (double)x, Minecraft.player.posY + (double)y + (double)y2, Minecraft.player.posZ + (double)z)
                     );
                  }

                  for (int z2 = -1; z2 <= 1; z2 += 2) {
                     possibilities.add(
                        new Vec3d(Minecraft.player.posX + (double)x, Minecraft.player.posY + (double)y, Minecraft.player.posZ + (double)z + (double)z2)
                     );
                  }
               }
            }
         }
      }

      return possibilities;
   }

   boolean lookingAtBlock() {
      RayTraceResult movingObjectPosition = Minecraft.player
         .rayTraceCustom((double)mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks(), this.yaw, this.pitch);
      Vec3d hitVec = movingObjectPosition.hitVec;
      if (hitVec == null) {
         return false;
      } else if (hitVec.xCoord - (double)this.blockFace.getX() > 1.0 || hitVec.xCoord - (double)this.blockFace.getX() < 0.0) {
         return false;
      } else {
         return !(hitVec.yCoord - (double)this.blockFace.getY() > 1.0) && !(hitVec.yCoord - (double)this.blockFace.getY() < 0.0)
            ? !(hitVec.zCoord - (double)this.blockFace.getZ() > 1.0) && !(hitVec.zCoord - (double)this.blockFace.getZ() < 0.0)
            : false;
      }
   }

   Block getBlockRelativeToPlayer(double offsetX, double offsetY, double offsetZ) {
      return mc.world.getBlockState(new BlockPos(Minecraft.player.posX + offsetX, Minecraft.player.posY + offsetY, Minecraft.player.posZ + offsetZ)).getBlock();
   }

   public class EnumFacingOffset {
      public EnumFacing enumFacing;
      private final Vec3d offset;

      public EnumFacingOffset(EnumFacing enumFacing, Vec3d offset) {
         this.enumFacing = enumFacing;
         this.offset = offset;
      }

      public EnumFacing getEnumFacing() {
         return this.enumFacing;
      }

      public Vec3d getOffset() {
         return this.offset;
      }
   }
}
