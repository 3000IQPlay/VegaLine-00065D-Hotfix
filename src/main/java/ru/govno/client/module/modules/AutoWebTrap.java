package ru.govno.client.module.modules;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventCanPlaceBlock;
import ru.govno.client.event.events.EventPlayerMotionUpdate;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.utils.InventoryUtil;
import ru.govno.client.utils.Combat.RotationUtil;
import ru.govno.client.utils.Math.TimerHelper;

public class AutoWebTrap extends Module {
   BoolSettings Rotations;
   BoolSettings LookingCheck;
   BoolSettings Players;
   BoolSettings Mobs;
   BoolSettings IgnoreWalls;
   BoolSettings FatalTrap;
   FloatSettings RangePlace;
   FloatSettings PlaceDelay;
   private final TimerHelper timer = new TimerHelper();
   private static BlockPos posEntity = null;
   private static boolean doPlaceWeb = false;
   private static float Yaw;
   private static float Pitch;

   public AutoWebTrap() {
      super("AutoWebTrap", 0, Module.Category.COMBAT);
      this.settings.add(this.Rotations = new BoolSettings("Rotations", true, this));
      this.settings.add(this.LookingCheck = new BoolSettings("LookingCheck", false, this, () -> this.Rotations.getBool()));
      this.settings.add(this.Players = new BoolSettings("Players", true, this));
      this.settings.add(this.Mobs = new BoolSettings("Mobs", false, this));
      this.settings.add(this.IgnoreWalls = new BoolSettings("IgnoreWalls", true, this));
      this.settings.add(this.RangePlace = new FloatSettings("RangePlace", 5.0F, 8.0F, 3.0F, this));
      this.settings.add(this.PlaceDelay = new FloatSettings("PlaceDelay", 100.0F, 250.0F, 0.0F, this));
      this.settings.add(this.FatalTrap = new BoolSettings("FatalTrap", true, this));
   }

   private final EntityLivingBase currentEntity() {
      EntityLivingBase entity = null;
      if (mc.world != null || mc.world.loadedEntityList != null) {
         boolean players = this.Players.getBool();
         boolean mobs = this.Mobs.getBool();
         boolean walls = this.IgnoreWalls.getBool();
         float range = this.RangePlace.getFloat();
         EntityLivingBase entityStarted = null;

         for (Entity object : mc.world.loadedEntityList) {
            if (object instanceof EntityLivingBase && (entity != null || ((EntityLivingBase)object).getHealth() != 0.0F && !object.isDead)) {
               entityStarted = (EntityLivingBase)object;
            }

            if (entityStarted != null
               && (entityStarted instanceof EntityOtherPlayerMP && players || entityStarted instanceof EntityMob && mobs)
               && (walls || entityStarted.canEntityBeSeen(Minecraft.player))
               && Minecraft.player.getDistanceToEntity(entityStarted) <= range
               && !Client.friendManager.isFriend(entityStarted.getName())
               && !Client.isClientAdmin(entityStarted)
               && this.currentToPlace(entityStarted)) {
               entity = entityStarted;
            }
         }
      }

      return entity;
   }

   private final BlockPos playerPos(EntityLivingBase entity) {
      if (entity != null) {
         double posX = entity.posX;
         double prevX = posX - entity.prevPosX;
         double posY = entity.posY;
         double prevY = posY - entity.prevPosY;
         double posZ = entity.posZ;
         double prevZ = posZ - entity.prevPosZ;
         BlockPos entityInted = new BlockPos(entity.posX, entity.posY + 0.3, entity.posZ);
         return entityInted.add(prevX * 1.5, prevY, prevZ * 1.5);
      } else {
         return null;
      }
   }

   private final boolean currentToPlace(Entity entityIn) {
      return entityIn != null
         && (
            entityIn.isCollidedVertically
               || !this.getBlockWithExpand(
                  0.0F,
                  (double)this.playerPos((EntityLivingBase)entityIn).getX(),
                  (double)this.playerPos((EntityLivingBase)entityIn).getY(),
                  (double)this.playerPos((EntityLivingBase)entityIn).getZ(),
                  Blocks.WEB
               )
         );
   }

   private final int getSlotWebInHotbar() {
      return InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB));
   }

   private final boolean haveWebInOffhand() {
      return Minecraft.player.getHeldItemOffhand().getItem() == Item.getItemFromBlock(Blocks.WEB);
   }

   private final boolean haveWebInInventory() {
      return this.haveWebInOffhand() || this.getSlotWebInHotbar() != -1;
   }

   private final void start_update() {
      posEntity = this.playerPos(this.currentEntity());
   }

   private final void end_update(boolean rotations) {
      posEntity = null;
      if (rotations) {
         Yaw = Minecraft.player.rotationYaw;
         Pitch = Minecraft.player.rotationPitch;
      }
   }

   private final boolean canPlace(BlockPos pos) {
      return pos != null && this.haveWebInInventory();
   }

   private void updateCansPlacingWeb(boolean checkRotate, float range) {
      if (this.canPlace(this.playerPos(this.currentEntity())) && (this.lookingAtPos(Yaw, Pitch, this.playerPos(this.currentEntity()), range) || !checkRotate)) {
         doPlaceWeb = true;
      }
   }

   private final void rotateToPos(EventPlayerMotionUpdate event, BlockPos pos) {
      if (pos != null) {
         float[] rotation = RotationUtil.getMatrixRotations4BlockPos(pos);
         Yaw = RotationUtil.getMatrixRotations4BlockPos(pos.add(0.0, -0.1, 0.0))[0];
         Pitch = RotationUtil.getMatrixRotations4BlockPos(pos.add(0.0, -0.1, 0.0))[1];
         event.setYaw(Yaw);
         event.setPitch(Pitch);
         Minecraft.player.renderYawOffset = event.getYaw();
         Minecraft.player.rotationYawHead = event.getYaw();
         Minecraft.player.rotationPitchHead = event.getPitch();
      }
   }

   @EventTarget
   public void onUpdate(EventPlayerMotionUpdate s) {
      boolean rotate = this.Rotations.getBool();
      boolean check = this.LookingCheck.getBool();
      float range = this.RangePlace.getFloat();
      this.start_update();
      if (rotate) {
         this.rotateToPos(s, posEntity);
      }

      this.updateCansPlacingWeb(rotate && check, range);
   }

   @EventTarget
   public void can(EventCanPlaceBlock event) {
      if (posEntity != null) {
         boolean rotate = this.Rotations.getBool();
         float delay = this.PlaceDelay.getFloat();
         boolean fatal = this.FatalTrap.getBool();
         if (this.timer.hasReached((double)((int)delay)) && doPlaceWeb) {
            this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, posEntity);
            if (fatal) {
               this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, posEntity.add(0, 1, 0));
               this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, posEntity.add(1, 0, 0));
               this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, posEntity.add(-1, 0, 0));
               this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, posEntity.add(0, 0, 1));
               this.placeWeb(this.haveWebInOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, posEntity.add(0, 0, -1));
            }

            this.end_update(rotate);
            this.timer.reset();
            doPlaceWeb = false;
         }
      }
   }

   private final void placeWeb(EnumHand hand, BlockPos currentPosToPlace) {
      if (currentPosToPlace != null) {
         EnumFacing v4 = this.getPlaceableSide(currentPosToPlace);
         if (v4 != null) {
            EnumFacing v2 = v4.getOpposite();
            BlockPos v1 = currentPosToPlace.offset(v4);
            Vec3d v3 = new Vec3d(v1).addVector(0.5, 0.5, 0.5).add(new Vec3d(v2.getDirectionVec()).scale(0.5));
            if (InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB)) != -1
               && hand == EnumHand.MAIN_HAND
               && Minecraft.player.inventory.currentItem != InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB))) {
               Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB))));
            }

            mc.playerController.processRightClickBlock(Minecraft.player, mc.world, v1, v2, v3, hand);
            if (InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB)) != -1
               && hand == EnumHand.MAIN_HAND
               && Minecraft.player.inventory.currentItem != InventoryUtil.getItemInHotbar(Item.getItemFromBlock(Blocks.WEB))) {
               Minecraft.player.connection.sendPacket(new CPacketHeldItemChange(Minecraft.player.inventory.currentItem));
            }
         }
      }
   }

   private final boolean getBlockWithExpand(float expand, double x, double y, double z, Block block) {
      return mc.world.getBlockState(new BlockPos(x, y + 0.03, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z - (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z - (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x + (double)expand, y, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x - (double)expand, y, z)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x, y, z + (double)expand)).getBlock() == block
         || mc.world.getBlockState(new BlockPos(x, y, z - (double)expand)).getBlock() == block;
   }

   private final EnumFacing getPlaceableSide(BlockPos var0) {
      for (EnumFacing v3 : EnumFacing.values()) {
         BlockPos v4 = var0.offset(v3);
         if (mc.world.getBlockState(v4).getBlock().canCollideCheck(mc.world.getBlockState(v4), false)) {
            IBlockState v5 = mc.world.getBlockState(v4);
            if (!v5.getMaterial().isReplaceable()) {
               return v3;
            }
         }
      }

      return null;
   }

   private final boolean lookingAtPos(float yaw, float pitch, BlockPos pos, float range) {
      RayTraceResult zalupa = Minecraft.player.rayTraceCustom((double)range, mc.getRenderPartialTicks(), yaw, pitch);
      Vec3d hitVec = zalupa.hitVec;
      if (hitVec == null) {
         return false;
      } else if (hitVec.xCoord - (double)pos.getX() > 1.0 || hitVec.xCoord - (double)pos.getX() < 0.0) {
         return false;
      } else {
         return !(hitVec.yCoord - (double)pos.getY() > 1.0) && !(hitVec.yCoord - (double)pos.getY() < 0.0)
            ? !(hitVec.zCoord - (double)pos.getZ() > 1.0) && !(hitVec.zCoord - (double)pos.getZ() < 0.0)
            : false;
      }
   }
}
